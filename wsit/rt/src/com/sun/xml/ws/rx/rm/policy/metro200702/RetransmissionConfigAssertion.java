/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rx.rm.policy.metro200702;

import com.sun.istack.NotNull;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.ComplexAssertion;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.rx.policy.AssertionInstantiator;
import com.sun.xml.ws.rx.rm.policy.RmAssertionNamespace;
import com.sun.xml.ws.rx.rm.policy.RmConfigurator;
import com.sun.xml.ws.rx.rm.ReliableMessagingFeature;
import com.sun.xml.ws.rx.rm.ReliableMessagingFeature.BackoffAlgorithm;
import com.sun.xml.ws.rx.rm.ReliableMessagingFeatureBuilder;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import java.util.Collection;
import javax.xml.namespace.QName;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class RetransmissionConfigAssertion extends ComplexAssertion implements RmConfigurator {

    public static final QName NAME = RmAssertionNamespace.METRO_200702.getQName("RetransmissionConfig");
    //
    private static final Logger LOGGER = Logger.getLogger(RetransmissionConfigAssertion.class);
    //
    private static final QName INTERVAL_PARAMETER_QNAME = RmAssertionNamespace.METRO_200702.getQName("Interval");
    private static final QName ALGORITHM_PARAMETER_QNAME = RmAssertionNamespace.METRO_200702.getQName("Algorithm");
    private static final QName MAX_RETRIES_PARAMETER_QNAME = RmAssertionNamespace.METRO_200702.getQName("MaxRetries");
    //
    private static final QName MILLISECONDS_ATTRIBUTE_QNAME = new QName("", "Milliseconds");
    //
    private static AssertionInstantiator instantiator = new AssertionInstantiator() {

        public PolicyAssertion newInstance(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) throws AssertionCreationException {
            return new RetransmissionConfigAssertion(data, assertionParameters, nestedAlternative);
        }
    };

    public static AssertionInstantiator getInstantiator() {
        return instantiator;
    }
    private final long interval;
    private final long maxRetries;
    private final ReliableMessagingFeature.BackoffAlgorithm algorithm;

    private RetransmissionConfigAssertion(AssertionData data, Collection<? extends PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) throws AssertionCreationException {
        super(data, assertionParameters, nestedAlternative);

        if (assertionParameters == null || assertionParameters.isEmpty()) {
            // TODO P1
            throw new AssertionCreationException(data, "No assertion parameters found.");
        }
        PolicyAssertion _interval = getParameter(INTERVAL_PARAMETER_QNAME, data, assertionParameters);
        interval = (_interval == null) ? ReliableMessagingFeature.DEFAULT_MESSAGE_RETRANSMISSION_INTERVAL : Long.parseLong(_interval.getAttributeValue(MILLISECONDS_ATTRIBUTE_QNAME));

        PolicyAssertion _maxRetries = getParameter(MAX_RETRIES_PARAMETER_QNAME, data, assertionParameters);
        maxRetries = (_maxRetries == null) ? ReliableMessagingFeature.DEFAULT_MAX_MESSAGE_RETRANSMISSION_COUNT : Long.parseLong(_maxRetries.getValue());

        final PolicyAssertion algorithmParameter = getParameter(ALGORITHM_PARAMETER_QNAME, data, assertionParameters);
        BackoffAlgorithm _algorithm = (algorithmParameter == null) ? null : ReliableMessagingFeature.BackoffAlgorithm.parse(algorithmParameter.getValue());
        algorithm = (_algorithm == null) ? ReliableMessagingFeature.BackoffAlgorithm.getDefault() : _algorithm;
    }

    private static PolicyAssertion getParameter(@NotNull QName parameterName, AssertionData data, @NotNull Collection<? extends PolicyAssertion> assertionParameters) throws AssertionCreationException {
        assert parameterName != null;
        assert assertionParameters != null;

        PolicyAssertion parameter = null;
        boolean parameterSet = false;

        for (PolicyAssertion assertion : assertionParameters) {
            if (parameterName.equals(assertion.getName())) {
                if (parameterSet) {
                    throw LOGGER.logSevereException(new AssertionCreationException(
                            data,
                            LocalizationMessages.WSRM_1007_MULTIPLE_OCCURENCES_OF_ASSERTION_PARAMETER(parameterName, NAME)));
                } else {
                    parameter = assertion;
                }
            }
        }

        return parameter;
    }

    public BackoffAlgorithm getAlgorithm() {
        return algorithm;
    }

    public long getInterval() {
        return interval;
    }

    public long getMaxRetries() {
        return maxRetries;
    }

    public ReliableMessagingFeatureBuilder update(ReliableMessagingFeatureBuilder builder) {
        return builder.messageRetransmissionInterval(interval).retransmissionBackoffAlgorithm(algorithm).maxMessageRetransmissionCount(maxRetries);
    }

    public boolean isCompatibleWith(RmVersion version) {
        return RmVersion.WSRM200702 == version;
    }
}
