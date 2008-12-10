/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rm.runtime;

import com.sun.xml.ws.assembler.TubeFactory;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.assembler.ClientTubelineAssemblyContext;
import com.sun.xml.ws.assembler.ServerTubelineAssemblyContext;
import com.sun.xml.ws.rm.MakeConnectionSupportedFeature;
import com.sun.xml.ws.rm.ReliableMessagingFeature;
import javax.xml.ws.WebServiceException;

/**
 * This factory class is responsible for instantiating RX tubes based on 
 * the actual configuration of RX-related web services features.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 *
 * @see ReliableMessagingFeature
 * @see MakeConnectionSupportedFeature
 */
public final class RxTubeFactory implements TubeFactory {
    /**
     * Adds RM tube to the client-side tubeline, depending on whether RM is enabled or not.
     * 
     * @param context wsit client tubeline assembler context
     * @return new tail of the client-side tubeline
     */
    public Tube createTube(ClientTubelineAssemblyContext context) throws WebServiceException {
        RxConfiguration configuration = RxConfigurationFactory.INSTANCE.createConfiguration(context.getWsdlPort(), context.getBinding());

        Tube newTubelineHead = context.getTubelineHead();

        if (configuration.isMakeConnectionSupportEnabled()) {
            newTubelineHead = new McClientTube(configuration, newTubelineHead);
        }
        if (configuration.isReliableMessagingEnabled()) {
             newTubelineHead = new RmClientTube(configuration, newTubelineHead, context);
        }

        return newTubelineHead;
    }

    /**
     * Adds RM tube to the service-side tubeline, depending on whether RM is enabled or not.
     * 
     * @param context wsit service tubeline assembler context
     * @return new head of the service-side tubeline
     */
    public Tube createTube(ServerTubelineAssemblyContext context) throws WebServiceException {
        RxConfiguration configuration = RxConfigurationFactory.INSTANCE.createConfiguration(context.getWsdlPort(), context.getEndpoint().getBinding());

        Tube newTubelineHead = context.getTubelineHead();

        if (configuration.isReliableMessagingEnabled()) {
             newTubelineHead = AbstractRmServerTube.getInstance(configuration, newTubelineHead);
        }
        if (configuration.isMakeConnectionSupportEnabled()) {
            newTubelineHead = new McServerTube(configuration, newTubelineHead);
        }
        
        return newTubelineHead;
    }

//    /**
//     * Checks to see whether WS-ReliableMessaging is enabled or not.
//     *
//     * @param policyMap policy map for {@link this} assembler
//     * @param port wsdl:port
//     * @return true if ReliableMessaging is enabled, false otherwise
//     */
//    private boolean isReliableMessagingEnabled(PolicyMap policyMap, WSDLPort port) throws WebServiceException {
//        if (policyMap == null || port == null) {
//            return false;
//        }
//
//        try {
//            PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(port.getOwner().getName(), port.getName());
//            Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);
//            if (policy == null) {
//                return false;
//            } else {
//                return policy.contains(RmVersion.WSRM10.policyNamespaceUri) || policy.contains(RmVersion.WSRM11.policyNamespaceUri);
//            }
//        } catch (PolicyException e) {
//            throw new WebServiceException(e);
//        }
//    }
}
