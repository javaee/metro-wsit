/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.xml.ws.transport.tcp.grizzly;

import com.sun.enterprise.web.portunif.ProtocolHandler;
import com.sun.enterprise.web.portunif.util.ProtocolInfo;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.server.IncomeMessageProcessor;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public final class WSTCPProtocolHandler implements ProtocolHandler {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private static IncomeMessageProcessor processor;
    
    public static void setIncomingMessageProcessor(final IncomeMessageProcessor processor) {
        WSTCPProtocolHandler.processor = processor;
    }
    
    public String[] getProtocols() {
        return new String[] {TCPConstants.PROTOCOL_SCHEMA};
    }
    
    public void handle(final ProtocolInfo tupple) throws IOException {
        if (processor != null) {
            tupple.byteBuffer.flip();
            processor.process(tupple.byteBuffer, (SocketChannel) tupple.key.channel());
        } else {
            logger.log(Level.WARNING, MessagesMessages.WSTCP_0013_TCP_PROCESSOR_NOT_REGISTERED());
        }
    }
}
