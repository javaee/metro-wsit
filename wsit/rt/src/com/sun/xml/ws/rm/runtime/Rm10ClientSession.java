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
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.rm.CreateSequenceException;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.v200502.AckRequestedElement;
import com.sun.xml.ws.rm.v200502.CreateSequenceElement;
import com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement;
import com.sun.xml.ws.rm.v200502.Identifier;
import com.sun.xml.ws.rm.v200502.OfferType;
import com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement;
import com.sun.xml.ws.rm.v200502.SequenceElement;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class Rm10ClientSession extends ClientSession {

    private static final RmLogger LOGGER = RmLogger.getLogger(Rm10ClientSession.class);

    public Rm10ClientSession(WSDLPort wsdlPort, WSBinding binding, ProtocolCommunicator communicator, Configuration configuration) {
        super(wsdlPort, binding, communicator, configuration);
    }

    @Override
    protected Message prepareHandshakeRequest(String offerInboundSequenceId, SecurityTokenReferenceType strType) {
        CreateSequenceElement scElement = new CreateSequenceElement();

        if (configuration.getAddressingVersion() == AddressingVersion.W3C) {
            scElement.setAcksTo(new W3CEndpointReference(AddressingVersion.W3C.anonymousEpr.asSource("AcksTo")));
        } else {
            // TODO L10N
            throw LOGGER.logSevereException(new IllegalStateException("Unsupported addressing version"));
        }

        if (offerInboundSequenceId != null) {
            Identifier offerIdentifier = new Identifier();
            offerIdentifier.setValue(offerInboundSequenceId);

            OfferType offer = new OfferType();
            offer.setIdentifier(offerIdentifier);
            scElement.setOffer(offer);
        }
        if (strType != null) {
            scElement.setSecurityTokenReference(strType);
        }

        return Messages.create(configuration.getRMVersion().jaxbContext, scElement, configuration.getSoapVersion());
    }

    @Override
    protected String processHandshakeResponseMessage(Message handshakeResponseMessage) throws CreateSequenceException, RmException {
        if (handshakeResponseMessage.isFault()) {
            // TODO L10N
            throw LOGGER.logSevereException(new CreateSequenceException("CreateSequence was refused by the RMDestination \n ", handshakeResponseMessage));
        }

        CreateSequenceResponseElement csrMessage = unmarshallResponse(handshakeResponseMessage);
        Identifier idOutbound = csrMessage.getIdentifier();
        // TODO accept = ((com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement) csr).getAccept();
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

    protected void appendAckRequestedHeader(Message outboundMessage) {
        AckRequestedElement ackRequestedElement = new AckRequestedElement();
        ackRequestedElement.setId(outboundSequenceId);
        
        outboundMessage.getHeaders().add(createHeader(ackRequestedElement));
    }

    protected void appendSequenceAcknowledgementHeader(Message outboundMessage) throws UnknownSequenceException {
        SequenceAcknowledgementElement ackElement = new SequenceAcknowledgementElement();
        Identifier identifier = new Identifier();
        identifier.setValue(inboundSequenceId);
        ackElement.setIdentifier(identifier);

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
    protected void disconnect() throws RmException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
