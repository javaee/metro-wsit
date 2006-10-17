/*
 * $Id: KeyIdentifierImpl.java,v 1.2 2006-10-17 05:45:47 raharsha Exp $
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

package com.sun.xml.ws.security.trust.impl.elements.str;

import com.sun.xml.ws.security.secext10.KeyIdentifierType;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * KeyIdentifier implementation
 */
public class KeyIdentifierImpl extends KeyIdentifierType implements KeyIdentifier {
    
    
    public KeyIdentifierImpl() {
        // default c'tor
    }

    public KeyIdentifierImpl(String valueType, String encodingType) {
        setValueType(valueType);
        setEncodingType(encodingType);
    }
    
    public KeyIdentifierImpl(KeyIdentifierType kidType){
        this(kidType.getValueType(), kidType.getEncodingType());
        setValue(kidType.getValue());
    }
    
    public URI getValueTypeURI(){
        URI valueType = null;
        try {
             valueType = new URI(super.getValueType());
        } catch (URISyntaxException ex){
            throw new RuntimeException(ex);
        }
        
        return valueType;
    }
    
    public URI getEncodingTypeURI (){
        URI encType = null;
        try {
             encType = new URI(super.getEncodingType());
        } catch (URISyntaxException ex){
            throw new RuntimeException(ex);
        }
        
        return encType;
    }
    
    public void setValue (String value){
        super.setValue(value);
    }
    
    public String getValue (){
        return super.getValue();
    }
    
    public String getType(){
        return "KeyIdentifier";
    }
}
