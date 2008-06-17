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
package com.sun.xml.ws.rm.runtime;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.assembler.WsitServerTubeAssemblyContext;
import com.sun.xml.ws.rm.RmRuntimeException;
import com.sun.xml.ws.rm.RmSoapFaultException;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.policy.ConfigurationManager;
import com.sun.xml.ws.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rm.runtime.sequence.SequenceManagerFactory;
import com.sun.xml.ws.rm.runtime.sequence.UnknownSequenceException;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public abstract class AbstractRmServerTube extends AbstractFilterTubeImpl {

    private static final RmLogger LOGGER = RmLogger.getLogger(AbstractRmServerTube.class);
    //
    protected final Configuration configuration;
    protected final SequenceManager sequenceManager;
    //
    private PacketAdapter requestAdapter;

    protected AbstractRmServerTube(AbstractRmServerTube original, TubeCloner cloner) {
        super(original, cloner);

        this.configuration = original.configuration;
        this.sequenceManager = original.sequenceManager;

        this.requestAdapter = null;
    }

    public AbstractRmServerTube(WsitServerTubeAssemblyContext context) {
        super(context.getTubelineHead());

        this.configuration = ConfigurationManager.createServiceConfigurationManager(context.getWsdlPort(), context.getEndpoint().getBinding()).getConfigurationAlternatives()[0];

        // TODO don't take the first config alternative automatically...
        if (configuration.getAddressingVersion() != AddressingVersion.W3C) {
            throw new RmRuntimeException(LocalizationMessages.WSRM_1120_UNSUPPORTED_WSA_VERSION(configuration.getAddressingVersion().toString()));
        }

        this.sequenceManager = SequenceManagerFactory.getInstance().getSequenceManager();
        this.requestAdapter = null;
    }

    @Override
    public NextAction processRequest(Packet requestPacket) {
        LOGGER.entering();
        requestAdapter = PacketAdapter.getInstance(configuration, requestPacket);
        try {
            if (requestAdapter.isProtocolMessage()) {
                if (requestAdapter.isProtocolRequest()) {
                    PacketAdapter protocolResponseAdapter = processProtocolRequest(requestAdapter);
                    return doReturnWith(protocolResponseAdapter.getPacket());
                } else {
                    return doThrow(new RmRuntimeException(LocalizationMessages.WSRM_1128_INVALID_WSA_ACTION_IN_PROTOCOL_REQUEST(requestAdapter.getWsaAction())));
                }
            } else {
                if (configuration.isOrderedDelivery() && !isMessageInOrder(requestAdapter)) {
                    /*
                    TODO: ordered case processing (suspend until it's this message's turn)
                     */
                }

                processRmHeaders(requestAdapter, true);
                return super.processRequest(requestAdapter.getPacket());
            }
        } catch (RmSoapFaultException ex) {
            return doReturnWith(ex.getSoapFaultResponse());
        } catch (RmRuntimeException ex) {
            LOGGER.logSevereException(ex);
            return doThrow(ex);
        } finally {
            requestAdapter.getPacket();
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processResponse(Packet responsePacket) {
        LOGGER.entering();
        try {
            // TODO response processing
            return super.processResponse(responsePacket);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public void preDestroy() {
        LOGGER.entering();
        try {
            super.preDestroy();
        } finally {
            LOGGER.exiting();
        }
    }

    private boolean isMessageInOrder(PacketAdapter requestAdapter) {
        Sequence inboundSequence = sequenceManager.getSequence(requestAdapter.getSequenceId());
        return inboundSequence.getLastMessageId() == requestAdapter.getMessageNumber();
    }

    /**
     * TODO javadoc
     */
    private PacketAdapter processProtocolRequest(PacketAdapter requestAdapter) throws RmSoapFaultException {
        if (configuration.getRmVersion().createSequenceAction.equals(requestAdapter.getWsaAction())) {
            return handleCreateSequenceAction(requestAdapter);
        } else if (configuration.getRmVersion().terminateSequenceAction.equals(requestAdapter.getWsaAction())) {
            return handleTerminateSequenceAction(requestAdapter);
        } else if (configuration.getRmVersion().ackRequestedAction.equals(requestAdapter.getWsaAction())) {
            return handleAckRequestedAction(requestAdapter);
        } else if (configuration.getRmVersion().sequenceAcknowledgementAction.equals(requestAdapter.getWsaAction())) {
            return handleSequenceAcknowledgementAction(requestAdapter);
        } else {
            return processOtherProtocolRequest(requestAdapter);
        }
    }

    protected PacketAdapter processOtherProtocolRequest(PacketAdapter requestAdapter) {
        throw new UnsupportedOperationException(LocalizationMessages.WSRM_1134_UNSUPPORTED_PROTOCOL_MESSAGE(requestAdapter.getWsaAction()));
    }

    /**
     * TODO javadoc
     */
    protected abstract PacketAdapter handleCreateSequenceAction(PacketAdapter requestAdapter) throws CreateSequenceRefusedException;

    /**
     * TODO javadoc
     */
    protected abstract PacketAdapter handleTerminateSequenceAction(PacketAdapter requestAdapter);

    /**
     * TODO javadoc
     */
    protected PacketAdapter handleAckRequestedAction(PacketAdapter requestAdapter) {

        Sequence inboundSequence;
        try {
            inboundSequence = sequenceManager.getSequence(requestAdapter.getAckRequestedHeaderSequenceId());
        } catch (UnknownSequenceException e) {
            // TODO process exception
            // throw LOGGER.logSevereException(new InvalidSequenceException(LocalizationMessages.WSRM_3022_UNKNOWN_SEQUENCE_ID_IN_MESSAGE(id), id));
            throw e;
        }

        inboundSequence.updateLastActivityTime();

        return requestAdapter.createAckResponse(inboundSequence, configuration.getRmVersion().sequenceAcknowledgementAction);
    }

    /**
     * TODO javadoc
     */
    protected PacketAdapter handleSequenceAcknowledgementAction(PacketAdapter requestAdapter) {
        processRmHeaders(requestAdapter, false);

        // FIXME maybe we should send acknowledgements back if any?
        return requestAdapter.closeTransportAndReturnNull();
    }

    /**
     * TODO javadoc
     */
    private void processRmHeaders(PacketAdapter requestAdapter, boolean expectSequenceHeader) {
        if (expectSequenceHeader) {
            if (requestAdapter.getSequenceId() == null) {
                throw new RmRuntimeException(LocalizationMessages.WSRM_1118_MANDATORY_HEADER_NOT_PRESENT("wsrm:Sequence"));
            }

            Sequence inboundSequence = sequenceManager.getSequence(requestAdapter.getSequenceId());
            inboundSequence.acknowledgeMessageId(requestAdapter.getMessageNumber());
        }

        String ackRequestedSequenceId = requestAdapter.getAckRequestedHeaderSequenceId();
        if (ackRequestedSequenceId != null) {
            sequenceManager.getSequence(ackRequestedSequenceId).setAckRequestedFlag();
        }

        requestAdapter.processAcknowledgements(sequenceManager, getOutboundSequenceId4Request(requestAdapter));
    }

    private String getOutboundSequenceId4Request(PacketAdapter requestAdapter) {
        String sequenceId = requestAdapter.getSequenceId();
        return (sequenceId != null) ? sequenceManager.getBoundSequence(requestAdapter.getSequenceId()).getId() : null;
    }
}
