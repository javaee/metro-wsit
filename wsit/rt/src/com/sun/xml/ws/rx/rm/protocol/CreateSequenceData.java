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
import com.sun.istack.Nullable;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.IncompleteSequenceBehavior;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import javax.xml.ws.EndpointReference;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class CreateSequenceData {
    public static class Builder {
        private @NotNull final EndpointReference acksToEpr;
        private long duration;
        private @Nullable SecurityTokenReferenceType strType;
        private @Nullable String offeredSequenceId;
        private long offeredSequenceExpiry;
        private IncompleteSequenceBehavior offeredSequenceIncompleteBehavior;

        private Builder(EndpointReference acksToEpr) {
            this.acksToEpr = acksToEpr;
            this.duration = Sequence.NO_EXPIRY;
            this.offeredSequenceExpiry = Sequence.NO_EXPIRY;
            this.offeredSequenceIncompleteBehavior = IncompleteSequenceBehavior.getDefault();
        }

        public void duration(long expiry) {
            this.duration = expiry;
        }

        public Builder strType(SecurityTokenReferenceType value) {
            this.strType = value;

            return this;
        }

        public void offeredSequenceExpiry(long offeredSequenceExpiry) {
            this.offeredSequenceExpiry = offeredSequenceExpiry;
        }


        public Builder offeredInboundSequenceId(String value) {
            this.offeredSequenceId = value;

            return this;
        }

        public void offeredSequenceIncompleteBehavior(IncompleteSequenceBehavior value) {
            this.offeredSequenceIncompleteBehavior = value;
        }

        public CreateSequenceData build() {
            return new CreateSequenceData(acksToEpr, duration, strType, offeredSequenceId, offeredSequenceExpiry, offeredSequenceIncompleteBehavior);
        }
    }

    public static Builder getBuilder(EndpointReference acksToEpr) {
        return new Builder(acksToEpr);
    }

    private @NotNull final EndpointReference acksToEpr;
    private final long duration;
    private @Nullable final String offeredSequenceId;
    private final long offeredSequenceExpiry;
    private @Nullable final SecurityTokenReferenceType strType;
    private @NotNull final IncompleteSequenceBehavior offeredSequenceIncompleteBehavior;

    private CreateSequenceData(
            @NotNull EndpointReference acksToEpr,
            @Nullable long exipry,
            @Nullable SecurityTokenReferenceType strType,
            @Nullable String offeredSequenceId,
            @Nullable long offeredSequenceExpiry,
            @NotNull IncompleteSequenceBehavior offeredSequenceIncompleteBehavior) {
        this.acksToEpr = acksToEpr;
        this.duration = exipry;
        this.offeredSequenceId = offeredSequenceId;
        this.offeredSequenceExpiry = offeredSequenceExpiry;
        this.strType = strType;
        this.offeredSequenceIncompleteBehavior = offeredSequenceIncompleteBehavior;
    }

    public @NotNull EndpointReference getAcksToEpr() {
        return acksToEpr;
    }

    public long getDuration() {
        return duration;
    }

    public boolean doesNotExpire() {
        return duration == Sequence.NO_EXPIRY;
    }

    public @Nullable SecurityTokenReferenceType getStrType() {
        return strType;
    }

    public @Nullable String getOfferedSequenceId() {
        return offeredSequenceId;
    }

    public long getOfferedSequenceExpiry() {
        return offeredSequenceExpiry;
    }

    public boolean offeredSequenceDoesNotExpire() {
        return offeredSequenceExpiry == Sequence.NO_EXPIRY;
    }

    public IncompleteSequenceBehavior getOfferedSequenceIncompleteBehavior() {
        return offeredSequenceIncompleteBehavior;
    }
}
