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
import com.sun.xml.ws.security.policy.AlgorithmSuiteValue;
import java.lang.UnsupportedOperationException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.sun.xml.ws.security.impl.policy.Constants.*;


/**
 *
 * @author K.Venugopal@sun.com Abhijit.das@Sun.com
 */

public class AlgorithmSuite extends com.sun.xml.ws.policy.PolicyAssertion implements com.sun.xml.ws.security.policy.AlgorithmSuite{
    
    
    private AlgorithmSuiteValue value;
    private Set props = new HashSet();
    private boolean populated = false;
    private boolean isServer = false;
    /**
     * Creates a new instance of AlgorithmSuite
     */
    public AlgorithmSuite() {
    }
    
    public AlgorithmSuite(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public Set getAdditionalProps() {
        return props;
    }
    
    public void setAdditionalProps(Set properties) {
    }
    
//    public QName getName() {
//        return _AlgorithmSuite_QNAME;
//    }
    
    public void setType(AlgorithmSuiteValue value) {
        this.value = value;
        populated = true;
    }
    
    public AlgorithmSuiteValue getType() {
        populate();
        return value;
    }
    
    public String getDigestAlgorithm() {
        populate();
        return value.getDigAlgorithm();
    }
    
    
    public String getEncryptionAlgorithm() {
        populate();
        return value.getEncAlgorithm();
    }
    
    
    public String getSymmetricKeyAlgorithm() {
        populate();
        return value.getSymKWAlgorithm();
    }
    
    public String getAsymmetricKeyAlgorithm() {
        populate();
        return value.getAsymKWAlgorithm();
    }
    
    public String getSignatureKDAlogrithm() {
        populate();
        return value.getSigKDAlgorithm();
    }
    
    public String getEncryptionKDAlogrithm() {
        populate();
        return value.getEncKDAlgorithm();
    }
    
    public int getMinSKLAlgorithm() {
        populate();
        return value.getMinSKLAlgorithm();
    }
    
    public String getSymmetricKeySignatureAlgorithm() {
        return com.sun.xml.ws.security.policy.Constants.HMAC_SHA1;
    }
    
    public String getAsymmetricKeySignatureAlgorithm() {
        return com.sun.xml.ws.security.policy.Constants.RSA_SHA1;
    }
    
    
    private void populate() {
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
                    return;
                }
                AssertionSet as = policy.getAssertionSet();
                
                Iterator<PolicyAssertion> ast = as.iterator();
                while(ast.hasNext()){
                    PolicyAssertion assertion = ast.next();
                    if(this.value == null){
                        AlgorithmSuiteValue av = PolicyUtil.isValidAlgorithmSuiteValue(assertion);
                        if(av != null){
                            this.value = av;
                            continue;
                        }
                        
                    }
                    if(PolicyUtil.isInclusiveC14N(assertion)){
                        this.props.add(Constants.InclusiveC14N);
                    }else if(PolicyUtil.isXPath(assertion)){
                        this.props.add(Constants.XPath);
                    }else if(PolicyUtil.isXPathFilter20(assertion)){
                        this.props.add(Constants.XPathFilter20);
                    }else if(PolicyUtil.isSTRTransform10(assertion)){
                        this.props.add(Constants.STRTransform10);
                    }else{
                        if(!assertion.isOptional()){
                            if(logger.getLevel() == Level.SEVERE){
                                logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"AlgorithmSuite"});
                            }
                            if(isServer){
                                throw new UnsupportedPolicyAssertion("Policy assertion "+
                                          assertion+" is not supported under AlgorithmSuite assertion");
                            }
                        }
                    }
                }
            }
            
            populated = true;
        }
    }
    
    public String getComputedKeyAlgorithm() {
        return com.sun.xml.ws.security.policy.Constants.PSHA1;
    }
    
    public int getMaxSymmetricKeyLength() {
        return MAX_SKL;
    }
    
    public int getMinAsymmetricKeyLength() {
        return MIN_AKL;
    }
    
    public int getMaxAsymmetricKeyLength() {
        return MAX_AKL;
    }
}
