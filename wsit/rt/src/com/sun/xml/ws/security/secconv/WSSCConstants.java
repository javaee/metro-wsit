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


package com.sun.xml.ws.security.secconv;

/**
 * Common Constants pertaining to WS-SecureConversation
 * @author WS-Trust Implementation Team
 */
public class WSSCConstants {
    
    /** the SecureConversation namespace URI */
    public static final String WSC_NAMESPACE = "http://schemas.xmlsoap.org/ws/2005/02/sc";
    
    /** the prefix to use for WS-SecureConversation */
    public static final String WSC_PREFIX = "wsc";
    
    /** URI for SCT token type */
    public static final String SECURITY_CONTEXT_TOKEN_TYPE = WSC_NAMESPACE + "/sct";

    /** SecurityContextToken Type String */
    public static final String SECURITY_CONTEXT_TOKEN = "SecurityContextToken";
    
    public static final String SECURITY_CONTEXT_ID = "Incomimg_SCT";
    
    /** Action URIs */
    public static final String REQUEST_SECURITY_CONTEXT_TOKEN_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/SCT";    
    public static final String REQUEST_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RSTR/SCT";    
    
    public static final String CANCEL_SECURITY_CONTEXT_TOKEN_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/SCT/Cancel";    
    public static final String CANCEL_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RSTR/SCT/Cancel";    
    

}
