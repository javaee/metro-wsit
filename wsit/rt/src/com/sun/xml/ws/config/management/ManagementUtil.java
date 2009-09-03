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

package com.sun.xml.ws.config.management;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.config.management.policy.ManagedServiceAssertion;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.ws.WebServiceException;

/**
 * Utility methods for the management code
 *
 * @author Fabian Ritzmann
 */
public class ManagementUtil {

    /**
     * Name of the JDBC data source passed into the JDBC implementations.
     */
    public static final String DATA_SOURCE_PARAMETER_NAME = "DATA_SOURCE_NAME";

    /**
     * Default JDBC data source name if no custom name was given.
     */
    private static final String DEFAULT_DATA_SOURCE_NAME = "jdbc/metro/management";

    private static final Logger LOGGER = Logger.getLogger(ManagementUtil.class);

    /**
     * Return ManagedService assertion if there is one associated with the endpoint.
     *
     * @param endpoint The endpoint. Must not be null.
     * @return The policy assertion if found. Null otherwise.
     * @throws WebServiceException If computing the effective policy of the endpoint failed.
     */
    public static ManagedServiceAssertion getAssertion(WSEndpoint endpoint) throws WebServiceException {
        LOGGER.entering(endpoint);
        try {
            PolicyAssertion assertion = null;
            final PolicyMap policyMap = endpoint.getPolicyMap();
            if (policyMap != null) {
                final PolicyMapKey key = PolicyMap.createWsdlEndpointScopeKey(endpoint.getServiceName(), endpoint.getPortName());
                final Policy policy = policyMap.getEndpointEffectivePolicy(key);
                if (policy != null) {
                    final Iterator<AssertionSet> assertionSets = policy.iterator();
                    if (assertionSets.hasNext()) {
                        final AssertionSet assertionSet = assertionSets.next();
                        final Iterator<PolicyAssertion> assertions = assertionSet.get(
                                ManagementConstants.SERVICE_ASSERTION_QNAME).iterator();
                        if (assertions.hasNext()) {
                            assertion = assertions.next();
                        }
                    }
                }
            }
            LOGGER.exiting(assertion);
            return assertion == null ? null : assertion.getImplementation(ManagedServiceAssertion.class);
        } catch (PolicyException ex) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5003_FAILED_ASSERTION(), ex));
        }
    }

    /**
     * Read all data and return it as a String.
     *
     * @param reader The reader. Must not be null
     * @return All data provided by the reader.
     * @throws WebServiceException If an exception occurred when reading data from the reader.
     */
    public static String convert(Reader reader) throws WebServiceException {
        try {
            final StringBuilder data = new StringBuilder();
            final char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) >= 0) {
                data.append(buffer, 0, count);
            }
            return data.toString();
        } catch (IOException e) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5018_FAILED_READ(), e));
        }
    }

    /**
     * Look up the data source for the JDBC implementation.
     *
     * @param dataSourceName May hold a custom name of the JDBC data source. Null otherwise.
     * @return A data source.
     * @throws WebServiceException If the lookup failed.
     */
    public static DataSource getManagementDS(String dataSourceName) throws WebServiceException {
        final String sourceName = dataSourceName == null ? DEFAULT_DATA_SOURCE_NAME : dataSourceName;
        try {
            LOGGER.config(ManagementMessages.WSM_5020_LOOKUP_DATASOURCE(sourceName));
            InitialContext initCtx = new InitialContext();
            return (DataSource) initCtx.lookup(sourceName);
        } catch (NamingException e) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5019_FAILED_LOOKUP(sourceName), e));
        }
    }

}