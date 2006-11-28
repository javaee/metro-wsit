
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
 * RMSource.java
 *
 * @author Mike Grogan
 * Created on October 15, 2005, 6:24 PM
 */

package com.sun.xml.ws.rm.jaxws.runtime.client;
import com.sun.xml.ws.rm.Message;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.runtime.RMProvider;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import com.sun.xml.ws.rm.Constants;
import java.util.logging.Logger;

/**
 * An RMSource represents a Collection of RMSequences with a
 * common acksTo endpoint.
 */
public class RMSource  extends RMProvider<ClientInboundSequence,
        ClientOutboundSequence> {
    
    private  static final Logger logger = 
            Logger.getLogger(RMSource.class.getName());
    
    private static RMSource rmSource = new RMSource();
    
   
    public static RMSource getRMSource() {
        return rmSource;
    }
    
    private long retryInterval;
    private RetryTimer retryTimer;
    
    public RMSource() {
        
        retryInterval = 2000;
        
        retryTimer = new RetryTimer(this);
        
    }
    
    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }
    
    public long getRetryInterval() {
        return retryInterval;
    }
    
    
    public synchronized void terminateSequence(ClientOutboundSequence seq) 
            throws RMException {
        
        String id = seq.getId();
        if (seq != null && outboundMap.keySet().contains(id)) {
            seq.disconnect();
            removeOutboundSequence(id);
        }   
    }
    
    public synchronized void addOutboundSequence(ClientOutboundSequence seq) {
        logger.fine("adding sequence " + seq.getId());    
        
        boolean firstSequence = outboundMap.isEmpty();
        outboundMap.put(seq.getId(), seq);
        
        ClientInboundSequence iseq = 
                    (ClientInboundSequence)seq.getInboundSequence();
            
        String iseqid = null;
       
        if (iseq != null && null != (iseqid = iseq.getId())) {
            inboundMap.put(iseqid, iseq);
        }
        if (firstSequence) {
            retryTimer.start();
        }
    }
    
    public synchronized void removeOutboundSequence(ClientOutboundSequence seq) {
         logger.fine("removing sequence " + seq.getId());
         
         String id = seq.getId();
         
        ClientInboundSequence iseq = 
                    (ClientInboundSequence)seq.getInboundSequence();
            
        String iseqid = null;
        if (iseq != null && null != (iseqid = iseq.getId())) {
            inboundMap.remove(iseqid);
        }
        outboundMap.remove(id);
        
        if (outboundMap.isEmpty()) {
            retryTimer.stop();
        }
    }
    
    private void removeOutboundSequence(String id) {
        
         ClientOutboundSequence seq = outboundMap.get(id);
         
         if (seq != null) {
            removeOutboundSequence(seq);
         } else {
             throw new IllegalArgumentException("No Outbound sequence with id " +
                                                 id + " exists.");
         }
    }
    
    /**
     * Allow a clean shutdown
     *
     * @deprecated - This was a plugfest hack.  The Retry thread is now a
     * daemon, obviating the need to shut it down.  At the moment, we do not
     * need a ProtocolMessageReceiver, since we are not doing duplex
     * bindings.
     */
    /*
    public static void stop() {
        
        //allow the RetryThread to exit
        running = false;
        
        //allow the Protocol message listenter, if any to stop listening
        ProtocolMessageReceiver.stop();
    }
     **/
    
    /**
     * Do the necessary maintenance tasks for each <code>ClientInboundSequence</code>
     * managed by this RMSource.  This is done by calling the <code>doMaintenanceTasks</code>
     * method of each managed sequence.
     * 
     * @throws RMException Propogates <code>RMException</code> thrown by any of the managed
     * sequences.
     */

    public void doMaintenanceTasks() throws RMException {
        
        for (String key : outboundMap.keySet()) {
            
            ClientOutboundSequence seq =
                    getOutboundSequence(key);
            
            synchronized(seq) {
                //1. resend all incomplete messages
                //2. send ackRequested messages in any sequences
                //   in danger of timing out.
                seq.doMaintenanceTasks();
            }
        }
        
    }
    
     /**
     * Initialize a sequence using a CreateSequence handshake.  The
     * returned Sequence can be set in BindingProvider properies which will
     * result in the Sequence being used for the BindingProvider's request messages.
     *
     * @param client A Service hosting the endpoint
     * @param port The QName for the RM enpoint.
     * @return The ClientOutboundSequence.  null if the sequence could not be created
     *  
     */
    public ClientOutboundSequence createSequence(javax.xml.ws.Service service, 
                                                QName portName) 
    {
   
        Dispatch<Source> disp = service.createDispatch(portName, 
                                                        Source.class, 
                                                        Service.Mode.PAYLOAD,
                                                        new javax.xml.ws.RespectBindingFeature());
        
        byte[] bytes = Constants.createSequencePayload.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        StreamSource source = new StreamSource(stream);
        
        try {
            disp.invoke(source);
        } catch (Exception e) {
            
            //dont care what happened processing the response message.  We are only
            //interested in the sequence that has been stored in the request context
            //
            //TODO - At the same time, it would be prettier to get something other than
            //a fault
        }
        
        ClientOutboundSequence seq =  (ClientOutboundSequence)disp.getRequestContext()
                    .get(Constants.sequenceProperty);
        seq.setService(service);
        return seq;
       
    }

 

/**
     * Initialize a sequence using an existing seuence id known to an RM endpoint.
     * The method is designed to be used after a startup to reinitialize a
     * sequence from persisted data.
     *
     * @param client A Service hosting the endpoint
     * @param port The QName for the RM enpoing.
     * @param sequencID The id to be used for the outbound sequence
     * @param companionSequenceID The id to be used for the companion inbound sequence,
     *              if any
     * @return The ClientOutboundSequence.  null if the sequence could not be created
     */
    public ClientOutboundSequence createSequence(javax.xml.ws.Service service, 
                                                QName portName, String sequenceID,
                                                String companionSequenceID){
        
   
        //this will throw and exception if the specified sequence does not exist.
        //removeOutboundSequence(sequenceID);
        
        ClientOutboundSequence seq = createSequence(service, portName);
        if (seq == null ) {
            return null;
        }
         
        try {
            seq.disconnect(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        seq.setId(sequenceID);
        
        ClientInboundSequence iseq = 
                    (ClientInboundSequence)seq.getInboundSequence();
        
        if (companionSequenceID != null) {
           
            if (iseq == null || iseq.getId() == null) {
                throw new IllegalArgumentException(
                        "Sequence does not contain a two-way operation, " +
                        "but an inbound sequence id is specified");
            }     
            iseq.setId(companionSequenceID);
        } else if (iseq != null && iseq.getId() != null) {
            throw new IllegalArgumentException(
                    "Sequence id for inbound sequence must be specified.");
        }
        
        if (outboundMap.get(sequenceID) != null) {
            throw new IllegalArgumentException(
                    "Sequence " + sequenceID + " already exists.");
        }
        
        if (companionSequenceID != null &&
                inboundMap.get(companionSequenceID) != null) {
            throw new IllegalArgumentException(
                    "Sequence " + companionSequenceID + " already exists.");
        }
        
        addOutboundSequence(seq);
        
        return seq;
    }
        
    
}
