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
 * ClientInboundSequence.java
 *
 * @author Mike Grogan
 * Created on October 15, 2005, 3:11 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime.client;
import com.sun.xml.ws.rm.Sequence;
import com.sun.xml.ws.rm.Header;
import com.sun.xml.ws.rm.protocol.*;
import java.util.List;
import java.net.URI;
import com.sun.xml.ws.rm.jaxws.runtime.InboundSequence;


/**
 * An <code>ClientInboundSequence</code> represents a sequence of incoming messages.  
 * For an <code>RMSource</code>, a <code>ClientInboundSequence</code> is comprised 
 * of all the messages containing responses to a companion <code>ClientOutboundSequence</code>.  
 */

public class ClientInboundSequence extends InboundSequence {
          
    public ClientInboundSequence(ClientOutboundSequence outboundSequence , 
                            String identifier, 
                            URI acksTo) {
        
        this.acksTo = acksTo;
        this.outboundSequence = outboundSequence;
        setId(identifier);
    }
    
   
}
