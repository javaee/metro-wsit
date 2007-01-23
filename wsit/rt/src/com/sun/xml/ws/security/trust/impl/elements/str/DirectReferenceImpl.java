/*
 * $Id: DirectReferenceImpl.java,v 1.3 2007-01-23 11:41:58 raharsha Exp $
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

import com.sun.xml.ws.security.secext10.ReferenceType;
import com.sun.xml.ws.security.trust.elements.str.DirectReference;

import java.net.URI;

/**
 * Reference Interface
 */
public class DirectReferenceImpl extends ReferenceType implements DirectReference {

    public DirectReferenceImpl(String valueType, String uri){
        setValueType(valueType);
        setURI(uri);
    }
    
    public DirectReferenceImpl(ReferenceType refType){
        this(refType.getValueType(), refType.getURI());
    }

    public URI getURIAttr(){
        return URI.create(super.getURI());
    }

    public URI getValueTypeURI(){
        return URI.create(super.getValueType());
    }
    
    public String getType(){
        return "Reference";
    }

}
