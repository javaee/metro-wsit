/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
    
    
    public boolean isDisableTimestampSigning();
 
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
    
    /**
     *
     * @return the version of Security Policy
     */
    public SecurityPolicyVersion getSecurityPolicyVersion();
}
