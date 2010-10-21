/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
        if (validator == null) {
            throw new TimestampValidationException("A Required TimestampValidator object was not set by the CallbackHandler");
        }
        validator.validate(request);
    }
    
    /**
     * The CallbackHandler handling this callbacl should set the validator.
     *
     */
    public void setValidator(TimestampValidator validator) {
        this.validator = validator;
         if (this.validator instanceof ValidatorExtension) {
            ((ValidatorExtension)this.validator).setRuntimeProperties(this.getRuntimeProperties());
        }
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
