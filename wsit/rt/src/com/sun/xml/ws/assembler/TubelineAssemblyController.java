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

import com.sun.xml.ws.messagedump.MessageDumpingTubeAppender;
import com.sun.xml.ws.tx.common.Util;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.transport.tcp.wsit.TCPConstants;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.helper.PipeAdapter;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.rx.mc.runtime.McTubeAppender;
import com.sun.xml.ws.rx.rm.runtime.RmTubeAppender;
import com.sun.xml.ws.rx.testing.PacketFilteringTubeAppender;
import com.sun.xml.ws.transport.tcp.wsit.TCPTransportPipeFactory;
import com.sun.xml.ws.tx.client.TxClientPipe;
import com.sun.xml.ws.tx.service.TxServerPipe;
import com.sun.xml.wss.provider.wsit.SecurityTubeAppender;
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
    private static final String WSMC_SUFFIX = ".wsmc";
    private static final String WSTX_SUFFIX = ".wstx";

    public static class TxTubeAppender implements TubeAppender {

        private static final String WSAT_SOAP_NSURI = "http://schemas.xmlsoap.org/ws/2004/10/wsat";
        private static final QName AT_ALWAYS_CAPABILITY = new QName(WSAT_SOAP_NSURI, "ATAlwaysCapability");
        private static final QName AT_ASSERTION = new QName(WSAT_SOAP_NSURI, "ATAssertion");

        /**
         * Adds TX tube to the client-side tubeline, depending on whether TX is enabled or not.
         * 
         * @param context wsit client tubeline assembler context
         * @return new tail of the client-side tubeline
         */
        public Tube appendTube(WsitClientTubeAssemblyContext context) {
            if (isTransactionsEnabled(context.getPolicyMap(), context.getWsdlPort(), false)) {
                return PipeAdapter.adapt(new TxClientPipe(context, context.getAdaptedTubelineHead()));
            } else {
                return context.getTubelineHead();
            }
        }

        /**
         * Adds TX tube to the service-side tubeline, depending on whether TX is enabled or not.
         * 
         * @param context wsit service tubeline assembler context
         * @return new head of the service-side tubeline
         */
        public Tube appendTube(WsitServerTubeAssemblyContext context) {
            if (isTransactionsEnabled(context.getPolicyMap(), context.getWsdlPort(), true)) {
                return PipeAdapter.adapt(new TxServerPipe(context, context.getAdaptedTubelineHead()));
            } else {
                return context.getTubelineHead();
            }
        }

        /**
         * Checks to see whether WS-Atomic Transactions are enabled or not.
         *
         * @param policyMap policy map for {@link this} assembler
         * @param wsdlPort the WSDLPort object
         * @param isServerSide true iff this method is being called from {@link PipelineAssembler#createServer(ServerPipeAssemblerContext)}
         * @return true if Transactions is enabled, false otherwise
         */
        private boolean isTransactionsEnabled(PolicyMap policyMap, WSDLPort wsdlPort, boolean isServerSide) {
            if (policyMap == null || wsdlPort == null ||
                    !Util.isJTAAvailable()) {     // false for standalone WSIT client or WSIT Service in Tomcat
                return false;
            }
            try {
                PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(wsdlPort.getOwner().getName(),
                        wsdlPort.getName());
                Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

                for (WSDLBoundOperation wbo : wsdlPort.getBinding().getBindingOperations()) {
                    PolicyMapKey operationKey = PolicyMap.createWsdlOperationScopeKey(wsdlPort.getOwner().getName(),
                            wsdlPort.getName(),
                            wbo.getName());
                    policy = policyMap.getOperationEffectivePolicy(operationKey);

                    if (policy != null) {
                        // look for ATAlwaysCapable on the server side
                        if ((isServerSide) && (policy.contains(AT_ALWAYS_CAPABILITY))) {
                            return true;
                        }

                        // look for ATAssertion in both client and server
                        if (policy.contains(AT_ASSERTION)) {
                            return true;
                        }
                    }
                }
            } catch (PolicyException e) {
                throw new WebServiceException(e);
            }

            return false;
        }
    }

    public static class DumpTubeAppender implements TubeAppender {

        public static final String PREFIX = "com.sun.xml.ws.assembler";
        public static final String CLIENT_PREFIX = PREFIX + ".client";
        public static final String SERVER_PREFIX = PREFIX + ".server";
        private final String name;

        public DumpTubeAppender(String name) {
            this.name = name;
        }

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            if (Boolean.getBoolean(CLIENT_PREFIX + name)) {
                return context.getWrappedContext().createDumpTube(name, System.out, context.getTubelineHead());
            }

            return context.getTubelineHead();
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            if (Boolean.getBoolean(SERVER_PREFIX + name)) {
                return context.getWrappedContext().createDumpTube(name, System.out, context.getTubelineHead());
            }

            return context.getTubelineHead();
        }
    }

    public static class TransportTubeAppender implements TubeAppender {

        private static final String AUTO_OPTIMIZED_TRANSPORT_POLICY_NAMESPACE_URI = "http://java.sun.com/xml/ns/wsit/2006/09/policy/transport/client";
        private static final QName AUTO_OPTIMIZED_TRANSPORT_POLICY_ASSERTION = new QName(AUTO_OPTIMIZED_TRANSPORT_POLICY_NAMESPACE_URI, "AutomaticallySelectOptimalTransport");

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            if (isOptimizedTransportEnabled(context.getPolicyMap(), context.getWsdlPort(), context.getPortInfo())) {
                return TCPTransportPipeFactory.doCreate(context, false);
            } else {
                return context.getWrappedContext().createTransportTube();
            }
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getTubelineHead();
        }

        /**
         * Checks to see whether OptimizedTransport is enabled or not.
         *
         * @param policyMap policy map for {@link this} assembler
         * @param port the WSDLPort object
         * @param portInfo the WSPortInfo object
         * @return true if OptimizedTransport is enabled, false otherwise
         */
        private boolean isOptimizedTransportEnabled(PolicyMap policyMap, WSDLPort port, WSPortInfo portInfo) {
            if (policyMap == null || (port == null && portInfo == null)) {
                return false;
            }

            String schema;
            QName serviceName;
            QName portName;
            if (port != null) {
                schema = port.getAddress().getURI().getScheme();
                serviceName = port.getOwner().getName();
                portName = port.getName();
            } else {
                schema = portInfo.getEndpointAddress().getURI().getScheme();
                serviceName = portInfo.getServiceName();
                portName = portInfo.getPortName();
            }
            
            if (com.sun.xml.ws.transport.tcp.util.TCPConstants.PROTOCOL_SCHEMA.equals(schema)) {
                // if target endpoint URI starts with TCP schema - dont check policies, just return true
                return true;
            }

            try {
                PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(serviceName, portName);
                Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

                if (policy != null && policy.contains(TCPConstants.TCPTRANSPORT_POLICY_ASSERTION) &&
                        policy.contains(AUTO_OPTIMIZED_TRANSPORT_POLICY_ASSERTION)) {
                    /* if client set to choose optimal transport and server has TCP transport policy
                    then need to check server side policy "enabled" attribute*/
                    for (AssertionSet assertionSet : policy) {
                        for (PolicyAssertion assertion : assertionSet) {
                            if (assertion.getName().equals(TCPConstants.TCPTRANSPORT_POLICY_ASSERTION)) {
                                String value = assertion.getAttributeValue(new QName("enabled"));
                                if (value == null) {
                                    return false;
                                }
                                value = value.trim();

                                return (Boolean.valueOf(value) || value.equalsIgnoreCase("yes"));
                            }
                        }
                    }
                }

                return false;
            } catch (PolicyException e) {
                throw new WebServiceException(e);
            }
        }
    }

    public static class AddressingTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createWsaTube(context.getTubelineHead());
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createWsaTube(context.getTubelineHead());
        }
    }


    public static class MonitoringTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            return context.getTubelineHead();
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createMonitoringTube(context.getTubelineHead());
        }
    }

    public static class MustUnderstandTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createClientMUTube(context.getTubelineHead());
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createServerMUTube(context.getTubelineHead());
        }
    }

    public static class ValidationTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createValidationTube(context.getTubelineHead());
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createValidationTube(context.getTubelineHead());
        }
    }

    public static class HandlerTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createHandlerTube(context.getTubelineHead());
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createHandlerTube(context.getTubelineHead());
        }
    }

    public static class TerminalTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            return context.getTubelineHead();
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().getTerminalTube();
        }
    }
    private static final TubeAppender transportAppender = new TransportTubeAppender();
    private static final TubeAppender messageDumpingAppender = new MessageDumpingTubeAppender();
    private static final TubeAppender packetFilteringAppender = new PacketFilteringTubeAppender();
    private static final TubeAppender actionDumpAppender = new ActionDumpTubeAppender();
    private static final TubeAppender securityAppender = new SecurityTubeAppender();
    private static final TubeAppender makeConnectionAppender = new McTubeAppender();
    private static final TubeAppender reliableMessagingAppender = new RmTubeAppender();
    private static final TubeAppender transactionsAppender = new TxTubeAppender();
    private static final TubeAppender addressingAppender = new AddressingTubeAppender();
    private static final TubeAppender monitoringAppender = new MonitoringTubeAppender();
    private static final TubeAppender mustUnderstandAppender = new MustUnderstandTubeAppender();
    private static final TubeAppender validationAppender = new ValidationTubeAppender();
    private static final TubeAppender handlerAppender = new HandlerTubeAppender();
    private static final TubeAppender terminalAppender = new TerminalTubeAppender();
    private static final TubeAppender[] clientAppenders = new TubeAppender[]{
        transportAppender,
        messageDumpingAppender,
        packetFilteringAppender,
        new DumpTubeAppender(""),
        actionDumpAppender,
        new DumpTubeAppender(TRANSPORT_SUFFIX),
        new DumpTubeAppender(WSS_SUFFIX + AFTER_SUFFIX),
        securityAppender,
        new DumpTubeAppender(WSS_SUFFIX + BEFORE_SUFFIX),
        // TODO MEX pipe here
        new DumpTubeAppender(WSMC_SUFFIX + AFTER_SUFFIX),
        makeConnectionAppender,
        new DumpTubeAppender(WSMC_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(WSRM_SUFFIX + AFTER_SUFFIX),
        reliableMessagingAppender,
        new DumpTubeAppender(WSRM_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(WSTX_SUFFIX + AFTER_SUFFIX),
        transactionsAppender,
        new DumpTubeAppender(WSTX_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(WSA_SUFFIX + AFTER_SUFFIX),
        addressingAppender,
        new DumpTubeAppender(WSA_SUFFIX + BEFORE_SUFFIX),
        monitoringAppender,
        mustUnderstandAppender,
        validationAppender,
        handlerAppender/*,
    terminalAppender*/

    };
    private static final TubeAppender[] serverAppenders = new TubeAppender[]{
        terminalAppender,
        validationAppender,
        handlerAppender,
        mustUnderstandAppender,
        monitoringAppender,
        new DumpTubeAppender(WSTX_SUFFIX + AFTER_SUFFIX),
        transactionsAppender,
        new DumpTubeAppender(WSTX_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(WSRM_SUFFIX + AFTER_SUFFIX),
        reliableMessagingAppender,
        new DumpTubeAppender(WSRM_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(WSA_SUFFIX + AFTER_SUFFIX),
        addressingAppender,
        new DumpTubeAppender(WSA_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(WSMC_SUFFIX + AFTER_SUFFIX),
        makeConnectionAppender,
        new DumpTubeAppender(WSMC_SUFFIX + BEFORE_SUFFIX),
        // TODO MEX pipe here ?
        new DumpTubeAppender(WSS_SUFFIX + AFTER_SUFFIX),
        securityAppender,
        new DumpTubeAppender(WSS_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(TRANSPORT_SUFFIX),
        actionDumpAppender,
        new DumpTubeAppender(""),
        packetFilteringAppender,
        messageDumpingAppender /*,
    transportAppender*/

    };

    /**
     * Provides a ordered collection of WSIT/Metro client-side tube appenders that are be used to 
     * construct a client-side Metro tubeline in {@link TubelineAssemblerFactoryImpl}.
     * 
     * <b>
     * WARNING: This method is part of Metro internal API and may be changed, removed or 
     * replaced by a different method without a prior notice. The method SHOULD NOT be used 
     * outside of Metro codebase.
     * </b>
     * 
     * @return collection of WSIT/Metro client-side tube appenders
     * 
     * TODO: Make method package-private once a declarative tubeline assembly mechanism is introduced.
     */
    public Collection<TubeAppender> getClientSideAppenders() {
        return Arrays.asList(clientAppenders);
    }

    /**
     * Provides a ordered collection of WSIT/Metro server-side tube appenders that are be used to 
     * construct a server-side Metro tubeline in {@link TubelineAssemblerFactoryImpl}.
     * 
     * <b>
     * WARNING: This method is part of Metro internal API and may be changed, removed or 
     * replaced by a different method without a prior notice. The method SHOULD NOT be used 
     * outside of Metro codebase.
     * </b>
     * 
     * @return collection of WSIT/Metro client-side tube appenders
     * 
     * TODO: Make method package-private once a declarative tubeline assembly mechanism is introduced.
     */
    public Collection<TubeAppender> getServerSideAppenders() {
        return Arrays.asList(serverAppenders);
    }
}
