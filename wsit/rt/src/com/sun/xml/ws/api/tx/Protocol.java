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
package com.sun.xml.ws.api.tx;

/**
 * This enum defines types for the AT protocol identifiers.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.3.22.2 $
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
