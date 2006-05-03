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
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.EndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SignedElements;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.SignedSupportingTokens;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.Target;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.crypto.dsig.CanonicalizationMethod;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class EndorsingSupportingTokensProcessor extends SupportingTokensProcessor {
    
    protected SignaturePolicy primarySP= null;
    /** Creates a new instance of EndorsingSupportingTokensProcessor */
    public EndorsingSupportingTokensProcessor(SupportingTokens st,TokenProcessor tokenProcessor,Binding binding,
            XWSSPolicyContainer container,SignaturePolicy sp,EncryptionPolicy ep,PolicyID pid) {
        super(st,tokenProcessor,binding,container,sp,ep,pid);
       // this.primarySP = primarySP;
    }
    
    protected void addToPrimarySignature(WSSPolicy policy,Token token) throws PolicyException{
    }
    
    
    protected void collectSignaturePolicies(Token token) throws PolicyException{
        createSupportingSignature(token);
    }
    
    protected void endorseSignature(SignaturePolicy sp){
        SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding)sp.getFeatureBinding();
        SignatureTarget sigTarget = stc.newURISignatureTarget(signaturePolicy.getUUID());
        spFB.addTargetBinding(sigTarget);
        spFB.isEndorsingSignature(true);
    }
}
