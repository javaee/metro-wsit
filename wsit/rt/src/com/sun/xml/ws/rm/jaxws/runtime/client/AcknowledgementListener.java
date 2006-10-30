/*
 * AcknowledgementListener.java
 *
 * @author Mike Grogan
 * Created on October 30, 2006, 1:17 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime.client;

/**
 * Interface implemented be consumer of SequenceAcknowledgement
 * notifications
 */
public interface AcknowledgementListener {
   
   /**
    * Called when the state of a message is changed to <code>complete</code>,
    * meaning that the message has been acked, and its response, if any has been received.
    * The notification is only called for messages belonging to sequences for which
    * an Acknowledgement has been registered usint the <code>setAcknowledgementListener</code> 
    * method of <code>ClientOutboundSequence.</code>
    *
    * @param seq The sequence to which the completed message belongs
    * @param messageNumber The message number of the message.
    */
   public void notify(ClientOutboundSequence seq, int messageNumber);
}
