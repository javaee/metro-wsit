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

package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cache XWSS Policy i,e MessagePolicy for each message and cache all the
 * Issued and SecureConversation Tokens for quick lookup.
 *
 * @author K.Venugopal@sun.com
 */
public class SecurityPolicyHolder {
    
    private MessagePolicy mp = null;
    private List<PolicyAssertion> scList = null;
    private List<PolicyAssertion> issuedTokenList = null;
    private static final List<PolicyAssertion> EMPTY_LIST = Collections.emptyList();
    private AlgorithmSuite suite  = null;
    private HashMap<WSDLFault,SecurityPolicyHolder> faultFPMap = null;
    private HashMap<String,Set> configAssertions;
    
    /**
     * Creates a new instance of SecurityPolicyHolder
     */
    public SecurityPolicyHolder() {
    }
    
    public void setMessagePolicy(MessagePolicy mp){
        this.mp= mp;
    }
    
    public MessagePolicy getMessagePolicy(){
        return this.mp;
    }
    
    public void  addSecureConversationToken(PolicyAssertion pa){
        if(scList == null){
            scList = new ArrayList<PolicyAssertion> ();
        }
        scList.add(pa);
    }
    
    public List<PolicyAssertion> getSecureConversationTokens(){
        return ((scList==null)?EMPTY_LIST:scList);
    }
    
    public void addIssuedToken(PolicyAssertion pa){
        if(issuedTokenList == null){
            issuedTokenList = new ArrayList<PolicyAssertion> ();
        }
        issuedTokenList.add(pa);
    }
    
    public void addIssuedTokens(List<PolicyAssertion> list ){
        if(issuedTokenList == null){
            issuedTokenList =  list;
        }else{
            issuedTokenList.addAll(list);
        }
    }
    
    public List<PolicyAssertion> getIssuedTokens(){
        return ((issuedTokenList==null)?EMPTY_LIST:issuedTokenList);
    }
    
    public AlgorithmSuite getBindingLevelAlgSuite(){
        return suite;
    }
    
    public void setBindingLevelAlgSuite(AlgorithmSuite suite){
        this.suite = suite;
    }
    
    public void addFaultPolicy(WSDLFault fault , SecurityPolicyHolder policy){
        if(faultFPMap == null){
            faultFPMap =  new HashMap<WSDLFault,SecurityPolicyHolder>();
        }
        faultFPMap.put(fault,policy);
    }
    
    public SecurityPolicyHolder getFaultPolicy(WSDLFault fault){
        if(faultFPMap == null){
            return null;
        }
        return faultFPMap.get(fault);
    }
    
    public void addConfigAssertions(PolicyAssertion assertion){
        if(configAssertions == null){
            configAssertions = new HashMap<String,Set>();
        }
        Set assertions = configAssertions.get(assertion.getName().getNamespaceURI());
        if(assertions == null){
            assertions = new HashSet();
            configAssertions.put(assertion.getName().getNamespaceURI(),assertions);
        }
        assertions.add(assertion);
    }
    
    public Set getConfigAssertions(String namespaceuri){
        if(configAssertions == null){
            return null;
        }
        return configAssertions.get(namespaceuri);        
    }
}
