/*
 * $Id: WSTrustConstants.java,v 1.2 2006-08-02 18:03:54 jdg6688 Exp $
 */

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

package com.sun.xml.ws.security.trust;

/**
 * Common Constants pertaining to WS-Trust
 * @author WS-Trust Implementation Team
 */
public interface WSTrustConstants {
    
    public static final String STS_CALL_BACK_HANDLER = "stsCallbackHandler";
    
    public static final String SAML11_ASEERTION_TOKEN_TYPE ="urn:oasis:names:tc:SAML:1.0:assertion";
    
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
    public static final String [] STS_PROPERTIES = { PROPERTY_URL, PROPERTY_PORT_NAME, PROPERTY_SERVICE_NAME, PROPERTY_SERVICE_END_POINT };
}
