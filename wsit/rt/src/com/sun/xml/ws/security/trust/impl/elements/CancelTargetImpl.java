/*
 * $Id: CancelTargetImpl.java,v 1.3 2007-01-12 14:44:10 raharsha Exp $
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
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.elements.CancelTarget;
import com.sun.xml.ws.security.trust.impl.bindings.CancelTargetType;
import javax.xml.bind.JAXBElement;

/**
 * Defines Binding for requesting security tokens to be cancelled.
 *
 * @author Manveen Kaur
 */
public class CancelTargetImpl extends CancelTargetType implements CancelTarget {
    
    private String targetType = null;
    
    // either STR will be present or the token will be
    // carried directly. This will typically be a BST.
    private SecurityTokenReference str = null;    
    private Token token = null;
        
    public CancelTargetImpl(SecurityTokenReference str) {
        setSecurityTokenReference(str);
        setTargetType(CancelTarget.STR_TARGET_TYPE);
    }
    
    public CancelTargetImpl(Token token) {
        setToken(token);
        setTargetType(CancelTarget.CUSTOM_TARGET_TYPE);
    }
    
    public CancelTargetImpl (CancelTargetType ctType){
        final JAXBElement obj = (JAXBElement)ctType.getAny();
        final String local = obj.getName().getLocalPart();
        if ("SecurityTokenReference".equals(local)) {
            final SecurityTokenReference str = 
                        new SecurityTokenReferenceImpl((SecurityTokenReferenceType)obj.getValue());
            setSecurityTokenReference(str);
            setTargetType(CancelTarget.STR_TARGET_TYPE);
        } else {
            //ToDo
        } 
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public final void setTargetType(final String ttype) {
        targetType = ttype;
    }
    
    public final void setSecurityTokenReference(final SecurityTokenReference ref) {
        if (ref != null) {
            str = ref;
            final JAXBElement<SecurityTokenReferenceType> strElement=
                    (new com.sun.xml.ws.security.secext10.ObjectFactory()).createSecurityTokenReference((SecurityTokenReferenceType)ref);
            setAny(strElement);
        }
        setTargetType(CancelTarget.STR_TARGET_TYPE);
        token = null;        
    }
    
    public SecurityTokenReference getSecurityTokenReference() {
        return str;
    }
    
    public final void setToken(final Token token) {
        if (token != null) {
            this.token = token;
            setAny(token);
        }
        setTargetType(CancelTarget.CUSTOM_TARGET_TYPE);                
        str = null;
    }
    
    public Token getToken() {
        return token;
    }
    
}
