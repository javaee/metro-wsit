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
package com.sun.xml.ws.rm.runtime.sequence.persistent;

import com.sun.xml.ws.rm.runtime.sequence.Sequence.Status;
import com.sun.xml.ws.rm.runtime.sequence.SequenceData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class PersistentSequenceData implements SequenceData {

    /**
    DROP TABLE RM_UNACKED_MESSAGES;
    
    DROP TABLE RM_SEQUENCES;
    
    CREATE TABLE RM_SEQUENCES (
    id VARCHAR(50) NOT NULL,
    expiration INTEGER NOT NULL,
    status SMALLINT NOT NULL,
    ack_requested_flag CHAR(1) NOT NULL,
    last_message_id BIGINT NOT NULL,
    PRIMARY KEY (id)
    );
    
    CREATE TABLE RM_UNACKED_MESSAGES (
    sequence_id VARCHAR(50) NOT NULL,
    message_id BIGINT NOT NULL,
    PRIMARY KEY (sequence_id, message_id)
    );
    
    ALTER TABLE RM_UNACKED_MESSAGES ADD CONSTRAINT FK_RM_SEQUENCE_ID FOREIGN KEY (SEQUENCE_ID) REFERENCES RM_SEQUENCES (ID);
     */
    private final String sequenceId;
    private final String boundSecurityTokenReferenceId;
    private final long expirationTime;
    private final Connection sqlConnection;
    private final ResultSet sequenceData;
    
    

    public PersistentSequenceData(
            Connection connection,
            String sequenceId,
            String boundSecurityTokenReferenceId,
            long expirationTime,
            long lastMessageId,
            Status status,
            boolean ackRequestedFlag) throws PersistenceException {
        this.sequenceId = sequenceId;
        this.boundSecurityTokenReferenceId = boundSecurityTokenReferenceId;
        this.expirationTime = expirationTime;
        this.sqlConnection = connection;

        try {
            PreparedStatement insertPS = sqlConnection.prepareStatement("INSERT INTO RM_SEQUENCES VALUES (?, ?, ?, ?, ?)");
            insertPS.setString(1, sequenceId);
            insertPS.setLong(2, expirationTime);
            insertPS.setInt(3, status.getValue());
            insertPS.setBoolean(4, ackRequestedFlag);
            insertPS.setLong(5, lastMessageId);

            if (insertPS.executeUpdate() != 1) {
                throw new PersistenceException("Sequence data not inserted into database.");
            }

            // TODO commit
            PreparedStatement queryPS = sqlConnection.prepareStatement("SELECT * FROM RM_SEQUENCES WHERE sequence_id = ?");
            queryPS.setString(1, sequenceId);
            sequenceData = queryPS.executeQuery();
            if (!sequenceData.first()) {
                // TODO L10N
                throw new PersistenceException("Unable to get the sequence data");
            }
        } catch (SQLException ex) {
            // TODO L10N
            throw new PersistenceException("Error creating the new sequence data record.", ex);
        } finally {
            // resource cleanup
        }
    }

    public boolean isAckRequestedFlag() {
        try {
            return sequenceData.getBoolean("ack_requested_flag");
        } catch (SQLException ex) {
            // TODO handle
            return false;
        }
    }

    public void setAckRequestedFlag(boolean ackRequestedFlag) {
        try {
            sequenceData.updateBoolean("ack_requested_flag", ackRequestedFlag);
        } catch (SQLException ex) {
            // TODO handle
        }
    }

    public Status getStatus() {
        try {
            return Status.valueToStatus(sequenceData.getInt("status"));
        } catch (SQLException ex) {
            // TODO handle
            return null;
        }
    }

    public void setStatus(Status status) {
        try {
            sequenceData.updateInt("status", status.getValue());
        } catch (SQLException ex) {
            // TODO handle
        }
    }

    public long getExpirationTime() {
        return expirationTime; // no need to synchronize

    }

    public String getSequenceId() {
        return sequenceId; // no need to synchronize

    }

    public long getLastMessageId() {
        try {
            return sequenceData.getLong("last_message_id");
        } catch (SQLException ex) {
            // TODO handle
            return -1;
        }
    }

    public long updateLastMessageId(long newId) {
        try {
            // TODO handle transaction
            long retVal = getLastMessageId();
            sequenceData.updateLong("last_message_id", newId);
            return retVal;
        } catch (SQLException ex) {
            // TODO handle
            return -1;
        }
    }

    public long incrementAndGetLastMessageId() {
        try {
            // TODO handle transaction
            long retVal = getLastMessageId() + 1;
            sequenceData.updateLong("last_message_id", retVal);
            return retVal;
        } catch (SQLException ex) {
            // TODO handle
            return -1;
        }
    }

    public Collection<Long> getAllUnackedIndexes() {
        // TODO
        return null;
    }

    public boolean noUnackedMessageIds() {
        // TODO
        return false;
    }

    public void addUnackedMessageId(long messageId) {
        // TODO
    }

    public boolean removeUnackedMessageId(long messageId) {
        // TODO
        return false;
    }

    public void acquireMessageIdDataReadOnlyLock() {
        // TODO
    }

    public void releaseMessageIdDataReadOnlyLock() {
        // TODO
    }

    public void acquireMessageIdDataReadWriteLock() {
        // TODO
    }

    public void releaseMessageIdDataReadWriteLock() {
        // TODO
    }

    public String getBoundSecurityTokenReferenceId() {
        // TODO
        return boundSecurityTokenReferenceId;
    }

    public long getLastActivityTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateLastActivityTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
