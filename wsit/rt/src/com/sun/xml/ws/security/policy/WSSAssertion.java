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
import java.util.Set;

/**
 * Represents WSS Properties
 * @author K.Venugopal@sun.com
 */
public interface WSSAssertion {
    public final static String MUSTSUPPORT_REF_THUMBPRINT = "MustSupportRefThumbprint";
    public final static String MUSTSUPPORT_REF_ENCRYPTED_KEY = "MustSupportRefEncryptedKey";
    public final static String REQUIRED_SIGNATURE_CONFIRMATION = "RequireSignatureConfirmation";    
    public static final String MUST_SUPPORT_CLIENT_CHALLENGE = "MustSupportClientChallenge";
    public static final String MUST_SUPPORT_SERVER_CHALLENGE = "MustSupportServerChallenge";
    public static final String REQUIRE_CLIENT_ENTROPY = "RequireClientEntropy";
    public static final String REQUIRE_SERVER_ENTROPY= "RequireServerEntropy";
    public static final String MUST_SUPPORT_ISSUED_TOKENS = "MustSupportIssuedTokens";
    public static final String MUSTSUPPORT_REF_ISSUER_SERIAL= "MustSupportRefIssuerSerial";
    public static final String REQUIRE_EXTERNAL_URI_REFERENCE = "RequireExternalUriReference";
    public static final String REQUIRE_EMBEDDED_TOKEN_REF = "RequireEmbeddedTokenReference";
    public static final String MUST_SUPPORT_REF_KEYIDENTIFIER = "MustSupportRefKeyIdentifier";
   
    /**
     * List of WSS properties
     * @return {@link java.util.Set}
     */
    public Set getRequiredProperties();
    /**
     * WSS version
     * @return 1.0
     */
    public String getType();
}
