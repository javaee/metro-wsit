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

package com.sun.xml.ws.policy.spi;

import com.sun.xml.ws.policy.*;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;

/**
 * Exception thrown in case of assertion creation failure.
 *
 * @author Marek Potociar
 */
public final class AssertionCreationException extends PolicyException {
    
    private AssertionData assertionData; 
    
    /**
     * Constructs a new assertion creation exception with the specified detail message and cause.  
     * <p/>
     * Note that the detail message associated with {@code cause} is <emph>not</emph> automatically incorporated in
     * this exception's detail message.
     *
     * @param assertionData the data provided for assertion creation
     * @param  message the detail message.
     */
    public AssertionCreationException(AssertionData assertionData, String message) {
        super(message);
        this.assertionData = assertionData;
    }
    
    /**
     * Constructs a new assertion creation exception with the specified detail message and cause.  
     * <p/>
     * Note that the detail message associated with {@code cause} is <emph>not</emph> automatically incorporated in
     * this exception's detail message.
     *
     * @param assertionData the data provided for assertion creation
     * @param  message the detail message.
     * @param  cause the cause.  (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public AssertionCreationException(AssertionData assertionData, String message, Throwable cause) {
        super(message, cause);
        this.assertionData = assertionData;
    }
    
    /**
     * Constructs a new assertion creation exception with the specified detail message and cause.  
     *
     * @param assertionData the data provided for assertion creation
     * @param  cause the cause.  (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public AssertionCreationException(AssertionData assertionData, Throwable cause) {
        super(cause);
        this.assertionData = assertionData;
    }
    
    /**
     * Retrieves assertion data associated with the exception.
     *
     * @return associated assertion data (present when assertion creation failed raising this exception).
     */
    public AssertionData getAssertionData() {
        return this.assertionData;
    }
}
