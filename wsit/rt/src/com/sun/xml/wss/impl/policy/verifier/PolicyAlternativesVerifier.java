/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.wss.impl.policy.verifier;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.PolicyViolationException;
import com.sun.xml.wss.impl.policy.PolicyAlternatives;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.spi.PolicyVerifier;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author vbkumarjayanti
 */
public class PolicyAlternativesVerifier implements PolicyVerifier {
    private ProcessingContext ctx = null;
    private TargetResolver targetResolver;
    
    private static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /** Creates a new instance of MessagePolicyVerifier */
    public PolicyAlternativesVerifier(ProcessingContext ctx, TargetResolver targetResolver) {
        this.ctx = ctx;
        this.targetResolver = targetResolver;
    }

    public void verifyPolicy(SecurityPolicy recvdPolicy, SecurityPolicy configPolicy) throws PolicyViolationException {
        PolicyAlternatives confPolicies = (PolicyAlternatives)configPolicy;

        List<MessagePolicy> mps = confPolicies.getSecurityPolicy();
        if (mps.size() == 1) {
            PolicyVerifier verifier = PolicyVerifierFactory.createVerifier(mps.get(0), ctx);
            verifier.verifyPolicy(recvdPolicy, mps.get(0));
            return;
        } else {
           //do policy verification 
           // try with an AlternativeSelector first
           AlternativeSelector selector = new  DefaultAlternativeSelector();
           MessagePolicy toVerify = selector.selectAlternative(ctx, mps, recvdPolicy);
           //TODO: the PolicyVerifier.verifyPolicy() method expects the toVerify argument to be
           //passed again. since that interface is a legacy interface,  not changing it
           //right now.
            if (toVerify != null) {
                PolicyVerifier verifier = PolicyVerifierFactory.createVerifier(toVerify, ctx);
                verifier.verifyPolicy(recvdPolicy, toVerify);
                return;
            } else {
                //unsupported
               throw new UnsupportedOperationException(
                       "Cannot verify the request against the configured PolicyAlternatives in the WebService");
            }

        }

    }

    //TODO: make the AlternativeSelector to be used configurable via META-INF/Services or as Classpath Resource ?.
    //Or Have a Factory Method.
    private static interface AlternativeSelector {
        public MessagePolicy selectAlternative(ProcessingContext ctx, List<MessagePolicy> alternatives, SecurityPolicy recvdPolicy);
    }

    private static class DefaultAlternativeSelector  implements AlternativeSelector {

        public MessagePolicy selectAlternative(ProcessingContext ctx, List<MessagePolicy> alternatives, SecurityPolicy recvdPolicy) {
            MessagePolicy selected = null;
            if (alternatives.size() == 2) {
                selected = (new UsernameOrSAMLAlternativeSelector()).selectAlternative(ctx, alternatives, recvdPolicy);
            }
            if (selected != null) {
                return selected;
            }
            throw new UnsupportedOperationException(
                    "Cannot verify the request against the configured PolicyAlternatives in the WebService");
        }
    }


    /**
     * A class which can select specific alternatives of a Username or a SAMLToken appearing
     * as SignedSupportingTokens.
     */
    private static class UsernameOrSAMLAlternativeSelector implements AlternativeSelector {

        private enum SupportingTokenType {USERNAME, SAML, UNKNOWN};

        public MessagePolicy selectAlternative(ProcessingContext ctx, List<MessagePolicy> alternatives, SecurityPolicy recvdPolicy) {
            //TODO: assert that the number of alternatives is two only
            //it can handle the alternatives as defined in the following oracle security profiles :
            //1. wss11_saml_or_username_token_with_message_protection_service_policy
            //2. OR wss_saml_or_username_token_over_ssl_service_policy
            SupportingTokenType reqMsgTokenType = determineTokenType(recvdPolicy);
            for (MessagePolicy mp: alternatives) {
                SupportingTokenType alternativeTokenType = determineTokenType(mp);
                if (reqMsgTokenType !=SupportingTokenType.UNKNOWN &&
                        reqMsgTokenType.equals(alternativeTokenType)) {
                    return mp;
                }
            }
            return null;
        }

        private SupportingTokenType determineTokenType(SecurityPolicy recvdPolicy) {
            SupportingTokenType ret = SupportingTokenType.UNKNOWN;
            if (recvdPolicy instanceof MessagePolicy) {
                MessagePolicy pol = (MessagePolicy)recvdPolicy;
                for (int i=0; i < pol.size(); i++) {
                    try {
                        if (PolicyTypeUtil.usernameTokenBinding(pol.get(i))) {
                            ret = SupportingTokenType.USERNAME;
                            break;
                        } else if (PolicyTypeUtil.samlTokenPolicy(pol.get(i))) {
                            ret = SupportingTokenType.SAML;
                            break;
                        }
                    } catch (Exception e) {
                        //nothing to do.
                    }
                }
            }
            return ret;
        }

    }

}
