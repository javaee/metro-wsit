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


package com.sun.xml.ws.security.secconv;



import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.WSTrustDOMContract;
 
/**
 * A Factory for creating WS-SecureConversation contract instances.
 */

public class WSSCFactory {
    
    private WSSCFactory(){
        //empty constructor
    }
    
    public static WSSCPlugin newSCPlugin(final Configuration config) {
        return new WSSCPlugin(config);
    }

     public static NewWSSCPlugin newNewSCPlugin(final Configuration config) {
        return new NewWSSCPlugin(config);
    }


    public static WSSCContract newWSSCContract(final Configuration config) {
        final WSSCContract contract = new WSSCContract();
        contract.init(config); 
        
        return contract;
    }
    
   
    public static WSSCSourceContract newWSSCSourceContract(final Configuration config) {
        final WSSCSourceContract contract = new WSSCSourceContract();
        contract.init(config); 
        
        return contract;
    }
   
    public static WSTrustDOMContract newWSSCDOMContract(final Configuration config) {
        final WSSCDOMContract contract = new WSSCDOMContract();
        contract.init(config); 
        
        return contract;
    }
    
    public static WSSCClientContract newWSSCClientContract(final Configuration config) {
        return new WSSCClientContract(config);
    }
}
