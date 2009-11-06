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

/*
 * WSITClientAuthContext.java
 *
 * Created on November 1, 2006, 11:59 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.security.secconv.client.SCTokenConfiguration;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.client.IssuedTokenManager;
import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.message.stream.LazyStreamBasedMessage;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.impl.kerberos.KerberosContext;
import com.sun.xml.ws.security.impl.policy.CertificateRetriever;
import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyHolder;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.IssuedToken;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.security.secconv.impl.client.DefaultSCTokenConfiguration;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.STSIssuedTokenFeature;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.trust.impl.client.DefaultSTSIssuedTokenConfiguration;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.NewSecurityRecipient;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.ProcessingContextImpl;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.misc.DefaultCallbackHandler;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.misc.WSITProviderSecurityEnvironment;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.jaxws.impl.Constants;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ClientAuthContext;
import javax.xml.bind.JAXBElement;

import static com.sun.xml.wss.jaxws.impl.Constants.SC_ASSERTION;
import static com.sun.xml.wss.jaxws.impl.Constants.OPERATION_SCOPE;
import com.sun.xml.wss.jaxws.impl.PolicyResolverImpl;
import com.sun.xml.wss.provider.wsit.logging.LogStringsMessages;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import java.util.logging.Level;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.ListIterator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author kumar jayanti
 */
public class WSITClientAuthContext extends WSITAuthContextBase
        implements ClientAuthContext {

    //*****************STATIC****************
    // Plugin instances for Trust and SecureConversation invocation
//    private static TrustPlugin trustPlugin = WSTrustFactory.newTrustPlugin(null);
//    private static NewWSSCPlugin  scPlugin = WSSCFactory.newNewSCPlugin(null, wsscVer);
    
      private IssuedTokenManager itm;
      //private static TrustPlugin trustPlugin;
      //private static NewWSSCPlugin  scPlugin;
      //private static WSSCPlugin  scPlugin;
    
    //******************INSTANCE VARIABLES*******
    // do not use this operation it will be null
    //String operation = null;
    //Subject subject = null;
    //Map map = null;
    private Set trustConfig = null;
    private Set wsscConfig = null;
    private CallbackHandler handler = null;
    //***************AuthModule Instance**********
    WSITClientAuthModule authModule = null;  
    private Hashtable<String, String> scPolicyIDtoSctIdMap = new Hashtable<String, String>();
    protected WeakReference<WSITClientAuthConfig> authConfig;
    protected WeakReference<Object> tubeOrPipe;


    
    /** Creates a new instance of WSITClientAuthContext */
    @SuppressWarnings("unchecked")
    public WSITClientAuthContext(String operation, Subject subject, Map<Object, Object> map, CallbackHandler callbackHandler) {
        super(map);
        this.authConfig = new WeakReference<WSITClientAuthConfig>((WSITClientAuthConfig) map.get(PipeConstants.AUTH_CONFIG));
        this.tubeOrPipe = new WeakReference<Object>(map.get(PipeConstants.SECURITY_PIPE));
        WSDLPortImpl wpi = (WSDLPortImpl) map.get("WSDL_MODEL");
        ClientTubeAssemblerContext context = (ClientTubeAssemblerContext) map.get(PipeConstants.WRAPPED_CONTEXT);
        //this.serverCert = (X509Certificate) map.get(PipeConstants.SERVER_CERT);

        //this.operation = operation;
        //this.subject = subject;
        //this.map = map;
        //trustPlugin = WSTrustFactory.newTrustPlugin(null);
        itm = IssuedTokenManager.getInstance();
        //scPlugin = WSSCFactory.newNewSCPlugin(null, wsscVer);
        //scPlugin = WSSCFactory.newSCPlugin(null, wsscVer);
        Set configAssertions = null;
        Iterator it = outMessagePolicyMap.values().iterator();
        while (it.hasNext()) {
            SecurityPolicyHolder holder = (SecurityPolicyHolder) it.next();
            if (configAssertions != null) {
                configAssertions.addAll(holder.getConfigAssertions(Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS));
            } else {
                configAssertions = holder.getConfigAssertions(Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS);
            }
            if (trustConfig != null) {
                trustConfig.addAll(holder.getConfigAssertions(com.sun.xml.ws.security.impl.policy.Constants.SUN_TRUST_CLIENT_SECURITY_POLICY_NS));
            } else {
                trustConfig = holder.getConfigAssertions(
                        com.sun.xml.ws.security.impl.policy.Constants.SUN_TRUST_CLIENT_SECURITY_POLICY_NS);
            }
            if (wsscConfig != null) {
                wsscConfig.addAll(holder.getConfigAssertions(com.sun.xml.ws.security.impl.policy.Constants.SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS));
            } else {
                wsscConfig = holder.getConfigAssertions(
                        com.sun.xml.ws.security.impl.policy.Constants.SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS);
            }
        }

        Properties props = new Properties();
        if (callbackHandler != null) {
            populateConfigProperties(configAssertions, props);
            try {
                String jmacHandler = props.getProperty(DefaultCallbackHandler.JMAC_CALLBACK_HANDLER);
                if (jmacHandler != null) {
                    handler = loadGFHandler(true, jmacHandler);
                } else {
                    handler = callbackHandler;
                }
                secEnv = new WSITProviderSecurityEnvironment(handler, map, props);
            } catch (XWSSecurityException ex) {
                log.log(Level.SEVERE,
                        LogStringsMessages.WSITPVD_0027_ERROR_POPULATING_CLIENT_CONFIG_PROP(), ex);
                throw new WebServiceException(
                        LogStringsMessages.WSITPVD_0027_ERROR_POPULATING_CLIENT_CONFIG_PROP(), ex);
            }
        } else {
            handler = configureClientHandler(configAssertions, props);
            String jmacHandler = props.getProperty(DefaultCallbackHandler.JMAC_CALLBACK_HANDLER);
            if (jmacHandler != null) {
                try {
                    handler = loadGFHandler(true, jmacHandler);
                    secEnv = new WSITProviderSecurityEnvironment(handler, map, props);
                } catch (XWSSecurityException ex) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSITPVD_0027_ERROR_POPULATING_CLIENT_CONFIG_PROP(), ex);
                    throw new WebServiceException(
                            LogStringsMessages.WSITPVD_0027_ERROR_POPULATING_CLIENT_CONFIG_PROP(), ex);
                }
            } else {
                secEnv = new DefaultSecurityEnvironmentImpl(handler, props);
            }
        }
        //computing EPR related stuff
        //get certificate from EPR or from XWSSConstants.SERVER_CERTIFICATE_PROPERTY
        X509Certificate cert = getCertificateFromEPR(context,wpi);
        if (cert != null) {
            props.put(PipeConstants.SERVER_CERT, cert);
            this.serverCert = cert;
        }
        //initialize the AuthModules and keep references to them
        authModule = new WSITClientAuthModule();
        try {
            authModule.initialize(null, null, null, map);
        } catch (AuthException e) {
            log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0028_ERROR_INIT_AUTH_MODULE(), e);
            throw new RuntimeException(LogStringsMessages.WSITPVD_0028_ERROR_INIT_AUTH_MODULE(), e);
        }
    }

    public AuthStatus secureRequest(MessageInfo messageInfo, Subject clientSubject) throws AuthException {

        try {

            Packet packet = getRequestPacket(messageInfo);
            // Add Action header to trust message
            boolean isTrustMsg = false;
            if ("true".equals(packet.invocationProperties.get(WSTrustConstants.IS_TRUST_MESSAGE))) {
                isTrustMsg = true;
                String action = (String) packet.invocationProperties.get(WSTrustConstants.TRUST_ACTION);
                HeaderList headers = packet.getMessage().getHeaders();
                headers.fillRequestAddressingHeaders(packet, addVer, soapVersion, false, action);
            }

            //set the isTrustProperty into MessageInfo
            @SuppressWarnings("unchecked")
            Map<Object, Object> msgInfoMap = messageInfo.getMap();
            msgInfoMap.put("IS_TRUST_MSG", Boolean.valueOf(isTrustMsg));

            // keep the message
            //Message msg = packet.getMessage();

            //invoke the SCPlugin here
            invokeSCPlugin(packet);

            //secure the outbound request here
            Packet ret = secureRequest(packet, clientSubject, false);

            //put the modified packet back
            setRequestPacket(messageInfo, ret);

        } catch (XWSSecurityException e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0050_ERROR_SECURE_REQUEST(), e);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0050_ERROR_SECURE_REQUEST(),
                    getSOAPFaultException(e));
        }

        return AuthStatus.SEND_SUCCESS;
    }

    public Packet secureRequest(
            Packet packet, Subject clientSubject, boolean isSCMessage) throws XWSSecurityException {
        // invoke the Trust Plugin if necessary
        Message msg = packet.getMessage();
        invokeTrustPlugin(packet, isSCMessage);

        ProcessingContext ctx = initializeOutgoingProcessingContext(packet, isSCMessage);
        ((ProcessingContextImpl)ctx).setIssuedTokenContextMap(issuedTokenContextMap);
        ((ProcessingContextImpl)ctx).setSCPolicyIDtoSctIdMap(scPolicyIDtoSctIdMap);
        ctx.isClient(true);
        if(hasKerberosTokenPolicy()){
            populateKerberosContext(packet, (ProcessingContextImpl)ctx, isSCMessage);
        }
        if(isSCRenew(packet)){
            SCTokenConfiguration config = new DefaultSCTokenConfiguration(wsscVer.getNamespaceURI());
            config.getOtherOptions().put("MessagePolicy", (MessagePolicy) ctx.getSecurityPolicy());
            IssuedTokenContext itc =itm.createIssuedTokenContext(config, packet.endpointAddress.toString());                    
            try{
                itm.renewIssuedToken(itc);
            }catch(WSTrustException se){
                log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
                throw new WebServiceException(LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
            }
        }
        //TODO: replace this code with calls to the Module now
        try {
            if (!optimized) {
                if (!isSCMessage) {
                    cacheOperation(msg, packet);
                }
                SOAPMessage soapMessage = msg.readAsSOAPMessage();
                soapMessage = secureOutboundMessage(soapMessage, ctx);
                msg = Messages.create(soapMessage);
            } else {
                msg = secureOutboundMessage(msg, ctx);
            }
        } catch (WssSoapFaultException ex) {
             log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(), ex);
             throw new  WebServiceException(
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(), getSOAPFaultException(ex));
        } catch (SOAPException se) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(), se);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(), se);
        }
        packet.setMessage(msg);
        if(isSCMessage){
            if(isSCRenew(packet)){
                Token scToken = (Token)packet.invocationProperties.get(SC_ASSERTION);
                SCTokenConfiguration config = new DefaultSCTokenConfiguration(wsscVer.getNamespaceURI(), false);
                config.getOtherOptions().put("MessagePolicy", getOutgoingXWSBootstrapPolicy(scToken));
                IssuedTokenContext itc =itm.createIssuedTokenContext(config, packet.endpointAddress.toString());
                try{
                    itm.renewIssuedToken(itc);
                }catch(WSTrustException se){
                    log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
                    throw new WebServiceException(LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
                }                
            }
            Packet responsePacket = null;
            if (nextPipe != null) {
                //legacy pipes in GF
                responsePacket = nextPipe.process(packet);
            } else {
                if (nextTube != null) {
                    responsePacket = Fiber.current().owner.createFiber().runSync(nextTube, packet);
                }
            }
            packet = validateResponse(responsePacket, null, null);
        }
        return packet;
    }

    @SuppressWarnings("unchecked")
    public AuthStatus validateResponse(
            MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {

        try {
            Packet ret = getResponsePacket(messageInfo);

            // Not required, commeting
//            if (!optimized) {
//                try {
//                    SOAPMessage sm = ret.getMessage().readAsSOAPMessage();
//                    Message newMsg = Messages.create(sm);
//                    ret.setMessage(newMsg);
//                } catch (SOAPException ex) {
//                    log.log(Level.SEVERE,
//                            LogStringsMessages.WSITPVD_0033_ERROR_VALIDATE_RESPONSE(), ex);
//                    throw new WebServiceException(
//                            LogStringsMessages.WSITPVD_0033_ERROR_VALIDATE_RESPONSE(), ex);
//                }
//            }
            ret = validateResponse(ret, clientSubject, serviceSubject);
            resetCachedOperation(ret);

            Boolean trustMsgProp = (Boolean) messageInfo.getMap().get("IS_TRUST_MSG");
            boolean isTrustMsg = (trustMsgProp != null) ? trustMsgProp.booleanValue() : false;
            if (isTrustMsg) {
                //String action = getAction(ret);
                getAction(ret);
            }

            setResponsePacket(messageInfo, ret);

        } catch (XWSSecurityException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0033_ERROR_VALIDATE_RESPONSE(), ex);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0033_ERROR_VALIDATE_RESPONSE(),
                    getSOAPFaultException(ex));
        }
        return AuthStatus.SUCCESS;
    }

    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
        cancelSecurityContextToken();
        authConfig.get().cleanupAuthContext(this.tubeOrPipe.get());
        authConfig.clear();
        this.tubeOrPipe.clear();
        //issuedTokenContextMap.clear();
        //scPolicyIDtoSctIdMap.clear();
    }

    public Packet validateResponse(Packet req, Subject clientSubject, Subject serviceSubject)
            throws XWSSecurityException {
        ProcessingContext ctx = initializeInboundProcessingContext(req);
        ctx.isClient(true);
        
        ((ProcessingContextImpl) ctx).setIssuedTokenContextMap(issuedTokenContextMap);
        ((ProcessingContextImpl)ctx).setSCPolicyIDtoSctIdMap(scPolicyIDtoSctIdMap);
        ctx.setExtraneousProperty(ProcessingContext.OPERATION_RESOLVER,
                new PolicyResolverImpl(inMessagePolicyMap, inProtocolPM, cachedOperation(req), pipeConfig, addVer, true, rmVer));
        Message msg = req.getMessage();

        try {
            if (!optimized) {
                SOAPMessage soapMessage = msg.readAsSOAPMessage();
                soapMessage = verifyInboundMessage(soapMessage, ctx);
                if (msg.isFault()) {
                    if (debug) {
                        DumpFilter.process(ctx);
                    }
                    SOAPFault fault = soapMessage.getSOAPBody().getFault();
                    //log.log(Level.SEVERE,
                    //        LogStringsMessages.WSITPVD_0034_FAULTY_RESPONSE_MSG(fault));
                    throw new SOAPFaultException(fault);
                }
                msg = Messages.create(soapMessage);
            } else {
                msg = verifyInboundMessage(msg, ctx);
            }
        } catch (XWSSecurityException xwse) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), xwse);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(),
                    getSOAPFaultException(xwse));
        } catch (SOAPException se) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), se);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), se);
        }

        //set the verified message back into the packet
        req.setMessage(msg);
        return req;
    }

    @Override
    protected SOAPMessage secureOutboundMessage(SOAPMessage message, ProcessingContext ctx) {
        try {
            ctx.setSOAPMessage(message);
            SecurityAnnotator.secureMessage(ctx);
            return ctx.getSOAPMessage();
        } catch (WssSoapFaultException soapFaultException) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(), soapFaultException);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(),
                    getSOAPFaultException(soapFaultException));
        } catch (XWSSecurityException xwse) {
            WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INTERNAL_SERVER_ERROR,
                    xwse.getMessage(), xwse);
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(), wsfe);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(),
                    getSOAPFaultException(wsfe));
        }
    }

    @Override
    protected Message secureOutboundMessage(Message message, ProcessingContext ctx) {
        try {
            JAXBFilterProcessingContext context = (JAXBFilterProcessingContext) ctx;
            context.setSOAPVersion(soapVersion);
            context.setAllowMissingTimestamp(allowMissingTimestamp);
            context.setMustUnderstandValue(securityMUValue);
            context.setJAXWSMessage(message, soapVersion);
            context.isOneWayMessage(message.isOneWay(this.pipeConfig.getWSDLPort()));
            context.setDisableIncPrefix(disableIncPrefix);
            context.setEncHeaderContent(encHeaderContent);
            SecurityAnnotator.secureMessage(context);
            return context.getJAXWSMessage();
        } catch (XWSSecurityException xwse) {
            WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INTERNAL_SERVER_ERROR,
                    xwse.getMessage(), xwse);
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(), wsfe);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(),
                    getSOAPFaultException(wsfe));
        }
    }

    protected SOAPMessage verifyInboundMessage(SOAPMessage message, ProcessingContext ctx)
            throws WssSoapFaultException, XWSSecurityException {
        try {
            ctx.setSOAPMessage(message);
            if (debug) {
                DumpFilter.process(ctx);
            }
            NewSecurityRecipient.validateMessage(ctx);
            return ctx.getSOAPMessage();
        } catch (WssSoapFaultException soapFaultException) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), soapFaultException);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(),
                    getSOAPFaultException(soapFaultException));
        } catch (XWSSecurityException xwse) {
            WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INTERNAL_SERVER_ERROR,
                    xwse.getMessage(), xwse);
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), wsfe);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(),
                    getSOAPFaultException(wsfe));
        }
    }

    protected Message verifyInboundMessage(Message message, ProcessingContext ctx) throws XWSSecurityException {
        JAXBFilterProcessingContext context = (JAXBFilterProcessingContext) ctx;
        //  context.setJAXWSMessage(message, soapVersion);
        if (debug) {
            try {
                ((LazyStreamBasedMessage) message).print();
            } catch (XMLStreamException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0003_PROBLEM_PRINTING_MSG(), ex);
                throw new XWSSecurityException(LogStringsMessages.WSITPVD_0003_PROBLEM_PRINTING_MSG(), ex);
            }
        }
        LazyStreamBasedMessage lazyStreamMessage = (LazyStreamBasedMessage)message;
        AttachmentSet attachSet = lazyStreamMessage.getAttachments();
        com.sun.xml.ws.security.opt.impl.incoming.SecurityRecipient recipient = null;
        if(attachSet == null || attachSet.isEmpty()){
            recipient =
                      new com.sun.xml.ws.security.opt.impl.incoming.SecurityRecipient(lazyStreamMessage.readMessage(),soapVersion);
        } else{
            recipient = new com.sun.xml.ws.security.opt.impl.incoming.SecurityRecipient(lazyStreamMessage.readMessage(),soapVersion, attachSet);
        }

        return recipient.validateMessage(context);
    }

    protected SecurityPolicyHolder addOutgoingMP(WSDLBoundOperation operation, Policy policy) throws PolicyException {

        SecurityPolicyHolder sph = constructPolicyHolder(policy, false, false);
        outMessagePolicyMap.put(operation, sph);
        return sph;
    }

    protected SecurityPolicyHolder addIncomingMP(WSDLBoundOperation operation, Policy policy) throws PolicyException {

        SecurityPolicyHolder sph = constructPolicyHolder(policy, false, true);
        inMessagePolicyMap.put(operation, sph);
        return sph;
    }

    protected void addIncomingProtocolPolicy(Policy effectivePolicy, String protocol) throws PolicyException {
        inProtocolPM.put(protocol, constructPolicyHolder(effectivePolicy, false, true, true));
    }

    protected void addOutgoingProtocolPolicy(Policy effectivePolicy, String protocol) throws PolicyException {
        outProtocolPM.put(protocol, constructPolicyHolder(effectivePolicy, false, false, false));
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

    public JAXBElement startSecureConversation(Packet packet)
            throws WSSecureConversationException {

        List toks = getOutBoundSCP(packet.getMessage());
        if (toks.isEmpty()) {
            // we no longer throw exception here: Secure RM can work without SC
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                        LogStringsMessages.WSITPVD_0030_NO_POLICY_FOUND_FOR_SC());
            }
            //throw new WSSecureConversationException(
            //      LogStringsMessages.WSITPVD_0030_NO_POLICY_FOUND_FOR_SC());
            return null;
        }
        //Note: Assuming only one SC assertion
        Token tok = (Token) toks.get(0);
        IssuedTokenContext ctx =
                (IssuedTokenContext) issuedTokenContextMap.get(tok.getTokenId());

        PolicyAssertion scClientAssertion = null;
        if (wsscConfig != null) {
            Iterator it = wsscConfig.iterator();
            while (it != null && it.hasNext()) {
                scClientAssertion = (PolicyAssertion) it.next();
            }
        }
        
        if (ctx == null) {

            //create RST for Issue
            try{
                SCTokenConfiguration config = new DefaultSCTokenConfiguration(wsscVer.getNamespaceURI(), (SecureConversationToken)tok, pipeConfig.getWSDLPort(), pipeConfig.getBinding(), packet, addVer, scClientAssertion);
                config.getOtherOptions().put("WSITClientAuthCOntext", this);
                ctx =itm.createIssuedTokenContext(config, packet.endpointAddress.toString());
                itm.getIssuedToken(ctx);
                issuedTokenContextMap.put(((Token)tok).getTokenId(), ctx);
                //PolicyID to sctID map
                SCTokenConfiguration sctConfig = (SCTokenConfiguration)ctx.getSecurityPolicy().get(0);
                scPolicyIDtoSctIdMap.put(((Token)tok).getTokenId(), sctConfig.getTokenId());
            }catch(WSTrustException se){
                log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
                throw new WebServiceException(LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
            }
        }

        SecurityTokenReference str = (SecurityTokenReference) ctx.getUnAttachedSecurityTokenReference();

        return WSTrustElementFactory.newInstance().toJAXBElement(str);
    }

    private CallbackHandler configureClientHandler(Set configAssertions, Properties props) {
        //Properties props = new Properties();
        String ret = populateConfigProperties(configAssertions, props);
        try {
            if (ret != null) {
                Class handlerClass = loadClass(ret);
                Object obj = handlerClass.newInstance();
                if (!(obj instanceof CallbackHandler)) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSITPVD_0031_INVALID_CALLBACK_HANDLER_CLASS(ret));
                    throw new RuntimeException(
                            LogStringsMessages.WSITPVD_0031_INVALID_CALLBACK_HANDLER_CLASS(ret));
                }
                return (CallbackHandler) obj;
            }
            return new DefaultCallbackHandler("client", props);
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0032_ERROR_CONFIGURE_CLIENT_HANDLER(), e);
            throw new RuntimeException(
                    LogStringsMessages.WSITPVD_0032_ERROR_CONFIGURE_CLIENT_HANDLER(), e);
        }
    }

    private X509Certificate getCertificateFromEPR(ClientTubeAssemblerContext context, WSDLPortImpl wpi) {
        X509Certificate x509Cert = null;
        if (context != null) {
            WSBindingProvider bpr = context.getBindingProvider();
            x509Cert = (X509Certificate) bpr.getRequestContext().get(XWSSConstants.SERVER_CERTIFICATE_PROPERTY);
            if (x509Cert == null) {
                return x509Cert;
            }
        }
        if (wpi != null) {
            WSEndpointReference epr = wpi.getEPR();
            if (epr != null) {
                WSEndpointReference.EPRExtension idExtn = null;
                XMLStreamReader xmlReader = null;
                try {
                    QName ID_QNAME = new QName("http://schemas.xmlsoap.org/ws/2006/02/addressingidentity", "Identity");
                    idExtn = epr.getEPRExtension(ID_QNAME);
                    if (idExtn != null) {
                        xmlReader = idExtn.readAsXMLStreamReader();
                        CertificateRetriever cr = new CertificateRetriever();
                        byte[] bstValue = cr.digestBST(xmlReader);
                        if (bstValue == null) {
                            throw new RuntimeException("binary security token value obtained from XMLStreamReader is null");
                        }
                        X509Certificate certificate = cr.constructCertificate(bstValue);
                       
                        boolean valid = false;
                        try {
                            valid = secEnv.validateCertificate(certificate, null);
                        } catch (WssSoapFaultException ex) {
                            log.log(Level.WARNING, "Could not validate the certificate "+ certificate);
                        }
                        if (valid) {
                            return certificate;
                        }
                    }
                } catch (XMLStreamException ex) {
                    log.log(Level.SEVERE, null, ex);
                    throw new RuntimeException(ex);
                } catch (XWSSecurityException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
          return null;
        }
        return null;
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
                try{
                    //create RST for Issue         
                    SCTokenConfiguration config = new DefaultSCTokenConfiguration(wsscVer.getNamespaceURI(), (SecureConversationToken)scToken, pipeConfig.getWSDLPort(), pipeConfig.getBinding(), packet, addVer, scClientAssertion);
                    config.getOtherOptions().put("WSITClientAuthContext", this);
                    IssuedTokenContext ctx =itm.createIssuedTokenContext(config, packet.endpointAddress.toString());
                    itm.getIssuedToken(ctx);
                    issuedTokenContextMap.put(((Token)scToken).getTokenId(), ctx);
                    //PolicyID to sctID map
                    SCTokenConfiguration sctConfig = (SCTokenConfiguration)ctx.getSecurityPolicy().get(0);
                    scPolicyIDtoSctIdMap.put(((Token)scToken).getTokenId(), sctConfig.getTokenId());
                  }catch(WSTrustException se){
                    log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
                    throw new WebServiceException(LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
                }
            }
        }
    }        

    private void cancelSecurityContextToken() {
        Enumeration keys = issuedTokenContextMap.keys();
        while (keys.hasMoreElements()) {
            String id = (String) keys.nextElement();
            IssuedTokenContext ctx =
                    (IssuedTokenContext) issuedTokenContextMap.get(id);

            if (ctx.getSecurityToken() instanceof SecurityContextToken) {
                try{
                    itm.cancelIssuedToken(ctx);
                    issuedTokenContextMap.remove(id);
                    scPolicyIDtoSctIdMap.remove(id);
                }catch(WSTrustException se){
                    log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
                    throw new WebServiceException(LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
                }
            }
        }
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

        // Get issued tokens
        for (PolicyAssertion issuedTokenAssertion : policies) {
            // Get run time STSIssuedTokenConfiguration
            STSIssuedTokenConfiguration rtConfig = null;
            STSIssuedTokenFeature stsFeature = pipeConfig.getBinding().getFeature(STSIssuedTokenFeature.class);
            if (stsFeature != null) {
                rtConfig = stsFeature.getSTSIssuedTokenConfiguration();
            }

            // Create the configuration
            STSIssuedTokenConfiguration config = null;
            if (issuedTokenContextMap.get(((Token) issuedTokenAssertion).getTokenId()) == null || rtConfig != null) {
                try {
                    // Get STS information from message context
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
                    if (serverCert != null){
                        config.getOtherOptions().put("Identity", serverCert);
                    }

                    // get entries from run time configuration
                    if (rtConfig != null){
                        rtConfig.getOtherOptions().put(STSIssuedTokenConfiguration.ISSUED_TOKEN, config);
                        rtConfig.getOtherOptions().put(STSIssuedTokenConfiguration.APPLIES_TO, packet.endpointAddress.toString());
                        ((DefaultSTSIssuedTokenConfiguration)config).copy(rtConfig);
                    }

                    // Obtain issued token from STS
                    IssuedTokenContext ctx = itm.createIssuedTokenContext(config, packet.endpointAddress.toString());
                    itm.getIssuedToken(ctx);
                    issuedTokenContextMap.put(
                            ((Token) issuedTokenAssertion).getTokenId(), ctx);

                    updateMPForIssuedTokenAsEncryptedSupportingToken(packet, ctx, ((Token) issuedTokenAssertion).getTokenId());

                } catch (WSTrustException se) {
                    log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
                    throw new WebServiceException(LogStringsMessages.WSITPVD_0052_ERROR_ISSUEDTOKEN_CREATION(), se);
                }
            }
        }
    }

    // returns a list of IssuedTokenPolicy Assertions contained in the
    // service policy
    protected List<PolicyAssertion> getIssuedTokenPolicies(Packet packet, String scope) {
        if (outMessagePolicyMap == null) {
            return new ArrayList<PolicyAssertion>();
        }

        WSDLBoundOperation operation = null;
        if (isTrustMessage(packet)) {
            operation = getWSDLOpFromAction(packet, false);
        } else {
            operation = getOperation(packet.getMessage(), packet);
        }

        SecurityPolicyHolder sph = (SecurityPolicyHolder) outMessagePolicyMap.get(operation);
        if (sph == null) {
            return EMPTY_LIST;
        }
        return sph.getIssuedTokens();
    }

    protected void populateKerberosContext(Packet packet, ProcessingContextImpl ctx, boolean isSCMessage) throws XWSSecurityException {
        List toks = getOutBoundKTP(packet, isSCMessage);
        if (toks.isEmpty()) {
            return;
        }
        //Note: Assuming only one Kerberos token assertion
        Token tok = (Token) toks.get(0);
        String tokId = tok.getTokenId();

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
    
    private void updateMPForIssuedTokenAsEncryptedSupportingToken(Packet packet, final IssuedTokenContext ctx, final String issuedTokenPolicyId){
        /*
         * If IssuedToken is present as SignedSupprotingToken in the wsdl, then the
         * primary signature must have IssuedToken's id for the signature target instead
         * of policyId of issuedTokenAssertion
         */        
        Message message = packet.getMessage();
        WSDLBoundOperation operation = message.getOperation(pipeConfig.getWSDLPort());
        SecurityPolicyHolder sph = (SecurityPolicyHolder) outMessagePolicyMap.get(operation);
        if(sph != null && sph.isIssuedTokenAsEncryptedSupportingToken()){
            MessagePolicy policy = sph.getMessagePolicy();
            ArrayList list = policy.getPrimaryPolicies();
            Iterator i = list.iterator();
            boolean breakOuterLoop = false;
            while (i.hasNext()) {
                SecurityPolicy primaryPolicy = (SecurityPolicy) i.next();
                if(PolicyTypeUtil.encryptionPolicy(primaryPolicy)){
                    EncryptionPolicy encPolicy = (EncryptionPolicy)primaryPolicy;
                    EncryptionPolicy.FeatureBinding featureBinding = (EncryptionPolicy.FeatureBinding)encPolicy.getFeatureBinding();
                    ArrayList targetList = featureBinding.getTargetBindings();
                    ListIterator iterator = targetList.listIterator();
                    while(iterator.hasNext()) {
                        EncryptionTarget encryptionTarget = (EncryptionTarget)iterator.next();
                        String targetURI = encryptionTarget.getValue();
                        if(targetURI.equals(issuedTokenPolicyId)){
                            if (ctx != null) {
                                GenericToken issuedToken = (GenericToken)ctx.getSecurityToken();
                                encryptionTarget.setValue(issuedToken.getId());
                                sph.setMessagePolicy(policy);
                                outMessagePolicyMap.put(operation, sph);
                                breakOuterLoop = true;
                                break;
                            }
                        }
                    }
                    if(breakOuterLoop){
                        break;
                    }
                }
            }
        }
    }

    private void copyStandardSecurityProperties(Packet packet, Packet requestPacket) {
        String username = (String) packet.invocationProperties.get(com.sun.xml.wss.XWSSConstants.USERNAME_PROPERTY);
        if (username != null) {
            requestPacket.invocationProperties.put(com.sun.xml.wss.XWSSConstants.USERNAME_PROPERTY, username);
        }
        String password = (String) packet.invocationProperties.get(com.sun.xml.wss.XWSSConstants.PASSWORD_PROPERTY);
        if (password != null) {
            requestPacket.invocationProperties.put(com.sun.xml.wss.XWSSConstants.PASSWORD_PROPERTY, password);
        }
    }    
}
