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
            scPolicy.setUUID(this.getUUID());
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
