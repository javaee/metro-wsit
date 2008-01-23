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

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.policy.ConfigurationManager;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * <p>
 * RM session represents a contract between single WS proxy and it's corresponding service. Multiple tubelines (of the same
 * WS proxy) may share a single RM session, each WS proxy however creates it's own session.
 * </p>
 * <p>
 * RM session performs all tasks related to RM message processing, while being focused on a single reliable connection.
 * </p>
 * 
 * TODO: Decide: is this going to be considered as a key element of a RM failover implementation?
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class ClientRmSession {
    private static class FiberRegistration {
        private final long timestamp;
        final Fiber fiber;
        final Packet packet;
        
        FiberRegistration(Fiber fiber, Packet packet) {
            this.timestamp = System.currentTimeMillis();
            this.fiber = fiber;                 
            this.packet = packet;
        }
        
        boolean expired(long period) {
            return System.currentTimeMillis() - timestamp >= period;
        }        
    }
    
    
    private static final RmLogger LOGGER = RmLogger.getLogger(ClientRmSession.class);
    private final Configuration configuration;
    private final SequenceManager sequenceManager;
    private final ProtocolCommunicator communicator;
    private final boolean isRequestResponseSession;
    private final Lock initLock = new ReentrantLock();
    private String inboundSequenceId = null;
    private String outboundSequenceId = null;
    private final Queue<FiberRegistration> fibersToResend = new LinkedList<FiberRegistration>();
    private final ResendTimer resendTimer;

    public ClientRmSession(WSDLPort wsdlPort, WSBinding binding, ProtocolCommunicator communicator) {
        // take the first config alternative for now...
        this.configuration = ConfigurationManager.createClientConfigurationManager(wsdlPort, binding).getConfigurationAlternatives()[0];
        this.sequenceManager = SequenceManagerFactory.getInstance().getSequenceManager();
        this.communicator = communicator;
        this.isRequestResponseSession = checkForRequestResponseOperations(wsdlPort);
        this.resendTimer = new ResendTimer(this);        
    }

    /**
     * Closes and terminates associated sequences and releases other resources associated with this RM session
     */
    void close() {
        try {
            sequenceManager.getSequence(outboundSequenceId).close();
        } catch (UnknownSequenceException ex) {
            LOGGER.logException(ex, Level.WARNING);
        }
        try {
            sequenceManager.getSequence(inboundSequenceId).close();
        } catch (UnknownSequenceException ex) {
            LOGGER.logException(ex, Level.WARNING);
        }
        resendTimer.stop();
    }
    
    public Packet processOutgoingPacket(Packet requestPacket) throws UnknownSequenceException {
        initializeIfNecessary();

        Message message = requestPacket.getMessage();
        message = sequenceManager.getSequence(outboundSequenceId).processOutgoingMessage(message);
        if (isRequestResponseSession) {
            message = sequenceManager.getSequence(inboundSequenceId).processOutgoingMessage(message);
        }
        requestPacket.setMessage(message);

        return requestPacket;
    }

    Packet processIncommingPacket(Packet responsePacket) throws UnknownSequenceException {
        initializeIfNecessary();

        Message message = responsePacket.getMessage();
        message = sequenceManager.getSequence(outboundSequenceId).processIncommingMessage(message);
        if (isRequestResponseSession) {
            message = sequenceManager.getSequence(inboundSequenceId).processIncommingMessage(message);
        }
        responsePacket.setMessage(message);

        return responsePacket;
    }

    /**
     * Registers given fiber for a resend (resume)
     * 
     * @param fiber a fiber to be resumed after resend interval has passed
     * @return {@code true} if the fiber was successfully registered; {@code false} otherwise.
     */
    boolean registerForResend(Fiber fiber, Packet packet) {
        return fibersToResend.offer(new FiberRegistration(fiber, packet)); // TODO: do I need to create a new packet?
    }
    
    void resend() {
        while (!fibersToResend.isEmpty() && fibersToResend.peek().expired(configuration.getMessageRetransmissionInterval())) {
            FiberRegistration registration = fibersToResend.poll();
            registration.fiber.resume(registration.packet);
        }
    }

    /**
     * Performs late initialization of sequences and timer task, provided those have not yet been initialized.
     * The actual initialization thus happens only once in the lifetime of each client RM session object.
     */
    private void initializeIfNecessary() {
        initLock.lock();
        try {
            if (inboundSequenceId == null && outboundSequenceId == null) {
                if (isRequestResponseSession) {
                    inboundSequenceId = sequenceManager.createInboundSequence(sequenceManager.generateSequenceUID()).getId();
                }
                sequenceManager.createOutboudSequence(outboundSequenceId);
                
                resendTimer.start();
            }
        } finally {
            initLock.unlock();
        }
    }

    /**
     * Determine whether wsdl port contains any two-way operations.
     * 
     * @param port WSDL port to check
     * @return {@code true} if there are request/response present on the port; returns {@code false} otherwise
     */
    private boolean checkForRequestResponseOperations(WSDLPort port) {
        WSDLBoundPortType portType;
        if (port == null || null == (portType = port.getBinding())) {
            //no WSDL perhaps? Returning false here means that will be no
            //reverse sequence.  That is the correct behavior.
            return false;
        }

        for (WSDLBoundOperation boundOperation : portType.getBindingOperations()) {
            if (!boundOperation.getOperation().isOneWay()) {
                return true;
            }
        }

        return false;
    }
}
