/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

/*
 * ClientSequenceFactory.java
 *
 *
 * @author Mike Grogan
 * Created on January 19, 2007, 10:16 AM
 *
 */
package com.sun.xml.ws.api.rm.client;

import com.sun.xml.ws.api.rm.SequenceSettings;
import javax.xml.namespace.QName;
import com.sun.xml.ws.rm.jaxws.runtime.client.RMSource;

/**
 * Factory used by clients who need to provide their own
 * sequences for use by the RMClient runtime.  Typically, the
 * client application is maintaining its own persistent store
 * of sent messages and needs to monitor their acknowledgements.
 */
public class ClientSequenceFactory {

    private ClientSequenceFactory() {
    }

    /**
     * Establishes a new session with the endpoint.  The returned (@link ClientSequence}
     * can be specified for use by one or more client instances connected to the endpoint
     * by setting it as the value of the <code>com.sun.xml.rm.session</code> property.
     *
     * @param service The JAX-WS service representing the service.
     * @param portName The name of the endpoint.
     * @return The ClientSequence obtained by sending a RM Protol CreateSequence message
     *         to the endpoint.  Returns <code>null</code> if the
     *         sequence creation fails.
     */
    public static ClientSequence createSequence(javax.xml.ws.Service service, QName portName) {
        return RMSource.getRMSource().createSequence(service, portName);
    }

    /**
     * Re-establishes a session using persisted data from an existing session.
     * 
     * @param service The JAX-WS service representing the service.
     * @param portName The name of the endpoint.
     * @return The ClientSequence obtained by sending a RM Protol CreateSequence message
     *         to the endpoint and replacing its state with the state saved
     *         previously from another session with the same endpoint. Returns
     *         <code>null</code> is sequence creation fails.
     */
    public static ClientSequence createSequence(javax.xml.ws.Service service, QName portName, SequenceSettings settings) {
        return RMSource.getRMSource().createSequence(service, portName, settings.getSequenceId(), settings.getCompanionSequenceId());
    }
}
