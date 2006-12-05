/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

/*
 * RMServerPipe.java
 *
 * @author Mike Grogan
 * @author Bhakti Mehta
 * Created on February 7, 2006, 2:10 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime.server;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.*;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.rm.*;
import com.sun.xml.ws.rm.jaxws.runtime.InboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.PipeBase;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.jaxws.runtime.client.ProtocolMessageReceiver;
import com.sun.xml.ws.rm.protocol.*;
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
import javax.xml.ws.soap.SOAPBinding;
import java.net.URI;
import java.util.HashMap;

/**
 * Server-side RM Pipe implementation
 */
public class RMServerPipe extends PipeBase<RMDestination,
        ServerOutboundSequence,
        ServerInboundSequence> {

    private static HashMap<String, ActionHandler> actionMap =
            new HashMap<String, ActionHandler>();

    private RMConstants constants;

    protected WSDLPort wsdlModel;

    protected WSEndpoint owner;

    protected SequenceConfig config;

    private boolean secureReliableMessaging = false;

    protected WSBinding binding;



    private SessionManager sessionManager =
            SessionManager.getSessionManager();

    //populate map if wsa:Action values for protocol messages to handlers used to process
    //messages with those headers
    static {
        initActionMap();
    }

    /**
     * Constructor is passed everything available in PipelineAssembler.
     *
     * @param wsdlModel The WSDLPort
     * @param owner The WSEndpoint.
     * @param nextPipe The next Pipe in the pipeline.
     *
     */
    public RMServerPipe(WSDLPort wsdlModel,
            WSEndpoint owner,
            Pipe nextPipe) {

        super(RMDestination.getRMDestination(), nextPipe);
        this.wsdlModel = wsdlModel;
        this.owner = owner;

        this.binding = this.owner.getBinding();
        this.config = getSequenceConfig();
        this.constants = config.getRMConstants();
        this.unmarshaller = constants.createUnmarshaller();
        this.marshaller = constants.createMarshaller();

    }

    /**
     * Copy constructor used by <code>copy</code> method.
     *
     * @param toCopy to be copied.
     * @param cloner passed as an argument to copy.
     */
    private RMServerPipe( RMServerPipe toCopy, PipeCloner cloner) {

        super(RMDestination.getRMDestination(), null);
        cloner.add(toCopy, this);
        nextPipe = cloner.copy(toCopy.nextPipe);
        wsdlModel = toCopy.wsdlModel;
        owner = toCopy.owner;
        config = toCopy.config;
        binding = owner.getBinding();
        this.constants = RMConstants.getRMConstants(binding.getAddressingVersion());
        this.unmarshaller = constants.createUnmarshaller();
        this.marshaller = constants.createMarshaller();

        //RMConstants.setAddressingVersion(binding.getAddressingVersion());

    }


    public Packet process(Packet packet) {

        com.sun.xml.ws.rm.Message message = null;
        ServerInboundSequence inboundSequence = null;
        SOAPFault soapFault = null;

        try {

            //handle special protocol messages
            Packet ret = null;
            try {
                ret = handleProtocolMessage(packet);
            } catch (CreateSequenceException e) {
                soapFault = newCreateSequenceRefusedFault(e);
            } catch (TerminateSequenceException e ) {
                soapFault = newSequenceTerminatedFault(e);
            }  catch (InvalidSequenceException e){
                soapFault = newUnknownSequenceFault(e);
            }
            
            if (ret != null) {
                //the contract of handleProtocolMessage is to return null if messager is non-protocol
                //message or protocol message is piggybacked on an application message.
                return ret;
            }

            //If we got here, this is an application message
            //do inbound bookkeeping
            try {
                message = handleInboundMessage(packet);
            } catch (MessageNumberRolloverException e) {
                soapFault = newMessageNumberRolloverFault(e);
            } catch (InvalidSequenceException e){
                soapFault = newUnknownSequenceFault(e);
            }
            
            if (soapFault != null){
                Message m = com.sun.xml.ws.api.message.Messages.create(soapFault);

                //SequenceFault is to be added for only SOAP 1.1
                if (binding.getSOAPVersion() == SOAPVersion.SOAP_11) {
                    //FIXME - need JAXBRIContext that can marshall SequenceFaultElement
                    Header header = Headers.create(binding.getSOAPVersion(),marshaller, new SequenceFaultElement());
                    m.getHeaders().add(header);
                }

                packet.setMessage(m);
                return packet;
            }

            //allow diagnostic access to message if ProcessingFilter has been specified
            if (filter != null) {
                filter.handleEndpointRequestMessage(message);
            }

            //If Message is a duplicate, handleInboundMessage threw a DuplicateMessageException,
            //Therefore, this is the first time processing this message.


            //use sequence id to initialize inboundSequence and outboundSequence
            //local variables by doing a lookup in RMDestination
            inboundSequence =
                    (ServerInboundSequence)message.getSequence();

            if (inboundSequence == null ) {
                throw new RMException("Message does not belong to a Reliable Messaging Sequence " +
                                      "and is not a Reliable Messaging protocol message.");

            }
            //reset inactivity timer
            inboundSequence.resetLastActivityTime();

            ServerOutboundSequence outboundSequence =
                    (ServerOutboundSequence)inboundSequence.getOutboundSequence();


            //determine whether the correct STR has been used to sign message
            if (secureReliableMessaging)
                checkSTR(packet,  inboundSequence);

            //set com.sun.xml.ws.session and com.sun.xml.ws.sessionid
            //invocationProperties if they have not already been set
            //by SC pipe.
            setSessionData(packet, 
                            inboundSequence);



            //If ordered deliver is configured,
            //Block here if InboundSequence reports gaps before this message.
            inboundSequence.holdIfUndeliverable(message);

            //clear packet.transporBackChannel so downstream pipes do not prevent
            //empty one-way response bodies to be sent back when we need to use the
            //bodies for RM SequenceAcknowledgemnts.
            packet.transportBackChannel = null;

            //make these available in an injected WebServiceContext
            packet.invocationProperties.put(Constants.sequenceProperty,
                                        inboundSequence);
            packet.invocationProperties.put(Constants.messageNumberProperty,
                                        message.getMessageNumber());
            ret = nextPipe.process(packet);
            
            // FIXME
            // This shouldn't be necessary, but havint messageNumberProperty
            // set has side-effects here due to the fact that RMClientPipe
            // and RMServerPipe share an implementation of handleOutboundMessage
            packet.invocationProperties.put(Constants.sequenceProperty,
                                        null);
            packet.invocationProperties.put(Constants.messageNumberProperty,
                                        null);


            Message responseMessage = ret.getMessage();
            Message emptyMessage ;

            if (responseMessage == null) {
                //This a one-way response. handleOutboundMessage
                //might need a message to write sequenceAcknowledgent headers to.
                //Give it one.
                emptyMessage = com.sun.xml.ws.api.message.Messages.createEmpty(config.getSoapVersion());
                ret.setMessage(emptyMessage);
                
                //propogate this information so handleOutboundMessage will not
                //add this to the outbound sequence, if any.
                ret.invocationProperties.put("onewayresponse",true);
            }

            //If ordered delivery is configured, unblock the next message in the sequence
            //if it is waiting for this one to be delivered.
            inboundSequence.releaseNextMessage(message);

            //Let Outbound sequence do its bookkeeping work, which consists of writing
            //outbound RM headers.

            //need to handle any error caused by
            //handleOutboundMessage.. Request has already been processed
            //by the endpoint.
            com.sun.xml.ws.rm.Message om =
                    handleOutboundMessage(outboundSequence, ret);

            //allow diagnostic access to outbound message if ProcessingFilter is
            //specified
            if (filter != null) {
                filter.handleEndpointResponseMessage(om);
            }


            //If we populated
            //ret with an empty message to be used by RM protocol, and it
            //was not used, get rid of the empty message.
            if (responseMessage == null &&
                    ret.getMessage() != null &&
                    !ret.getMessage().hasHeaders()) {
                        ret.setMessage(null);
                    
            } else {

                //Fill in relatedMessage field in request message for use in case request is resent.
                //the com.sun.xml.ws.api.message.Message referenced will be a copy of the one
                //contained in the returned packet.  See implementation of message.setRelatedMessage.
                message.setRelatedMessage(om);

                // MS client expects SequenceAcknowledgement action incase of oneway messages
                if (responseMessage == null && ret.getMessage() != null) {
                        HeaderList headerList = ret.getMessage().getHeaders();
                        
                        headerList.add(Headers.create(constants.getAddressingVersion().actionTag,
                                                        Constants.SEQUENCE_ACKNOWLEDGEMENT_ACTION));
                         
                }
            }

            return ret;

        } catch (BufferFullException e) {

            //need to return message with empty body and SequenceAcknowledgement
            //header for inboundSequence.  This is similar to handleAckRequestedAction, which
            //should do much the same thing.  The only difference is that handleAckRequestedAction
            //must also have logic to get the inboundSequence from the AckRequested header in the
            //packet
            if (packet.getMessage().isOneWay(wsdlModel)) {
                //refuse to process the request.  Client will retry
                Packet ret = new Packet();
                ret.invocationProperties.putAll(packet.invocationProperties);
                return ret;
            }

            //handleInboundMessage shouldn't let inboundSequence be null.
            try {
                ServerInboundSequence seq = (ServerInboundSequence)e.getSequence();
                if (seq != null) {
                    return generateAckMessage(packet, seq);
                } else {
                    //unreachable
                    return null;
                }
            } catch (RMException ee) {
                throw new WebServiceException(Messages.ACKNOWLEDGEMENT_MESSAGE_EXCEPTION.format() +e);
            }

        } catch (DuplicateMessageException e) {

            //  1. If one-way return empty message  without invoking process on next pipe
            //  2. If two-way, formulate response according to secret Microsoft protocol.
            //          a. If original was processed and response is still available in OutboundSequence,
            //              return it again.
            //          b. Otherwise (original not yet processed or response already discarded, return
            //             ack message.

            if (packet.getMessage().isOneWay(wsdlModel)) {
                //ignore the message.
                Packet ret = new Packet();
                ret.invocationProperties.putAll(packet.invocationProperties);
                return ret;

            } else {
                //check whether original response is available.
                com.sun.xml.ws.api.message.Message  response ;
                com.sun.xml.ws.rm.Message original = e.getRMMessage();
                com.sun.xml.ws.rm.Message origresp = original.getRelatedMessage();

                if (origresp != null) {
                    response = origresp.getCopy();
                    if (response != null) {
                        //original response is available, resend it.
                        Packet ret = new Packet(response);
                        ret.invocationProperties.putAll(packet.invocationProperties);
                        return ret;
                    }
                }

                //either the original request is waiting to be processing, or the response has been
                //acked and thrown away.  All we can do is return a SequenceAcknowledgement.
                try {

                    ServerInboundSequence seq =
                            (ServerInboundSequence)original.getSequence();
                    return generateAckMessage(packet, seq);

                } catch (RMException ee) {
                    throw new WebServiceException(ee);
                }
            }
        }    catch (RMException e) {

            //see if a RM Fault type has been assigned to this exception type.  If so, let
            //the exception generate a fault message.  Otherwise, wrap as WebServiceException and
            //rethrow.
            Message m = e.getFaultMessage();
            if (m != null) {
                return new Packet(m);
            } else {
                throw new WebServiceException(e);
            }

        } catch (RuntimeException e) {
            throw new WebServiceException(e);
        }
    }


    public void preDestroy() {

        //nothing to do here so far
        nextPipe.preDestroy();
    }

    public  Pipe copy(PipeCloner cloner) {
        return new RMServerPipe(this, cloner);

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

        ActionHandler handler ;
        String actionValue = null;
        actionValue = packet.getMessage()
                    .getHeaders().getAction(constants.getAddressingVersion(),
                                            config.getSoapVersion());
        if (actionValue == null || actionValue.equals("")) {
          throw new RMException("Non RM Request or Missing wsa:Action header" )
                                        ;
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

    public Packet handleCreateSequenceAction(Packet packet) throws RMException{

        CreateSequenceElement csrElement;
        Identifier id ;
        String offeredId = null;
        Message message = packet.getMessage();


        try {
            csrElement = message.readPayloadAsJAXB(unmarshaller);
        } catch (JAXBException e) {
            throw new RMException(Messages.CREATESEQUENCE_HEADER_PROBLEM.format() + e);
        }

        /**ADDRESSING_FIXME
         *  Assume for now that AcksTo is anonymous.
         */
        URI acksTo = constants.getAnonymousURI();
        String acksToString = acksTo.toString();
        String replyToString = acksToString;
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

        OfferType offer = csrElement.getOffer();
        if (offer != null) {
            id = offer.getIdentifier();
            if (id != null) {
                offeredId = id.getValue();
            }
        }

        //create server-side data structures.
        ServerInboundSequence inboundSequence =
                provider.createSequence(acksTo,
                                        null, //assign random id
                                        offeredId,
                                        config);

        //start the inactivity timer
        inboundSequence.resetLastActivityTime();


        //TODO.. Read STR element in csrElement if any
        this.secureReliableMessaging = csrElement.getSecurityTokenReference()!=null?true:false;
        if (this.secureReliableMessaging) {
            com.sun.xml.ws.security.secext10.SecurityTokenReferenceType strType= csrElement.getSecurityTokenReference();
            SecurityContextToken sct = (SecurityContextToken)packet.invocationProperties.get(MessageConstants.INCOMING_SCT);
            if (sct != null){
                String strId = sct.getIdentifier().toString();
                WSTrustElementFactory wsTrustElemFactory = WSTrustElementFactory.newInstance();
                JAXBElement jaxbElem = new com.sun.xml.ws.security.secext10.ObjectFactory().createSecurityTokenReference(strType);
                SecurityTokenReference str = wsTrustElemFactory.createSecurityTokenReference(jaxbElem);

                com.sun.xml.ws.security.trust.elements.str.Reference ref = str.getReference();
                if (ref instanceof com.sun.xml.ws.security.trust.elements.str.DirectReference) {
                    DirectReference directRef = (DirectReference)ref;
                    String gotId = directRef.getURIAttr().toString();
                    if (gotId.equals(strId)){
                        inboundSequence.setStrId(strId);
                    } else {
                        throw new RMSecurityException(Messages.SECURITY_TOKEN_AUTHORIZATION_ERROR.format(gotId ,strId ));
                    }
                } else throw new RMSecurityException(Messages.SECURITY_REFERENCE_ERROR.format( ref.getClass().getName()));

            } else throw new RMSecurityException(Messages.NULL_SECURITY_TOKEN.format());
        }

        startSession(inboundSequence);

        if (offeredId == null) {
            inboundSequence.getOutboundSequence().saveMessages = false;
        }

        //initialize CreateSequenceResponseElement
        CreateSequenceResponseElement crsElement = new CreateSequenceResponseElement();

        AcceptType accept ;

        Identifier id2 = new Identifier();
        id2.setValue(inboundSequence.getId());
        crsElement.setIdentifier(id2);
        URI dest;
        if (offeredId != null) {


            String destString = message.getHeaders()
                                        .getTo(constants.getAddressingVersion(),
                                               config.getSoapVersion());
            try {
                dest = new URI(destString);
            } catch (Exception e) {
                throw new RMException(
                        "Invalid or missing \"To\" header on CreateSequence messge.");
            }
            
            accept = new AcceptType();
            W3CEndpointReference endpointReference = null;
            WSEndpointReference wsepr = new WSEndpointReference(dest,constants.getAddressingVersion());
            if ( constants.getAddressingVersion()== AddressingVersion.W3C){
                endpointReference = (W3CEndpointReference)wsepr.toSpec();
                accept.setAcksTo(endpointReference);
            }    /*else {
                //TODO support MemberSubmissionEndpointReference when issue 131 of JAXB is resolved
                //endpointReference = (MemberSubmissionEndpointReference)wsepr.toSpec() ;
            }*/

            crsElement.setAccept(accept);
        }

        Message response = com.sun.xml.ws.api.message.Messages.create(marshaller,
                            crsElement,
                            config.getSoapVersion());

        message.assertOneWay(false);

        /*ADDRESSING_FIXME
         * This will probably be broken with MS client if they still send CS with
         * missing reply-to.
         */
        Packet ret = packet.createServerResponse(response, constants.getAddressingVersion(),
                                                config.getSoapVersion(),
                                                Constants.CREATE_SEQUENCE_RESPONSE_ACTION);
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
                constants.getCreateSequenceResponseAction()));



        ret.invocationProperties.put(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND,
                outboundAddressingProperties);
        */
        return ret;
    }

    public Packet handleTerminateSequenceAction(Packet packet)
    throws RMException {

        TerminateSequenceElement tsElement ;
        Message message = packet.getMessage();

        try {
            tsElement = message.readPayloadAsJAXB(unmarshaller);
        } catch (JAXBException e) {
            throw new TerminateSequenceException(Messages.TERMINATE_SEQUENCE_EXCEPTION.format() + e);
        }

        String id = tsElement.getIdentifier().getValue();
        ServerInboundSequence seq = provider.getInboundSequence(id);
        if (seq == null) {
            throw new InvalidSequenceException(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,id),id);
        }


        //end the session if we own its lifetime..i.e. SC is not
        //present
        endSession(seq);

        provider.terminateSequence(id);

        //formulate response if required
        Packet ret ;
        OutboundSequence outboundSequence = seq.getOutboundSequence();

        //If there is an "real" outbound sequence, client expects us to terminate it.
        if (outboundSequence.saveMessages) {
            TerminateSequenceElement terminateSeqResponse = new TerminateSequenceElement();
            Identifier id2 = new Identifier();
            id2.setValue(outboundSequence.getId());

            terminateSeqResponse.setIdentifier(id2);
            Message response = com.sun.xml.ws.api.message.Messages.create(marshaller,
                    terminateSeqResponse,
                    config.getSoapVersion());

            //ret = packet.createServerResponse(response, wsdlModel, binding);
             ret = packet.createServerResponse(response,
                    constants.getAddressingVersion(),
                    config.getSoapVersion(), Constants.TERMINATE_SEQUENCE_ACTION);
             
            SequenceAcknowledgementElement element = seq.generateSequenceAcknowledgement(null, marshaller);
            //Header header = Headers.create(config.getSoapVersion(),marshaller,element);
            //Header actionHeader = Headers.create(constants.getAddressingVersion().actionTag,
            //                                       Constants.TERMINATE_SEQUENCE_ACTION);
            Header header = createHeader(element);
            response.getHeaders().add(header);
            //response.getHeaders().add(actionHeader);

        } else {
            packet.transportBackChannel.close();
            ret = new Packet(null);
        }

        return ret;

    }

    public Packet handleLastMessageAction(Packet inbound) throws RMException {

        try {
            Message message = inbound.getMessage();
            Header header = message.getHeaders().get(constants.getSequenceQName(), true);
            if (header == null) {
                throw new RMException(Messages.INVALID_LAST_MESSAGE.format());
            }

            SequenceElement el = (SequenceElement)header.readAsJAXB(unmarshaller);
            String id = el.getId();

            ServerInboundSequence seq = provider.getInboundSequence(id);
            if (seq == null) {
                throw new InvalidSequenceException(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,id),id);
            }

            //add message to ClientInboundSequence so that this message
            //number appears in sequence acknowledgement
            int messageNumber = (int)el.getNumber();
            seq.set(messageNumber, new com.sun.xml.ws.rm.Message(message));

            return generateAckMessage(inbound, seq, constants.getLastAction());

        } catch (JAXBException e) {
            throw new RMException(Messages.LAST_MESSAGE_EXCEPTION.format() +e);
        }
    }

    public Packet handleAckRequestedAction(Packet inbound) throws RMException {

        try {

            Message message = inbound.getMessage();
            Header header = message.getHeaders().get(constants.getAckRequestedQName(), true);
            if (header == null) {
                throw new RMException(Messages.INVALID_ACK_REQUESTED.format());
            }

            AckRequestedElement el = (AckRequestedElement)header
                    .readAsJAXB(unmarshaller);
            String id = el.getId();

            ServerInboundSequence seq = provider.getInboundSequence(id);

            if (seq == null) {
                throw new InvalidSequenceException(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,id),id);
            }
            seq.resetLastActivityTime();

            return generateAckMessage(inbound, seq);

        } catch (JAXBException e) {
            throw new RMException(Messages.ACK_REQUESTED_EXCEPTION.format() +e);
        }

    }

    /**
     * Handles a raw SequenceAcknowledgement
     */
    public Packet handleSequenceAcknowledgementAction(Packet inbound) throws RMException {
        try {

            Message message = inbound.getMessage();
            Header header = message.getHeaders().get(constants.getSequenceAcknowledgementQName());
            if (header == null) {
                throw new RMException(Messages.INVALID_SEQ_ACKNOWLEDGEMENT.format());
            }

            SequenceAcknowledgementElement el = (SequenceAcknowledgementElement)header
                    .readAsJAXB(unmarshaller);


            String id = el.getId();
            ServerInboundSequence seq = provider.getInboundSequence(id);

            //reset inactivity timer
            seq.resetLastActivityTime();

            if (seq == null) {
                //we may be in the pipeline of a ProtocolMessageReceiver.  In
                //that case, pass it on to ProtocolMessageReceiver who can hold
                //onto it if the sequence creation is still pending
                ProtocolMessageReceiver.handleAcknowledgement(el);

            } else {
                handleInboundMessage(inbound);
            }

            inbound.transportBackChannel.close();
            Packet ret = new Packet(null);
            ret.invocationProperties.putAll(inbound.invocationProperties);
            return ret;


        } catch (JAXBException e) {
            throw new RMException(Messages.SEQ_ACKNOWLEDGEMENT_EXCEPTION.format() +e);
        }
    }

    /**
     * This part of a Plugfest hack.  We are trying to support "non-addressable client"
     * scenarios wherein protocol responses are received on separate HTTP connections.
     * A request containing a CreateSequenceElement has been sent to the endpoint and
     * we are hosting an endpoint in ProtocolMessageReceiver and this handler will be
     * called in the Pipeline for that endpoint.  ProtocolMessageReceiver will correlate
     * the response culled from the message here, with the request.
     */
    public Packet handleCreateSequenceResponseAction(Packet inbound) throws RMException {

        /*
         * ADDRESSING_FIXME
         * Fix this when we need to support non-anonymous acksTo
         */
        return null;
        /*
        CreateSequenceResponseElement csrElement ;

        Message message = inbound.getMessage();

        AddressingProperties inboundAddressingProperties =
                (AddressingProperties)(inbound.invocationProperties
                .get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND));

        try {
            csrElement = message.readPayloadAsJAXB(unmarshaller);
        } catch (JAXBException e) {
            throw new RMException(Messages.INVALID_CREATE_SEQUENCE_RESPONSE.format() +e);
        }

        Relationship[] relatesTo = inboundAddressingProperties.getRelatesTo();
        Relationship relationship ;
        if (relatesTo == null || null == (relationship = relatesTo[0])){
            throw new RMException(Messages.CREATE_SEQUENCE_CORRELATION_ERROR.format());
        }
        String messageId = relationship.getID().toString();
        ProtocolMessageReceiver.setCreateSequenceResponse(messageId, csrElement);

        inbound.transportBackChannel.close();
        Packet ret = new Packet(null);
        ret.invocationProperties.putAll(inbound.invocationProperties);
        return ret;
        */
    }


    /***********************************************************************************/
    /* Wiring for dispatch Map mapping wsa:Action values to handlers.  We are jumping  */
    /* through some hoops here to create a Map that only needs to be initialized once. */
    /***********************************************************************************/


    private interface ActionHandler {
        public Packet process(RMServerPipe pipe, Packet packet)
        throws RMException ;
    }

    private static void initActionMap(){
        actionMap.put(Constants.CREATE_SEQUENCE_ACTION,
                new ActionHandler() {
            public Packet process(RMServerPipe pipe, Packet packet)
            throws RMException   {
                return pipe.handleCreateSequenceAction(packet);
            }
        });

        actionMap.put(Constants.TERMINATE_SEQUENCE_ACTION,
                new ActionHandler() {
            public Packet process(RMServerPipe pipe, Packet packet)
            throws RMException  {
                return pipe.handleTerminateSequenceAction(packet);
            }
        });

        actionMap.put(Constants.ACK_REQUESTED_ACTION,
                new ActionHandler() {
            public Packet process(RMServerPipe pipe, Packet packet)
            throws RMException  {
                return pipe.handleAckRequestedAction(packet);
            }
        });

        actionMap.put(Constants.LAST_MESSAGE_ACTION,
                new ActionHandler() {
            public Packet process(RMServerPipe pipe, Packet packet)
            throws RMException {
                return pipe.handleLastMessageAction(packet);
            }
        });

        actionMap.put(Constants.CREATE_SEQUENCE_RESPONSE_ACTION,
                new ActionHandler() {
            public Packet process(RMServerPipe pipe, Packet packet)
            throws RMException {
                return pipe.handleCreateSequenceResponseAction(packet);
            }
        });

        actionMap.put(Constants.SEQUENCE_ACKNOWLEDGEMENT_ACTION,
                new ActionHandler() {
            public Packet process(RMServerPipe pipe, Packet packet)
            throws RMException {
                return pipe.handleSequenceAcknowledgementAction(packet);
            }
        });

    }

    /*
     * Private helper functions.
     */

    /**
     * Returns a message containing a fault defined by the WS-RM spec.
     *
     * @param
     *      e An Exception mapped to a WS-RM defined fault.
     * @return
     *      The mapped fault
     * @throws
     *      Exception - Exceptions not mapped to well-known fault types are
     *      rethrown.
     */

    /**
     * Initialize a <code>SequenceConfig</code> using the metadata passed in the
     * ctor.
     */
    SequenceConfig getSequenceConfig() {

        SequenceConfig ret;
        if (wsdlModel != null) {
            /*
             If there is a WSDL, use the SequenceConfig ctor taking a WSDLPort and
             initialize SOAPVersion according to the value obtained from the binding.
             */
            ret =  new SequenceConfig(wsdlModel,this.binding);
            BindingID bindingid = wsdlModel.getBinding().getBindingId();
            if (bindingid.equals(BindingID.parse(SOAPBinding.SOAP11HTTP_BINDING))) {
                ret.setSoapVersion(SOAPVersion.SOAP_11);
            } else {
                ret.setSoapVersion(SOAPVersion.SOAP_12);
            }
        }  else {
            /*
             Use SequenceConfig initialized with default values.
             */
            ret = new SequenceConfig();
        }
        return ret;
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

    private void checkSTR(Packet packet, InboundSequence seq)throws RMSecurityException {
        SecurityContextToken sct = (SecurityContextToken)packet.invocationProperties.get(MessageConstants.INCOMING_SCT);
        URI uri = sct.getIdentifier();
        if(!uri.toString().equals(seq.getStrId())){
            throw new RMSecurityException(Messages.SECURITY_TOKEN_MISMATCH.format());
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
     * @throws RMException
     */
    private Packet generateAckMessage(Packet inbound, ServerInboundSequence seq)
    throws RMException {
        return generateAckMessage(inbound, seq, null);
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
     * @throws RMException
     */
    private Packet generateAckMessage(Packet inbound, ServerInboundSequence seq, String action)
    throws RMException {

        //construct empty non-application message to be used as a conduit for
        //this SequenceAcknowledgement header.
        Message message = com.sun.xml.ws.api.message.Messages.createEmpty(config.getSoapVersion());
        Packet outbound = new Packet(message);
        outbound.invocationProperties.putAll(inbound.invocationProperties);

        //construct the SequenceAcknowledgement header and  add it to thge message.
        SequenceAcknowledgementElement element = seq.generateSequenceAcknowledgement(null, marshaller);
        //Header header = Headers.create(config.getSoapVersion(),marshaller,element);
        Header header = createHeader(element);
        message.getHeaders().add(header);
        if (action != null) {
            Header h = Headers.create(constants.getAddressingVersion().actionTag,
                                        action);
            message.getHeaders().add(h);
        }

        return outbound;
    }

    private SOAPFault newMessageNumberRolloverFault(MessageNumberRolloverException e) throws RMException {
        QName subcode = Constants.MESSAGE_NUMBER_ROLLOVER_QNAME;
        String faultstring = String.format(Constants.MESSAGE_NUMBER_ROLLOVER_TEXT, e.getMessageNumber());

        try {
            SOAPFactory factory;
            SOAPFault fault;
            if (binding.getSOAPVersion() == SOAPVersion.SOAP_12) {
                factory = SOAPVersion.SOAP_12.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(Constants.SOAP12_SENDER_QNAME);
                fault.appendFaultSubcode(subcode);
                // not sure what more to put in detail element

            } else {
                factory = SOAPVersion.SOAP_11.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(subcode);
            }

            fault.setFaultString(faultstring);

            return fault;
        } catch (SOAPException se) {
            throw new RMException(se);
        }
    }

    private SOAPFault newUnknownSequenceFault(InvalidSequenceException e) throws RMException {
        QName subcode = Constants.UNKNOWN_SEQUENCE_QNAME;
        String faultstring = String.format(Constants.UNKNOWN_SEQUENCE_TEXT,e.getSequenceId());

        try {
            SOAPFactory factory;
            SOAPFault fault;
            if (binding.getSOAPVersion() == SOAPVersion.SOAP_12) {
                factory = SOAPVersion.SOAP_12.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(Constants.SOAP12_SENDER_QNAME);
                fault.appendFaultSubcode(subcode);
                // not sure what more to put in detail element

            } else {
                factory = SOAPVersion.SOAP_11.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(subcode);
            }

            fault.setFaultString(faultstring);

            return fault;
        } catch (SOAPException se) {
            throw new RMException(se);
        }
    }

    private SOAPFault newSequenceTerminatedFault(TerminateSequenceException e) throws RMException {
        QName subcode = Constants.SEQUENCE_TERMINATED_QNAME;
        String faultstring = String.format(Constants.SEQUENCE_TERMINATED_TEXT,e.getMessage());

        try {
            SOAPFactory factory;
            SOAPFault fault;
            if (binding.getSOAPVersion() == SOAPVersion.SOAP_12) {
                factory = SOAPVersion.SOAP_12.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(Constants.SOAP12_SENDER_QNAME);
                fault.appendFaultSubcode(subcode);
                // detail empty

            } else {
                factory = SOAPVersion.SOAP_11.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(subcode);
            }

            fault.setFaultString(faultstring);

            return fault;
        } catch (SOAPException se) {
            throw new RMException(se);
        }
    }

    private SOAPFault newCreateSequenceRefusedFault(CreateSequenceException e) throws RMException {
        QName subcode = Constants.CREATE_SEQUENCE_REFUSED_QNAME;
        String faultstring = String.format(Constants.CREATE_SEQUENCE_REFUSED_TEXT,e.getMessage());

        try {
            SOAPFactory factory;
            SOAPFault fault;
            if (binding.getSOAPVersion() == SOAPVersion.SOAP_12) {
                factory = SOAPVersion.SOAP_12.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(Constants.SOAP12_SENDER_QNAME);
                fault.appendFaultSubcode(subcode);
                // detail empty

            } else {
                factory = SOAPVersion.SOAP_11.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(subcode);
            }

            fault.setFaultString(faultstring);

            return fault;
        } catch (SOAPException se) {
            throw new RMException(se);
        }

    }
    
    private Header createHeader(Object obj) {
        return Headers.create(constants.getJAXBRIContext(), obj);
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
    public void setSessionData(Packet packet,
                                InboundSequence seq) {
        if (null == packet.invocationProperties
                .get(Session.SESSION_ID_KEY)) {
            packet.invocationProperties
                    .put(Session.SESSION_ID_KEY, seq.getSessionId());
        }

        if (null == packet.invocationProperties
                .get(Session.SESSION_KEY)) {
            Session sess = sessionManager.getSession(seq.getSessionId());
            packet.invocationProperties
                    .put(Session.SESSION_KEY, sess.getUserData());

        }
        
       
    }


}
