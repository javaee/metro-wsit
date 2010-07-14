/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.assembler.jaxws;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.assembler.dev.ClientTubelineAssemblyContext;
import com.sun.xml.ws.assembler.dev.ServerTubelineAssemblyContext;
import com.sun.xml.ws.assembler.dev.TubeFactory;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.transport.SelectOptimalTransportFeature;
import com.sun.xml.ws.transport.TcpTransportFeature;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.wsit.TCPTransportPipeFactory;
import javax.xml.ws.WebServiceException;

/**
 * TubeFactory implementation creating one of the standard JAX-WS RI tubes
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class TransportTubeFactory implements TubeFactory {

    public Tube createTube(ClientTubelineAssemblyContext context) throws WebServiceException {
        if (isOptimizedTransportEnabled(context.getWsdlPort(), context.getPortInfo(), context.getBinding())) {
            return TCPTransportPipeFactory.doCreate(context.getWrappedContext(), false);
        } else {
            return context.getWrappedContext().createTransportTube();
        }
    }

    public Tube createTube(ServerTubelineAssemblyContext context) throws WebServiceException {
        return context.getTubelineHead();
    }

    /**
     * Checks to see whether OptimizedTransport is enabled or not.
     *
     * @param port the WSDLPort object
     * @param portInfo the WSPortInfo object
     * @return true if OptimizedTransport is enabled, false otherwise
     */
    private boolean isOptimizedTransportEnabled(WSDLPort port, WSPortInfo portInfo, WSBinding binding) {
        if (port == null && portInfo == null) {
            return false;
        }

        String schema;
        if (port != null) {
            schema = port.getAddress().getURI().getScheme();
        } else {
            schema = portInfo.getEndpointAddress().getURI().getScheme();
        }

        if (TCPConstants.PROTOCOL_SCHEMA.equals(schema)) {
            // if target endpoint URI starts with TCP schema - dont check policies, just return true
            return true;
        } else if (binding == null) {
            return false;
        }

        TcpTransportFeature tcpTransportFeature = binding.getFeature(TcpTransportFeature.class);
        SelectOptimalTransportFeature optimalTransportFeature = binding.getFeature(SelectOptimalTransportFeature.class);

        return (tcpTransportFeature != null && tcpTransportFeature.isEnabled()) &&
                (optimalTransportFeature != null && optimalTransportFeature.isEnabled());
    }
}
