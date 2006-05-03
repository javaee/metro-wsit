/*
 * $Id: BinarySecret.java,v 1.1 2006-05-03 22:57:16 arungupta Exp $
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
import com.sun.xml.ws.security.trust.WSTrustConstants;

import java.util.Map;
import javax.xml.namespace.QName;

/**
 * @author WS-Trust Implementation Team
 */
public interface BinarySecret {

    /** Predefined constants for the Type of BinarySecret desired in the Security Token
     * Values for the wst:BinarySecret/@Type parameter
     */
    public static final String ASYMMETRIC_KEY_TYPE = WSTrustConstants.WST_NAMESPACE + "/AsymmetricKey";
    public static final String SYMMETRIC_KEY_TYPE = WSTrustConstants.WST_NAMESPACE + "/SymmetricKey";
    public static final String NONCE_KEY_TYPE = WSTrustConstants.WST_NAMESPACE + "/Nonce";

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

    /**
     * Gets the value of the type property. This is a URI that indicates the
     * type of secret being encoded.
     * 
     * @return {@link String }
     *     
     */
    String getType();

    /**
       * Gets the decoded value or the raw bytes of the binary secret.
       *
       * @return {@link byte[]}
       *
       */
      byte[] getRawValue();

      /**
       * Gets the encoded value of the binary secret. This represents the
       * base64 encoded BinarySecret.
       *
       * @return {@link String}
       * @see {getRawValue}
       *
       */
      String getTextValue();

    /**
     * Sets the value of the type property indicating the type of
     * secret being encoded.
     * 
     * @param type {@link String }
     *     
     */
    void setType(String type);

    /**
      * Sets the value of the Binary Secret element.
      * This is the base64 encoded value of the raw BinarySecret.
      *
      * @param encodedText {@link String }
      */
      void setTextValue(String encodedText);

      /**
       * Sets the value of the binary secret as raw bytes.
       * The value that appears in the element will be encoded appropriately.
       *
       * @param rawText {@link byte[]}
       *
       */
      void setRawValue(byte[] rawText);
    
}
