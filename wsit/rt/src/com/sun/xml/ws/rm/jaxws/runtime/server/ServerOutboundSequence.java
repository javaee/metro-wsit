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
 * ServerOutboundSequence.java
 *
 * @author Mike Grogan
 * Created on November 21, 2005, 3:02 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime.server;
import com.sun.xml.ws.rm.Sequence;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.protocol.SequenceAcknowledgementElement;
import com.sun.xml.ws.rm.protocol.AcknowledgementHandler;

/**
 * A <code>ServerOutboundSequence</code> represents all the response messages
 * to requests belonging to a companion <code>ServerInboundSequence</code>.
 */
public class ServerOutboundSequence extends OutboundSequence {
     
    /**
     * Public ctor stores companion inbound sequence as well as
     * sequence id from an Offer element in the CreateSequence
     * message from the client.  If the id is null then no messages
     * are stored in this ServerOutboundSequence.
     */
    public ServerOutboundSequence(ServerInboundSequence seq, 
                                    String id, 
                                    SequenceConfig config) {
        inboundSequence = seq;
        this.config = config;
        setId(id);
        saveMessages = true;
        ackHandler = new AcknowledgementHandler(config);
    }
    
}
