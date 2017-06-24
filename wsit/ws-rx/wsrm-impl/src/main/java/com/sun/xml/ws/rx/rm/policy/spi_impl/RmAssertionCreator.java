/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.rx.rm.policy.spi_impl;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.policy.spi.PolicyAssertionCreator;
import com.sun.xml.ws.rx.rm.api.RmAssertionNamespace;
import com.sun.xml.ws.rx.rm.policy.net200502.RmFlowControlAssertion;
import com.sun.xml.ws.rx.rm.policy.wsrm200502.Rm10Assertion;
import com.sun.xml.ws.rx.rm.policy.wsrm200702.DeliveryAssuranceAssertion;
import com.sun.xml.ws.rx.rm.policy.wsrm200702.Rm11Assertion;
import com.sun.xml.ws.rx.rm.policy.metro200603.AckRequestIntervalClientAssertion;
import com.sun.xml.ws.rx.rm.policy.net200702.AcknowledgementIntervalAssertion;
import com.sun.xml.ws.rx.rm.policy.metro200603.AllowDuplicatesAssertion;
import com.sun.xml.ws.rx.rm.policy.metro200603.CloseTimeoutClientAssertion;
import com.sun.xml.ws.rx.rm.policy.net200702.InactivityTimeoutAssertion;
import com.sun.xml.ws.rx.rm.policy.metro200603.OrderedDeliveryAssertion;
import com.sun.xml.ws.rx.rm.policy.metro200603.ResendIntervalClientAssertion;
import com.sun.xml.ws.rx.policy.AssertionInstantiator;
import com.sun.xml.ws.rx.rm.policy.metro200702.AckRequestIntervalAssertion;
import com.sun.xml.ws.rx.rm.policy.metro200702.CloseSequenceTimeoutAssertion;
import com.sun.xml.ws.rx.rm.policy.metro200702.MaintenanceTaskPeriodAssertion;
import com.sun.xml.ws.rx.rm.policy.metro200702.MaxConcurrentSessionsAssertion;
import com.sun.xml.ws.rx.rm.policy.metro200702.PersistentAssertion;
import com.sun.xml.ws.rx.rm.policy.metro200702.RetransmissionConfigAssertion;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class RmAssertionCreator implements PolicyAssertionCreator {

    private static final Map<QName, AssertionInstantiator> instantiationMap = new HashMap<QName, AssertionInstantiator>();
    static {
        // metro200603
        instantiationMap.put(AckRequestIntervalClientAssertion.NAME, AckRequestIntervalClientAssertion.getInstantiator());
        instantiationMap.put(AllowDuplicatesAssertion.NAME, AllowDuplicatesAssertion.getInstantiator());
        instantiationMap.put(CloseTimeoutClientAssertion.NAME, CloseTimeoutClientAssertion.getInstantiator());
        instantiationMap.put(OrderedDeliveryAssertion.NAME, OrderedDeliveryAssertion.getInstantiator());
        instantiationMap.put(ResendIntervalClientAssertion.NAME, ResendIntervalClientAssertion.getInstantiator());

        // metro200702
        instantiationMap.put(AckRequestIntervalAssertion.NAME, AckRequestIntervalAssertion.getInstantiator());
        instantiationMap.put(CloseSequenceTimeoutAssertion.NAME, CloseSequenceTimeoutAssertion.getInstantiator());
        instantiationMap.put(MaintenanceTaskPeriodAssertion.NAME, MaintenanceTaskPeriodAssertion.getInstantiator());
        instantiationMap.put(MaxConcurrentSessionsAssertion.NAME, MaxConcurrentSessionsAssertion.getInstantiator());
        instantiationMap.put(PersistentAssertion.NAME, PersistentAssertion.getInstantiator());
        instantiationMap.put(RetransmissionConfigAssertion.NAME, RetransmissionConfigAssertion.getInstantiator());

        // net200502
        instantiationMap.put(RmFlowControlAssertion.NAME, RmFlowControlAssertion.getInstantiator());

        // net200702
        instantiationMap.put(AcknowledgementIntervalAssertion.NAME, AcknowledgementIntervalAssertion.getInstantiator());
        instantiationMap.put(InactivityTimeoutAssertion.NAME, InactivityTimeoutAssertion.getInstantiator());

        // wsrm200502
        instantiationMap.put(Rm10Assertion.NAME, Rm10Assertion.getInstantiator());

        // wsrm200702
        instantiationMap.put(DeliveryAssuranceAssertion.NAME, DeliveryAssuranceAssertion.getInstantiator());
        instantiationMap.put(Rm11Assertion.NAME, Rm11Assertion.getInstantiator());

    }    
    
    private static final List<String> SUPPORTED_DOMAINS = Collections.unmodifiableList(RmAssertionNamespace.namespacesList());

    public String[] getSupportedDomainNamespaceURIs() {
        return SUPPORTED_DOMAINS.toArray(new String[SUPPORTED_DOMAINS.size()]);
    }

    public PolicyAssertion createAssertion(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative, PolicyAssertionCreator defaultCreator) throws AssertionCreationException {
        AssertionInstantiator instantiator = instantiationMap.get(data.getName());
        if (instantiator != null) {
            return instantiator.newInstance(data, assertionParameters, nestedAlternative);
        } else {
            return defaultCreator.createAssertion(data, assertionParameters, nestedAlternative, null);
        }
    }
}
