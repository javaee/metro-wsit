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
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;
import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.api.tx.at.Transactional;
import com.sun.xml.ws.tx.at.common.client.CoordinatorProxyBuilder;
import com.sun.xml.ws.tx.at.common.client.ParticipantProxyBuilder;
import com.sun.xml.ws.tx.at.v10.client.CoordinatorProxyBuilderImpl;
import com.sun.xml.ws.tx.at.v10.NotificationBuilderImpl;
import com.sun.xml.ws.tx.at.v10.client.ParticipantProxyBuilderImpl;
import com.sun.xml.ws.tx.at.v10.types.Notification;
import com.sun.xml.ws.tx.coord.common.EndpointReferenceBuilder;

import javax.xml.ws.WebServiceFeature;


class WSATVersion10 extends WSATVersion<Notification>  {
    WSATVersion10() {
        super(Transactional.Version.WSAT10);
        addressingVersion =  AddressingVersion.MEMBER;
        soapVersion = SOAPVersion.SOAP_11;
    }

    @Override
    public WSATHelper getWSATHelper() {
        return WSATHelper.V10;
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
    public EndpointReferenceBuilder newEndpointReferenceBuilder() {
        return EndpointReferenceBuilder.MemberSubmission();
    }

    @Override
    public WebServiceFeature newAddressingFeature() {
        return new MemberSubmissionAddressingFeature();
    }

    @Override
    public NotificationBuilder newNotificationBuilder() {
        return new NotificationBuilderImpl();
    }

}