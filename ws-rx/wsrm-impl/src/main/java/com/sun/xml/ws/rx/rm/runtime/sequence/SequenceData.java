/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import java.util.List;

public interface SequenceData {

    String getSequenceId();

    long getExpirationTime();

    String getBoundSecurityTokenReferenceId();

    long getLastMessageNumber();

    boolean getAckRequestedFlag();

    void setAckRequestedFlag(boolean newValue);

    long getLastAcknowledgementRequestTime();

    void setLastAcknowledgementRequestTime(long newTime);

    /**
     * Provides information on the last activity time of this sequence data instance. Following is the
     * list of operations invocation of which causes an update of last activity time:
     * 
     * <ul>
     *   <li>{@link #attachMessageToUnackedMessageNumber(ApplicationMessage) }</li>
     *   <li>{@link #incrementAndGetLastMessageNumber(boolean) }</li>
     *   <li>{@link #markAsAcknowledged(long) }</li>
     *   <li>{@link #registerUnackedMessageNumber(long, boolean) }</li>
     *   <li>{@link #retrieveMessage(java.lang.String) }</li>
     *   <li>{@link #setAckRequestedFlag(boolean) }</li>
     *   <li>{@link #setLastAcknowledgementRequestTime(long) }</li>
     *   <li>{@link #setState(com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State) }</li>
     * </ul>
     *
     * @return last activity time of the sequence
     */
    long getLastActivityTime();

    State getState();

    void setState(State newState);

    /**
     * Increments last message number on the sequence and returns new last message number value.
     * Method automatically marks the newly created message number as unacknowledged.
     *
     * @param received this flag specifies whether the message with such number should be marked
     * as received on the sequence.
     *
     * @return new value of the last message number
     */
    long incrementAndGetLastMessageNumber(boolean received);

    /**
     * Registers the message number as unacknowledged.
     * <p/>
     * If the value of {@code messageNumber} parameter is greater that the actual 
     * last mesasge number, the last message number value is increased to the value of
     * {@code messageNumber} parameter. All message numbers lying between the original
     * and new value of last message number are automatically marked as unacknowldeged
     * and not received.
     *
     * @param messageNumber unacknowledged message number to register
     *
     * @return new value of the last message number
     * @exception DuplicateMessageRegistrationException in case such registration already exists
     */
    void registerReceivedUnackedMessageNumber(long messageNumber) throws DuplicateMessageRegistrationException;

    /**
     * Removes the provided {@code messageNumber} from the collection of unacked message
     * numbers and and marks stored message with given {@code messageNumber} (if any)
     * as eligible for removal from the underlying message storage.
     *
     * This method does nothing if there's no such unacknowledged message number found
     *
     * @param messageNumber
     */
    void markAsAcknowledged(long messageNumber);
    
    boolean isFailedOver(long messageNumber);

    void attachMessageToUnackedMessageNumber(ApplicationMessage message);

    ApplicationMessage retrieveMessage(String correlationId);

    public List<Long> getUnackedMessageNumbers();

    /**
     * In contrast to {@link #getUnackedMessageNumbers()}, this method returns allways a non-empty
     * {@link List} in which first item represents a current last message number value (see also
     * {@link #getLastMessageNumber()}. The items following the first item represent
     * the collection of unacked message numbers (see also {@link #getUnackedMessageNumbers()}).
     * <p/>
     * This special method was introduced in order to allow for an atomic retrieval of both values.
     *
     * @return {@link List} where first item represents the last message number and all subsequent
     * values represent unacknowledged message numbers.
     *
     */
    public List<Long> getLastMessageNumberWithUnackedMessageNumbers();
}
