/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

/*
 * WSSAssertion.java
 *
 * Created on September 5, 2006, 12:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sk112103
 */
public class WSSAssertion {
    
    Set<String> requiredPropSet;
    String version = "1.0";
    
    /** Creates a new instance of WSSAssertion */
    public WSSAssertion(Set<String> props, String version) {
        requiredPropSet = props;
        //fix for wsit issue# 510
        if (requiredPropSet == null) {
            requiredPropSet = new HashSet<String>();
        }
        this.version = version;
    }
    
    public final static String MUSTSUPPORT_REF_THUMBPRINT = "MustSupportRefThumbprint";
    public final static String MUSTSUPPORT_REF_ENCRYPTED_KEY = "MustSupportRefEncryptedKey";
    public final static String REQUIRE_SIGNATURE_CONFIRMATION = "RequireSignatureConfirmation";    
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
    public Set getRequiredProperties() {
        return requiredPropSet;
    }
    /**
     * WSS version
     * @return 1.0
     */
    public String getType() {
        return version;    
    }

}
