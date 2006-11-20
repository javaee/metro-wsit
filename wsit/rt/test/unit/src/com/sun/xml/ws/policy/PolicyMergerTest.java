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

import java.util.Collection;
import java.util.LinkedList;
import junit.framework.TestCase;

import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;

/**
 *
 * @author Marek Potociar
 */
public class PolicyMergerTest extends TestCase {
    private PolicyMerger merger = PolicyMerger.getMerger();
    
    public PolicyMergerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testMergeTwoPolicies() throws Exception {
        Collection<Policy> policies = new LinkedList<Policy>();
        policies.add(PolicyResourceLoader.loadPolicy("merge/policy1.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("merge/policy2.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("merge/policy3.xml"));
        
        Policy result = merger.merge(policies);
        Policy expected = PolicyResourceLoader.loadPolicy("merge/merge_1-2-3.xml");
        
        assertEquals(expected, result);
    }
    
    public void testMergeEmtpyNonEmptyPolicies() throws Exception {
        Collection<Policy> policies = new LinkedList<Policy>();
        policies.add(PolicyResourceLoader.loadPolicy("merge/policy1.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("merge/policy-empty-alt.xml"));
        
        Policy result = merger.merge(policies);
        Policy expected = PolicyResourceLoader.loadPolicy("merge/policy1.xml");
        
        assertEquals(expected, result);
    }
    
}
