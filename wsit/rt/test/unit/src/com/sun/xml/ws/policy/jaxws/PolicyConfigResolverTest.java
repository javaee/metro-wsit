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

package com.sun.xml.ws.policy.jaxws;

import junit.framework.TestCase;
import com.sun.xml.ws.wsdl.parser.XMLEntityResolver;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 */
public class PolicyConfigResolverTest extends TestCase {
    
    public PolicyConfigResolverTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of resolveEntity method, of class com.sun.xml.ws.policy.jaxws.PolicyConfigResolver.
     */
    public void testResolveEntity() throws Exception {
        String publicId = null;
        String systemId = null;
        PolicyConfigResolver resolver = new PolicyConfigResolver();
        
        try {
            XMLEntityResolver.Parser result = resolver.resolveEntity(publicId, systemId);
            fail("Expected MalformedURLException");
        } catch (MalformedURLException e) {
        }
        
        publicId = "someString";
        systemId = null;
        XMLEntityResolver.Parser result = null;

        try {
            result = resolver.resolveEntity(publicId, systemId);
            fail("Expected MalformedURLException");
        } catch (MalformedURLException e) {
        }

        publicId = null;
        systemId = "http://test.invalid/";
        
        try {
            result = resolver.resolveEntity(publicId, systemId);
            fail("Expected IOException after connect to non-existent HTTP URL");
        } catch (IOException e) {
        }
        
        publicId = "http://example.org/";
        systemId = "http://test.invalid/";
        
        try {
            result = resolver.resolveEntity(publicId, systemId);
            fail("Expected IOException after connect to non-existent HTTP URL");
        } catch (IOException e) {
        }
    }
    
}
