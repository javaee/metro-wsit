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

/*
 * WSITServerAuthContext.java
 *
 * Created on November 1, 2006, 12:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.security.secconv.WSSecureConversationRuntimeException;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.security.message.stream.LazyStreamBasedMessage;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.runtime.dev.Session;
import com.sun.xml.ws.runtime.dev.SessionManager;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyHolder;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.secconv.WSSCContract;
import com.sun.xml.ws.security.secconv.WSSCFactory;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.wss.NonceManager;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.RealmAuthenticationAdapter;
import com.sun.xml.wss.SubjectAccessor;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.NewSecurityRecipient;
import com.sun.xml.wss.impl.PolicyResolver;
import com.sun.xml.wss.impl.ProcessingContextImpl;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.misc.DefaultCallbackHandler;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.misc.WSITProviderSecurityEnvironment;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.jaxws.impl.Constants;
import com.sun.xml.wss.provider.wsit.logging.LogStringsMessages;
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
import javax.security.auth.message.config.ServerAuthContext;
//import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;

import static com.sun.xml.wss.jaxws.impl.Constants.SC_ASSERTION;
import java.net.URI;
import javax.xml.bind.JAXBElement;

import java.util.logging.Level;
import java.lang.ref.WeakReference;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *
 * @author kumar jayanti
 */
public class WSITServerAuthContext extends WSITAuthContextBase implements ServerAuthContext {
    
    protected static final String TRUE="true";
    static final String SERVICE_ENDPOINT = "SERVICE_ENDPOINT";
    //****************Class Variables***************
    private SessionManager sessionManager= null;
    
    
    //******************Instance Variables*********
    private Set trustConfig = null;
    private Set wsscConfig = null;
    private CallbackHandler handler = null;
    
    //****************Variables passed to Context CTOR********
    String operation = null;
    //Subject subject = null;
    //Map map = null;
    WeakReference<WSEndpoint> endPoint =  null;
    
    //***************AuthModule Instance**********
    WSITServerAuthModule  authModule = null;
    
    static final String PIPE_HELPER = "PIPE_HELPER";
    
    /** Creates a new instance of WSITServerAuthContext */
    @SuppressWarnings("unchecked")
    public WSITServerAuthContext(String operation, Subject subject, Map<Object, Object> map, CallbackHandler callbackHandler) {
        super(map);
        this.operation = operation;
        //this.subject = subject;
        //this.map = map;
        endPoint = new WeakReference((WSEndpoint)map.get("ENDPOINT"));
        sessionManager = SessionManager.getSessionManager(endPoint.get());

        //need to merge config assertions from all alternatives
        //because we do not know which alternative the req uses
        //and so if username comes we need to have usersupplied validator
        //and if SAML comes we need the userspecified SAML validator
        Set configAssertions = null;
        for (PolicyAlternativeHolder p : policyAlternatives) {           
            Iterator it = p.getInMessagePolicyMap().values().iterator();
            while (it.hasNext()) {
                SecurityPolicyHolder holder = (SecurityPolicyHolder) it.next();
                if (configAssertions != null) {
                    configAssertions.addAll(holder.getConfigAssertions(Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS));
                } else {
                    configAssertions = holder.getConfigAssertions(Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS);
                }
                if (trustConfig != null) {
                    trustConfig.addAll(holder.getConfigAssertions(com.sun.xml.ws.security.impl.policy.Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS));
                } else {
                    trustConfig = holder.getConfigAssertions(com.sun.xml.ws.security.impl.policy.Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS);
                }
                if (wsscConfig != null) {
                    wsscConfig.addAll(holder.getConfigAssertions(com.sun.xml.ws.security.impl.policy.Constants.SUN_SECURE_SERVER_CONVERSATION_POLICY_NS));
                } else {
                    wsscConfig = holder.getConfigAssertions(com.sun.xml.ws.security.impl.policy.Constants.SUN_SECURE_SERVER_CONVERSATION_POLICY_NS);
                }
            }
        }
        String isGF = System.getProperty("com.sun.aas.installRoot");
        if (isGF != null) {            
            try {
                Properties props = new Properties();
                populateConfigProperties(configAssertions, props);
                String jmacHandler = props.getProperty(DefaultCallbackHandler.JMAC_CALLBACK_HANDLER);
                if (jmacHandler != null) {
                    handler = loadGFHandler(false, jmacHandler);
                } else if (callbackHandler != null) {
                    handler = callbackHandler;
                }
                if (handler == null) {
                   handler = loadGFHandler(false, jmacHandler); 
                }
                
                secEnv = new WSITProviderSecurityEnvironment(handler, map, props);
            }catch (XWSSecurityException ex) {
                log.log(Level.SEVERE,
                        LogStringsMessages.WSITPVD_0048_ERROR_POPULATING_SERVER_CONFIG_PROP(), ex);
                throw new WebServiceException(
                        LogStringsMessages.WSITPVD_0048_ERROR_POPULATING_SERVER_CONFIG_PROP(), ex);
            }
        } else {
            //This will handle Non-GF containers where no config assertions
            // are required in the WSDL. Ex. UsernamePassword validation
            // with Default Realm Authentication
            Properties props = new Properties();
            handler = configureServerHandler(configAssertions, props);
            String jmacHandler = props.getProperty(DefaultCallbackHandler.JMAC_CALLBACK_HANDLER);
            if (jmacHandler != null) {
                try {
                    handler = loadGFHandler(false, jmacHandler);
                    secEnv = new WSITProviderSecurityEnvironment(handler, map, props);
                } catch (XWSSecurityException ex) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSITPVD_0048_ERROR_POPULATING_SERVER_CONFIG_PROP(), ex);
                    throw new WebServiceException(
                            LogStringsMessages.WSITPVD_0048_ERROR_POPULATING_SERVER_CONFIG_PROP(), ex);
                }
            } else {
                secEnv = new DefaultSecurityEnvironmentImpl(handler, props);
            }
        }
        
        //initialize the AuthModules and keep references to them
        authModule = new WSITServerAuthModule();
        try {
            authModule.initialize(null, null, null,map);
        }catch (AuthException e) {
            log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0028_ERROR_INIT_AUTH_MODULE(), e);
            throw new RuntimeException(LogStringsMessages.WSITPVD_0028_ERROR_INIT_AUTH_MODULE(), e);
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {

        Packet packet = getRequestPacket(messageInfo);
        Packet ret = null;

        // Not required, commenting
//        if (!optimized) {
//            cacheMessage(packet);
//        }
        
        try {
            ret = validateRequest(packet, clientSubject, serviceSubject, messageInfo.getMap());
        }catch (XWSSecurityException ex) {
            throw getSOAPFaultException(ex);
        }
        
        if (messageInfo.getMap().get("THERE_WAS_A_FAULT") != null) {
            setResponsePacket(messageInfo, ret);
            return AuthStatus.SEND_FAILURE;
        }
        
        boolean isSCMessage = ((messageInfo.getMap().get("IS_SC_ISSUE") != null) ||
                (messageInfo.getMap().get("IS_SC_CANCEL") != null));
        if (isSCMessage) {
            
            setResponsePacket(messageInfo, ret);
            //this would cause skipping the application processing for now
            return AuthStatus.SEND_SUCCESS;
        }
        
        setRequestPacket(messageInfo, ret);
        
        return AuthStatus.SUCCESS;
    }
    
    @SuppressWarnings("unchecked")
    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
        // Add addressing headers to trust message
        
        //TODO: this is the one that came from nextPipe.process
        //TODO: replace this with call to packetMessageInfo.getResponsePacket
        Packet retPacket = getResponsePacket(messageInfo);
       // if (isTrustMessage){
         //   retPacket = addAddressingHeaders(packet, retPacket.getMessage(), wsTrustVer.getFinalResponseAction((String)messageInfo.getMap().get("TRUST_REQUEST_ACTION")));
       // }
        Packet ret = null;
        try {
            ret= secureResponse(retPacket, serviceSubject, messageInfo.getMap());
        } catch (XWSSecurityException ex) {
            //TODO: acutally rewrite the message in the packet to contain a fault here
            throw getSOAPFaultException(ex);
        }
        
        setResponsePacket(messageInfo, ret);
        
        if (messageInfo.getMap().get("THERE_WAS_A_FAULT") != null) {
            return AuthStatus.SEND_FAILURE;
        }
        
        return AuthStatus.SUCCESS;
    }
    
    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
        issuedTokenContextMap.clear();
        SessionManager.removeSessionManager(endPoint.get());
        NonceManager.deleteInstance(endPoint.get());
    }
    
    public Packet validateRequest(Packet packet, Subject clientSubject, Subject serviceSubject, Map<Object, Object> sharedState)
    throws XWSSecurityException {
        
        Message msg = packet.getMessage();
        
        boolean isSCIssueMessage = false;
        boolean isSCCancelMessage = false;
        boolean isTrustMessage = false;
        String msgId = null;
        String action = null;
        
        boolean thereWasAFault = false;
        
        //Do Security Processing for Incoming Message
        
        //---------------INBOUND SECURITY VERIFICATION----------
        ProcessingContext ctx = initializeInboundProcessingContext(packet);
        
        //update the client subject passed to the AuthModule itself.
        ctx.setExtraneousProperty(MessageConstants.AUTH_SUBJECT, clientSubject);
        PolicyResolver pr = PolicyResolverFactory.createPolicyResolver(policyAlternatives,
                cachedOperation(packet),pipeConfig,addVer,false, rmVer, mcVer);
        ctx.setExtraneousProperty(ProcessingContext.OPERATION_RESOLVER, pr);

        ctx.setExtraneousProperty("SessionManager", sessionManager);
        try{
            if(!optimized) {
                SOAPMessage soapMessage = msg.readAsSOAPMessage();
                soapMessage = verifyInboundMessage(soapMessage, ctx);
                msg = Messages.create(soapMessage);
            }else{
                msg = verifyInboundMessage(msg, ctx);
            }
        } catch (WssSoapFaultException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), ex);
            thereWasAFault = true;
            SOAPFaultException sfe = SOAPUtil.getSOAPFaultException(ex, soapFactory, soapVersion);
            msg = Messages.create(sfe, soapVersion);
        } catch (XWSSecurityException xwse) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), xwse);
            thereWasAFault = true;
            SOAPFaultException sfe = SOAPUtil.getSOAPFaultException(xwse, soapFactory, soapVersion);
            msg = Messages.create(sfe, soapVersion);
            
        } catch (XWSSecurityRuntimeException xwse) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), xwse);
            thereWasAFault = true;
              SOAPFaultException sfe = SOAPUtil.getSOAPFaultException(xwse, soapFactory, soapVersion);
            msg = Messages.create(sfe, soapVersion);
            
        }  catch (WebServiceException xwse) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), xwse);
            thereWasAFault = true;
            SOAPFaultException sfe = SOAPUtil.getSOAPFaultException(xwse, soapFactory, soapVersion);
            msg = Messages.create(sfe, soapVersion);
            
        } catch (WSSecureConversationRuntimeException wsre){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), wsre);
            thereWasAFault = true;
             QName faultCode = wsre.getFaultCode();
             if (faultCode != null){
                 faultCode = new QName(wsscVer.getNamespaceURI(), faultCode.getLocalPart());
             }
             SOAPFaultException sfe = SOAPUtil.getSOAPFaultException(faultCode, wsre, soapFactory, soapVersion);
             msg = Messages.create(sfe, soapVersion);
        } catch(SOAPException se){
            // internal error
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), se);
            thereWasAFault = true;
            SOAPFaultException sfe = SOAPUtil.getSOAPFaultException(se, soapFactory, soapVersion);
            msg = Messages.create(sfe, soapVersion);
        } catch (Exception ex) {
            //NPE's from server need to be handled as well
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), ex);
            thereWasAFault = true;
            SOAPFaultException sfe = SOAPUtil.getSOAPFaultException(ex, soapFactory, soapVersion);
            msg = Messages.create(sfe, soapVersion);
        }
        
        if (thereWasAFault) {
            sharedState.put("THERE_WAS_A_FAULT", Boolean.valueOf(thereWasAFault));
            if (this.isAddressingEnabled()) {
                if (optimized) {
                    packet.setMessage(((JAXBFilterProcessingContext)ctx).getPVMessage());
                }
                Packet ret = packet.createServerResponse(
                        msg, this.addVer, this.soapVersion, this.addVer.getDefaultFaultAction());
                return ret;
            } else {
                packet.setMessage(msg);
                return packet;
            }
        }
        
        packet.setMessage(msg);
        
        if (isAddressingEnabled()) {
            action = getAction(packet);
            if (wsscVer.getSCTRequestAction().equals(action) || wsscVer.getSCTRenewRequestAction().equals(action)) {
                isSCIssueMessage = true;
                sharedState.put("IS_SC_ISSUE", TRUE);
                if(wsscConfig != null){
                    packet.invocationProperties.put(
                            com.sun.xml.ws.security.impl.policy.Constants.SUN_SECURE_SERVER_CONVERSATION_POLICY_NS, wsscConfig.iterator());
                }
            } else if (wsscVer.getSCTCancelRequestAction().equals(action)) {
                isSCCancelMessage = true;
                sharedState.put("IS_SC_CANCEL", TRUE);
            } else if (wsTrustVer.getIssueRequestAction().equals(action)||
                       wsTrustVer.getValidateRequestAction().equals(action)) {
                isTrustMessage = true;
                sharedState.put("IS_TRUST_MESSAGE", TRUE);
                sharedState.put("TRUST_REQUEST_ACTION", action);
                //packet.getMessage().getHeaders().getTo(addVer, pipeConfig.getBinding().getSOAPVersion());
                
                if(trustConfig != null){
                    packet.invocationProperties.put(
                            com.sun.xml.ws.security.impl.policy.Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS,trustConfig.iterator());
                }
                
                //set the SecurityEnvironment
                packet.invocationProperties.put(WSTrustConstants.SECURITY_ENVIRONMENT, secEnv);
                packet.invocationProperties.put(WSTrustConstants.WST_VERSION, this.wsTrustVer);
                IssuedTokenContext ictx = ((ProcessingContextImpl)ctx).getTrustContext();
                if(ictx != null && ictx.getAuthnContextClass() != null){                    
                    packet.invocationProperties.put(WSTrustConstants.AUTHN_CONTEXT_CLASS, ictx.getAuthnContextClass());
                }                
            }
            
            if (isSCIssueMessage){
                List<PolicyAssertion> policies = getInBoundSCP(packet.getMessage());
                if(!policies.isEmpty()) {
                    packet.invocationProperties.put(SC_ASSERTION, (PolicyAssertion)policies.get(0));
                }
            }
        }
        
        if(!isSCIssueMessage ){
            WSDLBoundOperation cachedOperation = cacheOperation(msg, packet);
            if(cachedOperation == null){
                if(addVer != null) {
                    cachedOperation = getWSDLOpFromAction(packet, true);
                    packet.invocationProperties.put("WSDL_BOUND_OPERATION", cachedOperation);
                }
            }
        }
        
        sharedState.put("VALIDATE_REQ_PACKET", packet);
        
        Packet retPacket = null;
        
        if (isSCIssueMessage || isSCCancelMessage) {
            //-------put application message on hold and invoke SC contract--------
            retPacket = invokeSecureConversationContract(
                    packet, ctx, isSCIssueMessage);
            // if this is SC message we need to secure it in ValidateRequest Itself
            retPacket = secureResponse(retPacket, serviceSubject, sharedState);
        } else {
             updateSCSessionInfo(packet);
            retPacket = packet;
        }
        
        return retPacket;
    }
    
    @SuppressWarnings("unchecked")
    public Packet secureResponse(Packet retPacket, Subject serviceSubject, Map sharedState) throws XWSSecurityException {
        
        boolean isSCIssueMessage = (sharedState.get("IS_SC_ISSUE") != null) ? true : false;
        boolean isSCCancelMessage =(sharedState.get("IS_SC_CANCEL") != null) ? true : false;
        boolean isTrustMessage =(sharedState.get("IS_TRUST_MESSAGE") != null) ? true: false;
        
        Packet packet = (Packet)sharedState.get("VALIDATE_REQ_PACKET");
        Boolean thereWasAFaultSTR = (Boolean)sharedState.get("THERE_WAS_A_FAULT");
        boolean thereWasAFault =  (thereWasAFaultSTR != null) ? thereWasAFaultSTR.booleanValue(): false;
        
        if (thereWasAFault) {
            return retPacket;
        }
        
        /* TODO:this piece of code present since payload should be read once*/
        if (!optimized) {
            try{
                SOAPMessage sm = retPacket.getMessage().readAsSOAPMessage();
                Message newMsg = Messages.create(sm);
                retPacket.setMessage(newMsg);
            }catch(SOAPException ex){
                throw new WebServiceException(ex);
            }
        }
        
        //---------------OUTBOUND SECURITY PROCESSING----------
        ProcessingContext ctx = initializeOutgoingProcessingContext(retPacket, isSCIssueMessage);
        ctx.setExtraneousProperty("SessionManager", sessionManager);
        Message msg = retPacket.getMessage();
        
        try{
            
            if (ctx.getSecurityPolicy() != null && ((MessagePolicy)ctx.getSecurityPolicy()).size() >0) {
                if(!optimized) {
                    SOAPMessage soapMessage = msg.readAsSOAPMessage();
                    soapMessage = secureOutboundMessage(soapMessage, ctx);
                    msg = Messages.create(soapMessage);
                }else{
                    msg = secureOutboundMessage(msg, ctx);
                }
            }
        } catch (WssSoapFaultException ex) {
            sharedState.put("THERE_WAS_A_FAULT", Boolean.valueOf(true));
            msg = Messages.create(getSOAPFault(ex));
        } catch(SOAPException se) {
            // internal error
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(), se);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(), se);
        } finally{
            if (isSCCancel(retPacket)){
                removeContext(packet);
            }
        }
        resetCachedOperation(retPacket);
        retPacket.setMessage(msg);
        return retPacket;
    }
    
    protected SOAPMessage verifyInboundMessage(SOAPMessage message, ProcessingContext ctx)
    throws WssSoapFaultException, XWSSecurityException {
        if (debug) {
            DumpFilter.process(ctx);
        }
        ctx.setSOAPMessage(message);
        NewSecurityRecipient.validateMessage(ctx);
        return ctx.getSOAPMessage();
    }
    
    
    protected Message verifyInboundMessage(Message message, ProcessingContext ctx) throws XWSSecurityException{
        JAXBFilterProcessingContext  context = (JAXBFilterProcessingContext)ctx;
        //  context.setJAXWSMessage(message, soapVersion);
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

    @Override
    @SuppressWarnings("unchecked")
    protected ProcessingContext initializeOutgoingProcessingContext(Packet packet, boolean isSCMessage) {
        ProcessingContextImpl ctx = null;
        if(optimized){
            ctx = new JAXBFilterProcessingContext(packet.invocationProperties);
            ((JAXBFilterProcessingContext)ctx).setAddressingVersion(addVer);
            ((JAXBFilterProcessingContext)ctx).setSOAPVersion(soapVersion);
        }else{
            ctx = new ProcessingContextImpl( packet.invocationProperties);
        }
        if (addVer != null) {
            ctx.setAction(getAction(packet));
        }
        //set timestamp timeout
        ctx.setTimestampTimeout(this.timestampTimeOut);
        ctx.setSecurityPolicyVersion(spVersion.namespaceUri);
        try {
            MessagePolicy policy = null;
             PolicyAlternativeHolder applicableAlternative =
                    resolveAlternative(packet,isSCMessage);

            if (packet.getMessage().isFault()) {
                policy =  getOutgoingFaultPolicy(packet);
            } else if (isRMMessage(packet)|| isMakeConnectionMessage(packet)) {
                SecurityPolicyHolder holder = applicableAlternative.getOutProtocolPM().get("RM");
                policy = holder.getMessagePolicy();
            } else if(isSCCancel(packet)){
                SecurityPolicyHolder holder = applicableAlternative.getOutProtocolPM().get("SC-CANCEL");
                policy = holder.getMessagePolicy();
            }else {
                policy = getOutgoingXWSSecurityPolicy(packet, isSCMessage);
            }
            
            if (debug && policy != null) {
                policy.dumpMessages(true);
            }
            //this might mislead if there is a bug in code above
            //but we are doing this check for cases such as no-fault-security-policy
            if (policy != null) {
                ctx.setSecurityPolicy(policy);
            }
            if(isTrustMessage(packet)){
                ctx.isTrustMessage(true);
            }
            // set the policy, issued-token-map, and extraneous properties
            //ctx.setIssuedTokenContextMap(issuedTokenContextMap);
             if (isSCMessage){
                ctx.setAlgorithmSuite(policy.getAlgorithmSuite());
            } else {
                ctx.setAlgorithmSuite(getAlgoSuite(getBindingAlgorithmSuite(packet)));
            }
            ctx.setSecurityEnvironment(secEnv);
            ctx.isInboundMessage(false);          
            @SuppressWarnings("unchecked")
            Map<Object, Object> extProps = ctx.getExtraneousProperties();
            extProps.put(WSITServerAuthContext.WSDLPORT,pipeConfig.getWSDLPort());
        } catch (XWSSecurityException e) {
            log.log(
                    Level.SEVERE, LogStringsMessages.WSITPVD_0006_PROBLEM_INIT_OUT_PROC_CONTEXT(), e);
            throw new RuntimeException(
                    LogStringsMessages.WSITPVD_0006_PROBLEM_INIT_OUT_PROC_CONTEXT(), e);
        }
        return ctx;
    }
    
    private void removeContext(final Packet packet) {
        SecurityContextToken sct = (SecurityContextToken)packet.invocationProperties.get(MessageConstants.INCOMING_SCT);
        if (sct != null){
            String strId = sct.getIdentifier().toString();
            if(strId!=null){
                issuedTokenContextMap.remove(strId);
                sessionManager.terminateSession(strId);
            }
        }
    }
    
    @Override
    protected MessagePolicy getOutgoingXWSSecurityPolicy(
            Packet packet, boolean isSCMessage) {
        if (isSCMessage) {
            Token scToken = (Token)packet.invocationProperties.get(SC_ASSERTION);
            return getOutgoingXWSBootstrapPolicy(scToken);
        }
        //Message message = packet.getMessage();
        
        MessagePolicy mp = null;
        PolicyAlternativeHolder applicableAlternative =
                    resolveAlternative(packet,isSCMessage);
        WSDLBoundOperation wsdlOperation = cachedOperation(packet);
        //if(operation == null){
        //Body could be encrypted. Security will have to infer the
        //policy from the message till the Body is decrypted.
        //    mp = emptyMessagePolicy;
        //}
        if (applicableAlternative.getOutMessagePolicyMap() == null) {
            //empty message policy
            return new MessagePolicy();
        }
        
        if(isTrustMessage(packet)){
            //TODO: no runtime updates of variables: store this in Map of MessageInfo
            wsdlOperation = getWSDLOpFromAction(packet,false);
            cacheOperation(wsdlOperation, packet);
        }
        
        SecurityPolicyHolder sph = (SecurityPolicyHolder)
                applicableAlternative.getOutMessagePolicyMap().get(wsdlOperation);
        if(sph == null){
            return new MessagePolicy();
        }
        mp = sph.getMessagePolicy();
        return mp;
    }
    
    protected MessagePolicy getOutgoingFaultPolicy(Packet packet) {
        WSDLBoundOperation cachedOp = cachedOperation(packet);
        PolicyAlternativeHolder applicableAlternative =
                    resolveAlternative(packet,false);
        if(cachedOp != null){
            WSDLOperation wsdlOperation = cachedOp.getOperation();
            QName faultDetail = packet.getMessage().getFirstDetailEntryName();
            WSDLFault fault = null;
            if (faultDetail != null) {
                fault = wsdlOperation.getFault(faultDetail);
            }
            SecurityPolicyHolder sph = applicableAlternative.getOutMessagePolicyMap().get(cachedOp);
            if (fault == null) {
                MessagePolicy faultPolicy1 = (sph != null)?(sph.getMessagePolicy()):new MessagePolicy();
                return faultPolicy1;
            }
            SecurityPolicyHolder faultPolicyHolder = sph.getFaultPolicy(fault);
            MessagePolicy faultPolicy = (faultPolicyHolder == null) ? new MessagePolicy() : faultPolicyHolder.getMessagePolicy();
            return faultPolicy;
        }
        return null;
        
    }
    
    
    private CallbackHandler configureServerHandler(Set configAssertions, Properties props) {
        //Properties props = new Properties();
        String ret = populateConfigProperties(configAssertions, props);
        try {
            if (ret != null) {
                Class hdlr = loadClass(ret);
                Object obj = hdlr.newInstance();
                if (!(obj instanceof CallbackHandler)) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSITPVD_0031_INVALID_CALLBACK_HANDLER_CLASS(ret));
                    throw new RuntimeException(
                            LogStringsMessages.WSITPVD_0031_INVALID_CALLBACK_HANDLER_CLASS(ret));
                }
                return (CallbackHandler)obj;
            } else {
                //ServletContext context = endPoint.getContainer().getSPI(ServletContext.class);
                RealmAuthenticationAdapter adapter = this.getRealmAuthenticationAdapter(endPoint.get());
                return new DefaultCallbackHandler("server", props, adapter);
            }
        }catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSITPVD_0043_ERROR_CONFIGURE_SERVER_HANDLER(), e);
            throw new RuntimeException(
                    LogStringsMessages.WSITPVD_0043_ERROR_CONFIGURE_SERVER_HANDLER(), e);
        }
    }
    
    @Override
    protected boolean bindingHasIssuedTokenPolicy() {
        return hasIssuedTokens;
    }
    
    @Override
    protected boolean bindingHasSecureConversationPolicy() {
        return hasSecureConversation;
    }
    
    @Override
    protected boolean bindingHasRMPolicy() {
        return hasReliableMessaging;
    }
    
    // The packet has the Message with RST/SCT inside it
    // TODO: Need to inspect if it is really a Issue or a Cancel
    private Packet invokeSecureConversationContract(Packet packet, ProcessingContext ctx, boolean isSCTIssue) {
        
        IssuedTokenContext ictx = new IssuedTokenContextImpl();
        ictx.getOtherProperties().put("SessionManager", sessionManager);
        Message msg = packet.getMessage();
        Message retMsg = null;
        String retAction = null;
        
        try {
            
            // Set the requestor authenticated Subject in the IssuedTokenContext
            Subject subject = SubjectAccessor.getRequesterSubject(ctx);
            ictx.setRequestorSubject(subject);
                        
            WSTrustElementFactory wsscEleFac = WSTrustElementFactory.newInstance(wsscVer);
            JAXBElement rstEle = msg.readPayloadAsJAXB(WSTrustElementFactory.getContext(wsTrustVer).createUnmarshaller());
            BaseSTSRequest rst = wsscEleFac.createRSTFrom(rstEle);
            
            URI requestType = ((RequestSecurityToken)rst).getRequestType();
            BaseSTSResponse rstr = null;
            WSSCContract scContract = WSSCFactory.newWSSCContract(wsscVer);
            scContract.setWSSCServerConfig((Iterator)packet.invocationProperties.get(
                    com.sun.xml.ws.security.impl.policy.Constants.SUN_SECURE_SERVER_CONVERSATION_POLICY_NS));
            if (requestType.toString().equals(wsTrustVer.getIssueRequestTypeURI())) {
                List<PolicyAssertion> policies = getOutBoundSCP(packet.getMessage());
                rstr =  scContract.issue(rst, ictx, (SecureConversationToken)policies.get(0));
                retAction = wsscVer.getSCTResponseAction();
                SecurityContextToken sct = (SecurityContextToken)ictx.getSecurityToken();
                String sctId = sct.getIdentifier().toString();
                
                Session session = sessionManager.getSession(sctId);
                if (session == null) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSITPVD_0044_ERROR_SESSION_CREATION());
                    throw new WSSecureConversationException(
                            LogStringsMessages.WSITPVD_0044_ERROR_SESSION_CREATION());
                }
                
                // Put it here for RM to pick up
                packet.invocationProperties.put(
                        Session.SESSION_ID_KEY, sctId);
                
                packet.invocationProperties.put(
                        Session.SESSION_KEY, session.getUserData());
                
                IssuedTokenContext itctx = session.getSecurityInfo().getIssuedTokenContext();
                //add the subject of requestor
                itctx.setRequestorSubject(ictx.getRequestorSubject());
                //((ProcessingContextImpl)ctx).getIssuedTokenContextMap().put(sctId, itctx);
                
            } else if (requestType.toString().equals(wsTrustVer.getRenewRequestTypeURI())) {
                List<PolicyAssertion> policies = getOutBoundSCP(packet.getMessage());
                retAction = wsscVer.getSCTRenewResponseAction();
                rstr =  scContract.renew(rst, ictx,(SecureConversationToken)policies.get(0));
            } else if (requestType.toString().equals(wsTrustVer.getCancelRequestTypeURI())) {
                retAction = wsscVer.getSCTCancelResponseAction();
                rstr =  scContract.cancel(rst, ictx);
            } else {
                log.log(Level.SEVERE,
                        LogStringsMessages.WSITPVD_0045_UNSUPPORTED_OPERATION_EXCEPTION(requestType));
                throw new UnsupportedOperationException(
                        LogStringsMessages.WSITPVD_0045_UNSUPPORTED_OPERATION_EXCEPTION(requestType));
            }
            
            // construct the complete message here containing the RSTR and the
            // correct Action headers if any and return the message.     
            retMsg = Messages.create(WSTrustElementFactory.getContext(wsTrustVer).createMarshaller(), wsscEleFac.toJAXBElement(rstr), soapVersion);
            
        } catch (javax.xml.bind.JAXBException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0001_PROBLEM_MAR_UNMAR(), ex);
            throw new RuntimeException(LogStringsMessages.WSITPVD_0001_PROBLEM_MAR_UNMAR(), ex);
        } catch (com.sun.xml.wss.XWSSecurityException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0046_ERROR_INVOKE_SC_CONTRACT(), ex);
            throw new RuntimeException(LogStringsMessages.WSITPVD_0046_ERROR_INVOKE_SC_CONTRACT(), ex);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0046_ERROR_INVOKE_SC_CONTRACT(), ex);
            throw new RuntimeException(LogStringsMessages.WSITPVD_0046_ERROR_INVOKE_SC_CONTRACT(), ex);
        }
        
        
        //SecurityContextToken sct = (SecurityContextToken)ictx.getSecurityToken();
        //String sctId = sct.getIdentifier().toString();
        //((ProcessingContextImpl)ctx).getIssuedTokenContextMap().put(sctId, ictx);
        
        Packet retPacket = addAddressingHeaders(packet, retMsg, retAction);
        if (isSCTIssue){
            List<PolicyAssertion> policies = getOutBoundSCP(packet.getMessage());
            
            if(!policies.isEmpty()) {
                retPacket.invocationProperties.put(SC_ASSERTION, (PolicyAssertion)policies.get(0));
            }
        }
        
        return retPacket;
    }
    
    
    private Packet addAddressingHeaders(Packet packet, Message retMsg, String action){
        Packet retPacket = packet.createServerResponse(retMsg, addVer, soapVersion, action);
        
        retPacket.proxy = packet.proxy;
        retPacket.invocationProperties.putAll(packet.invocationProperties);
        
        return retPacket;
    }
   
    protected SecurityPolicyHolder addOutgoingMP(WSDLBoundOperation operation,Policy policy, PolicyAlternativeHolder ph)throws PolicyException{
        SecurityPolicyHolder sph = constructPolicyHolder(policy,true,true);
        ph.getInMessagePolicyMap().put(operation,sph);
        return sph;
    }
    
    protected SecurityPolicyHolder addIncomingMP(WSDLBoundOperation operation,Policy policy, PolicyAlternativeHolder ph)throws PolicyException{
        SecurityPolicyHolder sph = constructPolicyHolder(policy,true,false);
        ph.getOutMessagePolicyMap().put(operation,sph);
        return sph;
    }
    
    protected void addIncomingProtocolPolicy(Policy effectivePolicy,String protocol, PolicyAlternativeHolder ph)throws PolicyException{
        ph.getOutProtocolPM().put(protocol,constructPolicyHolder(effectivePolicy, true, false, true));
    }
    
    protected void addOutgoingProtocolPolicy(Policy effectivePolicy,String protocol, PolicyAlternativeHolder ph)throws PolicyException{
        ph.getInProtocolPM().put(protocol,constructPolicyHolder(effectivePolicy, true, true, false));
    }
    
    protected void addIncomingFaultPolicy(Policy effectivePolicy,SecurityPolicyHolder sph,WSDLFault fault)throws PolicyException{
        SecurityPolicyHolder faultPH = constructPolicyHolder(effectivePolicy,true,false);
        sph.addFaultPolicy(fault,faultPH);
    }
    
    protected void addOutgoingFaultPolicy(Policy effectivePolicy,SecurityPolicyHolder sph,WSDLFault fault)throws PolicyException{
        SecurityPolicyHolder faultPH = constructPolicyHolder(effectivePolicy,true,true);
        sph.addFaultPolicy(fault,faultPH);
    }
    
    protected String getAction(WSDLOperation operation,boolean inComming){
        if(inComming){
            return operation.getInput().getAction();
        }else{
            return operation.getOutput().getAction();
        }
    }
    
    @SuppressWarnings("unchecked")
    private RealmAuthenticationAdapter getRealmAuthenticationAdapter(WSEndpoint wSEndpoint) {
        String className = "javax.servlet.ServletContext";
        Class ret = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            try {
                ret = loader.loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        if (ret == null) {
            // if context classloader didnt work, try this
            loader = this.getClass().getClassLoader();
            try {
                ret = loader.loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        if (ret != null) {
            Object obj = wSEndpoint.getContainer().getSPI(ret);
            if (obj != null) {
                return RealmAuthenticationAdapter.newInstance(obj);
            }
        }
        return null;
    }   
    
    @SuppressWarnings("unchecked")
    private void updateSCSessionInfo(Packet packet) {
        SecurityContextToken sct =
                (SecurityContextToken)packet.invocationProperties.get(MessageConstants.INCOMING_SCT);
        if (sct != null) {
            // get the secure session id 
            String sessionId = sct.getIdentifier().toString();
            
            // put the secure session id the the message context
            packet.invocationProperties.put(Session.SESSION_ID_KEY, sessionId);
            packet.invocationProperties.put(Session.SESSION_KEY, sessionManager.getSession(sessionId).getUserData());
        }
    }
}