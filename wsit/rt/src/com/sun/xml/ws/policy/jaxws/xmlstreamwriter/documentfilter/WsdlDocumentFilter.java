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

package com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter;

import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.SDDocumentFilter;
import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.EnhancedXmlStreamWriterProxy;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * The class provides an implementaion of JAX-WS {@code SDDocumentFilter} interface.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class WsdlDocumentFilter implements SDDocumentFilter {    
    public XMLStreamWriter filter(SDDocument sdDocument, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        XMLStreamWriter result = EnhancedXmlStreamWriterProxy.createProxy(xmlStreamWriter, FilteringInvocationProcessorFactory.getFactory(FilteringInvocationProcessorFactory.FilterType.PRIVATE_ASSERTION_FILTER));
        result = EnhancedXmlStreamWriterProxy.createProxy(result, FilteringInvocationProcessorFactory.getFactory(FilteringInvocationProcessorFactory.FilterType.MEX_FILTER));
                
        return result;
    }
}
