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

/*
 * SignatureConfirmationPolicy.java
 *
 * Created on January 24, 2006, 5:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.MessageConstants;

/**
 * A policy representing a WSS1.1 SignatureConfirmation element.
 * Note: The SignatureConfirmationPolicy is WSSPolicy element that does not contain a
 * concrete FeatureBinding and/or KeyBinding.
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class SignatureConfirmationPolicy extends WSSPolicy{
    
    private String signatureValue = MessageConstants._EMPTY;
    
    /**
     * DefaultConstructor 
     */
    public SignatureConfirmationPolicy() {
        setPolicyIdentifier(PolicyTypeUtil.SIGNATURE_CONFIRMATION_POLICY_TYPE);
    }
    
    /**
     * sets the Value attribute for SignatureConfirmation in this SignatureConfirmationPolicy
     * @param signatureValue
     */
    public void setSignatureValue(String signatureValue){
        this.signatureValue = signatureValue;
    }
    
    /**
     * @return the Value attribute of SignatureConfirmation
     */
    public String getSignatureValue(){
        return this.signatureValue;
    }
    
    /**
     * @param policy the policy to be compared for equality
     * @return true if the argument policy is equal to this policy
     */
    public boolean equals(WSSPolicy policy){
        
        boolean assrt = false;
        try{
            SignatureConfirmationPolicy scPolicy = (SignatureConfirmationPolicy)policy;
            assrt = signatureValue.equals(scPolicy.getSignatureValue());
        } catch(Exception e) {}
        
        return assrt;
    }
    
    /**
     * Equality comparison ignoring the targets
     * @param policy the policy to be compared for equality
     * @return true if the argument policy is equal to this policy
     */
    public boolean equalsIgnoreTargets(WSSPolicy policy) {
        return equals(policy);
    }
    
    /**
     * Clone operator
     * @return clone of this policy
     */
    public Object clone(){
        SignatureConfirmationPolicy scPolicy = new SignatureConfirmationPolicy();
        
        try{
            scPolicy.setSignatureValue(signatureValue);
        } catch (Exception e) {}
        
        return scPolicy;
    }
    
    /**
     * @return the type of policy
     */
    public String getType(){
        return PolicyTypeUtil.SIGNATURE_CONFIRMATION_POLICY_TYPE;
    }
}
