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
package com.sun.xml.ws.rm.jaxws.runtime.client;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.rm.AcknowledgementListener;
import com.sun.xml.ws.api.rm.SequenceSettings;
import com.sun.xml.ws.api.rm.client.ClientSequence;
import com.sun.xml.ws.client.ContentNegotiation;
import com.sun.xml.ws.rm.InvalidMessageNumberException;
import com.sun.xml.ws.rm.RMMessage;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.jaxws.runtime.InboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.protocol.AbstractCreateSequence;
import com.sun.xml.ws.rm.protocol.AbstractCreateSequenceResponse;
import com.sun.xml.ws.rm.protocol.AbstractTerminateSequence;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;

import javax.xml.transform.Source;
import javax.xml.ws.Service;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.util.UUID;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.BindingProvider;

/**
 * ClientOutboundSequence represents the set of all messages from a single BindingProvider instance.
 * It includes methods that connect and disconnect to a remote RMDestination using
 * a client for a WebService that uses CreateSequence and TerminateSequence as its request messages.
 */
public class ClientOutboundSequence extends OutboundSequence implements ClientSequence {

    private static final RmLogger LOGGER = RmLogger.getLogger(ClientOutboundSequence.class);
    /**
     * Current value of receive buffer read from incoming SequenceAcknowledgement
     * messages if RM Destination implements properietary Indigo Flow Control feature.
     */
    protected int receiveBufferSize;
    /**
     * The helper class used to send protocol messages
     * <code>CreateSequenceElement</code>
     * <code>CreateSequenceResponseElement</code>
     * <code>LastMessage</code>
     * <code>AckRequestedElement</code>
     *
     */
    private ProtocolMessageSender protocolMessageSender;
    /**
     * Indicates whether the sequence uses anonymous acksTo
     */
    private boolean isAnonymous = false;
    /*
     * Flag which indicates whether sequence is active (disconnect() has not
     * been called.
     */
    private boolean isActive = true;
    /**
     * Time after which resend of messages in sequences is attempted at
     * next opportunity.
     */
    private long resendDeadline;
    /**
     * Time after which Ack is requested at next opportunity.
     */
    private long ackRequestDeadline;
    /**
     * Can be registered to listen for sequence acknowledgements.
     */
    private AcknowledgementListener ackListener;
    /**
     * Service using this sequence (if known)
     */
    private Service service;
    /**
     * This field is used only as a hack to test Server-side
     * timeout functionality.  It is not intended to be used
     * for any other purpose.
     */
    private static boolean sendHeartbeats = true;

    /**
     * Creates new sequence and onnects it to the remote RM Destination by sending 
     * an request through the proxy stored in the <code>port</code> field.
     */
    public ClientOutboundSequence(
            SequenceConfig config,
            SecurityTokenReferenceType str,
            String destination,
            String acksToUri,
            boolean twoWay,
            Unmarshaller unmarshaller,
            Tube nextTube,
            BindingProvider proxy,
            ContentNegotiation contentNegotiation) throws RmException {
        super(config);
        //FIXME for now
        super.setBufferRemaining(config.getBufferSize());

        this.protocolMessageSender = new ProtocolMessageSender(config, unmarshaller, nextTube, proxy, contentNegotiation);

        this.connect(destination, acksToUri, str, twoWay);
    }

    /**
     * Mutator for the <code>receiveBufferSize</code> field.
     *
     * @param receiveBufferSize The new value for the field.
     */
    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    /**
     * Accessor for the <code>receiveBufferSize</code> field.
     *
     * @return The value for the field.
     */
    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * Return the hoped-for limit to number of stored messages.  Currently
     * the limit is not enforced, but as the number of stored messages approaches
     * the limit, resends and ackRequests occur more frequently.
     */
    public int getTransferWindowSize() {
        //Use server size receive buffer size for now.  Might
        //want to make this configurable.
        return getConfig().getBufferSize();
    }

    /**
     * Registers a <code>AcknowledgementListener</code> for this
     * sequence
     *
     * @param listener The <code>AcknowledgementListener</code>
     */
    public void setAcknowledgementListener(AcknowledgementListener listener) {
        this.ackListener = listener;
    }

    /**
     * Implementation of the getSequenceSettings method in
     * com.sun.xml.ws.rm.api.client.ClientSequence.  Need
     * to populate the sequence ids in the returned SequenceSettings
     * object, since in general, they will not be set in the underlying
     * SequenceConfig object.
     */
    public SequenceSettings getSequenceSettings() {
        SequenceConfig settings = getConfig();
        settings.setSequenceId(getId());

        InboundSequence iseq = getInboundSequence();

        settings.setCompanionSequenceId((iseq != null) ? iseq.getId() : null);
        return settings;
    }

    /**
     * Accessor for the AcknowledgementListener field.
     *
     * @return The AcknowledgementListener.
     */
    public AcknowledgementListener getAcknowledgementListener() {
        return ackListener;
    }

    /**
     * Accessor for the service field.
     *
     * @returns The value of the service field.  May be null if not known.
     */
    public Service getService() {
        return service;
    }

    /**
     * Sets the value of the service field.
     *
     * @param service The service using the sequence.
     */
    public void setService(Service service) {
        this.service = service;
    }

    private AbstractCreateSequence prepareCreateSequenceMessage(String incomingId, boolean twoWay, SecurityTokenReferenceType strType) throws RmException {
            AbstractCreateSequence createSequence = null;
            if (getConfig().getRMVersion() == RmVersion.WSRM10) {
                createSequence = new com.sun.xml.ws.rm.v200502.CreateSequenceElement();
            } else {
                createSequence = new com.sun.xml.ws.rm.v200702.CreateSequenceElement();
            }

            W3CEndpointReference sourceEndpointReference = null;
            AddressingVersion addressingVersion = getConfig().getAddressingVersion();
            if (addressingVersion == AddressingVersion.W3C) {
                //WSEndpointReference wsepr = new WSEndpointReference(getClass().getResourceAsStream("w3c-anonymous-acksTo.xml"), addressingVersion);
                WSEndpointReference epr = AddressingVersion.W3C.anonymousEpr;
                Source source = epr.asSource("AcksTo");
                sourceEndpointReference = new W3CEndpointReference(source);
            } else {
                // TODO L10N
                throw LOGGER.logSevereException(new RmException("Unsupported addressing version"));
            }
            createSequence.setAcksTo(sourceEndpointReference);

            if (twoWay) {
                if (getConfig().getRMVersion() == RmVersion.WSRM10) {
                    com.sun.xml.ws.rm.v200502.Identifier offerIdentifier = new com.sun.xml.ws.rm.v200502.Identifier();
                    offerIdentifier.setValue(incomingId);

                    com.sun.xml.ws.rm.v200502.OfferType offer = new com.sun.xml.ws.rm.v200502.OfferType();
                    offer.setIdentifier(offerIdentifier);

                    ((com.sun.xml.ws.rm.v200502.CreateSequenceElement) createSequence).setOffer(offer);
                } else {
                    com.sun.xml.ws.rm.v200702.Identifier offerIdentifier = new com.sun.xml.ws.rm.v200702.Identifier();
                    offerIdentifier.setValue(incomingId);

                    com.sun.xml.ws.rm.v200702.OfferType offer = new com.sun.xml.ws.rm.v200702.OfferType();
                    offer.setIdentifier(offerIdentifier);
                    // Microsoft does not accept CreateSequence messages if AcksTo and Offer/Endpoint are not the same
                    offer.setEndpoint(sourceEndpointReference);
                    ((com.sun.xml.ws.rm.v200702.CreateSequenceElement) createSequence).setOffer(offer);
                }
            }

            if (strType != null) {
                createSequence.setSecurityTokenReference(strType);
            }        
            
            return createSequence;
    }
    
    /**
     * Connects to remote RM Destination by sending request through the proxy
     * stored in the <code>port</code> field.
     *
     * @param destination Destination URI for RM Destination
     * @param acksTo reply to EPR for protocol responses.  The null value indicates
     *          use of the WS-Addressing anonymous EPR
     * @throws RMException wrapper for all exceptions thrown during execution of method.
     */
    private void connect(String destination, String acksToUri, SecurityTokenReferenceType strType, boolean twoWay) throws RmException {
        LOGGER.entering(destination, acksToUri, twoWay);
        try {
            this.setDestination(destination);
            this.isAnonymous = (acksToUri != null) ? getConfig().getAddressingVersion().anonymousUri.equals(acksToUri.toString()) : true;

            String incomingID = "uuid:" + UUID.randomUUID();
            AbstractCreateSequence createSequence = prepareCreateSequenceMessage(incomingID, twoWay, strType);           
            // TODO check if the security flag is needed
            AbstractCreateSequenceResponse csr = protocolMessageSender.sendCreateSequence(createSequence, destination, acksToUri, strType != null);

            // AbstractAcceptType accept = null;
            if (csr != null) {
                if (csr instanceof com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement) {
                    com.sun.xml.ws.rm.v200502.Identifier idOutbound = ((com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement) csr).getIdentifier();
                    this.setId(idOutbound.getValue());

                    // accept = ((com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement) csr).getAccept();
                } else {
                    com.sun.xml.ws.rm.v200702.Identifier idOutbound = ((com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement) csr).getIdentifier();
                    this.setId(idOutbound.getValue());

                    // accept = ((com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement) csr).getAccept();
                }

                setCompanionSequence(new ClientInboundSequence(this, incomingID, getConfig()));

                //start the inactivity clock
                resetLastActivityTime();
            } else {
            //maybe a non-anonymous AcksTo
            //Handle CreateSequenceRefused fault
            }
        } finally {
            LOGGER.exiting();
        }
    }

    /**
     * Disconnect from the RMDestination by invoking <code>TerminateSequence</code> on
     * the proxy stored in the <code>port</code> field. State of 
     * sequence is set to inactive.
     *
     * @throws RMException wrapper for all exceptions thrown during execution of method.
     */
    public void disconnect() throws RmException {
        disconnect(false);
    }

    /**
     * Disconnect from the RMDestination by invoking <code>TerminateSequence</code> on
     * the proxy stored in the <code>port</code> field.
     *
     * @param keepAlive If true, state of sequence is kept in
     * active atate allowing the reuse of the sequence.
     *
     * @throws RMException wrapper for all exceptions thrown during execution of method.
     */
    public void disconnect(boolean keepAlive) throws RmException {
        //FIXME - find another check for connectiveness.. want to get rid of
        //unnecessary InboundSequences.
        if (getInboundSequence() == null) {
            throw new IllegalStateException("Not connected.");
        }

        isActive = keepAlive;

        //TODO Move this after waitForAcks to obviate  problems caused by
        //the LastMessage Protocol message being processed concurrently with
        //application messages.  At the moment, this may cause problems in
        //Glassfish container with ordered delivery configured.  This will
        //probably no longer be the case when the Tube/Fibre architecture
        //is used.
        if (getConfig().getRMVersion() == RmVersion.WSRM10) {
            sendLast();
        } else {
            sendCloseSequence();
        }

        //this will block until all messages are complete
        waitForAcks();
        AbstractTerminateSequence ts = null;
        if (getConfig().getRMVersion() == RmVersion.WSRM10) {
            ts = new com.sun.xml.ws.rm.v200502.TerminateSequenceElement();
            com.sun.xml.ws.rm.v200502.Identifier idTerminate = new com.sun.xml.ws.rm.v200502.Identifier();
            idTerminate.setValue(getId());
            ((com.sun.xml.ws.rm.v200502.TerminateSequenceElement) ts).setIdentifier(idTerminate);
        } else {
            ts = new com.sun.xml.ws.rm.v200702.TerminateSequenceElement();
            com.sun.xml.ws.rm.v200702.Identifier idTerminate = new com.sun.xml.ws.rm.v200702.Identifier();
            idTerminate.setValue(getId());
            ((com.sun.xml.ws.rm.v200702.TerminateSequenceElement) ts).setIdentifier(idTerminate);

        }

        protocolMessageSender.sendTerminateSequence(ts, this);

    }

    private void sendLast() throws RmException {
        protocolMessageSender.sendLast(this);
    }

    private void sendCloseSequence() throws RmException {
        protocolMessageSender.sendCloseSequence(this);
    }

    /**
     * Causes the specified message number to be resent.
     *
     * @param messageNumber The message number to resend
     */
    public void resend(int messageNumber) throws RmException {
        RMMessage mess = get(messageNumber);
        mess.resume();
    }

    /**
     * Forces an ack request on next message
     */
    public synchronized void requestAck() {
        ackRequestDeadline = System.currentTimeMillis();
    }

    /**
     * Checks whether an ack should be requested.  Currently checks whether the
     * The algorithm checks whether the ackRequest deadline has elapsed.  
     * The ackRequestDeadline is determined by the ackRequestInterval in the 
     * SequenceConfig member for this sequence.
     *
     */
    @Override
    protected synchronized boolean isAckRequested() {
        long time = System.currentTimeMillis();
        if (time > ackRequestDeadline) {
            //reset the clock
            ackRequestDeadline = time + getAckRequestInterval();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether a resend should happen.  The algorithm checks whether 
     * the resendDeadline has elapsed.  
     * The resendDeadline is determined by the resendInterval in the 
     * SequenceConfig member for this sequence.
     *
     */
    @Override
    public synchronized boolean isResendDue() {
        long time = System.currentTimeMillis();
        if (time > resendDeadline) {
            //reset the clock
            resendDeadline = time + getResendInterval();
            return true;
        } else {
            return false;
        }
    }

    private long getResendInterval() {
        //do a resend at every opportunity under these conditions
        //1. Sequence has been terminated
        //2. Number of stored messages exceeds 1/2 available space.
        if (!isActive ||
                getStoredMessages() > (getTransferWindowSize() / 2)) {
            return 0;
        }
        return getConfig().getResendInterval();
    }

    /**
     * Returns true if TransferWindow is full.  In this case, we 
     * hold off on sending messages.
     */
    public boolean isTransferWindowFull() {
        return getTransferWindowSize() == getStoredMessages();
    }

    private long getAckRequestInterval() {
        //send an ackRequest at every opportunity under these conditions
        //1. Sequence has been terminated
        //2. Number of stored messages exceeds 1/2 available space.
        //3. Number of stored messages at endpoint exceeds 1/2
        //   available space.
        if (!isActive ||
                getStoredMessages() > (getTransferWindowSize() / 2) ||
                getReceiveBufferSize() > (getConfig().getBufferSize() / 2)) {
            return 0;
        }
        return getConfig().getAckRequestInterval();
    }

    /**
     * Implementation of acknowledge defers discarding stored messages when
     * the AcksTo endpoint is anonymous and the message is a two-way request.
     * In this case, the actual work usually done by acknowledge() needs to
     * wait until the response is received.  The RMClientPipe invokes 
     * <code>acknowledgeResponse</code> at that time.
     *
     * @param i The index to acknowledge
     * @throws InvalidMessageNumberException
     */
    @Override
    public synchronized void acknowledge(int i) throws InvalidMessageNumberException {

        RMMessage mess = get(i);
        if (isAnonymous && mess.isTwoWayRequest()) {
            return;
        } else {
            super.acknowledge(i);

            if (ackListener != null) {
                ackListener.notify(this, i);
            }
            //if this acknowledgement is not on the protocol
            //response for the one-way message (endpoint behaved
            //unkindly, or possibly dropped the request), the sending
            //thread is waiting in the resend loop in RMClientPipe.
            mess.resume();
        }
    }

    /**
     * Acknowledges that a response to a two-way operation has been
     * received. See Javadoc for <code>acknowledge</code>
     *
     * @param i The index to acknowledge
     * @throws InvalidMessageNumberException
     */
    public synchronized void acknowledgeResponse(int i) throws InvalidMessageNumberException {
        super.acknowledge(i);
        if (ackListener != null) {
            ackListener.notify(this, i);
        }
    }

    /**
     * Handler periodically invoked by RMSource.MaintenanceThread.
     * Has two duties:<p>
     * <ul><li>Resend incomplete messages.</li>
     *     <li>Send AckRequested message down the pipeline if Inactivity 
     *      timeout is approaching.</li>
     * </ul>
     *
     * @throws RMException 
     */
    public synchronized void doMaintenanceTasks() throws RmException {
        if (getStoredMessages() > 0 && isResendDue()) {
            int top = getNextIndex();
            for (int i = 1; i < top; i++) {
                RMMessage mess = get(i);
                if (mess != null && !mess.isComplete()) {
                    // TODO L10N
                    LOGGER.fine("Resending message with id [" + getId() + "]: " + i);
                    resend(i);
                }
            }
        } else {
            //check whether we need to prime the pump
            if (isGettingClose(System.currentTimeMillis() - getLastActivityTime(), getConfig().getInactivityTimeout())) {
                //send an AckRequested down the pipe.  Need to use a background
                //Thread.  This is being called by the RMSource maintenance thread
                //whose health we have to be very careful with.  If the heartbeat
                //message takes inordinately long to process, the maintenance thread
                //could miss many assignments.
                new AckRequestedSender(this).start();
            }
        }
    }

    // TODO: use runnable instead of thread.
    private class AckRequestedSender extends Thread {

        private ClientOutboundSequence sequence;

        AckRequestedSender(ClientOutboundSequence sequence) {
            this.sequence = sequence;
        }

        @Override
        public void run() {
            try {
                if (sendHeartbeats) {
                    LOGGER.fine(LocalizationMessages.WSRM_2010_HEARTBEAT_MESSAGE_MESSAGE(sequence.getId(), System.currentTimeMillis()));
                    protocolMessageSender.sendAckRequested(sequence, getConfig().getSoapVersion());
                }
            } catch (Exception e) {
                //We get here in at least two cases.
                //1. Client running in Webapp that is undeployed, 
                //2. SequenceFault from AckRequested message.
                //
                //In both cases the sequence is of no further use.  We
                //will assume for now that this is already the case.
                LOGGER.fine(LocalizationMessages.WSRM_2009_HEARTBEAT_MESSAGE_EXCEPTION(sequence.getId()), e);
                try {
                    RMSource.getRMSource().removeOutboundSequence(sequence);
                } catch (Exception ex) {
                    //TODO L10N + handle exception; or remove the exception catching
                    LOGGER.severe("Exception occured while removing the outbound sequence [" + sequence.getId() + "]", ex);
                }
            }
        }
    }
}
