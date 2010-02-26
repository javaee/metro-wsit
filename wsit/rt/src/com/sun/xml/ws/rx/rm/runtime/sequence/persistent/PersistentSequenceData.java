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
import com.sun.xml.ws.rx.util.TimeSynchronizer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
LAST_MESSAGE_NUMBER BIGINT NOT NULL,
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
WSA_ACTION VARCHAR(256),
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

    private static final class FieldInfo<T> {

        final String columnName;
        final int sqlType;
        final Class<T> javaClass;

        public FieldInfo(String columnName, int sqlType, Class<T> javaClass) {
            this.columnName = columnName;
            this.sqlType = sqlType;
            this.javaClass = javaClass;
        }
    }

    static enum SequenceType {

        Inbound("I"),
        Outbound("O");
        //
        private final String id;

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

    private static String b2s(boolean value) {
        return (value) ? "T" : "F";
    }

    private static boolean s2b(String string) {
        return "T".equals(string);
    }
    //
    private final String endpointUid;
    private final String sequenceId;
    private final SequenceType type;
    private final String boundSecurityTokenReferenceId;
    private final String boundSequenceId;
    private final long expirationTime;
    //
    private final FieldInfo<Integer> fState = new FieldInfo<Integer>("STATUS", Types.SMALLINT, Integer.class);
    private final FieldInfo<String> fAckRequestedFlag = new FieldInfo<String>("ACK_REQUESTED_FLAG", Types.CHAR, String.class);
    private final FieldInfo<Long> fLastMessageNumber = new FieldInfo<Long>("LAST_MESSAGE_NUMBER", Types.BIGINT, Long.class);
    private final FieldInfo<Long> fLastActivityTime = new FieldInfo<Long>("LAST_ACTIVITY_TIME", Types.BIGINT, Long.class);
    private final FieldInfo<Long> fLastAcknowledgementRequestTime = new FieldInfo<Long>("LAST_ACK_REQUEST_TIME", Types.BIGINT, Long.class);
    //
    private final ConnectionManager cm;
    private final TimeSynchronizer ts;

    private PersistentSequenceData(TimeSynchronizer ts, ConnectionManager cm, String endpointUid, String sequenceId, SequenceType type, String securityContextTokenId, String boundId, long expirationTime) {
        this.ts = ts;
        this.cm = cm;

        this.endpointUid = endpointUid;
        this.sequenceId = sequenceId;
        this.type = type;
        this.boundSecurityTokenReferenceId = securityContextTokenId;
        this.boundSequenceId = boundId;
        this.expirationTime = expirationTime;
    }

    static PersistentSequenceData newInstance(
            TimeSynchronizer ts,
            ConnectionManager cm,
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

        Connection con = cm.getConnection(false);
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "INSERT INTO RM_SEQUENCES " +
                    "(ENDPOINT_UID, ID, TYPE, EXP_TIME, STR_ID, STATUS, ACK_REQUESTED_FLAG, LAST_MESSAGE_NUMBER, LAST_ACTIVITY_TIME, LAST_ACK_REQUEST_TIME) " +
                    "VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            int i = 0;
            ps.setString(++i, enpointUid); // ENDPOINT_UID VARCHAR(256) NOT NULL,
            ps.setString(++i, sequenceId); // ID VARCHAR(256) NOT NULL,
            ps.setString(++i, type.id); // TYPE CHARACTER NOT NULL,

            ps.setLong(++i, expirationTime); // EXP_TIME TIMESTAMP NOT NULL,
            ps.setString(++i, securityContextTokenId); // STR_ID VARCHAR(256),


            ps.setInt(++i, state.asInt()); // STATUS SMALLINT NOT NULL,
            ps.setString(++i, b2s(ackRequestedFlag)); // ACK_REQUESTED_FLAG CHARACTER,
            ps.setLong(++i, lastMessageId); // LAST_MESSAGE_NUMBER BIGINT NOT NULL,
            ps.setLong(++i, lastActivityTime); // LAST_ACTIVITY_TIME TIMESTAMP NOT NULL,
            ps.setLong(++i, lastAcknowledgementRequestTime); // LAST_ACK_REQUEST_TIME TIMESTAMP NOT NULL,

            if (ps.executeUpdate() != 1) {
                cm.rollback(con);

                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Inserting sequence data for %s sequence with id = [ %s ] failed: " +
                        "Expected inserted rows: 1, Actual: %d",
                        type,
                        sequenceId)));
            }

            PersistentSequenceData data = loadInstance(con, ts, cm, enpointUid, sequenceId);
            cm.commit(con);

            return data;
        } catch (SQLException ex) {
            cm.rollback(con);
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Inserting sequence data for %s sequence with id = [ %s ] failed: " +
                    "An unexpected JDBC exception occured",
                    type,
                    sequenceId), ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);
        }

    }

    static PersistentSequenceData loadInstance(TimeSynchronizer ts, ConnectionManager cm, String endpointUid, String sequenceId) {
        Connection con = cm.getConnection(true);
        try {
            return loadInstance(con, ts, cm, endpointUid, sequenceId);
        } finally {
            cm.recycle(con);
        }
    }

    private static PersistentSequenceData loadInstance(Connection connection, TimeSynchronizer ts, ConnectionManager cm, String endpointUid, String sequenceId) {
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(connection, "SELECT " +
                    "TYPE, EXP_TIME, BOUND_ID, STR_ID " +
                    "FROM RM_SEQUENCES " +
                    "WHERE ENDPOINT_UID=? AND ID=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            if (!rs.isFirst() && !rs.isLast()) {
                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Duplicate sequence records detected for a sequence with id [ %s ]", sequenceId)));
            }

            return new PersistentSequenceData(
                    ts,
                    cm,
                    endpointUid,
                    sequenceId,
                    SequenceType.fromId(rs.getString("TYPE")),
                    rs.getString("STR_ID"),
                    rs.getString("BOUND_ID"),
                    rs.getLong("EXP_TIME"));

        } catch (SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Loading sequence data for a sequence with id = [ %s ] failed: " +
                    "An unexpected JDBC exception occured",
                    sequenceId), ex));
        } finally {
            cm.recycle(ps);
        }
    }

    static void remove(ConnectionManager cm, String endpointUid, String sequenceId) {
        Connection con = cm.getConnection(false);
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "DELETE FROM RM_UNACKED_MESSAGES WHERE ENDPOINT_UID=? AND SEQ_ID=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);

            int rowsAffected = ps.executeUpdate();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("%d unacknowledged message records removed for a sequence with id [ %s ]", rowsAffected, sequenceId));
            }

            cm.recycle(ps);

            ps = cm.prepareStatement(con, "DELETE FROM RM_SEQUENCES WHERE ENDPOINT_UID=? AND ID=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);

            rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                cm.rollback(con);
                throw LOGGER.logException(
                        new PersistenceException(String.format(
                        "Removing sequence with id = [ %s ] failed: " +
                        "Expected deleted rows: 1, Actual: %d",
                        sequenceId,
                        rowsAffected)),
                        Level.WARNING);
            }

            cm.commit(con);

        } catch (SQLException ex) {
            cm.rollback(con);
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Removing sequence with id = [ %s ] failed: " +
                    "An unexpected JDBC exception occured",
                    sequenceId), ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);
        }
    }

    static void bind(ConnectionManager cm, String endpointUid, String referenceSequenceId, String boundSequenceId) {
        Connection con = cm.getConnection(false);
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "UPDATE RM_SEQUENCES SET " +
                    "BOUND_ID=? " +
                    "WHERE ENDPOINT_UID=? AND ID=?");

            ps.setString(1, boundSequenceId);
            ps.setString(2, endpointUid);
            ps.setString(3, referenceSequenceId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                cm.rollback(con);
                throw LOGGER.logException(
                        new PersistenceException(String.format(
                        "Binding a sequence with id = [ %s ] to a sequence with id [ %s ] failed: " +
                        "Expected updated rows: 1, Actual: %d",
                        boundSequenceId,
                        referenceSequenceId,
                        rowsAffected)),
                        Level.WARNING);
            }

            cm.commit(con);
        } catch (SQLException ex) {
            cm.rollback(con);
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Binding a sequence with id = [ %s ] to a sequence with id [ %s ] failed: " +
                    "An unexpected JDBC exception occured",
                    boundSequenceId,
                    referenceSequenceId), ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);
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

    public String getBoundSequenceId() {
        return boundSequenceId;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    private <T> T getFieldData(Connection con, FieldInfo<T> fi) throws PersistenceException {
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "SELECT " +
                    fi.columnName + " " +
                    "FROM RM_SEQUENCES " +
                    "WHERE ENDPOINT_UID=? AND ID=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Sequence record not found for a sequence with id [ %s ]", sequenceId)));
            }

            if (!rs.isFirst() && !rs.isLast()) {
                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Duplicate sequence records detected for a sequence with id [ %s ]", sequenceId)));
            }

            return fi.javaClass.cast(rs.getObject(fi.columnName));
        } catch (SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Loading %s column data on a sequence with id = [ %s ]  failed: " +
                    "An unexpected JDBC exception occured",
                    fi.columnName,
                    sequenceId), ex));
        } finally {
            cm.recycle(ps);
        }
    }

    private <T> T getFieldData(FieldInfo<T> fi) {
        Connection con = cm.getConnection(true);
        try {
            return getFieldData(con, fi);
        } finally {
            cm.recycle(con);
        }
    }

    private <T> void setFieldData(Connection con, FieldInfo<T> fi, T value, boolean updateLastActivityTime) {
        PreparedStatement ps = null;
        try {
            String lastActivityTimeUpdateString = "";
            if (updateLastActivityTime) {
                lastActivityTimeUpdateString = ", " + fLastActivityTime.columnName + "=? ";
            }

            ps = cm.prepareStatement(con, "UPDATE RM_SEQUENCES SET " +
                    fi.columnName + "=?" + lastActivityTimeUpdateString + " " +
                    "WHERE ENDPOINT_UID=? AND ID=?");

            int i = 0;
            ps.setObject(++i, value, fi.sqlType);
            if (updateLastActivityTime) {
                ps.setLong(++i, ts.currentTimeInMillis());
            }
            ps.setString(++i, endpointUid);
            ps.setString(++i, sequenceId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw LOGGER.logException(
                        new PersistenceException(String.format(
                        "Updating %s column data on a sequence with id = [ %s ]  failed: " +
                        "Expected updated rows: 1, Actual: %d",
                        fi.columnName,
                        sequenceId,
                        rowsAffected)),
                        Level.WARNING);
            }
        } catch (SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Updating %s column data on a sequence with id = [ %s ]  failed: " +
                    "An unexpected JDBC exception occured",
                    fi.columnName,
                    sequenceId), ex));
        } finally {
            cm.recycle(ps);
        }
    }

    private <T> void setFieldData(FieldInfo<T> fi, T value, boolean updateLastActivityTime) {
        Connection con = cm.getConnection(false);
        boolean commit = false;
        try {
            setFieldData(con, fi, value, updateLastActivityTime);
            commit = true;
        } finally {
            if (commit) {
                cm.commit(con);
            }
            cm.recycle(con);
        }
    }

    public long getLastMessageNumber() {
        return getFieldData(fLastMessageNumber);
    }

    public State getState() {
        return State.asState(getFieldData(fState));
    }

    public void setState(State newValue) {
        setFieldData(fState, newValue.asInt(), true);
    }

    public boolean getAckRequestedFlag() {
        return s2b(getFieldData(fAckRequestedFlag));
    }

    public void setAckRequestedFlag(boolean newValue) {
        setFieldData(fAckRequestedFlag, b2s(newValue), true);
    }

    public long getLastAcknowledgementRequestTime() {
        return getFieldData(fLastAcknowledgementRequestTime);
    }

    public void setLastAcknowledgementRequestTime(long newValue) {
        setFieldData(fLastAcknowledgementRequestTime, newValue, true);
    }

    public long getLastActivityTime() {
        return getFieldData(fLastActivityTime);
    }

    /**
     * {@inheritDoc }
     */
    public long incrementAndGetLastMessageNumber(boolean received) {
        Connection con = cm.getConnection(false);
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "UPDATE RM_SEQUENCES SET " +
                    "LAST_MESSAGE_NUMBER=LAST_MESSAGE_NUMBER+1, " + fLastActivityTime.columnName + "=? " +
                    "WHERE ENDPOINT_UID=? AND ID=?");

            ps.setLong(1, ts.currentTimeInMillis());
            ps.setString(2, endpointUid);
            ps.setString(3, sequenceId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                cm.rollback(con);
                throw LOGGER.logException(
                        new PersistenceException(String.format(
                        "Incrementing last message number on a sequence with id = [ %s ] failed: " +
                        "Expected updated rows: 1, Actual: %d",
                        sequenceId,
                        rowsAffected)),
                        Level.WARNING);
            }

            long newLastMessageId = getFieldData(con, fLastMessageNumber);
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("New last message id: " + newLastMessageId);
            }

            try {
                registerSingleUnackedMessageNumber(con, newLastMessageId, received);
            } catch (DuplicateMessageRegistrationException ex) {
                cm.rollback(con);
                throw new PersistenceException("Registering newly created last message id ", ex);
            } catch (PersistenceException ex) {
                cm.rollback(con);
                throw ex;
            }

            cm.commit(con);
            return newLastMessageId;
        } catch (SQLException ex) {
            cm.rollback(con);
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Incrementing last message number on a sequence with id = [ %s ] failed: " +
                    "An unexpected JDBC exception occured",
                    sequenceId), ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);
        }
    }

    private boolean containsUnackedMessageNumberRegistration(Connection con, long messageNumber) throws PersistenceException {
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "SELECT IS_RECEIVED FROM RM_UNACKED_MESSAGES WHERE ENDPOINT_UID=? AND SEQ_ID=? AND MSG_NUMBER=?");
            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);
            ps.setLong(3, messageNumber);

            ResultSet rs = ps.executeQuery();

            return rs.next();
        } catch (SQLException ex) {
            throw LOGGER.logSevereException(
                    new PersistenceException(String.format(
                    "Retrieving an unacked message number record for a message number [ %d ] on a sequence with id = [ %s ]  failed: " +
                    "An unexpected JDBC exception occured",
                    messageNumber,
                    sequenceId), ex));
        } finally {
            cm.recycle(ps);
        }
    }

    private void registerSingleUnackedMessageNumber(Connection con, long messageNumber, boolean received) throws PersistenceException, DuplicateMessageRegistrationException {
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "SELECT IS_RECEIVED FROM RM_UNACKED_MESSAGES WHERE ENDPOINT_UID=? AND SEQ_ID=? AND MSG_NUMBER=?");
            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);
            ps.setLong(3, messageNumber);

            ResultSet rs = ps.executeQuery();

            final boolean doInsert = !rs.next();
            // check for duplicate registration
            if (!doInsert && s2b(rs.getString("IS_RECEIVED")) == received) {
                throw new DuplicateMessageRegistrationException(sequenceId, messageNumber);
            }

            cm.recycle(ps);

            final int rowsAffected;
            if (doInsert) {
                // insert
                ps = cm.prepareStatement(con, "INSERT INTO RM_UNACKED_MESSAGES " +
                        "(ENDPOINT_UID, SEQ_ID, MSG_NUMBER, IS_RECEIVED) " +
                        "VALUES (?, ?, ?, ?)");
                ps.setString(1, endpointUid);
                ps.setString(2, sequenceId);
                ps.setLong(3, messageNumber);
                ps.setString(4, b2s(received));

                rowsAffected = ps.executeUpdate();
                if (rowsAffected != 1) {
                    throw LOGGER.logSevereException(
                            new PersistenceException(String.format(
                            "Inserting new unacked message number record for a message number [ %d ] on a sequence with id = [ %s ]  failed: " +
                            "Expected updated rows: 1, Actual: %d",
                            messageNumber,
                            sequenceId,
                            rowsAffected)));
                }
            } else {
                ps = cm.prepareStatement(con, "UPDATE RM_UNACKED_MESSAGES SET " +
                        "IS_RECEIVED=? " +
                        "WHERE ENDPOINT_UID=? AND SEQ_ID=? AND MSG_NUMBER=? AND IS_RECEIVED=?");
                ps.setString(1, b2s(received));
                ps.setString(2, endpointUid);
                ps.setString(3, sequenceId);
                ps.setLong(4, messageNumber);
                ps.setString(5, b2s(!received));

                rowsAffected = ps.executeUpdate();
            }
            if (rowsAffected != 1) {
                throw LOGGER.logSevereException(
                        new PersistenceException(String.format(
                        "Registering an unacked message number record for a message number [ %d ] on a sequence with id = [ %s ]  failed: " +
                        "Expected affected rows: 1, Actual: %d",
                        messageNumber,
                        sequenceId,
                        rowsAffected)));
            }

        } catch (SQLException ex) {
            throw LOGGER.logSevereException(
                    new PersistenceException(String.format(
                    "Registering an unacked message number record for a message number [ %d ] on a sequence with id = [ %s ]  failed: " +
                    "An unexpected JDBC exception occured",
                    messageNumber,
                    sequenceId), ex));
        } finally {
            cm.recycle(ps);
        }
    }

    /**
     * {@inheritDoc }
     */
    public void registerReceivedUnackedMessageNumber(long messageNumber) throws DuplicateMessageRegistrationException {
        Connection con = cm.getConnection(false);
        try {
            long lastMessageNumber = getFieldData(con, fLastMessageNumber);
            if (lastMessageNumber < messageNumber) {
                setFieldData(con, fLastMessageNumber, messageNumber, false);
                for (long i = lastMessageNumber + 1; i < messageNumber; i++) {
                    registerSingleUnackedMessageNumber(con, i, false);
                }
            } else if (! containsUnackedMessageNumberRegistration(con, messageNumber)) {
                throw new DuplicateMessageRegistrationException(sequenceId, messageNumber);
            }

            registerSingleUnackedMessageNumber(con, messageNumber, true);
            setFieldData(con, fLastActivityTime, ts.currentTimeInMillis(), false);

            cm.commit(con);
        } catch (PersistenceException ex) {
            cm.rollback(con);
            throw ex;
        } catch (DuplicateMessageRegistrationException ex) {
            cm.rollback(con);
            throw ex;
        } finally {
            cm.recycle(con);
        }
    }

    public void markAsAcknowledged(long messageNumber) {
        Connection con = cm.getConnection(false);
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "DELETE FROM RM_UNACKED_MESSAGES " +
                    "WHERE ENDPOINT_UID=? AND SEQ_ID=? AND MSG_NUMBER=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);
            ps.setLong(3, messageNumber);

            final int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                if (rowsAffected == 0) {
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.finer(String.format(
                                "No unacknowledged message record found for %s sequence with id = [ %s ] and message number [ %d ]: " +
                                "Message was probably already acknowledged earlier",
                                type,
                                sequenceId,
                                messageNumber));
                    }
                } else {
                    throw LOGGER.logSevereException(new PersistenceException(String.format(
                            "Message acknowledgement failed for %s sequence with id = [ %s ] and message number [ %d ]: " +
                            "Expected deleted rows: 1, Actual: %d",
                            type,
                            sequenceId,
                            messageNumber,
                            rowsAffected)));
                }
            }

            setFieldData(con, fLastActivityTime, ts.currentTimeInMillis(), false);

            cm.commit(con);
        } catch (SQLException ex) {
            cm.rollback(con);
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Message acknowledgement failed for %s sequence with id = [ %s ] and message number [ %d ]: " +
                    "An unexpected JDBC exception occured",
                    type,
                    sequenceId,
                    messageNumber), ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);
        }
    }

    public List<Long> getUnackedMessageNumbers() {
        Connection con = cm.getConnection(true);
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "SELECT MSG_NUMBER FROM RM_UNACKED_MESSAGES " +
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
                    "Unable to load list of unacked message registration for %s sequence with id = [ %s ]: " +
                    "An unexpected JDBC exception occured",
                    type,
                    sequenceId), ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);
        }
    }

    public List<Long> getLastMessageNumberWithUnackedMessageNumbers() {
        Connection con = cm.getConnection(false);
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "SELECT RM_SEQUENCES.LAST_MESSAGE_NUMBER AS LAST_NUMBER, RM_UNACKED_MESSAGES.MSG_NUMBER AS MESSAGE_NUMBER " +
                    "FROM RM_UNACKED_MESSAGES " +
                    "INNER JOIN RM_SEQUENCES ON " +
                    "RM_UNACKED_MESSAGES.ENDPOINT_UID=RM_SEQUENCES.ENDPOINT_UID AND RM_UNACKED_MESSAGES.SEQ_ID=RM_SEQUENCES.ID " +
                    "WHERE RM_UNACKED_MESSAGES.ENDPOINT_UID=? AND SEQ_ID=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);

            ResultSet rs = ps.executeQuery();

            List<Long> result = new LinkedList<Long>();
            if (rs.next()) { // add last message id and first message number
                result.add(rs.getLong("LAST_NUMBER"));
                result.add(rs.getLong("MESSAGE_NUMBER"));
            } else {
                result.add(getFieldData(con, fLastMessageNumber)); // load last message number again
            }

            while (rs.next()) { // add only message numbers
                result.add(rs.getLong("MESSAGE_NUMBER"));
            }

            cm.commit(con);

            return result;
        } catch (SQLException ex) {
            cm.rollback(con);
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Unable to load list of unacked message registration for %s sequence with id = [ %s ]: " +
                    "An unexpected JDBC exception occured",
                    type,
                    sequenceId), ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);
        }
    }

    public void attachMessageToUnackedMessageNumber(ApplicationMessage message) {
        ByteArrayInputStream bais = null;
        Connection con = cm.getConnection(false);
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "UPDATE RM_UNACKED_MESSAGES SET " +
                    "IS_RECEIVED=?, CORRELATION_ID=?, NEXT_RESEND_COUNT=?, WSA_ACTION=?, MSG_DATA=? " +
                    "WHERE ENDPOINT_UID=? AND SEQ_ID=? AND MSG_NUMBER=?");

            int i = 0;

            ps.setString(++i, b2s(true));

            ps.setString(++i, message.getCorrelationId());
            ps.setLong(++i, message.getNextResendCount());

            ps.setString(++i, ((JaxwsApplicationMessage) message).getWsaAction());
            final byte[] msgData = message.toBytes();
            bais = new ByteArrayInputStream(msgData);
            ps.setBinaryStream(++i, bais, msgData.length);

            ps.setString(++i, endpointUid);
            ps.setString(++i, sequenceId);
            ps.setLong(++i, message.getMessageNumber());

            final int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                cm.rollback(con);
                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Storing message data in an unacked message registration for %s sequence with id = [ %s ] and message number [ %d ] has failed: " +
                        "Expected updated rows: 1, Actual: %d",
                        type,
                        sequenceId,
                        message.getMessageNumber(),
                        rowsAffected)));
            }

            setFieldData(con, fLastActivityTime, ts.currentTimeInMillis(), false);

            cm.commit(con);
        } catch (SQLException ex) {
            cm.rollback(con);
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Unable to store message data in an unacked message registration for %s sequence with id = [ %s ] and message number [ %d ]: " +
                    "An unexpected JDBC exception occured",
                    type,
                    sequenceId,
                    message.getMessageNumber()), ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);

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

        Connection con = cm.getConnection(false);
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "SELECT MSG_NUMBER, NEXT_RESEND_COUNT, WSA_ACTION, MSG_DATA FROM RM_UNACKED_MESSAGES " +
                    "WHERE ENDPOINT_UID=? AND SEQ_ID=? AND CORRELATION_ID=?");

            ps.setString(1, endpointUid);
            ps.setString(2, sequenceId);
            ps.setString(3, correlationId);


            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }

            if (!rs.isFirst() && !rs.isLast()) {
                cm.rollback(con);
                throw LOGGER.logSevereException(new PersistenceException(String.format(
                        "Duplicate records detected for unacked message registration on %s sequence with id = [ %s ] and correlation id [ %d ]",
                        type,
                        sequenceId,
                        correlationId)));
            }


            ApplicationMessage message = JaxwsApplicationMessage.newInstance(
                    rs.getBlob("MSG_DATA").getBinaryStream(),
                    rs.getInt("NEXT_RESEND_COUNT"),
                    correlationId,
                    rs.getString("WSA_ACTION"),
                    sequenceId,
                    rs.getLong("MSG_NUMBER"));

            setFieldData(con, fLastActivityTime, ts.currentTimeInMillis(), false);

            cm.commit(con);

            return message;
        } catch (SQLException ex) {
            cm.rollback(con);
            throw LOGGER.logSevereException(new PersistenceException(String.format(
                    "Unable to load message data from an unacked message registration for %s sequence with id = [ %s ] and correlation id [ %d ]: " +
                    "An unexpected JDBC exception occured",
                    type,
                    sequenceId,
                    correlationId), ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);

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
