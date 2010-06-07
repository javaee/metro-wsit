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
package com.sun.xml.ws.commons;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
public final class ScheduledTaskManager {
    private static final Logger LOGGER = Logger.getLogger(ScheduledTaskManager.class);
    private static final AtomicInteger instanceNumber = new AtomicInteger(1);

    private static final long DELAY = 2000;
    private static final long PERIOD = 100;
    //
    private final String name;
    private final ScheduledExecutorService executorService;
    private final Queue<ScheduledFuture<?>> scheduledTaskHandles;
    /**
     * TODO javadoc
     */
    public ScheduledTaskManager(String name) {
        this.name = name.trim();

        // make all lowercase, replace all occurences of subsequent empty characters with a single dash and append some info
        String threadNamePrefix = this.name.toLowerCase().replaceAll("\\s+", "-") + "-scheduler-" + instanceNumber.getAndIncrement();

        this.executorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory(threadNamePrefix));
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

        executorService.shutdown();
        if (!executorService.isTerminated()) {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException ex) {
                LOGGER.fine("Interrupted while waiting for a scheduler to shut down.", ex);
            }
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
            }
        }
    }

    /**
     * Adds a new task for scheduled execution.
     *
     * @param task new task to be executed regularly at a defined rate
     * @param initialDelay the time to delay first execution (in milliseconds)
     * @param period the period between successive executions (in milliseconds)
     */
    public ScheduledFuture<?> startTask(Runnable task, long initialDelay, long period) {
        final ScheduledFuture<?> taskHandle = executorService.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
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
}
