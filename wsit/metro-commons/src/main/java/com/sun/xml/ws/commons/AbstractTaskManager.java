/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.commons;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import com.sun.istack.logging.Logger;

import com.sun.xml.ws.api.Component;

/**
 * Abstract parent of *TaskManager classes com.sun.xml.ws.commons created to 
 * avoid duplication of code in the individual implementations 
 *
 */
public abstract class AbstractTaskManager {
    private final AtomicBoolean isClosed;
    private volatile ScheduledExecutorService executorService;
    private boolean useContainerSpi = true;
    
    protected abstract Component getComponent();
    protected abstract String getThreadPoolName();
    protected abstract ThreadFactory createThreadFactory();
    protected abstract int getThreadPoolSize();
    protected abstract Logger getLogger();
    
    protected AbstractTaskManager() {
        this.isClosed = new AtomicBoolean(false);
    }
    
    /**
     * shutdown the ScheduledExecutorService if we created it.
     * @param force - if true, wait for time given by delayMillis, then force shutdown if needed
     * @param delayMillis
     */
    protected void close(boolean force, long delayMillis) {
        if (useContainerSpi) return;
        if (executorService == null) return;
        if (isClosed.compareAndSet(false, true)) {
            executorService.shutdown();
        }
        if (!force) return;
        
        if (!executorService.isTerminated()) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException ex) {
                getLogger().fine("Interrupted while waiting for a scheduler to shut down.", ex);
            }
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
            }
        }
    }
    
    /**
     * shutdown the ScheduledExecutorService if we created it.
     */
    public void close() {
        close(false, 0);
    }

    public boolean isClosed() {
        if (useContainerSpi) return false;
        return isClosed.get();
    }
    
    /**
     * Return the appropriate ScheduledExecutorService - on initial access, check for container.getSPI
     * NOTE - A COPY OF THIS METHOD CAN BE FOUND AT {@link com.sun.xml.ws.metro.api.config.management.ManagedEndpoint#getExecutorService() ManagedEndpoint.getExecutorService() } IN metro-cm-api
     * IF A SUITABLE COMMON LOCATION CAN BE FOUND BOTH COPIES MUST BE MOVED
     * @return
     */
    protected ScheduledExecutorService getExecutorService() {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null) {
                    if (getComponent() != null) {
                        executorService = getComponent().getSPI(ScheduledExecutorService.class);
                    }
                    if (executorService == null) {
                        //container did not return an SPI - create our own thread pool
                        getLogger().finer("Container did not return SPI for ScheduledExecutorService - creating thread pool for " + getThreadPoolName());
                        ThreadFactory threadFactory = createThreadFactory();
                        if (threadFactory == null) {
                            executorService = Executors.newScheduledThreadPool(getThreadPoolSize());
                        } else {
                            executorService = Executors.newScheduledThreadPool(getThreadPoolSize(), threadFactory);
                        }
                        useContainerSpi = false;
                    } else {
                        getLogger().finer("Using Container SPI for ScheduledExecutorService for " + getThreadPoolName());
                        useContainerSpi = true;
                    }
                    this.isClosed.set(false);
                }
            }
        }
        return executorService;
    }
}
