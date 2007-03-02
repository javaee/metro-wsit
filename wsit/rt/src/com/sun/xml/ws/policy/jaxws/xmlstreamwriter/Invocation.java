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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.ws.policy.privateutil.PolicyLogger;

import static com.sun.xml.ws.policy.jaxws.xmlstreamwriter.XmlStreamWriterMethodType.WRITE_CHARACTERS;
import static com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages.WSP_1052_NO_ARGUMENTS_IN_INVOCATION;
/**
 * The class represents a wrapper around {@code XMLStreamWriter} invocations. 
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class Invocation {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(Invocation.class);    
    
    private Method method;
    private Object[] arguments;
    private String argsString;
    private XmlStreamWriterMethodType methodType;
    
    /**
     * Factory method that creates {@link Invocation} instance according to input 
     * arguments
     *
     * @param method method represented by the {@link Invocation} instance returned 
     *        as a result of this factory method call
     * @param args invocation arguments to be passed to the method when {@link #execute()} 
     *        method is invoked on the {@link Invocation} instance.
     * @return the {@link Invocation} instance representing invocation of method 
     *        defined by value of {@code method} argument.
     */
    public static Invocation createInvocation(final Method method, final Object[] args) {
        Object[] arguments;
        XmlStreamWriterMethodType methodType = XmlStreamWriterMethodType.getMethodType(method.getName());
        if (methodType == WRITE_CHARACTERS && args.length == 3) {
            final Integer start = (Integer) args[1];
            final Integer length = (Integer) args[2];
            final char[] charArrayCopy = new char[length.intValue()];
            System.arraycopy(args[0], start, charArrayCopy, 0, length);
            
            arguments = new Object[3];
            arguments[0] = charArrayCopy;
            arguments[1] = Integer.valueOf(0);
            arguments[2] = length;
        } else {
            arguments = args;
        }
        
        return new Invocation(method, methodType, arguments);
    }
    
    /**
     * Private constructor of the class used in the {@link createInvocation(Method, Object[])} 
     * factory method.
     *
     * @param method method represented by the new {@link Invocation} instance
     * @param type method type represented by the new {@link Invocation} instance
     * @param args invocation arguments to be passed to the method when {@link #execute()} 
     *        method is invoked on the {@link Invocation} instance.
     *
     * @see XmlStreamWriterMethodType
     */
    private Invocation(final Method method, final XmlStreamWriterMethodType type, final Object[] args) {
        this.method = method;
        this.arguments = args;
        this.methodType = type;
    }
    
    /**
     * Returns information about the name of the method represented by this {@link Invocation} instance
     *
     * @return method name represented by this {@link Invocation} instance
     */
    public String getMethodName() {
        return method.getName();
    }
    
    /**
     * Returns information about the type of the method represented by this {@link Invocation} instance
     *
     * @return method type represented by this {@link Invocation} instance
     * @see XmlStreamWriterMethodType
     */
    public XmlStreamWriterMethodType getMethodType() {
        return methodType;
    }
    
    /**
     * Returns single invocation argument for this {@link Invocation} instance that 
     * is stored in the invocation arguments array at position determined by {@code index}
     * argument.
     *
     * @return single invocation argument for this {@link Invocation} instance at 
     *         position determined by {@code index} argument
     *
     * @throws ArrayIndexOutOfBoundsException if there are no arguments in the array
     *         or if the index parameter is out of bounds of invocation arguments array
     */
    public Object getArgument(final int index) throws ArrayIndexOutOfBoundsException {
        if (arguments == null) {
            throw LOGGER.logSevereException(new ArrayIndexOutOfBoundsException(WSP_1052_NO_ARGUMENTS_IN_INVOCATION(this.toString())));
        }
        return arguments[index];
    }
    
    /**
     * Returns information about the number of arguments stored in this {@link Invocation} 
     * instance
     *
     * @return number of arguments stored in this {@link Invocation} instance
     */
    public int getArgumentsCount() {
        return (arguments != null) ? arguments.length : 0;
    }
    
    /**
     * Executes the method on {@code target} {@code XMLStreamWriter} instance.
     * 
     * @return execution result.
     * @exception IllegalAccessException see {@link java.lang.reflect.Method.invoke(Object, Object[]) Method.invoke()}.
     * @exception IllegalArgumentException see {@link java.lang.reflect.Method.invoke(Object, Object[]) Method.invoke()}.
     * @exception InvocationTargetException see {@link java.lang.reflect.Method.invoke(Object, Object[]) Method.invoke()}.
     * @exception NullPointerException see {@link java.lang.reflect.Method.invoke(Object, Object[]) Method.invoke()}.
     * @exception ExceptionInInitializerError see {@link java.lang.reflect.Method.invoke(Object, Object[]) Method.invoke()}.
     */
    public Object execute(final XMLStreamWriter target) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException {
        return method.invoke(target, arguments);
    }
    
    /**
     * Method returns {@link String} representation of the {@link Invocation} instance.
     * 
     * @return {@link String} representation of the {@link Invocation} instance.
     */
    public String toString() {
        final StringBuffer retValue = new StringBuffer("invocation {");
        retValue.append("method='").append(method.getName()).append("', args=").append(argsToString());
        retValue.append('}');
        
        return retValue.toString();
    }
    
    /**
     * Method returns {@link String} representation of arguments stored in the 
     * {@link Invocation} instance.
     * 
     * @return {@link String} representation of arguments stored in the {@link Invocation} 
     *         instance.
     */
    public String argsToString() {
        if (argsString == null) {
            List<Object> argList = null;
            if (arguments != null && arguments.length > 0) {
                if (arguments.length == 3 && "writeCharacters".equals(method.getName())) {
                    argList = new ArrayList<Object>(3);
                    argList.add(new String((char[]) arguments[0]));
                    argList.add(arguments[1]);
                    argList.add(arguments[2]);
                } else {
                    argList = Arrays.asList(arguments);
                }
            }
            argsString = (argList != null) ? argList.toString() : "no arguments";
        }
        
        return argsString;
    }
}
