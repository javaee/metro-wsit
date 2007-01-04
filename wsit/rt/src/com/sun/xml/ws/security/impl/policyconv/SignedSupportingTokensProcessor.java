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

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.SignedSupportingTokens;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class SignedSupportingTokensProcessor extends SupportingTokensProcessor {
    
    /** Creates a new instance of SignedSupportingTokensProcessor */
    public SignedSupportingTokensProcessor(SignedSupportingTokens st,TokenProcessor tokenProcessor,Binding binding,
            XWSSPolicyContainer container,SignaturePolicy sp,EncryptionPolicy ep,PolicyID pid) {
        super(st,tokenProcessor,binding,container,sp,ep,pid);
    }
    
    protected void addToPrimarySignature(WSSPolicy policy,Token token) throws PolicyException{
        SignatureTarget target = stc.newURISignatureTarget(policy.getUUID());
        SecurityPolicyUtil.setName(target, policy);
        if(!PolicyUtil.isUsernameToken((PolicyAssertion) token)){
            stc.addSTRTransform(target);
        }
        SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding();
        spFB.addTargetBinding(target);
    }
    
//    protected void collectSignaturePolicies(Token token) throws PolicyException{
//        createSupportingSignature(token);
//    }
}
