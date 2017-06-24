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

package com.sun.xml.ws.rx.mc.dev;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import java.util.Collection;

/**
 * Implementations of this interface that are registered with 
 * {@link com.sun.xml.ws.rx.mc.runtime.WsMcResponseHandler#processResponse(Packet)}
 * are invoked to handle protocol response messages that don't correlate with any 
 * client request.
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public interface ProtocolMessageHandler {

    /**
     * Provides information about all WS-Addressing actions that this handler understands and can process.
     *
     * @return collection of all WS-Addressing actions that this handler understands and can process.
     *         Must not return {@code null}.
     */
    public @NotNull Collection<String> getSuportedWsaActions();

    /**
     * <p>
     * This method is invoked from {@link com.sun.xml.ws.rx.mc.runtime.WsMcResponseHandler#processResponse(Packet)}
     * in case it is not possible to resolve WS-A {@code RelatesTo} header from the response message to an existing
     * suspended fiber. In such case it is assumed that the response may contain some general WS-* protocol message
     * and collection of registered {@link ProtocolMessageHandler}s is consulted.
     * </p>
     *
     * <p>
     * In case the WS-Addressing {@code wsa:Action} header matches one of the supported WS-Addressing actions returned 
     * from {@link #getSuportedWsaActions()} method, the {@link #processProtocolMessage(com.sun.xml.ws.api.message.Packet)}
     * is invoked on {@link ProtocolMessageHandler} instance to process the protocol message.
     * </p>
     *
     * @param protocolMessage a protocol message to be handled
     */
    public void processProtocolMessage(Packet protocolMessage);
}
