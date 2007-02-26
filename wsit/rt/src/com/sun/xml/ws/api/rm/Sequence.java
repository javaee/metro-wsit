/*
 * Sequence.java
 *
 *
 * @author Mike Grogan
 * Created on January 19, 2007, 9:02 AM
 *
 */

package com.sun.xml.ws.api.rm;
import com.sun.xml.ws.api.rm.server.ServerSequence;
import com.sun.xml.ws.api.rm.client.ClientSequence;
/**
 * Base class for {@link ClientSequence} and
 * {@link ServerSequence} classes.
 */
public interface Sequence {
    
   /**
    * Returns the unique identifier for the <code>Sequence</code>.
    *
    * @return The sequence identifier.
    */
   public String getId();
   
   /**
    * Returns a {@link SequenceSettings} object that can be persisted and used to 
    * reconstruct the Sequence after a restart using a <code>createSequence</code> 
    * method of one of the subclasses, {@link ServerSequence} or {@link ClientSequence}.
    *
    * @return The SequenceSettings containing the data that can be used to reinitialize
    *         the sequence.
    */
   public SequenceSettings getSequenceSettings();
    
}
