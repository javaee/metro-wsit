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
package com.sun.xml.ws.dump;

import com.sun.xml.ws.assembler.*;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class ActionDumpTubeFactory implements TubeFactory {

    public static final String CLIENT_NAME = "com.sun.xml.ws.assembler.client.action";
    public static final String SERVER_NAME = "com.sun.xml.ws.assembler.server.action";

    public Tube createTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
        if (Boolean.getBoolean(CLIENT_NAME)) {
            return new ActionDumpTube(CLIENT_NAME, context.getBinding(), context.getTubelineHead());
        }
        return context.getTubelineHead();
    }

    public Tube createTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
        if (Boolean.getBoolean(SERVER_NAME)) {
            return new ActionDumpTube(SERVER_NAME, context.getEndpoint().getBinding(), context.getTubelineHead());
        }
        return context.getTubelineHead();
    }

    private static class ActionDumpTube extends AbstractFilterTubeImpl {

        private final String name;
        private final WSBinding binding;

        public ActionDumpTube(String name, WSBinding binding, Tube next) {
            super(next);
            this.name = name;
            this.binding = binding;
        }

        /**
         * Copy constructor.
         */
        private ActionDumpTube(ActionDumpTube original, TubeCloner cloner) {
            super(original, cloner);
            this.name = original.name;
            this.binding = original.binding;
        }

        public ActionDumpTube copy(TubeCloner cloner) {
            return new ActionDumpTube(this, cloner);
        }

        @Override
        public NextAction processRequest(Packet request) {
            dumpAction(request.getMessage());
            return super.processRequest(request);
        }

        @Override
        public NextAction processResponse(Packet response) {
            dumpAction(response.getMessage());
            return super.processResponse(response);
        }

        private void dumpAction(@Nullable Message message) {
            if (message != null) {
                try {
                    Message messageCopy = message.copy();

                    String to = messageCopy.getHeaders().getTo(binding.getAddressingVersion(), binding.getSOAPVersion());
                    String action = messageCopy.getHeaders().getAction(binding.getAddressingVersion(), binding.getSOAPVersion());

                    System.out.println("{To, Action}: {" + to + ", " + action + "}");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
