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

import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.Invocation;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class XmlFilteringUtils {
    public static final class AttributeInfo {
        private QName name;
        private String value;
        
        private AttributeInfo(QName name, String value) {
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
    
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(XmlFilteringUtils.class);
    /** Prevents creation of a new instance of XmlFilteringUtils */
    private XmlFilteringUtils() {
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
        final int argumentsCount = invocation.getArgumentsLength();
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
                throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_1009_UNEXPECTED_ARGUMENTS_COUNT(XmlStreamWriterMethodType.WRITE_START_ELEMENT + "(...)", argumentsCount)));
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
        final int argumentsCount = invocation.getArgumentsLength();
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
                throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_1009_UNEXPECTED_ARGUMENTS_COUNT(XmlStreamWriterMethodType.WRITE_ATTRIBUTE + "(...)", argumentsCount)));
        }
        
        return new AttributeInfo(new QName(namespaceURI, localName), value);
    }
    
    private static final void checkInvocationParameter(final Invocation invocation, XmlStreamWriterMethodType expectedType) {
        if (invocation == null) {
            throw LOGGER.logSevereException(new NullPointerException(LocalizationMessages.WSP_1038_METHOD_PARAMETER_CANNOT_BE_NULL("Invocation parameter")));
        } else {
            XmlStreamWriterMethodType methodType = XmlStreamWriterMethodType.getMethodType(invocation.getMethodName());
            if (methodType != expectedType) {
                throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_1039_ILLEGAL_INVOCATION_METHOD_TYPE(methodType, expectedType)));
            }
        }
    }
}
