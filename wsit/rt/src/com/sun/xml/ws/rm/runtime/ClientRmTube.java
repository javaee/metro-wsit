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
package com.sun.xml.ws.rm.runtime;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.assembler.WsitClientTubeAssemblyContext;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.rm.RmWsException;
import com.sun.xml.ws.rm.localization.RmLogger;
import java.io.IOException;
import java.net.SocketTimeoutException;
import javax.xml.ws.WebServiceException;

/**
 * Attaches additional RM-specific headers to each request message and ensures the reliable delivery of the message (in 
 * case of any problems with sending the message, the exception is evaluated and the message is scheduled for a resend 
 * if possible.
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class ClientRmTube extends AbstractFilterTubeImpl {

    private static final RmLogger LOGGER = RmLogger.getLogger(ClientRmTube.class);
    private final ClientRmSession session;
    private boolean resend;
    private Packet currentRequestPacket;

    public ClientRmTube(ClientRmTube original, TubeCloner cloner) {
        super(original, cloner);

        this.session = original.session;
        this.resend = false; // this is a single request processing-bound variable, setting to default in the tube copy
        this.currentRequestPacket = null;
    }

    public ClientRmTube(WsitClientTubeAssemblyContext context, Tube next) throws RmWsException {
        super(next);
        this.session = new ClientRmSession(context.getWsdlPort(), context.getBinding(), new ProtocolCommunicator(super.next));
        this.resend = false;
        this.currentRequestPacket = null;
    }

    @Override
    public ClientRmTube copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new ClientRmTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processRequest(Packet requestPacket) {
        LOGGER.entering();
        try {
//            if (resend) { // this is a resend
                // TODO (resending message): register with resend notifier and suspend, else send to next tube
//            } else {
                currentRequestPacket = session.processOutgoingPacket(requestPacket);
                return super.processRequest(currentRequestPacket);
//            }
        } catch (UnknownSequenceException ex) {
            LOGGER.logSevereException(ex);
            return doThrow(ex);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processResponse(Packet responsePacket) {
        LOGGER.entering();
        try {
            if (resend) {
                // FIXME this is a hack because suspend can return only to processResponse for now; this should normally happen in processRequest()
                resend = false;
                return super.doInvoke(super.next, responsePacket);
            } else {                
                return super.processResponse(session.processIncommingPacket(responsePacket));
            }
        } catch (UnknownSequenceException ex) {
            LOGGER.logSevereException(ex);
            return doThrow(ex);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processException(Throwable throwable) {
        LOGGER.entering();
        try {
            resend = checkResendPossibility(throwable);
            if (resend) {
                // eat exception and forward processing to this.processRequest() (INVOKE_AND_FORGET) for request message resend
                // FIXME this is a hack because suspend can return only to processResponse for now...
                session.registerForResend(Fiber.current(), currentRequestPacket);
                return super.doSuspend();
            } else {
                return super.processException(throwable);
            }
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public void preDestroy() {
        LOGGER.entering();
        try {
            session.close();
            super.preDestroy();
        } finally {
            LOGGER.exiting();
        }
    }

    private boolean checkResendPossibility(Throwable throwable) {
        if (throwable instanceof ClientTransportException) {
            return true;
        } else if (throwable instanceof WebServiceException) {
            //Unwrap exception and see if it makes sense to retry this request (no need to check for null).
            if (throwable.getCause() instanceof IOException || throwable.getCause() instanceof SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }
}
