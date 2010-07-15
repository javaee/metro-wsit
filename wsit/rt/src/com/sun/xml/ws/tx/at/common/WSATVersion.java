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
import com.sun.xml.ws.tx.coord.common.EndpointReferenceBuilder;

import javax.xml.ws.WebServiceFeature;

/**
 *
 * A class absorbs differences of WSAT versions.
 */
public abstract class WSATVersion<T>{
    public final static WSATVersion<com.sun.xml.ws.tx.at.v10.types.Notification> v10 = new WSATVersion10();
    public final static WSATVersion<com.sun.xml.ws.tx.at.v11.types.Notification> v11 = new WSATVersion11();

    private Transactional.Version version;
    protected AddressingVersion addressingVersion;
    protected SOAPVersion soapVersion;

    public static WSATVersion getInstance(Transactional.Version version){
    if (Transactional.Version.WSAT10 == version||Transactional.Version.DEFAULT == version) {
            return v10;
        }else if (Transactional.Version.WSAT11 == version || Transactional.Version.WSAT12 == version) {
            return v11;
        } else {
            throw new IllegalArgumentException(version + "is not a supported ws-at version");
        }
    }

     WSATVersion(Transactional.Version version) {
        this.version = version;
    }

     public abstract WSATHelper getWSATHelper();

    public AddressingVersion getAddressingVersion() {
        return addressingVersion;
    }

    public SOAPVersion getSOPAVersion() {
        return soapVersion;
    }

    public Transactional.Version getVersion() {
        return version;
    }

    public abstract CoordinatorProxyBuilder<T> newCoordinatorProxyBuilder();
    public abstract ParticipantProxyBuilder<T> newParticipantProxyBuilder();
    public abstract NotificationBuilder<T> newNotificationBuilder();
    public abstract EndpointReferenceBuilder newEndpointReferenceBuilder();
    public abstract WebServiceFeature newAddressingFeature();
}
