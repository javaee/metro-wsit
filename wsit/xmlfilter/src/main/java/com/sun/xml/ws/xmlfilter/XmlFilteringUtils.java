/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.xmlfilter.localization.LocalizationMessages;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class XmlFilteringUtils {
    public static final class AttributeInfo {
        private final QName name;
        private final String value;
        
        AttributeInfo(QName name, String value) {
            this.name = name;
            this.value = value;
        }

        public QName getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
    
    private static final Logger LOGGER = Logger.getLogger(XmlFilteringUtils.class);
   
    /** 
     * Prevents creation of a new instance of XmlFilteringUtils 
     */
    private XmlFilteringUtils() {
        // nothing to initialize
    }
    
    public static String getDefaultNamespaceURI(final XMLStreamWriter writer) {
        return writer.getNamespaceContext().getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
    }
    
    public static QName getElementNameToWrite(final Invocation invocation, final String defaultNamespaceURI) {
        checkInvocationParameter(invocation, XmlStreamWriterMethodType.WRITE_START_ELEMENT);
        
        /**
         * void writeStartElement(String localName)
         * void writeStartElement(String namespaceURI, String localName)
         * void writeStartElement(String prefix, String localName, String namespaceURI)
         */
        final int argumentsCount = invocation.getArgumentsCount();
        final String namespaceURI;
        final String localName;
        
        switch (argumentsCount) {
            case 1:
                namespaceURI = defaultNamespaceURI;
                localName = invocation.getArgument(0).toString();
                break;
            case 2:
                namespaceURI = invocation.getArgument(0).toString();
                localName = invocation.getArgument(1).toString();
                break;
            case 3:
                localName = invocation.getArgument(1).toString();
                namespaceURI = invocation.getArgument(2).toString();
                break;
            default:
                throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.XMLF_5003_UNEXPECTED_ARGUMENTS_COUNT(XmlStreamWriterMethodType.WRITE_START_ELEMENT + "(...)", argumentsCount)));
        }
        
        return new QName(namespaceURI, localName);
    }
    
    public static AttributeInfo getAttributeNameToWrite(final Invocation invocation, final String defaultNamespaceURI) {
        checkInvocationParameter(invocation, XmlStreamWriterMethodType.WRITE_ATTRIBUTE);
        
        /*
         * void writeAttribute(String localName, String value)
         * void writeAttribute(String namespaceURI, String localName, String value)
         * void writeAttribute(String prefix, String namespaceURI, String localName, String value)
         */
        final int argumentsCount = invocation.getArgumentsCount();
        String namespaceURI, localName, value;
        
        switch (argumentsCount) {
            case 2:
                namespaceURI = defaultNamespaceURI;
                localName = invocation.getArgument(0).toString();
                value = invocation.getArgument(1).toString();
                break;
            case 3:
                namespaceURI = invocation.getArgument(0).toString();
                localName = invocation.getArgument(1).toString();
                value = invocation.getArgument(2).toString();
                break;
            case 4:
                namespaceURI = invocation.getArgument(1).toString();
                localName = invocation.getArgument(2).toString();
                value = invocation.getArgument(3).toString();
                break;
            default:
                throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.XMLF_5003_UNEXPECTED_ARGUMENTS_COUNT(XmlStreamWriterMethodType.WRITE_ATTRIBUTE + "(...)", argumentsCount)));
        }
        
        return new AttributeInfo(new QName(namespaceURI, localName), value);
    }
    
    private static void checkInvocationParameter(final Invocation invocation, final XmlStreamWriterMethodType expectedType) {
        if (invocation == null) {
            throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.XMLF_5012_METHOD_PARAMETER_CANNOT_BE_NULL("Invocation parameter")));
        } else {
            if (invocation.getMethodType() != expectedType) {
                throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.XMLF_5013_ILLEGAL_INVOCATION_METHOD_TYPE(invocation.getMethodType(), expectedType)));
            }
        }
    }
}
