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

package com.sun.xml.ws.policy.jaxws.xmlstreamwriter;

import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
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
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(EnhancedXmlStreamWriterProxy.class);
    
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
            LOGGER.severe("<static initialization>", e.getMessage());
            throw new NoSuchMethodError(e.getMessage());
        }
    }
    
    // invocation procesor that processes 
    private InvocationProcessor invocationProcessor;
    
    /**
     * Creates a wrapper {@link XMLStreamWriter} proxy that adds enhanced feature
     * to the {@code writer} instance.
     * 
     * @param writer {@link XMLStreamWriter} instance that should be enhanced with
     *        content filtering feature.
     * @param processorFactory {@link InvocationProcessorFactory} instance that 
     *        is used to create {@InvocationProcessor} which implements new enhancement
     *        or feature.   
     *
     * @return new enhanced {XMLStreamWriter} (proxy) instance
     * @throws XMLStreamException
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
        LOGGER.entering(new Object[] {method, args});
        
        final Class declaringClass = method.getDeclaringClass();
        if (declaringClass == Object.class) {
            return handleObjectMethodCall(proxy, method, args);
        } else {
            final Invocation invocation = Invocation.createInvocation(method, args);
            return invocationProcessor.process(invocation);            
        }
    }
    
    private Object handleObjectMethodCall(final Object proxy, final Method method, final Object[] args) {
        if (method.equals(hashCodeMethod)) {
            return new Integer(System.identityHashCode(proxy));
        } else if (method.equals(equalsMethod)) {
            return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
        } else if (method.equals(toStringMethod)) {
            return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
        } else {
            LOGGER.severe("handleObjectMethodCall", LocalizationMessages.WSP_001007_UNEXPECTED_OBJECT_METHOD(method));
            throw new InternalError(LocalizationMessages.WSP_001007_UNEXPECTED_OBJECT_METHOD(method));
        }
    }
}
