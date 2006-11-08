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
 * AcknowledgementHandler.java
 *
 * @author Mike Grogan
 * Created on October 30, 2005, 9:08 AM
 *
 */

package com.sun.xml.ws.rm.protocol;
import com.sun.xml.ws.rm.Message;
import com.sun.xml.ws.rm.Sequence;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import com.sun.xml.ws.rm.InvalidMessageNumberException;



/**
 * Utility class that manages the acknowledgement of sequence messages.  Methods
 * return the values for <code>AckRequested</code> protocol requests as well as update 
 * Sequence contents based on <code>SequenceAcknowledgement</code> protocol elements.
 *
 */
public class AcknowledgementHandler {

    private static final Logger logger = 
            Logger.getLogger(AcknowledgementHandler.class.getName());
    
    /**
     * Configuration for this sequence.
     */
    protected SequenceConfig config;


    public AcknowledgementHandler(SequenceConfig seqConfig) {
        this.config = seqConfig;
    }

    
    /**
     * Mark the messages in the sequence delivered according to the contents
     * of the specified <code>SequenceAcknowledgement</code> element.
     *
     * @param element The <code>SequenceAcknowledgementElement</code>
     */
    public void handleAcknowledgement(OutboundSequence sequence, 
                        SequenceAcknowledgementElement element) 
                     throws InvalidMessageNumberException {
        
        synchronized (sequence) {
            
            sequence.setBufferRemaining(element.getBufferRemaining());
            
            List<SequenceAcknowledgementElement.AcknowledgementRange> ranges =
                    element.getAcknowledgementRange();

            List<BigInteger> nacks = element.getNack();

            //TODO - error checking
            //either nacks or ranges must be null or protocol element is malformed.
            if (nacks != null && !nacks.isEmpty()) {

                int size = sequence.getNextIndex() + 1;
                ArrayList<Boolean> list = new ArrayList<Boolean>();
                for (int i = 1; i < sequence.getNextIndex(); i++) {
                    list.set(i, true);
                }

                for (BigInteger big : nacks) {
                    int index = (int)big.longValue();
                    list.set(index, false);
                }

                for (int i = 1; i < sequence.getNextIndex();i++) {
                    Message mess = sequence.get(i);
                    if (list.get(i)) {
                        acknowledgeIfValid(sequence, i);
                    }
                }     
            } else {
                for (SequenceAcknowledgementElement.AcknowledgementRange range : ranges) {

                    int lower = range.getLower().intValue();
                    int upper = range.getUpper().intValue();
                    
                    //if a SequenceHeader with Last elemet has been sent, we may
                    //receive acks for that "Message" although one was never stored
                    //at the index.
                    if (sequence.isLast() && upper == sequence.getNextIndex()) {
                        upper--;
                    }

                    for (int i = lower; i <= upper; i++) {
                        acknowledgeIfValid(sequence, i);
                    }
                }
            } 
        }
    }
    
    /**
     * We may receive an ack for an unknown message if we are restarting
     * after a crash or if the RMD is broken. Allow processing to continue
     * after logging.
     * 
     */
    private void acknowledgeIfValid(OutboundSequence seq, int i) {
        try {
            Message mess = seq.get(i);
            if (mess != null) {
                seq.acknowledge(i);
            }
        } catch (InvalidMessageNumberException e) {
            //this can happen if the sequence has been resurrected
            //after a restart.
            logger.fine("Received acknowledgement of unknown message.  " +
                        "Sequence = " + seq.getId() +
                        "MessageNumber = " + i);
        }
    }
    
}
