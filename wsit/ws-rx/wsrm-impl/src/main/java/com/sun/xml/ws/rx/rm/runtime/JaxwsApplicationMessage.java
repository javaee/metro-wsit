/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.commons.xmlutil.Converter;
import com.sun.xml.ws.rx.message.RxMessage;
import com.sun.xml.ws.rx.message.jaxws.SerializableMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * JAX-WS specific application message
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class JaxwsApplicationMessage extends ApplicationMessageBase {
    public static class JaxwsApplicationMessageState implements RxMessage.State {

        private final String sequenceId;
        private final long messageNumber;
        private final int nextResendCount;
        private final String correlationId;
        private final String wsaAction;
        private final byte[] data;

        private JaxwsApplicationMessageState(JaxwsApplicationMessage message) {
            this.data = message.toBytes();
            this.nextResendCount = message.getNextResendCount();
            this.correlationId = message.getCorrelationId();
            this.wsaAction = message.getWsaAction();
            this.sequenceId = message.getSequenceId();
            this.messageNumber = message.getMessageNumber();
        }

        public JaxwsApplicationMessage toMessage() {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            return JaxwsApplicationMessage.newInstance(bais, nextResendCount, correlationId, wsaAction, sequenceId, messageNumber);                        
            // closing ByteArrayInputStream has no effect, so ignoring the redundant call
        }

        @Override
        public String toString() {
            return "JaxwsApplicationMessageState" + 
                    "{\n\tsequenceId=" + sequenceId + 
                    ",\n\tmessageNumber=" + messageNumber + 
                    ",\n\tnextResendCount=" + nextResendCount + 
                    ",\n\tcorrelationId=" + correlationId + 
                    ",\n\twsaAction=" + wsaAction + 
                    ",\n\tmessage data=\n" + Converter.messageDataToString(data, Converter.UTF_8) + 
                    "\n}";
        }        
    }
    //
    private final SerializableMessage jaxwsMessage;

    public JaxwsApplicationMessage(@NotNull Packet packet, @NotNull String correlationId) {
        super(correlationId);

        assert packet != null;
        assert packet.getMessage() != null;

        this.jaxwsMessage = new SerializableMessage(packet, null);
    }

    private JaxwsApplicationMessage(@NotNull SerializableMessage jaxwsMessage, int initialResendCounterValue, @NotNull String correlationId, @NotNull String sequenceId, long messageNumber) {
        super(initialResendCounterValue, correlationId, sequenceId, messageNumber, null);

        this.jaxwsMessage = jaxwsMessage;
    }

    public 
    @NotNull
    Message getJaxwsMessage() {
        return jaxwsMessage.getMessage();
    }

    public 
    @NotNull
    Packet getPacket() {
        return jaxwsMessage.getPacket();
    }

    void setPacket(Packet newPacket) {
        // FIXME once this method is not needed, remove it and make packet attribute final
        jaxwsMessage.setPacket(newPacket);
    }

    @Override
    public byte[] toBytes() {
        return jaxwsMessage.toBytes();
    }

    /**
     * Returns WS-Addressing action header value - used in ServerTube as a workaround
     *
     * FIXME remove when no longer needed
     * 
     * @return WS-Addressing action header value
     */
    public String getWsaAction() {
        return jaxwsMessage.getWsaAction();
    }

    public JaxwsApplicationMessageState getState() {
        return new JaxwsApplicationMessageState(this);
    }

    public static JaxwsApplicationMessage newInstance(@NotNull InputStream dataStream, int initialResendCounterValue, @NotNull String correlationId, @NotNull String wsaAction, @NotNull String sequenceId, long messageNumber) {
        SerializableMessage jaxwsMessage = SerializableMessage.newInstance(dataStream, wsaAction);
        return new JaxwsApplicationMessage(jaxwsMessage, initialResendCounterValue, correlationId, sequenceId, messageNumber);
    }

    public static JaxwsApplicationMessage newInstance(@NotNull Packet packet, int initialResendCounterValue, @NotNull String correlationId, @NotNull String wsaAction, @NotNull String sequenceId, long messageNumber) {
        SerializableMessage jaxwsMessage = new SerializableMessage(packet, wsaAction);
        return new JaxwsApplicationMessage(jaxwsMessage, initialResendCounterValue, correlationId, sequenceId, messageNumber);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("JAX-WS Application Message { ");
        sb.append("sequenceId=[ ").append(this.getSequenceId()).append(" ], ");
        sb.append("messageNumber=[ ").append(this.getMessageNumber()).append(" ], ");
        sb.append("correlationId=[ ").append(this.getCorrelationId()).append(" ], ");
        sb.append("nextResendCount=[ ").append(this.getNextResendCount()).append(" ], ");
        sb.append("wsaAction=[ ").append(this.jaxwsMessage.getWsaAction());
        sb.append(" ] }");
        return sb.toString();
    }
}
