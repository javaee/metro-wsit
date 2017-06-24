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

package com.sun.xml.ws.rx.mc.api;

import javax.xml.namespace.QName;

/**
 * Enumeration holding supported WS-MakeConnection protocol versions
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public enum McProtocolVersion {

    WSMC200702(
    "http://docs.oasis-open.org/ws-rx/wsmc/200702",
    "http://docs.oasis-open.org/ws-rx/wsmc/200702");

    /**
     * Namespaces
     */
    public final String protocolNamespaceUri;
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

    private McProtocolVersion(String protocolNamespaceUri, String policyNamespaceUri) {
        this.protocolNamespaceUri = protocolNamespaceUri;
        this.policyNamespaceUri = policyNamespaceUri;

        this.wsmcAction = protocolNamespaceUri + "/MakeConnection";
        this.wsmcFaultAction = protocolNamespaceUri + "/fault";

        this.messagePendingHeaderName = new QName(protocolNamespaceUri, "MessagePending");

        this.unsupportedSelectionFaultCode = new QName(protocolNamespaceUri, "UnsupportedSelection");
        this.missingSelectionFaultCode = new QName(protocolNamespaceUri, "MissingSelection");
    }

    /**
     * Provides a default reliable messaging version value.
     *
     * @return a default reliable messaging version value. Currently returns {@link #WSRM200702}.
     *
     * @see RmVersion
     */
    public static McProtocolVersion getDefault() {
        return McProtocolVersion.WSMC200702; // if changed, update also MakeConnectionSupported annotation
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
               isFault(wsaAction));
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
    public boolean isFault(String wsaAction) {
        return wsmcFaultAction.equals(wsaAction);
    }

}
