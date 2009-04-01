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

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.dump.MessageDumper.ProcessingState;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class WrapperDumpTube implements Tube {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    //
    private final MessageDumper messageDumper;
    private final int tubeId;
    //
    private final boolean logBefore;
    private final boolean logAfter;
    private final Tube wrappedTube;


    public WrapperDumpTube(boolean logBefore, boolean logAfter, Level loggingLevel, Tube wrappedTube) {
        final String wrappedTubeName = wrappedTube.getClass().getName();
        this.messageDumper = new MessageDumper(wrappedTubeName, Logger.getLogger(wrappedTubeName), loggingLevel);
        this.tubeId = ID_GENERATOR.incrementAndGet();

        this.logBefore = logBefore;
        this.logAfter = logAfter;
        this.wrappedTube = wrappedTube;
    }

    /**
     * Copy constructor.
     */
    private WrapperDumpTube(WrapperDumpTube that, TubeCloner cloner) {
        cloner.add(that, this);

        this.messageDumper = that.messageDumper;
        this.tubeId = ID_GENERATOR.incrementAndGet();

        this.logBefore = that.logBefore;
        this.logAfter = that.logAfter;
        this.wrappedTube = cloner.copy(that.wrappedTube);
    }

    public WrapperDumpTube copy(TubeCloner cloner) {
        return new WrapperDumpTube(this, cloner);
    }


    public NextAction processRequest(Packet request) {
        if (logBefore && messageDumper.isLoggable()) {
            messageDumper.dump(MessageDumper.MessageType.Request, ProcessingState.Received, messageDumper.convertToString(request), tubeId, Fiber.current().owner.id);
        }

        NextAction next = wrappedTube.processRequest(request);
        
        if (logAfter && messageDumper.isLoggable()) {
            messageDumper.dump(MessageDumper.MessageType.Request, ProcessingState.Processed, messageDumper.convertToString(next.getPacket()), tubeId, Fiber.current().owner.id);
            if (next.getThrowable() != null) {
                messageDumper.dump(MessageDumper.MessageType.Request, ProcessingState.Processed, messageDumper.convertToString(next.getThrowable()), tubeId, Fiber.current().owner.id);
            }
        }

        return next;
    }

    public NextAction processResponse(Packet response) {
        if (logBefore && messageDumper.isLoggable()) {
            messageDumper.dump(MessageDumper.MessageType.Request, ProcessingState.Received, messageDumper.convertToString(response), tubeId, Fiber.current().owner.id);
        }

        NextAction next = wrappedTube.processResponse(response);

        if (logAfter && messageDumper.isLoggable()) {
            messageDumper.dump(MessageDumper.MessageType.Request, ProcessingState.Processed, messageDumper.convertToString(next.getPacket()), tubeId, Fiber.current().owner.id);
            if (next.getThrowable() != null) {
                messageDumper.dump(MessageDumper.MessageType.Request, ProcessingState.Processed, messageDumper.convertToString(next.getThrowable()), tubeId, Fiber.current().owner.id);
            }
        }

        return next;
    }

    public NextAction processException(Throwable t) {
        if (logBefore && messageDumper.isLoggable()) {
            messageDumper.dump(MessageDumper.MessageType.Request, ProcessingState.Received, messageDumper.convertToString(t), tubeId, Fiber.current().owner.id);
        }

        NextAction next = wrappedTube.processException(t);
        if (logAfter && messageDumper.isLoggable()) {
            if (next.getPacket() != null) {
                messageDumper.dump(MessageDumper.MessageType.Request, ProcessingState.Processed, messageDumper.convertToString(next.getPacket()), tubeId, Fiber.current().owner.id);
            }
            messageDumper.dump(MessageDumper.MessageType.Request, ProcessingState.Processed, messageDumper.convertToString(next.getThrowable()), tubeId, Fiber.current().owner.id);
        }
        
        return next;
    }

    public void preDestroy() {
        wrappedTube.preDestroy();
    }
}
