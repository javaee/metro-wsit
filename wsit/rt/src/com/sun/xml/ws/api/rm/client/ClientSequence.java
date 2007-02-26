/*
 * ClientSequence.java
 *
 *
 * @author Mike Grogan
 * Created on January 19, 2007, 10:07 AM
 *
 */

package com.sun.xml.ws.api.rm.client;
import com.sun.xml.ws.api.rm.Sequence;
import com.sun.xml.ws.api.rm.AcknowledgementListener;

/**
 * Abstraction of ClientOutboundSequence, which is returned by the <code>createSequence</code>
 * methods of {@link ClientSequenceFactory}
 */
public interface ClientSequence extends Sequence {
       
    /**
     * Registers an {@link AcknowledgementListener} to receive callbacks when 
     * acknowledgements of messages are received.
     *
     * @param listener The listener instance provided by the caller.
     */
    public void setAcknowledgementListener(AcknowledgementListener listener);
 
}
