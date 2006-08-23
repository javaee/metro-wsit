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

