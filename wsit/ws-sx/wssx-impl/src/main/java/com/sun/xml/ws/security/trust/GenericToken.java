/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
import javax.xml.bind.JAXBElement;

/**
 *
 * @author Jiandong Guo
 */
public class GenericToken implements Token{
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    private Object token;
    
    //private JAXBElement tokenEle;
    
    private String tokenType;
    private SecurityHeaderElement she = null;
    private String id;
    
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

    public GenericToken(JAXBElement token){
        this.token = token;
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
        return WSTrustConstants.OPAQUE_TYPE;
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
