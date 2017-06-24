/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.oracle.webservices.oracle_internal_api.rm.OutboundDelivered;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.protocol.AcknowledgementData;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.AckRange;

/**
 * Handles OutboundDelivered object that comes with the application 
 * request message at the ClientTube. Provides methods that store,
 * retrieve and remove OutboundDelivered to/from a Map.
 * 
 * @author Uday Joshi <uday.joshi at oracle.com>
 */
class OutboundDeliveredHandler {
    private static final Logger LOGGER = Logger.getLogger(OutboundDeliveredHandler.class);

    /*
     * Key class for the Map.
     */
    private static class MessageInfo {
        private final String sequenceId;
        private final long messageNumber;

        private MessageInfo(String seqId, long msgNumber) {
            this.sequenceId = seqId;
            this.messageNumber = msgNumber;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + (int) (messageNumber ^ (messageNumber >>> 32));
            result = prime * result
                    + ((sequenceId == null) ? 0 : sequenceId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MessageInfo other = (MessageInfo) obj;
            if (messageNumber != other.messageNumber)
                return false;
            if (sequenceId == null) {
                if (other.sequenceId != null)
                    return false;
            } else if (!sequenceId.equals(other.sequenceId))
                return false;
            return true;
        }
    }

    private ConcurrentHashMap<MessageInfo, OutboundDelivered> map =
            new ConcurrentHashMap<MessageInfo, OutboundDelivered> ();

    /**
     * Store OutboundDelivered in a map for later retrieval
     * (keyed by sequenceId+messageNumber).
     * @param sequenceId Sequence ID
     * @param messageNumber Message number
     * @param outboundDelivered OutboundDelivered to put away
     */
    void store(String sequenceId, long messageNumber, OutboundDelivered outboundDelivered) {
        MessageInfo messageInfo = new MessageInfo(sequenceId, messageNumber);
        map.put(messageInfo, outboundDelivered);
    }

    /**
     * Retrieve OutboundDelivered that was put away in store.
     * @param sequenceId Sequence ID
     * @param messageNumber Message number
     * @return OutboundDelivered that was put away before using add
     */
    OutboundDelivered retrieve(String sequenceId, long messageNumber) {
        MessageInfo messageInfo = new MessageInfo(sequenceId, messageNumber);
        OutboundDelivered result = map.get(messageInfo);
        return result;
    }

    /**
     * Remove OutboundDelivered once it is used so that it is not
     * found again for use.
     * @param sequenceId Sequence ID
     * @param messageNumber Message number
     */
    void remove(String sequenceId, long messageNumber) {
        MessageInfo messageInfo = new MessageInfo(sequenceId, messageNumber);
        map.remove(messageInfo);
    }

    /**
     * Looks at all the ack'ed message numbers for the sequence and
     * invokes OutboundDelivered.setDelivered(true) as appropriate.
     * @param acknowledgementData AcknowledgementData to be processed
     */
    void processAcknowledgements(AcknowledgementData acknowledgementData) {
        String seqId = acknowledgementData.getAcknowledgedSequenceId();
        final List<AckRange> listOfAckRange = acknowledgementData.getAcknowledgedRanges();
        for (AckRange ackRange : listOfAckRange) {
            List<Long> messageNumbers = ackRange.rangeValues();
            for(long messageNumber : messageNumbers) {
                OutboundDelivered outboundDelivered = retrieve(seqId, messageNumber);
                if (outboundDelivered != null) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Invoking outboundDelivered.setDelivered(true) for " +
                                "seq id:"+seqId+" and " +
                                "message number:"+messageNumber);
                    }
                    outboundDelivered.setDelivered(Boolean.TRUE);
                    remove(seqId, messageNumber);
                }
            }
        }
    }
}
