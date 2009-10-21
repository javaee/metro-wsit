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
import java.util.Collection;
import javax.xml.namespace.QName;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.ComplexAssertion;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.rx.policy.assertion.AssertionInstantiator;
import com.sun.xml.ws.rx.policy.assertion.AssertionNamespace;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.ReliableMessagingFeature.DeliveryAssurance;

/**
 * <wsrmp:DeliveryAssurance>
 *   <wsp:Policy>
 *     [ <wsrmp:ExactlyOnce/> |
 *       <wsrmp:AtLeastOnce/> |
 *       <wsrmp:AtMostOnce/> ]
 *     <wsrmp:InOrder/> ?
 *   </wsp:Policy>
 * </wsrmp:DeliveryAssurance>
 */
/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class DeliveryAssuranceAssertion extends ComplexAssertion {

    private static final Logger LOGGER = Logger.getLogger(DeliveryAssuranceAssertion.class);
    private static final QName EXACTLY_ONCE_QNAME = AssertionNamespace.WSRMP_200702.getQName("ExactlyOnce");
    private static final QName AT_LEAST_ONCE_QNAME = AssertionNamespace.WSRMP_200702.getQName("AtLeastOnce");
    private static final QName AT_MOST_ONCE_QNAME = AssertionNamespace.WSRMP_200702.getQName("AtMostOnce");
    private static final QName IN_ORDER_QNAME = AssertionNamespace.WSRMP_200702.getQName("InOrder");
    public static final QName NAME = AssertionNamespace.WSRMP_200702.getQName("DeliveryAssurance");
    private static AssertionInstantiator instantiator = new AssertionInstantiator() {

        public PolicyAssertion newInstance(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) throws AssertionCreationException {
            return new DeliveryAssuranceAssertion(data, assertionParameters, nestedAlternative);
        }
    };

    public static AssertionInstantiator getInstantiator() {
        return instantiator;
    }
    private final DeliveryAssurance deliveryAssurance;
    private final boolean orderedDelivery;

    private DeliveryAssuranceAssertion(AssertionData data, Collection<? extends PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) throws AssertionCreationException {
        super(data, assertionParameters, nestedAlternative);

        DeliveryAssurance _deliveryAssurance = null;
        boolean _orderedDelivery = false;

        if (nestedAlternative != null) {
            for (PolicyAssertion nestedAssertion : nestedAlternative) {

                if (EXACTLY_ONCE_QNAME.equals(nestedAssertion.getName())) {
                    _deliveryAssurance = evaluateDeliveryAssurance(_deliveryAssurance == null, DeliveryAssurance.EXACTLY_ONCE, data);
                } else if (AT_LEAST_ONCE_QNAME.equals(nestedAssertion.getName())) {
                    _deliveryAssurance = evaluateDeliveryAssurance(_deliveryAssurance == null, DeliveryAssurance.AT_LEAST_ONCE, data);
                } else if (AT_MOST_ONCE_QNAME.equals(nestedAssertion.getName())) {
                    _deliveryAssurance = evaluateDeliveryAssurance(_deliveryAssurance == null, DeliveryAssurance.AT_MOST_ONCE, data);
                } else if (IN_ORDER_QNAME.equals(nestedAssertion.getName())) {
                    _orderedDelivery = true;
                }
            }
        }
        deliveryAssurance = (_deliveryAssurance == null) ? DeliveryAssurance.getDefault() : _deliveryAssurance;
        orderedDelivery = _orderedDelivery;
    }

    public DeliveryAssurance getDeliveryAssurance() {
        return deliveryAssurance;
    }

    public boolean isOrderedDelivery() {
        return orderedDelivery;
    }

    private DeliveryAssurance evaluateDeliveryAssurance(boolean successCondition, DeliveryAssurance daOnSuccess, AssertionData data) throws AssertionCreationException {
        if (successCondition) {
            return daOnSuccess;
        } else {
            throw LOGGER.logSevereException(new AssertionCreationException(data, LocalizationMessages.WSRM_1003_MUTLIPLE_DA_TYPES_IN_POLICY()));
        }
    }
}
