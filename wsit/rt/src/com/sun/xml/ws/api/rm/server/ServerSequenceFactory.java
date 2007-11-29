/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * ServerSequenceFactory.java
 *
 *
 * @author Mike Grogan
 * Created on January 19, 2007, 11:58 AM
 *
 */
package com.sun.xml.ws.api.rm.server;

import com.sun.xml.ws.api.rm.SequenceSettings;
import com.sun.xml.ws.rm.jaxws.runtime.server.RMDestination;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.localization.RmLogger;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Factory class contains a method that can be used to re-initialize a server-side
 * sequence using persisted data after a system failure.
 */
public class ServerSequenceFactory {

    private static RmLogger logger = RmLogger.getLogger(ServerSequenceFactory.class);

    private ServerSequenceFactory() {
    }

    /**
     * Factory method initializes a server-side sequence using saved data.  This
     * is necessary after a restart in order for the system to recognize incoming
     * messages belonging to a sequence established before the restart.
     *
     * @param settings A {@link SequenceSettings} obtained by an earlier call to
     *          ServerSequence.getSequenceSettings and persisted.
     * @return The reinitialized sequence.  It will use the initialization settings
     *          from the original sequence, but will not contain state 
     *          concerning which message numbers have  been  received.  
     */
    public static ServerSequence createSequence(SequenceSettings settings) {
        try {
            return RMDestination.getRMDestination().createSequence(
                    new URI(settings.getAcksTo()),
                    settings.getSequenceId(),
                    settings.getCompanionSequenceId(),
                    new SequenceConfig(settings));
        } catch (RMException e) {
            //TODO L10N
            logger.severe("ServerSequenceFactory.createSequence failed", e);
            return null;
        } catch (URISyntaxException e) {
            //TODO L10N
            logger.severe("ServerSequenceFactory.createSequence failed", e);
            return null;
        }
    }
}
