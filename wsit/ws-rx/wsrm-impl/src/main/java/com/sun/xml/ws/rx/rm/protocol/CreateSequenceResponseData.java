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

package com.sun.xml.ws.rx.rm.protocol;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.IncompleteSequenceBehavior;
import javax.xml.ws.EndpointReference;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class CreateSequenceResponseData {
    public static class Builder {
        private final @NotNull String sequenceId;
        private long duration;
        private @Nullable EndpointReference acceptedSequenceAcksTo;
        private Sequence.IncompleteSequenceBehavior incompleteSequenceBehavior;

        private Builder(String sequenceId) {
            this.sequenceId = sequenceId;
            this.duration = Sequence.NO_EXPIRY;
            this.incompleteSequenceBehavior = Sequence.IncompleteSequenceBehavior.getDefault();
        }

        public Builder acceptedSequenceAcksTo(EndpointReference acceptedSequenceAcksTo) {
            this.acceptedSequenceAcksTo = acceptedSequenceAcksTo;
            return this;
        }

        public Builder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder incompleteSequenceBehavior(Sequence.IncompleteSequenceBehavior value) {
            this.incompleteSequenceBehavior = value;
            return this;
        }

        public CreateSequenceResponseData build() {
            return new CreateSequenceResponseData(sequenceId, duration, acceptedSequenceAcksTo, incompleteSequenceBehavior);
        }
    }

    public static Builder getBuilder(String sequenceId) {
        return new Builder(sequenceId);
    }

    private final @NotNull String sequenceId;
    private final long duration;
    private final @Nullable EndpointReference acceptedSequenceAcksTo;
    private final Sequence.IncompleteSequenceBehavior incompleteSequenceBehavior;
    // TODO add incompleteSequenceBehavior handling

    private CreateSequenceResponseData(@NotNull String sequenceId, long expirationTime, @Nullable EndpointReference acceptedSequenceAcksTo, Sequence.IncompleteSequenceBehavior incompleteSequenceBehavior) {
        this.sequenceId = sequenceId;
        this.duration = expirationTime;
        this.acceptedSequenceAcksTo = acceptedSequenceAcksTo;
        this.incompleteSequenceBehavior = incompleteSequenceBehavior;
    }

    public @Nullable EndpointReference getAcceptedSequenceAcksTo() {
        return acceptedSequenceAcksTo;
    }

    public long getDuration() {
        return duration;
    }

    public boolean doesNotExpire() {
        return duration == Sequence.NO_EXPIRY;
    }

    public @NotNull String getSequenceId() {
        return sequenceId;
    }

    public IncompleteSequenceBehavior getIncompleteSequenceBehavior() {
        return incompleteSequenceBehavior;
    }
}
