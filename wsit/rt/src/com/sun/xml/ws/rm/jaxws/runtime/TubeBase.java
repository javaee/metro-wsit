/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * TubeBase.java
 *
 * @author Mike Grogan
 * Created on August 17, 2007, 1:28 PM
 *
 */
package com.sun.xml.ws.rm.jaxws.runtime;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.RMVersion;
import com.sun.xml.ws.rm.jaxws.util.ProcessingFilter;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * BaseClass for <code>RMClientTube</code> and <code>RMServerTube</code>.  <coded>Tube</code>
 * methods are implemented in the subclasses.  The base class contains common code used
 *  by the JAX-WS runtime to communicate with the RM Providers.
 */
public abstract class TubeBase<PROVIDER extends RMProvider, 
                            OUTBOUND extends OutboundSequence, 
                            INBOUND extends InboundSequence>
        
        extends AbstractFilterTubeImpl {

    /**
     * Either RMSource or RMDestination
     */
    public PROVIDER provider;
   
    protected RMVersion version;
    protected Marshaller marshaller;
    protected Unmarshaller unmarshaller;
    
    protected ProcessingFilter filter;
    
    protected TubeBase(PROVIDER provider, Tube nextTube)  {
        
        super(nextTube);
        this.provider = provider;
        this.filter = provider.getProcessingFilter();
    }
    
    protected TubeBase(PROVIDER provider, TubeBase that, TubeCloner cloner) {
        
        super(that, cloner);
        this.provider = provider;
        this.filter = provider.getProcessingFilter();
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
                new com.sun.xml.ws.rm.Message(message, version);
        
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
                new com.sun.xml.ws.rm.Message(message, version);

        provider.processInboundMessage(msg, marshaller, unmarshaller);
        return msg;
    }

}
