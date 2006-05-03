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
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.HttpsToken;
import com.sun.xml.ws.security.policy.MessageLayout;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
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
public class TransportBinding extends PolicyAssertion implements com.sun.xml.ws.security.policy.TransportBinding, SecurityAssertionValidator{
    
    HttpsToken transportToken;
    private AlgorithmSuite algSuite;
    boolean includeTimeStamp=true;
    MessageLayout layout = MessageLayout.Lax;
    boolean populated = false;
    /**
     * Creates a new instance of TransportBinding
     */
    public TransportBinding() {
    }
    
    public TransportBinding(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public void addTransportToken(Token token) {
        transportToken = (HttpsToken) token;
    }
    
    public Token getTransportToken() {
        populate();
        return transportToken;
    }
    
    public void setAlgorithmSuite(AlgorithmSuite algSuite) {
        this.algSuite = algSuite;
    }
    
    public AlgorithmSuite getAlgorithmSuite() {
        populate();
        return algSuite;
    }
    
    public void includeTimeStamp(boolean value) {
        includeTimeStamp = value;
    }
    
    public boolean isIncludeTimeStamp() {
        populate();
        return includeTimeStamp;
    }
    
    public void setLayout(MessageLayout layout) {
        this.layout = layout;
    }
    
    public MessageLayout getLayout() {
        populate();
        return layout;
    }
    
    public boolean isSignContent() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    public void setSignContent(boolean contentOnly) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    public void setProtectionOrder(String order) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    public String getProtectionOrder() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    public void setTokenProtection(boolean token) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    public void setSignatureProtection(boolean token) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    public boolean getTokenProtection() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    public boolean getSignatureProtection() {
        throw new UnsupportedOperationException("Not supported");
    }
    
//    public QName getName() {
//        return Constants._TransportBinding_QNAME;
//    }
    
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
                AssertionSet assertions = policy.getAssertionSet();
                if(assertions == null){
                    if(logger.getLevel() == Level.FINE){
                        logger.log(Level.FINE,"NestedPolicy is null");
                    }
                    populated = true;
                    return;
                }
                for(PolicyAssertion assertion : assertions){
                    if(PolicyUtil.isAlgorithmAssertion(assertion)){
                        this.algSuite = (AlgorithmSuite) assertion;
                    }else if(PolicyUtil.isToken(assertion)){
                        transportToken = (HttpsToken)((com.sun.xml.ws.security.impl.policy.Token)assertion).getToken();
                    }else if(PolicyUtil.isMessageLayout(assertion)){
                        layout = ((Layout)assertion).getMessageLayout();
                    }else if(PolicyUtil.isIncludeTimestamp(assertion)){
                        includeTimeStamp=true;
                    } else{
                        if(!assertion.isOptional()){
                            if(logger.getLevel() == Level.SEVERE){
                                logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"TransportBinding"});
                            }
                            throw new UnsupportedPolicyAssertion("Policy assertion "+
                                    assertion+" is not supported under TransportBinding assertion");
                            
                        }
                    }
                }
            }
            populated = true;
        }
    }
}
