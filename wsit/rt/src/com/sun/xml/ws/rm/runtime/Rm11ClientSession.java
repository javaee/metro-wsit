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

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.rm.CloseSequenceException;
import com.sun.xml.ws.rm.CreateSequenceException;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.TerminateSequenceException;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
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
import com.sun.xml.ws.rm.v200702.UsesSequenceSTR;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class Rm11ClientSession extends ClientSession {

    private static final RmLogger LOGGER = RmLogger.getLogger(Rm11ClientSession.class);

    public Rm11ClientSession(WSDLPort wsdlPort, WSBinding binding, ProtocolCommunicator communicator, Configuration configuration) {
        super(wsdlPort, binding, communicator, configuration);
    }

    @Override
    protected Message prepareHandshakeRequest(String offerInboundSequenceId, SecurityTokenReferenceType strType) {
        CreateSequenceElement csElement = new com.sun.xml.ws.rm.v200702.CreateSequenceElement();
        if (configuration.getAddressingVersion() == AddressingVersion.W3C) {
            csElement.setAcksTo(new W3CEndpointReference(AddressingVersion.W3C.anonymousEpr.asSource("AcksTo")));
        } else {
            // TODO L10N
            throw LOGGER.logSevereException(new IllegalStateException("Unsupported addressing version"));
        }

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

        Message csMessage = Messages.create(configuration.getRMVersion().jaxbContext, csElement, configuration.getSoapVersion());
        if (strType != null) {
            HeaderList headerList = csMessage.getHeaders();
            UsesSequenceSTR usesSequenceSTR = new UsesSequenceSTR();
            usesSequenceSTR.getOtherAttributes().put(new QName(configuration.getSoapVersion().nsUri, "mustUnderstand"), "true");
            headerList.add(Headers.create(configuration.getRMVersion().jaxbContext, usesSequenceSTR));
        }
        return csMessage;
    }

    @Override
    protected String processHandshakeResponseMessage(Message handshakeResponseMessage) throws CreateSequenceException, RmException {
        if (handshakeResponseMessage.isFault()) {
            // TODO L10N
            throw LOGGER.logSevereException(new CreateSequenceException("CreateSequence was refused by the RMDestination", handshakeResponseMessage));
        }

        CreateSequenceResponseElement csrMessage = unmarshallResponse(handshakeResponseMessage);
        Identifier idOutbound = csrMessage.getIdentifier();
        // TODO accept = ((com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement) csr).getAccept();
        return idOutbound.getValue();
    }

    @Override
    protected void appendSequenceHeader(Message outboundMessage) throws UnknownSequenceException {
        long messageNumber = sequenceManager.getSequence(outboundSequenceId).getNextMessageId();

        SequenceElement sequenceHeaderElement = new SequenceElement();
        sequenceHeaderElement.setNumber(messageNumber);
        sequenceHeaderElement.setId(outboundSequenceId);

        outboundMessage.getHeaders().add(createHeader(sequenceHeaderElement));
    }

    @Override
    protected void appendAckRequestedHeader(Message outboundMessage) {
        AckRequestedElement ackRequestedElement = new AckRequestedElement();
        ackRequestedElement.setId(outboundSequenceId);

        outboundMessage.getHeaders().add(createHeader(ackRequestedElement));
    }

    @Override
    protected void appendSequenceAcknowledgementHeader(Message outboundMessage) throws UnknownSequenceException {
        SequenceAcknowledgementElement ackElement = new SequenceAcknowledgementElement();
        Identifier identifier = new Identifier();
        identifier.setValue(inboundSequenceId);
        ackElement.setIdentifier(identifier);
        // In RM v1.1 the SequenceAcknowledgmenet.Final needs to be added when CloseSequence message is processed
        if (sequenceManager.getSequence(inboundSequenceId).isClosed()) {
            com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement.Final finalElement = new com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement.Final();
            ((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement) ackElement).setFinal(finalElement);
        }

        if (configuration.getDestinationBufferQuota() != Configuration.UNSPECIFIED) {
            ackElement.setBufferRemaining(-1); // TODO
        }

        for (Sequence.AckRange range : sequenceManager.getSequence(outboundSequenceId).getAcknowledgedIndexes()) {
            ackElement.addAckRange(range.lower, range.upper);
        }

        outboundMessage.getHeaders().add(createHeader(ackElement));
    }

    @Override
    protected void processInboundMessageHeaders(Message inboundMessage) throws RmException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void closeOutboundSequence() throws RmException {
        Identifier idClose = new Identifier();
        idClose.setValue(outboundSequenceId);

        CloseSequenceElement cs = new CloseSequenceElement();
        cs.setIdentifier(idClose);
        cs.setLastMsgNumber(sequenceManager.getSequence(outboundSequenceId).getLastMessageId());

        Message closeSequenceRequest = Messages.create(configuration.getRMVersion().jaxbContext, cs, configuration.getSoapVersion());

        Message response = communicator.send(closeSequenceRequest, configuration.getRMVersion().closeSequenceAction);
        if (response != null && response.isFault()) {
            // TODO L10N
            throw LOGGER.logException(new CloseSequenceException("CloseSequence was refused by the RMDestination", response), Level.WARNING);
        }

        CloseSequenceResponseElement csr = unmarshallResponse(response);
        // TODO process CloseSequenceRespose element...
    }

    @Override
    protected void terminateOutboundSequence() throws RmException {
        //TODO piggyback an acknowledgement if one is pending
        //seq.processAcknowledgement(new RMMessage(request));
        TerminateSequenceElement ts = new TerminateSequenceElement();
        Identifier idTerminate = new Identifier();
        idTerminate.setValue(outboundSequenceId);
        ts.setIdentifier(idTerminate);

        Message terminateSequenceRequest = Messages.create(configuration.getRMVersion().jaxbContext, ts, configuration.getSoapVersion());
        Message response = null;
        try {
            response = communicator.send(terminateSequenceRequest, configuration.getRMVersion().terminateSequenceAction);
            if (response != null && response.isFault()) {
                throw LOGGER.logException(new TerminateSequenceException("There was an error trying to terminate the sequence ", response), Level.WARNING);
            }
            //TODO process TerminateSequenceResponse element? It may have a TerminateSequence for reverse sequence on it as well as ack headers
        } finally {
            if (response != null) {
                response.consume();
            }
        }
    }
}
