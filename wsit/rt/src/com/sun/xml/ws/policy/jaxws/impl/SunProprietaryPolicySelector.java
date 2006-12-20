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

/**
 * Implements SPI for selecting wsit related SUN's proprietary assertions.
 *
 * @author japod
 */
public class SunProprietaryPolicySelector implements PolicyAssertionValidator{
    
    private static final ArrayList<QName> supportedAssertions = new ArrayList<QName>();
    
    static {
        
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/server",
                "CertStore"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/server",
                "CallbackHandlerConfiguration"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/server",
                "CallbackHandler"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/server",
                "ValidatorConfiguration"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/server",
                "Validator"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/server",
                "Timestamp"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/client",
                "CertStore"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/client",
                "CallbackHandlerConfiguration"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/client",
                "CallbackHandler"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/client",
                "ValidatorConfiguration"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/client",
                "Validator"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/2006/03/wss/client",
                "Timestamp"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/ws/2006/05/sc/server",
                "SCConfiguration"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/ws/2006/05/sc/server",
                "Lifetime"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/ws/2006/05/sc/client",
                "LifeTime"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/ws/2006/05/trust/server",
                "Contract"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/ws/2006/05/trust/server",
                "ServiceProviders"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/ws/2006/05/trust/server",
                "ServiceProvider"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/ws/2006/05/trust/server",
                "CertAlias"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/ws/2006/05/trust/server",
                "TokenType"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/ws/2006/05/trust/server",
                "KeyType"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/ws/2006/05/trust/server",
                "Issuer"));
        supportedAssertions.add(new QName(
                "http://schemas.sun.com/ws/2006/05/trust/server",
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
