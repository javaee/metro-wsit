/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.xml.ws.rx.util.TimeSynchronizer;
import java.util.Map;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
@ManagedObject
@Description("Reliable Messaging Sequence Manager")
@AMXMetadata(type = "WSRMSequenceManager")
public interface SequenceManager extends TimeSynchronizer {
    public static final String MANAGED_BEAN_NAME = "RMSequenceManager";

    @ManagedAttribute
    @Description("All RM sequences")
    public Map<String, Sequence> sequences();

    @ManagedAttribute
    @Description("Collection of sequence ID pairs that form an RM session")
    public Map<String, String> boundSequences();

    @ManagedAttribute
    @Description("Unique identifier of the WS endpoint for which this particular sequence manager will be used")
    public String uniqueEndpointId();

    @ManagedAttribute
    @Description("Determines whether this implementation of SeqenceManager is persistent")
    public boolean persistent();

    @ManagedAttribute
    @Description("Number of concurrently opened (not terminated) inbound sequences (determines number of concurrent RM sessions)")
    public long concurrentlyOpenedInboundSequencesCount();

    /**
     * Closes an existing sequence. The closed sequence is still kept in the internal sequence storage
     * 
     * @param sequenceId the unique sequence identifier
     *
     * @return closed sequence object
     */
    public Sequence closeSequence(String sequenceId) throws UnknownSequenceException;

    /**
     * Creates a new outbound sequence object with a given Id. It is assumed that RM handshake has been already established,
     * thus no RM handshake is performed.
     * 
     * @param sequenceId identifier of the new sequence
     * @param strId security reference token identifier which this session is bound to
     * @param expirationTime expiration time of the sequence in milliseconds; value of {@link com.sun.xml.ws.rm.policy.Configuration#UNSPECIFIED}
     * means that this sequence never expires.
     * 
     * @return newly created inbound sequence
     * 
     * @exception DuplicateSequenceExcepton in case a sequence instance with this 
     * identifier is already registered with this sequence manager
     */
    public Sequence createOutboundSequence(String sequenceId, String strId, long expirationTime) throws DuplicateSequenceException;

    /**
     * Creates a new inbound sequence object
     * 
     * @param sequenceId identifier of the new sequence
     * @param strId security reference token identifier which this session is bound to
     * @param expirationTime expiration time of the sequence in milliseconds; value of {@link com.sun.xml.ws.rm.policy.Configuration#UNSPECIFIED}
     * means that this sequence never expires.
     * 
     * @return newly created inbound sequence
     * 
     * @exception DuplicateSequenceExcepton in case a sequence instance with this 
     * identifier is already registered with this sequence manager
     */
    public Sequence createInboundSequence(String sequenceId, String strId, long expirationTime) throws DuplicateSequenceException;
    
    /**
     * Generates a unique identifier of a sequence
     * 
     * @return new unique sequence identifier which can be used to construct a new sequence.
     */
    public String generateSequenceUID();

    /**
     * Retrieves an existing sequence from the internal sequence storage
     *
     * @param sequenceId the unique sequence identifier
     *
     * @return sequence identified with the {@code sequenceId} identifier
     *
     * @exception UnknownSequenceExceptio in case no such sequence is registered within the sequence manager
     */
    public Sequence getSequence(String sequenceId) throws UnknownSequenceException;

    /**
     * Provides information on whether the sequence identifier is a valid identifier that belongs to an existing 
     * sequence registered with the sequence manager.
     * 
     * @param sequenceId sequence identifier to be checked
     * 
     * @return {@code true} in case the sequence identifier is valid, {@code false} otherwise
     */
    public boolean isValid(String sequenceId);
    
    /**
     * Terminates an existing sequence by calling the {@link Sequence#preDestroy()} method. In addition to this, the terminated
     * sequence is removed from the internal sequence storage
     * 
     * @param sequenceId the unique sequence identifier
     * 
     * @return terminated sequence object
     * 
     * @exception UnknownSequenceExceptio in case no such sequence is registered within the sequence manager
     */
    public Sequence terminateSequence(String sequenceId) throws UnknownSequenceException;
    
    /**
     * Binds two sequences together. This method is mainly intended to be used for 
     * binding together request and response sequences.
     * 
     * @param referenceSequenceId a reference sequence identifier to which the other sequence shall be bound.
     * @param boundSequenceId a bound sequence identifier
     * 
     * @throws UnknownSequenceException in case any of the sequence identifiers does not represent a valid sequence
     */
    public void bindSequences(String referenceSequenceId, String boundSequenceId) throws UnknownSequenceException;
    
    /**
     * Retrieves a sequence previously bound to the reference sequence
     * 
     * @param referenceSequenceId a reference sequence identifier to which the other sequence has been bound.
     * 
     * @return bound sequence or {@code null} in case no sequence is bound to the reference sequence
     * 
     * @exception UnknownSequenceExceptio in case no such reference sequence is registered within the sequence manager
     */
    public Sequence getBoundSequence(String referenceSequenceId) throws UnknownSequenceException;

    /**
     * Terminates all sequences that became expired in the meantime and removes all
     * previously terminated sequences that were terminated sooner than a pre-configured
     * period of time.
     * <p />
     *
     * This maintenance method is intended to be called externally by a {@link SequenceMaintenanceTask}
     * instance associated with this {@code SequenceManager}.
     */
    public void onMaintenance();

    /**
     * Instructs the {@link SequenceManager} instance to invalidate it's local 
     * cache. This prevents stale data being used and ensures that fresh data are 
     * loaded from the RM HA backing stores.
     */
    public void invalidateCache();
    
    /**
     * Tells the {@link SequenceManager} that it is going to be disposed. An implementation
     * of this interface can use the method to do the necessary resource cleanup.
     */
    public void dispose();
}
