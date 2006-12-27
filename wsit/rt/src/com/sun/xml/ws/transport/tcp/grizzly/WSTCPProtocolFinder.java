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

import com.sun.enterprise.web.portunif.ProtocolFinder;
import com.sun.enterprise.web.portunif.util.ProtocolInfo;
import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * A <code>ProtocolFinder</code> implementation that parse the available
 * SocketChannel bytes looking for the PROTOCOL_ID bytes. An SOAP/TCP request will
 * always start with: vnd.sun.ws.tcp
 *
 * This object shoudn't be called by several threads simultaneously.
 *
 * @author Jeanfrancois Arcand
 * @author Alexey Stashok
 */
public final class WSTCPProtocolFinder implements ProtocolFinder {
    
    public WSTCPProtocolFinder() {
    }
    
    
    /**
     * Try to find the protocol from the <code>SocketChannel</code> bytes.
     *
     * @param selectionKey The key from which the SocketChannel can be retrieved.
     * @return ProtocolInfo The ProtocolInfo that contains the information about the
     *                   current protocol.
     */
    public void find(@NotNull final ProtocolInfo protocolInfo) throws IOException {
        final SelectionKey key = protocolInfo.key;
        final SocketChannel socketChannel = (SocketChannel)key.channel();
        final ByteBuffer byteBuffer = protocolInfo.byteBuffer;
        
        int loop = 0;
        int count = -1;
        
        if (protocolInfo.bytesRead == 0) {
            try {
                while ( socketChannel.isOpen() &&
                        ((count = socketChannel.read(byteBuffer))> -1)){
                    
                    if ( count == 0 ){
                        loop++;
                        if (loop > 2){
                            break;
                        }
                        continue;
                    }
                }
            } catch (IOException ex){
                ;
            } finally {
                if ( count == -1 ){
                    return;
                }
                protocolInfo.bytesRead = count;
            }
        }

        final int curPosition = byteBuffer.position();
        final int curLimit = byteBuffer.limit();
        
        // Rule a - If read length < PROTOCOL_ID.length, return to the Selector.
        if (curPosition < TCPConstants.PROTOCOL_SCHEMA.length()){
            return;
        }
        
        byteBuffer.position(0);
        byteBuffer.limit(curPosition);
        
        // Rule b - check protocol id
        try {
            final byte[] protocolBytes = new byte[TCPConstants.PROTOCOL_SCHEMA.length()];
            byteBuffer.get(protocolBytes);
            final String incomeProtocolId = new String(protocolBytes);
            if (TCPConstants.PROTOCOL_SCHEMA.equals(incomeProtocolId)) {
                protocolInfo.protocol = TCPConstants.PROTOCOL_SCHEMA;
                protocolInfo.byteBuffer = byteBuffer;
                protocolInfo.socketChannel =
                        (SocketChannel)key.channel();
                protocolInfo.isSecure = false;
            }
        } catch (BufferUnderflowException bue) {
        } finally {
            byteBuffer.limit(curLimit);
            byteBuffer.position(curPosition);
        }
    }
    
}
