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

import com.sun.xml.ws.rm.runtime.sequence.UnknownSequenceException;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.rm.CloseSequenceException;
import com.sun.xml.ws.rm.CreateSequenceException;
import com.sun.xml.ws.rm.MessageNumberRolloverException;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.TerminateSequenceException;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rm.runtime.sequence.Sequence.AckRange;
import com.sun.xml.ws.rm.v200702.AcceptType;
import com.sun.xml.ws.rm.v200702.AckRequestedElement;
import com.sun.xml.ws.rm.v200702.CloseSequenceElement;
import com.sun.xml.ws.rm.v200702.CloseSequenceResponseElement;
import com.sun.xml.ws.rm.v200702.CreateSequenceElement;
import com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement;
import com.sun.xml.ws.rm.v200702.Identifier;
import com.sun.xml.ws.rm.v200702.OfferType;
import com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement;
import com.sun.xml.ws.rm.v200702.SequenceElement;
import com.sun.xml.ws.rm.v200702.TerminateSequenceElement;
import com.sun.xml.ws.rm.v200702.TerminateSequenceResponseElement;
import com.sun.xml.ws.rm.v200702.UsesSequenceSTR;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class Rm11ClientSession extends ClientSession {

    private static final RmLogger LOGGER = RmLogger.getLogger(Rm11ClientSession.class);

    public Rm11ClientSession(Configuration configuration, ProtocolCommunicator communicator) {
        super(configuration, communicator);
    }

    @Override
    protected void openRmSession(String offerInboundSequenceId, SecurityTokenReferenceType strType) throws RmException {
        CreateSequenceElement csElement = new CreateSequenceElement();
        csElement.setAcksTo(new W3CEndpointReference(configuration.getAddressingVersion().anonymousEpr.asSource("AcksTo")));

        if (offerInboundSequenceId != null) {
            Identifier offerIdentifier = new Identifier();
            offerIdentifier.setValue(offerInboundSequenceId);

            OfferType offer = new OfferType();
            offer.setIdentifier(offerIdentifier);
            // Microsoft does not accept CreateSequence messages if AcksTo and Offer/Endpoint are not the same
            offer.setEndpoint(csElement.getAcksTo());
            csElement.setOffer(offer);
        }
        if (strType != null) {
            csElement.setSecurityTokenReference(strType);
        }

        Message csMessage = communicator.createMessage(csElement);
        if (strType != null) {
            HeaderList headerList = csMessage.getHeaders();
            UsesSequenceSTR usesSequenceSTR = new UsesSequenceSTR();
            usesSequenceSTR.getOtherAttributes().put(communicator.soapMustUnderstandAttributeName, "true");
            headerList.add(Headers.create(RmVersion.WSRM11.jaxbContext, usesSequenceSTR));
        }

        Message csResponseMessage = communicator.send(csMessage, RmVersion.WSRM11.createSequenceAction);
        if (csResponseMessage == null) {
            throw LOGGER.logSevereException(new CreateSequenceException(LocalizationMessages.WSRM_1114_NULL_RESPONSE_ON_PROTOCOL_MESSAGE_REQUEST("CreateSequenceResponse")));
        }
        if (csResponseMessage.isFault()) {
            throw LOGGER.logSevereException(new CreateSequenceException(LocalizationMessages.WSRM_1115_PROTOCOL_MESSAGE_REQUEST_REFUSED("CreateSequence"), csResponseMessage));
        }

        CreateSequenceResponseElement csrElement = communicator.unmarshallMessage(csResponseMessage);
        outboundSequenceId = csrElement.getIdentifier().getValue();

        long expirationTime = Configuration.UNSPECIFIED;
        if (csrElement.getExpires() != null && !"PT0S".equals(csrElement.getExpires().getValue().toString())) {
            expirationTime = csrElement.getExpires().getValue().getTimeInMillis(Calendar.getInstance()) + System.currentTimeMillis();
        }
        sequenceManager.createOutboudSequence(outboundSequenceId, expirationTime);

        if (offerInboundSequenceId != null) {
            AcceptType accept = csrElement.getAccept();
            if (accept == null || !communicator.getDestination().getAddress().equals(new WSEndpointReference(accept.getAcksTo()).getAddress())) {
                throw new CreateSequenceException(
                        LocalizationMessages.WSRM_1116_ACKS_TO_NOT_EQUAL_TO_ENDPOINT_DESTINATION(accept.getAcksTo().toString(), communicator.getDestination()),
                        inboundSequenceId);
            }
            inboundSequenceId = offerInboundSequenceId;
            sequenceManager.createInboundSequence(inboundSequenceId, Configuration.UNSPECIFIED);
        }
    }

    @Override
    protected void appendSequenceHeader(Message outboundMessage) throws UnknownSequenceException, MessageNumberRolloverException {
        long messageNumber = sequenceManager.getSequence(outboundSequenceId).getNextMessageId();

        SequenceElement sequenceHeaderElement = new SequenceElement();
        sequenceHeaderElement.setNumber(messageNumber);
        sequenceHeaderElement.setId(outboundSequenceId);

        outboundMessage.getHeaders().add(communicator.createHeader(sequenceHeaderElement));
    }

    @Override
    protected void appendAckRequestedHeader(Message outboundMessage) {
        AckRequestedElement ackRequestedElement = new AckRequestedElement();
        ackRequestedElement.setId(outboundSequenceId);

        outboundMessage.getHeaders().add(communicator.createHeader(ackRequestedElement));
    }

    @Override
    protected void appendSequenceAcknowledgementHeader(Message outboundMessage) throws UnknownSequenceException {
        SequenceAcknowledgementElement ackElement = new SequenceAcknowledgementElement();
        Identifier identifier = new Identifier();
        identifier.setValue(inboundSequenceId);
        ackElement.setIdentifier(identifier);

        final List<AckRange> acknowledgedIndexes = sequenceManager.getSequence(inboundSequenceId).getAcknowledgedMessageIds();
        if (acknowledgedIndexes != null && !acknowledgedIndexes.isEmpty()) {
            for (Sequence.AckRange range : acknowledgedIndexes) {
                ackElement.addAckRange(range.lower, range.upper);
            }
        } else {
            ackElement.setNone(new SequenceAcknowledgementElement.None());
        }

        if (sequenceManager.getSequence(inboundSequenceId).isClosed()) {
            SequenceAcknowledgementElement.Final finalElement = new SequenceAcknowledgementElement.Final();
            ackElement.setFinal(finalElement);
        }

// TODO move this to server side - we don't have a buffer support on the client side
//        if (configuration.getDestinationBufferQuota() != Configuration.UNSPECIFIED) {
//            ackElement.setBufferRemaining(-1/*calculate remaining quota*/);
//        }

        outboundMessage.getHeaders().add(communicator.createHeader(ackElement));
    }

    @Override
    protected void closeOutboundSequence() throws RmException {
        final Message request = communicator.createMessage(new CloseSequenceElement(
                outboundSequenceId,
                sequenceManager.getSequence(outboundSequenceId).getLastMessageId()));

        appendSequenceAcknowledgementHeader(request);

        Message response = communicator.send(request, RmVersion.WSRM11.closeSequenceAction);
        if (response == null) {
            throw new CloseSequenceException(LocalizationMessages.WSRM_1114_NULL_RESPONSE_ON_PROTOCOL_MESSAGE_REQUEST("CloseSequenceResponse"));
        }

        processInboundMessageHeaders(response.getHeaders(), false);

        if (response.isFault()) {
            throw new CloseSequenceException(LocalizationMessages.WSRM_1115_PROTOCOL_MESSAGE_REQUEST_REFUSED("CloseSequence"), response);
        }

        CloseSequenceResponseElement csrElement = communicator.unmarshallMessage(response); // consuming message here

        if (!outboundSequenceId.equals(csrElement.getIdentifier().getValue())) {
            throw new CloseSequenceException(
                    LocalizationMessages.WSRM_1119_UNEXPECTED_SEQUENCE_ID_IN_CLOSE_SR(csrElement.getIdentifier().getValue(), outboundSequenceId));
        }
    }

    @Override
    protected void terminateOutboundSequence() throws RmException {
        final Message request = communicator.createMessage(new TerminateSequenceElement(
                outboundSequenceId,
                sequenceManager.getSequence(outboundSequenceId).getLastMessageId()));

        Message response = null;
        try {
            response = communicator.send(request, RmVersion.WSRM11.terminateSequenceAction);
            if (response == null) {
                throw new TerminateSequenceException(LocalizationMessages.WSRM_1114_NULL_RESPONSE_ON_PROTOCOL_MESSAGE_REQUEST("TerminateSequenceResponse"));
            }

            processInboundMessageHeaders(response.getHeaders(), false);

            if (response.isFault()) {
                throw new TerminateSequenceException(LocalizationMessages.WSRM_1115_PROTOCOL_MESSAGE_REQUEST_REFUSED("TerminateSequence"), response);
            }

            String responseAction = communicator.getAction(response);
            if (RmVersion.WSRM11.terminateSequenceAction.equals(responseAction)) {
                TerminateSequenceElement tsElement = communicator.unmarshallMessage(response);
                response = null; // marking response as consumed...

                sequenceManager.terminateSequence(tsElement.getIdentifier().getValue());
            } else if (RmVersion.WSRM11.terminateSequenceResponseAction.equals(responseAction)) {
                TerminateSequenceResponseElement tsrElement = communicator.unmarshallMessage(response);
                response = null; // marking response as consumed...

                if (!outboundSequenceId.equals(tsrElement.getIdentifier().getValue())) {
                    throw new TerminateSequenceException(
                            LocalizationMessages.WSRM_1117_UNEXPECTED_SEQUENCE_ID_IN_TERMINATE_SR(tsrElement.getIdentifier().getValue(), outboundSequenceId));
                }
            }
        } finally {
            if (response != null) {
                response.consume();
            }
        }
    }

    @Override
    protected void processSequenceHeader(HeaderList inboundMessageHeaders) throws RmException {
        SequenceElement sequenceElement = communicator.readHeaderAsUnderstood(inboundMessageHeaders, "Sequence");
        if (sequenceElement != null) {
            assertSequenceIdInInboundHeader(inboundSequenceId, sequenceElement.getId());
            sequenceManager.getSequence(sequenceElement.getId()).acknowledgeMessageId(sequenceElement.getMessageNumber());
        } else {
            throw new RmException(LocalizationMessages.WSRM_1118_MANDATORY_HEADER_NOT_PRESENT("wsrm:Sequence"));
        }
    }

    @Override
    protected void processAcknowledgementHeader(HeaderList inboundMessageHeaders) throws RmException {
        SequenceAcknowledgementElement ackElement = communicator.readHeaderAsUnderstood(inboundMessageHeaders, "SequenceAcknowledgement");

        if (ackElement != null) {
            assertSequenceIdInInboundHeader(outboundSequenceId, ackElement.getId());

            List<Sequence.AckRange> ranges = new LinkedList<Sequence.AckRange>();
            if (ackElement.getNone() == null) {
                if (!ackElement.getNack().isEmpty()) {
                    List<BigInteger> nacks = new ArrayList<BigInteger>(ackElement.getNack());
                    Collections.sort(nacks);
                    long lastLowerBound = 1;
                    for (BigInteger nackId : nacks) {
                        if (lastLowerBound == nackId.longValue()) {
                            lastLowerBound++;
                        } else {
                            ranges.add(new Sequence.AckRange(lastLowerBound, nackId.longValue() - 1));
                            lastLowerBound = nackId.longValue() + 1;
                        }
                    }

                    long lastMessageId = sequenceManager.getSequence(outboundSequenceId).getLastMessageId();
                    if (lastLowerBound <= lastMessageId) {
                        ranges.add(new Sequence.AckRange(lastLowerBound, lastMessageId));
                    }
                } else if (ackElement.getAcknowledgementRange() != null && !ackElement.getAcknowledgementRange().isEmpty()) {
                    for (SequenceAcknowledgementElement.AcknowledgementRange rangeElement : ackElement.getAcknowledgementRange()) {
                        ranges.add(new Sequence.AckRange(rangeElement.getLower().longValue(), rangeElement.getUpper().longValue()));
                    }
                }
            }

            sequenceManager.getSequence(outboundSequenceId).acknowledgeMessageIds(ranges);

        // TODO handle final and remaining buffer in the header
        // ackElement.getBufferRemaining();
        // ackElement.getFinal();
        }
    }

    @Override
    protected void processAckRequestedHeader(HeaderList inboundMessageHeaders) throws RmException {
        AckRequestedElement ackRequestedElement = communicator.readHeaderAsUnderstood(inboundMessageHeaders, "AckRequested");
        if (ackRequestedElement != null) {
            String sequenceId = ackRequestedElement.getId();
            assertSequenceIdInInboundHeader(inboundSequenceId, sequenceId);
            sequenceManager.getSequence(sequenceId).setAckRequestedFlag();
        }
    }
}
