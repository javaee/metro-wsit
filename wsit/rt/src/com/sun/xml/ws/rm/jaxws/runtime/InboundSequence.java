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
package com.sun.xml.ws.rm.jaxws.runtime;

import com.sun.xml.ws.rm.InvalidMessageNumberException;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.Sequence;
import com.sun.xml.ws.rm.protocol.AbstractSequenceAcknowledgement;
import com.sun.xml.ws.rm.v200502.Identifier;
import com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement;


/**
 * An <code>InboundSequence</code> represents a sequence of incoming messages.  For an 
 * <code>RMDestination</code>, an <code>InboundSequnce</code> consists of all
 * the requests to a service from a particular proxy.  For an <code>RMSource</code>,
 * an <code>InboundSequence</code> contains all the response messages to requests 
 * in the companion <code>OutboundSequence</code>.
 */
public abstract class InboundSequence extends Sequence {

    /**
     * Companion OutboundSequence
     */
    private OutboundSequence companionOutboundSequence;
    /**
     * The SecurityTokenReference obtained from the CreateSequence
     */
    private String securityTokenReferenceId;

    protected InboundSequence(SequenceConfig config) {
        super(config);
    }

    protected InboundSequence(SequenceConfig config, boolean setMaxMessages) {
        // TODO: remove
        super(config, setMaxMessages);
    }

    protected void setCompanionSequence(OutboundSequence companionSequence) {
        // TODO: remove this method if possible
        this.companionOutboundSequence = companionSequence;
    }

    /** Construct a <code>SequenceAcknowlegementElement</code> based on the contents of this sequence.
     *
     * @param reqElement The <code>AckRequestedElement</code> to process.  May be
     *                   null.  It is only used to determine its (optional) LastMessage
     *                   element.  If missing, LastMessage is assumed to be nextIndex - 1.
     *        marshaller The marshaller to be used for construction of the return value.
     *
     * TODO - decide whether this needs to be synchronized.  It does not
     * need to be if concurrent modifications only cause messages that
     * have indeed arrived to be unacknowledged
     */
    public synchronized AbstractSequenceAcknowledgement generateSequenceAcknowledgement(boolean generateIsFinal) throws InvalidMessageNumberException {
        AbstractSequenceAcknowledgement ackElement = null;
        if (getConfig().getRMVersion() == RmVersion.WSRM10) {
            ackElement = new SequenceAcknowledgementElement();
            Identifier identifier = new Identifier();
            identifier.setValue(getId());
            ((SequenceAcknowledgementElement) ackElement).setIdentifier(identifier);
        } else {
            ackElement = new com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement();
            com.sun.xml.ws.rm.v200702.Identifier identifier = new com.sun.xml.ws.rm.v200702.Identifier();
            identifier.setValue(getId());
            ((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement) ackElement).setIdentifier(identifier);
            //If the RM version is 1.1 then the SequenceAcknowledgmenet.Final needs to be added
            //when CloseSequence message is processed
            if (generateIsFinal) {
                com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement.Final finalElement = new com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement.Final();
                ((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement) ackElement).setFinal(finalElement);
            }
        }

        if (getConfig().isFlowControlRequired()) {
            ackElement.setBufferRemaining(getMaxMessages() - getStoredMessages());
        }

        int maxMessageNumber = 0;

        //FIXME The new WSRM 1.1 spec has no element for MaxMessageNumber in AckRequested hence commenting this code we will just use the last index of the message
        /*if (reqElement != null) {
        maxMessageNumber = (int)(reqElement.getMaxMessageNumber());
        }*/

        //if max message number element is not present, use the last
        //index we know of. 
        if (maxMessageNumber == 0) {
            maxMessageNumber = getNextIndex() - 1;
        }

        int lower = 1;
        int current = 1;
        boolean gap = (get(current) == null);

        while (current <= maxMessageNumber) {
            if (gap) {
                if (get(current) != null) {
                    lower = current;
                    gap = false;
                }
            } else {
                if (get(current) == null) {
                    ackElement.addAckRange(lower, current - 1);
                    gap = true;
                }
            }
            current++;
        }

        if (!gap) {
            ackElement.addAckRange(lower, current - 1);
        }

        return ackElement;
    }

    /**
     * Queue up a <code>SequenceAcknowledgement</code> element on companion <code>OutboundSequence</code>
     * for delivery on next outbound application message.
     * TODO Currently only works for replyTo = AcksTo scenarios.  Expand functionality to allow AcksTo
     * to different destination.
     *   
     * @param reqElement The <code>AbstractAckRequested</code> to process.
     *        marshaller The marshaller to be used for construction of the return value.
     */
    public synchronized void handleAckRequested() throws InvalidMessageNumberException {
        // TODO rename method to better suit the meaning
        AbstractSequenceAcknowledgement ackElement = generateSequenceAcknowledgement(false);
        companionOutboundSequence.setSequenceAcknowledgement(ackElement);
    }

    /**
     * Accessor for the companion <code>OutboundSequence</code>
     *
     * @return The OutboundSequence.
     */
    public OutboundSequence getOutboundSequence() {
        return companionOutboundSequence;

    }

    public String getSecurityTokenReferenceId() {
        return securityTokenReferenceId;
    }

    public void setSecurityTokenReferenceId(String strId) {
        this.securityTokenReferenceId = strId;
    }

    public String getSessionId() {
        return securityTokenReferenceId != null ? securityTokenReferenceId : getId();
    }
}


