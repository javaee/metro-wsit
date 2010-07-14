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
package com.sun.xml.ws.tx.coordinator;

import com.sun.istack.NotNull;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.tx.common.ActivityIdentifier;
import static com.sun.xml.ws.tx.common.Constants.WSAT_2004_PROTOCOL;
import static com.sun.xml.ws.tx.common.Constants.WSAT_OASIS_NSURI;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.webservice.member.coord.CreateCoordinationContextType;

import java.util.UUID;
import java.util.logging.Level;

/**
 * This class is an abstraction of the two kinds of CoordinationContexts defined
 * in WS-Coordination 2004/10 member submission and 2006/03 OASIS.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.11.22.2 $
 * @since 1.0
 */
public class ContextFactory {

    private static String activityId;

    static private TxLogger logger = TxLogger.getCoordLogger(ContextFactory.class);


    /**
     * Create a new {@link CoordinationContextInterface} of the appropriate type based on
     * the coordination type namespace uri.
     * <p/>
     *
     * @param coordType the nsuri of the coordination type, either {@link com.sun.xml.ws.tx.common.Constants#WSAT_2004_PROTOCOL}
     *                  or {@link com.sun.xml.ws.tx.common.Constants#WSAT_OASIS_NSURI}
     * @param expires   expiration timout in ms
     * @return the {@link CoordinationContextInterface}
     */
    public static CoordinationContextInterface createContext(@NotNull final String coordType, final long expires) {

        if (logger.isLogging(Level.FINER)) {
            logger.entering("ContextFactory.createContext: coordType=" + coordType + " expires=" + expires);
        }
        CoordinationContextInterface context;

        if (WSAT_2004_PROTOCOL.equals(coordType)) {
            context = new CoordinationContext200410();
            context.setCoordinationType(coordType);

            context.setExpires(expires);

            activityId = UUID.randomUUID().toString();
            context.setIdentifier("uuid:WSCOOR-SUN-" + activityId);

            // bake the activity id as <wsa:ReferenceParameters> into the registration service EPR, so we can
            // identify the activity when the <register> requests come in.
            context.setRegistrationService(
                    RegistrationManager.newRegistrationEPR(
                            new ActivityIdentifier(context.getIdentifier()), expires));
        } else if (WSAT_OASIS_NSURI.equals(coordType)) {
            throw new UnsupportedOperationException(
                    LocalizationMessages.OASIS_UNSUPPORTED_3000()
            );
        } else {
            throw new UnsupportedOperationException(
                    LocalizationMessages.UNRECOGNIZED_COORDINATION_TYPE_3001(coordType)
            );
        }

        if (logger.isLogging(Level.FINER)) {
            logger.exiting("ContextFactory.createContext: created context for activity id: " + context.getIdentifier());
        }
        return context;
    }

    /**
     * Create a context from the incoming <createContext> message
     *
     * @param contextRequest <createContext> request
     * @return the coordination context
     */
    public static CoordinationContextInterface createContext(@NotNull final CreateCoordinationContextType contextRequest) {
        return createContext(contextRequest.getCoordinationType(), contextRequest.getExpires().getValue());
    }

    /*
     * FOR UNIT TESTING ONLY
     */
    static CoordinationContextInterface createTestContext(final String coordType, final long expires) {

        CoordinationContextInterface context;

        if (WSAT_2004_PROTOCOL.equals(coordType)) {
            context = new CoordinationContext200410();
            context.setCoordinationType(coordType);

            context.setExpires(expires);

            activityId = UUID.randomUUID().toString();
            context.setIdentifier("uuid:WSCOOR-SUN-" + activityId);

            // we can't unit test the normal creation of an EPR this way because
            // it requires the use of injected stateful webservice managers on
            // the port type impls.  So set a dummy EPR instead.
            //
            // ActivityIdentifier activityId = new ActivityIdentifier(context.getIdentifier());
            // context.setRegistrationService(RegistrationManager.newRegistrationEPR(activityId));
            context.setRegistrationService(new MemberSubmissionEndpointReference());
        } else if (WSAT_OASIS_NSURI.equals(coordType)) {
            throw new UnsupportedOperationException(
                    LocalizationMessages.OASIS_UNSUPPORTED_3000()
            );
        } else {
            throw new UnsupportedOperationException(
                    LocalizationMessages.UNRECOGNIZED_COORDINATION_TYPE_3001(coordType)
            );
        }

        return context;
    }
}
