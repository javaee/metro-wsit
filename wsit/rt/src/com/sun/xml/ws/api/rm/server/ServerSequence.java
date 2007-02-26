/*
 * ServerSequence.java
 *
 *
 * @author Mike Grogan
 * Created on January 19, 2007, 11:57 AM
 *
 */

package com.sun.xml.ws.api.rm.server;
import com.sun.xml.ws.api.rm.Sequence;

/**
 * Abstraction of a server-side sequence that is returned by the
 * createSequence method of {@link ServerSequenceFactory}, which is used
 * to reinitialize sequences using persisted data after a restart.
 */
public interface ServerSequence extends Sequence {
        
     /**
     * Used to re-populate a sequence with persisted messages
     * after a restart.
     *
     * @param index The index to add message at.
     * @param message The JAX-WS message to add
     * @param complete Indicates whether to mark the message
     *          as complete.
     */
    public void resetMessage(int index, 
            com.sun.xml.ws.api.message.Message message, 
            boolean complete);
}
