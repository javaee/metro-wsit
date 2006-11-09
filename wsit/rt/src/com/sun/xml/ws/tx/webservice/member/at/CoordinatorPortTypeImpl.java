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
package com.sun.xml.ws.tx.webservice.member.at;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.HeaderList;
import static com.sun.xml.ws.developer.JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY;
import com.sun.xml.ws.developer.MemberSubmissionAddressing;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.developer.StatefulWebServiceManager;
import com.sun.xml.ws.tx.at.ATCoordinator;
import static com.sun.xml.ws.tx.common.Constants.UNKNOWN_ID;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.coordinator.CoordinationManager;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.util.logging.Level;

/**
 * Implements WS-AT Coordinator  web service.
 * <p/>
 * Proceses notificaions from participants in coordinated atomic transaction activity.
 *
 * @author Joe.Fialli@Sun.COM
 * @version $Revision: 1.1 $
 * @since 1.0
 */
@MemberSubmissionAddressing
@com.sun.xml.ws.developer.Stateful
@WebService(serviceName = CoordinatorPortTypeImpl.serviceName,
        portName = CoordinatorPortTypeImpl.portName,
        endpointInterface = "com.sun.xml.ws.tx.webservice.member.at.CoordinatorPortType",
        targetNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat",
        wsdlLocation = "WEB-INF/wsdl/wsat.wsdl")
public class CoordinatorPortTypeImpl implements CoordinatorPortType {

    public static final String serviceName = "WSATCoordinator";
    public static final String portName = "Coordinator";

    /* stateful fields */
    public static StatefulWebServiceManager<CoordinatorPortTypeImpl> manager;
    private String activityId;
    private String participantId;

    @Resource
    private WebServiceContext wsContext;

    private static final TxLogger logger = TxLogger.getLogger(CoordinatorPortTypeImpl.class);

    private ATCoordinator coordinator = null;

    private EndpointReference fallbackEPR = null;

    public CoordinatorPortTypeImpl() {
    }

    public CoordinatorPortTypeImpl(String activityId, String participantId) {
        this.activityId = activityId;
        this.participantId = participantId;
    }

    private void initContextFromIncomingMessage() {
        coordinator = (ATCoordinator) CoordinationManager.getInstance().getCoordinator(activityId);
        if (wsContext != null) {
            MessageContext mc = wsContext.getMessageContext();
            //TODO  start using replyTo EPR if coordinator/participant is unknown or if initial remote 2PC notification
            //      fails one should see if replyTo EPR can get notfication through.
            HeaderList hdrLst = (HeaderList) mc.get(INBOUND_HEADER_LIST_PROPERTY);
            if (hdrLst != null) {
                fallbackEPR = hdrLst.getReplyTo(AddressingVersion.MEMBER, SOAPVersion.SOAP_11).toSpec(MemberSubmissionEndpointReference.class);
            }
        } else {
            if (logger.isLogging(Level.WARNING)) {
                logger.warning("initContextFromIncomingMessage", "wsContext unexpectedly null");
            }
        }

        if (activityId == UNKNOWN_ID) {
            if (logger.isLogging(Level.INFO)) {
                logger.info("Atomic Transaction Coordinator", "handling notification for an unknown transaction");
            }
            coordinator = null;
        } else {
            coordinator = (ATCoordinator) CoordinationManager.getInstance().getCoordinator(activityId);
        }
    }

    public void preparedOperation(Notification parameters) {
        final String METHOD_NAME = "preparedOperation";

        initContextFromIncomingMessage();
        if (logger.isLogging(Level.FINER)) {
            logger.entering(METHOD_NAME, getCoordIdPartId());
        }
        if (coordinator != null) {
            coordinator.prepared(participantId);
        } else {
            // TODO unknown activity id, send rollbackOperation to participant
            if (logger.isLogging(Level.SEVERE)) {
                logger.severe("preparedOperation", "unknown coordId or partId " + getCoordIdPartId());
            }
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting(METHOD_NAME, getCoordIdPartId());
        }
    }

    public void abortedOperation(Notification parameters) {
        final String METHOD_NAME = "abortedOperation";

        initContextFromIncomingMessage();
        if (logger.isLogging(Level.FINER)) {
            logger.entering(METHOD_NAME, getCoordIdPartId());
        }
        if (coordinator != null) {
            coordinator.aborted(participantId);
        } else {
            if (logger.isLogging(Level.SEVERE)) {
                logger.severe("abortedOperation", "unknown coordId or partId " + getCoordIdPartId());
            }
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting(METHOD_NAME, getCoordIdPartId());
        }
    }

    public void readOnlyOperation(Notification parameters) {
        final String METHOD_NAME = "readonlyOperation";

        initContextFromIncomingMessage();
        if (logger.isLogging(Level.FINER)) {
            logger.entering(METHOD_NAME, getCoordIdPartId());
        }
        if (coordinator != null) {
            coordinator.readonly(participantId);
        } else {
            // TODO unknown activity id, is logging enough or send something to fallback EPR
            if (logger.isLogging(Level.SEVERE)) {
                logger.severe("readonlyOperation", "unknown coordId or partId " + getCoordIdPartId());
            }
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting(METHOD_NAME, getCoordIdPartId());
        }
    }

    public void committedOperation(Notification parameters) {
        final String METHOD_NAME = "committedOperation";

        initContextFromIncomingMessage();
        if (logger.isLogging(Level.FINER)) {
            logger.entering(METHOD_NAME, getCoordIdPartId());
        }
        if (coordinator != null) {
            coordinator.committed(participantId);
        } else {
            // TODO unknown activity id, is logging enough or send something to participant fallbackEPR
            if (logger.isLogging(Level.SEVERE)) {
                logger.severe("committedOperation", "unknown coordId or partId " + getCoordIdPartId());
            }
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting(METHOD_NAME, getCoordIdPartId());
        }
    }

    public void replayOperation(Notification parameters) {
        final String METHOD_NAME = "replayOperation";

        initContextFromIncomingMessage();
        if (logger.isLogging(Level.FINER)) {
            logger.entering(METHOD_NAME, getCoordIdPartId());
        }
        if (coordinator != null) {
            coordinator.replay(participantId);
        } else {
            // TODO unknown activity id, is logging enough or send something to participant fallbackEPR
            if (logger.isLogging(Level.SEVERE)) {
                logger.severe("replayOperation", "unknown coordId or partId " + getCoordIdPartId());
            }
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting(METHOD_NAME, getCoordIdPartId());
        }
    }

    private String getCoordIdPartId() {
        return "CoorId=" + activityId + " PartId=" + participantId + " ";
    }
}
