/*
 * $Id: EntropyImpl.java,v 1.3 2010-04-20 17:32:13 m_potociar Exp $
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

package com.sun.xml.ws.security.trust.impl.wssx.elements;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import javax.xml.namespace.QName;

import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.api.security.EncryptedKey;

import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.EntropyType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.BinarySecretType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.ObjectFactory;
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
       // setEntropyType(this.ENCRYPTED_KEY_TYPE);
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
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.ws.security.trust.impl.elements");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
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
