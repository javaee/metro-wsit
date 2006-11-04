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
 * PipeBase.java
 *
 * @author Mike Grogan
 * Created on February 4, 2006, 10:57 AM
 *
 */
package com.sun.xml.ws.rm.jaxws.runtime;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.jaxws.util.ProcessingFilter;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * BaseClass for <code>RMClientPipe</code> and <code>RMServerPipe</code>.  <coded>Pipe</code>
 * methods are implemented in the subclasses.  The base class contains common code used
 *  by the JAX-WS runtime to communicate with the RM Providers.
 */
public abstract class PipeBase<PROVIDER extends RMProvider, 
                            OUTBOUND extends OutboundSequence, 
                            INBOUND extends InboundSequence>
        implements Pipe {

    /**
     * Either RMSource or RMDestination
     */
    public PROVIDER provider;
    
    /**
     * Next Pipe in pipeline
     */
    protected Pipe nextPipe;


    protected  Marshaller marshaller;
    protected Unmarshaller unmarshaller;
    
    protected ProcessingFilter filter;
    
    protected PipeBase(PROVIDER provider, Pipe nextPipe)  {
        
        this.provider = provider;
        this.nextPipe = nextPipe;
        this.filter = provider.getProcessingFilter();

       /* marshaller = RMConstants.createMarshaller();
        unmarshaller = RMConstants.createUnmarshaller();*/
    }


    /**
     * Use methods of <code>OutboundSequence</code> field to store and write headers to
     * outbound message.
     *
     * @param packet Packet containing Outbound message
     * @return The wrapped message
     */
    protected com.sun.xml.ws.rm.Message handleOutboundMessage(OUTBOUND outboundSequence,
                                                                Packet packet) 
            throws RMException {
             
        Message message = packet.getMessage();
        com.sun.xml.ws.rm.Message msg = 
                new com.sun.xml.ws.rm.Message(message);
        
        Object mn = packet.invocationProperties.get(Constants.messageNumberProperty);
        Object oneWayResponse = packet.invocationProperties.get("onewayresponse");
        
        if (oneWayResponse != null) {
            //don't want to add this message to a sequence.
            msg.isOneWayResponse = true;
        }
        
        if (mn instanceof Integer) {
            msg.setMessageNumber((Integer)mn);
        }
        
        outboundSequence.processOutboundMessage(msg, marshaller);
      
        return msg;
    }

     /**
     * Use methods of <code>RMProvider</code> field to store and write headers to
     * inbound message.
     *
     * @param packet Packet containing Outbound message
     * @return The wrapped message
     */
    protected  com.sun.xml.ws.rm.Message handleInboundMessage(Packet packet)
            throws RMException {

        Message message = packet.getMessage();
        com.sun.xml.ws.rm.Message msg =
                new com.sun.xml.ws.rm.Message(message);

        provider.processInboundMessage(msg, marshaller, unmarshaller);
        return msg;
    }

    /*
    * Implement in subclasses
    */
    public abstract Packet process(Packet packet);
    public abstract void preDestroy();
    public abstract Pipe copy(PipeCloner cloner);

}
