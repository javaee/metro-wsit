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

package com.sun.xml.ws.xmlfilter;

import com.sun.xml.ws.api.policy.ModelTranslator;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.policy.ModelUnmarshaller;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

/**
 * This class provides utility methods to load resources and unmarshall policy source model.
 *
 * @author Marek Potociar
 * @author Fabian Ritzmann
 */
final class ResourceLoader {

    public static final String POLICY_UNIT_TEST_RESOURCE_ROOT = "xmlfilter/";

    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    
    private ResourceLoader() {
    }
    
    public static PolicySourceModel unmarshallModel(String resource) throws PolicyException, IOException {
        Reader resourceReader = getResourceReader(resource);
        PolicySourceModel model = ModelUnmarshaller.getUnmarshaller().unmarshalModel(resourceReader);
        resourceReader.close();
        return model;
    }
    
    public static PolicySourceModel unmarshallModel(Reader resourceReader) throws PolicyException, IOException {
        PolicySourceModel model = ModelUnmarshaller.getUnmarshaller().unmarshalModel(resourceReader);
        resourceReader.close();
        return model;
    }
    
    public static InputStream getResourceStream(String resourceName) throws PolicyException {
        String fullName = POLICY_UNIT_TEST_RESOURCE_ROOT + resourceName;
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(fullName);
        if (input == null) {
            throw new PolicyException("Failed to find resource \"" + fullName + "\"");
        }
        return input;
    }
    
    public static Reader getResourceReader(String resourceName) throws PolicyException {
        return new InputStreamReader(getResourceStream(resourceName));
    }
    
    public static XMLStreamBuffer getResourceXmlBuffer(String resourceName)
        throws PolicyException {
        try {
            return XMLStreamBuffer.createNewBufferFromXMLStreamReader(inputFactory.createXMLStreamReader(getResourceStream(resourceName)));
        } catch (XMLStreamException ex) {
            throw new PolicyException("Failed to create XMLStreamBuffer", ex);
        }
    }
    
    public static URL getResourceUrl(String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResource(POLICY_UNIT_TEST_RESOURCE_ROOT + resourceName);
    }
    
    public static Policy translateModel(PolicySourceModel model) throws PolicyException {
        return ModelTranslator.getTranslator().translate(model);
    }
    
    public static Policy loadPolicy(String resourceName) throws PolicyException, IOException {
        return translateModel(unmarshallModel(resourceName));
    }

   
    // reads policy map from given wsdl document
    public static PolicyMap getPolicyMap(String resourceName)
        throws PolicyException {
        
        WSDLModel model = getWSDLModel(resourceName, true);
        return model.getPolicyMap();
    }
    
    public static PolicyMap getPolicyMap(String resourceName, boolean isClient)
        throws PolicyException {
        
        WSDLModel model = getWSDLModel(resourceName, isClient);
        return model.getPolicyMap();
    }

    public static WSDLModel getWSDLModel(String resourceName) throws PolicyException {
        return getWSDLModel(resourceName, true);        
    }
    
    // reads wsdl model from given wsdl document
    public static WSDLModel getWSDLModel(String resourceName, boolean isClient) throws PolicyException {        
        URL resourceUrl = getResourceUrl(resourceName);
        try {
            return com.sun.xml.ws.policy.parser.PolicyResourceLoader.getWsdlModel(resourceUrl, isClient);
        } catch (XMLStreamException ex) {
            throw new PolicyException("Failed to parse document", ex);
        } catch (IOException ex) {
            throw new PolicyException("Failed to parse document", ex);
        } catch (SAXException ex) {
            throw new PolicyException("Failed to parse document", ex);
        }
    }
 
}
