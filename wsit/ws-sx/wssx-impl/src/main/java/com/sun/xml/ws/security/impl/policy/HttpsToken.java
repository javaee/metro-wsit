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

package com.sun.xml.ws.security.impl.policy;


import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import java.util.Collection;

import java.util.Map;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator.AssertionFitness;
import java.util.Iterator;


/**
 *
 * @author K.Venugopal@sun.com
 */
public class HttpsToken extends PolicyAssertion implements com.sun.xml.ws.security.policy.HttpsToken, SecurityAssertionValidator{
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    private boolean populated = false;
    private boolean requireCC = false;
    private boolean httpBasicAuthentication = false;
    private boolean httpDigestAuthentication = false;
    private String id = "";
    private SecurityPolicyVersion spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
    private final QName rccQname;
    private Issuer issuer = null;
    private IssuerName issuerName = null;
    private Claims claims = null;
    /**
     * Creates a new instance of HttpsToken
     */
    public HttpsToken() {
        id= PolicyUtil.randomUUID();
        rccQname = new QName(spVersion.namespaceUri, Constants.RequireClientCertificate);
    }
    
    public HttpsToken(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        id= PolicyUtil.randomUUID();
        String nsUri = getName().getNamespaceURI();
        spVersion = PolicyUtil.getSecurityPolicyVersion(nsUri);
        rccQname = new QName(spVersion.namespaceUri, Constants.RequireClientCertificate);
    }
    
    public void setRequireClientCertificate(boolean value) {
        Map<QName, String> attrs = this.getAttributes();
        QName rccQname = new QName(spVersion.namespaceUri, Constants.RequireClientCertificate);
        attrs.put(rccQname,Boolean.toString(value));
        requireCC = value;
    }
    
    public boolean isRequireClientCertificate() {
        populate();
        return this.requireCC;
    }
    
    public String getIncludeToken() {
        throw new UnsupportedOperationException("This method is not supported for HttpsToken");
    }
    
    public void setIncludeToken(String type) {
        throw new UnsupportedOperationException("This method is not supported for HttpsToken");
    }
    
    public String getTokenId() {
        return id;
    }
    
    public Issuer getIssuer() {
        populate();
        return issuer;
    }
    
    public IssuerName getIssuerName() {
        populate();
        return issuerName;
    }
    
    public Claims getClaims(){
        populate();
        return claims;
    }
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        
        if(!populated){
            if(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri.equals(
                    spVersion.namespaceUri)){
                String value = this.getAttributeValue(rccQname);
                requireCC = Boolean.valueOf(value);
            }
            NestedPolicy policy = this.getNestedPolicy();
            if(policy == null){
                if(logger.getLevel() == Level.FINE){
                    logger.log(Level.FINE,"NestedPolicy is null");
                }
                populated = true;
                return fitness;
            }
            AssertionSet assertionSet = policy.getAssertionSet();
            for(PolicyAssertion assertion: assertionSet){
               if(PolicyUtil.isRequireClientCertificate(assertion, spVersion)){
                   requireCC = true;
               } else if(PolicyUtil.isHttpBasicAuthentication(assertion, spVersion)){
                   httpBasicAuthentication = true;
               } else if(PolicyUtil.isHttpDigestAuthentication(assertion, spVersion)){
                   httpDigestAuthentication = true;
               }else{
                    if(!assertion.isOptional()){
                        log_invalid_assertion(assertion, isServer,"HttpsToken");
                        fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                    }
                }
            }
            if ( this.hasParameters() ) {
                Iterator <PolicyAssertion> it = this.getParametersIterator();
                while(it.hasNext()){
                    PolicyAssertion assertion = it.next();
                    if(PolicyUtil.isIssuer(assertion, spVersion)){
                        issuer = (Issuer)assertion;
                    } else if(PolicyUtil.isIssuerName(assertion, spVersion)){
                        issuerName = (IssuerName)assertion;
                    } else if(PolicyUtil.isClaimsElement(assertion) && 
                            SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri.equals(spVersion.namespaceUri) ){
                        claims = (Claims)assertion;
                    }
                }
            }
            if(issuer != null && issuerName != null){
                log_invalid_assertion(issuerName, isServer,SecureConversationToken);
                fitness = AssertionFitness.HAS_INVALID_VALUE;
            }
            populated = true;
        }
        return fitness;
        
    }
    
    public SecurityPolicyVersion getSecurityPolicyVersion() {
        return spVersion;
    }

    public boolean isHttpBasicAuthentication() {
        populate();
        if(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri.equals(
                spVersion.namespaceUri)){
            throw new UnsupportedOperationException("HttpBasicAuthentication is only supported for" +
                    "SecurityPolicy 1.2 and later");
        }
        return httpBasicAuthentication;
    }

    public boolean isHttpDigestAuthentication() {
        populate();
        if(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri.equals(
                spVersion.namespaceUri)){
            throw new UnsupportedOperationException("HttpDigestAuthentication is only supported for" +
                    "SecurityPolicy 1.2 and later");
        }
        return httpDigestAuthentication;
    }
}
