/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rx.policy.assertion.wsrm200702;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.ReliableMessagingFeatureBuilder;
import java.util.Collection;
import javax.xml.namespace.QName;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.ComplexAssertion;
import com.sun.xml.ws.rx.policy.assertion.AssertionInstantiator;
import com.sun.xml.ws.rx.policy.assertion.AssertionNamespace;
import com.sun.xml.ws.rx.policy.assertion.RmConfigurator;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.ReliableMessagingFeature.DeliveryAssurance;
import com.sun.xml.ws.rx.rm.ReliableMessagingFeature.SecurityBinding;
import javax.xml.ws.WebServiceException;

/**
 * <wsrmp:RMAssertion [wsp:Optional="true"]? ... >
 *   <wsp:Policy>
 *     [ <wsrmp:SequenceSTR/> |
 *       <wsrmp:SequenceTransportSecurity/> ] ?
 *     <wsrmp:DeliveryAssurance>
 *       <wsp:Policy>
 *         [ <wsrmp:ExactlyOnce/> |
 *           <wsrmp:AtLeastOnce/> |
 *           <wsrmp:AtMostOnce/> ]
 *         <wsrmp:InOrder/> ?
 *       </wsp:Policy>
 *     </wsrmp:DeliveryAssurance> ?
 *   </wsp:Policy>
 *   ...
 * </wsrmp:RMAssertion>
 */
/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class Rm11Assertion extends ComplexAssertion implements RmConfigurator {
    // TODO: add new assertions for acknowledgement interval and backoff algorithm

    private static final Logger LOGGER = Logger.getLogger(Rm11Assertion.class);
    //
    public static final QName NAME = RmVersion.WSRM200702.rmAssertionName;
    private static final QName SEQUENCE_STR_QNAME = AssertionNamespace.WSRMP_200702.getQName("SequenceSTR");
    private static final QName SEQUENCE_TRANSPORT_SECURITY_QNAME = AssertionNamespace.WSRMP_200702.getQName("SequenceTransportSecurity");
    private static AssertionInstantiator instantiator = new AssertionInstantiator() {

        public PolicyAssertion newInstance(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) throws AssertionCreationException {
            return new Rm11Assertion(data, assertionParameters, nestedAlternative);
        }
    };

    public static AssertionInstantiator getInstantiator() {
        return instantiator;
    }
    private final SecurityBinding securityBinding;
    private final DeliveryAssurance deliveryAssurance;
    private final boolean isOrderedDelivery;

    private Rm11Assertion(AssertionData data, Collection<? extends PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) throws AssertionCreationException {
        super(data, assertionParameters, nestedAlternative);

        SecurityBinding _securityBinding = SecurityBinding.NONE;
        DeliveryAssuranceAssertion deliveryAssuranceAssertion = null;

        if (nestedAlternative != null) {
            for (PolicyAssertion nestedAssertion : nestedAlternative) {
                if (SEQUENCE_STR_QNAME.equals(nestedAssertion.getName())) {
                    _securityBinding = evaluateDeliveryAssurance(_securityBinding == SecurityBinding.NONE, SecurityBinding.STR, data);
                } else if (SEQUENCE_TRANSPORT_SECURITY_QNAME.equals(nestedAssertion.getName())) {
                    _securityBinding = evaluateDeliveryAssurance(_securityBinding == SecurityBinding.NONE, SecurityBinding.TRANSPORT, data);
                } else if (DeliveryAssuranceAssertion.NAME.equals(nestedAssertion.getName())) {
                    deliveryAssuranceAssertion = (DeliveryAssuranceAssertion) nestedAssertion;
                }
            }
        }
        
        if (deliveryAssuranceAssertion == null) {
            deliveryAssurance = DeliveryAssurance.getDefault();
            isOrderedDelivery = false;
        } else {
            deliveryAssurance = deliveryAssuranceAssertion.getDeliveryAssurance();
            isOrderedDelivery = deliveryAssuranceAssertion.isOrderedDelivery();            
        }

        securityBinding = _securityBinding;
    }

    public DeliveryAssurance getDeliveryAssurance() {
        return deliveryAssurance;
    }

    public boolean isOrderedDelivery() {
        return isOrderedDelivery;
    }

    public SecurityBinding getSecurityBinding() {
        return securityBinding;
    }

    private SecurityBinding evaluateDeliveryAssurance(boolean successCondition, SecurityBinding bindingOnSuccess, AssertionData data) throws AssertionCreationException {
        if (successCondition) {
            return bindingOnSuccess;
        } else {
            throw LOGGER.logSevereException(new AssertionCreationException(data, LocalizationMessages.WSRM_1005_MULTIPLE_SECURITY_BINDINGS_IN_POLICY()));
        }
    }

    public ReliableMessagingFeatureBuilder update(ReliableMessagingFeatureBuilder builder) {
        if (builder.getVersion() != RmVersion.WSRM200702) {
            // TODO L10N
            throw new WebServiceException("Multiple WS-ReliableMessaging versions detected within a single WS-Policy expression.");
        }

        if (isOrderedDelivery) {
            builder = builder.enableOrderedDelivery();
        }

        return builder.deliveryAssurance(deliveryAssurance).securityBinding(securityBinding);
    }
    
    public boolean isCompatibleWith(RmVersion version) {
        return RmVersion.WSRM200702 == version;
    }
}
