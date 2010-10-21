/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.tx.at.v10.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sun.xml.ws.tx.at.v10.types package.
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

    private final static QName _Replay_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "Replay");
    private final static QName _Committed_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "Committed");
    private final static QName _ReadOnly_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "ReadOnly");
    private final static QName _Aborted_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "Aborted");
    private final static QName _Commit_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "Commit");
    private final static QName _Prepare_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "Prepare");
    private final static QName _Prepared_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "Prepared");
    private final static QName _Rollback_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "Rollback");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sun.xml.ws.tx.at.v10.types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Notification }
     * 
     */
    public Notification createNotification() {
        return new Notification();
    }

    /**
     * Create an instance of {@link ATAlwaysCapability }
     * 
     */
    public ATAlwaysCapability createATAlwaysCapability() {
        return new ATAlwaysCapability();
    }

    /**
     * Create an instance of {@link ATAssertion }
     * 
     */
    public ATAssertion createATAssertion() {
        return new ATAssertion();
    }

    /**
     * Create an instance of {@link PrepareResponse }
     * 
     */
    public PrepareResponse createPrepareResponse() {
        return new PrepareResponse();
    }

    /**
     * Create an instance of {@link ReplayResponse }
     * 
     */
    public ReplayResponse createReplayResponse() {
        return new ReplayResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Notification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat", name = "Replay")
    public JAXBElement<Notification> createReplay(Notification value) {
        return new JAXBElement<Notification>(_Replay_QNAME, Notification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Notification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat", name = "Committed")
    public JAXBElement<Notification> createCommitted(Notification value) {
        return new JAXBElement<Notification>(_Committed_QNAME, Notification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Notification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat", name = "ReadOnly")
    public JAXBElement<Notification> createReadOnly(Notification value) {
        return new JAXBElement<Notification>(_ReadOnly_QNAME, Notification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Notification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat", name = "Aborted")
    public JAXBElement<Notification> createAborted(Notification value) {
        return new JAXBElement<Notification>(_Aborted_QNAME, Notification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Notification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat", name = "Commit")
    public JAXBElement<Notification> createCommit(Notification value) {
        return new JAXBElement<Notification>(_Commit_QNAME, Notification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Notification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat", name = "Prepare")
    public JAXBElement<Notification> createPrepare(Notification value) {
        return new JAXBElement<Notification>(_Prepare_QNAME, Notification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Notification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat", name = "Prepared")
    public JAXBElement<Notification> createPrepared(Notification value) {
        return new JAXBElement<Notification>(_Prepared_QNAME, Notification.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Notification }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat", name = "Rollback")
    public JAXBElement<Notification> createRollback(Notification value) {
        return new JAXBElement<Notification>(_Rollback_QNAME, Notification.class, null, value);
    }

}
