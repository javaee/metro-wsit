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

package com.sun.xml.ws.transport.tcp.servicechannel.stubs;

import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelException.ServiceChannelExceptionBean;
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
    public ServiceChannelExceptionBean createServiceChannelExceptionBean() {
        return new ServiceChannelExceptionBean();
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
    public JAXBElement<ServiceChannelExceptionBean> createServiceChannelExceptionBean(ServiceChannelExceptionBean value) {
        return new JAXBElement<ServiceChannelExceptionBean>(_ServiceChannelException_QNAME, ServiceChannelExceptionBean.class, null, value);
    }
}
