/*
* $Id: ClaimsImpl.java,v 1.2 2008-02-21 22:48:27 jdg6688 Exp $
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

package com.sun.xml.ws.security.trust.impl.wssx.elements;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.sun.xml.ws.api.security.trust.WSTrustException;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.ClaimsType;
import java.util.ArrayList;

/**
 * Implementation class for Claims.
 *
 * @author Manveen Kaur
 */
public class ClaimsImpl extends ClaimsType implements Claims {

    List<Object> supportingInfo = new ArrayList<Object>();
    
    public ClaimsImpl() {
        // default constructor
    }

    public ClaimsImpl(String dialect) {
        setDialect(dialect);
    }
    
    public ClaimsImpl(ClaimsType clType)throws Exception{
        setDialect(clType.getDialect());
    }

    public static ClaimsType fromElement(org.w3c.dom.Element element)
        throws WSTrustException {
        try {
            JAXBContext jc =
                JAXBContext.newInstance("com.sun.xml.ws.security.trust.impl.wssx.elements");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (ClaimsType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new WSTrustException(ex.getMessage(), ex);
        }
    }

    public List<Object> getSupportingProperties() {
        return supportingInfo;
    }
}
