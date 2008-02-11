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
package com.sun.xml.ws.rm.runtime;

import com.sun.xml.ws.rm.MessageNumberRolloverException;
import com.sun.xml.ws.rm.localization.RmLogger;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class InboundSequence extends AbstractSequence {
    private static final RmLogger LOGGER = RmLogger.getLogger(InboundSequence.class);
    
    private final Set<Long> ackedIndexes;

    public InboundSequence(String id, long expirationTime) {
        super(id, expirationTime);
        this.ackedIndexes = new TreeSet<Long>();
    }

    public long getNextMessageId() throws MessageNumberRolloverException {
        // TODO L10N
        throw new UnsupportedOperationException("This operation is not supported in this Sequence implementation.");
    }

    public long getLastMessageId() {
        // TODO L10N
        throw new UnsupportedOperationException("This operation is not supported in this Sequence implementation.");
    }

    public Collection<AckRange> getAcknowledgedMessageIds() {
        if (ackedIndexes.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<AckRange> result = new LinkedList<Sequence.AckRange>();

        long lower = ackedIndexes.iterator().next();
        long lastIndex = lower;
        for (long index : ackedIndexes) {
            if (index > lastIndex + 1) {
                result.add(new AckRange(lower, lastIndex));
                lower = index;
            }
            lastIndex = index;
        }
        result.add(new AckRange(lower, lastIndex));

        return result;
    }

    public boolean hasPendingAcknowledgements() {
        // TODO implement
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // TODO decide if e need two methods or one is enough: 
    //   1. we need to track received messages on the inbound sequence and throw exception if duplicate message number occurs. 
    //      we need to be able to send acknowledgements for the received messages
    //   2. we need to track sent messages on the outbound sequence and resend any unacked message
    //      we need to be able to mark messages as acknowledged
    public void acknowledgeMessageId(long messageIdentifier) throws IllegalMessageIdentifierException {
        // TODO implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    public void registerMessageId(long messageIdentifier) throws IllegalMessageIdentifierException {
        if (!ackedIndexes.add(messageIdentifier)) {
            throw LOGGER.logSevereException(new IllegalMessageIdentifierException(messageIdentifier));
        }
    }
}
