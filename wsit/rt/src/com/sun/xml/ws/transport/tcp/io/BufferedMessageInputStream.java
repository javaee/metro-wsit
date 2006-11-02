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

import com.sun.xml.ws.util.ByteArrayBuffer;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Alexey Stashok
 */
public class BufferedMessageInputStream extends InputStream {
    private InputStream inputStream;
    
    private boolean isClosed;
    private boolean isBuffered;
    
    private int bufferedSize;
    
    public BufferedMessageInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        isBuffered = false;
    }
    
    
    public int read() throws IOException {
        return inputStream.read();
    }
    
    public int read(byte[] b, int offset, int length) throws IOException {
        return inputStream.read(b, offset, length);
    }
    
    public void bufferMessage() throws IOException {
        if (!isBuffered) {
            ByteArrayBuffer baBuffer = new ByteArrayBuffer();
            try {
                baBuffer.write(inputStream);
                inputStream = baBuffer.newInputStream();
                bufferedSize = baBuffer.size();
                isBuffered = true;
            } finally {
                baBuffer.close();
            }
        }
    }
    
    public InputStream getSourceInputStream() {
        return inputStream;
    }
    
    public int getBufferedSize() {
        if (isBuffered) {
            return bufferedSize;
        }
        
        return 0;
    }
    
    public boolean isClosed() {
        return isClosed;
    }
    
    public boolean isBuffered() {
        return isBuffered;
    }
    
    public void close() throws IOException {
        isClosed = true;
        inputStream.close();
    }
    
    
}
