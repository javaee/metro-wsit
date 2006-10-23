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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

import com.sun.istack.NotNull;
import com.sun.xml.ws.mex.client.schema.Metadata;
import com.sun.xml.ws.mex.client.schema.MetadataReference;
import com.sun.xml.ws.mex.client.schema.MetadataSection;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.sun.xml.ws.mex.MetadataConstants.ERROR_LOG_LEVEL;
import static com.sun.xml.ws.mex.MetadataConstants.WSDL_DIALECT;

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
    
    private String [] suffixes = { "" , "/mex" };
    private MetadataUtil mexUtil;
    private static final JAXBContext jaxbContext;
    
    private static final Logger logger =
        Logger.getLogger(MetadataClient.class.getName());
    
    static {
        try {
            jaxbContext = JAXBContext.newInstance(
                "com.sun.xml.ws.mex.client.schema");
        } catch (JAXBException jaxbE) {
            throw new WebServiceException(jaxbE);
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
    public Metadata retrieveMetadata(@NotNull String address) {
        for (String suffix : suffixes) {
            String newAddress = address.concat(suffix);
            for (Protocol p : Protocol.values()) {
                InputStream responseStream = null;
                try {
                    responseStream = mexUtil.getMetadata(newAddress, p);
                } catch (Exception e) {
                    logger.log(ERROR_LOG_LEVEL,
                        "Exception retrieving data with protocol" + p +
                        ", address " + newAddress,
                        e);
                    continue;
                }
                try {
                    return createMetadata(responseStream);
                } catch (Exception e) {
                    throw new WebServiceException(e);
                }
            }
        }
        return null;
    }
    
    /**
     * Currently only supports Get requests (not Get Metadata),
     * so we only need the reference's address. Any metadata
     * about the request is ignored.
     *
     * @see #retrieveMetadata(String)
     */
    public Metadata retrieveMetadata(@NotNull MetadataReference reference) {
        List nodes = reference.getAny();
        for (Object o : nodes) {
            Node node = (Node) o;
            if (node.getLocalName().equals("Address")) {
                String address = node.getFirstChild().getNodeValue();
                return retrieveMetadata(address);
            }
        }
        return null;
    }
    
    /**
     * Used to retrieve the service and port names and port addresses
     * from metadata. If there is more than one wsdl section in the metadata,
     * only the first is parsed by this method.
     *
     * @see com.sun.xml.ws.mex.client.PortInfo
     * @return A list of PortInfo objects
     */
    public List<PortInfo> getServiceInformation(@NotNull Metadata data) {
        for (MetadataSection section : data.getMetadataSection()) {
            if (section.getDialect().equals(WSDL_DIALECT)) {
                if (section.getAny() != null) {
                    return getServiceInformation(section.getAny());
                }
                if (section.getMetadataReference() != null) {
                    Metadata newMetadata =
                        retrieveMetadata(section.getMetadataReference());
                    return getServiceInformation(newMetadata);
                }
                if (section.getLocation() != null) {
                    Metadata newMetadata =
                        retrieveMetadata(section.getLocation());
                    return getServiceInformation(newMetadata);
                }
            }
        }
        return null;
    }

    private List<PortInfo> getServiceInformation(Object o) {
        if (o == null) {
            return null;
        }
        List<PortInfo> portInfos = new ArrayList<PortInfo>();
        Node wsdlNode = (Node) o;
        String ns = getAttributeValue(wsdlNode, "targetNamespace");
        NodeList nodes = wsdlNode.getChildNodes();
        for (int i=0; i<nodes.getLength(); i++) {
            Node serviceNode = nodes.item(i);
            if (serviceNode.getLocalName() != null &&
                serviceNode.getLocalName().equals("service")) {
                
                Node nameAtt = wsdlNode.getAttributes().getNamedItem("name");
                QName serviceName = new QName(ns, nameAtt.getNodeValue());
                NodeList portNodes = serviceNode.getChildNodes();
                for (int j=0; j<portNodes.getLength(); j++) {
                    Node portNode = portNodes.item(j);
                    if (portNode.getLocalName() != null &&
                        portNode.getLocalName().equals("port")) {
                        
                        QName portName = new QName(ns,
                            getAttributeValue(portNode, "name"));
                        String address = getPortAddress(portNode);
                        portInfos.add(new PortInfo(serviceName,
                            portName, address));
                    }
                }
            }
        }
        return portInfos;
    }
    
    /*
     * Create Metadata object from output stream. Need to remove
     * the metadata from soap wrapper. If the metadata contains
     * metadata refernces or HTTP GET location elements, these
     * are dereferenced later.
     */
    private Metadata createMetadata(InputStream stream) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader =
            factory.createXMLStreamReader(stream);
        int state = 0;
        do {
            state = reader.next();
        } while (state != reader.START_ELEMENT ||
            !reader.getLocalName().equals("Metadata"));
        
        Unmarshaller uMarhaller = jaxbContext.createUnmarshaller();
        Metadata mData = (Metadata) uMarhaller.unmarshal(reader);
        cleanData(mData);
        return mData;
    }

    /*
     * Get the value of an attribute from a given node.
     */
    private String getAttributeValue(Node node, String attName) {
        Node attNode = node.getAttributes().getNamedItem(attName);
        return attNode.getNodeValue();
    }

    /*
     * Get the port address from a port node. Returns null
     * if there is not one (which would be an error).
     */
    private String getPortAddress(Node portNode) {
        NodeList portDetails = portNode.getChildNodes();
        for (int i=0; i<portDetails.getLength(); i++){
            Node addressNode = portDetails.item(i);
            if (addressNode.getLocalName() != null &&
                addressNode.getLocalName().equals("address")) {
                
                return getAttributeValue(addressNode, "location");
            }
        }
        logger.warning("No address node was found for the port " +
            portNode);
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
    private void cleanData(Metadata md) {
        for (MetadataSection section : md.getMetadataSection()) {
            if (section.getDialect().equals(WSDL_DIALECT) &&
                section.getAny() != null) {
                cleanWSDLNode((Node) section.getAny());
            }
        }
    }

    /*
     * This should be passed the top level wsdl:definitions node.
     */
    private void cleanWSDLNode(final Node wsdlNode) {
        NodeList nodes = wsdlNode.getChildNodes();
        for (int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getLocalName() != null) {
                if (node.getLocalName().equals("types")) {
                    NodeList schemaNodes = node.getChildNodes();
                    for (int j=0; j<schemaNodes.getLength(); j++) {
                        Node schemaNode = schemaNodes.item(j);
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
    
    private void cleanSchemaNode(Node schemaNode) {
        NodeList children = schemaNode.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node importNode = children.item(i);
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
    private void cleanImport(Node importNode) {
        NamedNodeMap atts = importNode.getAttributes();
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
