/*
 * $Id: BinarySecretImpl.java,v 1.2.2.1 2006-08-14 12:33:59 m_potociar Exp $
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

import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import javax.xml.namespace.QName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlAccessorType;

import com.sun.xml.ws.security.trust.WSTrustException;

import com.sun.xml.ws.security.trust.impl.bindings.BinarySecretType;

import com.sun.xml.ws.security.trust.elements.BinarySecret;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;

/**
 *
 * @author WS-Trust Implementation Team
 */
public class BinarySecretImpl extends BinarySecretType implements BinarySecret {
   
    public BinarySecretImpl(byte[] rawValue, String type) {        
        setRawValue(rawValue);
        setType(type);
        
    }
    
    public BinarySecretImpl(BinarySecretType bsType){
        this(bsType.getValue(), bsType.getType());
        
    }
    
    /**
     * Constructs a <code>BinarySecret</code> element from
     * an existing XML block.
     *
     * @param lifetimeElement A
     *        <code>org.w3c.dom.Element</code> representing DOM tree
     *        for <code>BinarySecret</code> object.
     * @exception WSTrustException if it could not process the
     *            <code>org.w3c.dom.Element</code> properly, implying that
     *            there is an error in the sender or in the element definition.
     */
    public static BinarySecretType fromElement(org.w3c.dom.Element element)
        throws WSTrustException {
        try {
            javax.xml.bind.Unmarshaller u = WSTrustElementFactory.getContext().createUnmarshaller();
            return (BinarySecretType)((JAXBElement)u.unmarshal(element)).getValue();
        } catch (Exception ex) {
            throw new WSTrustException(ex.getMessage(), ex);
        }
    }

     public byte[] getRawValue() {
        return super.getValue();
     }
     
     
     public String getTextValue() {
        return Base64.encode(getRawValue());         
     }
     
     public void setRawValue(byte[] rawText) {
        setValue(rawText);
     }
      
     public void setTextValue(String encodedText) {
         try {
             setValue(Base64.decode(encodedText));
         } catch (Base64DecodingException de) {
             throw new RuntimeException("Error while decoding " + 
                                        de.getMessage()); 
         }
     }
     
 /*    public void setType(String type) {
        if (!(this.ASYMMETRIC_KEY_TYPE.equalsIgnoreCase(type)  
              || this.NONCE_KEY_TYPE.equalsIgnoreCase(type) 
              || this.SYMMETRIC_KEY_TYPE.equalsIgnoreCase(type))) {
            throw new RuntimeException("Invalid BinarySecret Type: " + type);
        }
        
        setType(type);
        
     }
     
     public String getType(){
         return this.type;
     }
     */
}
