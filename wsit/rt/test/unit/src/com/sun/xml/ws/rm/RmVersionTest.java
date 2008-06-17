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
package com.sun.xml.ws.rm;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class RmVersionTest extends TestCase {
    
    public RmVersionTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testUnmarshallElement() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("rm/elements/RM_1.1_TerminateSequence.xml"));                
        RmVersion.WSRM11.jaxbUnmarshaller.unmarshal(reader);
    }
    
//    /**
//     * Test of values method, of class RmVersion.
//     */
//    public void testValues() {
//        System.out.println("values");
//        RmVersion[] expResult = null;
//        RmVersion[] result = RmVersion.values();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of valueOf method, of class RmVersion.
//     */
//    public void testValueOf() {
//        System.out.println("valueOf");
//        String name = "";
//        RmVersion expResult = null;
//        RmVersion result = RmVersion.valueOf(name);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isRMAction method, of class RmVersion.
//     */
//    public void testIsRMAction() {
//        System.out.println("isRMAction");
//        String action = "";
//        RmVersion instance = null;
//        boolean expResult = false;
//        boolean result = instance.isRMAction(action);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
