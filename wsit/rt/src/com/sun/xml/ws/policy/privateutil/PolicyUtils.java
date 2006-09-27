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

package com.sun.xml.ws.policy.privateutil;

import com.sun.xml.ws.policy.PolicyException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.xml.namespace.QName;

/**
 * This is a wrapper class for various utilities that may be reused within Policy API implementation.
 * The class is not part of public Policy API. Do not use it from your client code!
 *
 * @author Marek Potociar
 */
public final class PolicyUtils {
    private PolicyUtils() { }
    
    /**
     * Text utilities wrapper.
     */
    public static class Text {
        /**
         * System-specific line separator character retrieved from the Java system property
         * <code>line.separator</code>
         */
        public final static String NEW_LINE = System.getProperty("line.separator");
        
        /**
         * Method creates indent string consisting of as many {@code TAB} characters as specified by {@code indentLevel} parameter
         *
         * @param indentLevel indentation level
         * @return indentation string as specified by indentation level
         *
         */
        public static String createIndent(int indentLevel) {
            char[] charData = new char[indentLevel * 4];
            Arrays.fill(charData, ' ');
            return String.valueOf(charData);
        }
    }
    
    public static class Comparison {
        /**
         * The comparator comapres QName objects according to their publicly accessible attributes, in the following
         * order of attributes:
         *
         * 1. namespace (not null String)
         * 2. local name (not null String)
         */
        public static final Comparator<QName> QNAME_COMPARATOR = new Comparator<QName>() {
            public int compare(QName qn1, QName qn2) {
                if (qn1 == qn2 || qn1.equals(qn2)) {
                    return 0;
                }
                
                int result;
                
                result = qn1.getNamespaceURI().compareTo(qn2.getNamespaceURI());
                if (result != 0) {
                    return result;
                }
                
                return qn1.getLocalPart().compareTo(qn2.getLocalPart());
            }
        };
        
        /**
         * Compares two boolean values in the following way: {@code false < true}
         *
         * @returns {@code -1} if {@code b1 < b2}, {@code 0} if {@code b1 == b2}, {@code 1} if {@code b1 > b2}
         */
        public static int compareBoolean(boolean b1, boolean b2) {
            int i1 = (b1) ? 1 : 0;
            int i2 = (b2) ? 1 : 0;
            
            return i1 - i2;
        }
        
        /**
         * Compares two String values, that may possibly be null in the following way: {@code null < "string value"}
         *
         * @returns {@code -1} if {@code s1 < s2}, {@code 0} if {@code s1 == s2}, {@code 1} if {@code s1 > s2}
         */
        public static int compareNullableStrings(String s1, String s2) {
            return ((s1 == null) ? ((s2 == null) ? 0 : -1) : s1.compareTo(s2));
        }
    }
    
    public static class Collections {
        /**
         * TODO javadocs
         *
         * @param base the combination base that will be present in each combination. May be {@code null} or empty.
         * @param options options that should be combined. May be {@code null} or empty.
         * @param ignoreEmptyOption flag identifies whether empty options should be ignored or whether the method should halt
         *        processing and return {@code null} when an empty option is encountered
         * @return TODO
         */
        public static <E, T extends Collection<? extends E>, U extends Collection<? extends E>> Collection<Collection<E>> combine(final U initialBase, final Collection<T> options, boolean ignoreEmptyOption) {
            List<Collection<E>> combinations = null;
            if (options == null || options.isEmpty()) {
                // no combination creation needed
                if (initialBase != null) {
                    combinations = new ArrayList<Collection<E>>(1);
                    combinations.add(new ArrayList<E>(initialBase));
                }
                return combinations;
            }
            
            // creating defensive and modifiable copy of the base
            Collection<E> base = new LinkedList<E>();
            if (initialBase != null && !initialBase.isEmpty()) {
                base.addAll(initialBase);
            }
            /**
             * now we iterate over all options and build up an option processing queue:
             *   1. if ignoreEmptyOption flag is not set and we found an empty option, we are going to stop processing and return null. Otherwise we
             *      ignore the empty option.
             *   2. if the option has one child only, we add the child directly to the base.
             *   3. if there are more children in examined node, we add it to the queue for further processing and precoumpute the final size of
             *      resulting collection of combinations.
             */
            int finalCombinationsSize = 1;
            Queue<T> optionProcessingQueue = new LinkedList<T>();
            for (T option : options) {
                int optionSize =  option.size();
                
                if (optionSize == 0) {
                    if (ignoreEmptyOption) {
                        // do nothing
                    } else {
                        return null;
                    }
                } else if (optionSize == 1) {
                    base.addAll(option);
                } else {
                    optionProcessingQueue.offer(option);
                    finalCombinationsSize *= optionSize;
                }
            }
            
            // creating final combinations
            combinations = new ArrayList<Collection<E>>(finalCombinationsSize);
            combinations.add(base);
            if (finalCombinationsSize > 1) {
                T processedOption;
                while ((processedOption = optionProcessingQueue.poll()) != null) {
                    int actualSemiCombinationCollectionSize = combinations.size();
                    int newSemiCombinationCollectionSize = actualSemiCombinationCollectionSize * processedOption.size();
                    
                    int semiCombinationIndex = 0;
                    for (E optionElement : processedOption) {
                        for (int i = 0; i < actualSemiCombinationCollectionSize; i++) {
                            Collection<E> semiCombination = combinations.get(semiCombinationIndex); // unfinished combination
                            
                            if (semiCombinationIndex + actualSemiCombinationCollectionSize < newSemiCombinationCollectionSize) {
                                // this is not the last optionElement => we create a new combination copy for the next child
                                combinations.add(new LinkedList<E>(semiCombination));
                            }
                            
                            semiCombination.add(optionElement);
                            semiCombinationIndex++;
                        }
                    }
                }
            }
            return combinations;
        }
    }
    
    /**
     * Reflection utilities wrapper
     */
    public static class Reflection {
        
        /**
         * Reflectively invokes specified method on the specified target
         */
        public static <T> T invoke(Object target, String methodName, Class<T> resultClass, Object... parameters) throws PolicyException {
            Class[] parameterTypes;
            if (parameters != null && parameters.length > 0) {
                parameterTypes = new Class[parameters.length];
                int i = 0;
                for (Object parameter : parameters) {
                    parameterTypes[i++] = parameter.getClass();
                }
            } else {
                parameterTypes = null;
            }
            
            return invoke(target, methodName, resultClass, parameters, parameterTypes);
        }
        
        /**
         * Reflectively invokes specified method on the specified target
         */
        public static <T> T invoke(Object target, String methodName, Class<T> resultClass, Object[] parameters, Class[] parameterTypes) throws PolicyException {
            try {
                Method method = target.getClass().getMethod(methodName, parameterTypes);
                Object result = method.invoke(target, parameters);
                
                return resultClass.cast(result);
            } catch (IllegalArgumentException e) {
                throw new PolicyException(Messages.METHOD_INVOCATION_FAILED.format(methodName, target.getClass().getName(), Arrays.asList(parameters).toString()), e);
            } catch (InvocationTargetException e) {
                throw new PolicyException(Messages.METHOD_INVOCATION_FAILED.format(methodName, target.getClass().getName(), Arrays.asList(parameters).toString()), e);
            } catch (IllegalAccessException e) {
                throw new PolicyException(Messages.METHOD_INVOCATION_FAILED.format(methodName, target.getClass().getName(), Arrays.asList(parameters).toString()), e);
            } catch (SecurityException e) {
                throw new PolicyException(Messages.METHOD_INVOCATION_FAILED.format(methodName, target.getClass().getName(), Arrays.asList(parameters).toString()), e);
            } catch (NoSuchMethodException e) {
                throw new PolicyException(Messages.METHOD_INVOCATION_FAILED.format(methodName, target.getClass().getName(), Arrays.asList(parameters).toString()), e);
            }
        }
    }
    
    public static class ConfigFile {
        /**
         * Generates a config file resource name from provided config file identifier. The generated file name can be
         * transformed into a URL instance using {@link #loadResource(String, Object)} method.
         *
         * @param configFileIdentifier the string used to generate the config file URL that will be parsed. Each WSIT config
         *        file is in form of <code>wsit-<i>{configFileIdentifier}</i>.xml</code>. Must not be {@code null}.
         * @return generated config file resource name
         */
        public static String generateFullName(String configFileIdentifier) {
            //TODO: replace with some algorithm that retrieves the actual file name.
            if (configFileIdentifier != null) {
                StringBuffer buffer = new StringBuffer("wsit-");
                buffer.append(configFileIdentifier).append(".xml");
                return buffer.toString();
            } else {
                return "wsit.xml"; //TODO: throw exception instead
            }
        }
        
        /**
         * Loads the config file as an URL resource.
         */
        public static URL loadAsResource(String configFileName, Object context) throws PolicyException {
            if (context == null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null) {
                    return ClassLoader.getSystemResource(configFileName);
                } else {
                    return cl.getResource(configFileName);
                }
            } else {
                return Reflection.invoke(context, "getResource", URL.class, "/WEB-INF/" + configFileName);
            }
        }
    }

    /**
     * Wrapper for ServiceFinder class which is not part of the Java SE yet.
     */
    public static class ServiceProvider {
        /**
         * Locates and incrementally instantiates the available providers of a
         * given service using the given class loader.
         * <p/>
         * <p> This method transforms the name of the given service class into a
         * provider-configuration filename as described above and then uses the
         * <tt>getResources</tt> method of the given class loader to find all
         * available files with that name.  These files are then read and parsed to
         * produce a list of provider-class names. Eventually each provider class is
         * instantiated and array of those instances is returned.
         * <p/>
         * <p> Because it is possible for extensions to be installed into a running
         * Java virtual machine, this method may return different results each time
         * it is invoked. <p>
         *
         * @param service The service's abstract service class. Must not be {@code null}.
         * @param loader  The class loader to be used to load provider-configuration files
         *                and instantiate provider classes, or <tt>null</tt> if the system
         *                class loader (or, failing that the bootstrap class loader) is to
         *                be used
         * @throws NullPointerException in case {@code service} input parameter is {@code null}.
         * @throws ServiceConfigurationError If a provider-configuration file violates the specified format
         *                                   or names a provider class that cannot be found and instantiated
         * @see #load(Class)
         */
        public static <T> T[] load(Class<T> serviceClass, ClassLoader loader) {
            return ServiceFinder.find(serviceClass, loader).toArray();
        }
        
        /**
         * Locates and incrementally instantiates the available providers of a
         * given service using the context class loader.  This convenience method
         * is equivalent to
         * <p/>
         * <pre>
         *   ClassLoader cl = Thread.currentThread().getContextClassLoader();
         *   return PolicyUtils.ServiceProvider.load(service, cl);
         * </pre>
         *
         * @param service The service's abstract service class. Must not be {@code null}.
         *
         * @throws NullPointerException in case {@code service} input parameter is {@code null}.
         * @throws ServiceConfigurationError If a provider-configuration file violates the specified format
         *                                   or names a provider class that cannot be found and instantiated
         * @see #load(Class, ClassLoader)
         */
        public static <T> T[] load(Class<T> serviceClass) {
            return ServiceFinder.find(serviceClass).toArray();
        }
    }    
}
