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

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.InputStreamReader;
import java.io.Reader;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar
 */
public class PolicySourceModelTest extends TestCase {    
    public PolicySourceModelTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testCloneModel() throws Exception {
        PolicySourceModel model = PolicyResourceLoader.unmarshallModel("complex_policy/nested_assertions_with_alternatives.xml");
        PolicySourceModel clone = model.clone();
        
        //System.out.println("Model: \n" + model.toString());
        //System.out.println("Clone: \n" + clone.toString());
        //System.out.println("====================================================================");
        model.toString();
        clone.toString();
        assertEquals(model, clone);
    }
 }
