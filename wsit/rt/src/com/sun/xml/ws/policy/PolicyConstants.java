/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.policy;

import javax.xml.namespace.QName;

/**
 * Commonly used constants by the policy implementations
 */
public final class PolicyConstants {
    
    /**
     * Standard WS-Policy namespace URI
     */       
    public static final String POLICY_NAMESPACE_URI = "http://schemas.xmlsoap.org/ws/2004/09/policy";

    /**
     * Sun proprietary policy namespace URI
     */       
    public static final String SUN_POLICY_NAMESPACE_URI = "http://java.sun.com/xml/ns/wsit/policy";

    /**
     * Default WS-Policy namespace prefix
     */       
    public static final String POLICY_NAMESPACE_PREFIX = "wsp";

    /**
     * Sun proprietary policy namespace prefix
     */       
    public static final String SUN_POLICY_NAMESPACE_PREFIX = "sunwsp";

    /**
     * Standard WS-Security Utility namespace URI, used in Policy Id
     */
    public static final String WSU_NAMESPACE_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    
    /**
     * Standard WS-Security Utility namespace prefix, used in Policy Id
     */
    public static final String WSU_NAMESPACE_PREFIX = "wsu";

    /**
     * Standard XML namespace URI
     */       
    public static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";

    /**
     * Fully qualified name of the SUN's proprietary policy assertion visibility attribute
     */       
    public static final QName VISIBILITY_ATTRIBUTE = new QName(SUN_POLICY_NAMESPACE_URI, "visibility");

    /**
     * Recognized value of the SUN's proprietary policy assertion visibility attribute
     */       
    public static final String VISIBILITY_VALUE_PRIVATE = "private";
    
    /**
     * Fully qualified name of the WS-Policy Policy XML element
     */       
    public static final QName POLICY = new QName(POLICY_NAMESPACE_URI, "Policy");

    /**
     * Fully qualified name of the Policy wsu:Id XML attribute
     */       
    public static final QName WSU_ID = new QName(WSU_NAMESPACE_URI, "Id");

    /**
     * Fully qualified name of the xml:id policy attribute
     */       
    public static final QName XML_ID = new QName(XML_NAMESPACE_URI, "id");

    /**
     * Fully qualified name of the WS-Policy Policy Id XML attribute
     */       
    public static final QName POLICY_NAME = new QName(POLICY_NAMESPACE_URI, "Name");

    /**
     * Fully qualified name of the WS-Policy Policy XML element
     */       
    public static final QName POLICY_REFERENCE = new QName(POLICY_NAMESPACE_URI, "PolicyReference");

    /**
     * Fully qualified name of the WS-Policy ExactlyOne XML element
     */       
    public static final QName EXACTLY_ONE = new QName(POLICY_NAMESPACE_URI, "ExactlyOne");

    /**
     * Fully qualified name of the WS-Policy All XML element
     */       
    public static final QName ALL = new QName(POLICY_NAMESPACE_URI, "All");

    /**
     * Fully qualified name of the WS-PolicyAttachment UsingPolicy XML element
     */
    public static final QName USING_POLICY = new QName(POLICY_NAMESPACE_URI, "UsingPolicy");
    
    /**
     * Fully qualified name of the WS-Policy Optional XML attribute
     */
    public static final QName OPTIONAL = new QName(POLICY_NAMESPACE_URI, "Optional");
    
    /**
     * Fully qualified name of the WS-Policy Optional XML attribute
     */
    public static final QName IGNORABLE = new QName(POLICY_NAMESPACE_URI, "Ignorable");

    /**
     * Fully qualified name of the WS-Policy URI XML attribute
     */
    public static final QName POLICY_URI = new QName(null, "URI");
    
    /**
     * Fully qualified name of the WS-Policy PolicyURIs XML attribute
     */
    public static final QName POLICY_URIs = new QName(POLICY_NAMESPACE_URI, "PolicyURIs");
    
    /**
     * Name under which we look up a default factory implementation
     */       
    public static final String POLICY_FACTORY_PROPERTY = "com.sun.xml.ws.policy.PolicyFactory";

    /**
     * Class name of the default factory implementation
     */       
    public static final String DEFAULT_POLICY_FACTORY_IMPLEMENTATION = "com.sun.xml.ws.policy.wspol.WSPolicyFactory";
    
    /**
     * Identifier of the client-side configuration file 
     */
    public static final String CLIENT_CONFIGURATION_IDENTIFIER = "client";
    
    /**
     * Prevent instantiation of this class.
     */
    private PolicyConstants() {
        // nothing to initialize
    }
}
