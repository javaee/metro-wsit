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
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.policy.jcaps;


import com.sun.xml.ws.policy.jaxws.PolicyWSDLParserExtension;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver;
import com.sun.xml.ws.policy.jaxws.PolicyConfigResolver;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import java.io.IOException;
import java.net.URL;
import javax.xml.stream.XMLStreamException;
import junit.framework.*;
import org.xml.sax.SAXException;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class JCapsPolicyValidatorTest extends TestCase {
    public JCapsPolicyValidatorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testAssertionValidation() throws Exception {
        URL cfgFileUrl = PolicyUtils.ConfigFile.loadFromClasspath("policy/jcaps/test.wsdl");
        assertNotNull("Unable to locate test WSDL", cfgFileUrl);        
        
        parseFile(cfgFileUrl, false);
        parseFile(cfgFileUrl, true);
        
    }

    private void parseFile(URL cfgFileUrl, boolean isClient) throws IOException, XMLStreamException, SAXException {
        final SDDocumentSource doc = SDDocumentSource.create(cfgFileUrl);
        final XMLEntityResolver.Parser parser =  new XMLEntityResolver.Parser(doc);
        WSDLModel.WSDLParser.parse(
                parser,
                new PolicyConfigResolver(),
                isClient,
                new WSDLParserExtension[] { new PolicyWSDLParserExtension(false) }
        );        
    }
}
