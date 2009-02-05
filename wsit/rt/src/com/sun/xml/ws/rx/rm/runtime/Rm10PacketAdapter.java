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
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rx.rm.protocol.wsrm200502.AckRequestedElement;
import com.sun.xml.ws.rx.rm.protocol.wsrm200502.Identifier;
import com.sun.xml.ws.rx.rm.protocol.wsrm200502.SequenceAcknowledgementElement;
import com.sun.xml.ws.rx.rm.protocol.wsrm200502.SequenceElement;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
class Rm10PacketAdapter extends PacketAdapter {

    Rm10PacketAdapter(@NotNull SOAPVersion soapVersion, @NotNull AddressingVersion addressingVersion, @NotNull Packet packet) {
        super(RmVersion.WSRM200502, soapVersion, addressingVersion, packet);
    }

    @Override
    public void appendSequenceHeader(@NotNull String sequenceId, long messageNumber) throws RxRuntimeException {
        SequenceElement sequenceHeaderElement = new SequenceElement();
        sequenceHeaderElement.setNumber(messageNumber);
        sequenceHeaderElement.setId(sequenceId);

        appendHeader(sequenceHeaderElement);
    }

    @Override
    public void appendAckRequestedHeader(@NotNull String sequenceId) throws RxRuntimeException {
        AckRequestedElement ackRequestedElement = new AckRequestedElement();
        ackRequestedElement.setId(sequenceId);

        appendHeader(ackRequestedElement);
    }

    @Override
    public void appendSequenceAcknowledgementHeader(@NotNull Sequence sequence) throws RxRuntimeException {
        SequenceAcknowledgementElement ackElement = new SequenceAcknowledgementElement();
        Identifier identifier = new Identifier();
        identifier.setValue(sequence.getId());
        ackElement.setIdentifier(identifier);

        List<Sequence.AckRange> ackedMessageIds = sequence.getAcknowledgedMessageIds();
        sequence.clearAckRequestedFlag();
        if (ackedMessageIds != null && !ackedMessageIds.isEmpty()) {
            for (Sequence.AckRange range : ackedMessageIds) {
                ackElement.addAckRange(range.lower, range.upper);
            }
        } else {
            ackElement.addAckRange(0, 0); // we don't have any ack ranges => we have not received any message yet

        }

// TODO move this to server side - we don't have a buffer support on the client side
//        if (configuration.getDestinationBufferQuota() != Configuration.UNSPECIFIED) {
//            ackElement.setBufferRemaining(-1/*calculate remaining quota*/);
//        }

        appendHeader(ackElement);
    }

    @Override
    void initSequenceHeaderData() throws RxRuntimeException {
        SequenceElement sequenceElement = this.readHeaderAsUnderstood(RmVersion.WSRM200502.namespaceUri, "Sequence");
        if (sequenceElement != null) {
            this.setSequenceData(sequenceElement.getId(), sequenceElement.getMessageNumber());
        }
    }

    @Override
    String initAckRequestedHeaderData() throws RxRuntimeException {
        AckRequestedElement ackRequestedElement = this.readHeaderAsUnderstood(RmVersion.WSRM200502.namespaceUri, "AckRequested");
        return (ackRequestedElement != null) ? ackRequestedElement.getId() : null;
    }

    @Override
    public void processAcknowledgements(SequenceManager sequenceManager, String expectedAckedSequenceId) throws RxRuntimeException {
        SequenceAcknowledgementElement ackElement = this.readHeaderAsUnderstood(RmVersion.WSRM200502.namespaceUri, "SequenceAcknowledgement");

        if (ackElement != null) {
            if (expectedAckedSequenceId != null) {
                Utilities.assertSequenceId(expectedAckedSequenceId, ackElement.getId());
            }

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
                        lastLowerBound =
                                nackId.longValue() + 1;
                    }

                }

                long lastMessageId = sequenceManager.getSequence(ackElement.getId()).getLastMessageId();
                if (lastLowerBound <= lastMessageId) {
                    ranges.add(new Sequence.AckRange(lastLowerBound, lastMessageId));
                }

            } else if (ackElement.getAcknowledgementRange() != null && !ackElement.getAcknowledgementRange().isEmpty()) {
                for (SequenceAcknowledgementElement.AcknowledgementRange rangeElement : ackElement.getAcknowledgementRange()) {
                    ranges.add(new Sequence.AckRange(rangeElement.getLower().longValue(), rangeElement.getUpper().longValue()));
                }

            }

            sequenceManager.getSequence(ackElement.getId()).acknowledgeMessageIds(ranges);

        // TODO handle other stuff in the header
        // ackElement.getBufferRemaining();
        }
    }
}
