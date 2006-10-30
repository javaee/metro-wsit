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
package com.sun.xml.ws.transport.tcp.util;


import java.nio.ByteBuffer;


/**
 * Class was copied from GlassFish Grizzly sources to be available
 * also for client side and don't require GlassFish to be installed
 *
 * Factory class used to create views of a <code>ByteBuffer</code>. 
 * The ByteBuffer can by direct or not.
 *
 * @author Jean-Francois Arcand
 */
public class ByteBufferFactory{

    
    /**
     * The default capacity of the default view of a <code>ByteBuffer</code>
     */ 
    public static int defaultCapacity = 9000;
    
    
    /**
     * The default capacity of the <code>ByteBuffer</code> from which views
     * will be created.
     */
    public static int capacity = 4000000; 
    
    
    /**
     * The <code>ByteBuffer</code> used to create views.
     */
    private static ByteBuffer byteBuffer;
            
    
    /**
     * Private constructor.
     */
    private ByteBufferFactory(){
    }
    
    
    /**
     * Return a direct <code>ByteBuffer</code> view
     * @param size the Size of the <code>ByteBuffer</code>
     */ 
    public synchronized static ByteBuffer allocateView(int size, boolean direct){
        if (byteBuffer == null || 
               (byteBuffer.capacity() - byteBuffer.limit() < size)){
            if ( direct )
                byteBuffer = ByteBuffer.allocateDirect(capacity); 
            else
                byteBuffer = ByteBuffer.allocate(capacity);              
        }

        byteBuffer.limit(byteBuffer.position() + size);
        ByteBuffer view = byteBuffer.slice();
        byteBuffer.position(byteBuffer.limit());  
        
        return view;
    }

    
    /**
     * Return a direct <code>ByteBuffer</code> view using the default size.
     */ 
    public synchronized static ByteBuffer allocateView(boolean direct){
        return allocateView(defaultCapacity, direct);
    }
     
}
