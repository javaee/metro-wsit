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
import com.sun.istack.Nullable;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.protocol.AcknowledgementData;
import com.sun.xml.ws.rx.rm.protocol.CloseSequenceData;
import com.sun.xml.ws.rx.rm.protocol.CloseSequenceResponseData;
import com.sun.xml.ws.rx.rm.protocol.CreateSequenceData;
import com.sun.xml.ws.rx.rm.protocol.CreateSequenceResponseData;
import com.sun.xml.ws.rx.rm.protocol.TerminateSequenceData;
import com.sun.xml.ws.rx.rm.protocol.TerminateSequenceResponseData;
import com.sun.xml.ws.rx.util.Communicator;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public abstract class WsrmProtocolHandler {

    private static final Logger LOGGER = Logger.getLogger(WsrmProtocolHandler.class);

    public static WsrmProtocolHandler getInstance(RmConfiguration configuration, Communicator communicator, RuntimeContext rc) {
        switch (configuration.getRmFeature().getVersion()) {
            case WSRM200502:
                return new Wsrm200502ProtocolHandler(configuration, rc, communicator);
            case WSRM200702:
                return new Wsrm200702ProtocolHandler(configuration, rc, communicator);
            default:
                return null;
        }
    }

    protected final RmVersion rmVersion;
    protected final Communicator communicator;

    protected final AddressingVersion addressingVersion;
    protected final SOAPVersion soapVersion;

    public abstract void appendSequenceHeader(@NotNull Message jaxwsMessage, @NotNull ApplicationMessage message) throws RxRuntimeException;

    public abstract void appendAcknowledgementHeaders(@NotNull Packet packet, @NotNull AcknowledgementData ackData) throws RxRuntimeException;

    public abstract AcknowledgementData getAcknowledgementData(Message jaxwsMessage) throws RxRuntimeException;

    public abstract void loadAcknowledgementData(@NotNull ApplicationMessage message, @NotNull Message jaxwsMessage) throws RxRuntimeException;

    public abstract void loadSequenceHeaderData(@NotNull ApplicationMessage message, @NotNull Message jaxwsMessage) throws RxRuntimeException;

    public abstract CreateSequenceData toCreateSequenceData(@NotNull Packet packet) throws RxRuntimeException;

    public abstract Packet toPacket(@NotNull CreateSequenceData data, @Nullable Packet requestPacket) throws RxRuntimeException;

    public abstract CreateSequenceResponseData toCreateSequenceResponseData(@NotNull Packet packet) throws RxRuntimeException;

    public abstract Packet toPacket(@NotNull CreateSequenceResponseData data, @Nullable Packet requestPacket) throws RxRuntimeException;

    public abstract CloseSequenceData toCloseSequenceData(@NotNull Packet packet) throws RxRuntimeException;

    public abstract Packet toPacket(@NotNull CloseSequenceData data, @Nullable Packet requestPacket) throws RxRuntimeException;

    public abstract CloseSequenceResponseData toCloseSequenceResponseData(@NotNull Packet packet) throws RxRuntimeException;

    public abstract Packet toPacket(@NotNull CloseSequenceResponseData data, @Nullable Packet requestPacket) throws RxRuntimeException;

    public abstract TerminateSequenceData toTerminateSequenceData(@NotNull Packet packet) throws RxRuntimeException;

    public abstract Packet toPacket(@NotNull TerminateSequenceData data, @Nullable Packet requestPacket) throws RxRuntimeException;

    public abstract TerminateSequenceResponseData toTerminateSequenceResponseData(@NotNull Packet packet) throws RxRuntimeException;

    public abstract Packet toPacket(@NotNull TerminateSequenceResponseData data, @Nullable Packet requestPacket) throws RxRuntimeException;

    public abstract Header createSequenceFaultElementHeader(QName subcode, Object detail);

    public abstract Packet createEmptyAcknowledgementResponse(AcknowledgementData ackData, Packet requestPacket) throws RxRuntimeException;

    public final boolean containsProtocolMessage(@NotNull Packet packet) {
        assert packet != null;

        return (packet.getMessage() == null) ? false : rmVersion.isRmAction(getWsaAction(packet.getMessage()));
    }

    public final boolean containsProtocolRequest(@NotNull Packet packet) {
        assert packet != null;

        return (packet.getMessage() == null) ? false : rmVersion.isRmProtocolRequest(getWsaAction(packet.getMessage()));
    }

    public final boolean containsProtocolResponse(@NotNull Packet packet) {
        assert packet != null;

        return (packet.getMessage() == null) ? false : rmVersion.isRmProtocolResponse(getWsaAction(packet.getMessage()));
    }

    protected WsrmProtocolHandler(@NotNull RmVersion rmVersion, @NotNull RmConfiguration configuration, @NotNull Communicator communicator) {
        assert rmVersion != null;
        assert rmVersion == configuration.getRmFeature().getVersion();
        assert configuration != null;
        assert communicator != null;

        this.rmVersion = rmVersion;
        this.communicator = communicator;

        this.addressingVersion = configuration.getAddressingVersion();
        this.soapVersion = configuration.getSoapVersion();
    }

    protected final Header createHeader(Object jaxbHeaderContent) {
        return Headers.create(rmVersion.getJaxbContext(addressingVersion), jaxbHeaderContent);
    }

    protected final <T> T readHeaderAsUnderstood(@NotNull String nsUri, @NotNull String name, @NotNull Message message) throws RxRuntimeException {
        assert nsUri != null;
        assert name != null;
        assert message != null;

        Header header = message.getHeaders().get(nsUri, name, true);
        if (header == null) {
            return null;
        }
        try {
            @SuppressWarnings(value = "unchecked")
            T result = (T) header.readAsJAXB(getJaxbUnmarshaller());
            return result;
        } catch (JAXBException ex) {
            throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRM_1122_ERROR_MARSHALLING_RM_HEADER(nsUri + "#" + name), ex));
        }
    }

    protected final String getWsaAction(@NotNull Message message) {
        return message.getHeaders().getAction(addressingVersion, soapVersion);
    }

    protected final JAXBRIContext getJaxbContext() {
        return rmVersion.getJaxbContext(addressingVersion);
    }

    protected final Unmarshaller getJaxbUnmarshaller() throws RxRuntimeException {
        return rmVersion.createUnmarshaller(addressingVersion);
    }

    /**
     * Unmarshalls underlying JAXWS {@link Message} using JAXB context of a configured RM version
     *
     * @return message content unmarshalled JAXB bean
     *
     * @throws com.sun.xml.ws.rm.RxException in case the message unmarshalling failed
     */
    protected final <T> T unmarshallMessage(@NotNull Message message) throws RxRuntimeException {
        assert message != null;

        try {
            @SuppressWarnings("unchecked") T result = (T) message.readPayloadAsJAXB(getJaxbUnmarshaller());
            return result;
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRM_1123_ERROR_UNMARSHALLING_MESSAGE(), e));
        }
    }
}
