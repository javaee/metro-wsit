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

package com.sun.xml.ws.transport.tcp.io;

import com.sun.xml.ws.transport.tcp.util.DumpUtils;
import com.sun.xml.ws.transport.tcp.util.SelectorFactory;
import java.io.EOFException;
import java.io.IOException;
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
    public static void flushChannel(SocketChannel socketChannel, ByteBuffer bb)
            throws IOException{
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Output dump:");
            logger.log(Level.FINEST, DumpUtils.dump(bb));
        }
        
        SelectionKey key = null;
        Selector writeSelector = null;
        int attempts = 0;
        try {
            while ( bb.hasRemaining() ) {
                int len = socketChannel.write(bb);
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
                    
                    key = socketChannel.register(writeSelector, key.OP_WRITE);
                    
                    if (writeSelector.select(30 * 1000) == 0) {
                        if (attempts > 2)
                            throw new IOException("Client disconnected");
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
    public static void flushChannel(SocketChannel socketChannel, ByteBuffer[] bb)
            throws IOException{
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Output dump:");
            logger.log(Level.FINEST, DumpUtils.dump(bb));
        }
        SelectionKey key = null;
        Selector writeSelector = null;
        int attempts = 0;
        try {
            while (hasRemaining(bb)) {
                long len = socketChannel.write(bb);
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
                    
                    key = socketChannel.register(writeSelector, key.OP_WRITE);
                    
                    if (writeSelector.select(30 * 1000) == 0) {
                        if (attempts > 2)
                            throw new IOException("Client disconnected");
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

    private static boolean hasRemaining(ByteBuffer[] bb) {
        for(int i=0; i<bb.length; i++) {
            if (bb[i].hasRemaining()) {
                return true;
            }
        }
        
        return false;
    }
}
