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

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.rm.faults.AbstractRmSoapFault;
import com.sun.xml.ws.rm.faults.CreateSequenceRefusedFault;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.faults.UnknownSequenceFault;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rm.v200702.AcceptType;
import com.sun.xml.ws.rm.v200702.CloseSequenceElement;
import com.sun.xml.ws.rm.v200702.CloseSequenceResponseElement;
import com.sun.xml.ws.rm.v200702.CreateSequenceElement;
import com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement;
import com.sun.xml.ws.rm.v200702.Identifier;
import com.sun.xml.ws.rm.v200702.OfferType;
import com.sun.xml.ws.rm.v200702.TerminateSequenceElement;
import com.sun.xml.ws.rm.v200702.TerminateSequenceResponseElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class Rm11ServerTube extends AbstractRmServerTube {

    private static final RmLogger LOGGER = RmLogger.getLogger(Rm11ServerTube.class);

    Rm11ServerTube(Configuration configuration, Tube tubelineHead) {
        super(configuration, tubelineHead);
    }

    Rm11ServerTube(Rm11ServerTube original, TubeCloner cloner) {
        super(original, cloner);
    }

    @Override
    public Rm11ServerTube copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new Rm11ServerTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    PacketAdapter processVersionSpecificProtocolRequest( PacketAdapter requestAdapter) throws AbstractRmSoapFault {
        if (RmVersion.WSRM11.closeSequenceAction.equals(requestAdapter.getWsaAction())) {
            return handleCloseSequenceAction(requestAdapter);
        } else if (RmVersion.WSRM11.makeConnectionAction.equals(requestAdapter.getWsaAction())) {
            return handleMakeConnectionAction(requestAdapter);
        } else {
            return super.processVersionSpecificProtocolRequest(requestAdapter);
        }
    }

    @Override
    PacketAdapter handleCreateSequenceAction( PacketAdapter requestAdapter) throws CreateSequenceRefusedFault {
        CreateSequenceElement csElement = requestAdapter.unmarshallMessage();

        long expirationTime = Configuration.UNSPECIFIED;
        if (csElement.getExpires() != null && !"PT0S".equals(csElement.getExpires().getValue().toString())) {
            expirationTime = csElement.getExpires().getValue().getTimeInMillis(Calendar.getInstance()) + System.currentTimeMillis();
        }

        String offeredId = null;
        long offeredExpirationTime = Configuration.UNSPECIFIED;
        OfferType offerElement = csElement.getOffer();
        if (offerElement != null) {
            Identifier id = offerElement.getIdentifier();
            if (id != null) {
                offeredId = id.getValue();
                if (sequenceManager.isValid(offeredId)) { // we already have such sequence
                    throw LOGGER.logSevereException(new CreateSequenceRefusedFault(
                            configuration,
                            requestAdapter.getPacket(),
                            LocalizationMessages.WSRM_1137_OFFERED_ID_ALREADY_IN_USE(offeredId)));
                }
            }

            if (offerElement.getExpires() != null && !"PT0S".equals(offerElement.getExpires().getValue().toString())) {
                offeredExpirationTime = offerElement.getExpires().getValue().getTimeInMillis(Calendar.getInstance()) + System.currentTimeMillis();
            }
        }


        // FIXME: The STR processing should probably only check if the 
        // com.sun.xml.ws.runtime.util.Session was started by security tube
        // and if the STR id equals to the one in this session...

        // Read STR element in csrElement if any
        com.sun.xml.ws.security.secext10.SecurityTokenReferenceType strType = csElement.getSecurityTokenReference();

        String receivedSctId = null;
        if (strType != null) { // RM messaging should be bound to a secured session
            String activeSctId = requestAdapter.getSecurityContextTokenId();
            if (activeSctId == null) {
                throw LOGGER.logSevereException(new CreateSequenceRefusedFault(
                        configuration,
                        requestAdapter.getPacket(),
                        LocalizationMessages.WSRM_1133_NO_SECURITY_TOKEN_IN_REQUEST_PACKET()));
            }
            try {
                receivedSctId = Utilities.extractSecurityContextTokenId(strType);
            } catch (RmException ex) {
                throw LOGGER.logSevereException(new CreateSequenceRefusedFault(
                        configuration,
                        requestAdapter.getPacket(),
                        ex.getMessage()));
            }

            if (!activeSctId.equals(receivedSctId)) {
                throw LOGGER.logSevereException(new CreateSequenceRefusedFault(
                        configuration,
                        requestAdapter.getPacket(),
                        LocalizationMessages.WSRM_1131_SECURITY_TOKEN_AUTHORIZATION_ERROR(receivedSctId, activeSctId)));
            }
        }


        Sequence inboundSequence = sequenceManager.createInboundSequence(sequenceManager.generateSequenceUID(), receivedSctId, expirationTime);
        if (offeredId != null) {
            sequenceManager.createOutboundSequence(offeredId, receivedSctId, offeredExpirationTime);
            sequenceManager.bindSequences(inboundSequence.getId(), offeredId);
        }

        if (!requestAdapter.hasSession()) { // security did not start session - we must do it
            Utilities.startSession(inboundSequence.getId());
        }

//      //initialize CreateSequenceResponseElement
        CreateSequenceResponseElement crsElement = new CreateSequenceResponseElement();
        Identifier id2 = new Identifier();
        id2.setValue(inboundSequence.getId());
        crsElement.setIdentifier(id2);
        AcceptType accept = new AcceptType();

        URI dest;
        if (offeredId != null) {
            try {
                dest = new URI(requestAdapter.getDestination());
            } catch (URISyntaxException e) {
                throw LOGGER.logSevereException(new CreateSequenceRefusedFault(
                        configuration,
                        requestAdapter.getPacket(),
                        LocalizationMessages.WSRM_1129_INVALID_VALUE_OF_MESSAGE_HEADER("To", "CreateSequence", requestAdapter.getDestination())), e);
            } catch (NullPointerException e) {
                throw LOGGER.logSevereException(new CreateSequenceRefusedFault(
                        configuration,
                        requestAdapter.getPacket(),
                        LocalizationMessages.WSRM_1130_MISSING_MESSAGE_HEADER("To", "CreateSequence", requestAdapter.getDestination())), e);
            }

            W3CEndpointReference endpointReference;
            WSEndpointReference wsepr = new WSEndpointReference(dest, configuration.getAddressingVersion());
            if (configuration.getAddressingVersion() == AddressingVersion.W3C) {
                endpointReference = (W3CEndpointReference) wsepr.toSpec();
                accept.setAcksTo(endpointReference);
            }
            crsElement.setAccept(accept);
        }

//        request.assertOneWay(false); // what is this for?
        return requestAdapter.createServerResponse(crsElement, configuration.getRmVersion().createSequenceResponseAction);
    }

    /**
     * Handles the close sequence message request processing
     * 
     * @param  requestAdapter close sequence message request packet adapter
     * 
     * @return close sequence response message wrapped in a response packet adapter
     * 
     * @exception UnknownSequenceFault if there is no such sequence registered with current 
     *            sequence manager.
     */
    PacketAdapter handleCloseSequenceAction(PacketAdapter requestAdapter) throws UnknownSequenceFault {
        CloseSequenceElement closeSeqElement = requestAdapter.unmarshallMessage();

        Sequence inboundSequence = getSequenceOrSoapFault(requestAdapter.getPacket(), closeSeqElement.getIdentifier().getValue());

        // TODO handle last message number - pass it to the sequence so that it can allocate new unacked messages if necessary
        // int lastMessageNumber = closeSeqElement.getLastMsgNumber();

        inboundSequence.close();

        CloseSequenceResponseElement closeSeqResponseElement = new CloseSequenceResponseElement();
        closeSeqResponseElement.setIdentifier(new Identifier(inboundSequence.getId()));

        PacketAdapter responseAdapter = requestAdapter.createServerResponse(closeSeqResponseElement, RmVersion.WSRM11.closeSequenceResponseAction);
        responseAdapter.appendSequenceAcknowledgementHeader(inboundSequence);
        return responseAdapter;
    }

    @Override
    PacketAdapter handleTerminateSequenceAction( PacketAdapter requestAdapter) throws UnknownSequenceFault {
        TerminateSequenceElement tsElement = requestAdapter.unmarshallMessage();

        Sequence inboundSequence = getSequenceOrSoapFault(requestAdapter.getPacket(), tsElement.getIdentifier().getValue());

        // Formulating response:
        //   If there is an outbound sequence, client expects us to terminate it => sending TerminateSequence back.
        //   If not, we send TerminateSequenceResponse
        Sequence outboundSeqence = null;
        try {
            outboundSeqence = sequenceManager.getBoundSequence(inboundSequence.getId());

            String responseAction;
            Object responseObject;
            if (outboundSeqence != null) {
                TerminateSequenceElement terminateSeqResponse = new TerminateSequenceElement();
                Identifier id = new Identifier(outboundSeqence.getId());
                terminateSeqResponse.setIdentifier(id);

                responseAction = RmVersion.WSRM11.terminateSequenceAction;
                responseObject = terminateSeqResponse;
            } else {
                TerminateSequenceResponseElement terminateSeqResponse = new TerminateSequenceResponseElement();
                Identifier id = new Identifier(outboundSeqence.getId());
                terminateSeqResponse.setIdentifier(id);

                responseAction = RmVersion.WSRM11.terminateSequenceResponseAction;
                responseObject = terminateSeqResponse;
            }

            PacketAdapter responseAdapter = requestAdapter.createServerResponse(responseObject, responseAction);
            responseAdapter.appendSequenceAcknowledgementHeader(inboundSequence);

            return responseAdapter;
        } finally {
            Utilities.endSessionIfExists(inboundSequence.getId());
            try {
                sequenceManager.terminateSequence(inboundSequence.getId());
            } finally {
                if (outboundSeqence != null) {
                    sequenceManager.terminateSequence(outboundSeqence.getId());
                }
            }
        }
    }

    /**
     * TODO javadoc
     */
    PacketAdapter handleMakeConnectionAction(PacketAdapter requestAdapter) {
        throw new UnsupportedOperationException("Not supported yet.");
    // TODO
//        MakeConnectionElement mcElement = requestAdapter.unmarshallMessage();
//        Sequence outboundSequence = sequenceManager.getBoundSequence(mcElement.getIdentifier().getValue());

//        sequenceId = element.getIdentifier().getValue();
//        OutboundSequence outboundSequence = RMDestination.getRMDestination().getOutboundSequence(sequenceId);
//        if (outboundSequence == null) {
//            throw LOGGER.logSevereException(new RmException(LocalizationMessages.WSRM_3025_INVALID_SEQUENCE_ID_IN_MAKECONNECTION_MESSAGE(sequenceId)));
//        }
//
//        //see if we can find a message in the sequence that needs to be resent.
//        Packet ret = new Packet();
//
//        RMMessage unacknowledgedMessage = outboundSequence.getUnacknowledgedMessage();
//        if (unacknowledgedMessage != null) {
//            ret.setMessage(unacknowledgedMessage.getCopy());
//        } else {
//            ret.setMessage(Messages.createEmpty(getConfig().getSoapVersion()));
//        }
//
//        ret.invocationProperties.putAll(packet.invocationProperties);
//        return ret;
    }
}
