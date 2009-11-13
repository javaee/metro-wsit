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

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.Reader;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar
 */
public class XmlPolicyModelUnmarshallerTest extends TestCase {
    private static final PolicyModelUnmarshaller xmlUnmarshaller = PolicyModelUnmarshaller.getXmlUnmarshaller();
    
    public XmlPolicyModelUnmarshallerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testUnmarshallSingleSimplePolicyModel() throws Exception {
        unmarshalModel("single_alternative_policy/policy5.xml");
    }
    
    public void testUnmarshallSingleComplexPolicyModel() throws Exception {
        unmarshalModel("complex_policy/nested_assertions_with_alternatives.xml");
    }
    
    public void testUnmarshallComplexPolicyModelWithAssertionParameters() throws Exception {
        unmarshalModel("complex_policy/assertion_parameters1.xml");
    }
    
    public void testUnmarshallComplexPolicyModelWithAssertionParametersWithValues() throws Exception {
        unmarshalModel("bug_reproduction/assertion_parameter_value_unmarshalling.xml");
    }
    
    public void testUnmarshallPolicyModelWithPolicyReference() throws Exception {
        unmarshalModel("bug_reproduction/policy_reference1.xml");
    }
    
    public void testUnmarshallPolicyModelWithXmlId() throws Exception {
        PolicySourceModel model = unmarshalModel("complex_policy/policy_with_xmlid.xml");
        assertEquals("Unmarshalled xml:id is not the same as expected", "testXmlId", model.getPolicyId());
    }
    
    public void testUnmarshallPolicyModelWithWsuId() throws Exception {
        PolicySourceModel model = unmarshalModel("complex_policy/policy_with_wsuid.xml");
        assertEquals("Unmarshalled wsu:Id is not the same as expected", "testWsuId", model.getPolicyId());
    }
    
    public void testUnmarshallPolicyModelWithXmlIdAndWsuId() throws Exception {
        try {
            unmarshalModel("complex_policy/policy_with_xmlid_and_wsuid.xml");
            fail("Should throw an exception");
        } catch (PolicyException ignored) {
            // ok.
        } catch (Exception e) {
            fail("Should throw PolicyException instead: " + e);
        }
    }
    
    public void testUnmarshallModelWithProperPolicyNamespaceVersion() throws Exception {
        PolicySourceModel model = unmarshalModel("namespaces/policy-v1.2.xml");
        assertEquals("Unmarshalled policy namespace version does not match with original.", NamespaceVersion.v1_2, model.getNamespaceVersion());

        model = unmarshalModel("namespaces/policy-v1.5.xml");
        assertEquals("Unmarshalled policy namespace version does not match with original.", NamespaceVersion.v1_5, model.getNamespaceVersion());
    }
    
    /**
     * Testcase for https://wsit.dev.java.net/issues/show_bug.cgi?id=1305
     * @throws Exception
     */
    public void testPolicyAssertionOptionalFalse() throws Exception {
        PolicySourceModel model = unmarshalModel("bug_reproduction/assertion_optional_false_unmarshalling.xml");
        assertFalse(model.getRootNode().getChildren().iterator().next().getNodeData().isOptionalAttributeSet());
    }

    private PolicySourceModel unmarshalModel(String resource) throws Exception {
        Reader reader = PolicyResourceLoader.getResourceReader(resource);
        PolicySourceModel model = xmlUnmarshaller.unmarshalModel(reader);
        reader.close();
        return model;
    }
}
