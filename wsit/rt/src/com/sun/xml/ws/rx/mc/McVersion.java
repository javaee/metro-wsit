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
package com.sun.xml.ws.rx.mc;

import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.util.JaxbContextRepository;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public enum McVersion {

    WSMC200702(
    "http://docs.oasis-open.org/ws-rx/wsmc/200702",
    "http://docs.oasis-open.org/ws-rx/wsmc/200702",

    com.sun.xml.ws.rx.mc.protocol.wsmc200702.MakeConnectionElement.class,
    com.sun.xml.ws.rx.mc.protocol.wsmc200702.MessagePendingElement.class,
    com.sun.xml.ws.rx.mc.protocol.wsmc200702.UnsupportedSelectionType.class);

    /**
     * General constants
     */
    public final String namespaceUri;
    public final String policyNamespaceUri;
    /**
     * Action constants
     */
    public final String wsmcAction;
    public final String wsmcFaultAction;
    /**
     * Header names
     */
    public final QName messagePendingHeaderName;
    /**
     * Fault codes
     */
    public final QName unsupportedSelectionFaultCode;
    public final QName missingSelectionFaultCode;
    /**
     * Private fields
     */
    private final JaxbContextRepository jaxbContextRepository;

    McVersion(String nsUri, String policyNsUri, Class<?>... protocolClasses) {
        this.namespaceUri = nsUri;
        this.policyNamespaceUri = policyNsUri;
        this.wsmcAction = nsUri + "/MakeConnection";
        this.wsmcFaultAction = nsUri + "/fault";

        this.messagePendingHeaderName = new QName(namespaceUri, "MessagePending");

        this.unsupportedSelectionFaultCode = new QName(namespaceUri, "UnsupportedSelection");
        this.missingSelectionFaultCode = new QName(namespaceUri, "MissingSelection");

        this.jaxbContextRepository = new JaxbContextRepository(protocolClasses);
    }

    /**
     * TODO javadoc
     * 
     * @param replyToAddress
     * @return
     */
    public String getClientId(String eprAddress) {
        final String mcAnnonymousAddressPrefix = namespaceUri + "/anonymous?id=";
        if (eprAddress.startsWith(mcAnnonymousAddressPrefix)) {
            return eprAddress.substring(mcAnnonymousAddressPrefix.length());
        }
        return null;
    }

    /**
     * Determines if the tested string is a valid WS-Addressing action header value
     * that belongs to a WS-MakeConnection protocol message
     *
     * @param WS-Addressing action string
     *
     * @return {@code true} in case the {@code wsaAction} parameter is a valid WS-Addressing
     *         action header value that belongs to a WS-MakeConnection protocol message
     */
    public boolean isProtocolAction(String wsaAction) {
        return (wsaAction != null) && 
               (wsmcAction.equals(wsaAction) ||
               isMcFault(wsaAction));
    }

    /**
     * Determines if the tested string is a valid WS-Addressing action header value
     * that belongs to a WS-MakeConnection protocol fault
     *
     * @param WS-Addressing action string
     *
     * @return {@code true} in case the {@code wsaAction} parameter is a valid WS-Addressing
     *         action header value that belongs to a WS-MakeConnection protocol fault
     */
    public boolean isMcFault(String wsaAction) {
        return wsmcFaultAction.equals(wsaAction);
    }

    /**
     * TODO javadoc
     *
     * @return
     */
    public String getWsmcAnonymousAddress(String uuid) {
        return namespaceUri + "/anonymous?id=" + uuid;
    }

    /**
     * Creates JAXB {@link Unmarshaller} that is able to unmarshall protocol elements for given WS-MC version.
     * <p />
     * As JAXB unmarshallers are not thread-safe, this method should be used to create a new {@link Unmarshaller}
     * instance whenever there is a chance that the same instance might be invoked concurrently from multiple
     * threads. On th other hand, it is prudent to cache or pool {@link Unmarshaller} instances if possible as
     * constructing a new {@link Unmarshaller} instance is rather expensive.
     * <p />
     * For additional information see this <a href="https://jaxb.dev.java.net/guide/Performance_and_thread_safety.html">blog entry</a>.
     *
     * @return created JAXB unmarshaller
     *
     * @exception RxRuntimeException in case the creation of unmarshaller failed
     */
    public Unmarshaller getUnmarshaller(AddressingVersion av) throws RxRuntimeException {
        return jaxbContextRepository.getUnmarshaller(av);
    }

    /**
     * Returns JAXB context that is intitialized based on a given addressing version.
     *
     * @param av addressing version used to initialize JAXB context
     *
     * @return JAXB context that is intitialized based on a given addressing version.
     */
    public JAXBRIContext getJaxbContext(AddressingVersion av) {
        return jaxbContextRepository.getJaxbContext(av);
    }
}
