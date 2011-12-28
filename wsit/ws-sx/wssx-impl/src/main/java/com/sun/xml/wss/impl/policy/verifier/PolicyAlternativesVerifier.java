/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.wss.impl.policy.verifier;

import com.sun.xml.ws.security.spi.AlternativeSelector;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.impl.PolicyViolationException;
import com.sun.xml.wss.impl.policy.PolicyAlternatives;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.spi.PolicyVerifier;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 *
 * @author vbkumarjayanti
 */
public class PolicyAlternativesVerifier implements PolicyVerifier {
    private ProcessingContext ctx = null;   
    
    private static final  Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /** Creates a new instance of MessagePolicyVerifier */
    public PolicyAlternativesVerifier(ProcessingContext ctx, TargetResolver targetResolver) {
        this.ctx = ctx;
        //this.targetResolver = targetResolver;
    }

    public void verifyPolicy(SecurityPolicy recvdPolicy, SecurityPolicy configPolicy) throws PolicyViolationException {
        PolicyAlternatives confPolicies = (PolicyAlternatives)configPolicy;
       
        List<MessagePolicy> mps = confPolicies.getSecurityPolicy();        
        if (mps.size() == 1) {
            PolicyVerifier verifier = PolicyVerifierFactory.createVerifier(mps.get(0), ctx);
            verifier.verifyPolicy(recvdPolicy, mps.get(0));
            if (mps.get(0).getPolicyAlternativeId() != null) {
                ctx.getExtraneousProperties().put(POLICY_ALTERNATIVE_ID,mps.get(0).getPolicyAlternativeId());
            }
            return;
        } else {
           //do policy verification 
           // try with an AlternativeSelector first
           //AlternativeSelector selector = new  DefaultAlternativeSelector();
           AlternativeSelector selector = findAlternativesSelector(mps);
           MessagePolicy toVerify = selector.selectAlternative(ctx, mps, recvdPolicy);
           //TODO: the PolicyVerifier.verifyPolicy() method expects the toVerify argument to be
           //passed again. since that interface is a legacy interface,  not changing it
           //right now.
            if (toVerify != null) {
                PolicyVerifier verifier = PolicyVerifierFactory.createVerifier(toVerify, ctx);
                verifier.verifyPolicy(recvdPolicy, toVerify);
                if (toVerify.getPolicyAlternativeId() != null) {
                   ctx.getExtraneousProperties().put(POLICY_ALTERNATIVE_ID,toVerify.getPolicyAlternativeId());
                }
                return;
            } else {
                //unsupported
               throw new UnsupportedOperationException(
                       "Cannot verify the request against the configured PolicyAlternatives in the WebService");
            }

        }

    }
    
    private AlternativeSelector findAlternativesSelector(List<MessagePolicy> alternatives) {
        ServiceLoader<AlternativeSelector> alternativeSelectorLoader = ServiceLoader.load(AlternativeSelector.class);
        //not clear from javadoc if null is returned ever or an RT exception thrown when it does not find
        // the services definitions.
        if (alternativeSelectorLoader == null) {
            if (alternatives.size() == 2) {
                return new UsernameOrSAMLAlternativeSelector();
            } else {
                throw new UnsupportedOperationException("No AlternativeSelector accepts the policy alternatives combination.");
            }
        }
        Iterator<AlternativeSelector> alternativeSelectorIterator = alternativeSelectorLoader.iterator();

        while (alternativeSelectorIterator.hasNext()) {
            AlternativeSelector selector = alternativeSelectorIterator.next();
            if (selector.supportsAlternatives(alternatives)) {
                return selector;
            }
        }

        throw new UnsupportedOperationException("No AlternativeSelector accepts the policy alternatives combination.");
    }


}
