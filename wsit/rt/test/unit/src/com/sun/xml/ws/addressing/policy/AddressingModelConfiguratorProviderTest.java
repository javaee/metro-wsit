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

package com.sun.xml.ws.addressing.policy;

import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;
import junit.framework.*;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.jaxws.WSDLPolicyMapWrapper;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.AddressingFeature;

import static com.sun.xml.ws.policy.testutils.PolicyResourceLoader.getWSDLModel;


/**
 *
 * @author japod
 */
public class AddressingModelConfiguratorProviderTest extends TestCase {
    
    
    /**
     * Test of configure method, of class com.sun.xml.ws.policy.jaxws.addressing.AddressingModelConfiguratorProvider.
     * policy assertion present
     */
    public void testConfigureW3CAddressingAssertionPresent() throws Exception {
        WSDLModel model = getWSDLModel("jaxws-spi/testModelConfigProviderAddrW3C.wsdl");
        PolicyMap policyMap = model.getExtension(WSDLPolicyMapWrapper.class).getPolicyMap();
        
        assertTrue(model.getService(new QName("http://example.org","DictionaryService")).
                getFirstPort().getFeature(AddressingFeature.class).isEnabled());
    }
    
    /**
     * Test of configure method, of class com.sun.xml.ws.policy.jaxws.addressing.AddressingModelConfiguratorProvider.
     * policy assertion present
     */
    public void testConfigureMEMBERAddressingAssertionPresent() throws Exception {
        WSDLModel model = getWSDLModel("jaxws-spi/testModelConfigProviderAddrMEMBER.wsdl");
        PolicyMap policyMap = model.getExtension(WSDLPolicyMapWrapper.class).getPolicyMap();
        
        assertTrue(model.getService(new QName("http://example.org","DictionaryService")).
                getFirstPort().getFeature(MemberSubmissionAddressingFeature.class).isEnabled());
    }

    /**
     * Test of configure method, of class com.sun.xml.ws.policy.jaxws.addressing.AddressingModelConfiguratorProvider.
     * policy assertion not present
     */
    public void testConfigureAddressingAssertionNotPresent() throws Exception {
        WSDLModel model = getWSDLModel("jaxws-spi/testModelConfigProviderAddrPolicyNotPresent.wsdl");
        PolicyMap policyMap = model.getExtension(WSDLPolicyMapWrapper.class).getPolicyMap();
        
        assertNull(model.getService(new QName("http://example.org","DictionaryService")).
                getFirstPort().getFeature(AddressingFeature.class));
    }
    
}
