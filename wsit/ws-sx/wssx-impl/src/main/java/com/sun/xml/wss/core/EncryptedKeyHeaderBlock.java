/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * $Id: EncryptedKeyHeaderBlock.java,v 1.2 2010-10-21 15:37:11 snajper Exp $
 */

package com.sun.xml.wss.core;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import java.util.Iterator;
import java.util.logging.Level;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;

import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;

/**
 * The schema definition of EncryptedKey element is as follows:
 * <xmp>
 * <element name='EncryptedKey' type='xenc:EncryptedKeyType'/>
 * <complexType name='EncryptedKeyType'>
 *     <complexContent>
 *         <extension base='xenc:EncryptedType'>
 *             <sequence>
 *                 <element ref='xenc:ReferenceList' minOccurs='0'/>
 *                 <element name='CarriedKeyName' type='string' minOccurs='0'/>
 *             </sequence>
 *             <attribute name='Recipient' type='string' use='optional'/>
 *         </extension>
 *     </complexContent>
 * </complexType>
 * </xmp>
 *
 * @author Vishal Mahajan
 */
public class EncryptedKeyHeaderBlock extends EncryptedTypeHeaderBlock {

    ReferenceListHeaderBlock referenceList;

    SOAPElement carriedKeyName;

    /**
     * Create an empty EncryptedKey element.
     *
     * @throws XWSSecurityException
     *     If there is problem creating an EncryptedKey element.
     */
    public EncryptedKeyHeaderBlock() throws XWSSecurityException {
        try {
            setSOAPElement(
                getSoapFactory().createElement(
                    MessageConstants.XENC_ENCRYPTED_KEY_LNAME,
                    MessageConstants.XENC_PREFIX,
                    MessageConstants.XENC_NS));
            addNamespaceDeclaration(
                MessageConstants.XENC_PREFIX,
                MessageConstants.XENC_NS);
        } catch (SOAPException e) {
            log.log(Level.SEVERE, "WSS0348.error.creating.ekhb", e.getMessage()); 
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Create an empty EncryptedKey element whose owner document is given.
     *
     * @throws XWSSecurityException
     *     If there is problem creating an EncryptedKey element
     */
    public EncryptedKeyHeaderBlock(Document doc) throws XWSSecurityException {
        try {
            setSOAPElement(
                (SOAPElement) doc.createElementNS(
                    MessageConstants.XENC_NS,
                    MessageConstants.XENC_ENCRYPTED_KEY_QNAME));
            addNamespaceDeclaration(
                MessageConstants.XENC_PREFIX,
                MessageConstants.XENC_NS);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0348.error.creating.ekhb", e.getMessage());
            throw new XWSSecurityException(e);
        }
    }

    /**
     * @throws XWSSecurityException
     *     If there is problem in initializing EncryptedKey element.
     */
    public EncryptedKeyHeaderBlock(SOAPElement element)
        throws XWSSecurityException {

        try {

            setSOAPElement(element);

            if (!(element.getLocalName().equals(
                      MessageConstants.XENC_ENCRYPTED_KEY_LNAME) &&
                  XMLUtil.inEncryptionNS(element))) {
                log.log(Level.SEVERE, "WSS0349.error.creating.ekhb", element.getTagName());  
                throw new XWSSecurityException("Invalid EncryptedKey passed");
            }

            initializeEncryptedType(element);

            Iterator referenceLists =
                getChildElements(
                    getSoapFactory().createName(
                        "ReferenceList",
                        MessageConstants.XENC_PREFIX,
                        MessageConstants.XENC_NS));
            if (referenceLists.hasNext())
                this.referenceList =
                    new ReferenceListHeaderBlock(
                        (SOAPElement) referenceLists.next());

        
            Iterator carriedKeyNames =
                getChildElements(
                    getSoapFactory().createName(
                        "CarriedKeyName",
                        MessageConstants.XENC_PREFIX,
                        MessageConstants.XENC_NS));
            if (carriedKeyNames.hasNext())
                this.carriedKeyName = (SOAPElement) carriedKeyNames.next(); 

        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0348.error.creating.ekhb", e.getMessage());
            throw new XWSSecurityException(e);
        }
    }

    public void setCipherData(SOAPElement cipherData) {
        this.cipherData = cipherData;
        updateRequired = true;
    }

    public void setCipherValue(String cipherValue)
        throws XWSSecurityException {

        try {
            cipherData =
                getSoapFactory().createElement(
                    "CipherData",
                    MessageConstants.XENC_PREFIX,
                    MessageConstants.XENC_NS);
            cipherData.addNamespaceDeclaration(
                MessageConstants.XENC_PREFIX,
                MessageConstants.XENC_NS);

            cipherData.addTextNode("\n    ");
            SOAPElement cipherValueElement =
                cipherData.addChildElement(
                    "CipherValue", MessageConstants.XENC_PREFIX);
            cipherData.addTextNode("\n    ");

            cipherValueElement.addTextNode(cipherValue);

            cipherData.removeNamespaceDeclaration(
                MessageConstants.XENC_PREFIX);
        } catch (SOAPException e) {
            log.log(Level.SEVERE, "WSS0350.error.setting.ciphervalue", e.getMessage());
            throw new XWSSecurityException(e);
        }
        updateRequired = true;
    }

    public ReferenceListHeaderBlock getReferenceList() {
        return referenceList;
    }

    public void setReferenceList(ReferenceListHeaderBlock referenceList) {
        this.referenceList = referenceList;
        updateRequired = true;
    }

    /**
     * Returns null if Recipient attr is not present
     */
    public String getRecipient() {
        String recipient = getAttribute("Recipient");
        if(recipient.equals(""))
            return null;
        return recipient;
    }

    public void setRecipient(String recipient) {
        setAttribute("Recipient", recipient);
    }

    public SOAPElement getCarriedKeyName() {
        return carriedKeyName;
    }

    public void setCarriedKeyName(SOAPElement carriedKeyName) {
        this.carriedKeyName = carriedKeyName;
        updateRequired = true;
    }

    public static SecurityHeaderBlock fromSoapElement(SOAPElement element)
        throws XWSSecurityException {
        return SecurityHeaderBlockImpl.fromSoapElement(
            element, EncryptedKeyHeaderBlock.class);
    }

    public SOAPElement getAsSoapElement() throws XWSSecurityException {
        if (updateRequired) {
            removeContents();
            try {
                addTextNode("\n    ");
                if (encryptionMethod != null) {
                    addChildElement(encryptionMethod);
                    addTextNode("\n    ");
                }
                if (keyInfo != null) {
                    addChildElement(keyInfo.getAsSoapElement());
                    addTextNode("\n    ");
                }
                if (cipherData == null) {
                    log.log(Level.SEVERE, 
                            "WSS0347.missing.cipher.data");
                    throw new XWSSecurityException(
                        "CipherData is not present inside EncryptedType");
                }
                addChildElement(cipherData);
                addTextNode("\n    ");
                if (encryptionProperties != null) {
                    addChildElement(encryptionProperties);
                    addTextNode("\n    ");
                }
                if (referenceList != null) {
                    addChildElement(referenceList.getAsSoapElement());
                    addTextNode("\n    ");
                }
                if (carriedKeyName != null) {
                    addChildElement(carriedKeyName);
                    addTextNode("\n    ");
                }
            } catch (SOAPException e) {
                log.log(Level.SEVERE, "WSS0348.error.creating.ekhb", e.getMessage());
                throw new XWSSecurityException(e);
            }
        }
        return super.getAsSoapElement();
    }
}
