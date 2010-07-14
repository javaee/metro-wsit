/*
 * $Id: SecurityHeader.java,v 1.4.2.2 2010-07-14 14:05:34 m_potociar Exp $
 */

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

package com.sun.xml.wss.core;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.XWSSecurityException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.TypeInfo;

import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;
import com.sun.xml.wss.impl.misc.SOAPElementExtension;
import com.sun.xml.wss.impl.MessageConstants;

/**
 * @author XWS-Security Development Team
 */
public class SecurityHeader extends SOAPElementExtension implements SOAPElement {
    
    private final SOAPElement delegateHeader;
    private Document ownerDoc;
    
    private static Logger log =
    Logger.getLogger(
    LogDomainConstants.WSS_API_DOMAIN,
    LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /**
     * The child element of security header to be processed next.
     */
    private SOAPElement currentSoapElement;
    
    /**
     * The first child element of the security header.
     */
    private SOAPElement topMostSoapElement;
    
    public SecurityHeader(SOAPElement delegateHeader) {
        this.delegateHeader = delegateHeader;
        this.ownerDoc = delegateHeader.getOwnerDocument();
        topMostSoapElement = getFirstChildElement();
        currentSoapElement = null;
    }
     
    /**
     * Inserts the header block at the top of the security header, i.e,
     * the block becomes the first child element of the security header.
     * This method will be used on the sender side.
     */
    public void insertHeaderBlock(SecurityHeaderBlock block)
    throws XWSSecurityException {
        SOAPElement elementToInsert = block.getAsSoapElement();
        try {
            if (elementToInsert.getOwnerDocument() != ownerDoc) {
                elementToInsert =
                (SOAPElement) ownerDoc.importNode(
                elementToInsert, true);
            }
            
            updateTopMostSoapElement();
            
            insertBefore(elementToInsert, topMostSoapElement);

        } catch (DOMException e) {
            log.log(Level.SEVERE, "WSS0376.error.inserting.header", e.getMessage());
            throw new XWSSecurityException(e);
        }
        topMostSoapElement = elementToInsert;
    }
    
    public void insertBefore(SecurityHeaderBlock block,Node elem) throws XWSSecurityException {
        SOAPElement elementToInsert = block.getAsSoapElement();
        try {
            if (elementToInsert.getOwnerDocument() != ownerDoc) {
                elementToInsert =
                (SOAPElement) ownerDoc.importNode(
                elementToInsert, true);
            }
        } catch (DOMException e) {
            log.log(Level.SEVERE, "WSS0376.error.inserting.header", e.getMessage());
            throw new XWSSecurityException(e);
        }
        insertBefore(elementToInsert,elem);
    }
    
    
    public void appendChild(SecurityHeaderBlock block) throws XWSSecurityException {
        SOAPElement elementToInsert = block.getAsSoapElement();
        try {
            if (elementToInsert.getOwnerDocument() != ownerDoc) {
                elementToInsert =
                (SOAPElement) ownerDoc.importNode(
                elementToInsert, true);
            }
            appendChild(elementToInsert);
            
        } catch (DOMException e) {
            log.log(Level.SEVERE, "WSS0376.error.inserting.header", e.getMessage());
            throw new XWSSecurityException(e);
        }
    }
    
    public void insertHeaderBlockElement(SOAPElement blockElement)
    throws XWSSecurityException {
        try {
            if (blockElement.getOwnerDocument() != ownerDoc) {
                blockElement =
                (SOAPElement) ownerDoc.importNode(blockElement, true);
            }
            updateTopMostSoapElement();
            
            insertBefore(blockElement, topMostSoapElement);
            
        } catch (DOMException e) {
            log.log(Level.SEVERE, "WSS0376.error.inserting.header", e.getMessage());
            throw new XWSSecurityException(e);
        }
        topMostSoapElement = blockElement;
    }
    
    /**
     * Get the header block to be processed next.
     * This method will be used on the receiver side.
     */
    public SecurityHeaderBlock getCurrentHeaderBlock(Class implType)
    throws XWSSecurityException {
        if (null == currentSoapElement)
            currentSoapElement = getFirstChildElement();
        else {
            Node nextChild = currentSoapElement.getNextSibling();
            while ((null != nextChild) && (nextChild.getNodeType() != Node.ELEMENT_NODE))
                nextChild = nextChild.getNextSibling();
            currentSoapElement = (SOAPElement) nextChild;
        }
        return SecurityHeaderBlockImpl.fromSoapElement(
        currentSoapElement, implType);
    }
    
    public SOAPElement getCurrentHeaderBlockElement() {
        if (null == currentSoapElement)
            currentSoapElement = getFirstChildElement();
        else {
            Node nextChild = currentSoapElement.getNextSibling();
            while ((null != nextChild) && (nextChild.getNodeType() != Node.ELEMENT_NODE))
                nextChild = nextChild.getNextSibling();
            currentSoapElement = (SOAPElement) nextChild;
        }
        return currentSoapElement;
    }
    
    public void setCurrentHeaderElement(SOAPElement currentElement)
    throws XWSSecurityException {
        if (currentElement != null &&
        currentElement.getParentNode() != delegateHeader) {
            log.log(Level.SEVERE, "WSS0396.notchild.securityHeader", 
                    new Object[] {currentElement.toString()} );
            throw new XWSSecurityException(
            "Element set is not a child of SecurityHeader");
        }
        currentSoapElement = currentElement;
    }
    
    public SOAPElement getCurrentHeaderElement() {
        return currentSoapElement;
    }
    
    // TODO : Obsolete method -
    // To be removed once we get rid of the OldEncryptFilter.
    public void updateTopMostSoapElement() {
        topMostSoapElement = getNextSiblingOfTimestamp();

    }
    
    public SOAPElement getFirstChildElement() {
        Iterator eachChild = getChildElements();
        javax.xml.soap.Node node = null;

        if (eachChild.hasNext()) {
            node = (javax.xml.soap.Node) eachChild.next();
        }else {
            return null;
        }

        while ((node.getNodeType() != Node.ELEMENT_NODE) && eachChild.hasNext()) {
            node = (javax.xml.soap.Node) eachChild.next();
        }
        if ((null != node) /*&& (node.getNodeType() == Node.ELEMENT_NODE)*/)
            return (SOAPElement) node;
        else
            return null;
    }
    
    public SOAPElement getNextSiblingOfTimestamp(){
        SOAPElement firstElement = getFirstChildElement();
        Node temp;
        if(firstElement != null && MessageConstants.TIMESTAMP_LNAME.equals(firstElement.getLocalName())){
            temp = firstElement.getNextSibling();
            if(temp == null)
                return null;
            while(temp.getNodeType() != Node.ELEMENT_NODE && temp.getNextSibling() != null){
                temp = (javax.xml.soap.Node)temp.getNextSibling();
            }
            if(null != temp){
                while((temp != null) && (MessageConstants.SIGNATURE_CONFIRMATION_LNAME.equals(temp.getLocalName()))){
                    temp = temp.getNextSibling();
                    if(temp == null)
                        return null;
                    while(temp.getNodeType() != Node.ELEMENT_NODE && temp.getNextSibling() != null){
                        temp = (javax.xml.soap.Node)temp.getNextSibling();
                    }
                }
                if(temp == null)
                    return null;
                return (SOAPElement)temp;
            } else
                return null;
        } else{
            return firstElement;
        }
    }
    
    // This method was introduced to use a work-around for the
    // selectSingleNode() problem.
    public SOAPElement getAsSoapElement() {
        return delegateHeader;
    }
    
    // Mimic SOAPHeaderElement (almost)
    public void setRole(String roleURI) {
        throw new UnsupportedOperationException();
    }
    public String getRole() {
        throw new UnsupportedOperationException();
    }
    public void setMustUnderstand(boolean mustUnderstand) {
        throw new UnsupportedOperationException();
    }
    public boolean isMustUnderstand() {
        throw new UnsupportedOperationException();
    }
    
    // All of the following methods are generated delegate methods...
    public SOAPElement addAttribute(Name arg0, String arg1)
    throws SOAPException {
        return delegateHeader.addAttribute(arg0, arg1);
    }
    
    public SOAPElement addChildElement(String arg0) throws SOAPException {
        return delegateHeader.addChildElement(arg0);
    }
    
    public SOAPElement addChildElement(String arg0, String arg1)
    throws SOAPException {
        return delegateHeader.addChildElement(arg0, arg1);
    }
    
    public SOAPElement addChildElement(String arg0, String arg1, String arg2)
    throws SOAPException {
        return delegateHeader.addChildElement(arg0, arg1, arg2);
    }
    
    public SOAPElement addChildElement(Name arg0) throws SOAPException {
        return delegateHeader.addChildElement(arg0);
    }
    
    public SOAPElement addChildElement(SOAPElement arg0) throws SOAPException {
        return delegateHeader.addChildElement(arg0);
    }
    
    public SOAPElement addNamespaceDeclaration(String arg0, String arg1)
    throws SOAPException {
        return delegateHeader.addNamespaceDeclaration(arg0, arg1);
    }
    
    public SOAPElement addTextNode(String arg0) throws SOAPException {
        return delegateHeader.addTextNode(arg0);
    }
    
    public Node appendChild(Node arg0) throws DOMException {
        return delegateHeader.appendChild(arg0);
    }
  
    public SOAPElement makeUsable(SOAPElement elem)throws XWSSecurityException {          
        SOAPElement elementToInsert = elem;
        try {
            if (elem.getOwnerDocument() != ownerDoc) {
                elementToInsert =
                (SOAPElement) ownerDoc.importNode(
                elem, true);
            }            
            return elementToInsert;
        } catch (DOMException e) {
            log.log(Level.SEVERE, "WSS0376.error.inserting.header", e.getMessage());
            throw new XWSSecurityException(e);
        }
    }
    public Node cloneNode(boolean arg0) {
        return delegateHeader.cloneNode(arg0);
    }
    
    public void detachNode() {
        delegateHeader.detachNode();
    }
    
    public boolean equals(Object obj) {
        return delegateHeader.equals(obj);
    }
    
    public Iterator getAllAttributes() {
        return delegateHeader.getAllAttributes();
    }
    
    public String getAttribute(String arg0) {
        return delegateHeader.getAttribute(arg0);
    }
    
    public Attr getAttributeNode(String arg0) {
        return delegateHeader.getAttributeNode(arg0);
    }
    
    public Attr getAttributeNodeNS(String arg0, String arg1) {
        return delegateHeader.getAttributeNodeNS(arg0, arg1);
    }
    
    public String getAttributeNS(String arg0, String arg1) {
        return delegateHeader.getAttributeNS(arg0, arg1);
    }
    
    public NamedNodeMap getAttributes() {
        return delegateHeader.getAttributes();
    }
    
    public String getAttributeValue(Name arg0) {
        return delegateHeader.getAttributeValue(arg0);
    }
    
    public Iterator getChildElements() {
        return delegateHeader.getChildElements();
    }
    
    public Iterator getChildElements(Name arg0) {
        return delegateHeader.getChildElements(arg0);
    }
    
    public NodeList getChildNodes() {
        return delegateHeader.getChildNodes();
    }
    
    public Name getElementName() {
        return delegateHeader.getElementName();
    }
    
    public NodeList getElementsByTagName(String arg0) {
        return delegateHeader.getElementsByTagName(arg0);
    }
    
    public NodeList getElementsByTagNameNS(String arg0, String arg1) {
        return delegateHeader.getElementsByTagNameNS(arg0, arg1);
    }
    
    public String getEncodingStyle() {
        return delegateHeader.getEncodingStyle();
    }
    
    public Node getFirstChild() {
        return delegateHeader.getFirstChild();
    }
    
    public Node getLastChild() {
        return delegateHeader.getLastChild();
    }
    
    public String getLocalName() {
        return delegateHeader.getLocalName();
    }
    
    public Iterator getNamespacePrefixes() {
        return delegateHeader.getNamespacePrefixes();
    }
    
    public String getNamespaceURI() {
        return delegateHeader.getNamespaceURI();
    }
    
    public String getNamespaceURI(String arg0) {
        return delegateHeader.getNamespaceURI(arg0);
    }
    
    public Node getNextSibling() {
        return delegateHeader.getNextSibling();
    }
    
    public String getNodeName() {
        return delegateHeader.getNodeName();
    }
    
    public short getNodeType() {
        return delegateHeader.getNodeType();
    }
    
    public String getNodeValue() throws DOMException {
        return delegateHeader.getNodeValue();
    }
    
    public Document getOwnerDocument() {
        return delegateHeader.getOwnerDocument();
    }
    
    public SOAPElement getParentElement() {
        return delegateHeader.getParentElement();
    }
    
    public Node getParentNode() {
        return delegateHeader.getParentNode();
    }
    
    public String getPrefix() {
        return delegateHeader.getPrefix();
    }
    
    public Node getPreviousSibling() {
        return delegateHeader.getPreviousSibling();
    }
    
    public String getTagName() {
        return delegateHeader.getTagName();
    }
    
    public String getValue() {
        return delegateHeader.getValue();
    }
    
    public Iterator getVisibleNamespacePrefixes() {
        return delegateHeader.getVisibleNamespacePrefixes();
    }
    
    public boolean hasAttribute(String arg0) {
        return delegateHeader.hasAttribute(arg0);
    }
    
    public boolean hasAttributeNS(String arg0, String arg1) {
        return delegateHeader.hasAttributeNS(arg0, arg1);
    }
    
    public boolean hasAttributes() {
        return delegateHeader.hasAttributes();
    }
    
    public boolean hasChildNodes() {
        return delegateHeader.hasChildNodes();
    }
    
    public int hashCode() {
        return delegateHeader.hashCode();
    }
    
    public Node insertBefore(Node arg0, Node arg1) throws DOMException {
        
        return delegateHeader.insertBefore(arg0, arg1);
    }
    
    public boolean isSupported(String arg0, String arg1) {
        return delegateHeader.isSupported(arg0, arg1);
    }
    
    public void normalize() {
        delegateHeader.normalize();
    }
    
    public void recycleNode() {
        delegateHeader.recycleNode();
    }
    
    public void removeAttribute(String arg0) throws DOMException {
        delegateHeader.removeAttribute(arg0);
    }
    
    public boolean removeAttribute(Name arg0) {
        return delegateHeader.removeAttribute(arg0);
    }
    
    public Attr removeAttributeNode(Attr arg0) throws DOMException {
        return delegateHeader.removeAttributeNode(arg0);
    }
    
    public void removeAttributeNS(String arg0, String arg1)
    throws DOMException {
        delegateHeader.removeAttributeNS(arg0, arg1);
    }
    
    public Node removeChild(Node arg0) throws DOMException {
        return delegateHeader.removeChild(arg0);
    }
    
    public void removeContents() {
        delegateHeader.removeContents();
    }
    
    public boolean removeNamespaceDeclaration(String arg0) {
        return delegateHeader.removeNamespaceDeclaration(arg0);
    }
    
    public Node replaceChild(Node arg0, Node arg1) throws DOMException {
        return delegateHeader.replaceChild(arg0, arg1);
    }
    
    public void setAttribute(String arg0, String arg1) throws DOMException {
        delegateHeader.setAttribute(arg0, arg1);
    }
    
    public Attr setAttributeNode(Attr arg0) throws DOMException {
        return delegateHeader.setAttributeNode(arg0);
    }
    
    public Attr setAttributeNodeNS(Attr arg0) throws DOMException {
        return delegateHeader.setAttributeNodeNS(arg0);
    }
    
    public void setAttributeNS(String arg0, String arg1, String arg2)
    throws DOMException {
        delegateHeader.setAttributeNS(arg0, arg1, arg2);
    }
    
    public void setEncodingStyle(String arg0) throws SOAPException {
        delegateHeader.setEncodingStyle(arg0);
    }
    
    public void setNodeValue(String arg0) throws DOMException {
        delegateHeader.setNodeValue(arg0);
    }
    
    public void setParentElement(SOAPElement arg0) throws SOAPException {
        delegateHeader.setParentElement(arg0);
    }
    
    public void setPrefix(String arg0) throws DOMException {
        delegateHeader.setPrefix(arg0);
    }
    
    public void setValue(String arg0) {
        delegateHeader.setValue(arg0);
    }
    
    public String toString() {
        return delegateHeader.toString();
    }
    
    // DOM L3 methods from org.w3c.dom.Node
    public String getBaseURI() {
        return delegateHeader.getBaseURI();
    }
    
    public short compareDocumentPosition(org.w3c.dom.Node other)
    throws DOMException {
        return delegateHeader.compareDocumentPosition(other);
    }
    
    public String getTextContent()
    throws DOMException {
        return delegateHeader.getTextContent();
    }
    
    public void setTextContent(String textContent) throws DOMException {
        delegateHeader.setTextContent(textContent);
    }
    
    public boolean isSameNode(org.w3c.dom.Node other) {
        return delegateHeader.isSameNode(other);
    }
    
    public String lookupPrefix(String namespaceURI) {
        return delegateHeader.lookupPrefix(namespaceURI);
    }
    
    public boolean isDefaultNamespace(String namespaceURI) {
        return delegateHeader.isDefaultNamespace(namespaceURI);
    }
    
    public String lookupNamespaceURI(String prefix) {
        return  delegateHeader.lookupNamespaceURI(prefix);
    }
    
    public boolean isEqualNode(org.w3c.dom.Node arg) {
        return  delegateHeader.isEqualNode(arg);
    }
    
    public Object getFeature(String feature,
    String version) {
        return  delegateHeader.getFeature(feature,version);
    }
    
    public Object setUserData(String key,
    Object data,
    UserDataHandler handler) {
        return  delegateHeader.setUserData(key,data,handler);
    }
    
    public Object getUserData(String key) {
        return  delegateHeader.getUserData(key);
    }
    
    // DOM L3 methods from org.w3c.dom.Element
    
    public void setIdAttribute(String name, boolean isId) throws DOMException {
        delegateHeader.setIdAttribute(name, isId);
    }
    
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        delegateHeader.setIdAttributeNode(idAttr, isId);
    }
    
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
        delegateHeader.setIdAttributeNS(namespaceURI, localName, isId);
    }
    
    public TypeInfo getSchemaTypeInfo() {
        return  delegateHeader.getSchemaTypeInfo();
    }

   public Iterator getAllAttributesAsQNames() {
       return  delegateHeader. getAllAttributesAsQNames();
   }

}
