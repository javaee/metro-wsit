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
import com.sun.istack.Nullable;
import com.sun.xml.ws.tx.common.ActivityIdentifier;
import com.sun.xml.ws.tx.common.Identifier;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.webservice.member.coord.CreateCoordinationContextType;

import javax.xml.ws.EndpointReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * This class encapsulates a coordinated activity.
 * <p/>
 * Whenever a client (participant) registers for the activity, a {@link Registrant}
 * is constructed and managed by this class.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.5 $
 * @since 1.0
 */
public abstract class Coordinator {

    /* the actual coordination context for this activity */
    private final CoordinationContextInterface context;

    /* the SOAP request to create a new context, if it exists */
    private final CreateCoordinationContextType request;

    /* the unique coordination activity id */
    private final ActivityIdentifier id;

    /**
     * Timer to manage expiration of registrants
     */
    private final static Timer expirationTimer = new Timer("WS-TX Expiration Timer");
    private boolean expired = false;

    static private TxLogger logger = TxLogger.getCoordLogger(Coordinator.class);

    /**
     * Construct a new Coordinator object from the specified context and soap request.
     *
     * @param context The coordination context
     * @param request The soap request
     */
    public Coordinator(@NotNull CoordinationContextInterface context, @Nullable CreateCoordinationContextType request) {
        this.context = context;
        this.request = request;

        this.id = new ActivityIdentifier(context.getIdentifier());

        if (logger.isLogging(Level.FINER)) {
            logger.finer("Coordinator constructor", "New Coordinator created for activity: " + context.getIdentifier());
        }

        if (context.getExpires() != 0L) {
            // start a expiration timer task if necessary
            if (logger.isLogging(Level.FINER)) {
                logger.finer("Coordinator constructor", "Starting expiration task for activity: "
                        + context.getIdentifier() + " will expire in " + context.getExpires() + "ms");
            }
            expirationTimer.schedule(new ExpirationTask(this), context.getExpires());
        }
    }

    /**
     * Construct a new Coordinator object from the specified context.
     * <p/>
     * This constructor will be the main entry point for activity within the
     * AppServer.
     *
     * @param context The coordination context
     */
    public Coordinator(@NotNull CoordinationContextInterface context) {
        this(context, null);
    }

    /**
     * Get the coordination context associated with this coordinated activity
     *
     * @return The coordination context
     */
    @NotNull
    public CoordinationContextInterface getContext() {
        return context;
    }

    /**
     * Get the SOAP request associated with this coordinated activity, if it
     * exists.
     *
     * @return The original SOAP request (createCoordinationContext) or null
     *         if it doesn't exist.
     */
    @Nullable
    public CreateCoordinationContextType getRequest() {
        return request;
    }

    /**
     * Get the activity id value
     *
     * @return The activity id value
     */
    @NotNull
    public String getIdValue() {
        return id.getValue();
    }

    /**
     * Get the {@link ActivityIdentifier} object.
     * <p/>
     * This object can be used when it is necessary to insert the id as
     * a ReferenceParameter in a soap message
     *
     * @return The activity id object
     */
    @NotNull
    public Identifier getId() {
        return id;
    }

    /**
     * Get the expiration value
     *
     * @return The expiration value
     */
    public long getExpires() {
        return context.getExpires();
    }

    public void setExpires(final long i) {
        if (context != null) {
            context.setExpires(i);
        }
    }

    /**
     * Get the list of {@link Registrant}s for this coordinated activity.
     * <p/>
     * The returned list is unmodifiable (read-only).  Add new Registrants
     * with the {@link #addRegistrant(Registrant)} api instead.
     *
     * @return the list of Registrant objects
     */
    @NotNull
    public abstract List<Registrant> getRegistrants();

    /**
     * Add the specified Registrant to the list of registrants for this
     * coordinated activity.
     *
     * @param registrant The {@link Registrant}
     */
    public abstract void addRegistrant(Registrant registrant);

    /**
     * Get the registrant with the specified id or null if it does not exist.
     *
     * @param id the registrant id
     * @return the Registrant object or null if the id does not exist
     */
    @Nullable
    public abstract Registrant getRegistrant(String id);

    /**
     * Remove the registrant with the specified id
     *
     * @param id the registrant id
     */

    public abstract void removeRegistrant(String id);

    /**
     * Return true iff this coordinator is delegating to a root coordinator
     * @return true iff this coordinator is delegating to a root coordinator
     */
    public boolean isSubordinate() {
        return context.getRootRegistrationService() != null;
    }

    /**
     * Return the Coordinator Protocol Service EPR for registrant r.
     * @param r registrant
     * @return the CPS EPT for the specified registrant
     */
    @NotNull
    public abstract EndpointReference getCoordinatorProtocolServiceForRegistrant(@NotNull Registrant r);

    /**
     * Return true iff registrant should register with its root registration service.
     * <p/>
     * Enables local participants to be cached with coordinator locally when this method returns
     * true.
     * @param r restistrant
     * @return Return true iff registrant should register with its root registration service
     */
    public boolean registerWithRootRegistrationService(@NotNull final Registrant r) {
        return false;
    }

    /**
     * Sub classes will implement this method to indicate whether or not they
     * are subject to expiration.
     *
     * @return true if the coordinator should NOT expire, false otherwise.
     */
    abstract public boolean expirationGuard();

    /**
     * Release resources held by this coordinator.
     * <p>
     * This method will be automatically invoked once if the activity has a non-zero expiration.
     * <p>
     * During expiration, the coordinator will iterate over all of its registrants and tell them
     * to expire.  Depending on their state, registrants will either expire or not.  A coordinator
     * will not completely expire until all of its registrants have expired.
     */
    public void expire() {
        expired = true;
        if(logger.isLogging(Level.FINEST)) {
            logger.finest("Coordinator.expire", "attempting to expire coordinator: " + id.getValue());
        }
        if (!expirationGuard()) {
            if(logger.isLogging(Level.FINEST)) {
                logger.finest("Coordinator.expire", "forgetting resources for: " + id.getValue());
            }
            // TODO: send fault S4.4 wscoor:NoActivity

            forget();
        } else {
            if(logger.isLogging(Level.FINEST)) {
                logger.finest("Coordinator.expire", "expiration was guarded, returning without expiration");
            }
        }
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(final boolean expired) {
        this.expired = expired;
    }

    /**
     * Release all resources associated with this coordinator
     */
    public void forget() {
        CoordinationManager.getInstance().removeCoordinator(this.id.getValue());
    }

    /**
     * Timer class for controlling the expiration of registrants.
     * <p/>
     * Schedule timer tasks whenever we add a registrant and expire them when
     * ever the timers go off.
     */
    class ExpirationTask extends TimerTask {
        Coordinator c;

        ExpirationTask(Coordinator c) {
            this.c = c;
        }

        public void run() {
            c.expire();
            this.cancel(); // we only want to be triggered once
        }
    }
}
