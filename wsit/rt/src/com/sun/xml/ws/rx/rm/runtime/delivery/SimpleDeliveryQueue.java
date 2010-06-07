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

import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
final class SimpleDeliveryQueue implements DeliveryQueue {

    private final Postman postman;
    private final Postman.Callback deliveryCallback;
    private final AtomicBoolean isClosed;

    SimpleDeliveryQueue(Postman postman, Postman.Callback deliveryCallback) {
        this.postman = postman;
        this.deliveryCallback = deliveryCallback;
        this.isClosed = new AtomicBoolean(false);
    }

    public void put(ApplicationMessage message) throws RxRuntimeException {
        if (isClosed.get()) {
            throw new RxRuntimeException(LocalizationMessages.WSRM_1160_DELIVERY_QUEUE_CLOSED());
        }

        postman.deliver(message, deliveryCallback);
    }

    public long getRemainingMessageBufferSize() {
        return DeliveryQueue.UNLIMITED_BUFFER_SIZE;
    }

    public void onSequenceAcknowledgement() {
        // do nothing
    }

    public void close() {
        isClosed.set(true);
    }
}
