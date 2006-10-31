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

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

/**
 *
 */
public class PolicyConfigParserTest extends TestCase {
    
    public PolicyConfigParserTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testParseContainerNullWithoutConfig() throws Exception {
        Container container = null;
        
        PolicyMap result = null;
        
        result = PolicyConfigParser.parse(null, container);
        assertNull(result);
    }
    
    public void testParseContainerNullWithConfig() throws Exception {
        Container container = null;
        
        PolicyMap map = null;
        
        try {
            copyFile("test/unit/data/policy/config/wsit.xml", "test/unit/data/wsit.xml");
            map = PolicyConfigParser.parse(null, container);
        } finally {
            File wsitxml = new File("test/unit/data/wsit.xml");
            wsitxml.delete();
        }
        PolicyMapKey key = map.createWsdlEndpointScopeKey(new QName("http://example.org/", "AddNumbersService"), new QName("http://example.org/", "AddNumbersPort"));
        Policy policy = map.getEndpointEffectivePolicy(key);
        assertNotNull(policy);
        assertEquals("MutualCertificate10Sign_IPingService_policy", policy.getId());
    }
    
    public void testParseContainerWithoutContextWithoutConfig() throws Exception {
        Container container = new MockContainer(null);
        
        PolicyMap result = null;
        
        result = PolicyConfigParser.parse(null, container);
        assertNull(result);
    }
    
    public void testParseContainerWithoutContext() throws Exception {
        Container container = new MockContainer(null);
        
        PolicyMap map = null;
        
        try {
            copyFile("test/unit/data/policy/config/wsit.xml", "test/unit/data/wsit.xml");
            map = PolicyConfigParser.parse(null, container);
        } finally {
            File wsitxml = new File("test/unit/data/wsit.xml");
            wsitxml.delete();
        }
        PolicyMapKey key = map.createWsdlEndpointScopeKey(new QName("http://example.org/", "AddNumbersService"), new QName("http://example.org/", "AddNumbersPort"));
        Policy policy = map.getEndpointEffectivePolicy(key);
        assertNotNull(policy);
        assertEquals("MutualCertificate10Sign_IPingService_policy", policy.getId());
    }
    
    public void testParseContainerWithContext() throws Exception {
        // TODO Need MockServletContext
    }
    
    /**
     * Test of parse method, of class com.sun.xml.ws.policy.jaxws.PolicyConfigParser.
     */
    public void testParseURLNull() throws Exception {
        PolicyMap result = null;
        
        try {
            result = PolicyConfigParser.parse((URL) null, false);
            fail("Expected PolicyException");
        } catch (PolicyException e) {
        }
        assertNull(result);
    }
    
    public void testParseBufferMex() throws Exception {
        URL url = PolicyUtils.ConfigFile.loadAsResource("policy/mex/mex.xml", null);
        PolicyMap map = PolicyConfigParser.parse(url, false);
        PolicyMapKey key = map.createWsdlEndpointScopeKey(new QName("http://schemas.xmlsoap.org/ws/2004/09/mex", "MetadataExchangeService"), new QName("http://schemas.xmlsoap.org/ws/2004/09/mex", "MetadataExchangePort"));
        Policy policy = map.getEndpointEffectivePolicy(key);
        assertNotNull(policy);
        assertEquals("MEXPolicy", policy.getId());
    }

    
    public void testParseBufferSimple() throws Exception {
        PolicyMap map = parseConfigFile("config/simple.wsdl");
        PolicyMapKey key = map.createWsdlEndpointScopeKey(new QName("http://example.org/", "AddNumbersService"), new QName("http://example.org/", "AddNumbersPort"));
        Policy policy = map.getEndpointEffectivePolicy(key);
        assertNotNull(policy);
        assertEquals("MutualCertificate10Sign_IPingService_policy", policy.getId());
    }
    
    public void testParseBufferSingleImport() throws Exception {
        WSDLModel result = null;
        
        PolicyMap map = parseConfigFile("config/single-import.wsdl");
        assertNotNull(map);
        
        PolicyMapKey key1 = map.createWsdlEndpointScopeKey(new QName("http://example.org/", "AddNumbersService"),
                new QName("http://example.org/", "AddNumbersPort"));
        Policy policy1 = map.getEndpointEffectivePolicy(key1);
        assertNotNull(policy1);
        assertEquals("MutualCertificate10Sign_IPingService_policy", policy1.getId());
        
        PolicyMapKey key2 = map.createWsdlEndpointScopeKey(new QName("http://example.net/", "AddNumbersService"),
                new QName("http://example.net/", "AddNumbersPort"));
        Policy policy2 = map.getEndpointEffectivePolicy(key2);
        assertNotNull(policy2);
        assertEquals("MutualCertificate10Sign_IPingService_policy", policy2.getId());
    }
    
    public void testParseBufferMultiImport() throws Exception {
        PolicyMap map = parseConfigFile("config/import.wsdl");
        
        assertNotNull(map);
        
        PolicyMapKey key1 = map.createWsdlEndpointScopeKey(new QName("http://example.org/", "AddNumbersService"),
                new QName("http://example.org/", "AddNumbersPort"));
        Policy policy1 = map.getEndpointEffectivePolicy(key1);
        assertNotNull(policy1);
        assertEquals("MutualCertificate10Sign_IPingService_policy", policy1.getId());
        
        PolicyMapKey key2 = map.createWsdlEndpointScopeKey(new QName("http://example.net/", "AddNumbersService"),
                new QName("http://example.net/", "AddNumbersPort"));
        Policy policy2 = map.getEndpointEffectivePolicy(key2);
        assertNotNull(policy2);
        assertEquals("MutualCertificate10Sign_IPingService_policy", policy2.getId());
        
        PolicyMapKey key3 = map.createWsdlEndpointScopeKey(new QName("http://example.com/", "AddNumbersService"),
                new QName("http://example.com/", "AddNumbersPort"));
        Policy policy3 = map.getEndpointEffectivePolicy(key3);
        assertNotNull(policy3);
        assertEquals("MutualCertificate10Sign_IPingService_policy", policy3.getId());
        
        PolicyMapKey key4 = map.createWsdlEndpointScopeKey(new QName("http://example.com/import3/", "AddNumbersService"),
                new QName("http://example.com/import3/", "AddNumbersPort"));
        Policy policy4 = map.getEndpointEffectivePolicy(key4);
        assertNotNull(policy4);
        assertEquals("MutualCertificate10Sign_IPingService_policy", policy4.getId());
    }
    
    public void testParseBufferCyclicImport() throws Exception {
        PolicyMap map = parseConfigFile("config/cyclic.wsdl");
        PolicyMapKey key = map.createWsdlEndpointScopeKey(new QName("http://example.org/", "AddNumbersService"), new QName("http://example.org/", "AddNumbersPort"));
        Policy policy = map.getEndpointEffectivePolicy(key);
        assertNotNull(policy);
        assertEquals("MutualCertificate10Sign_IPingService_policy", policy.getId());
    }
    
    public void testParseBufferExternalReference() throws Exception {
        PolicyMap map = parseConfigFile("config/service.wsdl");
        PolicyMapKey key = map.createWsdlEndpointScopeKey(new QName("http://example.org/AddNumbers/service", "AddNumbersService"), new QName("http://example.org/AddNumbers/service", "AddNumbersPort"));
        Policy policy = map.getEndpointEffectivePolicy(key);
        assertNotNull(policy);
        assertEquals("AddNumbersServicePolicy", policy.getId());
    }
    
    public void testParseBufferExternalReferenceName() throws Exception {
        PolicyMap map = parseConfigFile("config/service-name.wsdl");
        PolicyMapKey key = map.createWsdlEndpointScopeKey(new QName("http://example.org/AddNumbers/service", "AddNumbersService"), new QName("http://example.org/AddNumbers/service", "AddNumbersPort"));
        Policy policy = map.getEndpointEffectivePolicy(key);
        assertNotNull(policy);
        assertEquals("http://example.org/AddNumbers/porttype#AddNumbersServicePolicy", policy.getName());
    }
        
    private PolicyMap parseConfigFile(String configFile) throws Exception {
        URL url = PolicyUtils.ConfigFile.loadAsResource(PolicyResourceLoader.POLICY_UNIT_TEST_RESOURCE_ROOT + configFile, null);
        return PolicyConfigParser.parse(url, false);
    }

    /**
     * Copy a file
     */
    private static final void copyFile(String sourceName, String destName) throws IOException {
        FileChannel source = null;
        FileChannel dest = null;
        try {
            // Create channel on the source
            source = new FileInputStream(sourceName).getChannel();
            
            // Create channel on the destination
            dest = new FileOutputStream(destName).getChannel();
            
            // Copy file contents from source to destination
            dest.transferFrom(source, 0, source.size());
            
        } finally {
            // Close the channels
            if (source != null) {
                try {
                    source.close();
                } catch (IOException e) {
                }
            }
            if (dest != null) {
                dest.close();
            }
        }
    }
    
    class MockContainer extends Container {
        private final Object spi;
        
        public <T> MockContainer(T spi) {
            this.spi = spi;
        }
        
        public <T> T getSPI(Class<T> spiType) {
            if (spiType.isInstance(this.spi)) {
                return (T) this.spi;
            } else {
                return null;
            }
        }
        
    }
}
