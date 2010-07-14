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

package com.sun.xml.ws.api.config.management;

import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion;

import javax.xml.ws.WebServiceException;

/**
 * Establish if the configuration data has changed and reconfigure the endpoint
 * with the new configuration data.
 *
 * @param <T> The endpoint implementation class type.
 * @author Fabian Ritzmann
 */
public interface ConfigReader<T> {

    /**
     * Initialize the reader.
     *
     * @param endpoint A ManagedEndpoint instance. Must not be null.
     * @param assertion This assertion contains the policy that configured the
     *   managed endpoint. May be null.
     * @param attributes The attributes with which the original WSEndpoint instance
     *   was created.
     * @param classLoader The class loader that is associated with the original
     *   WSEndpoint instance.
     * @param starter An EndpointStarter instance. Must not be null.
     * @throws WebServiceException If the initialization failed.
     */
    public void init(ManagedEndpoint<T> endpoint, ManagedServiceAssertion assertion, EndpointCreationAttributes attributes,
            ClassLoader classLoader, EndpointStarter starter) throws WebServiceException;

    /**
     * Start this reader.
     *
     * It is assumed that the reader will concurrently poll or wait for a
     * configuration change event.
     *
     * @param parameters Custom configurator implementations can use this to pass
     *   in their own parameters.
     * @throws WebServiceException If the start failed.
     */
    public void start(NamedParameters parameters) throws WebServiceException;

    /**
     * Stop this reader.
     *
     * @throws WebServiceException If stopping failed.
     */
    public void stop() throws WebServiceException;
    
}