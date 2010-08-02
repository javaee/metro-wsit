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
package com.sun.xml.ws.tx.at.common;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.tx.at.api.Transactional;
import com.sun.xml.ws.tx.at.common.client.CoordinatorProxyBuilder;
import com.sun.xml.ws.tx.at.common.client.ParticipantProxyBuilder;
import com.sun.xml.ws.tx.at.v11.client.CoordinatorProxyBuilderImpl;
import com.sun.xml.ws.tx.at.v11.NotificationBuilderImpl;
import com.sun.xml.ws.tx.at.v11.client.ParticipantProxyBuilderImpl;
import com.sun.xml.ws.tx.at.v11.types.Notification;
import com.sun.xml.ws.tx.coord.common.EndpointReferenceBuilder;

import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;


class WSATVersion11 extends WSATVersion<Notification>  {

    WSATVersion11() {
        super(Transactional.Version.WSAT11);
        addressingVersion =  AddressingVersion.W3C;
        soapVersion = SOAPVersion.SOAP_11;
    }

    @Override
    public WSATHelper getWSATHelper() {
        return WSATHelper.V11;
    }

    @Override
    public CoordinatorProxyBuilder<Notification> newCoordinatorProxyBuilder() {
        return new CoordinatorProxyBuilderImpl();
    }

    @Override
    public ParticipantProxyBuilder<Notification> newParticipantProxyBuilder() {
        return new ParticipantProxyBuilderImpl();
    }

    @Override
    public NotificationBuilder<Notification> newNotificationBuilder() {
        return new NotificationBuilderImpl();
    }

    @Override
    public EndpointReferenceBuilder newEndpointReferenceBuilder() {
        return EndpointReferenceBuilder.W3C();
    }

    @Override
    public WebServiceFeature newAddressingFeature() {
        return new AddressingFeature();
    }
}