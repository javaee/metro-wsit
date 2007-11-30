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
package com.sun.xml.ws.rm.jaxws.runtime;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.rm.Sequence;
import com.sun.xml.ws.rm.BufferFullException;
import com.sun.xml.ws.rm.DuplicateMessageException;
import com.sun.xml.ws.rm.InvalidMessageNumberException;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.RMMessage;
import com.sun.xml.ws.rm.RMVersion;
import com.sun.xml.ws.rm.jaxws.util.ProcessingFilter;
import com.sun.xml.ws.rm.protocol.AbstractAckRequested;
import com.sun.xml.ws.rm.protocol.AbstractSequence;
import com.sun.xml.ws.rm.protocol.AbstractSequenceAcknowledgement;
import com.sun.xml.ws.rm.protocol.AcknowledgementHandler;
import com.sun.xml.ws.rm.localization.LocalizationMessages;

import com.sun.xml.ws.rm.localization.RmLogger;
import java.net.URI;

/**
 *
 */
public abstract class OutboundSequence extends Sequence {

    private static final RmLogger LOGGER = RmLogger.getLogger(OutboundSequence.class);
    /**
     * Instance of helper class that processes SequnceAcknowledgement headers.
     */
    private AcknowledgementHandler ackHandler;
    /**
     * Space available in receiving buffer at destination, if
     * this can be determined.
     */
    private int bufferRemaining;
    /**
     * Common destination for all application messages in the sequence.
     */
    private URI destination;
    /**
     * Processing filter whose handleRequestHeaders method
     * can access headers before they are marshalled.
     */
    private ProcessingFilter filter = null;
    /**
     *  Companion <code>InboundSequence</code>
     */
    private InboundSequence companionInboundSequence;
    /**
     * Flag determines whether messages will be saved. Will only be
     * false in the case of companions to ServerInboundSequences for
     * endpoints with no two-way operations.
     */
    private boolean saveMessages = true;
    /**
     * Sequence acknowledgement to be sent back to client on next
     * available message to the AcksTo endpoint.
     */
    private AbstractSequenceAcknowledgement sequenceAcknowledgement;

    protected OutboundSequence(SequenceConfig config) {
        super(config);

        this.ackHandler = new AcknowledgementHandler(config);
    }

    /**
     * Accessor for the value of the Destination URI.
     *
     * @return The destination URI.
     */
    public URI getDestination() {
        return destination;
    }

    public void setDestination(URI destination) {
        this.destination = destination;
    }

    /**
     * Invoked by Incoming message processor to post Sequence Acknowledgement
     * from companion Incoming Sequence for transmission on next OutboundMessage.
     */
    public void setSequenceAcknowledgement(AbstractSequenceAcknowledgement element) {
        this.sequenceAcknowledgement = element;
    }

    /**
     * Accessor for the <code>inboundSequence</code> field.
     *
     * @return The <code>inboundSequence</code> field.
     */
    public InboundSequence getInboundSequence() {
        return companionInboundSequence;
    }

    protected void setCompanionSequence(InboundSequence companionSequence) {
        // TODO: remove this method if possible
        this.companionInboundSequence = companionSequence;
    }

    /**
     * Accessor for bufferRemaining field.
     */
    public int getBufferRemaining() {
        return bufferRemaining;
    }

    /**
     * Mutator for bufferRemaining field.
     */
    public void setBufferRemaining(int value) {
        bufferRemaining = value;
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
    public void processOutboundMessage(RMMessage outboundMessage) throws InvalidMessageNumberException, BufferFullException, DuplicateMessageException {
        if (saveMessages && !outboundMessage.isOneWayResponse()) {
            //Add the message to the sequence unless this has been done previously
            int messageNumber = outboundMessage.getMessageNumber();
            if (messageNumber == 0) {
                messageNumber = set(getNextIndex(), outboundMessage);
            } else {
                set(messageNumber, outboundMessage);
            }

            AbstractSequence element = null;
            if (getConfig().getRMVersion() == RMVersion.WSRM10) {
                element = new com.sun.xml.ws.rm.v200502.SequenceElement();
            } else {
                element = new com.sun.xml.ws.rm.v200702.SequenceElement();
            }
            element.setNumber(messageNumber);
            element.setId(this.getId());
            outboundMessage.setSequenceElement(element);

            //if it is time to request an ack for this sequence, add AckRequestedHeader
            if (isAckRequested()) {
                AbstractAckRequested ack = null;
                if (getConfig().getRMVersion() == RMVersion.WSRM10) {
                    ack = new com.sun.xml.ws.rm.v200502.AckRequestedElement();
                    ack.setId(this.getId());
                } else {
                    ack = new com.sun.xml.ws.rm.v200702.AckRequestedElement();
                    ack.setId(this.getId());
                }
                outboundMessage.setAckRequestedElement(ack);
            }
        }

        //if companion Inbound sequence is returning an acknowledgement, add the
        //SequenceAcknowledgement header
        if (sequenceAcknowledgement != null) {
            //mess.addHeader(Headers.create(getVersion(), marshaller,sequenceAcknowledgement));
            if (sequenceAcknowledgement instanceof com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) {
                outboundMessage.setSequenceAcknowledgementElement((com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) sequenceAcknowledgement);
            } else {
                outboundMessage.setSequenceAcknowledgementElement((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement) sequenceAcknowledgement);
            }
            sequenceAcknowledgement = null;
        }

        if (filter != null) {
            filter.handleOutboundHeaders(outboundMessage);
        }

        if (outboundMessage.getSequenceElement() != null) {
            outboundMessage.addHeader(createHeader(outboundMessage.getSequenceElement()));
        }

        if (outboundMessage.getAckRequestedElement() != null) {
            outboundMessage.addHeader(createHeader(outboundMessage.getAckRequestedElement()));
        }

        if (outboundMessage.getSequenceAcknowledgementElement() != null) {
            outboundMessage.addHeader(createHeader(outboundMessage.getSequenceAcknowledgementElement()));
        }
    }

    /**
     * Add a pending acknowledgement to a message without adding message to sequence.  Used
     * for sending final ack on a TerminateSequence message if necessary.
     *
     *  @param mess The OutboundMessage.
     */
    public synchronized void processAcknowledgement(RMMessage mess) throws RMException {
        //if companion Inbound sequence is returning an acknowledgement, add the
        //SequenceAcknowledgement header
        if (sequenceAcknowledgement != null) {
            //mess.addHeader(Headers.create(getVersion(), marshaller,sequenceAcknowledgement));
            mess.addHeader(createHeader(sequenceAcknowledgement));
            sequenceAcknowledgement = null;
        }
    }

    /**
     * Sets the state of the message at the specified index to complete, and discards
     * a com.sun.xml.ws.api.message.Message.
     * 
     * @param i Index to set.
     */
    public synchronized void acknowledge(int messageIndex) throws InvalidMessageNumberException {
        RMMessage rmMessage;
        if (messageIndex >= getNextIndex() || (null == (rmMessage = get(messageIndex)))) {
            // TODO L10N
            throw LOGGER.logSevereException(new InvalidMessageNumberException("No RM message stored under index " + messageIndex));
        }

        if (!rmMessage.isComplete()) {
            decreaseStoredMessages();
            if (getStoredMessages() == 0) {
                //A thread on which waitForAcks() has been called
                //may be waiting for all the acks to arrive.
                notifyAll();
            }
            rmMessage.complete();
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
    public void handleAckResponse(AbstractSequenceAcknowledgement element) throws InvalidMessageNumberException {
        ackHandler.handleAcknowledgement(this, element);
    }

    /**
     *
     * Called by disconnect before sending Last and Terminate sequence.  Blocks until all messages
     * have been acked.  The notifyAll method is called by OutboundSequence.acknowledge when
     * stored message count reaches 0.
     */
    public synchronized void waitForAcks() {
        while (getStoredMessages() != 0) {
            try {
                //wait for the specified timeout or a notify(), which is called
                //whenever a message is acked.
                long timeout = getConfig().getCloseTimeout();
                wait(timeout);

                if (getStoredMessages() > 0) {
                    LOGGER.severe(LocalizationMessages.WSRM_5000_TIMEOUT_IN_WAITFORACKS_STRING(timeout / 1000, getStoredMessages()));
                    break;
                }
            } catch (InterruptedException e) {
                //allow preDestroy to continue
                //TODO L10N
                LOGGER.finest("Waiting for acknowledgement interrupted, resuming processing", e);
                break;
            }
        }
    }

    protected boolean isAckRequested() {
        // For oneway messages it does not make sense to send
        // AckRequestedElement on the ServerOutbound messages
        // saveMessages will be true in case of two way messages
        // for AckRequestedElement will be generated then
        // otherwise it will return false
        return saveMessages;
    }

    public boolean isSaveMessages() {
        // FIXME this is a real mess as there are two getters, one overriden (above)
        return saveMessages;
    }

    public void setSaveMessages(boolean value) {
        this.saveMessages = value;
    }

    protected boolean isResendDue() {
        return true;
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
     */
    public void ensureAckRequested(RMMessage rmMessage) {
        // TODO use this method or remove it?
        if (rmMessage.getAckRequestedElement() == null) {
            AbstractAckRequested ack = null;
            if (getConfig().getRMVersion() == RMVersion.WSRM10) {
                ack = new com.sun.xml.ws.rm.v200502.AckRequestedElement();
                ack.setId(this.getId());
            } else {
                ack = new com.sun.xml.ws.rm.v200702.AckRequestedElement();
                ack.setId(this.getId());
            }

            rmMessage.setAckRequestedElement(ack);
            rmMessage.addHeader(createHeader(rmMessage.getAckRequestedElement()));
        }
    }

    protected Header createHeader(Object obj) {
        return Headers.create(getConfig().getRMVersion().jaxbContext, obj);
    }

    public RMMessage getUnacknowledgedMessage() {
        for (int i = 0; i < getNextIndex(); i++) {
            try {
                RMMessage mess = get(i);
                if (mess != null && !mess.isComplete()) {
                    return mess;
                }
            } catch (InvalidMessageNumberException e) {
                //TODO L10N + handle exception
                LOGGER.fine("Attemted to access message with an invalid index [" + i + "]", e);
            }
        }
        return null;
    }
}
