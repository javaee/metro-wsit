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
 * ProtocolMessageSender.java
 *
 * @author Mike Grogan
 * Created on January 30, 2006, 12:12 PM
 *
 */
package com.sun.xml.ws.rm.jaxws.runtime.client;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.client.ContentNegotiation;
import com.sun.xml.ws.rm.CloseSequenceException;
import com.sun.xml.ws.rm.CreateSequenceException;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.RMMessage;
import com.sun.xml.ws.rm.RMVersion;
import com.sun.xml.ws.rm.TerminateSequenceException;
import com.sun.xml.ws.rm.jaxws.runtime.InboundMessageProcessor;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.protocol.AbstractAckRequested;
import com.sun.xml.ws.rm.protocol.AbstractCreateSequence;
import com.sun.xml.ws.rm.protocol.AbstractCreateSequenceResponse;
import com.sun.xml.ws.rm.protocol.AbstractTerminateSequence;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.URI;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

/**
 * Helper class used to send protocol message addressed to the endpoint.
 * The messages belong to the following types:
 * <ul>
 * <li>CreateSequence. A message with a CreateSequence element in its body is sent.  The
 * response message contains a CreateSequenceResponse element in its body</li>
 * <li>Last. A message with empty body, and a Sequence header with Last child
 * is sent.  The headers on the body are processed.</li>
 * <li>AckRequensted. A message with empty body, and a Sequence header with Last child
 * is sent.  The headers on the body are processed.</li>
 * <li>TerminateSequence A message with a TerminateSequence element in its body is sent.</li>
 * </ul>
 *
 */
public class ProtocolMessageSender {

    private static final RmLogger LOGGER = RmLogger.getLogger(ProtocolMessageSender.class);
    /**
     * TODO javadoc
     */
    private SequenceConfig config;
    /**
     * TODO javadoc
     */
    private final Engine engine;
    private final Tube nextTube;
    /**
     * TODO javadoc
     */
    private BindingProvider proxy;
    private ContentNegotiation contentNegotiation;
    /**
     * The unmarshaller to read the messages
     */
    private Unmarshaller unmarshaller;

    /**
     * Public constructor.  Initialize the fields
     */
    public ProtocolMessageSender(
            SequenceConfig config,
            Unmarshaller unmarshaller,
            Tube nextTube,
            BindingProvider proxy,
            ContentNegotiation contentNegotiation) {

        this.unmarshaller = unmarshaller;
        this.config = config;        
        this.proxy = proxy;
        this.contentNegotiation = contentNegotiation;

        this.nextTube = nextTube;
        Fiber currentFiber = Fiber.current();
        if (currentFiber == null) {
            // TODO L10N
            throw LOGGER.logSevereException(new IllegalStateException("No current fiber found"));
        }
        this.engine = currentFiber.owner;
    }

    public AbstractCreateSequenceResponse sendCreateSequence(
            AbstractCreateSequence cs,
            URI destination,
            URI acksTo,
            boolean secureReliableMessaging) throws RMException {

        //Used from com.sun.xml.ws.jaxws.runtime.client.ClientOutboundSequence.connect, where
        //CreateSequence object is constructed and resulting CreateSequenceResponse object is
        //processed.
        AbstractCreateSequenceResponse csrElem = null;

        //1. Initialize  message adding CreateSequence to body
        if (cs != null) {
            Message request = Messages.create(config.getRMVersion().jaxbContext, cs, config.getSoapVersion());

            //Addressing Headers are added by configuring the following property
            //on the packet
            Packet requestPacket = new Packet(request);
            requestPacket.proxy = this.proxy;
            requestPacket.contentNegotiation = this.contentNegotiation;
            requestPacket.setEndPointAddressString(destination.toString());

            addAddressingHeaders(requestPacket, config.getRMVersion().createSequenceAction, destination, false);
            if (secureReliableMessaging) {
                addSecurityHeaders(requestPacket);
            }

            Packet responsePacket = process(requestPacket);
            if (acksTo.equals(config.getAnonymousAddressingUri())) {
                Message response = responsePacket.getMessage();
                //need the null check because this might be a non-anonymous ackto and
                //CSR will be processed on another connection.
                if (response != null) {
                    if (response.isFault()) {
                        // TODO L10N
                        throw LOGGER.logSevereException(new CreateSequenceException("CreateSequence was refused by the RMDestination \n ", response));
                    }

                    //unmarshall CreateSequenceResponse object from body of response.
                    csrElem = unmarshallCreateSequenceResponse(response);
                }
            } else {
                // TODO: L10N
                throw LOGGER.logSevereException(new RMException("Addressable endpoints are currently not supported"));
            }
        }
        return csrElem;
    }

    public void sendTerminateSequence(
            AbstractTerminateSequence ts,
            OutboundSequence seq) throws RMException {

        //Used from com.sun.xml.ws.jaxws.runtime.client.ClientOutboundSequence.disconnect, where the
        //TerminateSequence message is initialzied.
        Message request = Messages.create(config.getRMVersion().jaxbContext, ts, config.getSoapVersion());

        //piggyback an acknowledgement if one is pending
        seq.processAcknowledgement(new RMMessage(request));

        Packet requestPacket = new Packet(request);
        requestPacket.proxy = this.proxy;
        requestPacket.contentNegotiation = this.contentNegotiation;
        addAddressingHeaders(requestPacket, config.getRMVersion().terminateSequenceAction, seq.getDestination(),/*true*/ false);
        requestPacket.setEndPointAddressString(seq.getDestination().toString());
        Packet responsePacket = process(requestPacket);
        Message response = responsePacket.getMessage();
        if (response != null && response.isFault()) {
            throw LOGGER.logException(new TerminateSequenceException("There was an error trying to terminate the sequence ", response), Level.WARNING);
        }
    //TODO What to do with response?
    //It may have a TerminateSequence for reverse sequence on it as well as ack headers
    //Process these.
    }

    /**
     * Send Message with empty body and a single SequenceElement (with Last child) down the pipe.  Process the response,
     * which may contain a SequenceAcknowledgementElement.
     *
     * @param seq Outbound sequence to which SequenceHeaderElement will belong.
     *
     */
    public void sendLast(OutboundSequence seq) throws RMException {
        Message request = createEmptyMessage(config.getSoapVersion());
        com.sun.xml.ws.rm.v200502.SequenceElement el = createLastHeader(seq);
        request.getHeaders().add(createHeader(el));

        seq.setLast();

        Packet requestPacket = new Packet(request);
        requestPacket.proxy = this.proxy;
        requestPacket.setEndPointAddressString(seq.getDestination().toString());
        requestPacket.contentNegotiation = this.contentNegotiation;
        addAddressingHeaders(requestPacket, config.getRMVersion().lastAction, seq.getDestination(), /*true*/ false);

        Packet responsePacket = process(requestPacket);
        Message response = responsePacket.getMessage();

        RMMessage rmResponse = new RMMessage(response);
        if (response != null && response.isFault()) {
            // TODO L10N
            throw LOGGER.logException(new RMException("Error sending Last message", response), Level.WARNING);
        }

        InboundMessageProcessor.processMessage(rmResponse, unmarshaller, RMSource.getRMSource(), config.getRMVersion());
    }

    /**
     * Send Message with empty body and a AckRequestedElement (with Last child) down the pipe.  Process the response,
     * which may contain a SequenceAcknowledgementElement.
     *
     * @param seq Outbound sequence to which SequenceHeaderElement will belong.
     *
     */
    public void sendAckRequested(OutboundSequence seq, SOAPVersion version) throws RMException {
        try {
            Message request = createEmptyMessage(version);
            AbstractAckRequested el = createAckRequestedElement(seq);
            //request.getHeaders().add(Headers.create(version,marshaller,el));
            request.getHeaders().add(createHeader(el));


            Packet requestPacket = new Packet(request);
            requestPacket.proxy = this.proxy;
            requestPacket.contentNegotiation = this.contentNegotiation;

            addAddressingHeaders(requestPacket, config.getRMVersion().ackRequestedAction, seq.getDestination(), /*true*/ false);

            requestPacket.setEndPointAddressString(seq.getDestination().toString());

            Packet responsePacket = process(requestPacket);
            Message response = responsePacket.getMessage();
            if (response != null && response.isFault()) {
                //reset alarm
                ((ClientOutboundSequence) seq).resetLastActivityTime();
                // TODO L10N
                throw LOGGER.logException(new RMException("Error sending AckRequestedElement", response), Level.WARNING);
            }

            InboundMessageProcessor.processMessage(new RMMessage(response), unmarshaller, RMSource.getRMSource(), config.getRMVersion());
        } finally {
            //Make sure that alarm is reset.
            ((ClientOutboundSequence) seq).resetLastActivityTime();
        }
    }

    /**
     * Initialize an AddressingProperties object using the arguments.  Put the AddressingProperties
     * object in the RequestContext obtained from getMessageProperties.
     */
    private Packet addAddressingHeaders(
            Packet requestPacket,
            String action,
            URI destination,
            boolean oneWay) throws RMException {

        /*ADDRESSING FIX_ME
        Current API does not allow assignment of non-anon reply to, if we
        need to support non-anon acksTo.
         */
        if (oneWay) {
            requestPacket.getMessage().assertOneWay(true);
        } else {
            requestPacket.getMessage().assertOneWay(false);
        }
        //list.fillRequestAddressingHeaders(port, binding, requestPacket, action);
        requestPacket.setEndPointAddressString(destination.toString());
        requestPacket.getMessage().getHeaders().fillRequestAddressingHeaders(
                requestPacket,
                config.getAddressingVersion(),
                config.getSoapVersion(),
                oneWay,
                action);
        return requestPacket;
    }

    private void addSecurityHeaders(Packet requestPacket) {
        if (config.getRMVersion() == RMVersion.WSRM11) {
            HeaderList headerList = requestPacket.getMessage().getHeaders();

            com.sun.xml.ws.rm.v200702.UsesSequenceSTR usesSequenceSTR = new com.sun.xml.ws.rm.v200702.UsesSequenceSTR();
            usesSequenceSTR.getOtherAttributes().put(new QName(config.getSoapVersion().nsUri, "mustUnderstand"), "true");
            headerList.add(createHeader(usesSequenceSTR));
        }
    }

    /**
     * Create an empty message using correct SOAPVersion
     */
    private Message createEmptyMessage(SOAPVersion version) {
        return Messages.createEmpty(version);
    }

    private AbstractCreateSequenceResponse unmarshallCreateSequenceResponse(Message response) throws RMException {
        try {
            return response.readPayloadAsJAXB(unmarshaller);
        } catch (JAXBException e) {
            // TODO L10N
            throw LOGGER.logSevereException(new RMException("Unable to unmarshall CreateSequenceResponse", e));
        }
    }

    /**
     * Return a <code>SequenceElement.LastMessage</code>
     */
    private com.sun.xml.ws.rm.v200502.SequenceElement createLastHeader(OutboundSequence seq) {
        com.sun.xml.ws.rm.v200502.SequenceElement sequenceElement = new com.sun.xml.ws.rm.v200502.SequenceElement();
        sequenceElement.setId(seq.getId());
        sequenceElement.setNumber(seq.getNextIndex());
        sequenceElement.setLastMessage(new com.sun.xml.ws.rm.v200502.SequenceElement.LastMessage());
        return sequenceElement;
    }

    /**
     * Return a <code>AckReqesutedElement</code> whose Sequence ID matches the specified
     * <code>OutboundSequence</code> and whose MessageNumber is the
     * highest <MessageNumber> sent within a Sequence
     * sequence.
     */
    private AbstractAckRequested createAckRequestedElement(OutboundSequence seq) {
        AbstractAckRequested ackRequestedElement = null;
        if (config.getRMVersion() == RMVersion.WSRM10) {
            ackRequestedElement = new com.sun.xml.ws.rm.v200502.AckRequestedElement();
            ackRequestedElement.setId(seq.getId());
        } else {
            ackRequestedElement = new com.sun.xml.ws.rm.v200702.AckRequestedElement();
            ackRequestedElement.setId(seq.getId());
        }

        return ackRequestedElement;
    }

    private Header createHeader(Object obj) {
        return Headers.create(config.getRMVersion().jaxbContext, obj);
    }

    public void sendCloseSequence(OutboundSequence seq) throws RMException {

        com.sun.xml.ws.rm.v200702.Identifier idClose = new com.sun.xml.ws.rm.v200702.Identifier();
        idClose.setValue(seq.getId());

        com.sun.xml.ws.rm.v200702.CloseSequenceElement cs = new com.sun.xml.ws.rm.v200702.CloseSequenceElement();
        cs.setIdentifier(idClose);
        cs.setLastMsgNumber(seq.getNextIndex() - 1);

        Packet requestPacket = new Packet(Messages.create(config.getRMVersion().jaxbContext, cs, config.getSoapVersion()));
        requestPacket.proxy = this.proxy;
        requestPacket.contentNegotiation = this.contentNegotiation;
        requestPacket.setEndPointAddressString(seq.getDestination().toString());

        // TODO: check why only WS-RM1.1 ???
        addAddressingHeaders(requestPacket, RMVersion.WSRM11.closeSequenceAction, seq.getDestination(), false);

        seq.setClosed();

        Packet responsePacket = process(requestPacket);
        Message response = responsePacket.getMessage();
        if (response.isFault()) {
            // TODO L10N
            throw LOGGER.logException(new CloseSequenceException("CloseSequence was refused by the RMDestination", response), Level.WARNING);
        }
        unmarshallCloseSequenceResponse(response);
    }

    private com.sun.xml.ws.rm.v200702.CloseSequenceResponseElement unmarshallCloseSequenceResponse(Message response) throws RMException {
        try {
            return response.readPayloadAsJAXB(unmarshaller);
        } catch (JAXBException e) {
            // TODO L10N
            throw LOGGER.logSevereException(new RMException("Unable to unmarshall CloseSequenceResponse", e));
        }
    }

    /**
     * Synchronously executes the protocol exchange.  The implementation sends
     * the request through a clone of the stored Tubeline and blocks until a
     * response is received.
     */
    private Packet process(Packet request) throws RMException {
        //we will use a fresh Fiber and Tube for each request.  We need to do this
        //because there may be another request being procesed by the original tube.
        //This can happen when this ProtocolMessageHelper is being used to resend
        //messages or send AckRequested's from the maintenance thread.  These
        //things might happen while other requests are being processe.'
        Fiber fiber = engine.createFiber();
        Tube tubeline = TubeCloner.clone(nextTube);
        return fiber.runSync(tubeline, request);
    }
}
