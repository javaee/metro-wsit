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

package com.sun.tools.ws.policy.jaxws;

import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler;
import com.sun.tools.ws.wscompile.AbortException;
import com.sun.tools.ws.wscompile.ErrorReceiverFilter;
import com.sun.tools.ws.wscompile.WsimportListener;
import com.sun.tools.ws.wscompile.WsimportOptions;
import java.io.File;
import java.net.URL;
import junit.framework.*;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
public class PolicyExtensionHandlerTest extends TestCase {
    
    
    // we only need a fake implementation
    class Listener extends WsimportListener {

        @Override
        public void generatedFile(String fileName) {
        }
        
        @Override
        public void message(String msg) {
        }
        
        @Override
        public void error(SAXParseException exception) {
        }
        
        @Override
        public void fatalError(SAXParseException exception) {
        }
        
        @Override
        public void warning(SAXParseException exception) {
        }
        
        @Override
        public void info(SAXParseException exception) {
        }
        
        public void enableDebugging(){
        }
    }
    
    public PolicyExtensionHandlerTest(String testName) {
        super(testName);
    }
    
    public void testByRunningFakeWsImport() {
        WsimportOptions importOptions = new WsimportOptions();

        URL resource = Thread.currentThread().getContextClassLoader().getResource("testPolicyTWSDLExtension.wsdl");
        importOptions.addWSDL(new File(resource.getFile()));
        final Listener listener = new Listener();
        // another fake impl
        ErrorReceiverFilter receiver = new ErrorReceiverFilter(listener) {
            public void info(SAXParseException exception) {
            }
            
            public void warning(SAXParseException exception) {
            }
            
            @Override
            public void pollAbort() throws AbortException {
            }
        };
        
        WSDLModeler wsdlModeler = new WSDLModeler(importOptions, receiver);
        Model model = wsdlModeler.buildModel();
        assertNotNull(model);
    }
    
}
