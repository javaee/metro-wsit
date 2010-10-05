/*
 * $Id: EncryptedDataHeaderBlock.java,v 1.1 2010-10-05 11:52:37 m_potociar Exp $
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

import java.util.logging.Level;

import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;

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
