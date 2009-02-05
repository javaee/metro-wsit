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
package com.sun.xml.ws.rx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class ScheduledTaskManager {

    private static final long DELAY = 2000;
    private static final long PERIOD = 100;
    private final ScheduledExecutorService scheduler;
    private final Queue<ScheduledFuture<?>> scheduledTaskHandles;

    /**
     * TODO javadoc
     */
    public ScheduledTaskManager() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduledTaskHandles = new ConcurrentLinkedQueue<ScheduledFuture<?>>();
    }

    /**
     * Starts the scheduled task executor
     */
    public List<ScheduledFuture<?>> startTasks(Runnable... tasks) {
        List<ScheduledFuture<?>> handles = new ArrayList<ScheduledFuture<?>>(tasks.length);
        for (Runnable task : tasks) {
            handles.add(startTask(task));
        }
        
        return handles;
    }

    /**
     * Stops the  scheduled task executor
     */
    public void stopAll() {
        ScheduledFuture<?> handle;
        while ((handle = scheduledTaskHandles.poll()) != null) {
            handle.cancel(false);
        }
        scheduler.shutdown();
    }

    /**
     * Adds a new task for scheduled execution.
     *
     * @param task new task to be executed regularly at a defined rate
     */
    public ScheduledFuture<?> startTask(Runnable task, long delay, long period) {
        final ScheduledFuture<?> taskHandle = scheduler.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
        if (!scheduledTaskHandles.offer(taskHandle)) {
            // TODO: handle error condition
        }
        return taskHandle;
    }

    /**
     * Adds a new task for scheduled execution.
     *
     * @param task new task to be executed regularly at a predefined rate
     */
    public ScheduledFuture<?> startTask(Runnable task) {
        return startTask(task, DELAY, PERIOD);
    }
}
