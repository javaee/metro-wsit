/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.xmlfilter.localization.LocalizationMessages;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * The class provides an implementation of an {@link InvocationHandler} interface
 * that handles requests of {@link XMLStreamWriter} proxy instances.
 *<p/>
 * This {@link InvocationHandler} implementation adds additional feature or enhancement
 * to the underlying {@link XMLStreamWriter} instance. The new enhancement or feature is
 * defined by an {@link InvocationProcessor} implementation.
 * <p/>
 * The class also contains a static factory method for creating such 'enhanced'
 * {@link XMLStreamWriter} proxies.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class EnhancedXmlStreamWriterProxy implements InvocationHandler {
    private static final Logger LOGGER = Logger.getLogger(EnhancedXmlStreamWriterProxy.class);
    
    private static final Class<?>[] PROXIED_INTERFACES = new Class<?>[] {XMLStreamWriter.class};
    
    // preloaded Method objects for the methods in java.lang.Object
    private static final Method hashCodeMethod;
    private static final Method equalsMethod;
    private static final Method toStringMethod;
    static {
        try {
            hashCodeMethod = Object.class.getMethod("hashCode");
            equalsMethod = Object.class.getMethod("equals", new Class[] { Object.class });
            toStringMethod = Object.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
            throw LOGGER.logSevereException(new NoSuchMethodError(e.getMessage()), e);
        }
    }
    
    // invocation procesor that processes
    private final InvocationProcessor invocationProcessor;
    
    /**
     * Creates a wrapper {@link XMLStreamWriter} proxy that adds enhanced feature
     * to the {@code writer} instance.
     *
     * @param writer {@link XMLStreamWriter} instance that should be enhanced with
     *        content filtering feature.
     * @param processorFactory {@link InvocationProcessorFactory} instance that
     *        is used to create {@link InvocationProcessor} which implements new enhancement
     *        or feature.
     *
     * @return new enhanced {XMLStreamWriter} (proxy) instance
     * @throws XMLStreamException in case of any problems with creating the proxy
     */
    public static XMLStreamWriter createProxy(final XMLStreamWriter writer, final InvocationProcessorFactory processorFactory) throws XMLStreamException {
        LOGGER.entering();
        
        XMLStreamWriter proxy = null;
        try {
            proxy = (XMLStreamWriter) Proxy.newProxyInstance(
                    writer.getClass().getClassLoader(),
                    PROXIED_INTERFACES,
                    new EnhancedXmlStreamWriterProxy(writer, processorFactory));
            
            return proxy;
        } finally {
            LOGGER.exiting(proxy);
        }
    }
    
    private EnhancedXmlStreamWriterProxy(final XMLStreamWriter writer, final InvocationProcessorFactory processorFactory) throws XMLStreamException {
        this.invocationProcessor = processorFactory.createInvocationProcessor(writer);
    }
    
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (LOGGER.isMethodCallLoggable()) {
            LOGGER.entering(method, args);
        }
        
        Object result = null;
        try {
            final Class declaringClass = method.getDeclaringClass();
            if (declaringClass == Object.class) {
                return handleObjectMethodCall(proxy, method, args);
            } else {
                final Invocation invocation = Invocation.createInvocation(method, args);
                result = invocationProcessor.process(invocation);
                return result;
            }
        } finally {
            LOGGER.exiting(result);
        }
    }
    
    private Object handleObjectMethodCall(final Object proxy, final Method method, final Object[] args) {
        if (method.equals(hashCodeMethod)) {
            return Integer.valueOf(System.identityHashCode(proxy));
        } else if (method.equals(equalsMethod)) {
            return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
        } else if (method.equals(toStringMethod)) {
            return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
        } else {
            throw LOGGER.logSevereException(new InternalError(LocalizationMessages.XMLF_5002_UNEXPECTED_OBJECT_METHOD(method)));
        }
    }
}
