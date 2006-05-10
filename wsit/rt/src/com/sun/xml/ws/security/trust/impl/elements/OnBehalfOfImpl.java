/*
 * $Id: OnBehalfOfImpl.java,v 1.2 2006-05-10 23:11:53 jdg6688 Exp $
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

package com.sun.xml.ws.security.trust.impl.elements;

import com.sun.xml.ws.addressing.EndpointReferenceImpl;
import javax.xml.ws.addressing.EndpointReference;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.impl.bindings.SecurityTokenReferenceType;
import com.sun.xml.ws.security.trust.elements.OnBehalfOf;
import com.sun.xml.ws.security.trust.impl.bindings.OnBehalfOfType;
import javax.xml.bind.JAXBElement;

/**
 *
 * @author Manveen Kaur
 */
public class OnBehalfOfImpl extends OnBehalfOfType implements OnBehalfOf {
    
    private EndpointReference epr = null;
    private SecurityTokenReference str = null;
    
    public OnBehalfOfImpl(OnBehalfOfType oboType)throws Exception{
        //ToDo
    }
    public EndpointReference getEndpointReference() {
        return epr;
    }
    
    public void setEndpointReference(EndpointReference endpointReference) {
        epr = endpointReference;
        if (endpointReference != null) {
            JAXBElement<EndpointReferenceImpl> eprElement=
                    (new com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory()).
                    createEndpointReference((EndpointReferenceImpl)endpointReference);
            setAny(eprElement);
        }
        str = null;
    }
    
    public void setSecurityTokenReference(SecurityTokenReference ref) {
        str = ref;
        if (ref != null) {
            JAXBElement<SecurityTokenReferenceType> strElement=
                    (new com.sun.xml.ws.security.impl.bindings.ObjectFactory()).createSecurityTokenReference((SecurityTokenReferenceType)ref);
            setAny(strElement);
        }
        epr = null;
    }
    
    public SecurityTokenReference getSecurityTokenReference() {
        return str;
    }
    
}
