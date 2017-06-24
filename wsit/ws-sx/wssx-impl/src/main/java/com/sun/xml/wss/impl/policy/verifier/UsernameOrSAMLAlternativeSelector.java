/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.util.List;

/**
 * A class which can select specific alternatives of a Username or a SAMLToken appearing
 * as SignedSupportingTokens.
 */
public class UsernameOrSAMLAlternativeSelector implements AlternativeSelector {



	private enum SupportingTokenType {

		USERNAME, SAML, UNKNOWN
	}

	@Override
	public MessagePolicy selectAlternative(ProcessingContext ctx, List<MessagePolicy> alternatives, SecurityPolicy recvdPolicy) {
		//TODO: assert that the number of alternatives is two only
		//it can handle the alternatives as defined in the following oracle security profiles :
		//1. wss11_saml_or_username_token_with_message_protection_service_policy
		//2. OR wss_saml_or_username_token_over_ssl_service_policy
		SupportingTokenType reqMsgTokenType = determineTokenType(recvdPolicy);
		for (MessagePolicy mp : alternatives) {
			SupportingTokenType alternativeTokenType = determineTokenType(mp);
			if (reqMsgTokenType != SupportingTokenType.UNKNOWN && reqMsgTokenType.equals(alternativeTokenType)) {
				return mp;
			}
		}
		return null;
	}

	@Override
	public boolean supportsAlternatives(List<MessagePolicy> alternatives) {
		 if (alternatives.size() != 2) {
			 return false;
		 }
		 SupportingTokenType firstAlternativeType = determineTokenType(alternatives.get(0));

		 if(firstAlternativeType == SupportingTokenType.UNKNOWN) {
			 return false;
		 }

		 SupportingTokenType secondAlternativeType = determineTokenType(alternatives.get(1));

		 if(secondAlternativeType == SupportingTokenType.UNKNOWN) {
			 return false;
		 }

		 if(firstAlternativeType == secondAlternativeType) {
			 return false;
		 }

		 return true;
	}

	private SupportingTokenType determineTokenType(SecurityPolicy recvdPolicy) {
		SupportingTokenType ret = SupportingTokenType.UNKNOWN;
		if (recvdPolicy instanceof MessagePolicy) {
			MessagePolicy pol = (MessagePolicy) recvdPolicy;
			for (int i = 0; i < pol.size(); i++) {
				try {
					WSSPolicy p = (WSSPolicy) pol.get(i);
					if (PolicyTypeUtil.usernameTokenBinding(p) || PolicyTypeUtil.usernameTokenBinding(p.getFeatureBinding())) {
						ret = SupportingTokenType.USERNAME;
						break;
					} else if (PolicyTypeUtil.samlTokenPolicy(p) || PolicyTypeUtil.samlTokenPolicy(p.getFeatureBinding())) {
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

