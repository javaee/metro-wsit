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
package com.sun.xml.ws.tx.webservice.member.at;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.tx.TXException;
import static com.sun.xml.ws.developer.JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY;
import com.sun.xml.ws.developer.MemberSubmissionAddressing;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.developer.Stateful;
import com.sun.xml.ws.developer.StatefulWebServiceManager;
import com.sun.xml.ws.tx.at.ATCoordinator;
import com.sun.xml.ws.tx.at.ATParticipant;
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
 * WS-Atomic Transaction participant protocol service
 *
 * @author Joe.Fialli@Sun.COM
 * @version $Revision: 1.7.6.2 $
 * @since 1.0
 */
@MemberSubmissionAddressing
@Stateful
@WebService(serviceName = ParticipantPortTypeImpl.serviceName,
        portName = ParticipantPortTypeImpl.portName,
        endpointInterface = "com.sun.xml.ws.tx.webservice.member.at.ParticipantPortType",
        targetNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat",
        wsdlLocation = "WEB-INF/wsdl/wsat.wsdl")
public class ParticipantPortTypeImpl implements ParticipantPortType {

    public static final String serviceName = "WSATCoordinator";
    public static final String portName = "Participant";

    private static final TxLogger logger = TxLogger.getLogger(ParticipantPortTypeImpl.class);

    @Resource
    private WebServiceContext wsContext;

    // stateful web service
    private static StatefulWebServiceManager<ParticipantPortTypeImpl> manager;
    private String activityId;
    private String participantId;

    private EndpointReference fallbackEPR;
    private ATParticipant participant;

    public ParticipantPortTypeImpl() {
    }

    public ParticipantPortTypeImpl(String activityId, String participantId) {
        this.activityId = activityId;
        this.participantId = participantId;
    }

    private void initPerOperationData() {
        participant = null;
        fallbackEPR = null;

        if (wsContext != null) {
            MessageContext mc = wsContext.getMessageContext();
            if (mc != null) {
                HeaderList hdrLst = (HeaderList) mc.get(INBOUND_HEADER_LIST_PROPERTY);
                if (hdrLst != null) {
                    fallbackEPR = hdrLst.getReplyTo(AddressingVersion.MEMBER, SOAPVersion.SOAP_11).
                            toSpec(MemberSubmissionEndpointReference.class);
                } else {
                    if (logger.isLogging(Level.WARNING)) {
                        logger.warning("initOperationData", "INBOUND_HEADER_LIST_PROPERTY unexpectedly null");
                    }
                }
            } else {
                if (logger.isLogging(Level.WARNING)) {
                    logger.warning("initOperationData", "WebServiceContext.getMessageContext() unexpectedly null");
                }
            }
        } else {
            if (logger.isLogging(Level.WARNING)) {
                logger.warning("initContextFromIncomingMessage", "wsContext unexpectedly null");
            }
        }

        ATCoordinator coordinator = null;
        if (UNKNOWN_ID.equals(activityId)) {
            if (logger.isLogging(Level.INFO)) {
                logger.info("Two Phase Commit Participant", "handling notification for an unknown transaction");
            }
            return;
        } else {
            coordinator = (ATCoordinator) CoordinationManager.getInstance().getCoordinator(activityId);
            if (coordinator != null) {
                try {
                    participant = (ATParticipant) coordinator.getRegistrant(participantId);
                } catch (ClassCastException ce) {
                    if (logger.isLogging(Level.WARNING)) {
                        logger.warning("getATPartcipant", ce.getLocalizedMessage());
                    }
                }
            }
        }
    }

    public void prepareOperation
            (Notification
                    parameters) {
        final String METHOD_NAME = "prepareOperation";

        initPerOperationData();
        if (logger.isLogging(Level.FINER)) {
            logger.entering(METHOD_NAME, getCoordIdPartId());
        }
        if (participant != null) {
            try {
                participant.prepare();
            } catch (TXException ex) {
                // TODO: should this be a fault
                if (logger.isLogging(Level.WARNING)) {
                    logger. warning("prepareOperation", "caught TXException during prepare");
                }
            }
        } else {
            if (fallbackEPR != null) {
                // 2004 WS-AT, Section 3.3: Participant unknown, must send abort back to wsa:ReplyTo
                if (logger.isLogging(Level.WARNING)) {
                    logger.warning("prepareOperation", "Unknown participant " + getCoordIdPartId() +
                            " reply with aborted to " + fallbackEPR);
                }
                ATParticipant.getATCoordinatorWS(fallbackEPR, null, false).abortedOperation(null);
            } else {
                if (logger.isLogging(Level.SEVERE)) {
                    logger.severe("prepareOperation", "required wsa:ReplyTo property not found");
                }
            }
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting(METHOD_NAME, getCoordIdPartId());
        }
    }

    public void commitOperation
            (Notification
                    parameters) {
        final String METHOD_NAME = "commitOperation";

        initPerOperationData();
        if (logger.isLogging(Level.FINER)) {
            logger.entering(METHOD_NAME, getCoordIdPartId());
        }
        if (participant != null) {
            try {
                participant.commit();
            } catch (TXException ex) {
                if (logger.isLogging(Level.WARNING)) {
                    logger.warning("commitOperation", ex.getLocalizedMessage());
                }
            }
        } else {
            // Participant unknown.
            // TODO: send committed to coordinator using wsa:replyTo of this message
            // Specified in 2004 WS-AT, Section 3.3
            // 2004 WS-AT, Section 3.3: Participant unknown, must send abort back to wsa:ReplyTo

            if (fallbackEPR != null) {
                if (logger.isLogging(Level.WARNING)) {
                    logger.warning("commitOperation", "Unknown participant " + getCoordIdPartId() +
                            " reply with committed to " + fallbackEPR);
                }
                ATParticipant.getATCoordinatorWS(fallbackEPR, null, false).committedOperation(null);
            } else {
                if (logger.isLogging(Level.SEVERE)) {
                    logger.severe("commitOperation", "required wsa:ReplyTo property not found");
                }
            }
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting(METHOD_NAME, getCoordIdPartId());
        }
    }

    public void rollbackOperation
            (Notification
                    parameters) {
        final String METHOD_NAME = "rollbackOperation";

        initPerOperationData();
        if (logger.isLogging(Level.FINER)) {
            logger.entering(METHOD_NAME, parameters);
        }
        if (participant != null) {
            participant.abort();
        } else {
            if (fallbackEPR != null) {
                // Particpant unknown. (no need to forget)
                if (logger.isLogging(Level.WARNING)) {
                    logger.warning("rollbackOperation", "Unknown participant " + getCoordIdPartId() +
                            " reply with aborted to" + fallbackEPR);
                }
                ATParticipant.getATCoordinatorWS(fallbackEPR, null, false).abortedOperation(null);
            } else {
                if (logger.isLogging(Level.SEVERE)) {
                    logger.severe("rollbackOperation", "required wsa:ReplyTo property not found");
                }
            }
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting(METHOD_NAME, getCoordIdPartId());
        }
    }

    private String getCoordIdPartId
            () {
        return "CoorId=" + activityId + " PartId=" + participantId + " ";
    }

    public static StatefulWebServiceManager<ParticipantPortTypeImpl> getManager() {
        return manager;
    }

    public static void setManager(StatefulWebServiceManager<ParticipantPortTypeImpl> aManager) {
        manager = aManager;
    }
}
