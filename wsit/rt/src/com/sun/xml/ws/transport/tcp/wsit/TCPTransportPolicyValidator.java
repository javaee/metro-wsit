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

package com.sun.xml.ws.transport.tcp.wsit;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator.Fitness;
import java.util.ArrayList;
import javax.xml.namespace.QName;

import static com.sun.xml.ws.transport.tcp.wsit.TCPConstants.*;

/**
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class TCPTransportPolicyValidator implements PolicyAssertionValidator {
    
    private static final ArrayList<QName> clientSupportedAssertions = new ArrayList<QName>(2);
    private static final ArrayList<QName> commonSupportedAssertions = new ArrayList<QName>(2);
    
    static {
        clientSupportedAssertions.add(SELECT_OPTIMAL_TRANSPORT_ASSERTION);
        commonSupportedAssertions.add(TCPTRANSPORT_POLICY_ASSERTION);
        commonSupportedAssertions.add(TCPTRANSPORT_CONNECTION_MANAGEMENT_ASSERTION);
    }
    
    /** Creates a new instance of TCPTransportPolicyValidator */
    public TCPTransportPolicyValidator() {
    }
    
    public PolicyAssertionValidator.Fitness validateClientSide(final PolicyAssertion assertion) {
        return clientSupportedAssertions.contains(assertion.getName()) ||
                commonSupportedAssertions.contains(assertion.getName()) ? Fitness.SUPPORTED : Fitness.UNKNOWN;
    }
    
    public PolicyAssertionValidator.Fitness validateServerSide(final PolicyAssertion assertion) {
        final QName assertionName = assertion.getName();
        return commonSupportedAssertions.contains(assertion.getName()) ? Fitness.SUPPORTED : Fitness.UNKNOWN;
    }
    
    public String[] declareSupportedDomains() {
        return new String[] {TCPTRANSPORT_POLICY_NAMESPACE_URI,
        CLIENT_TRANSPORT_NS, TCPTRANSPORT_CONNECTION_MANAGEMENT_NAMESPACE_URI};
    }
    
}
