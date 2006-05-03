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

package com.sun.xml.ws.security;


import java.net.URI;
import javax.crypto.SecretKey;

/**
 * DerivedKeyToken Interface
 * TODO: This defintion is incomplete. Currently it has only those members which are required
 * for the Trust Interop Scenarios
 */
public interface DerivedKeyToken extends Token {

    public static final String DERIVED_KEY_TOKEN_TYPE="http://schemas.xmlsoap.org/ws/2005/02/sc/dk";

    public static final String DEFAULT_DERIVED_KEY_TOKEN_ALGORITHM="http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";
    
    public static final String DEFAULT_DERIVEDKEYTOKEN_LABEL = "WS-SecureConversationWS-SecureConversation"; 

    URI getAlgorithm();

    byte[] getNonce();

    long  getLength();

    long  getOffset();
    
    long getGeneration();
    
    String getLabel();
    
    SecretKey generateSymmetricKey(String algorithm) throws Exception;
}
