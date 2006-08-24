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
        assertNotNull("PolicyMap can not be null",getPolicyMap("testWsdlParserBasics.wsdl"));
    }
    
    public void testServiceElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemService.wsdl");
        assertNotNull(policyMap.getServiceEffectivePolicy(PolicyMap.createWsdlServiceScopeKey(
                new QName("http://any.net","DictionaryService"))));
    }
    
    public void testPortElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemPort.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemPortType-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    public void testBindingElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemBinding.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    public void testPortTypeOperationElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemPortTypeOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOperationElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemBindingOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageInElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemMessageIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemMessageOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemMessageFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpInElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemPortTypeOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemPortTypeOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemPortTypeOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
    public void testBindingOpInElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemBindingOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOpOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemBindingOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOpFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtElemBindingOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testServiceAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrService-invalid.wsdl");
        assertNotNull(policyMap.getServiceEffectivePolicy(policyMap.createWsdlServiceScopeKey(
                new QName("http://any.net","DictionaryService"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrPort-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    public void testPortTypeAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrPortType.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrBinding-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOperationAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrPortTypeOperation-invalid.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingOperationAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrBindingOperation-invalid.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testMessageInAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrMessageIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testMessageOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrMessageOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testMessageFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrMessageFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
    public void testPortTypeOpInAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrPortTypeOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testPortTypeOpOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrPortTypeOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testPortTypeOpFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrPortTypeOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingOpInAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrBindingOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingOpOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrBindingOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingOpFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtAttrBindingOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
    public void testServiceHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocService.wsdl");
        assertNotNull(policyMap.getServiceEffectivePolicy(policyMap.createWsdlServiceScopeKey(
                new QName("http://any.net","DictionaryService"))));
    }
    
    public void testPortHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocPort.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocPortType-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    public void testBindingHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocBinding.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    public void testPortTypeOperationHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocPortTypeOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOperationHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocBindingOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageInHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocMessageIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocMessageOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocMessageFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpInHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocPortTypeOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocPortTypeOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocPortTypeOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
    public void testBindingOpInHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocBindingOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOpOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocBindingOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOpFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("testRuntimeWSExtHeredocBindingOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
}
