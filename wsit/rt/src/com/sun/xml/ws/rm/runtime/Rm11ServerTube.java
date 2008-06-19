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

import com.sun.xml.ws.rm.faults.AbstractRmSoapFault;
import com.sun.xml.ws.rm.faults.CreateSequenceRefusedFault;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.assembler.WsitServerTubeAssemblyContext;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rm.v200702.CloseSequenceElement;
import com.sun.xml.ws.rm.v200702.CloseSequenceResponseElement;
import com.sun.xml.ws.rm.v200702.Identifier;

/**
 *
 * @author m_potociar
 */
public class Rm11ServerTube extends AbstractRmServerTube {

    private static final RmLogger LOGGER = RmLogger.getLogger(Rm11ServerTube.class);

    protected Rm11ServerTube(AbstractRmServerTube original, TubeCloner cloner) {
        super(original, cloner);

    // TODO initialize all instance variables
    }

    public Rm11ServerTube(WsitServerTubeAssemblyContext context) {
        super(context);
    // TODO initialize all instance variables        
    }

    @Override
    public Rm10ServerTube copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new Rm10ServerTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    protected PacketAdapter processVersionSpecificProtocolRequest(PacketAdapter requestAdapter) throws AbstractRmSoapFault {
        if (RmVersion.WSRM11.closeSequenceAction.equals(requestAdapter.getWsaAction())) {
            // FIXME: split RM11 and RM10 processing
            return handleCloseSequenceAction(requestAdapter);
        } else if (RmVersion.WSRM11.makeConnectionAction.equals(requestAdapter.getWsaAction())) {
            return handleMakeConnectionAction(requestAdapter);
        } else {
            return super.processVersionSpecificProtocolRequest(requestAdapter);
        }
    }

    @Override
    protected PacketAdapter handleCreateSequenceAction(PacketAdapter requestAdapter) throws CreateSequenceRefusedFault {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * TODO javadoc
     */
    protected PacketAdapter handleCloseSequenceAction(PacketAdapter requestAdapter) {
        CloseSequenceElement closeSeqElement = requestAdapter.unmarshallMessage();
        Sequence inboundSequence = sequenceManager.getSequence(closeSeqElement.getIdentifier().getValue());
        
        // TODO handle last message number
        // int lastMessageNumber = closeSeqElement.getLastMsgNumber();
        
        inboundSequence.close();
        
        CloseSequenceResponseElement closeSeqResponseElement = new CloseSequenceResponseElement();
        closeSeqResponseElement.setIdentifier(new Identifier(inboundSequence.getId()));
        
        PacketAdapter responseAdapter = requestAdapter.createServerResponse(closeSeqResponseElement, RmVersion.WSRM11.closeSequenceResponseAction);
        responseAdapter.appendSequenceAcknowledgementHeader(inboundSequence);
        return responseAdapter;
    }

    @Override
    protected PacketAdapter handleTerminateSequenceAction(PacketAdapter requestAdapter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * TODO javadoc
     */
    protected PacketAdapter handleMakeConnectionAction(PacketAdapter requestAdapter) {
        throw new UnsupportedOperationException("Not supported yet.");
        // TODO
//        MakeConnectionElement mcElement = requestAdapter.unmarshallMessage();
//        Sequence outboundSequence = sequenceManager.getBoundSequence(mcElement.getIdentifier().getValue());
                
//        sequenceId = element.getIdentifier().getValue();
//        OutboundSequence outboundSequence = RMDestination.getRMDestination().getOutboundSequence(sequenceId);
//        if (outboundSequence == null) {
//            throw LOGGER.logSevereException(new RmException(LocalizationMessages.WSRM_3025_INVALID_SEQUENCE_ID_IN_MAKECONNECTION_MESSAGE(sequenceId)));
//        }
//
//        //see if we can find a message in the sequence that needs to be resent.
//        Packet ret = new Packet();
//
//        RMMessage unacknowledgedMessage = outboundSequence.getUnacknowledgedMessage();
//        if (unacknowledgedMessage != null) {
//            ret.setMessage(unacknowledgedMessage.getCopy());
//        } else {
//            ret.setMessage(Messages.createEmpty(getConfig().getSoapVersion()));
//        }
//
//        ret.invocationProperties.putAll(packet.invocationProperties);
//        return ret;
    }
}
