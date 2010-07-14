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

package com.sun.xml.ws.security.policy;

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
    
    /**
     * Get the KeyWrap Algorithm used for key wrapping when STS encrypts the issued token 
     * for the relying party using an asymmetric key.     
     */
    String getKeyWrapAlgorithm();
      
    /**
     * 
     * @return Claims
     */ 
    Claims getClaims();
    
}

