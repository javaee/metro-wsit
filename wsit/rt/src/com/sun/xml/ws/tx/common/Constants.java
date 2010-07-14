/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.tx.common;


import javax.xml.namespace.QName;

/**
 * This class contains useful namespace uri constants
 *
 * @author Ryan.Shoemaker@Sun.COM, Joeseph.Fialli@Sun.COM
 * @version $Revision: 1.11.22.2 $
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
    
    // patch for wsit 419
    public static final QName WSP2002_OPTIONAL = 
            new QName("http://schemas.xmlsoap.org/ws/2002/12/policy", "Optional");
    
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

    public static final String WSAT_FAULT_ACTION_URI   = "http://schemas.xmlsoap.org/ws/2004/10/wsat/fault";
    public static final String WSCOOR_FAULT_ACTION_URI = "http://schemas.xmlsoap.org/ws/2004/10/wscoor/fault";

    public static final String UNKNOWN_ID = "-1";

}
