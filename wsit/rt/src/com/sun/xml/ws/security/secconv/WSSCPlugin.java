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
 * WSSCPlugin.java
 *
 * Created on February 7, 2006, 4:09 PM
 */

package com.sun.xml.ws.security.secconv;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.security.secconv.client.SCTokenConfiguration;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.security.impl.policyconv.PolicyID;
import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyUtil;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.CancelTarget;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;

import java.net.URI;
import java.security.SecureRandom;
import javax.xml.soap.SOAPException;
import javax.xml.ws.soap.SOAPFaultException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.secconv.logging.LogDomainConstants;
import com.sun.xml.ws.security.secconv.logging.LogStringsMessages;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RenewTarget;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.provider.wsit.WSITClientAuthContext;
import com.sun.xml.wss.jaxws.impl.SecurityClientTube;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;
import javax.xml.bind.JAXBContext;
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
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSSC_IMPL_DOMAIN,
            LogDomainConstants.WSSC_IMPL_DOMAIN_BUNDLE);
    
    //private WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance();    
    
    //private WSSCVersion wsscVer = WSSCVersion.WSSC_10;
    //private WSTrustVersion wsTrustVer = WSTrustVersion.WS_TRUST_10;
    private static final int DEFAULT_KEY_SIZE = 256;
    private static final String SC_ASSERTION = "SecureConversationAssertion";
    private static final String FOR_CANCEL = "For Cancel";
    private static SignaturePolicy renewSignaturePolicy = null;
    private static PolicyID pid = new PolicyID();    
    private static Binding binding = null;
    private Engine fiberEngine;
    
    private Packet packet = null;
    
    /** Creates a new instance of WSSCPlugin */
    public WSSCPlugin() {
  
    }
    
    /*
    public IssuedTokenContext process(final PolicyAssertion token, final WSDLPort wsdlPort, final WSBinding binding, final Pipe securityPipe, final Marshaller marshaller, final Unmarshaller unmarshaller, final String endPointAddress, final Packet packet, final AddressingVersion addVer){
        
        this.packet = packet;
        
        //==============================
        // Get Required policy assertions
        //==============================
        final SecureConversationToken scToken = (SecureConversationToken)token;
        final AssertionSet assertions = getAssertions(scToken);
        Trust10 trust10 = null;
        Trust13 trust13 = null;
        SymmetricBinding symBinding = null;
        for(PolicyAssertion policyAssertion : assertions){
            SecurityPolicyVersion spVersion = getSPVersion(policyAssertion);
            if(PolicyUtil.isTrust13(policyAssertion, spVersion)){
                trust13 = (Trust13)policyAssertion;
            }else if(PolicyUtil.isTrust10(policyAssertion, spVersion)){
                trust10 = (Trust10)policyAssertion;                
            }else if(PolicyUtil.isSymmetricBinding(policyAssertion, spVersion)){
                symBinding = (SymmetricBinding)policyAssertion;
            }
        }
        
        int skl = DEFAULT_KEY_SIZE;
        boolean reqClientEntropy = true;
        if(symBinding!=null){
            final AlgorithmSuite algoSuite = symBinding.getAlgorithmSuite();
            skl = algoSuite.getMinSKLAlgorithm();
            if(skl<1){
                skl = DEFAULT_KEY_SIZE;
            }
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                        LogStringsMessages.WSSC_1006_SYM_BIN_KEYSIZE(skl, this.DEFAULT_KEY_SIZE));
            }
        }
        
        if(trust10 != null){
            final Set trustReqdProps = trust10.getRequiredProperties();
            reqClientEntropy = trustReqdProps.contains(Constants.REQUIRE_CLIENT_ENTROPY);
        }
        if(trust13 != null){
            final Set trustReqdProps = trust13.getRequiredProperties();
            reqClientEntropy = trustReqdProps.contains(Constants.REQUIRE_CLIENT_ENTROPY);
        }
        
        //==============================
        // Create RequestSecurityToken
        //==============================
        BaseSTSRequest rst = null;
        try{
            rst = createRequestSecurityToken(reqClientEntropy,skl);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(""), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(""), ex);
        }
        
        final BaseSTSResponse rstr = sendRequest(token, wsdlPort, binding, securityPipe, marshaller, unmarshaller, rst, wsscVer.getSCTRequestAction(), endPointAddress, addVer);
        
        // Handle the RequestSecurityTokenResponse
        final IssuedTokenContext context = new IssuedTokenContextImpl();
        try {
            processRequestSecurityTokenResponse(rst, rstr, context);
        } catch (WSSecureConversationException ex){
            throw new RuntimeException(ex);
        }
        context.setEndpointAddress(endPointAddress);
        
        return context;
    }
    */

    public void process(IssuedTokenContext itc){
        SCTokenConfiguration sctConfig = (SCTokenConfiguration)itc.getSecurityPolicy().get(0);
        WSSCVersion wsscVer = WSSCVersion.getInstance(sctConfig.getProtocol());
        WSTrustVersion wsTrustVer = null;
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13_NS_URI)){
            wsTrustVer = WSTrustVersion.WS_TRUST_13;
        }else{
            wsTrustVer = WSTrustVersion.WS_TRUST_10;
        }
        this.packet = sctConfig.getPacket();
        //==============================
        // Get Required policy assertions
        //==============================        
        
        int skl = DEFAULT_KEY_SIZE;        
        if(sctConfig.isSymmetricBinding()){            
            skl = sctConfig.getKeySize();
            if(skl<1){
                skl = DEFAULT_KEY_SIZE;
            }
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                        LogStringsMessages.WSSC_1006_SYM_BIN_KEYSIZE(skl, this.DEFAULT_KEY_SIZE));
            }
        }                
        
        //==============================
        // Create RequestSecurityToken
        //==============================
        BaseSTSRequest rst = null;
        try{
            rst = createRequestSecurityToken(sctConfig, sctConfig.getReqClientEntropy(),skl);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(""), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(""), ex);
        }/* catch (WSTrustException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0021_PROBLEM_CREATING_RST_TRUST(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0021_PROBLEM_CREATING_RST_TRUST(), ex);
        }*/
        
        final BaseSTSResponse rstr = sendRequest(sctConfig, rst, itc.getEndpointAddress(), wsscVer.getSCTRequestAction());
        
        if(log.isLoggable(Level.FINE)){            
            log.log(Level.FINE, 
                    LogStringsMessages.WSSC_1012_RECEIVED_SCT_RSTR_ISSUE(WSTrustUtil.elemToString(rstr, wsTrustVer)));
        }
        
        // Handle the RequestSecurityTokenResponse
        //final IssuedTokenContext context = new IssuedTokenContextImpl();
        try {
            processRequestSecurityTokenResponse(sctConfig, rst, rstr, itc);
        } catch (WSSecureConversationException ex){
            throw new RuntimeException(ex);
        }                
    }
    
    public void processRenew(final IssuedTokenContext itc){
        SCTokenConfiguration sctConfig = (SCTokenConfiguration)itc.getSecurityPolicy().get(0);
        WSSCVersion wsscVer = WSSCVersion.getInstance(sctConfig.getProtocol());
        WSTrustVersion wsTrustVer = null;
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13_NS_URI)){
            wsTrustVer = WSTrustVersion.WS_TRUST_13;
        }else{
            wsTrustVer = WSTrustVersion.WS_TRUST_10;
        }
        //==============================
        // Get Required policy assertions
        //==============================        
        
        int skl = DEFAULT_KEY_SIZE;        
        if(sctConfig.isSymmetricBinding()){            
            skl = sctConfig.getKeySize();
            if(skl<1){
                skl = DEFAULT_KEY_SIZE;
            }
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                        LogStringsMessages.WSSC_1006_SYM_BIN_KEYSIZE(skl, this.DEFAULT_KEY_SIZE));
            }
        }                
        
        //======================================
        // Create RequestSecurityToken for Renew
        //======================================
        BaseSTSRequest rst = null;
        try{
            rst = createRequestSecurityTokenForRenew(itc, sctConfig.getReqClientEntropy(),skl);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(""), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(""), ex);
        }/* catch (WSTrustException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0021_PROBLEM_CREATING_RST_TRUST(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0021_PROBLEM_CREATING_RST_TRUST(), ex);
        }*/
        createRenewSignaturePolicy(sctConfig.getSCToken());
        
        final BaseSTSResponse rstr = sendRequest(sctConfig, rst, itc.getEndpointAddress(), wsscVer.getSCTRenewRequestAction());
                
        if(log.isLoggable(Level.FINE)){            
            log.log(Level.FINE, 
                    LogStringsMessages.WSSC_1014_RECEIVED_SCT_RSTR_RENEW(WSTrustUtil.elemToString(rstr, wsTrustVer)));
        }
        
        try {
            processRequestSecurityTokenResponse(sctConfig, rst, rstr, itc);
        } catch (WSSecureConversationException ex){
            throw new RuntimeException(ex);
        }                
    }
    
    private BaseSTSResponse sendRequest(final SCTokenConfiguration sctConfig, final BaseSTSRequest rst, final String endPointAddress, final String action) {
        Marshaller marshaller;
        Unmarshaller unmarshaller;
        final JAXBContext jaxbContext;   
        WSSCVersion wsscVer = WSSCVersion.getInstance(sctConfig.getProtocol());        
        WSTrustVersion wsTrustVer = null;        
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13_NS_URI)){
            wsTrustVer = WSTrustVersion.WS_TRUST_13;
        }else{
            wsTrustVer = WSTrustVersion.WS_TRUST_10;
        }
        WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance(wsTrustVer);
        jaxbContext = WSTrustElementFactory.getContext(wsTrustVer);        
         try {
           marshaller = jaxbContext.createMarshaller();
           unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException ex){
           log.log(Level.SEVERE,"WSSC0016.problem.mar.unmar", ex);
          throw new RuntimeException("Problem creating JAXB Marshaller/Unmarshaller", ex);
        }
        
        final Message request = Messages.create(marshaller, eleFac.toJAXBElement(rst), sctConfig.getWSBinding().getSOAPVersion());    
        
        // Log Request created
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_1009_SEND_REQ_MESSAGE(printMessageAsString(request)));
        }
        Packet reqPacket = new Packet(request);
        if (sctConfig.getSCToken() != null){
            reqPacket.invocationProperties.put(SC_ASSERTION, sctConfig.getSCToken());
        }
        if (sctConfig.getPacket() != null){
            for(WSTrustConstants.STS_PROPERTIES stsProperty : WSTrustConstants.STS_PROPERTIES.values()) {
                reqPacket.invocationProperties.put(stsProperty.toString(),sctConfig.getPacket().invocationProperties.get(stsProperty.toString()));
            }
        }
        
        reqPacket.setEndPointAddressString(endPointAddress);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_1008_SET_EP_ADDRESS(endPointAddress));
        }
        
        // Add addressing headers to the message
        try{
            reqPacket = addAddressingHeaders(reqPacket, sctConfig.getWSDLPort(), sctConfig.getWSBinding(), action, sctConfig.getAddressingVersion());
        }catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0017_PROBLEM_ADD_ADDRESS_HEADERS(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0017_PROBLEM_ADD_ADDRESS_HEADERS(), ex);
        }
        
        // Ideally this property for enabling FI or not should be available to the pipeline.
        // As a workaround for now, we
        // copy the property for the client packet to the reqPacket mananually here.
        if (sctConfig.getPacket() != null){
            reqPacket.contentNegotiation = sctConfig.getPacket().contentNegotiation;
        }
        
        copyStandardSecurityProperties(sctConfig.getPacket(),reqPacket);
        
        // Send the message
        Packet respPacket = null;
        if(sctConfig.getClientTube() != null){            
            reqPacket = ((SecurityClientTube)sctConfig.getClientTube()).processClientRequestPacket(reqPacket);
            Tube tubeline = sctConfig.getNextTube();            
            Fiber fiber = getFiberEngine().createFiber(); 
            respPacket = fiber.runSync(tubeline, reqPacket);
            respPacket = ((SecurityClientTube)sctConfig.getClientTube()).processClientResponsePacket(respPacket);            
        }else{
            WSITClientAuthContext wsitAuthCtx = (WSITClientAuthContext)sctConfig.getOtherOptions().get("WSITClientAuthContext");
            if (wsitAuthCtx != null){
                try{
                    respPacket = wsitAuthCtx.secureRequest(reqPacket, null, true);
                } catch (XWSSecurityException e) {
                    throw new RuntimeException( e);
                }
            }
        }
        
        // Obtain the RequestSecurtyTokenResponse
        final Message response = respPacket.getMessage();
        BaseSTSResponse rstr = null;
        if (!response.isFault()){
            JAXBElement rstrEle = null;
            try {
                rstrEle = (JAXBElement)response.readPayloadAsJAXB(unmarshaller);
            }catch (JAXBException ex){
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0018_ERR_JAXB_RSTR(), ex);
                throw new RuntimeException(LogStringsMessages.WSSC_0018_ERR_JAXB_RSTR(), ex);
            }
            if (wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())) {
                try {
                    rstr = eleFac.createRSTRCollectionFrom(rstrEle);
                } catch (Exception e) {
                    rstr = eleFac.createRSTRFrom(rstrEle);
                }

            } else {
                rstr = eleFac.createRSTRFrom(rstrEle);
            }
        } else {
            try{
                // SOAPFaultBuilder builder = SOAPFaultBuilder.create(response);
                //throw (SOAPFaultException)builder.createException(null, response);
                throw new SOAPFaultException(response.readAsSOAPMessage().getSOAPBody().getFault());
            } catch (SOAPException ex){
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0022_PROBLEM_CREATING_FAULT(), ex);
                throw new RuntimeException(LogStringsMessages.WSSC_0022_PROBLEM_CREATING_FAULT(), ex);
            }
        }
        
        return rstr;
    }
    
    private AssertionSet getAssertions(final SecureConversationToken scToken) {
        return scToken.getBootstrapPolicy().getAssertionSet();
    }
    
    /*
    public IssuedTokenContext processCancellation(final IssuedTokenContext ctx, final WSDLPort wsdlPort, final WSBinding binding, final Pipe securityPipe, final Marshaller marshaller, final Unmarshaller unmarshaller, final String endPointAddress, final AddressingVersion addVer){
        
        //==============================
        // Create RequestSecurityToken
        //==============================
        BaseSTSRequest rst = null;
        try{
            rst = createRequestSecurityTokenForCancel(ctx);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_CANCEL), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_CANCEL), ex);
        }
        
        final BaseSTSResponse rstr = sendRequest(null, wsdlPort, binding, securityPipe, marshaller, unmarshaller, rst, wsscVer.getSCTCancelRequestAction(), endPointAddress, addVer);
        
        // Handle the RequestSecurityTokenResponse
        try {
            processRequestSecurityTokenResponse(rst, rstr, ctx);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0020_PROBLEM_CREATING_RSTR(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0020_PROBLEM_CREATING_RSTR(), ex);
        }
        
        return ctx;
    }*/
    
    public void processCancellation(final IssuedTokenContext itc){
        SCTokenConfiguration sctConfig = (SCTokenConfiguration)itc.getSecurityPolicy().get(0);
        WSSCVersion wsscVer = WSSCVersion.getInstance(sctConfig.getProtocol());
        WSTrustVersion wsTrustVer = null;
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13_NS_URI)){
            wsTrustVer = WSTrustVersion.WS_TRUST_13;
        }else{
            wsTrustVer = WSTrustVersion.WS_TRUST_10;
        }
        //==============================
        // Create RequestSecurityToken
        //==============================
        BaseSTSRequest rst = null;
        try{
            rst = createRequestSecurityTokenForCancel(sctConfig, itc);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_CANCEL), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_CANCEL), ex);
        }
        
        final BaseSTSResponse rstr = sendRequest(sctConfig, rst, itc.getEndpointAddress(), wsscVer.getSCTCancelRequestAction());
        
        if(log.isLoggable(Level.FINE)){            
            log.log(Level.FINE, 
                    LogStringsMessages.WSSC_1016_RECEIVED_SCT_RSTR_CANCEL(WSTrustUtil.elemToString(rstr, wsTrustVer)));
        }
        // Handle the RequestSecurityTokenResponse
        try {
            processRequestSecurityTokenResponse(sctConfig, rst, rstr, itc);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0020_PROBLEM_CREATING_RSTR(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0020_PROBLEM_CREATING_RSTR(), ex);
        }                
    }
        
    /*
    private BaseSTSResponse sendRequest(final PolicyAssertion issuedToken, final WSDLPort wsdlPort, final WSBinding binding, final Pipe securityPipe, final Marshaller marshaller, final Unmarshaller unmarshaller, final BaseSTSRequest rst, final String action, final String endPointAddress, final AddressingVersion addVer) {
        // Marshaller marshaller;
        //Unmarshaller unmarshaller;
        
        // try {
        //   marshaller = jbCxt.createMarshaller();
        // unmarshaller = jbCxt.createUnmarshaller();
        //} catch (JAXBException ex){
        //   log.log(Level.SEVERE,"WSSC0016.problem.mar.unmar", ex);
        //  throw new RuntimeException("Problem creating JAXB Marshaller/Unmarshaller", ex);
        //}
        
        final Message request = Messages.create(marshaller, eleFac.toJAXBElement(rst), binding.getSOAPVersion());    
        
        // Log Request created
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_1009_SEND_REQ_MESSAGE(printMessageAsString(request)));
        }
        Packet reqPacket = new Packet(request);
        if (issuedToken != null){
            reqPacket.invocationProperties.put(SC_ASSERTION, issuedToken);
        }
        if (packet != null){
            for(WSTrustConstants.STS_PROPERTIES stsProperty : WSTrustConstants.STS_PROPERTIES.values()) {
                reqPacket.invocationProperties.put(stsProperty.toString(),packet.invocationProperties.get(stsProperty.toString()));
            }
        }
        
        reqPacket.setEndPointAddressString(endPointAddress);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_1008_SET_EP_ADDRESS(endPointAddress));
        }
        
        // Add addressing headers to the message
        try{
            reqPacket = addAddressingHeaders(reqPacket, wsdlPort, binding, action, addVer);
        }catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0017_PROBLEM_ADD_ADDRESS_HEADERS(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0017_PROBLEM_ADD_ADDRESS_HEADERS(), ex);
        }
        
        // Ideally this property for enabling FI or not should be available to the pipeline.
        // As a workaround for now, we
        // copy the property for the client packet to the reqPacket mananually here.
        if (packet != null){
            reqPacket.contentNegotiation = packet.contentNegotiation;
        }
        
        copyStandardSecurityProperties(packet,reqPacket);
        
        // Send the message
        final Packet respPacket = securityPipe.process(reqPacket);
        
        // Obtain the RequestSecurtyTokenResponse
        final Message response = respPacket.getMessage();
        BaseSTSResponse rstr = null;
        if (!response.isFault()){
            JAXBElement rstrEle = null;
            try {
                rstrEle = (JAXBElement)response.readPayloadAsJAXB(unmarshaller);
            }catch (JAXBException ex){
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0018_ERR_JAXB_RSTR(), ex);
                throw new RuntimeException(LogStringsMessages.WSSC_0018_ERR_JAXB_RSTR(), ex);
            }
            if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())){
                rstr = eleFac.createRSTRCollectionFrom(rstrEle);    
            }else{
                rstr = eleFac.createRSTRFrom(rstrEle);
            }
        } else {
            try{
                // SOAPFaultBuilder builder = SOAPFaultBuilder.create(response);
                //throw (SOAPFaultException)builder.createException(null, response);
                throw new SOAPFaultException(response.readAsSOAPMessage().getSOAPBody().getFault());
            } catch (SOAPException ex){
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0022_PROBLEM_CREATING_FAULT(), ex);
                throw new RuntimeException(LogStringsMessages.WSSC_0022_PROBLEM_CREATING_FAULT(), ex);
            }
        }
        
        return rstr;
    }*/
    
    /*
    private RequestSecurityToken createRequestSecurityToken(final boolean reqClientEntropy,final int skl) throws WSSecureConversationException{
        
        final URI tokenType = URI.create(wsscVer.getSCTTokenTypeURI());
        final URI requestType = URI.create(wsTrustVer.getIssueRequestTypeURI());
        final SecureRandom random = new SecureRandom();
        final byte[] rawValue = new byte[skl/8];
        random.nextBytes(rawValue);
        final BinarySecret secret = eleFac.createBinarySecret(rawValue, wsTrustVer.getNonceBinarySecretTypeURI());
        final Entropy entropy = reqClientEntropy?eleFac.createEntropy(secret):null;
        
        RequestSecurityToken rst = null;
        try {
            rst = eleFac.createRSTForIssue(tokenType, requestType, null, null, null, entropy, null);
            rst.setKeySize(skl);
            rst.setKeyType(URI.create(wsTrustVer.getSymmetricKeyTypeURI()));
            rst.setComputedKeyAlgorithm(URI.create(wsTrustVer.getCKPSHA1algorithmURI()));
        } catch (WSTrustException ex){
            throw new WSSecureConversationException(ex);
        }
        
        return rst;
    }*/
    
    private RequestSecurityToken createRequestSecurityToken(final SCTokenConfiguration sctConfig, final boolean reqClientEntropy,final int skl) throws WSSecureConversationException{
        WSSCVersion wsscVer = WSSCVersion.getInstance(sctConfig.getProtocol());
        WSTrustVersion wsTrustVer = null;
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13_NS_URI)){
            wsTrustVer = WSTrustVersion.WS_TRUST_13;
        }else{
            wsTrustVer = WSTrustVersion.WS_TRUST_10;
        }
        WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance(wsTrustVer);
        final URI tokenType = URI.create(wsscVer.getSCTTokenTypeURI());
        final URI requestType = URI.create(wsTrustVer.getIssueRequestTypeURI());
        final SecureRandom random = new SecureRandom();
        final byte[] rawValue = new byte[skl/8];
        random.nextBytes(rawValue);
        final BinarySecret secret = eleFac.createBinarySecret(rawValue, wsTrustVer.getNonceBinarySecretTypeURI());
        final Entropy entropy = reqClientEntropy?eleFac.createEntropy(secret):null;
        Lifetime lifetime = null;
        if(sctConfig.getSCTokenTimeout() > 0){
            // Create Lifetime
            long currentTime = WSTrustUtil.getCurrentTimeWithOffset();
            lifetime = WSTrustUtil.createLifetime(currentTime, sctConfig.getSCTokenTimeout(), wsTrustVer);
        }
        RequestSecurityToken rst = null;
        try {
            rst = eleFac.createRSTForIssue(tokenType, requestType, null, null, null, entropy, lifetime);
            rst.setKeySize(skl);
            rst.setKeyType(URI.create(wsTrustVer.getSymmetricKeyTypeURI()));
            rst.setComputedKeyAlgorithm(URI.create(wsTrustVer.getCKPSHA1algorithmURI()));            
        } catch (WSTrustException ex){
            throw new WSSecureConversationException(ex);
        }
        
        if(log.isLoggable(Level.FINE)){
            log.log(Level.FINE, LogStringsMessages.WSSC_1011_CREATED_SCT_RST_ISSUE(WSTrustUtil.elemToString(rst, wsTrustVer)));
        }
        
        return rst;
    }
    
    private RequestSecurityToken createRequestSecurityTokenForRenew( final IssuedTokenContext itc, final boolean reqClientEntropy,final int skl) throws WSSecureConversationException{
        SCTokenConfiguration sctConfig = (SCTokenConfiguration)itc.getSecurityPolicy().get(0);
        WSSCVersion wsscVer = WSSCVersion.getInstance(sctConfig.getProtocol());
        WSTrustVersion wsTrustVer = null;
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13_NS_URI)){
            wsTrustVer = WSTrustVersion.WS_TRUST_13;
        }else{
            wsTrustVer = WSTrustVersion.WS_TRUST_10;
        }
        WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance(wsTrustVer);
        final URI tokenType = URI.create(wsscVer.getSCTTokenTypeURI());                
        final URI requestType = URI.create(wsTrustVer.getRenewRequestTypeURI());   
        final SecureRandom random = new SecureRandom();
        final byte[] rawValue = new byte[skl/8];
        random.nextBytes(rawValue);
        final BinarySecret secret = eleFac.createBinarySecret(rawValue, wsTrustVer.getNonceBinarySecretTypeURI());
        final Entropy entropy = reqClientEntropy?eleFac.createEntropy(secret):null;
        final RenewTarget target = eleFac.createRenewTarget((SecurityTokenReference)itc.getUnAttachedSecurityTokenReference());
        
        RequestSecurityToken rst = null;
        try {
            rst = eleFac.createRSTForRenew(tokenType, requestType, null, target, null, null);
            rst.setEntropy(entropy);
            rst.setKeySize(skl);
            rst.setKeyType(URI.create(wsTrustVer.getSymmetricKeyTypeURI()));
            rst.setComputedKeyAlgorithm(URI.create(wsTrustVer.getCKPSHA1algorithmURI()));
        } catch (WSTrustException ex){
            throw new WSSecureConversationException(ex);
        }
        Lifetime lifetime = null;
        if(sctConfig.getSCTokenTimeout() > 0){
            // Create Lifetime
            long currentTime = WSTrustUtil.getCurrentTimeWithOffset();
            lifetime = WSTrustUtil.createLifetime(currentTime, sctConfig.getSCTokenTimeout(), wsTrustVer);
            rst.setLifetime(lifetime);
        }
        
        //final RequestSecurityToken rst = eleFac.createRSTForRenew(null, requestType, null, target, null, null);        
        
        if(log.isLoggable(Level.FINE)){
            log.log(Level.FINE, LogStringsMessages.WSSC_1013_CREATED_SCT_RST_RENEW(WSTrustUtil.elemToString(rst, wsTrustVer)));
        }
        return rst;
    }
    
    private RequestSecurityToken createRequestSecurityTokenForCancel(final SCTokenConfiguration sctConfig, final IssuedTokenContext ctx) throws WSSecureConversationException{
        WSSCVersion wsscVer = WSSCVersion.getInstance(sctConfig.getProtocol());
        WSTrustVersion wsTrustVer = null;
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13_NS_URI)){            
            wsTrustVer = WSTrustVersion.WS_TRUST_13;
        }else{
            wsTrustVer = WSTrustVersion.WS_TRUST_10;
        }
        WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance(wsTrustVer);
        URI requestType = URI.create(wsTrustVer.getCancelRequestTypeURI());
        
        final CancelTarget target = eleFac.createCancelTarget((SecurityTokenReference)ctx.getUnAttachedSecurityTokenReference());
        final RequestSecurityToken rst = eleFac.createRSTForCancel(requestType, target);
        
        if(log.isLoggable(Level.FINE)){
            log.log(Level.FINE, LogStringsMessages.WSSC_1015_CREATED_SCT_RST_CANCEL(WSTrustUtil.elemToString(rst, wsTrustVer)));
        }
        
        return rst;
    }
    
    /*
    private RequestSecurityToken createRequestSecurityTokenForCancel(final IssuedTokenContext ctx) throws WSSecureConversationException{        
        URI requestType = null;
        requestType = URI.create(wsTrustVer.getCancelRequestTypeURI());
        
        final CancelTarget target = eleFac.createCancelTarget((SecurityTokenReference)ctx.getUnAttachedSecurityTokenReference());
        final RequestSecurityToken rst = eleFac.createRSTForCancel(requestType, target);
        
        return rst;
    }*/
    
    /*
    private void processRequestSecurityTokenResponse(final BaseSTSRequest rst, final BaseSTSResponse rstr, final IssuedTokenContext context)
    throws WSSecureConversationException {
        SCTokenConfiguration sctConfig = (SCTokenConfiguration)context.getSecurityPolicy().get(0);
        WSSCVersion wsscVer = WSSCVersion.getInstance(sctConfig.getProtocol());
        final WSSCClientContract contract = WSSCFactory.newWSSCClientContract(config);
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())){
            contract.handleRSTRC((RequestSecurityToken)rst, (RequestSecurityTokenResponseCollection)rstr, context);    
        }else{
            contract.handleRSTR((RequestSecurityToken)rst, (RequestSecurityTokenResponse)rstr, context);
        }
    }*/
    
    private void processRequestSecurityTokenResponse(final SCTokenConfiguration sctConfig, final BaseSTSRequest rst, final BaseSTSResponse rstr, final IssuedTokenContext context)
    throws WSSecureConversationException {
        WSSCVersion wsscVer = WSSCVersion.getInstance(sctConfig.getProtocol());
        final WSSCClientContract contract = WSSCFactory.newWSSCClientContract();
        if (wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())) {
            try {
                contract.handleRSTRC((RequestSecurityToken) rst, (RequestSecurityTokenResponseCollection) rstr, context);
            } catch (Exception ex) {
                contract.handleRSTR((RequestSecurityToken) rst, (RequestSecurityTokenResponse) rstr, context);
            }

        } else {
            contract.handleRSTR((RequestSecurityToken)rst, (RequestSecurityTokenResponse)rstr, context);
        }
    }
    
    private String printMessageAsString(final Message message) {
        final StringWriter writer = new StringWriter();
        final XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            final XMLStreamWriter streamWriter = factory.createXMLStreamWriter(writer);
            message.writeTo(streamWriter);
            streamWriter.flush();
            return writer.toString();
        } catch (XMLStreamException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0025_PROBLEM_PRINTING_MSG(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0025_PROBLEM_PRINTING_MSG(), ex);
        }
    }
    
    private Packet addAddressingHeaders(final Packet packet, final WSDLPort wsdlPort, final WSBinding binding, final String action, final AddressingVersion addVer)throws WSSecureConversationException {
        final HeaderList list = packet.getMessage().getHeaders();
        list.fillRequestAddressingHeaders(packet, addVer,binding.getSOAPVersion(),false,action);
        
        return packet;
    }
    
    private void copyStandardSecurityProperties(Packet packet, Packet requestPacket) {
        /*String username = (String) packet.invocationProperties.get(com.sun.xml.wss.XWSSConstants.USERNAME_PROPERTY);
        if (username != null) {
            requestPacket.invocationProperties.put(com.sun.xml.wss.XWSSConstants.USERNAME_PROPERTY, username);
        }
        String password = (String) packet.invocationProperties.get(com.sun.xml.wss.XWSSConstants.PASSWORD_PROPERTY);
        if (password != null) {
            requestPacket.invocationProperties.put(com.sun.xml.wss.XWSSConstants.PASSWORD_PROPERTY, password);
        }*/
        Set<String> set = packet.invocationProperties.keySet();        
        for (Iterator it = set.iterator(); it.hasNext();) {
                String key = (String)it.next();
                requestPacket.invocationProperties.put(key, packet.invocationProperties.get(key));
        }
    }
    
    private void createRenewSignaturePolicy(final Token token){        
        renewSignaturePolicy = new SignaturePolicy();
        //renewSignaturePolicy.setUUID(pid.generateID());
        renewSignaturePolicy.setUUID("_99");
        SecurityPolicyVersion spVersion = token.getSecurityPolicyVersion();
        SecureConversationTokenKeyBinding sct = new SecureConversationTokenKeyBinding();
        SecureConversationToken scToken = (SecureConversationToken)token;        
        if(scToken.isRequireDerivedKeys()){
                DerivedTokenKeyBinding dtKB =  new DerivedTokenKeyBinding();
                dtKB.setOriginalKeyBinding(sct);
                renewSignaturePolicy.setKeyBinding(dtKB);
                dtKB.setUUID("_100");
            }else{
                renewSignaturePolicy.setKeyBinding(sct);
            }
        if(spVersion == SecurityPolicyVersion.SECURITYPOLICY200507){
            sct.setIncludeToken(((Token)token).getIncludeToken());
        } else{
            // SecurityPolicy 1.2
            sct.setIncludeToken(SecurityPolicyVersion.SECURITYPOLICY200507.includeTokenAlwaysToRecipient);            
        }
        sct.setUUID(((Token)token).getTokenId());
        
        final AssertionSet assertions = getAssertions(scToken);                
        for(PolicyAssertion policyAssertion : assertions){            
            if(PolicyUtil.isBinding(policyAssertion, spVersion)){
                binding =(Binding) policyAssertion;
            }
        }
        //SignaturePolicy sp = scPlugin.getRenewSignaturePolicy();
        SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding)renewSignaturePolicy.getFeatureBinding();
        AlgorithmSuite as = binding.getAlgorithmSuite();
        SecurityPolicyUtil.setCanonicalizationMethod(spFB, as);        
    }
    
    public SignaturePolicy getRenewSignaturePolicy(){
        return this.renewSignaturePolicy;
    }
    
    public AlgorithmSuite getAlgorithmSuite(){
        return binding.getAlgorithmSuite();
    }
    
    private SecurityPolicyVersion getSPVersion(PolicyAssertion pa){
        String nsUri = pa.getName().getNamespaceURI();
        // Default SPVersion
        SecurityPolicyVersion spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
        // If spec version, update
        if(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri.equals(nsUri)){
            spVersion = SecurityPolicyVersion.SECURITYPOLICY12NS;
        }
        return spVersion;
    }
    
    private Engine getFiberEngine() {
        if (fiberEngine == null) {
            fiberEngine = Fiber.current().owner;
        }
        return fiberEngine;
    }
}
