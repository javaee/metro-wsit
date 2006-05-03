/*
 * $Id: TimestampPolicy.java,v 1.1 2006-05-03 22:57:56 arungupta Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.wss.impl.MessageConstants;
import java.util.Date;
import java.text.SimpleDateFormat;

import com.sun.xml.wss.impl.PolicyTypeUtil;

/**
 * A policy representing a WSS Timestamp element.
 * Note: The TimestampPolicy is the only WSSPolicy element that does not contain a
 * concrete FeatureBinding and/or KeyBinding.
 */
public class TimestampPolicy extends WSSPolicy {
    
    /*
     * Feature Bindings
     * Key Bindings
     */
    
    private String creationTime     = MessageConstants._EMPTY;
    private String expirationTime   = MessageConstants._EMPTY;
    
    private long timeout = 300000;
    private long maxClockSkew = 1200000;
    private long timestampFreshness = 1200000;
    
    static SimpleDateFormat formatter  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'");
    
    /**
     *Default constructor
     */
    public TimestampPolicy() {
        setPolicyIdentifier(PolicyTypeUtil.TIMESTAMP_POLICY_TYPE);
    }
    
    /**
     * set the CreationTime for the timestamp in this TimestampPolicy
     * @param creationTime
     */
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }
    
    /**
     * If the current time on a receiving system is past the CreationTime of the timestamp plus the
     * timeout, then the timestamp is to be considered expired.
     * @param timeout the number of milliseconds after which the Timestamp in this
     * TimestampPolicy will expire.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    
    /**
     * set the maximum clock skew adjustment value (in milliseconds)
     * @param maxClockSkew the Maximum Clock Skew adjustment to be used
     * when validating received timestamps
     */
    public void setMaxClockSkew(long maxClockSkew) {
        this.maxClockSkew = maxClockSkew;
    }
    
    /**
     * set the Timestamp Freshness Limit (in milliseconds) for this Timestamp
     * Timestamps received by a receiver with creation Times older than
     * the Timestamp Freshness Limit period are supposed to be rejected by the receiver.
     * @param timestampFreshness the Timestamp Freshness Limit (in milliseconds)
     */
    public void setTimestampFreshness(long timestampFreshness) {
        this.timestampFreshness = timestampFreshness;
    }
    
    /**
     * @return creationTime the creation time of the timestamp in this
     * TimestampPolicy if set, empty string otherwise
     */
    public String getCreationTime() {
        return this.creationTime;
    }
    
    /**
     * @return timeout the Timeout in milliseconds for this Timestamp
     */
    public long getTimeout() {
        return this.timeout;
    }
    
    /**
     * @return expirationTime the expiration time if set for this Timestamp, empty string otherwise
     */
    public String getExpirationTime() throws Exception {
        if (expirationTime.equals("") && timeout != 0 && !creationTime.equals("")) {
            try {
                synchronized(formatter) {
                    expirationTime = new Long(
                            ((Date) formatter.parse(creationTime)).getTime() + timeout).toString();
                }
            } catch (Exception e) {
                synchronized(formatter1) {
                    expirationTime = new Long(
                            ((Date) formatter1.parse(creationTime)).getTime() + timeout).toString();
                }
            }
        }
        
        return this.expirationTime;
    }
    
    /**
     * @param expirationTime the expiration time
     */
    public void setExpirationTime(String expirationTime) {
        this.expirationTime= expirationTime;
    }
    
    /**
     * @return maxClockSkew the maximum Clock Skew adjustment
     */
    public long getMaxClockSkew() {
        return this.maxClockSkew;
    }
    
    /**
     * @return timeStampFreshness limit
     */
    public long getTimestampFreshness() {
        return this.timestampFreshness;
    }
    
    /**
     * @param policy the policy to be compared for equality
     * @return true if the argument policy is equal to this
     */
    public boolean equals(WSSPolicy policy) {
        
        boolean assrt = false;
        
        try {
            TimestampPolicy tPolicy = (TimestampPolicy) policy;
            boolean b1 = creationTime.equals("") ? true : creationTime.equalsIgnoreCase(tPolicy.getCreationTime());
            boolean b2 = getExpirationTime().equals("") ? true : getExpirationTime().equalsIgnoreCase(tPolicy.getExpirationTime());
            assrt = b1 && b2;
        } catch (Exception e) {}
        
        return assrt;
    }
    
    /*
     * Equality comparision ignoring the Targets
     * @param policy the policy to be compared for equality
     * @return true if the argument policy is equal to this
     */
    public boolean equalsIgnoreTargets(WSSPolicy policy) {
        return equals(policy);
    }
    
    /**
     * Clone operator
     * @return clone of this policy
     */
    public Object clone(){
        TimestampPolicy tPolicy = new TimestampPolicy();
        
        try {
            tPolicy.setTimeout(timeout);
            tPolicy.setCreationTime(creationTime);
            tPolicy.setExpirationTime(expirationTime);
            tPolicy.setMaxClockSkew(maxClockSkew);
            tPolicy.setTimestampFreshness(timestampFreshness);
        } catch (Exception e) {}
        
        return tPolicy;
    }
    
    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.TIMESTAMP_POLICY_TYPE;
    }
    
}

