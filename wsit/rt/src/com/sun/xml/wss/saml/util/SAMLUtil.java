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


package com.sun.xml.wss.saml.util;

import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
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
import javax.xml.bind.JAXBException;

public class SAMLUtil {
    private static Logger logger = Logger.getLogger(LogDomainConstants.SAML_API_DOMAIN,
            LogDomainConstants.SAML_API_DOMAIN_BUNDLE);
    
    
    public static Element locateSamlAssertion(String assertionId,Document soapDocument)
    throws XWSSecurityException {
        
        //System.out.println("\n\n--------SOAP DOCUMENT : " + soapDocument + "--------\n\n");
        
        NodeList nodeList = null;
        
        try {
            nodeList = soapDocument.getElementsByTagNameNS(
                    MessageConstants.SAML_v1_0_NS, MessageConstants.SAML_ASSERTION_LNAME);
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        int nodeListLength = nodeList.getLength();
        if (nodeListLength == 0) {
            // Log Message
            if(logger.isLoggable(Level.SEVERE)){
                logger.log(Level.SEVERE,"WSS001.SAML_ASSERTION_NOT_FOUND",new Object[] {assertionId});
            }
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
            if (aId.equals(assertionId)) {
                //return  XMLUtil.convertToSoapElement(soapDocument, assertion);
                return assertion;
            }
        }
        if(logger.isLoggable(Level.SEVERE)){
            logger.log(Level.SEVERE,"WSS001.SAML_ASSERTION_NOT_FOUND",new Object[] {assertionId});
        }
        throw SecurableSoapMessage.newSOAPFaultException(
                MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                "Referenced Security Token could not be retrieved",
                null);
        //throw new XWSSecurityException("Could not locate SAML assertion with AssertionId:" + assertionId);
    }
    
    public static Element toElement(Node doc, Object element) throws XWSSecurityException{
        
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
                throw new XWSSecurityException("Unable to create Document : " + ex.getMessage());
            }
            result = new DOMResult(document);
        }
        
        try {
            JAXBContext jc = null;
            
            if ( System.getProperty("com.sun.xml.wss.saml.binding.jaxb") == null ) {
                jc = JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb20");
            } else {
                jc = JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb10");
            }
            Marshaller m = jc.createMarshaller();
            
            m.marshal(element, result);
            
        } catch (Exception ex) {
            throw new XWSSecurityException("Not able to Marshal " + element.getClass().getName() + 
                ", got exception: " + ex.getMessage());
        }
        
        if ( doc != null) {
            //return ((Document)doc).getDocumentElement();
            
            
            if (doc.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element)((Element)doc).getElementsByTagNameNS("urn:oasis:names:tc:SAML:1.0:assertion", "Assertion").item(0);
                return el;
            } else {
                Element el = (Element)((Document)doc).getElementsByTagNameNS("urn:oasis:names:tc:SAML:1.0:assertion", "Assertion").item(0);
                return el;
            }
            
        } else {
            Element el = (Element)document.getElementsByTagNameNS("urn:oasis:names:tc:SAML:1.0:assertion", "Assertion").item(0);
            return el;
        }
        
    }
}
