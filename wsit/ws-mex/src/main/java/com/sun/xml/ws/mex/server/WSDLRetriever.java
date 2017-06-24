/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.mex.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.DocumentAddressResolver;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.api.server.WSEndpoint;

import com.sun.xml.ws.mex.MessagesMessages;

import static com.sun.xml.ws.mex.MetadataConstants.MEX_NAMESPACE;
import static com.sun.xml.ws.mex.MetadataConstants.MEX_PREFIX;
import static com.sun.xml.ws.mex.MetadataConstants.SCHEMA_DIALECT;
import static com.sun.xml.ws.mex.MetadataConstants.WSDL_DIALECT;

/**
 * This class is used to add the endpoint's metadata to
 * the mex MetadataResponse element.
 *
 * @author WS Development Team
 */
public class WSDLRetriever {

    private final WSEndpoint endpoint;
    
    private static final Logger logger =
        Logger.getLogger(WSDLRetriever.class.getName());
    
    /*
     * This class is used by the SDDocument object in the
     * jax-ws runtime. The agreement we have is to return
     * null to the jax-ws runtime for mex responses. This
     * tells the jax-ws runtime not to add schemaLocation
     * attributes for schema import statements.
     */
    private static final DocumentAddressResolver dar =
        new DocumentAddressResolver() {
        public String getRelativeAddressFor(final SDDocument doc1,
            final SDDocument doc2) {
            
            return null;
        }
    };
    
    public WSDLRetriever(WSEndpoint endpoint) {
        this.endpoint = endpoint;
    }
    
    /*
     * This method is called by the server pipe to write out
     * the wsdl and schema documents to the mex response.
     */
    void addDocuments(final XMLStreamWriter writer, final Packet request,
        final String address) throws XMLStreamException {
        
        final ServiceDefinition sDef = endpoint.getServiceDefinition();
        if (sDef == null) {
            return;
        }
        final Iterator<SDDocument> docs = sDef.iterator();
        while (docs.hasNext()) {
            writeDoc(writer, docs.next(), address);
        }
    }
    
    /*
     * This method writes out each individual document, which
     * must be wrapped in a MetadataSection element within
     * the mex response. It also sets the Dialect attribure
     * of the section so that sections can be classified as
     * wsdl, schema, etc. when parsed.
     */
    private void writeDoc(final XMLStreamWriter writer, final SDDocument doc,
        final String address) throws XMLStreamException {
        
        try {
            writer.writeStartElement(MEX_PREFIX,
                "MetadataSection", MEX_NAMESPACE);
            if (doc.isWSDL()) {
                writer.writeAttribute("Dialect", WSDL_DIALECT);
                writer.writeAttribute("Identifier",
                    ((SDDocument.WSDL) doc).getTargetNamespace());
            } else if(doc.isSchema()) {
                writer.writeAttribute("Dialect", SCHEMA_DIALECT);
                writer.writeAttribute("Identifier",
                    ((SDDocument.Schema) doc).getTargetNamespace());
            }
            doc.writeTo(new MEXAddressResolver(endpoint.getServiceName(), endpoint.getPortName(), address), dar, writer);
            writer.writeEndElement();
        } catch (IOException ioe) {
            // this should be very rare
            String exceptionMessage =
                MessagesMessages.MEX_0015_IOEXCEPTION_WHILE_WRITING_RESPONSE(address);
            logger.log(Level.SEVERE, exceptionMessage, ioe);
            throw new WebServiceException(exceptionMessage, ioe);
        }
    }

}
