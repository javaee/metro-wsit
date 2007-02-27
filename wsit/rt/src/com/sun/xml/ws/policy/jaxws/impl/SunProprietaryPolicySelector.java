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
