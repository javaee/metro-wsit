/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * InvocationTest.java
 * JUnit based test
 *
 * Created on March 2, 2007, 11:44 AM
 */

package com.sun.xml.ws.xmlfilter;

import javax.xml.stream.XMLStreamWriter;
import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class InvocationTest extends TestCase {
    
    public InvocationTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of createInvocation method, of class com.sun.xml.ws.policy.jaxws.xmlstreamwriter.Invocation.
     */
    public void testCreateInvocation() throws Exception {
        Method method = XMLStreamWriter.class.getMethod(XmlStreamWriterMethodType.WRITE_START_ELEMENT.getMethodName(), String.class);
        Object[] args = new Object[] {"test"};
        Invocation result = Invocation.createInvocation(method, args);
        
        assertNotNull(result);
    }
    
    /**
     * Test of getMethodName method, of class com.sun.xml.ws.policy.jaxws.xmlstreamwriter.Invocation.
     */
    public void testGetMethodName() throws Exception {
        String methodName;
        Method method;
        Object[] args;
        Invocation result;
        
        methodName = XmlStreamWriterMethodType.WRITE_START_ELEMENT.getMethodName();
        method = XMLStreamWriter.class.getMethod(methodName, String.class);
        args = new Object[] {"test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodName, result.getMethodName());
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class);
        args = new Object[] {"test", "test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodName, result.getMethodName());
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class, String.class);
        args = new Object[] {"test", "test", "test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodName, result.getMethodName());
        
        methodName = XmlStreamWriterMethodType.WRITE_END_ELEMENT.getMethodName();
        method = XMLStreamWriter.class.getMethod(methodName);
        args = null;
        result = Invocation.createInvocation(method, args);
        assertEquals(methodName, result.getMethodName());
        
        methodName = XmlStreamWriterMethodType.WRITE_ATTRIBUTE.getMethodName();
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class);
        args = new Object[] {"test", "test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodName, result.getMethodName());
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class, String.class);
        args = new Object[] {"test", "test", "test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodName, result.getMethodName());
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class, String.class, String.class);
        args = new Object[] {"test", "test", "test", "test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodName, result.getMethodName());
        
        methodName = XmlStreamWriterMethodType.WRITE_CHARACTERS.getMethodName();
        method = XMLStreamWriter.class.getMethod(methodName, String.class);
        args = new Object[] {"test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodName, result.getMethodName());
        method = XMLStreamWriter.class.getMethod(methodName, char[].class, int.class, int.class);
        args = new Object[] {new char[] {'t', 'e', 's', 't'}, 0, 4};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodName, result.getMethodName());
        
        methodName = XmlStreamWriterMethodType.FLUSH.getMethodName();
        method = XMLStreamWriter.class.getMethod(methodName);
        args = null;
        result = Invocation.createInvocation(method, args);
        assertEquals(methodName, result.getMethodName());
        
    }
    
    /**
     * Test of getMethodType method, of class com.sun.xml.ws.policy.jaxws.xmlstreamwriter.Invocation.
     */
    public void testGetMethodType() throws Exception {
        String methodName;
        XmlStreamWriterMethodType methodType;
        Method method;
        Object[] args;
        Invocation result;
        
        methodName = XmlStreamWriterMethodType.WRITE_START_ELEMENT.getMethodName();
        methodType = XmlStreamWriterMethodType.WRITE_START_ELEMENT;
        method = XMLStreamWriter.class.getMethod(methodName, String.class);
        args = new Object[] {"test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodType, result.getMethodType());
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class);
        args = new Object[] {"test", "test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodType, result.getMethodType());
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class, String.class);
        args = new Object[] {"test", "test", "test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodType, result.getMethodType());
        
        methodName = XmlStreamWriterMethodType.WRITE_END_ELEMENT.getMethodName();
        methodType = XmlStreamWriterMethodType.WRITE_END_ELEMENT;
        method = XMLStreamWriter.class.getMethod(methodName);
        args = null;
        result = Invocation.createInvocation(method, args);
        assertEquals(methodType, result.getMethodType());
        
        methodName = XmlStreamWriterMethodType.WRITE_ATTRIBUTE.getMethodName();
        methodType = XmlStreamWriterMethodType.WRITE_ATTRIBUTE;
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class);
        args = new Object[] {"test", "test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodType, result.getMethodType());
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class, String.class);
        args = new Object[] {"test", "test", "test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodType, result.getMethodType());
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class, String.class, String.class);
        args = new Object[] {"test", "test", "test", "test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodType, result.getMethodType());
        
        methodName = XmlStreamWriterMethodType.WRITE_CHARACTERS.getMethodName();
        methodType = XmlStreamWriterMethodType.WRITE_CHARACTERS;
        method = XMLStreamWriter.class.getMethod(methodName, String.class);
        args = new Object[] {"test"};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodType, result.getMethodType());
        method = XMLStreamWriter.class.getMethod(methodName, char[].class, int.class, int.class);
        args = new Object[] {new char[] {'t', 'e', 's', 't'}, 0, 4};
        result = Invocation.createInvocation(method, args);
        assertEquals(methodType, result.getMethodType());
        
        methodName = "flush";
        methodType = XmlStreamWriterMethodType.FLUSH;
        method = XMLStreamWriter.class.getMethod(methodName);
        args = null;
        result = Invocation.createInvocation(method, args);
        assertEquals(methodType, result.getMethodType());
    }
    
    /**
     * Test of getArgument method, of class com.sun.xml.ws.policy.jaxws.xmlstreamwriter.Invocation.
     */
    public void testGetArgument() throws Exception {
        String methodName;
        Method method;
        Object[] args;
        Invocation result;
        
        methodName = XmlStreamWriterMethodType.WRITE_START_ELEMENT.getMethodName();
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class, String.class);
        args = new Object[] {"aaa", "bbb", "ccc"};
        result = Invocation.createInvocation(method, args);
        for (int i = 0; i < args.length; i++) {
            assertEquals(args[i], result.getArgument(i));
        }
        
        methodName = "flush";
        method = XMLStreamWriter.class.getMethod(methodName);
        args = null;
        result = Invocation.createInvocation(method, args);
        try {
            result.getArgument(1);
            fail("ArrayIndexOutOfBoundsException expected.");
        } catch (ArrayIndexOutOfBoundsException e) {
            // ok
        }
    }
    
    /**
     * Test of getArgumentsLength method, of class com.sun.xml.ws.policy.jaxws.xmlstreamwriter.Invocation.
     */
    public void testGetArgumentsLength() throws Exception {
        String methodName;
        Method method;
        Object[] args;
        Invocation result;
        
        methodName = XmlStreamWriterMethodType.WRITE_START_ELEMENT.getMethodName();
        method = XMLStreamWriter.class.getMethod(methodName, String.class, String.class, String.class);
        args = new Object[] {"aaa", "bbb", "ccc"};
        result = Invocation.createInvocation(method, args);        
        assertEquals(args.length, result.getArgumentsCount());
        
        methodName = "flush";
        method = XMLStreamWriter.class.getMethod(methodName);
        args = null;
        result = Invocation.createInvocation(method, args);        
        assertEquals(0, result.getArgumentsCount());
    }
    
    /**
     * Tests proper behaviour of defence copying of arguments of writeCharacters() method
     */
    public void testDefenceCopyInWriteCharactersMethod() throws Exception {
        String methodName = XmlStreamWriterMethodType.WRITE_CHARACTERS.getMethodName();
        Method method = XMLStreamWriter.class.getMethod(methodName, char[].class, int.class, int.class);
        Object[] args;
        Invocation result;
        char[] originalChars, expectedChars;
        int originalStart, originalLength, expectedStart, expectedLength;
        
        originalChars = new char[] {'x', 'y', 't', 'e', 's', 't', 'z'};
        originalStart = 2;
        originalLength = 4;
        
        expectedChars = new char[] {'t', 'e', 's', 't'};
        expectedStart = 0;
        expectedLength = 4;
        
        args = new Object[] {originalChars, originalStart, originalLength};
        result = Invocation.createInvocation(method, args);
        // modifying the original char array to test defence copy
        originalChars[2] = 'w';
        
        assertEquals(expectedStart, result.getArgument(1));
        assertEquals(expectedLength, result.getArgument(2));
        char[] resultChars = (char[]) result.getArgument(0);
        for (int i = expectedStart; i < expectedLength; i++) {
            assertEquals(expectedChars[i], resultChars[i]);
        }
        
    }
}
