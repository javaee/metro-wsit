/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * A generic immutable reference holder that implements {@link Delayed} interface
 * and thus is suitable for use in a {@link java.util.concurrent.DelayQueue}
 * instances.
 *</p>
 *
 * <p>
 * Instances of this {@code DelayedReference} class work with a milliseconds precision.
 *</p>
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class DelayedReference<V> implements Delayed {

    private final V data;
    private final long resumeTimeInMilliseconds;

    private DelayedReference(V data, long resumeTimeInMilliseconds) {
        this.data = data;
        this.resumeTimeInMilliseconds = resumeTimeInMilliseconds;
    }

    public DelayedReference(V data, long delay, TimeUnit timeUnit) {
        this(data, timeUnit.toMillis(delay) + System.currentTimeMillis());
    }

    public V getValue() {
        return data;
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(resumeTimeInMilliseconds - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public int compareTo(Delayed other) {
        long thisDelay = resumeTimeInMilliseconds - System.currentTimeMillis();
        long thatDelay = other.getDelay(TimeUnit.MILLISECONDS);

        return (thisDelay < thatDelay) ? -1 : ((thisDelay == thatDelay) ? 0 : 1);
    }

    public DelayedReference<V> updateData(V data) {
        return new DelayedReference<V>(data, resumeTimeInMilliseconds);
    }

    public DelayedReference<V> updateDelay(long newDelay, TimeUnit timeUnit) {
        return new DelayedReference<V>(data, timeUnit.toMillis(newDelay) + System.currentTimeMillis());
    }
}
