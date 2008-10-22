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
import com.sun.xml.ws.rm.RmVersion;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public interface Configuration {
    public static final long UNSPECIFIED = -1;
    
    public static final long DEFAULT_INACTIVITY_TIMEOUT = 600000;
    public static final long DEFAULT_CLOSE_SEQUENCE_OPERATION_TIMEOUT = 3000;
//    public static final SecurityBinding DEFAULT_SECURITY_BINDING = SecurityBinding.NONE;
//    public static final DeliveryAssurance DEFAULT_DELIVERY_ASSURANCE = DeliveryAssurance.EXACTLY_ONCE;

    public static enum SecurityBinding {
        STR,
        TRANSPORT,
        NONE
    }

    public static enum DeliveryAssurance {
        EXACTLY_ONCE,
        AT_LEAST_ONCE,
        AT_MOST_ONCE;

        public static DeliveryAssurance getDefault() {
            return DeliveryAssurance.EXACTLY_ONCE;
        }
    }

    /**
     * Provides information about the RM protocol version used for the sequence.
     * 
     * @return the RM protocol version that will be used for the sequence.
     */
    public RmVersion getRmVersion();

    /**
     * Provides information about the SOAP protocol version used on the RM-enabled endpoint.
     * 
     * @return the SOAP protocol version used on the RM-enabled endpoint
     */
    public SOAPVersion getSoapVersion();

    /**
     * Provides information about the WS-Addressing protocol version used on the RM-enabled endpoint.
     * 
     * @return the WS-Addressing protocol version used on the RM-enabled endpoint
     */
    public AddressingVersion getAddressingVersion();
    
    /**
     * Provides information if the port, which this configuration belongs to, has any request/response operations 
     * @return {@code true} in case the port has any request/response operations; {@code false} otherwise
     */
    public boolean requestResponseOperationsDetected();
    
    /**
     * Specifies a period of inactivity for the sequence (in miliseconds).
     * 
     * @return a period of inactivity for the sequence in milliseconds.
     */
    public long getInactivityTimeout();

    /**
     * Specifies the duration after which the RM Destination will transmit a sequence acknowledgement.
     * 
     * @return the duration after which the RM Destination will transmit a sequence acknowledgement.
     */
    public long getSequenceAcknowledgementInterval();

    /**
     * Specifies if the RM session must be bound to an explicit token referenced from a 
     * {@code wsse:SecurityTokenReference} in the {@code CreateSequence} message or to 
     * the session of the underlying transport-level protocol used to carry {@code CreateSequence} 
     * and {@code CreateSequenceResponse} message.
     * 
     * @return security binding requirement status.
     */
    public SecurityBinding getSecurityBinding();

    /**
     * TODO delivery assurance javadoc
     * @return
     */
    public DeliveryAssurance getDeliveryAssurance();

    /**
     * TODO oredered delivery javadoc
     * @return
     */
    public boolean isOrderedDelivery();

    /**
     * TODO flow control settings javadoc
     * @return
     */
    public long getDestinationBufferQuota();
    
    /**
     * Specifies how long (in miliseconds) the RM source is expected to wait before 
     * retransmitting the message.
     * 
     * @return a period of time (in miliseconds) the RM source is expected to wait 
     * before retransmitting the message.
     */
    public long getMessageRetransmissionInterval();
        
    /**
     * Specifies whether Exponetial backoff retransmission interval adjustment 
     * algorithm should be used on the client side or not.
     * 
     * @return {@code true} if the Exponetial backoff retransmission interval adjustment 
     * algorithm should be used on the client side; {@code false} otherwise
     */
    public boolean useExponetialBackoffRetransmission();    
    
    /**
     * Specifies the duration after which the RM source will transmit an acknowledgement request.
     * 
     * @return the duration after which the RM source will transmit an acknowledgement request.
     */
    public long getAcknowledgementRequestInterval();    
    
    /**
     * Specifies the timeout for the "Close sequence" operation that occurs when sequence
     * is being closed and terminated on the client side.
     * 
     * @return the timeout for the "Close sequence" operation.
     */
    public long getCloseSequenceOperationTimeout();
}
