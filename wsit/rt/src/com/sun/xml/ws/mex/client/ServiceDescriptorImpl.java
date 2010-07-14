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
package com.sun.xml.ws.mex.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
    private final List<Node> importsToPatch;
    
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
        importsToPatch = new ArrayList<Node>();
        nsToSysIdMap = new HashMap<String, String>();
        populateLists(mData);
        if (!importsToPatch.isEmpty()) {
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
    private void populateLists(final Metadata mData) {
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
    private void handleXml(final MetadataSection section) {
        final String dialect = section.getDialect();
        final String identifier = section.getIdentifier();
        if (dialect.equals(WSDL_DIALECT)) {
            wsdls.add(createSource(section, identifier));
        } else if (dialect.equals(SCHEMA_DIALECT)) {
            schemas.add(createSource(section, identifier));
        } else {
            logger.warning(
                MessagesMessages.MEX_0002_UNKNOWN_DIALECT_WITH_ID(
                dialect, identifier));
        }
    }

    /*
     * If the metadata section contains a metadata reference,
     * retrieve the new metadata and add it to the lists. This
     * method recursively calls the the populateLists method.
     */
    private void handleReference(final MetadataSection section) {
        final MetadataReference ref = section.getMetadataReference();
        populateLists(new MetadataClient().retrieveMetadata(ref));
    }
    
    /*
     * A mex location is simply the url of a document that can
     * be retrieved with an http GET call.
     */
    private void handleLocation(final MetadataSection section) {
        final String location = section.getLocation();
        final String dialect = section.getDialect();
        final String identifier = section.getIdentifier();
        if (dialect.equals(WSDL_DIALECT)) {
            wsdls.add(getSourceFromLocation(location, identifier));
        } else if (dialect.equals(SCHEMA_DIALECT)) {
            schemas.add(getSourceFromLocation(location, identifier));
        } else {
            logger.warning(
                MessagesMessages.MEX_0002_UNKNOWN_DIALECT_WITH_ID(
                dialect, identifier));
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
    private Source createSource(final MetadataSection section,
        final String identifier) {
        
        final Node node = (Node) section.getAny();
        String sysId = identifier;
        if (section.getDialect().equals(WSDL_DIALECT)) {
            final String targetNamespace = getNamespaceFromNode(node);
            if (sysId == null) {
                sysId = targetNamespace;
            }
            nsToSysIdMap.put(targetNamespace, sysId);
            checkWsdlImports(node);
        } else {
            if (sysId == null) {
                sysId = getNamespaceFromNode(node);
            }
        }
        final Source source = new DOMSource(node);
        source.setSystemId(sysId);
        return source;
    }
    
    /*
     * Turn the address of a document into a source. The document
     * referred to in a mex location element must be retrievable
     * with an HTTP GET call.
     */
    private Source getSourceFromLocation(final String address,
        final String identifier) {
        
        try {
            final HttpPoster poster = new HttpPoster();
            final InputStream response = poster.makeGetCall(address);
            if (identifier != null) {
                final StreamSource source = new StreamSource(response);
                source.setSystemId(identifier);
                return source;
            }
            return parseAndConvertStream(address, response);
        } catch (IOException ioe) {
            final String exceptionMessage =
                MessagesMessages.MEX_0014_RETRIEVAL_FROM_ADDRESS_FAILURE(
                address);
            logger.log(Level.SEVERE, exceptionMessage, ioe);
            throw new WebServiceException(exceptionMessage, ioe);
        }
    }
    
    /*
     * This method used when metadata section did not include
     * an identifier. The node passed in must be a wsdl:definitions
     * or an xsd:schema node.
     */
    private String getNamespaceFromNode(final Node node) {
        final Node namespace = node.getAttributes().getNamedItem("targetNamespace");
        if (namespace == null) {
            // bug in the server? want to avoid NPE if so
            logger.warning(
                MessagesMessages.MEX_0003_UNKNOWN_WSDL_NAMESPACE(
                node.getNodeName()));
            return null;
        }
        return namespace.getNodeValue();
    }

    /*
     * This method will check the wsdl for import nodes
     * that have no location attribute and add them to
     * the list to be patched.
     */
    private void checkWsdlImports(final Node wsdl) {
        final NodeList kids = wsdl.getChildNodes();
        for (int i=0; i<kids.getLength(); i++) {
            final Node importNode = kids.item(i);
            if (importNode.getLocalName() != null &&
                importNode.getLocalName().equals("import")) {
                
                final Node location =
                    importNode.getAttributes().getNamedItem(LOCATION);
                if (location == null) {
                    importsToPatch.add(importNode);
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
    private Source parseAndConvertStream(final String address,
        final InputStream stream) {
        
        try {
            final Transformer xFormer =
                TransformerFactory.newInstance().newTransformer();
            Source source = new StreamSource(stream);
            final DOMResult result = new DOMResult();
            xFormer.transform(source, result);
            final Node wsdlDoc = result.getNode();
            source = new DOMSource(wsdlDoc);
            source.setSystemId(getNamespaceFromNode(wsdlDoc.getFirstChild()));
            return source;
        } catch (TransformerException te) {
            final String exceptionMessage =
                MessagesMessages.MEX_0004_TRANSFORMING_FAILURE(address);
            logger.log(Level.SEVERE, exceptionMessage, te);
            throw new WebServiceException(exceptionMessage, te);
        }
    }

    /*
     * For wsdl:import statements that have no location attribute,
     * add a location with the value of the sysId of the imported
     * wsdl.
     */
    private void patchImports() throws DOMException {
        for (Node importNode : importsToPatch) {
            final NamedNodeMap atts = importNode.getAttributes();
            final String targetNamespace =
                atts.getNamedItem(NAMESPACE).getNodeValue();
            final String sysId = nsToSysIdMap.get(targetNamespace);
            if (sysId == null) {
                logger.warning(
                    MessagesMessages.MEX_0005_WSDL_NOT_FOUND_WITH_NAMESPACE(
                    targetNamespace));
                continue;
            }
            final Attr locationAtt =
                importNode.getOwnerDocument().createAttribute(LOCATION);
            locationAtt.setValue(sysId);
            atts.setNamedItem(locationAtt);
        }
    }
    
}
