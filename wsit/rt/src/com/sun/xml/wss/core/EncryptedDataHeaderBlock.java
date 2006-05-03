/*
 * $Id: EncryptedDataHeaderBlock.java,v 1.1 2006-05-03 22:57:31 arungupta Exp $
 */

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

package com.sun.xml.wss.core;

import java.util.logging.Level;

import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.SecurityTokenException;
import com.sun.xml.wss.impl.XMLUtil;

/**
 * Corresponds to Schema definition for EncryptedData. 
 * Schema definition for EncryptedData is as follows:
 * <p>
 * <xmp>
 * <element name='EncryptedData' type='xenc:EncryptedDataType'/>
 * <complexType name='EncryptedDataType'>
 *     <complexContent>
 *         <extension base='xenc:EncryptedType'/>
 *     </complexContent>
 * </complexType>
 * </xmp>
 *
 * @author Vishal Mahajan
 */
public class EncryptedDataHeaderBlock extends EncryptedTypeHeaderBlock {

            
    /**
     * Create an empty EncryptedData element.
     *
     * @throws XWSSecurityException
     *     If there is problem creating an EncryptedData element.
     */
    public EncryptedDataHeaderBlock() throws XWSSecurityException {
        try {
            setSOAPElement(
                getSoapFactory().createElement(
                    MessageConstants.ENCRYPTED_DATA_LNAME,
                    MessageConstants.XENC_PREFIX,
                    MessageConstants.XENC_NS));
            addNamespaceDeclaration(
                MessageConstants.XENC_PREFIX,
                MessageConstants.XENC_NS);
        } catch (SOAPException e) {
            log.log(Level.SEVERE, "WSS0345.error.creating.edhb", e.getMessage());
            throw new XWSSecurityException(e);
        }
    }
    
    

    /**
     * @throws XWSSecurityException
     *     If there is problem in initializing EncryptedData element.
     */
    public EncryptedDataHeaderBlock(SOAPElement element)
        throws XWSSecurityException {

        setSOAPElement(element);

        if (!(element.getLocalName().equals(
                  MessageConstants.ENCRYPTED_DATA_LNAME) &&
              XMLUtil.inEncryptionNS(element))) {
            log.log(Level.SEVERE, "WSS0346.error.creating.edhb", element.getTagName());  
            throw new XWSSecurityException("Invalid EncryptedData passed");
        }
        initializeEncryptedType(element);
    }

    public static SecurityHeaderBlock fromSoapElement(SOAPElement element)
        throws XWSSecurityException {
        return SecurityHeaderBlockImpl.fromSoapElement(
            element, EncryptedDataHeaderBlock.class);
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
            } catch (SOAPException e) {
                log.log(Level.SEVERE, "WSS0345.error.creating.edhb", e.getMessage());
                throw new XWSSecurityException(e);
            }
        }
        return super.getAsSoapElement();
    }  
        
}
