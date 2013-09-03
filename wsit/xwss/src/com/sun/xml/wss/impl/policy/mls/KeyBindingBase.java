/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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


/**
 * Base class for all KeyBindings 
 *
 */
package com.sun.xml.wss.impl.policy.mls;

public abstract class KeyBindingBase extends WSSPolicy {
     //added for policy integration
    public static final String INCLUDE_ONCE = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Once".intern() ;
    public static final String INCLUDE_NEVER = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Never".intern();
    public static final String INCLUDE_ALWAYS_TO_RECIPIENT = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/AlwaysToRecipient".intern();
    public static final String INCLUDE_ALWAYS="http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Always".intern();
    
    //protected com.sun.xml.ws.security.policy.Token policyToken;
    protected boolean policyToken = false;
    protected String includeToken = INCLUDE_ALWAYS;

    /*public void setPolicyToken(com.sun.xml.ws.security.policy.Token tok) {
       // policyToken = tok;
    }*/
        
    protected String issuer;
    protected byte[] claims;
    protected String claimsDialect;
    
    public boolean policyTokenWasSet() {
        return policyToken;
    }

    public void setPolicyTokenFlag(boolean flag) {
        policyToken = flag;
    }
    
    public void setIncludeToken(String include){
        if (INCLUDE_ONCE.equals(include)) {
            throw new UnsupportedOperationException("IncludeToken Policy ONCE is not yet Supported");
        }
        this.includeToken = include;
        policyToken = true;
    }
    
    public String getIncludeToken(){
        return includeToken;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setClaims(byte[] claims) {
        this.claims = claims;
    }
    
    public byte[] getClaims() {
        return claims;
    }
}
