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
 * RMDestination.java
 *
 * @author Mike Grogan
 * Created on October 15, 2005, 6:24 PM
 */

package com.sun.xml.ws.rm.jaxws.runtime.server;
import com.sun.xml.ws.rm.InvalidSequenceException;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.jaxws.runtime.RMProvider;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import java.net.URI;
import java.util.*;

/**
 * An RMDestination represents a Collection of Inbound RMSequences.
 */
public class RMDestination extends RMProvider<ServerInboundSequence,
                                              ServerOutboundSequence>{
   
    private static RMDestination rmDestination = new RMDestination();
  
    
    public static RMDestination getRMDestination() {
        return rmDestination;
    }
    
    //TODO - make an intelligent choice for  wake-up interval.
    private SequenceReaper reaper = new SequenceReaper(5000, inboundMap);
    
    private RMDestination() {   
    }
    
   
    public void terminateSequence(String id) 
                throws InvalidSequenceException {
        ServerInboundSequence seq = getInboundSequence(id);
        
        if (seq == null) {
            throw new InvalidSequenceException(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,id),id);
        }
        
        ServerOutboundSequence out = 
                (ServerOutboundSequence)seq.getOutboundSequence();
        
        if (seq != null) {
            inboundMap.remove(id);
        }
        
        if (out != null) {
            String outid = out.getId();
            if (outid != null) {
                outboundMap.remove(outid);
            }
        }
    }
    
    //TODO add endpoint address argument to this method and corresponding
    //member in ServerInboundSequence
    public ServerInboundSequence createSequence(URI acksTo, 
                                          String outboundId,
                                          SequenceConfig config) throws RMException {
        
        ServerInboundSequence seq = new ServerInboundSequence(acksTo, outboundId, config);
        inboundMap.put(seq.getId(), seq);
        
        ServerOutboundSequence outbound = 
                (ServerOutboundSequence)seq.getOutboundSequence();
        String id = outbound.getId();
        
        if (id != null) {
            outboundMap.put(id ,  outbound);
        }
        
        return seq;
    }
    
    /**
     * SequenceReaper is a timer with a single task that periodically checks the map
     * of active ServerInboundSequences for expired ones an peremptorily terminates them.
     */
    private class SequenceReaper extends Timer {
        
        private long frequency;
        private Map<String, 
                ServerInboundSequence> map;
        
        private TimerTask timerTask = new TimerTask() {
            public void run() {
                //go though all the sequences and shut down any that
                //are expired.
                HashSet<String> keysToRemove = new HashSet<String>();
                for (String key : map.keySet()) {
                    
                    ServerInboundSequence sis= map.get(key);
                    synchronized (sis) {
                        if (sis.isExpired()) {
                            System.out.println("Terminating expired sequence " +
                                    sis.getId());
                                    keysToRemove.add(key);
                        }
                   }
                }
                
                for (String str : keysToRemove) {                           
                    try {
                        terminateSequence(str);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                 }
                   
            }
        };
        
        public SequenceReaper(long frequency, Map<String, 
                ServerInboundSequence> map) {
            //make the Timer Thread a daemon.
            super(true);
            this.map = map;
            this.frequency = frequency;
            schedule(timerTask, 
                     new Date(System.currentTimeMillis()),
                     frequency);
        }
    }
}
