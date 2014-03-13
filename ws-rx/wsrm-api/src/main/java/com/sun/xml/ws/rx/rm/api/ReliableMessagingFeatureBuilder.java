/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.rx.rm.api;

import static com.sun.xml.ws.rx.rm.api.ReliableMessagingFeature.*;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public final class ReliableMessagingFeatureBuilder {
    // General RM config values
    private final RmProtocolVersion protocolVersion;
    
    private boolean enabled = true;
    private long inactivityTimeout = DEFAULT_SEQUENCE_INACTIVITY_TIMEOUT;
    private long destinationBufferQuota = DEFAULT_DESTINATION_BUFFER_QUOTA;
    private boolean orderedDelivery = false;
    private DeliveryAssurance deliveryAssurance = DeliveryAssurance.getDefault();
    private SecurityBinding securityBinding = SecurityBinding.getDefault();
    //
    private long messageRetransmissionInterval = DEFAULT_MESSAGE_RETRANSMISSION_INTERVAL;
    private BackoffAlgorithm retransmissionBackoffAlgorithm = BackoffAlgorithm.getDefault();
    private long maxMessageRetransmissionCount = DEFAULT_MAX_MESSAGE_RETRANSMISSION_COUNT;
    //
    private long maxRmSessionControlMessageResendAttempts = DEFAULT_MAX_RM_SESSION_CONTROL_MESSAGE_RESEND_ATTEMPTS;
    //
    private long ackTransmissionInterval = DEFAULT_ACKNOWLEDGEMENT_TRANSMISSION_INTERVAL;
    private long ackRequestTransmissionInterval = DEFAULT_ACK_REQUEST_TRANSMISSION_INTERVAL;
    private long closeSequenceOperationTimeout = DEFAULT_CLOSE_SEQUENCE_OPERATION_TIMEOUT;
    private boolean persistenceEnabled = false;
    private long sequenceMaintenancePeriod = DEFAULT_SEQUENCE_MANAGER_MAINTENANCE_PERIOD;
    private long maxConcurrentSessions = DEFAULT_MAX_CONCURRENT_SESSIONS;
    //
    private boolean offerElementGenerationDisabled = DEFAULT_OFFER_ELEMENT_GENERATION_DISABLED;
    
    private boolean rejectOutOfOrderMessagesEnabled = DEFAULT_REJECT_OUT_OF_ORDER_MESSAGES;

    public ReliableMessagingFeatureBuilder(RmProtocolVersion version) {
        this.protocolVersion = version;
    }

    public ReliableMessagingFeature build() {
        return new ReliableMessagingFeature(
                this.enabled,
                this.protocolVersion,
                this.inactivityTimeout,
                this.destinationBufferQuota,
                this.orderedDelivery,
                this.deliveryAssurance,
                this.securityBinding,
                this.messageRetransmissionInterval,
                this.retransmissionBackoffAlgorithm,
                this.maxMessageRetransmissionCount,
                this.maxRmSessionControlMessageResendAttempts,
                this.ackTransmissionInterval,
                this.ackRequestTransmissionInterval,
                this.closeSequenceOperationTimeout,
                this.persistenceEnabled,
                this.sequenceMaintenancePeriod,
                this.maxConcurrentSessions,
                this.offerElementGenerationDisabled,
                this.rejectOutOfOrderMessagesEnabled);
    }

    /**
     * @see ReliableMessagingFeature#getAcknowledgementTransmissionInterval()
     */
    public ReliableMessagingFeatureBuilder acknowledgementTransmissionInterval(long value) {
        this.ackTransmissionInterval = value;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getAckRequestTransmissionInterval()
     */
    public ReliableMessagingFeatureBuilder ackRequestTransmissionInterval(long value) {
        this.ackRequestTransmissionInterval = value;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getMessageRetransmissionInterval() 
     */
    public ReliableMessagingFeatureBuilder messageRetransmissionInterval(long value) {
        this.messageRetransmissionInterval = value;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getRetransmissionBackoffAlgorithm()
     */
    public ReliableMessagingFeatureBuilder retransmissionBackoffAlgorithm(BackoffAlgorithm value) {
        this.retransmissionBackoffAlgorithm = value;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getMaxMessageRetransmissionCount() 
     */
    public ReliableMessagingFeatureBuilder maxMessageRetransmissionCount(long value) {
        this.maxMessageRetransmissionCount = value;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getMaxInitRmSessionAttempts()
     */
    public ReliableMessagingFeatureBuilder maxRmSessionControlMessageResendAttempts(long value) {
        this.maxRmSessionControlMessageResendAttempts = value;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getDestinationBufferQuota()
     */
    public ReliableMessagingFeatureBuilder destinationBufferQuota(long value) {
        this.destinationBufferQuota = value;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getCloseSequenceOperationTimeout()
     */
    public ReliableMessagingFeatureBuilder closeSequenceOperationTimeout(long value) {
        this.closeSequenceOperationTimeout = value;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getDeliveryAssurance()
     */
    public ReliableMessagingFeatureBuilder deliveryAssurance(DeliveryAssurance value) {
        this.deliveryAssurance = value;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getSequenceInactivityTimeout()
     */
    public ReliableMessagingFeatureBuilder sequenceInactivityTimeout(long value) {
        this.inactivityTimeout = value;
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
     * @see ReliableMessagingFeature#getVersion()
     */
    public RmProtocolVersion getProtocolVersion() {
        return this.protocolVersion;
    }

    /**
     * @see ReliableMessagingFeature#getSecurityBinding()
     */
    public ReliableMessagingFeatureBuilder securityBinding(SecurityBinding value) {
        this.securityBinding = value;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#isPersistenceEnabled() 
     */
    public ReliableMessagingFeatureBuilder enablePersistence() {
        this.persistenceEnabled = true;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#isPersistenceEnabled()
     */
    public ReliableMessagingFeatureBuilder disablePersistence() {
        this.persistenceEnabled = false;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getSequenceManagerMaintenancePeriod() 
     */
    public ReliableMessagingFeatureBuilder sequenceMaintenancePeriod(long value) {
        this.sequenceMaintenancePeriod = value;
        return this;
    }

    /**
     * @see ReliableMessagingFeature#getMaxConcurrentSessions()
     */
    public ReliableMessagingFeatureBuilder maxConcurrentSessions(long value) {
        this.maxConcurrentSessions = value;

        return this;
    }

    /**
     * @see ReliableMessagingFeature#isOfferElementGenerationDisabled()
     */
    public ReliableMessagingFeatureBuilder disableOfferElementGeneration() {
        this.offerElementGenerationDisabled = true;

        return this;
    }

    /**
     * @see ReliableMessagingFeature#isRejectOutOfOrderMessagesEnabled()
     */
    public ReliableMessagingFeatureBuilder rejectOutOfOrderMessages() {
        this.rejectOutOfOrderMessagesEnabled = true;
        return this;
    }
}
