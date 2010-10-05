/*
 * $Id: PropertyCallback.java,v 1.1 2010-10-05 11:54:12 m_potociar Exp $
 */

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

package com.sun.xml.wss.impl.callback;

import javax.security.auth.callback.Callback;
                                                                                                         
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
