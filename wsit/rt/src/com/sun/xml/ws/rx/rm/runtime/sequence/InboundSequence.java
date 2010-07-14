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
package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.faults.AbstractSoapFaultException.Code;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueueBuilder;
import com.sun.xml.ws.rx.util.TimeSynchronizer;
import java.util.List;

/**
 * Inbound sequence implementation
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class InboundSequence extends AbstractSequence {
    private static final Logger LOGGER = Logger.getLogger(InboundSequence.class);
    public static final long INITIAL_LAST_MESSAGE_ID = Sequence.UNSPECIFIED_MESSAGE_ID;

    public InboundSequence(SequenceData data, DeliveryQueueBuilder deliveryQueueBuilder, TimeSynchronizer timeSynchronizer) {
        super(data, deliveryQueueBuilder, timeSynchronizer);
    }

    public void registerMessage(ApplicationMessage message, boolean storeMessageFlag) throws DuplicateMessageRegistrationException, IllegalStateException {
        this.getState().verifyAcceptingMessageRegistration(getId(), Code.Receiver);

        if (!this.getId().equals(message.getSequenceId())) {
            throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSRM_1149_DIFFERENT_MSG_SEQUENCE_ID(
                    message.getSequenceId(),
                    this.getId())));
        }

        data.registerReceivedUnackedMessageNumber(message.getMessageNumber());
        if (storeMessageFlag) {
            data.attachMessageToUnackedMessageNumber(message);
        }
    }

    public void acknowledgeMessageNumbers(List<AckRange> ranges) {
        throw new UnsupportedOperationException(String.format("This operation is not supported on %s class", this.getClass().getName()));
    }

    public void acknowledgeMessageNumber(long messageId) throws IllegalStateException {
        this.getState().verifyAcceptingAcknowledgement(getId(), Code.Receiver);

        data.markAsAcknowledged(messageId);

        this.getDeliveryQueue().onSequenceAcknowledgement();
    }
}
