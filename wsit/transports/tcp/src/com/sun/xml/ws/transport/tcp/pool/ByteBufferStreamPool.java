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

package com.sun.xml.ws.transport.tcp.pool;

import com.sun.xml.ws.util.Pool;

/**
 * @author Alexey Stashok
 */
public class ByteBufferStreamPool<T extends LifeCycle> {
    
    private Pool<T> pool;
    public ByteBufferStreamPool(final Class<T> memberClass) {
        pool = new Pool() {
            protected T create() {
                T member = null;
                try {
                    member = ByteBufferStreamPool.this.create(memberClass);
                } catch (Exception e) {
                }
                
                return member;
            }
        };
    }
    
    private T create(Class<T> memberClass) throws InstantiationException, IllegalAccessException {
        return memberClass.newInstance();
    }
    
    public T take() {
        T member = pool.take();
        member.activate();
        return member;
    }
    
    public void release(T member) {
        member.passivate();
        pool.recycle(member);
    }
}
