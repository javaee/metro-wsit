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
 * RMClientTube.java
 *
 * @author Mike Grogan
 * @author Bhakti Mehta
 * Created on February 4, 2006, 2:58 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime.client;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.istack.NotNull;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.MessageSender;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.runtime.InboundMessageProcessor;
import com.sun.xml.ws.rm.jaxws.runtime.TubeBase;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.jaxws.util.LoggingHelper;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;

import javax.xml.bind.JAXBElement;
import javax.xml.soap.SOAPException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Client-side Pipe implementation.
 */
public class RMClientTube
        extends TubeBase<RMSource,
                ClientOutboundSequence,
                ClientInboundSequence>{
    
    public static final Logger logger =
            Logger.getLogger(
                LoggingHelper.getLoggerName(RMClientTube.class));
    public static final LoggingHelper logHelper = 
            new LoggingHelper(logger);
    
    
    /*
     * Metadata from ctor.
     */
    private WSDLPort port;
    private WSService service;
    private WSBinding binding;
    private SecureConversationInitiator securityPipe;
    
    /*
     * SequenceConfig from policy
     */
    private SequenceConfig config;
    
    /**
     * RM OutboundSequence handled by this Pipe.
     */
    private ClientOutboundSequence outboundSequence;
    
    /**
     * RM InboundSequence handled by this Pipe.
     */
    private ClientInboundSequence inboundSequence;
    
    /**
     * Message processor to handle inbound messages
     */
    private InboundMessageProcessor messageProcessor;
    
    /**
     * Flag to indicate if security pipe is our next pipe
     * then we need to create CreateSequenceRequest with
     * STR
     */
    private  boolean secureReliableMessaging ;
    
    /**
     * The BindingProvider instance using the OutboundSequence
     * serviced by this pipe.
     */
    private BindingProvider proxy;
    
    /**
     * State of a specific request/response.  The Tubeline handle one such
     * exchange at a time, so it is safe to store these if they are not copied
     * by the clone operation.
     */
    
   
    /*Store the MEP of the current exchange*/
    private Boolean isOneWayMessage = false;
    
    /* TubelineHelper to assist in resending of messages */
    private TubelineHelper tubelineHelper;
    
    
    /**
     * Constructor accepts all possible arguments available in
     * <code>PipelineAssembler.createClient</code>.  It may not need all of them.
     * TODO It also needs a way to access the Security Pipe.
     */
    public RMClientTube(WSDLPort port,
            WSService service,
            WSBinding binding,
            SecureConversationInitiator securityPipe,
            Tube nextTube) {
        
        super(RMSource.getRMSource(), nextTube);
        
        this.port = port;
        this.service = service;
        this.binding = binding;
        this.securityPipe = securityPipe;
        
        this.config = new SequenceConfig(port,binding);
        config.setSoapVersion(binding.getSOAPVersion());
        
        this.messageProcessor = this.provider.getInboundMessageProcessor();        
        
        if (securityPipe != null) {
            this.secureReliableMessaging = true;
        }else {
            this.secureReliableMessaging = false;
        }
        
       
        this.version = config.getRMVersion();
        this.unmarshaller = version.createUnmarshaller();
        this.marshaller = version.createMarshaller();
        
    }
    
    /**
     * Copy constructor used by <code>copy</code> method.
     *
     * @param toCopy to be copied.
     * @param cloner passed as an argument to copy.
     */
    private RMClientTube( RMClientTube toCopy, TubeCloner cloner) {
        
        super(RMSource.getRMSource(), toCopy, cloner);
        
        if (securityPipe != null) {
            
            securityPipe = toCopy.securityPipe;
            this.secureReliableMessaging = true;
        } else {
            securityPipe = null;
            this.secureReliableMessaging = false;
        }
        
        this.port = toCopy.port;
        this.service = toCopy.service;
        this.binding = toCopy.binding;
         
        this.config = toCopy.config;
        this.version = toCopy.version;
        this.messageProcessor = this.provider.getInboundMessageProcessor();
        
        this.outboundSequence = toCopy.outboundSequence;
        this.inboundSequence = toCopy.inboundSequence;
        this.unmarshaller = config.getRMVersion().createUnmarshaller();
        this.marshaller = config.getRMVersion().createMarshaller();
        
        
    }
    
    
    /**
     * Perform lazy initialization when the first message is processed. Need to:
     * <ul>
     * <li>Initialize a SequenceConfig using the metadata parameters passed in the ctor</li>
     * <li>Initialize outboundSequence and inboundSequence using the SequenceConfig</li>
     * </ul>
     *
     * @param packet
     *      Destination EndpointAddress URI from the request context.  Default to EndpointAddress
     *          from WSDLPort if it is missing
     */
    @SuppressWarnings("unchecked")
    private synchronized void initialize(Packet packet) throws RMException {
        
        String dest = packet.endpointAddress.toString();
        
        if (outboundSequence != null) {
            
            //sequence has already been initialized.  We need to
            //make sure that application programmer has not changed
            //the destination for requests by changing the value of
            //the BindingProvider ENDPOINT_ADDRESS_PROPERTY.  This is
            //allowable from the JAX-WS POV, but breaks the RM assumption
            //that sequences exactly correspond to connections between
            //single client instances and endpoints.
            
            if (dest != null && !dest.equals("") &&
                    outboundSequence.getDestination().toString() != dest) {
                //WSRM2017: The Endpoint Address cannot be changed by a client of an RM-enabled endpoint//
                throw new RMException(Messages.UNCHANGEABLE_ENDPOINT_ADDRESS.format());
            }
            
        } else {
            
            if (binding.getAddressingVersion() == AddressingVersion.MEMBER) {
         
                //WSRM2008: The Reliable Messaging Client does not support the Member submission addressing version, which is used by the endpoint.//
                throw new RMException(Messages.UNSUPPORTED_ADDRESSING_VERSION.format());
            }
            //store this in field
            this.proxy = packet.proxy;
            
            //make sure we have a destination
            if (dest == null) {
                dest = port.getAddress().toString();
            }
            
            String acksTo = ProtocolMessageReceiver.getAcksTo();
            
            //use helper function to speilunk the metadata and find out if the port
            //has a two-way operation.
            boolean twoWay = checkForTwoWayOperation();
            
            URI destURI;
            URI acksToURI;
            
            try {
                destURI = new URI(dest);
            } catch (URISyntaxException e) {
                //Invalid destination URI   {0}//
                throw new RMException(Messages.INVALID_DEST_URI.format( dest));
            }
            
            try {
                acksToURI = new URI(acksTo);
            } catch (URISyntaxException e) {              
                //Invalid acksTo URI   {0}//
                throw new RMException(Messages.INVALID_ACKS_TO_URI.format( acksTo));
            }
            
            ClientOutboundSequence specifiedOutboundSequence =
                    (ClientOutboundSequence)packet.proxy.getRequestContext()
                    .get(Constants.sequenceProperty);
            if (specifiedOutboundSequence != null) {
                outboundSequence = specifiedOutboundSequence;
            } else {
                //we need to connect to the back end.
                outboundSequence = new ClientOutboundSequence(config);
                
                if (secureReliableMessaging) {
                    try {
                        JAXBElement<SecurityTokenReferenceType> str = 
                                 securityPipe.startSecureConversation(packet);
                        
                        outboundSequence.setSecurityTokenReference(str);
			if (str == null) {
				//Without this, no security configuration
				//that does not include SC is allowed.
				secureReliableMessaging = false;
			}
                    } catch (Exception e) {
                        secureReliableMessaging = false;
                        outboundSequence.setSecurityTokenReference(null);
                    }
                }
                
                outboundSequence.setSecureReliableMessaging(secureReliableMessaging);
                
                outboundSequence.registerProtocolMessageSender(
                        new ProtocolMessageSender(messageProcessor,
                                                    config,
                                                    marshaller,
                                                    unmarshaller,
                                                    port, binding,
                                                    next, packet));
                
                
                outboundSequence.connect(destURI,  acksToURI, twoWay);
                
                inboundSequence = (ClientInboundSequence)outboundSequence.getInboundSequence();
                
                
                //set a Session object in BindingProvider property allowing user to close
                //the sequence
                ClientSession.setSession(this.proxy, 
                                         new ClientSession(outboundSequence.getId(), this));
                
                provider.addOutboundSequence(outboundSequence);
                
                //if the message in the packet was sent by RMSource.createSequence,
                //put the sequence in a packet property.  The process method, that
                //called us will find it there and return it to the caller.
                String reqUri = packet.getMessage().getPayloadNamespaceURI();
                if (reqUri.equals(Constants.createSequenceNamespace)) {
                    packet.invocationProperties.put(Constants.createSequenceProperty,
                            outboundSequence);
                }
                
                //make this available to the client
                //FIXME - Can this work?
                packet.proxy.getRequestContext().put(Constants.sequenceProperty,
                        outboundSequence);
                
                
            }   
        }
    }
    
      
    /**
     * Look in the WSDLPort and determine whether it contains any two-way operations.
     */
    private boolean checkForTwoWayOperation() {
        
        WSDLBoundPortType portType;
        if (port == null || null == (portType = port.getBinding())) {
            
            //no WSDL perhaps? Returning false here means that will be no
            //reverse sequence.  That is the correct behavior.
            return false;
        }
        
        for (WSDLBoundOperation op : portType.getBindingOperations()) {
            WSDLOperation operation = op.getOperation();
            if (!operation.isOneWay()) {
                return true;
            }
        }
        
        //all operations are one-way
        return false;
    }
    
    
   
    private com.sun.xml.ws.rm.Message prepareRequestMessage(Packet request) 
        throws RMException {
       
        com.sun.xml.ws.rm.Message message = null;
                
        //FIXME - Need a better way for client to pass a message number.
        Object mn = request.proxy.getRequestContext().get(Constants.messageNumberProperty);
        if (mn != null) {
            request.invocationProperties.put(Constants.messageNumberProperty, mn);
        }

        //Add to OutboundSequence and include RM headers according to the
        //state of the RMSource
        message = handleOutboundMessage(outboundSequence,
                request);

        if (!request.getMessage().isOneWay(port)) {
            //ClientOutboundSequence needs to know this.  If this flag is true,
            //messages stored in the sequence cannot be discarded when they are acked.
            //They may need to be resent to provide a vehicle or resends of lost responses.
            //Instead, they are discarded when ClientOutboundSequence.acknowledgeResponse
            //is called by the in RMClientPipe.process when a response is received.

            //The behavior of the retry loop also varies according to whether the message
            //is one-way.  If it is, the retry loop needs wait for acks.  If not, the loop
            //can exit if an application response has been received.
            message.isTwoWayRequest = true;
            this.isOneWayMessage = false;
        } else {
            //TODO eliminate one of these flags
            message.isTwoWayRequest = false;
            this.isOneWayMessage = true;
        }
        
         
                
        //RM would always want expectReply to the true.  We need to look at
        //protocol responses for all messages, since they might contain RM
        //headers
        request.expectReply = true;
        
        //initialize TubelineHelper
        tubelineHelper = new TubelineHelper(request, message);
            
                
        //Make the helper available in the message, so it can be used to resend the message, if necessary
        message.setMessageSender(tubelineHelper);

        
        return message;
                          
    }
    
    /*
     * Set state of message to "complete" when its processing results in an
     * unrecoverable failure.
     */
    private void completeFaultedMessage(com.sun.xml.ws.rm.Message message )  {
        
        try {
            outboundSequence.acknowledge(message.getMessageNumber());
        } catch (RMException e) {
            //TODO log entry
        }
    }
    
    
    /* Tube methods */
    
   
    
      /**
     * Send a Last message and a TerminateSequence message down the pipeline.
     */
    public synchronized void preDestroy() {
        try {
            provider.terminateSequence(outboundSequence);
            next.preDestroy();
        } catch (Exception e) {
            //Faulted TerminateSequence message of bug downstream.  We are
            //done with the sequence anyway.  Log and go about our business
            logger.log(Level.FINE,
                       //WSRM2007: RMClientPipe threw Exception in preDestroy//
                       Messages.UNEXPECTED_PREDESTROY_EXCEPTION.format(), 
                       e);
        }
    }
    
    public  RMClientTube copy(TubeCloner cloner) {
        //Need to prevent copying during the time-consuming process
        //of connecting to the endpoint and creating a sequence.  Conveniently, this
        //takes place in the <code>initialize</code> method, which needs to be
        //synchronized anyway.  Therefore it works to use the synchronized block here.
        
        //TODO - This race condition probably cannot ocurr any more.  If not, remove
        //the synchronization
        synchronized(this) {  
            return new RMClientTube(this, cloner);
        }
    }
    
    
    public @NotNull NextAction processRequest(Packet request) {
         
        com.sun.xml.ws.rm.Message message = null;
        
         try {
             
             if (tubelineHelper == null) {
                 //First time through here
                 
                //prepare the state of the tube with initialize, etc
                initialize(request);

                 //If the request is being sent by RMSource.createSequence, we are done.
                Object seq = request.invocationProperties.get(Constants.createSequenceProperty);

                if (seq != null) {
                    request.invocationProperties.put(Constants.createSequenceProperty, null);
                    request.proxy.getRequestContext().put(Constants.sequenceProperty, seq);
                    //TODO..return something reasonable that will not cause disp.invoke
                    //to throw an exception here.  Other than that, we don't care about the
                    //response message.  We are only interested in the sequence that has been
                    //stored in the requestcontext.
                    com.sun.xml.ws.api.message.Message mess =
                            com.sun.xml.ws.api.message.Messages
                            .createEmpty(binding.getSOAPVersion());
                    request.setMessage(mess);
                    doReturnWith(request);
                }

                
             }
             
             message = prepareRequestMessage(request);
            
            //BUGBUG - It is possible for filter to be uninitialized here or have the wrong
            //value.  The initialization should be done here rather than in the RMClientPipe
            //ctor, and there is no reason for it to be a field (at least in the client pipe)
            filter = this.provider.getProcessingFilter();

            if (filter == null || filter.handleClientRequestMessage(message)) {

                //reset last activity timer in sequence.
                outboundSequence.resetLastActivityTime();
  
                tubelineHelper.send();
                
            } 
            
            return doSuspend();
       
         } catch (RMException e) {
             
             Message faultMessage = e.getFaultMessage();
             if (faultMessage != null){
                try {
                    Packet ret = new Packet(com.sun.xml.ws.api.message.Messages.create(faultMessage.readAsSOAPMessage()));
                    ret.invocationProperties.putAll(request.invocationProperties);
                    
                    return doReturnWith(ret);
                    
                } catch (SOAPException e1) {
                    
                    return doThrow(new WebServiceException(e));
                }
            } else {
      
                return processException(e);
            }
             
         } catch (Throwable ee) {
   
            logger.log(Level.SEVERE,
                    //WSRM2006: Unexpected  Exception in RMClientPipe.process.
                    Messages.UNEXPECTED_PROCESS_EXCEPTION.format(),                  
                    ee);
            return processException(new WebServiceException(ee));
         }
                 
       
    }

    /**
     * Use the default implementation of processReponse.  This will be invoked
     * by CompletionCallback in TubelineHelper.
     */
    public @NotNull NextAction processResponse(Packet response) {
       if (response != null) {
            return doReturnWith(response);
       } else if (tubelineHelper != null) {
            return doThrow(tubelineHelper.throwable);
       } else {
           throw new IllegalStateException("null Packet in response.");
       }
    }
    
    /**
     * Use default implementation of processException.  It will be:
     *
     * 1. invoked by CompletionCallback.onCompletion(Throwable) in TubelineHelper 
     * in the event that the Exception is on that needs to be returned to application
     * 2. Exceptions caught in initialize() and prepareRequest()
     */
    public @NotNull NextAction processException(Throwable t) {

       if (!(t instanceof WebServiceException)) {
  
           t = new WebServiceException(t);
       }

       return doThrow(t);
   }
    
    /* Inner classes */
    
    /**
     * ApplicationMessageHelper is used to execute a single request in the tail of the
     * Tubeline.  
     */
    public class TubelineHelper implements MessageSender {

        private final Fiber fiber;
        private final Fiber parentFiber;
        private final TubelineHelperCallback callback;
        
        //Store the request packet and message for this helper.
        private  Packet packet;
        private  com.sun.xml.ws.rm.Message message;

        public Throwable throwable;
        
        
        public TubelineHelper(Packet packet, com.sun.xml.ws.rm.Message message) {
            
            this.message = message;
            this.packet = packet;
           
            
            parentFiber = Fiber.current();
            if (parentFiber == null) {
                throw new IllegalStateException("No current fiber.");
            }

            Engine engine = parentFiber.owner;
            
            fiber = engine.createFiber();
            callback = new TubelineHelperCallback();
            throwable = null;
        };


        public void send() {
            
            
            message.setIsWaiting(true);
           
            if (packet == null) {
                throw new IllegalStateException("Request not set in TubelineHelper");
            }
            
           //TODO - Figure out whether it is necessary to copy
           //fiber and/or next here.
            //use a copy of the original message
           com.sun.xml.ws.api.message.Message copy = message.getCopy();
           packet.setMessage(copy);
           fiber.start(next, packet, callback);
           
        }

        private class TubelineHelperCallback implements Fiber.CompletionCallback {
      
            TubelineHelperCallback() {
            }
            

            public void onCompletion(@NotNull Packet response) {
                
                 try {
                     if (response != null) {
                         
                
                        //Perform operations in the RMSource according to the contents of
                        //the RM Headers on the incoming message.
                        Message mess = response.getMessage();
                        com.sun.xml.ws.rm.Message rmMessage = null;
 
                        if (mess != null) {
                            rmMessage = handleInboundMessage(response);
                        }
 
                        //if a diagnostic / debugging filter has been set, allow it to inspect
                        //the response message.
                        if (filter != null) {
                            filter.handleClientResponseMessage(rmMessage);
                        }
 
                        if (mess != null && mess.isFault()) {
                            //don't want to resend
                            logger.log(Level.FINE, 
                                    //WSRM2004: Marking faulted message {0} as acked.
                                    Messages.ACKING_FAULTED_MESSAGE
                                          .format(message.getMessageNumber()));
                            outboundSequence.acknowledge(message.getMessageNumber());
                        }
 
                        //check for empty body response to two-way message.  WCF will return
                        //one when it drops the request message.  In this case we also need to retry.

                        if (mess != null && !isOneWayMessage &&
                                mess.getPayloadNamespaceURI() == null) {
                            //resend
                            logger.log(Level.FINE, 
                                    //WSRM2005: Queuing dropped message for resend.
                                    Messages.RESENDING_DROPPED_MESSAGE
                                        .format());
                            return;
                        }

                        //If a response to a two-way operation has been received, it is
                        //time to release the request being retained on the OutboundSequence.
                        //This will also result in the state of the message being set to
                        //"complete" so the retry loop will exit.
                        if (message.isTwoWayRequest) {
                            outboundSequence.acknowledgeResponse(
                                    message.getMessageNumber());
                        }
 
                    }

                     //invoke the rest of the pipeline
                     parentFiber.resume(response);
              
                 } catch (Exception e) {
                     onCompletion(e);
                 } finally {
                     message.setIsWaiting(false);
                 }

            }

            public void onCompletion(@NotNull Throwable t) {
                
                throwable = t;
                
                try {
                    if (t instanceof WebServiceException) {

                        //Unwrap exception and see if it makes sense to retry this
                        //request.
                        Throwable cause = t.getCause();

                        if (cause != null &&
                                (cause instanceof IOException ||
                                cause instanceof SocketTimeoutException)) {
                            if (logger.isLoggable(Level.FINE)) {
                                //Sending message caused {0}. Queuing for resend.//
                                logger.log(Level.FINE, 
                                            //WSRM2000: Sending message caused {0}. Queuing for resend.//
                                            Messages.QUEUE_FOR_RESEND.format(t.toString()),
                                            t);
                            }

                            //Simply return. Maintenance thread will invoke send() until
                            //we get a normal
                            return;

                        } else {
                           //non-transport-related Exception;
                            logger.log(Level.SEVERE,
                                     //WSRM2003: Unexpected exception  wrapped in WSException.//
                                       Messages.UNEXPECTED_WRAPPED_EXCEPTION.format(), t);
                            completeFaultedMessage(message);
           
                            parentFiber.resume(null);
                        } 
                    } else  {
                        //Bug in software somewhere..  Any RuntimeException here must be a 
                        //WebServiceException
                        logger.log(Level.SEVERE, 
                                   //  WSRM2001: Unexpected exception in trySend.//
                                   Messages.UNEXPECTED_TRY_SEND_EXCEPTION.format(), t);
                       
                         parentFiber.resume(null);
                    }
                } finally {
                    message.setIsWaiting(false);
                }
            }     
        };
    }

    
}
