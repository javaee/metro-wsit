/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

/*
 * PipelineAssemblerFactory.java
 *
 * @author Mike Grogan
 * Created on February 6, 2006, 12:38 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime;

import com.sun.xml.ws.addressing.jaxws.WsaClientPipe;
import com.sun.xml.ws.addressing.jaxws.WsaServerPipe;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipelineAssembler;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.client.dispatch.StandalonePipeAssembler;
import com.sun.xml.ws.rm.jaxws.runtime.client.RMClientPipe;
import com.sun.xml.ws.rm.jaxws.runtime.server.RMServerPipe;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.BindingID;


/**
 *
 */
public class PipelineAssemblerFactory extends
        com.sun.xml.ws.api.pipe.PipelineAssemblerFactory {

    public PipelineAssembler doCreate(BindingID bindingId) {

        return new StandalonePipeAssembler() {
            @Override
            public Pipe createClient(EndpointAddress address, WSDLPort wsdlModel, WSService service, WSBinding binding) {
                Pipe p;
                Pipe tp = createTransport(address, wsdlModel, service, binding);
                p = new WsaClientPipe(wsdlModel,binding, tp);
                Pipe retPipe = new RMClientPipe(wsdlModel, service, binding, null, p );

                return retPipe;

            }

            @Override
            public Pipe createServer(SEIModel seiModel, WSDLPort wsdlModel, WSEndpoint endpoint, Pipe terminal) {
                Pipe serverPipe = new RMServerPipe(wsdlModel,  endpoint, terminal);
                return new WsaServerPipe(seiModel, wsdlModel, endpoint.getBinding(), serverPipe);
            }

            @Override
            protected Pipe createTransport(EndpointAddress address, WSDLPort wsdlModel, WSService service, WSBinding binding) {
                return super.createTransport(address, wsdlModel,service,binding);
            }

        };
    }

}
