/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.mex.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sun.istack.NotNull;
import com.sun.xml.ws.mex.MessagesMessages;
import com.sun.xml.ws.mex.client.schema.Metadata;
import com.sun.xml.ws.mex.client.schema.MetadataReference;
import com.sun.xml.ws.mex.client.schema.MetadataSection;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.sun.xml.ws.mex.MetadataConstants.ERROR_LOG_LEVEL;
import static com.sun.xml.ws.mex.MetadataConstants.WSDL_DIALECT;
import static com.sun.xml.ws.mex.MetadataConstants.SCHEMA_DIALECT;

/**
 * Class used for retrieving metadata at runtime. The intended usage is:
 * <p>
 * <code>MetadataClient mClient = new MetadataClient();</code><br>
 * <code>Metadata mData = mClient.retrieveMetadata(someAddress);</code><br>
 * <p>
 * Utility methods will be added for common usages of the metadata. For
 * instance, the service and port QNames from the endpoint can be
 * retrieved from the metadata with:
 * <p>
 * <code>Map&lt;QName, List&lt;PortInfo&gt;&gt; names = mClient.getServiceAndPortNames(mData);</code>
 */
public class MetadataClient {
    
    enum Protocol { SOAP_1_2, SOAP_1_1 };
    
    private final String [] suffixes = { "" , "/mex" };
    private final MetadataUtil mexUtil;
    private static final JAXBContext jaxbContext;
    
    private static final Logger logger =
        Logger.getLogger(MetadataClient.class.getName());
    
    static {
        try {
            jaxbContext = JAXBContext.newInstance(
                "com.sun.xml.ws.mex.client.schema");
        } catch (JAXBException jaxbE) {
            throw new AssertionError(jaxbE);
        }
    }
   
   /**
     * Default constructor.
     */
    public MetadataClient() {
        mexUtil = new MetadataUtil();
    }
    
    /**
     * Method used to load the metadata from the endpoint. First
     * soap 1.2 is used, then 1.1. If both attempts fail, the
     * client will try again adding "/mex" to the address.
     * <P>
     * If any wsdl or schema import elements are found with
     * empty location attributes, these attributes are removed.
     * In the case of data returned to JAX-WS through
     * ServiceDescriptorImpl, these attributes are added back
     * in with appropriate location information.
     *
     * @see com.sun.xml.ws.mex.client.ServiceDescriptorImpl
     * @param address The address used to query for Metadata
     * @return The metadata object, or null if no metadata could
     *     be obtained from the service
     */
    public Metadata retrieveMetadata(@NotNull final String address) {
        for (String suffix : suffixes) {
            final String newAddress = address.concat(suffix);
            for (Protocol p : Protocol.values()) {
                InputStream responseStream = null;
                try {
                    responseStream = mexUtil.getMetadata(newAddress, p);
                    return createMetadata(responseStream);
                } catch (IOException e) {
                    logger.log(ERROR_LOG_LEVEL,
                        MessagesMessages.MEX_0006_RETRIEVING_MDATA_FAILURE(
                            p, newAddress));
                    continue;
                } catch (Exception e) {
                    logger.log(Level.WARNING,
                        MessagesMessages.MEX_0008_PARSING_MDATA_FAILURE(
                            p, newAddress));
                    continue;
                }
            }
        }
        logger.log(ERROR_LOG_LEVEL,
            MessagesMessages.MEX_0007_RETURNING_NULL_MDATA());
        return null;
    }
    
    /**
     * Currently only supports Get requests (not Get Metadata),
     * so we only need the reference's address. Any metadata
     * about the request is ignored.
     *
     * @see #retrieveMetadata(String)
     */
    public Metadata retrieveMetadata(
        @NotNull final MetadataReference reference) {
        
        final List nodes = reference.getAny();
        for (Object o : nodes) {
            final Node node = (Node) o;
            if (node.getLocalName().equals("Address")) {
                final String address = node.getFirstChild().getNodeValue();
                return retrieveMetadata(address);
            }
        }
        return null;
    }
    
    /**
     * Used to retrieve the service and port names and port addresses
     * from metadata. 
     *
     * @see com.sun.xml.ws.mex.client.PortInfo
     * @return A list of PortInfo objects
     */
    public List<PortInfo> getServiceInformation(@NotNull final Metadata data) {
        List<PortInfo> portInfos = new ArrayList<PortInfo>();
        for (MetadataSection section : data.getMetadataSection()) {
            if (section.getDialect().equals(WSDL_DIALECT)) {
                if (section.getAny() != null) {
                    getServiceInformationFromNode(portInfos, section.getAny());
                }
                if (section.getMetadataReference() != null) {
                    final Metadata newMetadata =
                        retrieveMetadata(section.getMetadataReference());
                    List<PortInfo> newPortInfos = getServiceInformation(newMetadata);
                    portInfos.addAll(newPortInfos);
                }
                if (section.getLocation() != null) {
                    final Metadata newMetadata =
                        retrieveMetadata(section.getLocation());
                    List<PortInfo> newPortInfos = getServiceInformation(newMetadata);
                    portInfos.addAll(newPortInfos);
                }
            }
        }
        return portInfos;
    }

    private void getServiceInformationFromNode(List<PortInfo> portInfos, final Object node) {
        
        final Node wsdlNode = (Node) node;
        final String namespace = getAttributeValue(wsdlNode, "targetNamespace");
        final NodeList nodes = wsdlNode.getChildNodes();
        for (int i=0; i<nodes.getLength(); i++) {
            final Node serviceNode = nodes.item(i);
            if (serviceNode.getLocalName() != null &&
                serviceNode.getLocalName().equals("service")) {
                
                final Node nameAtt = serviceNode.getAttributes().getNamedItem("name");
                final QName serviceName = new QName(namespace,
                    nameAtt.getNodeValue());
                final NodeList portNodes = serviceNode.getChildNodes();
                for (int j=0; j<portNodes.getLength(); j++) {
                    final Node portNode = portNodes.item(j);
                    if (portNode.getLocalName() != null &&
                        portNode.getLocalName().equals("port")) {
                        
                        final QName portName = new QName(namespace,
                            getAttributeValue(portNode, "name"));
                        final String address = getPortAddress(portNode);
                        portInfos.add(new PortInfo(serviceName,
                            portName, address));
                    }
                }
            }
        }
    }
    
    /*
     * Create Metadata object from output stream. Need to remove
     * the metadata from soap wrapper. If the metadata contains
     * metadata refernces or HTTP GET location elements, these
     * are dereferenced later.
     */
    private Metadata createMetadata(final InputStream stream)
        throws XMLStreamException, JAXBException {
        
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader =
            factory.createXMLStreamReader(stream);
        int state = 0;
        do {
            state = reader.next();
        } while (state != reader.START_ELEMENT ||
            !reader.getLocalName().equals("Metadata"));
        
        final Unmarshaller uMarhaller = jaxbContext.createUnmarshaller();
        final Metadata mData = (Metadata) uMarhaller.unmarshal(reader);
        cleanData(mData);
        return mData;
    }

    /*
     * Get the value of an attribute from a given node.
     */
    private String getAttributeValue(final Node node, final String attName) {
        return node.getAttributes().getNamedItem(attName).getNodeValue();
    }

    /*
     * Get the port address from a port node. Returns null
     * if there is not one (which would be an error).
     */
    private String getPortAddress(final Node portNode) {
        final NodeList portDetails = portNode.getChildNodes();
        for (int i=0; i<portDetails.getLength(); i++){
            final Node addressNode = portDetails.item(i);
            if (addressNode.getLocalName() != null &&
                addressNode.getLocalName().equals("address")) {
                
                return getAttributeValue(addressNode, "location");
            }
        }
        logger.warning(
            MessagesMessages.MEX_0009_ADDRESS_NOT_FOUND_FOR_PORT(portNode));
        return null;
    }

    /*
     * This is a workaround for a bug in some indigo wsdls
     * that are being returned with schema or wsdl imports
     * containing empty location attributes.
     *
     * If getAny() returns null, the metadata section contains
     * a metadata reference or location rather than the wsdl.
     */
    private void cleanData(final Metadata mData) {
        for (MetadataSection section : mData.getMetadataSection()) {
            if (section.getDialect().equals(WSDL_DIALECT) &&
                section.getAny() != null) {
                cleanWSDLNode((Node) section.getAny());
            }
            else if(section.getDialect().equals(SCHEMA_DIALECT) &&
                section.getAny() != null){
                cleanSchemaNode((Node) section.getAny());
            }
        }
    }

    /*
     * This should be passed the top level wsdl:definitions node.
     */
    private void cleanWSDLNode(final Node wsdlNode) {
        final NodeList nodes = wsdlNode.getChildNodes();
        for (int i=0; i<nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            if (node.getLocalName() != null) {
                if (node.getLocalName().equals("types")) {
                    final NodeList schemaNodes = node.getChildNodes();
                    for (int j=0; j<schemaNodes.getLength(); j++) {
                        final Node schemaNode = schemaNodes.item(j);
                        if (schemaNode.getLocalName() != null &&
                            schemaNode.getLocalName().equals("schema")) {
                            
                            cleanSchemaNode(schemaNode);
                        }
                    }
                } else if (node.getLocalName().equals("import")) {
                    cleanImport(node);
                }
            }
        }
    }
    
    private void cleanSchemaNode(final Node schemaNode) {
        final NodeList children = schemaNode.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            final Node importNode = children.item(i);
            if (importNode.getLocalName() != null &&
                importNode.getLocalName().equals("import")) {
                cleanImport(importNode);            }
        }
    }

    /*
     * Takes the import node itself and removes any empty
     * schemaLocation or location attributes. There will
     * only be one or the other, so the method returns if
     * it finds a schema location.
     */
    private void cleanImport(final Node importNode) {
        final NamedNodeMap atts = importNode.getAttributes();
        Node location = atts.getNamedItem("schemaLocation");
        if (location != null &&
            location.getNodeValue().equals("")) {
            atts.removeNamedItem("schemaLocation");
            return;
        }
        location = atts.getNamedItem("location");
        if (location != null &&
            location.getNodeValue().equals("")) {
            atts.removeNamedItem("location");
        }
    }
    
}
