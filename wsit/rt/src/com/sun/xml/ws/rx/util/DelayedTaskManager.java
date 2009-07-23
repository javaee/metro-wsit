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

import com.sun.istack.NotNull;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.commons.NamedThreadFactory;
import com.sun.xml.ws.rx.util.DelayedReference;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public final class DelayedTaskManager<T> {

    private static final Logger LOGGER = Logger.getLogger(DelayedTaskManager.class);

    public static interface DelayedTask<T> {

        public void handle(T data, DelayedTaskManager<T> manager);
    }

    private static class Registration<T> {

        public final T message;
        public final DelayedTask<T> handler;

        public Registration(T message, DelayedTask<T> handler) {
            this.message = message;
            this.handler = handler;
        }
    }
    //

    private class WorkerTask implements Runnable {

        private final AtomicBoolean isRunning = new AtomicBoolean(false);

        /**
         * This method contains main task loop. It should not be called directly from outside.
         */
        public void run() {
            try {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest(String.format("Worker task executed - registration queue size: [ %d ]", registrations.size()));
                }

                while (!registrations.isEmpty()) {
                    DelayedReference<Registration<T>> registration = registrations.take();
                    registration.getData().handler.handle(registration.getData().message, DelayedTaskManager.this);
                }

            } catch (InterruptedException ex) {
                LOGGER.logException(ex, Level.CONFIG);
                Thread.currentThread().interrupt();
            } finally {
                isRunning.set(false);
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest(String.format("Worker task execution finished - registration queue size: [ %d ]", registrations.size()));
                }
            }
        }
    }
    //
    private final ExecutorService executorService;
    private final DelayQueue<DelayedReference<Registration<T>>> registrations;
    private final WorkerTask workerTask;

    public DelayedTaskManager(String name) {
        this.executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory(name + "-worker-executor"));
        this.registrations = new DelayQueue<DelayedReference<Registration<T>>>();
        this.workerTask = new WorkerTask();
    }

    public boolean register(@NotNull T data, long delay, TimeUnit timeUnit, @NotNull DelayedTask<T> dataHandler) {
        assert data != null;
        assert dataHandler != null;
        boolean offerResult = registrations.offer(new DelayedReference<Registration<T>>(new Registration<T>(data, dataHandler), delay, timeUnit));
        if (workerTask.isRunning.compareAndSet(false, true)) {
            executorService.submit(workerTask);
        }
        return offerResult;
    }
}
