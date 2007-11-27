/*
 * $Id: RMMessage.java,v 1.2 2007-11-27 15:40:58 m_potociar Exp $
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

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.rm.protocol.AbstractAckRequested;
import com.sun.xml.ws.rm.protocol.AbstractSequence;
import com.sun.xml.ws.rm.protocol.AbstractSequenceAcknowledgement;
import com.sun.xml.ws.rm.localization.LocalizationMessages;

/**
 * Message is an abstraction of messages that can be added to WS-RM Sequences. 
 * Each instance wraps a JAX-WS message.
 */
public final class RMMessage {

    /**
     * The JAX-WS Message wrapped by this instance.
     */
    private Message message;
    /**
     * The Sequence to which the message belongs.
     */
    private Sequence sequence = null;
    /**
     * The messageNumber of the Message in its Sequence.
     */
    private int messageNumber = 0;
    /**
     * Flag which is true if and only if the message is being processed
     */
    private boolean isBusy = false;
    /**
     * Flag indicating whether message is delivered/acked.
     * The meaning differs according to the type of sequence
     * to which the message belongs.  The value must only be
     * changed using the complete() method, which should only
     * be invoked by the Sequence containing the message.
     */
    private boolean isComplete = false;
    /**
     * For messages belonging to 2-way MEPS, the corresponding message.
     */
    private RMMessage relatedMessage = null;
    /**
     * Sequence stored when the corresponding com.sun.xml.ws.api.message.Header
     * is added to the message.
     */
    private AbstractSequence sequenceElement = null;
    /**
     * SequenceAcknowledgmentElement stored when the corresponding com.sun.xml.ws.api.message.Header
     * is added to the message.
     */
    private AbstractSequenceAcknowledgement sequenceAcknowledgementElement = null;
    /**
     * SequenceElement stored when the corresponding com.sun.xml.ws.api.message.Header
     * is added to the message.
     */
    private AbstractAckRequested ackRequestedElement = null;
    /**
     * Version of RM spec being used.
     */
    private RMVersion version;
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
     * Instance of TublineHelper used to resend messages.
     */
    private MessageSender messageSender;

    /**
     * Public ctor takes wrapped JAX-WS message as its argument.
     */
    public RMMessage(Message message, RMVersion version) {
        this.message = message;
        this.version = version;
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
     * For client message, sets the messageSender used to resend messages.
     */
    public void setMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Accessor for the relatedMessage field.
     *
     * @return The response corresponding to a request and vice-versa.
     */
    public RMMessage getRelatedMessage() {
        return relatedMessage;
    }

    /**
     * Mutator for the relatedMessage field.
     *
     * @param mess
     */
    public void setRelatedMessage(RMMessage mess) {
        //store the message with a copy of the "inner" com.sun.xml.ws.api.message.Message
        //since the original one will be consumed
        mess.copyContents();
        relatedMessage = mess;
    }

    /**
     * Accessors for isBusy field used with Tubeline implementation.
     */
    public boolean getIsBusy() {
        return isBusy();
    }

    public void setIsBusy(boolean value) {
        isBusy = value;
    }

    /**
     * Get the RM Header Element with the specified name from the underlying
     * JAX-WS message's HeaderList
     * @param name The name of the Header to find.
     */
    public Header getHeader(String name) {
        if (message == null || !message.hasHeaders()) {
            return null;
        }

        return message.getHeaders().get(version.getNamespaceURI(), name, true);
    }

    /**
     * Add the specified RM Header element to the underlying JAX-WS message's
     * <code>HeaderList</code>.
     *
     * @param header The <code>Header</code> to add to the <code>HeaderList</code>.
     */
    public void addHeader(Header header) {
        message.getHeaders().add(header);
    }

    /**
     * Determines whether this message is delivered/acked
     *
     * @return The value of the isComplete flag
     */
    public boolean isComplete() {
        //synchronized block is redundant.
        synchronized (sequence) {
            return isComplete;
        }
    }

    /**
     * Sets the isComplete field to true, indicating that the message has been acked. Also
     * discards the stored com.sun.xml.api.message.Message.
     */
    public void complete() {
        //release reference to JAX-WS message.
        synchronized (sequence) {
            message = null;
            isComplete = true;
        }
    }

    /**
     * Resume processing of the message on this Message's monitor.
     */
    public synchronized void resume() {

        if (!isBusy && !isComplete) {
            messageSender.send();
        }

    }

    public synchronized boolean isBusy() {
        return isBusy;
    }

    /**
     * Returns a copy of the wrapped com.sun.xml.ws.api.message.Message.
     */
    public Message getCopy() {
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
            Message newMessage = message.copy();
            message = newMessage;
        }
    }

    @Override
    public String toString() {
        String ret = LocalizationMessages.MESSAGE_NUMBER_STRING(messageNumber);
        ret += LocalizationMessages.SEQUENCE_STRING(getSequence() != null ? getSequence().getId() : "null");

        AbstractSequence sel;
        AbstractSequenceAcknowledgement sael;
        AbstractAckRequested ael;
        if (null != (sel = getSequenceElement())) {
            ret += sel.toString();
        }

        if (null != (sael = getSequenceAcknowledgementElement())) {
            ret += sael.toString();
        }

        if (null != (ael = getAckRequestedElement())) {
            ret += ael.toString();
        }

        return ret;


    }

    /*      Diagnostic methods store com.sun.xml.ws.protocol.* elements when
     *      corresponding com.sun.xml.ws.api.message.Headers are added to the 
     *      message
     */
    public AbstractSequenceAcknowledgement getSequenceAcknowledgementElement() {
        return sequenceAcknowledgementElement;
    }

    public void setSequenceAcknowledgementElement(AbstractSequenceAcknowledgement el) {
        sequenceAcknowledgementElement = el;
    }

    public AbstractSequence getSequenceElement() {
        return sequenceElement;
    }

    public void setSequenceElement(AbstractSequence el) {
        sequenceElement = el;
    }

    public AbstractAckRequested getAckRequestedElement() {
        return ackRequestedElement;
    }

    public void setAckRequestedElement(AbstractAckRequested el) {
        ackRequestedElement = el;
    }
}
