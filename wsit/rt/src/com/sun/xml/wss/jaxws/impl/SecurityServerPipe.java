/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.wss.jaxws.impl;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.security.impl.policy.Constants;

import com.sun.xml.ws.api.message.stream.InputStreamMessage;
import com.sun.xml.ws.api.message.stream.XMLStreamReaderMessage;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.assembler.ServerPipeConfiguration;
import com.sun.xml.ws.runtime.util.Session;
import com.sun.xml.ws.runtime.util.SessionManager;
import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyHolder;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.trust.WSTrustConstants;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.ProcessingContextImpl;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.WssSoapFaultException;

import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;

import javax.security.auth.callback.CallbackHandler;

import java.util.Properties;
import java.util.Iterator;
import java.util.Set;

import java.net.URI;
import com.sun.xml.ws.security.policy.Token;

import com.sun.xml.ws.security.secconv.WSSCContract;
import com.sun.xml.ws.security.secconv.WSSCConstants;
import com.sun.xml.ws.security.secconv.WSSCElementFactory;
import com.sun.xml.ws.security.secconv.WSSCFactory;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.wss.impl.NewSecurityRecipient;

import com.sun.xml.wss.impl.misc.DefaultCallbackHandler;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.sun.xml.wss.jaxws.impl.Constants.SC_ASSERTION;
import static com.sun.xml.wss.jaxws.impl.Constants.OPERATION_SCOPE;
import static com.sun.xml.wss.jaxws.impl.Constants.EMPTY_LIST;
import static com.sun.xml.wss.jaxws.impl.Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS;
import static com.sun.xml.wss.jaxws.impl.Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS;


//TODO: add logging before 4/13

/**
 * @author K.Venugopal@sun.com
 * @author Vbkumar.Jayanti@Sun.COM
 */
public class SecurityServerPipe extends SecurityPipeBase {
    
    private SessionManager sessionManager =
                            SessionManager.getSessionManager();
    private WSDLBoundOperation cachedOperation = null;
    private Set trustConfig = null;
    private CallbackHandler handler = null;
    
    // Creates a new instance of SecurityServerPipe
    public SecurityServerPipe(ServerPipeConfiguration config,Pipe nextPipe) {
        super(config,nextPipe);
        
        try {
            Iterator it = inMessagePolicyMap.values().iterator();
            SecurityPolicyHolder holder = (SecurityPolicyHolder)it.next();
            Set configAssertions = holder.getConfigAssertions(SUN_WSS_SECURITY_SERVER_POLICY_NS);
            trustConfig = holder.getConfigAssertions(Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS);
            /*
            if (configAssertions == null || configAssertions.isEmpty()) {
                throw new RuntimeException("Null or Empty Config WSDL encountered");
            }*/
            handler = configureServerHandler(configAssertions);
            secEnv = new DefaultSecurityEnvironmentImpl(handler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    // copy constructor
    protected SecurityServerPipe(SecurityServerPipe that) {
        super(that);
        sessionManager = that.sessionManager;
        trustConfig = that.trustConfig;
        handler = that.handler;
    }
    
    
    //Note: There is an Assumption that the STS is distinct from the WebService in case of
    // WS-Trust and the STS and WebService are the same entity for SecureConversation
    public Packet process(Packet packet) {
        
        if (!optimized) {
            cacheMessage(packet);
        }
        
        Message msg = packet.getMessage();
        
        boolean isSCIssueMessage = false;
        boolean isSCCancelMessage = false;
        boolean isTrustMessage = false;
        String msgId = null;
        String action = null;
        
        boolean thereWasAFault = false;
        
        //Do Security Processing for Incoming Message
        //---------------INBOUND SECURITY VERIFICATION----------
        ProcessingContext ctx = initializeInboundProcessingContext(packet/*, isSCIssueMessage, isTrustMessage*/);
        ctx.setExtraneousProperty(ctx.OPERATION_RESOLVER, new PolicyResolverImpl(inMessagePolicyMap,inProtocolPM,cachedOperation,pipeConfig,addVer,false));
        try{
            if(!optimized) {
                SOAPMessage soapMessage = msg.readAsSOAPMessage();
                soapMessage = verifyInboundMessage(soapMessage, ctx);
                msg = Messages.create(soapMessage);
            }else{
                msg = verifyInboundMessage(msg, ctx);
            }
        } catch (WssSoapFaultException ex) {
            thereWasAFault = true;
            ex.printStackTrace();
            msg = Messages.create(ex, pipeConfig.getBinding().getSOAPVersion());
        } catch (XWSSecurityException xwse) {
            thereWasAFault = true;
            xwse.printStackTrace();
            msg = Messages.create(xwse, pipeConfig.getBinding().getSOAPVersion());
            
        } catch(SOAPException se){
            // internal error
            throw new WebServiceException(se);
        }
        packet.setMessage(msg);
        
        if (isAddressingEnabled()) {
            action = getAction(packet);
            if (WSSCConstants.REQUEST_SECURITY_CONTEXT_TOKEN_ACTION.equals(action)) {
                isSCIssueMessage = true;
            } else if (WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_ACTION.equals(action)) {
                isSCCancelMessage = true;
            } else if (WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION.equals(action)) {
                isTrustMessage = true;
                packet.getMessage().getHeaders().getTo(addVer, pipeConfig.getBinding().getSOAPVersion());
                
                if(trustConfig != null){
                    packet.invocationProperties.put(Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS,trustConfig.iterator());
                    //packet.getHandlerScopePropertyNames(false).add(Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS);
                }
                
                //set the callbackhandler
                packet.invocationProperties.put(WSTrustConstants.STS_CALL_BACK_HANDLER, handler);
                // packet.getHandlerScopePropertyNames(false).add(WSTrustConstants.STS_CALL_BACK_HANDLER);
            }
            
            if (isSCIssueMessage){
                List<PolicyAssertion> policies = getInBoundSCP(packet.getMessage());
                if(!policies.isEmpty()) {
                    packet.invocationProperties.put(SC_ASSERTION, (PolicyAssertion)policies.get(0));
                }
            }
        }
        
        if(!isSCIssueMessage ){
            cachedOperation = msg.getOperation(pipeConfig.getWSDLModel());
        }
        
        Packet retPacket = null;
        
        if (thereWasAFault) {
            retPacket = packet;
        } else {
            
            if (isSCIssueMessage || isSCCancelMessage) {
                //-------put application message on hold and invoke SC contract--------
                
                retPacket = invokeSecureConversationContract(
                        packet, ctx, isSCIssueMessage, action);
                
            } else {
                //--------INVOKE NEXT PIPE------------
                // Put the addressing headers as unread
                // packet.invocationProperties.put(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND, null);
                
                if (nextPipe != null) {
                    retPacket = nextPipe.process(packet);
                    
                    // Add addrsssing headers to trust message
                    if (isTrustMessage){
                        retPacket = addAddressingHeaders(packet, retPacket.getMessage(), WSTrustConstants.REQUEST_SECURITY_TOKEN_RESPONSE_ISSUE_ACTION);
                    }
                }else {
                    retPacket = packet;
                }
                
               /* // Add addrsssing headers to trust message
                if (isTrustMessage){
                    System.out.println("****It is a trust message. Adding addressing herder to it");
                    Packet newRetPacket = packet.createServerResponse(retPacket.getMessage(), addVer, pipeConfig.getBinding().getSOAPVersion(), WSTrustConstants.REQUEST_SECURITY_TOKEN_RESPONSE_ISSUE_ACTION);
                    newRetPacket.proxy = retPacket.proxy;
                    newRetPacket.invocationProperties.putAll(retPacket.invocationProperties);
                
                    retPacket = newRetPacket;
                }*/
            }
        }
        
        if(retPacket.getMessage() == null){
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
        ctx = initializeOutgoingProcessingContext(retPacket, isSCIssueMessage, isTrustMessage /*, thereWasAFault*/);
        
        try{
            msg = retPacket.getMessage();
            if (ctx.getSecurityPolicy() != null) {
                if(!optimized) {
                    SOAPMessage soapMessage = msg.readAsSOAPMessage();
                    soapMessage = secureOutboundMessage(soapMessage, ctx);
                    msg = Messages.create(soapMessage);
                }else{
                    msg = secureOutboundMessage(msg, ctx);
                }
            }
        } catch (WssSoapFaultException ex) {
            msg = Messages.create(getSOAPFault(ex));
        } catch(SOAPException se) {
            // internal error
            throw new WebServiceException(se);
        } finally{
            if (isSCCancel(retPacket)){
                removeContext(packet);
            }
        }
        resetCachedOperation();
        retPacket.setMessage(msg);
        return retPacket;
        
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
    
    public void preDestroy() {
        if (nextPipe != null) {
            nextPipe.preDestroy();
        }
        issuedTokenContextMap.clear();
    }
    
    public Pipe copy(PipeCloner cloner) {
        Pipe clonedNextPipe = null;
        if (nextPipe != null) {
            clonedNextPipe = cloner.copy(nextPipe);
        }
        Pipe copied = new SecurityServerPipe(this);
        ((SecurityServerPipe)copied).setNextPipe(clonedNextPipe);
        cloner.add(this, copied);
        return copied;
    }
    
    public Packet processMessage(XMLStreamReaderMessage msg) {
        //TODO:Optimized security
        throw new UnsupportedOperationException();
    }
    
    public InputStreamMessage processInputStream(XMLStreamReaderMessage msg) {
        //TODO:Optimized security
        throw new UnsupportedOperationException();
    }
    
    public InputStreamMessage processInputStream(Message msg) {
        //TODO:Optimized security
        throw new UnsupportedOperationException();
    }
    
    /*public ServerEdgePipe.RequestResponseTypes getRequestResponseType() {
        //TODO:Optimized security
        throw new UnsupportedOperationException();
    }*/
    
    
//    protected ProcessingContext initializeInboundProcessingContext(
//            Packet packet /*, boolean isSCMessage, boolean isTrustMessage*/) {
//
//        ProcessingContextImpl ctx =
//                (ProcessingContextImpl)initializeInboundProcessingContext(packet /*, isSCMessage*/);
//
//
////        if (isTrustMessage /*|| isSCMessage*/) {
////            // this is an RST to the STS
////            // Security runtime would populate received client creds into it
////            // for use by the STS (for TRUST/SC)
////            IssuedTokenContext trustCredHolder = new IssuedTokenContextImpl();
////            ctx.setTrustCredentialHolder(trustCredHolder);
////        }
//        return ctx;
//    }
    
    protected ProcessingContext initializeOutgoingProcessingContext(
            Packet packet, boolean isSCMessage, boolean isTrustMessage /*, boolean thereWasAFault*/) {
        ProcessingContext ctx = initializeOutgoingProcessingContext(packet, isSCMessage/*, thereWasAFault*/);
        return ctx;
    }
    
    protected ProcessingContext initializeOutgoingProcessingContext(
            Packet packet, boolean isSCMessage /*, boolean thereWasAFault*/) {
        
        ProcessingContextImpl ctx = null;
        if(optimized){
            ctx = new JAXBFilterProcessingContext(packet.invocationProperties);
        }else{
            ctx = new ProcessingContextImpl( packet.invocationProperties);
        }
        // set the policy, issued-token-map, and extraneous properties
        ctx.setIssuedTokenContextMap(issuedTokenContextMap);
        ctx.setAlgorithmSuite(getAlgoSuite(getBindingAlgorithmSuite(packet)));
        try {
            MessagePolicy policy = null;
            if (packet.getMessage().isFault()) {
                policy =  getOutgoingFaultPolicy(packet);
            } else if (isRMMessage(packet)) {
                SecurityPolicyHolder holder = outProtocolPM.get("RM");
                policy = holder.getMessagePolicy();
            } else if(isSCCancel(packet)){
                SecurityPolicyHolder holder = outProtocolPM.get("SC");
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
            ctx.setSecurityEnvironment(secEnv);
            ctx.isInboundMessage(false);
        } catch (XWSSecurityException e) {
            throw new RuntimeException(e);
        }
        return ctx;
    }
    
    protected MessagePolicy getOutgoingXWSSecurityPolicy(
            Packet packet, boolean isSCMessage) {
        if (isSCMessage) {
            Token scToken = (Token)packet.invocationProperties.get(SC_ASSERTION);
            return getOutgoingXWSBootstrapPolicy(scToken);
        }
        Message message = packet.getMessage();
        
        MessagePolicy mp = null;
        if(cachedOperation == null){
            //Body could be encrypted. Security will have to infer the
            //policy from the message till the Body is decrypted.
            mp = emptyMessagePolicy;
        }
        if (outMessagePolicyMap == null) {
            //empty message policy
            return new MessagePolicy();
        }
        
        if(isTrustMessage(packet)){
            cachedOperation = getWSDLOpFromAction(packet,false);
        }
        
        SecurityPolicyHolder sph = (SecurityPolicyHolder) outMessagePolicyMap.get(cachedOperation);
        if(sph == null){
            return new MessagePolicy();
        }
        mp = sph.getMessagePolicy();
        return mp;
    }
    
    protected MessagePolicy getOutgoingFaultPolicy(Packet packet) {
        if(!optimized){
            if(cachedOperation != null){
                WSDLOperation operation = cachedOperation.getOperation();
                try{
                    SOAPBody body = packet.getMessage().readAsSOAPMessage().getSOAPBody();
                    NodeList nodes = body.getElementsByTagName("detail");
                    if(nodes.getLength() == 0){
                        nodes = body.getElementsByTagNameNS(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE,"Detail");
                    }
                    if(nodes.getLength() >0){
                        Node node = nodes.item(0);
                        Node faultNode = node.getFirstChild();
                        if(faultNode == null){
                            return null;
                        }
                        String uri = faultNode.getNamespaceURI();
                        QName faultDetail = null;
                        if(uri != null && uri.length() >0){
                            faultDetail = new QName(faultNode.getNamespaceURI(),faultNode.getLocalName());
                        }else{
                            faultDetail = new QName(faultNode.getNodeName());
                        }
                        WSDLFault fault = operation.getFault(faultDetail);
                        SecurityPolicyHolder sph = outMessagePolicyMap.get(cachedOperation);
                        SecurityPolicyHolder faultPolicyHolder = sph.getFaultPolicy(fault);
                        MessagePolicy faultPolicy = (faultPolicyHolder == null) ? new MessagePolicy() : faultPolicyHolder.getMessagePolicy();
                        return faultPolicy;
                        
                    }
                }catch(SOAPException sx){
                    sx.printStackTrace();
                    //log error
                }
            }
            return null;
        }else{
            throw new UnsupportedOperationException("Optimized path not supported");
        }
    }
    
    
    
    
    protected SOAPMessage verifyInboundMessage(SOAPMessage message, ProcessingContext ctx)
    throws WssSoapFaultException, XWSSecurityException {
        ctx.setSOAPMessage(message);
        NewSecurityRecipient.validateMessage(ctx);
        return ctx.getSOAPMessage();
    }
    
    // The packet has the Message with RST/SCT inside it
    // TODO: Need to inspect if it is really a Issue or a Cancel
    private Packet invokeSecureConversationContract(
            Packet packet, ProcessingContext ctx, boolean isSCTIssue, String action) {
        
        IssuedTokenContext ictx = new IssuedTokenContextImpl();
        Message msg = packet.getMessage();
        Message retMsg = null;
        String retAction = null;
        
        try {
            WSSCElementFactory eleFac = WSSCElementFactory.newInstance();
            JAXBElement rstEle = msg.readPayloadAsJAXB(jaxbContext.createUnmarshaller());
            RequestSecurityToken rst = eleFac.createRSTFrom(rstEle);
            URI requestType = rst.getRequestType();
            RequestSecurityTokenResponse rstr = null;
            WSSCContract scContract = WSSCFactory.newWSSCContract(null);
            if (requestType.toString().equals(WSTrustConstants.ISSUE_REQUEST)) {
                List<PolicyAssertion> policies = getOutBoundSCP(packet.getMessage());
                rstr =  scContract.issue(rst, ictx, (SecureConversationToken)policies.get(0));
                retAction = WSSCConstants.REQUEST_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION;
                SecurityContextToken sct = (SecurityContextToken)ictx.getSecurityToken();
                String sctId = sct.getIdentifier().toString();
                
                Session session = sessionManager.getSession(sctId);
                if (session == null) {
                    throw new WSSecureConversationException("Session could not be " + "" +
                            "created successfully while issuing");
                }
                
                // Put it here for RM to pick up
                packet.invocationProperties.put(
                        Session.SESSION_ID_KEY, sctId);
                
                packet.invocationProperties.put(
                        Session.SESSION_KEY, session.getUserData());
                
                IssuedTokenContext itctx = session.getSecurityInfo().getIssuedTokenContext();
                ((ProcessingContextImpl)ctx).getIssuedTokenContextMap().put(sctId, itctx);
                
            } else if (requestType.toString().equals(WSTrustConstants.CANCEL_REQUEST)) {
                retAction = WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION;
                rstr =  scContract.cancel(rst, ictx, issuedTokenContextMap);
            } else {
                throw new UnsupportedOperationException(
                        "RequestType :" + requestType + " not supported");
            }
            
            // construct the complete message here containing the RSTR and the
            // correct Action headers if any and return the message.
            retMsg = Messages.create(jaxbContext.createMarshaller(), eleFac.toJAXBElement(rstr), soapVersion);
        } catch (javax.xml.bind.JAXBException ex) {
            throw new RuntimeException(ex);
        } catch (WSSecureConversationException ex){
            throw new RuntimeException(ex);
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
    
    public InputStreamMessage processInputStream(Packet packet) {
        //TODO:Optimized security
        throw new UnsupportedOperationException("Will be supported for optimized path");
    }
    
    /** private Packet addAddressingHeaders(Packet packet, String relatesTo, String action){
     * AddressingBuilder builder = AddressingBuilder.newInstance();
     * AddressingProperties ap = builder.newAddressingProperties();
     *
     * try{
     * // Action
     * ap.setAction(builder.newURI(new URI(action)));
     *
     * // RelatesTo
     * Relationship[] rs = new Relationship[]{builder.newRelationship(new URI(relatesTo))};
     * ap.setRelatesTo(rs);
     *
     * // To
     * ap.setTo(builder.newURI(new URI(builder.newAddressingConstants().getAnonymousURI())));
     *
     * } catch (URISyntaxException e) {
     * throw new RuntimeException("Exception when adding Addressing Headers");
     * }
     *
     * WsaRuntimeFactory fac = WsaRuntimeFactory.newInstance(ap.getNamespaceURI(), pipeConfig.getWSDLModel(), pipeConfig.getBinding());
     * fac.writeHeaders(packet, ap);
     * packet.invocationProperties
     * .put(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND, ap);
     *
     * return packet;
     * }*/
    
    protected SecurityPolicyHolder addOutgoingMP(WSDLBoundOperation operation,Policy policy)throws PolicyException{
        
//        XWSSPolicyGenerator xwssPolicyGenerator = new XWSSPolicyGenerator(policy,true,true);
//        xwssPolicyGenerator.process();
//        MessagePolicy messagePolicy = xwssPolicyGenerator.getXWSSPolicy();
//        SecurityPolicyHolder sph = new SecurityPolicyHolder();
//        sph.setMessagePolicy(messagePolicy);
//        sph.setBindingLevelAlgSuite(xwssPolicyGenerator.getBindingLevelAlgSuite());
//        this.bindingLevelAlgSuite = xwssPolicyGenerator.getBindingLevelAlgSuite();
//        List<PolicyAssertion> tokenList = getTokens(policy);
//        for(PolicyAssertion token:tokenList){
//            if(PolicyUtil.isSecureConversationToken(token)){
//                NestedPolicy bootstrapPolicy = ((SecureConversationToken)token).getBootstrapPolicy();
//                Policy effectiveBP = getEffectiveBootstrapPolicy(bootstrapPolicy);
//                xwssPolicyGenerator = new XWSSPolicyGenerator(effectiveBP,true,true);
//                xwssPolicyGenerator.process();
//                MessagePolicy bmp = xwssPolicyGenerator.getXWSSPolicy();
//                PolicyAssertion sct = new SCTokenWrapper(token,bmp);
//                sph.addSecureConversationToken(sct);
//            }else if(PolicyUtil.isIssuedToken(token)){
//                sph.addIssuedToken(token);
//            }
//        }
//
        SecurityPolicyHolder sph = constructPolicyHolder(policy,true,true);
        inMessagePolicyMap.put(operation,sph);
        return sph;
    }
    
    protected SecurityPolicyHolder addIncomingMP(WSDLBoundOperation operation,Policy policy)throws PolicyException{
//
//        XWSSPolicyGenerator xwssPolicyGenerator = new XWSSPolicyGenerator(policy,true,false);
//        xwssPolicyGenerator.process();
//        MessagePolicy messagePolicy = xwssPolicyGenerator.getXWSSPolicy();
//        SecurityPolicyHolder sph = new SecurityPolicyHolder();
//        sph.setMessagePolicy(messagePolicy);
//        sph.setBindingLevelAlgSuite(xwssPolicyGenerator.getBindingLevelAlgSuite());
//        this.bindingLevelAlgSuite = xwssPolicyGenerator.getBindingLevelAlgSuite();
//        List<PolicyAssertion> tokenList = getTokens(policy);
//        for(PolicyAssertion token:tokenList){
//            if(PolicyUtil.isSecureConversationToken(token)){
//                NestedPolicy bootstrapPolicy = ((SecureConversationToken)token).getBootstrapPolicy();
//                Policy effectiveBP = getEffectiveBootstrapPolicy(bootstrapPolicy);
//                xwssPolicyGenerator = new XWSSPolicyGenerator(effectiveBP,true,false);
//                xwssPolicyGenerator.process();
//                MessagePolicy bmp = xwssPolicyGenerator.getXWSSPolicy();
//                PolicyAssertion sct = new SCTokenWrapper(token,bmp);
//                sph.addSecureConversationToken(sct);
//            }else if(PolicyUtil.isIssuedToken(token)){
//                sph.addIssuedToken(token);
//            }
//        }
        SecurityPolicyHolder sph = constructPolicyHolder(policy,true,false);
        outMessagePolicyMap.put(operation,sph);
        return sph;
    }
    
    protected void addIncomingProtocolPolicy(Policy effectivePolicy,String protocol)throws PolicyException{
        outProtocolPM.put(protocol,constructPolicyHolder(effectivePolicy,true,false,true));
    }
    
    protected void addOutgoingProtocolPolicy(Policy effectivePolicy,String protocol)throws PolicyException{
        inProtocolPM.put(protocol,constructPolicyHolder(effectivePolicy,true,true,true));
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
    
    private Packet addAddressingHeaders(Packet packet, Message retMsg, String action){
        Packet retPacket = packet.createServerResponse(retMsg, addVer, soapVersion, action);
        
        retPacket.proxy = packet.proxy;
        retPacket.invocationProperties.putAll(packet.invocationProperties);
        
        return retPacket;
    }
    
   /* protected boolean isRMMessage(Packet packet){
        //TODO: For incoming messages we need to look at the
        //Action header
        AddressingProperties ap = (AddressingProperties)packet.invocationProperties
                .get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
    
        if (ap != null) {
            AttributedURI uri = ap.getAction();
            if (uri != null) {
                String action = uri.toString();
                if (RM_CREATE_SEQ.equals(action) || RM_CREATE_SEQ_RESP.equals(action)
                || RM_SEQ_ACK.equals(action) || RM_TERMINATE_SEQ.equals(action)
                || RM_LAST_MESSAGE.equals(action)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    protected boolean isSCCancel(Packet packet){
        AddressingProperties ap = (AddressingProperties)packet.invocationProperties
                  .get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
        if (ap != null) {
            AttributedURI uri = ap.getAction();
            if (uri != null){
                String uriValue = uri.toString();
                if(WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION.equals(uriValue) ||
                      WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_ACTION .equals(uriValue)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    protected boolean isTrustMessage(Packet packet){
        AddressingProperties ap = (AddressingProperties)packet.invocationProperties
                .get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
        if (ap != null) {
            AttributedURI uri = ap.getAction();
            if (uri != null){
                String uriValue = uri.toString();
                if(WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION.equals(uriValue) ||
                        WSTrustConstants.REQUEST_SECURITY_TOKEN_RESPONSE_ISSUE_ACTION.equals(uriValue)){
                    return true;
                }
            }
        }
        return false;
    
    }*/
    
    /**  protected AttributedURI getAction(Packet packet ){
     * AddressingProperties ap = (AddressingProperties)packet.invocationProperties
     * .get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
     * if (ap != null) {
     * AttributedURI uri = ap.getAction();
     *
     * return uri;
     * }
     * return null;
     * }*/
    
    private CallbackHandler configureServerHandler(Set configAssertions) {
        Properties props = new Properties();
        String ret = populateConfigProperties(configAssertions, props);
        try {
            if (ret != null) {
                Class handler = loadClass(ret);
                Object obj = handler.newInstance();
                if (!(obj instanceof CallbackHandler)) {
                    throw new RuntimeException("The specified CallbackHandler class, " +
                            ret + ", Is not a valid CallbackHandler");
                }
                return (CallbackHandler)obj;
            }
            return new DefaultCallbackHandler("server", props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected Policy getWSITConfig(){
//        if(wsitConfig == null){
//            try{
//                PolicySourceModel model =  unmarshalPolicy("wsit-server.xml");
//                if(model != null){
//                    wsitConfig =  PolicyModelTranslator.getTranslator().translate(model);
//                }
//            }catch(PolicyException ex){
//                ex.printStackTrace();
//            }catch(IOException ex){
//                ex.printStackTrace();
//            }
//        }
        return wsitConfig;
    }
    
}
