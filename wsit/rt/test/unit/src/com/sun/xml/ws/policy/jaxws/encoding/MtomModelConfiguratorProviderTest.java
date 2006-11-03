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

package com.sun.xml.ws.policy.jaxws.encoding;

import junit.framework.*;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.jaxws.WSDLPolicyMapWrapper;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.MTOMFeature;

import static com.sun.xml.ws.policy.testutils.PolicyResourceLoader.getWSDLModel;
import javax.xml.ws.WebServiceFeature;


/**
 *
 * @author japod
 */
public class MtomModelConfiguratorProviderTest extends TestCase {
    
    
    /**
     * Test of configure method, of class com.sun.xml.ws.policy.jaxws.encoding.MtomModelConfiguratorProvider.
     * policy assertion present
     */
    public void testConfigureMtomAssertionPresent() throws Exception {
        WSDLModel model = getWSDLModel("jaxws-spi/testModelConfigProviderMtom.wsdl");
        PolicyMap policyMap = model.getExtension(WSDLPolicyMapWrapper.class).getPolicyMap();

        WSDLService service = model.getService(new QName("http://example.org","DictionaryService"));
        assertNotNull(service);
        
        WSDLPort port = service.getFirstPort();
        assertNotNull(port);
        
        WebServiceFeature feature = port.getFeature(MTOMFeature.class);
        assertNotNull(feature);
        
        assertTrue(feature.isEnabled());
    }
    
    /**
     * Test of configure method, of class com.sun.xml.ws.policy.jaxws.encoding.MtomModelConfiguratorProvider.
     * policy assertion not present
     */
    public void testConfigureMtomAssertionNotPresent() throws Exception {
        WSDLModel model = getWSDLModel("jaxws-spi/testModelConfigProviderMtomPolicyNotPresent.wsdl");
        PolicyMap policyMap = model.getExtension(WSDLPolicyMapWrapper.class).getPolicyMap();
        
        WSDLService service = model.getService(new QName("http://example.org","DictionaryService"));
        assertNotNull(service);
        
        WSDLPort port = service.getFirstPort();
        assertNotNull(port);
        
        WebServiceFeature feature = port.getFeature(MTOMFeature.class);
        assertNull(feature);
    }
    
}
