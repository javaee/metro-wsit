/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.xml.ws.xmlfilter;

/**
 *
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public enum XmlStreamWriterMethodType {

    WRITE_START_DOCUMENT("writeStartDocument", true),
    WRITE_END_DOCUMENT("writeEndDocument", true),
    WRITE_START_ELEMENT("writeStartElement", true),
    WRITE_END_ELEMENT("writeEndElement", true),
    WRITE_EMPTY_ELEMENT("writeEmptyElement", true),
    WRITE_ATTRIBUTE("writeAttribute", true),
    WRITE_CHARACTERS("writeCharacters", true),
    WRITE_PROCESSING_INSTRUCTION("writeProcessingInstruction", true),
    WRITE_ENTITY_REFERENCE("writeEntityRef", true),
    WRITE_CDATA("writeCData", true),
    WRITE_COMMENT("writeComment", true),
    WRITE_DTD("writeDTD", true),
    WRITE_DEFAULT_NAMESPACE("writeDefaultNamespace", true),
    WRITE_NAMESPACE("writeNamespace", true),
    //
    GET_NAMESPACE_CONTEXT("getNamespaceContext", false),
    GET_PREFIX("getPrefix", false),
    GET_PROPERTY("getProperty", false),
    //
    SET_DEFAULT_NAMESPACE("setDefaultNamespace", true),
    SET_NAMESPACE_CONTEXT("setNamespaceContext", true),
    SET_PREFIX("setPrefix", true),
    //
    CLOSE("close", false),
    FLUSH("flush", true),
    //
    UNKNOWN("", true);

    static XmlStreamWriterMethodType getMethodType(final String methodName) {
        if (methodName != null && methodName.length() > 0) {
            for (XmlStreamWriterMethodType type : values()) {
                if (type.methodName.equals(methodName)) {
                    return type;
                }
            }
        }
        return UNKNOWN;
    }
    private String methodName;
    private boolean filterable;

    private XmlStreamWriterMethodType(String methodName, boolean isFilterable) {
        this.methodName = methodName;
        this.filterable = isFilterable;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isFilterable() {
        return filterable;
    }
}
