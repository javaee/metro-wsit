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
 * SecureConversationTokenKeyBinding.java
 *
 * Created on December 20, 2005, 5:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityTokenReference;
import com.sun.xml.ws.security.Token;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import java.net.URI;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 *
 */
public class SecureConversationTokenKeyBinding extends KeyBindingBase {

    /** Creates a new instance of IssuedTokenKeyBinding */
    public SecureConversationTokenKeyBinding() {
        setPolicyIdentifier(PolicyTypeUtil.SECURE_CONVERSATION_TOKEN_KEY_BINDING);
    }
    
    public Object clone() {
        SecureConversationTokenKeyBinding itb = new SecureConversationTokenKeyBinding();
        itb.setPolicyToken(this.getPolicyToken());
        itb.setIncludeToken(this.getIncludeToken());
        return itb;
    }
    
    public boolean equals(WSSPolicy policy) {
        if ( !PolicyTypeUtil.secureConversationTokenKeyBinding(policy)) {
            return false;
        }
        
        //TODO: Check the contents of IssuedTokenContext
        return true;
    }
    
    public boolean equalsIgnoreTargets(WSSPolicy policy) {
        return equals(policy);
    }
    
    public String getType() {
        return PolicyTypeUtil.SECURE_CONVERSATION_TOKEN_KEY_BINDING;
    }
    
}
