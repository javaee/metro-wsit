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
import com.sun.xml.ws.security.policy.AlgorithmSuiteValue;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import com.sun.xml.ws.api.security.policy.SecurityPolicyVersion;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import static com.sun.xml.ws.security.impl.policy.Constants.*;


/**
 *
 * @author K.Venugopal@sun.com Abhijit.das@Sun.com
 */

public class AlgorithmSuite extends com.sun.xml.ws.policy.PolicyAssertion implements com.sun.xml.ws.security.policy.AlgorithmSuite,SecurityAssertionValidator{
    
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    private AlgorithmSuiteValue value;
    private HashSet<String> props = new HashSet<String>();
    private boolean populated = false;
    private boolean isValid = true;
    private SecurityPolicyVersion spVersion;
    private String signatureAlgo = null;
    /**
     * Creates a new instance of AlgorithmSuite
     */
    public AlgorithmSuite() {
        spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
    }
    
    public AlgorithmSuite(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        String nsUri = getName().getNamespaceURI();
        spVersion = PolicyUtil.getSecurityPolicyVersion(nsUri);
    }
    
    public Set getAdditionalProps() {
        return props;
    }
    
    public void setAdditionalProps(Set properties) {
    }
    
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
    
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        
        if(!populated){
            NestedPolicy policy = this.getNestedPolicy();
            if(policy == null){
                if(logger.isLoggable(Level.FINE)){
                    logger.log(Level.FINE,"NestedPolicy is null");
                }
                if(this.value == null){
                    this.value = AlgorithmSuiteValue.Basic128;
                }
                return fitness;
            }
            AssertionSet as = policy.getAssertionSet();
            
            Iterator<PolicyAssertion> ast = as.iterator();
            while(ast.hasNext()){
                PolicyAssertion assertion = ast.next();
                if(this.value == null){
                    AlgorithmSuiteValue av = PolicyUtil.isValidAlgorithmSuiteValue(assertion, spVersion);
                    if(av != null){
                        this.value = av;
                        continue;
                    }
                }
                if(PolicyUtil.isInclusiveC14N(assertion, spVersion)){
                    this.props.add(Constants.InclusiveC14N);
                }else if(PolicyUtil.isXPath(assertion, spVersion)){
                    this.props.add(Constants.XPath);
                }else if(PolicyUtil.isXPathFilter20(assertion)){
                    this.props.add(Constants.XPathFilter20);
                }else if(PolicyUtil.isSTRTransform10(assertion, spVersion)){
                    this.props.add(Constants.STRTransform10);
                }else if(PolicyUtil.isInclusiveC14NWithComments(assertion)){
                    if(PolicyUtil.isInclusiveC14NWithCommentsForTransforms(assertion)){
                        this.props.add(Constants.InclusiveC14NWithCommentsForTransforms);
                    }
                    if(PolicyUtil.isInclusiveC14NWithCommentsForCm(assertion)){
                        this.props.add(Constants.InclusiveC14NWithCommentsForCm);
                    }
                }else if(PolicyUtil.isExclusiveC14NWithComments(assertion)){
                    if(PolicyUtil.isExclusiveC14NWithCommentsForTransforms(assertion)){
                        this.props.add(Constants.ExclusiveC14NWithCommentsForTransforms);
                    } 
                    if(PolicyUtil.isExclusiveC14NWithCommentsForCm(assertion)){
                        this.props.add(Constants.ExclusiveC14NWithCommentsForCm);
                    }
                }else{
                    if(!assertion.isOptional()){
                        log_invalid_assertion(assertion, isServer,AlgorithmSuite);
                        fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                    }
                }
            }
            if(this.value == null){
                this.value = AlgorithmSuiteValue.Basic128;
            }
            populated = true;
        }
        return fitness;
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
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }

    public void setSignatureAlgorithm(String sigAlgo) {
       this.signatureAlgo = sigAlgo;
    }
    public String getSignatureAlgorithm() {
       return this.signatureAlgo ;
    }
}
