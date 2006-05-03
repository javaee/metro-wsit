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

import com.sun.xml.ws.policy.PolicyAssertion;

import javax.xml.namespace.QName;


/**
 *
 * @author K.Venugopal@sun.com
 */
public abstract class WSSecurityPolicyFactory{
    
    /** Creates a new instance of WSSecurityPolicyFactory */
    public void WSSecurityPolicyFactory() {
    }
    
    public static WSSecurityPolicyFactory getInstance(){
        //default
        throw new UnsupportedOperationException();
      //  return new com.sun.xml.ws.security.impl.policy.WSSecurityPolicyFactory();
    }
    
    public static WSSecurityPolicyFactory getInstance(String namespaceURI){
        throw new UnsupportedOperationException("This method is not supported");
    }
    
    public abstract EncryptedParts createEncryptedParts();
    public abstract SignedParts createSignedParts();
    public abstract SpnegoContextToken createSpnegoContextToken();
    public abstract TransportBinding createTransportBinding();
    public abstract TransportToken createTransportToken();
    public abstract AlgorithmSuite createAlgorithmSuite();
    public abstract UserNameToken createUsernameToken();
    public abstract SymmetricBinding createSymmetricBinding();
    public abstract AsymmetricBinding createASymmetricBinding();
    public abstract X509Token createX509Token();
    public abstract EndorsingSupportingTokens createEndorsingSupportingToken();
    public abstract IssuedToken createIssuedToken();
    public abstract PolicyAssertion createSecurityAssertion(QName name);
    public abstract PolicyAssertion createSecurityAssertion(QName qname, ClassLoader classLoader);
    
}
