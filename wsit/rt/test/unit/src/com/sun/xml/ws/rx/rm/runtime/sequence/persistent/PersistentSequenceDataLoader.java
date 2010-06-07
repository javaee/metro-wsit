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

import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceData;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceDataLoader;
import com.sun.xml.ws.rx.util.TimeSynchronizer;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class PersistentSequenceDataLoader implements SequenceDataLoader {

    private class UnitTestDerbyDataSourceProvider implements DataSourceProvider {

        private final DataSource ds = new DataSource() {

            public Connection getConnection() throws SQLException {
                return dbInstance.getConnection();
            }

            public Connection getConnection(String username, String password) throws SQLException {
                return dbInstance.getConnection();
            }

            public PrintWriter getLogWriter() throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setLogWriter(PrintWriter out) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setLoginTimeout(int seconds) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int getLoginTimeout() throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public <T> T unwrap(Class<T> iface) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        public DataSource getDataSource() throws PersistenceException {
            return ds;
        }
    }
    //
    private static final String TEST_ENDPOINT_UID = "test_endpoint_001";
    private static final PersistentSequenceData.SequenceType TEST_SEQUENCE_TYPE = PersistentSequenceData.SequenceType.Inbound;
    //
    private final ConnectionManager cm = ConnectionManager.getInstance(new UnitTestDerbyDataSourceProvider());
    private final TimeSynchronizer ts = new TimeSynchronizer() {

        public long currentTimeInMillis() {
            return System.currentTimeMillis();
        }
    };
    private EmbeddedDerbyDbInstance dbInstance;

    public void setUp() {
        tearDown();

        dbInstance = EmbeddedDerbyDbInstance.start("PersistentRmTestDb");

        if (dbInstance.tableExists("RM_UNACKED_MESSAGES")) {
            dbInstance.execute("DROP TABLE RM_UNACKED_MESSAGES");
        }
        if (dbInstance.tableExists("RM_SEQUENCES")) {
            dbInstance.execute("DROP TABLE RM_SEQUENCES");
        }
        
        dbInstance.execute(
                "CREATE TABLE RM_SEQUENCES ( " +
                "ENDPOINT_UID VARCHAR(512) NOT NULL, " +
                "ID VARCHAR(256) NOT NULL, " +
                "TYPE CHARACTER NOT NULL, " +
                "EXP_TIME BIGINT NOT NULL, " +
                "BOUND_ID VARCHAR(256), " +
                "STR_ID VARCHAR(256), " +
                "STATUS SMALLINT NOT NULL, " +
                "ACK_REQUESTED_FLAG CHARACTER, " +
                "LAST_MESSAGE_NUMBER BIGINT NOT NULL, " +
                "LAST_ACTIVITY_TIME BIGINT NOT NULL, " +
                "LAST_ACK_REQUEST_TIME BIGINT NOT NULL, " +
                "PRIMARY KEY (ENDPOINT_UID, ID)" +
                ")");
        dbInstance.execute(
                "CREATE INDEX IDX_RM_SEQUENCES_BOUND_ID ON RM_SEQUENCES (BOUND_ID)");

        dbInstance.execute(
                "CREATE TABLE RM_UNACKED_MESSAGES ( " +
                "ENDPOINT_UID VARCHAR(512) NOT NULL, " +
                "SEQ_ID VARCHAR(256) NOT NULL, " +
                "MSG_NUMBER BIGINT NOT NULL, " +
                "IS_RECEIVED CHARACTER NOT NULL, " +
                "CORRELATION_ID VARCHAR(256), " +
                "NEXT_RESEND_COUNT INT, " +
                "WSA_ACTION VARCHAR(256), " +
                "MSG_DATA BLOB, " +
                "PRIMARY KEY (ENDPOINT_UID, SEQ_ID, MSG_NUMBER)" +
                ")");

        dbInstance.execute(
                "ALTER TABLE RM_UNACKED_MESSAGES " +
                "ADD CONSTRAINT FK_SEQUENCE " +
                "FOREIGN KEY (ENDPOINT_UID, SEQ_ID) REFERENCES RM_SEQUENCES(ENDPOINT_UID, ID)");
        dbInstance.execute(
                "CREATE INDEX IDX_RM_UNACKED_MESSAGES_CORRELATION_ID ON RM_UNACKED_MESSAGES (CORRELATION_ID)");
    }

    public void tearDown() {
        if (dbInstance != null) {
            dbInstance.stop();
            dbInstance = null;
        }
    }

    public SequenceData newInstance(String sequenceId, String securityContextTokenId, long expirationTime, State state, boolean ackRequestedFlag, long lastMessageId, long lastActivityTime, long lastAcknowledgementRequestTime) {
        return PersistentSequenceData.newInstance(
                ts,
                cm,
                TEST_ENDPOINT_UID,
                sequenceId,
                TEST_SEQUENCE_TYPE,
                securityContextTokenId,
                expirationTime,
                state,
                ackRequestedFlag,
                lastMessageId,
                lastActivityTime,
                lastAcknowledgementRequestTime);
    }
}
