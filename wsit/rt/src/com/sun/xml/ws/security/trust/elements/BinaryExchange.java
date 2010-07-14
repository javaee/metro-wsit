/*
 * $Id: BinaryExchange.java,v 1.3.22.2 2010-07-14 14:00:09 m_potociar Exp $
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
