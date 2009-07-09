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

    public static Collection<CommunicationAPI> createCommunicationImpls(InitParameters parameters) throws WebServiceException {
        final ServiceFinder<CommunicationAPI> finder = ServiceFinder.find(CommunicationAPI.class);
        final Collection<CommunicationAPI> commImpls = new ArrayList<CommunicationAPI>();
        final Iterator<CommunicationAPI> commImplIterator = finder.iterator();
        while (commImplIterator.hasNext()) {
            final CommunicationAPI commImpl = commImplIterator.next();
            commImpl.init(parameters);
            commImpls.add(commImpl);
        }
        if (commImpls.size() < 1) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5007_NO_INTERFACE_IMPL(CommunicationAPI.class.getName())));
        }
        return commImpls;
    }

    public static ConfigurationAPI createConfigurationImpl() throws WebServiceException {
        final ServiceFinder<ConfigurationAPI> finder = ServiceFinder.find(ConfigurationAPI.class);
        final Iterator<ConfigurationAPI> configImpls = finder.iterator();
        if (!configImpls.hasNext()) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5007_NO_INTERFACE_IMPL(ConfigurationAPI.class.getName())));
        }
        final ConfigurationAPI configImpl = configImpls.next();
        if (configImpls.hasNext()) {
            LOGGER.warning(ManagementMessages.WSM_5008_MULTIPLE_INTERFACE_IMPLS(ConfigurationAPI.class, configImpl));
        }
        return configImpl;
    }

    public static PersistenceAPI createPersistenceImpl() throws WebServiceException {
        final ServiceFinder<PersistenceAPI> finder = ServiceFinder.find(PersistenceAPI.class);
        final Iterator<PersistenceAPI> persistenceImpls = finder.iterator();
        if (!persistenceImpls.hasNext()) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5007_NO_INTERFACE_IMPL(PersistenceAPI.class.getName())));
        }
        final PersistenceAPI persistenceImpl = persistenceImpls.next();
        if (persistenceImpls.hasNext()) {
            LOGGER.warning(ManagementMessages.WSM_5008_MULTIPLE_INTERFACE_IMPLS(ConfigurationAPI.class, persistenceImpl));
        }
        return persistenceImpl;
    }

}