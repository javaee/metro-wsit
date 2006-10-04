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

package com.sun.xml.ws.policy.jaxws.documentfilter;

import com.sun.xml.ws.policy.PolicyConstants;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.Queue;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class FilteringXmlStreamWriterProxy implements InvocationHandler {
    
    private enum MethodType {
        WRITE_START_ELEMENT("writeStartElement"),
        WRITE_END_ELEMENT("writeEndElement"),
        WRITE_ATTRIBUTE("writeAttribute"),
        CLOSE("close"),
        OTHER(null);
        
        private static final MethodType[] types = new MethodType[] {
            WRITE_START_ELEMENT,
            WRITE_END_ELEMENT,
            WRITE_ATTRIBUTE,
            CLOSE
        };
        public static MethodType getMethodType(String methodName) {
            for (MethodType type : types) {
                if (type.methodName.equals(methodName)) {
                    return type;
                }
            }
            return OTHER;
        }
        
        private String methodName;
        
        private MethodType(String methodName) {
            this.methodName = methodName;
        }
    }
    
    private class CommandQueueItem {
        private Method method;
        private Object[] arguments;
        
        public CommandQueueItem(Method method, Object[] arguments) {
            this.method = method;
            this.arguments = arguments;
        }
        
        public Object execute(Object target) throws IllegalAccessException, InvocationTargetException {
            return method.invoke(target, arguments);
        }
    }
    
    private static final Class<?>[] PROXIED_INTERFACES = new Class<?>[] {XMLStreamWriter.class};
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();
    
    private XMLStreamWriter writer; // underlying XML stream writer which we use to eventually serve the requests
    private XMLStreamWriter mirrorWriter;
    
    private Queue<CommandQueueItem> commandQueue; // command queue that stores requests to be still executed on the underlying XML output stream
    private int depth; // indicates the depth in which we are currently nested in the element that should be filtered out
    private boolean filteringOn; // indicates that currently processed elements will be filtered out.
    private boolean cmdBufferingOn; // indicates whether the commands should be buffered or whether they can be directly executed on the underlying XML output stream
    
    public static XMLStreamWriter createProxy(XMLStreamWriter writer) throws XMLStreamException {
        return (XMLStreamWriter) Proxy.newProxyInstance(
                writer.getClass().getClassLoader(),
                PROXIED_INTERFACES,
                new FilteringXmlStreamWriterProxy(writer));
    }
    
    private FilteringXmlStreamWriterProxy(XMLStreamWriter writer) throws XMLStreamException {
        this.writer = writer;
        
        this.mirrorWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(new ByteArrayOutputStream());
        this.commandQueue = new LinkedList<CommandQueueItem>();
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodType methodType = MethodType.getMethodType(method.getName());
        try {
            switch (methodType) {
                case WRITE_START_ELEMENT:
                    if (filteringOn) {
                        depth++;
                    } else {
                        executeCommands();
                        cmdBufferingOn = true;
                    }
                    break;
                case WRITE_END_ELEMENT:
                    if (filteringOn) {
                        if (depth == 0) {
                            filteringOn = false;
                        } else {
                            depth--;
                        }
                    } else {
                        executeCommands();
                        cmdBufferingOn = false;
                    }
                    break;
                case WRITE_ATTRIBUTE:
                    if (!filteringOn && startFiltering(args)) {
                        filteringOn = true;
                        commandQueue.clear(); // remowing buffered commands that should not be executed
                    }
                    break;
                case CLOSE:
                    cmdBufferingOn = false;
                    filteringOn = false;
                    executeCommands();
                    break;
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
                    this.commandQueue.offer(new CommandQueueItem(method, args));
                    invocationTarget = mirrorWriter;
                } else {
                    method.invoke(mirrorWriter, args);
                    invocationTarget = writer;
                }
            }
            
            return method.invoke(invocationTarget, args);
        } catch (IllegalArgumentException ex) {
            throw new PrivateAssertionFilterException(Messages.INVOCATION_ERROR.format(), ex);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        } catch (IllegalAccessException ex) {
            throw new PrivateAssertionFilterException(Messages.INVOCATION_ERROR.format(), ex);
        }
    }
    
    private void executeCommands() throws IllegalAccessException, InvocationTargetException {
        while (!commandQueue.isEmpty()) {
            CommandQueueItem command = commandQueue.poll();
            command.execute(writer);
        }
    }
    
    private boolean startFiltering(Object[] writeAttributeMethodArguments) {
        /*
         * void writeAttribute(String localName, String value)
         * void writeAttribute(String namespaceURI, String localName, String value)
         * void writeAttribute(String prefix, String namespaceURI, String localName, String value)
         */
        int argumentsCount = writeAttributeMethodArguments.length;
        String namespaceURI, localName, value;
        
        switch (argumentsCount) {
            case 2:
                namespaceURI = mirrorWriter.getNamespaceContext().getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
                localName = writeAttributeMethodArguments[0].toString();
                value = writeAttributeMethodArguments[1].toString();
                break;
            case 3:
                namespaceURI = writeAttributeMethodArguments[0].toString();
                localName = writeAttributeMethodArguments[1].toString();
                value = writeAttributeMethodArguments[2].toString();
                break;
            case 4:
                namespaceURI = writeAttributeMethodArguments[1].toString();
                localName = writeAttributeMethodArguments[2].toString();
                value = writeAttributeMethodArguments[3].toString();
                break;
            default:
                throw new IllegalArgumentException(
                        Messages.UNEXPECTED_ARGUMENTS_COUNT.format(MethodType.WRITE_ATTRIBUTE + "(...)", argumentsCount)
                        );
        }        
        
        QName attributeName = new QName(namespaceURI, localName);
        if (PolicyConstants.VISIBILITY_ATTRIBUTE.equals(attributeName) && PolicyConstants.VISIBILITY_VALUE_PRIVATE.equals(value)) {
            return true;
        } else {
            return false;            
        }        
    }
}