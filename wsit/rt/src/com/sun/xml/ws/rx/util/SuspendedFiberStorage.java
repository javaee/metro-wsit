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

package com.sun.xml.ws.rx.util;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.istack.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class SuspendedFiberStorage extends TimestampedCollection<String, Fiber> {
    private static final Logger LOGGER = Logger.getLogger(SuspendedFiberStorage.class);

    @Override
    public Fiber register(String correlationId, Fiber subject) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format("Registering fiber [ %s ] with correlationId [ %s ] for suspend", subject.toString(), correlationId));
        }

        return super.register(correlationId, subject);
    }

    @Override
    public boolean register(long timestamp, Fiber subject) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format("Registering fiber [ %s ] with timestamp [ %d ] for suspend", subject.toString(), timestamp));
        }

        return super.register(timestamp, subject);
    }



    public void resumeFiber(String correlationId, Packet response) throws ResumeFiberException {
        Fiber fiber = remove(correlationId);
        if (fiber == null) {
            throw LOGGER.logSevereException(new ResumeFiberException(String.format("Unable to resume fiber with a response packet: No registered fiber found for correlationId [ %s ].", correlationId)));
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format("Resuming fiber [ %s ] with a response", fiber.toString()));
        }

        fiber.resume(response);
    }

    public void resumeFiber(String correlationId, Throwable error) throws ResumeFiberException {
        Fiber fiber = remove(correlationId);
        if (fiber == null) {
            throw LOGGER.logSevereException(new ResumeFiberException(String.format("Unable to resume fiber with a response packet: No registered fiber found for correlationId [ %s ].", correlationId)));
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format("Resuming fiber [ %s ] with an exception", fiber.toString()));
        }

        fiber.resume(error);
    }

    public void resumeAllFibers(Throwable error) {
        for (Fiber fiber : removeAll()) {
            fiber.resume(error);
        }
    }
}
