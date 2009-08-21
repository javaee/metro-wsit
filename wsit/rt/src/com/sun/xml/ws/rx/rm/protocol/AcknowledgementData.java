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

        private Builder() {
        }

        /**
         * Sets acknowledgements
         *
         * @param ackedSequenceId idnetifier of a sequence to which acknowledged message number ranges (if any) belong
         * @param acknowledgedMessageIds acknowledged ranges for the sequence identified by {@code ackSequenceId}
         */
        public void acknowledgements(@NotNull String ackedSequenceId, List<AckRange> acknowledgedMessageIds) {
            assert ackedSequenceId != null;

            this.ackedSequenceId = ackedSequenceId;
            this.ackedRanges = acknowledgedMessageIds;
        }

        /**
         * Sets value of AckRequested flag for the sequence associated with this message
         *
         * @param ackRequestedSequenceId value of sequence identifier for which acknowledgement is requested
         */
        public void ackReqestedSequenceId(String ackRequestedSequenceId) {
            this.ackRequestedSequenceId = ackRequestedSequenceId;
        }

        public AcknowledgementData build() {
            return new AcknowledgementData(ackedSequenceId, ackedRanges, ackRequestedSequenceId);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    private String ackedSequenceId;
    private List<AckRange> ackedRanges;
    private String ackRequestedSequenceId;

    private AcknowledgementData(String ackedSequenceId, List<AckRange> ackedRanges, String ackRequestedSequenceId) {
        this.ackedSequenceId = ackedSequenceId;
        this.ackedRanges = ackedRanges;
        this.ackRequestedSequenceId = ackRequestedSequenceId;
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

    public boolean containsSequenceAcknowledgementData() {
        return this.ackedSequenceId != null && this.ackedRanges != null && !this.ackedRanges.isEmpty();
    }
}
