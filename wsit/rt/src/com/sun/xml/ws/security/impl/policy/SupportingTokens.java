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
import com.sun.xml.ws.security.policy.EndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.Target;
import com.sun.xml.ws.security.policy.Token;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SupportingTokens extends PolicyAssertion implements com.sun.xml.ws.security.policy.SupportingTokens{
    
    private AlgorithmSuite algSuite;
    private List<com.sun.xml.ws.security.policy.SignedParts> spList = new ArrayList(1);
    private List<com.sun.xml.ws.security.policy.EncryptedParts> epList = new ArrayList(1);
    private List<com.sun.xml.ws.security.policy.SignedElements> seList = new ArrayList(1);
    private List<com.sun.xml.ws.security.policy.EncryptedElements> eeList = new ArrayList(1);;
    private boolean isServer = false;
    private List<Token> _tokenList;
    private boolean populated;
    
    /**
     * Creates a new instance of SupportingTokens
     */
    public SupportingTokens() {
    }
    
    public SupportingTokens(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        
    }
    
    public void setAlgorithmSuite(AlgorithmSuite algSuite) {
        this.algSuite =algSuite;
    }
    
    public AlgorithmSuite getAlgorithmSuite() {
        populate();
        return algSuite;
    }
    
//    public void addTarget(Target target) {
//        if(targetList == null){
//            targetList = new ArrayList<Target>();
//        }
//        targetList.add(target);
//    }
//
//    public Iterator getTargets() {
//        populate();
//        if ( targetList != null ) {
//            return targetList.iterator();
//        }
//        return Collections.emptyList().iterator();
//    }
    
    public void addToken(Token token) {
        if(_tokenList == null){
            _tokenList = new ArrayList<Token>();
            //Workaround - workaround to remove duplicate UsernameToken : uncomment this
            //_tokenList.add(token);
        }
        //Workaround - comment
        _tokenList.add(token);
    }
    
    public Iterator getTokens() {
        populate();
        if ( _tokenList != null ) {
            return _tokenList.iterator();
        }
        return Collections.emptyList().iterator();
    }
    
//    public QName getName() {
//        return Constants._SupportingTokens_QNAME;
//    }
    
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
                    if(PolicyUtil.isAlgorithmAssertion(assertion)){
                        this.algSuite = (AlgorithmSuite) assertion;
                    }else if(PolicyUtil.isToken(assertion)){
                        addToken((Token)assertion);
                        //this._tokenList.add((Token)assertion);
                    }else if(PolicyUtil.isSignedParts(assertion)){
                        spList.add((SignedParts) assertion);
                    }else if(PolicyUtil.isSignedElements(assertion)){
                        seList.add((SignedElements)assertion);
                    }else if(PolicyUtil.isEncryptParts(assertion)){
                        epList.add((EncryptedParts)assertion);
                    }else if(PolicyUtil.isEncryptedElements(assertion)){
                        eeList.add((EncryptedElements)assertion);
                    }else{
                        if(!assertion.isOptional()){
                            if(logger.getLevel() == Level.SEVERE){
                                logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"SupportingTokens"});
                            }
                            if(isServer){
                                throw new UnsupportedPolicyAssertion("Policy assertion "+
                                          assertion+" is not supported under SupportingTokens assertion");
                            }
                        }
                    }
                }
            }
            populated = true;
        }
    }
    
    public String getIncludeToken() {
        return "";
    }
    
    public void setIncludeToken(String type) {
    }
    
    public String getTokenId() {
        return "";
    }
    
    public Iterator<com.sun.xml.ws.security.policy.SignedParts> getSignedParts() {
        populate();
        return spList.iterator();
    }
    
    public Iterator<com.sun.xml.ws.security.policy.SignedElements> getSignedElements() {
        populate();
        return seList.iterator();
    }
    
    public Iterator<com.sun.xml.ws.security.policy.EncryptedParts> getEncryptedParts() {
        populate();
        return epList.iterator();
    }
    
    public Iterator<com.sun.xml.ws.security.policy.EncryptedElements> getEncryptedElements() {
        populate();
        return eeList.iterator();
    }
    
}
