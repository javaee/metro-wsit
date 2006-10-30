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

package com.sun.xml.ws.transport.tcp.servicechannel.stubs;

import com.sun.xml.ws.transport.tcp.util.ChannelSettings;
import com.sun.xml.ws.transport.tcp.util.MimeType;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sun.xml.ws.transport.tcp.servicechannel.stubs package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _InitiateSessionResponse_QNAME = new QName("http://servicechannel.tcp.transport.ws.xml.sun.com/", "initiateSessionResponse");
    private final static QName _CloseSession_QNAME = new QName("http://servicechannel.tcp.transport.ws.xml.sun.com/", "closeSession");
    private final static QName _CloseChannelResponse_QNAME = new QName("http://servicechannel.tcp.transport.ws.xml.sun.com/", "closeChannelResponse");
    private final static QName _CloseChannel_QNAME = new QName("http://servicechannel.tcp.transport.ws.xml.sun.com/", "closeChannel");
    private final static QName _OpenChannel_QNAME = new QName("http://servicechannel.tcp.transport.ws.xml.sun.com/", "openChannel");
    private final static QName _InitiateSession_QNAME = new QName("http://servicechannel.tcp.transport.ws.xml.sun.com/", "initiateSession");
    private final static QName _OpenChannelResponse_QNAME = new QName("http://servicechannel.tcp.transport.ws.xml.sun.com/", "openChannelResponse");
    private final static QName _ServiceChannelException_QNAME = new QName("http://servicechannel.tcp.transport.ws.xml.sun.com/", "ServiceChannelException");
    private final static QName _CloseSessionResponse_QNAME = new QName("http://servicechannel.tcp.transport.ws.xml.sun.com/", "closeSessionResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sun.xml.ws.transport.tcp.servicechannel.stubs
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link OpenChannelResponse }
     * 
     */
    public OpenChannelResponse createOpenChannelResponse() {
        return new OpenChannelResponse();
    }

    /**
     * Create an instance of {@link ServiceChannelException }
     * 
     */
    public ServiceChannelException createServiceChannelException() {
        return new ServiceChannelException();
    }

    /**
     * Create an instance of {@link CloseSessionResponse }
     * 
     */
    public CloseSessionResponse createCloseSessionResponse() {
        return new CloseSessionResponse();
    }

    /**
     * Create an instance of {@link CloseChannelResponse }
     * 
     */
    public CloseChannelResponse createCloseChannelResponse() {
        return new CloseChannelResponse();
    }

    /**
     * Create an instance of {@link InitiateSessionResponse }
     * 
     */
    public InitiateSessionResponse createInitiateSessionResponse() {
        return new InitiateSessionResponse();
    }

    /**
     * Create an instance of {@link CloseSession }
     */
    public CloseSession createCloseSession() {
        return new CloseSession();
    }

    /**
     * Create an instance of {@link ChannelSettings }
     * 
     */
    public ChannelSettings createChannelSettings() {
        return new ChannelSettings();
    }

    /**
     * Create an instance of {@link MimeType }
     * 
     */
    public MimeType createMimeType() {
        return new MimeType();
    }

    /**
     * Create an instance of {@link OpenChannel }
     * 
     */
    public OpenChannel createOpenChannel() {
        return new OpenChannel();
    }

    /**
     * Create an instance of {@link InitiateSession }
     * 
     */
    public InitiateSession createInitiateSession() {
        return new InitiateSession();
    }

    /**
     * Create an instance of {@link CloseChannel }
     * 
     */
    public CloseChannel createCloseChannel() {
        return new CloseChannel();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InitiateSessionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/", name = "initiateSessionResponse")
    public JAXBElement<InitiateSessionResponse> createInitiateSessionResponse(InitiateSessionResponse value) {
        return new JAXBElement<InitiateSessionResponse>(_InitiateSessionResponse_QNAME, InitiateSessionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CloseSession }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/", name = "closeSession")
    public JAXBElement<CloseSession> createCloseSession(CloseSession value) {
        return new JAXBElement<CloseSession>(_CloseSession_QNAME, CloseSession.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CloseChannelResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/", name = "closeChannelResponse")
    public JAXBElement<CloseChannelResponse> createCloseChannelResponse(CloseChannelResponse value) {
        return new JAXBElement<CloseChannelResponse>(_CloseChannelResponse_QNAME, CloseChannelResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CloseChannel }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/", name = "closeChannel")
    public JAXBElement<CloseChannel> createCloseChannel(CloseChannel value) {
        return new JAXBElement<CloseChannel>(_CloseChannel_QNAME, CloseChannel.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OpenChannel }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/", name = "openChannel")
    public JAXBElement<OpenChannel> createOpenChannel(OpenChannel value) {
        return new JAXBElement<OpenChannel>(_OpenChannel_QNAME, OpenChannel.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InitiateSession }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/", name = "initiateSession")
    public JAXBElement<InitiateSession> createInitiateSession(InitiateSession value) {
        return new JAXBElement<InitiateSession>(_InitiateSession_QNAME, InitiateSession.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OpenChannelResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/", name = "openChannelResponse")
    public JAXBElement<OpenChannelResponse> createOpenChannelResponse(OpenChannelResponse value) {
        return new JAXBElement<OpenChannelResponse>(_OpenChannelResponse_QNAME, OpenChannelResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceChannelException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/", name = "ServiceChannelException")
    public JAXBElement<ServiceChannelException> createServiceChannelException(ServiceChannelException value) {
        return new JAXBElement<ServiceChannelException>(_ServiceChannelException_QNAME, ServiceChannelException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CloseSessionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/", name = "closeSessionResponse")
    public JAXBElement<CloseSessionResponse> createCloseSessionResponse(CloseSessionResponse value) {
        return new JAXBElement<CloseSessionResponse>(_CloseSessionResponse_QNAME, CloseSessionResponse.class, null, value);
    }

}
