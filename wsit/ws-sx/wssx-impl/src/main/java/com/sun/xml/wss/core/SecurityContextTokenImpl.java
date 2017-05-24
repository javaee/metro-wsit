/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * SecurityContextTokenHeaderBlock.java
 *
 * Created on December 15, 2005, 6:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.core;

import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurityTokenException;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;

import java.util.Iterator;
import java.util.List;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.URI;
import java.util.ArrayList;

/**
 *&lt;wsc:SecurityContextToken wsu:Id="..." ...&gt; 
 *    &lt;wsc:Identifier&gt;...&lt;/wsc:Identifier&gt; 
 *    &lt;wsc:Instance&gt;...&lt;/wsc:Instance&gt; 
 *    ... 
 *&lt;/wsc:SecurityContextToken&gt;
 *
 */
public class SecurityContextTokenImpl extends SecurityHeaderBlockImpl 
    implements SecurityContextToken, SecurityToken {
    
    private String securityContextId = null;
    private String instance = null;
    private List extElements = null;
    
    private String wsuId = null;
    
    /**
     *
     * @param element
     * @return
     * @throws XWSSecurityException
     */
    public static SecurityHeaderBlock fromSoapElement(SOAPElement element)
    throws XWSSecurityException {
        return SecurityHeaderBlockImpl.fromSoapElement(
                element, SecurityContextTokenImpl.class);
    }
    
    private Document contextDocument = null;
    
    
    public SecurityContextTokenImpl(
        Document contextDocument, String contextId, String instance, String wsuId, List extElements) throws XWSSecurityException {
        securityContextId = contextId;
        this.instance = instance;
        this.wsuId = wsuId;
        this.extElements = extElements;
        this.contextDocument = contextDocument;
    }
    
    @SuppressWarnings("unchecked")
    public SecurityContextTokenImpl(SOAPElement sct) throws XWSSecurityException {
        
        setSOAPElement(sct);
        
        this.contextDocument = getOwnerDocument();
        
        if (!("SecurityContextToken".equals(getLocalName()) &&
                XMLUtil.inWsscNS(this))) {
            throw new SecurityTokenException(
                    "Expected wsc:SecurityContextToken Element, but Found " + getPrefix() + ":" + getLocalName());
        }
        
        String wsuIdVal = getAttributeNS(MessageConstants.WSU_NS, "Id");
        if (!"".equals(wsuIdVal)) {
            this.wsuId = wsuIdVal;
        }
        
        Iterator children = getChildElements();
        Node object;
        
        while (children.hasNext()) {

            object = (Node)children.next();
            if (object.getNodeType() == Node.ELEMENT_NODE) {
                
                SOAPElement element = (SOAPElement) object;
                if ("Identifier".equals(element.getLocalName()) &&
                        XMLUtil.inWsscNS(element)) {
                     securityContextId = element.getFirstChild().getNodeValue();
                } else if ( "Instance".equals(element.getLocalName()) &&
                        XMLUtil.inWsscNS(element)) {
                    this.instance = element.getFirstChild().getNodeValue();
                } else {
                    if (extElements == null) {
                        extElements = new ArrayList();
                    }
                    extElements.add(object);
                }
            }
        }
        
        if (securityContextId == null) {
            throw new XWSSecurityException("Missing Identifier subelement in SecurityContextToken");
        }
    }
    
    @Override
    public SOAPElement getAsSoapElement() throws XWSSecurityException {
        if ( delegateElement != null )
            return delegateElement;
        
        try {
            setSOAPElement(
                    (SOAPElement) contextDocument.createElementNS(
                    MessageConstants.WSSC_NS,
                    MessageConstants.WSSC_PREFIX + ":SecurityContextToken"));
            /*addNamespaceDeclaration(
                    MessageConstants.WSSE11_PREFIX,
                    MessageConstants.WSS11_SPEC_NS);*/
            addNamespaceDeclaration(
                    MessageConstants.WSSC_PREFIX,
                    MessageConstants.WSSC_NS);
            if (securityContextId == null )  {
                throw new XWSSecurityException("Missing SecurityContextToken Identifier");
            } else {
                addChildElement("Identifier", MessageConstants.WSSC_PREFIX).addTextNode(securityContextId);
            }

            if (this.instance != null) {
                addChildElement("Instance", MessageConstants.WSSC_PREFIX).addTextNode(this.instance);
            }
            
            if (wsuId != null) {
                setWsuIdAttr(this, wsuId);
            }
            
            if (extElements != null) {
                for (int i=0; i<extElements.size(); i++) {
                    Element element = (Element)extElements.get(i);
                    Node newElement = delegateElement.getOwnerDocument().importNode(element,true);
                    appendChild(newElement);
                }
            }
            
        } catch (SOAPException se) {
            throw new SecurityTokenException(
                    "There was an error creating SecurityContextToken " +
                    se.getMessage());
        }
        
        return super.getAsSoapElement();
    }
    
    public Document getContextDocument() {
        return contextDocument;
    }

    public String getType() {
        return MessageConstants.SECURITY_CONTEXT_TOKEN_NS;
    }

    public Object getTokenValue() {
        return this;
    }
    
    public void setId(String wsuId) {
        this.wsuId = wsuId;
    }
    
    public String getWsuId() {
        return this.wsuId;
    }

    // dont use this
    public URI getIdentifier() {
        try {
            return new URI(securityContextId);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getSCId() {
        return securityContextId;
    }

    public String getInstance() {
        return instance;
    }

    public List getExtElements() {
        return extElements;
    }
    
}
