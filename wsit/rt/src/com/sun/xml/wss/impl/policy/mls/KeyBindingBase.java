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


/**
 * Base class for all KeyBindings 
 *
 */
package com.sun.xml.wss.impl.policy.mls;

public abstract class KeyBindingBase extends WSSPolicy {
     //added for policy integration
    protected com.sun.xml.ws.security.policy.Token policyToken;
    protected String includeToken = com.sun.xml.ws.security.policy.Token.INCLUDE_ALWAYS;

    public void setPolicyToken(com.sun.xml.ws.security.policy.Token tok) {
        policyToken = tok;
    }
                                                                                                              
    public com.sun.xml.ws.security.policy.Token getPolicyToken() {
        return policyToken;
    }

    public void setIncludeToken(String include){
        if (com.sun.xml.ws.security.policy.Token.INCLUDE_ONCE.equals(include)) {
            throw new UnsupportedOperationException("IncludeToken Policy ONCE is not yet Supported");
        }
        this.includeToken = include;
    }
    
    public String getIncludeToken(){
        return includeToken;
    }
}
