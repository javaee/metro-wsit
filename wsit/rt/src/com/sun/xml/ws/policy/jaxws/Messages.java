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

package com.sun.xml.ws.policy.jaxws;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 */
enum Messages {

    WSDL_IMPORT_FAILED,
    GET_RESOURCE_INVOCATION_FAILED,
    URL_OPEN_FAILED,
    BUFFER_CREATE_FAILED,
    READER_CREATE_FAILED,
    BUFFER_NOT_EXIST,
    FAILED_LOAD_CLASSPATH,
    FAILED_LOAD_CONTEXT,
    FAILED_CONFIGURE_WSDL_MODEL,
    FAILED_UPDATE_POLICY_MAP,
    POLICY_REFERENCE_NOT_EXIST;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String format(Object... args) {
        return MessageFormat.format(rb.getString(name()), args);
    }

    public String toString() {
        return format();
    }
}
