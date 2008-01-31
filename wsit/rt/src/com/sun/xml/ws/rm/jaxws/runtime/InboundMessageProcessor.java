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
import com.sun.xml.ws.rm.BufferFullException;
import com.sun.xml.ws.rm.CloseSequenceException;
import com.sun.xml.ws.rm.DuplicateMessageException;
import com.sun.xml.ws.rm.InvalidMessageNumberException;
import com.sun.xml.ws.rm.InvalidSequenceException;
import com.sun.xml.ws.rm.MessageNumberRolloverException;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.RMMessage;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.protocol.AbstractAckRequested;
import com.sun.xml.ws.rm.protocol.AbstractSequence;
import com.sun.xml.ws.rm.protocol.AbstractSequenceAcknowledgement;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * 
 * InboundMessageProcessor examines the headers of inbound <code>Messages</code> and
 * based on the sequence id's and types of the headers, dispatches them to 
 * appropriate <code>ClientInboundSequence</code> or <code>ClientOutboundSequence</code> 
 * methods.
 */
public class InboundMessageProcessor {

    private static final RmLogger LOGGER = RmLogger.getLogger(InboundMessageProcessor.class);

    private InboundMessageProcessor() {
    }
    
    private static void processAckRequestHeader(Header header, Unmarshaller unmarshaller, RMMessage message, RMProvider provider) throws RmException, InvalidMessageNumberException {
        try {
            //dispatch to InboundSequence to construct response.
            //TODO handle error condition no such sequence
            AbstractAckRequested el = header.readAsJAXB(unmarshaller);
            message.setAckRequestedElement(el);

            String id = null;
            if (el instanceof com.sun.xml.ws.rm.v200502.AckRequestedElement) {
                id = ((com.sun.xml.ws.rm.v200502.AckRequestedElement) el).getId();
            } else {
                id = ((com.sun.xml.ws.rm.v200702.AckRequestedElement) el).getId();
            }

            InboundSequence seq = provider.getInboundSequence(id);
            if (seq != null) {
                seq.handleAckRequested();
            }
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RmException("Unable to unmarshall AckRequested RM header", e));
        }
    }

    private static void processAckHeader(Header header, Unmarshaller unmarshaller, RMMessage message, RMProvider provider) throws InvalidMessageNumberException, RmException {
        try {
            AbstractSequenceAcknowledgement ackHeader = header.readAsJAXB(unmarshaller);

            String ackHeaderId = null;
            if (ackHeader instanceof com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) {
                ackHeaderId = ((com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) ackHeader).getId();
            } else {
                ackHeaderId = ((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement) ackHeader).getId();
            }

            message.setSequenceAcknowledgementElement(ackHeader);
            OutboundSequence seq = provider.getOutboundSequence(ackHeaderId);
            if (seq != null) {
                seq.handleAckResponse(ackHeader);
            }
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RmException("Unable to unmarshall SequenceAcknowledgement RM header", e));
        }
    }

    private static InboundSequence processSequenceHeader(Header header, Unmarshaller unmarshaller, RMMessage message, RMProvider provider, InboundSequence inseq) throws DuplicateMessageException, InvalidSequenceException, InvalidMessageNumberException, BufferFullException, CloseSequenceException, MessageNumberRolloverException, RmException {
        try {
            //identify sequence and message number from data in header and add
            //the message to the sequence at the specified index.
            //TODO handle error condition seq == null
            AbstractSequence el = header.readAsJAXB(unmarshaller);
            message.setSequenceElement(el);

            String seqid = null;
            long messageNumber;
            if (el instanceof com.sun.xml.ws.rm.v200502.SequenceElement) {
                seqid = ((com.sun.xml.ws.rm.v200502.SequenceElement) el).getId();
                messageNumber = ((com.sun.xml.ws.rm.v200502.SequenceElement) el).getNumber();
            } else {
                seqid = ((com.sun.xml.ws.rm.v200702.SequenceElement) el).getId();
                messageNumber = ((com.sun.xml.ws.rm.v200702.SequenceElement) el).getNumber();
            }

            if (messageNumber == Integer.MAX_VALUE) {
                throw LOGGER.logSevereException(new MessageNumberRolloverException(LocalizationMessages.WSRM_3026_MESSAGE_NUMBER_ROLLOVER(messageNumber), messageNumber));
            }

            inseq = provider.getInboundSequence(seqid);
            if (inseq != null) {
                if (inseq.isClosed()) {
                    throw LOGGER.logSevereException(new CloseSequenceException(LocalizationMessages.WSRM_3029_SEQUENCE_CLOSED(seqid), seqid));
                }
                //add message to ClientInboundSequence
                inseq.set((int) messageNumber, message);
            } else {
                throw LOGGER.logSevereException(new InvalidSequenceException(LocalizationMessages.WSRM_3022_UNKNOWN_SEQUENCE_ID_IN_MESSAGE(seqid), seqid));
            }
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RmException("Unable to unmarshall Sequence RM header", e));
        }
        return inseq;
    }

    /**
     * For each inbound <code>Message</code>, invokes protocol logic dictated by the contents of the
     * WS-RM protocol headers on the message.
     * <ul>
     *  <li><b>Sequence Header</b><br>Adds the message to the instance data of this incoming sequence according using the
     *          Sequence Identifier and Message Number in the header.</li>
     *  <li><b>SequenceAcknowledgement Header</b><br>Invokes the <code>handleAckResponse</code> method of the companion 
     *          <code>ClientOutboundSequence</code> which marks acknowledged messages as delivered.</li>
     *  <li><b>AckRequested Header</b><br>Constructs a <code>SequenceAcknowledgementElement</code> reflecting the messages 
     *          belonging to this sequence that have been received.  Sets the resulting 
     *      
     * <code>SequenceAcknowledgementElement</code> in the state of the companion <code>ClientOutboundSequence</code>.</li>
     * </ul>
     * <br>
     * @param message The inbound <code>Message</code>.
     */
    public static void processMessage(RMMessage message, Unmarshaller unmarshaller, RMProvider provider, RmVersion rmVersion) throws RmException {
        /*
         * Check for each RM header type and do the right thing in RMProvider
         * depending on the type.
         */
        InboundSequence inseq = null;
        Header header = getHeader(message, "Sequence", rmVersion);
        if (header != null) {
            inseq = processSequenceHeader(header, unmarshaller, message, provider, inseq);
        }

        header = getHeader(message, "SequenceAcknowledgement", rmVersion);
        if (header != null) {
            processAckHeader(header, unmarshaller, message, provider);
        }

        header = getHeader(message, "AckRequested", rmVersion);
        if (header != null) {
            processAckRequestHeader(header, unmarshaller, message, provider);
        } else {
            // FIXME - We need to be checking whether this is a ServerInboundSequence
            // in a port with a two-way operation.  This is the case where MS
            // puts a SequenceAcknowledgement on every message.
            // Need to check this with the latest CTP
            // Currently with Dec CTP the client message
            // does not have AckRequested element
            // but they are expecting a SequenceAcknowledgement
            // Hack for now
            if (inseq != null) {
                inseq.handleAckRequested();
            } else {
            //we can get here if there is no sequence header.  Perhaps this
            //is a ClientInboundSequence where the OutboundSequence has no two-ways
            }
        }
    }
    
    /**
     * Get the RM Header Element with the specified name from the underlying
     * JAX-WS message's HeaderList
     * @param name The name of the Header to find.
     */
    private static Header getHeader(RMMessage message, String name, RmVersion rmVersion) {
        return (message.getHeaders() != null) ? message.getHeaders().get(rmVersion.namespaceUri, name, true) : null;
    }
}
