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
package com.sun.xml.ws.rx.rm.runtime.delivery;

import com.sun.istack.NotNull;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.delivery.Postman.Callback;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.AckRange;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
class InOrderDeliveryQueue implements DeliveryQueue {

    private static final class MessageIdComparator implements Comparator<ApplicationMessage> {

        public int compare(ApplicationMessage o1, ApplicationMessage o2) {
            return (o1.getMessageNumber() < o2.getMessageNumber()) ? -1 : (o1.getMessageNumber() > o2.getMessageNumber()) ? 1 : 0;
        }
    }
    private static final Logger LOGGER = Logger.getLogger(InOrderDeliveryQueue.class);
    private static final MessageIdComparator MSG_ID_COMPARATOR = new MessageIdComparator();
    //
    private final @NotNull Postman postman;
    private final @NotNull Postman.Callback deliveryCallback;
    private final @NotNull Sequence sequence;
    //
    private final long maxMessageBufferSize;
    private final @NotNull BlockingQueue<ApplicationMessage> postponedMessageQueue;
    //
    private final AtomicBoolean isClosed;

    public InOrderDeliveryQueue(@NotNull Postman postman, @NotNull Callback deliveryCallback, @NotNull Sequence sequence, long maxMessageBufferSize) {
        assert postman != null;
        assert deliveryCallback != null;
        assert sequence != null;
        assert maxMessageBufferSize >= DeliveryQueue.UNLIMITED_BUFFER_SIZE;

        this.postman = postman;
        this.deliveryCallback = deliveryCallback;
        this.sequence = sequence;

        this.maxMessageBufferSize = maxMessageBufferSize;
        this.postponedMessageQueue = new PriorityBlockingQueue<ApplicationMessage>(32, MSG_ID_COMPARATOR);

        this.isClosed = new AtomicBoolean(false);
    }

    public void put(ApplicationMessage message) {
//        LOGGER.info(Thread.currentThread().getName() + " put: mesageNumber = " + message.getMessageNumber());
        assert message.getSequenceId().equals(sequence.getId());

        try {
            postponedMessageQueue.put(message);
        } catch (InterruptedException ex) {
            throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRM_1147_ADDING_MSG_TO_QUEUE_INTERRUPTED(), ex));
        }

        tryDelivery();
    }
    public void onSequenceAcknowledgement() {
//        LOGGER.info(Thread.currentThread().getName() + " onSequenceAcknowledgement");
        if (!isClosed.get()) {
            tryDelivery();
        }
    }
    
    private void tryDelivery() {
//        LOGGER.info(Thread.currentThread().getName() + " postponedMessageQueue.size() = " + postponedMessageQueue.size());
        if (isClosed.get()) {
            throw new RxRuntimeException(LocalizationMessages.WSRM_1160_DELIVERY_QUEUE_CLOSED());
        }

        if (!postponedMessageQueue.isEmpty()) {
            for (;;) {
                ApplicationMessage deliverableMessage = null;

                synchronized (postponedMessageQueue) {
                    ApplicationMessage queueHead = postponedMessageQueue.peek();

//                    LOGGER.info(Thread.currentThread().getName() + " postponedMessageQueue head message number = " + ((queueHead != null) ? queueHead.getMessageNumber() + " is deliverable: " + isDeliverable(queueHead) : "n/a"));

                    if (queueHead != null && isDeliverable(queueHead)) {
                        deliverableMessage = postponedMessageQueue.poll();
                        assert isDeliverable(deliverableMessage);
                    }
                }

                if (deliverableMessage != null) {
//                    LOGGER.info(Thread.currentThread().getName() + " delivering message number = " + deliverableMessage.getMessageNumber());
                    postman.deliver(deliverableMessage, deliveryCallback);
                } else {
                    break;
                }
            }
        }
    }

    public long getRemainingMessageBufferSize() {
        return (maxMessageBufferSize == DeliveryQueue.UNLIMITED_BUFFER_SIZE) ? maxMessageBufferSize : maxMessageBufferSize - postponedMessageQueue.size();
    }

    public void close() {
//        LOGGER.info(Thread.currentThread().getName() + " close");
        isClosed.set(true);
    }

    private boolean isDeliverable(ApplicationMessage message) {
        List<Sequence.AckRange> ackedIds = sequence.getAcknowledgedMessageNumbers();
        if (ackedIds.isEmpty()) {
            return message.getMessageNumber() == 1L; // this is a first message
        } else {
            AckRange firstRange = ackedIds.get(0);
            return (firstRange.lower != 1L) ? message.getMessageNumber() == 1L : message.getMessageNumber() == firstRange.upper + 1;
        }
    }
}
