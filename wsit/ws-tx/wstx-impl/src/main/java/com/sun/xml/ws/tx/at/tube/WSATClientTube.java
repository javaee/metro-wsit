/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.tx.at.tube;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.assembler.dev.ClientTubelineAssemblyContext;
import com.sun.xml.ws.tx.at.WSATConstants;
import com.sun.xml.ws.api.tx.at.TransactionalFeature;
import com.sun.xml.ws.tx.dev.WSATRuntimeConfig;

import javax.xml.namespace.QName;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

public class WSATClientTube extends AbstractFilterTubeImpl implements WSATConstants {
    WSBinding m_wsbinding;
    WSATClient m_wsatClientHelper = new WSATClientHelper();
    private TransactionalFeature m_transactionalFeature;
    private WSDLPort m_port;

    public WSATClientTube(Tube next, ClientTubelineAssemblyContext context, TransactionalFeature feature) {  //for tube
        super(next);
        m_wsbinding = context.getBinding();
        m_transactionalFeature = feature;
        m_port = context.getWsdlPort();
    }

    private WSATClientTube(WSATClientTube that, TubeCloner cloner) {
        super(that, cloner);
        this.m_wsbinding = that.m_wsbinding;
        m_transactionalFeature = that.m_transactionalFeature;
        m_port =that.m_port;
    }


    public Set<QName> getHeaders() {
        return new HashSet<QName>();
    }

    @NotNull
    public NextAction processRequest(@NotNull Packet request) {
        try{
            doProcessRequest(request);
        } catch (Exception e){
            e.printStackTrace();
        }
        return super.processRequest(request);
    }

    private void doProcessRequest(Packet request) {
        URL url = request.endpointAddress.getURL();
        String host = url.getHost();
        int port = url.getPort();
        String localhostAndPort =
                WSATRuntimeConfig.getInstance().getHostAndPort().replace("http://","").replace("https://","");
        String thisServersInetAddress = null;
        String[] localhostAndPortArray;
        String thisServersHost= null;
        String thisServersHostWithoutDomain = null;
        String thisServersPort= null;
        try {
            thisServersInetAddress = InetAddress.getLocalHost().getHostAddress();
            localhostAndPortArray = localhostAndPort.split(":");
            thisServersHost= localhostAndPortArray[0];
            thisServersPort= localhostAndPortArray[1];
            thisServersHostWithoutDomain = thisServersHost.split(".")[0];
        } catch (Exception e) { 
        }
        boolean isSameHost =
                host.equals("localhost") || host.equals(thisServersHost)
                        || host.equals(thisServersInetAddress) || host.equals(thisServersHostWithoutDomain);
        boolean isSamePort = new String("" + port).equals(thisServersPort);
        boolean isColoc = isSameHost && isSamePort;
        request.invocationProperties.put("wsat.isColoc", isColoc);
        TransactionalAttribute transactionalAttribute =
                WSATTubeHelper.getTransactionalAttribute(m_transactionalFeature, request, m_port);
        transactionalAttribute.setSoapVersion(m_wsbinding.getSOAPVersion());
        List<Header> addedHeaders =
                m_wsatClientHelper.doHandleRequest(transactionalAttribute, request.invocationProperties);
        if (addedHeaders != null) {
            for (Header header : addedHeaders) {
                request.getMessage().getHeaders().add(header);
            }
        }
    }


    @NotNull
    public NextAction processResponse(@NotNull Packet response) {
        m_wsatClientHelper.doHandleResponse(response.invocationProperties);
        return super.processResponse(response);
    }


    @NotNull
    public NextAction processException(Throwable t) {
        Map<String, Object> map = com.sun.xml.ws.api.pipe.Fiber.current().getPacket().invocationProperties;
        m_wsatClientHelper.doHandleResponse(map);
        return super.processException(t);   
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new WSATClientTube(this, cloner);
    }
}
