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
    public static final long DEFAULT_INACTIVITY_TIMEOUT = 600000;
    public static final long DEFAULT_DESTINATION_BUFFER_QUOTA = 32;
    public static final long DEFAULT_BASE_RETRANSMISSION_INTERVAL = 2000;
    public static final long DEFAULT_ACK_REQUESTED_INTERVAL = 200;
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
                DEFAULT_DESTINATION_BUFFER_QUOTA, // this.bufferQuota
                false, // this.orderedDelivery
                DeliveryAssurance.getDefault(), // this.deliveryAssurance
                SecurityBinding.getDefault(), // this.securityBinding
                DEFAULT_BASE_RETRANSMISSION_INTERVAL, // this.baseRetransmissionInterval
                BackoffAlgorithm.getDefault(), // this.retransmissionBackoffAlgorithm
                DEFAULT_ACK_REQUESTED_INTERVAL, // this.ackRequestInterval
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
            long baseRetransmissionInterval,
            BackoffAlgorithm retransmissionBackoffAlgorithm,
            long ackRequestInterval,
            long closeSequenceOperationTimeout) {

        super.enabled = enabled;
        this.version = version;
        this.inactivityTimeout = inactivityTimeout;
        this.bufferQuota = bufferQuota;
        this.orderedDelivery = orderedDelivery;
        this.deliveryAssurance = deliveryAssurance;
        this.securityBinding = securityBinding;
        this.baseRetransmissionInterval = baseRetransmissionInterval;
        this.retransmissionBackoffAlgorithm = retransmissionBackoffAlgorithm;
        this.ackRequestInterval = ackRequestInterval;
        this.closeSequenceOperationTimeout = closeSequenceOperationTimeout;
    }
    
    @Override
    public String getID() {
        return ID;
    }

    /**
     * Specifies which WS-RM version SOAP messages and SOAP message headers should
     * be used for communication between RM source and RM destination
     *
     * @return version currently configured for the feature. If not set explicitly, 
     *         the default value is specified by a call to {@link RmVersion#getDefault()}.
     */
    public RmVersion getVersion() {
        return version;
    }

    /**
     * Specifies a period of inactivity for a Sequence in ms.
     *
     * @return currently configured sequence inactivity timeout. If not set explicitly, 
     *         the default value is specified by {@link ReliableMessagingFeature#DEFAULT_INACTIVITY_TIMEOUT}
     *         constant.
     */
    public long getInactivityTimeout() {
        return inactivityTimeout;
    }

    /**
     * Specifies whether each created RM sequence must be bound to a specific
     * underlying security token or secured transport.
     * <dl>
     *   <dt>STR</dt>
     *   <dd>
     *     an RM Sequence MUST be bound to an explicit token that is referenced
     *     from a wsse:SecurityTokenReference in the CreateSequence message.
     *   </dd>
     *   <dt>TRANSPORT</dt>
     *   <dd>
     *     an RM Sequence MUST be bound to the session(s) of the underlying transport-level
     *     security protocol (e.g. SSL/TLS) used to carry the {@code CreateSequence}
     *     and {@code CreateSequenceResponse} messages.
     *     <p />
     *     The assertion specifying this requirement MUST be used in conjunction
     *     with the sp:TransportBinding assertion that requires the use of some
     *     transport-level security mechanism (e.g. sp:HttpsToken)."
     *   </dd>
     * </dl>
     *
     * @return configured security binding requirement. If not set explicitly, the 
     *         default value is specified by a call to {@link SecurityBinding#getDefault()}.
     */
    public SecurityBinding getSecurityBinding() {
        return securityBinding;
    }

    /**
     * Specifies the message delivery quality of service between the RM and
     * application layer. It expresses the delivery assurance in effect between
     * the RM Destination and its corresponding application destination, and it
     * also indicates requirements on any RM Source that transmits messages to
     * this RM destination. Conversely when used by an RM Source it expresses
     * the delivery assurance in effect between the RM Source and its corresponding
     * application source, as well as indicating requirements on any RM Destination
     * that receives messages from this RM Source. In either case the delivery
     * assurance does not affect the messages transmitted on the wire.
     *
     * @return currently configured delivery assurance mode. If not set explicitly, 
     *         the default value is specified by a call to {@link DeliveryAssurance#getDefault()}.
     */
    public DeliveryAssurance getDeliveryAssurance() {
        return deliveryAssurance;
    }

    /**
     * Specifies a requirement that all request messages must be processed by an
     * RM destination in the same order as they were sent by RM source.
     * This order is defined by an RM sequence message number assigned to each
     * request message.
     *
     * @return {@code true} if the ordered delivery si required, {@code false} otherwise.
     *         If not set explicitly, the default value is {@code false}.
     */
    public boolean isOrderedDelivery() {
        return orderedDelivery;
    }

    /**
     * This attribute may be used together with ordered delivery requirement.
     * It specifies the maximum number of out-of-order unprocessed request messages
     * that may be stored in the unprocessed request message buffer within the RM
     * destination before the RM destination starts rejecting new request messages.
     *
     * @return currently configured flow control buffer on the destination. If not 
     *         set explicitly, the default value is specified by {@link ReliableMessagingFeature#DEFAULT_DESTINATION_BUFFER_QUOTA}
     *         constant.
     */
    public long getDestinationBufferQuota() {
        return bufferQuota;
    }

    /**
     * Specifies how long the RM Source will wait after transmitting a message
     * before retransmitting the message if no acknowledgement arrives.
     *
     * @return currently configured base retransmission interval. If not set explicitly, 
     *         the default value is specified by {@link ReliableMessagingFeature#DEFAULT_BASE_RETRANSMISSION_INTERVAL}
     *         constant.
     */
    public long getBaseRetransmissionInterval() {
        return baseRetransmissionInterval;
    }

    /**
     * Specifies that the retransmission interval will be adjusted using a specific
     * backoff algorithm.
     *
     * @return currently configured retransmission back-off algorithm that should be
     *         used. If not set explicitly, the default value is specified by a
     *         call to {@link BackoffAlgorithm#getDefault()}.
     */
    public BackoffAlgorithm getRetransmissionBackoffAlgorithm() {
        return retransmissionBackoffAlgorithm;
    }

    /**
     * Specifies interval between sending subsequent acknowledgement request messages 
     * by an RM Source in case of any unacknowledged messages on the sequence.
     * 
     * @return currently configured acknowledgement request interval. If not set explicitly, 
     *         the default value is specified by the {@link ReliableMessagingFeature#DEFAULT_ACK_REQUESTED_INTERVAL}
     *         constant.
     */
    public long getAcknowledgementRequestInterval() {
        return ackRequestInterval;
    }

    /**
     * Specifies the timeout for a {@code CloseSequenceRequest} message. If no response
     * is returned from RM destination before the timout expires, the sequence is
     * automatically closed by the RM source and all associated resources are released.
     *
     * @return currently configured close sequence operation timeout. If not set explicitly,
     *         the default value is specified by the {@link ReliableMessagingFeature#DEFAULT_CLOSE_SEQUENCE_OPERATION_TIMEOUT}
     *         constant.
     */
    public long getCloseSequenceOperationTimeout() {
        return closeSequenceOperationTimeout;
    }
}
