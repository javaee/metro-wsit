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

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.assembler.WsitServerTubeAssemblyContext;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.policy.ConfigurationManager;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class ServerRmTube extends AbstractFilterTubeImpl {
    private static final RmLogger LOGGER = RmLogger.getLogger(ServerRmTube.class);
    private final Configuration configuration;

    public ServerRmTube(ServerRmTube original, TubeCloner cloner) {
        super(original, cloner);
        
        this.configuration = original.configuration;
        // TODO: initialize all instance variables
    }

    public ServerRmTube(WsitServerTubeAssemblyContext context) {
        super(context.getTubelineHead());
        
        this.configuration = ConfigurationManager.createServiceConfigurationManager(context.getWsdlPort(), context.getEndpoint().getBinding()).getConfigurationAlternatives()[0];
        // TODO initialize all instance variables        
    }

    @Override
    public ServerRmTube copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new ServerRmTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processException(Throwable throwable) {
        LOGGER.entering();
        try {
            return super.processException(throwable);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processRequest(Packet requestPacket) {
        LOGGER.entering();
        try {
            if (isProtocolMessage(requestPacket.getMessage())) {
                Packet protocolResponsePacket = processProtocolRequest(requestPacket);
                return doReturnWith(protocolResponsePacket);
            } else {
                requestPacket = processApplicationRequest(requestPacket);
                // TODO: process RM headers
                if (configuration.isOrderedDelivery()) {                    
                    // TODO: ordered case processing (suspend until it's this message's turn)
                }
                return super.processRequest(requestPacket);
            }
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processResponse(Packet responsePacket) {
        LOGGER.entering();
        try {
            return super.processResponse(responsePacket);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public void preDestroy() {
        LOGGER.entering();
        try {
            super.preDestroy();
        } finally {
            LOGGER.exiting();
        }
    }

    private boolean isProtocolMessage(Message message) {
        return configuration.getRmVersion().isRmAction(
                message.getHeaders().getAction(configuration.getAddressingVersion(), configuration.getSoapVersion())
                );
    }

    private Packet processApplicationRequest(Packet requestPacket) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Packet processProtocolRequest(Packet requestPacket) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
