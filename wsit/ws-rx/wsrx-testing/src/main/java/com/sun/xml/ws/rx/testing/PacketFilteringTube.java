/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.assembler.dev.ClientTubelineAssemblyContext;
import com.sun.xml.ws.assembler.dev.ServerTubelineAssemblyContext;
import com.sun.xml.ws.rx.rm.runtime.RmConfiguration;
import com.sun.xml.ws.rx.rm.runtime.RuntimeContext;
import com.sun.xml.ws.rx.util.Communicator;
import java.io.IOException;
import java.util.List;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
class PacketFilteringTube extends AbstractFilterTubeImpl {

    private static final Logger LOGGER = Logger.getLogger(PacketFilteringTube.class);
    private final boolean isClientSide;
    private final RuntimeContext rc;
    private final List<PacketFilter> filters;

    public PacketFilteringTube(PacketFilteringTube original, TubeCloner cloner) {
        super(original, cloner);
        this.isClientSide = original.isClientSide;
        this.rc = original.rc;
        this.filters = original.filters;
    }
    public PacketFilteringTube(RmConfiguration configuration, Tube tubelineHead, ClientTubelineAssemblyContext context) {
        super(tubelineHead);
        this.isClientSide = true;

        RuntimeContext.Builder rcBuilder = RuntimeContext.builder(
                configuration,
                Communicator.builder("packet-filtering-client-tube-communicator")
                .tubelineHead(super.next)
                .addressingVersion(configuration.getAddressingVersion())
                .soapVersion(configuration.getSoapVersion())
                .jaxbContext(configuration.getRuntimeVersion().getJaxbContext(configuration.getAddressingVersion()))
                .container(context.getContainer())
                .build());

        this.rc = rcBuilder.build();

        this.filters = getConfiguredFilters(context.getBinding(), rc);
    }

    public PacketFilteringTube(RmConfiguration configuration, Tube tubelineHead, ServerTubelineAssemblyContext context) {
        super(tubelineHead);
        this.isClientSide = false;

        RuntimeContext.Builder rcBuilder = RuntimeContext.builder(
                configuration,
                Communicator.builder("packet-filtering-server-tube-communicator")
                .tubelineHead(super.next)
                .addressingVersion(configuration.getAddressingVersion())
                .soapVersion(configuration.getSoapVersion())
                .jaxbContext(configuration.getRuntimeVersion().getJaxbContext(configuration.getAddressingVersion()))
                .container(context.getEndpoint().getContainer())
                .build());

        this.rc = rcBuilder.build();

        this.filters = getConfiguredFilters(context.getEndpoint().getBinding(), rc);
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
        rc.close();
        
        super.preDestroy();
    }

    @Override
    public NextAction processRequest(Packet request) {
        if (isClientSide) {
            try {
                for (PacketFilter filter : filters) {
                    if (request != null) {
                        request = filter.filterClientRequest(request);
                    } else {
                        break;
                    }
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
                // simulate IO error
                return doThrow(new WebServiceException(new IOException("Simulated IO error while sending a request")));
            }
        }
        return super.processRequest(request);
    }

    @Override
    public NextAction processResponse(Packet response) {
        if (!isClientSide) {
            try {
                for (PacketFilter filter : filters) {
                    if (response != null) {
                        response = filter.filterServerResponse(response);
                    } else {
                        break;
                    }
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
        return super.processResponse(response);
    }

    private List<PacketFilter> getConfiguredFilters(WSBinding binding, RuntimeContext context) {
        PacketFilteringFeature pfFeature = binding.getFeature(PacketFilteringFeature.class);
        return pfFeature.createFilters(context);
    }
}
