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


import com.sun.xml.ws.policy.Policy;

import com.sun.xml.ws.security.policy.UseKey;
import com.sun.xml.ws.security.policy.Lifetime;

/**
 * Contains information to be sent in message to the token issuer when requesting for IssuedTokens
 * @author K.Venugopal@sun.com
 */
public interface RequestSecurityTokenTemplate {
    
  
    public String getTrustVersion();
    
    /**
     * Get the type of security token, specified as a String.
     * @return {@link String}
     */
    String getTokenType();
    
  
    
    /**
     * Get the type of request, specified as a String.
     * The String indicates the class of function that is requested.
     * @return {@link String}
     */
    String getRequestType();
    
  
    
    /**
     * Get the desired LifeTime settings for the token if specified, null otherwise
     */
    Lifetime getLifetime();
    
   
    
    /**
     * Set the desired policy settings for the requested token
     * @param appliesTo {@link AppliesTo}
     */
//    void setAppliesTo(AppliesTo appliesTo);
    
    /**
     * Get the desired AppliesTo policy settings for the token if specified, null otherwise
     * @return {@link AppliesTo}
     */
//    AppliesTo getAppliesTo();
    
    
    /**
     * get Authentication Type parameter if set, null otherwise
     */
    String getAuthenticationType();
    
   
    /**
     * get KeyType Parameter if set, null otherwise
     */
    String getKeyType();
    
    
    /**
     * get the KeySize parameter if specified, 0 otherwise
     */
    int getKeySize();
    
   
    /**
     * get SignatureAlgorithm value if set, return default otherwise
     */
    String getSignatureAlgorithm();
    
        
    /**
     * get EncryptionAlgorithm value if set, return default otherwise
     */
    String getEncryptionAlgorithm();
    
   
    /**
     * get CanonicalizationAlgorithm value if set, return default otherwise
     */
    String getCanonicalizationAlgorithm();
    
   

  
    /**
     * Get the desired proofEncryption settings for the token if specified, false otherwise
     */
    boolean getProofEncryptionRequired();
    
  
    /**
     * get CanonicalizationAlgorithm value if set, return default otherwise
     */
    String getComputedKeyAlgorithm();
    
  
    
    /**
     * get Encryption value if set, return false otherwise
     */
    boolean getEncryptionRequired();
    
     
    /**
     * Get the Signature Algorithm to be used with the token if set, null otherwise
     */
    String getSignWith();
   
    
    /**
     * Get the Encryption Algorithm to be used with the token if set, null otherwise
     */
    String getEncryptWith();
    
      
}

