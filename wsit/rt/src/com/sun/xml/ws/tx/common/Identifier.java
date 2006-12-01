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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

/**
 * This serves as a base class for different kinds of ids
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.2 $
 * @since 1.0
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "value"
        })
public abstract class Identifier {
    @XmlValue
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
    public void setValue(@NotNull String value) {
        this.value = value;
    }

    protected abstract QName getName();


    /**
     * Workaround till ReferenceParameters could be JAXB element.
     * assumed to be SOAPElement now.
     * @return the id as a soap element
     */
    @NotNull
    public SOAPElement getSOAPElement() {
        SOAPElement element = null;
        try {
            element = factory.createElement(getName());
            element.setTextContent(value);
        } catch (SOAPException e) {
            // TODO: report/log exception
        }
        return element;
    }

    private static SOAPFactory factory;

    static {
        try {
            factory = SOAPFactory.newInstance();
        } catch (SOAPException se) {
            // TODO log/report exception
        }
    }
}
