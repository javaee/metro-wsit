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

package com.sun.xml.ws.policy.jaxws;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import com.sun.xml.ws.api.model.wsdl.WSDLExtension;
import com.sun.xml.ws.policy.EffectiveAlternativeSelector;
import com.sun.xml.ws.policy.EffectivePolicyModifier;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;

/**
 *
 * @author japod
 */
public class WSDLPolicyMapWrapper implements WSDLExtension {
    
    private static final PolicyLogger logger = PolicyLogger.getLogger(PolicyWSDLParserExtension.class);

    private static final QName NAME = new QName(null,"WSDLPolicyMapWrapper");
    
    private PolicyMap policyMap;
    private EffectivePolicyModifier modifier;

    
    protected WSDLPolicyMapWrapper(PolicyMap policyMap) {
        this.policyMap = policyMap;
    }

    public WSDLPolicyMapWrapper(PolicyMap policyMap, EffectivePolicyModifier modifier) {
        this(policyMap);
        this.modifier = modifier;
    }

    public PolicyMap getPolicyMap() {
        return policyMap;
    }
    
    public void doAlternativeSelection() {
        try {
            EffectiveAlternativeSelector.doSelection(modifier);
        } catch (PolicyException e) {
            throw new WebServiceException("Failed to find a valid policy alternative", e);
        }
    }
    
    public QName getName() {
        return NAME;
    }
    
}
