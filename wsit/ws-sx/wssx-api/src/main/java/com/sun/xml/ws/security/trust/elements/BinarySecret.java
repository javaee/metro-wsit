/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

/*
 * $Id: BinarySecret.java,v 1.2 2010-10-21 15:35:40 snajper Exp $
 */

package com.sun.xml.ws.security.trust.elements;
import com.sun.xml.ws.security.trust.WSTrustConstants;

import java.util.Map;
import javax.xml.crypto.XMLStructure;
import javax.xml.namespace.QName;

/**
 * @author WS-Trust Implementation Team
 */
public interface BinarySecret extends XMLStructure {

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
