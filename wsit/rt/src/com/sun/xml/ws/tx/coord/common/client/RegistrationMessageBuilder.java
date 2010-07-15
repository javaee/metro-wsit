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
package com.sun.xml.ws.tx.coord.common.client;

import org.w3c.dom.Element;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterType;
import com.sun.xml.ws.tx.coord.common.EndpointReferenceBuilder;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterResponseType;
import com.sun.xml.ws.tx.coord.common.WSCUtil;

import javax.xml.ws.EndpointReference;


public abstract class RegistrationMessageBuilder {
    protected boolean durable = true;
    protected Element txIdElement;
    protected Element routingElement;
    protected String participantAddress;
    protected String protocolIdentifier;


    public RegistrationMessageBuilder durable(boolean durable) {
        this.durable = durable;
        return this;
    }

    public RegistrationMessageBuilder txId(String txId) {
        txIdElement = WSCUtil.referenceElementTxId(txId);
        return this;
    }

    public RegistrationMessageBuilder routing() {
        routingElement = WSCUtil.referenceElementRoutingInfo();
        return this;
    }

    public RegistrationMessageBuilder participantAddress(String address) {
        this.participantAddress = address;
        return this;
    }

    public RegistrationMessageBuilder protocolIdentifier(String protocolIdentifier) {
        this.protocolIdentifier = protocolIdentifier;
        return this;
    }

    public BaseRegisterType build() {
        if (participantAddress == null)
            participantAddress = getDefaultParticipantAddress();
        BaseRegisterType registerType = newRegistrationRequest();
        registerType.setParticipantProtocolService(getParticipantProtocolService());
        registerType.setProtocolIdentifier(protocolIdentifier);
        return registerType;
    }

    protected EndpointReference getParticipantProtocolService() {
        EndpointReferenceBuilder eprBuilder = getEndpointReferenceBuilder();
        return eprBuilder.address(participantAddress).
                referenceParameter(txIdElement).
                referenceParameter(routingElement).
                build();
    }

    protected abstract BaseRegisterType newRegistrationRequest();

    protected abstract String getDefaultParticipantAddress();

    protected abstract BaseRegisterResponseType buildRegistrationResponse();

    protected abstract EndpointReferenceBuilder getEndpointReferenceBuilder();


}
