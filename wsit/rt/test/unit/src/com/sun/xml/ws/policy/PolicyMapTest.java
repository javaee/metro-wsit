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
package com.sun.xml.ws.policy;

import java.util.Arrays;
import java.util.LinkedList;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public class PolicyMapTest extends TestCase {
    
    public PolicyMapTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public void testCreatePolicyMapWithNullMutatorCollection() throws Exception {
        assertNotNull("Policy map instance should not be null", PolicyMap.createPolicyMap(null));
    }
    
    public void testCreatePolicyMapWithEmptyMutatorCollection() throws Exception {
        assertNotNull("Policy map instance should not be null", PolicyMap.createPolicyMap(new LinkedList<PolicyMapMutator>()));        
    }

    public void testCreatePolicyMapWithNonemptyMutatorCollection() throws Exception {
        assertNotNull("Policy map instance should not be null", PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {PolicyMapExtender.createPolicyMapExtender()})));                
    }
    
    public void testIsEmpty() {
        PolicyMap map = PolicyMap.createPolicyMap(null);
        assertTrue(map.isEmpty());
        
        map = PolicyMap.createPolicyMap(new LinkedList<PolicyMapMutator>());
        assertTrue(map.isEmpty());
        
        map = PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {PolicyMapExtender.createPolicyMapExtender()}));
        assertTrue(map.isEmpty());
        
        PolicyMapExtender extender = PolicyMapExtender.createPolicyMapExtender();
        PolicyMapMutator[] mutators = new PolicyMapMutator[] {extender};
        PolicyMapKey key = map.createWsdlServiceScopeKey(new QName("service"));
        map = PolicyMap.createPolicyMap(Arrays.asList(mutators));
        extender.putServiceSubject(key, null);
        assertFalse(map.isEmpty());

        mutators[0].disconnect();
        key = map.createWsdlEndpointScopeKey(new QName("service"), new QName("port"));
        map = PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {extender}));
        extender.putEndpointSubject(key, null);
        assertFalse(map.isEmpty());

        mutators[0].disconnect();
        key = map.createWsdlOperationScopeKey(new QName("service"), new QName("port"), new QName("operation"));
        map = PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {extender}));
        extender.putOperationSubject(key, null);
        assertFalse(map.isEmpty());

        mutators[0].disconnect();
        key = map.createWsdlMessageScopeKey(new QName("service"), new QName("port"), new QName("operation"));
        map = PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {extender}));
        extender.putInputMessageSubject(key, null);
        assertFalse(map.isEmpty());

        mutators[0].disconnect();
        key = map.createWsdlMessageScopeKey(new QName("service"), new QName("port"), new QName("operation"));
        map = PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {extender}));
        extender.putOutputMessageSubject(key, null);
        assertFalse(map.isEmpty());

        mutators[0].disconnect();
        key = map.createWsdlMessageScopeKey(new QName("service"), new QName("port"), new QName("operation"));
        map = PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {extender}));
        extender.putFaultMessageSubject(key, null);
        assertFalse(map.isEmpty());
    }
}
