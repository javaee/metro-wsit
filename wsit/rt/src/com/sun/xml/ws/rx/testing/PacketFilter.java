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
package com.sun.xml.ws.rx.testing;

import com.sun.xml.ws.api.message.Packet;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.runtime.JaxwsApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.RuntimeContext;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public abstract class PacketFilter {

    protected static final long UNSPECIFIED = -1;
    private static final Logger LOGGER = Logger.getLogger(PacketFilter.class);
    
    private RuntimeContext rc;

    protected PacketFilter() {
    }
    
    /**
     * Method is called during the client-side request packet processing, which means that it is called BEFORE the request 
     * is sent to the service.
     * 
     * @param request original request packet to be filtered
     * 
     * @return filtered packet
     * 
     * @exception java.lang.Exception any exception that may occur during processing.
     */
    public abstract Packet filterClientRequest(Packet request) throws Exception;

    /**
     * Method is called during the server-side response packet processing, which means that it is called BEFORE the response 
     * is sent to the client.
     * 
     * @param response original response packet to be filtered
     * 
     * @return filtered packet
     * 
     * @exception java.lang.Exception any exception that may occur during processing.
     */
    public abstract Packet filterServerResponse(Packet response) throws Exception;

    /**
     * Retrieves RM sequence identifier form the message stored in the packet.
     * 
     * @param packet packet to be checked for the RM sequence identifier
     * 
     * @return RM sequence identifier. May return {@code null} if there is no RM sequence identifier 
     * associated with this packet.
     */
    protected final String getSequenceId(Packet packet) {
        try {
            if (notInitialized(packet) || isRmProtocolMessage(packet)) {
                return null;
            }

            JaxwsApplicationMessage message = new JaxwsApplicationMessage(packet, packet.getMessage().getID(rc.addressingVersion, rc.soapVersion));
            rc.protocolHandler.loadSequenceHeaderData(message, message.getJaxwsMessage());
            return message.getSequenceId();
        } catch (Exception ex) {
            LOGGER.warning("Unexpected exception occured", ex);
            return null;
        }
    }

    /**
     * Retrieves RM sequence message identifier form the message stored in the packet.
     * 
     * @param packet packet to be checked for the RM message identifier
     * 
     * @return RM sequence message identifier. May return {@link UNSPECIFIED} if there is no RM message identifier 
     * associated with this packet.
     */
    protected final long getMessageId(Packet packet) {
        try {
            if (notInitialized(packet) || isRmProtocolMessage(packet)) {
                return UNSPECIFIED;
            }
            
            JaxwsApplicationMessage message = new JaxwsApplicationMessage(packet, packet.getMessage().getID(rc.addressingVersion, rc.soapVersion));
            rc.protocolHandler.loadSequenceHeaderData(message, message.getJaxwsMessage());
            return message.getMessageNumber();
        } catch (Exception ex) {
            LOGGER.warning("Unexpected exception occured", ex);
            return UNSPECIFIED;
        }
    }
    
    /**
     * Provides information on RM version configured on the current web service port.
     * May return {@code null} if RM is not enabled on the port.
     * 
     * @return RM version configured on the current WS port or {@code null} if RM is not enabled.
     */
    protected final RmVersion getRmVersion() {
        return rc.rmVersion;
    }

    protected final boolean isRmProtocolMessage(Packet packet) {
        return rc.rmVersion.isProtocolAction(rc.communicator.getWsaAction(packet));
    }

    final void configure(RuntimeContext context) {
        this.rc = context;
    }

    private boolean notInitialized(Packet packet) {
        return rc == null || packet == null || packet.getMessage() == null;
    }
}
