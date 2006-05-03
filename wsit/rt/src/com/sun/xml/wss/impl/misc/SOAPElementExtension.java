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

package com.sun.xml.wss.impl.misc;

import javax.xml.soap.*;
import javax.xml.namespace.QName;

import java.util.Iterator;

public class SOAPElementExtension {

    public SOAPElement addChildElement(QName qname) throws SOAPException {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }
    public SOAPElement addAttribute(QName qname, String value)
        throws SOAPException {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }
    public String getAttributeValue(QName qname) {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }
    public QName createQName(String localName, String prefix)
        throws SOAPException {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    public QName getElementQName() {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    public SOAPElement setElementQName(QName newName) throws SOAPException {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }
    public boolean removeAttribute(QName qname) {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }
    public Iterator getChildElements(QName qname) {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

}
