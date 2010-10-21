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

package com.sun.xml.ws.tx.coord.common.client;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.OneWayFeature;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.tx.coord.common.EndpointReferenceBuilder;
import com.sun.xml.ws.tx.coord.common.PendingRequestManager;
import com.sun.xml.ws.tx.coord.common.RegistrationIF;
import com.sun.xml.ws.tx.coord.common.WSCUtil;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterResponseType;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterType;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceFeature;
import java.util.ArrayList;
import java.util.List;


public abstract class RegistrationProxyBuilder {
    protected List<WebServiceFeature> features;
    protected EndpointReference to;
    protected String txId;
    protected long timeout;
    protected String callbackAddress;


    public RegistrationProxyBuilder feature(WebServiceFeature feature){
        if(feature == null) return  this;
        if(features == null) features = new ArrayList<WebServiceFeature>();
        features.add(feature);
        return  this;
   }

    public RegistrationProxyBuilder txIdForReference(String txId) {
        this.txId = txId;
        return  this;
    }

    public RegistrationProxyBuilder to(EndpointReference endpointReference){
        this.to = endpointReference;
        return this;
   }

    public RegistrationProxyBuilder timeout(long timeout){
        this.timeout = timeout;
        return this;
   }

    public RegistrationProxyBuilder callback(String callbackAddress){
        this.callbackAddress = callbackAddress;
        return this;
   }

   protected abstract String getDefaultCallbackAddress();

   protected abstract EndpointReferenceBuilder getEndpointReferenceBuilder();
   
    protected WebServiceFeature[] getEnabledFeatures(){
        return features.toArray(new WebServiceFeature[0]);
    }


    public RegistrationIF build(){
        if (callbackAddress == null)
            callbackAddress = getDefaultCallbackAddress();
        EndpointReference epr = getEndpointReferenceBuilder().address(callbackAddress).
                referenceParameter(WSCUtil.referenceElementTxId(txId), WSCUtil.referenceElementRoutingInfo()).build();
        WSEndpointReference wsepr = WSEndpointReference.create(epr);
        OneWayFeature oneway = new OneWayFeature(true, wsepr);
        this.feature(oneway);
        return null;
    }

    public abstract class RegistrationProxyF<T extends EndpointReference,K,P,D> implements RegistrationIF<T,K,P> {

        public BaseRegisterResponseType<T,P> registerOperation(BaseRegisterType<T,K> parameters){
            try {
                PendingRequestManager.ResponseBox box = PendingRequestManager.reqisterRequest(txId);
                asyncRegister(parameters.getDelegate());
                return box.getReponse(timeout);
            } finally {
                PendingRequestManager.removeRequest(txId);
            }
        }

        public abstract D getDelegate();

        public abstract void asyncRegister(K parameters);

        public abstract AddressingVersion getAddressingVersion();
    }
}
