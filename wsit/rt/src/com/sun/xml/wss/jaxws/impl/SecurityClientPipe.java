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


package com.sun.xml.wss.jaxws.impl;


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
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.client.IssuedTokenManager;
import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.assembler.ClientPipeConfiguration;
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
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.secconv.WSSCFactory;
import com.sun.xml.ws.security.secconv.WSSCPlugin;
import com.sun.xml.ws.security.policy.Token;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.BindingProvider;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.policy.IssuedToken;

import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.security.trust.impl.client.DefaultSTSIssuedTokenConfiguration;
import com.sun.xml.wss.impl.ProcessingContextImpl;
import javax.xml.ws.soap.SOAPFaultException;
import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.misc.DefaultCallbackHandler;
import java.util.Properties;
import static com.sun.xml.wss.jaxws.impl.Constants.SC_ASSERTION;
import static com.sun.xml.wss.jaxws.impl.Constants.OPERATION_SCOPE;
import static com.sun.xml.wss.jaxws.impl.Constants.EMPTY_LIST;
import static com.sun.xml.wss.jaxws.impl.Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS;

import java.util.logging.Level;
import com.sun.xml.wss.jaxws.impl.logging.LogStringsMessages;

/**
 *
 *  @author Vbkumar.Jayanti@Sun.COM, K.Venugopal@sun.com
 */
public class SecurityClientPipe extends SecurityPipeBase implements SecureConversationInitiator {
    
    // Plugin instances for Trust and SecureConversation invocation
    //private static TrustPlugin trustPlugin = WSTrustFactory.newTrustPlugin(null);
    private IssuedTokenManager itm = IssuedTokenManager.getInstance();
    private WSSCPlugin  scPlugin;
    private Set trustConfig = null;
    
    // Creates a new instance of SecurityClientPipe
    public SecurityClientPipe(ClientPipeConfiguration config,Pipe nextPipe) {
        super(config,nextPipe);
        scPlugin = new WSSCPlugin(null, wsscVer);
        CallbackHandler handler = null;
        try {
            Iterator it = outMessagePolicyMap.values().iterator();
            SecurityPolicyHolder holder = (SecurityPolicyHolder)it.next();
            Set configAssertions = holder.getConfigAssertions(SUN_WSS_SECURITY_CLIENT_POLICY_NS);
            trustConfig = holder.getConfigAssertions(Constants.SUN_TRUST_CLIENT_SECURITY_POLICY_NS);
            Properties props = new Properties();
            handler = configureClientHandler(configAssertions, props);
            secEnv = new DefaultSecurityEnvironmentImpl(handler, props);
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSPIPE_0023_ERROR_CREATING_NEW_INSTANCE_SEC_CLIENT_PIPE(), e);
            throw new RuntimeException(
                    LogStringsMessages.WSSPIPE_0023_ERROR_CREATING_NEW_INSTANCE_SEC_CLIENT_PIPE(), e);
        }
        
        
    }
    
    // copy constructor
    protected SecurityClientPipe(SecurityClientPipe that) {
        super(that);
        trustConfig = that.trustConfig;
        scPlugin = that.scPlugin;
    }
    
    public Packet process(Packet packet) {
        
        // Add Action header to trust message
        boolean isTrustMsg = false;
        if ("true".equals(packet.invocationProperties.get(WSTrustConstants.IS_TRUST_MESSAGE))){
            isTrustMsg = true;
            String action = (String)packet.invocationProperties.get(wsTrustVer.getIssueRequestAction());
            HeaderList headers = packet.getMessage().getHeaders();
        }
        
        // keep the message
        Message msg = packet.getMessage();
        
        boolean isSCMessage = isSCMessage(packet);
        
        if (!isSCMessage && !isSCCancel(packet)){
            // this is an application message
            // initialize any secure-conversation sessions for this message
            invokeSCPlugin(packet);
        }
        
        // invoke the Trust Plugin if necessary
        invokeTrustPlugin(packet, isSCMessage);
        
        //---------------OUTBOUND SECURITY PROCESSING----------
        ProcessingContext ctx = initializeOutgoingProcessingContext(packet, isSCMessage);
        ((ProcessingContextImpl)ctx).setIssuedTokenContextMap(issuedTokenContextMap);
        
        try{
            if(hasKerberosTokenPolicy()){
                populateKerberosContext(packet, (ProcessingContextImpl)ctx, isSCMessage);
                ((ProcessingContextImpl)ctx).setKerberosContextMap(kerberosTokenContextMap);
            }
        } catch(XWSSecurityException xwsse){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSPIPE_0024_ERROR_SECURING_OUTBOUND_MSG(), xwsse);
            throw new WebServiceException(
                    LogStringsMessages.WSSPIPE_0024_ERROR_SECURING_OUTBOUND_MSG(), xwsse);
        }
        
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
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSPIPE_0024_ERROR_SECURING_OUTBOUND_MSG(), se);
            throw new WebServiceException(
                    LogStringsMessages.WSSPIPE_0024_ERROR_SECURING_OUTBOUND_MSG(), se);
        }
        
        packet.setMessage(msg);
        
        //--------INVOKE NEXT PIPE------------
        Packet ret = nextPipe.process(packet);
        // Could be OneWay
        if (ret == null || ret.getMessage() == null) {
            return ret;
        }
        
        /* TODO:this piece of code present since payload should be read once*/
        if (!optimized) {
            try{
                SOAPMessage sm = ret.getMessage().readAsSOAPMessage();
                Message newMsg = Messages.create(sm);
                ret.setMessage(newMsg);
            }catch(SOAPException ex){
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSPIPE_0005_PROBLEM_PROC_SOAP_MESSAGE(), ex);
                throw new WebServiceException(
                        LogStringsMessages.WSSPIPE_0005_PROBLEM_PROC_SOAP_MESSAGE(), ex);
            }
        }
        //---------------INBOUND SECURITY VERIFICATION----------
        
        
        ctx = initializeInboundProcessingContext(ret);
        ctx.isClient(true);
        if(hasKerberosTokenPolicy()){
            ((ProcessingContextImpl)ctx).setKerberosContextMap(kerberosTokenContextMap);
        }
        ((ProcessingContextImpl)ctx).setIssuedTokenContextMap(issuedTokenContextMap);
        ctx.setExtraneousProperty(ctx.OPERATION_RESOLVER, new PolicyResolverImpl(inMessagePolicyMap,inProtocolPM,cachedOperation,pipeConfig,addVer,true));
        
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
                    //log.log(Level.SEVERE,
                    //        LogStringsMessages.WSSPIPE_0034_FAULTY_RESPONSE_MSG(fault));
                    throw new SOAPFaultException(fault);
                }
                msg = Messages.create(soapMessage);
            }else{
                msg = verifyInboundMessage(msg, ctx);
            }
        } catch (XWSSecurityException xwse) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSPIPE_0025_ERROR_VERIFY_INBOUND_MSG(), xwse);
            throw new WebServiceException(LogStringsMessages.WSSPIPE_0025_ERROR_VERIFY_INBOUND_MSG(),
                    getSOAPFaultException(xwse));
        }catch(SOAPException se){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSPIPE_0025_ERROR_VERIFY_INBOUND_MSG(), se);
            throw new WebServiceException(LogStringsMessages.WSSPIPE_0025_ERROR_VERIFY_INBOUND_MSG(), se);
        }
        resetCachedOperation();
        ret.setMessage(msg);
        
        if (isTrustMsg){
            //String action = getAction(ret);
            getAction(ret);
        }
        
        return ret;
    }
    
    private void invokeSCPlugin(Packet packet) {
        
        // get the secure conversation policies pertaining to this operation
        List<PolicyAssertion> policies = getOutBoundSCP(packet.getMessage());
        
        for (PolicyAssertion scAssertion : policies) {
            Token scToken = (Token)scAssertion;
            if (issuedTokenContextMap.get(scToken.getTokenId()) == null) {
                
                IssuedTokenContext ctx = scPlugin.process(
                        scAssertion, pipeConfig.getWSDLModel(), pipeConfig.getBinding(), this, marshaller, unmarshaller, packet.endpointAddress.toString(), packet, addVer);
                issuedTokenContextMap.put(((Token)scAssertion).getTokenId(), ctx);
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
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                        LogStringsMessages.WSSPIPE_0026_NO_POLICY_FOUND_FOR_SC());
            }
            //throw new WSSecureConversationException(LogStringsMessages.WSSPIPE_0026_NO_POLICY_FOUND_FOR_SC());
            return null;
        }
        //Note: Assuming only one SC assertion
        Token tok = (Token)toks.get(0);
        IssuedTokenContext ctx =
                (IssuedTokenContext)issuedTokenContextMap.get(tok.getTokenId());
        
        if (ctx == null) {
            ctx = scPlugin.process(
                    (PolicyAssertion)tok, pipeConfig.getWSDLModel(), pipeConfig.getBinding(),
                    this, marshaller, unmarshaller, packet.endpointAddress.toString(), packet, addVer);
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
            
            if (ctx.getSecurityToken() instanceof SecurityContextToken){
                ctx = scPlugin.processCancellation(
                        ctx, pipeConfig.getWSDLModel(), pipeConfig.getBinding(), this, marshaller, unmarshaller, ctx.getEndpointAddress(),addVer);
                issuedTokenContextMap.remove(id);
            }
        }
    }
    
    public void preDestroy() {
        cancelSecurityContextToken();
        if (nextPipe != null) {
            nextPipe.preDestroy();
        }
        issuedTokenContextMap.clear();
        kerberosTokenContextMap.clear();
    }
    
    public Pipe copy(PipeCloner cloner) {
        Pipe clonedNextPipe = cloner.copy(nextPipe);
        Pipe copied = new SecurityClientPipe(this);
        ((SecurityClientPipe)copied).setNextPipe(clonedNextPipe);
        cloner.add(this, copied);
        return copied;
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
        //URI stsEP = null;
        //URI wsdlLocation = null;
        //QName serviceName = null;
        //QName portName = null;
        if(trustConfig != null){
            Iterator it = trustConfig.iterator();
            while(it!=null && it.hasNext()) {
                preSetSTSAssertion = (PolicyAssertion)it.next();
            }
            //serviceName = (QName)packet.invocationProperties.get(WSTrustConstants.PROPERTY_SERVICE_NAME);
            //portName = (QName)packet.invocationProperties.get(WSTrustConstants.PROPERTY_PORT_NAME);
        }
        
        for (PolicyAssertion issuedTokenAssertion : policies) {
            //IssuedTokenContext ctx = trustPlugin.process(issuedTokenAssertion, preSetSTSAssertion, packet.endpointAddress.toString());
            //ToDo: Handling mixed trust versions??
            try {
                STSIssuedTokenConfiguration config = new DefaultSTSIssuedTokenConfiguration(wsTrustVer.getNamespaceURI(), (IssuedToken)issuedTokenAssertion, preSetSTSAssertion); 
                String userName = (String) packet.invocationProperties.get(BindingProvider.USERNAME_PROPERTY);
                String password = (String) packet.invocationProperties.get(BindingProvider.PASSWORD_PROPERTY);
                if (userName != null){
                    config.getOtherOptions().put(BindingProvider.USERNAME_PROPERTY, userName);
                }
                if (password != null){
                    config.getOtherOptions().put(BindingProvider.PASSWORD_PROPERTY, password);
                }
                IssuedTokenContext ctx =itm.createIssuedTokenContext(config, packet.endpointAddress.toString());
                itm.getIssuedToken(ctx);
                issuedTokenContextMap.put(
                    ((Token)issuedTokenAssertion).getTokenId(), ctx);
            }catch(WSTrustException se){
                //ToDo
                throw new WebServiceException("ERROR_ISSUED_TOKEN_CREATION", se);
            }
        }
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
    
    protected void populateKerberosContext(Packet packet, ProcessingContextImpl ctx, boolean isSCMessage) throws XWSSecurityException{
        List toks =getOutBoundKTP(packet, isSCMessage);
        if (toks.isEmpty()) {
            return;
        }
        //Note: Assuming only one Kerberos token assertion
        Token tok = (Token)toks.get(0);
        String tokId = tok.getTokenId();
        String sha1KerbToken = (String)ctx.getExtraneousProperty(MessageConstants.KERBEROS_SHA1_VALUE);
        if(sha1KerbToken == null){
            sha1KerbToken = this.kerberosTokenIdMap.get(tokId);
            if(sha1KerbToken != null){
                ctx.setExtraneousProperty(MessageConstants.KERBEROS_SHA1_VALUE, sha1KerbToken);
            }
        }
        KerberosContext krbContext = null;
        if(sha1KerbToken != null)
            krbContext = kerberosTokenContextMap.get(sha1KerbToken);
        
        if(krbContext == null){
            
            krbContext = ctx.getSecurityEnvironment().doKerberosLogin();
            try {
                byte[] krbSha1 = MessageDigest.getInstance("SHA-1").digest(krbContext.getKerberosToken());
                String encKrbSha1 = Base64.encode(krbSha1);
                ctx.setExtraneousProperty(MessageConstants.KERBEROS_SHA1_VALUE, encKrbSha1);
                kerberosTokenContextMap.put(encKrbSha1, krbContext);
                this.kerberosTokenIdMap.put(tokId, encKrbSha1);
            } catch (NoSuchAlgorithmException nsae) {
                throw new XWSSecurityException(nsae);
            }
        } else{
            krbContext.setOnce(false);
        }
    }
    
    //TODO use constants here
    private CallbackHandler configureClientHandler(Set configAssertions, Properties props) {
        //Properties props = new Properties();
        String ret = populateConfigProperties(configAssertions, props);
        try {
            if (ret != null) {
                Class handler = loadClass(ret);
                Object obj = handler.newInstance();
                if (!(obj instanceof CallbackHandler)) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSSPIPE_0033_INVALID_CALLBACK_HANDLER_CLASS(ret));
                    throw new RuntimeException(
                            LogStringsMessages.WSSPIPE_0033_INVALID_CALLBACK_HANDLER_CLASS(ret));
                }
                return (CallbackHandler)obj;
            }
            return new DefaultCallbackHandler("client", props);
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSPIPE_0027_ERROR_CONFIGURE_CLIENT_HANDLER(), e);
            throw new RuntimeException(LogStringsMessages.WSSPIPE_0027_ERROR_CONFIGURE_CLIENT_HANDLER(), e);
        }
    }
}
