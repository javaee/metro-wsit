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

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.assembler.WsitServerTubeAssemblyContext;
import com.sun.xml.ws.rm.runtime.CreateSequenceRefusedException;
import com.sun.xml.ws.rm.RmRuntimeException;
import com.sun.xml.ws.rm.RmSoapFaultException;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.policy.ConfigurationManager;
import com.sun.xml.ws.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rm.runtime.sequence.SequenceManagerFactory;
import java.util.Calendar;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class ServerRmTube extends AbstractFilterTubeImpl {

    private static final RmLogger LOGGER = RmLogger.getLogger(ServerRmTube.class);
    private final Configuration configuration;
    private final SequenceManager sequenceManager;
    private final PacketAdapter requestAdapter;

    protected ServerRmTube(ServerRmTube original, TubeCloner cloner) {
        super(original, cloner);

        this.configuration = original.configuration;
        this.sequenceManager = original.sequenceManager;

        this.requestAdapter = PacketAdapter.create(configuration);
    // TODO initialize all instance variables
    }

    public ServerRmTube(WsitServerTubeAssemblyContext context) {
        super(context.getTubelineHead());

        this.configuration = ConfigurationManager.createServiceConfigurationManager(context.getWsdlPort(), context.getEndpoint().getBinding()).getConfigurationAlternatives()[0];
        this.sequenceManager = SequenceManagerFactory.getInstance().getSequenceManager();
        this.requestAdapter = PacketAdapter.create(configuration);
    // TODO initialize all instance variables        
    }

    @Override
    public ServerRmTube copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new ServerRmTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processException(Throwable throwable) {
        LOGGER.entering();
        try {
            return super.processException(throwable);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processRequest(Packet requestPacket) {
        LOGGER.entering();
        requestAdapter.attach(requestPacket);
        try {
            if (requestAdapter.isProtocolMessage()) {
                if (requestAdapter.isProtocolRequest()) {
                    PacketAdapter protocolResponseAdapter = processProtocolRequest(requestAdapter);
                    return doReturnWith(protocolResponseAdapter.detach());
                } else {
                    return doThrow(new RmRuntimeException(LocalizationMessages.WSRM_1128_INVALID_WSA_ACTION_IN_PROTOCOL_REQUEST(requestAdapter.getWsaAction())));
                }
            } else {
                processApplicationRequestHeaders(requestAdapter);
                // TODO: process RM headers
                if (configuration.isOrderedDelivery()) {                    // TODO: ordered case processing (suspend until it's this message's turn)
                }
                return super.processRequest(requestAdapter.detach());
            }
        } catch (RmSoapFaultException ex) {
            return doReturnWith(ex.getSoapFaultResponse());
//        } catch (RmException ex) {
//            LOGGER.logSevereException(ex);
//            return doThrow(new WebServiceException(ex)); // the input argument has to be a runtime exception
        } finally {
            requestAdapter.detach();
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processResponse(Packet responsePacket) {
        LOGGER.entering();
        try {
            return super.processResponse(responsePacket);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public void preDestroy() {
        LOGGER.entering();
        try {
            super.preDestroy();
        } finally {
            LOGGER.exiting();
        }
    }

    /**
     * TODO javadoc
     */
    private PacketAdapter processProtocolRequest(PacketAdapter requestAdapter) throws RmSoapFaultException {
        if (configuration.getRmVersion().createSequenceAction.equals(requestAdapter.getWsaAction())) {
            return handleCreateSequenceAction(requestAdapter);
        } else if (configuration.getRmVersion().terminateSequenceAction.equals(requestAdapter.getWsaAction())) {
            return handleTerminateSequenceAction(requestAdapter);
        } else if (configuration.getRmVersion().ackRequestedAction.equals(requestAdapter.getWsaAction())) {
            return handleAckRequestedAction(requestAdapter);
        } else if (configuration.getRmVersion().lastAction.equals(requestAdapter.getWsaAction())) {
            return handleLastMessageAction(requestAdapter);
        } else if (RmVersion.WSRM11.closeSequenceAction.equals(requestAdapter.getWsaAction())) {
            // FIXME: split RM11 and RM10 processing
            return handleCloseSequenceAction(requestAdapter);
        } else if (configuration.getRmVersion().sequenceAcknowledgementAction.equals(requestAdapter.getWsaAction())) {
            return handleSequenceAcknowledgementAction(requestAdapter);
        } else if (configuration.getRmVersion().makeConnectionAction.equals(requestAdapter.getWsaAction())) {
            return handleMakeConnectionAction(requestAdapter);
        } else {
            throw new UnsupportedOperationException(
                    "Internal error: The RM protocol request processing for WS-Addressing action [ " + requestAdapter.getWsaAction() + " ] is not implemented");
        }
// TODO       } catch (CreateSequenceException e) {
//            return PacketAdapter.create(configuration, requestAdapter.createCreateSequenceProcessingSoapFaultResponse(
//                    configuration.getRmVersion().createSequenceRefusedFaultCode,
//                    LocalizationMessages.WSRM_3027_CREATE_SEQUENCE_REFUSED(e.getMessage())));
//        } catch (TerminateSequenceException e) {
//            return PacketAdapter.create(configuration, requestAdapter.createRmProcessingSoapFaultResponse(
//                    configuration.getRmVersion().sequenceTerminatedFaultCode,
//                    LocalizationMessages.WSRM_3028_SEQUENCE_TERMINATED_ON_ERROR(e.getMessage())));
//        } catch (InvalidSequenceException e) {
//            return PacketAdapter.create(configuration, requestAdapter.createRmProcessingSoapFaultResponse(
//                    configuration.getRmVersion().unknownSequenceFaultCode,
//                    LocalizationMessages.WSRM_3022_UNKNOWN_SEQUENCE_ID_IN_MESSAGE(e.getSequenceId())));
    }

    /**
     * TODO javadoc
     */
    private PacketAdapter handleCreateSequenceAction(PacketAdapter requestAdapter) {
        com.sun.xml.ws.rm.v200502.CreateSequenceElement csElement = requestAdapter.unmarshallMessage();

        String offeredId;
        if (csElement.getOffer() != null) {
            com.sun.xml.ws.rm.v200502.Identifier id = csElement.getOffer().getIdentifier();
            if (id != null) {
                offeredId = id.getValue();
            }
        }

        long expirationTime = Configuration.UNSPECIFIED;
        if (csElement.getExpires() != null && !"PT0S".equals(csElement.getExpires().getValue().toString())) {
            expirationTime = csElement.getExpires().getValue().getTimeInMillis(Calendar.getInstance()) + System.currentTimeMillis();
        }

        // Read STR element in csrElement if any
        com.sun.xml.ws.security.secext10.SecurityTokenReferenceType strType = csElement.getSecurityTokenReference();

// TODO       //create server-side data structures.
//        InboundSequence inboundSequence = RMDestination.getRMDestination().createSequence(
//                null, //assign random id
//                offeredId,
//                getConfig());
//
//        //start the inactivity timer
//        inboundSequence.resetLastActivityTime();

        boolean isSecuredMessaging = strType != null ? true : false;
        if (isSecuredMessaging) {
            String activeSctId = requestAdapter.getSecurityContextTokenId();
            if (activeSctId != null) {
                com.sun.xml.ws.security.trust.elements.str.Reference ref = com.sun.xml.ws.security.trust.WSTrustElementFactory.newInstance().createSecurityTokenReference(
                        new com.sun.xml.ws.security.secext10.ObjectFactory().createSecurityTokenReference(strType)).getReference();

                if (ref instanceof com.sun.xml.ws.security.trust.elements.str.DirectReference) {
                    String receivedSctId = ((com.sun.xml.ws.security.trust.elements.str.DirectReference) ref).getURIAttr().toString();
                    if (activeSctId.equals(receivedSctId)) {
// TODO                         inboundSequence.setSecurityTokenReferenceId(securityContextTokenId);
                    } else {
                        throw LOGGER.logSevereException(new CreateSequenceRefusedException(
                                configuration, 
                                requestAdapter.detach(),                                
                                LocalizationMessages.WSRM_3004_SECURITY_TOKEN_AUTHORIZATION_ERROR(receivedSctId, activeSctId)));
                    }
                } else {
                    throw LOGGER.logSevereException(new CreateSequenceRefusedException(
                                configuration, 
                                requestAdapter.detach(),                                
                                LocalizationMessages.WSRM_3005_SECURITY_REFERENCE_ERROR(ref.getClass().getName())));
                }
            } else {
                throw LOGGER.logSevereException(new CreateSequenceRefusedException(
                                configuration, 
                                requestAdapter.detach(),                                
                                LocalizationMessages.WSRM_3006_NULL_SECURITY_TOKEN()));
            }
        }

// TODO        startSession(inboundSequence);
//
// TODO        if (offeredId == null) {
//            inboundSequence.getOutboundSequence().setSaveMessages(false);
//        }


        Sequence inboundSequence = sequenceManager.createInboundSequence(sequenceManager.generateSequenceUID(), expirationTime);


        throw new UnsupportedOperationException("Not yet implemented");
    }
    
//    public Packet handleCreateSequenceAction(Packet packet) throws RmException {
//        AbstractCreateSequence csrElement;
//        String offeredId = null;
//        Message message = packet.getMessage();
//
//        try {
//            csrElement = message.readPayloadAsJAXB(getUnmarshaller());
//        } catch (JAXBException e) {
//            throw LOGGER.logSevereException(new RmException(LocalizationMessages.WSRM_3002_CREATESEQUENCE_HEADER_PROBLEM(), e));
//        }
//
//        com.sun.xml.ws.security.secext10.SecurityTokenReferenceType strType = null;
//        if (csrElement instanceof com.sun.xml.ws.rm.v200502.CreateSequenceElement) {
//            com.sun.xml.ws.rm.v200502.OfferType offer = ((com.sun.xml.ws.rm.v200502.CreateSequenceElement) csrElement).getOffer();
//            if (offer != null) {
//                com.sun.xml.ws.rm.v200502.Identifier id = offer.getIdentifier();
//                if (id != null) {
//                    offeredId = id.getValue();
//                }
//            }
//            // Read STR element in csrElement if any
//            strType = ((com.sun.xml.ws.rm.v200502.CreateSequenceElement) csrElement).getSecurityTokenReference();
//            this.secureReliableMessaging = strType != null ? true : false;
//        } else {
//            com.sun.xml.ws.rm.v200702.OfferType offer = ((com.sun.xml.ws.rm.v200702.CreateSequenceElement) csrElement).getOffer();
//            if (offer != null) {
//                com.sun.xml.ws.rm.v200702.Identifier id = offer.getIdentifier();
//                if (id != null) {
//                    offeredId = id.getValue();
//                }
//            }
//            // Read STR element in csrElement if any
//            strType = ((com.sun.xml.ws.rm.v200702.CreateSequenceElement) csrElement).getSecurityTokenReference();
//            this.secureReliableMessaging = strType != null ? true : false;
//        }
//        //create server-side data structures.
//        InboundSequence inboundSequence = RMDestination.getRMDestination().createSequence(
//                null, //assign random id
//                offeredId,
//                getConfig());
//
//        //start the inactivity timer
//        inboundSequence.resetLastActivityTime();
//
//        //TODO.. Read STR element in csrElement if any
//        if (this.secureReliableMessaging) {
//            SecurityContextToken sct = (SecurityContextToken) packet.invocationProperties.get(MessageConstants.INCOMING_SCT);
//            if (sct != null) {
//                String securityContextTokenId = sct.getIdentifier().toString();
//                WSTrustElementFactory wsTrustElemFactory = WSTrustElementFactory.newInstance();
//                JAXBElement jaxbElem = new com.sun.xml.ws.security.secext10.ObjectFactory().createSecurityTokenReference(strType);
//                SecurityTokenReference str = wsTrustElemFactory.createSecurityTokenReference(jaxbElem);
//
//                com.sun.xml.ws.security.trust.elements.str.Reference ref = str.getReference();
//                if (ref instanceof com.sun.xml.ws.security.trust.elements.str.DirectReference) {
//                    DirectReference directRef = (DirectReference) ref;
//                    String gotId = directRef.getURIAttr().toString();
//                    if (gotId.equals(securityContextTokenId)) {
//                        inboundSequence.setSecurityTokenReferenceId(securityContextTokenId);
//                    } else {
//                        throw LOGGER.logSevereException(new RmSecurityException(LocalizationMessages.WSRM_3004_SECURITY_TOKEN_AUTHORIZATION_ERROR(gotId, securityContextTokenId)));
//                    }
//                } else {
//                    throw LOGGER.logSevereException(new RmSecurityException(LocalizationMessages.WSRM_3005_SECURITY_REFERENCE_ERROR(ref.getClass().getName())));
//                }
//            } else {
//                throw LOGGER.logSevereException(new RmSecurityException(LocalizationMessages.WSRM_3006_NULL_SECURITY_TOKEN()));
//            }
//        }
//
//        startSession(inboundSequence);
//
//        if (offeredId == null) {
//            inboundSequence.getOutboundSequence().setSaveMessages(false);
//        }
//
//        //initialize CreateSequenceResponseElement
//        AbstractAcceptType accept = null;
//        AbstractCreateSequenceResponse crsElement = null;
//        if (getConfig().getRMVersion() == RmVersion.WSRM10) {
//            crsElement = new com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement();
//            com.sun.xml.ws.rm.v200502.Identifier id2 = new com.sun.xml.ws.rm.v200502.Identifier();
//            id2.setValue(inboundSequence.getId());
//            ((com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement) crsElement).setIdentifier(id2);
//            accept = new com.sun.xml.ws.rm.v200502.AcceptType();
//        } else {
//            crsElement = new com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement();
//            com.sun.xml.ws.rm.v200702.Identifier id2 = new com.sun.xml.ws.rm.v200702.Identifier();
//            id2.setValue(inboundSequence.getId());
//            ((com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement) crsElement).setIdentifier(id2);
//            accept = new com.sun.xml.ws.rm.v200702.AcceptType();
//
//        }
//
//        URI dest;
//        if (offeredId != null) {
//            String destString = message.getHeaders().getTo(getConfig().getAddressingVersion(), getConfig().getSoapVersion());
//            try {
//                dest = new URI(destString);
//            } catch (Exception e) {
//                throw LOGGER.logSevereException(new RmException(LocalizationMessages.WSRM_3019_INVALID_OR_MISSING_TO_ON_CS_MESSAGE(), e));
//            }
//
//            W3CEndpointReference endpointReference;
//            WSEndpointReference wsepr = new WSEndpointReference(dest, getConfig().getAddressingVersion());
//            if (getConfig().getAddressingVersion() == AddressingVersion.W3C) {
//                endpointReference = (W3CEndpointReference) wsepr.toSpec();
//                accept.setAcksTo(endpointReference);
//            }
//            crsElement.setAccept(accept);
//        }
//
//        Message response = Messages.create(
//                getConfig().getRMVersion().jaxbContext,
//                crsElement,
//                getConfig().getSoapVersion());
//
//        message.assertOneWay(false);
//
//        // ADDRESSING_FIXME
//        // This will probably be broken with MS client if they still send CS with
//        // missing reply-to.
//        Packet ret = packet.createServerResponse(
//                response,
//                getConfig().getAddressingVersion(),
//                getConfig().getSoapVersion(),
//                getConfig().getRMVersion().createSequenceResponseAction);
//
//        return ret;
//    }
    /**
     * TODO javadoc
     */
    private PacketAdapter handleAckRequestedAction(PacketAdapter requestAdapter) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO javadoc
     */
    private PacketAdapter handleCloseSequenceAction(PacketAdapter requestAdapter) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO javadoc
     */
    private PacketAdapter handleLastMessageAction(PacketAdapter requestAdapter) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO javadoc
     */
    private PacketAdapter handleMakeConnectionAction(PacketAdapter requestAdapter) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO javadoc
     */
    private PacketAdapter handleSequenceAcknowledgementAction(PacketAdapter requestAdapter) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO javadoc
     */
    private PacketAdapter handleTerminateSequenceAction(PacketAdapter requestAdapter) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO javadoc
     */
    private void processApplicationRequestHeaders(PacketAdapter requestAdapter) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
