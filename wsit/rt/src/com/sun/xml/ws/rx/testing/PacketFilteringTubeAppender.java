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
package com.sun.xml.ws.rx.testing;

import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.assembler.TubeAppender;
import com.sun.xml.ws.assembler.WsitClientTubeAssemblyContext;
import com.sun.xml.ws.assembler.WsitServerTubeAssemblyContext;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.rx.mc.api.MakeConnectionSupportedFeature;
import com.sun.xml.ws.rx.mc.policy.spi_impl.McFeatureConfigurator;
import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeature;
import com.sun.xml.ws.rx.rm.policy.spi_impl.RmFeatureConfigurator;
import com.sun.xml.ws.rx.rm.runtime.RmConfiguration;
import com.sun.xml.ws.rx.rm.runtime.RmConfigurationFactory;
import java.util.Collection;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class PacketFilteringTubeAppender implements TubeAppender {

    public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
        if (isPacketFilteringEnabled(context.getBinding())) {
            RmConfiguration configuration = createConfiguration(
                    context.getWsdlPort(),
                    context.getBinding(),
                    context.getPolicyMap());


            return new PacketFilteringTube(configuration, context.getTubelineHead(), context);
        } else {
            return context.getTubelineHead();
        }
    }

    public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
        if (isPacketFilteringEnabled(context.getEndpoint().getBinding())) {
            RmConfiguration configuration = createConfiguration(
                    context.getWsdlPort(),
                    context.getEndpoint().getBinding(),
                    context.getPolicyMap());

            return new PacketFilteringTube(configuration, context.getTubelineHead(), context);
        } else {
            return context.getTubelineHead();
        }
    }

    private boolean isPacketFilteringEnabled(WSBinding binding) {
        PacketFilteringFeature pfFeature = binding.getFeature(PacketFilteringFeature.class);
        return pfFeature != null && pfFeature.isEnabled() && pfFeature.hasFilters();
    }

    private static RmConfiguration createConfiguration(final WSDLPort port, final WSBinding binding, final PolicyMap policyMap) {
        ReliableMessagingFeature rmf = null;
        MakeConnectionSupportedFeature mcf = null;
        try {
            final PolicyMapKey key = PolicyMap.createWsdlEndpointScopeKey(port.getOwner().getName(), port.getName());
            Collection<WebServiceFeature> features;

            features = new RmFeatureConfigurator().getFeatures(key, policyMap);
            if (features != null) {
                WebServiceFeature feature = features.iterator().next();
                if (feature instanceof ReliableMessagingFeature) {
                    rmf = (ReliableMessagingFeature) feature;
                }
            }

            features = new McFeatureConfigurator().getFeatures(key, policyMap);
            if (features != null) {
                WebServiceFeature feature = features.iterator().next();
                if (feature instanceof MakeConnectionSupportedFeature) {
                    mcf = (MakeConnectionSupportedFeature) feature;
                }
            }

            return RmConfigurationFactory.INSTANCE.createInstance(
                    rmf,
                    mcf,
                    port,
                    binding);
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
    }
}
