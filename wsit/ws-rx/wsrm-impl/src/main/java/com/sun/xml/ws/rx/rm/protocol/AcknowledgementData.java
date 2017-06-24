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
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.AckRange;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public final class AcknowledgementData {
    public static final class Builder {
        private String ackedSequenceId;
        private List<AckRange> ackedRanges;
        private String ackRequestedSequenceId;
        private boolean isFinalAcknowledgement;

        private Builder() {
        }

        private Builder(AcknowledgementData data) {
            this.ackRequestedSequenceId = data.ackRequestedSequenceId;
            this.ackedRanges = data.ackedRanges;
            this.ackedSequenceId = data.ackedSequenceId;
            this.isFinalAcknowledgement = data.isFinalAcknowledgement;
        }

        /**
         * Sets acknowledgements
         *
         * @param ackedSequenceId idnetifier of a sequence to which acknowledged message number ranges (if any) belong
         * @param acknowledgedMessageIds acknowledged ranges for the sequence identified by {@code ackSequenceId}
         * @param isFinal sets the final flag on the acknowledgement data which means that this is a final acknowledgement.
         */
        public Builder acknowledgements(@NotNull String ackedSequenceId, List<AckRange> acknowledgedMessageIds, boolean isFinal) {
            assert ackedSequenceId != null;

            this.ackedSequenceId = ackedSequenceId;
            this.ackedRanges = acknowledgedMessageIds;
            this.isFinalAcknowledgement = isFinal;

            return this;
        }

        /**
         * Sets value of AckRequested flag for the sequence associated with this message
         *
         * @param ackRequestedSequenceId value of sequence identifier for which acknowledgement is requested
         */
        public Builder ackReqestedSequenceId(@NotNull String ackRequestedSequenceId) {
            assert ackRequestedSequenceId != null;

            this.ackRequestedSequenceId = ackRequestedSequenceId;

            return this;
        }

        public AcknowledgementData build() {
            return new AcknowledgementData(ackedSequenceId, ackedRanges, ackRequestedSequenceId, isFinalAcknowledgement);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static Builder getBuilder(AcknowledgementData data) {
        return new Builder(data);
    }

    private final String ackedSequenceId;
    private final List<AckRange> ackedRanges;
    private final String ackRequestedSequenceId;
    private final boolean isFinalAcknowledgement;

    private AcknowledgementData(String ackedSequenceId, List<AckRange> ackedRanges, String ackRequestedSequenceId, boolean isFinal) {
        this.ackedSequenceId = ackedSequenceId;
        this.ackedRanges = ackedRanges;
        this.ackRequestedSequenceId = ackRequestedSequenceId;
        this.isFinalAcknowledgement = isFinal;
    }
    /**
     * Returns idnetifier of a sequence to which acknowledged message number ranges (if any) belong
     *
     * @return idnetifier of a sequence to which acknowledged message number ranges (if any) belong
     */
    public String getAcknowledgedSequenceId() {
        return this.ackedSequenceId;
    }

    /**
     * Returns acknowledged ranges for the sequence identified by acknowledged sequence identifier
     *
     * @return acknowledged ranges for the sequence identified by acknowledged sequence identifier
     */
    public @NotNull List<AckRange> getAcknowledgedRanges() {
        if (this.ackedRanges != null) {
            return this.ackedRanges;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns sequence identifier for which acknowledgement is requested
     *
     * @return value of sequence identifier for which acknowledgement is requested
     */
    public String getAckReqestedSequenceId() {
        return this.ackRequestedSequenceId;
    }

    /**
     * Returns value of the final flag which determines whether this is a final acknowledgement or not
     *
     * @return value of the final flag which determines whether this is a final acknowledgement or not
     */
    public boolean isFinalAcknowledgement() {
        return isFinalAcknowledgement;
    }

    /**
     * Returns {@code true} if the instance contains any acknowledgement data that could be sent
     * to an RM source. Otherwise returns {@code false}.
     *
     * @return {@code true} if the instance contains any acknowledgement data that could be sent
     * to an RM source. Otherwise returns {@code false}.
     */
    public boolean containsSequenceAcknowledgementData() {
        return this.ackedSequenceId != null && this.ackedRanges != null && !this.ackedRanges.isEmpty();
    }
}
