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
 * @author m_potociar
 */
public class SequenceTest extends TestCase {

    private SequenceManager sequenceManager = SequenceManagerFactory.getInstance().getSequenceManager();
    private Sequence inboundSequnce;
    private Sequence outboundSequence;

    public SequenceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        inboundSequnce = sequenceManager.createInboundSequence(sequenceManager.generateSequenceUID(), null, -1);
        outboundSequence = sequenceManager.createOutboundSequence(sequenceManager.generateSequenceUID(), null, -1);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        sequenceManager.terminateSequence(inboundSequnce.getId());
        sequenceManager.terminateSequence(outboundSequence.getId());
        super.tearDown();
    }

    public void testGetNextMessageId() throws Exception {
        boolean passed = false;
        try {
            inboundSequnce.getNextMessageId();
        } catch (UnsupportedOperationException e) {
            passed = true;
        }
        assertTrue("Inbound sequence should throw exception when getNextMessageId() is invoked", passed);

        assertEquals(1, outboundSequence.getNextMessageId());
        assertEquals(2, outboundSequence.getNextMessageId());
        assertEquals(3, outboundSequence.getNextMessageId());
        assertEquals(4, outboundSequence.getNextMessageId());
        assertEquals(5, outboundSequence.getNextMessageId());
    }

    public void testGetLastMessageId() throws Exception {
        inboundSequnce.acknowledgeMessageId(1);
        inboundSequnce.acknowledgeMessageId(2);
        inboundSequnce.acknowledgeMessageId(3);
        inboundSequnce.acknowledgeMessageId(4);

        assertEquals(4, inboundSequnce.getLastMessageId());

        outboundSequence.getNextMessageId(); // 1
        outboundSequence.getNextMessageId(); // 2
        outboundSequence.getNextMessageId(); // 3
        outboundSequence.getNextMessageId(); // 4

        assertEquals(4, outboundSequence.getLastMessageId());
    }

    public void testInboundSequencePendingAcknowedgements() throws Exception {
        assertFalse(
                "Inbound sequence may not have pending acknowledgemets",
                inboundSequnce.hasPendingAcknowledgements());

        List<Sequence.AckRange> ackedRages;


        inboundSequnce.acknowledgeMessageId(1);
        ackedRages = inboundSequnce.getAcknowledgedMessageIds();
        assertEquals(1, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(1, ackedRages.get(0).upper);

        inboundSequnce.acknowledgeMessageIds(Arrays.asList(new Sequence.AckRange[]{
                    new Sequence.AckRange(2, 2),
                    new Sequence.AckRange(4, 5)
                }));
        ackedRages = inboundSequnce.getAcknowledgedMessageIds();
        assertEquals(2, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(2, ackedRages.get(0).upper);
        assertEquals(4, ackedRages.get(1).lower);
        assertEquals(5, ackedRages.get(1).upper);

        inboundSequnce.acknowledgeMessageId(3);
        ackedRages = inboundSequnce.getAcknowledgedMessageIds();
        assertEquals(1, ackedRages.size());
        assertEquals(1, ackedRages.get(0).lower);
        assertEquals(5, ackedRages.get(0).upper);
        
        boolean passed = false;
        try {
            inboundSequnce.acknowledgeMessageId(4); // duplicate message acknowledgement
        } catch (IllegalMessageIdentifierException e) {
            passed = true;
        }
        assertTrue("IllegalMessageIdentifierException expected", passed);

        passed = false;
        try {
        // duplicate message acknowledgement
        inboundSequnce.acknowledgeMessageIds(Arrays.asList(new Sequence.AckRange[]{
                    new Sequence.AckRange(2, 2),
                    new Sequence.AckRange(4, 5)
                }));
        } catch (IllegalMessageIdentifierException e) {
            passed = true;
        }
        assertTrue("IllegalMessageIdentifierException expected", passed);        
    }

    public void testOutboundSequencePendingAcknowedgements() throws Exception {
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
}
