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

/*
 * DerivedKeyTokenHeaderBlock.java
 *
 * Created on December 15, 2005, 6:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.core;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurityTokenException;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;

import com.sun.xml.ws.security.Token;
import java.util.Iterator;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.wss.impl.misc.Base64;


/**
 *
 * @author Abhijit Das
 */
public class DerivedKeyTokenHeaderBlock extends SecurityHeaderBlockImpl implements Token, SecurityToken {
    
    /**
     *
     * @param element
     * @return
     * @throws XWSSecurityException
     */
    public static SecurityHeaderBlock fromSoapElement(SOAPElement element)
    throws XWSSecurityException {
        return SecurityHeaderBlockImpl.fromSoapElement(
                element, DerivedKeyTokenHeaderBlock.class);
    }
    
    private Document contextDocument = null;
    private SecurityTokenReference securityTokenRefElement = null;
    private long offset = 0;
    private long length = 32;
    private String nonce = null;
    private long generation = -1;
    private String wsuId = null;
    private String label = null;

    private byte[] decodedNonce = null;
    
    
    public DerivedKeyTokenHeaderBlock(Document contextDocument, SecurityTokenReference securityTokenRefElement, String wsuId) throws XWSSecurityException {
        if (securityTokenRefElement != null ) {
            this.contextDocument = contextDocument;
            this.securityTokenRefElement = securityTokenRefElement;
            this.wsuId = wsuId;
        } else {
            throw new XWSSecurityException("DerivedKeyToken can not be null");
        }
    }
    
    public DerivedKeyTokenHeaderBlock(Document contextDocument,
            SecurityTokenReference securityTokenRefElement,
            String nonce, String wsuId) throws XWSSecurityException {
        
        if (securityTokenRefElement != null ) {
            this.contextDocument = contextDocument;
            this.securityTokenRefElement = securityTokenRefElement;
            this.wsuId = wsuId;
        } else {
            throw new XWSSecurityException("DerivedKeyToken can not be null");
        }
        
        if ( nonce != null ) {
            this.nonce = nonce;
        } else {
            throw new XWSSecurityException("Nonce can not be null");
        }
    }
    
    
    public DerivedKeyTokenHeaderBlock(Document contextDocument,
            SecurityTokenReference securityTokenRefElement,
            String nonce,
            long generation,
            String wsuId) throws XWSSecurityException {
        this(contextDocument, securityTokenRefElement, nonce, wsuId);
        this.generation = generation;
    }
    
    public DerivedKeyTokenHeaderBlock(Document contextDocument,
            SecurityTokenReference securityTokenRefElement,
            String nonce,
            long offset,
            long length, String wsuId ) throws XWSSecurityException {
        this(contextDocument, securityTokenRefElement, nonce, -1, wsuId);
        this.length = length;
        this.offset = offset;
        
    }
    
     public DerivedKeyTokenHeaderBlock(Document contextDocument,
            SecurityTokenReference securityTokenRefElement,
            String nonce,
            long offset,
            long length, String wsuId, String label ) throws XWSSecurityException {
        this(contextDocument, securityTokenRefElement, nonce, -1, wsuId);
        this.length = length;
        this.offset = offset;
        this.label = label;
        
    }
    
    
    public DerivedKeyTokenHeaderBlock(SOAPElement derivedKeyTokenHeaderBlock ) throws XWSSecurityException {
        setSOAPElement(derivedKeyTokenHeaderBlock);
        
        this.contextDocument = getOwnerDocument();
        
        if (!("DerivedKeyToken".equals(getLocalName()) &&
                XMLUtil.inWsscNS(this))) {
            throw new SecurityTokenException(
                    "Expected DerivedKeyToken Element, but Found " + getPrefix() + ":" + getLocalName());
        }
        
        boolean invalidToken = false;
        
        Iterator children = getChildElements();
        
        // Check whether SecurityTokenReference is present inside DerivedKeyToken
        String wsuId = getAttributeNS(MessageConstants.WSU_NS, "Id");
        if (!"".equals(wsuId))
            setId(wsuId);
        
        Node object = null;
        boolean offsetSpecified = false; 
        boolean genSpecified = false; 
        boolean lenSpecified = false; 

        while (children.hasNext()) {
            
            object = (Node)children.next();
            
            if (object.getNodeType() == Node.ELEMENT_NODE) {
                
                SOAPElement element = (SOAPElement) object;
                //TODO: Check for other attributes
                //TODO: Add static final constants for all these string constants below. 
                if ("SecurityTokenReference".equals(element.getLocalName()) &&
                        XMLUtil.inWsseNS(element)) {
                    securityTokenRefElement = new SecurityTokenReference(element);
                } else if ( "Offset".equals(element.getLocalName()) ) {
                    try {
                        offsetSpecified = true;
                        offset = Long.valueOf(element.getValue()).longValue();
                    } catch (NumberFormatException nfe) {
                        throw new XWSSecurityException(nfe);
                    }
                } else if ( "Length".equals(element.getLocalName()) ) {
                    try{
                        lenSpecified = true;
                        length = Long.valueOf(element.getValue()).longValue();
                    } catch (NumberFormatException nfe) {
                        throw new XWSSecurityException(nfe);
                    }
                } else if ( "Nonce".equals(element.getLocalName()) ) {
                    nonce = element.getValue();
                } else if ( "Generation".equals(element.getLocalName())) {
                    try {
                        genSpecified = true;
                        generation = Long.valueOf(element.getValue()).longValue();
                    } catch (NumberFormatException nfe) {
                        throw new XWSSecurityException(nfe);
                    }
                } else if ("Label".equals(element.getLocalName())) {
                    this.label = element.getValue();
                } else {
                    invalidToken = true;
                    break;
                }
            }
        }
 
        if (offsetSpecified && genSpecified) {
            invalidToken = true;
        }
        
        if ( invalidToken) {
            throw new XWSSecurityException("Invalid DerivedKeyToken");
        }
    }
    
    public SOAPElement getAsSoapElement() throws XWSSecurityException {
        if ( delegateElement != null )
            return delegateElement;
        
        try {
            setSOAPElement(
                    (SOAPElement) contextDocument.createElementNS(
                    MessageConstants.WSSC_NS,
                    MessageConstants.WSSC_PREFIX + ":DerivedKeyToken"));
            addNamespaceDeclaration(
                    MessageConstants.WSSC_PREFIX,
                    MessageConstants.WSSC_NS);

            if ( securityTokenRefElement == null )  {
                throw new SecurityTokenException("securitytokenreference was not set");
            } else {
                SOAPElement elem = securityTokenRefElement.getAsSoapElement();
                delegateElement.appendChild(elem);
            }
            if (generation == -1) {
                addChildElement("Offset", MessageConstants.WSSC_PREFIX).addTextNode(String.valueOf(offset));
                addChildElement("Length", MessageConstants.WSSC_PREFIX).addTextNode(String.valueOf(length));
            } else {
                addChildElement("Generation", MessageConstants.WSSC_PREFIX).addTextNode(String.valueOf(generation));
                addChildElement("Length", MessageConstants.WSSC_PREFIX).addTextNode(String.valueOf(length));
            }
            if (this.label != null) {
                addChildElement("Label", MessageConstants.WSSC_PREFIX).addTextNode(this.label);
            }
            if ( nonce != null ) {
                addChildElement("Nonce", MessageConstants.WSSC_PREFIX).addTextNode(nonce);
            }
            
            if (wsuId != null) {
                setWsuIdAttr(this, wsuId);
            }
            
        } catch (SOAPException se) {
            throw new SecurityTokenException(
                    "There was an error creating DerivedKey Token " +
                    se.getMessage());
        }
        
        return super.getAsSoapElement();
    }
    
    
    
    
    public Document getContextDocument() {
        return contextDocument;
    }
    
    public byte[] getNonce() {
        if (decodedNonce != null)
            return decodedNonce;
        try {
            decodedNonce = Base64.decode(nonce);
        } catch (Base64DecodingException bde) {
            throw new RuntimeException(bde);
        }
        return decodedNonce;
    }
    
    public long getOffset() {
        return offset;
    }
    
    public long getLength() {
        return length;
    }
    
    public SecurityTokenReference getDerivedKeyElement() {
        return securityTokenRefElement;
    }

    public String getType() {
        return MessageConstants.DERIVEDKEY_TOKEN_NS;
    }
                                                                                                                                    
    public Object getTokenValue() {
        return this;
    }

    private void setId(String wsuId) {
        this.wsuId = wsuId;
    }
    
    public String getLabel() {
        return this.label;
    }

}
