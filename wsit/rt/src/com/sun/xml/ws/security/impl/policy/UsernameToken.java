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
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import com.sun.xml.ws.api.security.policy.SecurityPolicyVersion;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class UsernameToken extends PolicyAssertion implements com.sun.xml.ws.security.policy.UserNameToken, java.lang.Cloneable, SecurityAssertionValidator {
    
    private String tokenType;
    private String id;
    private boolean populated;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    private boolean hasPassword = true;
    private boolean useHashPassword = false;
    private SecurityPolicyVersion spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
    private final QName itQname;
    private String includeToken;
    private Issuer issuer = null;
    private IssuerName issuerName = null;
    private Claims claims = null;
    private boolean reqDK=false;    
    
    /**
     * Creates a new instance of UsernameToken
     */
    public UsernameToken() {
         id= PolicyUtil.randomUUID();
         itQname = new QName(spVersion.namespaceUri, Constants.IncludeToken);
         includeToken = spVersion.includeTokenAlways;        
    }
    
    public UsernameToken(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        id= PolicyUtil.randomUUID();
        String nsUri = getName().getNamespaceURI();
        spVersion = PolicyUtil.getSecurityPolicyVersion(nsUri);
        itQname = new QName(spVersion.namespaceUri, Constants.IncludeToken);
        includeToken = spVersion.includeTokenAlways;        
    }    
    
    public void setType(String type) {
        this.tokenType = type;
    }
    
    public String getType() {
        populate();
        return tokenType;
    }
    
    
    public String getTokenId() {
        return id;
    }
    
    public void setTokenId(String _id) {
        this.id = _id;
    }
    
    public String getIncludeToken() {
        populate();
        return  includeToken;
    }
    
    public void setIncludeToken(String type) {
        Map<QName, String> attrs = this.getAttributes();
        QName itQname = new QName(spVersion.namespaceUri, Constants.IncludeToken);
        attrs.put(itQname,type);
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
    public boolean hasPassword(){
        return hasPassword;
    }
    
    public boolean useHashPassword(){
        return useHashPassword;
    }
    
    public boolean isRequireDerivedKeys() {
        populate();
        return reqDK;
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        if(!populated){
            if(this.getAttributeValue(itQname) != null){
                this.includeToken = this.getAttributeValue(itQname);
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
                if (PolicyUtil.isUsernameTokenType(assertion, spVersion)) {
                    tokenType = assertion.getName().getLocalPart();
                } else if (PolicyUtil.hasPassword(assertion, spVersion)) {
                    hasPassword = false;
                } else if(PolicyUtil.isHashPassword(assertion, spVersion)){
                    useHashPassword = true;
                } else if (PolicyUtil.isRequireDerivedKeys(assertion, spVersion)){
                       reqDK = true;
                }else{
                    if(!assertion.isOptional()){
                        log_invalid_assertion(assertion, isServer,"UsernameToken");
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
    
    public Object clone() throws CloneNotSupportedException  {
        throw new UnsupportedOperationException();
    }

    public SecurityPolicyVersion getSecurityPolicyVersion() {
        return spVersion;
    }

    public Set getTokenRefernceType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
