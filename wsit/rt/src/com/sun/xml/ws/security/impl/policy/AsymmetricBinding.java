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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/** The contents of this file are subject to the terms
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
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;


/**
 * 
 * @author K.Venugopal@sun.com 
 */

public class AsymmetricBinding extends com.sun.xml.ws.policy.PolicyAssertion implements com.sun.xml.ws.security.policy.AsymmetricBinding, SecurityAssertionValidator {   
    
    private AssertionFitness fitness = AssertionFitness.IS_VALID;   
    private Token initiatorToken;   
    private Token recipientToken;
    private Token initiatorSignatureToken;
    private Token recipientSignatureToken;
    private Token initiatorEncryptionToken;
    private Token recipientEncryptionToken;
    private AlgorithmSuite algSuite; 
    private boolean includeTimestamp = false;  
    private boolean disableTimestampSigning = false;
    private boolean contentOnly = true;   
    private  MessageLayout layout = MessageLayout.Lax; 
    private String protectionOrder = SIGN_ENCRYPT;   
    private boolean protectToken = false;  
    private boolean protectSignature = false;   
    private boolean populated = false;          
    private SecurityPolicyVersion spVersion;
    
    /**  
     * Creates a new instance of AsymmetricBinding     
     */ 
    public AsymmetricBinding() {
        spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
    }    
    public AsymmetricBinding(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {      
        super(name,nestedAssertions,nestedAlternative);
        String nsUri = getName().getNamespaceURI();
        spVersion = PolicyUtil.getSecurityPolicyVersion(nsUri);
    }    
    
    public Token getRecipientToken() {  
        populate();    
        return recipientToken; 
    }    
    
    public Token getInitiatorToken() {  
        populate();       
        return initiatorToken;   
    }

    public Token getRecipientSignatureToken() {
        populate();
        return recipientSignatureToken;
    }

    public Token getInitiatorSignatureToken() {
        populate();
        return initiatorSignatureToken;
    }
    public Token getRecipientEncryptionToken() {
        populate();
        return recipientEncryptionToken;
    }

    public Token getInitiatorEncryptionToken() {
        populate();
        return initiatorEncryptionToken;
    }
    
    public void setAlgorithmSuite(AlgorithmSuite algSuite) {   
        this.algSuite = algSuite;   
    }      
    
    public AlgorithmSuite getAlgorithmSuite() {   
        populate();      
        if(algSuite == null){    
            algSuite = new  com.sun.xml.ws.security.impl.policy.AlgorithmSuite();     
            logger.log(Level.FINE, "Using Default Algorithm Suite Basic128");    
        }       
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
    
    public boolean isDisableTimestampSigning() {
        populate();
        return disableTimestampSigning;
    }
    
    public void setLayout(MessageLayout layout) {
        this.layout = layout;    
    }   
    
    public MessageLayout getLayout() {
        populate();   
        return layout; 
    }      
    
    public void setInitiatorToken(Token token) {       
        this.initiatorToken = token; 
    } 
    
    public void setRecipientToken(Token token) {   
        this.recipientToken = token;   
    }

    public void setInitiatorSignatureToken(Token token) {
        this.initiatorSignatureToken = token;
    }

    public void setRecipientSignatureToken(Token token) {
        this.recipientSignatureToken = token;
    }

     public void setInitiatorEncryptionToken(Token token) {
        this.initiatorEncryptionToken = token;
    }

    public void setRecipientEncryptionToken(Token token) {
        this.recipientEncryptionToken = token;
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
    
    public AssertionFitness validate(boolean isServer) {    
        
        return populate(isServer);    
    
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
                populated = true;        
                return fitness;      
            }          
            AssertionSet as = policy.getAssertionSet();     
            Iterator<PolicyAssertion> ast = as.iterator();     
            while(ast.hasNext()){           
                PolicyAssertion assertion = ast.next(); 
                if(PolicyUtil.isInitiatorToken(assertion, spVersion)){    
                    this.initiatorToken = ((com.sun.xml.ws.security.impl.policy.Token)assertion).getToken();  
                }else if(PolicyUtil.isRecipientToken(assertion, spVersion)){     
                    this.recipientToken = ((com.sun.xml.ws.security.impl.policy.Token)assertion).getToken(); 
                }else if(PolicyUtil.isRecipientSignatureToken(assertion, spVersion)){
                    this.recipientSignatureToken = ((com.sun.xml.ws.security.impl.policy.Token)assertion).getToken();
                }else if(PolicyUtil.isRecipientEncryptionToken(assertion, spVersion)){
                    this.recipientEncryptionToken = ((com.sun.xml.ws.security.impl.policy.Token)assertion).getToken();
                }else if(PolicyUtil.isInitiatorSignatureToken(assertion, spVersion)){
                    this.initiatorSignatureToken = ((com.sun.xml.ws.security.impl.policy.Token)assertion).getToken();
                }else if(PolicyUtil.isInitiatorEncryptionToken(assertion, spVersion)){
                    this.initiatorEncryptionToken = ((com.sun.xml.ws.security.impl.policy.Token)assertion).getToken();
                }else if(PolicyUtil.isAlgorithmAssertion(assertion, spVersion)){
                    this.algSuite = (AlgorithmSuite) assertion;
                    String sigAlgo = assertion.getAttributeValue(new QName("signatureAlgorithm"));
                    this.algSuite.setSignatureAlgorithm(sigAlgo);
                }else if(PolicyUtil.isIncludeTimestamp(assertion, spVersion)){            
                    this.includeTimestamp = true;        
                }else if(PolicyUtil.isEncryptBeforeSign(assertion, spVersion)){    
                    this.protectionOrder = ENCRYPT_SIGN;             
                }else if (PolicyUtil.isSignBeforeEncrypt(assertion, spVersion)){
                    this.protectionOrder = SIGN_ENCRYPT;
                }else if(PolicyUtil.isContentOnlyAssertion(assertion, spVersion)){                      
                    this.contentOnly = false;          
                }else if(PolicyUtil.isMessageLayout(assertion, spVersion)){      
                    layout = ((Layout)assertion).getMessageLayout();   
                }else if(PolicyUtil.isProtectTokens(assertion, spVersion)){       
                    this.protectToken = true;         
                }else if(PolicyUtil.isEncryptSignature(assertion, spVersion)){    
                    this.protectSignature = true;        
                } else if(PolicyUtil.disableTimestampSigning(assertion)){
                    this.disableTimestampSigning = true;
                }else{      
                    if(!assertion.isOptional()){  
                        log_invalid_assertion(assertion, isServer,AsymmetricBinding); 
                        fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;            
                    }             
                }      
            }   
            populated = true;     
        } 
        return fitness; 
    }  

    public SecurityPolicyVersion getSecurityPolicyVersion() {
        return spVersion;
    }
}
