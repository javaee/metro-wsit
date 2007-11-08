package com.sun.xml.ws.rm;

import com.sun.xml.bind.api.JAXBRIContext;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

/**
 * This is the class which will determine which RM version we are dealing with
 * WSRM 1.0 or WSRM 1.1
 */
public enum RMVersion {

    WSRM10(
    "http://schemas.xmlsoap.org/ws/2005/02/rm",
    "http://schemas.xmlsoap.org/ws/2005/02/rm/policy",
    "com.sun.xml.ws.rm.v200502") {

        private final JAXBRIContext jaxbRiContext;

        {
            try {
                jaxbRiContext = JAXBRIContext.newInstance(new Class[]{
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
                    RMConstants.W3C.getAcksToClass()
                }, null, null, null, false, null);
            } catch (JAXBException e) {
                throw new Error(e);
            }
        }

        @Override
        public String getNamespaceURI() {
            return namespaceUri;
        }

        @Override
        public String getPolicyNamespaceURI() {
            return policyNamespaceUri;
        }

        @Override
        public String getPackageName() {
            return packageName;
        }

        @Override
        public JAXBRIContext getJAXBContext() {
            return jaxbRiContext;
        }
    },
    WSRM11(
    "http://docs.oasis-open.org/ws-rx/wsrm/200702",
    "http://docs.oasis-open.org/ws-rx/wsrmp/200702",
    "com.sun.xml.ws.rm.v200702") {

        private final JAXBRIContext jaxbRiContext;

        {
            try {
                jaxbRiContext = JAXBRIContext.newInstance(new Class[]{
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
                    RMConstants.W3C.getAcksToClass()
                }, null, null, null, false, null);
            } catch (JAXBException e) {
                throw new Error(e);
            }
        }

        @Override
        public String getNamespaceURI() {
            return namespaceUri;
        }

        @Override
        public String getPolicyNamespaceURI() {
            return policyNamespaceUri;
        }

        @Override
        public String getPackageName() {
            return packageName;
        }

        @Override
        public JAXBRIContext getJAXBContext() {
            return jaxbRiContext;
        }
    };
    public final String namespaceUri;
    public final String policyNamespaceUri;
    public final String packageName;

    private RMVersion(String nsUri, String policynsuri, String packagename) {
        this.namespaceUri = nsUri;
        this.policyNamespaceUri = policynsuri;
        this.packageName = packagename;
    }

    public abstract String getNamespaceURI();

    public abstract String getPolicyNamespaceURI();

    public abstract String getPackageName();

    public abstract JAXBRIContext getJAXBContext();

    public String getCreateSequenceAction() {
        return namespaceUri + "/CreateSequence";
    }

    public String getTerminateSequenceAction() {
        return namespaceUri + "/TerminateSequence";
    }

    public String getAckRequestedAction() {
        return namespaceUri + "/AckRequested";
    }

    public String getCreateSequenceResponseAction() {
        return namespaceUri + "/CreateSequenceResponse";
    }

    public String getSequenceAcknowledgementAction() {
        return namespaceUri + "/SequenceAcknowledgement";
    }

    public String getMakeConnectionAction() {
        return namespaceUri + "/MakeConnection";
    }

    public QName getRMPolicyAssertionQName() {
        return new QName(getPolicyNamespaceURI(), "RMAssertion");
    }

    public QName getInactivityTimeoutAssertionQName() {
        return new QName(getPolicyNamespaceURI(), "InactivityTimeout");
    }

    public QName getSequenceSTRAssertionQName() {
        return new QName(getPolicyNamespaceURI(), "SequenceSTR");
    }

    public QName getSequenceTransportSecurityAssertionQName() {
        return new QName(getPolicyNamespaceURI(), "SequenceTransportSecurity");
    }

    /**
     * Returns {@link RMVersion} whose {@link #nsUri} equals to
     * the given string.
     *
     * @param nsUri must not be null.
     * @return always non-null.
     */
    public static RMVersion fromNsUri(String nsUri) {
        if (nsUri.equals(WSRM10.namespaceUri)) {
            return WSRM10;
        } else {
            //return WSRM 1.1 by default
            return WSRM11;
        }
    }

    public Marshaller createMarshaller() {
        try {
            Marshaller marshaller = getJAXBContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            return marshaller;
        } catch (JAXBException e) {
            return null;
        }
    }

    public Unmarshaller createUnmarshaller() {
        try {
            return getJAXBContext().createUnmarshaller();
        } catch (JAXBException e) {
            return null;
        }
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
        return new QName(getNamespaceURI(), "Sequence");
    }

    public QName getAckRequestedQName() {
        return new QName(getNamespaceURI(), "AckRequested");
    }

    public QName getSequenceAcknowledgementQName() {
        return new QName(getNamespaceURI(),
                "SequenceAcknowledgement");
    }

    /**
     * Returns the value of the WS-Addressing Action property stand alone Sequence
     * messages with Last child.
     *
     * @return The Action value (http://schemas.xmlsoap.org/ws/2005/02/rm/Last)
     */
    public String getLastAction() {
        return namespaceUri + "/LastMessage";
    }

    public String getCloseSequenceAction() {
        return namespaceUri + "/CloseSequence";
    }

    public String getCloseSequenceResponseAction() {
        return namespaceUri + "/CloseSequenceResponse";
    }

    public String getTerminateSequenceResponseAction() {
        return namespaceUri + "/TerminateSequenceResponse";
    }
}
