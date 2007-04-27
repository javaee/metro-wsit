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
package com.sun.xml.ws.policy.jcaps;

import com.sun.xml.ws.policy.spi.AbstractQNameValidator;
import java.util.ArrayList;
import javax.xml.namespace.QName;

/**
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class JCapsPolicyValidator extends AbstractQNameValidator {
    public static final String NS_URI_BASIC_AUTHENTICATION_SECURITY_POLICY = "http://sun.com/ws/httpbc/security/BasicauthSecurityPolicy";    
    private static final ArrayList<QName> supportedAssertions = new ArrayList<QName>(2);
    
    static{
        supportedAssertions.add(new QName(NS_URI_BASIC_AUTHENTICATION_SECURITY_POLICY, "MustSupportBasicAuthentication"));
        supportedAssertions.add(new QName(NS_URI_BASIC_AUTHENTICATION_SECURITY_POLICY, "UsernameToken"));
    }
    
    /**
     * Creates new instance of JbiPolicyValidator
     */
    public JCapsPolicyValidator() {
        super(supportedAssertions, supportedAssertions);
    }
}
