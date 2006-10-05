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

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.fault.SOAPFaultBuilder;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
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
import java.security.SecureRandom;
import java.util.Set;
import java.util.UUID;
import javax.xml.ws.soap.SOAPFaultException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.secconv.logging.LogDomainConstants;
import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author ws-trust-impl-team
 *
 * ToDo: Handle Cancel request and response.
 */
public class WSSCPlugin {
    
    Configuration config;
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.WSSC_IMPL_DOMAIN,
            LogDomainConstants.WSSC_IMPL_DOMAIN_BUNDLE);
    
    private static WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance();
    
    private static final int DEFAULT_KEY_SIZE = 256;
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
            log.log(Level.FINE,"WSSC1006.sym.bin.keysize", new Object[] {skl});
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
            throw new RuntimeException("There is a problem creating RST", ex);
        } catch (WSTrustException ex){
            throw new RuntimeException("There is a problem in the Trust layer creating an RST", ex);
        }
        
        RequestSecurityTokenResponse rstr = sendRequest(token, wsdlPort, binding, securityPipe, jbCxt, rst, WSSCConstants.REQUEST_SECURITY_CONTEXT_TOKEN_ACTION, endPointAddress, packet);
        
        // Handle the RequestSecurityTokenResponse
        IssuedTokenContext context = new IssuedTokenContextImpl();
        try {
            processRequestSecurityTokenResponse(rst, rstr, context);
        } catch (WSSecureConversationException ex){
            throw new RuntimeException(ex);
        }
        context.setEndpointAddress(endPointAddress);
        
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
            throw new RuntimeException("There was a problem creating RST For Cancel", ex);
        }
        
        RequestSecurityTokenResponse rstr = sendRequest(null, wsdlPort, binding, securityPipe, jbCxt, rst, WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_ACTION, endPointAddress, null);
        
        // Handle the RequestSecurityTokenResponse
        try {
            processRequestSecurityTokenResponse(rst, rstr, ctx);
        } catch (WSSecureConversationException ex){
            throw new RuntimeException("Problem processing RSTR", ex);
        }
        
        return ctx;
    }
    
    private RequestSecurityTokenResponse sendRequest(PolicyAssertion issuedToken, WSDLPort wsdlPort, WSBinding binding, Pipe securityPipe, JAXBContext jbCxt, RequestSecurityToken rst, String action, String endPointAddress, Packet packet) {
        Marshaller marshaller;
        Unmarshaller unmarshaller;
        
        try {
            marshaller = jbCxt.createMarshaller();
            unmarshaller = jbCxt.createUnmarshaller();
        } catch (JAXBException ex){
            log.log(Level.SEVERE,"WSSC0016.problem.mar.unmar", ex);
            throw new RuntimeException("Problem creating JAXB Marshaller/Unmarshaller", ex);
        }
        
        Message request = Messages.create(marshaller, eleFac.toJAXBElement(rst), binding.getSOAPVersion());

        // Log Request created
        log.log(Level.FINE,"WSSC1009.send.req.message", new Object[] {printMessageAsString(request)});        
        Packet reqPacket = new Packet(request);
        if (issuedToken != null){
            reqPacket.invocationProperties.put(SC_ASSERTION, issuedToken);
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
            log.log(Level.SEVERE,"WSSC0017.problem.add.address.headers", ex);
            throw new RuntimeException(ex);
        }
        
        reqPacket.setEndPointAddressString(endPointAddress);
        log.log(Level.FINE,"WSSC1008.set.ep.address",
                new Object[]{endPointAddress});
        
        // Ideally this property for enabling FI or not should be available to the pipeline.
        // As a workaround for now, we
        // copy the property for the client packet to the reqPacket mananually here.
        if (packet != null){
            reqPacket.contentNegotiation = packet.contentNegotiation;
        }
        
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
                log.log(Level.SEVERE,"WSSC0018.err.jaxb.rstr", ex);
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
    
    private RequestSecurityToken createRequestSecurityToken(boolean requireClientEntropy,int skl) throws WSSecureConversationException, WSTrustException{
        
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
        rst.setKeyType(URI.create(WSTrustConstants.SYMMETRIC_KEY));
        rst.setComputedKeyAlgorithm(URI.create(WSTrustConstants.CK_PSHA1));
        
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
    
    private String printMessageAsString(Message message) {
        StringWriter writer = new StringWriter();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            XMLStreamWriter streamWriter = factory.createXMLStreamWriter(writer);
            message.writeTo(streamWriter);
            streamWriter.flush();
            return writer.toString();
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Problem Printing message ", ex);
        }
    }
    
    private Packet addAddressingHeaders(Packet packet, WSDLPort wsdlPort, WSBinding binding, String action)throws WSSecureConversationException {
        HeaderList hl = packet.getMessage().getHeaders();
        hl.fillRequestAddressingHeaders(wsdlPort, binding, packet, action);
        
        return packet;
    }
}
