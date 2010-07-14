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

import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.xml.ws.security.Token;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;
import java.security.Key;
import javax.crypto.SecretKey;
import javax.xml.soap.SOAPElement;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.Iterator;
import javax.xml.namespace.QName;

/**
 *
 * @author root
 */

public class EncryptedKeyToken extends SecurityHeaderBlockImpl implements SecurityToken, Token {
    
    EncryptedKey encryptedKey = null;
    SOAPElement elem = null;
    /** Creates a new instance of EncryptedKeyToken */
    public EncryptedKeyToken(SOAPElement elem) {
        this.elem = elem;
    }
    
    public Key getSecretKey(Key privKey, String dataEncAlgo) throws XWSSecurityException {
        try {
            XMLCipher xmlc = null;
            String algorithm = null;
            if(elem != null){
                NodeList nl = elem.getElementsByTagNameNS(MessageConstants.XENC_NS, "EncryptionMethod");
                if (nl != null)
                    algorithm = ((Element)nl.item(0)).getAttribute("Algorithm");
                xmlc = XMLCipher.getInstance(algorithm); 
                if ( encryptedKey == null)
                    encryptedKey = xmlc.loadEncryptedKey(elem);
            }
            if (xmlc == null){
                throw new XWSSecurityException("XMLCipher is null while getting SecretKey from EncryptedKey");
            }
            xmlc.init(XMLCipher.UNWRAP_MODE, privKey);
            SecretKey symmetricKey = (SecretKey) xmlc.decryptKey(encryptedKey, dataEncAlgo);
            return symmetricKey;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new XWSSecurityException("Error while getting SecretKey from EncryptedKey");
        }
    }
    
    public SOAPElement getAsSoapElement() {
        //throw new UnsupportedOperationException("Not supported");
        if(elem != null)
            return elem;
        else
           throw new UnsupportedOperationException("Not supported"); 
    }
    
    
    public String getId(){
        try {
            return elem.getAttribute("Id");
        } catch (Exception ex) {
            throw new RuntimeException("Error while extracting ID");
        }
    }
    
    public KeyInfoHeaderBlock getKeyInfo() {
         try {
            if (encryptedKey != null) {
                return  new KeyInfoHeaderBlock(encryptedKey.getKeyInfo());
            } else{
                Iterator iter = elem.getChildElements(new QName(MessageConstants.DSIG_NS,"KeyInfo"));
                Element keyInfoElem = null;
                if(iter.hasNext()){
                    keyInfoElem = (Element)iter.next();
                }
                KeyInfo keyInfo = new KeyInfo(keyInfoElem, "MessageConstants.DSIG_NS");
                return new KeyInfoHeaderBlock(keyInfo);
            }           
        } catch (XWSSecurityException ex) {
            throw new XWSSecurityRuntimeException("Error while extracting KeyInfo", ex);
        } catch (XMLSecurityException ex) {
            throw new XWSSecurityRuntimeException("Error while extracting KeyInfo", ex);
        }
    }
    
     public String getType() {
        return MessageConstants.XENC_ENCRYPTED_KEY_QNAME;
    }

    public Object getTokenValue() {
        return this;
    }
}
