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
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.rm.*;
import com.sun.xml.ws.rm.jaxws.runtime.InboundMessageProcessor;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.protocol.AbstractCreateSequence;
import com.sun.xml.ws.rm.protocol.AbstractCreateSequenceResponse;
import com.sun.xml.ws.rm.v200502.AckRequestedElement;
import com.sun.xml.ws.rm.v200502.SequenceElement;
import com.sun.xml.ws.rm.v200502.TerminateSequenceElement;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.net.URI;

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

    /**
     * Helper to process InboundMessages.
     */
    private InboundMessageProcessor processor;

    
    /**
     * The marshaller to write the messages
     */
    private  Marshaller marshaller;
    /**
     * The unmarshaller to read the messages
     */
    private  Unmarshaller unmarshaller;


    private RMConstants constants;

    /**
     * Properties like the BindingProvider to associate with the
     * request and response context
     * contentNegotiation etc can be obtained from
     * the packet
     */
    private Packet packet;

    /*
     * WSDLPort for use by addressing module when assigning headers.
     */
    private WSDLPort port;

    /*
     * WSBinding for use by addressing module when assigning headers.
     */
    private WSBinding binding;

    private SequenceConfig config;
    
    private final ProtocolMessageHelper helper;

    /**
     * Public ctor.  Initialize the fields
     */
    public ProtocolMessageSender(InboundMessageProcessor processor,
                                 SequenceConfig config,
                                 Marshaller marshaller,
                                 Unmarshaller unmarshaller,
                                 WSDLPort port,
                                 WSBinding binding,
                                 Tube nextTube,
                                 Packet packet) {

        this.processor = processor;
        this.port = port;
        this.binding = binding;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
        this.constants =  RMConstants.getRMConstants(binding.getAddressingVersion());
        this.packet = packet;
        this.config = config;
        this.helper = new ProtocolMessageHelper(nextTube);

    }

    public AbstractCreateSequenceResponse sendCreateSequence(AbstractCreateSequence cs,
                                                            URI destination,
                                                            URI acksTo,
                                                            SOAPVersion version) throws RMException {

        //Used from com.sun.xml.ws.jaxws.runtime.client.ClientOutboundSequence.connect, where
        //CreateSequence object is constructed and resulting CreateSequenceResponse object is
        //processed.
        AbstractCreateSequenceResponse csrElem = null;

        //1. Initialize  message adding CreateSequence to body
        if (cs != null) {
            Message request = null;
            if (config.getRMVersion() == RMVersion.WSRM10) {
                 request = Messages.create(config.getRMVersion().getJAXBContext(),((com.sun.xml.ws.rm.v200502.CreateSequenceElement)cs),version);
            } else {
               request = Messages.create(config.getRMVersion().getJAXBContext(),((com.sun.xml.ws.rm.v200702.CreateSequenceElement)cs),version);
            }



            //Addressing Headers are added by configuring the following property
            //on the packet

            Packet requestPacket = new Packet(request);
            requestPacket.proxy = packet.proxy;
            requestPacket.contentNegotiation = packet.contentNegotiation;
            requestPacket.setEndPointAddressString(destination.toString());

            addAddressingHeaders (requestPacket, config.getRMVersion().getCreateSequenceAction(),
                    destination , acksTo, false);

            String messageId = null ;/*= ADDRESSING_FIXME - initialize with mesageID
                                   assigned by addAddressingHeaders for use in
                                   correlating non-anonymous acksTo response*/



            Packet responsePacket = helper.process(requestPacket);

            if (acksTo.equals(constants.getAnonymousURI())) {

               Message response = responsePacket.getMessage();
               if (response.isFault()){
                    throw new CreateSequenceException("CreateSequence was refused by the RMDestination \n ",response);
                }

                //unmarshall CreateSequenceResponse object from body of response.
                //need the null check because this might be a non-anonymous ackto and
                //CSR will be processed on another connection.
                if (response != null) {
                    csrElem = unmarshallCreateSequenceResponse(response);
                }
            } else {

                csrElem = ProtocolMessageReceiver.getCreateSequenceResponse(messageId);
            }
        }

        return csrElem;
    }

    public void sendTerminateSequence(TerminateSequenceElement ts,
                                      OutboundSequence seq,
                                      SOAPVersion version) throws RMException {

        //Used from com.sun.xml.ws.jaxws.runtime.client.ClientOutboundSequence.disconnect, where the
        //TerminateSequence message is initialzied.

        Message request = Messages.create(config.getRMVersion().getJAXBContext(),ts,version);

        //piggyback an acknowledgement if one is pending
        seq.processAcknowledgement(new com.sun.xml.ws.rm.Message(request, config.rmVersion), marshaller);

        Packet requestPacket = new Packet(request);
        requestPacket.proxy = packet.proxy;
        requestPacket.contentNegotiation = packet.contentNegotiation;
        addAddressingHeaders (requestPacket,config.getRMVersion().getTerminateSequenceAction(),seq.getDestination(),seq.getAcksTo(),/*true*/ false);
        requestPacket.setEndPointAddressString(seq.getDestination().toString());
        Packet responsePacket = helper.process(requestPacket);
        Message response = responsePacket.getMessage();
        if (response != null && response.isFault()){
                throw new TerminateSequenceException("There was an error trying to terminate the sequence " ,response);
        }


        //What to do with response?
        //TODO
        //It may have a TerminateSequence for reverse sequence on it as well as
        //ack headers
        //Process these.

    }

    /**
     * Send Message with empty body and a single SequenceElement (with Last child) down the pipe.  Process the response,
     * which may contain a SequenceAcknowledgementElement.
     *
     * @param seq Outbound sequence to which SequenceHeaderElement will belong.
     *
     */
    public void sendLast(OutboundSequence seq, SOAPVersion version) throws RMException {

        Message request = createEmptyMessage(version);
        SequenceElement el = createLastHeader(seq);
        //request.getHeaders().add(Headers.create(version,marshaller,el));
        request.getHeaders().add(createHeader(el));

        seq.setLast();

        Packet requestPacket = new Packet(request);
        requestPacket.proxy = packet.proxy;
        //requestPacket.proxy = new ProxyWrapper(packet.proxy);
        requestPacket.setEndPointAddressString(seq.getDestination().toString());
        requestPacket.contentNegotiation = packet.contentNegotiation;
        addAddressingHeaders(requestPacket, config.getRMVersion().getLastAction(),seq.getDestination(),
                seq.getAcksTo(), /*true*/ false);

       

        Packet responsePacket = helper.process(requestPacket);
        Message response = responsePacket.getMessage();

        com.sun.xml.ws.rm.Message msg = new com.sun.xml.ws.rm.Message(response, config.rmVersion);
        if (response != null && response.isFault()){
                throw new RMException(response);
        }

        processor.processMessage(msg, marshaller, unmarshaller);

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
            AckRequestedElement el = createAckRequestedElement(seq);
            //request.getHeaders().add(Headers.create(version,marshaller,el));
            request.getHeaders().add(createHeader(el));


            Packet requestPacket = new Packet(request);
            requestPacket.proxy = packet.proxy;
            requestPacket.contentNegotiation = packet.contentNegotiation;

            addAddressingHeaders (requestPacket, config.getRMVersion().getAckRequestedAction(),
                    seq.getDestination(),seq.getAcksTo(), /*true*/ false);

            requestPacket.setEndPointAddressString(seq.getDestination().toString());

            Packet responsePacket = helper.process(requestPacket);
            Message response = responsePacket.getMessage();
            if (response != null && response.isFault()){
                    //reset alarm
                    ((ClientOutboundSequence)seq).resetLastActivityTime();
                    throw new RMException(response);
            }

            com.sun.xml.ws.rm.Message msg = new com.sun.xml.ws.rm.Message(response, config.rmVersion);
            processor.processMessage(msg, marshaller, unmarshaller);
        } finally {
            //Make sure that alarm is reset.
            ((ClientOutboundSequence)seq).resetLastActivityTime();
        }

    }

    /**
     * Initialize an AddressingProperties object using the arguments.  Put the AddressingProperties
     * object in the RequestContext obtained from getMessageProperties.
     */
    private Packet addAddressingHeaders(Packet requestPacket,
                                        String action,
                                        URI destination,
                                        URI acksTo,
                                        boolean oneWay) throws RMException {
       
        /*ADDRESSING FIX_ME

        Current API does not allow assignment of non-anon reply to, if we
           need to support non-anon acksTo.
         */
        Message message = requestPacket.getMessage();
        HeaderList list = message.getHeaders();
        if (oneWay) {
            message.assertOneWay(true);
        } else {
            message.assertOneWay(false);
        }
        //list.fillRequestAddressingHeaders(port, binding, requestPacket, action);
        requestPacket.setEndPointAddressString(destination.toString());
        list.fillRequestAddressingHeaders(requestPacket,constants.getAddressingVersion(),binding.getSOAPVersion(),oneWay,action);

        return requestPacket;
       
   }

    /**
     * Create an empty message using correct SOAPVersion
     */
    private Message createEmptyMessage(SOAPVersion version) {
        return Messages.createEmpty(version);
    }



    private AbstractCreateSequenceResponse unmarshallCreateSequenceResponse(Message response) throws RMException{
        try {
            return response.readPayloadAsJAXB(unmarshaller);
        } catch (JAXBException e) {
            throw new RMException(e);
        }
    }



     /**
     * Return a <code>SequenceElement.LastMessage</code>
     */
    private SequenceElement createLastHeader(OutboundSequence seq
                                                  ) {
        SequenceElement sequenceElement = new SequenceElement();
        sequenceElement.setId(seq.getId());
        sequenceElement.setNumber(seq.getNextIndex());
        sequenceElement.setLastMessage(new SequenceElement.LastMessage());
        return sequenceElement;
    }

    /**
     * Return a <code>AckReqesutedElement</code> whose Sequence ID matches the specified
     * <code>OutboundSequence</code> and whose MessageNumber is the
     * highest <MessageNumber> sent within a Sequence
     * sequence.
     */
    private AckRequestedElement createAckRequestedElement(OutboundSequence seq
                                                          ) {
        AckRequestedElement ackRequestedElement = new AckRequestedElement();
        ackRequestedElement.setId(seq.getId());
        ackRequestedElement.setMaxMessageNumber(seq.getNextIndex()-1);
        return ackRequestedElement;
    }

    public RMConstants getConstants() {
        return constants;
    }
    
    private com.sun.xml.ws.api.message.Header createHeader(Object obj) {
        return com.sun.xml.ws.api.message.Headers.create(config.getRMVersion().getJAXBRIContextHeaders(), obj);
    }

    
}
