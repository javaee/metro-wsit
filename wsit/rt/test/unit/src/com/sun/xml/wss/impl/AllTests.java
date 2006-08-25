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
package com.sun.xml.wss.impl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
                                                                                                                        
public class AllTests extends TestCase {
    public AllTests(String name) {
        super(name);
    }
                                                                                                                        
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(EndorsingSignatureTest.class);
        suite.addTestSuite(AsymmetricBindingTest.class);

      //  suite.addTestSuite(SCTDKTTest.class);
      //  suite.addTestSuite(SecurityContextTokenTest.class);
        suite.addTestSuite(SignAllHeadersTest.class);
        suite.addTestSuite(SignatureConfirmationTest.class);
        suite.addTestSuite(SignSOAPHeadersOnlyTest.class);
        suite.addTestSuite(SymmetricBindingTest.class);
        suite.addTestSuite(SymmetricDktTest.class);
        suite.addTestSuite(SymmetricKeyGenerationTest.class);
        suite.addTestSuite(TimestampTest.class);
        suite.addTestSuite(TrustTest.class);
        suite.addTestSuite(TrustDKTTest.class);

        return suite;
    }
                                                                                                                        
    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(AllTests.class);
    }
}

