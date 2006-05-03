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

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.PolicyException;
import java.net.URI;

/**
 *
 * @author Marek Potociar
 */
public class PolicyModelAccessException extends PolicyException {
    
    public PolicyModelAccessException(URI modelUri) {
        super(PolicyModelAccessException.createMessage(modelUri, null));
    }
    
    
    public PolicyModelAccessException(URI modelUri, String reason) {
        super(PolicyModelAccessException.createMessage(modelUri, reason));
    }
    
    public PolicyModelAccessException(URI modelUri, String reason, Throwable cause) {
        super(PolicyModelAccessException.createMessage(modelUri, reason), cause);
    }
    
    
    public PolicyModelAccessException(URI modelUri, Throwable cause) {
        super(PolicyModelAccessException.createMessage(modelUri, null), cause);
    }
    
    private static String createMessage(URI modelUri, String reason) {
        StringBuffer buffer = new StringBuffer("Unable to access policy source model identified by URI: '");
        buffer.append(modelUri.toString()).append('\'');
        
        if (reason != null && reason.length() > 0) {
            buffer.append(" Detailed reason: '").append(reason).append('\'');
        }        
        
        return buffer.toString();
    }
}
