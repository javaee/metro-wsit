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
package com.sun.xml.ws.rm.runtime.sequence;

import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.policy.Configuration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO javadoc
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public abstract class AbstractSequence implements Sequence {    
    
    protected final SequenceData data;
        
    /**
     * Initializes instance fields.
     * 
     * @param id sequence identifier
     * 
     * @param expirationTime sequence expiration time
     * 
     * @param unackedMessageIdentifiersStorage instance of a collection imlementation that should be used as a storage 
     *        for unacknowledged message identifiers on the sequence. <b>Note that the child implementation is responsible 
     *        for keeping the storage sorted! Otherwise a call to {@link #getAcknowledgedMessageIds()} may return undefined
     *        results.</b>
     */
    protected AbstractSequence(SequenceData data) {
        this.data = data;
    }

    public String getId() {
        return data.getSequenceId();
    }

    public long getNextMessageId() throws MessageNumberRolloverException {
        throw new UnsupportedOperationException(LocalizationMessages.WSRM_1101_UNSUPPORTED_INTERFACE_OPERATION_IN_IMPLEMENTATION(Sequence.class.getName()));
    }

    public List<AckRange> getAcknowledgedMessageIds() {
         if (getLastMessageId() == Sequence.UNSPECIFIED_MESSAGE_ID) {
            // nothing acknowledged yet
            return Collections.emptyList();
        } else if (data.noUnackedMessageIds()) {
            // no unacked indexes - we have a single acked range
            return Arrays.asList(new AckRange(Sequence.MIN_MESSAGE_ID, getLastMessageId()));
        } else {
            // need to calculate ranges from the unacked indexes
            List<AckRange> result = new LinkedList<Sequence.AckRange>();

            Collection<Long> unackedIndexes = data.getAllUnackedIndexes();
            
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
        return !data.noUnackedMessageIds();
    }

    public Status getStatus() {
        return data.getStatus();
    }

    protected void setStatus(Status newStatus) {
        data.setStatus(newStatus);
    }

    public void setAckRequestedFlag() {
        data.setAckRequestedFlag(true);
    }

    protected void clearAckRequestedFlag() {
        data.setAckRequestedFlag(false);        
    }
    
    public boolean isAckRequested() {
        return data.isAckRequestedFlag();
    }

    public void close() {
        data.setStatus(Status.CLOSED);
    }

    public boolean isClosed() {
        return data.getStatus() == Status.CLOSING || data.getStatus() == Status.CLOSED || data.getStatus() == Status.TERMINATING;
    }

    public boolean isExpired() {
        return (data.getExpirationTime() == Configuration.UNSPECIFIED) ? false : System.currentTimeMillis() < data.getExpirationTime();
    }

    public void preDestroy() {
        // nothing to do...
    }
}
