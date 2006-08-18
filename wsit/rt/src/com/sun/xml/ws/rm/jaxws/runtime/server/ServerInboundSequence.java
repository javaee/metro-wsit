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

/*
 * ServerInboundSequence.java
 *
 * @author Mike Grogan
 * Created on November 21, 2005, 2:50 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime.server;


import com.sun.xml.ws.rm.BufferFullException;
import com.sun.xml.ws.rm.InvalidMessageNumberException;
import com.sun.xml.ws.rm.Message;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.runtime.InboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;

import javax.xml.ws.addressing.EndpointReference;
import java.util.UUID;

/**
 * An <code>ServerInboundSequence</code> represents a sequence of incoming messages.  For an 
 * <code>RMDestination</code>, an <code>InboundSequnce</code> consists of all
 * the requests to a service from a particular proxy.
 */
public class ServerInboundSequence extends InboundSequence {

    /**
     * Session associated with this sequence.
     */
    private ServerSession session;
    
    public ServerInboundSequence( EndpointReference acksTo, 
                            String outboundId,
                            SequenceConfig config) {
        
        super(acksTo, config);
        this.outboundSequence = new ServerOutboundSequence(this, outboundId, config);
        
        String id = "uuid:" + UUID.randomUUID();
        setId(id);
        session = new ServerSession(id);
        
         //if flow control is enabled, set buffer size
        if (config.flowControl) {
            maxMessages = config.bufferSize;
        } else {
            maxMessages = -1;
        }
    }
    
    /**
     *  Gets the original message in a the Sequence with a given message number.
     *  
     *  @param duplicate Subsequent message with same number.
     *  @return the original message.
     */
    public com.sun.xml.ws.rm.Message getOriginalMessage(com.sun.xml.ws.rm.Message duplicate)
            throws InvalidMessageNumberException {
        
        int number = duplicate.getMessageNumber();
        return get(number);       
    }
    
    /**
     * Accessor for the <code>session</code> field.
     *
     * @return The value of the session field.
     */
    public ServerSession getSession() {
        return session;
    }

    /**
     * Mutator for the <code>session</code> field.
     */
    public void  setSession(ServerSession s) {
        session = s;
    }
    
    /**
     * If ordered delivery is required, resume processing the next Message
     * in the Sequence if it is waiting for this message to be delivered.
     * This method is called after ServerPipe.process returns for this message.
     * while waiting for gaps in the sequence to fill that can now be processed.
     *
     * @param message The message to be processed
     */
    public  void releaseNextMessage(Message message) throws RMException {

        /**
         * Flow Control can be enabled without ordered delivery in which case
         * we want the storedmessages to be decremented 
         */
       /* if (!config.ordered) {
           return;
       }*/
        
       message.complete();
       --storedMessages;
       
       //notify immediate successor if it is waiting 
       int num = message.getMessageNumber();
    
       if (num < nextIndex - 1 && get(num + 1) != null) {
           get(num + 1).resume();
        }
    }
    
     /**
     * If ordered delivery is required, resume processing the next Message
     * in the Sequence if it is waiting for this message to be delivered.
     * This method is called after ServerPipe.process returns for this message.
     * while waiting for gaps in the sequence to fill that can now be processed.
     *
     *@param message The message to  be processed
     */
    public  void holdIfUndeliverable(Message message) 
                        throws BufferFullException {
          if (!config.ordered) {
             return;
         }
          
         try {
            int num = message.getMessageNumber();
       
            //if immediate predecessor has not been processed, wait fcor it
            if (num > 1) {
                Message mess = get(num - 1);
                if (mess == null || !mess.isComplete()) {
                    message.block();
                }
            }
        } catch (InvalidMessageNumberException e) {}
    }
    
    
    /**
     * Return value determines whether the interval since last activity
     * exceeds the inactivity timeout setting.
     *
     * @return true if sequence has expired.
     *         false otherwise.
     */
    public boolean isExpired() {
        
        return System.currentTimeMillis() - this.getLastActivityTime()  >
                config.getInactivityTimeout();
    }
}


