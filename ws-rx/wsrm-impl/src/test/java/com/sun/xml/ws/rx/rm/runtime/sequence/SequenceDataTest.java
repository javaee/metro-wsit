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

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.runtime.sequence.invm.InVmSequenceDataLoader;
import com.sun.xml.ws.rx.rm.runtime.sequence.persistent.PersistentSequenceDataLoader;
import java.util.List;
import java.util.logging.Level;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class SequenceDataTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(SequenceDataTest.class);
    private static final String EXPECTED_SEQUENCE_ID = "sid_01";
    private static final long EXPECTED_EXPIRY_TIME = -1L;
    private static final String EXPECTED_STR_ID = "str_01";
    //
    private static final Sequence.State INITIAL_STATE = Sequence.State.CREATED;
    private static final boolean INITIAL_ACK_REQUESTED_FLAG = false;
    private static final long INITIAL_LAST_MESSAGE_NUMBER = 10;
    private static final long INITIAL_LAST_ACTIVITY_TIME = 0;
    private static final long INITIAL_LAST_ACKNOWLEDGEMENT_REQUEST_TIME = 0;
    //
    private final SequenceDataLoader[] loaders;
    //
    private SequenceData[] instances;

    public SequenceDataTest(String testName) {
        super(testName);

        this.loaders = new SequenceDataLoader[]{
                    new InVmSequenceDataLoader(),
                    new PersistentSequenceDataLoader()
                };
    }

    @Override
    protected void setUp() throws Exception {
        for (SequenceDataLoader loader : loaders) {
            loader.setUp();
        }

        instances = new SequenceData[loaders.length];

        for (int i = 0; i < instances.length; i++) {
            instances[i] = loaders[i].newInstance(
                    true, // this value has no effect on the actual tests
                    EXPECTED_SEQUENCE_ID,
                    EXPECTED_STR_ID,
                    EXPECTED_EXPIRY_TIME,
                    INITIAL_STATE,
                    INITIAL_ACK_REQUESTED_FLAG,
                    INITIAL_LAST_MESSAGE_NUMBER,
                    INITIAL_LAST_ACTIVITY_TIME,
                    INITIAL_LAST_ACKNOWLEDGEMENT_REQUEST_TIME);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        for (SequenceDataLoader loader : loaders) {
            loader.tearDown();
        }
    }

    /**
     * Test of getSequenceId method, of class SequenceData.
     */
    public void testGetSequenceId() {
        for (SequenceData instance : instances) {
            assertEquals(EXPECTED_SEQUENCE_ID, instance.getSequenceId());
        }
    }

    /**
     * Test of getExpirationTime method, of class SequenceData.
     */
    public void testGetExpirationTime() {
        for (SequenceData instance : instances) {
            assertEquals(EXPECTED_EXPIRY_TIME, instance.getExpirationTime());
        }
    }

    /**
     * Test of getBoundSecurityTokenReferenceId method, of class SequenceData.
     */
    public void testGetBoundSecurityTokenReferenceId() {
        for (SequenceData instance : instances) {
            assertEquals(EXPECTED_STR_ID, instance.getBoundSecurityTokenReferenceId());
        }
    }

    /**
     * Test of get/setAckRequestedFlag method, of class SequenceData.
     */
    public void testGetAndSetAckRequestedFlag() {
        for (SequenceData instance : instances) {
            assertEquals(INITIAL_ACK_REQUESTED_FLAG, instance.getAckRequestedFlag());

            instance.setAckRequestedFlag(!INITIAL_ACK_REQUESTED_FLAG);
            assertEquals(!INITIAL_ACK_REQUESTED_FLAG, instance.getAckRequestedFlag());
        }
    }

    /**
     * Test of get/setLastAcknowledgementRequestTime method, of class SequenceData.
     */
    public void testGetAndSetLastAcknowledgementRequestTime() {
        final long expectedNewValue = System.currentTimeMillis();

        for (SequenceData instance : instances) {
            assertEquals(INITIAL_LAST_ACKNOWLEDGEMENT_REQUEST_TIME, instance.getLastAcknowledgementRequestTime());

            instance.setLastAcknowledgementRequestTime(expectedNewValue);
            assertEquals(expectedNewValue, instance.getLastAcknowledgementRequestTime());
        }
    }

    /**
     * Test of getState method, of class SequenceData.
     */
    public void testGetAndSetState() {
        for (SequenceData instance : instances) {
            assertEquals(INITIAL_STATE, instance.getState());

            Sequence.State state;

            state = Sequence.State.CREATED;
            instance.setState(state);
            assertEquals(state, instance.getState());

            state = Sequence.State.CLOSING;
            instance.setState(state);
            assertEquals(state, instance.getState());

            state = Sequence.State.CLOSED;
            instance.setState(state);
            assertEquals(state, instance.getState());

            state = Sequence.State.TERMINATING;
            instance.setState(state);
            assertEquals(state, instance.getState());
        }
    }

    /**
     * Test of getLastMessageNumber and incrementAndGetLastMessageNumber method, of class SequenceData.
     */
    public void testIncrementAndGetLastMessageNumber() throws DuplicateMessageRegistrationException {
        for (SequenceData instance : instances) {
            long expectedLastMessageNumber = INITIAL_LAST_MESSAGE_NUMBER;

            assertEquals(INITIAL_LAST_MESSAGE_NUMBER, instance.getLastMessageNumber());

            long newLastMessageNumber;

            newLastMessageNumber = instance.incrementAndGetLastMessageNumber(false);
            expectedLastMessageNumber++;
            assertEquals(newLastMessageNumber, instance.getLastMessageNumber());
            assertEquals(expectedLastMessageNumber, newLastMessageNumber);

            newLastMessageNumber = instance.incrementAndGetLastMessageNumber(true);
            expectedLastMessageNumber++;
            assertEquals(newLastMessageNumber, instance.getLastMessageNumber());
            assertEquals(expectedLastMessageNumber, newLastMessageNumber);

            instance.registerReceivedUnackedMessageNumber(INITIAL_LAST_MESSAGE_NUMBER + 1);

            try {
                instance.registerReceivedUnackedMessageNumber(INITIAL_LAST_MESSAGE_NUMBER + 2);
                fail("DuplicateMessageRegistrationException expected here");
            } catch (DuplicateMessageRegistrationException ex) {
                // passed test
            }
        }
    }

    /**
     * Test of registerUnackedMessageNumber method, of class SequenceData.
     */
    public void testUnackedMessageNumberHandlingMethods() throws Exception {
        for (SequenceData instance : instances) {
            // Test bumping up last message number if the new number exceeds last message number
            long oldLastMessageNumber = instance.getLastMessageNumber();
            instance.registerReceivedUnackedMessageNumber(instance.getLastMessageNumber() + 1);
            assertEquals(oldLastMessageNumber + 1, instance.getLastMessageNumber());
            assertEquals(1, instance.getUnackedMessageNumbers().size());

            // Test filling in gaps
            oldLastMessageNumber = instance.getLastMessageNumber();
            instance.registerReceivedUnackedMessageNumber(instance.getLastMessageNumber() + 10);
            assertEquals(oldLastMessageNumber + 10, instance.getLastMessageNumber());
            assertEquals(11, instance.getUnackedMessageNumbers().size());

            List<Long> unacked = instance.getUnackedMessageNumbers();
            List<Long> unackedWithLastMsgNumber = instance.getLastMessageNumberWithUnackedMessageNumbers();

            assertEquals(instance.getLastMessageNumber(), unackedWithLastMsgNumber.get(0).longValue());
            assertEquals(unacked.size() + 1, unackedWithLastMsgNumber.size());
            for (int i = 0; i < unacked.size(); i++) {
                assertEquals(unacked.get(i), unackedWithLastMsgNumber.get(i+1));
            }

            try {
                instance.registerReceivedUnackedMessageNumber(unacked.get(0));
                fail("DuplicateMessageRegistrationException expected here");
            } catch (DuplicateMessageRegistrationException ex) {
                // passed test
            }
            try {
                instance.registerReceivedUnackedMessageNumber(unacked.get(unacked.size() - 1));
                fail("DuplicateMessageRegistrationException expected here");
            } catch (DuplicateMessageRegistrationException ex) {
                // passed test
            }
            for (int i = 1; i < unacked.size() - 1; i++) {
                instance.registerReceivedUnackedMessageNumber(unacked.get(i)); // marking the automatically filled in numbers as received should pass
            }

            int ackedCount = 0;
            for (long acknowledged : unacked) {
                instance.markAsAcknowledged(acknowledged);

                assertEquals(unacked.size() - ++ackedCount, instance.getUnackedMessageNumbers().size());
            }
            assertEquals(0, instance.getUnackedMessageNumbers().size());

            // second run should do nothing
            for (long acknowledged : unacked) {
                instance.markAsAcknowledged(acknowledged);

                assertEquals(0, instance.getUnackedMessageNumbers().size());
            }
        }
    }
//
//    /**
//     * Test of attachMessageToUnackedMessageNumber method, of class SequenceData.
//     */
//    public void testAttachMessageToUnackedMessageNumber() {
//        fail("The test case is not implemented yet.");
//    }
//
//    /**
//     * Test of retrieveMessage method, of class SequenceData.
//     */
//    public void testRetrieveMessage() {
//        fail("The test case is not implemented yet.");
//    }

    /**
     * Test of getLastActivityTime method, of class SequenceData.
     */
    @SuppressWarnings("SleepWhileHoldingLock")
    public void testGetLastActivityTime() throws DuplicateMessageRegistrationException, InterruptedException {
        for (SequenceData instance : instances) {
            assertEquals(INITIAL_LAST_ACTIVITY_TIME, instance.getLastActivityTime());

            long oldLastActivityTime;
            final int SLEEP_TIME = 100;

            oldLastActivityTime = instance.getLastActivityTime();
            instance.incrementAndGetLastMessageNumber(true);
            assertLowerThan(oldLastActivityTime, instance.getLastActivityTime());

            oldLastActivityTime = instance.getLastActivityTime();
            Thread.sleep(SLEEP_TIME);
            instance.setAckRequestedFlag(true);
            assertLowerThan(oldLastActivityTime, instance.getLastActivityTime());

            oldLastActivityTime = instance.getLastActivityTime();
            Thread.sleep(SLEEP_TIME);
            instance.setLastAcknowledgementRequestTime(System.currentTimeMillis());
            assertLowerThan(oldLastActivityTime, instance.getLastActivityTime());

            oldLastActivityTime = instance.getLastActivityTime();
            Thread.sleep(SLEEP_TIME);
            instance.setState(Sequence.State.CLOSED);
            assertLowerThan(oldLastActivityTime, instance.getLastActivityTime());

            oldLastActivityTime = instance.getLastActivityTime();
            Thread.sleep(SLEEP_TIME);
            instance.registerReceivedUnackedMessageNumber(instance.getLastMessageNumber() + 1);
            assertLowerThan(oldLastActivityTime, instance.getLastActivityTime());

            oldLastActivityTime = instance.getLastActivityTime();
            Thread.sleep(SLEEP_TIME);
            instance.markAsAcknowledged(instance.getLastMessageNumber());
            assertLowerThan(oldLastActivityTime, instance.getLastActivityTime());
        }
    }

    private void assertLowerThan(long lower, long greater) {
        LOGGER.log(Level.ALL, String.format("Lower number: %d Greater number: %d", lower, greater));
        System.out.println(String.format("Lower number: %d Greater number: %d", lower, greater));

        assertTrue(lower < greater);
    }
}
