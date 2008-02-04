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

import java.util.Collection;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public interface Sequence {
    public enum Status {
        CREATING,
        CREATED,
        CLOSING,
        CLOSED,
        TERMINATING
    }
    
    public class AckRange {
        public final long lower;
        public final long upper;
        
        public AckRange(long lower, long upper) {
            this.lower = lower;
            this.upper = upper;
        }
    }
/*
 * create sequence:
 * 1. assign new unique sequence id
 * 
 * process outgoing application message:
 * 1. add sequence id + message id headers
 * 2. add inbound message acknowledgement headers
 * 3. send message
 * 
 * process incomming application message:
 * 1. get inbound sequence
 * 2. check if duplicate (yes => stop processing)
 * 3. send back acknowledgement
 * 
 * 
 * RM session:
 * - create/close sequence
 * - hold incomming & outgoing sequences
 * - start timer tasks
 * 
 * RM outgoing sequence:
 * - hold sequence id
 * - hold next message id
 * - hold unacked messages
 * 
 * RM incomming sequence:
 * - hold sequence id
 * - hold acked message ranges
 * - buffer messages if ordered delivery si required
 * - filter duplicate messages
 */
    
    

    /**
     * Initializes a sequence and allocates all necessary resources
     */
    public void initialize();
    
    /**
     * Releases all resources associated with the sequence and closes the sequence
     */
    public void close();

    /**
     * Returns unique identifier of the sequence
     * 
     * @return unique sequence identifier
     */
    public String getId();
    
    /**
     * Provides the next message identifier within the sequence
     * 
     * @return the next message identifier that should be used for the next message sent on the sequence.
     */
    public long getNextMessageId();

    /**
     * Provides information on the last message id sent on this sequence
     * 
     * @return last message identifier registered on this sequence
     */
    public long getLastMessageId();
    
    /**
     * TODO javadoc
     * @return
     */
    public Collection<AckRange> getAcknowledgedIndexes();
    
    /**
     * TODO javadoc
     * @return
     */
    public Status getStatus();
    
    /**
     * TODO javadoc
     * @return
     */
    public boolean isClosed();
    
}
