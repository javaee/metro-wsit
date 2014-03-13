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

// GENERATED CODE.  DO NOT EDIT.
// GENERATED FROM reliable-messaging-internap-api-properties.xml

package com.oracle.webservices.oracle_internal_api.rm;

// BEGIN MANUAL EDIT:
// import com.oracle.webservices.api.FeatureValidator;
// END MANUAL EDIT:

import javax.xml.ws.WebServiceFeature;

/**
 * Configure WS-ReliableMessaging.
 */
public class ReliableMessagingFeature
    extends WebServiceFeature
{
    private void setEnabled(final boolean x) { enabled = x; }

    /**
     * 
       Specifies the period (in milliseconds) of a sequence manager maintenance task execution.
       Default value: 60000
     */
    public long getSequenceManagerMaintenancePeriod() { return sequenceManagerMaintenancePeriod; }
    private long sequenceManagerMaintenancePeriod = 60000;
    private void setSequenceManagerMaintenancePeriod(final long x) { sequenceManagerMaintenancePeriod = x; }

    /**
     * 
       Specifies how many concurrently active RM sessions (measured based on
       inbound RM sequences) the sequence manager dedicated to the WS Endpoint
       accepts before starting to refuse new requests for sequence creation.
       Default value: 100
     */
    public int getMaxConcurrentSession() { return maxConcurrentSession; }
    private int maxConcurrentSession = 100;
    private void setMaxConcurrentSession(final int x) { maxConcurrentSession = x; }

    /**
     * 
       When an XA TX is used to link the RMD with the user's ProviderRequest, this specifies the timeout value in seconds.
       The default value of 0 says to use the system default.
     */
    public int getUserTransactionTimeout() { return userTransactionTimeout; }
    private int userTransactionTimeout = 0;
    private void setUserTransactionTimeout(final int x) { userTransactionTimeout = x; }

    public static final String ID = "com.oracle.webservices.oracle_internal_api.rm.ReliableMessagingFeature";
    public String getID() { return ID; }

    public static       String getSeedPolicyName() { return "oracle/reliable_messaging_internal_api_policy"; }

    protected static final long serialVersionUID = 1523241975L; // TBD

    private ReliableMessagingFeature() { enabled = true; }
    public static Builder builder() { return new Builder(new ReliableMessagingFeature()); }

    /**
     * Configure WS-ReliableMessaging.
     */
    public final static class Builder {
        final private ReliableMessagingFeature o;
        Builder(final ReliableMessagingFeature x) { o = x; }
        // BEGIN MANUAL EDIT:
        //public ReliableMessagingFeature build() { return (ReliableMessagingFeature) FeatureValidator.validate(o); }
        public ReliableMessagingFeature build() { return o; }
        // END MANUAL EDIT:
        public Builder enabled(final boolean x) { o.setEnabled(x); return this; }
        public Builder sequenceManagerMaintenancePeriod(final long x) { o.setSequenceManagerMaintenancePeriod(x); return this; }
        public Builder maxConcurrentSession(final int x) { o.setMaxConcurrentSession(x); return this; }
        public Builder userTransactionTimeout(final int x) { o.setUserTransactionTimeout(x); return this; }
    }

    @Override
    public String toString() {
        return "["
               + getID() + ", "
               + "enabled=" + enabled + ", "
               + "sequenceManagerMaintenancePeriod=" + sequenceManagerMaintenancePeriod + ", "
               + "maxConcurrentSession=" + maxConcurrentSession + ", "
               + "userTransactionTimeout=" + userTransactionTimeout
               + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ReliableMessagingFeature that = (ReliableMessagingFeature) o;

        if (! getID().equals(that.getID())) return false;
        if (enabled != that.enabled) return false;
        if (sequenceManagerMaintenancePeriod != that.sequenceManagerMaintenancePeriod) return false;
        if (maxConcurrentSession != that.maxConcurrentSession) return false;
        if (userTransactionTimeout != that.userTransactionTimeout) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 3;
        result = 31 * result + getID().hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + ((int) (sequenceManagerMaintenancePeriod ^ (sequenceManagerMaintenancePeriod >>> 32)));
        result = 31 * result + (maxConcurrentSession);
        result = 31 * result + (userTransactionTimeout);
        return result;
    }
}
