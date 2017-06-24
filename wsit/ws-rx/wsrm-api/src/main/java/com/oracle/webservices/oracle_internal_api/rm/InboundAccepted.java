/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.webservices.oracle_internal_api.rm;

import com.oracle.webservices.api.message.BasePropertySet;

/**
 * {@code InboundAccepted} is created by the RMD.
 *
 * <p>It is passed as a
 * {@link com.oracle.webservices.api.message.PropertySet} to
 * {@link com.oracle.webservices.api.disi.ProviderRequest#request}.</p>
 *
 */
public abstract class InboundAccepted
    extends BasePropertySet
{
    // ----------------------------------------------------------------------
    /**
     * Key for accepted property
     *
     * @see #getAccepted
     * @see #setAccepted
     */
    public static final String ACCEPTED_PROPERTY = "com.oracle.webservices.api.rm.inbound.accepted.accepted";

    /**
     * @return the value set via {@link #setAccepted} or {@code null}
     * if {@link #setAccepted} has not been called or if the call to
     * {@link #setAccepted} resulted in {@link InboundAcceptedAcceptFailed}
     * being thrown.
     *
     * @see #ACCEPTED_PROPERTY
     * @see #setAccepted
     */
    @Property(ACCEPTED_PROPERTY)
    public abstract Boolean getAccepted();

    /**
     * <p>When the user determines that the message has been delivered to them then they call {@code #setAccepted(true)}.</p>
     *
     * <p>The RMD will <em>not</em> acknowledge the message to the RMS until {@code #setAccepted(true)} is called.</p>
     *
     * <p>If the user calls {@code #setAccepted(false)} then the RMD will not
     * acknowledge the delivery of this particular request.  Note: if the
     * RMS sends a retry, that is considered a new request and the
     * delivery/acceptance process starts anew.</p>
     *
     * <p>If the user calls {@code #setAccepted(false)} and an atomic
     * transaction is being used to handle the message, then that
     * transaction will be rolled back.</p>
     *
     * @throws {@link InboundAcceptedAcceptFailed}
     *     If the user calls {@code #accepted(true)} but the RMD is
     *     not able to internally record the message as delivered
     *     (e.g., an atomic transaction fails to commit) then this
     *     exception is thrown.
     *
     * @see #ACCEPTED_PROPERTY
     * @see #getAccepted
     */
    public abstract void setAccepted(Boolean accept) throws InboundAcceptedAcceptFailed;


    // ----------------------------------------------------------------------
    /**
     * Key for inbound RM sequence id
     *
     * @see  #getRMSequenceId
     */
    public static final String RM_SEQUENCE_ID_PROPERTY = "com.oracle.webservices.api.rm.inbound.accepted.rm.sequence.id";

    /**
     * @return The RM sequence id associated with the message.
     *     Note: it may be {@code null} if RM is not enabled.
     *
     * @see #RM_SEQUENCE_ID_PROPERTY
     */
    @Property(RM_SEQUENCE_ID_PROPERTY)
    public abstract String getRMSequenceId();


    // ----------------------------------------------------------------------
    /**
     * Key for inbound RM message number
     *
     * @see  #getRMMessageNumber
     */
    public static final String RM_MESSAGE_NUMBER_PROPERTY = "com.oracle.webservices.api.rm.inbound.accepted.rm.message.number";

    /**
     * @return The RM message number associated with the message.
     *     Note: it may be {@code -1} if RM is not enabled.
     *
     * @see #RM_MESSAGE_NUMBER_PROPERTY
     */
    @Property(RM_MESSAGE_NUMBER_PROPERTY)
    public abstract long getRMMessageNumber();


    ////////////////////////////////////////////////////
    //
    // PropertySet boilerplate
    //

    private static final PropertyMap model;

    static {
        model = parse(InboundAccepted.class);
    }

    @Override
    protected PropertyMap getPropertyMap() {
        return model;
    }
}

// End of file.
