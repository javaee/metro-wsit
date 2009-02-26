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
package com.sun.xml.ws.rx.mc.runtime;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.rx.RxConfiguration;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.util.FiberExecutor;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class McServerTube extends AbstractFilterTubeImpl {
    private static final class ResponseStorage {
        void offer(Packet response, String clientUID) {
            // TODO implement
            throw new UnsupportedOperationException("Not implemented yet");
        }
    
        void offer(Throwable exception, String clientUID) {
            // TODO implement
            throw new UnsupportedOperationException("Not implemented yet");
        }

        private boolean hasPendingResponse(String clientUID) {
            // TODO implement
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    private static final class AppRequestProcessingCallback implements Fiber.CompletionCallback {
        private static final Logger LOGGER = Logger.getLogger(AppRequestProcessingCallback.class);
        private final ResponseStorage responseStorage;
        private final String clientUID;

        public AppRequestProcessingCallback(ResponseStorage responseStorage, String clientUID) {
            this.responseStorage = responseStorage;
            this.clientUID = clientUID;
        }

        public void onCompletion(Packet response) {
            responseStorage.offer(response, clientUID);
        }

        public void onCompletion(Throwable error) {
            LOGGER.severe(String.format("An exception has been thrown during a request processing for the client UID [ %s ]", clientUID), error);
            responseStorage.offer(error, clientUID);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(McServerTube.class);

    private final RxConfiguration configuration;
    private final FiberExecutor fiberExecutor;
    private final ResponseStorage responseStorage;

    McServerTube(RxConfiguration configuration, Tube tubelineHead) {
        super(tubelineHead);

        this.configuration = configuration;
        this.fiberExecutor = new FiberExecutor("McServerTubeCommunicator", tubelineHead);
        this.responseStorage = new ResponseStorage();
    }

    McServerTube(McServerTube original, TubeCloner cloner) {
        super(original, cloner);

        this.configuration = original.configuration;
        this.fiberExecutor = original.fiberExecutor;
        this.responseStorage = original.responseStorage;
    }

    @Override
    public AbstractTubeImpl copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new McServerTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processRequest(Packet request) {
        final Message message = request.getMessage();
        assert message != null : "Unexpected [null] message in the server-side Tube.processRequest()";

        if (isMakeConnectionRequest(message)) {
            return handleMakeConnectionRequest(request);
        }

        String clientUID = getClientUID(message);
        if (clientUID == null) {
            // don't bother - this is not a WS-MC enabled request
            return super.processRequest(request);
        }

        fiberExecutor.start(request, new AppRequestProcessingCallback(responseStorage, clientUID));
        return super.doReturnWith(request.createServerResponse(null, null, null, ""));
    }

    @Override
    public NextAction processResponse(Packet response) {
        Message message = response.getMessage();
        if (message != null) {
            String clientUID = getClientUID(message);
            if (clientUID != null) {
                if (responseStorage.hasPendingResponse(clientUID)) {
                    // TODO append Pending header if there are pending messages
                    throw new UnsupportedOperationException("Not implemented yet");
                }
            }
        }

        return super.processResponse(response);
    }

    @Override
    public NextAction processException(Throwable t) {
        return super.processException(t);
    }

    @Override
    public void preDestroy() {
        super.preDestroy();
    }

    private String getClientUID(Message message) {
        HeaderList headers = message.getHeaders();

        Header replyToHeader = headers.get(configuration.getAddressingVersion().replyToTag, false);
        if (replyToHeader != null) {
            try {
                String replyToAddress = replyToHeader.readAsEPR(configuration.getAddressingVersion()).getAddress();
                return configuration.getMcVersion().getClientId(replyToAddress);
            } catch (XMLStreamException ex) {
                // TODO L10N - same as on client side in McClientTube
                throw LOGGER.logSevereException(new RxRuntimeException("Error unmarshalling content of WS-A ReplyTo header", ex));
            }
        }

        return null;
    }

    private NextAction handleMakeConnectionRequest(Packet request) {
        Packet response = null;

        // TODO implement retrieve one of the pending messages for the client (if any) and create a response
        //      if there are pending exceptions, return as well
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private boolean isMakeConnectionRequest(final Message message) {
        final HeaderList headers = message.getHeaders();
        if (headers == null) {
            return false;
        }
        return configuration.getMcVersion().wsmcAction.equals(headers.getAction(configuration.getAddressingVersion(), configuration.getSoapVersion()));
    }
}
