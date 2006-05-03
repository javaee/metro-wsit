/*
 * $Id: TrustPlugin.java,v 1.1 2006-05-03 22:57:14 arungupta Exp $
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

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.IssuedTokenContext;
import java.net.URL;

import java.util.Map;
import javax.xml.namespace.QName;

public interface TrustPlugin {

    /**
     * Obtain the Token by using WS-Trust or WS-SecureConversation.
     * @param issuedToken, an instance of <sp:IssuedToken> or <sp:SecureConversation> assertion
     * @return issuedTokenContext, a context containing the issued Token and related information
     */
    public IssuedTokenContext process(PolicyAssertion issuedToken, URL stsEP, URL wsdlLocation, QName serviceName, QName portName, String appliesTo);

}
