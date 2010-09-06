/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.xml.wss.saml.util;


import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.stax.StreamWriterBufferCreator;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.dsig.WSSPolicyConsumerImpl;
import com.sun.xml.wss.logging.saml.LogStringsMessages;
import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.AssertionType;
import java.text.ParseException;
import java.util.Date;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import com.sun.xml.wss.util.DateUtils;
import java.lang.reflect.Constructor;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import javax.xml.crypto.Data;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import org.w3c.dom.NamedNodeMap;

public class SAMLUtil {
    private static Logger logger = Logger.getLogger(LogDomainConstants.SAML_API_DOMAIN,
            LogDomainConstants.SAML_API_DOMAIN_BUNDLE);   
    
    public static JAXBElement element = null;
    public static Element locateSamlAssertion(String assertionId,Document soapDocument)
    throws XWSSecurityException {
        
        //System.out.println("\n\n--------SOAP DOCUMENT : " + soapDocument + "--------\n\n");
        
        NodeList nodeList = null;
        
//        try {
          nodeList = soapDocument.getElementsByTagNameNS(MessageConstants.SAML_v1_0_NS, MessageConstants.SAML_ASSERTION_LNAME);
          if((nodeList.item(0)) == null ){
              nodeList = soapDocument.getElementsByTagNameNS(MessageConstants.SAML_v2_0_NS,
                    MessageConstants.SAML_ASSERTION_LNAME);
          }
        
        int nodeListLength = nodeList.getLength();
        if (nodeListLength == 0) {
                logger.log(Level.SEVERE,LogStringsMessages.WSS_001_SAML_ASSERTION_NOT_FOUND(assertionId));
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                    "Referenced Security Token could not be retrieved",
                    null);
            //throw new XWSSecurityException(
            //"No SAML Assertion found with  AssertionID:" + assertionId );
        }
        
        for (int i=0; i<nodeListLength; i++) {
            Element assertion = (Element) nodeList.item(i);
            String  aId = assertion.getAttribute(MessageConstants.SAML_ASSERTIONID_LNAME);
            String id = assertion.getAttribute(MessageConstants.SAML_ID_LNAME);
            if (aId.equals(assertionId) || id.equals(assertionId)) {
                //return  XMLUtil.convertToSoapElement(soapDocument, assertion);
                return assertion;
            }
        }
            logger.log(Level.SEVERE,LogStringsMessages.WSS_001_SAML_ASSERTION_NOT_FOUND(assertionId));
        throw SecurableSoapMessage.newSOAPFaultException(
                MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                "Referenced Security Token could not be retrieved",
                null);
        //throw new XWSSecurityException("Could not locate SAML assertion with AssertionId:" + assertionId);
    }
    
    public static Element toElement(Node doc, Object element) throws XWSSecurityException{
        return toElement(doc, element, null);
    }

    public static Element toElement(Node doc, Object element,JAXBContext jcc) throws XWSSecurityException{
        
        DOMResult result = null;
        Document document = null;
        //TODO : If DOC is SUPPLIED then this code is not working
        if ( doc != null) {
            
            result = new DOMResult(doc);
        } else {
            
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                document = builder.newDocument();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_002_FAILED_CREATE_DOCUMENT(), ex);
                throw new XWSSecurityException("Unable to create Document : " + ex.getMessage());
            }
            result = new DOMResult(document);
        }
        
        try {
            JAXBContext jc = jcc;
            if (jc == null) {
                if (System.getProperty("com.sun.xml.wss.saml.binding.jaxb") == null) {
                    if (element instanceof com.sun.xml.wss.saml.assertion.saml20.jaxb20.Assertion) {
                        jc = SAML20JAXBUtil.getJAXBContext();
                    } else {
                        jc = SAMLJAXBUtil.getJAXBContext();
                    }
                } else {
                    jc = SAMLJAXBUtil.getJAXBContext();
                }
            }
            
            Marshaller m = jc.createMarshaller();
            
            if (element == null){
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE,"Element is Null in SAMLUtil.toElement()");
                }
            }
            
            m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new WSSNamespacePrefixMapper());
            m.marshal(element, result);            
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE,LogStringsMessages.WSS_003_FAILEDTO_MARSHAL(), ex);
            throw new XWSSecurityException("Not able to Marshal " + element.getClass().getName() + 
                ", got exception: " + ex.getMessage());
        }
        
        if ( doc != null) {
            //return ((Document)doc).getDocumentElement();
            
            
            if (doc.getNodeType() == Node.ELEMENT_NODE) {
                if (doc.getFirstChild().getNamespaceURI().equals(MessageConstants.SAML_v2_0_NS)){
                    Element el = (Element)((Element)doc).getElementsByTagNameNS(MessageConstants.SAML_v2_0_NS, "Assertion").item(0);
                    return el;
                }else{
                    Element el = (Element)((Element)doc).getElementsByTagNameNS(MessageConstants.SAML_v1_0_NS, "Assertion").item(0);
                    return el;
                }
            } else {
                if (doc.getFirstChild().getNamespaceURI().equals(MessageConstants.SAML_v2_0_NS)){
                    Element el = (Element)((Document)doc).getElementsByTagNameNS(MessageConstants.SAML_v2_0_NS,"Assertion").item(0);
                    return el;
                }else{
                    Element el = (Element)((Document)doc).getElementsByTagNameNS(MessageConstants.SAML_v1_0_NS,"Assertion").item(0);
                    return el;
                }
            }
            
        } else {
            if (document.getFirstChild().getNamespaceURI().equals(MessageConstants.SAML_v2_0_NS)){
                Element el = (Element)document.getElementsByTagNameNS(MessageConstants.SAML_v2_0_NS, "Assertion").item(0);
                return el;            
            }else{
                Element el = (Element)document.getElementsByTagNameNS(MessageConstants.SAML_v1_0_NS, "Assertion").item(0);
                return el;            
            }
        }
    }
    
    public static Element createSAMLAssertion(XMLStreamReader reader) throws XWSSecurityException,XMLStreamException{
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();        
        MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
        StreamWriterBufferCreator bCreator = new StreamWriterBufferCreator(buffer);
        Document doc = null;        
        try{                                
            XMLStreamWriter writer = xof.createXMLStreamWriter(baos);
            XMLStreamWriter writer_tmp = (XMLStreamWriter)bCreator;
            while(!(XMLStreamReader.END_DOCUMENT == reader.getEventType())){
                com.sun.xml.ws.security.opt.impl.util.StreamUtil.writeCurrentEvent(reader, writer_tmp);
                reader.next();
            }
            buffer.writeToXMLStreamWriter(writer);
            writer.close();
            try {
                baos.close();
            } catch (IOException ex) {
                throw new XWSSecurityException("Error occurred while trying to convert SAMLAssertion stream into DOM Element", ex);
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();            
            doc = db.parse(new ByteArrayInputStream(baos.toByteArray()));
            return  doc.getDocumentElement();    
        } catch(XMLStreamException xe){
            throw new XMLStreamException("Error occurred while trying to convert SAMLAssertion stream into DOM Element", xe);
        }catch(Exception xe){
            throw new XWSSecurityException("Error occurred while trying to convert SAMLAssertion stream into DOM Element", xe);
        }
    }

    public static Assertion createSAMLAssertion(XMLStreamReader reader,String SAMLVersion) throws XWSSecurityException {
        JAXBContext jc = null;
        Assertion _assertion = null;

        if(SAMLVersion == null){
            XWSSecurityRuntimeException ex  =new XWSSecurityRuntimeException("SAML Version should be set in the callback handler when using saml assertion reader");
            logger.log(Level.SEVERE, LogStringsMessages.WSS_004_SAML_VERSION_NOT_SET(), ex);
            throw ex;
        }

        try {
            if ("2.0".equals(SAMLVersion)) {
                jc = SAML20JAXBUtil.getJAXBContext();
                javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
                element = (JAXBElement) u.unmarshal(reader, AssertionType.class);
                _assertion = new com.sun.xml.wss.saml.assertion.saml20.jaxb20.Assertion((AssertionType) element.getValue());
            } else {
                jc = SAMLJAXBUtil.getJAXBContext();
                javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
                element = (JAXBElement) u.unmarshal(reader, com.sun.xml.wss.saml.internal.saml11.jaxb20.AssertionType.class);
                _assertion = new com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion((com.sun.xml.wss.saml.internal.saml11.jaxb20.AssertionType) element.getValue());
            }
        } catch (Exception ex) {
            throw new XWSSecurityException("Error occurred while trying to create SAMLAssertion from xml stream reader", ex);
        }

        return _assertion;
    }
    
    public static boolean validateTimeInConditionsStatement(Element samlAssertion) throws XWSSecurityException {
     
        Date _notBefore=null;
        Date  _notOnOrAfter=null;
        
        NodeList nl = samlAssertion.getElementsByTagNameNS(samlAssertion.getNamespaceURI(), "Conditions");
        Node conditionsElement = null;
        if (nl != null && nl.getLength() > 0) {
            conditionsElement = nl.item(0);
        } else {
            //no conditions stmt
            logger.log(Level.INFO, "No Conditions Element found in SAML Assertion");
            return true;
        }
        Element elt = (Element)conditionsElement;
        String eltName = elt.getLocalName();
        if (eltName == null)  {
            throw new XWSSecurityException("Internal Error: LocalName of Conditions Element found Null") ;
        }
        if (!(eltName.equals("Conditions")))  {
            throw new XWSSecurityException("Internal Error: LocalName of Conditions Element found to be :" + eltName) ;
        }
        
        String dt = elt.getAttribute("NotBefore");
        if ((dt != null) && (!dt.equals("")))  {
            try {
                _notBefore = DateUtils.stringToDate(dt);
            } catch (ParseException pe) {
               throw new XWSSecurityException(pe);
            }
                                                                                                                                                             
        }
        dt = elt.getAttribute("NotOnOrAfter");
        if ((dt != null) && (!dt.equals("")))  {
            try {
                _notOnOrAfter = DateUtils.stringToDate(
                            elt.getAttribute("NotOnOrAfter"));
            } catch (ParseException pe) {
               throw new XWSSecurityException(pe);
            }
        }
        
        long someTime = System.currentTimeMillis();
        
        if (_notBefore == null ) {
            if (_notOnOrAfter == null) {
                return true;
            } else {
                if (someTime < _notOnOrAfter.getTime()) {
                    return true;
                }
            }
        } else if (_notOnOrAfter == null ) {
            if (someTime >= _notBefore.getTime()) {
                return true;
            }
        } else if ((someTime >= _notBefore.getTime()) &&
            (someTime < _notOnOrAfter.getTime()))
        {
            return true;
        }
        return false;
    }

    public static boolean verifySignature(Element samlAssertion, PublicKey pubKey)throws XWSSecurityException {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            String id = samlAssertion.getAttribute("ID");
            if (id == null || id.length() < 1){
                id = samlAssertion.getAttribute("AssertionID");
            }
            map.put(id, samlAssertion);
            NodeList nl = samlAssertion.getElementsByTagNameNS(MessageConstants.DSIG_NS, "Signature");

            //verify the signature inside the SAML assertion
            if (nl.getLength() == 0) {
                throw new XWSSecurityException("Unsigned SAML Assertion encountered while verifying the SAML signature");
            }
            Element signElement = (Element) nl.item(0);
            DOMValidateContext validationContext = new DOMValidateContext(pubKey, signElement);
            XMLSignatureFactory signatureFactory = WSSPolicyConsumerImpl.getInstance().getSignatureFactory();

            // unmarshal the XMLSignature
            XMLSignature xmlSignature = signatureFactory.unmarshalXMLSignature(validationContext);
            validationContext.setURIDereferencer(new DSigResolver(map, samlAssertion));
            boolean coreValidity = xmlSignature.validate(validationContext);
            return coreValidity;
        } catch (Exception ex) {
            throw new XWSSecurityException(ex);
        }
    }

    private static class DSigResolver implements URIDereferencer{
        //TODO : Convert DSigResolver to singleton class.
        Element elem = null;
        Map map = null;
        Class<?> _nodeSetClass = null;
        String optNSClassName = "org.jcp.xml.dsig.internal.dom.DOMSubTreeData";
        Constructor _constructor = null;
        Boolean  _false = Boolean.valueOf(false);
        DSigResolver(Map map,Element elem){
            this.elem = elem;
            this.map = map;
            init();
        }

        void init(){
            try{
                _nodeSetClass = Class.forName(optNSClassName);
                _constructor = _nodeSetClass.getConstructor(new Class [] {org.w3c.dom.Node.class,boolean.class});
            }catch(LinkageError le){
                // logger.log (Level.FINE,"Not able load JSR 105 RI specific NodeSetData class ",le);
            }catch(ClassNotFoundException cne){
                // logger.log (Level.FINE,"Not able load JSR 105 RI specific NodeSetData class ",cne);
            }catch(NoSuchMethodException ne){

            }
        }
        public Data dereference(URIReference uriRef, XMLCryptoContext context) throws URIReferenceException {
            try{
                String uri = null;
                uri = uriRef.getURI();
                return dereferenceURI(uri,context);
            }catch(Exception ex){
                // log here
                throw new URIReferenceException(ex);
            }
        }
        Data dereferenceURI(String uri, XMLCryptoContext context) throws URIReferenceException{
            if(uri.charAt(0) == '#'){
                uri =  uri.substring(1,uri.length());
                Element el = elem.getOwnerDocument().getElementById(uri);
                if(el == null){
                    el = (Element)map.get(uri);
                }

                if(_constructor != null){
                    try{
                        return (Data)_constructor.newInstance(new Object[] {el,_false});
                    }catch(Exception ex){
                        // TODO: igonore this ?
                        ex.printStackTrace();
                    }
                }else{
                    final HashSet<Object> nodeSet = new HashSet<Object>();
                    toNodeSet(el,nodeSet);
                    return new NodeSetData(){
                        public Iterator iterator(){
                            return nodeSet.iterator();
                        }
                    };
                }

            }

            return null;
            //throw new URIReferenceException("Resource "+uri+" was not found");
        }

        void toNodeSet(final Node rootNode,final Set<Object> result){
            switch (rootNode.getNodeType()) {
                case Node.ELEMENT_NODE:
                    result.add(rootNode);
                    Element el=(Element)rootNode;
                    if (el.hasAttributes()) {
                        NamedNodeMap nl = ((Element)rootNode).getAttributes();
                        for (int i=0;i<nl.getLength();i++) {
                            result.add(nl.item(i));
                        }
                    }
                    //no return keep working
                case Node.DOCUMENT_NODE:
                    for (Node r=rootNode.getFirstChild();r!=null;r=r.getNextSibling()){
                        if (r.getNodeType()==Node.TEXT_NODE) {
                            result.add(r);
                            while ((r!=null) && (r.getNodeType()==Node.TEXT_NODE)) {
                                r=r.getNextSibling();
                            }
                            if (r==null)
                                return;
                        }
                        toNodeSet(r,result);
                    }
                    return;
                case Node.COMMENT_NODE:
                    return;
                case Node.DOCUMENT_TYPE_NODE:
                    return;
                default:
                    result.add(rootNode);
            }
            return;
        }
    }
}
