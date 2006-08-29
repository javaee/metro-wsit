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
/*
 * SymmetricKeyGenerationTest.java
 *
 * Created on April 7, 2006, 5:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl;

import com.sun.xml.ws.security.impl.DerivedKeyTokenImpl;
import com.sun.xml.ws.security.DerivedKeyToken;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import javax.crypto.SecretKey;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author ashutosh.shahi@sun.com
 */
public class SymmetricKeyGenerationTest extends TestCase{
    
    /** Creates a new instance of SymmetricKeyGenerationTest */
    public SymmetricKeyGenerationTest(String testName) {
        super(testName);
    }
     
    protected void setUp() throws Exception {
	    
    }
                                                                                                                                                             
    protected void tearDown() throws Exception {
    }
                                                                                                                                                             
    public static Test suite() {
        TestSuite suite = new TestSuite(EndorsingSignatureTest.class);
                                                                                                                                                             
        return suite;
    }
    
    public static void testSymmetricKeyGenerationTest() throws Exception {
            String algorithm = MessageConstants.AES_BLOCK_ENCRYPTION_256;
            byte[] secret = "randombytes".getBytes();
            long offset = 0;
            long length = SecurityUtil.getLengthFromAlgorithm(algorithm);
            DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, secret);
            SecretKey sKey = dkt.generateSymmetricKey(algorithm);
            assertEquals(sKey.getEncoded().length, length); 
    }
    
   public static void main(String[] args) throws Exception{
       testSymmetricKeyGenerationTest();
   }
}
