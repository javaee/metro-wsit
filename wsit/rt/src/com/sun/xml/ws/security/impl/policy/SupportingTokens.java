/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.security.policy.Token;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SupportingTokens extends PolicyAssertion implements com.sun.xml.ws.security.policy.SupportingTokens{
    
    private AlgorithmSuite algSuite;
    private List<com.sun.xml.ws.security.policy.SignedParts> spList = new ArrayList<com.sun.xml.ws.security.policy.SignedParts>(1);
    private List<com.sun.xml.ws.security.policy.EncryptedParts> epList = new ArrayList<com.sun.xml.ws.security.policy.EncryptedParts>(1);
    private List<com.sun.xml.ws.security.policy.SignedElements> seList = new ArrayList<com.sun.xml.ws.security.policy.SignedElements>(1);
    private List<com.sun.xml.ws.security.policy.EncryptedElements> eeList = new ArrayList<com.sun.xml.ws.security.policy.EncryptedElements>(1);;
    private boolean isServer = false;
    private List<Token> _tokenList;
    private boolean populated;
    private SecurityPolicyVersion spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
    
    /**
     * Creates a new instance of SupportingTokens
     */
    public SupportingTokens() {
    }
    
    public SupportingTokens(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        String nsUri = getName().getNamespaceURI();
        spVersion = PolicyUtil.getSecurityPolicyVersion(nsUri);
    }
    
    public void setAlgorithmSuite(AlgorithmSuite algSuite) {
        this.algSuite =algSuite;
    }
    
    public AlgorithmSuite getAlgorithmSuite() {
        populate();
        return algSuite;
    }
    
    
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
    
    private synchronized void populate(){
        
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
                if(PolicyUtil.isAlgorithmAssertion(assertion, spVersion)){
                    this.algSuite = (AlgorithmSuite) assertion;
                    String sigAlgo = assertion.getAttributeValue(new QName("signatureAlgorithm"));
                    this.algSuite.setSignatureAlgorithm(sigAlgo);
                }else if(PolicyUtil.isToken(assertion, spVersion)){
                    addToken((Token)assertion);
                    //this._tokenList.add((Token)assertion);
                }else if(PolicyUtil.isSignedParts(assertion, spVersion)){
                    spList.add((SignedParts) assertion);
                }else if(PolicyUtil.isSignedElements(assertion, spVersion)){
                    seList.add((SignedElements)assertion);
                }else if(PolicyUtil.isEncryptParts(assertion, spVersion)){
                    epList.add((EncryptedParts)assertion);
                }else if(PolicyUtil.isEncryptedElements(assertion, spVersion)){
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
            Iterator<PolicyAssertion> parameterAssertion = this.getParametersIterator();
            while(parameterAssertion.hasNext()){
                PolicyAssertion assertion = parameterAssertion.next();
                if(PolicyUtil.isSignedParts(assertion, spVersion)){
                    spList.add((SignedParts) assertion);
                }else if(PolicyUtil.isSignedElements(assertion, spVersion)){
                    seList.add((SignedElements)assertion);
                }else if(PolicyUtil.isEncryptParts(assertion, spVersion)){
                    epList.add((EncryptedParts)assertion);
                }else if(PolicyUtil.isEncryptedElements(assertion, spVersion)){
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

    public SecurityPolicyVersion getSecurityPolicyVersion() {
        return spVersion;
    }
    
}
