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
 * RMServerTube.java
 *
 * @author Mike Grogan
 * @author Bhakti Mehta
 * Created on August 25, 2007, 8:10 AM
 *
 */
package com.sun.xml.ws.rm.jaxws.runtime.server;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.rm.BufferFullException;
import com.sun.xml.ws.rm.CloseSequenceException;
import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.CreateSequenceException;
import com.sun.xml.ws.rm.DuplicateMessageException;
import com.sun.xml.ws.rm.InvalidSequenceException;
import com.sun.xml.ws.rm.MessageNumberRolloverException;
import com.sun.xml.ws.rm.MessageSender;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.RMMessage;
import com.sun.xml.ws.rm.RMSecurityException;
import com.sun.xml.ws.rm.RMVersion;
import com.sun.xml.ws.rm.TerminateSequenceException;
import com.sun.xml.ws.rm.jaxws.runtime.InboundMessageProcessor;
import com.sun.xml.ws.rm.jaxws.runtime.InboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.TubeBase;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.protocol.AbstractAcceptType;
import com.sun.xml.ws.rm.protocol.AbstractAckRequested;
import com.sun.xml.ws.rm.protocol.AbstractCreateSequence;
import com.sun.xml.ws.rm.protocol.AbstractCreateSequenceResponse;
import com.sun.xml.ws.rm.protocol.AbstractSequenceAcknowledgement;
import com.sun.xml.ws.rm.protocol.AbstractTerminateSequence;
import com.sun.xml.ws.runtime.util.Session;
import com.sun.xml.ws.runtime.util.SessionManager;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.elements.str.DirectReference;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.wss.impl.MessageConstants;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.net.URI;
import java.util.HashMap;
import javax.xml.soap.SOAPConstants;

/**
 * Server-side RM Tube implementation
 */
public final class RMServerTube extends TubeBase {

    private static final RmLogger LOGGER = RmLogger.getLogger(RMServerTube.class);
    private static HashMap<String, ActionHandler> actionMap = new HashMap<String, ActionHandler>();
    private boolean secureReliableMessaging = false;
    private RMMessage currentRequestMessage;
    private SessionManager sessionManager = SessionManager.getSessionManager();

    /**
     * Constructor is passed everything available in PipelineAssembler.
     *
     * @param wsdlPort The WSDLPort
     * @param ownerEndpoint The WSEndpoint.
     * @param nextTube The next Tube in the pipeline.
     *
     */
    public RMServerTube(WSDLPort wsdlPort, WSBinding binding, Tube nextTube) {
        super(wsdlPort, binding, nextTube);

        initActionMap();
    }

    /**
     * Copy constructor used by <code>copy</code> method.
     *
     * @param toCopy to be copied.
     * @param cloner passed as an argument to copy.
     */
    private RMServerTube(RMServerTube toCopy, TubeCloner cloner) {
        super(toCopy, cloner);

        initActionMap();
    }

    @Override
    public NextAction processRequest(Packet packet) {
        SOAPFault soapFault = null;
        RMMessage message = null;

        try {
            //handle special protocol messages
            Packet ret = null;
            try {
                ret = handleProtocolMessage(packet);
            } catch (CreateSequenceException e) {
                soapFault = createSoapFault(
                        getConfig().getRMVersion().getCreateSequenceRefusedQname(),
                        LocalizationMessages.WSRM_3027_CREATE_SEQUENCE_REFUSED(e.getMessage()));
            } catch (TerminateSequenceException e) {
                soapFault = createSoapFault(
                        getConfig().getRMVersion().getSequenceTerminatedQname(),
                        LocalizationMessages.WSRM_3028_SEQUENCE_TERMINATED_ON_ERROR(e.getMessage()));
            } catch (InvalidSequenceException e) {
                soapFault = createSoapFault(
                        getConfig().getRMVersion().getUnknownSequenceQname(),
                        LocalizationMessages.WSRM_3022_UNKNOWN_SEQUENCE_ID_IN_MESSAGE(e.getSequenceId()));
            }

            if (ret != null) {
                //the contract of handleProtocolMessage is to return null if messager is non-protocol
                //message or protocol message is piggybacked on an application message.
                return doReturnWith(ret);
            }

            //If we got here, this is an application message
            //do inbound bookkeeping
            try {
                message = handleInboundMessage(packet);
            } catch (MessageNumberRolloverException e) {
                soapFault = createSoapFault(
                        getConfig().getRMVersion().getMessageNumberRolloverQname(),
                        LocalizationMessages.WSRM_3026_MESSAGE_NUMBER_ROLLOVER(e.getMessageNumber()));
            } catch (InvalidSequenceException e) {
                soapFault = createSoapFault(
                        getConfig().getRMVersion().getUnknownSequenceQname(),
                        LocalizationMessages.WSRM_3022_UNKNOWN_SEQUENCE_ID_IN_MESSAGE(e.getSequenceId()));
            } catch (CloseSequenceException e) {
                soapFault = createSoapFault(
                        getConfig().getRMVersion().getClosedSequenceQname(),
                        LocalizationMessages.WSRM_3029_SEQUENCE_CLOSED(e.getSequenceId()));
            }

            Packet retPacket = null;
            if (soapFault != null) {
                Message soapFaultMessage = com.sun.xml.ws.api.message.Messages.create(soapFault);

                //SequenceFault is to be added for only SOAP 1.1
                if (getConfig().getSoapVersion() == SOAPVersion.SOAP_11) {
                    //FIXME - need JAXBRIContext that can marshall SequenceFaultElement
                    Header header = Headers.create(getConfig().getRMVersion().getJAXBContext(), new com.sun.xml.ws.rm.v200502.SequenceFaultElement());
                    soapFaultMessage.getHeaders().add(header);
                }

                retPacket = packet.createServerResponse(
                        soapFaultMessage,
                        getConfig().getConstants().getAddressingVersion(),
                        getConfig().getSoapVersion(),
                        getConfig().getConstants().getAddressingVersion().getDefaultFaultAction());
                retPacket.setMessage(soapFaultMessage);
                doReturnWith(retPacket);
            }

            //allow diagnostic access to message if ProcessingFilter has been specified
            if (RMDestination.getRMDestination().getProcessingFilter() != null) {
                RMDestination.getRMDestination().getProcessingFilter().handleEndpointRequestMessage(message);
            }

            //If Message is a duplicate, handleInboundMessage threw a DuplicateMessageException,
            //Therefore, this is the first time processing this message.
            //use sequence id to initialize inboundSequence and outboundSequence
            //local variables by doing a lookup in RMDestination
            ServerInboundSequence inboundSequence = (ServerInboundSequence) message.getSequence();

            if (inboundSequence == null) {
                throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3017_NOT_RELIABLE_SEQ_OR_PROTOCOL_MESSAGE()));

            }

            //reset inactivity timer
            inboundSequence.resetLastActivityTime();

            //determine whether the correct STR has been used to sign message
            if (secureReliableMessaging) {
                checkSTR(packet, inboundSequence);
            }

            //set com.sun.xml.ws.session and com.sun.xml.ws.sessionid
            //invocationProperties if they have not already been set
            //by SC pipe.
            setSessionData(packet, inboundSequence);

            //clear packet.transporBackChannel so downstream pipes do not prevent
            //empty one-way response bodies to be sent back when we need to use the
            //bodies for RM SequenceAcknowledgemnts.
            packet.transportBackChannel = null;

            //make these available in an injected WebServiceContext
            packet.invocationProperties.put(Constants.sequenceProperty, inboundSequence);
            packet.invocationProperties.put(Constants.messageNumberProperty, message.getMessageNumber());

            //If ordered deliver is configured,
            //Block here if InboundSequence reports gaps before this message.
            //inboundSequence.holdIfUndeliverable(message);

            //send the message down the Tubeline
            this.currentRequestMessage = message;

            if (!inboundSequence.isOrdered()) {
                return doInvoke(next, packet);
            } else {
                MessageSender sender = new TubelineSender(
                        this,
                        packet,
                        getConfig().getSoapVersion(),
                        getConfig().getConstants().getAddressingVersion());
                message.setMessageSender(sender);

                //send message down tubeline if predecesor has arrived.  Otherwise. 
                //it will have to wait until releaseNextMessage is called during the
                //processing of the predecessor in processResponse.
                if (inboundSequence.isDeliverable(message)) {
                    sender.send();
                }

                return doSuspend();
            }
        } catch (BufferFullException e) {
            //need to return message with empty body and SequenceAcknowledgement
            //header for inboundSequence.  This is similar to handleAckRequestedAction, which
            //should do much the same thing.  The only difference is that handleAckRequestedAction
            //must also have logic to get the inboundSequence from the AckRequested header in the
            //packet
            if (packet.getMessage().isOneWay(getWsdlPort())) {
                //refuse to process the request.  Client will retry
                Packet ret = new Packet();
                ret.invocationProperties.putAll(packet.invocationProperties);
                return doReturnWith(ret);
            }

            //handleInboundMessage shouldn't let inboundSequence be null.
            try {
                ServerInboundSequence seq = (ServerInboundSequence) e.getSequence();
                if (seq != null) {
                    Packet ret = generateAckMessage(packet, seq, getConfig().getRMVersion().getSequenceAcknowledgementAction());
                    return doReturnWith(ret);
                } else {
                    //unreachable
                    return null;
                }
            } catch (RMException ee) {
                // TODO hanlde expcetion ?

                LOGGER.severe(LocalizationMessages.WSRM_3001_ACKNOWLEDGEMENT_MESSAGE_EXCEPTION(), e);
                return doThrow(new WebServiceException(LocalizationMessages.WSRM_3001_ACKNOWLEDGEMENT_MESSAGE_EXCEPTION(), e));
            }
        } catch (DuplicateMessageException e) {
            //  1. If one-way return empty message  without invoking process on next pipe
            //  2. If two-way, formulate response according to secret Microsoft protocol.
            //          a. If original was processed and response is still available in OutboundSequence,
            //              return it again.
            //          b. Otherwise (original not yet processed or response already discarded, return
            //             ack message.
            if (packet.getMessage().isOneWay(getWsdlPort())) {
                //ignore the message.
                Packet ret = new Packet();
                ret.invocationProperties.putAll(packet.invocationProperties);
                return doReturnWith(ret);
            } else {
                //check whether original response is available.
                RMMessage original = e.getRMMessage();
                RMMessage origresp = original.getRelatedMessage();

                if (origresp != null) {
                    Message response = origresp.getCopy();
                    if (response != null) {
                        //original response is available, resend it.
                        Packet ret = new Packet(response);
                        ret.invocationProperties.putAll(packet.invocationProperties);
                        return doReturnWith(ret);
                    }
                }

                //either the original request is waiting to be processing, or the response has been
                //acked and thrown away.  All we can do is return a SequenceAcknowledgement.
                try {
                    ServerInboundSequence seq = (ServerInboundSequence) original.getSequence();
                    Packet ret = generateAckMessage(packet, seq, getConfig().getRMVersion().getSequenceAcknowledgementAction());
                    return doReturnWith(ret);
                } catch (RMException ee) {
                    return doThrow(new WebServiceException(ee));
                }
            }
        } catch (RMException e) {
            //see if a RM Fault type has been assigned to this exception type.  If so, let
            //the exception generate a fault message.  Otherwise, wrap as WebServiceException and
            //rethrow.
            Message m = e.getFaultMessage();
            if (m != null) {
                return doReturnWith(new Packet(m));
            } else {
                return doThrow(new WebServiceException(e));
            }
        } catch (RuntimeException e) {
            return doThrow(new WebServiceException(e));
        }
    }

    /** 
     * 
     */
    @Override
    public NextAction processResponse(Packet packet) {
        //on the non-ordered code path, we need to do post-processing.  On
        //the ordered path, this has been one
        ServerInboundSequence inboundSequence = (ServerInboundSequence) currentRequestMessage.getSequence();
        if (!inboundSequence.isOrdered()) {
            postProcess(packet);
        }

        return doReturnWith(packet);
    }

    @Override
    public NextAction processException(Throwable t) {
        return doThrow(t);
    }

    public void postProcess(Packet packet) {
        ServerInboundSequence inboundSequence = (ServerInboundSequence) currentRequestMessage.getSequence();
        try {
            // This shouldn't be necessary, but having messageNumberProperty
            // set has side-effects here due to the fact that RMClientPipe
            // and RMServerPipe share an implementation of handleOutboundMessage
            packet.invocationProperties.put(Constants.sequenceProperty, null);
            packet.invocationProperties.put(Constants.messageNumberProperty, null);

            Message responseMessage = packet.getMessage();
            Message emptyMessage;
            if (responseMessage == null) {
                //This a one-way response. handleOutboundMessage
                //might need a message to write sequenceAcknowledgent headers to.
                //Give it one.
                emptyMessage = com.sun.xml.ws.api.message.Messages.createEmpty(getConfig().getSoapVersion());
                packet.setMessage(emptyMessage);

                //propogate this information so handleOutboundMessage will not
                //add this to the outbound sequence, if any.
                packet.invocationProperties.put(Constants.oneWayResponseProperty, true);
            }

            //If ordered delivery is configured, unblock the next message in the sequence
            //if it is waiting for this one to be delivered.
            inboundSequence.releaseNextMessage(currentRequestMessage);

            //Let Outbound sequence do its bookkeeping work, which consists of writing
            //outbound RM headers.
            //need to handle any error caused by
            //handleOutboundMessage.. Request has already been processed
            //by the endpoint.
            RMMessage om = handleOutboundMessage(inboundSequence.getOutboundSequence(), packet);

            //allow diagnostic access to outbound message if ProcessingFilter is
            //specified
            if (RMDestination.getRMDestination().getProcessingFilter() != null) {
                RMDestination.getRMDestination().getProcessingFilter().handleEndpointResponseMessage(om);
            }

            //If we populated
            //ret with an empty message to be used by RM protocol, and it
            //was not used, get rid of the empty message.
            if (responseMessage == null && packet.getMessage() != null && !packet.getMessage().hasHeaders()) {
                packet.setMessage(null);
            } else {
                //Fill in relatedMessage field in request message for use in case request is resent.
                //the com.sun.xml.ws.api.message.Message referenced will be a copy of the one
                //contained in the returned packet.  See implementation of message.setRelatedMessage.
                currentRequestMessage.setRelatedMessage(om);

                // MS client expects SequenceAcknowledgement action incase of oneway messages
                if (responseMessage == null && packet.getMessage() != null) {
                    HeaderList headerList = packet.getMessage().getHeaders();
                    headerList.add(Headers.create(
                            getConfig().getConstants().getAddressingVersion().actionTag,
                            getConfig().getRMVersion().getSequenceAcknowledgementAction()));
                }
            }
        } catch (RMException e) {
            //see if a RM Fault type has been assigned to this exception type.  If so, let
            //the exception generate a fault message.  Otherwise, wrap as WebServiceException and
            //rethrow.
            Message m = e.getFaultMessage();
            if (m != null) {
                doReturnWith(new Packet(m));
            } else {
                doThrow(new WebServiceException(e));
            }
        } catch (RuntimeException e) {
            doThrow(new WebServiceException(e));
        }
    }

    @Override
    public void preDestroy() {
        //nothing to do here so far
        next.preDestroy();
    }

    public RMServerTube copy(TubeCloner cloner) {
        return new RMServerTube(this, cloner);
    }

    public Tube nextTube() {
        return next;
    }

    /**
     * Handle a non-Application message.  Look at the wsa:Action header and if it is
     * one mapped to a handler in our actionHandlers dispatch table, invoke the handler.
     *
     * @param packet The Packet containing the incoming message
     * @return The Packet returned by invocation of the handler
     *          null if no handler is registered
     * @throws RMException - if any thrown by the handler.
     */
    public Packet handleProtocolMessage(Packet packet) throws RMException {
        ActionHandler handler;
        String actionValue;
        actionValue = packet.getMessage().getHeaders().getAction(
                getConfig().getConstants().getAddressingVersion(),
                getConfig().getSoapVersion());
        if (actionValue == null || actionValue.equals("")) {
            throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3018_NON_RM_REQUEST_OR_MISSING_WSA_ACTION_HEADER()));
        }

        handler = actionMap.get(actionValue);
        if (handler != null) {
            return handler.process(this, packet);
        } else {
            return null;
        }
    }

    /**********************************/
    /* Handlers for wsa:Action values */
    /**********************************/
    public Packet handleCreateSequenceAction(Packet packet) throws RMException {
        AbstractCreateSequence csrElement;
        String offeredId = null;
        Message message = packet.getMessage();

        try {
            csrElement = message.readPayloadAsJAXB(getUnmarshaller());
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3002_CREATESEQUENCE_HEADER_PROBLEM(), e));
        }

        /**ADDRESSING_FIXME
         *  Assume for now that AcksTo is anonymous.
         */
        URI acksTo = getConfig().getConstants().getAnonymousURI();
        /*String acksToString = acksTo.toString();*/
        /*String replyToString = acksToString;*/
        /*
        EndpointReference replyTo = inboundAddressingProperties.getReplyTo();
        if (replyTo == null) {
        replyTo = addressingBuilder.newEndpointReference( ac.getAnonymousURI());
        inboundAddressingProperties.setReplyTo(replyTo);
        }
        EndpointReference acksTo = csrElement.getAcksTo();
        String ackstoUri = acksTo.getAddress().getURI().toString();
        String replytoUri = replyTo.getAddress().getURI().toString();
        if (!ackstoUri.equals(replytoUri)){
        throw new CreateSequenceException(Messages.ACKSTO_NOT_EQUAL_REPLYTO.format(ackstoUri,replytoUri)  );
        }
         */

        com.sun.xml.ws.security.secext10.SecurityTokenReferenceType strType = null;
        if (csrElement instanceof com.sun.xml.ws.rm.v200502.CreateSequenceElement) {
            com.sun.xml.ws.rm.v200502.OfferType offer = ((com.sun.xml.ws.rm.v200502.CreateSequenceElement) csrElement).getOffer();
            if (offer != null) {
                com.sun.xml.ws.rm.v200502.Identifier id = offer.getIdentifier();
                if (id != null) {
                    offeredId = id.getValue();
                }
            }
            // Read STR element in csrElement if any
            strType = ((com.sun.xml.ws.rm.v200502.CreateSequenceElement) csrElement).getSecurityTokenReference();
            this.secureReliableMessaging = strType != null ? true : false;
        } else {
            com.sun.xml.ws.rm.v200702.OfferType offer = ((com.sun.xml.ws.rm.v200702.CreateSequenceElement) csrElement).getOffer();
            if (offer != null) {
                com.sun.xml.ws.rm.v200702.Identifier id = offer.getIdentifier();
                if (id != null) {
                    offeredId = id.getValue();
                }
            }
            // Read STR element in csrElement if any
            strType = ((com.sun.xml.ws.rm.v200702.CreateSequenceElement) csrElement).getSecurityTokenReference();
            this.secureReliableMessaging = strType != null ? true : false;
        }
        //create server-side data structures.
        InboundSequence inboundSequence = RMDestination.getRMDestination().createSequence(
                acksTo,
                null, //assign random id
                offeredId,
                getConfig());

        //start the inactivity timer
        inboundSequence.resetLastActivityTime();

        //TODO.. Read STR element in csrElement if any
        if (this.secureReliableMessaging) {
            SecurityContextToken sct = (SecurityContextToken) packet.invocationProperties.get(MessageConstants.INCOMING_SCT);
            if (sct != null) {
                String strId = sct.getIdentifier().toString();
                WSTrustElementFactory wsTrustElemFactory = WSTrustElementFactory.newInstance();
                JAXBElement jaxbElem = new com.sun.xml.ws.security.secext10.ObjectFactory().createSecurityTokenReference(strType);
                SecurityTokenReference str = wsTrustElemFactory.createSecurityTokenReference(jaxbElem);

                com.sun.xml.ws.security.trust.elements.str.Reference ref = str.getReference();
                if (ref instanceof com.sun.xml.ws.security.trust.elements.str.DirectReference) {
                    DirectReference directRef = (DirectReference) ref;
                    String gotId = directRef.getURIAttr().toString();
                    if (gotId.equals(strId)) {
                        inboundSequence.setStrId(strId);
                    } else {
                        // TODO: log?
                        throw new RMSecurityException(LocalizationMessages.WSRM_3004_SECURITY_TOKEN_AUTHORIZATION_ERROR(gotId, strId));
                    }
                } else {
                    // TODO: log?
                    throw new RMSecurityException(LocalizationMessages.WSRM_3005_SECURITY_REFERENCE_ERROR(ref.getClass().getName()));
                }
            } else {
                // TODO: log?
                throw new RMSecurityException(LocalizationMessages.WSRM_3006_NULL_SECURITY_TOKEN());
            }
        }

        startSession(inboundSequence);

        if (offeredId == null) {
            inboundSequence.getOutboundSequence().saveMessages = false;
        }

        //initialize CreateSequenceResponseElement
        AbstractAcceptType accept = null;
        AbstractCreateSequenceResponse crsElement = null;
        if (getConfig().getRMVersion() == RMVersion.WSRM10) {
            crsElement = new com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement();
            com.sun.xml.ws.rm.v200502.Identifier id2 = new com.sun.xml.ws.rm.v200502.Identifier();
            id2.setValue(inboundSequence.getId());
            ((com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement) crsElement).setIdentifier(id2);
            accept = new com.sun.xml.ws.rm.v200502.AcceptType();
        } else {
            crsElement = new com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement();
            com.sun.xml.ws.rm.v200702.Identifier id2 = new com.sun.xml.ws.rm.v200702.Identifier();
            id2.setValue(inboundSequence.getId());
            ((com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement) crsElement).setIdentifier(id2);
            accept = new com.sun.xml.ws.rm.v200702.AcceptType();

        }

        URI dest;
        if (offeredId != null) {
            String destString = message.getHeaders().getTo(getConfig().getConstants().getAddressingVersion(), getConfig().getSoapVersion());
            try {
                dest = new URI(destString);
            } catch (Exception e) {
                throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3019_INVALID_OR_MISSING_TO_ON_CS_MESSAGE(), e));
            }

            W3CEndpointReference endpointReference;
            WSEndpointReference wsepr = new WSEndpointReference(dest, getConfig().getConstants().getAddressingVersion());
            if (getConfig().getConstants().getAddressingVersion() == AddressingVersion.W3C) {
                endpointReference = (W3CEndpointReference) wsepr.toSpec();
                accept.setAcksTo(endpointReference);
            }    /*else {
            //TODO support MemberSubmissionEndpointReference when issue 131 of JAXB is resolved
            //endpointReference = (MemberSubmissionEndpointReference)wsepr.toSpec() ;
            }*/
            crsElement.setAccept(accept);
        }

        Message response = com.sun.xml.ws.api.message.Messages.create(
                getConfig().getRMVersion().getJAXBContext(),
                crsElement,
                getConfig().getSoapVersion());

        message.assertOneWay(false);

        /*ADDRESSING_FIXME
         * This will probably be broken with MS client if they still send CS with
         * missing reply-to.
         */
        Packet ret = packet.createServerResponse(
                response,
                getConfig().getConstants().getAddressingVersion(),
                getConfig().getSoapVersion(),
                getConfig().getRMVersion().getCreateSequenceResponseAction());
        /*
        ret.setEndPointAddressString(acksToString);
        ret.proxy = packet.proxy;
        //there are some invocation properties.  Outgoing addressing headers at least
        ret.invocationProperties.putAll(packet.invocationProperties);
        //Set addressing headers
        AddressingProperties outboundAddressingProperties =
        addressingBuilder.newAddressingProperties();
        //outboundAddressingProperties.initializeAsReply(inboundAddressingProperties, false);
        outboundAddressingProperties.initializeAsReply(inboundAddressingProperties);
        //AddressingProperties.initializeAsResponse does not know how to set outbound Action
        //property.
        outboundAddressingProperties.setAction(addressingBuilder.newURI(
        getConfig().getConstants().getCreateSequenceResponseAction()));
        ret.invocationProperties.put(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND,
        outboundAddressingProperties);
         */
        return ret;
    }

    public Packet handleTerminateSequenceAction(Packet packet) throws RMException {
        AbstractTerminateSequence tsElement;
        Message message = packet.getMessage();

        try {
            tsElement = message.readPayloadAsJAXB(getUnmarshaller());
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new TerminateSequenceException(LocalizationMessages.WSRM_3007_TERMINATE_SEQUENCE_EXCEPTION(), e));
        }
        String id;
        if (tsElement instanceof com.sun.xml.ws.rm.v200502.TerminateSequenceElement) {
            id = ((com.sun.xml.ws.rm.v200502.TerminateSequenceElement) tsElement).getIdentifier().getValue();
        } else {
            id = ((com.sun.xml.ws.rm.v200702.TerminateSequenceElement) tsElement).getIdentifier().getValue();
        }

        InboundSequence seq = RMDestination.getRMDestination().getInboundSequence(id);
        if (seq == null) {
            throw LOGGER.logSevereException(new InvalidSequenceException(LocalizationMessages.WSRM_3022_UNKNOWN_SEQUENCE_ID_IN_MESSAGE(id), id));
        }

        //end the session if we own its lifetime..i.e. SC is not
        //present
        endSession(seq);
        RMDestination.getRMDestination().terminateSequence(id);

        //formulate response if required
        Packet ret = null;
        OutboundSequence outboundSequence = seq.getOutboundSequence();

        Message response = null;
        String tsAction = null;
        //If there is an "real" outbound sequence, client expects us to terminate it.
        switch (getConfig().getRMVersion()) {
            case WSRM10:
                tsAction = RMVersion.WSRM10.getTerminateSequenceAction();
                if (outboundSequence.saveMessages) {
                    com.sun.xml.ws.rm.v200502.TerminateSequenceElement terminateSeqResponse = new com.sun.xml.ws.rm.v200502.TerminateSequenceElement();
                    com.sun.xml.ws.rm.v200502.Identifier id2 = new com.sun.xml.ws.rm.v200502.Identifier();
                    id2.setValue(outboundSequence.getId());

                    terminateSeqResponse.setIdentifier(id2);
                    response = com.sun.xml.ws.api.message.Messages.create(
                            getConfig().getRMVersion().getJAXBContext(),
                            terminateSeqResponse,
                            getConfig().getSoapVersion());
                    ret = packet.createServerResponse(
                            response,
                            getConfig().getConstants().getAddressingVersion(),
                            getConfig().getSoapVersion(), tsAction);

                    AbstractSequenceAcknowledgement element = seq.generateSequenceAcknowledgement(null, getMarshaller(), false);

                    Header header = Headers.create(getConfig().getRMVersion().getJAXBContext(), element);
                    response.getHeaders().add(header);
                } else {
                    packet.transportBackChannel.close();
                    ret = new Packet(null);
                }
                break;

            case WSRM11:
                tsAction = RMVersion.WSRM11.getTerminateSequenceResponseAction();
                com.sun.xml.ws.rm.v200702.TerminateSequenceResponseElement terminateSeqResponse = new com.sun.xml.ws.rm.v200702.TerminateSequenceResponseElement();
                com.sun.xml.ws.rm.v200702.Identifier id2 = new com.sun.xml.ws.rm.v200702.Identifier();
                id2.setValue(outboundSequence.getId());

                terminateSeqResponse.setIdentifier(id2);
                response = com.sun.xml.ws.api.message.Messages.create(
                        getConfig().getRMVersion().getJAXBContext(),
                        terminateSeqResponse,
                        getConfig().getSoapVersion());
                response.assertOneWay(false);
                ret = packet.createServerResponse(
                        response,
                        getConfig().getConstants().getAddressingVersion(),
                        getConfig().getSoapVersion(), tsAction);

                AbstractSequenceAcknowledgement element = seq.generateSequenceAcknowledgement(null, getMarshaller(), false);

                Header header = Headers.create(getConfig().getRMVersion().getJAXBContext(), element);
                response.getHeaders().add(header);
                break;
        }
        return ret;
    }

    public Packet handleLastMessageAction(Packet inbound) throws RMException {
        try {
            Message message = inbound.getMessage();
            Header header = message.getHeaders().get(getConfig().getRMVersion().getSequenceQName(), true);
            if (header == null) {
                throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3008_INVALID_LAST_MESSAGE()));
            }

            com.sun.xml.ws.rm.v200502.SequenceElement el = (com.sun.xml.ws.rm.v200502.SequenceElement) header.readAsJAXB(getUnmarshaller());
            String id = el.getId();
            InboundSequence seq = RMDestination.getRMDestination().getInboundSequence(id);
            if (seq == null) {
                throw LOGGER.logSevereException(new InvalidSequenceException(LocalizationMessages.WSRM_3022_UNKNOWN_SEQUENCE_ID_IN_MESSAGE(id), id));
            }

            //add message to ClientInboundSequence so that this message
            //number appears in sequence acknowledgement
            int messageNumber = el.getNumber();
            seq.set(messageNumber, new RMMessage(message, getConfig().getRMVersion()));
            return generateAckMessage(inbound, seq, getConfig().getRMVersion().getLastAction());
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3009_LAST_MESSAGE_EXCEPTION(), e));
        }
    }

    public Packet handleCloseSequenceAction(Packet inbound) throws RMException {
        com.sun.xml.ws.rm.v200702.CloseSequenceElement csElement;
        String id = null;
        Message message = inbound.getMessage();

        try {
            csElement = message.readPayloadAsJAXB(getUnmarshaller());
        } catch (JAXBException e) {
            throw new RMException(LocalizationMessages.WSRM_3021_CLOSESEQUENCE_HEADER_PROBLEM(), e);
        }
        id = csElement.getIdentifier().getValue();
        InboundSequence seq = RMDestination.getRMDestination().getInboundSequence(id);
        if (seq == null) {
            throw LOGGER.logSevereException(new InvalidSequenceException(LocalizationMessages.WSRM_3022_UNKNOWN_SEQUENCE_ID_IN_MESSAGE(id), id));
        }

        // int lastMessageNumber = csElement.getLastMsgNumber();
        com.sun.xml.ws.rm.v200702.CloseSequenceResponseElement csrElement = new com.sun.xml.ws.rm.v200702.CloseSequenceResponseElement();
        com.sun.xml.ws.rm.v200702.Identifier identifier = new com.sun.xml.ws.rm.v200702.Identifier();
        identifier.setValue(seq.getId());
        csrElement.setIdentifier(identifier);

        Message response = com.sun.xml.ws.api.message.Messages.create(
                getConfig().getRMVersion().getJAXBContext(),
                csrElement,
                getConfig().getSoapVersion());

        message.assertOneWay(false);

        Packet returnPacket = inbound.createServerResponse(
                response,
                getConfig().getConstants().getAddressingVersion(),
                getConfig().getSoapVersion(),
                getConfig().getRMVersion().getCloseSequenceResponseAction());

        //Generate SequenceAcknowledgmenet with Final element
        AbstractSequenceAcknowledgement element = seq.generateSequenceAcknowledgement(null, getMarshaller(), true);
        Header header = Headers.create(getConfig().getRMVersion().getJAXBContext(), element);
        response.getHeaders().add(header);

        return returnPacket;
    }

    public Packet handleAckRequestedAction(Packet inbound) throws RMException {
        try {
            Message message = inbound.getMessage();
            Header header = message.getHeaders().get(getConfig().getRMVersion().getAckRequestedQName(), true);
            if (header == null) {
                throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3010_INVALID_ACK_REQUESTED()));
            }

            AbstractAckRequested el = (AbstractAckRequested) header.readAsJAXB(getUnmarshaller());
            String id = null;
            if (el instanceof com.sun.xml.ws.rm.v200502.AckRequestedElement) {
                id = ((com.sun.xml.ws.rm.v200502.AckRequestedElement) el).getId();
            } else {
                id = ((com.sun.xml.ws.rm.v200702.AckRequestedElement) el).getId();
            }

            InboundSequence seq = RMDestination.getRMDestination().getInboundSequence(id);
            if (seq == null) {
                throw LOGGER.logSevereException(new InvalidSequenceException(LocalizationMessages.WSRM_3022_UNKNOWN_SEQUENCE_ID_IN_MESSAGE(id), id));
            }
            seq.resetLastActivityTime();
            return generateAckMessage(inbound, seq, getConfig().getRMVersion().getSequenceAcknowledgementAction());
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3011_ACK_REQUESTED_EXCEPTION(), e));
        }
    }

    /**
     * Handles a raw SequenceAcknowledgement
     */
    public Packet handleSequenceAcknowledgementAction(Packet inbound) throws RMException {
        try {
            Message message = inbound.getMessage();
            Header header = message.getHeaders().get(getConfig().getRMVersion().getSequenceAcknowledgementQName(), false);
            if (header == null) {
                throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3012_INVALID_SEQ_ACKNOWLEDGEMENT()));
            }

            AbstractSequenceAcknowledgement el = (AbstractSequenceAcknowledgement) header.readAsJAXB(getUnmarshaller());
            String id;
            if (el instanceof com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) {
                id = ((com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) el).getId();
            } else {
                id = ((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement) el).getId();
            }

            InboundSequence seq = RMDestination.getRMDestination().getInboundSequence(id);
            //reset inactivity timer
            seq.resetLastActivityTime();
            handleInboundMessage(inbound);
            inbound.transportBackChannel.close();
            Packet ret = new Packet(null);
            ret.invocationProperties.putAll(inbound.invocationProperties);
            return ret;
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3013_SEQ_ACKNOWLEDGEMENT_EXCEPTION(), e));
        }
    }

    public Packet handleMakeConnectionAction(Packet packet) throws RMException {
        if (getConfig().getRMVersion() == RMVersion.WSRM10) {
            throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3023_UNSUPPORTED_MAKECONNECTION_MESSAGE()));
        }

        com.sun.xml.ws.rm.v200702.MakeConnectionElement element = null;
        String sequenceId = null;
        Message message = packet.getMessage();
        try {
            element = message.readPayloadAsJAXB(getUnmarshaller());
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3024_INVALID_MAKECONNECTION_MESSAGE(), e));
        }

        sequenceId = element.getIdentifier().getValue();
        OutboundSequence outboundSequence = RMDestination.getRMDestination().getOutboundSequence(sequenceId);
        if (outboundSequence == null) {
            throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_3025_INVALID_SEQUENCE_ID_IN_MAKECONNECTION_MESSAGE(sequenceId)));
        }

        //see if we can find a message in the sequence that needs to be resent.
        RMMessage mess = outboundSequence.getUnacknowledgedMessage();
        Message jaxwsMessage = null;
        if (mess != null) {
            jaxwsMessage = mess.getCopy();
        } else {
            jaxwsMessage = com.sun.xml.ws.api.message.Messages.createEmpty(getConfig().getSoapVersion());
        }

        Packet ret = new Packet();
        ret.setMessage(jaxwsMessage);
        ret.invocationProperties.putAll(packet.invocationProperties);
        return ret;
    }

    /***********************************************************************************/
    /* Wiring for dispatch Map mapping wsa:Action values to handlers.  We are jumping  */
    /* through some hoops here to create a Map that only needs to be initialized once. */
    /***********************************************************************************/
    private interface ActionHandler {

        public Packet process(RMServerTube tube, Packet packet) throws RMException;
    }

    private void initActionMap() {
        actionMap.put(getConfig().getRMVersion().getCreateSequenceAction(),
                new ActionHandler() {

                    public Packet process(RMServerTube tube, Packet packet) throws RMException {
                        return tube.handleCreateSequenceAction(packet);
                    }
                });

        actionMap.put(getConfig().getRMVersion().getTerminateSequenceAction(),
                new ActionHandler() {

                    public Packet process(RMServerTube tube, Packet packet) throws RMException {
                        return tube.handleTerminateSequenceAction(packet);
                    }
                });

        actionMap.put(getConfig().getRMVersion().getAckRequestedAction(),
                new ActionHandler() {

                    public Packet process(RMServerTube tube, Packet packet) throws RMException {
                        return tube.handleAckRequestedAction(packet);
                    }
                });

        actionMap.put(getConfig().getRMVersion().getLastAction(),
                new ActionHandler() {

                    public Packet process(RMServerTube tube, Packet packet) throws RMException {
                        return tube.handleLastMessageAction(packet);
                    }
                });

        actionMap.put(RMVersion.WSRM11.getCloseSequenceAction(),
                new ActionHandler() {

                    public Packet process(RMServerTube tube, Packet packet) throws RMException {
                        return tube.handleCloseSequenceAction(packet);
                    }
                });

        actionMap.put(getConfig().getRMVersion().getSequenceAcknowledgementAction(),
                new ActionHandler() {

                    public Packet process(RMServerTube tube, Packet packet) throws RMException {
                        return tube.handleSequenceAcknowledgementAction(packet);
                    }
                });

        actionMap.put(getConfig().getRMVersion().getMakeConnectionAction(),
                new ActionHandler() {

                    public Packet process(RMServerTube tube, Packet packet) throws RMException {
                        return tube.handleMakeConnectionAction(packet);
                    }
                });
    }

    /**
     * Determine whether the STR used to secure request message is the one passed in
     * the CreateSequence message for the sequence.
     *
     * @param packet The inbound Packet containing the STR in a property
     * @param seq The InboundSequence
     *
     * @throws RMSecurityException if STR is missing or incorrect.
     */
    private void checkSTR(Packet packet, InboundSequence seq) throws RMSecurityException {
        SecurityContextToken sct = (SecurityContextToken) packet.invocationProperties.get(MessageConstants.INCOMING_SCT);
        URI uri = sct.getIdentifier();
        if (!uri.toString().equals(seq.getStrId())) {
            throw LOGGER.logSevereException(new RMSecurityException(LocalizationMessages.WSRM_3016_SECURITY_TOKEN_MISMATCH()));
        }
    }

    /**
     * Create a Packet containing a message with empty body and a single
     * SequenceAcknowledgement header reflecting the current status of
     * the specified inbound sequence.
     *
     * @param inbound Packet in request for which this method is being used
     *        to build a response.
     * @param seq The specified InboundSequence
     * @param action If null, add as value of wsa:Action
     * @param isFinal Required when the version is RM 1.1 the SequenceAcknowledgement element should have a 
     * @throws RMException
     */
    private Packet generateAckMessage(Packet inbound, InboundSequence seq, String action) throws RMException {
        //construct empty non-application message to be used as a conduit for
        //this SequenceAcknowledgement header.
        Message message = com.sun.xml.ws.api.message.Messages.createEmpty(getConfig().getSoapVersion());
        Packet outbound = new Packet(message);
        outbound.invocationProperties.putAll(inbound.invocationProperties);

        //construct the SequenceAcknowledgement header and  add it to thge message.
        AbstractSequenceAcknowledgement element = seq.generateSequenceAcknowledgement(null, getMarshaller(), false);
        //Header header = Headers.create(getConfig().getSoapVersion(),getMarshaller(),element);
        Header header = Headers.create(getConfig().getRMVersion().getJAXBContext(), element);
        message.getHeaders().add(header);
        if (action != null) {
            Header actionHeader = Headers.create(getConfig().getConstants().getAddressingVersion().actionTag, action);
            message.getHeaders().add(actionHeader);
        }

        return outbound;
    }

    private SOAPFault createSoapFault(QName subcode, String faultMessage) throws RMException {
        try {
            SOAPFactory factory;
            SOAPFault fault;
            if (getConfig().getSoapVersion() == SOAPVersion.SOAP_12) {
                factory = SOAPVersion.SOAP_12.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(subcode);
            // detail empty
            } else {
                factory = SOAPVersion.SOAP_11.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(subcode);
            }

            fault.setFaultString(faultMessage);
            return fault;
        } catch (SOAPException se) {
            throw new RMException(se);
        }
    }

    /**
     * Either creates a new <code>Session</code> for the
     * <code>InboundSequence</code> or returns one that has
     * already been created by the SC Pipe.
     *
     * @param sequence The InboundSequence
     * @return The Session
     */
    public Session startSession(InboundSequence sequence) {
        String id = sequence.getSessionId();
        Session sess = sessionManager.getSession(id);
        if (sess == null) {
            sess = sessionManager.createSession(id);
        }

        sess.setSequence(sequence);
        return sess;
    }

    /**
     * Terminates the session associated with the sequence if
     * RM owns the lifetime of the session.. i.e. If SC is not present.
     *
     * @param sequence The InboundSequence
     */
    public void endSession(InboundSequence sequence) {
        String sessionId = sequence.getSessionId();
        if (sessionId.equals(sequence.getId())) {
            //we own the session
            sessionManager.terminateSession(sessionId);
        }
    }

    /**
     * Sets the session and session id properties in a request packet
     * if necessary.  This will be the case if SC has not already done
     * so.
     *
     * @param packet The packet.
     * @param seq The sequence to which the request message belongs.
     */
    public void setSessionData(Packet packet, InboundSequence seq) {
        if (null == packet.invocationProperties.get(Session.SESSION_ID_KEY)) {
            packet.invocationProperties.put(Session.SESSION_ID_KEY, seq.getSessionId());
        }

        if (null == packet.invocationProperties.get(Session.SESSION_KEY)) {
            Session sess = sessionManager.getSession(seq.getSessionId());
            packet.invocationProperties.put(Session.SESSION_KEY, sess.getUserData());
        }
    }

    @Override
    protected InboundMessageProcessor getMessageProcessor() {
        return RMDestination.getRMDestination().getInboundMessageProcessor();
    }
}
