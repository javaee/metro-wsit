/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scheduled task manager provides a higher-level API for scheduling and controlling
 * tasks that should run on a separate thread(s).
 *
 * <b>
 * WARNING: This class is a private utility class used by WSIT implementation. Any usage outside
 * the intedned scope is strongly discouraged. The API exposed by this class may be changed, replaced
 * or removed without any advance notice.
 * </b>
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class ScheduledTaskManager extends AbstractTaskManager {
    private static final Logger LOGGER = Logger.getLogger(ScheduledTaskManager.class);
    private static final AtomicInteger instanceNumber = new AtomicInteger(1);

    private static final long DELAY = 2000;
    private static final long PERIOD = 100;
    //
    private final String name;
    private final Queue<ScheduledFuture<?>> scheduledTaskHandles;
    private final Component component;
    private final String threadNamePrefix;
    
    /**
     * TODO javadoc
     */
    public ScheduledTaskManager(String name, Component component) {
        super();
        this.name = name.trim();
        this.component = component;
        
        // make all lowercase, replace all occurences of subsequent empty characters with a single dash and append some info
        this.threadNamePrefix = this.name.toLowerCase().replaceAll("\\s+", "-") + "-scheduler-" + instanceNumber.getAndIncrement();
        this.scheduledTaskHandles = new ConcurrentLinkedQueue<ScheduledFuture<?>>();
    }

    public void stopAllTasks() {
        ScheduledFuture<?> handle;
        while ((handle = scheduledTaskHandles.poll()) != null) {
            handle.cancel(false);
        }
    }

    /**
     * Stops all the tasks and shuts down the scheduled task executor
     */
    public void shutdown() {
        stopAllTasks();
        //force close after waiting for period given by DELAY
        close(true, DELAY);
    }

    /**
     * Adds a new task for scheduled execution.
     *
     * @param task new task to be executed regularly at a defined rate
     * @param initialDelay the time to delay first execution (in milliseconds)
     * @param period the period between successive executions (in milliseconds)
     */
    public ScheduledFuture<?> startTask(Runnable task, long initialDelay, long period) {
        final ScheduledFuture<?> taskHandle = getExecutorService().scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
        if (!scheduledTaskHandles.offer(taskHandle)) {
            // TODO L10N
            LOGGER.warning(String.format("Unable to store handle for task of class [ %s ]", task.getClass().getName()));
        }
        return taskHandle;
    }

    /**
     * Adds a new task for scheduled execution.
     *
     * @param task new task to be executed regularly at a predefined rate
     */
    public ScheduledFuture<?> runOnce(Runnable task) {
        return startTask(task, DELAY, PERIOD);
    }
    
    @Override
    protected ThreadFactory createThreadFactory() {
        return new NamedThreadFactory(threadNamePrefix);
    }
    
    @Override
    protected String getThreadPoolName() {
        return threadNamePrefix;
    }
    
    @Override
    protected int getThreadPoolSize() {
        return 1;
    }

    @Override
    protected Component getComponent() {
        return component;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
