/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.policy.parser;

import com.sun.xml.ws.policy.parser.PolicyConfigParser;
import static com.sun.xml.ws.policy.testutils.PolicyResourceLoader.getPolicyMap;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.net.URL;
import java.util.Collection;
import javax.xml.stream.XMLInputFactory;
import junit.framework.TestCase;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 * @author Fabian Ritzmann
 */
public class PolicyWSDLParserExtensionTest extends TestCase{
    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();
    
    public PolicyWSDLParserExtensionTest(String testName) {
        super(testName);
    }
    
    public void testClientParsingWithDifferentlyCreatedSDDocumentSource() throws Exception {
        final URL configFileUrl = PolicyResourceLoader.getResourceUrl("parser/wsit-client.xml");
        WSDLModel model = com.sun.xml.ws.policy.parser.PolicyResourceLoader.getWsdlModel(configFileUrl, true);
        assertNotNull(model);
    }
    
    public void testWsdlParserBasics() throws Exception {
        assertNotNull("PolicyMap can not be null", getPolicyMap("parser/testWsdlParserBasics.wsdl"));
    }
    
    public void testPolicyReferences() throws Exception {
        PolicyMap map = getPolicyMap("parser/testPolicyReferences.wsdl");
        assertNotNull("PolicyMap can not be null", map);
        
        map = PolicyConfigParser.parse(PolicyResourceLoader.getResourceUrl("parser/testPolicyReferences.wsdl"), true);
        assertNotNull("PolicyMap can not be null", map);
    }
    
    public void testWsdlParserImport() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testWsdlImportMain.wsdl");
        Policy policy;
        assertNotNull("PolicyMap can not be null", policyMap);
        assertNotNull(policy = policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
        assertTrue(policy.contains(new QName("http://example.org","dummyAssertion")));
        
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
     * @throws Exception
     */
    public void testPortTypeElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemPortType-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    public void testBindingElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemBinding.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    public void testPortTypeOperationElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemPortTypeOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(PolicyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOperationElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemBindingOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(PolicyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageInElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemMessageIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemMessageOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemMessageFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","Fault"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testPortTypeOpInElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemPortTypeOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testPortTypeOpOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemPortTypeOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testPortTypeOpFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemPortTypeOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","Fault"))));
    }
    
    public void testBindingOpInElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemBindingOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOpOutElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemBindingOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOpFaultElementAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtElemBindingOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","Fault"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testServiceAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrService-invalid.wsdl");
        assertNotNull(policyMap.getServiceEffectivePolicy(PolicyMap.createWsdlServiceScopeKey(
                new QName("http://example.org","DictionaryService"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testPortAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPort-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    public void testPortTypeAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPortType.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testBindingAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrBinding-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testPortTypeOperationAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPortTypeOperation-invalid.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(PolicyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testBindingOperationAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrBindingOperation-invalid.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(PolicyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testMessageInAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrMessageIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testMessageOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrMessageOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testMessageFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrMessageFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","Fault"))));
    }
    
    public void testPortTypeOpInAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPortTypeOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testPortTypeOpOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPortTypeOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testPortTypeOpFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrPortTypeOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","Fault"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testBindingOpInAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrBindingOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testBindingOpOutAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrBindingOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testBindingOpFaultAttrAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtAttrBindingOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","Fault"))));
    }
    
    public void testServiceHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocService.wsdl");
        assertNotNull(policyMap.getServiceEffectivePolicy(PolicyMap.createWsdlServiceScopeKey(
                new QName("http://example.org","DictionaryService"))));
    }
    
    public void testPortHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPort.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testPortTypeHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPortType-invalid.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    public void testBindingHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocBinding.wsdl");
        assertNotNull(policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish"))));
    }
    
    public void testPortTypeOperationHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPortTypeOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(PolicyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOperationHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocBindingOperation.wsdl");
        assertNotNull(policyMap.getOperationEffectivePolicy(PolicyMap.createWsdlOperationScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageInHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocMessageIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocMessageOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testMessageFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocMessageFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","Fault"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testPortTypeOpInHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPortTypeOpIn-invalid.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testPortTypeOpOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPortTypeOpOut-invalid.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    /**
     * invalid wsdl on input
     * @throws Exception
     */
    public void testPortTypeOpFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocPortTypeOpFault-invalid.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","Fault"))));
    }
    
    public void testBindingOpInHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocBindingOpIn.wsdl");
        assertNotNull(policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOpOutHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocBindingOpOut.wsdl");
        assertNotNull(policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation"))));
    }
    
    public void testBindingOpFaultHeredocAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtHeredocBindingOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","Fault"))));
    }
    
    public void testBindingOpFaultExternalPolicyAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtExternalBindingOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","Fault"))));
    }
    
    public void testBindingOpFaultExternalFromAnonymousPolicyAttachment() throws Exception {
        PolicyMap policyMap = getPolicyMap("parser/testRuntimeWSExtExternalFromAnonBindingOpFault.wsdl");
        assertNotNull(policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://example.org","DictionaryService")
                ,new QName("http://example.org","CzechToEnglish")
                ,new QName("http://example.org","TranslateOperation")
                ,new QName("http://example.org","Fault"))));
    }

    public void testInvalidAssertionShouldCauseException() throws Exception {
        try {
            PolicyMap policyMap = getPolicyMap("parser/testInvalidAssertionError.wsdl", false);
            fail("WSDL validation should fail");
        } catch (WebServiceException e) {
            // ok - exception thrown as expected
        }
    }
    
    public void testCircularReference() throws Exception {
        try {
            getPolicyMap("parser/testPolicyCircularReferences.wsdl", false);
            fail("Parsing WSDL containing circular policy references should fail");
        } catch (WebServiceException e) {
            // ok - exception thrown as expected
        }
    }

    public void testComprehensive() throws PolicyException {
        PolicyMap policyMap = getPolicyMap("parser/testComprehensive.wsdl");
        
        // Test service scope
        
        Collection<PolicyMapKey> keys = policyMap.getAllServiceScopeKeys();
        assertEquals(1, keys.size());
        
        Policy policy = policyMap.getServiceEffectivePolicy(PolicyMap.createWsdlServiceScopeKey(
                new QName("http://wsit.test/","FaultServiceService")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        AssertionSet assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "ServiceMarker")));
        
        // Test endpoint scope

        keys = policyMap.getAllEndpointScopeKeys();
        assertEquals(1, keys.size());
        
        policy = policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://wsit.test/","FaultServiceService"),
                new QName("http://wsit.test/","FaultServicePort")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingMarker")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "PortMarker")));
        
        // Test operation scope

        keys = policyMap.getAllOperationScopeKeys();
        assertEquals(3, keys.size());
        
        policy = policyMap.getOperationEffectivePolicy(PolicyMap.createWsdlOperationScopeKey(
                new QName("http://wsit.test/","FaultServiceService"),
                new QName("http://wsit.test/","FaultServicePort"),
                new QName("http://wsit.test/","echo")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingOperationEcho")));
        
        policy = policyMap.getOperationEffectivePolicy(PolicyMap.createWsdlOperationScopeKey(
                new QName("http://wsit.test/","FaultServiceService"),
                new QName("http://wsit.test/","FaultServicePort"),
                new QName("http://wsit.test/","hello")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingOperationHello")));
        
        policy = policyMap.getOperationEffectivePolicy(PolicyMap.createWsdlOperationScopeKey(
                new QName("http://wsit.test/","FaultServiceService"),
                new QName("http://wsit.test/","FaultServicePort"),
                new QName("http://wsit.test/","ping")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingOperationPing")));

        // Test input message scope

        keys = policyMap.getAllInputMessageScopeKeys();
        assertEquals(3, keys.size());
        
        policy = policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://wsit.test/","FaultServiceService"),
                new QName("http://wsit.test/","FaultServicePort"),
                new QName("http://wsit.test/","echo")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "MessageEcho")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingEchoInput")));
        
        policy = policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://wsit.test/","FaultServiceService"),
                new QName("http://wsit.test/","FaultServicePort"),
                new QName("http://wsit.test/","hello")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "MessageHello")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingHelloInput")));
        
        policy = policyMap.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://wsit.test/","FaultServiceService"),
                new QName("http://wsit.test/","FaultServicePort"),
                new QName("http://wsit.test/","ping")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "MessagePing")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingPingInput")));
        
        // Test output message scope

        keys = policyMap.getAllOutputMessageScopeKeys();
        assertEquals(3, keys.size());
        
        policy = policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://wsit.test/","FaultServiceService"),
                new QName("http://wsit.test/","FaultServicePort"),
                new QName("http://wsit.test/","echo")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "MessageEchoResponse")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingEchoOutput")));
        
        policy = policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://wsit.test/","FaultServiceService"),
                new QName("http://wsit.test/","FaultServicePort"),
                new QName("http://wsit.test/","hello")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "MessageHelloResponse")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingHelloOutput")));
        
        policy = policyMap.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("http://wsit.test/","FaultServiceService"),
                new QName("http://wsit.test/","FaultServicePort"),
                new QName("http://wsit.test/","ping")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingPingOutput")));
        
        // Test fault message scope

        keys = policyMap.getAllFaultMessageScopeKeys();
        assertEquals(6, keys.size());
        
        policy = policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://wsit.test/", "FaultServiceService"),
                new QName("http://wsit.test/", "FaultServicePort"),
                new QName("http://wsit.test/", "echo"),
                new QName("http://wsit.test/", "EchoException")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingEchoException")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "EchoException")));
        
        policy = policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://wsit.test/", "FaultServiceService"),
                new QName("http://wsit.test/", "FaultServicePort"),
                new QName("http://wsit.test/", "echo"),
                new QName("http://wsit.test/", "Echo2Exception")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingEcho2Exception")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "Echo2Exception")));
        
        policy = policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://wsit.test/", "FaultServiceService"),
                new QName("http://wsit.test/", "FaultServicePort"),
                new QName("http://wsit.test/", "hello"),
                new QName("http://wsit.test/", "HelloException")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingHelloException")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "HelloException")));
        
        policy = policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://wsit.test/", "FaultServiceService"),
                new QName("http://wsit.test/", "FaultServicePort"),
                new QName("http://wsit.test/", "hello"),
                new QName("http://wsit.test/", "Hello2Exception")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingHello2Exception")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "Hello2Exception")));
        
        policy = policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://wsit.test/", "FaultServiceService"),
                new QName("http://wsit.test/", "FaultServicePort"),
                new QName("http://wsit.test/", "ping"),
                new QName("http://wsit.test/", "EchoException")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingPingException")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingEchoException")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingEcho2Exception")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "EchoException")));
        assertFalse(assertionSet.contains(new QName("http://wsit.test/", "Echo2Exception")));
        
        policy = policyMap.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("http://wsit.test/", "FaultServiceService"),
                new QName("http://wsit.test/", "FaultServicePort"),
                new QName("http://wsit.test/", "ping"),
                new QName("http://wsit.test/", "Echo2Exception")));
        assertEquals(1, policy.getNumberOfAssertionSets());
        assertionSet = policy.iterator().next();
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "BindingPing2Exception")));
        assertFalse(assertionSet.contains(new QName("http://wsit.test/", "EchoException")));
        assertTrue(assertionSet.contains(new QName("http://wsit.test/", "Echo2Exception")));
        
    }
    
    public void testNamespaceImport() throws PolicyException {
        PolicyMap map = getPolicyMap("parser/testNamespaceImport.wsdl", false);

        Policy policy = map.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("STSUserAuth_svc_app", "casaService1"),
                new QName("STSUserAuth_svc_app", "SecuredEchoPort")));
        assertEquals("casaBinding1Policy", policy.getId());
        
        policy = map.getOperationEffectivePolicy(PolicyMap.createWsdlOperationScopeKey(
                new QName("STSUserAuth_svc_app", "casaService1"),
                new QName("STSUserAuth_svc_app", "SecuredEchoPort"),
                new QName("STSUserAuth_svc_app", "EchoServiceOperation")));
        assertEquals("casaBinding1_operation_Policy", policy.getId());
        
        policy = map.getInputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("STSUserAuth_svc_app", "casaService1"),
                new QName("STSUserAuth_svc_app", "SecuredEchoPort"),
                new QName("STSUserAuth_svc_app", "EchoServiceOperation")));
        assertEquals("casaBinding1_input1_Policy", policy.getId());

        policy = map.getOutputMessageEffectivePolicy(PolicyMap.createWsdlMessageScopeKey(
                new QName("STSUserAuth_svc_app", "casaService1"),
                new QName("STSUserAuth_svc_app", "SecuredEchoPort"),
                new QName("STSUserAuth_svc_app", "EchoServiceOperation")));
        assertEquals("casaBinding1_output1_Policy", policy.getId());

        policy = map.getFaultMessageEffectivePolicy(PolicyMap.createWsdlFaultMessageScopeKey(
                new QName("STSUserAuth_svc_app", "casaService1"),
                new QName("STSUserAuth_svc_app", "SecuredEchoPort"),
                new QName("STSUserAuth_svc_app", "EchoServiceOperation"),
                new QName("STSUserAuth_svc_app", "fault1")));
        assertEquals("casaBinding1_fault1_Policy", policy.getId());
    }

    public void testPolicyMapToString() throws Exception {
        PolicyMap policyMap = getPolicyMap("bug_reproduction/simple.wsdl");
        String result = policyMap.toString();
        assertNotNull(result);
    }
    
    public void testDuplicateId() throws PolicyException {
        try {
            getPolicyMap("duplicateid/duplicate.wsdl", true);
            fail("Read WSDL with two policies that have the same ID. This should have triggered a PolicyException.");
        } catch (WebServiceException e) {
            // This test is supposed to trigger an exception
        }
    }

    public void testDuplicateImport() throws PolicyException {
        final PolicyMap map = getPolicyMap("duplicateid/importer.wsdl", true);

        final Policy policy1 = map.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org", "DictionaryService"),
                new QName("http://example.org", "CzechToEnglish")));
        assertEquals("Policy1", policy1.getId());
        assertTrue(policy1.contains(new QName("http://example.org", "Assertion1")));
        assertFalse(policy1.contains(new QName("http://example.org", "Assertion2")));
        
        final Policy policy2 = map.getOperationEffectivePolicy(PolicyMap.createWsdlOperationScopeKey(
                new QName("http://example.org", "DictionaryService"),
                new QName("http://example.org", "CzechToEnglish"),
                new QName("http://example.org", "TranslateOperation")));
        assertEquals("Policy1", policy2.getId());
        assertTrue(policy2.contains(new QName("http://example.org", "Assertion2")));
        assertFalse(policy2.contains(new QName("http://example.org", "Assertion1")));
    }

    public void testDuplicateImport2() throws PolicyException {
        final PolicyMap map = getPolicyMap("duplicateid/importer2.wsdl", true);

        final Policy policy = map.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(
                new QName("http://example.org", "DictionaryService"),
                new QName("http://example.org", "CzechToEnglish")));
        assertTrue(policy.contains(new QName("http://example.org", "Assertion1")));
        assertTrue(policy.contains(new QName("http://example.org", "Assertion2")));
    }

}
