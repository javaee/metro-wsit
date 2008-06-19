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
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.RmRuntimeException;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.assertion.Rm10Assertion;
import com.sun.xml.ws.rm.policy.assertion.Rm11Assertion;
import com.sun.xml.ws.rm.policy.assertion.RmFlowControlAssertion;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class Rm11ServiceConfiguration implements Configuration {
     private static final RmLogger LOGGER = RmLogger.getLogger(Rm11ServiceConfiguration.class);
    
    private SOAPVersion soapVersion;
    private AddressingVersion addressingVersion;
    private final long inactivityTimeout;
    private final long bufferQuota;
    private final boolean orderedDelivery;
    private final DeliveryAssurance deliveryAssurance;
    private final SecurityBinding securityBinding;
    private final long acknowledgementInterval;
    private final boolean requestResponseDetected;

    public Rm11ServiceConfiguration(AssertionSet alternative, SOAPVersion soapVersion, AddressingVersion addressingVersion, boolean requestResponseDetected) throws RmRuntimeException {
        this.soapVersion = soapVersion;
        this.addressingVersion = addressingVersion;
        this.requestResponseDetected = requestResponseDetected;
        
        if (alternative.contains(Rm10Assertion.NAME)) {
            throw LOGGER.logSevereException(new RmRuntimeException(LocalizationMessages.WSRM_1002_MULTIPLE_WSRM_VERSIONS_IN_POLICY()));
        }
        
        Rm11Assertion rmAssertion = ConfigurationManager.extractAssertion(alternative, Rm11Assertion.NAME, Rm11Assertion.class);
        deliveryAssurance = rmAssertion.getDeliveryAssurance();
        orderedDelivery = rmAssertion.isOrderedDelivery();
        securityBinding = rmAssertion.getSecurityBinding();
        
        RmFlowControlAssertion rmFlowControlAssertion = ConfigurationManager.extractAssertion(alternative, RmFlowControlAssertion.NAME, RmFlowControlAssertion.class);
        bufferQuota = (rmFlowControlAssertion != null) ? rmFlowControlAssertion.getMaximumBufferSize() : UNSPECIFIED;
        
        // TODO: add new assertions for these
        inactivityTimeout = DEFAULT_INACTIVITY_TIMEOUT;
        acknowledgementInterval = UNSPECIFIED;        
    }

    public RmVersion getRmVersion() {
        return RmVersion.WSRM11;
    }

    public SOAPVersion getSoapVersion() {
        return soapVersion;
    }

    public AddressingVersion getAddressingVersion() {
        return addressingVersion;
    }

    public boolean requestResponseOperationsDetected() {
        return requestResponseDetected;
    }

    public long getInactivityTimeout() {
        return inactivityTimeout;
    }

    public long getSequenceAcknowledgementInterval() {
        return acknowledgementInterval;
    }

    public SecurityBinding getSecurityBinding() {
        return securityBinding;
    }

    public DeliveryAssurance getDeliveryAssurance() {
        return deliveryAssurance;
    }

    public boolean isOrderedDelivery() {
        return orderedDelivery;
    }

    public long getDestinationBufferQuota() {
        return bufferQuota;
    }

    public long getMessageRetransmissionInterval() {
        throw new UnsupportedOperationException("Not supported on the service side.");
    }

    public boolean useExponetialBackoffRetransmission() {
        throw new UnsupportedOperationException("Not supported on the service side.");
    }

    public long getAcknowledgementRequestInterval() {
        throw new UnsupportedOperationException("Not supported on the service side.");
    }

    public long getCloseSequenceOperationTimeout() {
        throw new UnsupportedOperationException("Not supported on the service side.");
    }
}
