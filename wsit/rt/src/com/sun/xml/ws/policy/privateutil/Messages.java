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

package com.sun.xml.ws.policy.privateutil;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 */
enum Messages {

    NO_DEFAULT_CONSTRUCTOR_IN_INNER_CLASS,
    UNSUPPORTED_PROPERTY,
    INVALID_PROPERTY_VALUE,
    ILLEGAL_ENTRY,
    BROKEN_CONTEXTPATH,
    ERROR_LOADING_CLASS,
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String format(Object... args) {
        return MessageFormat.format(rb.getString(name()), args);
    }

    public String toString() {
        return format();
    }
}
                                           
