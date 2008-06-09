/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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


package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.Lifetime;
import java.util.Collection;
import java.util.Iterator;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
/**
 *
 * @author K.Venugopal@sun.com Abhijit.Das@Sun.COM
 */
public class RequestSecurityTokenTemplate extends PolicyAssertion implements com.sun.xml.ws.security.policy.RequestSecurityTokenTemplate, SecurityAssertionValidator {
    
    private boolean populated = false;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    String tokenType;
    String requestType;
    Lifetime lifeTime;
    String authenticationType;
    private String keyType;
    private int keySize;
    private String sigAlgo;
    private String encAlgo;
    private String canonAlgo;
    private boolean isProofEncRequired = false;
    private String computedKeyAlgo;
    private boolean isEncRequired = false;
    private String signWith;
    private String encryptWith;
    private String keyWrapAlgo;
    private String wstVer;
    private Claims claims = null;
    
    /**
     * Creates a new instance of RequestSecurityTokenTemplate
     */
    public RequestSecurityTokenTemplate() {
    }
    
    public RequestSecurityTokenTemplate(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public String getTokenType() {
        populate();
        return tokenType;
    }
    
    public String getRequestType() {
        populate();
        return this.requestType;
    }
    
    public Lifetime getLifetime() {
        populate();
        return lifeTime;
    }
    
    
    public String getAuthenticationType() {
        populate();
        return authenticationType;
    }
    
    
    public String getKeyType() {
        populate();
        return keyType;
    }
    
    public int getKeySize() {
        populate();
        return keySize;
    }
    
    
    
    public String getSignatureAlgorithm() {
        populate();
        return sigAlgo;
    }
    
    
    public String getEncryptionAlgorithm() {
        populate();
        return encAlgo;
    }
    
    
    public String getCanonicalizationAlgorithm() {
        populate();
        return canonAlgo;
    }
    
    
    public boolean getProofEncryptionRequired() {
        populate();
        return isProofEncRequired;
    }
    
    
    
    public String getComputedKeyAlgorithm() {
        populate();
        return computedKeyAlgo;
    }
    
    public String getKeyWrapAlgorithm() {
        populate();
        return keyWrapAlgo;
    }    
    
    public boolean getEncryptionRequired() {
        populate();
        return isEncRequired;
    }
    
    
    
    public String getSignWith() {
        populate();
        return signWith;
    }
    
    
    public String getEncryptWith() {
        populate();
        return encryptWith;
    }
    
    public Claims getClaims(){
        populate();
        return claims;
    }
    
    public String getTrustVersion() {
        populate();
        return wstVer;
    }
    
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        if(!populated){
            if ( this.hasNestedAssertions() ) {
                
                Iterator <PolicyAssertion> it =this.getNestedAssertionsIterator();
                while( it.hasNext() ) {
                    PolicyAssertion assertion = (PolicyAssertion) it.next();
                    if (this.wstVer == null){
                        this.wstVer = assertion.getName().getNamespaceURI();
                    }
                    //TODO: Support all RequestSecurityTokenTemplate elements
                    if ( PolicyUtil.isKeyType(assertion) ) {
                        this.keyType = assertion.getValue();
                    } else if ( PolicyUtil.isKeySize(assertion) ) {
                        this.keySize = Integer.valueOf(assertion.getValue());
                    }  else if ( PolicyUtil.isEncryption(assertion) ) {
                        this.isEncRequired = true;
                    } else if ( PolicyUtil.isProofEncryption(assertion) ) {
                        this.isProofEncRequired = true;
                    } else if ( PolicyUtil.isLifeTime(assertion) ) {
                        this.lifeTime = (Lifetime) assertion;
                    }else if(PolicyUtil.isSignWith(assertion)){
                        this.signWith = assertion.getValue();
                    }else if(PolicyUtil.isEncryptWith(assertion)){
                        this.encryptWith = assertion.getValue();
                    }else if(PolicyUtil.isTrustTokenType(assertion)){
                        this.tokenType = assertion.getValue();
                    }else if(PolicyUtil.isRequestType(assertion)){
                        this.requestType = assertion.getValue();
                    }else if(PolicyUtil.isAuthenticationType(assertion)){
                        this.authenticationType = assertion.getValue();
                    }else if(PolicyUtil.isSignatureAlgorithm(assertion)){
                        this.sigAlgo = assertion.getValue();
                    }else if(PolicyUtil.isEncryptionAlgorithm(assertion)){
                        this.encAlgo = assertion.getValue();
                    }else if(PolicyUtil.isCanonicalizationAlgorithm(assertion)){
                        this.canonAlgo = assertion.getValue();
                    }else if(PolicyUtil.isComputedKeyAlgorithm(assertion)){
                        this.computedKeyAlgo = assertion.getValue();
                    }else if(PolicyUtil.isKeyWrapAlgorithm(assertion)){
                        this.keyWrapAlgo = assertion.getValue();
                    }else if(PolicyUtil.isEncryption(assertion)){
                        isEncRequired = true;
                    }else if(PolicyUtil.isClaimsElement(assertion)) {
                        claims = (Claims)assertion;
                    }else if(PolicyUtil.isEntropyElement(assertion)){
                        // Valid assertion.
                    }else {
                        if(!assertion.isOptional()){
                            log_invalid_assertion(assertion, isServer,RequestSecurityTokenTemplate);
                            fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                        }
                    }
                    
                }
            }
            populated = true;
        }
        return fitness;
    }
}
