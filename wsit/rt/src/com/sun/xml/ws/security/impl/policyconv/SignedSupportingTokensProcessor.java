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

package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.security.policy.SignedSupportingTokens;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
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
    @Override
    protected void addToPrimarySignature(WSSPolicy policy,Token token) throws PolicyException{
        String includeToken = token.getIncludeToken();
        SecurityPolicyVersion spVersion = SecurityPolicyUtil.getSPVersion((PolicyAssertion) token);
        SignatureTarget target = null;
        if (includeToken.endsWith("Never") && PolicyUtil.isX509Token((PolicyAssertion) token, spVersion)) {
            String uid = pid.generateID();
            ((AuthenticationTokenPolicy.X509CertificateBinding) policy).setSTRID(uid);
            target = stc.newURISignatureTargetForSSToken(uid);
           //this flag will be used for computing securitytokenreference when the includetoken type is Never !!
            target.isITNever(true);
        } else {
            target = stc.newURISignatureTargetForSSToken(policy.getUUID());
        }
        SecurityPolicyUtil.setName(target, policy);

        if((!PolicyUtil.isUsernameToken((PolicyAssertion) token, spVersion) &&
           !spVersion.includeTokenAlways.equals(includeToken) &&
           !spVersion.includeTokenAlwaysToRecipient.equals(includeToken)) || PolicyUtil.isSamlToken((PolicyAssertion)token,spVersion)
           || PolicyUtil.isIssuedToken((PolicyAssertion)token,spVersion)){
            stc.addSTRTransform(target);
            target.setPolicyName(getQName(policy));
        } else {
             stc.addTransform(target);
        }
        SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding();
        spFB.addTargetBinding(target);
    }    

//    protected void collectSignaturePolicies(Token token) throws PolicyException{
//        createSupportingSignature(token);
//    }
}
