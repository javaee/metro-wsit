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

package com.sun.xml.ws.mex;

import java.util.logging.Level;

/**
 * @author WS Development Team
 */
public class MetadataConstants {
    
    private static final String XMLSOAP_2004_09 =
        "http://schemas.xmlsoap.org/ws/2004/09/";
    
    public static final String GET_REQUEST = XMLSOAP_2004_09 + "transfer/Get";
    public static final String GET_RESPONSE =
        XMLSOAP_2004_09 + "transfer/GetResponse";
    public static final String GET_MDATA_REQUEST =
        XMLSOAP_2004_09 + "mex/GetMetadata/Request";
    
    public static final String MEX_NAMESPACE = XMLSOAP_2004_09 + "mex";
    public static final String MEX_PREFIX = "mex";
    
    // todo: get this from wsa api
    public static final String WSA_ANON =
        "http://www.w3.org/2005/08/addressing/anonymous";
    public static final String WSA_PREFIX = "wsa";
    
    public static final String SOAP_1_1 =
        "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP_1_2 =
        "http://www.w3.org/2003/05/soap-envelope";

    public static final String SCHEMA_DIALECT =
        "http://www.w3.org/2001/XMLSchema";
    public static final String WSDL_DIALECT =
        "http://schemas.xmlsoap.org/wsdl/";
    public static final String POLICY_DIALECT = XMLSOAP_2004_09 + "policy";
    
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
