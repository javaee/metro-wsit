/*
 * $Id: Message.java,v 1.4.6.1 2007-05-29 23:56:40 ofung Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.rm;
import com.sun.xml.ws.rm.protocol.AckRequestedElement;
import com.sun.xml.ws.rm.protocol.SequenceAcknowledgementElement;
import com.sun.xml.ws.rm.protocol.SequenceElement;

/**
 * Message is an abstraction of messages that can be added to WS-RM Sequences. 
 * Each instance wraps a JAX-WS message.
 */
public class Message {
    
    /**
     * The JAX-WS Message wrapped by this instance.
     */
    protected com.sun.xml.ws.api.message.Message message;
    
    /**
     * The Sequence to which the message belongs.
     */
    protected Sequence sequence = null;
    
    /**
     * The messageNumber of the Message in its Sequence.
     */
    protected int messageNumber = 0;
    
    
    /**
     * Flag which is true if and only if the message is waiting for
     * a notification.
     */ 
    protected boolean isWaiting = false;
    
    /**
     * Flag indicating whether message is delivered/acked.
     * The meaning differs according to the type of sequence
     * to which the message belongs.  The value must only be
     * changed using the complete() method, which should only
     * be invoked by the Sequence containing the message.
     */
    protected boolean isComplete = false;
    
    
    /**
     * For messages belonging to 2-way MEPS, the corresponding message.
     */
    protected com.sun.xml.ws.rm.Message relatedMessage = null;
    
    /**
     * Sequence stored when the corresponding com.sun.xml.ws.api.message.Header
     * is added to the message.
     */
    protected SequenceElement sequenceElement = null; 
    
    
    /**
     * SequenceElement stored when the corresponding com.sun.xml.ws.api.message.Header
     * is added to the message.
     */
    protected SequenceAcknowledgementElement sequenceAcknowledgementElement = null; 
    
    /**
     * SequenceElement stored when the corresponding com.sun.xml.ws.api.message.Header
     * is added to the message.
     */
    protected AckRequestedElement ackRequestedElement = null; 
    
    /**
     * When true, indicates that the message is a request message for
     * a two-way operation.  ClientOutboundSequence with anonymous
     * AcksTo has to handle Acknowledgements differently in this case.
     */
    public boolean isTwoWayRequest = false;
    
    /**
     * Set in empty message used to piggyback response 
     * headers on a one-way response.
     */
    public boolean isOneWayResponse = false;
    
    
    /**
     * Namespace URI corresponding to RM version.
     */
    public static final String namespaceURI = 
            RMBuilder.getConstants().getNamespaceURI();
    
    
    /**
     * Public ctor takes wrapped JAX-WS message as its argument.
     */
    public Message(com.sun.xml.ws.api.message.Message message) {
        this.message = message;
    }
    
    /**
     * Sets  the value of the sequence field.  Used by Sequence methods when
     * adding message to the sequence.
     * @param sequence The sequence.
     */
    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }
    
    /**
     * Gets the Sequence to which the Message belongs.
     * @return The sequence.
     */
    public Sequence getSequence() {
        return sequence;
    }
    
     /**
     * Sets  the value of the messageNumber field.  Used by Sequence methods when
     * adding message to the sequence.
     * @param messageNumber The message number.
     */
    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }
    
    /**
     * Returns the value of the messageNumber field
     * @return The message number.
     */
    public int getMessageNumber() {
        return messageNumber;
    }
    
    /**
     * Accessor for the relatedMessage field.
     *
     * @return The response corresponding to a request and vice-versa.
     */
    public com.sun.xml.ws.rm.Message getRelatedMessage() {
        return relatedMessage;
    }
    
    /**
     * Mutator for the relatedMessage field.
     *
     * @param mess
     */
    public void setRelatedMessage(com.sun.xml.ws.rm.Message mess) {
        //store the message with a copy of the "inner" com.sun.xml.ws.api.message.Message
        //since the original one will be consumed
        mess.copyContents();
        relatedMessage = mess;
    }
    
    /**
     * Get the RM Header Element with the specified name from the underlying
     * JAX-WS message's HeaderList
     * @param name The name of the Header to find.
     */
    public com.sun.xml.ws.api.message.Header getHeader(String name) {
        if (message == null || !message.hasHeaders()) {
            return null;
        }
        
        return message.getHeaders().get(namespaceURI, name, true);     
    }
    
    /**
     * Add the specified RM Header element to the underlying JAX-WS message's
     * <code>HeaderList</code>.
     *
     * @param header The <code>Header</code> to add to the <code>HeaderList</code>.
     */
    public void addHeader(com.sun.xml.ws.api.message.Header header) {
        message.getHeaders().add(header);
    }
    
    /**
     * Determines whether this message is delivered/acked
     *
     * @return The value of the isComplete flag
     */
    public boolean isComplete() {
        //synchronized block is redundant.
        synchronized(sequence) {
            return isComplete;
        }
    }
    
    /**
     * Sets the isComplete field to true, indicating that the message has been acked. Also
     * discards the stored com.sun.xml.api.message.Message.
     */
    public void complete() {
        //release reference to JAX-WS message.
        synchronized(sequence) {
            message = null;
            isComplete = true;
        }
    }
    
    /**
     * Block the current thread using the monitor of this <code>Message</code>.
     */
     public synchronized void block() {
     
        isWaiting = true;
        try {
            while (!isComplete && isWaiting) {
                wait();
            }
        } catch (InterruptedException e) {}
    }
    
    /**
     * Wake up the current thread which is waiting on this Message's monitor.
     */
    public synchronized  void resume() {
            isWaiting = false;
            notify();
    }
    
    public synchronized boolean isWaiting() {
        return isWaiting;
    }
    
    /**
     * Returns a copy of the wrapped com.sun.xml.ws.api.message.Message.
     */
    public com.sun.xml.ws.api.message.Message getCopy() {
        return message == null ? null : message.copy();
    }
    
    /**
     * Returns a com.sun.ws.rm.Message whose inner com.sun.xml.ws.api.message.Message is replaced by
     * a copy of the original one.  This message is stored in the relatedMessage field of ClientInboundSequence
     * messages.  A copy needs to be retained rather than the original since the original will already
     * have been consumed at such time the relatedMessage needs to be resent.
     *
     */
    public void copyContents() {
        if (message != null) {
            com.sun.xml.ws.api.message.Message newmessage = message.copy();
            message = newmessage;
        }
    }
    
    public String toString() {
        
         
        String ret = Messages.MESSAGE_NUMBER_STRING.format(messageNumber);
        ret += Messages.SEQUENCE_STRING.format(getSequence() != null ?
                                                getSequence().getId() :
                                                "null");
        
        SequenceElement sel;
        SequenceAcknowledgementElement sael;
        AckRequestedElement ael;
        if ( null != (sel = getSequenceElement())) {
            ret += sel.toString();
        }
        
        if ( null != (sael = getSequenceAcknowledgementElement())) {
            ret += sael.toString();
        }
        
        if ( null != (ael = getAckRequestedElement())) {
            ret += ael.toString();
        }
        
        return ret;
        
        
    }
    
    /*      Diagnostic methods store com.sun.xml.ws.protocol.* elements when
     *      corresponding com.sun.xml.ws.api.message.Headers are added to the 
     *      message
     */
    
    public SequenceAcknowledgementElement getSequenceAcknowledgementElement() {
        return sequenceAcknowledgementElement;
    }
    
    public void setSequenceAcknowledgementElement(SequenceAcknowledgementElement el) {
        sequenceAcknowledgementElement = el;
    }
    
    public SequenceElement getSequenceElement() {
        return sequenceElement;
    }
    
    public void setSequenceElement(SequenceElement el) {
        sequenceElement = el;
    }
    
    public AckRequestedElement getAckRequestedElement() {
        return ackRequestedElement;
    }
    
    public void setAckRequestedElement(AckRequestedElement el) {
        ackRequestedElement = el;
    }
              
}
