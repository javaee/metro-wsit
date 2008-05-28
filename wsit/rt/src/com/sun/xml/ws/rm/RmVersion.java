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
package com.sun.xml.ws.rm;

import com.sun.xml.bind.api.JAXBRIContext;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

/**
 * This is the class which will determine which RM version we are dealing with
 * WSRM 1.0 or WSRM 1.1
 */
public enum RmVersion {

    WSRM10(
    "http://schemas.xmlsoap.org/ws/2005/02/rm",
    "http://schemas.xmlsoap.org/ws/2005/02/rm/policy",
    com.sun.xml.ws.rm.v200502.AcceptType.class,
    com.sun.xml.ws.rm.v200502.AckRequestedElement.class,
    com.sun.xml.ws.rm.v200502.CreateSequenceElement.class,
    com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement.class,
    com.sun.xml.ws.rm.v200502.Expires.class,
    com.sun.xml.ws.rm.v200502.Identifier.class,
    com.sun.xml.ws.rm.v200502.OfferType.class,
    com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement.class,
    com.sun.xml.ws.rm.v200502.SequenceElement.class,
    com.sun.xml.ws.rm.v200502.SequenceFaultElement.class,
    com.sun.xml.ws.rm.v200502.TerminateSequenceElement.class,
    javax.xml.ws.wsaddressing.W3CEndpointReference.class),
    WSRM11(
    "http://docs.oasis-open.org/ws-rx/wsrm/200702",
    "http://docs.oasis-open.org/ws-rx/wsrmp/200702",
    com.sun.xml.ws.rm.v200702.AcceptType.class,
    com.sun.xml.ws.rm.v200702.AckRequestedElement.class,
    com.sun.xml.ws.rm.v200702.Address.class,
    com.sun.xml.ws.rm.v200702.CloseSequenceElement.class,
    com.sun.xml.ws.rm.v200702.CloseSequenceResponseElement.class,
    com.sun.xml.ws.rm.v200702.CreateSequenceElement.class,
    com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement.class,
    com.sun.xml.ws.rm.v200702.DetailType.class,
    com.sun.xml.ws.rm.v200702.Expires.class,
    com.sun.xml.ws.rm.v200702.Identifier.class,
    com.sun.xml.ws.rm.v200702.IncompleteSequenceBehaviorType.class,
    com.sun.xml.ws.rm.v200702.MakeConnectionElement.class,
    com.sun.xml.ws.rm.v200702.MessagePendingElement.class,
    com.sun.xml.ws.rm.v200702.OfferType.class,
    com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement.class,
    com.sun.xml.ws.rm.v200702.SequenceElement.class,
    com.sun.xml.ws.rm.v200702.SequenceFaultElement.class,
    com.sun.xml.ws.rm.v200702.TerminateSequenceElement.class,
    com.sun.xml.ws.rm.v200702.TerminateSequenceResponseElement.class,
    com.sun.xml.ws.rm.v200702.UsesSequenceSSL.class,
    com.sun.xml.ws.rm.v200702.UsesSequenceSTR.class,
    javax.xml.ws.wsaddressing.W3CEndpointReference.class);
    /**
     * General constants
     */
    public final String namespaceUri;
    public final String policyNamespaceUri;
    public final JAXBRIContext jaxbContext;
    public final Unmarshaller jaxbUnmarshaller;
    /**
     * Action constants
     */
    public final String ackRequestedAction;
    public final String createSequenceAction;
    public final String createSequenceResponseAction;
    public final String closeSequenceAction;
    public final String closeSequenceResponseAction;
    public final String lastAction;
    public final String makeConnectionAction;
    public final String sequenceAcknowledgementAction;
    public final String wsrmFaultAction;
    public final String terminateSequenceAction;
    public final String terminateSequenceResponseAction;
    /**
     * QName constants
     */
    public final QName ackRequestedQName;
    public final QName inactivityTimeoutAssertionQName;
    public final QName rmPolicyAssertionQName;
    public final QName sequenceAcknowledgementQName;
    public final QName sequenceQName;
    public final QName sequenceSTRAssertionQName;
    public final QName sequenceTransportSecurityAssertionQName;

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

    private RmVersion(String nsUri, String policynsuri, Class<?>... classes) {
        this.namespaceUri = nsUri;
        this.policyNamespaceUri = policynsuri;

        this.ackRequestedAction = namespaceUri + "/AckRequested";
        this.createSequenceAction = namespaceUri + "/CreateSequence";
        this.createSequenceResponseAction = namespaceUri + "/CreateSequenceResponse";
        this.closeSequenceAction = namespaceUri + "/CloseSequence";
        this.closeSequenceResponseAction = namespaceUri + "/CloseSequenceResponse";
        this.lastAction = namespaceUri + "/LastMessage";
        this.makeConnectionAction = namespaceUri + "/MakeConnection";
        this.sequenceAcknowledgementAction = namespaceUri + "/SequenceAcknowledgement";
        this.wsrmFaultAction = namespaceUri + "/fault";
        this.terminateSequenceAction = namespaceUri + "/TerminateSequence";
        this.terminateSequenceResponseAction = namespaceUri + "/TerminateSequenceResponse";

        this.ackRequestedQName = new QName(namespaceUri, "AckRequested");
        this.inactivityTimeoutAssertionQName = new QName(policyNamespaceUri, "InactivityTimeout");
        this.rmPolicyAssertionQName = new QName(policyNamespaceUri, "RMAssertion");
        this.sequenceAcknowledgementQName = new QName(namespaceUri, "SequenceAcknowledgement");
        this.sequenceQName = new QName(namespaceUri, "Sequence");
        this.sequenceSTRAssertionQName = new QName(policyNamespaceUri, "SequenceSTR");
        this.sequenceTransportSecurityAssertionQName = new QName(policyNamespaceUri, "SequenceTransportSecurity");

        this.sequenceTerminatedFaultCode = new QName(namespaceUri, "SequenceTerminated");
        this.unknownSequenceFaultCode = new QName(namespaceUri, "UnknownSequence");
        this.invalidAcknowledgementFaultCode = new QName(namespaceUri, "InvalidAcknowledgement");
        this.messageNumberRolloverFaultCode = new QName(namespaceUri, "MessageNumberRollover");
        this.lastMessageNumberExceededFaultCode = new QName(namespaceUri, "LastMessageNumberExceeded"); // WS-RM 1.0 only
        this.createSequenceRefusedFaultCode = new QName(namespaceUri, "CreateSequenceRefused");
        this.sequenceClosedFaultCode = new QName(namespaceUri, "SequenceClosed"); // since WS-RM 1.1
        this.wsrmRequiredFaultCode = new QName(namespaceUri, "WSRMRequired"); // since WS-RM 1.1

        try {
            this.jaxbContext = JAXBRIContext.newInstance(classes, null, null, null, false, null);
            this.jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new Error(e);
        }
    }

    /**
     * @deprecated Use {@link #isRmAction()} instead.
     */
    @Deprecated
    public boolean isRMAction(String wsaAction) {
        return isRmAction(wsaAction);
    }
    
    /**
     * TODO javadoc
     * 
     * @return
     */
    public boolean isRmAction(String wsaAction) {
        return (wsaAction != null) &&
                (isRmProtocolRequest(wsaAction) ||
                isRmProtocolResponse(wsaAction) ||
                isRmFault(wsaAction));        
    }
        
    /**
     * TODO javadoc
     * 
     * @return
     */
    public boolean isRmProtocolRequest(String wsaAction) {
        return (wsaAction != null) &&
                (ackRequestedAction.equals(wsaAction) ||
                createSequenceAction.equals(wsaAction) ||
                closeSequenceAction.equals(wsaAction) ||
                lastAction.equals(wsaAction) ||
                makeConnectionAction.equals(wsaAction) ||
                terminateSequenceAction.equals(wsaAction));
    }
    
    /**
     * TODO javadoc
     * 
     * @return
     */
    public boolean isRmProtocolResponse(String wsaAction) {
        return (wsaAction != null) &&
                (createSequenceResponseAction.equals(wsaAction) ||
                closeSequenceResponseAction.equals(wsaAction) ||
                sequenceAcknowledgementAction.equals(wsaAction) ||
                terminateSequenceResponseAction.equals(wsaAction));        
    }
    
    /**
     * TODO javadoc
     * 
     * @return
     */
    public boolean isRmFault(String wsaAction) {
        return wsrmFaultAction.equals(wsaAction);
    }
}
