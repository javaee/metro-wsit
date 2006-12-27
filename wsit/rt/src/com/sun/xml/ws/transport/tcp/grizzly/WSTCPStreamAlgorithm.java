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

import com.sun.enterprise.web.connector.grizzly.Handler;
import com.sun.enterprise.web.connector.grizzly.algorithms.StreamAlgorithmBase;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author Alexey Stashok
 */
public final class WSTCPStreamAlgorithm extends StreamAlgorithmBase {
    
    private ByteBuffer resultByteBuffer;
    
    public Handler getHandler() {
        return handler;
    }
    
    public boolean parse(final ByteBuffer byteBuffer) {
        byteBuffer.flip();
        this.resultByteBuffer = byteBuffer;
        return true;
    }
    
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }
    
    public ByteBuffer getByteBuffer() {
        return resultByteBuffer;
    }
    
    /**
     * Algorith is usually created with Class.newInstance -> its port 
     * is not set before,
     * but port value is required in handler's constructor
     */
    public void setPort(final int port) {
        super.setPort(port);
        handler = new WSTCPFramedConnectionHandler(this);
    }

    public void recycle(){
        resultByteBuffer = null;
        socketChannel = null;
        
        super.recycle();
    }

}
