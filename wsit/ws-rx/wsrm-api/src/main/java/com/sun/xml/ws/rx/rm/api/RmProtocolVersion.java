/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.rx.rm.api;

import javax.xml.namespace.QName;

/**
 * This enumeration contains all currently supported WS-ReliableMessaging versions.
 * <p/>
 * The choice of a WS-ReliableMessaging protocol version affects several attributes
 * of Metro Reliable Messaging implementation, including the following:
 * <ul>
 *     <li>FQN of WS-Policy assertions advertised in the service's WSDL</li>
 *     <li>Namespace of Ws-ReliableMessaging protocol messages</li>
 *     <li>Metro Reliable Messaging processing logic</li>
 * </ul>
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 *
 * @see #WSRM200502
 * @see #WSRM200702
 */
public enum RmProtocolVersion {
    WSRM200502(
    "http://schemas.xmlsoap.org/ws/2005/02/rm",
    RmAssertionNamespace.WSRMP_200502.toString(),
    "RMAssertion",
    "/LastMessage"),

    /**
     * <p>
     * This value represents the version of WS-ReliableMessaging protocol standardized
     * by OASIS organization. This is currently the most up-to-date version.
     * </p>
     * <p>
     * You should primarily use this version for your WS endpoints. It is compatible with
     * clients running on Metro 1.3 or .NET 3.5 and later.
     * </p>
     *
     * @see RmVersion
     */
    WSRM200702(
    "http://docs.oasis-open.org/ws-rx/wsrm/200702",
    RmAssertionNamespace.WSRMP_200702.toString(),
    "RMAssertion",
    "/CloseSequence");

    /**
     * Namespaces
     */
    public final String protocolNamespaceUri;
    public final String policyNamespaceUri;
    /**
     * Action constants
     */
    public final String ackRequestedAction;
    public final String createSequenceAction;
    public final String createSequenceResponseAction;
    public final String closeSequenceAction; // == lastAction
    public final String closeSequenceResponseAction;
    public final String sequenceAcknowledgementAction;
    public final String wsrmFaultAction;
    public final String terminateSequenceAction;
    public final String terminateSequenceResponseAction;
    /**
     * Specification assertion name
     */
    public final QName rmAssertionName;
    /**
     * Fault codes
     */
    public final QName sequenceTerminatedFaultCode;
    public final QName unknownSequenceFaultCode;
    public final QName invalidAcknowledgementFaultCode;
    public final QName messageNumberRolloverFaultCode;
    public final QName lastMessageNumberExceededFaultCode; // WS-RM 1.0 only
    public final QName createSequenceRefusedFaultCode;
    public final QName sequenceClosedFaultCode; // since WS-RM 1.1
    public final QName wsrmRequiredFaultCode; // since WS-RM 1.1


    private RmProtocolVersion(String protocolNamespaceUri, String policyNamespaceUri, String rmAssertionLocalName, String closeSequenceActionSuffix) {
        this.protocolNamespaceUri = protocolNamespaceUri;
        this.policyNamespaceUri = policyNamespaceUri;

        this.rmAssertionName = new QName(policyNamespaceUri, rmAssertionLocalName);

        this.ackRequestedAction = protocolNamespaceUri + "/AckRequested";
        this.createSequenceAction = protocolNamespaceUri + "/CreateSequence";
        this.createSequenceResponseAction = protocolNamespaceUri + "/CreateSequenceResponse";
        this.closeSequenceAction = protocolNamespaceUri + closeSequenceActionSuffix;
        this.closeSequenceResponseAction = protocolNamespaceUri + "/CloseSequenceResponse";
        this.sequenceAcknowledgementAction = protocolNamespaceUri + "/SequenceAcknowledgement";
        this.wsrmFaultAction = protocolNamespaceUri + "/fault";
        this.terminateSequenceAction = protocolNamespaceUri + "/TerminateSequence";
        this.terminateSequenceResponseAction = protocolNamespaceUri + "/TerminateSequenceResponse";

        this.sequenceTerminatedFaultCode = new QName(protocolNamespaceUri, "SequenceTerminated");
        this.unknownSequenceFaultCode = new QName(protocolNamespaceUri, "UnknownSequence");
        this.invalidAcknowledgementFaultCode = new QName(protocolNamespaceUri, "InvalidAcknowledgement");
        this.messageNumberRolloverFaultCode = new QName(protocolNamespaceUri, "MessageNumberRollover");
        this.lastMessageNumberExceededFaultCode = new QName(protocolNamespaceUri, "LastMessageNumberExceeded"); // WS-RM 1.0 only
        this.createSequenceRefusedFaultCode = new QName(protocolNamespaceUri, "CreateSequenceRefused");
        this.sequenceClosedFaultCode = new QName(protocolNamespaceUri, "SequenceClosed"); // since WS-RM 1.1
        this.wsrmRequiredFaultCode = new QName(protocolNamespaceUri, "WSRMRequired"); // since WS-RM 1.1
    }

    /**
     * Provides a default reliable messaging version value.
     *
     * @return a default reliable messaging version value. Currently returns {@link #WSRM200702}.
     *
     * @see RmVersion
     */
    public static RmProtocolVersion getDefault() {
        return RmProtocolVersion.WSRM200702; // if changed, update also in ReliableMesaging annotation
    }

    /**
     * Determines if the tested string is a valid WS-Addressing action header value
     * that belongs to a WS-ReliableMessaging protocol message
     *
     * @param WS-Addressing action string
     *
     * @return {@code true} in case the {@code wsaAction} parameter is a valid WS-Addressing
     *         action header value that belongs to a WS-ReliableMessaging protocol message
     */
    public boolean isProtocolAction(String wsaAction) {
        return (wsaAction != null) &&
                (isProtocolRequest(wsaAction) ||
                isProtocolResponse(wsaAction) ||
                isFault(wsaAction));
    }

    /**
     * Determines if the tested string is a valid WS-Addressing action header value
     * that belongs to a WS-ReliableMessaging protocol request message
     *
     * @param WS-Addressing action string
     *
     * @return {@code true} in case the {@code wsaAction} parameter is a valid WS-Addressing
     *         action header value that belongs to a WS-ReliableMessaging protocol request message
     */
    public boolean isProtocolRequest(String wsaAction) {
        return (wsaAction != null) &&
                (ackRequestedAction.equals(wsaAction) ||
                createSequenceAction.equals(wsaAction) ||
                closeSequenceAction.equals(wsaAction) ||
                terminateSequenceAction.equals(wsaAction));
    }

    /**
     * Determines if the tested string is a valid WS-Addressing action header value
     * that belongs to a WS-ReliableMessaging protocol response message
     *
     * @param WS-Addressing action string
     *
     * @return {@code true} in case the {@code wsaAction} parameter is a valid WS-Addressing
     *         action header value that belongs to a WS-ReliableMessaging protocol response message
     */
    public boolean isProtocolResponse(String wsaAction) {
        return (wsaAction != null) &&
                (createSequenceResponseAction.equals(wsaAction) ||
                closeSequenceResponseAction.equals(wsaAction) ||
                sequenceAcknowledgementAction.equals(wsaAction) ||
                terminateSequenceResponseAction.equals(wsaAction));
    }

    /**
     * Determines if the tested string is a valid WS-Addressing action header value
     * that belongs to a WS-ReliableMessaging protocol fault
     *
     * @param WS-Addressing action string
     *
     * @return {@code true} in case the {@code wsaAction} parameter is a valid WS-Addressing
     *         action header value that belongs to a WS-ReliableMessaging protocol fault
     */
    public boolean isFault(String wsaAction) {
        return wsrmFaultAction.equals(wsaAction);
    }

    @Override
    public String toString() {
        return "RmProtocolVersion" + 
                "{\n\tprotocolNamespaceUri=" + protocolNamespaceUri + 
                ",\n\tpolicyNamespaceUri=" + policyNamespaceUri + 
                "\n}";
    }    
}
