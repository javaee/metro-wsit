/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.rx.message.jaxws;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.commons.xmlutil.Converter;
import com.sun.xml.ws.rx.message.RxMessage;
import com.sun.xml.ws.rx.message.RxMessageBase;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * JAX-WS specific application message
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class JaxwsMessage extends RxMessageBase {
    public static class JaxwsMessageState implements RxMessage.State {

        private final byte[] data;
        private final String wsaAction;
        private final String correlationId;

        private JaxwsMessageState(JaxwsMessage message) {
            this.data = message.toBytes();
            this.wsaAction = message.getWsaAction();
            this.correlationId = message.getCorrelationId();
        }

        public JaxwsMessage toMessage() {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            return JaxwsMessage.newInstance(bais,correlationId, wsaAction);
        }

        @Override
        public String toString() {
            return "JaxwsMessageState" + 
                    "{\n\twsaAction=" + wsaAction + 
                    ",\n\tcorrelationId=" + correlationId + 
                    ",\n\tmessage data=\n" + Converter.messageDataToString(data, Converter.UTF_8) + 
                    "\n}";
        }               
    }
    //
    private final SerializableMessage jaxwsMessage;

    public JaxwsMessage(@NotNull Packet packet, @NotNull String correlationId) {
        super(correlationId);

        assert packet != null;
        assert packet.getMessage() != null;

        this.jaxwsMessage = new SerializableMessage(packet, null);
    }

    private JaxwsMessage(@NotNull SerializableMessage jaxwsMessage,@NotNull String correlationId) {
        super(correlationId);

        this.jaxwsMessage = jaxwsMessage;
    }

    public @NotNull Message getJaxwsMessage() {
        return jaxwsMessage.getMessage();
    }

    public @NotNull Packet getPacket() {
        return jaxwsMessage.getPacket();
    }

    public void setPacket(Packet newPacket) {
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

    public JaxwsMessageState getState() {
        return new JaxwsMessageState(this);
    }

    public static JaxwsMessage newInstance(@NotNull InputStream dataStream, @NotNull String correlationId, @NotNull String wsaAction) {
        SerializableMessage jaxwsMessage = SerializableMessage.newInstance(dataStream, wsaAction);
        return new JaxwsMessage(jaxwsMessage, correlationId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("JAX-WS Message { ");
        sb.append("correlationId=[ ").append(this.getCorrelationId()).append(" ], ");
        sb.append("wsaAction=[ ").append(this.jaxwsMessage.getWsaAction());
        sb.append(" ] }");
        return super.toString();
    }
}
