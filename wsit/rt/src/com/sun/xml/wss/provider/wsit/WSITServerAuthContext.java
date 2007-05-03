/*
 * WSITServerAuthContext.java
 *
 * Created on November 1, 2006, 12:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.message.stream.LazyStreamBasedMessage;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.runtime.util.Session;
import com.sun.xml.ws.runtime.util.SessionManager;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyHolder;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.secconv.WSSCConstants;
import com.sun.xml.ws.security.secconv.WSSCContract;
import com.sun.xml.ws.security.secconv.WSSCElementFactory;
import com.sun.xml.ws.security.secconv.WSSCFactory;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.RealmAuthenticationAdapter;
import com.sun.xml.wss.SubjectAccessor;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.NewSecurityRecipient;
import com.sun.xml.wss.impl.ProcessingContextImpl;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.misc.DefaultCallbackHandler;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.misc.WSITProviderSecurityEnvironment;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.jaxws.impl.Constants;
import com.sun.xml.wss.jaxws.impl.PolicyResolverImpl;
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
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.sun.xml.wss.jaxws.impl.Constants.SC_ASSERTION;
import static com.sun.xml.wss.jaxws.impl.Constants.OPERATION_SCOPE;
import static com.sun.xml.wss.jaxws.impl.Constants.EMPTY_LIST;
import static com.sun.xml.wss.jaxws.impl.Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS;
import static com.sun.xml.wss.jaxws.impl.Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS;
import java.net.URI;
import javax.xml.bind.JAXBElement;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.provider.wsit.logging.LogDomainConstants;
import com.sun.xml.wss.provider.wsit.logging.LogStringsMessages;

/**
 *
 * @author kumar jayanti
 */
public class WSITServerAuthContext extends WSITAuthContextBase implements ServerAuthContext {
    
    protected static final String TRUE="true";
    //****************Class Variables***************
    private SessionManager sessionManager= 
                SessionManager.getSessionManager();

    
    //******************Instance Variables*********
    private Set trustConfig = null;
    private CallbackHandler handler = null;
    
    //****************Variables passed to Context CTOR********
    String operation = null;
    //Subject subject = null; 
    //Map map = null;
    WSEndpoint endPoint =  null;
    
    //***************AuthModule Instance**********
    WSITServerAuthModule  authModule = null;
   
    
    /** Creates a new instance of WSITServerAuthContext */
    public WSITServerAuthContext(String operation, Subject subject, Map map) {
        super(map);
        this.operation = operation;
        //this.subject = subject;
        //this.map = map;
        endPoint = (WSEndpoint)map.get("ENDPOINT");                
        
        Iterator it = inMessagePolicyMap.values().iterator();
        SecurityPolicyHolder holder = (SecurityPolicyHolder)it.next();
        Set configAssertions = holder.getConfigAssertions(Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS);
        trustConfig = holder.getConfigAssertions(
                com.sun.xml.ws.security.impl.policy.Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS);
        
        String isGF = System.getProperty("com.sun.aas.installRoot");
        if (isGF != null) {
            handler = loadGFHandler(false);
            try {
                Properties props = new Properties();
                populateConfigProperties(configAssertions, props);
                secEnv = new WSITProviderSecurityEnvironment(handler, map, props);
            }catch (XWSSecurityException ex) {
                log.log(Level.SEVERE, 
                        LogStringsMessages.WSITPVD_0048_ERROR_POPULATING_SERVER_CONFIG_PROP(), ex);
                throw new WebServiceException(
                        LogStringsMessages.WSITPVD_0048_ERROR_POPULATING_SERVER_CONFIG_PROP(), ex);                  
            }
        } else {
            //This will handle Non-GF containers where no config assertions
            // are required in the WSDL. Ex. UsernamePassword validatio
            // with Default Realm Authentication
            handler = configureServerHandler(configAssertions);
            secEnv = new DefaultSecurityEnvironmentImpl(handler);
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
        
        if (!optimized) {
            cacheMessage(packet);
        }
        
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
        // Add addrsssing headers to trust message
        String iTM = (String)messageInfo.getMap().get("IS_TRUST_MESSAGE");
        boolean isTrustMessage = (iTM != null) ? true : false;
        //TODO: replace this with correct method, i believe we can update the reqPacket into MessageInfo
        Packet packet = (Packet)messageInfo.getMap().get("VALIDATE_REQ_PACKET");
        
        //TODO: this is the one that came from nextPipe.process
        //TODO: replace this with call to packetMessageInfo.getResponsePacket
        Packet retPacket = getResponsePacket(messageInfo);
        if (isTrustMessage){
            retPacket = addAddressingHeaders(packet, retPacket.getMessage(), WSTrustConstants.REQUEST_SECURITY_TOKEN_RESPONSE_ISSUE_ACTION);
        }
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
    }
    
    @SuppressWarnings("unchecked")
    public Packet validateRequest(Packet packet, Subject clientSubject, Subject serviceSubject, Map sharedState) 
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
        ctx.setExtraneousProperty(ctx.OPERATION_RESOLVER, 
                new PolicyResolverImpl(inMessagePolicyMap,inProtocolPM,cachedOperation(packet),pipeConfig,addVer,false));
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
            msg = Messages.create(ex, pipeConfig.getBinding().getSOAPVersion());
        } catch (XWSSecurityException xwse) {
            thereWasAFault = true;            
            msg = Messages.create(xwse, pipeConfig.getBinding().getSOAPVersion());
         
        }  catch (WebServiceException xwse) {
            thereWasAFault = true;            
            msg = Messages.create(xwse, pipeConfig.getBinding().getSOAPVersion());
            
        } catch(SOAPException se){
            // internal error
            log.log(Level.SEVERE, 
                    LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), se);
            thereWasAFault = true;            
            msg = Messages.create(se, pipeConfig.getBinding().getSOAPVersion());
            //throw new WebServiceException(
            //        LogStringsMessages.WSITPVD_0035_ERROR_VERIFY_INBOUND_MSG(), se);
        }
        packet.setMessage(msg);
        
        if (thereWasAFault) {
            sharedState.put("THERE_WAS_A_FAULT", Boolean.valueOf(thereWasAFault));
             if (this.isAddressingEnabled()) {
                Packet ret = packet.createServerResponse(
                        msg, this.addVer, this.soapVersion, this.addVer.getDefaultFaultAction());
                return ret;
             } else {
                return packet;
             }
        }
        
        if (isAddressingEnabled()) {
            action = getAction(packet);
            if (WSSCConstants.REQUEST_SECURITY_CONTEXT_TOKEN_ACTION.equals(action)) {
                isSCIssueMessage = true;
                sharedState.put("IS_SC_ISSUE", TRUE);
            } else if (WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_ACTION.equals(action)) {
                isSCCancelMessage = true;
                sharedState.put("IS_SC_CANCEL", TRUE);
            } else if (WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION.equals(action)) {
                isTrustMessage = true;
                sharedState.put("IS_TRUST_MESSAGE", TRUE);
                packet.getMessage().getHeaders().getTo(addVer, pipeConfig.getBinding().getSOAPVersion());
                
                if(trustConfig != null){
                    packet.invocationProperties.put(
                            com.sun.xml.ws.security.impl.policy.Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS,trustConfig.iterator());
                }
                
                //set the SecurityEnvironment
                packet.invocationProperties.put(WSTrustConstants.SECURITY_ENVIRONMENT, secEnv);
            }
            
            if (isSCIssueMessage){
                List<PolicyAssertion> policies = getInBoundSCP(packet.getMessage());
                if(!policies.isEmpty()) {
                    packet.invocationProperties.put(SC_ASSERTION, (PolicyAssertion)policies.get(0));
                }
            }
        }
        
        if(!isSCIssueMessage ){
            WSDLBoundOperation cachedOperation = cacheOperation (msg, packet);
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
                    packet, ctx, isSCIssueMessage, action);
            // if this is SC message we need to secure it in ValidateRequest Itself
            retPacket = secureResponse(retPacket, serviceSubject, sharedState);
        } else {
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
        Message msg = retPacket.getMessage();
        
        try{
            
            if (ctx.getSecurityPolicy() != null && ((MessagePolicy)ctx.getSecurityPolicy()).size() >0) {
                if(!optimized || msg.isFault()) {
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
        com.sun.xml.ws.security.opt.impl.incoming.SecurityRecipient recipient =
                new com.sun.xml.ws.security.opt.impl.incoming.SecurityRecipient(((LazyStreamBasedMessage)message).readMessage(),soapVersion);
        
        return recipient.validateMessage(context);
    }
   
    protected ProcessingContext initializeOutgoingProcessingContext(
            Packet packet, boolean isSCMessage) {
        
        ProcessingContextImpl ctx = null;
        if(optimized){
            ctx = new JAXBFilterProcessingContext(packet.invocationProperties);
            ((JAXBFilterProcessingContext)ctx).setAddressingVersion(addVer);
            ((JAXBFilterProcessingContext)ctx).setSOAPVersion(soapVersion);
        }else{
            ctx = new ProcessingContextImpl( packet.invocationProperties);
        }
        
        try {
            MessagePolicy policy = null;
            if (packet.getMessage().isFault()) {
                policy =  getOutgoingFaultPolicy(packet);
                if(optimized){
                    ctx = new ProcessingContextImpl( packet.invocationProperties);
                }
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
            // set the policy, issued-token-map, and extraneous properties
            ctx.setIssuedTokenContextMap(issuedTokenContextMap);
            ctx.setAlgorithmSuite(getAlgoSuite(getBindingAlgorithmSuite(packet)));
            ctx.setSecurityEnvironment(secEnv);
            ctx.isInboundMessage(false);
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
    
        protected MessagePolicy getOutgoingXWSSecurityPolicy(
            Packet packet, boolean isSCMessage) {
        if (isSCMessage) {
            Token scToken = (Token)packet.invocationProperties.get(SC_ASSERTION);
            return getOutgoingXWSBootstrapPolicy(scToken);
        }
        //Message message = packet.getMessage();
        
        MessagePolicy mp = null;
        WSDLBoundOperation operation = cachedOperation(packet); 
        //if(operation == null){
            //Body could be encrypted. Security will have to infer the
            //policy from the message till the Body is decrypted.
        //    mp = emptyMessagePolicy;
        //}
        if (outMessagePolicyMap == null) {
            //empty message policy
            return new MessagePolicy();
        }
        
        if(isTrustMessage(packet)){
            //TODO: no runtime updates of variables: store this in Map of MessageInfo
            operation = getWSDLOpFromAction(packet,false);
            cacheOperation(operation, packet);
        }
        
        SecurityPolicyHolder sph = (SecurityPolicyHolder) outMessagePolicyMap.get(operation);
        if(sph == null){
            return new MessagePolicy();
        }
        mp = sph.getMessagePolicy();
        return mp;
    }
    
        protected MessagePolicy getOutgoingFaultPolicy(Packet packet) {
            WSDLBoundOperation cachedOp = cachedOperation(packet);
            
            if(operation != null){
                WSDLOperation operation = cachedOp.getOperation();
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
                        SecurityPolicyHolder sph = outMessagePolicyMap.get(cachedOp);
                        SecurityPolicyHolder faultPolicyHolder = sph.getFaultPolicy(fault);
                        MessagePolicy faultPolicy = (faultPolicyHolder == null) ? new MessagePolicy() : faultPolicyHolder.getMessagePolicy();
                        return faultPolicy;
                        
                    }
                }catch(SOAPException sx){
                    //sx.printStackTrace();
                    //log error
                }
            }
            return null;
            
        }

    
    private CallbackHandler configureServerHandler(Set configAssertions) {
        Properties props = new Properties();
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
                RealmAuthenticationAdapter adapter = this.getRealmAuthenticationAdapter(endPoint);
                return new DefaultCallbackHandler("server", props, adapter);
            }
        }catch (Exception e) {
            log.log(Level.SEVERE, 
                    LogStringsMessages.WSITPVD_0043_ERROR_CONFIGURE_SERVER_HANDLER(), e);                 
            throw new RuntimeException(
                    LogStringsMessages.WSITPVD_0043_ERROR_CONFIGURE_SERVER_HANDLER(), e);            
        }
    }
    
    protected boolean bindingHasIssuedTokenPolicy() {
        return hasIssuedTokens;
    }
    
    protected boolean bindingHasSecureConversationPolicy() {
        return hasSecureConversation;
    }
    
    protected boolean bindingHasRMPolicy() {
        return hasReliableMessaging;
    }
    
    // The packet has the Message with RST/SCT inside it
    // TODO: Need to inspect if it is really a Issue or a Cancel
    @SuppressWarnings("unchecked")
    private Packet invokeSecureConversationContract(
            Packet packet, ProcessingContext ctx, boolean isSCTIssue, String action) {
        
        IssuedTokenContext ictx = new IssuedTokenContextImpl();
        Message msg = packet.getMessage();
        Message retMsg = null;
        String retAction = null;
        
        try {
            
             // Set the requestor authenticated Subject in the IssuedTokenContext
            Subject subject = SubjectAccessor.getRequesterSubject(ctx);
            ictx.setRequestorSubject(subject);
            
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
                ((ProcessingContextImpl)ctx).getIssuedTokenContextMap().put(sctId, itctx);
                
            } else if (requestType.toString().equals(WSTrustConstants.CANCEL_REQUEST)) {
                retAction = WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION;
                rstr =  scContract.cancel(rst, ictx, issuedTokenContextMap);
            } else {
                log.log(Level.SEVERE, 
                        LogStringsMessages.WSITPVD_0045_UNSUPPORTED_OPERATION_EXCEPTION(requestType));                
                throw new UnsupportedOperationException(
                        LogStringsMessages.WSITPVD_0045_UNSUPPORTED_OPERATION_EXCEPTION(requestType));                
            }
            
            // construct the complete message here containing the RSTR and the
            // correct Action headers if any and return the message.
            retMsg = Messages.create(jaxbContext.createMarshaller(), eleFac.toJAXBElement(rstr), soapVersion);
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
    
    protected SecurityPolicyHolder addOutgoingMP(WSDLBoundOperation operation,Policy policy)throws PolicyException{
        SecurityPolicyHolder sph = constructPolicyHolder(policy,true,true);
        inMessagePolicyMap.put(operation,sph);
        return sph;
    }
    
    protected SecurityPolicyHolder addIncomingMP(WSDLBoundOperation operation,Policy policy)throws PolicyException{
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
    
}
