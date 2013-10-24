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

package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeature;
import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeatureBuilder;
import com.sun.xml.ws.rx.rm.api.RmProtocolVersion;
import com.sun.xml.ws.rx.rm.runtime.RmRuntimeVersion;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.RmConfiguration;
import com.sun.xml.ws.rx.rm.runtime.RuntimeContext;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueueBuilder;
import com.sun.xml.ws.rx.rm.runtime.delivery.Postman;
import com.sun.xml.ws.rx.rm.runtime.delivery.PostmanPool;
import java.util.LinkedList;
import java.util.List;
import org.glassfish.gmbal.ManagedObjectManager;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
final class SequenceTestUtils  {
    private SequenceTestUtils() {}

    static RmConfiguration getConfiguration() {
        final ReliableMessagingFeature rmf = new ReliableMessagingFeatureBuilder(RmProtocolVersion.WSRM200702).build();

        return new RmConfiguration() {

            public boolean isReliableMessagingEnabled() {
                return true;
            }

            public boolean isMakeConnectionSupportEnabled() {
                return false;
            }

            public SOAPVersion getSoapVersion() {
                return SOAPVersion.SOAP_12;
            }

            public AddressingVersion getAddressingVersion() {
                return AddressingVersion.W3C;
            }

            public boolean requestResponseOperationsDetected() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public ReliableMessagingFeature getRmFeature() {
                return rmf;
            }
            public ManagedObjectManager getManagedObjectManager() {
                return null;
            }

            public RmRuntimeVersion getRuntimeVersion() {
                return RmRuntimeVersion.WSRM200702;
            }

            @Override
            public com.oracle.webservices.oracle_internal_api.rm.ReliableMessagingFeature getInternalRmFeature() {
                // TODO Auto-generated method stub
                return null;
            }

       };
    }

    static DeliveryQueueBuilder getDeliveryQueueBuilder() {


        return DeliveryQueueBuilder.getBuilder(getConfiguration(), PostmanPool.INSTANCE.getPostman(), new Postman.Callback() {

            public void deliver(ApplicationMessage message) {
            }

            @Override
            public RuntimeContext getRuntimeContext() {
                // TODO Auto-generated method stub
                return null;
            }
        });
    }

    static List<Sequence.AckRange> createAckRanges(long... msgNumbers) {
        List<Sequence.AckRange> ackList = new LinkedList<Sequence.AckRange>();

        if (msgNumbers.length > 0) {
            long lower = msgNumbers[0];
            long upper = msgNumbers[0] - 1;
            for (long number : msgNumbers) {
                if (number == upper + 1) {
                    upper = number;
                } else {
                    ackList.add(new Sequence.AckRange(lower, upper));
                    lower = upper = number;
                }
            }
            ackList.add(new Sequence.AckRange(lower, upper));
        }
        return ackList;
    }

}
