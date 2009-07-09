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

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.JaxwsApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateMessageRegistrationException;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateSequenceException;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/*
DROP TABLE RM_UNACKED_MESSAGES;
DROP TABLE RM_SEQUENCES;

CREATE TABLE RM_SEQUENCES (
ENDPOINT_UID VARCHAR(512) NOT NULL,
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

PRIMARY KEY (ENDPOINT_UID, ID)
);

CREATE INDEX IDX_RM_SEQUENCES_BOUND_ID ON RM_SEQUENCES (BOUND_ID);

CREATE TABLE RM_UNACKED_MESSAGES (
ENDPOINT_UID VARCHAR(512) NOT NULL,
SEQ_ID VARCHAR(256) NOT NULL,
MSG_NUMBER BIGINT NOT NULL,
IS_RECEIVED CHARACTER NOT NULL,

CORRELATION_ID VARCHAR(256),
NEXT_RESEND_COUNT INT,
MSG_DATA BLOB,

PRIMARY KEY (ENDPOINT_UID, SEQ_ID, MSG_NUMBER)
);

ALTER TABLE RM_UNACKED_MESSAGES
ADD CONSTRAINT FK_SEQUENCE
FOREIGN KEY (ENDPOINT_UID, SEQ_ID) REFERENCES RM_SEQUENCES(ENDPOINT_UID, ID);

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

        Inbound("I"),
        Outbound("O");
        //
        private final String  id;

        private SequenceType(String id) {
            this.id = id;
        }

        private static SequenceType fromId(String id) {
            for (SequenceType type : values()) {
                if (type.id.equals(id)) {
                    return type;
                }
            }

            return null;
        }
    }
    //
    private static final Logger LOGGER = Logger.getLogger(PersistentSequenceData.class);
    //
    private final String endpointUid;
    private final String sequenceId;
    private final SequenceType type;
    private final String boundSecurityTokenReferenceId;
    private final long expirationTime;
    //
    private final FieldInfo fState = new FieldInfo("STATUS", Types.SMALLINT);
    private final FieldInfo fAckRequestedFlag = new FieldInfo("ACK_REQUESTED_FLAG", Types.CHAR);
    private final FieldInfo fLastMessageId = new FieldInfo("LAST_MESSAGE_ID", Types.BIGINT);
    private final FieldInfo fLastActivityTime = new FieldInfo("LAST_ACTIVITY_TIME", Types.BIGINT);
    private final FieldInfo fLastAcknowledgementRequestTime = new FieldInfo("LAST_ACK_REQUEST_TIME", Types.BIGINT);
    //
    private final Connection sqlConnection;
    private final ResultSet sequenceDataResultSet;

    PersistentSequenceData(Connection sqlConnection, String endpointUid, String sequenceId, SequenceType type, String securityContextTokenId, long expirationTime, ResultSet dataResultSet) {
        this.sqlConnection = sqlConnection;

        this.endpointUid = endpointUid;
        this.sequenceId = sequenceId;
        this.type = type;
        this.boundSecurityTokenReferenceId = securityContextTokenId;
        this.expirationTime = expirationTime;
        //
        this.sequenceDataResultSet = dataResultSet;
    }

    static PersistentSequenceData newInstance(
            Connection sqlConnection,
            String enpointUid,
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
                    "(ENDPOINT_UID, ID, TYPE, EXP_TIME, STR_ID, STATUS, ACK_REQUESTED_FLAG, LAST_MESSAGE_ID, LAST_ACTIVITY_TIME, LAST_ACK_REQUEST_TIME) " +
                    "VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            int i = 0;
            ps.setString(++i, enpointUid); // ENDPOINT_UID VARCHAR(256) NOT NULL,
            ps.setString(++i, sequenceId); // ID VARCHAR(256) NOT NULL,
            ps.setString(++i, type.id); // TYPE CHARACTER NOT NULL,

            ps.setLong(++i, expirationTime); // EXP_TIME TIMESTAMP NOT NULL,
            ps.setString(++i, securityContextTokenId); // STR_ID VARCHAR(256),


            ps.setInt(++i, state.asInt()); // STATUS SMALLINT NOT NULL,
            ps.setString(++i, Boolean.toString(ackRequestedFlag)); // ACK_REQUESTED_FLAG CHARACTER,
            ps.setLong(++i, lastMessageId); // LAST_MESSAGE_ID BIGINT NOT NULL,
            ps.setLong(++i, lastActivityTime); // LAST_ACTIVITY_TIME TIMESTAMP NOT NULL,
            ps.setLong(++i, lastAcknowledgementRequestTime); // LAST_ACK_REQUEST_TIME TIMESTAMP NOT NULL,

            if (ps.executeUpdate() != 1) {
                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Unable to insert sequence data for %s sequence with id = [ %s ]",
                        type,
                        sequenceId)));
            }

        } catch (SQLException ex) {
            // TODO L10N
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "An exception occured while storing new sequence data for %s sequence with id = [ %s ]",
                    type,
                    sequenceId), ex));
        }

        return loadInstance(sqlConnection, enpointUid, sequenceId);
    }

    static PersistentSequenceData loadInstance(Connection sqlConnection, String endpointUid, String sequenceId) {
        PersistentSequenceData data = null;

        try {
            PreparedStatement ps = sqlConnection.prepareStatement(
                    "SELECT " +
                    "TYPE, EXP_TIME, BOUND_ID, STR_ID, STATUS, ACK_REQUESTED_FLAG, LAST_MESSAGE_ID, LAST_ACTIVITY_TIME, LAST_ACK_REQUEST_TIME " +
                    "FROM RM_SEQUENCES " +
                    "WHERE ENDPOINT_UID=? AND ID=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            if (!rs.isFirst() && !rs.isLast()) {
                // TODO L10N
                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Duplicate sequence records detected for a sequence with id [ %s ]", sequenceId)));
            }


            data = new PersistentSequenceData(
                    sqlConnection,
                    endpointUid,
                    sequenceId,
                    SequenceType.fromId(rs.getString("TYPE")),
                    rs.getString("STR_ID"),
                    rs.getLong("EXP_TIME"),
                    rs);

        } catch (SQLException ex) {
            // TODO L10N
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "An exception occured while loading sequence data for a sequence with id = [ %s ]",
                    sequenceId), ex));
        }

        return data;
    }

    static PersistentSequenceData remove(Connection sqlConnection, PersistentSequenceData data) {
        try {
            data.lockRead();

            remove(sqlConnection, data.endpointUid, data.sequenceId);
        } finally {
            data.unlockRead();
        }
        return data;
    }

    static void remove(Connection sqlConnection, String endpointUid, String sequenceId) {
        try {
            PreparedStatement ps = sqlConnection.prepareStatement("DELETE FROM RM_SEQUENCES WHERE ENDPOINT_UID=? AND ID=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);

            final int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw LOGGER.logException(
                        new PersistenceException(String.format("Removing sequence with id = [ %s ] failed: Expected deleted rows: 1, Actual: %d", sequenceId, rowsAffected)),
                        Level.WARNING);
            }
            
            // TODO clear bound column where needed, clear unacknowledged data

        } catch (SQLException ex) {
            // TODO L10N
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "An exception occured while deleting sequence data for a sequence with id = [ %s ]",
                    sequenceId), ex));
        }
    }

    static void bind(Connection sqlConnection, String endpointUid, String referenceSequenceId, String boundSequenceId) {
        // TODO implment & handle mutliple bindings (unbind old binding first)
    }

    public void lockRead() {
        // TODO P1
    }

    public void unlockRead() {
        // TODO P1
    }

    public void lockWrite() {
        // TODO P1
    }

    public void unlockWrite() {
        // TODO P1
        try {
            sequenceDataResultSet.updateRow();
        } catch (SQLException ex) {
            // TODO L10N
            LOGGER.logSevereException(new PersistenceException("Unable to update database with new data.", ex));
        }
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public SequenceType getType() {
        return type;
    }

    public String getBoundSecurityTokenReferenceId() {
        return boundSecurityTokenReferenceId;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public long getLastMessageNumber() {
        try {
            return sequenceDataResultSet.getLong(fLastMessageId.columnName);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to retrieve sequence data", ex));
        }
    }

    public void setLastMessageNumber(long newValue) {
        try {
            sequenceDataResultSet.updateLong(fLastMessageId.columnName, newValue);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to update sequence data", ex));
        }
    }

    public State getState() {
        try {
            return State.asState(sequenceDataResultSet.getInt(fState.columnName));
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to retrieve sequence data", ex));
        }
    }

    public void setState(State newValue) {
        try {
            sequenceDataResultSet.updateInt(fState.columnName, newValue.asInt());
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to update sequence data", ex));
        }
    }

    public boolean getAckRequestedFlag() {
        try {
            return sequenceDataResultSet.getBoolean(fAckRequestedFlag.columnName);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to retrieve sequence data", ex));
        }
    }

    public void setAckRequestedFlag(boolean newValue) {
        try {
            sequenceDataResultSet.updateBoolean(fAckRequestedFlag.columnName, newValue);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to update sequence data", ex));
        }
    }

    public long getLastAcknowledgementRequestTime() {
        try {
            return sequenceDataResultSet.getLong(fLastAcknowledgementRequestTime.columnName);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to retrieve sequence data", ex));
        }
    }

    public void setLastAcknowledgementRequestTime(long newValue) {
        try {
            sequenceDataResultSet.updateLong(fLastAcknowledgementRequestTime.columnName, newValue);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to update sequence data", ex));
        }
    }

    public long getLastActivityTime() {
        try {
            return sequenceDataResultSet.getLong(fLastActivityTime.columnName);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to retrieve sequence data", ex));
        }
    }

    public void setLastActivityTime(long newValue) {
        try {
            sequenceDataResultSet.updateLong(fLastActivityTime.columnName, newValue);
        } catch (SQLException ex) {
            // TODO P3 L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to update sequence data", ex));
        }
    }

    public void registerUnackedMessageNumber(long messageNumber, boolean received) throws DuplicateMessageRegistrationException {
        try {
            PreparedStatement ps = sqlConnection.prepareStatement("INSERT INTO RM_UNACKED_MESSAGES " +
                    "(ENDPOINT_UID, SEQ_ID, MSG_NUMBER, IS_RECEIVED) " +
                    "VALUES (?, ?, ?, ?)");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);
            ps.setLong(3, messageNumber);
            ps.setString(4, Boolean.toString(received));

            if (ps.executeUpdate() != 1) {
                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Unable to insert new unacked message registration for %s sequence with id = [ %s ] and message number [ %d ]",
                        type,
                        sequenceId,
                        messageNumber)));
            }

        } catch (SQLException ex) {
            LOGGER.logSevereException(ex);
            // TODO P1 error handling
        }
    }

    public void markAsAcknowledged(long messageNumber) {
        try {
            PreparedStatement ps = sqlConnection.prepareStatement(
                    "DELETE FROM RM_UNACKED_MESSAGES " +
                    "WHERE ENDPOINT_UID=? AND SEQ_ID=? AND MSG_NUMBER=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);
            ps.setLong(4, messageNumber);

            final int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Message acknowledgement failed for %s sequence with id = [ %s ] and message number [ %d ]: Expected deleted rows: 1, Actual: %d",
                        type,
                        sequenceId,
                        messageNumber,
                        rowsAffected)));
            }

        } catch (SQLException ex) {
            LOGGER.logSevereException(ex);
            // TODO P1 error handling
        }
    }

    public Collection<Long> getUnackedMessageNumbers() {
        try {
            PreparedStatement ps = sqlConnection.prepareStatement("SELECT MSG_NUMBER FROM RM_UNACKED_MESSAGES " +
                    "WHERE ENDPOINT_UID=? AND SEQ_ID=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);

            ResultSet rs = ps.executeQuery();

            List<Long> result = new LinkedList<Long>();
            while (rs.next()) {
                result.add(rs.getLong("MSG_NUMBER"));
            }

            return result;
        } catch (SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Unable to load list of unacked message registration for %s sequence with id = [ %s ]",
                    type,
                    sequenceId), ex));
        }
    }

    public void attachMessageToUnackedMessageNumber(ApplicationMessage message) {
        ByteArrayInputStream bais = null;
        try {
            PreparedStatement ps = sqlConnection.prepareStatement("UPDATE RM_UNACKED_MESSAGES SET " +
                    "IS_RECEIVED=?, CORRELATION_ID=?, NEXT_RESEND_COUNT=?, MSG_DATA=? " +
                    "WHERE ENDPOINT_UID=? AND SEQ_ID=? AND MSG_NUMBER=?");

            int i = 0;

            ps.setString(++i, Boolean.TRUE.toString());

            ps.setString(++i, message.getCorrelationId());
            ps.setLong(++i, message.getNextResendCount());

            final byte[] msgData = message.toBytes();
            bais = new ByteArrayInputStream(msgData);
            ps.setBinaryStream(++i, bais, msgData.length);

            ps.setString(++i, endpointUid);
            ps.setString(++i, sequenceId);
            ps.setLong(++i, message.getMessageNumber());

            final int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Storing message data in an unacked message registration for %s sequence with id = [ %s ] and message number [ %d ] has failed: Expected updated rows: 1, Actual: %d",
                        type,
                        sequenceId,
                        message.getMessageNumber(),
                        rowsAffected)));
            }

        } catch (SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Unable to store message data in an unacked message registration for %s sequence with id = [ %s ] and message number [ %d ]",
                    type,
                    sequenceId,
                    message.getMessageNumber()), ex));
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch (IOException ex) {
                    LOGGER.warning("Error closing ByteArrayOutputStream after message bytes were sent to DB", ex);
                }
            }
        }
    }

    public ApplicationMessage retrieveMessage(String correlationId) {
        ByteArrayInputStream bais = null;
        try {
            PreparedStatement ps = sqlConnection.prepareStatement(
                    "SELECT MSG_NUMBER, NEXT_RESEND_COUNT, MSG_DATA FROM RM_UNACKED_MESSAGES " +
                    "WHERE ENDPOINT_UID=? AND SEQ_ID=? AND CORRELATION_ID=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);
            ps.setString(3, correlationId);


            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            if (!rs.isFirst() && !rs.isLast()) {
                // TODO L10N
                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Duplicate records detected for unacked message registration on %s sequence with id = [ %s ] and correlation id [ %d ]",
                        type,
                        sequenceId,
                        correlationId)));
            }


            return JaxwsApplicationMessage.newInstance(
                    rs.getBlob("MSG_DATA").getBinaryStream(),
                    rs.getInt("NEXT_RESEND_COUNT"),
                    correlationId,
                    sequenceId,
                    rs.getLong("MSG_NUMBER"));

        } catch (SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Unable to load message data from an unacked message registration for %s sequence with id = [ %s ] and correlation id [ %d ]",
                    type,
                    sequenceId,
                    correlationId), ex));
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch (IOException ex) {
                    LOGGER.warning("Error closing ByteArrayOutputStream after message bytes were sent to DB", ex);
                }
            }
        }
    }
}
