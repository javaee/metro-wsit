/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.glassfish.ha.store.api.BackingStore;

/**
 * This class represents a plain sequence data POJO bean which is used as an internal
 * state holder for {@link InVmSequenceData} instances as well as for HA replication
 * sequence state replication.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
class SequenceDataPojo implements Serializable {
    static final long serialVersionUID = -5024744406713321676L;

    private transient BackingStore<String, SequenceDataPojo> backingStore;
    //
    private String sequenceId;
    private String boundSecurityTokenReferenceId;
    private long expirationTime;
    //
    private volatile State state;
    private volatile boolean ackRequestedFlag;
    private volatile long lastMessageNumber;
    private volatile long lastActivityTime;
    private volatile long lastAcknowledgementRequestTime;
    //
    private Set<Long> allUnackedMessageNumbers;
    private Set<Long> receivedUnackedMessageNumbers;
    //
    private Map<Long, String> unackedNumberToCorrelationIdMap;
    private boolean inbound;

    protected SequenceDataPojo() {
    }

    public SequenceDataPojo(String sequenceId, String boundSecurityTokenReferenceId, long expirationTime, boolean isInbound, BackingStore<String, SequenceDataPojo> bs) {
        this.sequenceId = sequenceId;
        this.boundSecurityTokenReferenceId = boundSecurityTokenReferenceId;
        this.expirationTime = expirationTime;
        this.inbound = isInbound;

        this.allUnackedMessageNumbers = new TreeSet<Long>();
        this.receivedUnackedMessageNumbers = new HashSet<Long>();
        //
        this.unackedNumberToCorrelationIdMap = new HashMap<Long, String>();
        //
        this.backingStore = bs;
    }

    public String getBoundSecurityTokenReferenceId() {
        return boundSecurityTokenReferenceId;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public boolean getAckRequestedFlag() {
        return ackRequestedFlag;
    }

    public void setAckRequestedFlag(boolean ackRequestedFlag) {
        this.ackRequestedFlag = ackRequestedFlag;
    }

    public long getLastAcknowledgementRequestTime() {
        return lastAcknowledgementRequestTime;
    }

    public void setLastAcknowledgementRequestTime(long lastAcknowledgementRequestTime) {
        this.lastAcknowledgementRequestTime = lastAcknowledgementRequestTime;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(long lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public long getLastMessageNumber() {
        return lastMessageNumber;
    }

    public void setLastMessageNumber(long lastMessageNumber) {
        this.lastMessageNumber = lastMessageNumber;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Set<Long> getAllUnackedMessageNumbers() {
        return allUnackedMessageNumbers;
    }

    public Set<Long> getReceivedUnackedMessageNumbers() {
        return receivedUnackedMessageNumbers;
    }

    public Map<Long, String> getUnackedNumberToCorrelationIdMap() {
        return unackedNumberToCorrelationIdMap;
    }

    public boolean isInbound() {
        return inbound;
    }

    public void setBackingStore(BackingStore<String, SequenceDataPojo> backingStore) {
        this.backingStore = backingStore;
    }

    public void replicate() {
        if (backingStore != null) {
            HighAvailabilityProvider.saveTo(backingStore, sequenceId, this, false);
        }
    }
}
