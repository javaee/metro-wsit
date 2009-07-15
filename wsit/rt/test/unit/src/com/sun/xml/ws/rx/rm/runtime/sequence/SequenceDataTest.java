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
package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.xml.ws.rx.rm.runtime.sequence.invm.InVmSequenceDataLoader;
import com.sun.xml.ws.rx.rm.runtime.sequence.persistent.PersistentSequenceDataLoader;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class SequenceDataTest extends TestCase {

    private static final String EXPECTED_SEQUENCE_ID = "sid_01";
    private static final long EXPECTED_EXPIRY_TIME = -1L;
    private static final String EXPECTED_STR_ID = "str_01";
    //
    private static final Sequence.State INITIAL_STATE = Sequence.State.CREATED;
    private static final boolean INITIAL_ACK_REQUESTED_FLAG = false;
    private static final long INITIAL_LAST_MESSAGE_ID = 10;
    private static final long INITIAL_LAST_ACTIVITY_TIME = 0;
    private static final long INITIAL_LAST_ACKNOWLEDGEMENT_REQUEST_TIME = 0;
    //
    private final SequenceDataLoader[] loaders;
    //
    private SequenceData[] instances;

    public SequenceDataTest(String testName) {
        this.loaders = new SequenceDataLoader[]{
                    new InVmSequenceDataLoader(),
                    new PersistentSequenceDataLoader()
                };
    }

//    @Override
    protected void setUp() throws Exception {
        // TODO:
        // - start database, create tables(, fill in intial data)

        instances = new SequenceData[loaders.length];

        for (int i = 0; i < instances.length; i++) {
            instances[i] = loaders[i].newInstance(
                    EXPECTED_SEQUENCE_ID,
                    EXPECTED_STR_ID,
                    EXPECTED_EXPIRY_TIME,
                    INITIAL_STATE,
                    INITIAL_ACK_REQUESTED_FLAG,
                    INITIAL_LAST_MESSAGE_ID,
                    INITIAL_LAST_ACTIVITY_TIME,
                    INITIAL_LAST_ACKNOWLEDGEMENT_REQUEST_TIME);
        }
    }

//    @Override
    protected void tearDown() throws Exception {
        // TODO
        // - drop tables, stop database
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
     * Test of getLastMessageNumber method, of class SequenceData.
     */
    public void testGetLastMessageNumber() {
        for (SequenceData instance : instances) {
            assertEquals(INITIAL_LAST_MESSAGE_ID, instance.getLastMessageNumber());
        }

        fail("The test case is not implemented yet.");
    }

    /**
     * Test of getAckRequestedFlag method, of class SequenceData.
     */
    public void testGetAckRequestedFlag() {
        for (SequenceData instance : instances) {
            assertEquals(INITIAL_ACK_REQUESTED_FLAG, instance.getAckRequestedFlag());
        }

        fail("The test case is not implemented yet.");
    }

    /**
     * Test of setAckRequestedFlag method, of class SequenceData.
     */
    public void testSetAckRequestedFlag() {
        fail("The test case is not implemented yet.");
    }

    /**
     * Test of getLastAcknowledgementRequestTime method, of class SequenceData.
     */
    public void testGetLastAcknowledgementRequestTime() {
        for (SequenceData instance : instances) {
            assertEquals(INITIAL_LAST_ACKNOWLEDGEMENT_REQUEST_TIME, instance.getLastAcknowledgementRequestTime());
        }

        fail("The test case is not implemented yet.");
    }

    /**
     * Test of setLastAcknowledgementRequestTime method, of class SequenceData.
     */
    public void testSetLastAcknowledgementRequestTime() {
        fail("The test case is not implemented yet.");
    }

    /**
     * Test of getLastActivityTime method, of class SequenceData.
     */
    public void testGetLastActivityTime() {
        for (SequenceData instance : instances) {
            assertEquals(INITIAL_LAST_ACTIVITY_TIME, instance.getLastActivityTime());
        }

        fail("The test case is not implemented yet.");
    }

    /**
     * Test of setLastActivityTime method, of class SequenceData.
     */
    public void testSetLastActivityTime() {
        fail("The test case is not implemented yet.");
    }

    /**
     * Test of getState method, of class SequenceData.
     */
    public void testGetState() {
        for (SequenceData instance : instances) {
            assertEquals(INITIAL_STATE, instance.getState());
        }

        fail("The test case is not implemented yet.");
    }

    /**
     * Test of setState method, of class SequenceData.
     */
    public void testSetState() {
        fail("The test case is not implemented yet.");
    }

    /**
     * Test of incrementAndGetLastMessageNumber method, of class SequenceData.
     */
    public void testIncrementAndGetLastMessageNumber() {
        fail("The test case is not implemented yet.");
    }

    /**
     * Test of registerUnackedMessageNumber method, of class SequenceData.
     */
    public void testRegisterUnackedMessageNumber() throws Exception {
        fail("The test case is not implemented yet.");
    }

    /**
     * Test of markAsAcknowledged method, of class SequenceData.
     */
    public void testMarkAsAcknowledged() {
        fail("The test case is not implemented yet.");
    }

    /**
     * Test of attachMessageToUnackedMessageNumber method, of class SequenceData.
     */
    public void testAttachMessageToUnackedMessageNumber() {
        fail("The test case is not implemented yet.");
    }

    /**
     * Test of retrieveMessage method, of class SequenceData.
     */
    public void testRetrieveMessage() {
        fail("The test case is not implemented yet.");
    }

    /**
     * Test of getUnackedMessageNumbers method, of class SequenceData.
     */
    public void testGetUnackedMessageNumbers() {
        fail("The test case is not implemented yet.");
    }

    /**
     * Test of getLastMessageNumberWithUnackedMessageNumbers method, of class SequenceData.
     */
    public void testGetLastMessageNumberWithUnackedMessageNumbers() {
        fail("The test case is not implemented yet.");
    }
}
