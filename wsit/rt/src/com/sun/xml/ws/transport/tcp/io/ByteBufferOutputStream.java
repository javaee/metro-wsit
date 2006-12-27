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

import com.sun.xml.ws.transport.tcp.util.ByteBufferFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author Alexey Stashok
 */
public class ByteBufferOutputStream extends OutputStream {
    private static final boolean USE_DIRECT_BUFFER = false;
    
    private ByteBuffer outputBuffer;
    
    public ByteBufferOutputStream() {
        outputBuffer = ByteBufferFactory.allocateView(USE_DIRECT_BUFFER);
    }
    
    public ByteBufferOutputStream(int initSize) {
        outputBuffer = ByteBufferFactory.allocateView(initSize, USE_DIRECT_BUFFER);
    }

    public ByteBufferOutputStream(final ByteBuffer outputBuffer) {
        this.outputBuffer = outputBuffer;
    }

    public void reset() {
        outputBuffer.clear();
    }
    
    public ByteBuffer getByteBuffer() {
        outputBuffer.flip();
        return outputBuffer;
    }
    
    public void write(final int data) throws IOException {
        if (outputBuffer.position() == outputBuffer.capacity() - 1) {
            final ByteBuffer tmpBuffer = ByteBufferFactory.allocateView(outputBuffer.capacity() * 2, USE_DIRECT_BUFFER);
            tmpBuffer.put(outputBuffer);
            outputBuffer = tmpBuffer;
        }
        
        outputBuffer.put((byte) data);
    }

    public void close() {
    }
}
