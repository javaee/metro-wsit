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

import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.*;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
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
final class PrivateAssertionFilteringInvocationProcessor implements InvocationProcessor {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PrivateAssertionFilteringInvocationProcessor.class);
    
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();
    
    private XMLStreamWriter originalWriter; // underlying XML stream writer which we use to eventually serve the requests
    private XMLStreamWriter mirrorWriter;
    
    private Queue<Invocation> invocationQueue; // parser method invocation queue that stores invocation requests to be still executed on the underlying XML output stream
    private int depth; // indicates the depth in which we are currently nested in the element that should be filtered out
    private boolean filteringOn; // indicates that currently processed elements will be filtered out.
    private boolean cmdBufferingOn; // indicates whether the commands should be buffered or whether they can be directly executed on the underlying XML output stream
    
    
    /** Creates a new instance of InvocationProcessor */
    public PrivateAssertionFilteringInvocationProcessor(XMLStreamWriter writer) throws XMLStreamException {
        this.originalWriter = writer;
        
        this.mirrorWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(new StringWriter());
        this.invocationQueue = new LinkedList<Invocation>();
    }
    
    public Object process(Invocation invocation) throws InvocationProcessingException {
        try {
            XmlStreamWriterMethodType methodType = XmlStreamWriterMethodType.getMethodType(invocation.getMethodName());
            switch (methodType) {
                case WRITE_START_ELEMENT:
                    if (filteringOn) {
                        depth++;
                    } else {
                        executeCommands(this.originalWriter);
                        cmdBufferingOn = true;
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
                    } else {
                        executeCommands(this.originalWriter);
                        cmdBufferingOn = false;
                    }
                    break;
                case WRITE_ATTRIBUTE:
                    if (!filteringOn && startFiltering(invocation)) {
                        filteringOn = true;
                        cmdBufferingOn = false;
                        invocationQueue.clear(); // removing buffered commands that should not be executed
                    }
                    break;
                case CLOSE:
                    cmdBufferingOn = false;
                    filteringOn = false;
                    executeCommands(this.originalWriter);
                    break;
                case WRITE_CHARACTERS:
                case OTHER:
                    break;
                default:
                    throw new IllegalStateException("Unexpected method type: '" + methodType + "'");
            }
            
            Object invocationTarget;
            if (filteringOn) {
                invocationTarget = mirrorWriter;
            } else {
                if (cmdBufferingOn) {
                    this.invocationQueue.offer(invocation);
                    invocationTarget = mirrorWriter;
                } else {
                    invocation.execute(mirrorWriter);
                    invocationTarget = originalWriter;
                }
            }
            
            return invocation.execute(invocationTarget);
        } catch (IllegalArgumentException ex) {
            throw new InvocationProcessingException(Messages.INVOCATION_ERROR.format(), ex);
        } catch (InvocationTargetException ex) {
            throw new InvocationProcessingException(Messages.INVOCATION_ERROR.format(), ex.getCause());
        } catch (IllegalAccessException ex) {
            throw new InvocationProcessingException(Messages.INVOCATION_ERROR.format(), ex);
        } finally {
            LOGGER.exiting();
        }
    }
    
    private void executeCommands(XMLStreamWriter writer) throws IllegalAccessException, InvocationTargetException {
        while (!invocationQueue.isEmpty()) {
            Invocation command = invocationQueue.poll();
            command.execute(writer);
        }
    }
    
    private boolean startFiltering(Invocation invocation) {
        /*
         * void writeAttribute(String localName, String value)
         * void writeAttribute(String namespaceURI, String localName, String value)
         * void writeAttribute(String prefix, String namespaceURI, String localName, String value)
         */
        int argumentsCount = invocation.getArgumentsLength();
        String namespaceURI, localName, value;
        
        switch (argumentsCount) {
            case 2:
                namespaceURI = mirrorWriter.getNamespaceContext().getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
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
                throw new IllegalArgumentException(
                        Messages.UNEXPECTED_ARGUMENTS_COUNT.format(XmlStreamWriterMethodType.WRITE_ATTRIBUTE + "(...)", argumentsCount)
                        );
        }
        
        QName attributeName = new QName(namespaceURI, localName);
        return PolicyConstants.VISIBILITY_ATTRIBUTE.equals(attributeName) && PolicyConstants.VISIBILITY_VALUE_PRIVATE.equals(value);
    }
    
}
