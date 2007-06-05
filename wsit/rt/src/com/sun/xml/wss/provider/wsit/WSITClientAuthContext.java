/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.message.stream.LazyStreamBasedMessage;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyHolder;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.secconv.NewWSSCPlugin;
import com.sun.xml.ws.security.secconv.WSSCFactory;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.security.trust.TrustPlugin;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustFactory;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.NewSecurityRecipient;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.misc.DefaultCallbackHandler;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.misc.WSITProviderSecurityEnvironment;
import com.sun.xml.wss.jaxws.impl.Constants;
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
import static com.sun.xml.wss.jaxws.impl.Constants.EMPTY_LIST;
import static com.sun.xml.wss.jaxws.impl.Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS;
import static com.sun.xml.wss.jaxws.impl.Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS;
import com.sun.xml.wss.jaxws.impl.PolicyResolverImpl;
import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.provider.wsit.logging.LogDomainConstants;
import com.sun.xml.wss.provider.wsit.logging.LogStringsMessages;

/**
 *
 * @author kumar jayanti
 */
public class WSITClientAuthContext  extends WSITAuthContextBase 
        implements ClientAuthContext {
    
    //*****************STATIC****************
    // Plugin instances for Trust and SecureConversation invocation
    private static TrustPlugin trustPlugin = WSTrustFactory.newTrustPlugin(null);
    private static NewWSSCPlugin  scPlugin = WSSCFactory.newNewSCPlugin(null);
    
    //******************INSTANCE VARIABLES*******
    // do not use this operation it will be null
    //String operation = null;
    //Subject subject = null; 
    //Map map = null;
  
    private Set trustConfig = null;
    private CallbackHandler handler = null;
    
    //***************AuthModule Instance**********
    WSITClientAuthModule  authModule = null;
    
    /** Creates a new instance of WSITClientAuthContext */
    public WSITClientAuthContext(String operation, Subject subject, Map map) {
        super(map);
        //this.operation = operation;
        //this.subject = subject;
        //this.map = map;
        
        
        Iterator it = outMessagePolicyMap.values().iterator();
        SecurityPolicyHolder holder = (SecurityPolicyHolder)it.next();
        Set configAssertions = holder.getConfigAssertions(Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS);
        trustConfig = holder.getConfigAssertions(
                com.sun.xml.ws.security.impl.policy.Constants.SUN_TRUST_CLIENT_SECURITY_POLICY_NS);

        boolean isACC = isGFAppClient();
        String isGF = System.getProperty("com.sun.aas.installRoot");
        //this client is an ACC client or a WebClient
        if (isACC || (isGF != null) ) {
            try {
                Properties props = new Properties();
                populateConfigProperties(configAssertions, props);
                String jmacHandler = props.getProperty(DefaultCallbackHandler.JMAC_CALLBACK_HANDLER);
                handler = loadGFHandler(true, jmacHandler);
                secEnv = new WSITProviderSecurityEnvironment(handler, map, props);
            }catch (XWSSecurityException ex) {
                log.log(Level.SEVERE, 
                        LogStringsMessages.WSITPVD_0027_ERROR_POPULATING_CLIENT_CONFIG_PROP(), ex);
                throw new WebServiceException(
                        LogStringsMessages.WSITPVD_0027_ERROR_POPULATING_CLIENT_CONFIG_PROP(), ex);  
            }
        } else {
            Properties props = new Properties();
            handler = configureClientHandler(configAssertions, props);
            secEnv = new DefaultSecurityEnvironmentImpl(handler, props);
        }
                
        //initialize the AuthModules and keep references to them
        authModule = new WSITClientAuthModule();
        try {
            authModule.initialize(null, null, null,map);
        } catch (AuthException e) {
            log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0028_ERROR_INIT_AUTH_MODULE(), e);                         
            throw new RuntimeException(LogStringsMessages.WSITPVD_0028_ERROR_INIT_AUTH_MODULE(), e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public AuthStatus secureRequest(
            MessageInfo messageInfo, Subject clientSubject) throws AuthException {
       
        try {
            
            Packet packet = getRequestPacket(messageInfo);
            // Add Action header to trust message
            boolean isTrustMsg = false;
            if ("true".equals(packet.invocationProperties.get(WSTrustConstants.IS_TRUST_MESSAGE))){
                isTrustMsg = true;
                String action = (String)packet.invocationProperties.get(WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION);
                HeaderList headers = packet.getMessage().getHeaders();
                headers.fillRequestAddressingHeaders(packet, addVer, soapVersion,false, action);
            }
            
            //set the isTrustProperty into MessageInfo
            messageInfo.getMap().put("IS_TRUST_MSG", Boolean.valueOf(isTrustMsg));
            
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
    
    @SuppressWarnings("unchecked")
    public Packet secureRequest(
            Packet packet, Subject clientSubject, boolean isSCMessage) throws XWSSecurityException { 
        // invoke the Trust Plugin if necessary
        Message msg = packet.getMessage();
        invokeTrustPlugin(packet, isSCMessage);
        ProcessingContext ctx = initializeOutgoingProcessingContext(packet, isSCMessage);
        
        //TODO: replace this code with calls to the Module now
         try{
            if(!optimized) {
                if(!isSCMessage){
                    cacheOperation(msg, packet);
                }
                SOAPMessage soapMessage = msg.readAsSOAPMessage();
                soapMessage = secureOutboundMessage(soapMessage, ctx);
                msg = Messages.create(soapMessage);
            }else{
                msg = secureOutboundMessage(msg, ctx);
            }
        } catch(SOAPException se){
            log.log(Level.SEVERE, 
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(), se);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0029_ERROR_SECURING_OUTBOUND_MSG(), se);
        }
        packet.setMessage(msg);
        return packet;
    }
   
    @SuppressWarnings("unchecked")
    public AuthStatus validateResponse(
            MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {
        
        try {
            Packet ret = getResponsePacket(messageInfo);
            
            if (!optimized) {
                try{
                    SOAPMessage sm = ret.getMessage().readAsSOAPMessage();
                    Message newMsg = Messages.create(sm);
                    ret.setMessage(newMsg);
                }catch(SOAPException ex){
                    log.log(Level.SEVERE, 
                            LogStringsMessages.WSITPVD_0033_ERROR_VALIDATE_RESPONSE(), ex);                    
                    throw new WebServiceException(
                            LogStringsMessages.WSITPVD_0033_ERROR_VALIDATE_RESPONSE(), ex);
                }
            }
            ret = validateResponse(ret, clientSubject, serviceSubject);
            resetCachedOperation(ret);
            
            Boolean trustMsgProp = (Boolean)messageInfo.getMap().get("IS_TRUST_MSG");
            boolean isTrustMsg = (trustMsgProp != null) ? trustMsgProp.booleanValue() : false;
            if (isTrustMsg){
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
        issuedTokenContextMap.clear();
    }
    
    
         
    public Packet validateResponse(Packet req, Subject clientSubject, Subject serviceSubject) 
        throws XWSSecurityException {
        ProcessingContext ctx = initializeInboundProcessingContext(req);
        ctx.setExtraneousProperty(ctx.OPERATION_RESOLVER, 
                new PolicyResolverImpl(inMessagePolicyMap,inProtocolPM,cachedOperation(req),pipeConfig,addVer,true));
        Message msg = req.getMessage();
        
        try{   
            if(!optimized) {
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
            }else{
                msg = verifyInboundMessage(msg, ctx);
            }
        } catch (XWSSecurityException xwse) {
            log.log(Level.SEVERE, 
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), xwse);
            throw new WebServiceException(
                LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), 
                    getSOAPFaultException(xwse));            
        }catch(SOAPException se){
            log.log(Level.SEVERE, 
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), se);            
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), se);            
        }
        
        //set the verified message back into the packet
        req.setMessage(msg);
        return req;
    }

    
    protected SOAPMessage secureOutboundMessage(SOAPMessage message, ProcessingContext ctx){
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
    
    protected Message secureOutboundMessage(Message message, ProcessingContext ctx){
        try{
            JAXBFilterProcessingContext  context = (JAXBFilterProcessingContext)ctx;
            context.setSOAPVersion(soapVersion);
            context.setJAXWSMessage(message, soapVersion);
            SecurityAnnotator.secureMessage(context);
            return context.getJAXWSMessage();
        } catch(XWSSecurityException xwse){            
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
    
    protected Message verifyInboundMessage(Message message, ProcessingContext ctx) throws XWSSecurityException{
        JAXBFilterProcessingContext  context = (JAXBFilterProcessingContext)ctx;
        //  context.setJAXWSMessage(message, soapVersion);
         if(debug){
            try {
                ((LazyStreamBasedMessage)message).print();
            } catch (XMLStreamException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0003_PROBLEM_PRINTING_MSG(), ex);
                throw new XWSSecurityException(LogStringsMessages.WSITPVD_0003_PROBLEM_PRINTING_MSG(), ex);                
            }
        }
        com.sun.xml.ws.security.opt.impl.incoming.SecurityRecipient recipient =
                new com.sun.xml.ws.security.opt.impl.incoming.SecurityRecipient(((LazyStreamBasedMessage)message).readMessage(),soapVersion);
        
        return recipient.validateMessage(context);
    }
        
    
     protected SecurityPolicyHolder addOutgoingMP(WSDLBoundOperation operation,Policy policy)throws PolicyException{
        
        SecurityPolicyHolder sph = constructPolicyHolder(policy,false,false);
        outMessagePolicyMap.put(operation,sph);
        return sph;
    }
    
    protected SecurityPolicyHolder addIncomingMP(WSDLBoundOperation operation,Policy policy)throws PolicyException{

        SecurityPolicyHolder sph = constructPolicyHolder(policy,false,true);
        inMessagePolicyMap.put(operation,sph);
        return sph;
    }
    
    protected void addIncomingProtocolPolicy(Policy effectivePolicy,String protocol)throws PolicyException{
        inProtocolPM.put(protocol,constructPolicyHolder(effectivePolicy,false,true,true));
    }
    
    protected void addOutgoingProtocolPolicy(Policy effectivePolicy,String protocol)throws PolicyException{
        outProtocolPM.put(protocol,constructPolicyHolder(effectivePolicy,false,false,true));
    }
    
    protected void addIncomingFaultPolicy(Policy effectivePolicy,SecurityPolicyHolder sph,WSDLFault fault)throws PolicyException{
        SecurityPolicyHolder faultPH = constructPolicyHolder(effectivePolicy,false,true);
        sph.addFaultPolicy(fault,faultPH);
    }
    
    protected void addOutgoingFaultPolicy(Policy effectivePolicy,SecurityPolicyHolder sph,WSDLFault fault)throws PolicyException{
        SecurityPolicyHolder faultPH = constructPolicyHolder(effectivePolicy,false,false);
        sph.addFaultPolicy(fault,faultPH);
    }
    
    protected String getAction(WSDLOperation operation,boolean inComming){
        if(!inComming){
            return operation.getInput().getAction();
        }else{
            return operation.getOutput().getAction();
        }
    }
   
    public JAXBElement startSecureConversation(Packet packet)
    throws WSSecureConversationException {
        
        List toks =getOutBoundSCP(packet.getMessage());
        if (toks.isEmpty()) {
            log.log(Level.SEVERE, 
                    LogStringsMessages.WSITPVD_0030_NO_POLICY_FOUND_FOR_SC());
            throw new WSSecureConversationException(
                    LogStringsMessages.WSITPVD_0030_NO_POLICY_FOUND_FOR_SC());            
        }
        //Note: Assuming only one SC assertion
        Token tok = (Token)toks.get(0);
        IssuedTokenContext ctx =
                (IssuedTokenContext)issuedTokenContextMap.get(tok.getTokenId());
        
        if (ctx == null) {
            
            //create RST for Issue
            RequestSecurityToken rst = scPlugin.createIssueRequest((PolicyAssertion)tok);
            Packet requestPacket = scPlugin.createIssuePacket((PolicyAssertion)tok, rst, pipeConfig.getWSDLModel(), pipeConfig.getBinding(),
                    jaxbContext, packet.endpointAddress.toString(), packet);
            
            try {            
                Packet secureRequestPacket = secureRequest(requestPacket, null, true);
                Packet responsePacket = nextPipe.process(secureRequestPacket);
                Packet validatedResponsePacket = validateResponse(responsePacket, null, null);

                RequestSecurityTokenResponse  rstr = scPlugin.getRSTR(jaxbContext, validatedResponsePacket);
                ctx = new IssuedTokenContextImpl();
                ctx = scPlugin.processRSTR(ctx,rst, rstr,packet.endpointAddress.toString());

                issuedTokenContextMap.put(((Token)tok).getTokenId(), ctx);
            } catch (XWSSecurityException e) {
                log.log(Level.SEVERE, 
                        LogStringsMessages.WSITPVD_0036_ERROR_PROC_REQ_PACKET(), e);                
                throw new RuntimeException(
                        LogStringsMessages.WSITPVD_0036_ERROR_PROC_REQ_PACKET(), e);
            }
        }
        
        SecurityTokenReference str = (SecurityTokenReference)ctx.getUnAttachedSecurityTokenReference();
        
        return WSTrustElementFactory.newInstance().toJAXBElement(str);
    }
    
    private CallbackHandler configureClientHandler(Set configAssertions, Properties props) {
        //Properties props = new Properties();
        String ret = populateConfigProperties(configAssertions, props);
        try {
            if (ret != null) {
                Class handler = loadClass(ret);
                Object obj = handler.newInstance();
                if (!(obj instanceof CallbackHandler)) {
                    log.log(Level.SEVERE, 
                            LogStringsMessages.WSITPVD_0031_INVALID_CALLBACK_HANDLER_CLASS(ret));
                    throw new RuntimeException(
                            LogStringsMessages.WSITPVD_0031_INVALID_CALLBACK_HANDLER_CLASS(ret));
                }
                return (CallbackHandler)obj;
            }
            return new DefaultCallbackHandler("client", props);
        } catch (Exception e) {
            log.log(Level.SEVERE, 
                    LogStringsMessages.WSITPVD_0032_ERROR_CONFIGURE_CLIENT_HANDLER(), e);
            throw new RuntimeException(
                    LogStringsMessages.WSITPVD_0032_ERROR_CONFIGURE_CLIENT_HANDLER(), e);            
        }
    }
    
    private void invokeSCPlugin(Packet packet) {
        
        // get the secure conversation policies pertaining to this operation
        List<PolicyAssertion> policies = getOutBoundSCP(packet.getMessage());
        
        for (PolicyAssertion scAssertion : policies) {
            Token scToken = (Token)scAssertion;
            if (issuedTokenContextMap.get(scToken.getTokenId()) == null) {
                
                //create RST for Issue
                RequestSecurityToken rst = scPlugin.createIssueRequest((PolicyAssertion)scAssertion);
                Packet requestPacket = scPlugin.createIssuePacket((PolicyAssertion)scAssertion, rst, pipeConfig.getWSDLModel(), pipeConfig.getBinding(),
                        jaxbContext, packet.endpointAddress.toString(), packet);
                
                try {
                    
                    Packet secureRequestPacket = secureRequest(requestPacket, null, true);
                    Packet responsePacket = nextPipe.process(secureRequestPacket);
                    Packet validatedResponsePacket = validateResponse(responsePacket, null, null);
                    
                    RequestSecurityTokenResponse  rstr = scPlugin.getRSTR(jaxbContext, validatedResponsePacket);
                    IssuedTokenContext ctx = new IssuedTokenContextImpl();
                    ctx = scPlugin.processRSTR(ctx,rst, rstr,packet.endpointAddress.toString());
                    issuedTokenContextMap.put(((Token)scAssertion).getTokenId(), ctx);
                } catch (XWSSecurityException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
        private void cancelSecurityContextToken(){
        Enumeration keys = issuedTokenContextMap.keys();
        while (keys.hasMoreElements()){
            String id = (String)keys.nextElement();
            IssuedTokenContext ctx =
                    (IssuedTokenContext)issuedTokenContextMap.get(id);
            
            if (ctx.getSecurityToken() instanceof SecurityContextToken){
                
                /*ctx = scPlugin.processCancellation(
                        ctx, pipeConfig.getWSDLModel(), pipeConfig.getBinding(), this, jaxbContext, ctx.getEndpointAddress());*/
                try {
                    RequestSecurityToken rst = scPlugin.createCancelRequest(ctx);
                    Packet cancelPacket = scPlugin.createCancelPacket(
                            rst,pipeConfig.getWSDLModel(), pipeConfig.getBinding(), jaxbContext, ctx.getEndpointAddress());
                    //only for issue we pass flag true
                    Packet secCancelPacket = this.secureRequest(cancelPacket, null,false);
                    
                    Packet response = nextPipe.process(secCancelPacket);
                    Packet cancelResponse = this.validateResponse(response, null,null);
                    RequestSecurityTokenResponse rstr = scPlugin.getRSTR(jaxbContext, cancelResponse);
                    ctx = scPlugin.processRSTR(ctx,rst,rstr,ctx.getEndpointAddress());
                    
                    issuedTokenContextMap.remove(id);
                } catch (XWSSecurityException ex) {
                    log.log(Level.SEVERE, 
                        LogStringsMessages.WSITPVD_0049_ERROR_CANCEL_SECURITY_CONTEXT_TOKEN(), ex);
                    throw new WebServiceException(
                        LogStringsMessages.WSITPVD_0049_ERROR_CANCEL_SECURITY_CONTEXT_TOKEN(), 
                            getSOAPFaultException(ex));                                                    
                }
            }
        }
    }

    private void invokeTrustPlugin(Packet packet, boolean isSCMessage) {
        
        List<PolicyAssertion> policies = null;
        
        if (isSCMessage) {
            Token scToken = (Token)packet.invocationProperties.get(SC_ASSERTION);
            policies =  getIssuedTokenPoliciesFromBootstrapPolicy(scToken);
        } else {
            policies = getIssuedTokenPolicies(packet, OPERATION_SCOPE);
        }
        
        PolicyAssertion preSetSTSAssertion = null;
        URI stsEP = null;
        URI wsdlLocation = null;
        QName serviceName = null;
        QName portName = null;
        if(trustConfig != null){
            Iterator it = trustConfig.iterator();
            while(it!=null && it.hasNext()) {
                preSetSTSAssertion = (PolicyAssertion)it.next();
            }
            //serviceName = (QName)packet.invocationProperties.get(WSTrustConstants.PROPERTY_SERVICE_NAME);
            //portName = (QName)packet.invocationProperties.get(WSTrustConstants.PROPERTY_PORT_NAME);
        }
        
        for (PolicyAssertion issuedTokenAssertion : policies) {
            IssuedTokenContext ctx = trustPlugin.process(issuedTokenAssertion, preSetSTSAssertion, packet.endpointAddress.toString());
            issuedTokenContextMap.put(
                    ((Token)issuedTokenAssertion).getTokenId(), ctx);
        }
    }
    
    // returns a list of IssuedTokenPolicy Assertions contained in the
    // service policy
    protected List<PolicyAssertion> getIssuedTokenPolicies(Packet packet, String scope) {
        if (outMessagePolicyMap == null) {
            return new ArrayList<PolicyAssertion>();
        }
        
        WSDLBoundOperation operation = null;
        if(isTrustMessage(packet)){
            operation = getWSDLOpFromAction(packet,false);
        }else{
            operation =getOperation(packet.getMessage(), packet);
        }
        
        SecurityPolicyHolder sph =(SecurityPolicyHolder) outMessagePolicyMap.get(operation);
        if(sph == null){
            return EMPTY_LIST;
        }
        return sph.getIssuedTokens();
    }
      
}
