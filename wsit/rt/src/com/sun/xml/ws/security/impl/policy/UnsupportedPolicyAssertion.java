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

package com.sun.xml.ws.security.impl.policy;

/**
 *
 * RuntimeException which is thrown by security policy 
 * assertion implementation when a Invalid PolicyAssertion is found.
 *
 * Note for {@link com.sun.xml.ws.api.pipe.Pipe} implementors using
 * SecurityPolicy Assertions should catch this exception and throw
 * exceptions required by the Pipe.
 *
 * @author K.Venugopal@sun.com
 *
 */

public class UnsupportedPolicyAssertion extends java.lang.RuntimeException{
    
    /** Creates a new instance of UnsupportedPolicyAssertion */
    public UnsupportedPolicyAssertion() {
    }
    
    public UnsupportedPolicyAssertion(String msg){
        super(msg);
    }
    
    public UnsupportedPolicyAssertion(String msg , Throwable exp){
        super(msg,exp);
    }
    
    public UnsupportedPolicyAssertion(Throwable exp){
        super(exp);
    }
}
