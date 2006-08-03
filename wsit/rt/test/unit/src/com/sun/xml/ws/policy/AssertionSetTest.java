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

import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


/**
 *
 */
public class AssertionSetTest extends AbstractPolicyApiClassTestBase {
    
    public AssertionSetTest(String testName) throws PolicyException {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    protected AssertionSet[][] getEqualInstanceRows() throws Exception {
        Collection<AssertionSet[]> rows = new LinkedList<AssertionSet[]>();
        
        for (String name : PolicyResourceLoader.SINGLE_ALTERNATIVE_POLICY) {
            Iterator<AssertionSet> iteratorA = PolicyResourceLoader.loadPolicy(name).iterator();
            Iterator<AssertionSet> iteratorB = PolicyResourceLoader.loadPolicy(name).iterator();
            
            if (iteratorA.hasNext()) {
                rows.add(new AssertionSet[] {iteratorA.next(), iteratorB.next()});
            }            
        }
        
        return rows.toArray(new AssertionSet[rows.size()][]);
    }    
}
