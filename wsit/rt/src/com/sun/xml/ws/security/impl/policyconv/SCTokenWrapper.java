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

package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.policy.AsymmetricBinding;
import com.sun.xml.ws.security.policy.Claims;
import com.sun.xml.ws.security.policy.Issuer;
import com.sun.xml.ws.security.policy.IssuerName;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.SymmetricBinding;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SCTokenWrapper extends PolicyAssertion implements SecureConversationToken{
    
    private SecureConversationToken scToken = null;
    private MessagePolicy messagePolicy = null;
    private List<PolicyAssertion> issuedTokenList = null;
    private List<PolicyAssertion> kerberosTokenList = null;
    private boolean cached = false;
    private SecurityPolicyVersion spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
    
    /** Creates a new instance of SCTokenWrapper */
    public SCTokenWrapper(PolicyAssertion scToken,MessagePolicy mp) {
        super(AssertionData.createAssertionData(
                                scToken.getName(),
                                scToken.getValue(),
                                scToken.getAttributes(),
                                scToken.isOptional(),
                                scToken.isIgnorable()
                            ),
                getAssertionParameters(scToken),
                (scToken.getNestedPolicy()== null ? null : scToken.getNestedPolicy().getAssertionSet()));
        this.scToken = (SecureConversationToken)scToken;
        this.messagePolicy = mp;
        
        String nsUri = scToken.getName().getNamespaceURI();
        if(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri.equals(nsUri)){
            spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
        } else if(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri.equals(nsUri)){
            spVersion = SecurityPolicyVersion.SECURITYPOLICY12NS;
        }
    }
    
    private static Collection<PolicyAssertion> getAssertionParameters(PolicyAssertion scToken){
        if(scToken.hasParameters()){
            Iterator<PolicyAssertion> itr = scToken.getParametersIterator();
            if(itr.hasNext()){// will have only one assertion set. TODO:Cross check with marek.
                return Collections.singletonList(itr.next());
            }
        }
        return null;
        
    }
    
    public SecureConversationToken getSecureConversationToken() {
        return scToken;
    }
    
    public void setSecureConversationToken(SecureConversationToken scToken) {
        this.scToken = scToken;
    }
    
    public MessagePolicy getMessagePolicy() {
        return messagePolicy;
    }
    
    public void setMessagePolicyp(MessagePolicy mp) {
        this.messagePolicy = mp;
    }
    
    
    public boolean isRequireDerivedKeys() {
        return this.scToken.isRequireDerivedKeys();
    }
    
    public boolean isMustNotSendCancel() {
        return this.scToken.isMustNotSendCancel();
    }
    
    public boolean isMustNotSendRenew() {
        return this.scToken.isMustNotSendRenew();
    }
    
    public String getTokenType() {
        return this.scToken.getTokenType();
    }
    
    public Issuer getIssuer() {
        return this.scToken.getIssuer();
    }
    
    public IssuerName getIssuerName() {
        return this.scToken.getIssuerName();
    }
    
    public Claims getClaims(){
        return this.scToken.getClaims();
    }
    
    public NestedPolicy getBootstrapPolicy() {
        return this.scToken.getBootstrapPolicy();
    }
    
    
    public String getIncludeToken() {
        return this.scToken.getIncludeToken();
    }
    
    public String getTokenId() {
        return this.scToken.getTokenId();
    }
    
    
    public List<PolicyAssertion> getIssuedTokens(){
        if(!cached){
            if(this.hasNestedPolicy()){
                getTokens(this.getNestedPolicy());
                cached = true;
            }
        }
        return issuedTokenList;
    }

    public List<PolicyAssertion> getKerberosTokens(){
        if(!cached){
            if(this.hasNestedPolicy()){
                getTokens(this.getNestedPolicy());
                cached = true;
            }
        }
        return kerberosTokenList;
    }
    
    private void getTokens(NestedPolicy policy){
        issuedTokenList = new ArrayList<PolicyAssertion>();
        kerberosTokenList = new ArrayList<PolicyAssertion>();
        AssertionSet assertionSet = policy.getAssertionSet();
        for(PolicyAssertion pa:assertionSet){
            if(PolicyUtil.isBootstrapPolicy(pa, spVersion)){
                NestedPolicy np = pa.getNestedPolicy();
                AssertionSet bpSet = np.getAssertionSet();
                for(PolicyAssertion assertion:bpSet){
                    if(PolicyUtil.isAsymmetricBinding(assertion, spVersion)){
                        AsymmetricBinding sb =  (AsymmetricBinding)assertion;
                         Token iToken = sb.getInitiatorToken();
                        if (iToken != null){
                            addToken(iToken);
                        }else{
                            addToken(sb.getInitiatorSignatureToken());
                            addToken(sb.getInitiatorEncryptionToken());
                        }

                        Token rToken = sb.getRecipientToken();
                        if (rToken != null){
                            addToken(rToken);
                        }else{
                            addToken(sb.getRecipientSignatureToken());
                            addToken(sb.getRecipientEncryptionToken());
                        }
                    }else if(PolicyUtil.isSymmetricBinding(assertion, spVersion)){
                        SymmetricBinding sb = (SymmetricBinding)assertion;
                        Token token = sb.getProtectionToken();
                        if(token != null){
                            addToken(token);
                        }else{
                            addToken(sb.getEncryptionToken());
                            addToken(sb.getSignatureToken());
                        }
                    }else if(PolicyUtil.isSupportingTokens(assertion, spVersion)){
                        SupportingTokens st = (SupportingTokens)assertion;
                        Iterator itr = st.getTokens();
                        while(itr.hasNext()){
                            addToken((Token)itr.next());
                        }
                    }
                }
            }
            
        }
    }
    
    private void addToken(Token token){
        if (token != null) {
            if (PolicyUtil.isIssuedToken((PolicyAssertion) token, spVersion)) {
                issuedTokenList.add((PolicyAssertion) token);
            } else if (PolicyUtil.isKerberosToken((PolicyAssertion) token, spVersion)) {
                kerberosTokenList.add((PolicyAssertion) token);
            }
        }
    }
    
    public Set getTokenRefernceTypes() {
        return this.scToken.getTokenRefernceTypes();
    }
    
    public void addBootstrapPolicy(NestedPolicy policy) {
    }

    public SecurityPolicyVersion getSecurityPolicyVersion() {
        return spVersion;
    }
}
