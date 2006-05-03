/*
 * $Id: Entropy.java,v 1.1 2006-05-03 22:57:17 arungupta Exp $
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

package com.sun.xml.ws.security.trust.elements;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import com.sun.xml.ws.security.EncryptedKey;

/**
 *
 * @author WS-Trust Implementation Team
 */
public interface Entropy {
    
    /** 
     * Constants defining the Type of Entropy
     */
     public static final String BINARY_SECRET_TYPE="BinarySecret";
     public static final String ENCRYPTED_KEY_TYPE="EncryptedKey";
     public static final String CUSTOM_TYPE="Custom";
     
     /**
      *Gets the type of the Entropy contents
      */
      String getEntropyType();

    /**
      *Sets the type of the Entropy contents
      */
      void setEntropyType(String entropyType);
      
    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link Object }
     * 
     * 
     */
    List<Object> getAny();

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    Map<QName, String> getOtherAttributes();
    
    
    /** Gets the BinarySecret (if any) inside this Entropy 
     * @return BinarySecret if set, null otherwise
     */
    BinarySecret getBinarySecret();

  /**
   * Sets the BinarySecret (if any) inside this Entropy
   */
    void setBinarySecret(BinarySecret binarySecret);

    /**
     * Gets the xenc:EncryptedKey set inside this Entropy instance
     * @return xenc:EncryptedKey if set, null otherwise
     */
    EncryptedKey getEncryptedKey();

    /**
     * Sets the xenc:EncryptedKey set inside this Entropy instance
     */
    void setEncryptedKey(EncryptedKey encryptedKey);
    
}
