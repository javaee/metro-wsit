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
 * ProtocolMessageSender.java
 *
 * @author Mike Grogan
 * Created on January 30, 2006, 12:12 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime.client;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.*;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.rm.*;
import com.sun.xml.ws.rm.jaxws.runtime.InboundMessageProcessor;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.protocol.*;

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
     * The next client pipe in the pipeline.  Used to propogate the messages.
     */
    private Pipe nextPipe;

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

    /**
     * Public ctor.  Initialize the fields
     */
    public ProtocolMessageSender(InboundMessageProcessor processor,
                                 Marshaller marshaller,
                                 Unmarshaller unmarshaller,
                                 WSDLPort port,
                                 WSBinding binding,
                                 Pipe nextPipe,
                                 Packet packet) {

        this.processor = processor;
        this.nextPipe = nextPipe;
        this.port = port;
        this.binding = binding;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
        this.constants =  RMConstants.getRMConstants(binding.getAddressingVersion());
        this.packet = packet;

    }

    public CreateSequenceResponseElement sendCreateSequence(CreateSequenceElement cs,
                                                            URI destination,
                                                            URI acksTo,
                                                            SOAPVersion version) throws RMException {

        //Used from com.sun.xml.ws.jaxws.runtime.client.ClientOutboundSequence.connect, where
        //CreateSequence object is constructed and resulting CreateSequenceResponse object is
        //processed.
        CreateSequenceResponseElement csrElem = null;

        //1. Initialize  message adding CreateSequence to body
        if (cs != null) {
            Message request = Messages.create(marshaller,cs,version);


            //Addressing Headers are added by configuring the following property
            //on the packet

            Packet requestPacket = new Packet(request);
            requestPacket.proxy = packet.proxy;
            requestPacket.contentNegotiation = packet.contentNegotiation;
            requestPacket.setEndPointAddressString(destination.toString());

            addAddressingHeaders (requestPacket, Constants.CREATE_SEQUENCE_ACTION,
                    destination , acksTo, false);

            String messageId = null ;/*= ADDRESSING_FIXME - initialize with mesageID
                                   assigned by addAddressingHeaders for use in
                                   correlating non-anonymous acksTo response*/



            Packet responsePacket = nextPipe.process(requestPacket);

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

        Message request = Messages.create(marshaller,ts,version);

        //piggyback an acknowledgement if one is pending
        seq.processAcknowledgement(new com.sun.xml.ws.rm.Message(request), marshaller);

        Packet requestPacket = new Packet(request);
        requestPacket.proxy = packet.proxy;
        requestPacket.contentNegotiation = packet.contentNegotiation;
        addAddressingHeaders (requestPacket,Constants.TERMINATE_SEQUENCE_ACTION,seq.getDestination(),seq.getAcksTo(),true);
        requestPacket.setEndPointAddressString(seq.getDestination().toString());
        Packet responsePacket = nextPipe.process(requestPacket);
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
        request.getHeaders().add(Headers.create(version,marshaller,el));

        seq.setLast();

        Packet requestPacket = new Packet(request);
        requestPacket.proxy = packet.proxy;
        //requestPacket.proxy = new ProxyWrapper(packet.proxy);
        requestPacket.setEndPointAddressString(seq.getDestination().toString());
        requestPacket.contentNegotiation = packet.contentNegotiation;
        addAddressingHeaders(requestPacket, constants.getLastAction(),seq.getDestination(),
                seq.getAcksTo(),true);

       

        Packet responsePacket = nextPipe.process(requestPacket);
        Message response = responsePacket.getMessage();

        com.sun.xml.ws.rm.Message msg = new com.sun.xml.ws.rm.Message(response);
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
            request.getHeaders().add(Headers.create(version,marshaller,el));


            Packet requestPacket = new Packet(request);
            requestPacket.proxy = packet.proxy;
            requestPacket.contentNegotiation = packet.contentNegotiation;

            addAddressingHeaders (requestPacket, Constants.ACK_REQUESTED_ACTION,
                    seq.getDestination(),seq.getAcksTo(),true);

            requestPacket.setEndPointAddressString(seq.getDestination().toString());

            Packet responsePacket = nextPipe.process(requestPacket);
            Message response = responsePacket.getMessage();
            if (response != null && response.isFault()){
                    //reset alarm
                    ((ClientOutboundSequence)seq).resetLastActivityTime();
                    throw new RMException(response);
            }

            com.sun.xml.ws.rm.Message msg = new com.sun.xml.ws.rm.Message(response);
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
        //if (requestPacket.proxy != null) {


            /*
             AddressingProperties appImpl = constants.getAddressingBuilder().newAddressingProperties();

            appImpl.setAction(constants.getAddressingBuilder().newURI(action));
            appImpl.setTo(destination.getAddress());
            appImpl.setReplyTo(acksTo);


            AttributedURI uri = constants.getAddressingBuilder()
                    .newURI("uuid:" + UUID.randomUUID());

            appImpl.setMessageID(uri);

            Map<String,Object> reqContext = requestPacket.proxy.getRequestContext();
            reqContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES,appImpl);
            requestPacket.invocationProperties.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES,appImpl);
            */

            /*ADDRESSING FIX_ME
            1. No longer need destination
            2. Current API does not allow assignment of non-anon reply to, if we
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
        /*
         } else {
            throw new RMException("Binding Provider null in packet");
        }
         */

   }

    /**
     * Create an empty message using correct SOAPVersion
     */
    private Message createEmptyMessage(SOAPVersion version) {
        return Messages.createEmpty(version);
    }



    private CreateSequenceResponseElement unmarshallCreateSequenceResponse(Message response) throws RMException{
        CreateSequenceResponseElement csrElement = null;
        try {
            csrElement = response.readPayloadAsJAXB(unmarshaller);
        } catch (JAXBException e) {
            throw new RMException (e);
        }
        return csrElement;
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

    /*
    private static class ProxyWrapper implements BindingProvider {
        private BindingProvider proxy;
        private Map<String, Object> requestContext = new HashMap<String, Object>();

        public ProxyWrapper(BindingProvider proxy) {
            this.proxy = proxy;
            requestContext.putAll(proxy.getRequestContext());
        }

        public Map<String, Object> getRequestContext() {
            return requestContext;
        }

        public Map<String, Object> getResponseContext() {
            return proxy.getResponseContext();
        }

        public Binding getBinding() {
            return proxy.getBinding();
        }
    }
    */
}
