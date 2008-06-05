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

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.RmRuntimeException;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.TerminateSequenceException;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.v200502.AcceptType;
import com.sun.xml.ws.rm.v200502.CreateSequenceElement;
import com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement;
import com.sun.xml.ws.rm.v200502.Identifier;
import com.sun.xml.ws.rm.v200502.OfferType;
import com.sun.xml.ws.rm.v200502.SequenceElement;
import com.sun.xml.ws.rm.v200502.SequenceElement.LastMessage;
import com.sun.xml.ws.rm.v200502.TerminateSequenceElement;
import com.sun.xml.ws.rm.v200702.TerminateSequenceResponseElement;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import java.util.Calendar;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class Rm10ClientSession extends ClientSession {

    private static final RmLogger LOGGER = RmLogger.getLogger(Rm10ClientSession.class);

    public Rm10ClientSession(Configuration configuration, ProtocolCommunicator communicator) {
        super(configuration, communicator);
    }

    @Override
    protected void openRmSession(String offerInboundSequenceId, SecurityTokenReferenceType strType) throws RmRuntimeException {
        CreateSequenceElement csElement = new CreateSequenceElement();
        csElement.setAcksTo(new W3CEndpointReference(configuration.getAddressingVersion().anonymousEpr.asSource("AcksTo")));

        if (offerInboundSequenceId != null) {
            Identifier offerIdentifier = new Identifier();
            offerIdentifier.setValue(offerInboundSequenceId);

            OfferType offer = new OfferType();
            offer.setIdentifier(offerIdentifier);
            csElement.setOffer(offer);
        }
        if (strType != null) {
            csElement.setSecurityTokenReference(strType);
        }

        PacketAdapter requestAdapter = PacketAdapter.create(configuration, communicator.createEmptyRequestPacket());
        requestAdapter.setMessage(csElement, RmVersion.WSRM10.createSequenceAction);

        PacketAdapter responseAdapter = PacketAdapter.create(configuration, communicator.send(requestAdapter.getPacket()));
        if (responseAdapter == null) {
            throw LOGGER.logSevereException(new RmRuntimeException(LocalizationMessages.WSRM_1114_NULL_RESPONSE_ON_PROTOCOL_MESSAGE_REQUEST("CreateSequenceResponse")));
        }
        if (responseAdapter.isFault()) {
            // FIXME: pass fault value into the exception
            throw LOGGER.logSevereException(new RmRuntimeException(LocalizationMessages.WSRM_1115_PROTOCOL_MESSAGE_REQUEST_REFUSED("CreateSequence")));
        }

        CreateSequenceResponseElement csrElement = responseAdapter.unmarshallMessage();
        responseAdapter.getPacket();
        
        outboundSequenceId = csrElement.getIdentifier().getValue();

        long expirationTime = Configuration.UNSPECIFIED;
        if (csrElement.getExpires() != null && !"PT0S".equals(csrElement.getExpires().getValue().toString())) {
            expirationTime = csrElement.getExpires().getValue().getTimeInMillis(Calendar.getInstance()) + System.currentTimeMillis();
        }
        sequenceManager.createOutboudSequence(outboundSequenceId, expirationTime);


        if (offerInboundSequenceId != null) {
            AcceptType accept = csrElement.getAccept();
            if (accept == null || !communicator.getDestination().getAddress().equals(new WSEndpointReference(accept.getAcksTo()).getAddress())) {
                throw new RmRuntimeException(LocalizationMessages.WSRM_1116_ACKS_TO_NOT_EQUAL_TO_ENDPOINT_DESTINATION(accept.getAcksTo().toString(), communicator.getDestination()));
            }
            inboundSequenceId = offerInboundSequenceId;
            sequenceManager.createInboundSequence(inboundSequenceId, Configuration.UNSPECIFIED);
        }
    }

    @Override
    protected void closeOutboundSequence() throws RmException {
        PacketAdapter requestAdapter = PacketAdapter.create(configuration, communicator.createEmptyRequestPacket());
        requestAdapter.setEmptyMessage(RmVersion.WSRM10.lastAction);
        requestAdapter.appendSequenceAcknowledgementHeader(
                inboundSequenceId,
                sequenceManager.getSequence(inboundSequenceId).getAcknowledgedMessageIds());
        
        SequenceElement sequenceElement = new SequenceElement();
        sequenceElement.setId(outboundSequenceId);
        sequenceElement.setNumber(sequenceManager.getSequence(outboundSequenceId).getLastMessageId());
        sequenceElement.setLastMessage(new LastMessage());

        requestAdapter.appendHeader(sequenceElement);
        
        PacketAdapter responseAdapter = null;
        try {
            responseAdapter = PacketAdapter.create(configuration, communicator.send(requestAdapter.getPacket()));
            if (responseAdapter.containsMessage()) {
                processInboundMessageHeaders(responseAdapter, false);
                if (responseAdapter.isFault()) {
                    // FIXME: refactor the exception creation - we should not pass the SOAP fault directly into the exception
                    throw new RmException(LocalizationMessages.WSRM_1115_PROTOCOL_MESSAGE_REQUEST_REFUSED("Last message"), responseAdapter.message);
                }
            }
        } finally {
            if (responseAdapter != null) {
                responseAdapter.consume(); // we need to consume message as we didn't read it (e.g. as JAXB bean)
            }
        }
    }

    @Override
    protected void terminateOutboundSequence() throws RmException {
        PacketAdapter requestAdapter = PacketAdapter.create(configuration, communicator.createEmptyRequestPacket());
        requestAdapter.setMessage(new TerminateSequenceElement(outboundSequenceId),  RmVersion.WSRM10.terminateSequenceAction);
        requestAdapter.appendSequenceAcknowledgementHeader(
                inboundSequenceId, 
                sequenceManager.getSequence(inboundSequenceId).getAcknowledgedMessageIds());
        
        PacketAdapter responseAdapter = null;
        try {
            responseAdapter = PacketAdapter.create(configuration, communicator.send(requestAdapter.getPacket()));
            if (!responseAdapter.containsMessage()) {
                throw new TerminateSequenceException(LocalizationMessages.WSRM_1114_NULL_RESPONSE_ON_PROTOCOL_MESSAGE_REQUEST("TerminateSequenceResponse"));
            }

            processInboundMessageHeaders(responseAdapter, false);

            if (responseAdapter.isFault()) {
                // FIXME: refactor the exception creation - we should not pass the SOAP fault directly into the exception
                throw new TerminateSequenceException(LocalizationMessages.WSRM_1115_PROTOCOL_MESSAGE_REQUEST_REFUSED("TerminateSequence"), responseAdapter.message);
            }

            String responseAction = responseAdapter.getWsaAction();
            if (RmVersion.WSRM10.terminateSequenceAction.equals(responseAction)) {
                TerminateSequenceElement tsElement = responseAdapter.unmarshallMessage();

                sequenceManager.terminateSequence(tsElement.getIdentifier().getValue());
            } else if (RmVersion.WSRM10.terminateSequenceResponseAction.equals(responseAction)) {
                TerminateSequenceResponseElement tsrElement = responseAdapter.unmarshallMessage();

                if (!outboundSequenceId.equals(tsrElement.getIdentifier().getValue())) {
                    throw new TerminateSequenceException(
                            LocalizationMessages.WSRM_1117_UNEXPECTED_SEQUENCE_ID_IN_TERMINATE_SR(tsrElement.getIdentifier().getValue(), outboundSequenceId));
                }
            }
        } finally {
            if (responseAdapter != null) {
                responseAdapter.consume();
            }
        }
    }
}
