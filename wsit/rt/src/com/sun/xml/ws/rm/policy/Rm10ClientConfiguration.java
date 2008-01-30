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
package com.sun.xml.ws.rm.policy;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.rm.RMVersion;
import com.sun.xml.ws.rm.policy.Configuration.DeliveryAssurance;
import com.sun.xml.ws.rm.policy.Configuration.SecurityBinding;
import com.sun.xml.ws.rm.policy.assertion.AckRequestIntervalClientAssertion;
import com.sun.xml.ws.rm.policy.assertion.CloseTimeoutClientAssertion;
import com.sun.xml.ws.rm.policy.assertion.ResendIntervalClientAssertion;
import com.sun.xml.ws.rm.policy.assertion.Rm10Assertion;

class Rm10ClientConfiguration implements Configuration {

    private final Configuration destinationConfig;
    private final long retransmittionInterval;
    private final boolean exponentialBackoff;
    private final long ackRequestInterval;
    private final long closeSequenceOperationTimeout;

    public Rm10ClientConfiguration(AssertionSet alternative, SOAPVersion soapVersion, AddressingVersion addressingVersion) {
        destinationConfig = new Rm10ServiceConfiguration(alternative, soapVersion, addressingVersion);

        Rm10Assertion rmAssertion = ConfigurationManager.extractAssertion(alternative, Rm10Assertion.NAME, Rm10Assertion.class);
        exponentialBackoff = rmAssertion.useExponentialBackoffAlgorithm();
        long _retransmittionInterval = rmAssertion.getBaseRetransmittionInterval();
        
        AckRequestIntervalClientAssertion ackIntervalAssertion = ConfigurationManager.extractAssertion(alternative, AckRequestIntervalClientAssertion.NAME, AckRequestIntervalClientAssertion.class);
        ackRequestInterval = (ackIntervalAssertion != null) ? ackIntervalAssertion.getInterval() : DEFAULT_ACKNOWLEDGEMENT_REQUEST_INTERVAL;
        
        CloseTimeoutClientAssertion closeTimeoutAssertion = ConfigurationManager.extractAssertion(alternative, CloseTimeoutClientAssertion.NAME, CloseTimeoutClientAssertion.class);
        closeSequenceOperationTimeout = (closeTimeoutAssertion != null) ? closeTimeoutAssertion.getTimeout() : DEFAULT_CLOSE_SEQUENCE_OPERATION_TIMEOUT;
        
        ResendIntervalClientAssertion resendIntervalAssertion = ConfigurationManager.extractAssertion(alternative, ResendIntervalClientAssertion.NAME, ResendIntervalClientAssertion.class);
        if (resendIntervalAssertion != null) {
            // override server settings
            _retransmittionInterval = resendIntervalAssertion.getInterval();
        }
        retransmittionInterval = _retransmittionInterval;
    }

    public long getMessageRetransmissionInterval() {
        return retransmittionInterval;
    }

    public boolean useExponetialBackoffRetransmission() {
        return exponentialBackoff;
    }

    public long getAcknowledgementRequestInterval() {
        return ackRequestInterval;
    }

    public long getCloseSequenceOperationTimeout() {
        return closeSequenceOperationTimeout;
    }

    public RMVersion getRMVersion() {
        return destinationConfig.getRMVersion();
    }

    public SOAPVersion getSoapVersion() {
        return destinationConfig.getSoapVersion();
    }

    public AddressingVersion getAddressingVersion() {
        return destinationConfig.getAddressingVersion();
    }

    public long getInactivityTimeout() {
        return destinationConfig.getInactivityTimeout();
    }

    public long getSequenceAcknowledgementInterval() {
        return destinationConfig.getSequenceAcknowledgementInterval();
    }

    public SecurityBinding getSecurityBinding() {
        return destinationConfig.getSecurityBinding();
    }

    public DeliveryAssurance getDeliveryAssurance() {
        return destinationConfig.getDeliveryAssurance();
    }

    public boolean isOrderedDelivery() {
        return destinationConfig.isOrderedDelivery();
    }

    public long getDestinationBufferQuota() {
        return destinationConfig.getDestinationBufferQuota();
    }
}
