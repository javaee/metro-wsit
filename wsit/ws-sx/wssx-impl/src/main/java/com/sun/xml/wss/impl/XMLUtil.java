/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.wss.impl;

import org.apache.xml.security.exceptions.Base64DecodingException;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.cert.X509Certificate;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.keyvalues.DSAKeyValue;
import org.apache.xml.security.keys.content.keyvalues.RSAKeyValue;
import com.sun.xml.wss.impl.misc.Base64;
//import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.xml.wss.*;
import com.sun.xml.wss.logging.LogStringsMessages;
import java.net.MalformedURLException;

public class XMLUtil {
    
    /**
     * This is a custom XML handler to load the dtds from the classpath
     * This should be used by all the xml parsing document builders to
     * set the default entity resolvers. This will avoid to have the
     * dtds having specified in a fixed directory  that will get replaced
     * during installation
     * This will need to specify the dtds as follows
     * jar://com/sun/identity/sm/sms.dtd
     * Bundle all the dtds along with the jar files and
     */
    
   protected static final Logger logger =  Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
        
        
    static class XMLHandler extends DefaultHandler {
        
        /**
         * This method reads the resource from a reader
         */
        String read(Reader aReader) {
            StringBuffer sb = new StringBuffer();
            
            try {
                BufferedReader bReader = new BufferedReader(aReader);
                char[] data = new char[2048];
                int count = 0;
                
                while ((count = bReader.read(data)) != -1) {
                    sb.append(data, 0, count);
                }
                
                //while loop
                bReader.close();
                aReader.close();
            } catch (IOException e) {
            }
            
            //try/catch
            return sb.toString();
        }
        
        /**
         * This method reads the resource from the classloader which load this class
         */
        String read(String fileName) {
            return read(fileName, XMLUtil.class);
        }
        
        /**
         * Reads the resource from a class loader.
         *
         * @param fileName resource to be read.
         * @param cl class which delegates the classloader functionality.
         * @return resource value.
         */
        String read(String fileName, Class cl) {
            String data = "";
            
           try {
                InputStream in = cl.getResourceAsStream(fileName);
                
                //may be absoulte file path is given
                if (in == null) {
                    try {
                        //works well if the user has given absolute path
                        in = new FileInputStream(fileName);
                    } catch (FileNotFoundException e) {
                        //works well if the user has given the relative path to the
                        //location of class file
                        String directoryURL = cl.getProtectionDomain()
                        .getCodeSource().getLocation()
                        .toString();
                        String fileURL = directoryURL + fileName;
                        URL url = new URL(fileURL);
                        in = url.openStream();
                    }
                }
                
                //if
                data = read(new InputStreamReader(in));
                in.close();
            }catch(MalformedURLException e) {
                //ignoring this as well
            } catch(IOException e) {
                //ignore
            } 
            
            //try/catch
            return data;
        }
        public InputSource resolveEntity(String aPublicID, String aSystemID) {
            String sysid = aSystemID.trim();
            
            if (sysid.toLowerCase().startsWith("jar://")) {
                String dtdname = sysid.substring(5);
                String dtdValue = read(dtdname).trim();
                
                return new InputSource(new StringReader(dtdValue));
            }
            
            return null;
        }
    }
    
    
    protected static SOAPFactory soapFactory;
    static {
        try {
            soapFactory = SOAPFactory.newInstance();
        } catch (SOAPException e) {
            
        }
    }
    
    
    
    private static boolean validating = false;
    
    /**
     * convertToSoapElement
     *
     * @param doc the Owner Soap Document of the SOAPElement to be returned
     * @param elem the DOM Element to be converted to SOAPElement
     * @return a SOAPElement whose parent node is null
     * @throws DOMException
     */
    public static SOAPElement convertToSoapElement(Document doc, Element elem)
    throws DOMException, ClassCastException {
        if (elem instanceof SOAPElement)
            return (SOAPElement) elem;
        return (SOAPElement) doc.importNode(elem, true);
    }
    
    /**
     * This method searches children of Element element for element with tagName
     * and namespaceURI nsName. It searchs one level down only.
     * @param element The root element
     * @param nsName NamespaceURI
     * @param tagName A String representing the name of the tag to be searched
     *                        for.
     * @return A List of elements that meet the criterial.
     */
    @SuppressWarnings("unchecked")
    public static List getElementsByTagNameNS1(Element element, String nsName,
    String tagName) {
        List list = new ArrayList();
        if (element != null) {
            NodeList nl = element.getChildNodes();
            int length = nl.getLength();
            Node child = null;
            String childName;
            String childNS;
            
            for (int i = 0; i < length; i++) {
                child = nl.item(i);
                childName = child.getLocalName();
                childNS = child.getNamespaceURI();
                
                if ((childName != null) && (childName.equals(tagName)) &&
                (childNS != null) && (childNS.equals(nsName))) {
                    list.add(child);
                }
            }
        }
        
        return list;
    }
    
    public static String getFullTextFromChildren(Element element) {
        if (element == null) {
            return null;
        }
        
        StringBuffer sb = new StringBuffer(1000);
        NodeList nl = element.getChildNodes();
        Node child = null;
        int length = nl.getLength();
        
        for (int i = 0; i < length; i++) {
            child = nl.item(i);
            
            if (child.getNodeType() == Node.TEXT_NODE) {
                sb.append(child.getNodeValue());
            }
        }
        
        return sb.toString().trim();
    }
    
    public static boolean inEncryptionNS(SOAPElement element) {
        return element.getNamespaceURI().equals(MessageConstants.XENC_NS);
    }
    
    public static boolean inSamlNSv1_0(SOAPElement element) {
        return element.getNamespaceURI().equals(MessageConstants.SAML_v1_0_NS);
    }
    
    public static boolean inSamlNSv2_0(SOAPElement element) {
        return element.getNamespaceURI().equals(MessageConstants.SAML_v2_0_NS);
    }
    
    public static boolean inSamlNSv1_1(SOAPElement element) {
        return element.getNamespaceURI().equals(MessageConstants.SAML_v1_1_NS);
    }
    
    public static boolean inSignatureNS(SOAPElement element) {
        return element.getNamespaceURI().equals(MessageConstants.DSIG_NS);
    }
    
    public static boolean inWsseNS(SOAPElement element) {
        return element.getNamespaceURI().equals(MessageConstants.WSSE_NS);
    }

    public static boolean inWsscNS(SOAPElement element) {
        return element.getNamespaceURI().equals(MessageConstants.WSSC_NS);
    }
    
    public static boolean inWsse11NS(SOAPElement element){
        return element.getNamespaceURI().equals(MessageConstants.WSSE11_NS);
    }

    public static boolean inWSS11_NS(SOAPElement element) {
        return element.getNamespaceURI().equals(MessageConstants.WSS11_SPEC_NS);
    }
        
            
    public static boolean inWsuNS(SOAPElement element) {
        return element.getNamespaceURI().equals(MessageConstants.WSU_NS);
    }
    
    public static String resolveXPath(Node element) throws Exception {
        if (element.getOwnerDocument() == null) {
            logger.log(Level.SEVERE,LogStringsMessages.WSS_0424_NULL_OWNER_DOCUMENT_ELEMENT());
            throw new Exception(
            "Element does not have an owner document");
        }
        StringBuffer xpath = new StringBuffer();
        String prefix = element.getPrefix();
        String lcname = element.getLocalName();
        String lxpath = prefix+":"+lcname;
        xpath.append(lxpath);
        Node parentNode = element.getParentNode();
        while (parentNode != null && parentNode.getNodeType() != Node.DOCUMENT_NODE) {
            prefix = parentNode.getPrefix();
            lcname = parentNode.getLocalName();
            lxpath = prefix+":"+lcname+"/";
            xpath.insert(0, lxpath);
            parentNode = parentNode.getParentNode();
        }
        xpath.insert(0, "./");
        return xpath.toString();
    }
    
    public static Element prependChildElement(
    Element parent,
    Element child,
    boolean addWhitespace,
    Document doc) {
        
        Node firstChild = parent.getFirstChild();
        if (firstChild == null) {
            parent.appendChild(child);
        } else {
            parent.insertBefore(child, firstChild);
        }
        
        if (addWhitespace) {
            Node whitespaceText = doc.createTextNode("\n");
            parent.insertBefore(whitespaceText, child);
        }
        return child;
    }
    
    public static Element prependChildElement(
    Element parent, Element child, Document doc) {
        return prependChildElement(parent, child, true, doc);
    }
    
    /**
     * Print a Node tree recursively.
     * @param node A DOM tree Node
     * @return An xml String representation of the DOM tree.
     */
    public static String print(Node node) {
        if (node == null) {
            return null;
        }
        
        StringBuffer xml = new StringBuffer(100);
        int type = node.getNodeType();
        
        switch (type) {
            // print element with attributes
            case Node.ELEMENT_NODE: {
                xml.append('<');
                xml.append(node.getNodeName());
                
                NamedNodeMap attrs = node.getAttributes();
                int length = attrs.getLength();
                ;
                
                for (int i = 0; i < length; i++) {
                    Attr attr = (Attr) attrs.item(i);
                    xml.append(' ');
                    xml.append(attr.getNodeName());
                    xml.append("=\"");
                    
                    //xml.append(normalize(attr.getNodeValue()));
                    xml.append(attr.getNodeValue());
                    xml.append('"');
                }
                
                xml.append('>');
                
                NodeList children = node.getChildNodes();
                
                if (children != null) {
                    int len = children.getLength();
                    
                    for (int i = 0; i < len; i++) {
                        xml.append(print(children.item(i)));
                    }
                }
                
                break;
            }
            
            // handle entity reference nodes
            case Node.ENTITY_REFERENCE_NODE: {
                NodeList children = node.getChildNodes();
                
                if (children != null) {
                    int len = children.getLength();
                    
                    for (int i = 0; i < len; i++) {
                        xml.append(print(children.item(i)));
                    }
                }
                
                break;
            }
            
            // print cdata sections
            case Node.CDATA_SECTION_NODE: {
                xml.append("<![CDATA[");
                xml.append(node.getNodeValue());
                xml.append("]]>");
                
                break;
            }
            
            // print text
            case Node.TEXT_NODE: {
                //xml.append(normalize(node.getNodeValue()));
                xml.append(node.getNodeValue());
                
                break;
            }
            
            // print processing instruction
            case Node.PROCESSING_INSTRUCTION_NODE: {
                xml.append("<?");
                xml.append(node.getNodeName());
                
                String data = node.getNodeValue();
                
                if ((data != null) && (data.length() > 0)) {
                    xml.append(' ');
                    xml.append(data);
                }
                
                xml.append("?>");
                
                break;
            }
        }
        
        if (type == Node.ELEMENT_NODE) {
            xml.append("</");
            xml.append(node.getNodeName());
            xml.append('>');
        }
        
        return xml.toString();
    }
    
//    public static  Node selectSingleNode(
//    Node contextNode,
//    String xpath,
//    Element nsContext)
//    throws XWSSecurityException {
//        
//        try {
//            return XPathAPI.selectSingleNode(contextNode, xpath, nsContext);
//        } catch (TransformerException e) {
//            logger.log(Level.SEVERE, "WSS0425.unableto.resolve.xpath",e);
//            throw new XWSSecurityException("Unable to resolve XPath", e);
//        }
//    }
    
    public static void setWsuIdAttr(Element element, String wsuId) {
        element.setAttributeNS(
        MessageConstants.NAMESPACES_NS,
        "xmlns:" + MessageConstants.WSU_PREFIX,
        MessageConstants.WSU_NS);
        element.setAttributeNS(
        MessageConstants.WSU_NS,
        MessageConstants.WSU_ID_QNAME,
        wsuId);
    }
    
     public static void setIdAttr(Element element, String Id) {
        element.setAttribute("Id", Id);
        element.setIdAttribute("Id", true);        
    }
    
    /**
     * Converts the XML document from an input stream  to DOM Document format.
     *
     * @param is is the InputStream that contains XML document
     * @return Document is the DOM object obtained by parsing the input stream.
     *         Returns null if there are any parser errores.
     */
    public static Document toDOMDocument(InputStream is) {
        /* Constructing a DocumentBuilderFactory for every call is less expensive than a
           synchronizing a single instance of the factory and obtaining the builder
         */
        DocumentBuilderFactory dbFactory = null;
        
        try {
            // Assign new debug object
            dbFactory = WSITXMLFactory.createDocumentBuilderFactory(WSITXMLFactory.DISABLE_SECURE_PROCESSING);
            //new com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl();
            dbFactory.setValidating(validating);
            dbFactory.setNamespaceAware(true);
        } catch (Exception e) {
        }
        
        try {
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            
            if (documentBuilder == null) {
                return null;
            }
            
            documentBuilder.setEntityResolver(new XMLHandler());
            //XMLHandler());
            
            return documentBuilder.parse(is);
        } catch (Exception e) {
            // Since there may potentially be several invalid XML documents
            // that are mostly client-side errors, only a warning is logged for
            // efficiency reasons.
            return null;
        }
    }
    
    /**
     * Converts the XML document from a String format to DOM Document format.
     *
     * @param xmlString is the XML document in a String format
     * @return Document is the DOM object obtained by converting the String XML
     *         Returns null if xmlString is null.
     *               Returns null if there are any parser errores.
     */
    public static Document toDOMDocument(String xmlString) {
        if (xmlString == null) {
            return null;
        }
        
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(xmlString.getBytes(
            "UTF-8"));
            
            return toDOMDocument(is);
        } catch (UnsupportedEncodingException uee) {
            return null;
        }
    }
    
    /**
     * Obtains a new instance of a DOM Document object
     * @return a new instance of a DOM Document object
     * @exception Exception if an error occurs while constructing a new
     *                      document
     */
    public static Document newDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = WSITXMLFactory.createDocumentBuilderFactory(WSITXMLFactory.DISABLE_SECURE_PROCESSING);
        dbFactory.setNamespaceAware(true);
        dbFactory.setValidating(validating);
        
        return dbFactory.newDocumentBuilder().newDocument();
    }
    
    /**
     * Checks if a node has a child of ELEMENT type.
     * @param node a node
     * @return true if the node has a child of ELEMENT type
     */
    public static boolean hasElementChild(Node node) {
        NodeList nl = node.getChildNodes();
        Node child = null;
        int length = nl.getLength();
        
        for (int i = 0; i < length; i++) {
            child = nl.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        
        return false;
    }
    
    public static DSAKeyValue getDSAKeyValue(
    Document doc, X509Certificate cert) throws XWSSecurityException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            DSAPublicKeySpec dsaPkSpec =
            (DSAPublicKeySpec) keyFactory.getKeySpec(
            cert.getPublicKey(), DSAPublicKeySpec.class);
            return
            new DSAKeyValue(
            doc,
            dsaPkSpec.getP(),
            dsaPkSpec.getQ(),
            dsaPkSpec.getG(),
            dsaPkSpec.getY());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE,LogStringsMessages.WSS_0426_FAILED_DSA_KEY_VALUE(), e);
            throw new XWSSecurityException(e);
        }
    }
    
    public static RSAKeyValue getRSAKeyValue(
    Document doc, X509Certificate cert) throws XWSSecurityException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec rsaPkSpec =
            (RSAPublicKeySpec) keyFactory.getKeySpec(
            cert.getPublicKey(), RSAPublicKeySpec.class);
            return
            new RSAKeyValue(
            doc,
            rsaPkSpec.getModulus(),
            rsaPkSpec.getPublicExponent()
            );
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_0293_FAILED_RSA_KEY_VALUE(), e);
            throw new XWSSecurityException(e);
        }
    }
    
    // The X509Certificate element, which contains
    // a base64-encoded [X509v3] certificate is added into the X509Data
    public static X509Data getX509Data(
    Document doc, X509Certificate cert) throws XWSSecurityException {
        try {
            X509Data x509Data = new X509Data(doc);
            x509Data.addCertificate(cert);
            return x509Data;
        } catch (Exception e) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_0294_FAILED_X_509_DATA(), e);
            throw new XWSSecurityException(e);
        }
    }
    
    /**
     * Looks up elements with wsu:Id or Id in xenc or dsig namespace.
     *
     * @param doc
     * @param id
     *
     * @return element
     *
     * @throws TransformerException
     */
//    public static Element getElementById(
//    Document doc,
//    String id)
//    throws TransformerException {
//
//        Element  selement = doc.getElementById(id);
//        if (selement != null) {
//            return selement;
//        }
//
//        Document sdoc = null;
//        if (doc instanceof SOAPDocumentImpl) {
//            sdoc = ((SOAPDocumentImpl)doc).getSOAPPart();
//        }
//        if (sdoc == null) {
//            sdoc = doc;
//        }
//                                                                                                                     
//        Element nscontext = XMLUtils.createDSctx(sdoc,
//            "wsu",
//            MessageConstants.WSU_NS);
//        Element element =
//            (Element) XPathAPI.selectSingleNode(
//                sdoc, "//*[@wsu:Id='" + id + "']", nscontext);
//        
//        if (element == null) {
//            NodeList elems = XPathAPI.selectNodeList(
//            sdoc,
//            "//*[@Id='" + id + "']",
//            nscontext);
//            for (int i=0; i < elems.getLength(); i++) {
//                Element elem = (Element)elems.item(i);
//                String namespace = elem.getNamespaceURI();
//                if (namespace.equals(MessageConstants.DSIG_NS) ||
//                    namespace.equals(MessageConstants.XENC_NS) ||
//                    namespace.equals(MessageConstants.WSSE11_NS)) {
//                    element = elem;
//                    break;
//                }
//            }
//        }
//
//        // look for SAML AssertionID
//        if (element == null) {
//                                
//            /*NodeList assertions =
//                sdoc.getElementsByTagNameNS(MessageConstants.SAML_v1_0_NS,
//                    MessageConstants.SAML_ASSERTION_LNAME);*/
//            NodeList assertions = sdoc.getElementsByTagName(MessageConstants.SAML_ASSERTION_LNAME);
//            int len = assertions.getLength();
//            if (len > 0) {
//                for (int i=0; i < len; i++) {
//                    SOAPElement elem = (SOAPElement)assertions.item(i);
//                    String assertionId = elem.getAttribute(MessageConstants.SAML_ASSERTIONID_LNAME);
//                    String saml20Id = elem.getAttribute(MessageConstants.SAML_ID_LNAME);
//                    if (id.equals(assertionId) || id.equals(saml20Id)) {
//                        element = elem;
//                        break;
//                    }
//                }
//            }
//        }
//
//        return element;
//    }

    
    public static String convertToXpath(String qname) {
        QName name = QName.valueOf(qname);
        if ("".equals(name.getNamespaceURI())) {
            return "//" + name.getLocalPart();
        } else {
            return "//*[local-name()='"
            + name.getLocalPart()
            + "' and namespace-uri()='"
            + name.getNamespaceURI()
            + "']";
        }
    }
    
    public static byte[] getDecodedBase64EncodedData(String encodedData)throws XWSSecurityException {
        try {
            return Base64.decode(encodedData);
        } catch (Base64DecodingException e) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_0427_UNABLETO_DECODE_BASE_64(), e);
            throw new XWSSecurityException(
            "Unable to decode Base64 encoded data",
            e);
        }
    }
    
    public static Document getOwnerDocument(Node node) {
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return (Document) node;
        } else {
            return node.getOwnerDocument();
        }
    }
    
    public static Element getFirstChildElement(Node node) {
        Node child = node.getFirstChild();
        while (child != null && child.getNodeType() != Node.ELEMENT_NODE) {
            child = child.getNextSibling();
        }
        return (Element) child;
    }
    
    public static Element createElement(Document doc, String tag, String nsURI,
        String prefix) {
        String qName = prefix == null ? tag : prefix + ":" + tag;
        return doc.createElementNS(nsURI, qName);
    } 
    
}
