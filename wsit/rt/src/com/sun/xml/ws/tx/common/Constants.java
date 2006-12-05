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
package com.sun.xml.ws.tx.common;


import javax.xml.namespace.QName;

/**
 * This class contains useful namespace uri constants
 *
 * @author Ryan.Shoemaker@Sun.COM, Joeseph.Fialli@Sun.COM
 * @version $Revision: 1.1.2.1 $
 * @since 1.0
 */
public class Constants {
    /**
     * XML namespace uri
     */
    public static final String XMLNS_URI =
            "http://www.w3.org/2000/xmlns/";

    /**
     * WS-Coordination SOAP namespace uri
     */
    public static final String WSCOOR_SOAP_NSURI =
            "http://schemas.xmlsoap.org/ws/2004/10/wscoor";

    /**
     * WS-AtomicTransaction SOAP namespace uri
     */
    public static final String WSAT_SOAP_NSURI =
            "http://schemas.xmlsoap.org/ws/2004/10/wsat";

    public static final String WSAT_2004_PROTOCOL = WSAT_SOAP_NSURI;

    /**
     * WS-Coordination OASIS namespace uri
     */
    public static final String WSCOOR_OASIS_NSURI =
            "http://docs.oasis-open.org/ws-tx/wscoor/2006/06";

    /**
     * WS-AtomicTransaction OASIS namespace uri
     */
    public static final String WSAT_OASIS_NSURI =
            "http://docs.oasis-open.org/ws-tx/wsat/2006/06";

    /**
     * WS-Coordination RI namespace uri
     */
    public static final String WSCOOR_SUN_URI =
            "http://java.sun.com/xml/ns/wsit/coord";

    /**
     * WS-Policy namespace uri and wsp:optional
     * <p/>
     * Note: both of these are defined in PolicyConstants, hence the deprecation
     */
    @Deprecated
    public static final String WSP_URI =
            "http://schemas.xmlsoap.org/ws/2004/09/policy";
    @Deprecated
    public static final QName WSP_OPTIONAL =
            new QName(WSP_URI, "optional");

    // Currently supported standard.
    public static final String WSAT_NS = WSAT_SOAP_NSURI;

    public static final String WSAT_PREFIX = WSAT_NS + "/";

    /**
     * QName object for wsat:ATAssertion - propogation of atomic transaction with a request
     */
    public static final QName AT_ASSERTION =
            new QName(WSAT_SOAP_NSURI, "ATAssertion");

    /**
     * QName object for wsat:ATAlwaysCapability - create atomic transaction context if one not propogated
     */
    public static final QName AT_ALWAYS_CAPABILITY =
            new QName(WSAT_SOAP_NSURI, "ATAlwaysCapability");

    public static final String WSAT_COMPLETION_PROTOCOL =
            WSAT_PREFIX + "Completion";

    public static final String WSAT_DURABLE2PC_PROTOCOL =
            WSAT_PREFIX + "Durable2PC";

    public static final String WSAT_VOLATILE2PC_PROTOCOL =
            WSAT_PREFIX + "Volatile2PC";

    /**
     * SOAP Namespace URI
     */
    public static final String SOAP_NSURI = "http://www.w3.org/2003/05/soap-envelope";

    /**
     * SOAP Namespace URI Prefix
     */
    public static final String SOAP_PREFIX = "soap";


    public static final String COORDINATION_CONTEXT = "CoordinationContext";

    public static final String PREPARED_ACTION = WSAT_PREFIX + "/Prepared";
    public static final String READONLY_ACTION = WSAT_PREFIX + "/ReadOnly";
    public static final String ABORTED_ACTION = WSAT_PREFIX + "/Aborted";
    public static final String COMMITTED_ACTION = WSAT_PREFIX + "/Committed";

    // use as key for transaction resource that references wsat transactional context
    public static final String WSAT_TRANSACTION_CONTEXT = "wsatTransactionContext";

    public static final String WSAT_FAULT_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/10/wsat/fault";

    public static final String WSTX_WS_SCHEME = "https";
    public static final int WSTX_WS_PORT = 8181;
    public static final String WSTX_WS_CONTEXT;

    static {
        String ctx = System.getProperty("com.sun.xml.ws.tx.contextroot", null);
        if (ctx != null) {
            WSTX_WS_CONTEXT = ctx;
        } else {
            WSTX_WS_CONTEXT = "/__wstx-services";
        }
    }

    public static final String UNKNOWN_ID = "-1";

}
