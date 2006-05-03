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
import java.util.Iterator;


/**
 * Supporting tokens are included in the security header and may optionally include
 * additional message parts to sign and/or encrypt.
 * @author K.Venugopal@sun.com
 */
public interface SupportingTokens extends Token{    
   
    /**
     * returns the {@link AlgorithmSuite} which will identify algorithms to use.
     * @return {@link AlgorithmSuite} or null
     */
    public AlgorithmSuite getAlgorithmSuite();
  
    /**
     * List of targets that need to be protected.
     * @return {@link java.util.Iterator } over targets that need to be protected.
     */
    public Iterator<SignedParts> getSignedParts();
    public Iterator<SignedElements> getSignedElements();
    public Iterator<EncryptedParts> getEncryptedParts();
    public Iterator<EncryptedElements> getEncryptedElements();
   
    /**
     * All tokens are set.
     * @return {@link java.util.Iterator } over tokens that are to be included in the message
     */
    public Iterator getTokens();
}
