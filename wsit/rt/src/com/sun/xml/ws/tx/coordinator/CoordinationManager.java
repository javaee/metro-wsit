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
import com.sun.istack.Nullable;
import com.sun.xml.ws.tx.at.ATCoordinator;
import com.sun.xml.ws.tx.at.ATSubCoordinator;
import com.sun.xml.ws.tx.common.ActivityIdentifier;
import static com.sun.xml.ws.tx.common.Constants.WSAT_2004_PROTOCOL;
import static com.sun.xml.ws.tx.common.Constants.WSAT_OASIS_NSURI;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.webservice.member.coord.CreateCoordinationContextType;

import java.util.HashMap;
import java.util.Map;
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
 * @version $Revision: 1.9.22.2 $
 * @since 1.0
 */
public final class CoordinationManager {

    /* singleton instance */
    private static final CoordinationManager instance = new CoordinationManager();

    /* HashMap<coordination id, Coordinator> */
    private static final Map<String, Coordinator> coordinators = new HashMap<String, Coordinator>();

    static private TxLogger logger = TxLogger.getCoordLogger(CoordinationManager.class);

    /**
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
    @Nullable
    public Coordinator getCoordinator(@NotNull final String id) {
        return coordinators.get(id);
    }

    /**
     * Add the specified {@link Coordinator} object to the list of managed
     * activities.
     * <p/>
     * TODO: what about duplicate keys or entries?
     *
     * @param coordinator coordinator
     */
    public void putCoordinator(final Coordinator coordinator) {
        coordinators.put(coordinator.getIdValue(), coordinator);
        if (logger.isLogging(Level.FINEST)) {
            logger.finest("putCoordinator", "add activity id:" + coordinator.getIdValue());
        }
    }

    /**
     * Remove the specified {@link Coordinator} object from the list of managed
     * activities.
     * <p>
     * @param id activity id
     */
    public void removeCoordinator(@NotNull final String id) {
        final Coordinator c = coordinators.remove(id);
        if(c!=null) {
            c.forget();
        }
        if (logger.isLogging(Level.FINEST)) {
            logger.finest("removeCoordinator", "remove activity id:" + id);
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
    @NotNull
    public Coordinator lookupOrCreateCoordinator(@NotNull final CreateCoordinationContextType contextRequest) {
        Coordinator c;
        if (contextRequest.getCurrentContext() == null) {
            c = createCoordinator(null, contextRequest);
        } else {
            c = createSubordinateCoordinator(null, contextRequest);
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
    @NotNull
    public Coordinator lookupOrCreateCoordinator(@NotNull final CoordinationContextInterface context) {
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
    @NotNull
    private Coordinator createCoordinator(@Nullable final CoordinationContextInterface context,
                                          @Nullable final CreateCoordinationContextType contextRequest) {

        assert((context == null) ^ (contextRequest == null));  // xor

        if (logger.isLogging(Level.FINER)) {
            logger.entering("CoordinationManager.createCoordinator");
        }

        final String coordType;
        if (contextRequest == null) {
            coordType = context.getCoordinationType();
        } else {
            coordType = contextRequest.getCoordinationType();
        }

        if (WSAT_2004_PROTOCOL.equals(coordType)) {
            final Coordinator coord;
            if (contextRequest == null) {
                coord = context.getRootRegistrationService() == null ?
                        new ATCoordinator(context) : new ATSubCoordinator(context);
            } else {
                coord = contextRequest.getCurrentContext() == null ?
                        new ATCoordinator(ContextFactory.createContext(contextRequest), contextRequest) :
                        new ATSubCoordinator(ContextFactory.createContext(contextRequest), contextRequest);
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
                    LocalizationMessages.OASIS_UNSUPPORTED_3000()
            );
        } else {
            throw new UnsupportedOperationException(
                    LocalizationMessages.UNRECOGNIZED_COORDINATION_TYPE_3001(coordType)
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
    @NotNull
    private Coordinator createSubordinateCoordinator(@Nullable final CoordinationContextInterface context,
                                                     @Nullable final CreateCoordinationContextType contextRequest) {

        assert((context == null) ^ (contextRequest == null));  // xor

        if (logger.isLogging(Level.FINER)) {
            logger.entering("CoordinationManager.createSubordinateCoordinator");
        }

        if (context == null) {
            if (logger.isLogging(Level.FINER)) {
                logger.exiting("CoordinationManager.createSubordinateCoordinator");
            }
            // in this case, the context should already have the registration EPRs initialized
            // properly, so we don't have to swap - just create a new coordinator
            return createCoordinator(null, contextRequest);
        } else {
            // replace the registration service EPR with our own, but remember the EPR of the
            // root coordinator's registration EPR for delegation.
            context.setRootCoordinatorRegistrationService(context.getRegistrationService());

            // delegate
            final Coordinator coord = createCoordinator(context, null);
            context.setRegistrationService(RegistrationManager.newRegistrationEPR((ActivityIdentifier) coord.getId(), context.getExpires()));
            if (logger.isLogging(Level.FINER)) {
                logger.exiting("CoordinationManager.createSubordinateCoordinator");
            }
            return coord;
        }
    }

}
