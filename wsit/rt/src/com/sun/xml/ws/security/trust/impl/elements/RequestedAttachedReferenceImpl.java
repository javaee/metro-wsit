/*
 * $Id: RequestedAttachedReferenceImpl.java,v 1.1 2006-05-03 22:57:27 arungupta Exp $
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

import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.trust.impl.elements.str.SecurityTokenReferenceImpl;
import com.sun.xml.ws.security.impl.bindings.SecurityTokenReferenceType;

import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedReferenceType;

/**
 * Implementation for RequestedAttachedReference.
 * 
 * @author Manveen Kaur
 */
public class RequestedAttachedReferenceImpl extends RequestedReferenceType implements RequestedAttachedReference {

    SecurityTokenReference str = null;
    
    public RequestedAttachedReferenceImpl() {
        // empty constructor    
    }

    public RequestedAttachedReferenceImpl(SecurityTokenReference str) {
        setSTR(str);
    }
    
    public RequestedAttachedReferenceImpl(RequestedReferenceType rrType) throws Exception {
        this(new SecurityTokenReferenceImpl(rrType.getSecurityTokenReference()));
    }
    
    public SecurityTokenReference getSTR() {
        return str;
    }

    public void setSTR(SecurityTokenReference str) {
        if (str != null) {
            setSecurityTokenReference((SecurityTokenReferenceType)str);
        }
        this.str = str;
    }    
}
