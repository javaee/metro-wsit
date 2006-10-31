/*
 * $Id: Sequence.java,v 1.4 2006-10-31 22:14:08 mikeg Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */


package com.sun.xml.ws.rm;
import java.util.ArrayList;

/**
 *  A Sequence is a sparse array of messages corresponding to an RM Sequence.  It
 *  is implemented as an ArrayList with nulls at unfilled indices.
 */

public class Sequence {
   
    /**
     * The sequence identifier.
     */
    protected String id;
    
    /**
     * The underlying list of messages
     */
    protected ArrayList<Message> list;
    
    /**
     * The smallest unfilled index.
     */
    protected int nextIndex = 1;
    
    /**
     * Flag indicates that message with Sequence header containing
     * Last element has been sent/received.  If this is the case,
     * SequenceAcknowledgements for the sequence may contain acks
     * for a message is one greater than the index for the last
     * message in the sequence.
     */
    protected boolean last = false;
    
    /**
     * Maximum number of stored messages.  Used for server-side sequences
     * for which flow control is enabled.  The value -1 indicates that
     * there is no limit.
     */
    protected int maxMessages = -1;
    
    
    /**
     * Number of messages currently being stored awaiting completion.
     */
    protected int storedMessages = 0;
    
    
    /**
     * Last accesse time.
     */
    protected long lastActivityTime;
    
    protected boolean allowDuplicates;

    /**
     * RMConstants associated with each Sequence which
     * will give information regarding addressing version, JAXBContext etc
     *
     */
    protected  RMConstants rmConstants;
    
   
    
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
    
    public Sequence() {
        
        list = new ArrayList<Message>();
        //fill in 0-th index that will never be used since
        //messageNumbers are 1-based and we will be keeping
        //messageNumbers in-sync with indices.
        list.add(null);
        allowDuplicates = false;
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
    public synchronized Message get(int index)
                throws InvalidMessageNumberException {
        
        if (index >= nextIndex) {
            throw new InvalidMessageNumberException("Invalid index -  " + index);
        }
        return list.get(index);
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
    public synchronized int set(int i, Message m) 
            throws InvalidMessageNumberException, 
                    BufferFullException,
                    DuplicateMessageException {
       
        if (i <= 0) {
            throw new InvalidMessageNumberException();
        }
        
        if (storedMessages == maxMessages) {
            throw new BufferFullException(this);
        }
        
        if (i < nextIndex) {
            Message mess = null;
            if (null != (mess = list.get(i))  && !allowDuplicates) {
                //Store the original message in the exception so
                //that exception handling can use it.
                throw new DuplicateMessageException(mess);
            }
            
            list.set(i, m);
        } else if (i == nextIndex) {
            list.add( m);
            nextIndex++;
        } else {
            //fill in nulls between nextIndex an new nextIndex.
            for (int j = nextIndex; j < i; j++) {
                list.add(null);
            }
            list.add( m);
            nextIndex = i + 1;
        }
       
        //record the index in the message
        m.setMessageNumber(i);
        m.setSequence(this);
        
        storedMessages++;
        
        return i;
    }
    
   
    
    /**
     * Sets the last flag.
     */
    public synchronized void setLast() {
        last = true;
    }
    
    /*
     * Gets the value of the last flag.
     *
     * @return The value of the flag.
     */
    public synchronized boolean isLast() {
        return last;
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
        //for now
        return elapsedTime > timeLimit / 2; 
    }
    
  
}
