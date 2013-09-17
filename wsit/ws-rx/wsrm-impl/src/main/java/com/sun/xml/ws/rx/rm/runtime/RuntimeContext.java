/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.xml.ws.rx.rm.runtime;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.UnknownSequenceException;
import com.sun.xml.ws.rx.util.Communicator;
import com.sun.xml.ws.commons.ScheduledTaskManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManagerFactory;
import com.sun.xml.ws.rx.rm.runtime.transaction.TransactionHandler;
import com.sun.xml.ws.rx.rm.runtime.transaction.TransactionHandlerImpl;
import com.sun.xml.ws.rx.util.SuspendedFiberStorage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public final class RuntimeContext {

    public static Builder builder(@NotNull RmConfiguration configuration, @NotNull Communicator communicator) {
        return new Builder(configuration, communicator);
    }

    public static final class Builder {

        private final 
        @NotNull
        RmConfiguration configuration;
        private final 
        @NotNull
        Communicator communicator;
        private 
        @Nullable
        SequenceManager sequenceManager;
        private 
        @Nullable
        SourceMessageHandler sourceMessageHandler;
        private 
        @Nullable
        DestinationMessageHandler destinationMessageHandler;
        private
        @Nullable
        TransactionHandler transactionHandler;

        public Builder(@NotNull RmConfiguration configuration, @NotNull Communicator communicator) {
            assert configuration != null;
            assert communicator != null;

            this.configuration = configuration;
            this.communicator = communicator;

            this.sourceMessageHandler = new SourceMessageHandler(null);
            this.destinationMessageHandler = new DestinationMessageHandler(null);
            this.transactionHandler = new TransactionHandlerImpl();
        }

        public Builder sequenceManager(SequenceManager sequenceManager) {
            this.sequenceManager = sequenceManager;

            this.sourceMessageHandler.setSequenceManager(sequenceManager);
            this.destinationMessageHandler.setSequenceManager(sequenceManager);

            return this;
        }

        public RuntimeContext build() {
            return new RuntimeContext(
                    configuration,
                    sequenceManager,
                    communicator,
                    new SuspendedFiberStorage(),
                    new ScheduledTaskManager("RM Runtime Context", communicator.getContainer()),
                    sourceMessageHandler,
                    destinationMessageHandler,
                    transactionHandler);
        }
    }
    public final RmConfiguration configuration;
    public final AddressingVersion addressingVersion;
    public final SOAPVersion soapVersion;
    public final RmRuntimeVersion rmVersion;
    private volatile SequenceManager sequenceManager;
    public final Communicator communicator;
    public final SuspendedFiberStorage suspendedFiberStorage;
    public final WsrmProtocolHandler protocolHandler;
    public final ScheduledTaskManager scheduledTaskManager;
    final SourceMessageHandler sourceMessageHandler;
    final DestinationMessageHandler destinationMessageHandler;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    public final TransactionHandler transactionHandler;

    @SuppressWarnings("LeakingThisInConstructor")
    private RuntimeContext(
            RmConfiguration configuration,
            SequenceManager sequenceManager,
            Communicator communicator,
            SuspendedFiberStorage suspendedFiberStorage,
            ScheduledTaskManager scheduledTaskManager,
            SourceMessageHandler srcMsgHandler,
            DestinationMessageHandler dstMsgHandler,
            TransactionHandler txHandler) {

        this.configuration = configuration;
        this.sequenceManager = sequenceManager;
        this.communicator = communicator;
        this.suspendedFiberStorage = suspendedFiberStorage;
        this.scheduledTaskManager = scheduledTaskManager;
        this.sourceMessageHandler = srcMsgHandler;
        this.destinationMessageHandler = dstMsgHandler;

        this.addressingVersion = configuration.getAddressingVersion();
        this.soapVersion = configuration.getSoapVersion();
        this.rmVersion = configuration.getRuntimeVersion();

        this.protocolHandler = WsrmProtocolHandler.getInstance(configuration, communicator, this);
        this.transactionHandler = txHandler;
    }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            scheduledTaskManager.shutdown();
            communicator.close();

            if (sequenceManager != null) {
                SequenceManagerFactory.INSTANCE.dispose(sequenceManager, configuration);
            }
        }
    }

    public String getBoundSequenceId(String sequenceId) throws UnknownSequenceException {
        assert sequenceManager != null;

        Sequence boundSequence = sequenceManager.getBoundSequence(sequenceId);
        return boundSequence != null ? boundSequence.getId() : null;
    }

    public SequenceManager sequenceManager() {
        assert sequenceManager != null;

        return this.sequenceManager;
    }

    public void setSequenceManager(@NotNull SequenceManager newValue) {
        assert newValue != null;

        this.sequenceManager = newValue;

        this.sourceMessageHandler.setSequenceManager(newValue);
        this.destinationMessageHandler.setSequenceManager(newValue);
    }
}
