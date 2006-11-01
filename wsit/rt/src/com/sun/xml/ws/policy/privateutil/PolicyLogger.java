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

import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * This is a helper class that provides some conveniece methods wrapped around the 
 * standard {@link java.util.logging.PolicyLogger} interface.
 * 
 * @author Marek Potociar
 */
public final class PolicyLogger {
    /**
     * If we run with JAX-WS, we are using its logging domain (appended with ".wspolicy").
     * Otherwise we default to "wspolicy".
     */
    private static final String LOGGING_SUBSYSTEM_NAME;
    private static final Level METHOD_CALL_LEVEL_VALUE = Level.FINER;
    
    static {
        String loggingSubsystemName = "wspolicy";
        try {
            // Looking up JAX-WS class at run-time, so that we don't need to depend
            // on it at compile-time.
            Class jaxwsConstants = Class.forName("com.sun.xml.ws.util.Constants");
            Field loggingDomainField = jaxwsConstants.getField("LoggingDomain");
            Object loggingDomain = loggingDomainField.get(null);
            loggingSubsystemName = loggingDomain.toString().concat(".wspolicy");
        } catch (Exception e) {
            // If we don't manage to extract the logging domain from JAX-WS, we
            // fall back to a default.
        } finally {
            LOGGING_SUBSYSTEM_NAME = loggingSubsystemName;
        }
    }

    private String componentClassName;
    private java.util.logging.Logger logger;
    
    /**
     * Prevents creation of a new instance of this PolicyLogger
     */
    private PolicyLogger(String componentName) {
        this.componentClassName = "[" + componentName + "] ";
        this.logger = java.util.logging.Logger.getLogger(LOGGING_SUBSYSTEM_NAME);
    }
    
    /**
     * The factory method returns preconfigured PolicyLogger wrapper for the class. Since there is no caching implemented,
     * it is advised that the method is called only once per a class in order to initialize a final static logger variable,
     * which is then used through the class to perform actual logging tasks.
     * 
     * @param componentClass class of the component that will use the logger instance. Must not be {@code null}.
     * @return logger instance preconfigured for use with the component
     * @throws NullPointerException if the componentClass parameter is {@code null}.
     */
    public static PolicyLogger getLogger(Class componentClass) {
        return new PolicyLogger(componentClass.getName());
    }
 
    public void log(Level level, String methodName, String message) {
        logger.logp(level, componentClassName, methodName, message);
    }

    public void log(Level level, String methodName, String message, Throwable thrown) {
        logger.logp(level, componentClassName, methodName, message, thrown);
    }

    public void finest(String methodName, String message) {
        logger.logp(Level.FINEST, componentClassName, methodName, message);
    }

    public void finest(String methodName, String message, Throwable thrown) {
        logger.logp(Level.FINEST, componentClassName, methodName, message, thrown);
    }

    public void finer(String methodName, String message) {
        logger.logp(Level.FINER, componentClassName, methodName, message);
    }

    public void finer(String methodName, String message, Throwable thrown) {
        logger.logp(Level.FINER, componentClassName, methodName, message, thrown);
    }

    public void fine(String methodName, String message) {
        logger.logp(Level.FINE, componentClassName, methodName, message);
    }

    public void fine(String methodName, String message, Throwable thrown) {
        logger.logp(Level.FINE, componentClassName, methodName, message, thrown);
    }

    public void info(String methodName, String message) {
        logger.logp(Level.INFO, componentClassName, methodName, message);
    }

    public void info(String methodName, String message, Throwable thrown) {
        logger.logp(Level.INFO, componentClassName, methodName, message, thrown);
    }

    public void config(String methodName, String message) {
        logger.logp(Level.CONFIG, componentClassName, methodName, message);
    }

    public void config(String methodName, String message, Throwable thrown) {
        logger.logp(Level.CONFIG, componentClassName, methodName, message, thrown);
    }

    public void warning(String methodName, String message) {
        logger.logp(Level.WARNING, componentClassName, methodName, message);
    }

    public void warning(String methodName, String message, Throwable thrown) {
        logger.logp(Level.WARNING, componentClassName, methodName, message, thrown);
    }

    public void severe(String methodName, String message) {
        logger.logp(Level.SEVERE, componentClassName, methodName, message);
    }

    public void severe(String methodName, String message, Throwable thrown) {
        logger.logp(Level.SEVERE, componentClassName, methodName, message, thrown);
    }
    
    public void entering() {
        if (!this.logger.isLoggable(METHOD_CALL_LEVEL_VALUE)) {
            return;
        }
        
        logger.entering(componentClassName, getStackMethodName(4));
    }

    public void entering(Object[] parameters) {
        if (!this.logger.isLoggable(METHOD_CALL_LEVEL_VALUE)) {
            return;
        }
                
        logger.entering(componentClassName, getStackMethodName(4), parameters);
    }
    
    public void exiting() {
        if (!this.logger.isLoggable(METHOD_CALL_LEVEL_VALUE)) {
            return;
        }
        logger.exiting(componentClassName, getStackMethodName(4));
    }
    
    public void exiting(Object result) {
        if (!this.logger.isLoggable(METHOD_CALL_LEVEL_VALUE)) {
            return;
        }
        logger.exiting(componentClassName, getStackMethodName(4), result);
    }

    private String getStackMethodName(int index) {
        String methodName;

        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length > index + 1) {
            methodName = stack[index].getMethodName();
        } else {
            methodName = "UNKNOWN METHOD";
        }       
        
        return methodName;
    }
    
    // TODO: refactor and remove usage of the following methods (replace with the version above): 
    public void entering(String methodName) {
        logger.entering(componentClassName, methodName);
    }

    public void entering(String methodName, Object parameter) {
        logger.entering(componentClassName, methodName, parameter);
    }
    
    public void entering(String methodName, Object[] parameters) {
        logger.entering(componentClassName, methodName, parameters);
    }
    
    public void exiting(String methodName) {
        logger.exiting(componentClassName, methodName);
    }
    
    public void exiting(String methodName, Object result) {
        logger.exiting(componentClassName, methodName, result);
    }
    
}
