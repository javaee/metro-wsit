/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.HttpsToken;
import com.sun.xml.ws.security.policy.MessageLayout;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.Collection;
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
    boolean includeTimeStamp=false;
    MessageLayout layout = MessageLayout.Lax;
    boolean populated = false;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    private SecurityPolicyVersion spVersion;
    /**
     * Creates a new instance of TransportBinding
     */
    public TransportBinding() {
        spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
    }
    
    public TransportBinding(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        String nsUri = getName().getNamespaceURI();
        spVersion = PolicyUtil.getSecurityPolicyVersion(nsUri);
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
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        if(!populated){
            NestedPolicy policy = this.getNestedPolicy();
            AssertionSet assertions = policy.getAssertionSet();
            if(assertions == null){
                if(logger.getLevel() == Level.FINE){
                    logger.log(Level.FINE,"NestedPolicy is null");
                }
                populated = true;
                return fitness;
            }
            for(PolicyAssertion assertion : assertions){
                if(PolicyUtil.isAlgorithmAssertion(assertion, spVersion)){
                    this.algSuite = (AlgorithmSuite) assertion;
                    String sigAlgo = assertion.getAttributeValue(new QName("signatureAlgorithm"));
                    this.algSuite.setSignatureAlgorithm(sigAlgo);
                }else if(PolicyUtil.isToken(assertion, spVersion)){
                    transportToken = (HttpsToken)((com.sun.xml.ws.security.impl.policy.Token)assertion).getToken();
                }else if(PolicyUtil.isMessageLayout(assertion, spVersion)){
                    layout = ((Layout)assertion).getMessageLayout();
                }else if(PolicyUtil.isIncludeTimestamp(assertion, spVersion)){
                    includeTimeStamp=true;
                } else{
                    if(!assertion.isOptional()){
                        log_invalid_assertion(assertion, isServer,TransportBinding);
                        fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                    }
                }
            }
            populated = true;
        }
        return fitness;
    }

    public boolean isDisableTimestampSigning() {
        throw new UnsupportedOperationException();
    }

    public SecurityPolicyVersion getSecurityPolicyVersion() {
        return spVersion;
    }
}
