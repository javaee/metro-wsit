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

/*
 * TimestampValidationCallback.java
 *
 * Created on July 12, 2005, 12:54 AM
 *
 * This callback is intended for Timestamp validation.
 * A validator that implements the TimestampValidator interface should 
 * set on the callback by callback handler.
 */

package com.sun.xml.wss.impl.callback;

import com.sun.xml.wss.XWSSecurityException;

import javax.security.auth.callback.*;
import java.util.*;

/**
 *
 * @author abhijit.das@Sun.COM
 */
public class TimestampValidationCallback extends XWSSCallback implements Callback {
    
    private Request request;
    private TimestampValidator validator;
    
    
    /** Creates a new instance of TimestampValidationCallback */
    public TimestampValidationCallback(Request request) {
        this.request = request;
    }
    
    public void getResult() throws TimestampValidationException {
        validator.validate(request);
    }
    
    /**
     * The CallbackHandler handling this callbacl should set the validator.
     *
     */
    public void setValidator(TimestampValidator validator) {
        this.validator = validator;
    }
    
    public static interface Request {
        
    }
    
    public static class UTCTimestampRequest implements Request {
        private String created;
        private String expired;
        private long maxClockSkew = 0;
        private long timestampFreshnessLimit = 0;
        
        private boolean isUsernameToken = false;
    
    
        /**
         * Set it to true if the Created Timestamp present inside 
         * UsernameToken needs to be validated.
         *
         */
        public void isUsernameToken(boolean isUsernameToken) {
            this.isUsernameToken = true;
        }
    
    
        /** 
         * Check if the Timestamp Created value is coming from UsernameToken 
         * @return true if Created is inside UsernameToken else false
         */
        public boolean isUsernameToken() {
            return isUsernameToken;
        }
        
        
        /**
         * Constructor.
         *
         * @param created <code>java.lang.String</code> representaion of Creation time.
         * @param expired <code>java.lang.String</code> representation of Expiration time.
         * @param maxClockSkew representing the max time difference between sender's
         * system time and receiver's system time in milliseconds.
         * @param timestampFreshnessLimit representing the maximum time interval for nonce 
         * cache removal.
         *
         */
        public UTCTimestampRequest(String created, 
                String expired, 
                long maxClockSkew,
                long timestampFreshnessLimit) {
            this.created = created;
            this.expired = expired;
            this.maxClockSkew = maxClockSkew;
            this.timestampFreshnessLimit = timestampFreshnessLimit;
        }
        
        public String getCreated() {
            return created;
        }
        
        public String getExpired() {
            return expired;
        }
        
        public long getMaxClockSkew() {
            return maxClockSkew;
        }
        
        public long getTimestampFreshnessLimit() {
            return timestampFreshnessLimit;
        }
    }
    
    public static class TimestampValidationException extends Exception {

        public TimestampValidationException(String message) {
            super(message);
        }

        public TimestampValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    
        public TimestampValidationException(Throwable cause) {
            super(cause);
        }
    }
    
    
    public static interface TimestampValidator {
        /** 
         * Timestamp validation method.
         *
         * @throws TimestampValidationException if validation does not succeed.
         */
        public void validate(Request request) throws TimestampValidationException;
    }
}
