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
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.assembler.dev.ServerTubelineAssemblyContext;
import com.sun.xml.ws.tx.at.WSATConstants;
import com.sun.xml.ws.api.tx.at.TransactionalFeature;

import javax.xml.namespace.QName;
import java.util.Set;


/**
 * Typical inbound message:
 * <p/>
 * <s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope" xmlns:a="http://www.w3.org/2005/08/addressing"><s:Header><a:Action s:mustUnderstand="1">http://tem
 * puri.org/IService/GetData</a:Action><a:MessageID>urn:uuid:353ec55b-3e04-4e13-9471-9652858f7680</a:MessageID><a:ReplyTo><a:Address>http://www.w3.org/2005/08/addr
 * essing/anonymous</a:Address></a:ReplyTo>
 * <p/>
 * <CoordinationContext s:mustUnderstand="1" xmlns="http://schemas.xmlsoap.org/ws/2004/10/wscoor" xmlns:mstx="http://schema
 * s.microsoft.com/ws/2006/02/transactions"><wscoor:Identifier xmlns:wscoor="http://schemas.xmlsoap.org/ws/2004/10/wscoor">urn:uuid:79c06523-2392-45d7-9b66-8cc06d0
 * 07d2d</wscoor:Identifier><Expires>599552</Expires><CoordinationType>http://schemas.xmlsoap.org/ws/2004/10/wsat</CoordinationType>
 * <p/>
 * <RegistrationService><Address x
 * mlns="http://schemas.xmlsoap.org/ws/2004/08/addressing">https://pparkins-us:453/WsatService/Registration/Coordinator/</Address>
 * <p/>
 * <ReferenceParameters xmlns="http:
 * //schemas.xmlsoap.org/ws/2004/08/addressing"><mstx:RegisterInfo><mstx:LocalTransactionId>79c06523-2392-45d7-9b66-8cc06d007d2d</mstx:LocalTransactionId></mstx:Re
 * gisterInfo></ReferenceParameters>
 * <p/>
 * </RegistrationService>
 * <p/>
 * <p/>
 * <mstx:IsolationLevel>0</mstx:IsolationLevel><mstx:LocalTransactionId>79c06523-2392-45d7-9b66-8cc06d007d2d
 * </mstx:LocalTransactionId><PropagationToken xmlns="http://schemas.microsoft.com/ws/2006/02/tx/oletx">AQAAAAMAAAAjZcB5kiPXRZtmjMBtAH0tAAAQAAAAAACIAAAAAMToedzE6Hk
 * 0W6xnBOupAC/M+Xk0W6xnUOypANwmcAFYCxcAlOupAGZjYThlYTc3LTYwYjQtNGEwNS1hODI0LWUxM2NjYjQ3MzVjYQABAAALAAAAZM1kzSEAAABQUEFSS0lOUy1VUwAYAAAAUABQAEEAUgBLAEkATgBTAC0AVQB
 * TAAAAAQAAAAEAAAAeAAAAdGlwOi8vcHBhcmtpbnMtdXMubG9jYWxkb21haW4vAAA=</PropagationToken>
 * </CoordinationContext>
 * <p/>
 * <p/>
 * <p/>
 * <a:To s:mustUnderstand="1">http://localhost:7001/Hello
 * TXWeb/DataService</a:To></s:Header><s:Body><GetData xmlns="http://tempuri.org/"><value>1</value></GetData></s:Body></s:Envelope>--------------------
 */

public class WSATServerTube extends AbstractFilterTubeImpl implements WSATConstants {
    private static final String  WSATATTRIBUTE = ".wsee.wsat.attribute";
    ServerTubelineAssemblyContext m_context;    
    private WSDLPort m_port;
    private TransactionalFeature m_transactionalFeature;
    WSATServer m_wsatServerHelper = new WSATServerHelper();

    public WSATServerTube(Tube next, ServerTubelineAssemblyContext context, TransactionalFeature feature) {  //for tube
        super(next);
        m_context = context;
        m_port = context.getWsdlPort();
        m_transactionalFeature = feature;
    }

    public WSATServerTube(WSATServerTube that, TubeCloner cloner) {
        super(that,cloner);
        this.m_context = that.m_context;
        this.m_port = that.m_port;
        m_transactionalFeature = that.m_transactionalFeature;
    }


    public
    @NotNull
    NextAction processRequest(Packet request) {
      TransactionalAttribute tx = WSATTubeHelper.getTransactionalAttribute(m_transactionalFeature, request, m_port);
      tx.setSoapVersion(m_context.getEndpoint().getBinding().getSOAPVersion());
      request.invocationProperties.put(WSATATTRIBUTE, tx);
      HeaderList headers = request.getMessage().getHeaders();
      m_wsatServerHelper.doHandleRequest(headers, tx);
      //this is workaround for the well-known MU tube problem - MU tube is placed before security tubes and WS-AT tubes
      if (!tx.isEnabled()) {
        Set<QName> notUnderstoodHeaders =null;//todoremove = MUHeaderHelper.getMUWSATHeaders(headers);
        if (notUnderstoodHeaders != null && !notUnderstoodHeaders.isEmpty())
            return super.processRequest(request); //todoremove        return doReturnWith(request.createResponse(MUHeaderHelper.createMUSOAPFaultMessage(tx.getSoapVersion(), notUnderstoodHeaders)));
      }
      return super.processRequest(request);
    }

    public
    @NotNull
    NextAction processResponse(Packet response) {
        TransactionalAttribute tx = (TransactionalAttribute) response.invocationProperties.get(WSATATTRIBUTE);
        m_wsatServerHelper.doHandleResponse(tx);
        return super.processResponse(response);
    }

    public
    @NotNull
    NextAction processException(Throwable t) {
        m_wsatServerHelper.doHandleException(t);
        return super.processException(t);
    }

    public void preDestroy() {
        super.preDestroy();
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new WSATServerTube(this, cloner);
    }


    NextAction doProcessResponse(Packet request) {
        return super.processResponse(request);
    }

}
