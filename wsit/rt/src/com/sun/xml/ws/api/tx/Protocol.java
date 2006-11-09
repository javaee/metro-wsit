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
package com.sun.xml.ws.api.tx;

/**
 * This enum defines types for the AT protocol identifiers.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.1 $
 * @since 1.0
 */
public enum Protocol {
    /* WS-AT protocols */
    WSAT2004("http://schemas.xmlsoap.org/ws/2004/10/wsat"),
    COMPLETION("http://schemas.xmlsoap.org/ws/2004/10/wsat/Completion"),
    DURABLE("http://schemas.xmlsoap.org/ws/2004/10/wsat/Durable2PC"),
    VOLATILE("http://schemas.xmlsoap.org/ws/2004/10/wsat/Volatile2PC"),
    /* other protocols would follow here */
    /* unknown */
    UNKNOWN("unknown protocol");

    /* convenience field containing string represenation of protocol uri */
    private final String uri;

    Protocol(String uri) {
        this.uri = uri;
    }

    /**
     * Get the uri of the protocol.
     *
     * @return The protocol uri String
     */
    public String getUri() {
        return uri;
    }

    /**
     * Return the Protocol object corresponding to the specified protocol uri
     *
     * @param protocolId the protocol id uri
     * @return the corresponding Protocol object
     */
    public static Protocol getProtocol(String protocolId) {
        if (WSAT2004.uri.equals(protocolId)) {
            return Protocol.WSAT2004;
        } else if (COMPLETION.uri.equals(protocolId)) {
            return Protocol.COMPLETION;
        } else if (DURABLE.uri.equals(protocolId)) {
            return Protocol.DURABLE;
        } else if (VOLATILE.uri.equals(protocolId)) {
            return Protocol.VOLATILE;
        } else {
            return Protocol.UNKNOWN;
        }
    }
}
