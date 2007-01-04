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


package com.sun.xml.ws.security.policy;

import com.sun.xml.ws.policy.NestedPolicy;
import java.util.Set;



/**
 * This interface represents requirement for Security Context Token defined in WS-SecureConversation 1.0
 * @author K.Venugopal@sun.com
 */
public interface SecureConversationToken extends Token {

    /**
     * returns a {@link java.util.Iterator } over the token reference types to be used.
     * @return either REQUIRE_EXTERNAL_URI_REFERENCE
     */
    public Set getTokenRefernceTypes();
    
    /**
     * returns true if RequiredDerivedKey element is present under SecureConversationToken
     * @return true if RequireDerviedKeys element is present under SecureConversationToken or false.
     */
    public boolean isRequireDerivedKeys();
   
  
    /**
     * returns the type of the token.
     * @return one of SC10_SECURITYCONTEXT_TOKEN
     */
    public String getTokenType();
    
    /**
     * returns the issuer for the SpnegoContext token.
     * @return returns the issuer
     */
    public Issuer getIssuer();
  
    /**
     * returns {@link com.sun.xml.ws.policy.Policy } which represents Bootstrap Policy
     * @return {@link com.sun.xml.ws.policy.Policy }
     */
    public NestedPolicy getBootstrapPolicy();
   
}
