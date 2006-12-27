/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.transport.tcp.grizzly;

import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.tcp.server.IncomeMessageProcessor;
import com.sun.enterprise.web.connector.grizzly.Handler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author Alexey Stashok
 */

public final class WSTCPFramedConnectionHandler implements Handler {
    private final WSTCPStreamAlgorithm streamAlgorithm;
    private final IncomeMessageProcessor messageProcessor;
    
    public WSTCPFramedConnectionHandler(@NotNull final WSTCPStreamAlgorithm streamAlgorithm) {
        this.streamAlgorithm = streamAlgorithm;
        this.messageProcessor = IncomeMessageProcessor.getMessageProcessorForPort(streamAlgorithm.getPort());
    }
    
    public int handle(final Object request, final int code) throws IOException {
        if (code == REQUEST_BUFFERED) {
            final ByteBuffer messageBuffer = streamAlgorithm.getByteBuffer();
            final SocketChannel socketChannel = streamAlgorithm.getSocketChannel();
            messageProcessor.process(messageBuffer, socketChannel);
        }
        
        return BREAK;
    }
    
    public void attachChannel(final SocketChannel socketChannel) {
    }
}
