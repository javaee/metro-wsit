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

