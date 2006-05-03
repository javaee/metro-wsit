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
 * SCSessionManagerFactory.java
 *
 * Created on February 1, 2006, 2:15 PM
 */

package com.sun.xml.ws.security.secconv;

/**
 *
 * @author Jiandong Guo
 */
public abstract class SCSessionManagerFactory {
    
    
    private static SCSessionManagerFactory fac = null;
    
    public static SCSessionManagerFactory newInstance(){
        if (fac == null){
            fac = new SCSessionManagerFactoryImpl();
        }
        
        return fac;
    }
    
    public abstract SCSessionManager getSessionManager();
    
}
