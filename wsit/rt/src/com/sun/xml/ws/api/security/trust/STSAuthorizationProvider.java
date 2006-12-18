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

package com.sun.xml.ws.api.security.trust;

import javax.security.auth.Subject;

/**
 * <p>
 * This interface is a plugin for authorization services to a Security Token Service (STS).
 * The authorization service determines if a requestor can be issued an token to access the target 
 * service. 
 * </p>
 @author Jiandong Guo
 */
public interface STSAuthorizationProvider {
    
    /**
     * Returns true if the requestor identified by the <code>Subject</code> can access the the target
     * service.
     * @param subject The <code>Subject</code> contgaining authentication information and context of the 
     *                authenticated requestor.
     * @param appliesTo Identifying target service(s) 
     * @param tokenType Type of token to be issued.
     * @param keyType Type of key to be issued
     * @return true ot false.
     */  
    boolean isAuthorized(Subject subject, String appliesTo, String tokenType, String keyType);
}
