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

import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class InboundSequenceTest extends TestCase {

    private SequenceManager sequenceManager = SequenceManagerFactory.INSTANCE.getClientSequenceManager(null);
    private Sequence sequence;

    public InboundSequenceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        sequence = sequenceManager.createInboundSequence(sequenceManager.generateSequenceUID(), null, Sequence.NO_EXPIRATION, null);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        sequenceManager.terminateSequence(sequence.getId());
        super.tearDown();
    }

    public void testRegisterMessage() throws Exception {
        for (int i = 1; i <= 5; i++) {
            DummyAppMessage message = new DummyAppMessage(sequence.getId(), i, null, null, false, "" + i);
            sequence.registerMessage(message, true);
            assertEquals(sequence.getId(), message.getSequenceId());
            assertEquals(1, message.getMessageNumber());

        }
    }

    public void testGetLastMessageId() throws Exception {
        for (int i = 1; i <= 5; i++) {
            sequence.registerMessage(new DummyAppMessage(sequence.getId(), i, null, null, false, "" + i), true);
        }
        assertEquals(5, sequence.getLastMessageId());

        DummyAppMessage message = new DummyAppMessage(sequence.getId(), 10, null, null, false, "" + 10);
        sequence.registerMessage(message, true);
        assertEquals(10, sequence.getLastMessageId());

    }

    public void testPendingAcknowedgements() throws Exception {
        assertFalse(sequence.hasUnacknowledgedMessages());


        sequence.registerMessage(new DummyAppMessage(sequence.getId(), 1, null, null, false, "A"), true);
        assertTrue(sequence.hasUnacknowledgedMessages());

        sequence.acknowledgeMessageId(1);
        assertFalse(sequence.hasUnacknowledgedMessages());

        List<Sequence.AckRange> ackedRages;
        ackedRages = sequence.getAcknowledgedMessageIds();
        assertEquals(1, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(1, ackedRages.get(0).upper);

        for (int i = 2; i <= 5; i++) {
            sequence.registerMessage(new DummyAppMessage(sequence.getId(), i, null, null, false, "" + i), true);
        }
        sequence.acknowledgeMessageIds(Arrays.asList(new Sequence.AckRange[]{
                    new Sequence.AckRange(2, 2),
                    new Sequence.AckRange(4, 5)
                }));
        assertTrue(sequence.hasUnacknowledgedMessages());

        ackedRages = sequence.getAcknowledgedMessageIds();
        assertEquals(2, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(2, ackedRages.get(0).upper);
        assertEquals(4, ackedRages.get(1).lower);
        assertEquals(5, ackedRages.get(1).upper);

        sequence.acknowledgeMessageId(3);
        assertFalse(sequence.hasUnacknowledgedMessages());
        ackedRages = sequence.getAcknowledgedMessageIds();
        assertEquals(1, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(5, ackedRages.get(0).upper);

        boolean passed = false;
        try {
            sequence.acknowledgeMessageId(4); // duplicate message acknowledgement
        } catch (IllegalMessageIdentifierException e) {
            passed = true;
        }
        assertTrue("IllegalMessageIdentifierException expected", passed);

        passed = false;
        try {
            // duplicate message acknowledgement
            sequence.acknowledgeMessageIds(Arrays.asList(new Sequence.AckRange[]{
                        new Sequence.AckRange(2, 2),
                        new Sequence.AckRange(4, 5)
                    }));
        } catch (IllegalMessageIdentifierException e) {
            passed = true;
        }
        assertTrue("IllegalMessageIdentifierException expected", passed);
    }

    public void testIsAcknowledged() throws Exception {
        sequence.registerMessage(new DummyAppMessage(sequence.getId(), 1, null, null, false, "A"), true);
        sequence.registerMessage(new DummyAppMessage(sequence.getId(), 2, null, null, false, "B"), true);
        sequence.registerMessage(new DummyAppMessage(sequence.getId(), 4, null, null, false, "D"), true);
        
        assertTrue(sequence.isAcknowledged(1));
        assertTrue(sequence.isAcknowledged(2));
        assertFalse(sequence.isAcknowledged(3));
        assertTrue(sequence.isAcknowledged(4));
        assertFalse(sequence.isAcknowledged(5));
    }
    
    public void testBehaviorAfterCloseOperation() throws Exception {
        sequence.registerMessage(new DummyAppMessage(sequence.getId(), 1, null, null, false, "A"), true);
        sequence.registerMessage(new DummyAppMessage(sequence.getId(), 2, null, null, false, "B"), true);
        sequence.registerMessage(new DummyAppMessage(sequence.getId(), 4, null, null, false, "D"), true);

        sequence.close();

        // sequence acknowledgement behavior
        boolean passed = false;
        try {
            sequence.registerMessage(new DummyAppMessage(sequence.getId(), 3, null, null, false, "C"), true); // error
        } catch (IllegalStateException e) {
            passed = true;
        }
        assertTrue("Expected exception was not thrown", passed);

        passed = false;
        try {
            sequence.acknowledgeMessageId(1); // error
        } catch (IllegalStateException e) {
            passed = true;
        }
        assertTrue("Expected exception was not thrown", passed);
    }

    public void testStatus() throws Exception {
        Sequence inbound = sequenceManager.createInboundSequence(sequenceManager.generateSequenceUID(), null, Sequence.NO_EXPIRATION, null);
        assertEquals(Sequence.Status.CREATED, inbound.getStatus());

        // TODO test closing

        inbound.close();
        assertEquals(Sequence.Status.CLOSED, inbound.getStatus());

        sequenceManager.terminateSequence(inbound.getId());
        assertEquals(Sequence.Status.TERMINATING, inbound.getStatus());
    }

    public void testStoreAndRetrieveMessage() throws Exception {
        Map<String, ApplicationMessage> correlatedMessageMap = new HashMap<String, ApplicationMessage>();
        for (int i = 0; i < 3; i++) {
            ApplicationMessage message = new DummyAppMessage(sequence.getId(), i, null, null, false, "" + i);
            sequence.registerMessage(message, true);
            correlatedMessageMap.put(message.getCorrelationId(), message);
        }

        System.gc();

        for (Map.Entry<String, ApplicationMessage> entry : correlatedMessageMap.entrySet()) {
            Object actual = sequence.retrieveMessage(entry.getKey());
            assertEquals("Retrieved message is not the same as stored message", entry.getValue(), actual);
            sequence.acknowledgeMessageId(entry.getValue().getMessageNumber());
        }
    }
}
