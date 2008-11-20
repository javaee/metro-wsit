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
package com.sun.xml.ws.rm;

import com.sun.xml.ws.api.FeatureConstructor;
import javax.xml.ws.WebServiceFeature;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class ReliableMessagingFeature extends WebServiceFeature {

    public static final String ID = "com.sun.xml.ws.rm.ReliableMessagingFeature";
    //
    public static final long UNSPECIFIED = -1;
    public static final long DEFAULT_INACTIVITY_TIMEOUT = 600000;
    public static final long DEFAULT_CLOSE_SEQUENCE_OPERATION_TIMEOUT = 3000;
//    public static final SecurityBinding DEFAULT_SECURITY_BINDING = SecurityBinding.NONE;
//    public static final DeliveryAssurance DEFAULT_DELIVERY_ASSURANCE = DeliveryAssurance.EXACTLY_ONCE;

    public static enum SecurityBinding {

        STR,
        TRANSPORT,
        NONE;

        public static SecurityBinding getDefault() {
            return NONE; // if changed, update also in ReliableMesaging annotation
        }
    }

    public static enum DeliveryAssurance {

        EXACTLY_ONCE,
        AT_LEAST_ONCE,
        AT_MOST_ONCE;

        public static DeliveryAssurance getDefault() {
            return DeliveryAssurance.EXACTLY_ONCE; // if changed, update also in ReliableMesaging annotation
        }
    }

    public static enum BackoffAlgorithm {

        LINEAR,
        EXPONENTIAL;

        public static BackoffAlgorithm getDefault() {
            return BackoffAlgorithm.LINEAR; // if changed, update also in ReliableMesaging annotation
        }
    }

    // General RM config values
    private final RmVersion version;
    private final long inactivityTimeout;
    private final long bufferQuota;
    private final boolean orderedDelivery;
    private final DeliveryAssurance deliveryAssurance;
    private final SecurityBinding securityBinding;
    private final long acknowledgementInterval;
    // Client-specific RM config values
    private final long baseRetransmissionInterval;
    private final BackoffAlgorithm retransmissionBackoffAlgorithm;
    private final long ackRequestInterval;
    private final long closeSequenceOperationTimeout;

    public ReliableMessagingFeature() {
        // this constructor is here just to satisfy JAX-WS specification requirements
        this(true);
    }

    public ReliableMessagingFeature(boolean enabled) {
        // this constructor is here just to satisfy JAX-WS specification requirements
        this(
                enabled, // this.enabled
                RmVersion.getDefault(), // this.rmVersion
                DEFAULT_INACTIVITY_TIMEOUT, // this.inactivityTimeout
                UNSPECIFIED, // this.bufferQuota
                false, // this.orderedDelivery
                DeliveryAssurance.getDefault(), // this.deliveryAssurance
                SecurityBinding.getDefault(), // this.securityBinding
                UNSPECIFIED, // this.acknowledgementInterval
                UNSPECIFIED, // this.baseRetransmissionInterval
                BackoffAlgorithm.getDefault(), // this.retransmissionBackoffAlgorithm
                UNSPECIFIED, // this.ackRequestInterval
                DEFAULT_CLOSE_SEQUENCE_OPERATION_TIMEOUT // this.closeSequenceOperationTimeout
                );

    }

    @FeatureConstructor({
        "enabled",
        "version",
        "inactivityTimeout",
        "bufferQuota",
        "orderedDelivery",
        "deliveryAssurance",
        "securityBinding",
        "acknowledgementInterval",
        "baseRetransmissionInterval",
        "retransmissionBackoffAlgorithm",
        "ackRequestInterval",
        "closeSequenceOperationTimeout"
    })
    public ReliableMessagingFeature(
            boolean enabled,
            RmVersion version,
            long inactivityTimeout,
            long bufferQuota,
            boolean orderedDelivery,
            DeliveryAssurance deliveryAssurance,
            SecurityBinding securityBinding,
            long acknowledgementInterval,
            long baseRetransmissionInterval,
            BackoffAlgorithm retransmissionBackoffAlgorithm,
            long ackRequestInterval,
            long closeSequenceOperationTimeout) {

        this.enabled = enabled;
        this.version = version;
        this.inactivityTimeout = inactivityTimeout;
        this.bufferQuota = bufferQuota;
        this.orderedDelivery = orderedDelivery;
        this.deliveryAssurance = deliveryAssurance;
        this.securityBinding = securityBinding;
        this.acknowledgementInterval = acknowledgementInterval;
        this.baseRetransmissionInterval = baseRetransmissionInterval;
        this.retransmissionBackoffAlgorithm = retransmissionBackoffAlgorithm;
        this.ackRequestInterval = ackRequestInterval;
        this.closeSequenceOperationTimeout = closeSequenceOperationTimeout;
    }
    
    @Override
    public String getID() {
        return ID;
    }

    public RmVersion getVersion() {
        return version;
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

    public long getBaseRetransmissionInterval() {
        return baseRetransmissionInterval;
    }

    public BackoffAlgorithm getRetransmissionBackoffAlgorithm() {
        return retransmissionBackoffAlgorithm;
    }

    public long getAcknowledgementRequestInterval() {
        return ackRequestInterval;
    }

    public long getCloseSequenceOperationTimeout() {
        return closeSequenceOperationTimeout;
    }
}
