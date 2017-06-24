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

package com.sun.xml.ws.tx.at.v10.client;

import com.sun.xml.ws.tx.at.common.client.CoordinatorProxyBuilder;
import com.sun.xml.ws.tx.at.common.CoordinatorIF;
import com.sun.xml.ws.tx.at.common.WSATVersion;
import com.sun.xml.ws.tx.at.v10.types.CoordinatorPortType;
import com.sun.xml.ws.tx.at.v10.types.Notification;
import com.sun.xml.ws.client.WSServiceDelegate;

import java.io.Closeable;
import java.io.IOException;

/**
 * 
 * @exclude
 *
 * This is the base class for building client proxy for invoking WSAT10 coordinator services..
 */
public class CoordinatorProxyBuilderImpl extends CoordinatorProxyBuilder<Notification> {
    private static final WSAT10Service service  = new WSAT10Service();

    public CoordinatorProxyBuilderImpl() {
        super(WSATVersion.v10);
    }

    @Override
    public CoordinatorIF<Notification> build() {
        return new CoordinatorProxyImpl();
    }

    class CoordinatorProxyImpl implements CoordinatorIF<Notification> {

        CoordinatorPortType port;

        CoordinatorProxyImpl() {
            port = service.getCoordinatorPortTypePort(to,getEnabledFeatures());
        }

        public void preparedOperation(Notification parameters) {
            port.preparedOperation(parameters);
            closePort();
        }

        public void abortedOperation(Notification parameters) {
            port.abortedOperation(parameters);
            closePort();
        }

        public void readOnlyOperation(Notification parameters) {
            port.readOnlyOperation(parameters);
            closePort();
        }

        public void committedOperation(Notification parameters) {
            port.committedOperation(parameters);
            closePort();
        }

        public void replayOperation(Notification parameters) {
           port.replayOperation(parameters);
           closePort();
        }

        private void closePort() {
            try {
                ((Closeable)port).close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
