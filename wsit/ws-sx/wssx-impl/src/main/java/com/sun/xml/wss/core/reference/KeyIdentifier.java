/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
 * $Id: KeyIdentifier.java,v 1.2 2010-10-21 15:37:14 snajper Exp $
 */

package com.sun.xml.wss.core.reference;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.core.ReferenceElement;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.wss.impl.misc.Base64;

/**
 * @author Vishal Mahajan
 */
public abstract class KeyIdentifier extends ReferenceElement {

    protected static final Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    /**
     * Creates an "empty" KeyIdentifier element with default encoding type
     * and default value type.
     */
    public KeyIdentifier(Document doc) throws XWSSecurityException {
        try {
            setSOAPElement(
                (SOAPElement) doc.createElementNS(
                    MessageConstants.WSSE_NS,
                    MessageConstants.WSSE_PREFIX + ":KeyIdentifier"));
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    "WSS0750.soap.exception", 
                    new Object[] {"wsse:KeyIdentifier", e.getMessage()});
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Takes a SOAPElement and checks if it has the right name.
     */
    public KeyIdentifier(SOAPElement element) throws XWSSecurityException {
        setSOAPElement(element);
        if (!(element.getLocalName().equals("KeyIdentifier") &&
              XMLUtil.inWsseNS(element))) {
            log.log(Level.SEVERE, "WSS0756.invalid.key.identifier", element.getLocalName());
            throw new XWSSecurityException("Invalid keyIdentifier passed");
        }
    }

    /**
     * If this attr is not present, returns null.
     */
    public String getValueType() {
        String valueType = getAttribute("ValueType");
        if (valueType.equals(""))
            return null;
        return valueType;
    }

    public void setValueType(String valueType) {
        setAttribute("ValueType", valueType);
    }

    /**
     * If this attr is not present, returns null.
     */
    public String getEncodingType() {
        String encodingType = getAttribute("EncodingType");
        if (encodingType.equals(""))
            return null;
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        setAttribute("EncodingType", encodingType);
    }

    public String getReferenceValue() {
        return XMLUtil.getFullTextFromChildren(this);
    }

    public void setReferenceValue(String encodedValue)
        throws XWSSecurityException {
        removeContents();
        try {
            addTextNode(encodedValue);
        } catch (SOAPException e) {
            log.log(Level.SEVERE,
                    "WSS0757.error.setting.reference",
                    e.getMessage());  
            throw new XWSSecurityException(e);
        }
    }

    /**
     * If this attr is not present, returns null.
     */
    public String getWsuId() {
        String wsuId = getAttribute("wsu:Id");
        if (wsuId.equals(""))
            return null;
        return wsuId;
    }

    public void setWsuId(String wsuId) {
        setAttributeNS(
            MessageConstants.NAMESPACES_NS,
            "xmlns:" + MessageConstants.WSU_PREFIX,
            MessageConstants.WSU_NS);
        setAttributeNS(
            MessageConstants.WSU_NS,
            MessageConstants.WSU_ID_QNAME,
            wsuId);
    }

    /**
     * Look at EncodingType (if any) and return
     * decoded result.
     * Handle Base64Binary for now.
     */
    public String getDecodedReferenceValue() throws XWSSecurityException {

        String encType = getEncodingType();

        if (encType == null) {
            return getReferenceValue();
        }
                                                                                                                                          
        String encodedText= XMLUtil.getFullTextFromChildren(this);
        if (MessageConstants.BASE64_ENCODING_NS.equals(encType)) {
            return new String(getDecodedBase64EncodedData(encodedText));
        } else {
            log.log(Level.SEVERE,
                    "WSS0762.unsupported.encodingType", 
                     new Object[] {encType});             
            throw new XWSSecurityException("Unsupported EncodingType: " + encType + " On KeyIdentifier");
        }
    }
                                                                                                                                          
    private static byte[] getDecodedBase64EncodedData(String encodedData)
        throws XWSSecurityException {
        try {
            return Base64.decode(encodedData);
        } catch (Base64DecodingException e) {
            log.log(Level.SEVERE,
                "WSS0144.unableto.decode.base64.data", 
                new Object[] {e.getMessage()});             
            throw new XWSSecurityException(
                "Unable to decode Base64 encoded data",
                e);
        }
    }


} 
