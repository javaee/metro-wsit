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

/*
 * OutboundSequence.java
 *
 * @author Mike Grogan
 * Created on November 24, 2005, 9:40 AM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.rm.*;
import com.sun.xml.ws.rm.jaxws.util.ProcessingFilter;
import com.sun.xml.ws.rm.protocol.AckRequestedElement;
import com.sun.xml.ws.rm.protocol.AcknowledgementHandler;
import com.sun.xml.ws.rm.protocol.SequenceAcknowledgementElement;
import com.sun.xml.ws.rm.protocol.SequenceElement;

import javax.xml.bind.Marshaller;
import java.net.URI;

/**
 *
 */
public abstract class OutboundSequence extends Sequence {
    
    /**
     * Common destination for all application messages in the sequence.
     */
    protected URI destination;
    
   
    /**
     * Endpoint for protocol responses.  May be the WS-Addressing anonymous endpoint.
     * There are several variations depending on whether this EPR is the same as 
     * the one used by application messages in the companion <code>InboundSequence</code>
     */
    protected URI acksTo;
    
    /**
     *  Companion <code>InboundSequence</code>
     */
    protected InboundSequence inboundSequence;
     
    /**
     * Sequence acknowledgement to be sent back to client on next
     * available message to the AcksTo endpoint.
     */
    protected SequenceAcknowledgementElement sequenceAcknowledgement;

   /**
     * Configuration for this sequence.
     */
    protected SequenceConfig config /*= new SequenceConfig()*/;
   
   /**
     * Instance of helper class that processes SequnceAcknowledgement headers.
     */
    protected AcknowledgementHandler ackHandler; 
    
    /**
     * Flag determines whether messages will be saved.  Will only be
     * false in the case of companions to ServerInboundSequences for
     * endpoints with no two-way operations.
     */
    public boolean saveMessages = true;
    
    /**
     * Processing filter whose handleRequestHeaders method
     * can access headers before they are marshalled.
     */
    private ProcessingFilter filter = null;
    
     /**
     * Accessor for the value of the Destination URI.
     *
     * @return The destination URI.
     */
    public URI getDestination() {
     
        return destination;
    }
    
    /**
     * Accessor for the value of the Destination URI.
     *
     * @return The destination String.
     */
    public URI getAcksTo() {
       
        return acksTo;
    }
    
     /**
     * Invoked by Incoming message processor to post Sequence Acknowledgement
     * from companion Incoming Sequence for transmission on next OutboundMessage.l
     */
    public void setSequenceAcknowledgement(SequenceAcknowledgementElement element) {
        this.sequenceAcknowledgement = element;
    }
    
    /**
     * Accessor for the <code>inboundSequence</code> field.
     *
     * @return The <code>inboundSequence</code> field.
     */
    public InboundSequence getInboundSequence() {
        return inboundSequence;
    }
    
    /**
     *  Handles an <code>OutboundMessage</code>.
     *  <br>
     *  <ul>
     *      <li> Store the message </li>
     *      <li> If ackRequested flag is set, add an <code>AckRequestedElement</code>
     *          header to the message.</code>
     *      <li> If complanion <code>ClientInboundSequence</code> has queued an acknowledgement,
     *          add a <code>SequenceAcknowledgementElement</code> header to the 
     *          message.</li>
     *  </ul>
     * 
     *  @param mess The OutboundMessage.
     *  @param marshaller The Marshaller to use 
     */ 
    
    public void processOutboundMessage(Message mess, Marshaller marshaller) 
                    throws InvalidMessageNumberException, 
                            BufferFullException,
                            DuplicateMessageException {
        
        if (saveMessages) {
            //Add the message to the sequence unless this has been done previously
            int messageNumber = mess.getMessageNumber();

            if (messageNumber == 0) {
                messageNumber = set(getNextIndex(), mess);
            }

            SequenceElement element = new SequenceElement();
            element.setNumber(messageNumber);
            element.setId(this.getId());
            
            //mess.addHeader(Headers.create(getVersion(),marshaller,element));
            
            mess.setSequenceElement(element);
        }
       
        //if it is time to request an ack for this sequence, add AckRequestedHeader
        if (isAckRequested()) {
            AckRequestedElement ack = new AckRequestedElement();
            ack.setId(this.getId());      
            mess.setAckRequestedElement(ack);
        }
        
        //if companion Inbound sequence is returning an acknowledgement, add the
        //SequenceAcknowledgement header
        if (sequenceAcknowledgement != null) {
            //mess.addHeader(Headers.create(getVersion(), marshaller,sequenceAcknowledgement));
            
            mess.setSequenceAcknowledgementElement(sequenceAcknowledgement);
            
            sequenceAcknowledgement = null;
        }     
        
        if (filter != null) {
            filter.handleOutboundHeaders(mess);
        }
        
        if (mess.getSequenceElement() != null) {
            mess.addHeader(Headers.create(getVersion(),
                            marshaller,
                            mess.getSequenceElement()));
        }
        
        if (mess.getAckRequestedElement() != null) {
            mess.addHeader(Headers.create(getVersion(), 
                           marshaller,
                           mess.getAckRequestedElement()));
        }
        
        if (mess.getSequenceAcknowledgementElement() != null) {
            mess.addHeader(Headers.create(getVersion(), 
                           marshaller,
                           mess.getSequenceAcknowledgementElement()));
        }   
    }
    
    /**
     * Add a pending acknowledgement to a message without adding message to sequence.  Used
     * for sending final ack on a TerminateSequence message if necessary.
     *
     *  @param mess The OutboundMessage.
     *  @param marshaller The Marshaller to use 
     */
    
    public synchronized void processAcknowledgement(Message mess, Marshaller marshaller) 
        throws RMException {
        //if companion Inbound sequence is returning an acknowledgement, add the
        //SequenceAcknowledgement header
        if (sequenceAcknowledgement != null) {
            mess.addHeader(Headers.create(getVersion(), marshaller,sequenceAcknowledgement));
            sequenceAcknowledgement = null;
        }       
    }
    
    /**
     * Sets the state of the message at the specified index to complete, and discards
     * a com.sun.xml.ws.api.message.Message.
     * 
     * @param i Index to set.
     */
    public synchronized void acknowledge(int i) 
                    throws InvalidMessageNumberException {
        
        Message mess;
        if (i >= nextIndex || (null == (mess = get(i)))) {
            throw new InvalidMessageNumberException();
        }
       
        if (!mess.isComplete()) {
            
            --storedMessages;
            if (storedMessages == 0) {
                //A thread on which waitForAcks() has been called
                //may be waiting for all the acks to arrive.
                notifyAll();
            }
            
            mess.complete();
        }
    }
    
    
    /**
     * Removes acked messages from list. 
     *(For anonymous client, need to widen definition of acked to include the 
     * requirement that responses have arrived.)
     *
     * @param element The <code>SequenceAcknowledgementElement</code> containing the
     *              ranges of messages to be removed.
     */
    public void handleAckResponse(SequenceAcknowledgementElement element) 
            throws InvalidMessageNumberException {

        if (ackHandler == null) {
            ackHandler = new AcknowledgementHandler(config);
        }
        ackHandler.handleAcknowledgement(this, element);
    }
    
    /**
     *
     * Called by disconnect before sending Last and Terminate sequence.  Blocks until all messages
     * have been acked.  The notifyAll method is called by OutboundSequence.acknowledge when
     * stored message count reaches 0.
     */
    public synchronized void waitForAcks() {
    
        while (storedMessages != 0) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }

    
     protected boolean isAckRequested(){
         //For oneway messages it does not make sense to send
         // AckRequestedElement on the ServerOutbound messages
         //saveMessages will be true in case of two way messages
         // for AckRequestedElement will be generated then
         //otherwise it will return false
         return saveMessages;
     }
     
     protected boolean isResendDue() {
         return true;
     }

    private SOAPVersion getVersion(){
        return config.getSoapVersion();
    }
    
    public void setProcessingFilter(ProcessingFilter filter) {
        this.filter = filter;
    }
    
    /**
     * Add AckRequested element to an existing message if one is not already 
     * present.  This is used to ensure that an AckRequested header is included
     * on every resend.
     *
     * @param mess The message
     * @param marshaller
     */
    public void ensureAckRequested(Message mess, Marshaller marshaller) {
        if (mess.getAckRequestedElement() == null) {
            
            AckRequestedElement ack = new AckRequestedElement();
            ack.setId(this.getId());      
            mess.setAckRequestedElement(ack);
            mess.addHeader(Headers.create(getVersion(), 
                               marshaller,
                               mess.getAckRequestedElement()));
        }
    }
    
  	
}
