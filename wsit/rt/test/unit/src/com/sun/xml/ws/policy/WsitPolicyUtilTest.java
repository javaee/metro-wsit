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

package com.sun.xml.ws.policy;

import com.sun.xml.ws.api.policy.SourceModel;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.sourcemodel.ModelNode;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelTranslator;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;

import java.util.Arrays;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;

/**
 *
 * @author Fabian Ritzmann
 */
public class WsitPolicyUtilTest extends TestCase {
    
    public WsitPolicyUtilTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of validateServerPolicyMap method, of class WsitPolicyUtil.
     *
     * @throws PolicyException
     */
    public void testValidateServerPolicyMapUnknown() throws PolicyException {
        final PolicySourceModel model = SourceModel.createPolicySourceModel(NamespaceVersion.v1_5, "id", null);
        final ModelNode root = model.getRootNode();
        final ModelNode alternatives = root.createChildExactlyOneNode();
        final ModelNode alternative1 = alternatives.createChildAllNode();
        final QName name1 = new QName("test1", "test1");
        final AssertionData assertion1 = AssertionData.createAssertionData(name1);
        alternative1.createChildAssertionNode(assertion1);
        final PolicyModelTranslator translator = PolicyModelTranslator.getTranslator();
        final Policy policy = translator.translate(model);

        final PolicyMapExtender extender = PolicyMapExtender.createPolicyMapExtender();
        final PolicyMap map = PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {extender}));

        final PolicySubject subject = new PolicySubject("dummy", policy);

        final PolicyMapKey key = PolicyMap.createWsdlServiceScopeKey(new QName("1"));
        extender.putServiceSubject(key, subject);

        try {
            WsitPolicyUtil.validateServerPolicyMap(map);
            fail("Expected WebServiceException because of an unknown policy assertion but did not get any exception at all.");
        } catch (WebServiceException e) {
            // expected
        }
    }

    /**
     * Test of validateServerPolicyMap method, of class WsitPolicyUtil.
     *
     * @throws PolicyException
     */
    public void testValidateServerPolicyMapKnown() throws PolicyException {
        final PolicySourceModel model = SourceModel.createPolicySourceModel(NamespaceVersion.v1_5, "id", null);
        final ModelNode root = model.getRootNode();
        final ModelNode alternatives = root.createChildExactlyOneNode();
        final ModelNode alternative1 = alternatives.createChildAllNode();
        final QName name1 = new QName("http://www.w3.org/2007/05/addressing/metadata", "Addressing");
        final AssertionData assertion1 = AssertionData.createAssertionData(name1);
        alternative1.createChildAssertionNode(assertion1);
        final PolicyModelTranslator translator = PolicyModelTranslator.getTranslator();
        final Policy policy = translator.translate(model);

        final PolicyMapExtender extender = PolicyMapExtender.createPolicyMapExtender();
        final PolicyMap map = PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {extender}));

        final PolicySubject subject = new PolicySubject("dummy", policy);

        final PolicyMapKey key = PolicyMap.createWsdlServiceScopeKey(new QName("1"));
        extender.putServiceSubject(key, subject);

        WsitPolicyUtil.validateServerPolicyMap(map);
    }

    /**
     * Test of doAlternativeSelection method, of class WsitPolicyUtil.
     *
     * @throws PolicyException
     */
    public void testDoAlternativeSelection() throws PolicyException {
        final PolicySourceModel model = SourceModel.createPolicySourceModel(NamespaceVersion.v1_5, "id", null);
        final ModelNode root = model.getRootNode();
        final ModelNode alternatives = root.createChildExactlyOneNode();
        final ModelNode alternative1 = alternatives.createChildAllNode();
        final ModelNode alternative2 = alternatives.createChildAllNode();
        final QName name1 = new QName("test1", "test1");
        final AssertionData assertion1 = AssertionData.createAssertionData(name1);
        alternative1.createChildAssertionNode(assertion1);
        final QName name2 = new QName("test2", "test2");
        final AssertionData assertion2 = AssertionData.createAssertionData(name2);
        alternative2.createChildAssertionNode(assertion2);
        final PolicyModelTranslator translator = PolicyModelTranslator.getTranslator();
        final Policy policy = translator.translate(model);

        final PolicyMapExtender extender = PolicyMapExtender.createPolicyMapExtender();
        final PolicyMap map = PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {extender}));
        final PolicySubject subject = new PolicySubject("dummy", policy);
        final PolicyMapKey key = PolicyMap.createWsdlServiceScopeKey(new QName("1"));
        extender.putServiceSubject(key, subject);

        final PolicyMap result = WsitPolicyUtil.doAlternativeSelection(map);
        final Policy policyResult = result.getServiceEffectivePolicy(key);
        int assertionSetCount = 0;
        for (AssertionSet set : policyResult) {
            assertionSetCount++;
        }
        assertEquals(1, assertionSetCount);
    }

    /**
     * Test of mergePolicyMap method, of class WsitPolicyUtil.
     *
     * @throws Exception
     */
    public void testMergePolicyMap() throws Exception {
        final PolicySourceModel model = SourceModel.createPolicySourceModel(NamespaceVersion.v1_5, "id", null);
        final ModelNode root = model.getRootNode();
        final ModelNode alternatives = root.createChildExactlyOneNode();
        final ModelNode alternative1 = alternatives.createChildAllNode();
        final QName name1 = new QName("test1", "test1");
        final AssertionData assertion1 = AssertionData.createAssertionData(name1);
        alternative1.createChildAssertionNode(assertion1);
        final PolicyModelTranslator translator = PolicyModelTranslator.getTranslator();
        final Policy policy = translator.translate(model);

        final PolicyMapExtender extender = PolicyMapExtender.createPolicyMapExtender();
        final PolicyMap policyMap = PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {extender}));
        final PolicySubject subject = new PolicySubject("dummy", policy);
        final PolicyMapKey key = PolicyMap.createWsdlServiceScopeKey(new QName("1"));
        extender.putServiceSubject(key, subject);

        final PolicySourceModel model2 = SourceModel.createPolicySourceModel(NamespaceVersion.v1_5, "id2", null);
        final ModelNode root2 = model2.getRootNode();
        final ModelNode alternatives2 = root2.createChildExactlyOneNode();
        final ModelNode alternative21 = alternatives2.createChildAllNode();
        final QName name2 = new QName("test2", "test2");
        final AssertionData assertion2 = AssertionData.createAssertionData(name2);
        alternative21.createChildAssertionNode(assertion2);
        final Policy policy2 = translator.translate(model2);

        final PolicyMapExtender extender2 = PolicyMapExtender.createPolicyMapExtender();
        final PolicyMap clientPolicyMap = PolicyMap.createPolicyMap(Arrays.asList(new PolicyMapMutator[] {extender2}));
        final PolicySubject subject2 = new PolicySubject("dummy2", policy2);
        final PolicyMapKey key2 = PolicyMap.createWsdlServiceScopeKey(new QName("2"));
        extender2.putServiceSubject(key2, subject2);

        final PolicyMap result = WsitPolicyUtil.mergePolicyMap(policyMap, clientPolicyMap);
        final Policy result1 = result.getServiceEffectivePolicy(key);
        final Policy result2 = result.getServiceEffectivePolicy(key2);
        assertEquals("id", result1.getId());
        assertEquals("id2", result2.getId());
    }

}
