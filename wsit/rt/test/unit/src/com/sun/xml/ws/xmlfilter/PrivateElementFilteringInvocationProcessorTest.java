/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

/*
 * PrivateElementFilteringInvocationProcessorTest.java
 * JUnit based test
 *
 * Created on November 10, 2006, 2:51 PM
 */

package com.sun.xml.ws.xmlfilter;

import java.io.StringWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class PrivateElementFilteringInvocationProcessorTest  extends AbstractFilteringTestCase {
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
