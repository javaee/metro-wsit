/*
 * JCapsPolicyValidatorTest.java
 * JUnit based test
 *
 * Created on April 27, 2007, 4:07 PM
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
