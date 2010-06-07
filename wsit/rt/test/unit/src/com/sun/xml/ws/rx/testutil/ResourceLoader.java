/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 *  Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 * 
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License. You can obtain
 *  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 * 
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *  Sun designates this particular file as subject to the "Classpath" exception
 *  as provided by Sun in the GPL Version 2 section of the License file that
 *  accompanied this code.  If applicable, add the following below the License
 *  Header, with the fields enclosed by brackets [] replaced by your own
 *  identifying information: "Portions Copyrighted [year]
 *  [name of copyright owner]"
 * 
 *  Contributor(s):
 * 
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */
package com.sun.xml.ws.rx.testutil;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelTranslator;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelUnmarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class ResourceLoader {
    public static final String RM_1_0_DEFAULT_POLICY_RESOURCE_NAME = "rm/policy/rm-1_0-policy-default.xml";
    public static final String RM_1_0_CUSTOM_POLICY_RESOURCE_NAME = "rm/policy/rm-1_0-policy-custom.xml";

    public static final String RM_1_1_DEFAULT_POLICY_RESOURCE_NAME = "rm/policy/rm-1_1-policy-default.xml";
    public static final String RM_1_1_CUSTOM_1_POLICY_RESOURCE_NAME = "rm/policy/rm-1_1-policy-custom_1.xml";
    public static final String RM_1_1_CUSTOM_2_POLICY_RESOURCE_NAME = "rm/policy/rm-1_1-policy-custom_2.xml";
    public static final String RM_1_1_CUSTOM_3_POLICY_RESOURCE_NAME = "rm/policy/rm-1_1-policy-custom_3.xml";

    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    private ResourceLoader() {
    }

    public static URL getResourceUrl(String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResource(resourceName);
    }

    public static InputStream getResourceAsStream(String resourceName) throws PolicyException {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (input == null) {
            throw new PolicyException("Failed to find resource \"" + resourceName + "\"");
        }
        return input;
    }

    public static Reader getResourceAsReader(String resourceName) throws PolicyException {
        return new InputStreamReader(getResourceAsStream(resourceName));
    }

    public static XMLStreamBuffer getResourceAsXmlBuffer(String resourceName)
            throws PolicyException {
        try {
            return XMLStreamBuffer.createNewBufferFromXMLStreamReader(inputFactory.createXMLStreamReader(getResourceAsStream(resourceName)));
        } catch (XMLStreamException ex) {
            throw new PolicyException("Failed to create XMLStreamBuffer", ex);
        }
    }


    public static PolicySourceModel unmarshallModel(String resource) throws PolicyException, IOException {
        Reader resourceReader = getResourceAsReader(resource);
        PolicySourceModel model = PolicyModelUnmarshaller.getXmlUnmarshaller().unmarshalModel(resourceReader);
        resourceReader.close();
        return model;
    }

    public static Policy translateModel(PolicySourceModel model) throws PolicyException {
        return PolicyModelTranslator.getTranslator().translate(model);
    }

    public static Policy loadPolicy(String resourceName) throws PolicyException, IOException {
        return translateModel(unmarshallModel(resourceName));
    }

    public static <T extends PolicyAssertion> T getAssertionFromPolicy(String resourceName, Class<T> assertionClass) {
        try {
            Policy policy = loadPolicy(resourceName);
            if (policy.getNumberOfAssertionSets() != 1) {
                throw new IllegalStateException(String.format("Policy in '%s' does not contain a single alternative. Number of alternatives is %d", resourceName, policy.getNumberOfAssertionSets()));
            }

            QName assertionName = (QName) assertionClass.getField("NAME").get(null);
            if (!policy.contains(assertionName)) {
                return null;
            }
            
            return assertionClass.cast(policy.iterator().next().get(assertionName).iterator().next());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        } catch (PolicyException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
