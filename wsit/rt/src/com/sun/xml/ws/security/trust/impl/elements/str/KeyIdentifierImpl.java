/*
 * $Id: KeyIdentifierImpl.java,v 1.4 2007-01-23 11:41:58 raharsha Exp $
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
        return URI.create(super.getValueType());
    }
    
    public URI getEncodingTypeURI (){
        return URI.create(super.getEncodingType());
    }
    
    public String getType(){
        return "KeyIdentifier";
    }
}
