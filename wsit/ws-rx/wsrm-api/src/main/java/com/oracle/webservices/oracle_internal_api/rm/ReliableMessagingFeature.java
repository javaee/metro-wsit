/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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
       Specifies the period (in seconds) of a sequence manager maintenance task execution.
       Default value: 60
     */
    public int getSequenceManagerMaintenancePeriod() { return sequenceManagerMaintenancePeriod; }
    private int sequenceManagerMaintenancePeriod = 60;
    private void setSequenceManagerMaintenancePeriod(final int x) { sequenceManagerMaintenancePeriod = x; }

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
        public Builder sequenceManagerMaintenancePeriod(final int x) { o.setSequenceManagerMaintenancePeriod(x); return this; }
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
        result = 31 * result + (sequenceManagerMaintenancePeriod);
        result = 31 * result + (maxConcurrentSession);
        result = 31 * result + (userTransactionTimeout);
        return result;
    }
}
