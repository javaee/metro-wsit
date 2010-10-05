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
package com.sun.xml.ws.tx.at.v10.client;

import com.sun.xml.ws.tx.at.common.client.ParticipantProxyBuilder;
import com.sun.xml.ws.tx.at.common.ParticipantIF;
import com.sun.xml.ws.tx.at.common.WSATVersion;
import com.sun.xml.ws.tx.at.v10.types.Notification;
import com.sun.xml.ws.tx.at.v10.types.ParticipantPortType;

import java.io.Closeable;
import java.io.IOException;

/**
 * This is the base class for building client proxy for invoking WSAT10 Participant services..
 */
public class ParticipantProxyBuilderImpl extends ParticipantProxyBuilder<Notification> {
    final static WSAT10Service service = new WSAT10Service();

    public ParticipantProxyBuilderImpl() {
        super(WSATVersion.v10);
    }

    @Override
    public ParticipantIF<Notification> build() {
        return new ParticipantProxyImpl();
    }

    class ParticipantProxyImpl implements ParticipantIF<Notification> {
       ParticipantPortType port;

        ParticipantProxyImpl() {
            port = service.getParticipantPortTypePort(to,getEnabledFeatures());
       }

        @Override
        public String toString() {
            return getClass().getName() + " hashcode:"+hashCode()+ " to(EndpointReference):"+to + " port:"+port;
        }

        public void prepare(Notification parameters) {
            port.prepare(parameters);
            // do not close port as we will cache for commit or rollback
        }

        public void commit(Notification parameters) {
            port.commit(parameters);
            closePort();
        }

        public void rollback(Notification parameters) {
            port.rollback(parameters);
            closePort();
        }

        private void closePort() {
            try {
                ((Closeable)port).close();
            } catch (IOException e) {
                e.printStackTrace(); 
            }
        }
    }

}