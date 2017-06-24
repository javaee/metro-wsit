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

    /** URI for DerivedKey token type */
    public static final String DERIVED_KEY_TOKEN_TYPE = WSC_NAMESPACE + "/dk";        
    
    /** SecurityContextToken Type String */
    public static final String SECURITY_CONTEXT_TOKEN = "SecurityContextToken";
    
    public static final String SECURITY_CONTEXT_ID = "Incomimg_SCT";
    
    /** Action URIs */
    public static final String REQUEST_SECURITY_CONTEXT_TOKEN_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/SCT";    
    public static final String REQUEST_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RSTR/SCT";    
    
    public static final String RENEW_SECURITY_CONTEXT_TOKEN_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/SCT/Renew";
    public static final String RENEW_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RSTR/SCT/Renew";
    
    public static final String CANCEL_SECURITY_CONTEXT_TOKEN_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/SCT/Cancel";    
    public static final String CANCEL_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RSTR/SCT/Cancel";            
    
}
