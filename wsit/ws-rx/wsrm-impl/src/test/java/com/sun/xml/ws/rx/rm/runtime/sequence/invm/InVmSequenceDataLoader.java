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

package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceData;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceDataLoader;
import com.sun.xml.ws.rx.util.TimeSynchronizer;
import java.util.HashMap;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class InVmSequenceDataLoader implements SequenceDataLoader {

    public void setUp() {
    }

    public void tearDown() {
    }

    public SequenceData newInstance(boolean isInbound, String sequenceId, String securityContextTokenId, long expirationTime, State state, boolean ackRequestedFlag, long lastMessageId, long lastActivityTime, long lastAcknowledgementRequestTime) {
        SequenceDataPojo sdPojo = new SequenceDataPojo(sequenceId, securityContextTokenId, expirationTime, isInbound, null);
        sdPojo.setState(state);
        sdPojo.setAckRequestedFlag(ackRequestedFlag);
        sdPojo.setLastMessageNumber(lastMessageId);
        sdPojo.setLastActivityTime(lastActivityTime);
        sdPojo.setLastAcknowledgementRequestTime(lastAcknowledgementRequestTime);

        return InVmSequenceData.newInstace(
                sdPojo,
                new TimeSynchronizer() {

                    public long currentTimeInMillis() {
                        return System.currentTimeMillis();
                    }
                },
                new HashMap<String, ApplicationMessage>());
    }
}
