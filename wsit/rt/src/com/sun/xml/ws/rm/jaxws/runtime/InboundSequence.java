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
 * InboundSequence.java
 *
 * @author Mike Grogan
 * Created on November 24, 2005, 9:29 AM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime;

import com.sun.xml.ws.rm.InvalidMessageNumberException;
import com.sun.xml.ws.rm.Sequence;
import com.sun.xml.ws.rm.protocol.AckRequestedElement;
import com.sun.xml.ws.rm.protocol.Identifier;
import com.sun.xml.ws.rm.protocol.SequenceAcknowledgementElement;
import javax.xml.bind.Marshaller;
import java.net.URI;

/**
 * An <code>InboundSequence</code> represents a sequence of incoming messages.  For an 
 * <code>RMDestination</code>, an <code>InboundSequnce</code> consists of all
 * the requests to a service from a particular proxy.  For an <code>RMSource</code>,
 * an <code>InboundSequence</code> contains all the response messages to requests 
 * in the companion <code>OutboundSequence</code>.
 */
public abstract class InboundSequence extends Sequence {
    
   
    /**
     * Configuration for this sequence.
     */
    protected SequenceConfig config;
    
    /**
     * AcksTo URI.  Assigned by ctor.
     */
    protected URI acksTo;
    
    /**
     * Companion OutboundSequence
     */
    protected OutboundSequence outboundSequence;



    /**
     * The SecurityTokenReference obtained from the CreateSequence
     */
    private String strId;

    public InboundSequence() {}
    
    public InboundSequence(URI acksTo, 
            SequenceConfig config) {
        
        this.acksTo = acksTo;
        this.config = config;
        this.rmConstants = config.getRMConstants();
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
    public synchronized SequenceAcknowledgementElement 
            generateSequenceAcknowledgement(AckRequestedElement reqElement, 
                                            Marshaller marshaller) 
                throws InvalidMessageNumberException {
        
        
        SequenceAcknowledgementElement ackElement= 
                new SequenceAcknowledgementElement();
       
 	Identifier id = new Identifier();
        id.setValue(getId());
        ackElement.setIdentifier(id);
        
        
        if (config != null && config.flowControl) {
            ackElement.setBufferRemaining(maxMessages - storedMessages);
        }
        
        int maxMessageNumber = 0;
        
        if (reqElement != null) {
            maxMessageNumber = (int)(reqElement.getMaxMessageNumber());
        }
        
        //if max message number element is not present, use the last
        //index we know of. 
        if (maxMessageNumber == 0) {
            maxMessageNumber = nextIndex - 1;
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
     * TODO
     * Currently only works for replyTo = AcksTo scenarios.  Expand functionality to allow AcksTo
     * to different destination.
     *
     *   
     * @param reqElement The <code>AckRequestedElement</code> to process.
     *        marshaller The marshaller to be used for construction of the return value.
     */
    public synchronized void  handleAckRequested(AckRequestedElement reqElement, 
                                   Marshaller marshaller) 
                throws InvalidMessageNumberException {
        
        SequenceAcknowledgementElement ackElement = 
                generateSequenceAcknowledgement(reqElement,
                                                marshaller);
        outboundSequence.setSequenceAcknowledgement(ackElement);
    }
    
    /**
     * Accessor for the companion <code>OutboundSequence</code>
     *
     * @return The OutboundSequence.
     */
    public OutboundSequence getOutboundSequence() {
        return outboundSequence;
        
    }

    public String getStrId() {
        return strId;
    }

    public void setStrId(String strId) {
        this.strId = strId;
    }
    
    public SequenceConfig getSequenceConfig() {
        return config;
    }
    
    public String getSessionId() {
        return strId != null ? strId : getId();
    }
}


