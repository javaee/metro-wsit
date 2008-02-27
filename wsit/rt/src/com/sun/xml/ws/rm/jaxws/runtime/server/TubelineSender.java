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

/*
 * TubelineSender.java
 *
 * @author Mike Grogan
 *
 * Created on August 27, 2007, 10:57 AM
 *
 */
package com.sun.xml.ws.rm.jaxws.runtime.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.rm.MessageSender;
import com.sun.xml.ws.rm.localization.RmLogger;

public class TubelineSender implements MessageSender, Fiber.CompletionCallback {

    private static final RmLogger LOGGER = RmLogger.getLogger(TubelineSender.class);
    private Fiber fiber;
    private Fiber parentFiber;
    private Packet requestPacket;
    private SOAPVersion soapVersion;
    private AddressingVersion addressingVersion;
    private RMServerTube tube;

    public TubelineSender(
            RMServerTube tube,
            Packet packet,
            SOAPVersion soapVersion,
            AddressingVersion addressingVersion) {

        this.requestPacket = packet;
        this.parentFiber = Fiber.current();
        this.fiber = parentFiber.owner.createFiber();
        this.soapVersion = soapVersion;
        this.addressingVersion = addressingVersion;
        this.tube = tube;
    }

    public void onCompletion(@NotNull Packet packet) {
        try {
            tube.postProcess(packet);
            parentFiber.resume(packet);
        } catch (Throwable t) {
            // TODO L10N
            LOGGER.severe("Unexcpected error occured, proceeding with handling the exception", t);
            onCompletion(t);
        }
    }

    public void onCompletion(@NotNull Throwable throwable) {
        Message mess = Messages.create(throwable, soapVersion);
        Packet packet = requestPacket.createServerResponse(
                mess,
                addressingVersion,
                soapVersion,
                addressingVersion.getDefaultFaultAction());
        parentFiber.resume(packet);
        // TODO: Should I really catch another Throwable here and just log & discard it?
    }

    public void send() {
        fiber.start(TubeCloner.clone(tube.nextTube()), requestPacket, this);
    }
}
