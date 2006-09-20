/*
 * $Id: IssuerImpl.java,v 1.2 2006-09-20 23:58:48 manveen Exp $
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

import javax.xml.ws.addressing.EndpointReference;
import com.sun.xml.ws.addressing.v200408.EndpointReferenceImpl;

import com.sun.xml.ws.security.trust.elements.Issuer;

/**
 * Implementation of wst:Issuer. 
 *
 * @author Manveen Kaur
 */
public class IssuerImpl extends EndpointReferenceImpl implements Issuer {

    EndpointReference epr = null;

    public IssuerImpl() {
    }

    public IssuerImpl(EndpointReference epr) {
         setEndpointReference(epr);
    }
    
    public IssuerImpl(EndpointReferenceImpl isType) throws Exception{
        // ToDo
    }
    
    public EndpointReference getEndpointReference() {
        return epr;
    }

    public void setEndpointReference(EndpointReference endpointReference) {
        epr = endpointReference;                
        // ToDo
    }
}
