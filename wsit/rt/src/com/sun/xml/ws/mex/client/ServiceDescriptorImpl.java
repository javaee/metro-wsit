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
package com.sun.xml.ws.mex.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Node;

import com.sun.xml.ws.api.wsdl.parser.ServiceDescriptor;
import com.sun.xml.ws.mex.client.schema.Metadata;
import com.sun.xml.ws.mex.client.schema.MetadataReference;
import com.sun.xml.ws.mex.client.schema.MetadataSection;

import static com.sun.xml.ws.mex.MetadataConstants.SCHEMA_DIALECT;
import static com.sun.xml.ws.mex.MetadataConstants.WSDL_DIALECT;

/**
 * This class is used by the JAX-WS code when it needs to retrieve
 * metadata from an endpoint using mex. An address is passed into
 * the MetadataResolverImpl class, which creates a service
 * descriptor and returns it.
 */
public class ServiceDescriptorImpl extends ServiceDescriptor {
    
    private final List<Source> wsdls;
    private final List<Source> schemas;
    
    private static final Logger logger =
        Logger.getLogger(ServiceDescriptorImpl.class.getName());
    
    /**
     * The ServiceDescriptorImpl constructor does the work of
     * parsing the data in the Metadata object.
     */
    public ServiceDescriptorImpl(Metadata mData) {
        wsdls = new ArrayList<Source>();
        schemas = new ArrayList<Source>();
        populateLists(mData);
    }
    
    /*
     * This will be called recursively for metadata sections
     * that contain metadata references. A metadata section can
     * contain the xml of metadata itself (the default case), a
     * metadata reference that needs to be retrieved, or a
     * mex location which can be retrieved with http GET call.
     */
    private void populateLists(Metadata mData) {
        for (MetadataSection section : mData.getMetadataSection()) {
            if (section.getMetadataReference() != null) {
                handleReference(section);
            } else if (section.getLocation() != null) {
                handleLocation(section);
            } else {
                handleXml(section);
            }
        }
    }

    /*
     * This is the normal case where a metadata section contains
     * xml representing a wsdl, schema, etc.
     */
    private void handleXml(MetadataSection section) {
        String dialect = section.getDialect();
        String id = section.getIdentifier();
        if (dialect.equals(WSDL_DIALECT)) {
            wsdls.add(createSource(section, id));
        } else if (dialect.equals(SCHEMA_DIALECT)) {
            schemas.add(createSource(section, id));
        } else {
            logger.warning("Ignoring unknown dialect \"" +
                dialect + "\" in metadata response with identifier \"" +
                id + "\"");
        }
    }

    /*
     * If the metadata section contains a metadata reference,
     * retrieve the new metadata and add it to the lists. This
     * method recursively calls the the populateLists method.
     */
    private void handleReference(MetadataSection section) {
        MetadataReference ref = section.getMetadataReference();
        populateLists(new MetadataClient().retrieveMetadata(ref));
    }
    
    /*
     * A mex location is simply the url of a document that can
     * be retrieved with an http GET call.
     */
    private void handleLocation(MetadataSection section) {
        String location = section.getLocation();
        String dialect = section.getDialect();
        String id = section.getIdentifier();
        if (dialect.equals(WSDL_DIALECT)) {
            wsdls.add(getSourceFromLocation(location, id));
        } else if (dialect.equals(SCHEMA_DIALECT)) {
            schemas.add(getSourceFromLocation(location, id));
        } else {
            logger.warning("Ignoring unknown dialect \"" +
                dialect + "\" in metadata response with identifier \"" +
                id + "\"");
        }
    }
    
    public List<Source> getWSDLs() {
        return wsdls;
    }

    public List<Source> getSchemas() {
        return schemas;
    }
    
    /*
     * Helper method used by handleXml() to turn the xml DOM nodes
     * into Sources objects.
     */
    private Source createSource(MetadataSection section, String id) {
        Node n = (Node) section.getAny();
        Source source = new DOMSource(n);
        if (id == null) {
            id = getIdFromNode(n);
        }
        source.setSystemId(id);
        return source;
    }
    
    /*
     * Turn the address of a document into a source. The document
     * referred to in a mex location element must be retrievable
     * with an HTTP GET call.
     */
    private Source getSourceFromLocation(String address, String id) {
        try {
            HttpPoster poster = new HttpPoster();
            InputStream response = poster.makeGetCall(address);
            if (id != null) {
                StreamSource source = new StreamSource(response);
                source.setSystemId(id);
                return source;
            }
            return parseAndConvertStream(address, response);
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }
    
    /*
     * This method used when metadata section did not include
     * an identifier. The node passed in must be a wsdl:definitions
     * or an xsd:schema node.
     */
    private String getIdFromNode(Node node) {
        Node namespace = node.getAttributes().getNamedItem("targetNamespace");
        if (namespace == null) {
            logger.warning("No targetNamespace was found for element " +
                node.getNamespaceURI() + ":" + node.getLocalName() +
                " in metadata response.");
            return null;
        }
        return namespace.getNodeValue();
    }

    /*
     * This method used when metadata section did not include
     * an identifier. Since we need to read some of this information
     * to get the namespace and then return it to be read again by
     * jax-ws, we cannot use the InputStream itself (cannot call
     * mark/reset on InputStream).
     *
     * TODO: This isn't a common use case, but can this be sped up?
     */
    private Source parseAndConvertStream(String address, InputStream stream) {
        try {
            Transformer xFormer =
                TransformerFactory.newInstance().newTransformer();
            Source source = new StreamSource(stream);
            DOMResult result = new DOMResult();
            xFormer.transform(source, result);
            Node node = result.getNode();
            source = new DOMSource(node);
            source.setSystemId(getIdFromNode(node.getFirstChild()));
            return source;
        } catch (TransformerException te) {
            throw new WebServiceException("Exception while " +
                "trying to convert and read targetNamespace from " +
                "location " + address, te);
        }
    }
    
}
