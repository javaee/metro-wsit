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

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.MessageLayout;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.sun.xml.ws.security.impl.policy.Constants.*;



/**
 *
 * @author K.Venugopal@sun.com
 */

public class AsymmetricBinding extends com.sun.xml.ws.policy.PolicyAssertion implements com.sun.xml.ws.security.policy.AsymmetricBinding, SecurityAssertionValidator {
    
    private static Logger logger = Logger.getLogger(Constants.WS_SECURITY_POLICY_DOMAIN,Constants.WS_SECURITY_POLICY_DOMAIN_BUNDLE);
    private Token initiatorToken;
    private Token recipientToken;
    private AlgorithmSuite algSuite;
    private boolean includeTimestamp = false;
    private boolean contentOnly = true;
    private  MessageLayout layout = MessageLayout.Lax;
    private String protectionOrder = SIGN_ENCRYPT;
    private boolean protectToken = false;
    private boolean protectSignature = false;
    private boolean populated = false;
    private boolean isServer = false;
    
    /**
     * Creates a new instance of AsymmetricBinding
     */
    public AsymmetricBinding() {
    }
    
    public AsymmetricBinding(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public Token getRecipientToken() {
        populate();
        return recipientToken;
    }
    
    public Token getInitiatorToken() {
        populate();
        return initiatorToken;
    }
    
    public void setAlgorithmSuite(AlgorithmSuite algSuite) {
        this.algSuite = algSuite;
    }
    
    public AlgorithmSuite getAlgorithmSuite() {
        populate();
        return algSuite;
    }
    
    public void includeTimeStamp(boolean value) {
        populate();
        this.includeTimestamp = value;
    }
    
    public boolean isIncludeTimeStamp() {
        populate();
        return includeTimestamp;
    }
    
    public void setLayout(MessageLayout layout) {
        this.layout = layout;
    }
    
    public MessageLayout getLayout() {
        populate();
        return layout;
    }
    
//    public QName getName() {
//        return Constants._AsymmetricBinding_QNAME;
//    }
    
    public void setInitiatorToken(Token token) {
        this.initiatorToken = token;
    }
    
    public void setRecipientToken(Token token) {
        this.recipientToken = token;
    }
    
    public boolean isSignContent() {
        populate();
        return contentOnly;
    }
    
    public void setSignContent(boolean contentOnly) {
        this.contentOnly = contentOnly;
    }
    
    public void setProtectionOrder(String order) {
        this.protectionOrder = order;
    }
    
    public String getProtectionOrder() {
        populate();
        return protectionOrder;
    }
    
    public void setTokenProtection(boolean value) {
        this.protectToken = value;
    }
    
    public void setSignatureProtection(boolean value) {
        this.protectSignature = value;
    }
    
    public boolean getTokenProtection() {
        populate();
        return protectToken;
    }
    
    public boolean getSignatureProtection() {
        populate();
        return protectSignature;
    }
    
    public boolean validate() {
        try{
            populate();
            return true;
        }catch(UnsupportedPolicyAssertion upaex) {
            return false;
        }
    }
    
    
    private void populate(){
        if(populated){
            return;
        }
        synchronized (this.getClass()){
            if(!populated){
                NestedPolicy policy = this.getNestedPolicy();
                if(policy == null){
                    if(logger.getLevel() == Level.FINE){
                        logger.log(Level.FINE,"NestedPolicy is null");
                    }
                    populated = true;
                    return;
                }
                AssertionSet as = policy.getAssertionSet();
                Iterator<PolicyAssertion> ast = as.iterator();
                while(ast.hasNext()){
                    PolicyAssertion assertion = ast.next();
                    if(PolicyUtil.isInitiatorToken(assertion)){
                        this.initiatorToken = ((com.sun.xml.ws.security.impl.policy.Token)assertion).getToken();
                    }else if(PolicyUtil.isRecipientToken(assertion)){
                        this.recipientToken = ((com.sun.xml.ws.security.impl.policy.Token)assertion).getToken();
                    }else if(PolicyUtil.isAlgorithmAssertion(assertion)){
                        this.algSuite = (AlgorithmSuite) assertion;
                    }else if(PolicyUtil.isIncludeTimestamp(assertion)){
                        this.includeTimestamp = true;
                    }else if(PolicyUtil.isProtectionOrder(assertion)){
                        this.protectionOrder = ENCRYPT_SIGN;
                    }else if(PolicyUtil.isContentOnlyAssertion(assertion)){
                        this.contentOnly = false;
                    }else if(PolicyUtil.isMessageLayout(assertion)){
                        layout = ((Layout)assertion).getMessageLayout();
                    }else if(PolicyUtil.isProtectTokens(assertion)){
                        this.protectToken = true;
                    }else if(PolicyUtil.isEncryptSignature(assertion)){
                        this.protectSignature = true;
                    }else{
                        if(!assertion.isOptional()){
                            if(logger.getLevel() == Level.SEVERE){
                                logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"AsymmetricBinding"});
                            }
                            if(isServer){
                                throw new UnsupportedPolicyAssertion("Policy assertion "+
                                          assertion+" is not supported under AsymmetricBinding assertion");
                            }
                        }
                    }
                }
                
                populated = true;
            }
        }
    }
    
    
}
