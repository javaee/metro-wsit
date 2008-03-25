/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xml.ws.rm;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import junit.framework.TestCase;

/**
 *
 * @author m_potociar
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
