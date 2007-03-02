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
import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.InvocationProcessor;
import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.InvocationProcessorFactory;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * The class provides an implementaion of JAX-WS {@code SDDocumentFilter} interface.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class WsdlDocumentFilter implements SDDocumentFilter {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(WsdlDocumentFilter.class);
    
    private static final InvocationProcessorFactory FILTERING_FACOTRY = new InvocationProcessorFactory() {
        public InvocationProcessor createInvocationProcessor(final XMLStreamWriter writer) throws XMLStreamException {
            return new FilteringInvocationProcessor(
                    writer,
                    new MexImportFilteringStateMachine(),
                    new PrivateAttributeFilteringStateMachine(),
                    new PrivateElementFilteringStateMachine(
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
                    
                    new QName("http://schemas.sun.com/ws/2006/05/trust/client", "PreconfiguredSTS")
                    )
                    );
        }
    };
    
    public XMLStreamWriter filter(final SDDocument sdDocument, final XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        if (LOGGER.isMethodCallLoggable()) {
            LOGGER.entering(sdDocument, xmlStreamWriter);
        }
        XMLStreamWriter result = null;
        try {
            result = EnhancedXmlStreamWriterProxy.createProxy(xmlStreamWriter, FILTERING_FACOTRY);
            return result;
        } finally {
            LOGGER.exiting(result);
        }
    }
}
