/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.tx.coordinator;

import com.sun.istack.NotNull;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.tx.common.ActivityIdentifier;
import static com.sun.xml.ws.tx.common.Constants.WSAT_2004_PROTOCOL;
import static com.sun.xml.ws.tx.common.Constants.WSAT_OASIS_NSURI;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.webservice.member.coord.CreateCoordinationContextType;

import java.util.logging.Level;

/**
 * This class is an abstraction of the two kinds of CoordinationContexts defined
 * in WS-Coordination 2004/10 member submission and 2006/03 OASIS.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.7 $
 * @since 1.0
 */
public class ContextFactory {

    private static long activityId;

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

            activityId += 1;
            context.setIdentifier("uuid:WSCOOR-SUN-" + activityId);

            // bake the activity id as <wsa:ReferenceParameters> into the registration service EPR, so we can
            // identify the activity when the <register> requests come in.
            context.setRegistrationService(
                    RegistrationManager.newRegistrationEPR(
                            new ActivityIdentifier(context.getIdentifier())));
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

    /**
     * FOR UNIT TESTING ONLY
     */
    static CoordinationContextInterface createTestContext(final String coordType, final long expires) {

        CoordinationContextInterface context;

        if (WSAT_2004_PROTOCOL.equals(coordType)) {
            context = new CoordinationContext200410();
            context.setCoordinationType(coordType);

            context.setExpires(expires);

            activityId += 1;
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
