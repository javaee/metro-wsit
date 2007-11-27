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
 * AcknowledgementHandler.java
 *
 * @author Mike Grogan
 * Created on October 30, 2005, 9:08 AM
 *
 */
package com.sun.xml.ws.rm.protocol;

import com.sun.xml.ws.rm.InvalidMessageNumberException;
import com.sun.xml.ws.rm.RMVersion;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;

import com.sun.xml.ws.rm.localization.LocalizationMessages;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class that manages the acknowledgement of sequence messages.  Methods
 * return the values for <code>AckRequested</code> protocol requests as well as update 
 * Sequence contents based on <code>SequenceAcknowledgement</code> protocol elements.
 *
 */
public class AcknowledgementHandler {

    private static final Logger logger = Logger.getLogger(AcknowledgementHandler.class.getName());
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
            AbstractSequenceAcknowledgement element)
            throws InvalidMessageNumberException {

        synchronized (sequence) {
            List<BigInteger> nacks = null;
            if (config.getRMVersion() == RMVersion.WSRM10) {
                sequence.setBufferRemaining(((com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) element).getBufferRemaining());
                nacks = ((com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) element).getNack();
            } else {
                sequence.setBufferRemaining(((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement) element).getBufferRemaining());
                nacks = ((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement) element).getNack();
            }

            //TODO - error checking
            //either nacks or ranges must be null or protocol element is malformed.
            if (nacks != null && !nacks.isEmpty()) {
                ArrayList<Boolean> list = new ArrayList<Boolean>();
                for (int i = 1; i < sequence.getNextIndex(); i++) {
                    list.set(i, true);
                }

                for (BigInteger big : nacks) {
                    int index = (int) big.longValue();
                    list.set(index, false);
                }

                for (int i = 1; i < sequence.getNextIndex(); i++) {
                    if (list.get(i)) {
                        acknowledgeIfValid(sequence, i);
                    }
                }
            } else {

                switch (config.getRMVersion()) {
                    case WSRM10: {
                        List<com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement.AcknowledgementRange> ranges =
                                ((com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) element).getAcknowledgementRange();
                        for (com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement.AcknowledgementRange range : ranges) {
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
                        break;
                    }
                    case WSRM11: {
                            List<com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement.AcknowledgementRange> ranges =
                                    ((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement) element).getAcknowledgementRange();
                            for (com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement.AcknowledgementRange range : ranges) {
                                int lower = range.getLower().intValue();
                                int upper = range.getUpper().intValue();
                                for (int i = lower; i <= upper; i++) {
                                    acknowledgeIfValid(sequence, i);
                                }
                            }
                        }
                        break;
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
            if (seq.get(i) != null) {
                seq.acknowledge(i);
            }
        } catch (InvalidMessageNumberException e) {
            //this can happen if the sequence has been resurrected
            //after a restart.
            logger.fine(LocalizationMessages.WSRM_4001_ACKNOWLEDGEMENT_MESSAGE(seq.getId(), i));
        }
    }
}
