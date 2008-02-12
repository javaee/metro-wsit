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
package com.sun.xml.ws.rm.runtime.testing;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.assembler.WsitClientTubeAssemblyContext;
import com.sun.xml.ws.assembler.WsitServerTubeAssemblyContext;
import com.sun.xml.ws.rm.RmWsException;
import com.sun.xml.ws.rm.localization.RmLogger;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class PacketFilteringTube extends AbstractFilterTubeImpl {

    private static final RmLogger LOGGER = RmLogger.getLogger(PacketFilteringTube.class);
    private final boolean isClientSide;
    private final PacketFilter[] filters;

    public PacketFilteringTube(PacketFilteringTube original, TubeCloner cloner) {
        super(original, cloner);
        this.isClientSide = original.isClientSide;
        this.filters = original.filters;
    }

    public PacketFilteringTube(WsitClientTubeAssemblyContext context) throws RmWsException {
        super(context.getTubelineHead());
        this.isClientSide = true;
        this.filters = getFilters(context.getBinding());
    }

    public PacketFilteringTube(WsitServerTubeAssemblyContext context) throws RmWsException {
        super(context.getTubelineHead());
        this.isClientSide = false;
        this.filters = getFilters(context.getEndpoint().getBinding());
    }

    @Override
    public PacketFilteringTube copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new PacketFilteringTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public void preDestroy() {
        super.preDestroy();
    }

    @Override
    public NextAction processRequest(Packet request) {
        if (isClientSide) {
            try {
                for (PacketFilter filter : filters) {
                    request = filter.filterClientRequest(request);
                }
            } catch (Exception ex) {
                LOGGER.logSevereException(ex);
                if (ex instanceof RuntimeException) {
                    return doThrow(ex);
                } else {
                    return doThrow(new WebServiceException(ex));
                }
            }

            if (request == null) {
                return doReturnWith(request); // TODO: is this ok?
            }
        }
        return super.processRequest(request);
    }

    @Override
    public NextAction processResponse(Packet response) {
        if (!isClientSide) {
            try {
                for (PacketFilter filter : filters) {
                    response = filter.filterServerResponse(response);
                }
            } catch (Exception ex) {
                LOGGER.logSevereException(ex);
                if (ex instanceof RuntimeException) {
                    return doThrow(ex);
                } else {
                    return doThrow(new WebServiceException(ex));
                }
            }
        }
        return super.processResponse(response); // TODO: is this ok?
    }

    private PacketFilter[] getFilters(WSBinding binding) {
        PacketFilteringFeature pfFeature = binding.getFeature(PacketFilteringFeature.class);
        return pfFeature.getFilters();
    }
}
