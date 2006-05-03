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

import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.xml.ws.security.Token;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;
import java.security.Key;
import javax.crypto.SecretKey;
import javax.xml.soap.SOAPElement;

import com.sun.xml.wss.impl.MessageConstants;
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
                return  new KeyInfoHeaderBlock(keyInfo);
            }           
        } catch (Exception ex) {
            throw new RuntimeException("Error while extracting KeyInfo");
        }
    }
    
     public String getType() {
        return MessageConstants.XENC_ENCRYPTED_KEY_QNAME;
    }

    public Object getTokenValue() {
        return this;
    }
}
