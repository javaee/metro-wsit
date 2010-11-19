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

package com.sun.xml.ws.metro.api.config.management;

import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion;

import javax.xml.ws.WebServiceException;

/**
 * This interface encapsulates the logic to reconfigure an endpoint instance.
 *
 * @param <T> The endpoint implementation class type.
 * @author Fabian Ritzmann
 */
public interface Configurator<T> {

    /**
     * Initializes the endpoint.
     *
     * @param endpoint The managed endpoint instance. Must not be null.
     * @param assertion This assertion contains the policy that configured the
     *   managed endpoint. May be null.
     * @param reader A ConfigReader instance. Must not be null.
     * @param saver A ConfigSaver instance. Must not be null.
     * @throws WebServiceException If the initialization failed.
     */
    public void init(ManagedEndpoint<T> endpoint, ManagedServiceAssertion assertion,
            ConfigReader<T> reader, ConfigSaver<T> saver)
            throws WebServiceException;

    /**
     * Starts any concurrent tasks required by the implementation.
     * 
     * @throws WebServiceException If the start failed.
     */
    public void start() throws WebServiceException;

    /**
     * Starts any concurrent tasks required by the implementation.
     *
     * @throws WebServiceException If stopping failed.
     */
    public void stop() throws WebServiceException;

    /**
     * Reconfigures an endpoint instance.
     *
     * @param parameters The reconfiguration data.
     * @throws WebServiceException If the reconfiguration failed.
     */
    public void recreate(NamedParameters parameters) throws WebServiceException;

}
