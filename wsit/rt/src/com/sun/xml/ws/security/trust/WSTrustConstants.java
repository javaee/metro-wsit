/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.security.trust;

/**
 * Common Constants pertaining to WS-Trust
 * @author WS-Trust Implementation Team
 */
public class WSTrustConstants {
    
    public static final String SAML_CONFIRMATION_METHOD = "Saml-Confirmation-Method";
    
    public static final String USE_KEY_RSA_KEY_PAIR = "UseKey-RSAKeyPair";

    public static final String USE_KEY_SIGNATURE_ID = "UseKey-SignatureID";

    public static final String STS_CALL_BACK_HANDLER = "stsCallbackHandler";

    public static final String SAML_ASSERTION_ELEMENT_IN_RST = "SamlAssertionElementInRST";

    public static final String WST_VERSION = "WSTrustVersion";
    
    public static final String AUTHN_CONTEXT_CLASS = "AuthnContextClass";
    
    public static final String SECURITY_ENVIRONMENT = "SecurityEnvironment";
    
    public static final String SAML10_ASSERTION_TOKEN_TYPE = "urn:oasis:names:tc:SAML:1.0:assertion";
    
    public static final String SAML11_ASSERTION_TOKEN_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1";
    
    public static final String SAML20_ASSERTION_TOKEN_TYPE = "urn:oasis:names:tc:SAML:2.0:assertion";

    public static final String SAML20_WSS_TOKEN_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0";

    public static final String OPAQUE_TYPE = "opaque";
   
    public static final String SAML11_TYPE = "urn:oasis:names:tc:SAML:1.1:assertion";
    
    /** the Trust namespace URI */
    public static final String WST_NAMESPACE = "http://schemas.xmlsoap.org/ws/2005/02/trust";
    
    /** the prefix to use for Trust */
    public static final String WST_PREFIX = "wst";
    
    /** URI for different request types */
    public static final String ISSUE_REQUEST = WST_NAMESPACE + "/Issue";
    public static final String RENEW_REQUEST = WST_NAMESPACE + "/Renew";
    public static final String CANCEL_REQUEST = WST_NAMESPACE + "/Cancel";
    public static final String VALIDATE_REQUEST = WST_NAMESPACE + "/Validate";
    public static final String KEY_EXCHANGE_REQUEST = WST_NAMESPACE + "/KET";
    
    /**
     * URI for KeyType
     */
    public static final String PUBLIC_KEY = WST_NAMESPACE+ "/PublicKey";
    public static final String SYMMETRIC_KEY = WST_NAMESPACE + "/SymmetricKey";
    public static final String NO_PROOF_KEY = "http://schemas.xmlsoap.org/ws/2005/05/identity/NoProofKey";
    
   /**
     * Constants denoting type of Elements
     */
    public static final String STR_TYPE = "SecurityTokenReference";
    public static final String TOKEN_TYPE = "Token";
    
    /** Action URIs */
    public static final String REQUEST_SECURITY_TOKEN_ISSUE_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/Issue";    
    public static final String REQUEST_SECURITY_TOKEN_RESPONSE_ISSUE_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RSTR/Issue";    
    
    
    /** computed key PSHA1 */
    public static final String CK_PSHA1= "http://schemas.xmlsoap.org/ws/2005/02/trust/CK/PSHA1";
    
    /** computed key HASH */
    public static final String CK_HASH= "http://schemas.xmlsoap.org/ws/2005/02/trust/CK/HASH";

    /**
     * The default value for AppliesTo if appliesTo is not specified.
     */
    public static final String DEFAULT_APPLIESTO = "default";
    
    /**
     * Property name for the STS WSDL location URL to be set on the client side
     */
    public static final String PROPERTY_URL= "WSTRUST_PROPERTY_URL";
    /**
     * Property name for the STS port name to be set on the client side
     */
    public static final String PROPERTY_PORT_NAME= "WSTRUST_PROPERTY_PORT_NAME";
    /**
     * Property name for the STS service name to be set on the client side
     */
    public static final String PROPERTY_SERVICE_NAME= "WSTRUST_PROPERTY_SERVICE_NAME";
    
    /**
     * Property name for the STS end point URL to be set on the client side
     */
    public static final String PROPERTY_SERVICE_END_POINT = "STS_END_POINT";
    
    /**
     * List of STS Properties
     */
    public static  enum STS_PROPERTIES  { PROPERTY_URL, PROPERTY_PORT_NAME, PROPERTY_SERVICE_NAME, PROPERTY_SERVICE_END_POINT };
    
    public static final String IS_TRUST_MESSAGE = "isTrustMessage";
    
    public static final String TRUST_ACTION = "trustAction";
}
