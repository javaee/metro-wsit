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
/*
 * RMPolicySelector.java
 *
 * @author Mike Grogan
 * Created on April 13, 2006, 11:40 AM
 *
 */


import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.spi.PolicySelector;
import static com.sun.xml.ws.rm.RMConstants.*;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
/**
 *
 */
public class RMPolicySelector extends PolicySelector {

    private static final ArrayList<QName> supportedAssertions = new ArrayList<QName>();
        
    private static final QName rmQName = new QName(version, "RMAssertion");
    private static final QName flowQName = new QName(microsoftVersion, "RmFlowControl");
    private static final QName orderedQName = new QName(sunVersion, "Ordered");
    private static final QName resendIntervalQName = new QName(sunVersion, "ResendInterval");
    private static final QName ackRequestIntervalQName = new QName(sunVersion, "AckRequestInterval");
    static {
        supportedAssertions.add(new QName(version, "AcknowledgementInterval"));
        supportedAssertions.add(new QName(version, "InactivityTimeout"));
        supportedAssertions.add(new QName(microsoftVersion, "MaxReceiveBufferSize"));
        supportedAssertions.add(new QName(sunVersion, "Ordered"));
        supportedAssertions.add(new QName(sunVersion, "ResendInterval"));
        supportedAssertions.add(new QName(sunVersion, "AckRequestInterval"));
    }
    
    public RMPolicySelector() {
        super(supportedAssertions);
    }
        
    public boolean test(PolicyAssertion assertion) {
        QName qname = assertion.getName();
        if (qname.equals(flowQName) || 
            qname.equals(rmQName) ||
            qname.equals(orderedQName) ||
            qname.equals(resendIntervalQName) ||
            qname.equals(ackRequestIntervalQName)){
            
            Iterator<PolicyAssertion> i = assertion.getNestedAssertionsIterator();
            while (i != null && i.hasNext()) {
                if (! supportedAssertions.contains(i.next().getName())) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

}
