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

package com.sun.xml.ws.api.management;

import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Fabian Ritzmann
 */
public class ManagementFactory<T> {

    private static final String COMM_DEFAULT_IMPL = "com.sun.xml.ws.management.jmx.JMXAgent";
    private static final String CONF_DEFAULT_IMPL = "com.sun.xml.ws.management.server.ReDelegate";
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(ManagementFactory.class);

    public static Collection<CommunicationAPI> createCommunicationImpls(InitParameters parameters) throws WebServiceException {
        try {
            // TBD: Return all available communication interfaces, not just one
            Class<? extends CommunicationAPI> implClass = Class.forName(COMM_DEFAULT_IMPL).asSubclass(CommunicationAPI.class);
            CommunicationAPI impl = implClass.newInstance();
            impl.init(parameters);
            ArrayList<CommunicationAPI> impls = new ArrayList<CommunicationAPI>();
            impls.add(impl);
            return impls;
        } catch (InstantiationException e) {
            throw LOGGER.logSevereException(new WebServiceException("Failed to created default management implementation", e));
        } catch (IllegalAccessException e) {
            throw LOGGER.logSevereException(new WebServiceException("Failed to created default management implementation", e));
        } catch (ClassNotFoundException e) {
            throw LOGGER.logSevereException(new WebServiceException("Failed to find default management implementation class " + COMM_DEFAULT_IMPL, e));
        }
    }

    public static ConfigurationAPI createConfigurationImpl() throws WebServiceException {
        try {
            Class<? extends ConfigurationAPI> implClass = Class.forName(CONF_DEFAULT_IMPL).asSubclass(ConfigurationAPI.class);
            ConfigurationAPI impl = implClass.newInstance();
            return impl;
        } catch (InstantiationException e) {
            throw LOGGER.logSevereException(new WebServiceException("Failed to created default management implementation", e));
        } catch (IllegalAccessException e) {
            throw LOGGER.logSevereException(new WebServiceException("Failed to created default management implementation", e));
        } catch (ClassNotFoundException e) {
            throw LOGGER.logSevereException(new WebServiceException("Failed to find default management implementation class " + CONF_DEFAULT_IMPL, e));
        }
    }

    public static PersistenceAPI createPersistenceImpl(InitParameters parameters) throws WebServiceException {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

}
