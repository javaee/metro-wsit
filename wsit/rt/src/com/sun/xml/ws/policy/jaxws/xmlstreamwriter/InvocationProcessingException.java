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

package com.sun.xml.ws.policy.jaxws.xmlstreamwriter;


import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class InvocationProcessingException extends RuntimeException {
    public InvocationProcessingException(final String message) {
	super(message);
    }

    public InvocationProcessingException(final String message, final Throwable cause) {
        super(message, cause);
    }    

    public InvocationProcessingException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }    

    public InvocationProcessingException(final Invocation invocation) {
	super(assemblyExceptionMessage(invocation));
    }

    public InvocationProcessingException(final Invocation invocation, final Throwable cause) {
        super(assemblyExceptionMessage(invocation), cause);
    }    
    
    private static String assemblyExceptionMessage(final Invocation invocation) {
        return LocalizationMessages.WSP_1025_INVOCATION_ERROR(invocation.getMethodName(), invocation.argsToString());
    }    
}
