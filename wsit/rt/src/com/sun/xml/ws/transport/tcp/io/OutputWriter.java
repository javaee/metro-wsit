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

package com.sun.xml.ws.transport.tcp.io;

import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.DumpUtils;
import com.sun.xml.ws.transport.tcp.util.SelectorFactory;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NIO utility to flush <code>ByteBuffer</code>
 *
 * @author Scott Oaks
 * @author Alexey Stashok
 */
public final class OutputWriter {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".dump");
    
    /**
     * Flush the buffer by looping until the <code>ByteBuffer</code> is empty
     * @param bb the ByteBuffer to write.
     */
    public static void flushChannel(final SocketChannel socketChannel, final ByteBuffer bb)
    throws IOException{
        if (logger.isLoggable(Level.FINEST)) {
            Socket socket = socketChannel.socket();
            logger.log(Level.FINEST, MessagesMessages.WSTCP_1070_OUTPUT_WRITER_DUMP(socket.getInetAddress().getHostAddress(), socketChannel.socket().getPort()));
            logger.log(Level.FINEST, DumpUtils.dumpBytes(bb));
        }
        
        SelectionKey key = null;
        Selector writeSelector = null;
        int attempts = 0;
        try {
            while ( bb.hasRemaining() ) {
                final int len = socketChannel.write(bb);
                attempts++;
                if (len < 0){
                    throw new EOFException();
                }
                
                if (len == 0) {
                    if ( writeSelector == null ){
                        writeSelector = SelectorFactory.getSelector();
                        if ( writeSelector == null){
                            // Continue using the main one.
                            continue;
                        }
                    }
                    
                    key = socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
                    
                    if (writeSelector.select(30 * 1000) == 0) {
                        if (attempts > 2) {
                            Socket socket = socketChannel.socket();
                            throw new IOException(MessagesMessages.WSTCP_0019_PEER_DISCONNECTED(socket.getInetAddress().getHostAddress(), socketChannel.socket().getPort()));
                        }
                    } else {
                        attempts--;
                    }
                } else {
                    attempts = 0;
                }
            }
        } finally {
            if (key != null) {
                key.cancel();
                key = null;
            }
            
            if ( writeSelector != null ) {
                // Cancel the key.
                writeSelector.selectNow();
                SelectorFactory.returnSelector(writeSelector);
            }
        }
    }
    
    /**
     * Flush the buffer by looping until the <code>ByteBuffer</code> is empty
     * @param bb the ByteBuffer to write.
     */
    public static void flushChannel(final SocketChannel socketChannel, final ByteBuffer[] bb)
    throws IOException{
        if (logger.isLoggable(Level.FINEST)) {
            Socket socket = socketChannel.socket();
            logger.log(Level.FINEST, MessagesMessages.WSTCP_1070_OUTPUT_WRITER_DUMP(socket.getInetAddress().getHostAddress(), socketChannel.socket().getPort()));
            logger.log(Level.FINEST, DumpUtils.dumpBytes(bb));
        }
        SelectionKey key = null;
        Selector writeSelector = null;
        int attempts = 0;
        try {
            while (hasRemaining(bb)) {
                final long len = socketChannel.write(bb);
                attempts++;
                if (len < 0){
                    throw new EOFException();
                }
                
                if (len == 0) {
                    if ( writeSelector == null ){
                        writeSelector = SelectorFactory.getSelector();
                        if ( writeSelector == null){
                            // Continue using the main one.
                            continue;
                        }
                    }
                    
                    key = socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
                    
                    if (writeSelector.select(30 * 1000) == 0) {
                        if (attempts > 2) {
                            Socket socket = socketChannel.socket();
                            throw new IOException(MessagesMessages.WSTCP_0019_PEER_DISCONNECTED(socket.getInetAddress().getHostAddress(), socketChannel.socket().getPort()));
                        }
                    } else {
                        attempts--;
                    }
                } else {
                    attempts = 0;
                }
            }
        } finally {
            if (key != null) {
                key.cancel();
                key = null;
            }
            
            if ( writeSelector != null ) {
                // Cancel the key.
                writeSelector.selectNow();
                SelectorFactory.returnSelector(writeSelector);
            }
        }
    }
    
    private static boolean hasRemaining(final ByteBuffer[] bb) {
        for(int i=bb.length - 1; i>=0; i--) {
            if (bb[i].hasRemaining()) {
                return true;
            }
        }
        
        return false;
    }
}
