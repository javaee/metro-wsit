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

package com.sun.xml.ws.policy.util;

import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicyMapMutator;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.util.Arrays;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

/**
 *
 * @author fr159072
 */
public class PolicyMapUtilTest extends TestCase {
    
    public PolicyMapUtilTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of rejectAlternatives method, of class com.sun.xml.ws.policy.util.PolicyMapUtil.
     */
    public void testRejectAlternatives() throws Exception {
        PolicyMap map = null;
        try {
            PolicyMapUtil.rejectAlternatives(map);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }

        PolicyMapExtender extender = PolicyMapExtender.createPolicyMapExtender();
        PolicyMapMutator[] mutators = new PolicyMapMutator[] {extender};
        PolicyMapKey key = map.createWsdlServiceScopeKey(new QName("service"));
        map = PolicyMap.createPolicyMap(Arrays.asList(mutators));
        Policy policy = Policy.createEmptyPolicy();
        PolicySubject subject = new PolicySubject(new Object(), policy);
        extender.putServiceSubject(key, subject);
        PolicyMapUtil.rejectAlternatives(map);
        
        mutators[0].disconnect();
        key = map.createWsdlEndpointScopeKey(new QName("service"), new QName("port"));
        map = PolicyMap.createPolicyMap(Arrays.asList(mutators));
        policy = PolicyResourceLoader.loadPolicy("merge/policy2.xml");
        subject = new PolicySubject(new Object(), policy);
        extender.putServiceSubject(key, subject);
        try {
            PolicyMapUtil.rejectAlternatives(map);
            fail("Expected PolicyException");
        } catch (PolicyException e) {
        }
        
    }
    
}
