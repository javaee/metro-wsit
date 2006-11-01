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

package com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter;

/**
 *
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public enum XmlStreamWriterMethodType {
    WRITE_START_ELEMENT("writeStartElement"),
    WRITE_END_ELEMENT("writeEndElement"),
    WRITE_ATTRIBUTE("writeAttribute"),
    WRITE_CHARACTERS("writeCharacters"),
    CLOSE("close"),
    OTHER(null);
    
    private static final XmlStreamWriterMethodType[] types = new XmlStreamWriterMethodType[] {
        WRITE_START_ELEMENT,
        WRITE_END_ELEMENT,
        WRITE_ATTRIBUTE,
        WRITE_CHARACTERS,
        CLOSE
    };
    public static XmlStreamWriterMethodType getMethodType(String methodName) {
        for (XmlStreamWriterMethodType type : types) {
            if (type.methodName.equals(methodName)) {
                return type;
            }
        }
        return OTHER;
    }
    
    private String methodName;
    
    private XmlStreamWriterMethodType(String methodName) {
        this.methodName = methodName;
    }
}
