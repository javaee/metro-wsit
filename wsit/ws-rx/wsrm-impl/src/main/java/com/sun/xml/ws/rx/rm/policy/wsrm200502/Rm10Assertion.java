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

package com.sun.xml.ws.rx.rm.policy.wsrm200502;

import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeature;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeatureBuilder;
import java.util.Collection;
import javax.xml.namespace.QName;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.SimpleAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;

import com.sun.xml.ws.rx.policy.AssertionInstantiator;
import com.sun.xml.ws.rx.rm.api.RmAssertionNamespace;
import com.sun.xml.ws.rx.rm.policy.RmConfigurator;
import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeature.BackoffAlgorithm;
import com.sun.xml.ws.rx.rm.api.RmProtocolVersion;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import javax.xml.ws.WebServiceException;

/**
 * <wsrm:RMAssertion [wsp:Optional="true"]? ... >
 *   <wsrm:InactivityTimeout Milliseconds="xs:unsignedLong" ... /> ?
 *   <wsrm:BaseRetransmissionInterval Milliseconds="xs:unsignedLong".../>?
 *   <wsrm:ExponentialBackoff ... /> ?
 *   <wsrm:AcknowledgementInterval Milliseconds="xs:unsignedLong" ... /> ?
 *   ...
 * </wsrm:RMAssertion>
 */
/** 
 * Specifies that WS-ReliableMessaging protocol MUST be used when sending messages.
 * Defines also the version of the WS-RM protocol to be used.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class Rm10Assertion extends SimpleAssertion implements RmConfigurator {

    public static final QName NAME = RmProtocolVersion.WSRM200502.rmAssertionName;
    private static final QName INACTIVITY_TIMEOUT_QNAME = RmAssertionNamespace.WSRMP_200502.getQName("InactivityTimeout");
    private static final QName RETRANSMITTION_INTERVAL_QNAME = RmAssertionNamespace.WSRMP_200502.getQName("BaseRetransmissionInterval");
    private static final QName EXPONENTIAL_BACKOFF_QNAME = RmAssertionNamespace.WSRMP_200502.getQName("ExponentialBackoff");
    private static final QName MILISECONDS_ATTRIBUTE_QNAME = new QName("", "Milliseconds");
    private static AssertionInstantiator instantiator = new AssertionInstantiator() {

        public PolicyAssertion newInstance(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) {
            return new Rm10Assertion(data, assertionParameters);
        }
    };

    public static AssertionInstantiator getInstantiator() {
        return instantiator;
    }
    private final long inactivityTimeout;
    private final long retransmittionInterval;
    private final boolean useExponentialBackoffAlgorithm;

    private Rm10Assertion(AssertionData data, Collection<? extends PolicyAssertion> assertionParameters) {
        super(data, assertionParameters);

        long _inactivityTimeout = ReliableMessagingFeature.DEFAULT_SEQUENCE_INACTIVITY_TIMEOUT;
        long _retransmittionInterval = ReliableMessagingFeature.DEFAULT_MESSAGE_RETRANSMISSION_INTERVAL;
        boolean _useExponentialBackoffAlgorithm = false;

        if (assertionParameters != null) {
            for (PolicyAssertion parameter : assertionParameters) {
                if (INACTIVITY_TIMEOUT_QNAME.equals(parameter.getName())) {
                    _inactivityTimeout = Long.parseLong(parameter.getAttributeValue(MILISECONDS_ATTRIBUTE_QNAME));
                } else if (RETRANSMITTION_INTERVAL_QNAME.equals(parameter.getName())) {
                    _retransmittionInterval = Long.parseLong(parameter.getAttributeValue(MILISECONDS_ATTRIBUTE_QNAME));
                } else if (EXPONENTIAL_BACKOFF_QNAME.equals(parameter.getName())) {
                    _useExponentialBackoffAlgorithm = true;
                }
            }
        }

        inactivityTimeout = _inactivityTimeout;
        retransmittionInterval = _retransmittionInterval;
        useExponentialBackoffAlgorithm = _useExponentialBackoffAlgorithm;
    }

    public long getInactivityTimeout() {
        return inactivityTimeout;
    }

    public long getBaseRetransmittionInterval() {
        return retransmittionInterval;
    }

    public boolean useExponentialBackoffAlgorithm() {
        return useExponentialBackoffAlgorithm;
    }

    public ReliableMessagingFeatureBuilder update(ReliableMessagingFeatureBuilder builder) {
        if (builder.getProtocolVersion() != RmProtocolVersion.WSRM200502) {
            throw new WebServiceException(LocalizationMessages.WSRM_1002_MULTIPLE_WSRM_VERSIONS_IN_POLICY());
        }

        if (inactivityTimeout != ReliableMessagingFeature.DEFAULT_SEQUENCE_INACTIVITY_TIMEOUT) { // prevents overwriting values set by other assertions
            builder.sequenceInactivityTimeout(inactivityTimeout);
        }
        if (inactivityTimeout != ReliableMessagingFeature.DEFAULT_MESSAGE_RETRANSMISSION_INTERVAL) { // prevents overwriting values set by other assertions
            builder.messageRetransmissionInterval(retransmittionInterval);
        }

        if (useExponentialBackoffAlgorithm) {
            builder.retransmissionBackoffAlgorithm(BackoffAlgorithm.EXPONENTIAL);
        }

        return builder;
    }

    public boolean isCompatibleWith(RmProtocolVersion version) {
        return RmProtocolVersion.WSRM200502 == version;
    }
}
