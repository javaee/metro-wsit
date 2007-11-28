package com.sun.xml.ws.rm;

import com.sun.xml.bind.api.JAXBRIContext;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

/**
 * This is the class which will determine which RM version we are dealing with
 * WSRM 1.0 or WSRM 1.1
 */
public enum RMVersion {

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

//    /**
//     * Returns {@link RMVersion} whose {@link #nsUri} equals to
//     * the given string.
//     *
//     * @param nsUri must not be null.
//     * @return always non-null.
//     */
//    public static RMVersion fromNsUri(String nsUri) {
//        if (nsUri.equals(WSRM10.namespaceUri)) {
//            return WSRM10;
//        } else {
//            //return WSRM 1.1 by default
//            return WSRM11;
//        }
//    }
    public final String namespaceUri;
    public final String policyNamespaceUri;
    public final JAXBRIContext jaxbContext;
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
    public final String terminateSequenceAction;
    public final String terminateSequenceResponseAction;

    private RMVersion(String nsUri, String policynsuri, Class<?>... classes) {
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
        this.terminateSequenceAction = namespaceUri + "/TerminateSequence";
        this.terminateSequenceResponseAction = namespaceUri + "/TerminateSequenceResponse";

        try {
            this.jaxbContext = JAXBRIContext.newInstance(classes, null, null, null, false, null);
        } catch (JAXBException e) {
            throw new Error(e);
        }
    }

    // TODO: replace with public final fields
    public QName getRMPolicyAssertionQName() {
        return new QName(policyNamespaceUri, "RMAssertion");
    }

    public QName getInactivityTimeoutAssertionQName() {
        return new QName(policyNamespaceUri, "InactivityTimeout");
    }

    public QName getSequenceSTRAssertionQName() {
        return new QName(policyNamespaceUri, "SequenceSTR");
    }

    public QName getSequenceTransportSecurityAssertionQName() {
        return new QName(policyNamespaceUri, "SequenceTransportSecurity");
    }

    public QName getMessageNumberRolloverQname() {
        return new QName(namespaceUri, "MessageNumberRollover");
    }

    public QName getUnknownSequenceQname() {
        return new QName(namespaceUri, "UnknownSequence");
    }

    public QName getClosedSequenceQname() {
        return new QName(namespaceUri, "SequenceClosed");
    }

    public QName getCreateSequenceRefusedQname() {
        return new QName(namespaceUri, "CreateSequenceRefused");
    }

    public QName getSequenceTerminatedQname() {
        return new QName(namespaceUri, "SequenceTerminated");
    }

    public QName getSequenceQName() {
        return new QName(namespaceUri, "Sequence");
    }

    public QName getAckRequestedQName() {
        return new QName(namespaceUri, "AckRequested");
    }

    public QName getSequenceAcknowledgementQName() {
        return new QName(namespaceUri, "SequenceAcknowledgement");
    }

    public boolean isRMAction(String action) {
        return ackRequestedAction.equals(action) ||
                createSequenceAction.equals(action) ||
                createSequenceResponseAction.equals(action) ||
                closeSequenceAction.equals(action) ||
                closeSequenceResponseAction.equals(action) ||
                lastAction.equals(action) ||
                makeConnectionAction.equals(action) ||
                sequenceAcknowledgementAction.equals(action) ||
                terminateSequenceAction.equals(action) ||
                terminateSequenceResponseAction.equals(action);
    }
}
