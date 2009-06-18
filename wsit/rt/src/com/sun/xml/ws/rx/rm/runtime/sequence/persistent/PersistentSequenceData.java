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
package com.sun.xml.ws.rx.rm.runtime.sequence.persistent;

import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateSequenceException;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
DROP TABLE RM_UNACKED_MESSAGES;
DROP TABLE RM_SEQUENCES;

CREATE TABLE RM_SEQUENCES (
ID VARCHAR(256) NOT NULL,
TYPE CHARACTER NOT NULL,

EXP_TIME TIMESTAMP NOT NULL,
BOUND_ID VARCHAR(256),
STR_ID VARCHAR(256),

STATUS CHARACTER NOT NULL,
ACK_REQUESTED_FLAG CHARACTER,
LAST_MESSAGE_ID BIGINT NOT NULL,
LAST_ACTIVITY_TIME TIMESTAMP NOT NULL,
LAST_ACK_REQUEST_TIME TIMESTAMP NOT NULL,

PRIMARY KEY (ID, TYPE)
);

CREATE INDEX IDX_RM_SEQUENCES_BOUND_ID ON RM_SEQUENCES (BOUND_ID);

CREATE TABLE RM_UNACKED_MESSAGES (
SEQ_ID VARCHAR(256) NOT NULL,
SEQ_TYPE CHARACTER NOT NULL,
MSG_NUMBER BIGINT NOT NULL,
IS_REGISTERED CHARACTER NOT NULL,

CORRELATION_ID VARCHAR(256),
NEXT_RESEND_COUNT INT,
MSG_DATA BLOB,

PRIMARY KEY (SEQ_ID, SEQ_TYPE, MSG_NUMBER)
);

ALTER TABLE RM_UNACKED_MESSAGES
ADD CONSTRAINT FK_SEQUENCE
FOREIGN KEY (SEQ_ID, SEQ_TYPE) REFERENCES RM_SEQUENCES(ID, TYPE);

CREATE INDEX IDX_RM_UNACKED_MESSAGES_CORRELATION_ID ON RM_UNACKED_MESSAGES (CORRELATION_ID);
 */
/**
 * Persistent implementation of sequence data
 *
 * TODO implement - currently only works as a copy of in-vm sequence data
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class PersistentSequenceData implements SequenceData {

    static enum SequenceType {

        INBOUND("IN"),
        OUTBOUND("OUT");
        private final String id;

        private SequenceType(String id) {
            this.id = id;
        }

        private static SequenceType toSequenceType(String id) {
            for (SequenceType st : values()) {
                if (st.id.equals(id)) {
                    return st;
                }
            }

            return null;
        }
    }
    private static final Logger LOGGER = Logger.getLogger(PersistentSequenceData.class);
    // lock used to synchronize the access to the mutable variables
    private final ReadWriteLock dataLock = new ReentrantReadWriteLock();
    //
    private final String sequenceId;
    private final SequenceType type;
    private final String boundSecurityTokenReferenceId;
    private final long expirationTime;
    //
    private volatile State state;
    private volatile boolean ackRequestedFlag;
    private volatile long lastMessageId;
    private volatile long lastActivityTime;
    private volatile long lastAcknowledgementRequestTime;
    //
    private final Map<String, ApplicationMessage> weakMessageStorage;
    private final Map<Long, String> weakUnackedNumberToCorrelationIdMap;

    PersistentSequenceData(String sequenceId, SequenceType type, String securityContextTokenId, long expirationTime, long lastMessageId, long lastActivityTime) {
        this(sequenceId, type, securityContextTokenId, expirationTime, State.CREATED, false, lastMessageId, lastActivityTime, 0L);
    }

    PersistentSequenceData(String sequenceId, SequenceType type, String securityContextTokenId, long expirationTime, State state, boolean ackRequestedFlag, long lastMessageId, long lastActivityTime, long lastAcknowledgementRequestTime) {
        super();

        this.sequenceId = sequenceId;
        this.type = type;
        this.boundSecurityTokenReferenceId = securityContextTokenId;
        this.expirationTime = expirationTime;
        this.state = state;
        this.ackRequestedFlag = ackRequestedFlag;
        this.lastMessageId = lastMessageId;
        this.lastActivityTime = lastActivityTime;
        this.lastAcknowledgementRequestTime = lastAcknowledgementRequestTime;
        this.weakMessageStorage = new WeakHashMap<String, ApplicationMessage>();
        this.weakUnackedNumberToCorrelationIdMap = new WeakHashMap<Long, String>();
    }

    static PersistentSequenceData insert(Connection sqlConnection, PersistentSequenceData data) throws DuplicateSequenceException {
        try {
            data.lockRead();

            PreparedStatement ps = sqlConnection.prepareStatement("INSERT INTO RM_SEQUENCES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            int i = 0;
            ps.setString(++i, data.sequenceId); // ID VARCHAR(256) NOT NULL,
            ps.setString(++i, data.type.id); // TYPE CHARACTER NOT NULL,

            ps.setTimestamp(++i, new Timestamp(data.expirationTime)); // EXP_TIME TIMESTAMP NOT NULL,
            ps.setNull(++i, Types.CHAR); // BOUND_ID VARCHAR(256),
            ps.setString(++i, data.boundSecurityTokenReferenceId); // STR_ID VARCHAR(256),


            ps.setString(++i, data.state.name()); // STATUS CHARACTER NOT NULL,
            ps.setString(++i, Boolean.toString(data.ackRequestedFlag)); // ACK_REQUESTED_FLAG CHARACTER,
            ps.setLong(++i, data.lastMessageId); // LAST_MESSAGE_ID BIGINT NOT NULL,
            ps.setTimestamp(++i, new Timestamp(data.lastActivityTime)); // LAST_ACTIVITY_TIME TIMESTAMP NOT NULL,
            ps.setTimestamp(++i, new Timestamp(data.lastAcknowledgementRequestTime)); // LAST_ACK_REQUEST_TIME TIMESTAMP NOT NULL,

            if (ps.executeUpdate() != 1) {
                // TODO P1 error inserting new row
            }

        } catch (SQLException ex) {
            LOGGER.logSevereException(ex);
            // TODO P1 error handling
        } finally {
            data.unlockRead();
        }

        return data;
    }

    static PersistentSequenceData save(Connection sqlConnection, PersistentSequenceData data) {
        try {
            data.lockRead();

            // TODO implement

            return data;
        } finally {
            data.unlockRead();
        }
    }

    static PersistentSequenceData load(Connection sqlConnection, String sequenceId, SequenceType type) {
        PersistentSequenceData data = null;

        // TODO implement

        return data;
    }

    static PersistentSequenceData remove(Connection sqlConnection, PersistentSequenceData data) {
        // TODO implement        

        return data;
    }

    public void lockRead() {
        dataLock.readLock().lock();
    }

    public void unlockRead() {
        dataLock.readLock().unlock();
    }

    public void lockWrite() {
        dataLock.writeLock().lock();
    }

    public void unlockWrite() {
        dataLock.writeLock().unlock();
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public String getBoundSecurityTokenReferenceId() {
        return boundSecurityTokenReferenceId;
    }

    public long getLastMessageId() {
        try {
            lockRead();
            return lastMessageId;
        } finally {
            unlockRead();
        }
    }

    public void setLastMessageId(long newLastMessageId) {
        try {
            lockWrite();
            this.lastMessageId = newLastMessageId;
        } finally {
            unlockWrite();
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State newState) {
        state = newState;
    }

    public boolean getAckRequestedFlag() {
        return ackRequestedFlag;
    }

    public void setAckRequestedFlag(boolean newValue) {
        ackRequestedFlag = newValue;
    }

    public long getLastAcknowledgementRequestTime() {
        return lastAcknowledgementRequestTime;
    }

    public void setLastAcknowledgementRequestTime(long newTime) {
        lastAcknowledgementRequestTime = newTime;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(long newTime) {
        lastActivityTime = newTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public final void storeMessage(ApplicationMessage message, Long msgNumberKey) throws UnsupportedOperationException {
        assert msgNumberKey != null;
        try {
            lockWrite();
            // NOTE this must be a new String object
            String correlationKey = new String(message.getCorrelationId());
            weakUnackedNumberToCorrelationIdMap.put(msgNumberKey, correlationKey);
            weakMessageStorage.put(correlationKey, message);
        } finally {
            unlockWrite();
        }
    }

    public ApplicationMessage retrieveMessage(String correlationId) {
        try {
            lockRead();
            return weakMessageStorage.get(correlationId);
        } finally {
            unlockRead();
        }
    }

    public ApplicationMessage retrieveUnackedMessage(long messageNumber) {
        try {
            lockRead();
            String correlationKey = weakUnackedNumberToCorrelationIdMap.get(messageNumber);
            return (correlationKey != null) ? weakMessageStorage.get(correlationKey) : null;
        } finally {
            unlockRead();
        }
    }
}
