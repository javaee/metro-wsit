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

package com.sun.xml.ws.policy.jaxws.impl;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator.Fitness;
import java.util.ArrayList;
import javax.xml.namespace.QName;

import com.sun.xml.ws.security.impl.policy.Constants;

/**
 * Implements SPI for selecting wsit related SUN's proprietary assertions.
 *
 * @author japod
 */
public class SunProprietaryPolicySelector implements PolicyAssertionValidator{
    
    private static final ArrayList<QName> supportedAssertions = new ArrayList<QName>();
    
    static {
        
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,
                "CertStore"));
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,
                "CallbackHandlerConfiguration"));
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,
                "CallbackHandler"));
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,
                "ValidatorConfiguration"));
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,
                "Validator"));
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,
                "Timestamp"));
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS,
                "CertStore"));
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS,
                "CallbackHandlerConfiguration"));
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS,
                "CallbackHandler"));
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS,
                "ValidatorConfiguration"));
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS,
                "Validator"));
        supportedAssertions.add(new QName(
                Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS,
                "Timestamp"));
        supportedAssertions.add(new QName(
                Constants.SUN_SECURE_SERVER_CONVERSATION_POLICY_NS,
                "SCConfiguration"));
        supportedAssertions.add(new QName(
                Constants.SUN_SECURE_SERVER_CONVERSATION_POLICY_NS,
                "Lifetime"));
        supportedAssertions.add(new QName(
                Constants.SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS,
                "LifeTime"));
        supportedAssertions.add(new QName(
                Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS,
                "Contract"));
        supportedAssertions.add(new QName(
                Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS,
                "ServiceProviders"));
        supportedAssertions.add(new QName(
                Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS,
                "ServiceProvider"));
        supportedAssertions.add(new QName(
                Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS,
                "CertAlias"));
        supportedAssertions.add(new QName(
                Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS,
                "TokenType"));
        supportedAssertions.add(new QName(
                Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS,
                "KeyType"));
        supportedAssertions.add(new QName(
                Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS,
                "Issuer"));
        supportedAssertions.add(new QName(
                Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS,
                "LifeTime"));        
    }
    
    /** Creates a new instance of SunProprietaryPolicySelector */
    public SunProprietaryPolicySelector() {
        // nothing to initialize
    }
    
    public PolicyAssertionValidator.Fitness validateClientSide(final PolicyAssertion assertion) {
        return supportedAssertions.contains(assertion.getName()) ? Fitness.SUPPORTED : Fitness.UNKNOWN;
    }

    public PolicyAssertionValidator.Fitness validateServerSide(final PolicyAssertion assertion) {
        return Fitness.UNKNOWN;
    }

    public String[] declareSupportedDomains() {
        return new String[] {};
    }
    
}
