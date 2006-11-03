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
 * WsdlDocumentFilterTest.java
 * JUnit based test
 *
 * @author Marek Potociar (marek.potociar at sun.com) 
 */

package com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter;

import java.io.StringWriter;
import com.sun.xml.ws.api.server.SDDocumentFilter;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class WsdlDocumentFilterTest extends AbstractFilteringTest {
    private static final String[] testResources = new String[] {
        "policy_0",
        "policy_1",
        "policy_2"
    };
    private static final SDDocumentFilter filter = new WsdlDocumentFilter();
    
    public WsdlDocumentFilterTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of createProxy method, of class com.sun.xml.ws.policy.jaxws.documentfilter.FilteringXmlStreamWriterProxy.
     */
    public void testCreateProxy() throws Exception {
        XMLStreamWriter result = openFilteredWriter(new StringWriter(), filter);
        
        assertNotNull(result);
    }
    
    public void testFilterPrivateAssertionsFromPolicyExpression() throws Exception {
        performResourceBasedTest(testResources, "wsdl_filter/", ".xml", filter);
    }}
