/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import javax.xml.stream.XMLStreamWriter;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.xmlfilter.localization.LocalizationMessages;
import static com.sun.xml.ws.xmlfilter.XmlStreamWriterMethodType.WRITE_CHARACTERS;

/**
 * The class represents a wrapper around {@code XMLStreamWriter} invocations. 
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class Invocation {

    private static final Logger LOGGER = Logger.getLogger(Invocation.class);
    private final Method method;
    private final Object[] arguments;
    private String argsString;
    private final XmlStreamWriterMethodType methodType;
    private final boolean returnsVoid;

    /**
     * Factory method that creates {@link Invocation} instance according to input 
     * arguments
     *
     * @param method method represented by the {@link Invocation} instance returned 
     *        as a result of this factory method call
     * @param args invocation arguments to be passed to the method when {@link #executeBatch()} 
     *        method is invoked on the {@link Invocation} instance.
     * @return the {@link Invocation} instance representing invocation of method 
     *        defined by value of {@code method} argument.
     */
    public static Invocation createInvocation(final Method method, final Object[] args) {
        final Object[] arguments;
        final XmlStreamWriterMethodType methodType = XmlStreamWriterMethodType.getMethodType(method.getName());
        
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
     * Method executes queue of invocations. All invocations must represent methods 
     * with {@code void} return type. After succesful invocation of the whole batch, 
     * the batch queue is fully consumed and empty.
     *
     * @param target {@link http://java.sun.com/javase/6/docs/api/javax/xml/stream/XMLStreamWriter.html|XmlStreamWriter}
     *        used for invocation queue execution
     * @param batch queue of invocations to be executed on the targeted
     *        {@link http://java.sun.com/javase/6/docs/api/javax/xml/stream/XMLStreamWriter.html|XmlStreamWriter}.
     *        After succesful invocation of the whole batch, the batch queue is fully
     *        consumed and empty.
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.IllegalArgumentException
     * @throws com.sun.xml.ws.policy.jaxws.xmlstreamwriter.InvocationProcessingException
     */
    public static void executeBatch(final XMLStreamWriter target, Queue<Invocation> batch) throws InvocationProcessingException {
        for (Invocation invocation : batch) {
            if (!invocation.returnsVoid) {
                throw LOGGER.logSevereException(new InvocationProcessingException("Cannot batch-execute invocation with non-void return type: '" + invocation.getMethodName() + "'"));
            }
        }

        while (!batch.isEmpty()) {
            batch.poll().execute(target);
        }
    }

    /**
     * Private constructor of the class used in the {@link createInvocation(Method, Object[])} 
     * factory method.
     *
     * @param method method represented by the new {@link Invocation} instance
     * @param type method type represented by the new {@link Invocation} instance
     * @param args invocation arguments to be passed to the method when {@link #executeBatch()} 
     *        method is invoked on the {@link Invocation} instance.
     *
     * @see XmlStreamWriterMethodType
     */
    private Invocation(final Method method, final XmlStreamWriterMethodType type, final Object[] args) {
        this.method = method;
        this.arguments = args;
        this.methodType = type;
        this.returnsVoid = void.class.isAssignableFrom(method.getReturnType());
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
            throw LOGGER.logSevereException(new ArrayIndexOutOfBoundsException(LocalizationMessages.XMLF_5019_NO_ARGUMENTS_IN_INVOCATION(this.toString())));
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
        return (arguments == null) ? 0 : arguments.length;
    }

    /**
     * Executes the method on {@code target} {@code XMLStreamWriter} instance.
     * 
     * @return execution result.
     * @exception InvocationProcessingException wraps underlying exception - see {@link java.lang.reflect.Method#invoke(Object, Object[]) Method.invoke()}.
     */
    public Object execute(final XMLStreamWriter target) throws InvocationProcessingException {
        try {
            return method.invoke(target, arguments);
        } catch (IllegalArgumentException e) {
            throw LOGGER.logSevereException(new InvocationProcessingException(this, e));
        } catch (InvocationTargetException e) {
            throw LOGGER.logSevereException(new InvocationProcessingException(this, e.getCause()));
        } catch (IllegalAccessException e) {
            throw LOGGER.logSevereException(new InvocationProcessingException(this, e));
        }
    }

    /**
     * Method returns {@link String} representation of the {@link Invocation} instance.
     * 
     * @return {@link String} representation of the {@link Invocation} instance.
     */
    @Override
    public String toString() {
        final StringBuffer retValue = new StringBuffer(30);
        retValue.append("invocation { method='").append(method.getName()).append("', args=").append(argsToString());
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
            argsString = (argList == null) ? "no arguments" : argList.toString();
        }

        return argsString;
    }
}
