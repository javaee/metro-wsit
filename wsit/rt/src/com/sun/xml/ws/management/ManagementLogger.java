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

package com.sun.xml.ws.management;

import com.sun.istack.logging.Logger;
import java.lang.reflect.Field;

/**
 * Implements a logger for the management component.
 *
 * @author Fabian Ritzmann
 */
public class ManagementLogger extends Logger {

    /**
     * If we run with JAX-WS, we are using its logging domain (appended with ".management").
     * Otherwise we default to "management".
     */
    private static final String MANAGEMENT_PACKAGE_ROOT = "com.sun.xml.ws.management";
    private static final String MANAGEMENT_API_PACKAGE_ROOT = "com.sun.xml.ws.api.management";

    /**
     * Create a new Logger instance. Meant to be called from derived classes.
     *
     * @param loggerName The dot-separated package name of a subsystem,
     *   e.g. "javax.enterprise.resource.webservices.jaxws.wspolicy.ClassName"
     * @param className The name of a class within the subsystem
     */
    private ManagementLogger(final String loggerName, final String className) {
        super(loggerName, className);
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
    public static ManagementLogger getLogger(final Class componentClass) {
        final String componentClassName = componentClass.getName();

        if (componentClassName.startsWith(MANAGEMENT_PACKAGE_ROOT)) {
            return new ManagementLogger(getLoggingSubsystemName() + componentClassName.substring(MANAGEMENT_PACKAGE_ROOT.length()),
                    componentClassName);
        } else if (componentClassName.startsWith(MANAGEMENT_API_PACKAGE_ROOT)) {
            return new ManagementLogger(getLoggingSubsystemName() + componentClassName.substring(MANAGEMENT_API_PACKAGE_ROOT.length()),
                    componentClassName);
        } else {
            return new ManagementLogger(getLoggingSubsystemName() + "." + componentClassName, componentClassName);
        }
    }

    private static String getLoggingSubsystemName() {
        String loggingSubsystemName = "management";
        try {
            // Looking up JAX-WS class at run-time, so that we don't need to depend
            // on it at compile-time.
            Class jaxwsConstants = Class.forName("com.sun.xml.ws.util.Constants");
            Field loggingDomainField = jaxwsConstants.getField("LoggingDomain");
            Object loggingDomain = loggingDomainField.get(null);
            loggingSubsystemName = loggingDomain.toString().concat(".management");
        } catch (Exception e) {
            // if we catch an exception, we stick with the default name
        }
        return loggingSubsystemName;
    }

}