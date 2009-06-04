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
package com.sun.xml.ws.rx.rm.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.rx.RxConfiguration;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.UnknownSequenceException;
import com.sun.xml.ws.rx.util.Communicator;
import com.sun.xml.ws.rx.util.ScheduledTaskManager;
import com.sun.xml.ws.rx.util.SuspendedFiberStorage;
import java.util.concurrent.ScheduledFuture;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public final class RuntimeContext {

    public static Builder getBuilder(@NotNull RxConfiguration configuration, @NotNull SequenceManager sequenceManager, @NotNull Communicator communicator) {
        return new Builder(configuration, sequenceManager, communicator);
    }

    public static final class Builder {

        private final
        @NotNull
        RxConfiguration configuration;
        private final
        @NotNull
        SequenceManager sequenceManager;
        private final
        @NotNull
        Communicator communicator;
        private final
        @NotNull
        SourceMessageHandler sourceMessageHandler;
        private final
        @NotNull
        DestinationMessageHandler destinationMessageHandler;
        private final
        @NotNull
        RedeliveryTask redeliveryTask;

        public Builder(@NotNull RxConfiguration configuration, @NotNull SequenceManager sequenceManager, @NotNull Communicator communicator) {
            assert configuration != null;
            assert sequenceManager != null;
            assert communicator != null;

            this.configuration = configuration;
            this.sequenceManager = sequenceManager;
            this.communicator = communicator;
            this.sourceMessageHandler = new SourceMessageHandler(sequenceManager);
            this.destinationMessageHandler = new DestinationMessageHandler(sequenceManager);
            this.redeliveryTask = new RedeliveryTask(sourceMessageHandler);
        }

        public RuntimeContext build() {
            return new RuntimeContext(
                    configuration,
                    sequenceManager,
                    communicator,
                    new SuspendedFiberStorage(),
                    WsrmProtocolHandler.getInstance(configuration, communicator, sequenceManager),
                    new ScheduledTaskManager(),
                    sourceMessageHandler,
                    destinationMessageHandler,
                    redeliveryTask);
        }
    }
    public final RxConfiguration configuration;
    public final AddressingVersion addressingVersion;
    public final SOAPVersion soapVersion;
    public final RmVersion rmVersion;
    public final SequenceManager sequenceManager;
    public final Communicator communicator;
    public final SuspendedFiberStorage suspendedFiberStorage;
    public final WsrmProtocolHandler protocolHandler;
    public final ScheduledTaskManager scheduledTaskManager;
    final RedeliveryTask redeliveryTask;
    final SourceMessageHandler sourceMessageHandler;
    final DestinationMessageHandler destinationMessageHandler;

    private RuntimeContext(
            RxConfiguration configuration,
            SequenceManager sequenceManager,
            Communicator communicator,
            SuspendedFiberStorage suspendedFiberStorage,
            WsrmProtocolHandler protocolHandler,
            ScheduledTaskManager scheduledTaskManager,
            SourceMessageHandler srcMsgHandler,
            DestinationMessageHandler dstMsgHandler,
            RedeliveryTask redeliveryTask) {

        this.configuration = configuration;
        this.sequenceManager = sequenceManager;
        this.communicator = communicator;
        this.suspendedFiberStorage = suspendedFiberStorage;
        this.protocolHandler = protocolHandler;
        this.scheduledTaskManager = scheduledTaskManager;
        this.sourceMessageHandler = srcMsgHandler;
        this.destinationMessageHandler = dstMsgHandler;

        this.addressingVersion = configuration.getAddressingVersion();
        this.soapVersion = configuration.getSoapVersion();
        this.rmVersion = configuration.getRmVersion();

        this.redeliveryTask = redeliveryTask;
    }

    public ScheduledFuture<?> startTask(Runnable task) {
        return scheduledTaskManager.startTask(task);
    }

    public ScheduledFuture<?> startTask(Runnable task, long delay, long period) {
        return scheduledTaskManager.startTask(task, delay, period);
    }

    public ScheduledFuture<?> startRedeliveryTask() {
        return scheduledTaskManager.startTask(
                redeliveryTask,
                configuration.getMessageRetransmissionInterval(),
                configuration.getMessageRetransmissionInterval());
    }

    public ScheduledFuture<?> startAckRequesterTask(Runnable ackRequesterTask) {
        return scheduledTaskManager.startTask(
                ackRequesterTask,
                configuration.getAcknowledgementRequestInterval(),
                configuration.getAcknowledgementRequestInterval());
    }

    public void stopAllTasks() {
        scheduledTaskManager.stopAll();
    }

    public Sequence getSequence(String sequenceId) throws UnknownSequenceException {
        return sequenceManager.getSequence(sequenceId);
    }

    public Sequence getBoundSequence(String sequenceId) throws UnknownSequenceException {
        return sequenceManager.getBoundSequence(sequenceId);
    }

    public String getBoundSequenceId(String sequenceId) throws UnknownSequenceException {
        Sequence boundSequence = sequenceManager.getBoundSequence(sequenceId);
        return boundSequence != null ? boundSequence.getId() : null;
    }
}
