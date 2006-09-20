/*
 * $Id: RequestedProofTokenImpl.java,v 1.3 2006-09-20 23:58:48 manveen Exp $
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

import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;

import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;

import java.net.URI;
import java.net.URISyntaxException;

import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory;

import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedProofTokenType;
import com.sun.xml.ws.security.trust.impl.bindings.BinarySecretType;

import com.sun.xml.ws.security.impl.bindings.SecurityTokenReferenceType;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

/**
 * @author Manveen Kaur
 */
public class RequestedProofTokenImpl extends RequestedProofTokenType implements RequestedProofToken {
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
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
    
    public RequestedProofTokenImpl(RequestedProofTokenType rptType){
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
        )) {
            log.log(Level.SEVERE,"WST0027.invalid.proofToken.type", proofTokenType);
            throw new RuntimeException("Invalid ProofToken Type: " + proofTokenType);
        }
        tokenType = proofTokenType;
    }
    
    public void setSecurityTokenReference(SecurityTokenReference reference) {
        if (reference != null) {
            str = reference;
            JAXBElement<SecurityTokenReferenceType> strElement=
                    (new com.sun.xml.ws.security.impl.bindings.ObjectFactory()).createSecurityTokenReference((SecurityTokenReferenceType)reference);
            setAny(strElement);
        }
        setProofTokenType(RequestedProofToken.TOKEN_REF_TYPE);
    }
    
    public SecurityTokenReference getSecurityTokenReference() {
        return str;
    }
    
    public void setComputedKey(URI computedKey) {
        
        if (computedKey == null) {
            log.log(Level.SEVERE,"WST0028.invalid.ck", "null");
            throw new RuntimeException("Null Computed Key specified");
        }
                
        if (computedKey != null) {
            String ckString = computedKey.toString();
            if (!(ckString.equalsIgnoreCase(WSTrustConstants.CK_HASH) || (ckString.equalsIgnoreCase(WSTrustConstants.CK_PSHA1)))) {
                log.log(Level.SEVERE,"WST0028.invalid.ck", ckString);
                throw new RuntimeException("Invalid computedKeyURI: " + ckString);
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
            javax.xml.bind.Unmarshaller u = WSTrustElementFactory.getContext().createUnmarshaller();
            return (RequestedProofTokenType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new WSTrustException(ex.getMessage(), ex);
        }
    }
    
}
