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
import com.sun.istack.Nullable;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.commons.xmlutil.Converter;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.localization.LocalizationMessages;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class SerializableMessage {

    private static final Logger LOGGER = Logger.getLogger(SerializableMessage.class);

    private @Nullable Packet packet;
    private @NotNull final Message message;
    private @Nullable final String wsaAction;

    public SerializableMessage(Packet packet, String wsaAction) {
        this.packet = packet;
        this.message = packet.getMessage();
        this.wsaAction = wsaAction;
    }

    public SerializableMessage(Message message, String wsaAction) {
        assert message != null;

        this.packet = null;
        this.message = message;
        this.wsaAction = wsaAction;
    }

    public Message getMessage() {
        return message;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet newPacket) {
        newPacket.setMessage(message);
        this.packet = newPacket;
    }

    public String getWsaAction() {
        return wsaAction;
    }

    public byte[] toBytes() {
        try {
            return Converter.toBytes(message.copy(), Converter.UTF_8);
        } catch (XMLStreamException ex) {
            throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRX_1001_UNABLE_TO_SERIALIZE_MSG_TO_XML_STREAM(), ex));
        }
    }

    public static SerializableMessage newInstance(@NotNull InputStream dataStream, String wsaAction) {
        Message m;
        try {
            m = Converter.toMessage(dataStream, Converter.UTF_8);
        } catch (XMLStreamException ex) {
            throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRX_1002_UNABLE_TO_DESERIALIZE_MSG_FROM_XML_STREAM(), ex));
        }
        return new SerializableMessage(m, wsaAction);
    }
}
