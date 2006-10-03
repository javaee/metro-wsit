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

package com.sun.xml.ws.policy.jaxws.documentfilter;

import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.SDDocumentFilter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

/**
 * The class provides an implementaion of JAX-WS {@code SDDocumentFilter} interface.
 * The filter may be used to instantiate {@code XMLStreamWriter} that do filter
 * private policy assertion elements and their contents out of the stream during
 * the serialization of a document.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class PrivateAssertionFilter implements SDDocumentFilter{
    public XMLStreamWriter filter(SDDocument sdDocument, XMLStreamWriter xmlStreamWriter) {
        try {
            return FilteringXmlStreamWriterProxy.createProxy(xmlStreamWriter);
        } catch (XMLStreamException ex) {
            throw new WebServiceException(ex); // TODO
        }
    }
}
