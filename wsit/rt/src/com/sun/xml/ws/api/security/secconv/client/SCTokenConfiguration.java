/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.api.security.secconv.client;

import java.util.HashMap;
import java.util.Map;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.security.trust.client.IssuedTokenConfiguration;
import com.sun.xml.ws.security.policy.Token;

/**
 *
 * @author Shyam Rao
 */
public abstract class SCTokenConfiguration implements IssuedTokenConfiguration{
    
    public static final String PROTOCOL_10 = "http://schemas.xmlsoap.org/ws/2005/02/sc";
    public static final String PROTOCOL_13 = "http://docs.oasis-open.org/ws-sx/ws-secureconversation/200512";
    public static final String MAX_CLOCK_SKEW = "maxClockSkew";

    protected String protocol;    
    
    protected boolean renewExpiredSCT = false;
    
    protected boolean requireCancelSCT = false;
    
    protected long scTokenTimeout = -1;
    
    private Map<String, Object> otherOptions = new HashMap<String, Object>();
    
    protected SCTokenConfiguration(){
        this(PROTOCOL_10);
    }
    
    protected SCTokenConfiguration(String protocol){
        this.protocol = protocol;        
    }            
    
    public String getProtocol(){
        return protocol;
    }                 
    
    public boolean isRenewExpiredSCT(){
        return renewExpiredSCT;
    }
    
    public boolean isRequireCancelSCT(){
        return requireCancelSCT;
    }
    
    public long getSCTokenTimeout(){
        return this.scTokenTimeout;
    }
    
    public abstract String getTokenId();
    
    public abstract boolean checkTokenExpiry();
    
    public abstract boolean isClientOutboundMessage();
    
    public abstract boolean addRenewPolicy();
    
    public abstract boolean getReqClientEntropy();
    
    public abstract boolean isSymmetricBinding();
    
    public abstract int getKeySize();
        
    public abstract Token getSCToken();
            
    public abstract Packet getPacket();
    
    public abstract Tube getClientTube();
    
    public abstract Tube getNextTube();
    
    public abstract WSDLPort getWSDLPort();
    
    public abstract WSBinding getWSBinding();
    
    public abstract AddressingVersion getAddressingVersion();
    
    public Map<String, Object> getOtherOptions(){
        return this.otherOptions;
    }
}
