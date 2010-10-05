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

package com.sun.xml.wss.provider.wsit;

import java.util.HashMap;


import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.assembler.ServerPipelineHook;
import com.sun.xml.ws.policy.PolicyMap;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.assembler.dev.ServerTubelineAssemblyContext;

/**
 * This is used by JAXWSContainer to return proper 196 security and
 *  app server monitoing pipes to the StandAlonePipeAssembler and 
 *  TangoPipeAssembler
 */
public class ServerPipeCreator extends ServerPipelineHook {
     
    public ServerPipeCreator(){
    }

    @Override
    public Pipe createSecurityPipe(PolicyMap map, SEIModel sei,
            WSDLPort port, WSEndpoint owner, Pipe tail) {

        HashMap<Object, Object> props = new HashMap<Object, Object>();

        boolean httpBinding = BindingID.XML_HTTP.equals(owner.getBinding().getBindingId());
        props.put(PipeConstants.POLICY, map);
        props.put(PipeConstants.SEI_MODEL, sei);
        props.put(PipeConstants.WSDL_MODEL, port);
        props.put(PipeConstants.ENDPOINT, owner);
        props.put(PipeConstants.NEXT_PIPE, tail);
        props.put(PipeConstants.CONTAINER, owner.getContainer());
        return new ServerSecurityPipe(props, tail, httpBinding);
    }
   
    @Override
    public 
    @NotNull
    Tube createSecurityTube(ServerTubelineAssemblyContext context) {

        HashMap<Object, Object> props = new HashMap<Object, Object>();
        boolean httpBinding = BindingID.XML_HTTP.equals(context.getEndpoint().getBinding().getBindingId());
        props.put(PipeConstants.POLICY, context.getPolicyMap());
        props.put(PipeConstants.SEI_MODEL, context.getSEIModel());
        props.put(PipeConstants.WSDL_MODEL, context.getWsdlPort());
        props.put(PipeConstants.ENDPOINT, context.getEndpoint());
        //props.put(PipeConstants.NEXT_PIPE,context.getAdaptedTubelineHead());
        props.put(PipeConstants.NEXT_TUBE, context.getTubelineHead());
        props.put(PipeConstants.CONTAINER, context.getEndpoint().getContainer());
        //TODO: Convert GF security pipes to TUBE(s).
        ServerSecurityTube serverTube = new ServerSecurityTube(props, context.getTubelineHead(), httpBinding);
        return serverTube;
    }
}
