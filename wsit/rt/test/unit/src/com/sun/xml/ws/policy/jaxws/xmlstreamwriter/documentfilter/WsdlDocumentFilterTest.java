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

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.IOException;
import java.io.StringWriter;
import com.sun.xml.ws.api.server.SDDocumentFilter;
import com.sun.xml.ws.util.xml.XMLStreamReaderToXMLStreamWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class WsdlDocumentFilterTest extends AbstractFilteringTest {
    private static final String[] testPolicyResources = new String[] {
        "policy_0",
        "policy_1",
        "policy_2",
        "policy_3",
        "policy_4"
    };
    
    private static final String[] testWsdlResources = new String[] {
        "W2JRLR2010TestService"
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
    
    public void testFilterPolicyExpression() throws Exception {
        performResourceBasedTest(testPolicyResources, "wsdl_filter/", ".xml", filter);
    }
    
    public void testFilterWSDL() throws Exception {
        StringWriter filteredBuffer = new StringWriter();
        StringWriter unfilteredBuffer = new StringWriter();

        readAndWriteWsdl(testWsdlResources[0], filteredBuffer, true);
        readAndWriteWsdl(testWsdlResources[0], unfilteredBuffer, false);

        assertEquals(unfilteredBuffer.toString(), filteredBuffer.toString());
    }
    
    private void readAndWriteWsdl(String wsdlName, StringWriter buffer, boolean filter) throws PolicyException, XMLStreamException {
        XMLStreamReader reader = null;
        XMLStreamWriter writer = null;
        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(PolicyResourceLoader.getResourceStream("wsdl_filter/" + wsdlName + ".wsdl"));
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(buffer /*, "UTF-8"*/);
            //generate the WSDL with utf-8 encoding and XML version 1.0
            writer.writeStartDocument("UTF-8", "1.0");
            if (filter) {
                writer = new WsdlDocumentFilter().filter(null, writer);
            }
            
            new XMLStreamReaderToXMLStreamWriter().bridge(reader, writer);
            
            writer.writeEndDocument();
        } finally {
            if (writer != null) try {writer.close();} finally {
                if (reader != null) reader.close();
            }
        }
        
    }
    
//    public void testPerformanceImpact() throws Exception {
//        SDDocumentFilter oldFilter = new SDDocumentFilter() {
//            InvocationProcessorFactory PRIVATE_ASSERTION_FILTER_FACTORY = new InvocationProcessorFactory() {
//                public InvocationProcessor createInvocationProcessor(final XMLStreamWriter writer) throws XMLStreamException {
//                    return new PrivateAssertionFilteringInvocationProcessor(writer);
//                }
//            };
//
//            InvocationProcessorFactory MEX_IMPORT_FILTER_FACTORY = new InvocationProcessorFactory() {
//                public InvocationProcessor createInvocationProcessor(final XMLStreamWriter writer) throws XMLStreamException {
//                    return new MexImportFilteringInvocationProcessor(writer);
//                }
//            };
//
//            InvocationProcessorFactory PRIVATE_ELEMENTS_FILTER_FACTORY = new InvocationProcessorFactory() {
//                public InvocationProcessor createInvocationProcessor(final XMLStreamWriter writer) throws XMLStreamException {
//                    return new PrivateElementFilteringInvocationProcessor(
//                            writer,
//                            new QName("http://schemas.sun.com/2006/03/wss/server", "KeyStore"),
//                            new QName("http://schemas.sun.com/2006/03/wss/server", "TrustStore"),
//                            new QName("http://schemas.sun.com/2006/03/wss/server", "CallbackHandlerConfiguration"),
//                            new QName("http://schemas.sun.com/2006/03/wss/server", "ValidatorConfiguration"),
//                            new QName("http://schemas.sun.com/2006/03/wss/server", "DisablePayloadBuffering"),
//
//                            new QName("http://schemas.sun.com/2006/03/wss/client", "KeyStore"),
//                            new QName("http://schemas.sun.com/2006/03/wss/client", "TrustStore"),
//                            new QName("http://schemas.sun.com/2006/03/wss/client", "CallbackHandlerConfiguration"),
//                            new QName("http://schemas.sun.com/2006/03/wss/client", "ValidatorConfiguration"),
//                            new QName("http://schemas.sun.com/2006/03/wss/client", "DisablePayloadBuffering"),
//
//                            new QName("http://schemas.sun.com/ws/2006/05/sc/server", "SCConfiguration"),
//
//                            new QName("http://schemas.sun.com/ws/2006/05/sc/client", "SCClientConfiguration"),
//
//                            new QName("http://schemas.sun.com/ws/2006/05/trust/server", "STSConfiguration"),
//
//                            new QName("http://schemas.sun.com/ws/2006/05/trust/client", "PreconfiguredSTS")
//                            );
//                }
//            };
//
//            public XMLStreamWriter filter(SDDocument sdDocument, XMLStreamWriter xmlStreamWriter) throws XMLStreamException,IOException {
//                XMLStreamWriter result = EnhancedXmlStreamWriterProxy.createProxy(xmlStreamWriter, PRIVATE_ASSERTION_FILTER_FACTORY);
//                result = EnhancedXmlStreamWriterProxy.createProxy(result, MEX_IMPORT_FILTER_FACTORY);
//                result = EnhancedXmlStreamWriterProxy.createProxy(result, PRIVATE_ELEMENTS_FILTER_FACTORY);
//                return result;
//            }
//        };
//
//        final int CYCLE_COUNT = 50;
//        long start = 0, end = 0;
//        double oldResult = 0, newResult = 0;
//        int oldCyclesRun = 0, newCyclesRun = 0;
//
//        for (int i = 0; i < 10; i++) {
//            newResult = runTest(newResult, i, CYCLE_COUNT, filter);
//            oldResult = runTest(oldResult, i, CYCLE_COUNT, oldFilter);
//            i++;
//            newResult = runTest(newResult, i, CYCLE_COUNT, filter);
//            oldResult = runTest(oldResult, i, CYCLE_COUNT, oldFilter);
//        }
//        System.out.println("Old: " + oldResult + "ms, new: " + newResult + "ms, improvement: " + (oldResult - newResult) + "ms, which is: " + (oldResult - newResult) / oldResult * 100 + "%");
//        for (int i = 0; i < 10; i++) {
//            oldResult = runTest(oldResult, i, CYCLE_COUNT, oldFilter);
//            newResult = runTest(newResult, i, CYCLE_COUNT, filter);
//            i++;
//            newResult = runTest(newResult, i, CYCLE_COUNT, filter);
//            oldResult = runTest(oldResult, i, CYCLE_COUNT, oldFilter);
//        }
//        System.out.println("Old: " + oldResult + "ms, new: " + newResult + "ms, improvement: " + (oldResult - newResult) + "ms, which is: " + (oldResult - newResult) / oldResult * 100 + "%");
//        for (int i = 0; i < 10; i++) {
//            newResult = runTest(newResult, i, CYCLE_COUNT, filter);
//            oldResult = runTest(oldResult, i, CYCLE_COUNT, oldFilter);
//            i++;
//            oldResult = runTest(oldResult, i, CYCLE_COUNT, oldFilter);
//            newResult = runTest(newResult, i, CYCLE_COUNT, filter);
//        }
//        System.out.println("Old: " + oldResult + "ms, new: " + newResult + "ms, improvement: " + (oldResult - newResult) + "ms, which is: " + (oldResult - newResult) / oldResult * 100 + "%");
//        for (int i = 0; i < 10; i++) {
//            oldResult = runTest(oldResult, i, CYCLE_COUNT, oldFilter);
//            newResult = runTest(newResult, i, CYCLE_COUNT, filter);
//            i++;
//            oldResult = runTest(oldResult, i, CYCLE_COUNT, oldFilter);
//            newResult = runTest(newResult, i, CYCLE_COUNT, filter);
//        }
//        System.out.println("Old: " + oldResult + "ms, new: " + newResult + "ms, improvement: " + (oldResult - newResult) + "ms, which is: " + (oldResult - newResult) / oldResult * 100 + "%");
//    }
//
//    private double calculateNewAverage(double oldCycleAverage, int cycleCount, int runNumber, long startTimeOfRun, long endTimeOfRun) {
//        double cycleAverageForRun = (double)(endTimeOfRun - startTimeOfRun) / cycleCount;
//        if (runNumber > 1) {
//            double newCycleAverage = (double) (runNumber - 1) / runNumber * oldCycleAverage + cycleAverageForRun / runNumber;
//            return newCycleAverage;
//        } else {
//            return cycleAverageForRun;
//        }
//    }
//
//    private double runTest(double oldAverage, int runNumber, final int CYCLE_COUNT, SDDocumentFilter filter) throws Exception {
//        long start = 0, end = 0;
//
//        start = System.currentTimeMillis();
//        for (int i = 0; i < CYCLE_COUNT; i++) {
//            performResourceBasedTest(testResources, "wsdl_filter/", ".xml", filter);
//        }
//        end = System.currentTimeMillis();
//        return calculateNewAverage(oldAverage, CYCLE_COUNT, runNumber, start, end);
//    }
}
