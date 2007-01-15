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

/*
 * GenericToken.java
 *
 * Created on February 15, 2006, 2:06 PM
 */

package com.sun.xml.ws.security.trust;

import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import java.util.UUID;

import com.sun.xml.ws.security.Token;

import org.w3c.dom.Element;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

/**
 *
 * @author Jiandong Guo
 */
public class GenericToken implements Token{
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    private Element token;
    
    //private JAXBElement tokenEle;
    
    private String tokenType;
    private SecurityHeaderElement she = null;
    private String id;
    
    public static final String OPAQUE_TYPE = "opaque";
    public static final String SAML11_TYPE =
            "urn:oasis:names:tc:SAML:1.1:assertion";
    
    /** Creates a new instance of GenericToken */
    public GenericToken(Element token) {
        this.token = token;
        id = token.getAttributeNS(null,"AssertionID");
        if(id == null || id.length() ==0){
            id = token.getAttributeNS(null,"ID");
        }
        if(id == null || id.length() ==0){
            id = token.getAttributeNS(null,"Id");
        }
        if(id == null || id.length() == 0){
            id = UUID.randomUUID().toString();
        }
    }
    
    public GenericToken(Element token, String tokenType){
        this(token);
        
        this.tokenType = tokenType;
    }
    
    public GenericToken(SecurityHeaderElement headerElement){
        this.she = headerElement;
    }
    
    public String getType(){
        if (tokenType != null) {
            if(log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                       LogStringsMessages.WST_1001_TOKEN_TYPE(tokenType)); 
            }
            return tokenType;
        }
        return OPAQUE_TYPE;
    }
    
    public Object getTokenValue(){
        return this.token;
    }
    
    public SecurityHeaderElement getElement(){
        return this.she;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
