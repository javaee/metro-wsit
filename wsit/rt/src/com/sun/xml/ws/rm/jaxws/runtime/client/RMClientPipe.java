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
 * RMClientPipe.java
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
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.runtime.InboundMessageProcessor;
import com.sun.xml.ws.rm.jaxws.runtime.PipeBase;
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
public class RMClientPipe
        extends PipeBase<RMSource,
        ClientOutboundSequence,
        ClientInboundSequence>{
    
    public static final Logger logger =
            Logger.getLogger(
                LoggingHelper.getLoggerName(RMClientPipe.class));
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
    
    private Boolean isOneWayMessage = false;
    
    
    /**
     * Constructor accepts all possible arguments available in
     * <code>PipelineAssembler.createClient</code>.  It may not need all of them.
     * TODO It also needs a way to access the Security Pipe.
     */
    public RMClientPipe(WSDLPort port,
            WSService service,
            WSBinding binding,
            SecureConversationInitiator securityPipe,
            Pipe nextPipe) {
        
        super(RMSource.getRMSource(), nextPipe);
        
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
        this.unmarshaller = config.getRMVersion().createUnmarshaller();
        this.marshaller = config.getRMVersion().createMarshaller();
        
    }
    
    /**
     * Copy constructor used by <code>copy</code> method.
     *
     * @param toCopy to be copied.
     * @param cloner passed as an argument to copy.
     */
    private RMClientPipe( RMClientPipe toCopy, PipeCloner cloner) {
        
        super(RMSource.getRMSource(), null);
        cloner.add(toCopy, this);
        
        nextPipe = cloner.copy(toCopy.nextPipe);
        
        if (securityPipe != null) {
            
            securityPipe = toCopy.securityPipe;
            this.secureReliableMessaging = true;
        } else {
            securityPipe = null;
            this.secureReliableMessaging = false;
        }
        
        port = toCopy.port;
        service = toCopy.service;
        binding = toCopy.binding;
       
        
        
        config = toCopy.config;
        messageProcessor = this.provider.getInboundMessageProcessor();
        
        //these are be threadsafe
        this.outboundSequence = toCopy.outboundSequence;
        this.inboundSequence = toCopy.inboundSequence;
        this.unmarshaller = config.getRMVersion().createUnmarshaller();
        this.marshaller = config.getRMVersion().createMarshaller();
        // RMConstants.setAddressingVersion(binding.getAddressingVersion());
        
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
                        marshaller,
                        unmarshaller,
                        port, binding,
                        nextPipe, packet));
                
                
                outboundSequence.connect(destURI,  acksToURI, twoWay);
                
                inboundSequence = (ClientInboundSequence)outboundSequence.getInboundSequence();
                
                
                //set a Session object in BindingProvider property allowing user to close
                //the sequence
                ClientSession.setSession(this.proxy, new ClientSession(outboundSequence.getId(), this));
                
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
     * Attempts to send a request message by calling <code>process(nextPipe)</code>.
     * There are several possible outcomes:
     * <ul>
     *   <li>
     *      <b>The attempt succeeds</b>
     *      <p>The return value from <code>process(nextPipe)</code> is returned.
     *   </li>
     *   <li>
     *      <b>The attempt fails due to a network error that may succeed in a later attempt</b>
     *      <p>A <code>RetriableException</code> is thrown.
     *   </li>
     *   <li>
     *      <b>The attempt fails due to a condition for which a fault is defined in
     *          the WS-RM spec.</b>
     *      <p>The resulting RMException is thrown and caught in the process(Packet)
     *         method, where the appropropriate Fault message is constructed and returned..
     *   </li>
     *   <li>
     *      <b>Some other error occurrs</b>
     *      <p>A <code>WebServiceException</code> with appropriate error message is thrown.
     *   </li>
     * </ul>
     *
     * @param   packet The packet containing "input" message (which may be a copy of the
     * @param  message originally occupying the packet).
     *      message The RM message wrapping the Message originally passed to
     *          process
     * @return packet
     *      A packet containing the "output" message.  If a failure occurs that might
     *      succeed in a retry, <code>null</code> is returne.
     *
     */
    private Packet trySend(Packet packet, com.sun.xml.ws.rm.Message message) {
        
        try {
            
            //RM would always want expectReply to the true.  We need to look at
            //protocol responses for all messages, since they might contain RM
            //headers
            packet.expectReply = true;
            
            //use a copy of the original message
            com.sun.xml.ws.api.message.Message copy = message.getCopy();
            packet.setMessage(copy);
           
            //We are sending one-way requests in the background.  The
            //tail of the Pipeline is non-reentrant.  We are using a pool
            //of copies of nextPipe here.
            return nextPipe.process(packet);
            
        } catch (ClientTransportException ee) {
            //resend in this case
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, 
                        //WSRM2000: Sending message caused {0}. Queuing for resend.//
                        Messages.QUEUE_FOR_RESEND.format(ee.toString()),
                        ee);
            }
            return null;
            
        } catch (WebServiceException e) {
            //Unwrap exception and see if it makes sense to retry this
            //request.
            Throwable cause = e.getCause();
            if (cause != null &&
                    (cause instanceof IOException ||
                    cause instanceof SocketTimeoutException)) {
                if (logger.isLoggable(Level.FINE)) {
                    //Sending message caused {0}. Queuing for resend.//
                    logger.log(Level.FINE, 
                                //WSRM2000: Sending message caused {0}. Queuing for resend.//
                                Messages.QUEUE_FOR_RESEND.format(e.toString()),
                                e);
                }
                //cause the retry loop in the process method to resend
                return null;
                
            } else {
               //non-transport-related Exception;
                logger.log(Level.SEVERE,
                         //WSRM2003: Unexpected exception  wrapped in WSException.//
                           Messages.UNEXPECTED_WRAPPED_EXCEPTION.format(), e);
                
                throw e;
            }
        } catch (Exception e) {
            //Bug in software somewhere..  Any RuntimeException here must be a 
            //WebServiceException
            logger.log(Level.SEVERE, 
                       //  WSRM2001: Unexpected exception in trySend.//
                       Messages.UNEXPECTED_TRY_SEND_EXCEPTION.format(), e);
            throw new WebServiceException(e);
           
        }
        
    }
    
    
    public Packet doRetryLoop(Packet packet, com.sun.xml.ws.rm.Message message)
        throws RMException  {
        
        try {
            while (!message.isComplete()) {

                Packet ret = null;

                //give debug/diagnostic filter access to the message and allow it
                //to simulate dropped message
                
                //BUGBUG - It is possible for filter to be uninitialized here or have the wrong
                //value.  The initialization should be done here rather than in the RMClientPipe
                //ctor, and there is no reason for it to be a field (at least in the client pipe)
                filter = this.provider.getProcessingFilter();
                
                if (filter == null || filter.handleClientRequestMessage(message)) {
                    
                    //reset last activity timer in sequence.
                    outboundSequence.resetLastActivityTime();

                    //Store if it a oneway message
                    this.isOneWayMessage = packet.getMessage().isOneWay(port);

                    //send down the pipe
                    ret = trySend(packet, message);
                   
                    if (ret != null) {
                        //Perform operations in the RMSource according to the contents of
                        //the RM Headers on the incoming message.
                        Message mess = ret.getMessage();
                        com.sun.xml.ws.rm.Message rmMessage = null;

                        if (mess != null) {
                            rmMessage = handleInboundMessage(ret);
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

                        //check for empty body response to two-way message.  Indigo will return
                        //one when it drops the request message.  In this case we also need to retry.
                        //
                        // Alternative things to check:
                        //
                        //Perhaps check for wsa:Action == AckRequested instead?
                        //Perhaps check whether message has an SequenceAcknowledgement
                        //     not containing the id for the request?

                        if (mess != null && !this.isOneWayMessage &&
                                mess.getPayloadNamespaceURI() == null) {
                            //resend
                            logger.log(Level.FINE, 
                                    //WSRM2005: Queuing dropped message for resend.
                                    Messages.RESENDING_DROPPED_MESSAGE
                                        .format());
                          
                            ret = null;
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
                }

                //if the original call to trySend for a two-way message resulted in a retriable
                //failure, wait here until awakakened by the RMSource's maintenance Thread that
                //will eventually notice that the request has not been acked.  The condition
                //(ret == null) determines that retriable failure has happende.
                //
                //for a one-way message, we need to wait for an ack, indicated by (message.isComplete).
                //This will be the case when an ack has been received.

                if (ret == null || !message.isComplete()) {
                    message.block();
                    if (message.isComplete()) {
                        return ret;
                    } else {
                        //make sure message now has an AckRequested header
                        //so there will be an AckRequested on every resend
                        outboundSequence.ensureAckRequested(message,
                                marshaller);
                    }
                } else {
                    return ret;
                }

            }  //while

            //Only a one-way message can reach here.  That will happen if it has to wait at least
            //once for an ack.  The return value is irrelevant here since the sending of the one-way message
            //is being done in the background.
            return null;
            
        } catch (RuntimeException e) {
            //There will not be any more opportunities to resend the message, so we may as
            //well fill the gap in the sequence so the maintenance thread can ignore it.
            //This will be logged in process()
            
            //FIXME - Refactor.. This is being called twice in most cases
            if (message != null) {
                 outboundSequence.acknowledge(message.getMessageNumber());
                 if (message.isTwoWayRequest) {
                            outboundSequence.acknowledgeResponse(
                                    message.getMessageNumber());
                 }
            }          
            throw e;
        }
    }
    
    /*
     * PIPE INTERFACE METHODS.
     */
    public Packet process(Packet packet) {
        com.sun.xml.ws.rm.Message message = null;
        
        try {
            //Initialize the RM Sequence if this is the first request through the Pipe.
            //We might also need to reinitialize if destination URI is different from
            //the last one used, but the runtime should probably deal with this, since
            //in this case a SC session will also need to reinitialize.
            //
            //TODO Figure out how to initialize at the time the Pipe is initialized.  Doing it
            //lazily means that the ClientSession will not  be available to the client
            //before the first request is processed.
            initialize(packet);
            
            
            //If the request is being sent by RMSource.createSequence, we are done.
            Object seq = packet.invocationProperties.get(Constants.createSequenceProperty);
            if (seq != null) {
                packet.invocationProperties.put(Constants.createSequenceProperty, null);
                packet.proxy.getRequestContext().put(Constants.sequenceProperty, seq);
                //TODO..return something reasonable that will not cause disp.invoke
                //to throw an exception here.  Other than that, we don't care about the
                //response message.  We are only interested in the sequence that has been
                //stored in the requestcontext.
                com.sun.xml.ws.api.message.Message mess =
                        com.sun.xml.ws.api.message.Messages
                        .createEmpty(binding.getSOAPVersion());
                packet.setMessage(mess);
                return packet;
            }
            
            //FIXME - Need a better way for client to pass a message number.
            Object mn = packet.proxy.getRequestContext().get(Constants.messageNumberProperty);
            if (mn != null) {
                packet.invocationProperties.put(Constants.messageNumberProperty, mn);
            }
            
            //Add to OutboundSequence and include RM headers according to the
            //state of the RMSource
            message = handleOutboundMessage(outboundSequence,
                    packet);
            
            if (!packet.getMessage().isOneWay(port)) {
                //ClientOutboundSequence needs to know this.  If this flag is true,
                //messages stored in the sequence cannot be discarded when they are acked.
                //They may need to be resent to provide a vehicle or resends of lost responses.
                //Instead, they are discarded when ClientOutboundSequence.acknowledgeResponse
                //is called by the in RMClientPipe.process when a response is received.
                
                //The behavior of the retry loop also varies according to whether the message
                //is one-way.  If it is, the retry loop needs wait for acks.  If not, the loop
                //can exit if an application response has been received.
                message.isTwoWayRequest = true;
            }
            
            return doRetryLoop(packet, message);
            
        } catch (RMException e) {
            Message faultMessage = e.getFaultMessage();
            if (faultMessage != null){
                try {
                    Packet ret = new Packet(com.sun.xml.ws.api.message.Messages.create(faultMessage.readAsSOAPMessage()));
                    ret.invocationProperties.putAll(packet.invocationProperties);
                    return ret;
                } catch (SOAPException e1) {
                    throw new WebServiceException(e);
                }
            } else {
                throw new WebServiceException(e);
            }
        } catch (Throwable ee) {
            logger.log(Level.SEVERE,
                    //WSRM2006: Unexpected  Exception in RMClientPipe.process.
                    Messages.UNEXPECTED_PROCESS_EXCEPTION.format(),                  
                    ee);
            throw new WebServiceException(ee);
            
        } 
    }
    
    /**
     * Send a Last message and a TerminateSequence message down the pipeline.
     */
    public synchronized void preDestroy() {
        try {
            provider.terminateSequence(outboundSequence);
            nextPipe.preDestroy();
        } catch (Exception e) {
            //Faulted TerminateSequence message of bug downstream.  We are
            //done with the sequence anyway.  Log and go about our business
            logger.log(Level.FINE,
                       //WSRM2007: RMClientPipe threw Exception in preDestroy//
                       Messages.UNEXPECTED_PREDESTROY_EXCEPTION.format(), 
                       e);
        }
    }
    
    /**
     * Create a copy, reusing thread-safe fields and cloning or recreating non-threadsafe ones.
     */
    public  Pipe copy(PipeCloner cloner) {
        //Need to prevent copying during the time-consuming process
        //of connecting to the endpoint and creating a sequence.  Conveniently, this
        //takes place in the <code>initialize</code> method, which needs to be
        //synchronized anyway.  Therefore it works to use the synchronized block here.
        synchronized(this) {
            
            return new RMClientPipe(this, cloner);
        }
    }
    
    /*
     * PRIVATE HELPERS
     */
    
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
}
