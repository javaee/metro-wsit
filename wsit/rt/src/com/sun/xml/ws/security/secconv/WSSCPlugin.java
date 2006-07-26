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

/*
 * WSSCPlugin.java
 *
 * Created on February 7, 2006, 4:09 PM
 */

package com.sun.xml.ws.security.secconv;

import com.sun.xml.ws.addressing.spi.WsaRuntimeFactory;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.fault.SOAPFaultBuilder;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.impl.policy.BootstrapPolicy;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.impl.policy.Trust10;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.Constants;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.policy.SymmetricBinding;
import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.CancelTarget;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingConstants;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;

/**
 *
 * @author ws-trust-impl-team
 *
 * ToDo: Hnadle Cancell request and response.
 */
public class WSSCPlugin {
    
    Configuration config;
    
    private static WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance();
    
    private static final int DEFAULT_KEY_SIZE = 128;
    private static final String SC_ASSERTION = "SecureConversationAssertion";
    
    /** Creates a new instance of WSSCPlugin */
    public WSSCPlugin(Configuration config) {
        this.config = config;
    }
    
    public IssuedTokenContext process(PolicyAssertion token, WSDLPort wsdlPort, WSBinding binding, Pipe securityPipe, JAXBContext jbCxt, String endPointAddress, Packet packet){

        //==============================
        // Get Required policy assertions 
        //==============================
        SecureConversationToken scToken = (SecureConversationToken)token;
        AssertionSet assertions = getAssertions(scToken);
        Trust10 trust10 = null;
        SymmetricBinding symBinding = null;
        for(PolicyAssertion policyAssertion : assertions){
            if(PolicyUtil.isTrust10(policyAssertion)){
                trust10 = (Trust10)policyAssertion;
            }else if(PolicyUtil.isSymmetricBinding(policyAssertion)){
                symBinding = (SymmetricBinding)policyAssertion;
            }
        }
    
        int skl = DEFAULT_KEY_SIZE;
        boolean requireClientEntropy = false;
        if(symBinding!=null){
            AlgorithmSuite algoSuite = symBinding.getAlgorithmSuite();
            skl = algoSuite.getMinSKLAlgorithm();
            if(skl<1){
                skl = DEFAULT_KEY_SIZE;
            }
        }
        if(trust10 != null){
            Set trustReqdProps = trust10.getRequiredProperties();
            requireClientEntropy = trustReqdProps.contains(Constants.REQUIRE_CLIENT_ENTROPY);
        }

        //==============================
        // Create RequestSecurityToken 
        //==============================
        RequestSecurityToken rst = null;
        try{
            rst = createRequestSecurityToken(requireClientEntropy,skl);
        } catch (WSSecureConversationException ex){
            throw new RuntimeException(ex);
        }
        
       RequestSecurityTokenResponse rstr = sendRequest(token, wsdlPort, binding, securityPipe, jbCxt, rst, WSSCConstants.REQUEST_SECURITY_CONTEXT_TOKEN_ACTION, endPointAddress, packet);
        
        // Handle the RequestSecurityTokenResponse
        IssuedTokenContext context = new IssuedTokenContextImpl();
        try {
            processRequestSecurityTokenResponse(rst, rstr, context);
        }
        catch (WSSecureConversationException ex){
            throw new RuntimeException(ex);
        }
        
        return context;
    }

    private AssertionSet getAssertions(final SecureConversationToken scToken) {
        NestedPolicy policy = scToken.getBootstrapPolicy();
        AssertionSet assertions = policy.getAssertionSet();
        return assertions;
    }
    
     public IssuedTokenContext processCancellation(IssuedTokenContext ctx, WSDLPort wsdlPort, WSBinding binding, Pipe securityPipe, JAXBContext jbCxt, String endPointAddress){
        
        //==============================
        // Create RequestSecurityToken 
        //==============================
        RequestSecurityToken rst = null;
        try{
            rst = createRequestSecurityTokenForCancel(ctx);
        } catch (WSSecureConversationException ex){
            throw new RuntimeException(ex);
        }
        
        RequestSecurityTokenResponse rstr = sendRequest(null, wsdlPort, binding, securityPipe, jbCxt, rst, WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_ACTION, endPointAddress, null);
        
        // Handle the RequestSecurityTokenResponse
        try {
            processRequestSecurityTokenResponse(rst, rstr, ctx);
        }
        catch (WSSecureConversationException ex){
            throw new RuntimeException(ex);
        }
        
        return ctx;
    }
    
    private RequestSecurityTokenResponse sendRequest(PolicyAssertion issuedToken, WSDLPort wsdlPort, WSBinding binding, Pipe securityPipe, JAXBContext jbCxt, RequestSecurityToken rst, String action, String endPointAddress, Packet packet){
        Marshaller marshaller;
        Unmarshaller unmarshaller;
        
        try {
            marshaller = jbCxt.createMarshaller();
            unmarshaller = jbCxt.createUnmarshaller();
        } catch (JAXBException ex){
            throw new RuntimeException(ex);
        }
            
        Message request = Messages.create(marshaller, eleFac.toJAXBElement(rst), binding.getSOAPVersion());
        
        Packet reqPacket = new Packet(request);
        if (issuedToken != null){
            reqPacket.otherProperties.put(SC_ASSERTION, issuedToken);
        }
        if (packet != null){
            for(String stsProperty : WSTrustConstants.STS_PROPERTIES) {
                reqPacket.invocationProperties.put(stsProperty,packet.invocationProperties.get(stsProperty));
            }
        }
        
        // Add addressing headers to the message
        try{
            reqPacket = addAddressingHeaders(reqPacket, wsdlPort, binding, action);
        }catch (WSSecureConversationException ex){
            throw new RuntimeException(ex);
        }
        
        reqPacket.setEndPointAddressString(endPointAddress);
        
        // Ideally this property for enabling FI or not should be available to the pipeline. 
        // As a workaround for now, we 
        // copy the property for the client packet to the reqPacket mananually here.
         reqPacket.contentNegotiation = packet.contentNegotiation;
        
        // Send the message 
        Packet respPacket = securityPipe.process(reqPacket);
        
        // Obtain the RequestSecurtyTokenResponse
        Message response = respPacket.getMessage();
        RequestSecurityTokenResponse rstr = null;
        if (!response.isFault()){
            JAXBElement rstrEle = null;
            try {
                rstrEle = (JAXBElement)response.readPayloadAsJAXB(unmarshaller);
            }catch (JAXBException ex){
                throw new RuntimeException(ex);
            }
            rstr = eleFac.createRSTRFrom(rstrEle);
         } else {
            try{
                SOAPFaultBuilder builder = SOAPFaultBuilder.create(response);
                throw (SOAPFaultException)builder.createException(null, response);
            } catch (JAXBException ex){
                throw new RuntimeException(ex);
            }
         }
        return rstr;
    }
    
    private RequestSecurityToken createRequestSecurityToken(boolean requireClientEntropy,int skl) throws WSSecureConversationException{
 
        URI tokenType = URI.create(WSSCConstants.SECURITY_CONTEXT_TOKEN_TYPE);
        URI requestType = URI.create(WSTrustConstants.ISSUE_REQUEST);
        SecureRandom sr = new SecureRandom();
        byte[] rawValue = new byte[skl/8];
        sr.nextBytes(rawValue);
        BinarySecret secret = eleFac.createBinarySecret(rawValue, BinarySecret.NONCE_KEY_TYPE);
        Entropy entropy = requireClientEntropy?eleFac.createEntropy(secret):null;
        
        RequestSecurityToken rst = null;
        try {
            rst = eleFac.createRSTForIssue(tokenType, requestType, null, null, null, entropy, null);
        } catch (WSTrustException ex){
            throw new WSSecureConversationException(ex);
        }
        rst.setKeySize(skl);
        
        return rst;
    }
    
    private RequestSecurityToken createRequestSecurityTokenForCancel(IssuedTokenContext ctx) throws WSSecureConversationException{
        URI requestType = null;
        requestType = URI.create(WSTrustConstants.CANCEL_REQUEST);
        
        CancelTarget target = eleFac.createCancelTarget((SecurityTokenReference)ctx.getUnAttachedSecurityTokenReference());
        RequestSecurityToken rst = eleFac.createRSTForCancel(requestType, target);
        
        return rst;
    }
    
    private void processRequestSecurityTokenResponse(RequestSecurityToken rst, RequestSecurityTokenResponse rstr, IssuedTokenContext context)
        throws WSSecureConversationException {
        WSSCClientContract contract = WSSCFactory.newWSSCClientContract(config);
        contract.handleRSTR(rst, rstr, context);
    }
    
    private Packet addAddressingHeaders(Packet packet, WSDLPort wsdlPort, WSBinding binding, String action)throws WSSecureConversationException {
        AddressingBuilder builder = AddressingBuilder.newInstance();
        AddressingProperties ap = builder.newAddressingProperties();
        
      
        // Action
        ap.setAction(builder.newURI(action));
        
        // MessageID
        String msgId = "uuid:" + UUID.randomUUID().toString();
        ap.setMessageID(builder.newURI(msgId));
        
        // To
        ap.setTo(builder.newURI(wsdlPort.getAddress().toString()));
        
        // ReplyTo
        ap.setReplyTo(builder.newEndpointReference(builder.newAddressingConstants().getAnonymousURI().toString()));
        
        
        WsaRuntimeFactory fac = WsaRuntimeFactory.newInstance(ap.getNamespaceURI(), wsdlPort, binding);
        fac.writeHeaders(packet, ap);
        packet.invocationProperties
                                .put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, ap);
        
        return packet;
    }   
}
