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
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.OneWayFeature;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.tx.Protocol;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.developer.StatefulWebServiceManager;
import com.sun.xml.ws.tx.at.ATParticipant;
import com.sun.xml.ws.tx.common.ActivityIdentifier;
import static com.sun.xml.ws.tx.common.Constants.*;
import com.sun.xml.ws.tx.common.StatefulWebserviceFactory;
import com.sun.xml.ws.tx.common.StatefulWebserviceFactoryFactory;
import com.sun.xml.ws.tx.common.TxFault;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.common.Util;
import com.sun.xml.ws.tx.common.WsaHelper;
import com.sun.xml.ws.tx.webservice.member.coord.RegisterResponseType;
import com.sun.xml.ws.tx.webservice.member.coord.RegisterType;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationCoordinatorPortType;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationRequesterPortType;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import java.net.URI;
import java.util.logging.Level;

/**
 * This singleton class handles the register and registerResponse operations for
 * both local and remote (or external) clients.  The exposed web service endpoints
 * for register and registerResponse delegate to the methods in this class.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.9 $
 * @since 1.0
 */
public final class RegistrationManager {

    /* singleton instance */
    private static final RegistrationManager instance = new RegistrationManager();


    private static final URI localRegistrationURI =
            Util.createURI(WSTX_WS_SCHEME, null, WSTX_WS_PORT, WSTX_WS_CONTEXT + "/wscoor/coordinator/synchRegister");

    private static final URI localAsynchronousRegistrationURI =
            Util.createURI(WSTX_WS_SCHEME, null, WSTX_WS_PORT, WSTX_WS_CONTEXT + "/wscoor/coordinator/register");

    private static final URI localRegistrationRequesterURI =
            Util.createURI(WSTX_WS_SCHEME, null, WSTX_WS_PORT, WSTX_WS_CONTEXT + "/wscoor/coordinator/registerResponse");

//    public static EndpointReference newSynchronousRegistrationEPR(ActivityIdentifier activityId) {
//        EndpointReference registrationEPR =
//                WsaHelper.getAddressingBuilder().newEndpointReference(localRegistrationURI);
//        WsaHelper.addRefParam(registrationEPR, activityId.getSOAPElement());
//        return registrationEPR;
//    }

    public static URI getLocalRegistrationURI() {
        return localRegistrationURI;
    }

    public static URI getLocalAsyncRegistrationURI() {
        return localAsynchronousRegistrationURI;
    }

    public static URI getLocalRegistrationRequesterURI() {
        return localRegistrationRequesterURI;
    }

    /**
     * Create a new EPR for our registration service.
     * <p/>
     * Note: as a side-effect, this method creates a stateful instance of the registration service that
     * will handle correlation when a client actually registers with us.  Should we consider moving this
     * code to point of use (ie during coordination context creation)?
     *
     * @param activityId the coordination id for this activity, maintained as state in the registration service
     * @return an EPR containing the address of our registration service
     */
    public static EndpointReference newRegistrationEPR(ActivityIdentifier activityId) {
        StatefulWebserviceFactory swf = StatefulWebserviceFactoryFactory.getInstance();
        return swf.createService("Coordinator", "RegistrationCoordinator",
                localAsynchronousRegistrationURI, AddressingVersion.MEMBER,
                activityId.getValue(), null);
    }

    public static StatefulWebServiceManager getRegistrationCoordinatorStatefulWebServiceManager() {
        StatefulWebserviceFactory swf = StatefulWebserviceFactoryFactory.getInstance();
        return swf.getManager("Coordinator", "RegistrationCoordinator");
    }

    private static TxLogger logger = TxLogger.getCoordLogger(RegistrationManager.class);


    /**
     * reference to the CoordinationManager
     */
    private static final CoordinationManager coordinationManager
            = CoordinationManager.getInstance();

    /**
     * Singleton constructor
     */
    private RegistrationManager() {
    }

    /**
     * Get the singleton instance of the RegistrationManager
     *
     * @return the RegistrationManager
     */
    public static RegistrationManager getInstance() {
        return instance;
    }

    /**
     * Handle an incoming <register> web service request from an external Participant
     * and send a <registerResponse> back.
     *
     * @param wsContext       webservice context
     * @param registerRequest the incoming <register> request
     * @param activityId      activity id
     */
    public void register(@NotNull WebServiceContext wsContext, @NotNull String activityId, @NotNull RegisterType registerRequest) {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("RegistrationManager.register(WebserviceContext, RegisterType)");
        }

        // request message must have wsa:MessageId and wsa:ReplyTo, cache them
        if (logger.isLogging(Level.FINEST)) {
            logger.finest("register with coordination id=", activityId);
        }
        String msgID = WsaHelper.getMsgID(wsContext);
        if (logger.isLogging(Level.FINEST)) {
            logger.finest("register", "register request msg id=" + msgID);
        }
        EndpointReference registrationRequesterEPR = WsaHelper.getReplyTo(wsContext);
        WSEndpointReference faultTo = WsaHelper.getFaultTo(wsContext);
        if (logger.isLogging(Level.FINEST)) {
            logger.finest("register", "replyTo:" + registrationRequesterEPR);
        }
        if (registrationRequesterEPR == null) {
            if (faultTo != null) {
                // send fault S4.3 wscoor:Invalid Parameters
                WsaHelper.sendFault(
                        faultTo,
                        null,
                        SOAPVersion.SOAP_11,
                        TxFault.InvalidParameters,
                        "register wsa:replyTo must be set",
                        msgID);
            }
            throw new WebServiceException(LocalizationMessages.REGISTER_REPLYTO_NOT_SET_3003());
        }

        Coordinator c = coordinationManager.getCoordinator(activityId);
        if (c == null) {
            // send fault S4.1 wscoor:Invalid State
            WsaHelper.sendFault(
                    faultTo,
                    null,
                    SOAPVersion.SOAP_11,
                    TxFault.InvalidState,
                    "attempting to register for an unknown activity id: " + activityId,
                    msgID);
            if (logger.isLogging(Level.WARNING)) {
                logger.warning("register",  LocalizationMessages.REGISTER_FOR_UNKNOWN_ACTIVITY_3004(activityId));
            }
        }

        Registrant r = null;
        Protocol requestProtocol = Protocol.getProtocol(registerRequest.getProtocolIdentifier());
        switch (requestProtocol) {
            case DURABLE:
            case VOLATILE:
            case COMPLETION:
                r = new ATParticipant(c, registerRequest);
                c.addRegistrant(r, wsContext);
                break;

            case WSAT2004:
            case UNKNOWN:
                // send fault S4.2 wscoor:Invalid Protocol
                WsaHelper.sendFault(
                        faultTo,
                        null,
                        SOAPVersion.SOAP_11,
                        TxFault.InvalidState,
                        registerRequest.getProtocolIdentifier() + " is not a recognized coordination type",
                        msgID);
                throw new UnsupportedOperationException(
                        LocalizationMessages.UNRECOGNIZED_COORDINATION_TYPE_3001(registerRequest.getProtocolIdentifier()));
        }

        /* send <registerResponse> to RegistrationRequesterEPR */

        // setup relatesTo and get the remote port EPR
        OneWayFeature owf = new OneWayFeature();
        owf.setRelatesToID(msgID);
        RegistrationRequesterPortType registrationRequester =
                getCoordinatorService().getRegistrationRequester(registrationRequesterEPR, owf);

        // build the registerResponse message
        RegisterResponseType registerResponse = new RegisterResponseType();
        registerResponse.setCoordinatorProtocolService(
                (MemberSubmissionEndpointReference) r.getCoordinatorProtocolService());

        // send the message
        try {
            registrationRequester.registerResponseOperation(registerResponse);
        } catch (WebServiceException wse) {
            if (logger.isLogging(Level.WARNING)) {
                logger.warning("register",
                        LocalizationMessages.REGISTERRESPONSE_FAILED_3005(
                                registrationRequesterEPR,
                                wse.getLocalizedMessage()));
            }
            throw wse;
        } catch (Exception e) {
            if (logger.isLogging(Level.SEVERE)) {
                logger.severe("register",
                        LocalizationMessages.REGISTERRESPONSE_FAILED_3005(
                                registrationRequesterEPR,
                                e.getLocalizedMessage()));
                // TODO: throw an exception
            }
        }

        if (logger.isLogging(Level.FINER)) {
            logger.exiting("RegistrationManager.register(WebserviceContext, RegisterType)");
        }
    }

    private static final com.sun.xml.ws.tx.webservice.member.coord.Coordinator coordinatorService =
            new com.sun.xml.ws.tx.webservice.member.coord.Coordinator();

    @NotNull
    private com.sun.xml.ws.tx.webservice.member.coord.Coordinator getCoordinatorService() {
        return coordinatorService;
    }

    /**
     * This is the local entry point for register.  Depending on the root registration
     * service contained in the coordinator, this method will either invoke registerOperation
     * on a remote registration service or simply register locally with our registration
     * service.
     *
     * @param c Coordinator
     * @param r registrant
     */
    public void register(Coordinator c, Registrant r) {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("RegistrationManager.register(Coordinator, Registrant)");
        }

        EndpointReference registrationEPR;
        if (c.registerWithRootRegistrationService(r)) {
            if (logger.isLogging(Level.FINE)) {
                logger.fine("RegistrationManager.register", "register with remote coordinator");
            }
            // if subordinate, send <register> message to root, wait for <registerResponse>, then return
            registrationEPR = c.getContext().getRootRegistrationService();
            r.setRemoteCPS(true);
            assert (registrationEPR != null);

            // Send register to remote registration coordinator to get Coordinator Protocol Service
            assert r.getCoordinatorProtocolService() == null;

            // Set register parameter. Same for both synchronous and asynchronous.
            RegisterType registerParam = new RegisterType();
            registerParam.setProtocolIdentifier(r.getProtocol().getUri());
            MemberSubmissionEndpointReference ppsEpr =
                    (MemberSubmissionEndpointReference) r.getLocalParticipantProtocolService();
            registerParam.setParticipantProtocolService(ppsEpr);
            if (logger.isLogging(Level.FINE)) {
                logger.fine("register", "participant protocol service" + ppsEpr.toString());
            }

//            // register following request/reply mep
//
//            RegistrationPortTypeRPC registerRPC = getCoordinatorService().getRegistration();
//            WsaHelper.initializeAsDestination((BindingProvider) registerRPC, registrationEPR);
//            ap = WsaHelper.getAddressingProperties((BindingProvider) registerRPC,
//                    WsaHelper.BindingProviderContextType.REQUEST_CONTEXT);
//            ap.setReplyTo(newSynchronousRegistrationEPR((ActivityIdentifier)c.getId()));
//            try {
//                RegisterResponseType response = registerRPC.registerOperation(registerParam);
//                if (response == null) {
//                    logger.warning("register", "synchronousRegisterRPC failed to get a non-null response.");
//                } else {
//                    registerResponse(c.getIdValue(), r.getIdValue(), response);
//                    logger.exiting("register", "synchronous register succeeded. Coordination Protocol Service:" +
//                        r.getCoordinatorProtocolService());
//                    return;
//                }
//            } catch (WebServiceException wse) {
//                // very likely that some WS-AT implementations might not implement this optional
//                // binding.  be prepared to use async version of register/register response.
//                logger.warning("register", "synchronous register failed, trying required registration protocol");
//                wse.printStackTrace();
//            }
//            logger.warning("register", "synchronous register failed, trying required registration protocol");
//

            // Asynchronous register/wait for asynchronous registerReply

            // setup stateful ws instance for registerResponse from remote registration coordinator
            if (logger.isLogging(Level.FINE)) {
                logger.fine("RegistrationManager.register", "Creating stateful ws...");
            }
            StatefulWebserviceFactory swf = StatefulWebserviceFactoryFactory.getInstance();
            EndpointReference registrationRequesterEPR =
                    swf.createService("Coordinator", "RegistrationRequester",
                            localRegistrationRequesterURI, AddressingVersion.MEMBER,
                            r.getCoordinator().getIdValue(), r.getIdValue());

            // set replyTo for outgoing register message
            if (logger.isLogging(Level.FINE)) {
                logger.fine("RegistrationManager.register", "setting replyTo...");
            }
            OneWayFeature owf = new OneWayFeature();
            owf.setReplyTo((new WSEndpointReference(registrationRequesterEPR)));
            if (logger.isLogging(Level.FINE)) {
                logger.fine("RegistrationManager.register", "getting port...");
            }
            RegistrationCoordinatorPortType registerCoordinator =
                    getCoordinatorService().getRegistrationCoordinator(registrationEPR, (WebServiceFeature) owf);

            if (logger.isLogging(Level.FINEST)) {
                logger.finest("RegistrationManager.register", "send wscoor:register to epr:" + registrationEPR
                        + " replyTo EPR: " + owf.getReplyTo());
            }

            r.setRegistrationCompleted(false);
            try {
                // prefer to try synchronouos register/register response than busy wait.
                if (logger.isLogging(Level.FINE)) {
                    logger.fine("RegistrationManager.register", "invoking remote registerOperation...");
                }
                registerCoordinator.registerOperation(registerParam);

                // next line is necessary. race condition that register did not complete before
                // tranaction initiator committed/rolled back tranaction.
                // This is why synchronous register preferable to asynchronous register.
                waitForRegisterResponse(r, registrationEPR);
            } catch (WebServiceException wse) {
                if (logger.isLogging(Level.WARNING)) {
                    logger.warning("register",
                            LocalizationMessages.REGISTER_FAILED_3006(registrationEPR, wse.getLocalizedMessage()));
                }
                wse.printStackTrace();
                throw wse;
            }

            if (r.isRegistrationCompleted()) {
                if (logger.isLogging(Level.FINE)) {
                    logger.fine("register", "asynch registration succeeded. Coordinator Protocol Service is " +
                            r.getCoordinatorProtocolService());
                }
            } else {
                // send fault S4.4 wscoor:No Activity
                WsaHelper.sendFault(
                        null,
                        registrationEPR,
                        SOAPVersion.SOAP_11,
                        TxFault.NoActivity,
                        "registration timed out for activity id: " + c.getIdValue(),
                        null /* TODO: what is RelatesTo in this case? */ );
                if (logger.isLogging(Level.WARNING)) {
                    logger.warning("register", LocalizationMessages.REGISTRATION_TIMEOUT_3007(c.getIdValue()));
                }
            }

            // reply processed in #registerResponse(WebServiceContext, RegisterResponseType) gets CPS for registrant.
        } else {
            if (logger.isLogging(Level.FINE)) {
                logger.fine("RegistrationManager.register", "register with local coordinator");
            }
            // else root coordinator, simply register and return
            r.setCoordinatorProtocolService(c.getCoordinatorProtocolServiceForRegistrant(r));
            c.addRegistrant(r, null);
        }

        if (logger.isLogging(Level.FINER)) {
            logger.exiting("RegistrationManager.register(Coordinator, Registrant)");
        }
    }

    /**
     * Process an incoming <registerResponse> message.
     *
     * @param activityId       activity id
     * @param registrantId     registrant id
     * @param registerResponse <registerResponse> message
     * @param wsContext        context of the inbound web service invocation
     */
    public void registerResponse(@NotNull WebServiceContext wsContext, @NotNull String activityId, @NotNull String registrantId, @NotNull RegisterResponseType registerResponse) {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("RegistrationManager.registerResponse");
        }

         // look up the registrant and remove it from outstanding Registrants
        Registrant r = Registrant.getOutstandingRegistrant(registrantId);
        if (r == null) {
            // send fault S4.1 wscoor:Invalid State
            WsaHelper.sendFault(
                    wsContext,
                    SOAPVersion.SOAP_11,
                    TxFault.InvalidState,
                    "received registerResponse for non-existent registrant : " +
                            registrantId + " for CoordId:" + activityId );
            if (logger.isLogging(Level.WARNING)) {
                logger.warning("registerResponse",
                        LocalizationMessages.NONEXISTENT_REGISTRANT_3008(registrantId, activityId));
            }
        } else {
            // set coordinator protocol service on registrant
            r.setCoordinatorProtocolService(registerResponse.getCoordinatorProtocolService());
            r.setRemoteCPS(true);
            r.getCoordinator().addRegistrant(r, wsContext);
            Registrant.removeOutstandingRegistrant(registrantId);
            if (logger.isLogging(Level.FINEST)) {
                logger.finest("registerResponse", "Completed registration for CoordId:" + activityId +
                        "registrantId:" + registrantId);
            }
        }

        if (logger.isLogging(Level.FINER)) {
            logger.exiting("RegistrationManager.registerResponse");
        }
    }

    /**
     * Must wait for response.
     * <p/>
     * Race conditions without wait:
     * 1. Transaction Initiating client commits transaction before register message is delivered and processed.
     * Participant misses being in transaction and thus no two phase commit.
     * 2. sun service trying to send prepared, aborted or readonly before receiving coordination protocol service in registerResponse.
     *
     * @param r               registrant
     * @param registrationEPR registration EPR
     */
    private void waitForRegisterResponse(@NotNull Registrant r, @NotNull EndpointReference registrationEPR) {
        int i = 0;

        int MAX_RETRY = 40;
        while (!r.isRegistrationCompleted()) {
            if (i++ > MAX_RETRY) {
                if (logger.isLogging(Level.WARNING)) {
                    logger.warning("register", LocalizationMessages.NO_RESPONSE_3009(registrationEPR, r.getIdValue()));
                }
                // TODO: do we want to ever resend another <register> message in this case?
                break;
            }
            if (logger.isLogging(Level.FINEST)) {
                logger.finest("register", "waiting for registerResponse");
            }
            try {
                Thread.sleep(1000L);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Register via request/reply message pattern.
     * <p/>
     * Add coordination faults to this method.
     *
     * @param activityId      activity id
     * @param registerRequest <register> request
     * @return a new <registerResponse>
     * @param wsContext context for incoming web service invocation
     */
    @NotNull
    public static RegisterResponseType synchronousRegister(@NotNull WebServiceContext wsContext,
                                                           @NotNull String activityId,
                                                           @NotNull RegisterType registerRequest) {
        Protocol requestProtocol = Protocol.getProtocol(registerRequest.getProtocolIdentifier());
        if (logger.isLogging(Level.FINER)) {
            logger.entering("synchronousRegister", "protocol=" + requestProtocol +
                    " coordId=" + activityId);
        }

        Coordinator c = coordinationManager.getCoordinator(activityId);
        if (c == null) {
            // send fault S4.3 wscoor:Invalid Parameters
            WsaHelper.sendFault(
                    wsContext,
                    SOAPVersion.SOAP_11,
                    TxFault.InvalidParameters,
                    "Received RegisterResponse for unknown activity id: " + activityId );
            if (logger.isLogging(Level.WARNING)) {
                logger.warning("synchronousRegister", LocalizationMessages.NONEXISTENT_ACTIVITY_3010(activityId));
            }
        }

        Registrant r = null;
        switch (requestProtocol) {
            case DURABLE:
            case VOLATILE:
            case COMPLETION:
                r = new ATParticipant(c, registerRequest);
                c.addRegistrant(r, wsContext);
                break;

            case WSAT2004:
            case UNKNOWN:
                // send fault S4.3 wscoor:Invalid Parameters
                WsaHelper.sendFault(
                        wsContext,
                        SOAPVersion.SOAP_11,
                        TxFault.InvalidParameters,
                        requestProtocol.getUri() + " is not a recognized coordination type" );
                throw new UnsupportedOperationException(LocalizationMessages.UNRECOGNIZED_COORDINATION_TYPE_3001(requestProtocol));
        }

        // build the registerResponse message
        RegisterResponseType registerResponse = new RegisterResponseType();
        registerResponse.setCoordinatorProtocolService((MemberSubmissionEndpointReference) r.getCoordinatorProtocolService());
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("synchronousRegister", r.getCoordinatorProtocolService());
        }

        return registerResponse;
    }
}
