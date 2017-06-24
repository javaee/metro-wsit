/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.rx.rm.runtime;

import java.util.logging.Level;

import com.oracle.webservices.oracle_internal_api.rm.InboundAccepted;
import com.oracle.webservices.oracle_internal_api.rm.InboundAcceptedAcceptFailed;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.runtime.transaction.TransactionPropertySet;

public class InboundAcceptedImpl extends InboundAccepted {
    private static final Logger LOGGER = Logger.getLogger(InboundAcceptedImpl.class);
    private final JaxwsApplicationMessage request;
    private final RuntimeContext rc;
    private Boolean accepted;

    public InboundAcceptedImpl(JaxwsApplicationMessage request, RuntimeContext rc) {
        this.request = request;
        this.rc = rc;
    }

    @Override
    public void setAccepted(Boolean accept) throws InboundAcceptedAcceptFailed {

        if (accept == null) {
            throw new IllegalArgumentException("Found supplied accept Boolean null.");
        }

        accepted = accept;

        TransactionPropertySet ps =
                request.getPacket().getSatellite(TransactionPropertySet.class);
        boolean txOwned = (ps != null && ps.isTransactionOwned());

        if (accept) {
            rc.destinationMessageHandler.acknowledgeApplicationLayerDelivery(request);

            if (txOwned) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Transaction status before commit: " + rc.transactionHandler.getStatusAsString());
                }

                try {
                    rc.transactionHandler.commit();
                } catch (Throwable t) {
                    accepted = null;
                    throw new InboundAcceptedAcceptFailed("Not able to commit the TX.", t);
                }
            } else {
                //Do nothing as we don't own the TX
            }
        } else {//accept == false
            if (txOwned) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Transaction status before rollback: " + rc.transactionHandler.getStatusAsString());
                }
                rc.transactionHandler.rollback();
            } else {
                //Don't roll back as we don't own the TX but if active then mark for roll back
                if (rc.transactionHandler.userTransactionAvailable() &&
                        rc.transactionHandler.isActive()) {
                    rc.transactionHandler.setRollbackOnly();
                }
            }
        }
    }

    @Override
    public Boolean getAccepted() {
        return accepted;
    }

    @Override
    public String getRMSequenceId() {
        String seqID = (String)request.getPacket().invocationProperties.get(ServerTube.SEQUENCE_PROPERTY);
        return seqID;
    }

    @Override
    public long getRMMessageNumber() {
        long msgNumber = (Long)request.getPacket().invocationProperties.get(ServerTube.MESSAGE_NUMBER_PROPERTY);
        return msgNumber;
    }
}
