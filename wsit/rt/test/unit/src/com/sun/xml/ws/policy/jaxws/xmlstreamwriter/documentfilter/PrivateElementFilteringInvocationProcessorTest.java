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
 * PrivateElementFilteringInvocationProcessorTest.java
 * JUnit based test
 *
 * Created on November 10, 2006, 2:51 PM
 */

package com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter;

import junit.framework.*;
import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.*;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Queue;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class PrivateElementFilteringInvocationProcessorTest  extends AbstractFilteringTest {
    private static final String[] testResources = new String[] {
        "element_01",
        "element_02"
    };

    private static final InvocationProcessorFactory factory = new InvocationProcessorFactory() {
        public InvocationProcessor createInvocationProcessor(XMLStreamWriter writer) throws XMLStreamException {
            return new FilteringInvocationProcessor(writer, new PrivateElementFilteringStateMachine(
                    new QName("http://schemas.sun.com/2006/03/wss/server", "KeyStore"),
                    new QName("http://schemas.sun.com/2006/03/wss/server", "TrustStore"),
                    new QName("http://schemas.sun.com/2006/03/wss/server", "CallbackHandlerConfiguration"),
                    new QName("http://schemas.sun.com/2006/03/wss/server", "ValidatorConfiguration"),
                    new QName("http://schemas.sun.com/2006/03/wss/server", "DisablePayloadBuffering"),
                    
                    new QName("http://schemas.sun.com/2006/03/wss/client", "KeyStore"),
                    new QName("http://schemas.sun.com/2006/03/wss/client", "TrustStore"),
                    new QName("http://schemas.sun.com/2006/03/wss/client", "CallbackHandlerConfiguration"),
                    new QName("http://schemas.sun.com/2006/03/wss/client", "ValidatorConfiguration"),
                    new QName("http://schemas.sun.com/2006/03/wss/client", "DisablePayloadBuffering"),
                    
                    new QName("http://schemas.sun.com/ws/2006/05/sc/server", "SCConfiguration"),
                    
                    new QName("http://schemas.sun.com/ws/2006/05/sc/client", "SCClientConfiguration"),
                    
                    new QName("http://schemas.sun.com/ws/2006/05/trust/server", "STSConfiguration"),
                    
                    new QName("http://schemas.sun.com/ws/2006/05/trust/client", "PreconfiguredSTS")));
        }
    };
    
    public PrivateElementFilteringInvocationProcessorTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of createProxy method, of class com.sun.xml.ws.policy.jaxws.documentfilter.FilteringXmlStreamWriterProxy.
     */
    public void testCreateProxy() throws Exception {
        XMLStreamWriter result = openFilteredWriter(new StringWriter(), factory);
        
        assertNotNull(result);
    }
    
    public void testFilterPrivateAssertionsFromPolicyExpression() throws Exception {
        performResourceBasedTest(testResources, "element_filtering/", ".xml", factory);
    }
}
