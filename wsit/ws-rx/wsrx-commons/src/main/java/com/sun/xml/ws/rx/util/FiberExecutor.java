/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.rx.util;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.Fiber.CompletionCallback;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.commons.NamedThreadFactory;
import com.sun.xml.ws.util.Pool;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO javadoc
 *
 * <b>
 * WARNING: This class is a private utility class used by WS-RX implementation. Any usage outside
 * the intended scope is strongly discouraged. The API exposed by this class may be changed, replaced
 * or removed without any advance notice.
 * </b>
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public final class FiberExecutor {

    private static class Schedule {

        private final Packet request;
        private final Fiber.CompletionCallback completionCallback;

        public Schedule(Packet request, CompletionCallback completionCallback) {
            this.request = request;
            this.completionCallback = completionCallback;
        }
    }
    private Pool<Tube> tubelinePool;
    private volatile Engine engine;
    private final List<Schedule> schedules = new LinkedList<Schedule>();
    private ExecutorService fiberExecutorService;

    public FiberExecutor(String id, Tube masterTubeline) {
        this.tubelinePool = new Pool.TubePool(masterTubeline);
        fiberExecutorService = Executors.newCachedThreadPool(new NamedThreadFactory(id + "-fiber-executor"));
        this.engine = new Engine(id, fiberExecutorService);
    }

    public Packet runSync(Packet request) {
        final Tube tubeline = tubelinePool.take();
        try {
            return engine.createFiber().runSync(tubeline, request);
        } finally {
            tubelinePool.recycle(tubeline);
        }
    }

    public synchronized void schedule(Packet request, @NotNull final Fiber.CompletionCallback callback) {
        schedules.add(new Schedule(request, callback));
    }

    public synchronized void startScheduledFibers() {
        Iterator<Schedule> iterator = schedules.iterator();
        while (iterator.hasNext()) {
            Schedule schedule = iterator.next();
            iterator.remove();

            start(schedule.request, schedule.completionCallback);
        }
    }

    public void start(Packet request, @NotNull final Fiber.CompletionCallback callback) {
        Fiber fiber = engine.createFiber();
//        if (interceptor != null) {
//            fiber.addInterceptor(interceptor);
//        }
        final Tube tube = tubelinePool.take();
        fiber.start(tube, request, new Fiber.CompletionCallback() {

            public void onCompletion(@NotNull Packet response) {
                tubelinePool.recycle(tube);
                callback.onCompletion(response);
            }

            public void onCompletion(@NotNull Throwable error) {
                // let's not reuse tubes as they might be in a wrong state, so not
                // calling tubePool.recycle()
                callback.onCompletion(error);
            }
        });
    }

    public void close() {
        Pool<Tube> tp = this.tubelinePool;
        if (tp != null) {
            // multi-thread safety of 'close' needs to be considered more carefully.
            // some calls might be pending while this method is invoked. Should we
            // block until they are complete, or should we abort them (but how?)
            Tube p = tp.take();
            p.preDestroy();
            this.tubelinePool = null;
            this.engine = null;
            this.schedules.clear();
        }


        ExecutorService fes = this.fiberExecutorService;
        if (fes != null) {
            fes.shutdownNow();
            this.fiberExecutorService = null;
        }
    }
}
