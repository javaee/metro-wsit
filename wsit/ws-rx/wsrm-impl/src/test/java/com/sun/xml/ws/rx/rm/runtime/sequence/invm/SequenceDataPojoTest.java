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

package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class SequenceDataPojoTest extends TestCase {
    
    public SequenceDataPojoTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSerialization() throws Exception {
        SequenceDataPojo original = new SequenceDataPojo("sequenceId", "boundTokenId", 1111, true, null);
        original.setAckRequestedFlag(true);
        original.setLastAcknowledgementRequestTime(2222);
        original.setLastActivityTime(3333);
        original.setLastMessageNumber(4444);
        original.setState(State.CLOSED);
        original.getReceivedUnackedMessageNumbers().add(Long.valueOf(1));
        original.getReceivedUnackedMessageNumbers().add(Long.valueOf(2));
        original.getReceivedUnackedMessageNumbers().add(Long.valueOf(3));
        original.getUnackedNumberToCorrelationIdMap().put(Long.valueOf(1), "1");
        original.getUnackedNumberToCorrelationIdMap().put(Long.valueOf(2), "2");
        original.getUnackedNumberToCorrelationIdMap().put(Long.valueOf(3), "3");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        Object _replica = ois.readObject();
        ois.close();

        assertTrue("Unexpected replica class: " + _replica.getClass(), _replica instanceof SequenceDataPojo);
        SequenceDataPojo replica = (SequenceDataPojo) _replica;

        assertEquals("Original and replica are expected to be equal", original, replica);
        assertEquals(original.getAckRequestedFlag(), replica.getAckRequestedFlag());
        assertEquals(original.getAllUnackedMessageNumbers(), replica.getAllUnackedMessageNumbers());
        assertEquals(original.getBoundSecurityTokenReferenceId(), replica.getBoundSecurityTokenReferenceId());
        assertEquals(original.getExpirationTime(), replica.getExpirationTime());
        assertEquals(original.getLastAcknowledgementRequestTime(), replica.getLastAcknowledgementRequestTime());
        assertEquals(original.getLastActivityTime(), replica.getLastActivityTime());
        assertEquals(original.getLastMessageNumber(), replica.getLastMessageNumber());
        assertEquals(original.getReceivedUnackedMessageNumbers(), replica.getReceivedUnackedMessageNumbers());
        assertEquals(original.getSequenceId(), replica.getSequenceId());
        assertEquals(original.getState(), replica.getState());
        assertEquals(original.getUnackedNumberToCorrelationIdMap(), replica.getUnackedNumberToCorrelationIdMap());
    }

}
