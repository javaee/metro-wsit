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

import com.sun.xml.ws.tx.common.ActivityIdentifier;
import com.sun.xml.ws.tx.common.Identifier;
import com.sun.xml.ws.tx.webservice.member.coord.CreateCoordinationContextType;

import javax.xml.ws.EndpointReference;
import java.util.List;
import java.util.TimerTask;

/**
 * This class encapsulates a coordinated activity.
 * <p/>
 * Whenever a client (participant) registers for the activity, a {@link Registrant}
 * is constructed and managed by this class.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.2 $
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
    // private static final Timer expirationTimer = new Timer("WS-TX Registrant Expiration Timer");

    /**
     * Construct a new Coordinator object from the specified context and soap request.
     *
     * @param context The coordination context
     * @param request The soap request
     */
    public Coordinator(CoordinationContextInterface context, CreateCoordinationContextType request) {
        this.context = context;
        this.request = request;

        this.id = new ActivityIdentifier(context.getIdentifier());
    }

    /**
     * Construct a new Coordinator object from the specified context.
     * <p/>
     * This constructor will be the main entry point for activity within the
     * AppServer.
     *
     * @param context The coordination context
     */
    public Coordinator(CoordinationContextInterface context) {
        this(context, null);
    }

    /**
     * Get the coordination context associated with this coordinated activity
     *
     * @return The coordination context
     */
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
    public CreateCoordinationContextType getRequest() {
        return request;
    }

    /**
     * Get the activity id value
     *
     * @return The activity id value
     */
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
    
    public void setExpires(long i) {
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
    public abstract List<Registrant> getRegistrants();

    /**
     * Add the specified Registrant to the list of registrants for this
     * coordinated activity.
     *
     * @param registrant The {@link Registrant}
     */
    public void addRegistrant(Registrant registrant) {
        // if (context.getExpires() != 0L) {
        //     expirationTimer.schedule(new ExpirationTask(registrant), context.getExpires());
        // }

        // actual addRegistrant logic implemented in subclasses
    }

    /**
     * Get the registrant with the specified id or null if it does not exist.
     *
     * @param id the registrant id
     * @return the Registrant object or null if the id does not exist
     */
    public abstract Registrant getRegistrant(String id);

    /**
     * Return true iff this coordinator is delegating to a root coordinator
     */
    public boolean isSubordinate() {
        return context.getRootRegistrationService() != null;
    }

    /**
     * Return the CPS for registrant r.
     */
    public abstract EndpointReference getCoordinatorProtocolServiceForRegistrant(Registrant r);

    /**
     * Return true iff registrant should register with its root registration service.
     * <p/>
     * Enables local participants to be cached with coordinator locally when this method returns
     * true.
     */
    public boolean registerWithRootRegistrationService(Registrant r) {
        return false;
    }


    /**
     * Timer class for controlling the expiration of registrants.
     * <p/>
     * Schedule timer tasks whenever we add a registrant and expire them when
     * ever the timers go off.
     */
    class ExpirationTask extends TimerTask {
        Registrant r;

        ExpirationTask(Registrant r) {
            this.r = r;
        }

        public void run() {
            if (r.expirationGuard()) {
                r.expire();
            }
        }
    }
}
