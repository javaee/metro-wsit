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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xml.ws.api.wsdl.parser.ServiceDescriptor;
import com.sun.xml.ws.mex.MessagesMessages;
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
 * <P>
 * Because wsdl and schema import@location attributes are removed
 * from the data when empty, this class will add them back in
 * for wsdl imports. The value that is used for the attribute
 * matches the systemId of the Source that contains the imported
 * wsdl (which may be different from the target namespace of the
 * wsdl).
 */
public class ServiceDescriptorImpl extends ServiceDescriptor {
    
    private final List<Source> wsdls;
    private final List<Source> schemas;
    
    // holds nodes that are missing location attributes
    private final List<Node> importNodesToPatch;
    
    // holds sysId for wsdls, key is wsdl targetNamespace
    private final Map<String, String> nsToSysIdMap;

    private static final String LOCATION = "location";
    private static final String NAMESPACE = "namespace";
    
    private static final Logger logger =
        Logger.getLogger(ServiceDescriptorImpl.class.getName());
    
    /**
     * The ServiceDescriptorImpl constructor does the work of
     * parsing the data in the Metadata object.
     */
    public ServiceDescriptorImpl(Metadata mData) {
        wsdls = new ArrayList<Source>();
        schemas = new ArrayList<Source>();
        importNodesToPatch = new ArrayList<Node>();
        nsToSysIdMap = new HashMap<String, String>();
        populateLists(mData);
        if (!importNodesToPatch.isEmpty()) {
            patchImports();
        }
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
            logger.warning(
                MessagesMessages.UNKNOWN_DIALECT_WITH_ID(dialect, id));
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
            logger.warning(
                MessagesMessages.UNKNOWN_DIALECT_WITH_ID(dialect, id));
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
     * into Sources objects. This method is also responsible for
     * adding data to the nsToSysIdMap map for wsdl sections in
     * case there are wsdl:import elements that need to be patched.
     */
    private Source createSource(MetadataSection section, String id) {
        Node n = (Node) section.getAny();
        if (section.getDialect().equals(WSDL_DIALECT)) {
            String targetNamespace = getNamespaceFromNode(n);
            if (id == null) {
                id = targetNamespace;
            }
            nsToSysIdMap.put(targetNamespace, id);
            checkWsdlImports(n);
        } else {
            if (id == null) {
                id = getNamespaceFromNode(n);
            }
        }
        Source source = new DOMSource(n);
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
    private String getNamespaceFromNode(Node node) {
        Node namespace = node.getAttributes().getNamedItem("targetNamespace");
        if (namespace == null) {
            // bug in the server? want to avoid NPE if so
            logger.warning(
                MessagesMessages.UNKNOWN_WSDL_NAMESPACE(node.toString()));
            return null;
        }
        return namespace.getNodeValue();
    }

    /*
     * This method will check the wsdl for import nodes
     * that have no location attribute and add them to
     * the list to be patched.
     */
    private void checkWsdlImports(Node wsdl) {
        NodeList kids = wsdl.getChildNodes();
        for (int i=0; i<kids.getLength(); i++) {
            Node importNode = kids.item(i);
            if (importNode.getLocalName() != null &&
                importNode.getLocalName().equals("import")) {
                
                Node location =
                    importNode.getAttributes().getNamedItem(LOCATION);
                if (location == null) {
                    importNodesToPatch.add(importNode);
                }
            }
        }
    }
    
    /*
     * This method used when metadata location section did not include
     * an identifier. Since we need to read some of this information
     * to get the namespace and then return it to be read again by
     * jax-ws, we cannot use the InputStream itself (cannot call
     * mark/reset on InputStream).
     *
     * It is not expected that a wsdl retrieved with mex location
     * will import another wsdl in the mex response, so this
     * wsdl is not checked for empty wsdl import locations.
     */
    private Source parseAndConvertStream(String address, InputStream stream) {
        try {
            Transformer xFormer =
                TransformerFactory.newInstance().newTransformer();
            Source source = new StreamSource(stream);
            DOMResult result = new DOMResult();
            xFormer.transform(source, result);
            Node wsdlDoc = result.getNode();
            source = new DOMSource(wsdlDoc);
            source.setSystemId(getNamespaceFromNode(wsdlDoc.getFirstChild()));
            return source;
        } catch (TransformerException te) {
            throw new WebServiceException(
                MessagesMessages.TRANSFORMING_FAILURE(address), te);
        }
    }

    /*
     * For wsdl:import statements that have no location attribute,
     * add a location with the value of the sysId of the imported
     * wsdl.
     */
    private void patchImports() throws DOMException {
        for (Node importNode : importNodesToPatch) {
            NamedNodeMap atts = importNode.getAttributes();
            String targetNamespace =
                atts.getNamedItem(NAMESPACE).getNodeValue();
            String sysId = nsToSysIdMap.get(targetNamespace);
            if (sysId == null) {
                logger.warning(MessagesMessages.WSDL_NOT_FOUND_WITH_NAMESPACE(
                    targetNamespace));
                return;
            }
            Attr locationAtt =
                importNode.getOwnerDocument().createAttribute(LOCATION);
            locationAtt.setValue(sysId);
            atts.setNamedItem(locationAtt);
        }
    }
    
}
