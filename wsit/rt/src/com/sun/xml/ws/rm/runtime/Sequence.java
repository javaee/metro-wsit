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
import java.util.Collection;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public interface Sequence {
    public static final long MIN_MESSAGE_ID = 1;
    public static final long MAX_MESSAGE_ID = 9223372036854775807L;

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

    /**
     * Returns unique identifier of the sequence
     * 
     * @return unique sequence identifier
     */
    public String getId();

    /**
     * Generates a new message identifier and registers it within the sequence
     * 
     * @return the next message identifier that should be used for the next message sent on the sequence.
     * 
     * @exception MessageNumberRolloverException in case the message identifier counter overflows
     */
    public long getNextMessageId() throws MessageNumberRolloverException;

    /**
     * Registers given message identifier with the sequence as aknowledged
     * 
     * @param messageId message identifier to be acknowledged
     * 
     * @exception IllegalMessageIdentifierException in case this is an {@link InboundSequence} instance and a message 
     * with the given identifier has been already registered
     */
    public void acknowledgeMessageId(long messageId) throws IllegalMessageIdentifierException;

    /**
     * Provides information on the last message id sent on this sequence
     * 
     * @return last message identifier registered on this sequence
     */
    public long getLastMessageId();

    /**
     * Provides a collection of ranges of messages identifier acknowledged with the sequence
     * 
     * @return collection of ranges of messages identifier registered with the sequence
     */
    public Collection<AckRange> getAcknowledgedMessageIds();

    /**
     * The method may be called to determine whether the sequence has some unacknowledged messages or not
     * 
     * @return {@code true} if the sequence has any unacknowledged message identifiers, {@code false} otherwise
     */
    public boolean hasPendingAcknowledgements();

    /**
     * Provides information on the status of the message sequence
     * 
     * @return current status of the message sequence
     */
    public Status getStatus();

    /**
     * This method should be called to set the AckRequested flag, which indicates a pending request for acknowledgement of all
     * message identifiers registered with this sequence. The flag is automatically cleared once {@link #getAcknowledgedMessageIds()}
     * method is called.
     */
    public void setAckRequestedFlag();

    /**
     * Provides information on the actual AckRequested flag status
     * 
     * @return {@code true} if the AckRequested flag is set, {@code false} otherwise
     */
    public boolean isAckRequested();
    
    /**
     * Closes the session. Subsequent calls to this method have no effect.
     * <p>
     * Once this method is called, any subsequent calls to the {@link #getNextMessageId()} method will
     * result in a {@link IllegalStateException} being raised. It is however still possible to accept message identifier 
     * acknowledgements, as well as retrieve any other information on the sequence.
     */
    public void close();

    /**
     * Provides information on the session closed status.
     * 
     * @return {@code true} if the session has been closed, {@code false} otherwise
     */
    public boolean isClosed();

    /**
     * Provides information on the session expiration status.
     * 
     * @return {@code true} if the session has already expired, {@code false} otherwise
     */
    public boolean isExpired();

    /**
     * The method is called during the sequence termination to allow sequence object to release its allocated resources
     */
    public void preDestroy();
}
