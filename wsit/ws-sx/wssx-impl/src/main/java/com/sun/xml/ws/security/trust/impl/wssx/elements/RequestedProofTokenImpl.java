/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
 * $Id: RequestedProofTokenImpl.java,v 1.2 2010-10-21 15:37:05 snajper Exp $
 */

package com.sun.xml.ws.security.trust.impl.wssx.elements;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import com.sun.xml.ws.api.security.trust.WSTrustException;

import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;

import java.net.URI;
import java.net.URISyntaxException;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.ObjectFactory;

import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestedProofTokenType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.BinarySecretType;

import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import com.sun.xml.ws.security.trust.WSTrustVersion;

/**
 * @author Manveen Kaur
 */
public class RequestedProofTokenImpl extends RequestedProofTokenType implements RequestedProofToken {

    private String tokenType;
    private URI computedKey;
    private BinarySecret secret;
    private SecurityTokenReference str;
    
    public RequestedProofTokenImpl() {
        // empty constructor
    }

    public RequestedProofTokenImpl(String proofTokenType) {
        setProofTokenType(proofTokenType);
    }
    
    public RequestedProofTokenImpl (RequestedProofTokenType rptType){
        JAXBElement obj = (JAXBElement)rptType.getAny();
        String local = obj.getName().getLocalPart();
        if (local.equalsIgnoreCase("ComputedKey")) {
            try {
                setComputedKey(new URI((String)obj.getValue()));
            } catch (URISyntaxException ex){
                throw new RuntimeException(ex);
            } 
        }else if (local.equalsIgnoreCase("BinarySecret")){
            BinarySecretType bsType = (BinarySecretType)obj.getValue();
            setBinarySecret(new BinarySecretImpl(bsType));
        } else{
                throw new UnsupportedOperationException("Unsupported requested proof token: " + local);
        } 
    }
    
    public String getProofTokenType() {
        return tokenType;
    }

    public void setProofTokenType(String proofTokenType) {
        if (! (proofTokenType.equalsIgnoreCase(RequestedProofToken.BINARY_SECRET_TYPE)
            || proofTokenType.equalsIgnoreCase(RequestedProofToken.COMPUTED_KEY_TYPE)
            || proofTokenType.equalsIgnoreCase(RequestedProofToken.ENCRYPTED_KEY_TYPE)
            || proofTokenType.equalsIgnoreCase(RequestedProofToken.CUSTOM_TYPE)
            || proofTokenType.equalsIgnoreCase(RequestedProofToken.TOKEN_REF_TYPE)
            )) 
            // make this a WSTrustException?
        throw new RuntimeException("Invalid tokenType");
        tokenType = proofTokenType;
    }
    
    public void setSecurityTokenReference(SecurityTokenReference reference) {        
        if (reference != null) {
            str = reference;        
            JAXBElement<SecurityTokenReferenceType> strElement=  
                    (new com.sun.xml.ws.security.secext10.ObjectFactory()).createSecurityTokenReference((SecurityTokenReferenceType)reference); 
            setAny(strElement);
        }
        setProofTokenType(RequestedProofToken.TOKEN_REF_TYPE);
    }
    
    public SecurityTokenReference getSecurityTokenReference() {
        return str;
    }
    
    public void setComputedKey(URI computedKey) {
        if (computedKey != null) {
            String ckString = computedKey.toString();
            if (!(ckString.equalsIgnoreCase(WSTrustVersion.WS_TRUST_13.getCKHASHalgorithmURI()) || 
                    (ckString.equalsIgnoreCase(WSTrustVersion.WS_TRUST_13.getCKPSHA1algorithmURI())))) {
                throw new RuntimeException("Invalid computedKeyURI");
            }
            this.computedKey = computedKey;         
            JAXBElement<String> ckElement=  
                    (new ObjectFactory()).createComputedKey(computedKey.toString()); 
            setAny(ckElement);
        }
        setProofTokenType(RequestedProofToken.COMPUTED_KEY_TYPE);
    }
    
    public URI getComputedKey() {
        return computedKey;
    }
    
    public void setBinarySecret(BinarySecret secret) {
       if (secret != null) {
            this.secret = secret;        
            JAXBElement<BinarySecretType> bsElement=  
                    (new ObjectFactory()).createBinarySecret((BinarySecretType)secret); 
            setAny(bsElement);
        }
        setProofTokenType(RequestedProofToken.BINARY_SECRET_TYPE);
    }
     
    public BinarySecret getBinarySecret() {
        return secret;
    }
    
    public static RequestedProofTokenType fromElement(org.w3c.dom.Element element)
        throws WSTrustException {
        try {
            JAXBContext jc =
                JAXBContext.newInstance("com.sun.xml.ws.security.trust.impl.wssx.elements");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (RequestedProofTokenType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new WSTrustException(ex.getMessage(), ex);
        }
    }

}
