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

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.output.DomSerializer;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.wsdl.writer.WSDLGenExtnContext;
import com.sun.xml.ws.util.Pool.Marshaller;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Fabian Ritzmann
 */
public class PolicyWSDLGeneratorExtensionTest extends TestCase {

    private DocumentBuilder builder;
//    private XMLOutputFactory factory;


    public PolicyWSDLGeneratorExtensionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws ParserConfigurationException {
        final DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        documentFactory.setNamespaceAware(true);
        builder = documentFactory.newDocumentBuilder();
//        factory = XMLOutputFactory.newInstance();
    }

    public void testStart() {
        final PolicyWSDLGeneratorExtension instance = new PolicyWSDLGeneratorExtension();
        final Document document = builder.newDocument();
        final TypedXmlWriter writer = TXW.create(new QName("test"), TypedXmlWriter.class, new DomSerializer(document));
        final WSDLGenExtnContext context = new WSDLGenExtnContext(writer, new MockSEIModel(), new MockWSBinding(), null, PolicyWSDLGeneratorExtensionTest.class);
        instance.start(context);
        writer.commit();
        final Element element = document.getDocumentElement();
        assertEquals("test", element.getLocalName());
        // final Node policyElement = element.getFirstChild();
    }


    private static class MockSEIModel implements SEIModel {

        public Marshaller getMarshallerPool() {
            return null;
        }

        public JAXBRIContext getJAXBContext() {
            return null;
        }

        public JavaMethod getJavaMethod(Method method) {
            return null;
        }

        public JavaMethod getJavaMethod(QName name) {
            return null;
        }

        public JavaMethod getJavaMethodForWsdlOperation(QName operationName) {
            return null;
        }

        public Collection<? extends JavaMethod> getJavaMethods() {
            return new LinkedList<JavaMethod>();
        }

        public String getWSDLLocation() {
            return null;
        }

        public QName getServiceQName() {
            return null;
        }

        public WSDLPort getPort() {
            return null;
        }

        public QName getPortName() {
            return null;
        }

        public QName getPortTypeName() {
            return null;
        }

        public QName getBoundPortTypeName() {
            return null;
        }

        public String getTargetNamespace() {
            return null;
        }

    }


    private static class MockWSBinding implements WSBinding {

        public SOAPVersion getSOAPVersion() {
            return null;
        }

        public AddressingVersion getAddressingVersion() {
            return null;
        }

        public BindingID getBindingId() {
            return null;
        }

        public List<Handler> getHandlerChain() {
            return null;
        }

        public boolean isFeatureEnabled(Class<? extends WebServiceFeature> feature) {
            return false;
        }

        public <F extends WebServiceFeature> F getFeature(Class<F> featureType) {
            return null;
        }

        public WSFeatureList getFeatures() {
            return null;
        }

        public void setHandlerChain(List<Handler> chain) {
        }

        public String getBindingID() {
            return null;
        }

    }
}
