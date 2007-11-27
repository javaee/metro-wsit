/*
 * $Id: Sequence.java,v 1.16 2007-11-27 23:45:43 m_potociar Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rm;

import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.localization.LocalizationMessages;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 *  A Sequence is a sparse array of messages corresponding to an RM Sequence.  It
 *  is implemented as an ArrayList with nulls at unfilled indices.
 */
// TODO check if synchronization is needed on all methods
public class Sequence {

    /**
     * Endpoint for protocol responses.  May be the WS-Addressing anonymous endpoint.
     * There are several variations depending on whether this EPR is the same as 
     * the one used by application messages in the companion <code>InboundSequence</code>
     *  
     * INFO: This field is currently not used by RM runtime.
     */
    private URI acksTo;
    /**
     * Flag that indicates if the CloseSequence message has been sent/received
     * If this is the case then the Sequence needs to be closed and no more messages
     * with that Sequence Id should be accepted
     */
    private boolean closed;
    /**
     * Current sequence configuration
     */
    private SequenceConfig config;
    /**
     * The sequence identifier.
     */
    private String id;
    /**
     * Flag indicates that message with Sequence header containing
     * Last element has been sent/received.  If this is the case,
     * SequenceAcknowledgements for the sequence may contain acks
     * for a message is one greater than the index for the last
     * message in the sequence.
     */
    private boolean last = false;
    /**
     * Last accesse time.
     */
    private long lastActivityTime;
    /**
     * Maximum number of stored messages.  Used for server-side sequences
     * for which flow control is enabled.  The value -1 indicates that
     * there is no limit.
     */
    private int maxMessages = -1;
    /**
     * The smallest unfilled index.
     */
    private int nextIndex = 1;
    /**
     * The underlying list of messages
     */
    private List<RMMessage> rmMessages;
    /**
     * Number of messages currently being stored awaiting completion.
     */
    private int storedMessages = 0;

    public Sequence(SequenceConfig config) {
        this.config = config;

        rmMessages = new ArrayList<RMMessage>();
        //fill in 0-th index that will never be used since
        //messageNumbers are 1-based and we will be keeping
        //messageNumbers in-sync with indices.
        rmMessages.add(null);
        resetLastActivityTime();
    }

    public Sequence(URI acksToUri, SequenceConfig config) {
        this(config);

        this.acksTo = acksToUri;
    }

    public Sequence(URI acksToUri, SequenceConfig config, boolean flag) {        
        this(acksToUri, config);

        // FIXME: provide generally working solution for maxMessages setup:
        // the next few lines of code that setup maxMessages work only when
        // called from ServerInboundSequence. Trying to call these from other 
        // Sequence classes results in a BufferFullException being thrown in 
        // roundtrip scenario from set() method (when processing a response 
        // on the client side).
        if (flag) {
            //if flow control is enabled, set buffer size.
            //don't try to use flow control if ordered delivery
            //is needed.  Even if the buffer is full, we
            //would still need to accept messages that "fill in the gaps"
            if (config.isFlowControlRequired() && !config.isOrdered()) {
                this.maxMessages = config.getBufferSize();
            } else {
                this.maxMessages = -1;
            }
        }
    }

    /**
     * Gets the sequence identifier
     * @return The sequence identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the sequence identifier.
     * @param id The sequence identifier.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Accessor for the nextIndex field.
     * @return The value of the nextIndex field
     */
    public synchronized int getNextIndex() {
        return nextIndex;
    }

    /**
     * Gets the Message at the specified index.
     * @param index The index to access
     * @return The Message at the specified index
     * @throws InvalidMessageNumberException If the index is larger than the largest
     * index used.
     */
    public synchronized RMMessage get(int index) throws InvalidMessageNumberException {
        if (index >= nextIndex) {
            throw new InvalidMessageNumberException(LocalizationMessages.WSRM_1000_INVALID_INDEX_MESSAGE(index));
        }
        return rmMessages.get(index);
    }

    /**
     * Adds a Message to the Sequence at a specified indes.  The index must be
     * positive.  If the index is larger than the nextIndex field, nulls are inserted 
     * between the largest index used and the index, and the index becomes the new
     * value of the nextIndex field
     * @param i The index at which to insert the message.
     * @param m The message to insert
     *
     * @return The new value of nextIndex.
     */
    public synchronized int set(int index, RMMessage rmMessage) throws InvalidMessageNumberException, BufferFullException, DuplicateMessageException {
        //record the index and sequence in the message
        rmMessage.setMessageNumber(index);
        rmMessage.setSequence(this);

        if (index <= 0) {
            throw new InvalidMessageNumberException();
        }

        if (storedMessages == maxMessages) {
            throw new BufferFullException(this);
        }

        if (index < nextIndex) {
            RMMessage mess = null;
            if (null != (mess = rmMessages.get(index)) && !config.isAllowDuplicatesEnabled()) {
                //Store the original message in the exception so
                //that exception handling can use it.
                throw new DuplicateMessageException(mess);
            }
            rmMessages.set(index, rmMessage);
        } else if (index == nextIndex) {
            rmMessages.add(rmMessage);
            nextIndex++;
        } else {
            //fill in nulls between nextIndex an new nextIndex.
            for (int j = nextIndex; j < index; j++) {
                rmMessages.add(null);
            }
            rmMessages.add(rmMessage);
            nextIndex = index + 1;
        }

        storedMessages++;

        return index;
    }

    /**
     * Sets the last flag.
     */
    public synchronized void setLast() {
        last = true;
    }

    /**
     * Sets the last flag.
     */
    public synchronized void setClosed() {
        closed = true;
    }

    /*
     * Gets the value of the last flag.
     *
     * @return The value of the flag.
     */
    public synchronized boolean isLast() {
        return last;
    }

    /*
     * Gets the value of the last flag.
     *
     * @return The value of the flag.
     */
    public synchronized boolean isClosed() {
        return closed;
    }

    /////////////////////////////////////////////////////////////////////////////
    /*
     *             InactivityTimeout management helpers
     *             used by ClientOutboundSequence and ServerInboundSequence
     *
     *////////////////////////////////////////////////////////////////////////////
    /**
     * Resets lastActivityTime field to current time.
     */
    public void resetLastActivityTime() {
        lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Accessor for lastActivityTime field.
     *
     * @return The value of the field.
     */
    protected long getLastActivityTime() {
        return lastActivityTime;
    }

    /**
     * Return value determines whether elapsed time is close enough to
     * time limit to pull the trigger and send an ackRequested to keep the
     * sequence alive.
     *
     * @param elapsedTime Elapsed time since last reset.
     * @param timeLimit Maximum time to wait
     */
    protected boolean isGettingClose(long elapsedTime, long timeLimit) {
        //FIXME for now it's here
        return elapsedTime > timeLimit / 2;
    }

    protected SequenceConfig getConfig() {
        return config;
    }

    protected int getMaxMessages() {
        return maxMessages;
    }

    protected int getStoredMessages() {
        return storedMessages;
    }

    protected void decreaseStoredMessages() {
        storedMessages--;
    }

    /**
     * Accessor for the value of the Destination URI.
     *
     * @return The destination String.
     */
    public URI getAcksTo() {
        return acksTo;
    }

    public void setAcksTo(URI uri) {
        this.acksTo = uri;
    }
}
