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
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.assembler.ClientTubelineAssemblyContext;
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rm.RxRuntimeException;
import com.sun.xml.ws.rm.protocol.wsmc200702.MessagePending;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class McClientTube extends AbstractFilterTubeImpl {

    private static class FiberExecutionResultHandler implements Fiber.CompletionCallback {

        private final Fiber suspendedParentFiber;

        FiberExecutionResultHandler(@NotNull Fiber suspendedParentFiber) {
            assert suspendedParentFiber != null;

            this.suspendedParentFiber = suspendedParentFiber;
        }

        public void onCompletion(Packet response) {
            /*
             * TODO process response:
             *
             * allways req-response scenario => null or empty response is not expected
             *   - if message is null or empty (maybe response is not available on the server), we may need to send MC message later
             *     to retrieve the response; leave this to automatic MC sender task
             *
             * 1. if message is null or empty,
             *      a. log fine
             * 2. else
             *      a. find original fiber by relatesTo message id
             *      b. resume the fiber
             * 3. process WS-MC header - will be done as part of processResponse() on the original fiber
             */

            if (response.getMessage() == null) { // TODO check for empty
                LOGGER.fine("Unexpected null message"); // TODO L10N
            }

            suspendedParentFiber.resume(response);
        }

        public void onCompletion(Throwable error) {
             // TODO resume original fiber with throwable
        }
    }

    private static enum SuspendedFiberStorage {

        INSTANCE;
        //
        private final Map<Object, Fiber> msgIdToFiberMap = new HashMap<Object, Fiber>();
        private final ReadWriteLock mapRwLock = new ReentrantReadWriteLock();

        void register(Object correlationId, Fiber fiber) {
            try {
                mapRwLock.writeLock().lock();
                msgIdToFiberMap.put(correlationId, fiber);
            } finally {
                mapRwLock.writeLock().unlock();
            }
        }

        Fiber remove(Object correlationId) {
            try {
                mapRwLock.writeLock().lock();
                return msgIdToFiberMap.remove(correlationId);
            } finally {
                mapRwLock.writeLock().unlock();
            }
        }

        boolean isEmpty() {
            try {
                mapRwLock.readLock().lock();
                return msgIdToFiberMap.isEmpty();
            } finally {
                mapRwLock.readLock().unlock();
            }
        }
    }

    private static class MakeConnectionSenderTask implements Runnable {

        private long lastMcMessageTimestamp;
        private final String wsmcClientIdentifier;

        MakeConnectionSenderTask(final String clintIdentifier) {
            this.lastMcMessageTimestamp = System.currentTimeMillis();
            this.wsmcClientIdentifier = clintIdentifier;
        }

        public synchronized void run() {
            if (!SuspendedFiberStorage.INSTANCE.isEmpty() && resendMakeConnectionIntervalPassed()) {
                sendMakeConnectionMessageNow();
            }
        }

        private synchronized boolean resendMakeConnectionIntervalPassed() {
            return lastMcMessageTimestamp - System.currentTimeMillis() > 2000; // TODO make configurable
        }

        synchronized void sendMakeConnectionMessageNow() {

            // TODO implement

            lastMcMessageTimestamp = System.currentTimeMillis();
        }
    }
    //
    private static final Logger LOGGER = Logger.getLogger(McClientTube.class);
    private static final String MC_CORRELATION_ID_KEY = McClientTube.class.getName() + ".CORRELATION_ID_KEY";
    //
    private final RxConfiguration configuration;
    private final Unmarshaller unmarshaller;
    private final String wsmcAnnonymousAddress;
    private final WSDLPort wsdlPort;
    private final FiberExecutor fiberExecutor;
    private final ScheduledTaskManager scheduler;
    private final MakeConnectionSenderTask mcSenderTask;

    McClientTube(McClientTube original, TubeCloner cloner) {
        super(original, cloner);

        this.configuration = original.configuration;

        this.unmarshaller = configuration.getMcVersion().createUnmarshaller(configuration.getAddressingVersion());

        this.wsmcAnnonymousAddress = original.wsmcAnnonymousAddress;
        this.wsdlPort = original.wsdlPort;
        this.fiberExecutor = original.fiberExecutor;
        this.scheduler = original.scheduler;
        this.mcSenderTask = original.mcSenderTask;
    }

    McClientTube(RxConfiguration configuration, Tube tubelineHead, ClientTubelineAssemblyContext context) throws RxRuntimeException {
        super(tubelineHead);

        this.configuration = configuration;

        this.unmarshaller = configuration.getMcVersion().createUnmarshaller(configuration.getAddressingVersion());
        this.wsmcAnnonymousAddress = configuration.getMcVersion().getWsmcAnonymousAddress(UUID.randomUUID().toString());
        this.wsdlPort = context.getWsdlPort();
        this.fiberExecutor = new FiberExecutor("MakeConnectionClient", tubelineHead);

        this.mcSenderTask = new MakeConnectionSenderTask(wsmcAnnonymousAddress);
        this.scheduler = new ScheduledTaskManager();
        this.scheduler.startTask(mcSenderTask, 2000, 500); // TODO make it configurable
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
        /**
         *
         * 1. attach WS-MC anonymous URI
         *
         * if this is not a one-way request:
         * 2. pair this fiber with the request message id
         * 3. create new fiber for sending the request
         * 4. start the new fiber and register tube for completion callback
         * 5. suspend this fiber = return doSuspend()
         */
        final Message message = request.getMessage();
        if (wsdlPort != null && !message.isOneWay(wsdlPort)) {
            setReplyToHeader(request);

            String correlationId = message.getID(configuration.getAddressingVersion(), configuration.getSoapVersion());
            SuspendedFiberStorage.INSTANCE.register(correlationId, Fiber.current());
            request.invocationProperties.put(MC_CORRELATION_ID_KEY, correlationId);

            fiberExecutor.start(request, new FiberExecutionResultHandler(Fiber.current()));
            return super.doSuspend();
        }

        // one-way request -> continue normally
        return super.processRequest(request);
    }

    @Override
    public NextAction processResponse(Packet response) {
        Message responseMessage = response.getMessage();
        if (responseMessage != null) {


            // we need to process MessagePending here and not in a fiber callback as the header may be present also on one-way responses
            MessagePending messagePendingHeader = readHeaderAsUnderstood(responseMessage, unmarshaller, configuration.getMcVersion().messagePendingHeaderName);
            if (messagePendingHeader.isPending()) {
                if (!SuspendedFiberStorage.INSTANCE.isEmpty()) {
                    this.mcSenderTask.sendMakeConnectionMessageNow();
                }
            }
            // processing WS-M SOAP faults
            if (responseMessage.isFault()) {
                String faultAction = responseMessage.getHeaders().getAction(configuration.getAddressingVersion(), configuration.getSoapVersion());
                if (configuration.getMcVersion().isMcFault(faultAction)) {
                    String faultReason = null;
                    try {
                        SOAPFault fault = responseMessage.readAsSOAPMessage().getSOAPBody().getFault();
                        faultReason = fault.getFaultString();
                    } catch (SOAPException ex) {
                        throw LOGGER.logSevereException(new WebServiceException(ex));
                    }

                    return super.doThrow(new WebServiceException(
                            String.format("Unexpected WS-MAkeConnection protocol error: %s", faultReason)));
                }
            }
        }
        return super.processResponse(response);
    }

    @Override
    public NextAction processException(Throwable t) {
        return super.processException(t);
    }

    @Override
    public void preDestroy() {
        super.preDestroy();
    }

    private void setReplyToHeader(Packet request) {
        // TODO replace annonymous replyTo header value with the WS-MC annonymous URI
    }


    // TODO merge this method with PacketAdapter method
    private final <T> T readHeaderAsUnderstood(Message message, Unmarshaller unmarshaller, QName headerName) throws RxRuntimeException {
        Header header = message.getHeaders().get(headerName, true);
        if (header == null) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            T result = (T) header.readAsJAXB(unmarshaller);
            return result;
        } catch (JAXBException ex) {
            throw LOGGER.logSevereException(new RxRuntimeException(String.format("Error unmarshalling header %s", headerName), ex));
        }
    }
}