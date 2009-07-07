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
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateMessageRegistrationException;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateSequenceException;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

/*
DROP TABLE RM_UNACKED_MESSAGES;
DROP TABLE RM_SEQUENCES;

CREATE TABLE RM_SEQUENCES (
ID VARCHAR(256) NOT NULL,
TYPE CHARACTER NOT NULL,

EXP_TIME BIGINT NOT NULL,
BOUND_ID VARCHAR(256),
STR_ID VARCHAR(256),

STATUS SMALLINT NOT NULL,
ACK_REQUESTED_FLAG CHARACTER,
LAST_MESSAGE_ID BIGINT NOT NULL,
LAST_ACTIVITY_TIME BIGINT NOT NULL,
LAST_ACK_REQUEST_TIME BIGINT NOT NULL,

PRIMARY KEY (ID, TYPE)
);

CREATE INDEX IDX_RM_SEQUENCES_BOUND_ID ON RM_SEQUENCES (BOUND_ID);

CREATE TABLE RM_UNACKED_MESSAGES (
SEQ_ID VARCHAR(256) NOT NULL,
SEQ_TYPE CHARACTER NOT NULL,
MSG_NUMBER BIGINT NOT NULL,
IS_REGISTERED CHARACTER NOT NULL,

CORRELATION_ID VARCHAR(256),
NEXT_RESEND_COUNT INT NOT NULL,
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
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class PersistentSequenceData implements SequenceData {

    private static final class FieldInfo {

        final String columnName;
        final int sqlType;

        public FieldInfo(String columnName, int sqlType) {
            this.columnName = columnName;
            this.sqlType = sqlType;
        }
    }

    static enum SequenceType {

        Inbound('I'),
        Outbound('O');
        //
        private final char id;

        private SequenceType(char id) {
            this.id = id;
        }

        private static SequenceType toSequenceType(char id) {
            for (SequenceType st : values()) {
                if (st.id == id) {
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
    /*
    private volatile State state;
    private volatile boolean ackRequestedFlag;
    private volatile long lastMessageId;
    private volatile long lastActivityTime;
    private volatile long lastAcknowledgementRequestTime;
     */
    private final FieldInfo fState = new FieldInfo("STATUS", Types.SMALLINT);
    private final FieldInfo fAckRequestedFlag = new FieldInfo("ACK_REQUESTED_FLAG", Types.CHAR);
    private final FieldInfo fLastMessageId = new FieldInfo("LAST_MESSAGE_ID", Types.BIGINT);
    private final FieldInfo fLastActivityTime = new FieldInfo("LAST_ACTIVITY_TIME", Types.BIGINT);
    private final FieldInfo fLastAcknowledgementRequestTime = new FieldInfo("LAST_ACK_REQUEST_TIME", Types.BIGINT);
    //
    private final ResultSet dataResultSet;
    //
    private final Map<String, ApplicationMessage> weakMessageStorage;
    private final Map<Long, String> weakUnackedNumberToCorrelationIdMap;

    PersistentSequenceData(String sequenceId, SequenceType type, String securityContextTokenId, long expirationTime, ResultSet dataResultSet) {
        this.sequenceId = sequenceId;
        this.type = type;
        this.boundSecurityTokenReferenceId = securityContextTokenId;
        this.expirationTime = expirationTime;
        //
        this.dataResultSet = dataResultSet;
//        this.state = state;
//        this.ackRequestedFlag = ackRequestedFlag;
//        this.lastMessageId = lastMessageId;
//        this.lastActivityTime = lastActivityTime;
//        this.lastAcknowledgementRequestTime = lastAcknowledgementRequestTime;
        this.weakMessageStorage = new WeakHashMap<String, ApplicationMessage>();
        this.weakUnackedNumberToCorrelationIdMap = new WeakHashMap<Long, String>();
    }

    static PersistentSequenceData newInstance(
            Connection sqlConnection,
            String sequenceId,
            SequenceType type,
            String securityContextTokenId,
            long expirationTime,
            State state,
            boolean ackRequestedFlag,
            long lastMessageId,
            long lastActivityTime,
            long lastAcknowledgementRequestTime) throws DuplicateSequenceException {
        try {
            PreparedStatement ps = sqlConnection.prepareStatement(
                    "INSERT INTO RM.RM_SEQUENCES " +
                    "(ID, TYPE, EXP_TIME, STR_ID, STATUS, ACK_REQUESTED_FLAG, LAST_MESSAGE_ID, LAST_ACTIVITY_TIME, LAST_ACK_REQUEST_TIME) " +
                    "VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?)");

            int i = 0;
            ps.setString(++i, sequenceId); // ID VARCHAR(256) NOT NULL,
            ps.setInt(++i, type.id); // TYPE CHARACTER NOT NULL,

            ps.setLong(++i, expirationTime); // EXP_TIME TIMESTAMP NOT NULL,
            ps.setString(++i, securityContextTokenId); // STR_ID VARCHAR(256),


            ps.setInt(++i, state.asInt()); // STATUS SMALLINT NOT NULL,
            ps.setString(++i, Boolean.toString(ackRequestedFlag)); // ACK_REQUESTED_FLAG CHARACTER,
            ps.setLong(++i, lastMessageId); // LAST_MESSAGE_ID BIGINT NOT NULL,
            ps.setLong(++i, lastActivityTime); // LAST_ACTIVITY_TIME TIMESTAMP NOT NULL,
            ps.setLong(++i, lastAcknowledgementRequestTime); // LAST_ACK_REQUEST_TIME TIMESTAMP NOT NULL,

            if (ps.executeUpdate() != 1) {
                throw LOGGER.logSevereException(
                        new PersistenceException(String.format("Unable to insert sequence data for %s sequence with id = [ %s ]", type, sequenceId)));
            }

        } catch (SQLException ex) {
            LOGGER.logSevereException(ex);
            // TODO P1 error handling
        }

        return loadInstance(sqlConnection, sequenceId, type);
    }

    static PersistentSequenceData loadInstance(Connection sqlConnection, String sequenceId, SequenceType type) {
        PersistentSequenceData data = null;

        try {
            PreparedStatement ps = sqlConnection.prepareStatement(
                    "SELECT " +
                    "EXP_TIME, BOUND_ID, STR_ID, STATUS, ACK_REQUESTED_FLAG, LAST_MESSAGE_ID, LAST_ACTIVITY_TIME, LAST_ACK_REQUEST_TIME " +
                    "FROM RM_SEQUENCES " +
                    "WHERE ID=? AND TYPE=?");

            ps.setString(1, sequenceId);
            ps.setInt(2, type.id);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            if (!rs.isFirst() && !rs.isLast()) {
                // TODO L10N
                throw LOGGER.logSevereException(new PersistenceException(""));
            }


            data = new PersistentSequenceData(
                    sequenceId,
                    type,
                    rs.getString("STR_ID"),
                    rs.getLong("EXP_TIME"),
                    rs);

            // TODO load unacked messages

        } catch (SQLException ex) {
            LOGGER.logSevereException(ex);
            // TODO P1 error handling
        }

        return data;
    }

    static PersistentSequenceData remove(Connection sqlConnection, PersistentSequenceData data) {
        try {
            data.lockRead();

            PreparedStatement ps = sqlConnection.prepareStatement("DELETE FROM RM_SEQUENCES WHERE ID=? AND TYPE=?");

            ps.setString(1, data.sequenceId); // ID VARCHAR(256) NOT NULL,
            ps.setInt(2, data.type.id); // TYPE CHARACTER NOT NULL,

            if (ps.executeUpdate() != 1) {
                throw LOGGER.logException(
                        new PersistenceException(String.format("Unable to delete sequence data for %s sequence with id = [ %s ]", data.type, data.sequenceId)),
                        Level.WARNING);
            }

        } catch (SQLException ex) {
            LOGGER.logSevereException(ex);
            // TODO P1 error handling
        } finally {
            data.unlockRead();
        }

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
        try {
            dataResultSet.updateRow();
        } catch (SQLException ex) {
            // TODO L10N
            LOGGER.logSevereException(new PersistenceException("Unable to update database with new data.", ex));
        }
        dataLock.writeLock().unlock();
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public String getBoundSecurityTokenReferenceId() {
        return boundSecurityTokenReferenceId;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public long getLastMessageNumber() {
        try {
            return dataResultSet.getLong(fLastMessageId.columnName);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to retrieve sequence data", ex));
        }
    }

    public void setLastMessageNumber(long newValue) {
        try {
            dataResultSet.updateLong(fLastMessageId.columnName, newValue);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to update sequence data", ex));
        }
    }

    public State getState() {
        try {
            return State.asState(dataResultSet.getInt(fState.columnName));
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to retrieve sequence data", ex));
        }
    }

    public void setState(State newValue) {
        try {
            dataResultSet.updateInt(fState.columnName, newValue.asInt());
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to update sequence data", ex));
        }
    }

    public boolean getAckRequestedFlag() {
        try {
            return dataResultSet.getBoolean(fAckRequestedFlag.columnName);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to retrieve sequence data", ex));
        }
    }

    public void setAckRequestedFlag(boolean newValue) {
        try {
            dataResultSet.updateBoolean(fAckRequestedFlag.columnName, newValue);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to update sequence data", ex));
        }
    }

    public long getLastAcknowledgementRequestTime() {
        try {
            return dataResultSet.getLong(fLastAcknowledgementRequestTime.columnName);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to retrieve sequence data", ex));
        }
    }

    public void setLastAcknowledgementRequestTime(long newValue) {
        try {
            dataResultSet.updateLong(fLastAcknowledgementRequestTime.columnName, newValue);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to update sequence data", ex));
        }
    }

    public long getLastActivityTime() {
        try {
            return dataResultSet.getLong(fLastActivityTime.columnName);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to retrieve sequence data", ex));
        }
    }

    public void setLastActivityTime(long newValue) {
        try {
            dataResultSet.updateLong(fLastActivityTime.columnName, newValue);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to update sequence data", ex));
        }
    }

    public void attachMessageToUnackedMessageNumber(ApplicationMessage message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ApplicationMessage retrieveMessage(String correlationId) {
        try {
            lockRead();
            return weakMessageStorage.get(correlationId);
        } finally {
            unlockRead();
        }

    }

    public void registerUnackedMessageNumber(long messageNumber, boolean received) throws DuplicateMessageRegistrationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Long> getUnackedMessageNumbers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void markAsAcknowledged(long messageNumber) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
