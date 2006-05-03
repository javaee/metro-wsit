/*
 * $Id: RequestedProofToken.java,v 1.1 2006-05-03 22:57:19 arungupta Exp $
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
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import java.net.URI;

/**
 * @author WS-Trust Implementation Team.
 */
public interface RequestedProofToken {
    
    /** constants indicating type of Proof Token 
     * @see getProofTokenType 
     */
    public static final String COMPUTED_KEY_TYPE = "ComputedKey";
    public static final String TOKEN_REF_TYPE = "SecurityTokenReference";
    public static final String ENCRYPTED_KEY_TYPE = "EncryptedKey";
    public static final String BINARY_SECRET_TYPE = "BinarySecret";
    public static final String CUSTOM_TYPE = "Custom";
    
    /**
     * Get the type of ProofToken present in this RequestedProofToken Instance
     */
    String getProofTokenType();

   /**
     * Set the type of ProofToken present in this RequestedProofToken Instance
     * @see getProofTokenType
     */
    void setProofTokenType(String proofTokenType);

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Element }
     *     {@link Object }
     *     
     */
    Object getAny();

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Element }
     *     {@link Object }
     *     
     */
    void setAny(Object value);
    
    /**
     * Set a SecurityTokenReference as the Proof Token 
     */
    void setSecurityTokenReference(SecurityTokenReference reference);
    
    /**
     * Gets the SecrityTokenReference if set 
     * @return SecurityTokenReference if set, null otherwise
     */
    SecurityTokenReference getSecurityTokenReference();
    
    /**
     *Sets the Computed Key URI (describing how to compute the Key)
     */
    void setComputedKey(URI computedKey);
    
    /**
     *Get the Computed Key URI (describing how to compute the Key)
     *@return computed key URI or null if none is set
     */
    URI getComputedKey();
    
    /**
     * Sets a wst:BinarySecret as the Proof Token
     */
     void setBinarySecret(BinarySecret secret);
     
     /**
      * Gets the BinarySecret proof Token if set
      * @return BinarySecret if set, null otherwise
      */
     BinarySecret getBinarySecret();
}
