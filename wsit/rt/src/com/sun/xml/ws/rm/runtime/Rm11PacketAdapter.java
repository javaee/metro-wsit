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
package com.sun.xml.ws.rm.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rm.runtime.sequence.Sequence.AckRange;
import com.sun.xml.ws.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rm.v200702.AckRequestedElement;
import com.sun.xml.ws.rm.v200702.Identifier;
import com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement;
import com.sun.xml.ws.rm.v200702.SequenceElement;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 *
 * @author m_potociar
 */
public class Rm11PacketAdapter extends PacketAdapter {

    public Rm11PacketAdapter(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void appendSequenceHeader(@NotNull String sequenceId, long messageNumber) throws RmException {
        SequenceElement sequenceHeaderElement = new SequenceElement();
        sequenceHeaderElement.setNumber(messageNumber);
        sequenceHeaderElement.setId(sequenceId);

        appendHeader(sequenceHeaderElement);
    }

    @Override
    public void appendAckRequestedHeader(@NotNull String sequenceId) throws RmException {
        AckRequestedElement ackRequestedElement = new AckRequestedElement();
        ackRequestedElement.setId(sequenceId);

        appendHeader(ackRequestedElement);
    }

    @Override
    public void appendSequenceAcknowledgementHeader(@NotNull String sequenceId, List<AckRange> acknowledgedIndexes) throws RmException {
        SequenceAcknowledgementElement ackElement = new SequenceAcknowledgementElement();
        Identifier identifier = new Identifier();
        identifier.setValue(sequenceId);
        ackElement.setIdentifier(identifier);

        if (acknowledgedIndexes != null && !acknowledgedIndexes.isEmpty()) {
            for (Sequence.AckRange range : acknowledgedIndexes) {
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
    protected void initSequenceHeaderData() throws RmException {
        SequenceElement sequenceElement = this.readHeaderAsUnderstood("Sequence");
        if (sequenceElement != null) {
            this.setSequenceData(sequenceElement.getId(), sequenceElement.getMessageNumber());
        }
    }

    @Override
    protected String initAckRequestedHeaderData() throws RmException {
        AckRequestedElement ackRequestedElement = this.readHeaderAsUnderstood("AckRequested");
        return (ackRequestedElement != null) ? ackRequestedElement.getId() : null;
    }

    @Override
    public void processAcknowledgements(SequenceManager sequenceManager, String expectedAckedSequenceId) throws RmException {
        SequenceAcknowledgementElement ackElement = this.readHeaderAsUnderstood("SequenceAcknowledgement");

        if (ackElement != null) {
            Utilities.assertSequenceId(expectedAckedSequenceId, ackElement.getId());

            List<Sequence.AckRange> ranges = new LinkedList<Sequence.AckRange>();
            if (ackElement.getNone() == null) {
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

                    long lastMessageId = sequenceManager.getSequence(ackElement.getId()).getLastMessageId();
                    if (lastLowerBound <= lastMessageId) {
                        ranges.add(new Sequence.AckRange(lastLowerBound, lastMessageId));
                    }
                } else if (ackElement.getAcknowledgementRange() != null && !ackElement.getAcknowledgementRange().isEmpty()) {
                    for (SequenceAcknowledgementElement.AcknowledgementRange rangeElement : ackElement.getAcknowledgementRange()) {
                        ranges.add(new Sequence.AckRange(rangeElement.getLower().longValue(), rangeElement.getUpper().longValue()));
                    }
                }
            }

            sequenceManager.getSequence(ackElement.getId()).acknowledgeMessageIds(ranges);

        // TODO handle final and remaining buffer in the header
        // ackElement.getBufferRemaining();
        // ackElement.getFinal();
        }
    }
    
    @Override
    protected Header createSequenceFaultHeader(QName subcode) {
        return Headers.create(RmVersion.WSRM11.jaxbContext, new com.sun.xml.ws.rm.v200702.SequenceFaultElement(subcode));
    }
}

