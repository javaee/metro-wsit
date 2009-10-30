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
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * JAX-WS specific application message
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class JaxwsApplicationMessage extends ApplicationMessageBase {

    private static final Logger LOGGER = Logger.getLogger(JaxwsApplicationMessage.class);
    //
    private @Nullable Packet packet;
    private @NotNull final Message message;
    private @Nullable final String wsaAction;

    public JaxwsApplicationMessage(@NotNull Packet packet, @NotNull String correlationId) {
        super(correlationId);

        assert packet != null;
        assert packet.getMessage() != null;

        this.packet = packet;
        this.message = packet.getMessage();
        this.wsaAction = null;
    }

    private JaxwsApplicationMessage(int initialResendCounterValue, @NotNull String correlationId, @NotNull String wsaAction, @NotNull String sequenceId, long messageNumber, Message message) {
        super(initialResendCounterValue, correlationId, sequenceId, messageNumber, null);

        assert message != null;

        this.packet = null;
        this.message = message;
        this.wsaAction = wsaAction;
    }

    public @NotNull Message getJaxwsMessage() {
        return message;
    }

    public @NotNull Packet getPacket() {
        return packet;
    }

    void setPacket(Packet newPacket) {
        // FIXME once this method is not needed, remove it and make packet attribute final
        newPacket.setMessage(message);
        this.packet = newPacket;
    }

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            if (message != null) {
                XMLStreamWriter xsw = XMLStreamWriterFactory.create(baos, "UTF-8");
                try {
                    packet.getMessage().copy().writeTo(xsw);
                } catch (XMLStreamException ex) {
                    throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRM_1142_UNABLE_TO_SERIALIZE_MSG_TO_XML_STREAM(), ex));
                } finally {
                    try {
                        xsw.close();
                    } catch (XMLStreamException ex) {
                        LOGGER.warning(LocalizationMessages.WSRM_1143_ERROR_CLOSING_XSW_AFTER_MSG_SERIALIZATION(), ex);
                    }
                }
            }

            return baos.toByteArray();
        } finally {
            try {
                baos.close();
            } catch (IOException ex) {
                LOGGER.warning(LocalizationMessages.WSRM_1144_ERROR_CLOSING_BAOS_AFTER_MSG_SERIALIZATION(), ex);
            }
        }
    }

    /**
     * Returns WS-Addressing action header value - used in ServerTube as a workaround
     *
     * FIXME remove when no longer needed
     * 
     * @return WS-Addressing action header value
     */
    public String getWsaAction() {
        return wsaAction;
    }

    public static JaxwsApplicationMessage newInstance(@NotNull InputStream dataStream, int initialResendCounterValue, @NotNull String correlationId, @NotNull String wsaAction, @NotNull String sequenceId, long messageNumber) {
        try {
            XMLStreamReader xsr = XMLStreamReaderFactory.create(null, dataStream, "UTF-8", true);
            try {
                Message m = Messages.create(xsr);
                return new JaxwsApplicationMessage(initialResendCounterValue, correlationId, wsaAction, sequenceId, messageNumber, m);
            } finally {
                try {
                    xsr.close();
                } catch (XMLStreamException ex) {
                    LOGGER.warning("Error closing XMLStreamReader after message was de-serialized from XML stream", ex);
                }
            }
        } finally {
            try {
                dataStream.close();
            } catch (IOException ex) {
                LOGGER.warning("Error closing data input stream after message was de-serialized from bytes", ex);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("JAX-WS Application Message { ");
        sb.append("sequenceId=[ ").append(this.getSequenceId()).append(" ], ");
        sb.append("messageNumber=[ ").append(this.getMessageNumber()).append(" ], ");
        sb.append("correlationId=[ ").append(this.getCorrelationId()).append(" ], ");
        sb.append("nextResendCount=[ ").append(this.getNextResendCount()).append(" ], ");
        sb.append("wsaAction=[ ").append(this.wsaAction);
        sb.append(" ] }");
        return super.toString();
    }
}
