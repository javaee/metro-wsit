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

import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class OutboundSequenceTest extends TestCase {

    private SequenceManager sequenceManager = SequenceManagerFactory.getInstance().getSequenceManager();
    private Sequence outboundSequence;

    public OutboundSequenceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        outboundSequence = sequenceManager.createOutboundSequence(sequenceManager.generateSequenceUID(), null, -1);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        sequenceManager.terminateSequence(outboundSequence.getId());
        super.tearDown();
    }

    public void testGetNextMessageId() throws Exception {
        assertEquals(1, outboundSequence.getNextMessageId());
        assertEquals(2, outboundSequence.getNextMessageId());
        assertEquals(3, outboundSequence.getNextMessageId());
        assertEquals(4, outboundSequence.getNextMessageId());
        assertEquals(5, outboundSequence.getNextMessageId());
    }

    public void testGetLastMessageId() throws Exception {
        outboundSequence.getNextMessageId(); // 1
        outboundSequence.getNextMessageId(); // 2
        outboundSequence.getNextMessageId(); // 3
        outboundSequence.getNextMessageId(); // 4

        assertEquals(4, outboundSequence.getLastMessageId());
    }

    public void testPendingAcknowedgements() throws Exception {
        outboundSequence.getNextMessageId(); // 1 
        outboundSequence.getNextMessageId(); // 2 
        outboundSequence.getNextMessageId(); // 3 
        outboundSequence.getNextMessageId(); // 4 
        outboundSequence.getNextMessageId(); // 5 

        assertTrue(outboundSequence.hasPendingAcknowledgements());

        List<Sequence.AckRange> ackedRages;

        outboundSequence.acknowledgeMessageId(1);
        assertTrue(outboundSequence.hasPendingAcknowledgements());
        ackedRages = outboundSequence.getAcknowledgedMessageIds();
        assertEquals(1, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(1, ackedRages.get(0).upper);

        outboundSequence.acknowledgeMessageIds(Arrays.asList(new Sequence.AckRange[]{
                    new Sequence.AckRange(1, 2),
                    new Sequence.AckRange(4, 4),
                }));
        assertTrue(outboundSequence.hasPendingAcknowledgements());
        ackedRages = outboundSequence.getAcknowledgedMessageIds();
        assertEquals(2, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(2, ackedRages.get(0).upper);
        assertEquals(4, ackedRages.get(1).lower);
        assertEquals(4, ackedRages.get(1).upper);

        outboundSequence.acknowledgeMessageIds(Arrays.asList(new Sequence.AckRange[]{
                    new Sequence.AckRange(1, 5)
                }));
        assertFalse(outboundSequence.hasPendingAcknowledgements());
        ackedRages = outboundSequence.getAcknowledgedMessageIds();
        assertEquals(1, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(5, ackedRages.get(0).upper);

        boolean passed = false;
        try {
            outboundSequence.acknowledgeMessageIds(Arrays.asList(new Sequence.AckRange[]{
                        new Sequence.AckRange(1, 6)
                    }));
        } catch (IllegalMessageIdentifierException e) {
            passed = true;
        }
        assertTrue("IllegalMessageIdentifierException expected", passed);

        passed = false;
        try {
            outboundSequence.acknowledgeMessageId(6);
        } catch (IllegalMessageIdentifierException e) {
            passed = true;
        }
        assertTrue("IllegalMessageIdentifierException expected", passed);
    }

    public void testSequenceStatusAfterCloseOperation() throws Exception {
        outboundSequence.close();
        assertEquals(Sequence.Status.CLOSED, outboundSequence.getStatus());
    }

    public void testBehaviorAfterCloseOperation() throws Exception {
        outboundSequence.getNextMessageId(); // 1
        outboundSequence.close();
        assertEquals(Sequence.Status.CLOSED, outboundSequence.getStatus());

        // sequence acknowledgement behavior
        outboundSequence.acknowledgeMessageId(1); // ok

        // sequence getNextMessageId behavior
        boolean passed = false;
        try {
            outboundSequence.getNextMessageId(); // error        
        } catch (IllegalStateException e) {
            passed = true;
        }
        assertTrue("Expected exception was not thrown", passed);
    }
    
    
    public void testStatus() throws Exception {
        Sequence inbound = sequenceManager.createInboundSequence(sequenceManager.generateSequenceUID(), null, -1);
        assertEquals(Sequence.Status.CREATED, inbound.getStatus());

        // TODO test closing
        
        inbound.close();
        assertEquals(Sequence.Status.CLOSED, inbound.getStatus());   
        
        sequenceManager.terminateSequence(inbound.getId());
        assertEquals(Sequence.Status.TERMINATING, inbound.getStatus());           
    }    
}
