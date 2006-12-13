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
import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.*;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class PrivateElementFilteringInvocationProcessor implements InvocationProcessor {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PrivateElementFilteringInvocationProcessor.class);
    
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();
    private static final String MEX_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/09/mex";
    private static final String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
    private static final QName WSDL_IMPORT_ELEMENT = new QName(WSDL_NAMESPACE, "import");
    private static final QName IMPORT_NAMESPACE_ATTIBUTE = new QName(WSDL_NAMESPACE, "namespace");
    
    private XMLStreamWriter originalWriter; // underlying XML stream writer which we use to eventually serve the requests
    private XMLStreamWriter mirrorWriter;
    
    private Queue<Invocation> invocationQueue; // parser method invocation queue that stores invocation requests to be still executed on the underlying XML output stream
    private int depth; // indicates the depth in which we are currently nested in the element that should be filtered out
    private boolean filteringOn; // indicates that currently processed elements will be filtered out.
    private boolean cmdBufferingOn; // indicates whether the commands should be buffered or whether they can be directly executed on the underlying XML output stream
    
    private QName[] filteredElements;
    
    
    /** Creates a new instance of InvocationProcessor */
    public PrivateElementFilteringInvocationProcessor(XMLStreamWriter writer, QName... filteredElements) throws XMLStreamException {
        this.originalWriter = writer;
        if (filteredElements == null) {
            this.filteredElements = new QName[]{};
        } else {
            this.filteredElements = filteredElements;
        }
        
        this.mirrorWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(new StringWriter());
    }
    
    public Object process(Invocation invocation) throws InvocationProcessingException {
        try {
            XmlStreamWriterMethodType methodType = XmlStreamWriterMethodType.getMethodType(invocation.getMethodName());
            switch (methodType) {
                case WRITE_START_ELEMENT:
                    if (filteringOn) {
                        depth++;
                    } else {
                        filteringOn = startFiltering(invocation);
                    }
                    break;
                case WRITE_END_ELEMENT:
                    if (filteringOn) {
                        if (depth == 0) {
                            filteringOn = false;
                            return invocation.execute(mirrorWriter);
                        } else {
                            depth--;
                        }
                    }
                    break;
                default:
                    break;
            }
            
            Object invocationTarget;
            if (filteringOn) {
                invocationTarget = mirrorWriter;
            } else {
                invocation.execute(mirrorWriter);
                invocationTarget = originalWriter;
            }
            
            return invocation.execute(invocationTarget);
        } catch (IllegalArgumentException ex) {
            throw new InvocationProcessingException(invocation, ex);
        } catch (InvocationTargetException ex) {
            throw new InvocationProcessingException(invocation, ex.getCause());
        } catch (IllegalAccessException ex) {
            throw new InvocationProcessingException(invocation, ex);
        } finally {
            LOGGER.exiting();
        }
    }
    
    private void executeCommands(XMLStreamWriter writer) throws IllegalAccessException, InvocationProcessingException {
        while (!invocationQueue.isEmpty()) {
            Invocation command = invocationQueue.poll();
            try {
                command.execute(writer);
            } catch (InvocationTargetException ex) {
                LOGGER.severe("executeCommands", "Error invoking " + command.toString(), ex.getCause());
                throw new InvocationProcessingException(command, ex.getCause());
            }
        }
    }
    
    private boolean startFiltering(Invocation invocation) {
        /**
         * void writeStartElement(String localName)
         * void writeStartElement(String namespaceURI, String localName)
         * void writeStartElement(String prefix, String localName, String namespaceURI)
         */
        int argumentsCount = invocation.getArgumentsLength();
        String namespaceURI, localName;
        
        switch (argumentsCount) {
            case 1:
                namespaceURI = mirrorWriter.getNamespaceContext().getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
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
                throw new IllegalArgumentException(
                        LocalizationMessages.UNEXPECTED_ARGUMENTS_COUNT(XmlStreamWriterMethodType.WRITE_START_ELEMENT + "(...)", argumentsCount)
                        );
        }
        
        QName elementName = new QName(namespaceURI, localName);
        
        for (QName filteredElement : filteredElements) {
            if (filteredElement.equals(elementName)) {
                return true;
            }
        }
        
        return false;
    }
}

