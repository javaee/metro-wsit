/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * PolicyLoggerTest.java
 * JUnit based test
 *
 * Created on February 21, 2007, 4:19 PM
 */

package com.sun.xml.ws.policy.privateutil;

import com.sun.xml.ws.policy.PolicyException;
import java.util.logging.Level;
import junit.framework.TestCase;
import static com.sun.xml.ws.policy.privateutil.PolicyUtils.Commons.getCallerMethodName;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class PolicyLoggerTest extends TestCase {
    private PolicyLogger instance;
    
    public PolicyLoggerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        instance = PolicyLogger.getLogger(PolicyLoggerTest.class);
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of getLogger method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testGetLogger() {
        PolicyLogger result = PolicyLogger.getLogger(PolicyLoggerTest.class);
        assertNotNull(result);
        
        try {
            PolicyLogger.getLogger(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) { /* ok */ }
    }
    
    /**
     * Test of log method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testLog() {
        Level level = Level.FINEST;
        String message = "Test";
        
        instance.log(level, message);
    }
    
    /**
     * Test of finest method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testFinest() {
        String message = "Test";
        
        instance.finest(message);
    }
    
    /**
     * Test of finer method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testFiner() {
        String message = "Test";
        
        instance.finer(message);
    }
    
    /**
     * Test of fine method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testFine() {
        String message = "Test";
        
        instance.fine(message);
    }
    
    /**
     * Test of info method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testInfo() {
        String message = "Test";
        
        instance.info(message);
    }
    
    /**
     * Test of config method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testConfig() {
        String message = "Test";
        
        instance.config(message);
    }
    
    /**
     * Test of warning method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testWarning() {
        String message = "Test";
        
        instance.warning(message);
    }
    
    /**
     * Test of severe method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testSevere() {
        String message = "Test";

        instance.severe(message);
    }
    
    /**
     * Test of isMethodCallLoggable method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testIsMethodCallLoggable() {
        boolean expResult = true;
        instance.setLevel(Level.FINEST);
        boolean result = instance.isMethodCallLoggable();
        assertEquals(expResult, result);

        expResult = false;
        instance.setLevel(Level.FINER);
        result = instance.isMethodCallLoggable();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of isLoggable method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testIsLoggable() {
        boolean expResult, result;
        
        instance.setLevel(Level.ALL);
        expResult = true;
        result = instance.isLoggable(Level.FINEST);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.FINER);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.FINE);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.CONFIG);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.INFO);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.WARNING);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.SEVERE);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.ALL);
        assertEquals(expResult, result);

        instance.setLevel(Level.OFF);
        expResult = false;
        result = instance.isLoggable(Level.FINEST);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.FINER);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.FINE);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.CONFIG);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.INFO);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.WARNING);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.SEVERE);
        assertEquals(expResult, result);
        result = instance.isLoggable(Level.ALL);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of setLevel method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testSetLevel() {
        instance.setLevel(Level.FINE);        
        assertFalse(instance.isLoggable(Level.FINER));
        assertTrue(instance.isLoggable(Level.INFO));
    }

    /**
     * Test of entering method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testEntering() {
        instance.entering();
    }
    
    /**
     * Test of exiting method, of class com.sun.xml.ws.policy.privateutil.PolicyLogger.
     */
    public void testExiting() {
        instance.exiting();
    }
    
    /**
     * Test of createAndLogException method, of class com.sun.xml.ws.policy.privateutil.PolicyUtils.Commons.
     */
    public void testCommonsCreateAndLogException() {
        PolicyLogger logger = PolicyLogger.getLogger(PolicyLoggerTest.class);
        Throwable cause, result;
        String message;
        
        cause = new Exception();
        message = "Test message.";
        result = logger.logSevereException(new IllegalArgumentException(message), cause);
        assertEquals(message, result.getMessage());
        assertEquals(cause, result.getCause());
        assertEquals("testCommonsCreateAndLogException", result.getStackTrace()[0].getMethodName());
        
        message = "Test message.";
        result = logger.logSevereException(new NullPointerException(message), cause);
        assertEquals(message, result.getMessage());
        assertEquals(cause, result.getCause());
        assertEquals("testCommonsCreateAndLogException", result.getStackTrace()[0].getMethodName());
        
        message = null;
        result = logger.logSevereException(new PolicyException(message), true);
        assertEquals(message, result.getMessage());
        assertEquals(null, result.getCause());
        assertEquals("testCommonsCreateAndLogException", result.getStackTrace()[0].getMethodName());
        
        message = "Test message.";
        result = logger.logSevereException(new PolicyException(message), false);
        assertEquals(message, result.getMessage());
        assertEquals(null, result.getCause());
        assertEquals("testCommonsCreateAndLogException", result.getStackTrace()[0].getMethodName());
        
        cause = new NullPointerException("test");
        message = null;
        result = logger.logSevereException(new PolicyException(message, cause), true);
        assertEquals(message, result.getMessage());
        assertEquals(cause, result.getCause());
        assertEquals("testCommonsCreateAndLogException", result.getStackTrace()[0].getMethodName());
        
        cause = new NullPointerException("test");
        message = "Test message.";
        result = logger.logSevereException(new PolicyException(message, cause), false);
        assertEquals(message, result.getMessage());
        assertEquals(cause, result.getCause());
        assertEquals("testCommonsCreateAndLogException", result.getStackTrace()[0].getMethodName());
        
        message = null;
        result = logger.logSevereException(new PolicyException(message));
        assertEquals(message, result.getMessage());
        assertEquals(null, result.getCause());
        assertEquals("testCommonsCreateAndLogException", result.getStackTrace()[0].getMethodName());
        
        message = "Test message.";
        result = logger.logSevereException(new PolicyException(message));
        assertEquals(message, result.getMessage());
        assertEquals(null, result.getCause());
        assertEquals("testCommonsCreateAndLogException", result.getStackTrace()[0].getMethodName());
        
        cause = new NullPointerException("test");
        message = null;
        result = logger.logSevereException(new PolicyException(message, cause));
        assertEquals(message, result.getMessage());
        assertEquals(cause, result.getCause());
        assertEquals("testCommonsCreateAndLogException", result.getStackTrace()[0].getMethodName());
        
        cause = new NullPointerException("test");
        message = "Test message.";
        result = logger.logSevereException(new PolicyException(message, cause));
        assertEquals(message, result.getMessage());
        assertEquals(cause, result.getCause());
        assertEquals("testCommonsCreateAndLogException", result.getStackTrace()[0].getMethodName());
    }
}
