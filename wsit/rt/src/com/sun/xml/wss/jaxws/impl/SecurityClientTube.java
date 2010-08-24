/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.wss.jaxws.impl;

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.security.impl.kerberos.KerberosContext;
import com.sun.xml.ws.security.impl.policy.Constants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.security.secconv.client.SCTokenConfiguration;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.client.IssuedTokenManager;
import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;
import com.sun.xml.ws.api.security.CallbackHandlerFeature;
import com.sun.xml.ws.assembler.dev.ClientTubelineAssemblyContext;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyHolder;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.security.trust.STSIssuedTokenFeature;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.policy.Token;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.bind.JAXBElement;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.impl.policy.CertificateRetriever;
import com.sun.xml.ws.security.policy.IssuedToken;
import com.sun.xml.ws.security.policy.SecureConversationToken;

import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.security.secconv.impl.client.DefaultSCTokenConfiguration;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.impl.client.DefaultSTSIssuedTokenConfiguration;
import com.sun.xml.wss.XWSSConstants;
import com.sun.xml.wss.impl.PolicyResolver;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.ProcessingContextImpl;
import com.sun.xml.wss.impl.WssSoapFaultException;
import javax.xml.ws.soap.SOAPFaultException;
import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.misc.DefaultCallbackHandler;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import java.util.Properties;
import static com.sun.xml.wss.jaxws.impl.Constants.SC_ASSERTION;
import static com.sun.xml.wss.jaxws.impl.Constants.OPERATION_SCOPE;
import static com.sun.xml.wss.jaxws.impl.Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS;

import java.util.logging.Level;
import com.sun.xml.wss.jaxws.impl.logging.LogStringsMessages;
import com.sun.xml.wss.provider.wsit.PipeConstants;
import com.sun.xml.wss.provider.wsit.PolicyAlternativeHolder;
import com.sun.xml.wss.provider.wsit.PolicyResolverFactory;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.ListIterator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 *  @author Vbkumar.Jayanti@Sun.COM, shyam.rao@sun.com
 */
public class SecurityClientTube extends SecurityTubeBase implements SecureConversationInitiator {

    // Plugin instances for Trust and SecureConversation invocation
    //private static TrustPlugin trustPlugin = WSTrustFactory.newTrustPlugin(null);
    private IssuedTokenManager itm = IssuedTokenManager.getInstance();
    private Hashtable<String, String> scPolicyIDtoSctIdMap = new Hashtable<String, String>();
    //private WSSCPlugin  scPlugin;
    private Set trustConfig = null;
    private Set wsscConfig = null;
    private Set<PolicyAssertion> configAssertions = null;
    Properties props = new Properties();
    private ClientTubelineAssemblyContext wsitContext;

    // Creates a new instance of SecurityClientTube
    @SuppressWarnings("unchecked")
    public SecurityClientTube(ClientTubelineAssemblyContext wsitContext, Tube nextTube) {
        super(new ClientTubeConfiguration(wsitContext.getPolicyMap(), wsitContext.getWsdlPort(), wsitContext.getBinding()), nextTube);
        //scPlugin = new WSSCPlugin(null, wsscVer);
        try {
            for (PolicyAlternativeHolder p : this.policyAlternatives) {
                Iterator it = p.outMessagePolicyMap.values().iterator();
                while (it.hasNext()) {
                    SecurityPolicyHolder holder = (SecurityPolicyHolder) it.next();
                    if (configAssertions != null) {
                        configAssertions.addAll(holder.getConfigAssertions(SUN_WSS_SECURITY_CLIENT_POLICY_NS));
                    } else {
                        configAssertions = holder.getConfigAssertions(SUN_WSS_SECURITY_CLIENT_POLICY_NS);
                    }
                    if (trustConfig != null) {
                        trustConfig.addAll(holder.getConfigAssertions(Constants.SUN_TRUST_CLIENT_SECURITY_POLICY_NS));
                    } else {
                        trustConfig = holder.getConfigAssertions(Constants.SUN_TRUST_CLIENT_SECURITY_POLICY_NS);
                    }
                    if (wsscConfig != null) {
                        wsscConfig.addAll(holder.getConfigAssertions(Constants.SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS));
                    } else {
                        wsscConfig = holder.getConfigAssertions(Constants.SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS);
                    }
                }
            }
            this.wsitContext = wsitContext;
            //props.put(PipeConstants.POLICY, wsitContext.getPolicyMap());
            //props.put(PipeConstants.WSDL_MODEL, wsitContext.getWrappedContext().getWsdlModel());

            CallbackHandler handler = configureClientHandler(configAssertions, props);
            secEnv = new DefaultSecurityEnvironmentImpl(handler, props);
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0023_ERROR_CREATING_NEW_INSTANCE_SEC_CLIENT_TUBE(), e);
            throw new RuntimeException(
                    LogStringsMessages.WSSTUBE_0023_ERROR_CREATING_NEW_INSTANCE_SEC_CLIENT_TUBE(), e);
        }
    }

    // copy constructor
    protected SecurityClientTube(SecurityClientTube that, TubeCloner cloner) {
        super(that, cloner);
        trustConfig = that.trustConfig;
        wsscConfig = that.wsscConfig;
        scPolicyIDtoSctIdMap = that.scPolicyIDtoSctIdMap;
    //scPlugin = that.scPlugin;
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new SecurityClientTube(this, cloner);
    }

    @Override
    public NextAction processRequest(Packet packet) {
        //computing EPR related stuff
        //get certificate from EPR or from XWSSConstants.SERVER_CERTIFICATE_PROPERTY
        if (wsitContext != null) {
            WSBindingProvider bpr = (WSBindingProvider) wsitContext.getWrappedContext().getBindingProvider();
            WSEndpointReference epr = bpr.getWSEndpointReference();
            X509Certificate x509Cert = (X509Certificate) bpr.getRequestContext().get(XWSSConstants.SERVER_CERTIFICATE_PROPERTY);
            if (x509Cert == null) {
                if (epr != null) {
                    WSEndpointReference.EPRExtension idExtn = null;
                    XMLStreamReader xmlReader = null;
                    try {
                        QName ID_QNAME = new QName("http://schemas.xmlsoap.org/ws/2006/02/addressingidentity", "Identity");
                        idExtn = epr.getEPRExtension(ID_QNAME);
                        if (idExtn != null) {
                            xmlReader = idExtn.readAsXMLStreamReader();
                            CertificateRetriever cr = new CertificateRetriever();
                            //byte[] bstValue = cr.digestBST(xmlReader);
                            byte[] bstValue = cr.getBSTFromIdentityExtension(xmlReader);
                            X509Certificate certificate = null;
                            if (bstValue != null) {
                                certificate = cr.constructCertificate(bstValue);
                            }
                            if (certificate != null) {
                                props.put(PipeConstants.SERVER_CERT, certificate);
                                this.serverCert = certificate;
                            }
                        }
                    } catch (XMLStreamException ex) {
                        log.log(Level.WARNING, ex.getMessage());
                    //throw new RuntimeException(ex);
                    }
                }
            } else {
                //log.log(Level.INFO, "certificate is found by SERVER_CERTIFICATE_PROPERTY,so using it");
                props.put(PipeConstants.SERVER_CERT, x509Cert);
                this.serverCert = x509Cert;
            }
        }//done
        try {
            packet = processClientRequestPacket(packet);
        } catch (Throwable t) {
            if (!(t instanceof WebServiceException)) {
                t = new WebServiceException(t);
            }
            return doThrow(t);
        }
        return doInvoke(super.next, packet);
    }

    public Packet processClientRequestPacket(Packet packet) {

        // Add Action header to trust message
        if ("true".equals(packet.invocationProperties.get(WSTrustConstants.IS_TRUST_MESSAGE))) {
            String action = (String) packet.invocationProperties.get(WSTrustConstants.TRUST_ACTION);
            HeaderList headers = packet.getMessage().getHeaders();
            headers.fillRequestAddressingHeaders(packet, addVer, soapVersion, false, action);
        }

        // keep the message
        Message msg = packet.getMessage();

        boolean isSCMessage = isSCMessage(packet);

        if (!isSCMessage && !isSCCancel(packet)) {
            // this is an application message
            // initialize any secure-conversation sessions for this message
            invokeSCPlugin(packet);
        }

        // invoke the Trust Plugin if necessary
        invokeTrustPlugin(packet, isSCMessage);

        //---------------OUTBOUND SECURITY PROCESSING----------
        ProcessingContext ctx = initializeOutgoingProcessingContext(packet, isSCMessage);
        ((ProcessingContextImpl) ctx).setIssuedTokenContextMap(issuedTokenContextMap);
        ((ProcessingContextImpl) ctx).setSCPolicyIDtoSctIdMap(scPolicyIDtoSctIdMap);
        ctx.isClient(true);
        try {
            if (hasKerberosTokenPolicy()) {
                populateKerberosContext(packet, (ProcessingContextImpl) ctx, isSCMessage);
            }
        } catch (XWSSecurityException xwsse) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0024_ERROR_SECURING_OUTBOUND_MSG(), xwsse);
            throw new WebServiceException(
                    LogStringsMessages.WSSTUBE_0024_ERROR_SECURING_OUTBOUND_MSG(), xwsse);
        }

        if (isSCRenew(packet)) {
            // To append the policy for using with renew message;
            // Need to refactor to make it logical more clean
            SCTokenConfiguration config = new DefaultSCTokenConfiguration(wsscVer.getNamespaceURI());
            config.getOtherOptions().put("MessagePolicy", (MessagePolicy) ctx.getSecurityPolicy());
            IssuedTokenContext itc = itm.createIssuedTokenContext(config, packet.endpointAddress.toString());
            try {
                itm.renewIssuedToken(itc);
            } catch (WSTrustException se) {
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
                throw new WebServiceException(LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
            }
        }

        try {
            if (!optimized) {
                if (!isSCMessage) {
                    cacheOperation(msg);
                }
                SOAPMessage soapMessage = msg.readAsSOAPMessage();
                soapMessage = secureOutboundMessage(soapMessage, ctx);
                msg = Messages.create(soapMessage);
            } else {
                msg = secureOutboundMessage(msg, ctx);
            }
        } catch (WssSoapFaultException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0024_ERROR_SECURING_OUTBOUND_MSG(), ex);
            throw new WebServiceException(
                    LogStringsMessages.WSSTUBE_0024_ERROR_SECURING_OUTBOUND_MSG(), getSOAPFaultException(ex));
        } catch (SOAPException se) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0024_ERROR_SECURING_OUTBOUND_MSG(), se);
            throw new WebServiceException(
                    LogStringsMessages.WSSTUBE_0024_ERROR_SECURING_OUTBOUND_MSG(), se);
        }
        packet.setMessage(msg);

        if (isSCRenew(packet)) {
            // To remove the appended policy for using with renew message;
            // Need to refactor to make it logical more clean
            Token scToken = (Token) packet.invocationProperties.get(SC_ASSERTION);
            SCTokenConfiguration config = new DefaultSCTokenConfiguration(wsscVer.getNamespaceURI(), false);
            config.getOtherOptions().put("MessagePolicy", getOutgoingXWSBootstrapPolicy(scToken));
            IssuedTokenContext itc = itm.createIssuedTokenContext(config, packet.endpointAddress.toString());
            try {
                itm.renewIssuedToken(itc);
            } catch (WSTrustException se) {
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
                throw new WebServiceException(LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
            }
        }

        return packet;
    }

    @Override
    public NextAction processResponse(Packet ret) {
        try {
            ret = processClientResponsePacket(ret);
        } catch (Throwable t) {
            if (!(t instanceof WebServiceException)) {
                t = new WebServiceException(t);
            }
            return doThrow(t);
        }
        return doReturnWith(ret);
    }

    public Packet processClientResponsePacket(Packet ret) {
        boolean isTrustMsg = false;
        if ("true".equals(ret.invocationProperties.get(WSTrustConstants.IS_TRUST_MESSAGE))) {
            isTrustMsg = true;
        }

        // Could be OneWay
        if (ret.getMessage() == null) {
            return ret;
        }

        // Not required, commenting
        /* TODO:this piece of code present since payload should be read once*/
//        if (!optimized) {
//            try {
//                SOAPMessage sm = ret.getMessage().readAsSOAPMessage();
//                Message newMsg = Messages.create(sm);
//                ret.setMessage(newMsg);
//            } catch (SOAPException ex) {
//                log.log(Level.SEVERE,
//                        LogStringsMessages.WSSTUBE_0005_PROBLEM_PROC_SOAP_MESSAGE(), ex);
//                throw new WebServiceException(
//                        LogStringsMessages.WSSTUBE_0005_PROBLEM_PROC_SOAP_MESSAGE(), ex);
//            }
//        }
        //---------------INBOUND SECURITY VERIFICATION----------


        ProcessingContext ctx = initializeInboundProcessingContext(ret);
        ctx.isClient(true);

        ((ProcessingContextImpl) ctx).setIssuedTokenContextMap(issuedTokenContextMap);
        ((ProcessingContextImpl) ctx).setSCPolicyIDtoSctIdMap(scPolicyIDtoSctIdMap);
        PolicyResolver pr = PolicyResolverFactory.createPolicyResolver(this.policyAlternatives, cachedOperation, tubeConfig, addVer, true, rmVer, mcVer);
        ctx.setExtraneousProperty(ProcessingContext.OPERATION_RESOLVER, pr);
        Message msg = null;
        try {
            msg = ret.getMessage();
            // Could be OneWay
            if (msg == null) {
                return ret;
            }

            if (!optimized) {
                SOAPMessage soapMessage = msg.readAsSOAPMessage();
                soapMessage = verifyInboundMessage(soapMessage, ctx);
                if (msg.isFault()) {
                    if (debug) {
                        DumpFilter.process(ctx);
                    }
                    SOAPFault fault = soapMessage.getSOAPBody().getFault();
                    if ((new QName(wsscVer.getNamespaceURI(), "RenewNeeded")).equals(fault.getFaultCodeAsQName())) {
                        renewSCT(ctx, ret);
                    }
                    //log.log(Level.SEVERE,
                    //        LogStringsMessages.WSSTUBE_0034_FAULTY_RESPONSE_MSG(fault));
                    throw new SOAPFaultException(fault);
                }
                msg = Messages.create(soapMessage);
            } else {
                msg = verifyInboundMessage(msg, ctx);
            }
        } catch (XWSSecurityException xwse) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0025_ERROR_VERIFY_INBOUND_MSG(), xwse);
            throw new WebServiceException(LogStringsMessages.WSSTUBE_0025_ERROR_VERIFY_INBOUND_MSG(),
                    getSOAPFaultException(xwse));
        } catch (SOAPException se) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0025_ERROR_VERIFY_INBOUND_MSG(), se);
            throw new WebServiceException(LogStringsMessages.WSSTUBE_0025_ERROR_VERIFY_INBOUND_MSG(), se);
        }
        resetCachedOperation();
        ret.setMessage(msg);

        if (isTrustMsg) {
            //String action = getAction(ret);
            getAction(ret);
        }

        return ret;
    }

    @Override
    public NextAction processException(Throwable t) {
        if (!(t instanceof WebServiceException)) {
            t = new WebServiceException(t);
        }
        return doThrow(t);
    }

    private void invokeSCPlugin(Packet packet) {

        // get the secure conversation policies pertaining to this operation
        List<PolicyAssertion> policies = getOutBoundSCP(packet.getMessage());

        PolicyAssertion scClientAssertion = null;
        if (wsscConfig != null) {
            Iterator it = wsscConfig.iterator();
            while (it != null && it.hasNext()) {
                scClientAssertion = (PolicyAssertion) it.next();
            }
        }

        for (PolicyAssertion scAssertion : policies) {
            Token scToken = (Token) scAssertion;
            if (issuedTokenContextMap.get(scToken.getTokenId()) == null) {
                try {
                    SCTokenConfiguration config = new DefaultSCTokenConfiguration(wsscVer.getNamespaceURI(),
                            (SecureConversationToken) scToken, tubeConfig.getWSDLPort(), tubeConfig.getBinding(),
                            this, packet, addVer, scClientAssertion, super.next);
                    IssuedTokenContext ctx = itm.createIssuedTokenContext(config, packet.endpointAddress.toString());
                    itm.getIssuedToken(ctx);
                    issuedTokenContextMap.put(
                            scToken.getTokenId(), ctx);
                    //PolicyID to sctID map
                    SCTokenConfiguration sctConfig = (SCTokenConfiguration) ctx.getSecurityPolicy().get(0);
                    scPolicyIDtoSctIdMap.put(scToken.getTokenId(), sctConfig.getTokenId());
                } catch (WSTrustException se) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
                    throw new WebServiceException(LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
                }
            }
        }
    }

    // returns a list of IssuedTokenPolicy Assertions contained in the
    // service policy
    protected List<PolicyAssertion> getIssuedTokenPolicies(Packet packet, String scope) {
        List<PolicyAssertion> ret = new ArrayList<PolicyAssertion>();
        for (PolicyAlternativeHolder p : this.policyAlternatives) {

            WSDLBoundOperation operation = null;
            if (isTrustMessage(packet)) {
                operation = getWSDLOpFromAction(packet, false);
            } else {
                operation = getOperation(packet.getMessage());
            }

            SecurityPolicyHolder sph = (SecurityPolicyHolder) p.outMessagePolicyMap.get(operation);
            if (sph != null) {
                ret.addAll(sph.getIssuedTokens());
            }
        }
        return ret;
    }

    public JAXBElement startSecureConversation(Packet packet)
            throws WSSecureConversationException {

        List toks = getOutBoundSCP(packet.getMessage());
        if (toks.isEmpty()) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                        LogStringsMessages.WSSTUBE_0026_NO_POLICY_FOUND_FOR_SC());
            }
            //throw new WSSecureConversationException(LogStringsMessages.WSSTUBE_0026_NO_POLICY_FOUND_FOR_SC());
            return null;
        }
        //Note: Assuming only one SC assertion
        Token tok = (Token) toks.get(0);
        IssuedTokenContext ctx =
                issuedTokenContextMap.get(tok.getTokenId());

        PolicyAssertion scClientAssertion = null;
        if (wsscConfig != null) {
            Iterator it = wsscConfig.iterator();
            while (it != null && it.hasNext()) {
                scClientAssertion = (PolicyAssertion) it.next();
            }
        }

        if (ctx == null) {
            try {
                SCTokenConfiguration config = new DefaultSCTokenConfiguration(wsscVer.getNamespaceURI(),
                        (SecureConversationToken) tok, tubeConfig.getWSDLPort(), tubeConfig.getBinding(),
                        this, packet, addVer, scClientAssertion, super.next);

                ctx = itm.createIssuedTokenContext(config, packet.endpointAddress.toString());
                itm.getIssuedToken(ctx);
                issuedTokenContextMap.put(tok.getTokenId(), ctx);
                //PolicyID to sctID map
                SCTokenConfiguration sctConfig = (SCTokenConfiguration) ctx.getSecurityPolicy().get(0);
                scPolicyIDtoSctIdMap.put(tok.getTokenId(), sctConfig.getTokenId());
            } catch (WSTrustException se) {
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
                throw new WebServiceException(LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
            }
        }

        SecurityTokenReference str = (SecurityTokenReference) ctx.getUnAttachedSecurityTokenReference();

        return WSTrustElementFactory.newInstance().toJAXBElement(str);
    }

    private void cancelSecurityContextToken() {
        Enumeration keys = issuedTokenContextMap.keys();
        while (keys.hasMoreElements()) {
            String id = (String) keys.nextElement();
            IssuedTokenContext ctx =
                    issuedTokenContextMap.get(id);

            if (ctx.getSecurityToken() instanceof SecurityContextToken) {
                try {
                    itm.cancelIssuedToken(ctx);
                    issuedTokenContextMap.remove(id);
                    scPolicyIDtoSctIdMap.remove(id);
                } catch (WSTrustException se) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
                    throw new WebServiceException(LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
                }
            }
        }
    }

    @Override
    public void preDestroy() {
        cancelSecurityContextToken();
        if (super.next != null) {
            super.next.preDestroy();
        }
    //issuedTokenContextMap.clear();
    //scPolicyIDtoSctIdMap.clear();
    }

    private void invokeTrustPlugin(Packet packet, boolean isSCMessage) {
        List<PolicyAssertion> policies = null;

        // Get IssuedToken policies from the service
        if (isSCMessage) {
            Token scToken = (Token) packet.invocationProperties.get(SC_ASSERTION);
            policies = getIssuedTokenPoliciesFromBootstrapPolicy(scToken);
        } else {
            policies = getIssuedTokenPolicies(packet, OPERATION_SCOPE);
        }

        // Get PreConfiguredSTS policy on the client side
        PolicyAssertion preSetSTSAssertion = null;
        if (trustConfig != null) {
            Iterator it = trustConfig.iterator();
            while (it != null && it.hasNext()) {
                preSetSTSAssertion = (PolicyAssertion) it.next();
            }
        }

        // Get issued tokens policy assertions
        for (PolicyAssertion issuedTokenAssertion : policies) {
            // Get run time STSIssuedTokenConfiguration
            //from STSIssuedTokenFeature
            STSIssuedTokenConfiguration rtConfig = null;
            STSIssuedTokenFeature stsFeature = tubeConfig.getBinding().getFeature(STSIssuedTokenFeature.class);
            if (stsFeature != null) {
                rtConfig = stsFeature.getSTSIssuedTokenConfiguration();
            }

            // Create the configuration to use
            STSIssuedTokenConfiguration config = null;
            if (issuedTokenContextMap.get(((Token) issuedTokenAssertion).getTokenId()) == null || rtConfig != null) {
                try {
                    // Get STS information from the request message context
                    String stsEndpoint = (String) packet.invocationProperties.get(STSIssuedTokenConfiguration.STS_ENDPOINT);
                    if (stsEndpoint != null) {
                        String stsMEXAddress = (String) packet.invocationProperties.get(STSIssuedTokenConfiguration.STS_MEX_ADDRESS);
                        if (stsMEXAddress == null) {
                            String stsNamespace = (String) packet.invocationProperties.get(STSIssuedTokenConfiguration.STS_NAMESPACE);
                            String stsWSDLLocation = (String) packet.invocationProperties.get(STSIssuedTokenConfiguration.STS_WSDL_LOCATION);
                            String stsServiceName = (String) packet.invocationProperties.get(STSIssuedTokenConfiguration.STS_SERVICE_NAME);
                            String stsPortName = (String) packet.invocationProperties.get(STSIssuedTokenConfiguration.STS_PORT_NAME);
                            config = new DefaultSTSIssuedTokenConfiguration(wsTrustVer.getNamespaceURI(), stsEndpoint, stsWSDLLocation, stsServiceName, stsPortName, stsNamespace);
                        } else {
                            config = new DefaultSTSIssuedTokenConfiguration(wsTrustVer.getNamespaceURI(), stsEndpoint, stsMEXAddress);
                        }
                    }

                    // Create config from IssuedToken and PreConfiguredSTS
                    if (config == null) {
                        config = new DefaultSTSIssuedTokenConfiguration(wsTrustVer.getNamespaceURI(), (IssuedToken) issuedTokenAssertion, preSetSTSAssertion);
                    }

                    config.getOtherOptions().putAll(packet.invocationProperties);

                    // put the server certificate, if available, in the configuration
                    X509Certificate x509ServerCertificate = (X509Certificate) props.get(PipeConstants.SERVER_CERT);
                    // and make sure the  validition of the server certificate happens only once
                    if (x509ServerCertificate != null) {
                        if (isCertValidityVerified == false) {
                            CertificateRetriever cr = new CertificateRetriever();
                            isCertValid = cr.setServerCertInTheSTSConfig(config, secEnv, x509ServerCertificate);
                            cr = null;
                            isCertValidityVerified = true;
                        } else {
                            if (isCertValid == true) {
                                config.getOtherOptions().put("Identity", x509ServerCertificate);
                            }
                        }
                    }

                    // get entries from run time configuration
                    if (rtConfig != null) {
                        rtConfig.getOtherOptions().put(STSIssuedTokenConfiguration.ISSUED_TOKEN, config);
                        rtConfig.getOtherOptions().put(STSIssuedTokenConfiguration.APPLIES_TO, packet.endpointAddress.toString());
                        ((DefaultSTSIssuedTokenConfiguration) config).copy(rtConfig);

                        config.getOtherOptions().put("RunTimeConfig", rtConfig);
                    }

                    // Obtain issued token from STS
                    IssuedTokenContext ctx = itm.createIssuedTokenContext(config, packet.endpointAddress.toString());
                    itm.getIssuedToken(ctx);
                    issuedTokenContextMap.put(
                            ((Token) issuedTokenAssertion).getTokenId(), ctx);

                    updateMPForIssuedTokenAsEncryptedSupportingToken(packet, ctx, ((Token) issuedTokenAssertion).getTokenId());

                } catch (WSTrustException se) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
                    throw new WebServiceException(LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
                }
            }
        }
    }

    protected SecurityPolicyHolder addOutgoingMP(WSDLBoundOperation operation, Policy policy, PolicyAlternativeHolder ph) throws PolicyException {
        SecurityPolicyHolder sph = constructPolicyHolder(policy, false, false);
        ph.outMessagePolicyMap.put(operation, sph);
        return sph;
    }

    protected SecurityPolicyHolder addIncomingMP(WSDLBoundOperation operation, Policy policy, PolicyAlternativeHolder ph) throws PolicyException {
        SecurityPolicyHolder sph = constructPolicyHolder(policy, false, true);
        ph.inMessagePolicyMap.put(operation, sph);
        return sph;
    }

    protected void addIncomingProtocolPolicy(Policy effectivePolicy, String protocol, PolicyAlternativeHolder ph) throws PolicyException {
        ph.inProtocolPM.put(protocol, constructPolicyHolder(effectivePolicy, false, true, true));
    }

    protected void addOutgoingProtocolPolicy(Policy effectivePolicy, String protocol, PolicyAlternativeHolder ph) throws PolicyException {
        ph.outProtocolPM.put(protocol, constructPolicyHolder(effectivePolicy, false, false, false));
    }

    protected void addIncomingFaultPolicy(Policy effectivePolicy, SecurityPolicyHolder sph, WSDLFault fault) throws PolicyException {
        SecurityPolicyHolder faultPH = constructPolicyHolder(effectivePolicy, false, true);
        sph.addFaultPolicy(fault, faultPH);
    }

    protected void addOutgoingFaultPolicy(Policy effectivePolicy, SecurityPolicyHolder sph, WSDLFault fault) throws PolicyException {
        SecurityPolicyHolder faultPH = constructPolicyHolder(effectivePolicy, false, false);
        sph.addFaultPolicy(fault, faultPH);
    }

    protected String getAction(WSDLOperation operation, boolean inComming) {
        if (!inComming) {
            return operation.getInput().getAction();
        } else {
            return operation.getOutput().getAction();
        }
    }

    protected void populateKerberosContext(Packet packet, ProcessingContextImpl ctx, boolean isSCMessage) throws XWSSecurityException {
        List toks = getOutBoundKTP(packet, isSCMessage);
        if (toks.isEmpty()) {
            return;
        }
        //Note: Assuming only one Kerberos token assertion         
        KerberosContext krbContext = ctx.getSecurityEnvironment().doKerberosLogin();

        try {
            byte[] krbSha1 = MessageDigest.getInstance("SHA-1").digest(krbContext.getKerberosToken());
            String encKrbSha1 = Base64.encode(krbSha1);
            ctx.setExtraneousProperty(MessageConstants.KERBEROS_SHA1_VALUE, encKrbSha1);
            ctx.setKerberosContext(krbContext);
        } catch (NoSuchAlgorithmException nsae) {
            throw new XWSSecurityException(nsae);
        }
    }

    private void updateMPForIssuedTokenAsEncryptedSupportingToken(Packet packet, final IssuedTokenContext ctx, final String issuedTokenPolicyId) {
        /*
         * If IssuedToken is present as SignedSupprotingToken in the wsdl, then the
         * primary signature must have IssuedToken's id for the signature target instead
         * of policyId of issuedTokenAssertion
         */
        Message message = packet.getMessage();
        for (PolicyAlternativeHolder p : this.policyAlternatives) {
            WSDLBoundOperation operation = message.getOperation(tubeConfig.getWSDLPort());
            SecurityPolicyHolder sph = (SecurityPolicyHolder) p.outMessagePolicyMap.get(operation);
            if (sph != null && sph.isIssuedTokenAsEncryptedSupportingToken()) {
                MessagePolicy policy = sph.getMessagePolicy();
                ArrayList list = policy.getPrimaryPolicies();
                Iterator i = list.iterator();
                boolean breakOuterLoop = false;
                while (i.hasNext()) {
                    SecurityPolicy primaryPolicy = (SecurityPolicy) i.next();
                    if (PolicyTypeUtil.encryptionPolicy(primaryPolicy)) {
                        EncryptionPolicy encPolicy = (EncryptionPolicy) primaryPolicy;
                        EncryptionPolicy.FeatureBinding featureBinding = (EncryptionPolicy.FeatureBinding) encPolicy.getFeatureBinding();
                        ArrayList targetList = featureBinding.getTargetBindings();
                        ListIterator iterator = targetList.listIterator();
                        while (iterator.hasNext()) {
                            EncryptionTarget encryptionTarget = (EncryptionTarget) iterator.next();
                            String targetURI = encryptionTarget.getValue();
                            if (targetURI.equals(issuedTokenPolicyId)) {
                                if (ctx != null) {
                                    GenericToken issuedToken = (GenericToken) ctx.getSecurityToken();
                                    encryptionTarget.setValue(issuedToken.getId());
                                    sph.setMessagePolicy(policy);
                                    p.outMessagePolicyMap.put(operation, sph);
                                    breakOuterLoop = true;
                                    break;
                                }
                            }
                        }
                        if (breakOuterLoop) {
                            break;
                        }
                    }
                }
            }
        }
    }
    //TODO use constants here

    private CallbackHandler configureClientHandler(Set<PolicyAssertion> configAssertions, Properties props) {
        CallbackHandlerFeature chf = tubeConfig.getBinding().getFeature(CallbackHandlerFeature.class);
        if (chf != null) {
            return chf.getHandler();
        }

        //Properties props = new Properties();
        String ret = populateConfigProperties(configAssertions, props);
        try {
            if (ret != null) {
                Class handler = loadClass(ret);
                Object obj = handler.newInstance();
                if (!(obj instanceof CallbackHandler)) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSSTUBE_0033_INVALID_CALLBACK_HANDLER_CLASS(ret));
                    throw new RuntimeException(
                            LogStringsMessages.WSSTUBE_0033_INVALID_CALLBACK_HANDLER_CLASS(ret));
                }
                return (CallbackHandler) obj;
            }
            return new DefaultCallbackHandler("client", props);
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0027_ERROR_CONFIGURE_CLIENT_HANDLER(), e);
            throw new RuntimeException(LogStringsMessages.WSSTUBE_0027_ERROR_CONFIGURE_CLIENT_HANDLER(), e);
        }
    }

    private void renewSCT(ProcessingContext ctx, Packet ret) {
        SCTokenConfiguration config = new DefaultSCTokenConfiguration(wsscVer.getNamespaceURI());
        config.getOtherOptions().put("MessagePolicy", (MessagePolicy) ctx.getSecurityPolicy());
        IssuedTokenContext itc = itm.createIssuedTokenContext(config, ret.endpointAddress.toString());
        try {
            itm.renewIssuedToken(itc);
        } catch (WSTrustException se) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
            throw new WebServiceException(LogStringsMessages.WSSTUBE_0035_ERROR_ISSUEDTOKEN_CREATION(), se);
        }
    }
}
