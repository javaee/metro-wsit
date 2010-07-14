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
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.OneWayFeature;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.tx.Protocol;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.developer.StatefulWebServiceManager;
import com.sun.xml.ws.tx.at.ATParticipant;
import com.sun.xml.ws.tx.common.ActivityIdentifier;
import com.sun.xml.ws.tx.common.AddressManager;
import com.sun.xml.ws.tx.common.StatefulWebserviceFactory;
import com.sun.xml.ws.tx.common.StatefulWebserviceFactoryFactory;
import com.sun.xml.ws.tx.common.TxFault;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.common.WsaHelper;
import com.sun.xml.ws.tx.webservice.member.coord.RegisterResponseType;
import com.sun.xml.ws.tx.webservice.member.coord.RegisterType;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationCoordinatorPortType;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationPortTypeRPC;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationRequesterPortType;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationRequesterPortTypeImpl;

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
 * @version $Revision: 1.18.22.2 $
 * @since 1.0
 */
public final class RegistrationManager {

    /* singleton instance */
    private static final RegistrationManager instance = new RegistrationManager();


    private static final URI localRegistrationURI =
            AddressManager.getPreferredAddress(RegistrationPortTypeRPC.class);

    private static final URI localAsynchronousRegistrationURI =
            AddressManager.getPreferredAddress(RegistrationCoordinatorPortType.class);

    private static final URI localRegistrationRequesterURI =
            AddressManager.getPreferredAddress(RegistrationRequesterPortType.class);

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
     * @param timeoutInMillis the expiration value for this context
     * @return an EPR containing the address of our registration service
     */
    public static EndpointReference newRegistrationEPR(ActivityIdentifier activityId, long timeoutInMillis) {
        StatefulWebserviceFactory swf = StatefulWebserviceFactoryFactory.getInstance();
        return swf.createService("Coordinator", "RegistrationCoordinator",
                localAsynchronousRegistrationURI, AddressingVersion.MEMBER,
                activityId.getValue(), null, timeoutInMillis);
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
        String msgID = WsaHelper.getMsgID(wsContext);
        EndpointReference registrationRequesterEPR = WsaHelper.getReplyTo(wsContext);
        WSEndpointReference faultTo = WsaHelper.getFaultTo(wsContext);
        if (logger.isLogging(Level.FINEST)) {
            logger.finest("register", "activityId:" + activityId + " register request msg id: " +
                    msgID + " replyTo: " + registrationRequesterEPR);
        }
        if (registrationRequesterEPR == null) {
            if (faultTo != null) {
                // send fault S4.3 wscoor:Invalid Parameters
                WsaHelper.sendFault(
                        faultTo,
                        null,
                        SOAPVersion.SOAP_11,
                        TxFault.InvalidParameters,
                        "register wsa:replyTo must be set for activityId " + activityId + " and msgId: " + msgID,  // no I18N - spec requires xml:lang="en"
                        msgID);
            }
            throw new WebServiceException(LocalizationMessages.REGISTER_REPLYTO_NOT_SET_3003(activityId, msgID));
        }

        Coordinator c = coordinationManager.getCoordinator(activityId);
        if (c == null) {
            // send fault S4.1 wscoor:Invalid State
            WsaHelper.sendFault(
                    faultTo,
                    null,
                    SOAPVersion.SOAP_11,
                    TxFault.InvalidState,
                    "attempting to register for an unknown activity Id: " + activityId + " and msgId: " + msgID,  // no I18N - spec requires xml:lang="en"
                    msgID);
            logger.warning("register", LocalizationMessages.REGISTER_FOR_UNKNOWN_ACTIVITY_3004(activityId, msgID));
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
                        registerRequest.getProtocolIdentifier() + " is not a recognized coordination type: activityId " +  // no I18N - spec requires xml:lang="en"
                                activityId + " and msgId " + msgID,
                        msgID);
                throw new UnsupportedOperationException(
                        LocalizationMessages.UNRECOGNIZED_COORDINATION_TYPE_3011(
                                registerRequest.getProtocolIdentifier(), activityId, msgID));
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
            logger.warning("register",
                    LocalizationMessages.REGISTERRESPONSE_FAILED_3005(
                            registrationRequesterEPR,
                            activityId,
                            msgID,
                            wse.getLocalizedMessage()));
            throw wse;
        } catch (Exception e) {
            logger.severe("register",
                    LocalizationMessages.REGISTERRESPONSE_FAILED_3005(
                            registrationRequesterEPR,
                            activityId,
                            msgID,
                            e.getLocalizedMessage()));
            throw new WebServiceException(e);
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

            // try synchronous register first and fallback to async if it fails
            // if (synchronousRegister(registrationEPR, c, registerParam, r)) return;

            // Asynchronous register/wait for asynchronous registerReply

            // setup stateful ws instance for registerResponse from remote registration coordinator
            StatefulWebserviceFactory swf = StatefulWebserviceFactoryFactory.getInstance();
            EndpointReference registrationRequesterEPR =
                    swf.createService("Coordinator", "RegistrationRequester",
                            localRegistrationRequesterURI, AddressingVersion.MEMBER,
                            r.getCoordinator().getIdValue(), r.getIdValue(),
                            r.getCoordinator().getExpires());

            // set replyTo for outgoing register message
            OneWayFeature owf = new OneWayFeature();
            owf.setReplyTo((new WSEndpointReference(registrationRequesterEPR)));
            RegistrationCoordinatorPortType registerCoordinator =
                    getCoordinatorService().getRegistrationCoordinator(registrationEPR, (WebServiceFeature) owf);

            if (logger.isLogging(Level.FINEST)) {
                logger.finest("RegistrationManager.register", "send wscoor:register to epr:" + registrationEPR
                        + " replyTo EPR: " + owf.getReplyTo());
            }

            boolean timedOut;
            r.setRegistrationCompleted(false);
            try {
                // prefer to try synchronouos register/register response than busy wait.
                registerCoordinator.registerOperation(registerParam);

                // next line is necessary. race condition that register did not complete before
                // tranaction initiator committed/rolled back tranaction.
                // This is why synchronous register preferable to asynchronous register.
                timedOut = r.waitForRegistrationResponse();
                if(logger.isLogging(Level.FINEST)) {
                    logger.finest("register(Coordinator, Registrant)", "timedOut = " + timedOut);
                }
            } catch (WebServiceException wse) {
                logger.warning("register",
                        LocalizationMessages.REGISTER_FAILED_3006(
                                registrationEPR, c.getIdValue(), wse.getLocalizedMessage()));
                throw wse;
            } finally {
                // release resources
                RegistrationRequesterPortTypeImpl rrpti = RegistrationRequesterPortTypeImpl.getManager().resolve(registrationRequesterEPR);
                if (rrpti != null) RegistrationRequesterPortTypeImpl.getManager().unexport(rrpti);
            }

            if (r.isRegistrationCompleted()) {
                if (logger.isLogging(Level.FINE)) {
                    logger.fine("register", "asynch registration succeeded. Coordinator Protocol Service is " +
                            r.getCoordinatorProtocolService());
                }
            }

            if(timedOut) {
                // send fault S4.4 wscoor:No Activity
                WsaHelper.sendFault(
                        null,
                        registrationEPR,
                        SOAPVersion.SOAP_11,
                        TxFault.NoActivity,
                        "registration timed out for activity id: " + c.getIdValue(),  // no I18N - spec requires xml:lang="en"
                        null /* TODO: what is RelatesTo in this case? */ );
                logger.warning("register", LocalizationMessages.REGISTRATION_TIMEOUT_3007(c.getIdValue(), registrationEPR));
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
    public void registerResponse(@NotNull WebServiceContext wsContext, @NotNull String activityId,
                                 @NotNull String registrantId, @NotNull RegisterResponseType registerResponse) {
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
                    "received registerResponse for non-existent registrant : " +  // no I18N - spec requires xml:lang="en"
                            registrantId + " for activityId:" + activityId);
            logger.warning("registerResponse",
                    LocalizationMessages.NONEXISTENT_REGISTRANT_3008(registrantId, activityId));
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
     * Synchronously register with remote coordinator.  Not all
     * providers will support the synchronous register/registerReply
     * since it is optional in the 2004 OASIS version of WS-COOR.
     *
     * @param registrationEPR epr of remote registration service
     * @param c coordinator
     * @param registerParam <register> message
     * @param r registrant
     * @return true if registration suceeded, false otherwise
     */
    private boolean synchronousRegister(EndpointReference registrationEPR, Coordinator c, RegisterType registerParam, Registrant r) {
//        RegistrationPortTypeRPC registerRPC = getCoordinatorService().getRegistration();
//        WsaHelper.initializeAsDestination((BindingProvider) registerRPC, registrationEPR);
//        ap = WsaHelper.getAddressingProperties((BindingProvider) registerRPC,
//                WsaHelper.BindingProviderContextType.REQUEST_CONTEXT);
//        ap.setReplyTo(newSynchronousRegistrationEPR((ActivityIdentifier)c.getId()));
//        try {
//            RegisterResponseType response = registerRPC.registerOperation(registerParam);
//            if (response == null) {
//                logger.warning("register", "synchronousRegisterRPC failed to get a non-null response.");
//            } else {
//                registerResponse(c.getIdValue(), r.getIdValue(), response);
//                logger.exiting("register", "synchronous register succeeded. Coordination Protocol Service:" +
//                    r.getCoordinatorProtocolService());
//                return true;
//            }
//        } catch (WebServiceException wse) {
//            // very likely that some WS-AT implementations might not implement this optional
//            // binding.  be prepared to use async version of register/register response.
//            logger.warning("register", "synchronous register failed, trying required registration protocol");
//            wse.printStackTrace();
//        }
//        logger.warning("register", "synchronous register failed, trying required registration protocol");
        return false;
    }

    /**
     * Handling incoming synchronous <register> and return <registerResponse>.
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
                    "Received RegisterResponse for unknown activity id: " + activityId );  // no I18N - spec requires xml:lang="en"
            logger.warning("synchronousRegister", LocalizationMessages.NONEXISTENT_ACTIVITY_3010(activityId));
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
                        requestProtocol.getUri() + " is not a recognized coordination type" );  // no I18N - spec requires xml:lang="en"
                throw new UnsupportedOperationException(
                        LocalizationMessages.UNRECOGNIZED_COORDINATION_TYPE_3011(
                                requestProtocol, activityId, WsaHelper.getMsgID(wsContext)));
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
