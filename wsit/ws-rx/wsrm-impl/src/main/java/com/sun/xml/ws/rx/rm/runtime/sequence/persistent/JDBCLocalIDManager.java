/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.rx.rm.runtime.sequence.persistent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.runtime.LocalIDManager;

/**
DROP TABLE RM_LOCALIDS;

CREATE TABLE RM_LOCALIDS (
LOCAL_ID VARCHAR(512) NOT NULL,
SEQ_ID VARCHAR(256) NOT NULL,
MSG_NUMBER BIGINT NOT NULL,
PRIMARY KEY (LOCAL_ID)
);
 */
public class JDBCLocalIDManager implements LocalIDManager {
    private final static String TABLE_NAME = "RM_LOCALIDS";
    private final static Logger LOGGER = Logger.getLogger(JDBCLocalIDManager.class);
    private ConnectionManager cm;

    public JDBCLocalIDManager() {
        this(new DefaultDataSourceProvider());
    }

    public JDBCLocalIDManager(DataSourceProvider dataSourceProvider) {
        super();
        this.cm = ConnectionManager.getInstance(dataSourceProvider);
    }

    public void createLocalID(String localID, String sequenceID, long messageNumber) {
        Connection con = cm.getConnection();
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, 
                    "INSERT INTO " + TABLE_NAME + 
                    " (LOCAL_ID, SEQ_ID, MSG_NUMBER)" +
                    " VALUES (?, ?, ?)");

            ps.setString(1, localID); 
            ps.setString(2, sequenceID);
            ps.setLong(3, messageNumber);

            int rowCount = ps.executeUpdate();
            if (rowCount != 1) {
                cm.rollback(con);

                throw LOGGER.logSevereException(new PersistenceException(
                        "Inserting LocalID failed."));
            }

            cm.commit(con);
        } catch (final Throwable ex) {
            cm.rollback(con);
            throw LOGGER.logSevereException(new PersistenceException(
                    "Inserting LocalID failed: An unexpected JDBC exception occured", ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);
        }
    }

    public void removeLocalIDs(Iterator<String> localIDs) {
        if (localIDs != null) {
            if (localIDs.hasNext()) {
                StringBuffer ids = new StringBuffer();
                while (localIDs.hasNext()) {
                    ids.append('\'');
                    ids.append(localIDs.next());
                    ids.append('\'');
                    if (localIDs.hasNext()) {
                        ids.append(',');
                    }
                }
                doRemove(ids.toString());
            }
        }
    }
    private void doRemove(String ids) {
        Connection con = cm.getConnection();
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "DELETE FROM "  + TABLE_NAME +
                    " WHERE LOCAL_ID IN (" + ids + ")");

            ps.executeUpdate();

            cm.commit(con);
        } catch (final Throwable ex) {
            cm.rollback(con);
            throw LOGGER.logSevereException(new PersistenceException(
                    "Removing LocalID failed: An unexpected JDBC exception occured", ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);
        }
    }

    public BoundMessage getBoundMessage(String localID) {
        BoundMessage result = null;
        Connection con = cm.getConnection();
        PreparedStatement ps = null;
        try {
            ps = cm.prepareStatement(con, "SELECT SEQ_ID, MSG_NUMBER FROM " + TABLE_NAME + 
                    " WHERE LOCAL_ID=?");

            ps.setString(1, localID);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                result = new BoundMessage(rs.getString("SEQ_ID"), rs.getLong("MSG_NUMBER"));
            }

            cm.commit(con);
        } catch (final SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException(
                    "Retrieving LocalID failed: An unexpected JDBC exception occured", ex));
        } finally {
            cm.recycle(ps);
            cm.recycle(con);
        }
        return result; 
    }
}
