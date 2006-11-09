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

import com.sun.xml.ws.api.tx.Protocol;
import static com.sun.xml.ws.api.tx.Protocol.*;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.tx.common.Identifier;
import com.sun.xml.ws.tx.common.RegistrantIdentifier;
import com.sun.xml.ws.tx.webservice.member.coord.RegisterType;

import javax.xml.ws.EndpointReference;
import java.util.HashMap;
import java.util.Map;

/**
 * This class encapsulates a coordination registrant.
 * <p/>
 * The coordination protocol implementations will extend this class and
 * add protocol specific functionality.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.1 $
 * @since 1.0
 */
public abstract class Registrant {

    /* The actual SOAP message containing the register message */
    private RegisterType registerRequest = null;

    /* reference to the parent coordinator */
    private final Coordinator parent;

    /* the protocol the registrant is registering for */
    private final Protocol protocol;

    // coordinator(received from coord:registerReply from coordinator)
    private EndpointReference coordinatorProtocolService = null;

    private static long nextId = 1;

    private final RegistrantIdentifier id;

    /**
     * Create a new registrant
     *
     * @param registerRequest
     * @param parent
     */
    public Registrant(Coordinator parent, RegisterType registerRequest) {
        this(parent, registerRequest.getProtocolIdentifier());
        this.registerRequest = registerRequest;
        // no need to add to outstanding registrants, the PPS is in registerRequest
    }

    protected Registrant(Coordinator parent, String protocolId) {
        this(parent, Protocol.getProtocol(protocolId));
    }

    /**
     * Create a Registratant with its coordinator parent for protocol.
     */
    protected Registrant(Coordinator parent, Protocol protocol) {
        id = new RegistrantIdentifier(Long.toString(nextId++));

        this.parent = parent;
        this.protocol = protocol;
        if (parent.isSubordinate()) {
            outstandingRegistrants.put(getIdValue(), this);
            registrationCompleted = false;
        } else {
            if (registerRequest == null) {
                this.setParticpantProtocolService(getLocalParticipantProtocolService());
            }
        }
    }

    private String getProtocolIdentifier() {
        switch (protocol) {
            case COMPLETION:
                return COMPLETION.getUri();
            case DURABLE:
                return DURABLE.getUri();
            case VOLATILE:
                return VOLATILE.getUri();
            default:
                return UNKNOWN.getUri();
        }
    }

    public void setParticpantProtocolService(EndpointReference pps) {
        if (registerRequest == null) {
            registerRequest = new RegisterType();
        }
        registerRequest.setProtocolIdentifier(this.getProtocolIdentifier());
        registerRequest.setParticipantProtocolService((MemberSubmissionEndpointReference) pps);
    }

    public EndpointReference getParticipantProtocolService() {
        return (registerRequest != null) ?
                registerRequest.getParticipantProtocolService() :
                getLocalParticipantProtocolService();
    }

    /**
     * Get the SOAP register request
     *
     * @return the SOAP register message
     */
    public RegisterType getRegisterRequest() {
        return registerRequest;
    }

    /**
     * Get the protocol for this registrant
     *
     * @return the protocol identifier
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * Get the parent coordinator for this Registrant
     *
     * @return the parent coordinator
     */
    public Coordinator getCoordinator() {
        return parent;
    }

    public EndpointReference getCoordinatorProtocolService() {
        return coordinatorProtocolService;
    }

    /**
     * Set the coordinator protocol service received by coor:registerResponse.
     */
    public void setCoordinatorProtocolService(EndpointReference cps) {
        coordinatorProtocolService = cps;
        setRegistrationCompleted(true);
    }

    public void register() {
        RegistrationManager.getInstance().register(parent, this);
    }

    public Identifier getId() {
        return id;
    }

    public String getIdValue() {
        return id.getValue();
    }


    // extent of participants
    protected static Map<String, Registrant> outstandingRegistrants = new HashMap<String, Registrant>();

    /**
     * Lookup registrant by its id.
     */
    public static Registrant getOutstandingRegistrant(String id) {
        return outstandingRegistrants.get(id);
    }

    /**
     * Lookup registrant by its id.
     */
    public static Registrant removeOutstandingRegistrant(String id) {
        return outstandingRegistrants.remove(id);
    }

    private boolean remoteCPS = false;

    public void setRemoteCPS(boolean value) {
        remoteCPS = value;
    }

    public boolean isRemoteCPS() {
        return remoteCPS;
    }

    // only set to false when constucting a Registrant with a remote coordination service.
    // when this value is true, coordination protocol service will be properly set.
    private Boolean registrationCompleted = true;

    public boolean isRegistrationCompleted() {
        return registrationCompleted;
    }

    public void setRegistrationCompleted(boolean value) {
        registrationCompleted = value;
    }

    abstract public EndpointReference getLocalParticipantProtocolService();

    /**
     * Sub classes will implement this method to indicate whether or not they
     * are subject to expiration.
     *
     * @return true if the registrant should expire, false otherwise.
     */
    abstract public boolean expirationGuard();

    /**
     * Expire this registrant, clean up resources, send fault messages
     */
    public void expire() {
        if (expirationGuard()) {
            // send fault S4.4 wscoor:NoActivity
            // remove registrant from parent coordinator
            parent.getRegistrants().remove(this);
        }
        throw new UnsupportedOperationException();
    }
}
