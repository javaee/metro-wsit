/*
 * $Id: BinaryExchange.java,v 1.1 2006-05-03 22:57:16 arungupta Exp $
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

import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author WS-Trust Implementation Team
 */
public interface BinaryExchange {
    /**
     * Gets the value of the encodingType property.
     * 
     * @return {@link String}
     *     
     */
    String getEncodingType();

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
     * Gets the decoded value of the text node. This represents the
     *  raw bytes for the Binary Exchange.
     *
     * @return {@link byte[] }
     *
     */
    byte[] getRawValue();


    /**
     * Gets the value of the text node. This method will return the
     * encoded value of the binary data exchanged. Encoding is specified
     * with the encodingType attibute.
     * 
     * @return {@link String}
     * @see {getRawValue}
     *     
     */
    String getTextValue();

    /**
     * Gets the value of the valueType property. ValueType contains the
     * URI that identifies the type of negotiation.
     *
     * @return  {@link String }
     *     
     */
    String getValueType();

    /**
     * Sets the value of the encodingType property.
     * 
     * @param encodingType {@link String}
     *     
     */
    void setEncodingType(String encodingType);

    /**
     * Sets the value of the text node. It is assumed that the
     * proper encoding has already been taken care of to create the
     * text value.
     * 
     * @param encodedText {@link String }
     *     
     */
    void setTextValue(String encodedText);

    /**
     * Sets the value of the binary exchange as raw bytes.
     * The value that appears in the element will be encoded appropriately.
     *
     * @param rawText {@link byte[]}
     *
     */
    void setRawValue(byte[] rawText);

    /**
     * Sets the value of the valueType property.
     * 
     * @param valueType {@link String}
     *     
     */
    void setValueType(String valueType);
    
}
