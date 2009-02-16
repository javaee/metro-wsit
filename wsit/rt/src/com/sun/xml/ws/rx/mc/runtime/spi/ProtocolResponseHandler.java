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

package com.sun.xml.ws.rx.mc.runtime.spi;

import com.sun.xml.ws.api.message.Packet;

/**
 * Implementations of this interface that are registered with 
 * {@link com.sun.xml.ws.rx.mc.runtime.WsMcResponseHandler#processResponse(Packet)}
 * are invoked to handle protocol response messages that don't correlate with any 
 * client request.
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public interface ProtocolResponseHandler {

    /**
     * <p>
     * This method is invoked from {@link com.sun.xml.ws.rx.mc.runtime.WsMcResponseHandler#processResponse(Packet)}
     * in case it is not possible to resolve WS-A {@code RelatesTo} header from the response message to an existing
     * suspended fiber. In such case it is assumed that the response may contain some general WS-* protocol message
     * and chain of registered {@link ProtocolResponseHandler}s is invoked to handle the response message.
     * </p>
     *
     * <p>
     * Implementation of this method is expected to scan the response message and process it if possible. If the message
     * has been identified as an understood protocol message and processed, method is expected to return {@code true}.
     * In all other cases, {@code false} should be returned.
     * </p>
     *
     * @param response an unmatched protocol response to be handled
     *
     * @return {@code true} if the message has been identified as an understood protocol message and processed,
     *         {@code false} otherwise.
     */
    public boolean processProtocolResponse(Packet response);
}
