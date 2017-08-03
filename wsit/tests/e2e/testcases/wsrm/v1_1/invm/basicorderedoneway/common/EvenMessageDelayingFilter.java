/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package wsrm.v1_1.invm.basicorderedoneway.common;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.rx.testing.PacketFilter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class EvenMessageDelayingFilter extends PacketFilter {

    private static final Logger LOGGER = Logger.getLogger(EvenMessageDelayingFilter.class.getName());
    private static final long MESSAGE_NUMBER_TO_BLOCK = 2;
    private static final AtomicInteger BLOCK_COUNT = new AtomicInteger(0);
    private static final int MAX_BLOCK_COUNT = 3;

    public EvenMessageDelayingFilter() {
        super();
    }

    @Override
    public Packet filterClientRequest(Packet request) throws Exception {
        long msgId = this.getMessageId(request);

        if (msgId == MESSAGE_NUMBER_TO_BLOCK && BLOCK_COUNT.getAndIncrement() < MAX_BLOCK_COUNT) {
            LOGGER.info(String.format("Blocking the request [ %d ] processing ...", msgId));
            return null;
        }
        return request;
    }

    @Override
    public Packet filterServerResponse(Packet response) throws Exception {
        return response;
    }
}
