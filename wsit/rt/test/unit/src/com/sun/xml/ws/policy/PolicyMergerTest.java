/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.policy;

import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
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
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
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
    
    public void testMergeNoAltPolicies() throws Exception {
        Collection<Policy> policies = new LinkedList<Policy>();
        policies.add(PolicyResourceLoader.loadPolicy("merge/policy1.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("merge/policy-no-alt.xml"));
        
        Policy result = merger.merge(policies);
        Policy expected = PolicyResourceLoader.loadPolicy("merge/policy-no-alt.xml");
        
        assertEquals(expected, result);
    }
    
    public void testMergeNamespaces() throws Exception {
        Collection<Policy> policies = new LinkedList<Policy>();
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.2.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.2.xml"));
        Policy result = merger.merge(policies);
        assertEquals(
                "When merging policies with same original namespace, the namespace should be preserved during merge operation", 
                NamespaceVersion.v1_2, 
                result.getNamespaceVersion()
                );        

        policies.clear();
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.5.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.5.xml"));
        result = merger.merge(policies);
        assertEquals(
                "When merging policies with same original namespace, the namespace should be preserved during merge operation", 
                NamespaceVersion.v1_5, 
                result.getNamespaceVersion()
                );        
    
        policies.clear();
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.2.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.5.xml"));
        result = merger.merge(policies);
        assertEquals(
                "When merging policies with different original namespace, the latest namespace should be preserved during merge operation", 
                NamespaceVersion.v1_5, 
                result.getNamespaceVersion()
                );            
    }   
}
