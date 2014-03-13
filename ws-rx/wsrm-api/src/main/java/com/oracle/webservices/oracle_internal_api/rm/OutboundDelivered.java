/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.webservices.oracle_internal_api.rm;

import com.oracle.webservices.api.message.BasePropertySet;

/**
 * {@code OutboundDelivered} is created by a user of client-side (i.e., RMS) RM.
 *
 * <p>It is passed as a
 * {@link com.oracle.webservices.api.message.PropertySet} to
 * {@link com.oracle.webservices.api.disi.DispatcherRequest#request}.</p>
 *
 */
public abstract class OutboundDelivered
    extends BasePropertySet
{
    // ----------------------------------------------------------------------
    /**
     * Key for delivered property
     *
     * @see #getDelivered
     * @see #setDelivered
     */
    public static final String DELIVERED_PROPERTY = "com.oracle.webservices.api.rm.outbound.delivered.delivered";

    /**
     * @return The value set by {@link #setDelivered} or {@code null}
     * if {@link #setDelivered} has not been called.
     *
     * @see #DELIVERED_PROPERTY
     * @see #setDelivered
     */
    @Property(DELIVERED_PROPERTY)
    public abstract Boolean getDelivered();

    /**
     * <p>When the RMS receives an ACK from the RMD for the request message instance
     * that contains this {@code com.oracle.webserivces.api.message.Property},
     * then the RMS will call {@code #delivered(true)}.</p>
     *
     * <p>If max retries, timeouts or
     * {@code com.oracle.webservices.api.disi.ClientResponseTransport#fail} is called
     * with an non {@code RMRetryException} exception, then the RMS calls
     * {@code #delivered(false)}.
     *
     * @see #DELIVERED_PROPERTY
     * @see #getDelivered
     */
    public abstract void setDelivered(Boolean accept);


    // ----------------------------------------------------------------------
    /**
     * Key for message identity property
     *
     * @see  #getMessageIdentity
     */
    public static final String MESSAGE_IDENTITY_PROPERTY = "com.oracle.webservices.api.rm.outbound.delivered.message.identity";

    /**
     * @return The identity of the message.
     *
     * @throws RuntimeException if String is longer than 256 characters.
     *
     * @see #MESSAGE_IDENTITY_PROPERTY
     */
    @Property(MESSAGE_IDENTITY_PROPERTY)
    public abstract String getMessageIdentity();


    ////////////////////////////////////////////////////
    //
    // PropertySet boilerplate
    //

    private static final PropertyMap model;

    static {
        model = parse(OutboundDelivered.class);
    }

    @Override
    protected PropertyMap getPropertyMap() {
        return model;
    }
}

// End of file.
