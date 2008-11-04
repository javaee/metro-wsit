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

import com.sun.xml.ws.tx.runtime.TxTubeFactory;
import com.sun.xml.ws.dump.ActionDumpTubeFactory;
import com.sun.xml.ws.assembler.jaxws.*;
import com.sun.xml.ws.messagedump.MessageDumpingTubeFactory;

import com.sun.xml.ws.rm.runtime.RmTubeFactory;
import com.sun.xml.ws.rm.runtime.testing.PacketFilteringTubeFactory;
import com.sun.xml.ws.runtime.config.TubelineDefinition;
import com.sun.xml.wss.provider.wsit.SecurityTubeFactory;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class TubelineAssemblyController {

    private static final String BEFORE_SUFFIX = ".before";
    private static final String AFTER_SUFFIX = ".after";
    private static final String TRANSPORT_SUFFIX = ".transport";
    private static final String WSS_SUFFIX = ".wss";
    private static final String WSA_SUFFIX = ".wsa";
    private static final String WSRM_SUFFIX = ".wsrm";
    private static final String WSTX_SUFFIX = ".wstx";
    //
    private static final TubeCreator transportTubeCreator = new TubeCreator(new TransportTubeFactory());
    private static final TubeCreator messageDumpingTubeCreator = new TubeCreator(new MessageDumpingTubeFactory());
    private static final TubeCreator packetFilteringTubeCreator = new TubeCreator(new PacketFilteringTubeFactory());
    private static final TubeCreator actionDumpTubeCreator = new TubeCreator(new ActionDumpTubeFactory());
    private static final TubeCreator securityTubeCreator = new TubeCreator(new SecurityTubeFactory());
    private static final TubeCreator reliableMessagingTubeCreator = new TubeCreator(new RmTubeFactory());
    private static final TubeCreator transactionsTubeCreator = new TubeCreator(new TxTubeFactory());
    private static final TubeCreator addressingTubeCreator = new TubeCreator(new AddressingTubeFactory());
    private static final TubeCreator monitoringTubeCreator = new TubeCreator(new MonitoringTubeFactory());
    private static final TubeCreator mustUnderstandTubeCreator = new TubeCreator(new MustUnderstandTubeFactory());
    private static final TubeCreator validationTubeCreator = new TubeCreator(new ValidationTubeFactory());
    private static final TubeCreator handlerTubeCreator = new TubeCreator(new HandlerTubeFactory());
    private static final TubeCreator terminalTubeCreator = new TubeCreator(new TerminalTubeFactory());
//
    private static final TubeCreator[] clientCreators = new TubeCreator[]{
        transportTubeCreator,
        messageDumpingTubeCreator,
        packetFilteringTubeCreator,
        new TubeCreator(new DumpTubeFactory("")),
        actionDumpTubeCreator,
        new TubeCreator(new DumpTubeFactory(TRANSPORT_SUFFIX)),
        new TubeCreator(new DumpTubeFactory(WSS_SUFFIX + AFTER_SUFFIX)),
        securityTubeCreator,
        new TubeCreator(new DumpTubeFactory(WSS_SUFFIX + BEFORE_SUFFIX)),
        // TODO MEX pipe here
        new TubeCreator(new DumpTubeFactory(WSRM_SUFFIX + AFTER_SUFFIX)),
        reliableMessagingTubeCreator,
        new TubeCreator(new DumpTubeFactory(WSRM_SUFFIX + BEFORE_SUFFIX)),
        new TubeCreator(new DumpTubeFactory(WSTX_SUFFIX + AFTER_SUFFIX)),
        transactionsTubeCreator,
        new TubeCreator(new DumpTubeFactory(WSTX_SUFFIX + BEFORE_SUFFIX)),
        new TubeCreator(new DumpTubeFactory(WSA_SUFFIX + AFTER_SUFFIX)),
        addressingTubeCreator,
        new TubeCreator(new DumpTubeFactory(WSA_SUFFIX + BEFORE_SUFFIX)),
        monitoringTubeCreator,
        mustUnderstandTubeCreator,
        validationTubeCreator,
        handlerTubeCreator,
        terminalTubeCreator

    };
    private static final TubeCreator[] serverCreators = new TubeCreator[]{
        terminalTubeCreator,
        validationTubeCreator,
        handlerTubeCreator,
        mustUnderstandTubeCreator,
        monitoringTubeCreator,
        new TubeCreator(new DumpTubeFactory(WSTX_SUFFIX + AFTER_SUFFIX)),
        transactionsTubeCreator,
        new TubeCreator(new DumpTubeFactory(WSTX_SUFFIX + BEFORE_SUFFIX)),
        new TubeCreator(new DumpTubeFactory(WSRM_SUFFIX + AFTER_SUFFIX)),
        reliableMessagingTubeCreator,
        new TubeCreator(new DumpTubeFactory(WSRM_SUFFIX + BEFORE_SUFFIX)),
        new TubeCreator(new DumpTubeFactory(WSA_SUFFIX + AFTER_SUFFIX)),
        addressingTubeCreator,
        new TubeCreator(new DumpTubeFactory(WSA_SUFFIX + BEFORE_SUFFIX)),
        // TODO MEX pipe here ?
        new TubeCreator(new DumpTubeFactory(WSS_SUFFIX + AFTER_SUFFIX)),
        securityTubeCreator,
        new TubeCreator(new DumpTubeFactory(WSS_SUFFIX + BEFORE_SUFFIX)),
        new TubeCreator(new DumpTubeFactory(TRANSPORT_SUFFIX)),
        actionDumpTubeCreator,
        new TubeCreator(new DumpTubeFactory("")),
        packetFilteringTubeCreator,
        messageDumpingTubeCreator,
        transportTubeCreator};

    /**
     * Provides a ordered collection of WSIT/Metro client-side tube creators that are be used to
     * construct a client-side Metro tubeline in {@link TubelineAssemblerFactoryImpl}.
     *
     * The order of the tube creators in the collection is last-to-first from the
     * client side request message processing perspective.
     *
     * <b>
     * WARNING: This method is part of Metro internal API and may be changed, removed or
     * replaced by a different method without a prior notice. The method SHOULD NOT be used
     * outside of Metro codebase.
     * </b>
     *
     * @param endpointUri URI of the endpoint for which the collection of tube factories should be returned
     *
     * @return collection of WSIT/Metro client-side tube creators
     */
    Collection<TubeCreator> getClientSideTubeCreators(URI endpointUri) {
        return Arrays.asList(clientCreators);
        // TODO implement loading from file
    }

    /**
     * Provides a ordered collection of WSIT/Metro server-side tube creators that are be used to
     * construct a server-side Metro tubeline for a given endpoint in {@link TubelineAssemblerFactoryImpl}.
     *
     * The order of the tube creators in the collection is last-to-first from the
     * server side request message processing perspective.
     *
     * <b>
     * WARNING: This method is part of Metro internal API and may be changed, removed or
     * replaced by a different method without a prior notice. The method SHOULD NOT be used
     * outside of Metro codebase.
     * </b>
     * 
     * @param endpointUri URI of the endpoint for which the collection of tube factories should be returned
     *
     * @return collection of WSIT/Metro client-side tube creators
     */
    Collection<TubeCreator> getServerSideTubeCreators(URI endpointUri) {
        return Arrays.asList(serverCreators);
        // TODO implement loading from file
    }

    private TubelineDefinition getTubelineDefinition(URI endpointUri) {
        return null; // TODO implement
    }

    private TubelineDefinition getDefaultApplicationTubelineDefinition() {
        return null; // TODO implement
    }

    private TubelineDefinition getDefaultMetroTubelineDefinition() {
        return null; // TODO implement
    }
}