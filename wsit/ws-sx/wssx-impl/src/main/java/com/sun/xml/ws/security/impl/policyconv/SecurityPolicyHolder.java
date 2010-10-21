/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
    private List<PolicyAssertion> kerberosTokenList = null;
    private static final List<PolicyAssertion> EMPTY_LIST = Collections.emptyList();
    private AlgorithmSuite suite  = null;
    private HashMap<WSDLFault,SecurityPolicyHolder> faultFPMap = null;
    private HashMap<String,Set<PolicyAssertion>> configAssertions;
    private boolean isIssuedTokenAsEncryptedSupportingToken = false;
    
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
    
    public void  addKerberosToken(PolicyAssertion pa){
        if(kerberosTokenList == null){
            kerberosTokenList = new ArrayList<PolicyAssertion> ();
        }
        kerberosTokenList.add(pa);
    }
    
    public List<PolicyAssertion> getKerberosTokens(){
        return ((kerberosTokenList==null)?EMPTY_LIST:kerberosTokenList);
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
    
    public boolean isIssuedTokenAsEncryptedSupportingToken(){
        return this.isIssuedTokenAsEncryptedSupportingToken;
    }
    
    public void isIssuedTokenAsEncryptedSupportingToken(boolean isIssuedTokenAsEncryptedSupportingToken){
        this.isIssuedTokenAsEncryptedSupportingToken = isIssuedTokenAsEncryptedSupportingToken;
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
            configAssertions = new HashMap<String,Set<PolicyAssertion>>();
        }
        Set<PolicyAssertion> assertions = configAssertions.get(assertion.getName().getNamespaceURI());
        if(assertions == null){
            assertions = new HashSet<PolicyAssertion>();
            configAssertions.put(assertion.getName().getNamespaceURI(),assertions);
        }
        assertions.add(assertion);
    }
    
    public Set<PolicyAssertion> getConfigAssertions(String namespaceuri){
        if(configAssertions == null){
            return null;
        }
        return configAssertions.get(namespaceuri);        
    }
}
