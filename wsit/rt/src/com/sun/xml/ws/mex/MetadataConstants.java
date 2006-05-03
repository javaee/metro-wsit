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
package com.sun.xml.ws.mex;

import java.util.logging.Level;

/**
 * TODO: get jaxws and wsa namespaces from them
 *
 * @author WS Development Team
 */
public class MetadataConstants {
    
    public static final String XML_PREFIX =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    
    public static final String GET_WXF_REQUEST =
        "http://schemas.xmlsoap.org/ws/2004/09/transfer/Get";
    public static final String GET_WXF_RESPONSE =
        "http://schemas.xmlsoap.org/ws/2004/09/transfer/GetResponse";
    
    public static final String MEX_NAMESPACE =
        "http://schemas.xmlsoap.org/ws/2004/09/mex";
    public static final String MEX_PREFIX = "wsx";
    public static final String WXF_NAMESPACE =
        "http://schemas.xmlsoap.org/ws/2004/09/transfer";
    public static final String WXF_PREFIX = "wxf";
    public static final String WSA_NAMESPACE =
        "http://www.w3.org/2005/08/addressing";
    
    public static final String SOAP_1_1 =
        "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP_1_2 =
        "http://www.w3.org/2003/05/soap-envelope";

    public static final String SCHEMA_DIALECT =
        "http://www.w3.org/2001/XMLSchema";
    public static final String WSDL_DIALECT =
        "http://schemas.xmlsoap.org/wsdl/";
    public static final String POLICY_DIALECT =
        "http://schemas.xmlsoap.org/ws/2004/09/policy";
    public static final String POLICYATT_DIALECT =
        "http://schemas.xmlsoap.org/ws/2004/09/policy/attachment";
    public static final String MEX_DIALECT =
        "http://schemas.xmlsoap.org/ws/2004/09/mex";
    
    /**
     * This is the logging level that is used for errors
     * that occur while retrieving metadata. May not need to
     * log as Level.SEVERE since some errors will be expected.
     * For instance, a soap 1.1 endpoint will return a version
     * mismatch fault when a soap 1.2 request is made.
     * <p>
     * Because this level may be changed as development continues,
     * we are storing it in one place.
     */
    public static final Level ERROR_LOG_LEVEL = Level.FINE;
    
}
