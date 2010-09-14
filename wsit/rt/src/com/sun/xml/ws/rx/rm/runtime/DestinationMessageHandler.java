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
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.faults.WsrmRequiredException;
import com.sun.xml.ws.rx.rm.protocol.AcknowledgementData;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateMessageRegistrationException;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.AckRange;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.UnknownSequenceException;
import java.util.List;
import java.util.logging.Level;

/**
 * Handles incomming application messages. This class encapsulates
 * RM Source logic that is independent on of tha actual delivery mechanism
 * or framework (such as JAX-WS fibers).
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
class DestinationMessageHandler implements MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(DestinationMessageHandler.class);
    //
    private volatile SequenceManager sequenceManager;

    DestinationMessageHandler(@Nullable SequenceManager sequenceManager) {
        this.sequenceManager = sequenceManager;
    }

    void setSequenceManager(SequenceManager sequenceManager) {
        this.sequenceManager = sequenceManager;
    }

    /**
     * Registers incomming message with the given inbound sequence and
     * processes any acknowledgement information that the message carries.
     *
     * Once the message is registered and ack information processed, the message
     * is placed into a delivery queue and delivery callback is invoked
     */
    public void registerMessage(@NotNull ApplicationMessage inMessage) throws DuplicateMessageRegistrationException, UnknownSequenceException, WsrmRequiredException {
        assert sequenceManager != null;
        assert inMessage != null;

        final String inboundSequenceId = inMessage.getSequenceId();
        if (inboundSequenceId == null) {
            throw new WsrmRequiredException();
        }
        final Sequence inboundSequence = sequenceManager.getSequence(inboundSequenceId);
        

        // register and possibly store message in the unacked message sequence queue
        inboundSequence.registerMessage(inMessage, true); // TODO this may not be always needed in case of AtMostOnce delivery

        inboundSequence.setAckRequestedFlag(); // simulate acknowledgement request for new each message
    }

    public void processAcknowledgements(@Nullable AcknowledgementData acknowledgementData) throws UnknownSequenceException {
        assert sequenceManager != null;

        if (acknowledgementData == null) {
            return;
        }

        if (acknowledgementData.getAcknowledgedSequenceId() != null) { // process outbound sequence acknowledgements           
            final List<AckRange> acknowledgedRanges = acknowledgementData.getAcknowledgedRanges();
            if (!acknowledgedRanges.isEmpty()) {
                Sequence sequence = sequenceManager.getSequence(acknowledgementData.getAcknowledgedSequenceId());
                if (!sequence.isClosed()) { // we ignore acknowledgments on closed sequences
                    sequence.acknowledgeMessageNumbers(acknowledgedRanges);
                }
            }
        }

        if (acknowledgementData.getAckReqestedSequenceId() != null) { // process inbound sequence ack requested flag
            final Sequence inboundSequence = sequenceManager.getSequence(acknowledgementData.getAckReqestedSequenceId());
            inboundSequence.setAckRequestedFlag();
        }
    }

    /**
     * Retrieves acknowledgement information for a given outbound (and inbound) sequence
     *
     * @param outboundSequenceId outbound sequence identifier
     * @return acknowledgement information for a given outbound sequence
     * @throws UnknownSequenceException if no such sequence exits for a given sequence identifier
     */
    public AcknowledgementData getAcknowledgementData(String inboundSequenceId) throws UnknownSequenceException {
        assert sequenceManager != null;

        AcknowledgementData.Builder ackDataBuilder = AcknowledgementData.getBuilder();
        final Sequence inboundSequence = sequenceManager.getSequence(inboundSequenceId);
        if (inboundSequence.isAckRequested() || inboundSequence.isClosed()) {
            ackDataBuilder.acknowledgements(inboundSequence.getId(), inboundSequence.getAcknowledgedMessageNumbers(), inboundSequence.isClosed());
            inboundSequence.clearAckRequestedFlag();
        }

        // outbound sequence ack requested flag
        Sequence outboundSequence = sequenceManager.getBoundSequence(inboundSequenceId);
        if (outboundSequence != null && outboundSequence.hasUnacknowledgedMessages()) {
            ackDataBuilder.ackReqestedSequenceId(outboundSequence.getId());
            outboundSequence.updateLastAcknowledgementRequestTime();
        }
        
        return ackDataBuilder.build();
    }

    public void acknowledgeApplicationLayerDelivery(ApplicationMessage inMessage) throws UnknownSequenceException {
        assert sequenceManager != null;

        sequenceManager.getSequence(inMessage.getSequenceId()).acknowledgeMessageNumber(inMessage.getMessageNumber());
    }

    public void putToDeliveryQueue(ApplicationMessage message) throws RxRuntimeException, UnknownSequenceException {
        assert sequenceManager != null;

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format("Putting a message with number [ %d ] to the delivery queue of a sequence [ %s ]", message.getMessageNumber(), message.getSequenceId()));
        }

        sequenceManager.getSequence(message.getSequenceId()).getDeliveryQueue().put(message);
    }
}
