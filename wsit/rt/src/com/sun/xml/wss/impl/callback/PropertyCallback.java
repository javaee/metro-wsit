/*
 * $Id: PropertyCallback.java,v 1.1 2006-05-03 22:57:43 arungupta Exp $
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

package com.sun.xml.wss.impl.callback;

import javax.security.auth.callback.Callback;
import java.util.Properties;
                                                                                                         
/**
 * This callback is an optional callback that can be handled by an
 * implementation of CallbackHandler to specify the values of properties
 * configurable with XWS-Security runtime. The properties are:
 *
 * <ul><li>MAX_CLOCK_SKEW  : The assumed maximum skew (milliseconds) between the local times of any two systems </li>
 * <li>TIMESTAMP_FRESHNESS_LIMIT : The period (milliseconds) for which a Timestamp is considered fresh </li>
 * <li>MAX_NONCE_AGE   : The length of time (milliseconds) a previously received Nonce value will be stored </li></ul>
 * @deprecated This callback is no longer supported by the XWS-Security runtime, use the XWS-Security configuration
 * file to set the above property values instead.
 */
public class PropertyCallback extends XWSSCallback implements Callback {
   
    public static final long MAX_NONCE_AGE = 900000 ;
    public static final long MAX_CLOCK_SKEW = 60000;
    public static final long TIMESTAMP_FRESHNESS_LIMIT = 300000;

    long maxSkew = MAX_CLOCK_SKEW;
    long freshnessLimit = TIMESTAMP_FRESHNESS_LIMIT;
    long maxNonceAge = MAX_NONCE_AGE;
                                                                                                         
    /**
     *@param skew the assumed maximum skew (milliseconds) between the local times of any two systems
     */
    public void setMaxClockSkew(long skew) {
        this.maxSkew = skew;
    }
   
    /**
     *@return the maximum clock skew
     */
    public long getMaxClockSkew() {
        return maxSkew;
    }

    /**
     *@param freshnessLimit the period (milliseconds) for which a Timestamp is considered fresh
     */
    public void setTimestampFreshnessLimit(long freshnessLimit) {
        this.freshnessLimit = freshnessLimit;
    }

    /**
     *@return the Timestamp Freshness Limit
     */
    public long getTimestampFreshnessLimit() {
        return freshnessLimit;
    }

    /**
     *@param maxNonceAge The length of time (milliseconds) a previously received Nonce value 
     *will be stored
     * Implementation Note: The actual time for which any Nonce will be stored can be greater
     * than maxNonceAge. In some cases when the implementation is unable to determine a receiver
     * side policy ahead of processing the Message, the maxNonceAge value used will be a default
     * value of 30 mins.
     */
    public void setMaxNonceAge(long maxNonceAge) {
        this.maxNonceAge = maxNonceAge;
    }

    /**
     *@return the Maximum Nonce Age value 
     */
    public long getMaxNonceAge() {
        return this.maxNonceAge;
    }

}
