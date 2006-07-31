/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

// ClientOutboundSequence.java
//
//
// @author Mike Grogan
// Created on October 15, 2005, 3:13 PM
//
package com.sun.xml.ws.rm.jaxws.runtime.client;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.rm.RMBuilder;
import com.sun.xml.ws.rm.RMConstants;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.Message;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.protocol.*;
import com.sun.xml.ws.rm.InvalidMessageNumberException;
import com.sun.xml.ws.security.impl.bindings.SecurityTokenReferenceType;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingConstants;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.bind.JAXBElement;
import java.net.URI;
import java.util.UUID;


/**
 * ClientOutboundSequence represents the set of all messages from a single BindingProvider instance.
 * It includes methods that connect and disconnect to a remote RMDestination using
 * a client for a WebService that uses CreateSequence and TerminateSequence as its request messages.
 */

public class ClientOutboundSequence extends OutboundSequence {

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
    protected ProtocolMessageSender protocolMessageSender ;
    
    
    private SOAPVersion version ;

    /**
     * Flag to indicate if secureReliableMessaging is on
     */
    private boolean secureReliableMessaging;



    /**
     * The SecurityTokenReference to pass to CreateSequence
     */
    private JAXBElement str = null;


    /**
     * Indicates whether the sequence uses anonymous acksTo
     */
    private boolean isAnonymous = false;
    

    /**
     * Last time that application or protocol message belonging to the sequence was received
     * at RMD.
     */
    private long lastActivityTime = System.currentTimeMillis();
    
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
    
    
    private static boolean sendHeartbeats = true;
    
    public ClientOutboundSequence(SequenceConfig config) {
        this.config = config;

        //for now
        this.version = config.getSoapVersion();
        this.ackHandler = new AcknowledgementHandler(config);

    }



    /**
     * Accessor for the sequenceConfig field
     *
     * @return The value of the field.
     */
    public SequenceConfig getSequenceConfig() {
        return config;
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

    public boolean isSecureReliableMessaging() {
        return secureReliableMessaging;
    }
    
    /**
     * Return the hoped-for limit to number of stored messages.  Currently
     * the limit is not enforced, but as the number of stored messages approaches
     * the limit, resends and ackRequests occurr more frequently.
     */
    private int getTransferWindowSize() {
       //Use server size receive buffer size for now.  Might
       //want to make this configurable.
       return config.getBufferSize(); 
    }

    public void setSecureReliableMessaging(boolean secureReliableMessaging) {
        this.secureReliableMessaging = secureReliableMessaging;
    }

    /**
     * Connects to remote RM Destination by sending request through the proxy
     * stored in the <code>port</code> field.
     *
     * @param destination Destination EPR for RM Destination
     * @param acksTo reply to EPR for protocol responses.  The null value indicates
     *          use of the WS-Addressing anonymous EPR
     * @throws RMException wrapper for all exceptions thrown during execution of method.
     */
    public void connect(EndpointReference destination,
                        EndpointReference acksTo,
                        boolean twoWay) throws RMException {
        try {

            this.destination = destination;
            this.acksTo = acksTo;

            RMConstants constants = RMBuilder.getConstants();

            AddressingConstants addressingConstants =
                    constants.getAddressingBuilder().newAddressingConstants();

            String anonymous = addressingConstants.getAnonymousURI();

            String acksToString;
            //AckListener listener = null; 
                        
            if (acksTo == null) {
                acksToString = anonymous;
            } else {
                acksToString = acksTo.getAddress().getURI().toString();
                
            }
            
            this.isAnonymous = acksToString.equals(anonymous);

            
            CreateSequenceElement cs = new CreateSequenceElement();
            EndpointReference epr = RMBuilder.getConstants().getAddressingBuilder().newEndpointReference(new URI(acksToString));
            AddressingConstants ac = RMBuilder.getConstants().getAddressingBuilder().newAddressingConstants();
            if (ac.getPackageName().equals("com.sun.xml.ws.addressing")){
                cs.setAcksTo(new W3CAcksToImpl(new URI(acksToString)));
            }    else {
                cs.setAcksTo(new MemberSubmissionAcksToImpl(new URI(acksToString)));
                
            }
            String incomingID = "uuid:" + UUID.randomUUID();

            if (twoWay) {
                Identifier id = new Identifier();
                id.setValue(incomingID);
                OfferType offer = new OfferType();
                offer.setIdentifier(id);

                cs.setOffer(offer);
            }

            if (secureReliableMessaging) {
                JAXBElement<SecurityTokenReferenceType> str = getSecurityTokenReference();
                if (str != null) {
                    cs.setSecurityTokenReference(str.getValue());
                }   else throw new RMException("SecurityTokenReference is null");
            }

            CreateSequenceResponseElement csr = protocolMessageSender.sendCreateSequence(cs,destination,
                    acksTo,version);
           
            
            if (csr != null ) {
                Identifier idOutbound = csr.getIdentifier();
                this.id = idOutbound.getValue();
                
                AcceptType accept = csr.getAccept();
                //TODO ... copy rest of eprAccept some day.
                
                if (accept != null) {

                    EndpointReference eprAccept = accept.getAcksTo();
                    javax.xml.ws.addressing.AttributedURI uriAccept = epr.getAddress();
                    EndpointReference eprNew = null;
                    if (eprAccept.getAddress()!=null) {

                        eprNew = AddressingBuilder
                                .newInstance()
                                .newEndpointReference(
                                        eprAccept.getAddress().getURI());
                    }
                    inboundSequence = new ClientInboundSequence(this,
                            incomingID,
                            eprNew);
                } else {
                    inboundSequence = new ClientInboundSequence(this,
                            incomingID, null);
                }
                
                //Todo
                decryptAndValidate();
                
                //start the inactivity clock
                resetLastActivityTime();

            } else {
                //maybe a non-anonymous AcksTo
                //Handle CreateSequenceRefused fault
            }
        } catch (Exception e) {
 
            throw new RMException(e);
        }
    }

    public void decryptAndValidate(){
        //We don't need to do anything:)  The SecurityPipe
        //will do it.  They gave us the STR they will use and
        //we sent it to the server-side in the CS message.  Thats
        //all we need to do on the client side.

    }

    /**
     * Disconnect from the RMDestination by invoking <code>TerminateSequence</code> on
     * the proxy stored in the <code>port</code> field.
     *
     * @throws RMException wrapper for all exceptions thrown during execution of method.
     */
    public void disconnect() throws RMException {

        //FIXME - find another check for connectiveness.. want to get rid of
        //unnecessary InboundSequences.
        if (inboundSequence == null) {
            throw new IllegalStateException("Not connected.");
        }
        
        isActive = false;

        sendLast();
         
        //this will block until all messages are complete
        waitForAcks();
                 
        TerminateSequenceElement ts = new TerminateSequenceElement();
        Identifier idTerminate = new Identifier();
        idTerminate.setValue(id);
        ts.setIdentifier(idTerminate);
        protocolMessageSender.sendTerminateSequence(ts,this,version);

        
    }

    private void sendLast() throws RMException{
        protocolMessageSender.sendLast(this,version);
    }

    /**
     * Causes the specified message number to be resent.
     *
     * @param messageNumber The message number to resend
     */
    public void resend(int messageNumber) throws RMException {
        Message mess = get(messageNumber);
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
    protected synchronized boolean isAckRequested(){
      
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
        //3. Number of stored messages at endpoint exceeds 1/2
        //   available space.
        if (!isActive || 
            storedMessages > (getTransferWindowSize() / 2) ||
            getReceiveBufferSize() > (config.getBufferSize() / 2)) {
            return 0;
        }
        return config.getResendInterval();
    }
    
    private long getAckRequestInterval() {
        //send an ackRequest at every opportunity under these conditions
        //1. Sequence has been terminated
        //2. Number of stored messages exceeds 1/2 available space.
        //3. Number of stored messages at endpoint exceeds 1/2
        //   available space.
        if (!isActive || 
            storedMessages > (getTransferWindowSize() / 2) ||
            getReceiveBufferSize() > (config.getBufferSize() / 2)) {
            return 0;
        }
        return config.getAckRequestInterval();
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
    public synchronized void acknowledge(int i) 
            throws InvalidMessageNumberException {
        
        Message mess = get(i);
        if (isAnonymous() && mess.isTwoWayRequest) {
            return;
        } else {
            super.acknowledge(i);
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
    public synchronized void acknowledgeResponse(int i) 
            throws InvalidMessageNumberException {
            
        super.acknowledge(i);
    }
    
    /**
     * Return value is determined by whether the destination endpoint is the
     * anonymous URI.
     * 
     * @return <code>true</code> if the destination is the anonymous URI.
     *         <code>false</code> otherwise.
     */
    public boolean isAnonymous() {
        return isAnonymous;
    }


    public void registerProtocolMessageSender(ProtocolMessageSender pms) {
        this.protocolMessageSender = pms;

    }

    public JAXBElement<SecurityTokenReferenceType> getSecurityTokenReference() {
        return str;
    }

    public void setSecurityTokenReference(JAXBElement<SecurityTokenReferenceType> str) {
        this.str = str;
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
    public synchronized void doMaintenanceTasks() throws RMException {
         
        if (storedMessages > 0 && isResendDue()) {
            int top = getNextIndex();
            for (int i = 1; i < top; i++) {
                Message mess = get(i);
                if (mess != null && !mess.isComplete()) {
                    System.out.println("resending "  + getId() + ":" + i);
                    resend(i);
                }
            }
        } else {
            //check whether we need to prime the pump
            if (isGettingClose(System.currentTimeMillis() - getLastActivityTime(),
                                config.getInactivityTimeout())) { 
                //send an AckRequested down the pipe.  Need to use a background
                //Thread.  This is being called by the RMSource maintenance thread
                //whose health we have to be very careful with.  If the heartbeat
                //message takes inordinately long to process, the maintenance thread
                //could miss many assignments.
                new AckRequestedSender(this).start();
            }
        }
    }
    
    
  
    private class AckRequestedSender extends Thread {
        
        private ClientOutboundSequence sequence;
        
        AckRequestedSender(ClientOutboundSequence sequence) {
            this.sequence = sequence;
        }
        public void run() {
            try {
               
                if (sendHeartbeats) {
                    System.out.println("Sending heartbeat message for sequence " + sequence.getId());
                    //BUGBUG - Need to fix ProtocolMessageSender to allow sendAckRequested
                    //to be called from this Thread or somehow prevent application messages
                    //from being processed at the same time.  
                    protocolMessageSender.sendAckRequested(sequence, 
                                                       version);
                }
                
            } catch (RMException e) {
                //probably unrecoverable.  This is probably caused by the same condition
                //forcing the heartbeat message to be resent.
                //TODO Log something here
                e.printStackTrace();
            }
        }
    }

}
