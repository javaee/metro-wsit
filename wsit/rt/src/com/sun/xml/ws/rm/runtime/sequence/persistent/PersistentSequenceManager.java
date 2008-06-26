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

import java.sql.Connection;
import java.util.UUID;

import com.sun.xml.ws.rm.runtime.sequence.DuplicateSequenceException;
import com.sun.xml.ws.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rm.runtime.sequence.UnknownSequenceException;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 * @author Jungwook Chae
 */
public final class PersistentSequenceManager implements SequenceManager {
    private final Connection sqlConnection = null; // TODO initialize properly
    
    public void closeSequence(String sequenceId) throws UnknownSequenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Sequence createOutboundSequence(String sequenceId, String strId, long expirationTime) throws DuplicateSequenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Sequence createInboundSequence(String sequenceId, String strId, long expirationTime) throws DuplicateSequenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String generateSequenceUID() {
        return "uuid:" + UUID.randomUUID();
    }

    public Sequence getSequence(String sequenceId) throws UnknownSequenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isValid(String sequenceId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Sequence terminateSequence(String sequenceId) throws UnknownSequenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void bindSequences(String referenceSequenceId, String boundSequenceId) throws UnknownSequenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Sequence getBoundSequence(String referenceSequenceId) throws UnknownSequenceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//
//    private final Map<String, Sequence> sequences = new HashMap<String, Sequence>();
//    private final ReadWriteLock sequenceLock = new ReentrantReadWriteLock();
//    private Connection connection;
//
//    public Connection getConnection() {
//        return connection;
//    }
//
//    public PersistentSequenceManager() {
//        connect();
//        recover();
//    }
//
//    public Sequence getSequence(String sequenceId) throws UnknownSequenceException {
//        try {
//            sequenceLock.readLock().lock();
//            if (sequences.containsKey(sequenceId)) {
//                return sequences.get(sequenceId);
//            } else {
//                throw new UnknownSequenceException(sequenceId);
//            }
//        } finally {
//            sequenceLock.readLock().unlock();
//        }
//    }
//
//    public boolean isValid(String sequenceId) {
//        try {
//            sequenceLock.readLock().lock();
//            return sequences.containsKey(sequenceId);
//        } finally {
//            sequenceLock.readLock().unlock();
//        }
//    }
//
//    public Sequence createOutboudSequence(String sequenceId, long expirationTime) throws DuplicateSequenceException {
//        Sequence seq = new OutboundPersistentSequence(sequenceId, expirationTime, connection);
//        return registerSequence(seq);
//    }
//
//    public Sequence createInboundSequence(String sequenceId, long expirationTime) throws DuplicateSequenceException {
//        Sequence seq = new InboundPersistentSequence(sequenceId, expirationTime, connection);
//
//        return registerSequence(seq);
//    }
//
//    public String generateSequenceUID() {
//        return "uuid:" + UUID.randomUUID();
//    }
//
//    public void closeSequence(String sequenceId) throws UnknownSequenceException {
//        Sequence sequence = getSequence(sequenceId);
//        sequence.close();
//    }
//
//    public void terminateSequence(String sequenceId) throws UnknownSequenceException {
//        try {
//            sequenceLock.writeLock().lock();
//            if (sequences.containsKey(sequenceId)) {
//                Sequence sequence = sequences.remove(sequenceId);
//                sequence.preDestroy();
//            } else {
//                throw new UnknownSequenceException(sequenceId);
//            }
//        } finally {
//            sequenceLock.writeLock().unlock();
//        }
//    }
//
//    /**
//     * Registers a new sequence in the internal sequence storage
//     * 
//     * @param sequence sequence object to be registered within the internal sequence storage
//     */
//    private Sequence registerSequence(Sequence sequence) throws DuplicateSequenceException {
//        try {
//            sequenceLock.writeLock().lock();
//            if (sequences.containsKey(sequence.getId())) {
//                throw new DuplicateSequenceException(sequence.getId());
//            } else {
//                sequences.put(sequence.getId(), sequence);
//            }
//
//        } finally {
//            sequenceLock.writeLock().unlock();
//        }
//
//        return sequence;
//    }
//
//    protected void abort() {
//        try {
//            connection.rollback();
//
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    protected void commit() throws SQLException {
//        connection.commit();
//    }
//
//    protected void beginTransaction() {
//        //no-op
//    }
//
//    synchronized void connect() {
//        try {
//            Class<Driver> oracleDriver = (Class<Driver>) Class.forName("oracle.jdbc.driver.OracleDriver");
//            DriverManager.registerDriver(oracleDriver.newInstance());
//
//            connection = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:ORCL", "scott", "jeus");
//            connection.setAutoCommit(false);
//
//            createTables();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//
//
//    }
//
//    public void recover() {
//        try {
//            Statement stmt = connection.createStatement();
//            ResultSet rst = stmt.executeQuery("SELECT SEQ_ID, LASTACKEDINDEX FROM RM_SEQUENCES");
//
//            while (rst.next()) {
//                String id = rst.getString(1);
//                Long lastackedindex = rst.getLong(2);
//                Sequence seq = null;
//                if (lastackedindex == 0) {
//                    seq = new OutboundPersistentSequence(id, connection);
//                } else {
//                    seq = new InboundPersistentSequence(id, connection);
//                }
//                registerSequence(seq);
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
////  Sequence's Data Structure
////  
//
////  private final String id; -> VARCHAR2
////  private final long expirationTime; -> NUMBER
////  private Status status; -> NUMBER
////  private boolean ackRequestedFlag; -> NUMBER
////  protected final Collection<Long> unackedIndexes; ->BLOB // e.g. : LinkedList implements Serializable
////  private long lastAckedIndex = AbstractSequence.UNSPECIFIED_MESSAGE_ID; ->NUMBER
////  private final AtomicLong lastMessageId; ->NUMBER
//    protected void createTables() {
//        try {
//            Statement stmt = connection.createStatement();
//            stmt.executeUpdate("CREATE TABLE RM_SEQUENCES " + "(SEQ_ID VARCHAR2(256) NOT NULL, " + "EXPTIME NUMBER, " + "STATUS NUMBER, " + "ACKREQUESTEDFLAG NUMBER, " + "UNACKEDINDEXES BLOB, " + "LASTACKEDINDEX NUMBER, " + "LASTMESSAGEID NUMBER, " + "PRIMARY KEY (SEQ_ID))");
//            commit();
//        } catch (SQLException ex) {
//            //abort();
//            //ex.printStackTrace();
//        }
//    }
}
