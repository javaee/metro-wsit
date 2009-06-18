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
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.glassfish.gmbal.ManagedObjectManager;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class PersistentSequenceManager implements SequenceManager {

    private static final Logger LOGGER = Logger.getLogger(PersistentSequenceManager.class);
    /**
     * JNDI name of the JDBC pool to be used for persisting RM data
     */
    private static final String RM_JDBC_POOL_NAME = "jdbc/ReliableMessagingPool";
    /**
     * JDBC connection
     */
    private Connection sqlConnection = null;
    /**
     * Internal in-memory data access lock
     */
    private final ReadWriteLock internalDataAccessLock = new ReentrantReadWriteLock();
    /**
     * Internal in-memory cache of sequence data
     */
    private final Map<String, AbstractSequence> sequences = new HashMap<String, AbstractSequence>();
    /**
     * Internal in-memory map of bound sequences
     */
    private final Map<String, String> boundSequences = new HashMap<String, String>();
    /**
     * Monitoring manager
     */
    private final ManagedObjectManager managedObjectManager;

    public PersistentSequenceManager(SequenceManager.Type type, ManagedObjectManager managedObjectManager) {
        this.managedObjectManager = managedObjectManager;
        if (managedObjectManager != null) {
            managedObjectManager.registerAtRoot(this, type.toString());
        }
        // TODO recover();
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, ? extends Sequence> sequences() {
        return null; // TODO
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> boundSequences() {
        return null; // TODO
    }

    /**
     * {@inheritDoc}
     */
    public Sequence createOutboundSequence(String sequenceId, String strId, long expirationTime, DeliveryQueueBuilder deliveryQueueBuilder) throws DuplicateSequenceException {
        PersistentSequenceData data = new PersistentSequenceData(sequenceId, PersistentSequenceData.SequenceType.OUTBOUND, strId, expirationTime, OutboundSequence.INITIAL_LAST_MESSAGE_ID, currentTimeInMillis());
        return registerSequence(new OutboundSequence(data, deliveryQueueBuilder, this));
    }

    /**
     * {@inheritDoc}
     */
    public Sequence createInboundSequence(String sequenceId, String strId, long expirationTime, DeliveryQueueBuilder deliveryQueueBuilder) throws DuplicateSequenceException {
        PersistentSequenceData data = new PersistentSequenceData(sequenceId, PersistentSequenceData.SequenceType.INBOUND, strId, expirationTime, InboundSequence.INITIAL_LAST_MESSAGE_ID, currentTimeInMillis());
        return registerSequence(new InboundSequence(data, deliveryQueueBuilder, this));
    }

    /**
     * {@inheritDoc}
     */
    public String generateSequenceUID() {
        return "uuid:" + UUID.randomUUID();
    }

    /**
     * {@inheritDoc}
     */
    public Sequence closeSequence(String sequenceId) throws UnknownSequenceException {
        Sequence sequence = getSequence(sequenceId);
        sequence.close();
        return sequence;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public boolean isValid(String sequenceId) {
//        try {
//            internalDataAccessLock.readLock().lock();
//            return sequences.containsKey(sequenceId);
//        } finally {
//            internalDataAccessLock.readLock().unlock();
//        }
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
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
//
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
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
    
    /**
     * Registers a new sequence in the internal sequence storage
     *
     * @param sequence sequence object to be registered within the internal sequence storage
     */
    private AbstractSequence registerSequence(AbstractSequence sequence) throws DuplicateSequenceException {

        try {
            internalDataAccessLock.writeLock().lock();
            if (sequences.containsKey(sequence.getId())) {
                throw new DuplicateSequenceException(sequence.getId());
            } else {
                @SuppressWarnings("unchecked") // this method is called only for sequences with PersistentSequenceData
                PersistentSequenceData data = PersistentSequenceData.class.cast(sequence.getData());
                data = PersistentSequenceData.insert(sqlConnection, data);

                sequences.put(sequence.getId(), sequence);
                if (managedObjectManager != null) {
                    managedObjectManager.register(this, sequence, sequence.getId().replace(':', '-'));
                }
            }

            return sequence;
        } finally {
            internalDataAccessLock.writeLock().unlock();
        }
    }

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

    /**
     * {@inheritDoc}
     */
    public long currentTimeInMillis() {
        // TODO sync date with database
        return System.currentTimeMillis();
    }
}
