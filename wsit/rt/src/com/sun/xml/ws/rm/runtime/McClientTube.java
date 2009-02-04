/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.xml.ws.rm.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rm.RxRuntimeException;
import com.sun.xml.ws.rm.protocol.wsmc200702.MakeConnection;
import com.sun.xml.ws.rm.runtime.McClientTube.SuspendedFiberStorage;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class McClientTube extends AbstractFilterTubeImpl {

    static enum SuspendedFiberStorage {
        INSTANCE;
        //
        private static class FiberReqistration {
            final long timestamp;
            final Fiber fiber;

            public FiberReqistration(Fiber fiber) {
                this.timestamp = System.currentTimeMillis();
                this.fiber = fiber;
            }
        }
        //
        private final PriorityQueue<Long> timestampQueue = new PriorityQueue<Long>();
        private final Map<Object, FiberReqistration> idToFiberMap = new HashMap<Object, FiberReqistration>();
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

        void register(@NotNull Object correlationId, @NotNull Fiber fiber) {
            try {
                FiberReqistration fr = new FiberReqistration(fiber);
                rwLock.writeLock().lock();
                idToFiberMap.put(correlationId, fr);
                timestampQueue.offer(fr.timestamp);
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        Fiber remove(@NotNull Object correlationId) {
            try {
                rwLock.writeLock().lock();
                FiberReqistration fr = idToFiberMap.remove(correlationId);
                if (fr != null) {
                    timestampQueue.remove(fr.timestamp);
                    return fr.fiber;
                }

                return null;
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        boolean isEmpty() {
            try {
                rwLock.readLock().lock();
                return idToFiberMap.isEmpty();
            } finally {
                rwLock.readLock().unlock();
            }
        }

        long getOldestRegistrationTimestamp() {
            try {
                rwLock.readLock().lock();
                return timestampQueue.peek();
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }

    static class MakeConnectionSenderTask implements Runnable {

        private final RxConfiguration configuration;
        private final FiberExecutor fiberExecutor;
        private final String wsmcAnonymousAddress;
        private final EndpointAddress endpointAddress;
        private long lastMcMessageTimestamp;

        MakeConnectionSenderTask(final EndpointAddress endpointAddress, final String wsmcAnonymousAddress, final RxConfiguration configuration, final FiberExecutor fiberExecutor) {
            this.configuration = configuration;
            this.fiberExecutor = fiberExecutor;
            this.wsmcAnonymousAddress = wsmcAnonymousAddress;
            this.endpointAddress = endpointAddress;

            this.lastMcMessageTimestamp = System.currentTimeMillis();
        }

        public synchronized void run() {
            if (suspendedFibersReadyForResend() && resendMakeConnectionIntervalPassed()) {
                sendMakeConnectionMessageNow();
            }
        }

        private boolean suspendedFibersReadyForResend() {
            // TODO P2 make configurable
            return !SuspendedFiberStorage.INSTANCE.isEmpty() &&
                    System.currentTimeMillis() - SuspendedFiberStorage.INSTANCE.getOldestRegistrationTimestamp() > 2000;
        }

        private synchronized boolean resendMakeConnectionIntervalPassed() {
            // TODO P2 make configurable
            return System.currentTimeMillis() - lastMcMessageTimestamp > 2000;
        }

        synchronized void sendMakeConnectionMessageNow() {
            Packet mcRequest = createRequestPacket(
                    this.endpointAddress,
                    configuration.getMcVersion().getJaxbContext(configuration.getAddressingVersion()),
                    new MakeConnection(wsmcAnonymousAddress),
                    configuration.getMcVersion().wsmcAction,
                    configuration.getSoapVersion(),
                    configuration.getAddressingVersion(),
                    true
                    );

            fiberExecutor.start(mcRequest, new WsMcResponseHandler(configuration, this));

            lastMcMessageTimestamp = System.currentTimeMillis();
        }

        private static final Packet createRequestPacket(
                EndpointAddress endpointAddress,
                JAXBRIContext jaxbContext,
                Object jaxbElement,
                String wsaAction,
                SOAPVersion soapVersion,
                AddressingVersion addressingVersion,
                boolean expectReply) {
            // TODO P3 merge with PacketAdapter
            Message message = Messages.create(jaxbContext, jaxbElement, soapVersion);
            Packet packet = new Packet(message);
            packet.endpointAddress = endpointAddress;
            packet.expectReply = expectReply;
            message.getHeaders().fillRequestAddressingHeaders(
                    packet,
                    addressingVersion,
                    soapVersion,
                    false,
                    wsaAction);

            return packet;
        }
    }
    //
    private static final Logger LOGGER = Logger.getLogger(McClientTube.class);
    //
    private final RxConfiguration configuration;
    private final Unmarshaller unmarshaller;
    private final Header wsmcAnnonymousReplyToHeader;
    private final FiberExecutor fiberExecutor;
    private final ScheduledTaskManager scheduler;
    private final MakeConnectionSenderTask mcSenderTask;

    McClientTube(McClientTube original, TubeCloner cloner) {
        super(original, cloner);

        this.configuration = original.configuration;

        this.unmarshaller = configuration.getMcVersion().getUnmarshaller(configuration.getAddressingVersion());

        this.wsmcAnnonymousReplyToHeader = original.wsmcAnnonymousReplyToHeader;
        this.fiberExecutor = original.fiberExecutor;
        this.scheduler = original.scheduler;
        this.mcSenderTask = original.mcSenderTask;
    }

    McClientTube(RxConfiguration configuration, Tube tubelineHead, EndpointAddress endpointAddress) throws RxRuntimeException {
        super(tubelineHead);

        this.configuration = configuration;

        this.unmarshaller = configuration.getMcVersion().getUnmarshaller(configuration.getAddressingVersion());
        this.fiberExecutor = new FiberExecutor("MakeConnectionClient", tubelineHead);

        final String wsmcAnonymousAddress = configuration.getMcVersion().getWsmcAnonymousAddress(UUID.randomUUID().toString());
        final WSEndpointReference wsmcAnnonymousEpr = new WSEndpointReference(wsmcAnonymousAddress, configuration.getAddressingVersion());
        this.wsmcAnnonymousReplyToHeader = wsmcAnnonymousEpr.createHeader(configuration.getAddressingVersion().replyToTag);

        this.mcSenderTask = new MakeConnectionSenderTask(endpointAddress, wsmcAnonymousAddress, configuration, this.fiberExecutor);
        this.scheduler = new ScheduledTaskManager();
        // TODO P2 make it configurable
        this.scheduler.startTask(mcSenderTask, 2000, 500);
    }

    @Override
    public AbstractTubeImpl copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new McClientTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processRequest(Packet request) {
        final Message message = request.getMessage();

        if (!message.hasHeaders()) {
            // TODO L10N
            throw LOGGER.logSevereException(new RxRuntimeException("Required WS-Addressing headers not found: No SOAP headers present on a client request message."));
        }


        String correlationId = message.getID(configuration.getAddressingVersion(), configuration.getSoapVersion());

        Fiber.CompletionCallback responseHandler;
        if (hasAnnonymousReplyToHeader(message.getHeaders())) { // most likely Req-Resp MEP
            setMcAnnonymousReplyToHeader(message.getHeaders());
            responseHandler = new RequestResponseMepHandler(configuration, mcSenderTask, correlationId);
        } else { // most likely One-Way MEP
            responseHandler = new OneWayMepHandler(configuration, mcSenderTask, correlationId);
        }

        SuspendedFiberStorage.INSTANCE.register(correlationId, Fiber.current());
        fiberExecutor.start(request, responseHandler);

        return super.doSuspend();
    }

    @Override
    public NextAction processResponse(Packet response) {
        return super.processResponse(response);
    }

    @Override
    public NextAction processException(Throwable t) {
        return super.processException(t);
    }

    @Override
    public void preDestroy() {
        this.scheduler.stopAll();

        super.preDestroy();
    }

    private boolean hasAnnonymousReplyToHeader(final HeaderList headers) {
        Header replyToHeader = headers.get(configuration.getAddressingVersion().replyToTag, false);
        if (replyToHeader != null) {
            try {
                return replyToHeader.readAsEPR(configuration.getAddressingVersion()).isAnonymous();
            } catch (XMLStreamException ex) {
                // TODO L10N
                throw LOGGER.logSevereException(new RxRuntimeException("Error unmarshalling content of WS-A ReplyTo header", ex));
            }
        }

        return false;
    }

    private void setMcAnnonymousReplyToHeader(final HeaderList headers) {
        headers.remove(configuration.getAddressingVersion().replyToTag);
        headers.add(wsmcAnnonymousReplyToHeader);
    }
}