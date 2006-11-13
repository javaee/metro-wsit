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

import com.sun.xml.ws.tx.at.ATCoordinator;
import com.sun.xml.ws.tx.at.ATSubCoordinator;
import com.sun.xml.ws.tx.common.ActivityIdentifier;
import static com.sun.xml.ws.tx.common.Constants.WSAT_2004_PROTOCOL;
import static com.sun.xml.ws.tx.common.Constants.WSAT_OASIS_NSURI;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.webservice.member.coord.CreateCoordinationContextType;

import javax.xml.ws.EndpointReference;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * This singleton class is responsible for managing coordinated
 * activities for the entire appserver.
 * <p/>
 * Whenever a new coordinated activity is started, a new {@link Coordinator}
 * object is constructed and managed by this class.
 * <p/>
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.2 $
 * @since 1.0
 */
public final class CoordinationManager {

    /* singleton instance */
    private static final CoordinationManager instance = new CoordinationManager();

    /* HashMap<coordination id, Coordinator> */
    private static final HashMap<String, Coordinator> coordinators = new HashMap<String, Coordinator>();

    static private TxLogger logger = TxLogger.getCoordLogger(CoordinationManager.class);

    /*
    * private constructor for singleton
    */
    private CoordinationManager() {
    }

    /**
     * Return the singleton instance of CoordinationManager.
     *
     * @return the CoordinationManager instance
     */
    public static CoordinationManager getInstance() {
        return instance;
    }

    /**
     * Get the {@link Coordinator} object with the given coordination id
     *
     * @param id the coordination context id
     * @return the Coordinator object or null if the id doesn't exist
     */
    public Coordinator getCoordinator(String id) {
        return coordinators.get(id);
    }

    /**
     * Add the specified {@link Coordinator} object to the list of managed
     * activities.
     * <p/>
     * TODO: what about duplicate keys or entries?
     *
     * @param coordinator
     */
    public void putCoordinator(Coordinator coordinator) {
        coordinators.put(coordinator.getIdValue(), coordinator);
        if (logger.isLogging(Level.FINEST)) {
            logger.finest("putCoordinator", "add activity id:" + coordinator.getIdValue());
        }
    }

    /**
     * Create a {@link Coordinator} object from the incoming request and
     * add it to the list of managed activities.  The actual type of the
     * {@link Coordinator} object created will depend on the
     * {@link com.sun.xml.ws.api.tx.Protocol} specified in the contextRequest
     * parameter.
     * <p/>
     * This method is invoked when we receive a createCoordinationContext soap
     * request.
     * <p/>
     *
     * @param contextRequest the incoming wscoor:createCoordinationContext message
     * @return the coordinator
     */
    public Coordinator lookupOrCreateCoordinator(CreateCoordinationContextType contextRequest) {
        Coordinator c;
        if (contextRequest.getCurrentContext() != null) {
            c = createSubordinateCoordinator(null, contextRequest);
        } else {
            c = createCoordinator(null, contextRequest);
        }
        putCoordinator(c);
        return c;
    }


    /**
     * Lookup if coordinator exists for context, if not, create a {@link Coordinator} object from the given coordination
     * context and add it to the list of managed activities.  The actual
     * type of the {@link Coordinator} object created will depend on the
     * protocol identifier contained in the context.
     * <p/>
     * This method is used for direct private invocation within the
     * appserver.
     *
     * @param context the coordination context
     * @return the coordinator
     */
    public Coordinator lookupOrCreateCoordinator(CoordinationContextInterface context) {
        Coordinator c = getCoordinator(context.getIdentifier());
        if (c == null) {
            // If registration service EPR is not created locally, make this a subordinate coordinator
            if (RegistrationManager.getRegistrationCoordinatorStatefulWebServiceManager().resolve(context.getRegistrationService()) == null) {
                c = createSubordinateCoordinator(context, null);
            } else {
                c = createCoordinator(context, null);
            }
        }
        putCoordinator(c);
        return c;
    }

    /**
     * create a {@link Coordinator} from either the given context or the given request.
     * <p/>
     * One of these parameters MUST be null, one of them MUST be non-null.
     *
     * @param context        coordination context
     * @param contextRequest createCoordinationContext soap msg
     * @return a Coordinator
     */
    private Coordinator createCoordinator(CoordinationContextInterface context, CreateCoordinationContextType contextRequest) {

        assert((context == null) ^ (contextRequest == null));  // xor

        if (logger.isLogging(Level.FINER)) {
            logger.entering("CoordinationManager.createCoordinator");
        }

        final String coordType;
        if (contextRequest != null) {
            coordType = contextRequest.getCoordinationType();
        } else {
            coordType = context.getCoordinationType();
        }

        if (WSAT_2004_PROTOCOL.equals(coordType)) {
            final Coordinator coord;
            if (contextRequest != null) {
                coord = contextRequest.getCurrentContext() == null ?
                        new ATCoordinator(ContextFactory.createContext(contextRequest), contextRequest) :
                        new ATSubCoordinator(ContextFactory.createContext(contextRequest), contextRequest);
            } else {
                coord = context.getRootRegistrationService() == null ?
                        new ATCoordinator(context) : new ATSubCoordinator(context);
            }
            if (logger.isLogging(Level.FINEST)) {
                logger.finest("CoordinationManager.createCoordinator id=", coord.getIdValue());
            }
            if (logger.isLogging(Level.FINER)) {
                logger.exiting("CoordinationManager.createCoordinator");
            }
            return coord;
        } else if (WSAT_OASIS_NSURI.equals(coordType)) {
            throw new UnsupportedOperationException(
                    LocalizationMessages.OASIS_UNSUPPORTED()
            );
        } else {
            throw new UnsupportedOperationException(
                    LocalizationMessages.UNRECOGNIZED_COORDINATION_TYPE(coordType)
            );
        }
    }

    /**
     * Create a {@link Coordinator} that delegates to a root coordinator that is probably on a remote
     * system not under our control.
     * <p/>
     * One of these parameters MUST be null, one of them MUST be non-null.
     *
     * @param context        the original coordination context generated by the root coordinator
     * @param contextRequest the original createCordinationContext request message
     * @return a new subordinate coordinator that will delegate for our participants
     */
    private Coordinator createSubordinateCoordinator(CoordinationContextInterface context, CreateCoordinationContextType contextRequest) {

        assert((context == null) ^ (contextRequest == null));  // xor

        if (logger.isLogging(Level.FINER)) {
            logger.entering("CoordinationManager.createSubordinateCoordinator");
        }

        if (context != null) {
            // replace the registration service EPR with our own, but remember the EPR of the
            // root coordinator's registration EPR for delegation.
            context.setRootCoordinatorRegistrationService(context.getRegistrationService());

            // delegate
            Coordinator coord = createCoordinator(context, null);
            context.setRegistrationService(RegistrationManager.newRegistrationEPR((ActivityIdentifier) coord.getId()));
            if (logger.isLogging(Level.FINER)) {
                logger.exiting("CoordinationManager.createSubordinateCoordinator");
            }
            return coord;
        } else {
            if (logger.isLogging(Level.FINER)) {
                logger.exiting("CoordinationManager.createSubordinateCoordinator");
            }
            // in this case, the context should already have the registration EPRs initialized
            // properly, so we don't have to swap - just create a new coordinator
            return createCoordinator(null, contextRequest);
        }
    }

}
