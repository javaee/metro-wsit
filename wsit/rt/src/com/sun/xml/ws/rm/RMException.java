/*
 * $Id: RMException.java,v 1.1 2006-05-03 22:56:35 arungupta Exp $
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

package com.sun.xml.ws.rm;
/**
 * Wrapper class for exceptions thrown by RM Methods.
 */
public class RMException extends Exception {

    private final com.sun.xml.ws.api.message.Message faultMessage;

    public RMException() {
        this.faultMessage = null;
    }

    public RMException(String str) {
        super(str);
        this.faultMessage = null;
    }

    public RMException(Throwable t) {
        super(t);
        this.faultMessage = null;
    }


     public RMException(com.sun.xml.ws.api.message.Message e) {
         this.faultMessage= e;
     }
    /**
     * Returns a Message containign a Fault defined by WS-RM.
     *
     * @return The Fault message
     *          null if there is no mapped Fault message
     */
    public com.sun.xml.ws.api.message.Message getFaultMessage() {
        return faultMessage;

    }



}
