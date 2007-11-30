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
package com.sun.xml.ws.rm.jaxws.runtime.client;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.MessageSender;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.RMMessage;
import com.sun.xml.ws.rm.jaxws.runtime.TubeBase;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import com.sun.xml.ws.rm.localization.LocalizationMessages;

import com.sun.xml.ws.rm.localization.RmLogger;
import javax.xml.bind.JAXBElement;
import javax.xml.soap.SOAPException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Client-side Pipe implementation.
 */
public final class RMClientTube extends TubeBase {

    private static final RmLogger LOGGER = RmLogger.getLogger(RMClientTube.class);
    private static final String CREATE_SEQUENCE_URI = "http://com.sun/createSequence";
    /*
     * Metadata from ctor.
     */
    private SecureConversationInitiator securityPipe;
    /**
     * RM OutboundSequence handled by this Pipe.
     */
    private ClientOutboundSequence outboundSequence;
    /**
     * Flag to indicate if security pipe is our next pipe
     * then we need to create CreateSequenceRequest with
     * STR
     */
    private boolean secureReliableMessaging;
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
    private boolean isOneWayMessage = false;
    /* TubelineHelper to assist in resending of messages */
    private TubelineHelper tubelineHelper;

    /**
     * Constructor accepts all possible arguments available in
     * <code>PipelineAssembler.createClient</code>.  It may not need all of them.
     * TODO It also needs a way to access the Security Pipe.
     */
    public RMClientTube(WSDLPort wsdlPort, WSBinding binding, SecureConversationInitiator securityPipe, Tube nextTube) {
        super(wsdlPort, binding, nextTube);

        this.securityPipe = securityPipe;
        if (securityPipe != null) {
            this.secureReliableMessaging = true;
        } else {
            this.secureReliableMessaging = false;
        }
    }

    /**
     * Copy constructor used by <code>copy</code> method.
     *
     * @param toCopy to be copied.
     * @param cloner passed as an argument to copy.
     */
    private RMClientTube(RMClientTube toCopy, TubeCloner cloner) {
        super(toCopy, cloner);

        this.securityPipe = toCopy.securityPipe;
        this.secureReliableMessaging = toCopy.secureReliableMessaging;
        this.outboundSequence = toCopy.outboundSequence;
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
     *      from WSDLPort if it is missing
     */
    // TODO remove suppress warnings annotation
    @SuppressWarnings("unchecked")
    private synchronized void initialize(Packet packet) throws RMException {
        String dest = packet.endpointAddress.toString();
        if (outboundSequence != null) {
            //sequence has already been initialized. We need to
            //make sure that application programmer has not changed
            //the destination for requests by changing the value of
            //the BindingProvider ENDPOINT_ADDRESS_PROPERTY.  This is
            //allowable from the JAX-WS POV, but breaks the RM assumption
            //that sequences exactly correspond to connections between
            //single client instances and endpoints.
            if (dest != null && !dest.equals("") && !dest.equals(outboundSequence.getDestination().toString())) {
                //WSRM2017: The Endpoint Address cannot be changed by a client of an RM-enabled endpoint//
                throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_2017_UNCHANGEABLE_ENDPOINT_ADDRESS()));
            }
        } else {
            if (getConfig().getAddressingVersion() == AddressingVersion.MEMBER) {
                //WSRM2008: The Reliable Messaging Client does not support the Member submission addressing version, which is used by the endpoint.//
                throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_2008_UNSUPPORTED_ADDRESSING_VERSION()));
            }
            //store this in field
            this.proxy = packet.proxy;

            //make sure we have a destination
            if (dest == null) {
                dest = getWsdlPort().getAddress().toString();
            }

            URI destURI;
            try {
                destURI = new URI(dest);
            } catch (URISyntaxException e) {
                throw LOGGER.logSevereException(new RMException(LocalizationMessages.WSRM_2018_INVALID_DEST_URI(dest), e));
            }

            // try to get outbound sequence from the request context
            outboundSequence = (ClientOutboundSequence) packet.proxy.getRequestContext().get(Constants.sequenceProperty);
            if (outboundSequence == null) {
                //we need to connect to the back end.
                JAXBElement<SecurityTokenReferenceType> str = null;
                if (secureReliableMessaging) {
                    try {
                        str = securityPipe.startSecureConversation(packet);
                    } catch (Exception e) {
                        // TODO: L10N (+ handle exception + chatch only the particular subclass of Exception?)
                        LOGGER.severe("Starting secure conversation failed", e);
                    }
                    if (str == null) {
                        // Without this or if there was exception, no security configuration that does not include SC is allowed.
                        secureReliableMessaging = false;
                    }
                }

                outboundSequence = new ClientOutboundSequence(
                        getConfig(),
                        str,
                        destURI,
                        getConfig().getAnonymousAddressingUri(),
                        checkForTwoWayOperation(),
                        new ProtocolMessageSender(
                        RMSource.getRMSource().getInboundMessageProcessor(),
                        getConfig(),
                        getUnmarshaller(),
                        next,
                        packet));

                RMSource.getRMSource().addOutboundSequence(outboundSequence);
                //make this available to the client
                //FIXME - Can this work?
                packet.proxy.getRequestContext().put(Constants.sequenceProperty, outboundSequence);

                //set a Session object in BindingProvider property allowing user to close the sequence
                // TODO do we need this? remove?
                proxy.getRequestContext().put(ClientSession.SESSION_PROPERTY_KEY, new ClientSession(outboundSequence.getId(), this));
            }
        }
    }

    /**
     * Look in the WSDLPort and determine whether it contains any two-way operations.
     */
    private boolean checkForTwoWayOperation() {
        WSDLBoundPortType portType;
        if (getWsdlPort() == null || null == (portType = getWsdlPort().getBinding())) {
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

    private RMMessage prepareRequestMessage(Packet requestPacket) throws RMException {
        RMMessage message = null;

        //FIXME - Need a better way for client to pass a message number.
        Object mn = requestPacket.proxy.getRequestContext().get(Constants.messageNumberProperty);
        if (mn != null) {
            requestPacket.invocationProperties.put(Constants.messageNumberProperty, mn);
        }

        //Add to OutboundSequence and include RM headers according to the
        //state of the RMSource
        if (!requestPacket.getMessage().isOneWay(getWsdlPort())) {
            //ClientOutboundSequence needs to know this.  If this flag is true,
            //messages stored in the sequence cannot be discarded when they are acked.
            //They may need to be resent to provide a vehicle or resends of lost responses.
            //Instead, they are discarded when ClientOutboundSequence.acknowledgeResponse
            //is called by the in RMClientPipe.process when a response is received.

            //The behavior of the retry loop also varies according to whether the message
            //is one-way.  If it is, the retry loop needs wait for acks.  If not, the loop
            //can exit if an application response has been received.
            message = handleOutboundMessage(outboundSequence, requestPacket, true, false);
            this.isOneWayMessage = false;
        } else {
            //TODO eliminate one of these flags
            message = handleOutboundMessage(outboundSequence, requestPacket, false, false);
            this.isOneWayMessage = true;
        }

        //RM would always want expectReply to the true.  We need to look at
        //protocol responses for all messages, since they might contain RM
        //headers
        requestPacket.expectReply = true;

        //initialize TubelineHelper
        tubelineHelper = new TubelineHelper(requestPacket, message);

        //Make the helper available in the message, so it can be used to resend the message, if necessary
        message.setMessageSender(tubelineHelper);
        return message;
    }

    /*
     * Set state of message to "complete" when its processing results in an
     * unrecoverable failure.
     */
    private void completeFaultedMessage(RMMessage message) {
        try {
            outboundSequence.acknowledge(message.getMessageNumber());
        } catch (RMException e) {
            //TODO L10N
            LOGGER.warning("Error acknowledging message [" + message.getMessageNumber() + "] in sequence [" + message.getSequence().getId() + "]", e);
        }
    }

    /* Tube methods */
    /**
     * Send a Last message and a TerminateSequence message down the pipeline.
     */
    @Override
    public synchronized void preDestroy() {
        try {
            RMSource.getRMSource().terminateSequence(outboundSequence);
            next.preDestroy();
        } catch (Exception e) {
            //Faulted TerminateSequence message of bug downstream.  We are
            //done with the sequence anyway.  Log and go about our business
            //WSRM2007: RMClientPipe threw Exception in preDestroy//
            LOGGER.warning(LocalizationMessages.WSRM_2007_UNEXPECTED_PREDESTROY_EXCEPTION(), e);
        }
    }

    public RMClientTube copy(TubeCloner cloner) {
        //Need to prevent copying during the time-consuming process
        //of connecting to the endpoint and creating a sequence.  Conveniently, this
        //takes place in the <code>initialize</code> method, which needs to be
        //synchronized anyway.  Therefore it works to use the synchronized block here.

        //TODO - This race condition probably cannot ocurr any more.  If not, remove
        //the synchronization
        synchronized (this) {
            return new RMClientTube(this, cloner);
        }
    }

    @Override
    @NotNull
    public NextAction processRequest(Packet request) {
        RMMessage rmMessage = null;
        try {
            if (tubelineHelper == null) {
                //First time through here

                //prepare the state of the tube with initialize, etc
                initialize(request);

                //If the request is being sent by RMSource.createSequence, we are done.
                if (CREATE_SEQUENCE_URI.equals(request.getMessage().getPayloadNamespaceURI())) {
                    request.proxy.getRequestContext().put(Constants.sequenceProperty, outboundSequence);
                    //TODO..return something reasonable that will not cause disp.invoke
                    //to throw an exception here.  Other than that, we don't care about the
                    //response message.  We are only interested in the sequence that has been
                    //stored in the requestcontext.
                    Message mess = Messages.createEmpty(getConfig().getSoapVersion());
                    request.setMessage(mess);
                    doReturnWith(request);
                }
            }

            rmMessage = prepareRequestMessage(request);

            if (RMSource.getRMSource().getProcessingFilter() == null ||
                    RMSource.getRMSource().getProcessingFilter().handleClientRequestMessage(rmMessage)) {
                //reset last activity timer in sequence.
                outboundSequence.resetLastActivityTime();
                tubelineHelper.send();
            }

            return doSuspend();
        } catch (RMException e) {
            Message faultMessage = e.getFaultMessage();
            if (faultMessage != null) {
                try {
                    Packet ret = new Packet(Messages.create(faultMessage.readAsSOAPMessage()));
                    ret.invocationProperties.putAll(request.invocationProperties);

                    return doReturnWith(ret);
                } catch (SOAPException e1) {
                    // TODO handle exception
                    return doThrow(new WebServiceException(e));
                }
            } else {
                return processException(e);
            }
        } catch (Throwable ee) {
            //WSRM2006: Unexpected  Exception in RMClientPipe.process.
            LOGGER.severe(LocalizationMessages.WSRM_2006_UNEXPECTED_PROCESS_EXCEPTION(), ee);
            return processException(new WebServiceException(ee));
        }
    }

    /**
     * Use the default implementation of processReponse.  This will be invoked
     * by CompletionCallback in TubelineHelper.
     */
    @NotNull
    @Override
    public NextAction processResponse(Packet response) {
        if (response != null) {
            return doReturnWith(response);
        } else if (tubelineHelper != null) {
            return doThrow(tubelineHelper.throwable);
        } else {
            // TODO L10N
            throw LOGGER.logSevereException(new IllegalStateException("'null' Packet in processResponse()"));
        }
    }

    /**
     * Use default implementation of processException.  It will be:
     *
     * 1. invoked by CompletionCallback.onCompletion(Throwable) in TubelineHelper 
     * in the event that the Exception is on that needs to be returned to application
     * 2. Exceptions caught in initialize() and prepareRequest()
     */
    @NotNull
    @Override
    public NextAction processException(Throwable t) {
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
        private Packet packet;
        private RMMessage message;
        private Throwable throwable;

        public TubelineHelper(Packet packet, RMMessage message) {
            this.message = message;
            this.packet = packet;

            parentFiber = Fiber.current();
            if (parentFiber == null) {
                // TODO L10N
                throw LOGGER.logSevereException(new IllegalStateException("No current fiber."));
            }

            Engine engine = parentFiber.owner;

            fiber = engine.createFiber();
            callback = new TubelineHelperCallback();
            throwable = null;
        }

        public void send() {
            message.setIsBusy(true);
            if (packet == null) {
                // TODO L10N
                throw LOGGER.logSevereException(new IllegalStateException("Request not set in TubelineHelper"));
            }

            //use a copy of the original message
            Message copy = message.getCopy();
            packet.setMessage(copy);
            fiber.start(TubeCloner.clone(next), packet, callback);
        }

        private class TubelineHelperCallback implements Fiber.CompletionCallback {

            TubelineHelperCallback() {
            }

            public void onCompletion(
                    @NotNull Packet response) {
                try {
                    if (response != null) {
                        //Perform operations in the RMSource according to the contents of
                        //the RM Headers on the incoming message.
                        Message responseMessage = response.getMessage();

                        RMMessage rmMessage = null;
                        if (responseMessage != null) {
                            rmMessage = handleInboundMessage(response, RMSource.getRMSource());
                        }

                        //if a diagnostic / debugging filter has been set, allow it to inspect
                        //the response message.
                        if (RMSource.getRMSource().getProcessingFilter() != null) {
                            RMSource.getRMSource().getProcessingFilter().handleClientResponseMessage(rmMessage);
                        }

                        if (responseMessage != null && responseMessage.isFault()) {
                            //don't want to resend
                            //WSRM2004: Marking faulted message {0} as acked.
                            LOGGER.fine(LocalizationMessages.WSRM_2004_ACKING_FAULTED_MESSAGE(message.getMessageNumber()));
                            outboundSequence.acknowledge(message.getMessageNumber());
                        }

                        //check for empty body response to two-way message.  WCF will return
                        //one when it drops the request message.  In this case we also need to retry.
                        if (responseMessage != null && !isOneWayMessage && responseMessage.getPayloadNamespaceURI() == null) {
                            //resend
                            //WSRM2005: Queuing dropped message for resend.
                            LOGGER.fine(LocalizationMessages.WSRM_2005_RESENDING_DROPPED_MESSAGE());
                            return;
                        }

                        //If a response to a two-way operation has been received, it is
                        //time to release the request being retained on the OutboundSequence.
                        //This will also result in the state of the message being set to
                        //"complete" so the retry loop will exit.
                        if (message.isTwoWayRequest()) {
                            outboundSequence.acknowledgeResponse(message.getMessageNumber());
                        }
                    }

                    //invoke the rest of the pipeline
                    parentFiber.resume(response);
                } catch (Exception e) {
                    onCompletion(e);
                } finally {
                    message.setIsBusy(false);
                }
            }

            public void onCompletion(
                    @NotNull Throwable t) {
                throwable = t;
                try {
                    if (t instanceof ClientTransportException) {
                        //resend in this case
                        //WSRM2000: Sending message caused {0}. Queuing for resend.
                        LOGGER.fine(LocalizationMessages.WSRM_2000_QUEUE_FOR_RESEND(t.getMessage()), t);
                        return;
                    } else if (t instanceof WebServiceException) {
                        //Unwrap exception and see if it makes sense to retry this
                        //request.
                        Throwable cause = t.getCause();
                        if (cause != null && (cause instanceof IOException || cause instanceof SocketTimeoutException)) {
                            //Sending message caused {0}. Queuing for resend.//
                            //WSRM2000: Sending message caused {0}. Queuing for resend.//
                            LOGGER.fine(LocalizationMessages.WSRM_2000_QUEUE_FOR_RESEND(t.getMessage()), t);
                            //Simply return. Maintenance thread will invoke send() until
                            //we get a normal
                            return;
                        } else {
                            //non-transport-related Exception;
                            //WSRM2003: Unexpected exception  wrapped in WSException.//
                            LOGGER.severe(LocalizationMessages.WSRM_2003_UNEXPECTED_WRAPPED_EXCEPTION(), t);
                            completeFaultedMessage(message);
                            //TODO - need to propogate exception back to client here 
                            parentFiber.resume(null);
                        }
                    } else {
                        //Bug in software somewhere..  Any RuntimeException here must be a 
                        //WebServiceException
                        // WSRM2001: Unexpected exception in trySend.//
                        LOGGER.severe(LocalizationMessages.WSRM_2001_UNEXPECTED_TRY_SEND_EXCEPTION(), t);
                        //TODO - need to propogate exception back to client 
                        parentFiber.resume(null);
                    }
                } finally {
                    message.setIsBusy(false);
                }
            }
        }
    }
}
