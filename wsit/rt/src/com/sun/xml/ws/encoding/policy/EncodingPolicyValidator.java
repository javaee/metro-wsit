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

package com.sun.xml.ws.encoding.policy;

import com.sun.xml.ws.policy. PolicyAssertion;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator.Fitness;
import java.util.ArrayList;
import javax.xml.namespace.QName;

import static com.sun.xml.ws.encoding.policy.EncodingConstants.*;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
public class EncodingPolicyValidator implements PolicyAssertionValidator {

    private static final ArrayList<QName> serverSideSupportedAssertions = new ArrayList<QName>(3);
    private static final ArrayList<QName> clientSideSupportedAssertions = new ArrayList<QName>(4);
    
    static {
        serverSideSupportedAssertions.add(OPTIMIZED_MIME_SERIALIZATION_ASSERTION);
        serverSideSupportedAssertions.add(UTF816FFFE_CHARACTER_ENCODING_ASSERTION);
        serverSideSupportedAssertions.add(OPTIMIZED_FI_SERIALIZATION_ASSERTION);
        
        clientSideSupportedAssertions.add(SELECT_OPTIMAL_ENCODING_ASSERTION);
        clientSideSupportedAssertions.addAll(serverSideSupportedAssertions);
    }
    
    /**
     * Creates a new instance of EncodingPolicyValidator
     */
    public EncodingPolicyValidator() {
    }
    
    public Fitness validateClientSide(PolicyAssertion assertion) {
        return clientSideSupportedAssertions.contains(assertion.getName()) ? Fitness.SUPPORTED : Fitness.UNKNOWN;
    }

    public Fitness validateServerSide(PolicyAssertion assertion) {
        QName assertionName = assertion.getName();
        if (serverSideSupportedAssertions.contains(assertionName)) {
            return Fitness.SUPPORTED;
        } else if (clientSideSupportedAssertions.contains(assertionName)) {
            return Fitness.UNSUPPORTED;
        } else {
            return Fitness.UNKNOWN;
        }
    }

    public String[] declareSupportedDomains() {
        return new String[] {OPTIMIZED_MIME_NS, ENCODING_NS, SUN_ENCODING_CLIENT_NS, SUN_FI_SERVICE_NS};
    }
}
