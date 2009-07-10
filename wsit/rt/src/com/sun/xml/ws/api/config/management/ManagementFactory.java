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

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.util.ServiceFinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.xml.ws.WebServiceException;

/**
 * Provides methods to create implementations for the Management APIs.
 *
 * @author Fabian Ritzmann
 */
public class ManagementFactory {

    private static final Logger LOGGER = Logger.getLogger(ManagementFactory.class);

    public static Collection<CommunicationServer> createCommunicationImpls(NamedParameters parameters) throws WebServiceException {
        final ServiceFinder<CommunicationServer> finder = ServiceFinder.find(CommunicationServer.class);
        final Collection<CommunicationServer> commImpls = new ArrayList<CommunicationServer>();
        final Iterator<CommunicationServer> commImplIterator = finder.iterator();
        while (commImplIterator.hasNext()) {
            final CommunicationServer commImpl = commImplIterator.next();
            commImpl.init(parameters);
            commImpls.add(commImpl);
        }
        if (commImpls.size() < 1) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5007_NO_INTERFACE_IMPL(CommunicationServer.class.getName())));
        }
        return commImpls;
    }

    public static Configurator createConfiguratorImpl() throws WebServiceException {
        final ServiceFinder<Configurator> finder = ServiceFinder.find(Configurator.class);
        final Iterator<Configurator> configImpls = finder.iterator();
        if (!configImpls.hasNext()) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5007_NO_INTERFACE_IMPL(Configurator.class.getName())));
        }
        final Configurator configImpl = configImpls.next();
        if (configImpls.hasNext()) {
            LOGGER.warning(ManagementMessages.WSM_5008_MULTIPLE_INTERFACE_IMPLS(Configurator.class, configImpl));
        }
        return configImpl;
    }

    public static ConfigSaver createConfigSaverImpl() throws WebServiceException {
        final ServiceFinder<ConfigSaver> finder = ServiceFinder.find(ConfigSaver.class);
        final Iterator<ConfigSaver> persistenceImpls = finder.iterator();
        if (!persistenceImpls.hasNext()) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5007_NO_INTERFACE_IMPL(ConfigSaver.class.getName())));
        }
        final ConfigSaver persistenceImpl = persistenceImpls.next();
        if (persistenceImpls.hasNext()) {
            LOGGER.warning(ManagementMessages.WSM_5008_MULTIPLE_INTERFACE_IMPLS(ConfigSaver.class.getName(), persistenceImpl));
        }
        return persistenceImpl;
    }

    public static ConfigReader createConfigReaderImpl() throws WebServiceException {
        final ServiceFinder<ConfigReader> finder = ServiceFinder.find(ConfigReader.class);
        final Iterator<ConfigReader> persistenceImpls = finder.iterator();
        if (!persistenceImpls.hasNext()) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5007_NO_INTERFACE_IMPL(ConfigReader.class.getName())));
        }
        final ConfigReader persistenceImpl = persistenceImpls.next();
        if (persistenceImpls.hasNext()) {
            LOGGER.warning(ManagementMessages.WSM_5008_MULTIPLE_INTERFACE_IMPLS(ConfigReader.class.getName(), persistenceImpl));
        }
        return persistenceImpl;
    }

}