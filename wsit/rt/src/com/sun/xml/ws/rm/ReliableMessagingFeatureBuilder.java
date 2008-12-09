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

import static com.sun.xml.ws.rm.ReliableMessagingFeature.*;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public final class ReliableMessagingFeatureBuilder {
    // General RM config values
    private boolean enabled = true;
    private RmVersion version = RmVersion.getDefault();
    private long inactivityTimeout = DEFAULT_SEQUENCE_INACTIVITY_TIMEOUT;
    private long destinationBufferQuota = DEFAULT_DESTINATION_BUFFER_QUOTA;
    private boolean orderedDelivery = false;
    private boolean makeConnection = false;
    private DeliveryAssurance deliveryAssurance = DeliveryAssurance.getDefault();
    private SecurityBinding securityBinding = SecurityBinding.getDefault();
    // Client-specific RM config values
    private long messageRetransmissionInterval = DEFAULT_MESSAGE_RETRANSMISSION_INTERVAL;
    private BackoffAlgorithm retransmissionBackoffAlgorithm = BackoffAlgorithm.getDefault();
    private long ackRequestInterval = DEFAULT_ACK_REQUESTED_INTERVAL;
    private long closeSequenceOperationTimeout = DEFAULT_CLOSE_SEQUENCE_OPERATION_TIMEOUT;

    public ReliableMessagingFeatureBuilder() {
    }

    public ReliableMessagingFeatureBuilder(RmVersion version) {
        this.version = version;
    }

    public ReliableMessagingFeature build() {
        return new ReliableMessagingFeature(
                this.enabled,
                this.version,
                this.inactivityTimeout,
                this.destinationBufferQuota,
                this.orderedDelivery,
                this.makeConnection,
                this.deliveryAssurance,
                this.securityBinding,
                this.messageRetransmissionInterval,
                this.retransmissionBackoffAlgorithm,
                this.ackRequestInterval,
                this.closeSequenceOperationTimeout);
    }

    /**
     * @see ReliableMessagingFeature#getAcknowledgementRequestInterval()
     */
    public ReliableMessagingFeatureBuilder ackRequestInterval(long ackRequestInterval) {
        this.ackRequestInterval = ackRequestInterval;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getMessageRetransmissionInterval() 
     */
    public ReliableMessagingFeatureBuilder messageRetransmissionInterval(long messageRetransmissionInterval) {
        this.messageRetransmissionInterval = messageRetransmissionInterval;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getDestinationBufferQuota()
     */
    public ReliableMessagingFeatureBuilder destinationBufferQuota(long bufferQuota) {
        this.destinationBufferQuota = bufferQuota;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getCloseSequenceOperationTimeout()
     */
    public ReliableMessagingFeatureBuilder closeSequenceOperationTimeout(long closeSequenceOperationTimeout) {
        this.closeSequenceOperationTimeout = closeSequenceOperationTimeout;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getDeliveryAssurance()
     */
    public ReliableMessagingFeatureBuilder deliveryAssurance(DeliveryAssurance deliveryAssurance) {
        this.deliveryAssurance = deliveryAssurance;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getSequenceInactivityTimeout()
     */
    public ReliableMessagingFeatureBuilder sequenceInactivityTimeout(long inactivityTimeout) {
        this.inactivityTimeout = inactivityTimeout;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#isOrderedDeliveryEnabled()
     */
    public ReliableMessagingFeatureBuilder enableOrderedDelivery() {
        this.orderedDelivery = true;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#isMakeConnectionEnabled()
     */
    public ReliableMessagingFeatureBuilder enableMakeConnection() {
        this.makeConnection = true;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getRetransmissionBackoffAlgorithm()
     */
    public ReliableMessagingFeatureBuilder retransmissionBackoffAlgorithm(BackoffAlgorithm retransmissionBackoffAlgorithm) {
        this.retransmissionBackoffAlgorithm = retransmissionBackoffAlgorithm;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getVersion()
     */
    public ReliableMessagingFeatureBuilder version(RmVersion version) {
        this.version = version;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getSecurityBinding()
     */
    public ReliableMessagingFeatureBuilder securityBinding(SecurityBinding securityBinding) {
        this.securityBinding = securityBinding;
        return this;
    }
}
