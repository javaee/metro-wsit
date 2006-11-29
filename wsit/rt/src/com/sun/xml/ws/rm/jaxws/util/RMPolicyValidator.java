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
package com.sun.xml.ws.rm.jaxws.util;


import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator.Fitness;
import static com.sun.xml.ws.rm.Constants.*;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

/*
 * RMPolicyValidator.java
 *
 * @author Mike Grogan
 * Created on April 13, 2006, 11:40 AM
 *
 */
public class RMPolicyValidator implements PolicyAssertionValidator {

    private static final ArrayList<QName> serverSideSupportedAssertions = new ArrayList<QName>(4);
    private static final ArrayList<QName> clientSideSupportedAssertions = new ArrayList<QName>(6);

    static {
        serverSideSupportedAssertions.add(new QName(version, "RMAssertion"));
        serverSideSupportedAssertions.add(new QName(sunVersion, "Ordered"));
        serverSideSupportedAssertions.add(new QName(sunVersion, "AllowDuplicates"));
        serverSideSupportedAssertions.add(new QName(microsoftVersion, "RmFlowControl"));

        clientSideSupportedAssertions.add(new QName(sunClientVersion, "AckRequestInterval"));
        clientSideSupportedAssertions.add(new QName(sunClientVersion, "ResendInterval"));        
        clientSideSupportedAssertions.addAll(serverSideSupportedAssertions);
    }
    
    public RMPolicyValidator() {
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
        return new String[] {version, microsoftVersion, sunVersion, sunClientVersion};
    }
}
