/*
 * ServerSequenceFactory.java
 *
 *
 * @author Mike Grogan
 * Created on January 19, 2007, 11:58 AM
 *
 */

package com.sun.xml.ws.api.rm.server;
import com.sun.xml.ws.api.rm.SequenceSettings;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.xml.ws.rm.jaxws.util.LoggingHelper;
import com.sun.xml.ws.rm.jaxws.runtime.server.RMDestination;
import com.sun.xml.ws.rm.RMException;
import java.net.URI;
import java.net.URISyntaxException;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
/**
 * Factory class contains a method that can be used to re-initialize a server-side
 * sequence using persisted data after a system failure.
 */
public class ServerSequenceFactory {
    
    private static Logger logger = 
            Logger.getLogger(LoggingHelper.getLoggerName(ServerSequenceFactory.class));
    
    private ServerSequenceFactory() {
    }
    
    /**
     * Factory method initializes a server-side sequence using saved data.  This
     * is necessary after a restart in order for the system to recognize incoming
     * messages belonging to a sequence established before the restart.
     *
     * @param settings A {@link SequenceSettings} obtained by an earlier call to
     *          ServerSequence.getSequenceSettings and persisted.
     * @return The reinitialized sequence.  It will use the initialization settings
     *          from the original sequence, but will not contain state 
     *          concerning which message numbers have  been  received.  
     */
    public static ServerSequence createSequence(SequenceSettings settings) {
        try {
            return RMDestination.getRMDestination().createSequence(
                                          new URI(settings.acksTo), 
                                          settings.sequenceId,
                                          settings.companionSequenceId,
                                          new SequenceConfig(settings));
        } catch (RMException e) {
            //TODO I18
            logger.log(Level.SEVERE, 
                    "ServerSequenceFactory.createSequence failed", e);
            return null;
        } catch (URISyntaxException ee) {
            //TODO I18
            logger.log(Level.SEVERE, 
                    "ServerSequenceFactory.createSequence failed", ee);
            return null;
        }
        
    }
    
}
