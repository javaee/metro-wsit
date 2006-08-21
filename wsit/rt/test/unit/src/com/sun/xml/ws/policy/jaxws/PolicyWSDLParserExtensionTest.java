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
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.WSDLContext;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
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
    
    private PolicyMap getPolicyMap(String filename) throws Exception {
        URL wsdlUrl = (new File(filename)).toURL();
        WSDLContext wsdlContext = new WSDLContext(wsdlUrl,XmlUtil.createDefaultCatalogResolver());
        return wsdlContext.getWSDLModel().getExtension(WSDLPolicyMapWrapper.class).getPolicyMap();
    }
    
    
    public void testWsdlParserBasics() throws Exception {
        assertNotNull("PolicyMap can not be null",getPolicyMap("build/test/unit/data/testWsdlParserBasics.wsdl"));
    }
    
    public void testServiceElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemService.wsdl");
        assertNotNull(policyMap.getServiceEffectivePolicy(PolicyMap.createWsdlServiceScopeKey(
                new QName("http://any.net","DictionaryService"))));
    }
    
    public void testPortElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemPort.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemPortType-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    public void testBindingElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemBinding.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    public void testPortTypeOperationElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemPortTypeOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOperationElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemBindingOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageInElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemMessageIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemMessageOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemMessageFault.wsdl");
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
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemPortTypeOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemPortTypeOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemPortTypeOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
    public void testBindingOpInElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemBindingOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOpOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemBindingOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOpFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtElemBindingOpFault.wsdl");
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
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrService-invalid.wsdl");
        assertNotNull(policyMap.getServiceEffectivePolicy(policyMap.createWsdlServiceScopeKey(
                new QName("http://any.net","DictionaryService"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrPort-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    public void testPortTypeAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrPortType.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrBinding-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOperationAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrPortTypeOperation-invalid.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingOperationAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrBindingOperation-invalid.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testMessageInAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrMessageIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testMessageOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrMessageOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testMessageFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrMessageFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
    public void testPortTypeOpInAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrPortTypeOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testPortTypeOpOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrPortTypeOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testPortTypeOpFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrPortTypeOpFault.wsdl");
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
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrBindingOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingOpOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrBindingOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testBindingOpFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtAttrBindingOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
            
    public void testServiceHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocService.wsdl");
        assertNotNull(policyMap.getServiceEffectivePolicy(policyMap.createWsdlServiceScopeKey(
                new QName("http://any.net","DictionaryService"))));
    }
    
    public void testPortHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocPort.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocPortType-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    public void testBindingHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocBinding.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(policyMap.createWsdlEndpointScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish"))));
    }
    
    public void testPortTypeOperationHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocPortTypeOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOperationHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocBindingOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(policyMap.createWsdlOperationScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageInHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocMessageIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocMessageOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testMessageFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocMessageFault.wsdl");
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
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocPortTypeOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocPortTypeOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     */
    public void testPortTypeOpFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocPortTypeOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
    
    public void testBindingOpInHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocBindingOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOpOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocBindingOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(policyMap.createWsdlMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation"))));
    }
    
    public void testBindingOpFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("build/test/unit/data/testRuntimeWSExtHeredocBindingOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(policyMap.createWsdlFaultMessageScopeKey(
                new QName("http://any.net","DictionaryService")
                ,new QName("http://any.net","CzechToEnglish")
                ,new QName("http://any.net","TranslateOperation")
                ,new QName("http://any.net","DictFault"))));
    }
            
}
