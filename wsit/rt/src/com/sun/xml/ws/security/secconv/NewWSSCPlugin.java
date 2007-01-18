/*
 * NewWSSCPlugin.java
 *
 * Created on November 1, 2006, 11:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.IssuedTokenContext;
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
import javax.xml.soap.SOAPException;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.secconv.logging.LogDomainConstants;
import com.sun.xml.ws.security.secconv.logging.LogStringsMessages;
import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 *
 * @author kumar jayanti
 */
public class NewWSSCPlugin {
    
    Configuration config;
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSSC_IMPL_DOMAIN,
            LogDomainConstants.WSSC_IMPL_DOMAIN_BUNDLE);
    
    private static WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance();
    
    private static final int DEFAULT_KEY_SIZE = 256;
    private static final String SC_ASSERTION = "SecureConversationAssertion";
    private static final String FOR_ISSUE = "For Issue";
    private static final String FOR_CANCEL = "For Cancel";
    
    /** Creates a new instance of NewWSSCPlugin */
    public NewWSSCPlugin(Configuration config) {
        this.config = config;
    }
    
    
    public RequestSecurityToken createIssueRequest(final PolicyAssertion token){
        
        //==============================
        // Get Required policy assertions
        //==============================
        final SecureConversationToken scToken = (SecureConversationToken)token;
        final AssertionSet assertions = getAssertions(scToken);
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
        boolean reqClientEntropy = false;
        if(symBinding!=null){
            final AlgorithmSuite algoSuite = symBinding.getAlgorithmSuite();
            skl = algoSuite.getMinSKLAlgorithm();
            if(skl<1){
                skl = DEFAULT_KEY_SIZE;
            }
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                        LogStringsMessages.WSSC_1006_SYM_BIN_KEYSIZE(skl, DEFAULT_KEY_SIZE));
            }
        }
        if(trust10 != null){
            final Set trustReqdProps = trust10.getRequiredProperties();
            reqClientEntropy = trustReqdProps.contains(Constants.REQUIRE_CLIENT_ENTROPY);
        }
        
        //==============================
        // Create RequestSecurityToken
        //==============================
        RequestSecurityToken rst = null;
        try{
            rst = createRequestSecurityToken(reqClientEntropy,skl);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_ISSUE), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_ISSUE), ex);
        } catch (WSTrustException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0021_PROBLEM_CREATING_RST_TRUST(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0021_PROBLEM_CREATING_RST_TRUST(), ex);
        }
        
        return rst;
    }
    
    public Packet  createIssuePacket(
            final PolicyAssertion token, final RequestSecurityToken rst, final WSDLPort wsdlPort, final WSBinding binding, final JAXBContext jbCxt, final String endPointAddress, final Packet packet) {
        final Packet ret = createSendRequestPacket(
                token, wsdlPort, binding,  jbCxt, rst, WSSCConstants.REQUEST_SECURITY_CONTEXT_TOKEN_ACTION, endPointAddress, packet);
        
        return ret;
    }
    
    public RequestSecurityTokenResponse getRSTR(final JAXBContext jbCxt, final Packet respPacket) {
        Unmarshaller unmarshaller;
        
        try {
            unmarshaller = jbCxt.createUnmarshaller();
        } catch (JAXBException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0016_PROBLEM_MAR_UNMAR(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0016_PROBLEM_MAR_UNMAR(), ex);
        }
        
        // Obtain the RequestSecurtyTokenResponse
        final Message response = respPacket.getMessage();
        RequestSecurityTokenResponse rstr = null;
        if (!response.isFault()){
            JAXBElement rstrEle = null;
            try {
                rstrEle = (JAXBElement)response.readPayloadAsJAXB(unmarshaller);
            }catch (JAXBException ex){
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0018_ERR_JAXB_RSTR(),ex);
                throw new RuntimeException( LogStringsMessages.WSSC_0018_ERR_JAXB_RSTR(),ex);
            }
            rstr = eleFac.createRSTRFrom(rstrEle);
        } else {
            try{
                throw new SOAPFaultException(response.readAsSOAPMessage().getSOAPBody().getFault());
            } catch (SOAPException ex){
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0022_PROBLEM_CREATING_FAULT(), ex);
                throw new RuntimeException(LogStringsMessages.WSSC_0022_PROBLEM_CREATING_FAULT(), ex);
            }
        }
        
        return rstr;
    }
    
    public IssuedTokenContext processRSTR(final IssuedTokenContext context,
            final RequestSecurityToken rst, final RequestSecurityTokenResponse rstr, final String endPointAddress) {
        
        // Handle the RequestSecurityTokenResponse
        //IssuedTokenContext context = new IssuedTokenContextImpl();
        try {
            processRequestSecurityTokenResponse(rst, rstr, context);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0023_ERROR_PROCESSING_RSTR(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0023_ERROR_PROCESSING_RSTR(), ex);
        }
        context.setEndpointAddress(endPointAddress);
        return context;
    }
    
    
    private AssertionSet getAssertions(final SecureConversationToken scToken) {
        return scToken.getBootstrapPolicy().getAssertionSet();
    }
    
    public RequestSecurityToken createCancelRequest(final IssuedTokenContext ctx) {
        //==============================
        // Create RequestSecurityToken
        //==============================
        RequestSecurityToken rst = null;
        try{
            rst = createRequestSecurityTokenForCancel(ctx);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_CANCEL), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_CANCEL), ex);
        }
        return rst;
    }
    
    public Packet  createCancelPacket(
            final RequestSecurityToken rst, final WSDLPort wsdlPort, final WSBinding binding, final JAXBContext jbCxt, final String endPointAddress) {
        final Packet ret = createSendRequestPacket(
                null, wsdlPort, binding,  jbCxt, rst, WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_ACTION, endPointAddress, null);
        return ret;
    }
    
    
    public IssuedTokenContext processCancellation(final IssuedTokenContext ctx, final WSDLPort wsdlPort, final WSBinding binding, final Pipe securityPipe, final JAXBContext jbCxt, final String endPointAddress){
        
        //==============================
        // Create RequestSecurityToken
        //==============================
        RequestSecurityToken rst = null;
        try{
            rst = createRequestSecurityTokenForCancel(ctx);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_CANCEL), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_CANCEL), ex);
        }
        
        final RequestSecurityTokenResponse rstr = sendRequest(null, wsdlPort, binding, securityPipe, jbCxt, rst, WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_ACTION, endPointAddress, null);
        
        // Handle the RequestSecurityTokenResponse
        try {
            processRequestSecurityTokenResponse(rst, rstr, ctx);
        } catch (WSSecureConversationException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0020_PROBLEM_CREATING_RSTR(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0020_PROBLEM_CREATING_RSTR(), ex);
        }
        
        return ctx;
    }
    
    private Packet createSendRequestPacket(
            final PolicyAssertion issuedToken, final WSDLPort wsdlPort, final WSBinding binding, final JAXBContext jbCxt, final RequestSecurityToken rst, final String action, final String endPointAddress, final Packet packet) {
        Marshaller marshaller;
        
        try {
            marshaller = jbCxt.createMarshaller();
        } catch (JAXBException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0016_PROBLEM_MAR_UNMAR(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0016_PROBLEM_MAR_UNMAR(), ex);
        }
        
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
            reqPacket = addAddressingHeaders(reqPacket, wsdlPort, binding, action);
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
        
        return reqPacket;
    }
    
    private RequestSecurityTokenResponse sendRequest(final PolicyAssertion issuedToken, final WSDLPort wsdlPort, final WSBinding binding, final Pipe securityPipe, final JAXBContext jbCxt, final RequestSecurityToken rst, final String action, final String endPointAddress, final Packet packet) {
        Marshaller marshaller;
        Unmarshaller unmarshaller;
        
        try {
            marshaller = jbCxt.createMarshaller();
            unmarshaller = jbCxt.createUnmarshaller();
        } catch (JAXBException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0016_PROBLEM_MAR_UNMAR(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0016_PROBLEM_MAR_UNMAR(), ex);
        }
        
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
        log.log(Level.FINE,
                LogStringsMessages.WSSC_1008_SET_EP_ADDRESS(endPointAddress));
        
        // Add addressing headers to the message
        try{
            reqPacket = addAddressingHeaders(reqPacket, wsdlPort, binding, action);
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
        
        // Send the message
        final Packet respPacket = securityPipe.process(reqPacket);
        
        // Obtain the RequestSecurtyTokenResponse
        final Message response = respPacket.getMessage();
        RequestSecurityTokenResponse rstr = null;
        if (!response.isFault()){
            JAXBElement rstrEle = null;
            try {
                rstrEle = (JAXBElement)response.readPayloadAsJAXB(unmarshaller);
            }catch (JAXBException ex){
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0018_ERR_JAXB_RSTR(), ex);
                throw new RuntimeException(LogStringsMessages.WSSC_0018_ERR_JAXB_RSTR(), ex);
            }
            rstr = eleFac.createRSTRFrom(rstrEle);
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
    
    private RequestSecurityToken createRequestSecurityToken(final boolean reqClientEntropy,final int skl) throws WSSecureConversationException, WSTrustException{
        
        final URI tokenType = URI.create(WSSCConstants.SECURITY_CONTEXT_TOKEN_TYPE);
        final URI requestType = URI.create(WSTrustConstants.ISSUE_REQUEST);
        final SecureRandom random = new SecureRandom();
        final byte[] rawValue = new byte[skl/8];
        random.nextBytes(rawValue);
        final BinarySecret secret = eleFac.createBinarySecret(rawValue, BinarySecret.NONCE_KEY_TYPE);
        final Entropy entropy = reqClientEntropy?eleFac.createEntropy(secret):null;
        
        RequestSecurityToken rst = null;
        try {
            rst = eleFac.createRSTForIssue(tokenType, requestType, null, null, null, entropy, null);
        } catch (WSTrustException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_ISSUE), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0024_ERROR_CREATING_RST(FOR_ISSUE), ex);
        }
        rst.setKeySize(skl);
        rst.setKeyType(URI.create(WSTrustConstants.SYMMETRIC_KEY));
        rst.setComputedKeyAlgorithm(URI.create(WSTrustConstants.CK_PSHA1));
        
        return rst;
    }
    
    private RequestSecurityToken createRequestSecurityTokenForCancel(final IssuedTokenContext ctx) throws WSSecureConversationException{
        URI requestType = null;
        requestType = URI.create(WSTrustConstants.CANCEL_REQUEST);
        
        final CancelTarget target = eleFac.createCancelTarget((SecurityTokenReference)ctx.getUnAttachedSecurityTokenReference());
        final RequestSecurityToken rst = eleFac.createRSTForCancel(requestType, target);
        
        return rst;
    }
    
    private void processRequestSecurityTokenResponse(final RequestSecurityToken rst, final RequestSecurityTokenResponse rstr, final IssuedTokenContext context)
    throws WSSecureConversationException {
        final WSSCClientContract contract = WSSCFactory.newWSSCClientContract(config);
        contract.handleRSTR(rst, rstr, context);
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
    
    private Packet addAddressingHeaders(final Packet packet, final WSDLPort wsdlPort, final WSBinding binding, final String action)throws WSSecureConversationException {
        final HeaderList headers = packet.getMessage().getHeaders();
        headers.fillRequestAddressingHeaders(packet, binding.getAddressingVersion(),binding.getSOAPVersion(),false,action);
        
        return packet;
    }
}
