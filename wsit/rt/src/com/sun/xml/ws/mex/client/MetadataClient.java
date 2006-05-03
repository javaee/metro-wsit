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
import static com.sun.xml.ws.mex.MetadataConstants.ERROR_LOG_LEVEL;
import static com.sun.xml.ws.mex.MetadataConstants.WSDL_DIALECT;
import com.sun.xml.ws.mex.client.schema.Metadata;
import com.sun.xml.ws.mex.client.schema.MetadataSection;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
 * <p>
 * TODO: once we finalize any of this, need to
 * enhance the error handling.
 */
public class MetadataClient {
    
    enum Protocol { SOAP_1_2, SOAP_1_1 };
    
    private String [] suffixes = { "" , "/mex" };
    private WSTransferUtil wxfUtil;
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
        wxfUtil = new WSTransferUtil();
    }
    
    /**
     * Method used to load the metadata from the endpoint. First
     * soap 1.2 is used, then 1.1. If both attempts fail, the
     * clienr will try again adding "/mex" to the address.
     *
     * @param address The address used to query for Metadata
     * @return The metadata object
     */
    public Metadata retrieveMetadata(@NotNull String address) {
        for (String suffix : suffixes) {
            String newAddress = address.concat(suffix);
            for (Protocol p : Protocol.values()) {
                InputStream responseStream = null;
                try {
                    responseStream = wxfUtil.getMetadata(address, p);
                } catch (Exception e) {
                    logger.log(ERROR_LOG_LEVEL,
                        "Exception retrieving data with protocol" + p +
                        ", address " + address,
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
                return getServiceInformation(section.getAny().get(0));
            }
        }
        return null;
    }

    private List<PortInfo> getServiceInformation(Object o) {
        List<PortInfo> portInfos = new ArrayList<PortInfo>();
        Node wsdlNode = (Node) o;
        String ns = getAttributeValue(wsdlNode, "targetNamespace");
        NodeList nodes = wsdlNode.getChildNodes();
        for (int i=0; i<nodes.getLength(); i++) {
            Node serviceNode = nodes.item(i);
            if (serviceNode.getLocalName() != null &&
                serviceNode.getLocalName().equalsIgnoreCase("service")) {
                
                Node nameAtt = wsdlNode.getAttributes().getNamedItem("name");
                QName serviceName = new QName(ns, nameAtt.getNodeValue());
                NodeList portNodes = serviceNode.getChildNodes();
                for (int j=0; j<portNodes.getLength(); j++) {
                    Node portNode = portNodes.item(j);
                    if (portNode.getLocalName() != null &&
                        portNode.getLocalName().equalsIgnoreCase("port")) {
                        
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
     * the metadata from soap wrapper.
     */
    private Metadata createMetadata(InputStream stream) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader =
            factory.createXMLStreamReader(stream);
        int state = 0;
        do {
            state = reader.next();
        } while (state != reader.START_ELEMENT ||
            !reader.getLocalName().equalsIgnoreCase("metadata"));
        
        Unmarshaller uMarhaller = jaxbContext.createUnmarshaller();
        Metadata mData = (Metadata) uMarhaller.unmarshal(reader);
        //cleanData(mData);
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
                addressNode.getLocalName().equalsIgnoreCase("address")) {
                
                return getAttributeValue(addressNode, "location");
            }
        }
        logger.warning("No address node was found for the port " +
            portNode);
        return null;
    }

    /*
     * This is a workaround for a bug in some indigo wsdls where
     * there are being returned with schema imports containing
     * an empty schemaLocation attribute.
     */
    private void cleanData(Metadata md) {
        for (MetadataSection section : md.getMetadataSection()) {
            if (section.getDialect().equals(WSDL_DIALECT)) {
                cleanWSDLNode((Node) section.getAny().get(0));
            }
        }
    }

    // remove this when cleanData() goes away
    private void cleanWSDLNode(final Node wsdlNode) {
        NodeList nodes = wsdlNode.getChildNodes();
        for (int i=0; i<nodes.getLength(); i++) {
            Node child = nodes.item(i);
            if (child.getLocalName() != null &&
                child.getLocalName().equalsIgnoreCase("types")) {
                
                // work in progress
            }
        }
    }
    
}
