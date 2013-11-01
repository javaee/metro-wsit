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

import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class OutboundSequenceTest extends TestCase {
    private final SequenceManager sequenceManager = SequenceManagerFactory.INSTANCE.createSequenceManager(
            false,
            "0987654321",
            SequenceTestUtils.getDeliveryQueueBuilder(),
            SequenceTestUtils.getDeliveryQueueBuilder(),
            SequenceTestUtils.getConfiguration(),
            Container.NONE,
            null);
    private Sequence sequence;

    public OutboundSequenceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        sequence = sequenceManager.createOutboundSequence(sequenceManager.generateSequenceUID(), null, -1);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        sequenceManager.terminateSequence(sequence.getId());
        super.tearDown();
    }

    public void testRegisterMessage() throws Exception {
        DummyAppMessage message;

        message = new DummyAppMessage("A");
        sequence.registerMessage(message, true);
        assertEquals(sequence.getId(), message.getSequenceId());
        assertEquals(1, message.getMessageNumber());

        message = new DummyAppMessage("B");
        sequence.registerMessage(message, true);
        assertEquals(sequence.getId(), message.getSequenceId());
        assertEquals(2, message.getMessageNumber());

        message = new DummyAppMessage("C");
        sequence.registerMessage(message, true);
        assertEquals(sequence.getId(), message.getSequenceId());
        assertEquals(3, message.getMessageNumber());

        message = new DummyAppMessage("D");
        sequence.registerMessage(message, true);
        assertEquals(sequence.getId(), message.getSequenceId());
        assertEquals(4, message.getMessageNumber());

        message = new DummyAppMessage("E");
        sequence.registerMessage(message, true);
        assertEquals(sequence.getId(), message.getSequenceId());
        assertEquals(5, message.getMessageNumber());
    }

    public void testGetLastMessageId() throws Exception {
        for (int i = 0; i < 4; i++) {
            sequence.registerMessage(new DummyAppMessage("" + i), true);
        }

        assertEquals(4, sequence.getLastMessageNumber());
    }

    public void testPendingAcknowedgements() throws Exception {
        for (int i = 0; i < 5; i++) {
            sequence.registerMessage(new DummyAppMessage("" + i), true);
        }

        assertTrue(sequence.hasUnacknowledgedMessages());

        List<Sequence.AckRange> ackedRages;

        sequence.acknowledgeMessageNumbers(SequenceTestUtils.createAckRanges(1));
        assertTrue(sequence.hasUnacknowledgedMessages());
        ackedRages = sequence.getAcknowledgedMessageNumbers();
        assertEquals(1, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(1, ackedRages.get(0).upper);

        sequence.acknowledgeMessageNumbers(SequenceTestUtils.createAckRanges(1, 2, 4));
        assertTrue(sequence.hasUnacknowledgedMessages());
        ackedRages = sequence.getAcknowledgedMessageNumbers();
        assertEquals(2, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(2, ackedRages.get(0).upper);
        assertEquals(4, ackedRages.get(1).lower);
        assertEquals(4, ackedRages.get(1).upper);

        sequence.acknowledgeMessageNumbers(SequenceTestUtils.createAckRanges(1, 2, 3, 4, 5));
        assertFalse(sequence.hasUnacknowledgedMessages());
        ackedRages = sequence.getAcknowledgedMessageNumbers();
        assertEquals(1, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(5, ackedRages.get(0).upper);

        boolean passed = false;
        try {
            sequence.acknowledgeMessageNumbers(SequenceTestUtils.createAckRanges(1, 2, 3, 4, 5, 6));
        } catch (InvalidAcknowledgementException e) {
            passed = true;
        }
        assertTrue("IllegalMessageIdentifierException expected", passed);
    }
    
    public void testSequenceStatusAfterCloseOperation() throws Exception {
        sequence.close();
        assertEquals(Sequence.State.CLOSED, sequence.getState());
    }

    public void testBehaviorAfterCloseOperation() throws Exception {
        sequence.registerMessage(new DummyAppMessage("A"), true); // 1
        sequence.close();
        assertEquals(Sequence.State.CLOSED, sequence.getState());

        // sequence acknowledgement behavior
        boolean passed = false;
        try {
            sequence.acknowledgeMessageNumbers(SequenceTestUtils.createAckRanges(1)); // ok
            sequence.registerMessage(new DummyAppMessage("B"), true); // error - closed sequence
        } catch (SequenceClosedException e) {
            passed = true;
        }

        // sequence generateNextMessageId behavior
        passed = false;
        try {
            sequence.registerMessage(new DummyAppMessage("B"), true); // error - closed sequence
        } catch (SequenceClosedException e) {
            passed = true;
        }
        assertTrue("Expected exception was not thrown", passed);
    }

    public void testSequenceState() throws Exception {
        Sequence outbound = sequenceManager.createOutboundSequence(sequenceManager.generateSequenceUID(), null, -1);
        assertEquals(Sequence.State.CREATED, outbound.getState());

        outbound.close();
        assertEquals(Sequence.State.CLOSED, outbound.getState());

        sequenceManager.terminateSequence(outbound.getId());
        assertEquals(Sequence.State.TERMINATING, outbound.getState());
    }

    public void testStoreAndRetrieveMessage() throws Exception {
        Map<String, ApplicationMessage> correlatedMessageMap = new HashMap<String, ApplicationMessage>();
        for (int i = 0; i < 3; i++) {
            ApplicationMessage message = new DummyAppMessage("" + i);
            sequence.registerMessage(message, true);
            correlatedMessageMap.put(message.getCorrelationId(), message);
        }

        System.gc();

        for (Map.Entry<String, ApplicationMessage> entry : correlatedMessageMap.entrySet()) {
            Object actual = sequence.retrieveMessage(entry.getKey());
            assertEquals("Retrieved message is not the same as stored message", entry.getValue(), actual);
        }
        /*
        System.gc();
        Thread.sleep(2000);
        System.gc();

        for (i = 0; i < messages.length; i++) {
        Object actual = outboundSequence.retrieveMessage(i + 1);
        assertEquals("Retrieved message is not the same as stored message", null, actual);
        }      
         */
    }

    public void testSequenceExpiry() throws Exception {
        Sequence inbound = sequenceManager.createInboundSequence(
        sequenceManager.generateSequenceUID(),
        null,
        System.currentTimeMillis() + 1000);
        
        assertFalse(inbound.isExpired());

        Thread.sleep(1100);
        assertTrue(inbound.isExpired());
    }
}
