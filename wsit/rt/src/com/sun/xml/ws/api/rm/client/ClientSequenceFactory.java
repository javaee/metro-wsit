/*
 * ClientSequenceFactory.java
 *
 *
 * @author Mike Grogan
 * Created on January 19, 2007, 10:16 AM
 *
 */

package com.sun.xml.ws.api.rm.client;
import com.sun.xml.ws.api.rm.SequenceSettings;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import com.sun.xml.ws.rm.jaxws.runtime.client.RMSource;
/**
 * Factory used by clients who need to provide their own
 * sequences for use by the RMClient runtime.  Typically, the
 * client application is maintaining its own persistent store
 * of sent messages and needs to monitor their acknowledgements.
 */
public class ClientSequenceFactory {
    
    private  ClientSequenceFactory() {
    }
    
    /**
     * Establishes a new session with the endpoint.  The returned (@link ClientSequence}
     * can be specified for use by one or more client instances connected to the endpoint
     * by setting it as the value of the <code>com.sun.xml.rm.session</code> property.
     *
     * @param service The JAX-WS service representing the service.
     * @param portName The name of the endpoint.
     * @return The ClientSequence obtained by sending a RM Protol CreateSequence message
     *         to the endpoint.  Returns <code>null</code> if the
     *         sequence creation fails.
     */
    public static ClientSequence createSequence(javax.xml.ws.Service service, 
                                                QName portName) {
        return RMSource.getRMSource()
            .createSequence(service, portName);
    }
    
    
    /**
     * Re-establishes a session using persisted data from an existing session.
     * 
     * @param service The JAX-WS service representing the service.
     * @param portName The name of the endpoint.
     * @return The ClientSequence obtained by sending a RM Protol CreateSequence message
     *         to the endpoint and replacing its state with the state saved
     *         previously from another session with the same endpoint. Returns
     *         <code>null</code> is sequence creation fails.
     */
    public static ClientSequence createSequence(javax.xml.ws.Service service, 
                                                QName portName, SequenceSettings settings)  {
        return RMSource.getRMSource()
            .createSequence(service, portName, 
                settings.sequenceId, settings.companionSequenceId);
    }
       
  
    
    
    
}
