/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rx.rm.runtime;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.protocol.AcknowledgementData;
import com.sun.xml.ws.rx.rm.protocol.CloseSequenceData;
import com.sun.xml.ws.rx.rm.protocol.CloseSequenceResponseData;
import com.sun.xml.ws.rx.rm.protocol.CreateSequenceData;
import com.sun.xml.ws.rx.rm.protocol.CreateSequenceResponseData;
import com.sun.xml.ws.rx.rm.protocol.TerminateSequenceData;
import com.sun.xml.ws.rx.rm.protocol.TerminateSequenceResponseData;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateMessageRegistrationException;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.protocol.wsrm200502.AckRequestedElement;
import com.sun.xml.ws.rx.rm.protocol.wsrm200502.CreateSequenceElement;
import com.sun.xml.ws.rx.rm.protocol.wsrm200502.CreateSequenceResponseElement;
import com.sun.xml.ws.rx.rm.protocol.wsrm200502.SequenceAcknowledgementElement;
import com.sun.xml.ws.rx.rm.protocol.wsrm200502.SequenceElement;
import com.sun.xml.ws.rx.rm.protocol.wsrm200502.SequenceElement.LastMessage;
import com.sun.xml.ws.rx.rm.protocol.wsrm200502.TerminateSequenceElement;
import com.sun.xml.ws.rx.rm.runtime.sequence.UnknownSequenceException;
import com.sun.xml.ws.rx.util.Communicator;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
final class Wsrm200502ProtocolHandler extends WsrmProtocolHandler {

    private static final Logger LOGGER = Logger.getLogger(Wsrm200502ProtocolHandler.class);
    private final RuntimeContext rc;

    Wsrm200502ProtocolHandler(RmConfiguration configuration, RuntimeContext rc, Communicator communicator) {
        super(RmVersion.WSRM200502, configuration, communicator);

        assert rc != null;

        this.rc = rc;
    }

    public CreateSequenceData toCreateSequenceData(@NotNull Packet packet) throws RxRuntimeException {
        assert packet != null;
        assert packet.getMessage() != null;
        assert !packet.getMessage().isFault();

        Message message = packet.getMessage();
        CreateSequenceElement csElement = unmarshallMessage(message);

        // TODO process UsesSequenceSTR

        return csElement.toDataBuilder().build();
    }

    public Packet toPacket(CreateSequenceData data, @Nullable Packet requestPacket) throws RxRuntimeException {
        Packet packet = communicator.createRequestPacket(requestPacket, new CreateSequenceElement(data), rmVersion.createSequenceAction, true);

        return packet;
    }

    public CreateSequenceResponseData toCreateSequenceResponseData(Packet packet) throws RxRuntimeException {
        assert packet != null;
        assert packet.getMessage() != null;
        assert !packet.getMessage().isFault();

        Message message = packet.getMessage();
        CreateSequenceResponseElement csrElement = unmarshallMessage(message);

        return csrElement.toDataBuilder().build();
    }

    public Packet toPacket(CreateSequenceResponseData data, @Nullable Packet requestPacket) throws RxRuntimeException {
        return communicator.createResponsePacket(requestPacket, new CreateSequenceResponseElement(data), rmVersion.createSequenceResponseAction);
    }

    public CloseSequenceData toCloseSequenceData(Packet packet) throws RxRuntimeException {
        assert packet != null;
        assert packet.getMessage() != null;
        assert !packet.getMessage().isFault();

        Message message = packet.getMessage();

        try {
            ApplicationMessage lastAppMessage = new ApplicationMessageBase("") {
            };
            loadSequenceHeaderData(lastAppMessage, message);
            loadAcknowledgementData(lastAppMessage, message);

            // simulating last message delivery
            Sequence inboundSequence = rc.getSequence(lastAppMessage.getSequenceId());
            try {
                inboundSequence.registerMessage(lastAppMessage, false);
            } catch (Exception ex) {
                throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRM_1146_UNEXPECTED_ERROR_WHILE_REGISTERING_MESSAGE(), ex));
            }
            inboundSequence.acknowledgeMessageNumber(lastAppMessage.getMessageNumber());
            inboundSequence.setAckRequestedFlag();

            CloseSequenceData.Builder dataBuilder = CloseSequenceData.getBuilder(lastAppMessage.getSequenceId(), lastAppMessage.getMessageNumber());
            dataBuilder.acknowledgementData(lastAppMessage.getAcknowledgementData());
            return dataBuilder.build();
        } finally {
            message.consume();
        }
    }

    public Packet toPacket(CloseSequenceData data, @Nullable Packet requestPacket) throws RxRuntimeException {
        Packet packet;
        if (requestPacket != null) {
            packet = communicator.createEmptyResponsePacket(requestPacket, rmVersion.closeSequenceAction);
        } else {
            packet = communicator.createEmptyRequestPacket(rmVersion.closeSequenceAction, false);
        }
        final Message message = packet.getMessage();

        ApplicationMessage lastAppMessage = new ApplicationMessageBase("") {
        };
        try {
            rc.getOutboundSequence(data.getSequenceId()).registerMessage(lastAppMessage, false);
        } catch (DuplicateMessageRegistrationException ex) {
            LOGGER.logSevereException(ex);
        } catch (IllegalStateException ex) {
            LOGGER.logSevereException(ex);
        }

        SequenceElement sequenceElement = new SequenceElement();
        sequenceElement.setId(lastAppMessage.getSequenceId());
        sequenceElement.setMessageNumber(lastAppMessage.getMessageNumber());
        sequenceElement.setLastMessage(new LastMessage());

        message.getHeaders().add(createHeader(sequenceElement));

        appendAcknowledgementHeaders(packet, data.getAcknowledgementData());

        return packet;
    }

    public CloseSequenceResponseData toCloseSequenceResponseData(Packet packet) throws RxRuntimeException {
        assert packet != null;
        assert packet.getMessage() != null;
        assert !packet.getMessage().isFault();

        Message message = packet.getMessage();
        try {
            AcknowledgementData ackData = getAcknowledgementData(message);

            CloseSequenceResponseData.Builder dataBuilder = CloseSequenceResponseData.getBuilder(ackData.getAcknowledgedSequenceId());

            dataBuilder.acknowledgementData(ackData);

            return dataBuilder.build();
        } finally {
            message.consume();
        }
    }

    public Packet toPacket(CloseSequenceResponseData data, @Nullable Packet requestPacket) throws RxRuntimeException {
        /**
         * Server-side Replay model (https://www.wso2.org/library/2792#Server) requirements:
         * 
         * D) If there is no response to a given request – i.e. the request is a one-way message, 
         *    then the Server MUST respond with an acknowledgement that includes the request message 
         *    in one of the ranges.
         * 
         * ...
         * 
         * F) The Server SHOULD respond to an incoming LastMessage with a LastMessage for the Offered Sequence
         */
        Sequence boundSequence = rc.sequenceManager().getBoundSequence(data.getSequenceId());
        if (boundSequence != null) {
            // Apply requirement D)
            CloseSequenceData closeSequenceData = CloseSequenceData.getBuilder(boundSequence.getId(), boundSequence.getLastMessageNumber()).acknowledgementData(data.getAcknowledgementData()).build();
            return toPacket(closeSequenceData, requestPacket);
        } else {
            // Apply requirement F)
            return createEmptyAcknowledgementResponse(data.getAcknowledgementData(), requestPacket);
        }
    }

    public TerminateSequenceData toTerminateSequenceData(Packet packet) throws RxRuntimeException {
        assert packet != null;
        assert packet.getMessage() != null;
        assert !packet.getMessage().isFault();

        Message message = packet.getMessage();
        TerminateSequenceElement tsElement = unmarshallMessage(message);
        final TerminateSequenceData.Builder dataBuilder = tsElement.toDataBuilder();

        dataBuilder.acknowledgementData(getAcknowledgementData(message));

        return dataBuilder.build();
    }

    public Packet toPacket(TerminateSequenceData data, @Nullable Packet requestPacket) throws RxRuntimeException {
        Packet packet = communicator.createRequestPacket(requestPacket, new TerminateSequenceElement(data), rmVersion.terminateSequenceAction, true);

        if (data.getAcknowledgementData() != null) {
            appendAcknowledgementHeaders(packet, data.getAcknowledgementData());
        }

        return packet;
    }

    public TerminateSequenceResponseData toTerminateSequenceResponseData(Packet packet) throws RxRuntimeException {
        assert packet != null;
        assert packet.getMessage() != null;
        assert !packet.getMessage().isFault();

        Message message = packet.getMessage();
        try {
            TerminateSequenceResponseData.Builder dataBuilder = TerminateSequenceResponseData.getBuilder(""/*TODO*/);
            dataBuilder.acknowledgementData(getAcknowledgementData(message));

            return dataBuilder.build();
        } finally {
            message.consume();
        }
    }

    public Packet toPacket(TerminateSequenceResponseData data, @Nullable Packet requestPacket) throws RxRuntimeException {
        if (data.getBoundSequenceId() != null) { // send back terminate sequence
            TerminateSequenceData tsData = TerminateSequenceData
                    .getBuilder(data.getBoundSequenceId(), data.getBoundSequenceLastMessageId())
                    .acknowledgementData(data.getAcknowledgementData())
                    .build();

            return toPacket(tsData, requestPacket);
        } else {
            requestPacket.transportBackChannel.close();
            return communicator.createNullResponsePacket(requestPacket);            
        }
    }

    public void appendSequenceHeader(@NotNull Message jaxwsMessage, @NotNull ApplicationMessage message) throws RxRuntimeException {
        assert message != null;
        assert message.getSequenceId() != null;

        SequenceElement sequenceHeaderElement = new SequenceElement();
        sequenceHeaderElement.setId(message.getSequenceId());
        sequenceHeaderElement.setMessageNumber(message.getMessageNumber());

        sequenceHeaderElement.getOtherAttributes().put(communicator.soapMustUnderstandAttributeName, "true");
        jaxwsMessage.getHeaders().add(createHeader(sequenceHeaderElement));
    }

    public void appendAcknowledgementHeaders(@NotNull Packet packet, @NotNull AcknowledgementData ackData) {
        assert packet != null;
        assert packet.getMessage() != null;
        assert ackData != null;


        Message jaxwsMessage = packet.getMessage();
        // ack requested header
        if (ackData.getAckReqestedSequenceId() != null) {
            AckRequestedElement ackRequestedElement = new AckRequestedElement();
            ackRequestedElement.setId(ackData.getAckReqestedSequenceId());

            // MU attribute removed to comply with WS-I RSP R0540 - see WSIT issue #1318
            // ackRequestedElement.getOtherAttributes().put(communicator.soapMustUnderstandAttributeName, "true");
            jaxwsMessage.getHeaders().add(createHeader(ackRequestedElement));
        }

        // sequence acknowledgement header
        if (ackData.containsSequenceAcknowledgementData()) {
            SequenceAcknowledgementElement ackElement = new SequenceAcknowledgementElement();
            ackElement.setId(ackData.getAcknowledgedSequenceId());

                for (Sequence.AckRange range : ackData.getAcknowledgedRanges()) {
                    ackElement.addAckRange(range.lower, range.upper);
                }

// TODO decide whether we will advertise remaining buffer
//        if (configuration.getDestinationBufferQuota() != Configuration.UNSPECIFIED) {
//            ackElement.setBufferRemaining(-1/*calculate remaining quota*/);
//        }

            // MU attribute removed to comply with WS-I RSP R0540 - see WSIT issue #1318
            // ackElement.getOtherAttributes().put(communicator.soapMustUnderstandAttributeName, "true");
            jaxwsMessage.getHeaders().add(createHeader(ackElement));
        }
    }

    public void loadSequenceHeaderData(@NotNull ApplicationMessage message, @NotNull Message jaxwsMessage) throws RxRuntimeException {
        assert message != null;
        assert message.getSequenceId() == null; // not initialized yet

        SequenceElement sequenceElement = readHeaderAsUnderstood(RmVersion.WSRM200502.namespaceUri, "Sequence", jaxwsMessage);
        if (sequenceElement != null) {
            message.setSequenceData(sequenceElement.getId(), sequenceElement.getMessageNumber());
        }
    }

    public void loadAcknowledgementData(@NotNull ApplicationMessage message, @NotNull Message jaxwsMessage) throws RxRuntimeException {
        assert message != null;
        assert message.getAcknowledgementData() == null; // not initialized yet

        message.setAcknowledgementData(getAcknowledgementData(jaxwsMessage));
    }

    public AcknowledgementData getAcknowledgementData(Message jaxwsMessage) throws UnknownSequenceException, RxRuntimeException {
        assert jaxwsMessage != null;

        AcknowledgementData.Builder ackDataBuilder = AcknowledgementData.getBuilder();
        AckRequestedElement ackRequestedElement = readHeaderAsUnderstood(rmVersion.namespaceUri, "AckRequested", jaxwsMessage);
        if (ackRequestedElement != null) {
            ackDataBuilder.ackReqestedSequenceId(ackRequestedElement.getId());
        }
        SequenceAcknowledgementElement ackElement = readHeaderAsUnderstood(rmVersion.namespaceUri, "SequenceAcknowledgement", jaxwsMessage);
        if (ackElement != null) {
            List<Sequence.AckRange> ranges = new LinkedList<Sequence.AckRange>();
            if (!ackElement.getNack().isEmpty()) {
                List<BigInteger> nacks = new ArrayList<BigInteger>(ackElement.getNack());
                Collections.sort(nacks);
                long lastLowerBound = 1;
                for (BigInteger nackId : nacks) {
                    if (lastLowerBound == nackId.longValue()) {
                        lastLowerBound++;
                    } else {
                        ranges.add(new Sequence.AckRange(lastLowerBound, nackId.longValue() - 1));
                        lastLowerBound = nackId.longValue() + 1;
                    }
                }

                long lastMessageId = rc.getSequence(ackElement.getId()).getLastMessageNumber();
                if (lastLowerBound <= lastMessageId) {
                    ranges.add(new Sequence.AckRange(lastLowerBound, lastMessageId));
                }

            } else if (ackElement.getAcknowledgementRange() != null && !ackElement.getAcknowledgementRange().isEmpty()) {
                for (SequenceAcknowledgementElement.AcknowledgementRange rangeElement : ackElement.getAcknowledgementRange()) {
                    ranges.add(new Sequence.AckRange(rangeElement.getLower().longValue(), rangeElement.getUpper().longValue()));
                }
            }
            // TODO handle final and remaining buffer in the header
            // ackElement.getBufferRemaining();
            ackDataBuilder.acknowledgements(ackElement.getId(), ranges, false);
        }
        return ackDataBuilder.build();
    }

    @Override
    public Header createSequenceFaultElementHeader(QName subcode, Object detail) {
        return Headers.create(rmVersion.getJaxbContext(addressingVersion),
                new com.sun.xml.ws.rx.rm.protocol.wsrm200502.SequenceFaultElement(subcode));
    }

    @Override
    public Packet createEmptyAcknowledgementResponse(AcknowledgementData ackData, Packet requestPacket) throws RxRuntimeException {
        if (ackData.getAckReqestedSequenceId() != null || ackData.containsSequenceAcknowledgementData()) {
            // create acknowledgement response only if there is something to send in the SequenceAcknowledgement header
            Packet response = rc.communicator.createEmptyResponsePacket(requestPacket, rc.rmVersion.sequenceAcknowledgementAction);
            response = rc.communicator.setEmptyResponseMessage(response, requestPacket, rc.rmVersion.sequenceAcknowledgementAction);
            appendAcknowledgementHeaders(response, ackData);
            return response;
        } else {
            return rc.communicator.createNullResponsePacket(requestPacket);
        }
    }
}
