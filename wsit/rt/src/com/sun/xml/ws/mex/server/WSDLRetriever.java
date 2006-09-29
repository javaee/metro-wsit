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
package com.sun.xml.ws.mex.server;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.DocumentAddressResolver;
import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.api.server.WSEndpoint;

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

    private WSEndpoint endpoint;
    
    /*
     * This class is used by the SDDocument object in the
     * jax-ws runtime. The agreement we have is to return
     * null to the jax-ws runtime for mex responses. This
     * tells the jax-ws runtime not to add schemaLocation
     * attributes for schema import statements.
     */
    private static final DocumentAddressResolver dar =
        new DocumentAddressResolver() {
        public String getRelativeAddressFor(SDDocument d1, SDDocument d2) {
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
    void addDocuments(XMLStreamWriter writer, Packet request, String address)
        throws XMLStreamException {
        
        ServiceDefinition sDef = endpoint.getServiceDefinition();
        Iterator<SDDocument> docs = sDef.iterator();
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
    private void writeDoc(XMLStreamWriter writer, SDDocument doc, String add)
        throws XMLStreamException {
        
        try {
            writer.writeStartElement(MEX_PREFIX,
                "MetadataSection", MEX_NAMESPACE);
            if (doc.isWSDL()) {
                writer.writeAttribute("Dialect", WSDL_DIALECT);
            } else if(doc.isSchema()) {
                writer.writeAttribute("Dialect", SCHEMA_DIALECT);
            }
            doc.writeTo(new PortAddressResolverImpl(add), dar, writer);
            writer.writeEndElement();
        } catch (IOException ioe) {
            // this should be very rare
            throw new WebServiceException(ioe);
        }
    }

    /*
     * This object is passed to the jax-ws runtime to give
     * the address to be included in the wsdl.
     */
    static class PortAddressResolverImpl implements PortAddressResolver {

        private String address;
        
        PortAddressResolverImpl(String address) {
            this.address = address;
        }
        
        public String getAddressFor(String portName) {
            return address;
        }
        
    }
}
