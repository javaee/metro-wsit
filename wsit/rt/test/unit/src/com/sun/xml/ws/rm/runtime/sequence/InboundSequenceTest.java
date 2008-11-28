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
package com.sun.xml.ws.rm.runtime.sequence;

import com.sun.xml.ws.rm.runtime.Configuration;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class InboundSequenceTest extends TestCase {

    private SequenceManager sequenceManager = SequenceManagerFactory.INSTANCE.getClientSequenceManager();
    private Sequence inboundSequence;

    public InboundSequenceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        inboundSequence = sequenceManager.createInboundSequence(sequenceManager.generateSequenceUID(), null, Sequence.NO_EXPIRATION);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        sequenceManager.terminateSequence(inboundSequence.getId());
        super.tearDown();
    }

    public void testGenerateNextMessageId() throws Exception {
        boolean passed = false;
        try {
            inboundSequence.generateNextMessageId();
        } catch (UnsupportedOperationException e) {
            passed = true;
        }
        assertTrue("Inbound sequence should throw exception when getNextMessageId() is invoked", passed);
    }

    public void testGetLastMessageId() throws Exception {
        inboundSequence.acknowledgeMessageId(1);
        inboundSequence.acknowledgeMessageId(2);
        inboundSequence.acknowledgeMessageId(3);
        inboundSequence.acknowledgeMessageId(4);

        assertEquals(4, inboundSequence.getLastMessageId());
    }

    public void testPendingAcknowedgements() throws Exception {
        assertFalse(inboundSequence.hasPendingAcknowledgements());

        List<Sequence.AckRange> ackedRages;


        inboundSequence.acknowledgeMessageId(1);
        assertFalse(inboundSequence.hasPendingAcknowledgements());
        ackedRages = inboundSequence.getAcknowledgedMessageIds();
        assertEquals(1, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(1, ackedRages.get(0).upper);

        inboundSequence.acknowledgeMessageIds(Arrays.asList(new Sequence.AckRange[]{
                    new Sequence.AckRange(2, 2),
                    new Sequence.AckRange(4, 5)
                }));
        assertTrue(inboundSequence.hasPendingAcknowledgements());
        ackedRages = inboundSequence.getAcknowledgedMessageIds();
        assertEquals(2, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(2, ackedRages.get(0).upper);
        assertEquals(4, ackedRages.get(1).lower);
        assertEquals(5, ackedRages.get(1).upper);

        inboundSequence.acknowledgeMessageId(3);
        assertFalse(inboundSequence.hasPendingAcknowledgements());
        ackedRages = inboundSequence.getAcknowledgedMessageIds();
        assertEquals(1, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(5, ackedRages.get(0).upper);

        boolean passed = false;
        try {
            inboundSequence.acknowledgeMessageId(4); // duplicate message acknowledgement
        } catch (IllegalMessageIdentifierException e) {
            passed = true;
        }
        assertTrue("IllegalMessageIdentifierException expected", passed);

        passed = false;
        try {
            // duplicate message acknowledgement
            inboundSequence.acknowledgeMessageIds(Arrays.asList(new Sequence.AckRange[]{
                        new Sequence.AckRange(2, 2),
                        new Sequence.AckRange(4, 5)
                    }));
        } catch (IllegalMessageIdentifierException e) {
            passed = true;
        }
        assertTrue("IllegalMessageIdentifierException expected", passed);
    }

    public void testIsAcknowledged() {
        inboundSequence.acknowledgeMessageId(1);
        inboundSequence.acknowledgeMessageId(2);
        inboundSequence.acknowledgeMessageId(4);
        
        assertTrue(inboundSequence.isAcknowledged(1));
        assertTrue(inboundSequence.isAcknowledged(2));
        assertFalse(inboundSequence.isAcknowledged(3));
        assertTrue(inboundSequence.isAcknowledged(4));
        assertFalse(inboundSequence.isAcknowledged(5));
    }
    
    public void testBehaviorAfterCloseOperation() throws Exception {
        inboundSequence.acknowledgeMessageId(1);
        inboundSequence.acknowledgeMessageId(2);
        inboundSequence.acknowledgeMessageId(4);

        inboundSequence.close();

        // sequence acknowledgement behavior
        boolean passed = false;
        try {
            inboundSequence.acknowledgeMessageId(3); // error        
        } catch (IllegalStateException e) {
            passed = true;
        }
        assertTrue("Expected exception was not thrown", passed);

        passed = false;
        try {
            inboundSequence.acknowledgeMessageId(5); // error        
        } catch (IllegalStateException e) {
            passed = true;
        }
        assertTrue("Expected exception was not thrown", passed);
    }

    public void testStatus() throws Exception {
        Sequence inbound = sequenceManager.createInboundSequence(sequenceManager.generateSequenceUID(), null, Sequence.NO_EXPIRATION);
        assertEquals(Sequence.Status.CREATED, inbound.getStatus());

        // TODO test closing

        inbound.close();
        assertEquals(Sequence.Status.CLOSED, inbound.getStatus());

        sequenceManager.terminateSequence(inbound.getId());
        assertEquals(Sequence.Status.TERMINATING, inbound.getStatus());
    }

    public void testStoreAndRetrieveMessage() {
        try {
            inboundSequence.storeMessage(1, 1, new Object());
            fail("UnsupportedOperationException was expected to be thrown");
        } catch (UnsupportedOperationException e) {
        }
        try {
            inboundSequence.retrieveMessage(1);
            fail("UnsupportedOperationException was expected to be thrown");
        } catch (UnsupportedOperationException e) {
        }
    }
}
