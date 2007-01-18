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
package com.sun.xml.ws.tx.common;

import com.sun.istack.NotNull;

import javax.xml.namespace.QName;

/**
 * This serves as a base class for different kinds of ids
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.4 $
 * @since 1.0
 */
public abstract class Identifier {
    protected String value;

    /**
     * Gets the value of the value property.
     * @return the id value
     */
    @NotNull
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * @param value the non-null value
     */
    public void setValue(@NotNull final String value) {
        this.value = value;
    }

    protected abstract QName getName();
}
