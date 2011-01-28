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

/*
 * EncryptedHeaderBlock.java
 *
 * Created on October 13, 2006, 4:48 PM
 *
 */

package com.sun.xml.wss.core;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.misc.SOAPElementExtension;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

/**
 *
 * @author Mayank.Mishra@Sun.com
 */
/**
 * Corresponds to Schema definition for EncryptedData. 
 * Schema definition for EncryptedData is as follows:
 * <p>
 * <xmp>
 * <element name='EncryptedHeader' type='wsse11:EncryptedHeaderType'/>
 * <complexType name='EncryptedHeaderType'>
 *   <element name='EncryptedData'>
 *     <complexContent>
 *         <extension base='xenc:EncryptedType'/>
 *     </complexContent>
 *   </element>
 * </complexType>
 * </xmp>
 *
 * @author Mayank Mishra
 */
public class EncryptedHeaderBlock extends SOAPElementExtension implements SOAPElement  {
    
        private static Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
        
        protected SOAPElement delegateElement;
        private static SOAPFactory soapFactory;
        
        private static final Name idAttributeName;
        //private Document ownerDoc;
        
        private boolean bsp=false;
        
    static
    {
        Name temp = null;
        try {
            soapFactory = SOAPFactory.newInstance();
            temp =
                getSoapFactory().createName(
                    "Id",
                    "wsu",
                    "http://schemas.xmlsoap.org/ws/2003/06/utility");
        } catch (SOAPException e) {
            log.log(Level.SEVERE,
                    "WSS0654.soap.exception",
                    e.getMessage());
        }

        idAttributeName = temp;
    }

    public EncryptedHeaderBlock(Document doc) throws XWSSecurityException {
                try {
            setSOAPElement(
                (SOAPElement) doc.createElementNS(
                    MessageConstants.WSSE11_NS,
                    MessageConstants.ENCRYPTED_HEADER_QNAME));
            addNamespaceDeclaration(
                MessageConstants.WSSE11_PREFIX,
                MessageConstants.WSSE11_NS);
        } catch (SOAPException e) {
            log.log(Level.SEVERE, "WSS0360.error.creating.ehb", e.getMessage());
            throw new XWSSecurityException(e);
        }
        //ownerDoc = doc;
    }
    
    /** Creates a new instance of EncryptedHeaderBlock */
    public EncryptedHeaderBlock(SOAPElement delegateElement) throws XWSSecurityException {
        setSOAPElement(delegateElement);
                        try {
            setSOAPElement(
                getSoapFactory().createElement(
                    "EncryptedHeader",
                    MessageConstants.WSSE11_PREFIX,
                    MessageConstants.WSSE11_NS));
            addNamespaceDeclaration(
                MessageConstants.WSSE11_PREFIX,
                MessageConstants.WSSE11_NS);
                        
        } catch (SOAPException e) {
            log.log(Level.SEVERE, "WSS0360.error.creating.ehb", e.getMessage());
            throw new XWSSecurityException(e);
        }
        //ownerDoc = delegateElement.getOwnerDocument();
    }
    
    protected void setSOAPElement(SOAPElement delegateElement) {
        this.delegateElement = delegateElement;
    }
    
    public void copyAttributes(final SecurableSoapMessage secureMsg, final SecurityHeader _secHeader) throws XWSSecurityException{
                
        String SOAP_namespace = secureMsg.getEnvelope().getNamespaceURI();
        String SOAP_prefix = secureMsg.getEnvelope().getPrefix();
        String value_mustUnderstand= _secHeader.getAttributeNS(SOAP_namespace, "mustUnderstand");
        String value_S12_role= _secHeader.getAttributeNS(SOAP_namespace, "role");
        String value_S11_actor = _secHeader.getAttributeNS(SOAP_namespace, "actor");
        String value_S12_relay = _secHeader.getAttributeNS(SOAP_namespace, "relay");
        
        if(value_mustUnderstand!=null && !value_mustUnderstand.equals("")){
            this.setAttributeNS(SOAP_namespace, SOAP_prefix+":mustUnderstand", value_mustUnderstand);
        }
        if(value_S12_role!=null && !value_S12_role.equals("")){
            this.setAttributeNS(SOAP_namespace, SOAP_prefix+":role", value_S12_role);
        }
        if(value_S11_actor!=null && !value_S11_actor.equals("")){
            this.setAttributeNS(SOAP_namespace, SOAP_prefix+":actor", value_S11_actor);
        }
        if(value_S12_relay!=null&&!value_S12_relay.equals("")){
            this.setAttributeNS(SOAP_namespace, SOAP_prefix+":relay", value_S12_relay);
        }
    }
    
    protected void setWsuIdAttr(Element element, String wsuId) {
        element.setAttributeNS(
            MessageConstants.NAMESPACES_NS,
            "xmlns:" + MessageConstants.WSU_PREFIX,
            MessageConstants.WSU_NS);
        element.setAttributeNS(
            MessageConstants.WSU_NS,
            MessageConstants.WSU_ID_QNAME,
            wsuId);
    }
    
    protected static SOAPFactory getSoapFactory() {
        return soapFactory;
    }
    
    /**
     * Returns null if id attr is not present
     */
    public String getId() {
        String id = getAttribute("Id");
        if (id.equals(""))
            return null;
        return id;
    }

    public void setId(String id) {
        setAttribute("Id", id);
        setIdAttribute("Id", true);
    }
    
        /**
     * Returns null if Type attr is not present
     */
    public String getType() {
        String type = getAttribute("Type");
        if (type.equals(""))
            return null;
        return type;
    }

    public void setType(String type) {
        setAttribute("Type", type);
    }

    /**
     * Returns null if MimeType attr is not present
     */
    public String getMimeType() {
        String mimeType = getAttribute("MimeType");
        if (mimeType.equals(""))
            return null;
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        setAttribute("MimeType", mimeType);
    }
    
//    public String getId() {
//        return delegateElement.getAttributeValue(idAttributeName);
//    }
//
    public SOAPElement getAsSoapElement() throws XWSSecurityException {
        return delegateElement;
    }

    public SOAPElement addChildElement(Name name) throws SOAPException {
        return delegateElement.addChildElement(name);
    }

    public SOAPElement addChildElement(String string) throws SOAPException {
        return delegateElement.addChildElement(string);
    }

    public SOAPElement addChildElement(String string, String string0) throws SOAPException {
        return delegateElement.addChildElement(string, string0);
    }

    public SOAPElement addChildElement(String string, String string0, String string1) throws SOAPException {
        return delegateElement.addChildElement(string, string0, string1);
    }

    public SOAPElement addChildElement(SOAPElement sOAPElement) throws SOAPException {
        return delegateElement.addChildElement(sOAPElement);
    }

    public void removeContents() {
        delegateElement.removeContents();
    }

    public SOAPElement addTextNode(String string) throws SOAPException {
        return delegateElement.addTextNode(string);
    }

    public SOAPElement addAttribute(Name name, String string) throws SOAPException {
        return delegateElement.addAttribute(name, string);
    }

    public SOAPElement addNamespaceDeclaration(String string, String string0) throws SOAPException {
        return delegateElement.addNamespaceDeclaration(string, string0);
    }

    public String getAttributeValue(Name name) {
        return delegateElement.getAttributeValue(name);
    }

    public Iterator getAllAttributes() {
        return delegateElement.getAllAttributes();
    }

    public Iterator getAllAttributesAsQNames() {
         return delegateElement.getAllAttributesAsQNames();
    }

    public String getNamespaceURI(String string) {
        return delegateElement.getNamespaceURI(string);
    }

    public Iterator getNamespacePrefixes() {
        return delegateElement.getNamespacePrefixes();
    }

    public Iterator getVisibleNamespacePrefixes() {
        return delegateElement.getVisibleNamespacePrefixes();
    }

    public Name getElementName() {
        return delegateElement.getElementName();
    }

    public boolean removeAttribute(Name name) {
        return delegateElement.removeAttribute(name);
    }

    public boolean removeNamespaceDeclaration(String string) {
        return delegateElement.removeNamespaceDeclaration(string);
    }

    public Iterator getChildElements() {
        return delegateElement.getChildElements();
    }

    public Iterator getChildElements(Name name) {
        return delegateElement.getChildElements(name);
    }

    public void setEncodingStyle(String string) throws SOAPException {
        delegateElement.setEncodingStyle(string);
    }

    public String getEncodingStyle() {
        return delegateElement.getEncodingStyle();
    }

    public String getValue() {
        return delegateElement.getValue();
    }

    public void setValue(String string) {
        delegateElement.setValue(string);
    }

    public void setParentElement(SOAPElement sOAPElement) throws SOAPException {
        delegateElement.setParentElement(sOAPElement);
    }

    public SOAPElement getParentElement() {
        return delegateElement.getParentElement();
    }

    public void detachNode() {
        delegateElement.detachNode();
    }

    public void recycleNode() {
        delegateElement.recycleNode();
    }

    public String getNodeName() {
        return delegateElement.getNodeName();
    }

    public String getNodeValue() throws DOMException {
        return delegateElement.getNodeValue();
    }

    public void setNodeValue(String nodeValue) throws DOMException {
        delegateElement.setNodeValue(nodeValue);                
    }

    public short getNodeType() {
        return delegateElement.getNodeType();
    }

    public Node getParentNode() {
        return delegateElement.getParentNode();
    }

    public NodeList getChildNodes() {
        return delegateElement.getChildNodes();
    }

    public Node getFirstChild() {
        return delegateElement.getFirstChild();
    }

    public Node getLastChild() {
        return delegateElement.getLastChild();
    }

    public Node getPreviousSibling() {
        return delegateElement.getPreviousSibling();
    }

    public Node getNextSibling() {
        return delegateElement.getNextSibling();
    }

    public NamedNodeMap getAttributes() {
        return delegateElement.getAttributes();
    }

    public Document getOwnerDocument() {
        return delegateElement.getOwnerDocument();
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return delegateElement.insertBefore(newChild, refChild);
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return delegateElement.replaceChild(newChild, oldChild);
    }

    public Node removeChild(Node oldChild) throws DOMException {
        return delegateElement.removeChild(oldChild);
    }

    public Node appendChild(Node newChild) throws DOMException {
        return delegateElement.appendChild(newChild);
    }

    public boolean hasChildNodes() {
        return delegateElement.hasChildNodes();
        
    }

    public Node cloneNode(boolean deep) {
        return delegateElement.cloneNode(deep);
    }

    public void normalize() {
        delegateElement.normalize();
    }

    public boolean isSupported(String feature, String version) {
        return delegateElement.isSupported(feature, version);
    }

    public String getNamespaceURI() {
        return delegateElement.getNamespaceURI();
    }

    public String getPrefix() {
        return delegateElement.getPrefix();
    }

    public void setPrefix(String prefix) throws DOMException {
        delegateElement.setPrefix(prefix);
    }

    public String getLocalName() {
        return delegateElement.getLocalName();
    }

    public boolean hasAttributes() {
        return delegateElement.hasAttributes();
    }

    public String getBaseURI() {
        return delegateElement.getBaseURI();
    }

    public short compareDocumentPosition(Node other) throws DOMException {
        return delegateElement.compareDocumentPosition(other);
    }

    public String getTextContent() throws DOMException {
        return delegateElement.getTextContent();
    }

    public void setTextContent(String textContent) throws DOMException {
        delegateElement.setTextContent(textContent);
    }

    public boolean isSameNode(Node other) {
        return delegateElement.isSameNode(other);
    }

    public String lookupPrefix(String namespaceURI) {
        return delegateElement.lookupPrefix(namespaceURI);
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        return delegateElement.isDefaultNamespace(namespaceURI);
    }

    public String lookupNamespaceURI(String prefix) {
        return delegateElement.lookupNamespaceURI(prefix);
    }

    public boolean isEqualNode(Node arg) {
        return delegateElement.isEqualNode(arg);
    }

    public Object getFeature(String feature, String version) {
        return delegateElement.getFeature(feature, version);
    }

    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return delegateElement.setUserData(key, data, handler);
    }

    public Object getUserData(String key) {
        return delegateElement.getUserData(key);
    }

    public String getTagName() {
        return delegateElement.getTagName();
    }

    public String getAttribute(String name) {
        return delegateElement.getAttribute(name);
    }

    public void setAttribute(String name, String value) throws DOMException {
        delegateElement.setAttribute(name, value);                
    }

    public void removeAttribute(String name) throws DOMException {
        delegateElement.removeAttribute(name);
    }

    public Attr getAttributeNode(String name) {
        return delegateElement.getAttributeNode(name);
    }

    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        return delegateElement.setAttributeNode(newAttr);
    }

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        return delegateElement.removeAttributeNode(oldAttr);
    }

    public NodeList getElementsByTagName(String name) {
        return delegateElement.getElementsByTagName(name);
    }

    public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
        return delegateElement.getAttributeNS(namespaceURI, localName);
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
       delegateElement.setAttributeNS(namespaceURI, qualifiedName, value); 
        
    }

    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        delegateElement.removeAttributeNS(namespaceURI, localName);
    }

    public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
        return delegateElement.getAttributeNodeNS(namespaceURI, localName);
    }

    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        return delegateElement.setAttributeNodeNS(newAttr);
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
        return delegateElement.getElementsByTagNameNS(namespaceURI, localName);
    }

    public boolean hasAttribute(String name) {
        return delegateElement.hasAttribute(name);
    }

    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
        return delegateElement.hasAttributeNS(namespaceURI, localName);
    }

    public TypeInfo getSchemaTypeInfo() {
        return delegateElement.getSchemaTypeInfo();
    }

    public void setIdAttribute(String name, boolean isId) throws DOMException {
        delegateElement.setIdAttribute(name, isId);
    }

    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
        delegateElement.setIdAttributeNS(namespaceURI, localName, isId);
    }

    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        delegateElement.setIdAttributeNode(idAttr, isId);
    }
    
    public void isBSP(boolean flag) {
        bsp = flag;
    }

    public boolean isBSP() {
        return bsp;
    }
    
}
