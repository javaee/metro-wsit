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

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Stream wrapper around a <code>ByteBuffer</code>
 */
public class ByteBufferInputStream extends InputStream {
    
    /**
     * The wrapped <code>ByteBuffer</code<
     */
    private ByteBuffer byteBuffer;
    
    // ------------------------------------------------- Constructor -------//
    
    
    public ByteBufferInputStream(final ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }
    
    /**
     * Set the wrapped <code>ByteBuffer</code>
     * @param byteBuffer The wrapped byteBuffer
     */
    public void setByteBuffer(final ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }
    
    
    /**
     * Return the available bytes
     * @return the wrapped byteBuffer.remaining()
     */
    public int available() {
        return byteBuffer.remaining();
    }
    
    
    /**
     * Close this stream.
     */
    public void close() {
    }
    
    
    /**
     * Return true if mark is supported.
     */
    public boolean markSupported() {
        return false;
    }
    
    
    /**
     * Read the first byte from the wrapped <code>ByteBuffer</code>.
     */
    public int read() {
        if (!byteBuffer.hasRemaining()){
            return -1;
        }
        
        return (byteBuffer.get() & 0xff);
    }
    
    
    /**
     * Read the bytes from the wrapped <code>ByteBuffer</code>.
     */
    public int read(final byte[] b) {
        return (read(b, 0, b.length));
    }
    
    
    /**
     * Read the first byte of the wrapped <code>ByteBuffer</code>.
     */
    public int read(final byte[] b, final int offset, int length) {
        if (!byteBuffer.hasRemaining()) {
            return -1;
        }
        
        if (length > available()) {
            length = available();
        }
        
        byteBuffer.get(b, offset, length);
        
        return length;
    }
    
    
}

