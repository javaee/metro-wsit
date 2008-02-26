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

package com.sun.xml.ws.assembler;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterPipeImpl;

/**
 * @author Arun Gupta
 */
public class ActionDumpPipe extends AbstractFilterPipeImpl {
    private final String name;
    private final WSBinding binding;

    public ActionDumpPipe(WSBinding binding, Pipe next) {
        this("ActionDumpPipe", binding, next);
    }

    public ActionDumpPipe(String name, WSBinding binding, Pipe next) {
        super(next);
        this.name = name;
        this.binding = binding;
    }

    /**
     * Copy constructor.
     */
    private ActionDumpPipe(ActionDumpPipe that, PipeCloner cloner) {
        super(that, cloner);
        this.name = that.name;
        this.binding = that.binding;
    }

    public Packet process(Packet packet) {
        dump(packet);
        Packet reply = next.process(packet);
        dump(reply);
        return reply;
    }

    protected void dump(Packet packet) {
        if (packet.getMessage() != null)
            dumpAction(packet);
    }

    protected void dumpAction(Packet packet) {
        try {
            Message m = packet.getMessage().copy();

            String to = m.getHeaders().getTo(binding.getAddressingVersion(), binding.getSOAPVersion());
            String action = m.getHeaders().getAction(binding.getAddressingVersion(), binding.getSOAPVersion());

            System.out.println("{To, Action}: {" + to + ", " + action + "}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Pipe copy(PipeCloner cloner) {
        return new ActionDumpPipe(this, cloner);
    }
}
