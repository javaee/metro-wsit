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
 * PolicyWSDLParserExtensionTest.java
 *
 * Created on February 28, 2006, 4:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.policy.jaxws;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.WSDLContext;
import java.net.URL;
import javax.xml.namespace.QName;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author japod
 */
public class PolicyWSDLParserExtensionTest extends TestCase{
    
    
    public PolicyWSDLParserExtensionTest(String testName) {
        super(testName);
    }
    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PolicyWSDLParserExtensionTest.class);
        return suite;
    }
    
    private PolicyMap getPolicyMap(WSDLModel model) {
        return model.getExtension(WSDLPolicyMapWrapper.class).getPolicyMap();
    }
    
    private PolicyMap getPolicyMap(String resourceName) throws Exception {
        URL wsdlUrl = PolicyResourceLoader.getResourceUrl(resourceName);
        WSDLContext wsdlContext = new WSDLContext(wsdlUrl,XmlUtil.createDefaultCatalogResolver());
        return wsdlContext.getWSDLModel().getExtension(WSDLPolicyMapWrapper.class).getPolicyMap();
    }
    
    
    public void testWsdlParserBasics() throws Exception {
        assertNotNull("PolicyMap can not be null",getPolicyMap("parser/testWsdlParserBasics.wsdl"));
    }
    
    public void testServiceElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemService.wsdl");
        assertNotNull(policyMap.getServiceEffectivePolicy(PolicyMap.createWsdlServiceScopeKey(
                new QName("http://example.org","DictionaryService"))));
    }
    
    public void testPortElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemPort.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemPortType-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    public void testBindingElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemBinding.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    public void testPortTypeOperationElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemPortTypeOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOperationElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemBindingOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageInElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemMessageIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemMessageOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemMessageFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","DictFault"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpInElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemPortTypeOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemPortTypeOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemPortTypeOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","DictFault"))));
    }
    
    public void testBindingOpInElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemBindingOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOpOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemBindingOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOpFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemBindingOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","DictFault"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testServiceAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrService-invalid.wsdl");
        assertNotNull(policyMap.getServiceEffectivePolicy(policyMap.createWsdlServiceScopeKey(
                new QName("http://example.org","DictionaryService"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPort-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    public void testPortTypeAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPortType.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrBinding-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOperationAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPortTypeOperation-invalid.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingOperationAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrBindingOperation-invalid.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testMessageInAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrMessageIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testMessageOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrMessageOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testMessageFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrMessageFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","DictFault"))));
    }
    
    public void testPortTypeOpInAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPortTypeOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testPortTypeOpOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPortTypeOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testPortTypeOpFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPortTypeOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","DictFault"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingOpInAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrBindingOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingOpOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrBindingOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingOpFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrBindingOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","DictFault"))));
    }
    
    public void testServiceHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocService.wsdl");
        assertNotNull(policyMap.getServiceEffectivePolicy(policyMap.createWsdlServiceScopeKey(
                new QName("http://example.org","DictionaryService"))));
    }
    
    public void testPortHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPort.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPortType-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    public void testBindingHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocBinding.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    public void testPortTypeOperationHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPortTypeOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOperationHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocBindingOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageInHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocMessageIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocMessageOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocMessageFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","DictFault"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpInHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPortTypeOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPortTypeOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPortTypeOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","DictFault"))));
    }
    
    public void testBindingOpInHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocBindingOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOpOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocBindingOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOpFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocBindingOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","DictFault"))));
    }

    public void testBindingOpFaultExternalPolicyAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtExternalBindingOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","DictFault"))));
    }
    
}
