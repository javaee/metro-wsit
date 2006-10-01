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
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.runtime.InboundMessageProcessor;
import com.sun.xml.ws.rm.jaxws.runtime.PipeBase;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.security.impl.bindings.SecurityTokenReferenceType;
import com.sun.xml.wss.jaxws.impl.SecurityClientPipe;
import com.sun.xml.ws.client.ClientTransportException;

import javax.xml.bind.JAXBElement;
import javax.xml.soap.SOAPException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.EndpointReference;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Client-side Pipe implementation.
 */
public class RMClientPipe
        extends PipeBase<RMSource,
                         ClientOutboundSequence,
                         ClientInboundSequence>{

    /*
     * Metadata from ctor.
     */
    private WSDLPort port;
    private WSService service;
    private WSBinding binding;
    private SecurityClientPipe securityPipe;

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
     * Pool of pipes to be used for invoking the tail of the
     * Pipeline.
     */
    private final ProcessorPool<RMClientPipe> processorPool;
    
   
    /**
     * Constructor accepts all possible arguments available in
     * <code>PipelineAssembler.createClient</code>.  It may not need all of them.
     * TODO It also needs a way to access the Security Pipe.
     */
    public RMClientPipe(WSDLPort port,
                      WSService service,
                      WSBinding binding,
                      SecurityClientPipe securityPipe,
                      Pipe nextPipe) {

        super(RMSource.getRMSource(), nextPipe);

        this.port = port;
        this.service = service;
        this.binding = binding;
        this.securityPipe = securityPipe;
        
        this.config = new SequenceConfig(port);
        config.setSoapVersion(binding.getSOAPVersion());
        
        this.messageProcessor = this.provider.getInboundMessageProcessor();
        this.processorPool = new ProcessorPool<RMClientPipe>(this);

        if (securityPipe != null) {
            this.secureReliableMessaging = true;
        }else {
            this.secureReliableMessaging = false;
        }
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
            securityPipe = cloner.copy(toCopy.securityPipe);
            this.secureReliableMessaging = true;
        } else {
            securityPipe = null;
            this.secureReliableMessaging = false;
        }

        port = toCopy.port;
        service = toCopy.service;
        binding = toCopy.binding;
        processorPool = toCopy.processorPool;


        config = toCopy.config;
        messageProcessor = this.provider.getInboundMessageProcessor();

         //these are be threadsafe
        this.outboundSequence = toCopy.outboundSequence;
        this.inboundSequence = toCopy.inboundSequence;

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
                outboundSequence.getDestination()
                    .getAddress().getURI().toString() != dest) {
                        throw new RMException(Messages.UNCHANGEABLE_ENDPOINT_ADDRESS.format());
            }
            
        } else {
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
            EndpointReference destEpr;
            EndpointReference acksToEpr;
            AddressingBuilder addressingBuilder = AddressingBuilder.newInstance();

            try {
                destURI = new URI(dest);
                destEpr = addressingBuilder.newEndpointReference(destURI);
            } catch (URISyntaxException e) {
                throw new RMException(Messages.INVALID_DEST_URI.format( dest));
            }

            try {
                acksToURI = new URI(acksTo);
                acksToEpr = addressingBuilder.newEndpointReference(acksToURI);
            } catch (URISyntaxException e) {
                throw new RMException(Messages.INVALID_ACKS_TO_URI.format( acksTo));
            }

            //BUGBUG?? - We may need to pass a new marshaller and unmarshaller and a clone of nextPipe
            //here if it turns out that it is possible that protocol messages are being sent at the
            //same time as application messages.
            outboundSequence = new ClientOutboundSequence(config);

            if (secureReliableMessaging) {
                try {
                    JAXBElement<SecurityTokenReferenceType> str = securityPipe.startSecureConversation(packet);
                    outboundSequence.setSecurityTokenReference(str);
                } catch (Exception e) {
                    secureReliableMessaging = false;
                    outboundSequence.setSecurityTokenReference(null);
                }
            }

            //store this in field
            this.proxy = packet.proxy;

            outboundSequence.setSecureReliableMessaging(secureReliableMessaging);

            outboundSequence.registerProtocolMessageSender(
                    new ProtocolMessageSender(messageProcessor,marshaller,unmarshaller,nextPipe, packet));


            outboundSequence.connect(destEpr,  acksToEpr, twoWay);

            inboundSequence = (ClientInboundSequence)outboundSequence.getInboundSequence();


            //set a Session object in BindingProvider property allowing user to close
            //the sequence
            ClientSession.setSession(this.proxy, new ClientSession(outboundSequence.getId(), this));

            provider.addOutboundSequence(outboundSequence);
            provider.addInboundSequence(inboundSequence);
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
    private Packet trySend(Packet packet, com.sun.xml.ws.rm.Message message)
            {
       
        try {
            
            //RM would always want expectReply to the true.  We need to look at
            //protocol responses for all messages, since they might contain RM
            //headers
            packet.expectReply = true;
            
            //use a copy of the original message
            com.sun.xml.ws.api.message.Message copy = message.getCopy();
            packet.setMessage(copy);
            
            //HACK - Do this now.  Any marshallings of the message need to be
            //done on this Thread.  Tbis will at least keep JAXBMessage.sniff
            //from doing marshallings.
            copy.isOneWay(port);

            
            //We are sending one-way requests in the background.  The
            //tail of the Pipeline is non-reentrant.  We are using a pool
            //of copies of nextPipe here.
            return nextPipe.process(packet);
        } catch (ClientTransportException ee) {
            //resend in this case
            return null;
        } catch (WebServiceException e) {
            //Unwrap exception and see if it makes sense to retry this
            //request.
            Throwable cause = e.getCause();
            if (cause != null &&
                    (cause instanceof IOException ||
                     cause instanceof SocketTimeoutException)) {

                //cause the retry loop in the process method to resend
                return null;

            } else {
                throw e;
            }
        }

    }
   
    
    public Packet doRetryLoop(Packet packet, com.sun.xml.ws.rm.Message message)
                        throws RMException  {

        while (!message.isComplete()) {

            Packet ret = null;

            //give debug/diagnostic filter access to the message and allow it
            //to simulate dropped message
            if (filter == null || filter.handleClientRequestMessage(message)) {
                //send down the pipe
                ret = trySend(packet, message);

                //reset last activity timer in sequence.
                outboundSequence.resetLastActivityTime();

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

                    //check for empty body response to two-way message.  Indigo will return
                    //one when it drops the request message.  In this case we also need to retry.
                    //
                    // Alternative things to check:
                    //
                    //Perhaps check for wsa:Action == AckRequested instead?
                    //Perhaps check whether message has an SequenceAcknowledgement
                    //     not containing the id for the request?

                    if (mess != null && !packet.getMessage().isOneWay(port) &&
                        mess.getPayloadNamespaceURI() == null) {
                        //resend
                        ret = null;
                    }

                    //If a response to a two-way operation has been received, it is
                    //time to release the request being retained on the OutboundSequence.
                    //This will also result in the state of the message being set to
                    //"complete" so the retry loop will exit.
                    if (message.isTwoWayRequest) {
                        outboundSequence.acknowledgeResponse(message.getMessageNumber());
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
                }
           } else {
                return ret;
           }

        }  //while

        //Only a one-way message can reach here.  That will happen if it has to wait at least
        //once for an ack.  The return value is irrelevant here since the sending of the one-way message
        //is being done in the background.
        return null;
    }

    /*
     * PIPE INTERFACE METHODS.
     */
    public Packet process(Packet packet) {
 
        com.sun.xml.ws.rm.Message message = null;
        RMClientPipe poolPipe = null;
        
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
            
            //Copy of this pipe used for processing this request.  Will initialize
            //it with processorPool.checkOut() when it is needed.  Will check it back
            //in to the pool in the finally handler.
            poolPipe = processorPool.checkOut();

            //Add to OutboundSequence and include RM headers according to the
            //state of the RMSource
            message = poolPipe.handleOutboundMessage(outboundSequence,
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
 
            if (message.isTwoWayRequest) {
                return poolPipe.doRetryLoop(packet, message);
            } else {
                //For a one-way message, the application thread does not need to wait.  It may
                //be some time if messages are lost, or if the RMD has to withhold the request 
                //waiting for earlier ones to arrive. The spec requires the runtime to wait for
                //the protocol response, and it will -- just not this thread.
                
                final Packet p = packet;
                final com.sun.xml.ws.rm.Message m = message; 
                final RMClientPipe pp = poolPipe;
                
                Thread t = new Thread() {
                    public void run() {
                        try {
                           pp.doRetryLoop(p,m);
                        } catch (RMException e) {
                           throw new WebServiceException(e);
                        } finally {
                            //this needs to wait for poolPipe.doRetryLoop to return.
                            //otherwise, poolPipe might end up being used concurrently
                            //by more than one one-way message.
                            if (pp != null) {
                                processorPool.checkIn(pp);
                            }
                        }
                    }
                };

                t.start();
                //client is not expecting a response here.
                Packet ret = new Packet(com.sun.xml.ws.api.message.Messages.createEmpty(binding.getSOAPVersion()));
                ret.invocationProperties.putAll(packet.invocationProperties);
                return ret;

            }
         } catch (RMException e) {
            Message faultMessage = e.getFaultMessage();
            if (faultMessage != null){
                try {
                    Packet ret = new Packet(com.sun.xml.ws.api.message.Messages.create(faultMessage.readAsSOAPMessage()));
                    ret.invocationProperties.putAll(packet.invocationProperties);
                    return ret;
                } catch (SOAPException e1) {
                    e1.printStackTrace();
                    throw new WebServiceException(e);
                }
            } else {
                e.printStackTrace();
                throw new WebServiceException(e);
            }
        } finally {
             if (message != null && 
                 message.isTwoWayRequest &&
                 poolPipe != null ){
                 processorPool.checkIn(poolPipe);
             }
         }
    }

    /**
     * Send a Last message and a TerminateSequence message down the pipeline.
     */
    public synchronized void preDestroy() {
        try {  
            provider.terminateSequence(outboundSequence);       
            nextPipe.preDestroy();
        } catch (RMException e) {
            //Is this right?
            throw new WebServiceException(e);
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
