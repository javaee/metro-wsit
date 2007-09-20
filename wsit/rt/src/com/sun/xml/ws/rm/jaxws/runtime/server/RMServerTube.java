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

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.*;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.rm.*;
import com.sun.xml.ws.rm.jaxws.runtime.InboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.jaxws.runtime.TubeBase;
import com.sun.xml.ws.rm.jaxws.util.LoggingHelper;
import com.sun.xml.ws.rm.protocol.*;
import com.sun.xml.ws.rm.v200502.*;
import com.sun.xml.ws.rm.v200702.MakeConnectionElement;
import com.sun.xml.ws.rm.v200702.CloseSequenceElement;
import com.sun.xml.ws.rm.v200702.CloseSequenceResponseElement;
import com.sun.xml.ws.rm.v200702.TerminateSequenceResponseElement;
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
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Server-side RM Tube implementation
 */
public class RMServerTube extends TubeBase<RMDestination,
        ServerOutboundSequence,
        ServerInboundSequence> {



    public static final Logger logger =
            Logger.getLogger(LoggingHelper.getLoggerName(RMServerTube.class),
                    Messages.class.getName());


    public static final LoggingHelper loggingHelper = new LoggingHelper(logger);

    private static HashMap<String, ActionHandler> actionMap =
            new HashMap<String, ActionHandler>();

    private RMConstants constants;

    protected WSDLPort wsdlModel;

    protected WSEndpoint owner;

    protected SequenceConfig config;

    private boolean secureReliableMessaging = false;

    protected WSBinding binding;
    
    private com.sun.xml.ws.rm.Message currentRequestMessage;
   

    private SessionManager sessionManager =
            SessionManager.getSessionManager();

    
    /**
     * Constructor is passed everything available in PipelineAssembler.
     *
     * @param wsdlModel The WSDLPort
     * @param owner The WSEndpoint.
     * @param nextTube The next Tube in the pipeline.
     *
     */
    public RMServerTube(WSDLPort wsdlModel,
            WSEndpoint owner,
            Tube nextTube) {

        super(RMDestination.getRMDestination(), nextTube);
        this.wsdlModel = wsdlModel;
        this.owner = owner;

        this.binding = this.owner.getBinding();
        
        this.config = getSequenceConfig();
        this.version = config.rmVersion;
        this.constants = config.getRMConstants();
        this.unmarshaller = config.getRMVersion().createUnmarshaller();
        this.marshaller = config.getRMVersion().createMarshaller();
        initActionMap();
        
    }

    /**
     * Copy constructor used by <code>copy</code> method.
     *
     * @param toCopy to be copied.
     * @param cloner passed as an argument to copy.
     */
    private RMServerTube( RMServerTube toCopy, TubeCloner cloner) {

        super(RMDestination.getRMDestination(), toCopy, cloner);
        
        this.wsdlModel = toCopy.wsdlModel;
        this.owner = toCopy.owner;
        this.config = toCopy.config;
        this.binding = owner.getBinding();
        this.version = toCopy.version;
        this.constants = RMConstants.getRMConstants(binding.getAddressingVersion());
        this.unmarshaller = config.getRMVersion().createUnmarshaller();
        this.marshaller = config.getRMVersion().createMarshaller();
        initActionMap();
     

    }

    @Override
    public NextAction processRequest(Packet packet) {

        
        SOAPFault soapFault = null;
        com.sun.xml.ws.rm.Message message = null;
     

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
                return doReturnWith(ret);
            }

            //If we got here, this is an application message
            //do inbound bookkeeping
            try {
                message = handleInboundMessage(packet);
            } catch (MessageNumberRolloverException e) {
                soapFault = newMessageNumberRolloverFault(e);
            } catch (InvalidSequenceException e){
                soapFault = newUnknownSequenceFault(e);
            } catch (CloseSequenceException e){
                soapFault = newClosedSequenceFault(e);
            }
            
            Packet retPacket = null;
            if (soapFault != null){
                Message m = com.sun.xml.ws.api.message.Messages.create(soapFault);

                //SequenceFault is to be added for only SOAP 1.1
                if (binding.getSOAPVersion() == SOAPVersion.SOAP_11) {
                    //FIXME - need JAXBRIContext that can marshall SequenceFaultElement
                    Header header = Headers.create(config.getRMVersion().getJAXBContext(), 
                                                    new SequenceFaultElement());
                    m.getHeaders().add(header);
                }

                 retPacket = packet.createServerResponse(
                         m,constants.getAddressingVersion() , 
                         binding.getSOAPVersion(), 
                         constants.getAddressingVersion().getDefaultFaultAction());
                 retPacket.setMessage(m);
                 doReturnWith(retPacket);
            }

            //allow diagnostic access to message if ProcessingFilter has been specified
            if (filter != null) {
                filter.handleEndpointRequestMessage(message);
            }

            //If Message is a duplicate, handleInboundMessage threw a DuplicateMessageException,
            //Therefore, this is the first time processing this message.


            //use sequence id to initialize inboundSequence and outboundSequence
            //local variables by doing a lookup in RMDestination
            ServerInboundSequence inboundSequence =
                    (ServerInboundSequence)message.getSequence();

            if (inboundSequence == null ) {
                logger.log(Level.SEVERE, com.sun.xml.ws.rm.jaxws.runtime.server.Messages.NOT_RELIABLE_SEQ_OR_PROTOCOL_MESSAGE.format()
                    );
                throw new RMException(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.NOT_RELIABLE_SEQ_OR_PROTOCOL_MESSAGE.format());

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



            

            //clear packet.transporBackChannel so downstream pipes do not prevent
            //empty one-way response bodies to be sent back when we need to use the
            //bodies for RM SequenceAcknowledgemnts.
            packet.transportBackChannel = null;

            //make these available in an injected WebServiceContext
            packet.invocationProperties.put(Constants.sequenceProperty,
                                        inboundSequence);
            packet.invocationProperties.put(Constants.messageNumberProperty,
                                        message.getMessageNumber());
            
            //If ordered deliver is configured,
            //Block here if InboundSequence reports gaps before this message.
            //inboundSequence.holdIfUndeliverable(message);
            
            //send the message down the Tubeline
             this.currentRequestMessage = message;
             
            if (!inboundSequence.isOrdered()) {
                return doInvoke(next , packet);
            } else {
                MessageSender sender = new TubelineSender(this, packet,
                                                    config.getSoapVersion(),
                                                    constants.getAddressingVersion()
                                                    );
             
                           
                message.setMessageSender(sender);
                
                //send message down tubeline if predecesor has arrived.  Otherwise. 
                //it will have to wait until releaseNextMessage is called during the
                //processing of the predecessor in processResponse.
                if (inboundSequence.isDeliverable(message)) {
                    sender.send();
                }
                
                return doSuspend();
           
            }
        }   catch (BufferFullException e) {

            //need to return message with empty body and SequenceAcknowledgement
            //header for inboundSequence.  This is similar to handleAckRequestedAction, which
            //should do much the same thing.  The only difference is that handleAckRequestedAction
            //must also have logic to get the inboundSequence from the AckRequested header in the
            //packet
            if (packet.getMessage().isOneWay(wsdlModel)) {
                //refuse to process the request.  Client will retry
                Packet ret = new Packet();
                ret.invocationProperties.putAll(packet.invocationProperties);
                return doReturnWith(ret);
            }

            //handleInboundMessage shouldn't let inboundSequence be null.
            try {
                ServerInboundSequence seq = (ServerInboundSequence)e.getSequence();
                if (seq != null) {
                    Packet ret = generateAckMessage(packet, seq, 
                                    config.getRMVersion().getSequenceAcknowledgementAction());
                    return doReturnWith(ret);
                } else {
                    //unreachable
                    return null;
                }
            } catch (RMException ee) {
                logger.severe(Messages.ACKNOWLEDGEMENT_MESSAGE_EXCEPTION.format() +e);
                return doThrow(new WebServiceException(Messages.ACKNOWLEDGEMENT_MESSAGE_EXCEPTION.format() +e));
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
                return doReturnWith(ret);

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
                        return doReturnWith(ret);
                    }
                }

                //either the original request is waiting to be processing, or the response has been
                //acked and thrown away.  All we can do is return a SequenceAcknowledgement.
                try {

                    ServerInboundSequence seq =
                            (ServerInboundSequence)original.getSequence();
                    Packet ret = generateAckMessage(packet, seq, 
                                                config.getRMVersion().getSequenceAcknowledgementAction());
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
        ServerInboundSequence inboundSequence = 
                (ServerInboundSequence)currentRequestMessage.getSequence();
        if (!inboundSequence.isOrdered()) {
           
            postProcess(packet);
        }
        
        return doReturnWith(packet);
    }

    @Override
    public  NextAction processException(Throwable t) {
        return doThrow(t);
    }
    

    public void postProcess(Packet packet) {
        
        ServerInboundSequence inboundSequence = 
                (ServerInboundSequence)currentRequestMessage.getSequence();
        ServerOutboundSequence outboundSequence = 
                (ServerOutboundSequence)inboundSequence.getOutboundSequence();
        try {
        
            // This shouldn't be necessary, but having messageNumberProperty
            // set has side-effects here due to the fact that RMClientPipe
            // and RMServerPipe share an implementation of handleOutboundMessage
            packet.invocationProperties.put(Constants.sequenceProperty,
                                        null);
            packet.invocationProperties.put(Constants.messageNumberProperty,
                                        null);


            Message responseMessage = packet.getMessage();
            Message emptyMessage ;

            if (responseMessage == null) {

                //This a one-way response. handleOutboundMessage
                //might need a message to write sequenceAcknowledgent headers to.
                //Give it one.
                emptyMessage = com.sun.xml.ws.api.message.Messages.createEmpty(config.getSoapVersion());
                packet.setMessage(emptyMessage);

                //propogate this information so handleOutboundMessage will not
                //add this to the outbound sequence, if any.
                packet.invocationProperties.put("onewayresponse",true);
            }

            //If ordered delivery is configured, unblock the next message in the sequence
            //if it is waiting for this one to be delivered.
            inboundSequence.releaseNextMessage(currentRequestMessage);
            

            //Let Outbound sequence do its bookkeeping work, which consists of writing
            //outbound RM headers.

            //need to handle any error caused by
            //handleOutboundMessage.. Request has already been processed
            //by the endpoint.
            com.sun.xml.ws.rm.Message om =
                    handleOutboundMessage(outboundSequence, packet);

            //allow diagnostic access to outbound message if ProcessingFilter is
            //specified
            if (filter != null) {
                filter.handleEndpointResponseMessage(om);
            }


            //If we populated
            //ret with an empty message to be used by RM protocol, and it
            //was not used, get rid of the empty message.
            if (responseMessage == null &&
                    packet.getMessage() != null &&
                    !packet.getMessage().hasHeaders()) {
                        packet.setMessage(null);

            } else {

                //Fill in relatedMessage field in request message for use in case request is resent.
                //the com.sun.xml.ws.api.message.Message referenced will be a copy of the one
                //contained in the returned packet.  See implementation of message.setRelatedMessage.
                currentRequestMessage.setRelatedMessage(om);

                // MS client expects SequenceAcknowledgement action incase of oneway messages
                if (responseMessage == null && packet.getMessage() != null) {

                        HeaderList headerList = packet.getMessage().getHeaders();

                        headerList.add(Headers.create(constants.getAddressingVersion().actionTag,
                                                        config.getRMVersion().getSequenceAcknowledgementAction()));

                }
            }


        }  catch (RMException e) {

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


    public void preDestroy() {

        //nothing to do here so far
        next.preDestroy();
    }

    public  RMServerTube copy(TubeCloner cloner) {
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

        ActionHandler handler ;
        String actionValue ;
        actionValue = packet.getMessage()
                    .getHeaders().getAction(constants.getAddressingVersion(),
                                            config.getSoapVersion());
        if (actionValue == null || actionValue.equals("")) {
          logger.severe(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.NON_RM_REQUEST_OR_MISSING_WSA_ACTION_HEADER.format());
          throw new RMException(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.NON_RM_REQUEST_OR_MISSING_WSA_ACTION_HEADER.format() )
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

        AbstractCreateSequence csrElement;

        String offeredId = null;
        Message message = packet.getMessage();


        try {
            csrElement = message.readPayloadAsJAXB(unmarshaller);
        } catch (JAXBException e) {
            logger.severe(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.CREATESEQUENCE_HEADER_PROBLEM.format() + e);
            throw new RMException(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.CREATESEQUENCE_HEADER_PROBLEM.format() + e);
        }

        /**ADDRESSING_FIXME
         *  Assume for now that AcksTo is anonymous.
         */
        URI acksTo = constants.getAnonymousURI();
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
        if (csrElement instanceof com.sun.xml.ws.rm.v200502.CreateSequenceElement)      {
            com.sun.xml.ws.rm.v200502.OfferType offer = ((com.sun.xml.ws.rm.v200502.CreateSequenceElement)csrElement).getOffer();
            if (offer != null) {
                com.sun.xml.ws.rm.v200502.Identifier id = offer.getIdentifier();
                if (id != null) {
                    offeredId = id.getValue();
                }
            }
            // Read STR element in csrElement if any
            strType= ((com.sun.xml.ws.rm.v200502.CreateSequenceElement)csrElement).getSecurityTokenReference();
            this.secureReliableMessaging = strType!=null?true:false;
        }   else {
             com.sun.xml.ws.rm.v200702.OfferType offer = ((com.sun.xml.ws.rm.v200702.CreateSequenceElement)csrElement).getOffer();
            if (offer != null) {
                com.sun.xml.ws.rm.v200702.Identifier id = offer.getIdentifier();
                if (id != null) {
                    offeredId = id.getValue();
                }
            }
            // Read STR element in csrElement if any
            strType= ((com.sun.xml.ws.rm.v200702.CreateSequenceElement)csrElement).getSecurityTokenReference();
            this.secureReliableMessaging = strType!=null?true:false;

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
         if (this.secureReliableMessaging) {

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
                        throw new RMSecurityException(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.SECURITY_TOKEN_AUTHORIZATION_ERROR.format(gotId ,strId ));
                    }
                } else throw new RMSecurityException(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.SECURITY_REFERENCE_ERROR.format( ref.getClass().getName()));

            } else throw new RMSecurityException(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.NULL_SECURITY_TOKEN.format());
        }

        startSession(inboundSequence);

        if (offeredId == null) {
            inboundSequence.getOutboundSequence().saveMessages = false;
        }

        //initialize CreateSequenceResponseElement
        AbstractAcceptType accept = null;
        AbstractCreateSequenceResponse crsElement = null;
        if (config.getRMVersion() == RMVersion.WSRM10)    {
            crsElement = new com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement();


            com.sun.xml.ws.rm.v200502.Identifier id2 = new com.sun.xml.ws.rm.v200502.Identifier();
            id2.setValue(inboundSequence.getId());
            ((com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement)crsElement).setIdentifier(id2);
            accept = new com.sun.xml.ws.rm.v200502.AcceptType();
        } else {
            crsElement = new com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement();


            com.sun.xml.ws.rm.v200702.Identifier id2 = new com.sun.xml.ws.rm.v200702.Identifier();
            id2.setValue(inboundSequence.getId());
            ((com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement)crsElement).setIdentifier(id2);
            accept = new com.sun.xml.ws.rm.v200702.AcceptType();

        }

        URI dest;
        if (offeredId != null) {


            String destString = message.getHeaders()
                                        .getTo(constants.getAddressingVersion(),
                                               config.getSoapVersion());
            try {
                dest = new URI(destString);
            } catch (Exception e) {
                logger.severe(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.INVALID_OR_MISSING_TO_ON_CS_MESSAGE.format()) ;
                throw new RMException(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.INVALID_OR_MISSING_TO_ON_CS_MESSAGE.format()
                        );
            }
            


            W3CEndpointReference endpointReference ;
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

        Message response = com.sun.xml.ws.api.message.Messages.create(config.getRMVersion().getJAXBContext(),
                            crsElement,
                            config.getSoapVersion());

        message.assertOneWay(false);

        /*ADDRESSING_FIXME
         * This will probably be broken with MS client if they still send CS with
         * missing reply-to.
         */

        Packet ret = packet.createServerResponse(response, constants.getAddressingVersion(),
                                                config.getSoapVersion(),
                                                config.getRMVersion().getCreateSequenceResponseAction());
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

        AbstractTerminateSequence tsElement ;
        Message message = packet.getMessage();

        try {
            tsElement = message.readPayloadAsJAXB(unmarshaller);
        } catch (JAXBException e) {
            logger.severe(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.TERMINATE_SEQUENCE_EXCEPTION.format() + e);
            throw new TerminateSequenceException(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.TERMINATE_SEQUENCE_EXCEPTION.format() + e);
        }
        String id ;
        if (tsElement instanceof com.sun.xml.ws.rm.v200502.TerminateSequenceElement){
           id = ((com.sun.xml.ws.rm.v200502.TerminateSequenceElement)tsElement).getIdentifier().getValue();
        }   else {
           id = ((com.sun.xml.ws.rm.v200702.TerminateSequenceElement)tsElement).getIdentifier().getValue(); 
        }


        ServerInboundSequence seq = provider.getInboundSequence(id);
        if (seq == null) {
            logger.severe(String.format(Constants.UNKNOWN_SEQUENCE_TEXT + id));
            throw new InvalidSequenceException(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,id),id);
        }


        //end the session if we own its lifetime..i.e. SC is not
        //present
        endSession(seq);

        provider.terminateSequence(id);

        //formulate response if required
        Packet ret = null;
        OutboundSequence outboundSequence = seq.getOutboundSequence();

        Message response = null;
        String tsAction = null;
        //If there is an "real" outbound sequence, client expects us to terminate it.
        switch (config.getRMVersion()) {
            case WSRM10: {

                tsAction =  RMVersion.WSRM10.getTerminateSequenceAction();
                if (outboundSequence.saveMessages) {
                    TerminateSequenceElement terminateSeqResponse = new TerminateSequenceElement();
                    Identifier id2 = new Identifier();
                    id2.setValue(outboundSequence.getId());

                    terminateSeqResponse.setIdentifier(id2);
                    response = com.sun.xml.ws.api.message.Messages.create(config.getRMVersion().getJAXBContext(),
                            terminateSeqResponse,
                            config.getSoapVersion());
                    ret = packet.createServerResponse(response,
                            constants.getAddressingVersion(),
                            config.getSoapVersion(), tsAction);

                    AbstractSequenceAcknowledgement element = seq.generateSequenceAcknowledgement(null, marshaller,false);

                    Header header = createHeader(element);
                    response.getHeaders().add(header);

                }else {

                    packet.transportBackChannel.close();
                    ret = new Packet(null);
                }
                break;
            }
            case WSRM11:{
                tsAction =  RMVersion.WSRM11.getTerminateSequenceResponseAction();
                TerminateSequenceResponseElement terminateSeqResponse = new TerminateSequenceResponseElement();
                com.sun.xml.ws.rm.v200702.Identifier id2 = new com.sun.xml.ws.rm.v200702.Identifier();
                id2.setValue(outboundSequence.getId());

                terminateSeqResponse.setIdentifier(id2);
                response = com.sun.xml.ws.api.message.Messages.create(config.getRMVersion().getJAXBContext(),
                        terminateSeqResponse,
                        config.getSoapVersion());
                response.assertOneWay(false);
                ret = packet.createServerResponse(response,
                        constants.getAddressingVersion(),
                        config.getSoapVersion(), tsAction);

                AbstractSequenceAcknowledgement element = seq.generateSequenceAcknowledgement(null, marshaller,false);

                Header header = createHeader(element);
                response.getHeaders().add(header);
                break;
            }

    }





        return ret;

    }

    public Packet handleLastMessageAction(Packet inbound) throws RMException {

        try {
            Message message = inbound.getMessage();
            Header header = message.getHeaders().get(config.getRMVersion().getSequenceQName(), true);
            if (header == null) {
                logger.severe(Messages.INVALID_LAST_MESSAGE.format());
                throw new RMException(Messages.INVALID_LAST_MESSAGE.format());
            }

            SequenceElement el = (SequenceElement)header.readAsJAXB(unmarshaller);
            String id = el.getId();

            ServerInboundSequence seq = provider.getInboundSequence(id);
            if (seq == null) {
                logger.severe(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,id +id));
                throw new InvalidSequenceException(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,id),id);
            }

            //add message to ClientInboundSequence so that this message
            //number appears in sequence acknowledgement
            int messageNumber = el.getNumber();
            seq.set(messageNumber, new com.sun.xml.ws.rm.Message(message, version));

            return generateAckMessage(inbound, seq, config.getRMVersion().getLastAction());

        } catch (JAXBException e) {
            logger.severe(Messages.LAST_MESSAGE_EXCEPTION.format() +e);
            throw new RMException(Messages.LAST_MESSAGE_EXCEPTION.format() +e);
        }
    }

    public Packet handleCloseSequenceAction(Packet inbound) throws RMException {


        CloseSequenceElement csElement;

        String id = null;
        Message message = inbound.getMessage();


        try {
            csElement = message.readPayloadAsJAXB(unmarshaller);
        } catch (JAXBException e) {

            throw new RMException(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.CLOSESEQUENCE_HEADER_PROBLEM.format() + e);
        }
        id = csElement.getIdentifier().getValue();
        ServerInboundSequence seq = provider.getInboundSequence(id);
        if (seq == null) {
            logger.severe(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,id +id));
            throw new InvalidSequenceException(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,id),id);
        }


        int lastMessageNumber = csElement.getLastMsgNumber();



        CloseSequenceResponseElement csrElement = new com.sun.xml.ws.rm.v200702.CloseSequenceResponseElement();
        com.sun.xml.ws.rm.v200702.Identifier identifier = new com.sun.xml.ws.rm.v200702.Identifier();
        identifier.setValue(seq.getId());
        csrElement.setIdentifier(identifier);

         Message response = com.sun.xml.ws.api.message.Messages.create(config.getRMVersion().getJAXBContext(),
                            csrElement,
                            config.getSoapVersion());

        message.assertOneWay(false);

        Packet returnPacket = inbound.createServerResponse(response,
                    constants.getAddressingVersion(),
                    config.getSoapVersion(), config.getRMVersion().getCloseSequenceResponseAction());

        //Generate SequenceAcknowledgmenet with Final element
        AbstractSequenceAcknowledgement element = seq.generateSequenceAcknowledgement(null, marshaller,true);
        Header header = createHeader(element);
        response.getHeaders().add(header);

      
        return returnPacket;

    }


    public Packet handleAckRequestedAction(Packet inbound) throws RMException {

        try {

            Message message = inbound.getMessage();
            Header header = message.getHeaders().get(config.getRMVersion().getAckRequestedQName(), true);
            if (header == null) {
                logger.severe(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.INVALID_ACK_REQUESTED.format());
                throw new RMException(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.INVALID_ACK_REQUESTED.format());
            }

            AbstractAckRequested el = (AbstractAckRequested)header
                    .readAsJAXB(unmarshaller);
            String id = el.getId();

            ServerInboundSequence seq = provider.getInboundSequence(id);

            if (seq == null) {
                logger.severe(Constants.UNKNOWN_SEQUENCE_TEXT + id);
                throw new InvalidSequenceException(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,id),id);
            }
            seq.resetLastActivityTime();

            return generateAckMessage(inbound, seq, 
                    config.getRMVersion().getSequenceAcknowledgementAction());

        } catch (JAXBException e) {
            logger.severe(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.ACK_REQUESTED_EXCEPTION.format());
            throw new RMException(com.sun.xml.ws.rm.jaxws.runtime.server.Messages.ACK_REQUESTED_EXCEPTION.format() +e);
        }

    }

    /**
     * Handles a raw SequenceAcknowledgement
     */
    public Packet handleSequenceAcknowledgementAction(Packet inbound) throws RMException {
        try {

            Message message = inbound.getMessage();
            Header header = message.getHeaders().get(config.getRMVersion().getSequenceAcknowledgementQName(),false);
            if (header == null) {
                logger.severe(Messages.INVALID_SEQ_ACKNOWLEDGEMENT.format());
                throw new RMException(Messages.INVALID_SEQ_ACKNOWLEDGEMENT.format());
            }

            AbstractSequenceAcknowledgement el = (AbstractSequenceAcknowledgement)header
                    .readAsJAXB(unmarshaller);

            String id;
            if (el instanceof SequenceAcknowledgementElement) {
                id = ((SequenceAcknowledgementElement)el).getId(); 
            }   else {
                id = ((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement)el).getId();
            }

            ServerInboundSequence seq = provider.getInboundSequence(id);

            //reset inactivity timer
            seq.resetLastActivityTime();
            handleInboundMessage(inbound);

            inbound.transportBackChannel.close();
            Packet ret = new Packet(null);
            ret.invocationProperties.putAll(inbound.invocationProperties);
            return ret;


        } catch (JAXBException e) {
            logger.severe(Messages.SEQ_ACKNOWLEDGEMENT_EXCEPTION.format());
            throw new RMException(Messages.SEQ_ACKNOWLEDGEMENT_EXCEPTION.format() +e);
        }
    }
    
    
    public Packet handleMakeConnectionAction(Packet packet) throws RMException {
        
        if (version == RMVersion.WSRM10) {
            logger.severe("Unsupported MakeConnection message to WS-RM 1.0 Endpoint.");
            throw new RMException("Unsupported MakeConnection message to WS-RM 1.0 Endpoint.");
        }
        
        MakeConnectionElement element = null;
        String sequenceId = null;
        Message message = packet.getMessage();


        try {
            element = message.readPayloadAsJAXB(unmarshaller);
        } catch (JAXBException e) {
            logger.severe("Invalid MakeConnection message.");
            throw new RMException("Invalid MakeConnection message.");
        }
        
        sequenceId = element.getIdentifier().getValue();
        OutboundSequence outboundSequence = provider.getOutboundSequence(sequenceId);
        
        if (outboundSequence == null) {
            logger.severe("Invalid sequence id " + sequenceId + " in MakeConnection message.");
            throw new RMException("Invalid sequence id " + sequenceId + " in MakeConnection message.");
        }
        
        //see if we can find a message in the sequence that needs to be resent.
        com.sun.xml.ws.rm.Message mess = outboundSequence.getUnacknowledgedMessage();
        Message jaxwsMessage = null;
        if (mess != null) {
            jaxwsMessage = mess.getCopy();
        } else {
            jaxwsMessage = com.sun.xml.ws.api.message.Messages.createEmpty(config.getSoapVersion());
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
        public Packet process(RMServerTube tube, Packet packet)
        throws RMException ;
    }

    private  void initActionMap(){
        actionMap.put(config.getRMVersion().getCreateSequenceAction(),
                new ActionHandler() {
            public Packet process(RMServerTube tube, Packet packet)
            throws RMException   {
                return tube.handleCreateSequenceAction(packet);
            }
        });

        actionMap.put(config.getRMVersion().getTerminateSequenceAction(),
                new ActionHandler() {
            public Packet process(RMServerTube tube, Packet packet)
            throws RMException  {
                return tube.handleTerminateSequenceAction(packet);
            }
        });

        actionMap.put(config.getRMVersion().getAckRequestedAction(),
                new ActionHandler() {
            public Packet process(RMServerTube tube, Packet packet)
            throws RMException  {
                return tube.handleAckRequestedAction(packet);
            }
        });

        actionMap.put(config.getRMVersion().getLastAction(),
                new ActionHandler() {
            public Packet process(RMServerTube tube, Packet packet)
            throws RMException {
                return tube.handleLastMessageAction(packet);
            }
        });

        actionMap.put(RMVersion.WSRM11.getCloseSequenceAction(),
                new ActionHandler() {
            public Packet process(RMServerTube tube, Packet packet)
            throws RMException {
                return tube.handleCloseSequenceAction(packet);
            }
        });

        

        actionMap.put(config.getRMVersion().getSequenceAcknowledgementAction(),
                new ActionHandler() {
            public Packet process(RMServerTube tube, Packet packet)
            throws RMException {
                return tube.handleSequenceAcknowledgementAction(packet);
            }
        });
        
        actionMap.put(config.getRMVersion().getMakeConnectionAction(),
                new ActionHandler() {
            public Packet process(RMServerTube tube, Packet packet)
            throws RMException {
                return tube.handleMakeConnectionAction(packet);
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
    private SequenceConfig getSequenceConfig() {

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
            logger.severe(Messages.SECURITY_TOKEN_MISMATCH.format());
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
     * @param isFinal Required when the version is RM 1.1 the SequenceAcknowledgement element should have a 
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
        AbstractSequenceAcknowledgement element = seq.generateSequenceAcknowledgement(null, marshaller,false);
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
        QName subcode = config.getRMVersion().getMessageNumberRolloverQname();
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
        QName subcode = config.getRMVersion().getUnknownSequenceQname();
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

    private SOAPFault newClosedSequenceFault(CloseSequenceException e) throws RMException {
            QName subcode = config.getRMVersion().getClosedSequenceQname();
            String faultstring = String.format(Constants.SEQUENCE_CLOSED_TEXT,e.getSequenceId());

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
        QName subcode = config.getRMVersion().getSequenceTerminatedQname();
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
        QName subcode = config.getRMVersion().getCreateSequenceRefusedQname();
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
        return Headers.create(config.getRMVersion().getJAXBRIContextHeaders(), obj);
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
