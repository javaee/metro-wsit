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
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueueBuilder;
import java.sql.Connection;
import java.util.Map;
import java.util.UUID;

import com.sun.xml.ws.rx.rm.runtime.sequence.AbstractSequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateSequenceException;
import com.sun.xml.ws.rx.rm.runtime.sequence.InboundSequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.OutboundSequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.UnknownSequenceException;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.ManagedObjectManager;


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
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
@ManagedObject
@Description("Persistent RM Sequence Manager")
public final class PersistentSequenceManager implements SequenceManager {

    private static final Logger LOGGER = Logger.getLogger(PersistentSequenceManager.class);
    private static final String RM_JDBC_POOL_NAME = "jdbc/ReliableMessagingPool";
    private Connection sqlConnection = null; // TODO initialize properly
    private ManagedObjectManager managedObjectManager;

    public PersistentSequenceManager(SequenceManager.Type type, ManagedObjectManager managedObjectManager) {
        this.managedObjectManager = managedObjectManager;
        if (managedObjectManager != null) {
            managedObjectManager.registerAtRoot(this, type.toString());
        }

        // TODO recover();
    }

    public Map<String, ? extends Sequence> sequences() {
        return null;
    }

    public Map<String, String> boundSequences() {
        return null;
    }

    public Sequence createOutboundSequence(String sequenceId, String strId, long expirationTime, DeliveryQueueBuilder deliveryQueueBuilder) throws DuplicateSequenceException {
        return registerSequence(new OutboundSequence(sequenceId, strId, expirationTime, deliveryQueueBuilder));
    }

    public Sequence createInboundSequence(String sequenceId, String strId, long expirationTime, DeliveryQueueBuilder deliveryQueueBuilder) throws DuplicateSequenceException {
        return registerSequence(new InboundSequence(sequenceId, strId, expirationTime, deliveryQueueBuilder));
    }

    public String generateSequenceUID() {
        return "uuid:" + UUID.randomUUID();
    }

    public Sequence closeSequence(String sequenceId) throws UnknownSequenceException {
        Sequence sequence = getSequence(sequenceId);
        sequence.close();
        return sequence;
    }

    public Sequence getSequence(String sequenceId) throws UnknownSequenceException {
//        try {
//            internalDataAccessLock.readLock().lock();
//            if (sequences.containsKey(sequenceId)) {
//                return sequences.get(sequenceId);
//            } else {
//                throw new UnknownSequenceException(sequenceId);
//            }
//        } finally {
//            internalDataAccessLock.readLock().unlock();
//        }
        throw new UnsupportedOperationException();
    }

    public boolean isValid(String sequenceId) {
//        try {
//            internalDataAccessLock.readLock().lock();
//            return sequences.containsKey(sequenceId);
//        } finally {
//            internalDataAccessLock.readLock().unlock();
//        }
        throw new UnsupportedOperationException();
    }

    public Sequence terminateSequence(String sequenceId) throws UnknownSequenceException {
//        try {
//            internalDataAccessLock.writeLock().lock();
//            if (sequences.containsKey(sequenceId)) {
//                AbstractSequence sequence = sequences.remove(sequenceId);
//                sequence.setStatus(Status.TERMINATING);
//
//                if (boundSequences.containsKey(sequenceId)) {
//                    boundSequences.remove(sequenceId);
//
//                }
//
//                if (managedObjectManager != null) {
//                    managedObjectManager.unregister(sequence);
//                }
//                sequence.preDestroy();
//
//                return sequence;
//            } else {
//                throw new UnknownSequenceException(sequenceId);
//            }
//        } finally {
//            internalDataAccessLock.writeLock().unlock();
//        }
        throw new UnsupportedOperationException();
    }

    /**
     * Registers a new sequence in the internal sequence storage
     *
     * @param sequence sequence object to be registered within the internal sequence storage
     */
    private Sequence registerSequence(AbstractSequence sequence) throws DuplicateSequenceException {
//        try {
//            internalDataAccessLock.writeLock().lock();
//            if (sequences.containsKey(sequence.getId())) {
//                throw new DuplicateSequenceException(sequence.getId());
//            } else {
//                sequences.put(sequence.getId(), sequence);
//                if (managedObjectManager != null) {
//                    managedObjectManager.register(this, sequence, sequence.getId().replace(':', '-'));
//                }
//            }
//
//            return sequence;
//        } finally {
//            internalDataAccessLock.writeLock().unlock();
//        }
        throw new UnsupportedOperationException();
    }

    public void bindSequences(String referenceSequenceId, String boundSequenceId) throws UnknownSequenceException {
//        try {
//            internalDataAccessLock.writeLock().lock();
//            if (!sequences.containsKey(referenceSequenceId)) {
//                throw new UnknownSequenceException(referenceSequenceId);
//            }
//
//            if (!sequences.containsKey(boundSequenceId)) {
//                throw new UnknownSequenceException(boundSequenceId);
//            }
//
//            boundSequences.put(referenceSequenceId, boundSequenceId);
//        } finally {
//            internalDataAccessLock.writeLock().unlock();
//        }
        throw new UnsupportedOperationException();
    }

    public Sequence getBoundSequence(String referenceSequenceId) throws UnknownSequenceException {
//        try {
//            internalDataAccessLock.readLock().lock();
//            if (!isValid(referenceSequenceId)) {
//                throw new UnknownSequenceException(referenceSequenceId);
//            }
//
//            return (boundSequences.containsKey(referenceSequenceId)) ? sequences.get(boundSequences.get(referenceSequenceId)) : null;
//        } finally {
//            internalDataAccessLock.readLock().unlock();
//        }
        throw new UnsupportedOperationException();
    }

    /*
        beginTransaction();
        try {
            PreparedStatement ps = sqlConnection.prepareStatement("");

            final int rowsUpdated = ps.executeUpdate();
            commit();

            if (rowsUpdated != 1) {
                throw new UnknownSequenceException("");
            }
        } catch (SQLException ex) {
            abort();
            // TODO L10N
            throw LOGGER.logSevereException(new PersistenceException(
                    String.format("Error closing a sequence with id '%s'", sequenceId), ex));
        }
     */

    protected void abort() {
        try {
            sqlConnection.rollback();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void commit() throws SQLException {
        sqlConnection.commit();
    }

    protected void beginTransaction() {
        //no-op
    }

    synchronized void connect() throws PersistenceException {
        try {
            javax.naming.InitialContext ic = new javax.naming.InitialContext();
            Object __ds = ic.lookup(RM_JDBC_POOL_NAME); // TODO
            DataSource ds;
            if (__ds instanceof DataSource) {
                ds = DataSource.class.cast(__ds);
            } else {
                // TODO L10N
                throw new PersistenceException(String.format(
                        "Object of class '%s' bound in the JNDI under '%s' is not an instance of '%s'.", __ds.getClass().getName(), RM_JDBC_POOL_NAME, DataSource.class.getName()));
            }

            sqlConnection = ds.getConnection("username", "password");
            sqlConnection.setAutoCommit(false);
        } catch (SQLException ex) {
            // TODO L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to retrieve JDBC connection to Metro reliable messaging database", ex));
        } catch (NamingException ex) {
            // TODO L10N
            throw LOGGER.logSevereException(new PersistenceException("Unable to lookup Metro reliable messaging JDBC connection pool", ex));
        }
    }
}
