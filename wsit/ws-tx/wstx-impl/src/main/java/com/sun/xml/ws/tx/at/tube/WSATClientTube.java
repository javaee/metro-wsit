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

import javax.xml.namespace.QName;
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
        TransactionalAttribute transactionalAttribute =
                WSATTubeHelper.getTransactionalAttribute(m_transactionalFeature, request, m_port);
        transactionalAttribute.setSoapVersion(m_wsbinding.getSOAPVersion());
        List<Header> addedHeaders = m_wsatClientHelper.doHandleRequest(transactionalAttribute, request.invocationProperties);
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
