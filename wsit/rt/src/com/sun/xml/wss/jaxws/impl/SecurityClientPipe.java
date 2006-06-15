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

import com.sun.xml.ws.addressing.jaxws.WsaWSDLOperationExtension;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;

import com.sun.xml.ws.policy.sourcemodel.PolicyModelTranslator;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.security.impl.policy.Constants;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Collection;
import java.util.Collections;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLInput;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLOutput;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;

import com.sun.xml.ws.api.pipe.ClientEdgePipe;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicyMerger;
import com.sun.xml.ws.runtime.ClientPipeConfiguration;
import com.sun.xml.ws.security.impl.policyconv.SCTokenWrapper;
import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyHolder;
import com.sun.xml.ws.security.impl.policyconv.XWSSPolicyGenerator;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.ProcessingContextImpl;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;

import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.security.trust.WSTrustFactory;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.TrustPlugin;
import com.sun.xml.ws.security.secconv.WSSCFactory;
import com.sun.xml.ws.security.secconv.WSSCPlugin;

import com.sun.xml.ws.security.policy.Token;


import javax.security.auth.callback.CallbackHandler;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;

import com.sun.xml.ws.policy.PolicyAssertion;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.impl.SecurityRecipient;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.WssSoapFaultException;

import com.sun.xml.ws.security.secconv.SecureConversationInitiator;

import javax.xml.soap.SOAPFactory;
import javax.xml.ws.soap.SOAPFaultException;

import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.JAXWSAConstants;

import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.misc.DefaultCallbackHandler;

import java.util.Properties;


//TODO: add logging before 4/13
/**
 *
 *  @author Vbkumar.Jayanti@Sun.COM, K.Venugopal@sun.com
 */
public class SecurityClientPipe extends SecurityPipeBase implements SecureConversationInitiator {
    
    // Plugin instances for Trust and SecureConversation invocation
    private static TrustPlugin trustPlugin = WSTrustFactory.newTrustPlugin(null);
    private static WSSCPlugin  scPlugin = WSSCFactory.newSCPlugin(null);
    private Set trustConfig = null;
    public static final String PRE_CONFIGURED_STS = "PreconfiguredSTS";
    public static final String NAMESPACE = "namespace";
    public static final String CONFIG_NAMESPACE = "";
    public static final String ENDPOINT = "endpoint";
    public static final String WSDL_LOCATION = "wsdlLocation";
    public static final String SERVICE_NAME = "serviceName";
    public static final String PORT_NAME = "portName";
    
    // Creates a new instance of SecurityClientPipe
    public SecurityClientPipe(ClientPipeConfiguration config,Pipe nextPipe) {
        super(config,nextPipe);
        
        CallbackHandler handler = null;
        try {
            Iterator it = outMessagePolicyMap.values().iterator();
            SecurityPolicyHolder holder = (SecurityPolicyHolder)it.next();
            Set configAssertions = holder.getConfigAssertions(SUN_WSS_SECURITY_CLIENT_POLICY_NS);
            trustConfig = holder.getConfigAssertions(Constants.SUN_TRUST_CLIENT_SECURITY_POLICY_NS);
            /*
            if (configAssertions == null || configAssertions.isEmpty()) {
                throw new RuntimeException("Null or Empty Config WSDL encountered");
            }*/
            handler = configureClientHandler(configAssertions);
            secEnv = new DefaultSecurityEnvironmentImpl(handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        if(nextPipe != null && nextPipe instanceof ClientEdgePipe){
            transportOptimization = true;
        }
    }
    
    // copy constructor
    protected SecurityClientPipe(SecurityClientPipe that) {
        super(that);
        trustConfig = that.trustConfig;
    }
    
    public Packet process(Packet packet) {
        
        // keep the message
        Message msg = packet.getMessage();
        
        boolean isSCMessage = isSCMessage(packet);
        
        if (!isSCMessage){
            // this is an application message
            // initialize any secure-conversation sessions for this message
            invokeSCPlugin(packet);
        }
        
        // invoke the Trust Plugin if necessary
        invokeTrustPlugin(packet, isSCMessage);
        
        //---------------OUTBOUND SECURITY PROCESSING----------
        ProcessingContext ctx = initializeOutgoingProcessingContext(packet, isSCMessage);
        
        try{
            if(!optimized) {
                if(!isSCMessage){
                    cacheOperation(msg);
                }
                SOAPMessage soapMessage = msg.readAsSOAPMessage();
                soapMessage = secureOutboundMessage(soapMessage, ctx);
                msg = Messages.create(soapMessage);
            }else{
                msg = secureOutboundMessage(msg, ctx);
            }
        } catch(SOAPException se){
            throw new WebServiceException(se);
        }
        
        packet.setMessage(msg);
        
        //--------INVOKE NEXT PIPE------------
        Packet ret = nextPipe.process(packet);
        // Could be OneWay
        if (ret == null) {
            return null;
        }
        /* TODO:this piece of code present since payload should be read once*/
        try{
            SOAPMessage sm = ret.getMessage().readAsSOAPMessage();
            Message newMsg = Messages.create(sm);
            ret.setMessage(newMsg);
        }catch(SOAPException ex){
            throw new WebServiceException(ex);
        }/**/
        //---------------INBOUND SECURITY VERIFICATION----------
        isSCMessage = isSCMessage(packet);
        if(isSCMessage){
            
            List<PolicyAssertion> policies = getInBoundSCP(packet.getMessage());
            if (!policies.isEmpty()) {
                ret.otherProperties.put(SC_ASSERTION, (PolicyAssertion)policies.get(0));
            }
        }
        
        ctx = initializeInboundProcessingContext(ret, isSCMessage);
        try{
            msg = ret.getMessage();
            // Could be OneWay
            if (msg == null) {
                return ret;
            }

            if(!optimized) {
                SOAPMessage soapMessage = msg.readAsSOAPMessage();
                soapMessage = verifyInboundMessage(soapMessage, ctx);
                if (msg.isFault()) {                                  

                    if (debug) {
                        DumpFilter.process(ctx);
                    }
                    SOAPFault fault = soapMessage.getSOAPBody().getFault();
                    throw new SOAPFaultException(fault);
                }
                msg = Messages.create(soapMessage);
            }else{
                msg = verifyInboundMessage(msg, ctx);
            }
        } catch (XWSSecurityException xwse) {
            throw getSOAPFaultException(xwse);
        }catch(SOAPException se){
            throw new WebServiceException(se);
        }
        resetCachedOperation();
        ret.setMessage(msg);
        
        return ret;
    }
    
    private void invokeSCPlugin(Packet packet) {
        
        // get the secure conversation policies pertaining to this operation
        List<PolicyAssertion> policies = getOutBoundSCP(packet.getMessage());
        
        for (PolicyAssertion scAssertion : policies) {
            Token scToken = (Token)scAssertion;
            if (issuedTokenContextMap.get(scToken.getTokenId()) == null) {
                
                IssuedTokenContext ctx = scPlugin.process(
                        scAssertion, pipeConfig.getWSDLModel(), pipeConfig.getBinding(), this, jaxbContext, packet.endpointAddress.toString(), packet);
                issuedTokenContextMap.put(((Token)scAssertion).getTokenId(), ctx);
            }
        }
    }
    
    // returns a list of IssuedTokenPolicy Assertions contained in the
    // service policy
    protected List<PolicyAssertion> getIssuedTokenPolicies(Packet packet, String scope) {
        if (outMessagePolicyMap == null) {
            return new ArrayList();
        }
        
        WSDLBoundOperation operation = null;
        if(isTrustMessage(packet)){
            operation = getWSDLOpFromAction(packet,false);
        }else{
            operation =getOperation(packet.getMessage());
        }
        
        SecurityPolicyHolder sph =(SecurityPolicyHolder) outMessagePolicyMap.get(operation);
        if(sph == null){
            return EMPTY_LIST;
        }
        return sph.getIssuedTokens();
    }
    
    public JAXBElement startSecureConversation(Packet packet)
    throws WSSecureConversationException {
        
        List toks =getOutBoundSCP(packet.getMessage());
        if (toks.isEmpty()) {
            throw new WSSecureConversationException("Cannot start SecureConversation, no policy found");
        }
        //Note: Assuming only one SC assertion
        Token tok = (Token)toks.get(0);
        IssuedTokenContext ctx =
                (IssuedTokenContext)issuedTokenContextMap.get(tok.getTokenId());
        
        if (ctx == null) {
            ctx = scPlugin.process(
                    (PolicyAssertion)tok, pipeConfig.getWSDLModel(), pipeConfig.getBinding(),
                    this, jaxbContext, packet.endpointAddress.toString(), packet);
            ctx.setEndpointAddress(packet.endpointAddress.toString());
            issuedTokenContextMap.put(((Token)tok).getTokenId(), ctx);
        }
        
        SecurityTokenReference str = (SecurityTokenReference)ctx.getUnAttachedSecurityTokenReference();
        
        return WSTrustElementFactory.newInstance().toJAXBElement(str);
    }
    
    private void cancelSecurityContextToken(){
        Enumeration keys = issuedTokenContextMap.keys();
        while (keys.hasMoreElements()){
            String id = (String)keys.nextElement();
            IssuedTokenContext ctx =
                    (IssuedTokenContext)issuedTokenContextMap.get(id);
            
            ctx = scPlugin.processCancellation(
                    ctx, pipeConfig.getWSDLModel(), pipeConfig.getBinding(), this, jaxbContext, ctx.getEndpointAddress());
            issuedTokenContextMap.remove(id);
        }
    }
    
    public void preDestroy() {
        cancelSecurityContextToken();
        if (nextPipe != null) {
            nextPipe.preDestroy();
        }
        issuedTokenContextMap.clear();
    }
    
    public Pipe copy(PipeCloner cloner) {
        Pipe clonedNextPipe = cloner.copy(nextPipe);
        Pipe copied = new SecurityClientPipe(this);
        ((SecurityClientPipe)copied).setNextPipe(clonedNextPipe);
        return copied;
    }
    
    private void invokeTrustPlugin(Packet packet, boolean isSCMessage) {
        
        List<PolicyAssertion> policies = null;
        
        if (isSCMessage) {
            Token scToken = (Token)packet.otherProperties.get(SC_ASSERTION);
            policies =  getIssuedTokenPoliciesFromBootstrapPolicy(scToken);
        } else {
            policies = getIssuedTokenPolicies(packet, OPERATION_SCOPE);
        }
        
        URL stsEP = null;
        URL wsdlLocation = null;
        QName serviceName = null;
        QName portName = null;
        if(trustConfig != null){
            Iterator it = trustConfig.iterator();
            while(it!=null && it.hasNext()) {
                PolicyAssertion as = (PolicyAssertion)it.next();
                if (PRE_CONFIGURED_STS.equals(as.getName().getLocalPart())) {
                    Map<QName,String> attrs = as.getAttributes();
                    String namespace = attrs.get(new QName(CONFIG_NAMESPACE,NAMESPACE));
                    try {
                        stsEP = new URL(attrs.get(new QName(CONFIG_NAMESPACE,ENDPOINT)));
                    } catch (MalformedURLException ex) {
                        throw new RuntimeException(ex);
                    }
                    try {
                        wsdlLocation = new URL(attrs.get(new QName(CONFIG_NAMESPACE,WSDL_LOCATION)));
                    } catch (MalformedURLException ex) {
                        throw new RuntimeException(ex);
                    }
                    serviceName = new QName(namespace,attrs.get(new QName(CONFIG_NAMESPACE,SERVICE_NAME)));
                    portName = new QName(namespace,attrs.get(new QName(CONFIG_NAMESPACE,PORT_NAME)));
                }
            }
        } else {
            stsEP = (URL)packet.invocationProperties.get(WSTrustConstants.PROPERTY_SERVICE_END_POINT);
            wsdlLocation = (URL)packet.invocationProperties.get(WSTrustConstants.PROPERTY_URL);
            serviceName = (QName)packet.invocationProperties.get(WSTrustConstants.PROPERTY_SERVICE_NAME);
            portName = (QName)packet.invocationProperties.get(WSTrustConstants.PROPERTY_PORT_NAME);
        }
        
        for (PolicyAssertion issuedTokenAssertion : policies) {
            IssuedTokenContext ctx = trustPlugin.process(issuedTokenAssertion, stsEP, wsdlLocation,serviceName,portName, packet.endpointAddress.toString());
            issuedTokenContextMap.put(
                    ((Token)issuedTokenAssertion).getTokenId(), ctx);
        }
    }
    
    protected SecurityPolicyHolder addOutgoingMP(WSDLBoundOperation operation,Policy policy)throws PolicyException{
        
//        SecurityPolicyHolder sph = new SecurityPolicyHolder();
//
//        XWSSPolicyGenerator xwssPolicyGenerator = new XWSSPolicyGenerator(policy,false,false);
//        xwssPolicyGenerator.process();
//        MessagePolicy messagePolicy = xwssPolicyGenerator.getXWSSPolicy();
//        sph.setMessagePolicy(messagePolicy);
//        sph.setBindingLevelAlgSuite(xwssPolicyGenerator.getBindingLevelAlgSuite());
//        this.bindingLevelAlgSuite = xwssPolicyGenerator.getBindingLevelAlgSuite();
//        List<PolicyAssertion> tokenList = getTokens(policy);
//        for(PolicyAssertion token:tokenList){
//            ArrayList asList= null;
//            if(PolicyUtil.isSecureConversationToken(token)){
//                NestedPolicy bootstrapPolicy = ((SecureConversationToken)token).getBootstrapPolicy();
//
//                Policy effectiveBP = getEffectiveBootstrapPolicy(bootstrapPolicy);
//                xwssPolicyGenerator = new XWSSPolicyGenerator(effectiveBP,false,false);
//
//                xwssPolicyGenerator.process();
//                MessagePolicy bmp = xwssPolicyGenerator.getXWSSPolicy();
//                PolicyAssertion sct = new SCTokenWrapper(token,bmp);
//                sph.addSecureConversationToken(sct);
//            }else if(PolicyUtil.isIssuedToken(token)){
//                sph.addIssuedToken(token);
//            }
//        }
//
        SecurityPolicyHolder sph = constructPolicyHolder(policy,false,false);
        outMessagePolicyMap.put(operation,sph);
        return sph;
    }
    
    protected SecurityPolicyHolder addIncomingMP(WSDLBoundOperation operation,Policy policy)throws PolicyException{
//        XWSSPolicyGenerator xwssPolicyGenerator = new XWSSPolicyGenerator(policy,false,true);
//        xwssPolicyGenerator.process();
//        this.bindingLevelAlgSuite = xwssPolicyGenerator.getBindingLevelAlgSuite();
//        MessagePolicy messagePolicy = xwssPolicyGenerator.getXWSSPolicy();
//
//        SecurityPolicyHolder sph = new SecurityPolicyHolder();
//        sph.setMessagePolicy(messagePolicy);
//        sph.setBindingLevelAlgSuite(xwssPolicyGenerator.getBindingLevelAlgSuite());
//        List<PolicyAssertion> tokenList = getTokens(policy);
//        for(PolicyAssertion token:tokenList){
//            if(PolicyUtil.isSecureConversationToken(token)){
//                NestedPolicy bootstrapPolicy = ((SecureConversationToken)token).getBootstrapPolicy();
//                Policy effectiveBP = getEffectiveBootstrapPolicy(bootstrapPolicy);
//                xwssPolicyGenerator = new XWSSPolicyGenerator(effectiveBP,false,true);
//                xwssPolicyGenerator.process();
//                MessagePolicy bmp = xwssPolicyGenerator.getXWSSPolicy();
//                PolicyAssertion sct = new SCTokenWrapper(token,bmp);
//                sph.addSecureConversationToken(sct);
//            }else if(PolicyUtil.isIssuedToken(token)){
//                sph.addIssuedToken(token);
//            }
//        }
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
    
    protected String getAction(WsaWSDLOperationExtension ext,boolean inComming){
        if(!inComming){
            return ext.getInputAction();
        }else{
            return ext.getOutputAction();
        }
    }
    
    //TODO use constants here
    private CallbackHandler configureClientHandler(Set configAssertions) {
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
            return new DefaultCallbackHandler("client", props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected Policy getWSITConfig(){
        if(wsitConfig == null){
            try{
                PolicySourceModel model =  unmarshalPolicy("wsit-client.xml");
                if(model != null){
                    wsitConfig =  PolicyModelTranslator.getTranslator().translate(model);
                }
            }catch(PolicyException ex){
                ex.printStackTrace();
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return wsitConfig;
    }
    
}
