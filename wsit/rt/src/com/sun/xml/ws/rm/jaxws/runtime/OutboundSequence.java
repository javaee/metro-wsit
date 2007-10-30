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

/*
 * OutboundSequence.java
 *
 * @author Mike Grogan
 * Created on November 24, 2005, 9:40 AM
 *
 */
package com.sun.xml.ws.rm.jaxws.runtime;

import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.rm.*;
import com.sun.xml.ws.rm.jaxws.util.ProcessingFilter;
import com.sun.xml.ws.rm.protocol.AbstractAckRequested;
import com.sun.xml.ws.rm.protocol.AbstractSequence;
import com.sun.xml.ws.rm.protocol.AbstractSequenceAcknowledgement;
import com.sun.xml.ws.rm.protocol.AcknowledgementHandler;
import com.sun.xml.ws.rm.v200502.AckRequestedElement;

import javax.xml.bind.Marshaller;
import java.net.URI;
import java.util.logging.Logger;

/**
 *
 */
public abstract class OutboundSequence extends Sequence {

    private static final Logger logger =
            Logger.getLogger(OutboundSequence.class.getName());
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
    protected AbstractSequenceAcknowledgement sequenceAcknowledgement;
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
     * Space available in receiving buffer at destination, if
     * this can be determined.
     */
    protected int bufferRemaining;

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
    public void setSequenceAcknowledgement(AbstractSequenceAcknowledgement element) {
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
    public void processOutboundMessage(Message mess, Marshaller marshaller)
            throws InvalidMessageNumberException,
            BufferFullException,
            DuplicateMessageException {

        if (saveMessages && !mess.isOneWayResponse) {
            //Add the message to the sequence unless this has been done previously
            int messageNumber = mess.getMessageNumber();

            if (messageNumber == 0) {
                messageNumber = set(getNextIndex(), mess);
            } else {
                set(messageNumber, mess);
            }

            AbstractSequence element = null;
            if (config.getRMVersion() == RMVersion.WSRM10) {
                element = new com.sun.xml.ws.rm.v200502.SequenceElement();
            } else {
                element = new com.sun.xml.ws.rm.v200702.SequenceElement();
            }
            element.setNumber(messageNumber);
            element.setId(this.getId());

            //mess.addHeader(Headers.create(getVersion(),marshaller,element));

            mess.setSequenceElement(element);

            //if it is time to request an ack for this sequence, add AckRequestedHeader
            if (isAckRequested()) {
                AbstractAckRequested ack = null;
                if (config.getRMVersion() == RMVersion.WSRM10) {
                    ack = new AckRequestedElement();
                    ack.setId(this.getId());
                } else {
                    ack = new com.sun.xml.ws.rm.v200702.AckRequestedElement();
                    ack.setId(this.getId());
                }

                mess.setAckRequestedElement(ack);
            }
        }

        //if companion Inbound sequence is returning an acknowledgement, add the
        //SequenceAcknowledgement header
        if (sequenceAcknowledgement != null) {
            //mess.addHeader(Headers.create(getVersion(), marshaller,sequenceAcknowledgement));
            if (sequenceAcknowledgement instanceof com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) {
                mess.setSequenceAcknowledgementElement((com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) sequenceAcknowledgement);
            } else {
                mess.setSequenceAcknowledgementElement((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement) sequenceAcknowledgement);
            }


            sequenceAcknowledgement = null;
        }

        if (filter != null) {
            filter.handleOutboundHeaders(mess);
        }

        if (mess.getSequenceElement() != null) {
            /*
            mess.addHeader(Headers.create(getVersion(),
            marshaller,
            mess.getSequenceElement()));
             */
            mess.addHeader(createHeader(mess.getSequenceElement()));
        }

        if (mess.getAckRequestedElement() != null) {
            /*
            mess.addHeader(Headers.create(getVersion(), 
            marshaller,
            mess.getAckRequestedElement()));
             */
            mess.addHeader(createHeader(mess.getAckRequestedElement()));
        }

        if (mess.getSequenceAcknowledgementElement() != null) {
            /*
            mess.addHeader(Headers.create(getVersion(), 
            marshaller,
            mess.getSequenceAcknowledgementElement()));
             */
            mess.addHeader(createHeader(mess.getSequenceAcknowledgementElement()));
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
    public synchronized void acknowledge(int i) throws InvalidMessageNumberException {
        Message mess;
        if (i >= nextIndex || (null == (mess = get(i)))) {
            throw new InvalidMessageNumberException();
        }

        if (!mess.isComplete()) {

            storedMessages--;
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
    public void handleAckResponse(AbstractSequenceAcknowledgement element)
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
                //wait for the specified timeout or a notify(), which is called
                //whenever a message is acked.
                long timeout = config.getCloseTimeout();
                wait(timeout);

                if (storedMessages > 0) {
                    logger.severe(Messages.TIMEOUT_IN_WAITFORACKS_STRING.format(timeout / 1000, storedMessages));
                    break;
                }
            } catch (InterruptedException e) {
                //allow preDestroy to continue
                break;
            }
        }
    }

    protected boolean isAckRequested() {
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

            AbstractAckRequested ack = null;
            if (config.getRMVersion() == RMVersion.WSRM10) {
                ack = new AckRequestedElement();
                ack.setId(this.getId());
            } else {
                ack = new com.sun.xml.ws.rm.v200702.AckRequestedElement();
                ack.setId(this.getId());
            }

            mess.setAckRequestedElement(ack);
            /*
            mess.addHeader(Headers.create(getVersion(), 
            marshaller,
            mess.getAckRequestedElement()));
             */
            mess.addHeader(createHeader(mess.getAckRequestedElement()));
        }
    }

    protected com.sun.xml.ws.api.message.Header createHeader(Object obj) {
        return Headers.create(
                config.getRMVersion().getJAXBRIContextHeaders(),
                obj);
    }

    public Message getUnacknowledgedMessage() {
        for (int i = 0; i < nextIndex; i++) {
            try {
                Message mess = get(i);
                if (mess != null && !mess.isComplete()) {
                    return mess;
                }
            } catch (InvalidMessageNumberException e) {
            }
        }
        return null;
    }
}
