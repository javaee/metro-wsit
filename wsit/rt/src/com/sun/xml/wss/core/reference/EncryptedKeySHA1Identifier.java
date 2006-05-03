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

/*
 * EncryptedKeySHA1Identifier.java
 *
 * Created on January 13, 2006, 3:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.core.reference;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.SecurityHeaderException;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.wss.impl.misc.Base64;

import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;

import java.util.logging.Level;

/**
 *
 * @author Ashutosh Shahi
 */
public class EncryptedKeySHA1Identifier extends KeyIdentifier{
    
    /**Defaults*/
    private String encodingType = MessageConstants.BASE64_ENCODING_NS;
    
    private String valueType = MessageConstants.EncryptedKeyIdentifier_NS;
    
    /**
     * Creates an "empty" KeyIdentifier element with default encoding type
     * and default value type.
     */
    public EncryptedKeySHA1Identifier(Document doc) throws XWSSecurityException {
        super(doc);
        // Set default attributes
        setAttribute("EncodingType", encodingType);
        setAttribute("ValueType", valueType);
    }
    
    public EncryptedKeySHA1Identifier(SOAPElement element) 
        throws XWSSecurityException {
        super(element);
    }
    
    public byte[] getDecodedBase64EncodedValue() throws XWSSecurityException {
        try {
            return Base64.decode(getReferenceValue());
        } catch (Base64DecodingException e) {
            log.log(Level.SEVERE, "WSS0144.unableto.decode.base64.data",
                new Object[] {e.getMessage()});
            throw new SecurityHeaderException(
                "Unable to decode Base64 encoded data",
                e);
        }
    }
}
