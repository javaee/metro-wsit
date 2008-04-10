/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.xml.ws.assembler.ClientPipelineHook;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.policy.PolicyMap;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.helper.PipeAdapter;
import com.sun.xml.ws.assembler.WsitClientTubeAssemblyContext;

/**
 * This is used by WSClientContainer to return proper 196 security pipe
 * to the StandAlonePipeAssembler and TangoPipeAssembler
 */
public class ClientPipeCreator extends ClientPipelineHook {
        
    public ClientPipeCreator(){
    }

    
    @Override
    public Pipe createSecurityPipe(PolicyMap map, 
            ClientPipeAssemblerContext ctxt, Pipe tail) {
        HashMap propBag = new HashMap();
        propBag.put(PipeConstants.POLICY, map);
        propBag.put(PipeConstants.WSDL_MODEL, ctxt.getWsdlModel());
        propBag.put(PipeConstants.SERVICE, ctxt.getService());
        propBag.put(PipeConstants.BINDING, ctxt.getBinding());
        propBag.put(PipeConstants.ENDPOINT_ADDRESS, ctxt.getAddress());
    	propBag.put(PipeConstants.NEXT_PIPE,tail);
        propBag.put(PipeConstants.CONTAINER,ctxt.getContainer());
        ClientSecurityPipe ret = new ClientSecurityPipe(propBag, tail);
        return ret;
    }
    
    
    @Override
    public @NotNull Tube createSecurityTube(WsitClientTubeAssemblyContext context) {
        HashMap propBag = new HashMap();
        propBag.put(PipeConstants.POLICY, context.getPolicyMap());
        propBag.put(PipeConstants.WSDL_MODEL, context.getWrappedContext().getWsdlModel());
        propBag.put(PipeConstants.SERVICE, context.getService());
        propBag.put(PipeConstants.BINDING, context.getBinding());
        propBag.put(PipeConstants.ENDPOINT_ADDRESS, context.getAddress());
        propBag.put(PipeConstants.NEXT_PIPE,context.getAdaptedTubelineHead());
        propBag.put(PipeConstants.CONTAINER, context.getContainer());
        ClientSecurityPipe ret = new ClientSecurityPipe(propBag, context.getAdaptedTubelineHead());
        
        return PipeAdapter.adapt(ret);
    }
    
}
