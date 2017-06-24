/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.rx.rm.protocol;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class TerminateSequenceResponseData {
    public static class Builder {

        private @NotNull final String sequenceId;
        private @Nullable String boundSequenceId;
        private long boundSequenceLastMessageId;
        private @Nullable AcknowledgementData acknowledgementData;

        public Builder(@NotNull String sequenceId) {
            this.sequenceId = sequenceId;
        }

        public Builder acknowledgementData(@Nullable AcknowledgementData acknowledgementData) {
            this.acknowledgementData = acknowledgementData;

            return this;
        }

        public Builder boundSequenceData(String sequenceId, long lastMessageId) {
            this.boundSequenceId = sequenceId;
            this.boundSequenceLastMessageId = lastMessageId;

            return this;
        }

        public TerminateSequenceResponseData build() {
            return new TerminateSequenceResponseData(sequenceId, boundSequenceId, boundSequenceLastMessageId, acknowledgementData);
        }
    }

    public static Builder getBuilder(String sequenceId) {
        return new Builder(sequenceId);
    }

    private @NotNull final String sequenceId;
    private @Nullable final String boundSequenceId;
    private final long boundSequenceLastMessageId;
    private @Nullable final AcknowledgementData acknowledgementData;

    private TerminateSequenceResponseData(
            @NotNull String sequenceId,
            @Nullable String boundSequenceId,
            long boundSequenceLastMessageId,
            @Nullable AcknowledgementData acknowledgementData) {
        this.sequenceId = sequenceId;
        this.boundSequenceId = boundSequenceId;
        this.boundSequenceLastMessageId = boundSequenceLastMessageId;
        this.acknowledgementData = acknowledgementData;
    }

    public @NotNull String getSequenceId() {
        return sequenceId;
    }

    public @Nullable AcknowledgementData getAcknowledgementData() {
        return acknowledgementData;
    }

    public @Nullable String getBoundSequenceId() {
        return boundSequenceId;
    }

    public long getBoundSequenceLastMessageId() {
        return boundSequenceLastMessageId;
    }
}
