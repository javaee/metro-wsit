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

import com.sun.xml.ws.rm.MessageNumberRolloverException;
import com.sun.xml.ws.rm.localization.RmLogger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Inbound sequence optimized for low memory footprint, fast message acknowledgement and ack range calculation optimized 
 * for standard scenario (no lost messages). This class is not reentrant.
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class InboundSequence extends AbstractSequence {

    private static final RmLogger LOGGER = RmLogger.getLogger(InboundSequence.class);
    private static final long UNSPECIFIED = 0;
    
    private long highestIdIndex = UNSPECIFIED;
    private final Set<Long> unackedIndexes;

    public InboundSequence(String id, long expirationTime) {
        super(id, expirationTime);
        this.unackedIndexes = new TreeSet<Long>();
    }

    public long getNextMessageId() throws MessageNumberRolloverException {
        // TODO L10N
        throw new UnsupportedOperationException("This operation is not supported in this Sequence implementation.");
    }

    public long getLastMessageId() {
        // TODO L10N
        throw new UnsupportedOperationException("This operation is not supported in this Sequence implementation.");
    }

    public Collection<AckRange> getAcknowledgedMessageIds() {
        if (highestIdIndex == UNSPECIFIED) {
            // nothing acknowledged yet
            return Collections.emptyList();
        } else if (unackedIndexes.isEmpty()) {
            // no unacked indexes - we have a single acked range
            return Arrays.asList(new AckRange(Sequence.MIN_MESSAGE_ID, highestIdIndex));
        } else {
            // need to calculate ranges from the unacked indexes
            Collection<AckRange> result = new LinkedList<Sequence.AckRange>();

            long lastUnacked = unackedIndexes.iterator().next();
            if (lastUnacked > Sequence.MIN_MESSAGE_ID) {
                result.add(new AckRange(Sequence.MIN_MESSAGE_ID, lastUnacked - 1));
            }
            for (long unackedIndex : unackedIndexes) {
                if (unackedIndex > lastUnacked + 1) {
                    result.add(new AckRange(lastUnacked + 1, unackedIndex - 1));
                }
                lastUnacked = unackedIndex;
            }

            return result;
        }
    }

    public boolean hasPendingAcknowledgements() {
        return !unackedIndexes.isEmpty();
    }

    public void acknowledgeMessageId(long messageIdentifier) throws IllegalMessageIdentifierException {
        if (messageIdentifier > highestIdIndex) {
            // new message - note that this will work even for the first message that arrives
            if (highestIdIndex + 1 != messageIdentifier) {
                // some message(s) got lost...
                for (long lostIndex = highestIdIndex + 1; lostIndex < messageIdentifier; lostIndex++) {
                    unackedIndexes.add(lostIndex);
                }
            }
            highestIdIndex = messageIdentifier;
        } else if (highestIdIndex == messageIdentifier) {
            // resent message
            if (unackedIndexes.contains(messageIdentifier)) {
                // lost message arrived
                unackedIndexes.remove(messageIdentifier);
            } else {
                // duplicate message
                throw LOGGER.logSevereException(new IllegalMessageIdentifierException(messageIdentifier));
            }
        }
    }
}
