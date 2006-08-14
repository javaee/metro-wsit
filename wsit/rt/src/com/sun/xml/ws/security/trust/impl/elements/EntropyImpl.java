/*
 * $Id: EntropyImpl.java,v 1.2.2.1 2006-08-14 12:34:00 m_potociar Exp $
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

package com.sun.xml.ws.security.trust.impl.elements;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import javax.xml.namespace.QName;

import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.EncryptedKey;

import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.impl.bindings.EntropyType;
import com.sun.xml.ws.security.trust.impl.bindings.BinarySecretType;
import com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory;
import java.util.List;

/**
 * Implementation of Entropy Interface.
 *
 * @author Manveen Kaur
 */
public class EntropyImpl extends EntropyType implements Entropy {
    
    private String entropyType;
    private final static QName _EntropyType_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Type");
    
    private BinarySecret binarySecret = null;
    
    private EncryptedKey encryptedKey = null;
    
    public EntropyImpl() {
    }
    
    public EntropyImpl(BinarySecret binarySecret) {
        //setEntropyType(this.BINARY_SECRET_TYPE);
        setBinarySecret(binarySecret);
    }
    
    public EntropyImpl(EncryptedKey encryptedKey) {
        setEntropyType(this.ENCRYPTED_KEY_TYPE);
        setEncryptedKey(encryptedKey);
    }
    
    public EntropyImpl(EntropyType etype) {
        entropyType = etype.getOtherAttributes().get(_EntropyType_QNAME);
        List list = etype.getAny();
        for (int i = 0; i < list.size(); i++) {
            JAXBElement obj = (JAXBElement)list.get(i);
            String local = obj.getName().getLocalPart();
            if (local.equalsIgnoreCase("BinarySecret")) {
                BinarySecretType bst = (BinarySecretType) obj.getValue();
                setBinarySecret(new BinarySecretImpl(bst));
            }
        }
    }
    
    /**
     * Constructs a <code>Entropy</code> element from
     * an existing XML block.
     *
     * @param element A
     *        <code>org.w3c.dom.Element</code> representing DOM tree
     *        for <code>Entropy</code> object.
     * @exception WSTrustException if it could not process the
     *            <code>org.w3c.dom.Element</code> properly, implying that
     *            there is an error in the sender or in the element definition.
     */
    public static EntropyType fromElement(org.w3c.dom.Element element)
    throws WSTrustException {
        try {
            javax.xml.bind.Unmarshaller u = WSTrustElementFactory.getContext().createUnmarshaller();
            return (EntropyType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new WSTrustException(ex.getMessage(), ex);
        }
    }
    
    /**
     *Gets the type of the Entropy contents
     */
    public String getEntropyType() {
        return entropyType;
    }
    
    /**
     *Sets the type of the Entropy contents
     */
    public void setEntropyType(String type)  {
        if (!(type.equalsIgnoreCase(this.BINARY_SECRET_TYPE)  ||
                type.equalsIgnoreCase(this.CUSTOM_TYPE)
                || type.equalsIgnoreCase(this.ENCRYPTED_KEY_TYPE))) {
            throw new RuntimeException("Invalid Entropy Type");
        }
        entropyType = type;
        getOtherAttributes().put(_EntropyType_QNAME,type);
    }
    
    
    /** Gets the BinarySecret (if any) inside this Entropy
     * @return BinarySecret if set, null otherwise
     */
    public BinarySecret getBinarySecret() {
        return binarySecret;
    }
    
    /**
     * Sets the BinarySecret (if any) inside this Entropy
     */
    public void setBinarySecret(BinarySecret binarySecret) {
        if (binarySecret != null) {
            this.binarySecret = binarySecret;
            JAXBElement<BinarySecretType> bsElement =
                    (new ObjectFactory()).createBinarySecret((BinarySecretType)binarySecret);
            getAny().add(bsElement);
        }
    }
    
    /**
     * Gets the xenc:EncryptedKey set inside this Entropy instance
     * @return xenc:EncryptedKey if set, null otherwise
     */
    public EncryptedKey getEncryptedKey() {
        return encryptedKey;
    }
    
    /**
     * Sets the xenc:EncryptedKey set inside this Entropy instance
     */
    public void setEncryptedKey(EncryptedKey encryptedKey) {
        if (encryptedKey != null) {
            this.encryptedKey = encryptedKey;
            getAny().add(encryptedKey);
        }
    }
}
