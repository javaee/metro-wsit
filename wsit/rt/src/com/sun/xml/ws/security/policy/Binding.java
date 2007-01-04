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


/**
 * Base Interface for Security Policy Binding assertions, identifies Algorithms that are supported,describes the layout of
 * the security header.
 * @author K.Venugopal@sun.com
 */
public interface Binding{
    
    public static final String ENCRYPT_SIGN = "EncryptBeforeSigning";
    public static final String SIGN_ENCRYPT = "SignBeforeEncrypting";
  
    /**
     * returns the {@link AlgorithmSuite} assertions defined in the policy.
     * @return {@link AlgorithmSuite}
     */
    public AlgorithmSuite getAlgorithmSuite();
  
    /**
     * returns true if TimeStamp property is enabled in this binding
     * @return true or false
     */
    public boolean isIncludeTimeStamp();
 
    /**
     * returns the Layout {@link MessageLayout }of  the SecurityHeader.
     * @return one of {@link MessageLayout }
     */
    public MessageLayout getLayout();
    /**
     * returns true if body and header content only has to be signed, false if entire body and header has to be signed.
     * @return true if body and header content only has to be signed, false if entire body and header has to be signed.
     */
    public boolean isSignContent();    
    
        
    /**
     * gets data protection order should be one one of Binding.SIGN_ENCRYPT or Binding.ENCRYPT_SIGN
     * @return one of Binding.SIGN_ENCRYPT or Binding.ENCRYPT_SIGN
     */
    public String getProtectionOrder();    
       
        
    /**
     * 
     * @return true if token has to be protected else false.
     */
    public boolean getTokenProtection();
    
    /**
     *
     * @return true if signature has to be encrypted else false.
     */
    public boolean getSignatureProtection();
}
