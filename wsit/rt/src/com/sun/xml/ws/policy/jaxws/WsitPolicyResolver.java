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

package com.sun.xml.ws.policy.jaxws;

import com.sun.xml.ws.api.policy.PolicyResolver;
import com.sun.xml.ws.api.policy.PolicyResolverFactory;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyMapMutator;
import com.sun.xml.ws.policy.WsitPolicyUtil;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;

import java.net.URL;
import java.util.Collection;
import javax.xml.ws.WebServiceException;

/**
 * Load and process the WSIT configuration files. If they are not present, fall
 * back to the JAX-WS default implementation.
 *
 * @author Rama Pulavarthi
 * @author Fabian Ritzmann
 */
public class WsitPolicyResolver implements PolicyResolver {

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(WsitPolicyResolver.class);

    public PolicyMap resolve(ServerContext context) throws WebServiceException {
        final Class endpointClass = context.getEndpointClass();
        final String configId = endpointClass == null ? null : endpointClass.getName();
        if (!context.hasWsdl()) {
            // Parse WSIT config file.
            PolicyMap map = null;
            try {
                Collection<PolicyMapMutator> mutators = context.getMutators();
                // No need to check if configId != null because it never is if we have WSDL
                map = PolicyConfigParser.parse(configId, context.getContainer(),
                                               mutators.toArray(new PolicyMapMutator[mutators.size()]));
            } catch (PolicyException e) {
                throw LOGGER.logSevereException(new WebServiceException(
                        LocalizationMessages.WSP_5006_FAILED_TO_READ_WSIT_CONFIG_FOR_ID(configId), e));
            }
            if (map == null)
                LOGGER.config(LocalizationMessages.WSP_5008_CREATE_POLICY_MAP_FOR_CONFIG(configId));
            else {
                // Validate server-side Policies such that there exists a single alternative in each scope.
                WsitPolicyUtil.validateServerPolicyMap(map);
            }
            return map;
        }
        else {
            try {
                if (configId != null) {
                    // Server-side, there should be only one policy configuration either WSDL or WSIT config.
                    final URL wsitConfigFile = PolicyConfigParser.findConfigFile(configId, context.getContainer());
                    if (wsitConfigFile != null) {
                        LOGGER.warning(LocalizationMessages.WSP_5024_WSIT_CONFIG_AND_WSDL(wsitConfigFile));
                    }
                }
                return PolicyResolverFactory.DEFAULT_POLICY_RESOLVER.resolve(context);
            } catch (PolicyException e) {
                throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_5023_FIND_WSIT_CONFIG_FAILED(), e));
            }
        }
    }

    public PolicyMap resolve(ClientContext context) {
        PolicyMap effectivePolicyMap;
        try {
            final PolicyMap clientConfigPolicyMap = PolicyConfigParser.parse(
                    PolicyConstants.CLIENT_CONFIGURATION_IDENTIFIER, context.getContainer());
            if (clientConfigPolicyMap == null) {
                LOGGER.config(LocalizationMessages.WSP_5014_CLIENT_CONFIG_PROCESSING_SKIPPED());
                effectivePolicyMap = context.getPolicyMap();
            } else {
                //Merge Policy Configuration from WSDL and configuration file.
                effectivePolicyMap = WsitPolicyUtil.mergePolicyMap(context.getPolicyMap(), clientConfigPolicyMap);
            }
        } catch (PolicyException e) {
            throw LOGGER.logSevereException(new WebServiceException(
                    LocalizationMessages.WSP_5004_ERROR_WHILE_PROCESSING_CLIENT_CONFIG(), e));
        }
        // Chooses best alternative and sets it as effective Policy in each scope.
        if(effectivePolicyMap != null)
            return WsitPolicyUtil.doAlternativeSelection(effectivePolicyMap);
        else
            return null;
    }
}
