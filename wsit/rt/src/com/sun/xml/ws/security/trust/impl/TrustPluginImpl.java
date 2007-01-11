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
 * TrustPluginImpl.java
 *
 * Created on January 2, 2006, 10:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.addressing.policy.Address;
import com.sun.xml.ws.mex.client.MetadataClient;
import com.sun.xml.ws.mex.client.PortInfo;
import com.sun.xml.ws.mex.client.schema.Metadata;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.policy.IssuedToken;
import com.sun.xml.ws.security.policy.Issuer;
import com.sun.xml.ws.security.policy.RequestSecurityTokenTemplate;
import com.sun.xml.ws.security.trust.*;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenResponseType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenType;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.bind.JAXBElement;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

import javax.xml.ws.soap.AddressingFeature;

import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

/**
 *
 * @author hr124446
 */
public class TrustPluginImpl implements TrustPlugin {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    private final Configuration config;
    private static WSTrustElementFactory fact = WSTrustElementFactory.newInstance();
    
    private static final String PRE_CONFIGURED_STS = "PreconfiguredSTS";
    private static final String NAMESPACE = "namespace";
    private static final String CONFIG_NAMESPACE = "";
    private static final String ENDPOINT = "endpoint";
    private static final String METADATA = "metadata";
    private static final String WSDL_LOCATION = "wsdlLocation";
    private static final String SERVICE_NAME = "serviceName";
    private static final String PORT_NAME = "portName";
    
    /** Creates a new instance of TrustPluginImpl */
    public TrustPluginImpl(Configuration config) {
        this.config = config;
    }
    
    /**
     * Obtain the Token by using WS-Trust or WS-SecureConversation.
     * @param issuedToken, an instance of <sp:IssuedToken> or <sp:SecureConversation> assertion
     * @return issuedTokenContext, a context containing the issued Token and related information
     */
    public IssuedTokenContext process(final PolicyAssertion token, final PolicyAssertion localToken, final String appliesTo){
        final IssuedToken issuedToken = (IssuedToken)token;
        final RequestSecurityTokenTemplate rstTemplate = issuedToken.getRequestSecurityTokenTemplate();
        URI stsURI =  getSTSURI(issuedToken);
        URI wsdlLocation = null;
        QName serviceName = null;
        QName portName = null;
        
        // Get STS information from IssuedToken
        if (stsURI != null){
            URI metadataAddress = null;
            try {
                metadataAddress = getAddressFromMetadata(issuedToken);
            } catch (MalformedURLException ex) {
                if (log.isLoggable(Level.WARNING)) {
                    log.log(Level.WARNING,
                            LogStringsMessages.WST_1011_PROBLEM_METADATA(stsURI), ex);
                }
            }
            
            if(metadataAddress != null){
                wsdlLocation = metadataAddress;
            }else{
                wsdlLocation = stsURI;
            }
        }else if (localToken != null){
            // Get STS information from local configuration
            if (PRE_CONFIGURED_STS.equals(localToken.getName().getLocalPart())) {
                final Map<QName,String> attrs = localToken.getAttributes();
                final String namespace = attrs.get(new QName(CONFIG_NAMESPACE,NAMESPACE));
                try {
                    final String stsEPStr = attrs.get(new QName(CONFIG_NAMESPACE,ENDPOINT));
                    if (stsEPStr != null){
                        stsURI = new URI(stsEPStr);
                    }
                    
                    final String metadataStr = attrs.get(new QName(CONFIG_NAMESPACE, METADATA));
                    if (metadataStr != null){
                        wsdlLocation = new URI(metadataStr);
                    }
                    
                    final String wsdlLocationStr = attrs.get(new QName(CONFIG_NAMESPACE,WSDL_LOCATION));
                    if (wsdlLocationStr != null){
                        wsdlLocation = new URI(wsdlLocationStr);
                    }
                } catch (URISyntaxException ex) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WST_0014_URI_SYNTAX(ex));
                    throw new RuntimeException("Invalid URI", ex);
                }
                
                final String serviceNameStr = attrs.get(new QName(CONFIG_NAMESPACE,SERVICE_NAME));
                if (serviceNameStr != null && namespace != null){
                    serviceName = new QName(namespace,serviceNameStr);
                }
                
                if (wsdlLocation == null){
                    wsdlLocation = stsURI;
                }
                
                final String portNameStr = attrs.get(new QName(CONFIG_NAMESPACE,PORT_NAME));
                if (portNameStr != null && namespace != null){
                    portName = new QName(namespace, portNameStr);
                }
            }
        }
        
        if(stsURI == null){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0029_COULD_NOT_GET_STS_LOCATION());
            throw new RuntimeException(LogStringsMessages.WST_0029_COULD_NOT_GET_STS_LOCATION());
        }
        
        RequestSecurityTokenResponse result = null;
        try {
            final RequestSecurityToken request = createRequest(rstTemplate, appliesTo);
            result = invokeRST(request, wsdlLocation, serviceName, portName, stsURI.toString());
            final IssuedTokenContext itc = new IssuedTokenContextImpl();
            final WSTrustClientContract contract = WSTrustFactory.createWSTrustClientContract(config);
            contract.handleRSTR(request, result, itc);
            return itc;
        } catch (RemoteException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0016_PROBLEM_IT_CTX());
            throw new RuntimeException(LogStringsMessages.WST_0016_PROBLEM_IT_CTX(), ex);
        } catch (URISyntaxException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0016_PROBLEM_IT_CTX(), ex);
            throw new RuntimeException(LogStringsMessages.WST_0016_PROBLEM_IT_CTX());
        } catch (WSTrustException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0016_PROBLEM_IT_CTX(), ex);
            throw new RuntimeException(LogStringsMessages.WST_0016_PROBLEM_IT_CTX());
        }
    }
    
    private IssuedTokenContext createIssuedTokenContext(final RequestSecurityTokenResponse rstr) {
        final URI tokType = rstr.getTokenType();
        final long keySize = rstr.getKeySize();
        final URI keyType = rstr.getKeyType();
        final RequestedSecurityToken securityToken = rstr.getRequestedSecurityToken();
        final RequestedAttachedReference attachedRef = rstr.getRequestedAttachedReference();
        final RequestedUnattachedReference unattachedRef = rstr.getRequestedUnattachedReference();
        final RequestedProofToken proofToken = rstr.getRequestedProofToken();
        final IssuedTokenContext itc = new IssuedTokenContextImpl();
        itc.setSecurityToken(securityToken.getToken());
        if(proofToken != null){
            itc.setAssociatedProofToken(proofToken.getSecurityTokenReference());
        }
        if(attachedRef != null){
            itc.setAttachedSecurityTokenReference(attachedRef.getSTR());
        }else{
            itc.setUnAttachedSecurityTokenReference(unattachedRef.getSTR());
        }
        return itc;
    }
    
    private RequestSecurityToken createRequest(final RequestSecurityTokenTemplate rstTemplate, final String appliesTo) throws URISyntaxException, WSTrustException, NumberFormatException {
        final URI requestType = URI.create(WSTrustConstants.ISSUE_REQUEST);
        AppliesTo applTo = null;
        if (appliesTo != null){
            applTo = WSTrustUtil.createAppliesTo(appliesTo);
        }
        
        int len = 32;
        final long keySize = rstTemplate.getKeySize();
        if (keySize > 0){
            len = (int)keySize/8;
        }
        
        final SecureRandom secRandom = new SecureRandom();
        final byte[] nonce = new byte[len];
        secRandom.nextBytes(nonce);
        final BinarySecret binarySecret = fact.createBinarySecret(nonce, BinarySecret.NONCE_KEY_TYPE);
        final Entropy entropy = fact.createEntropy(binarySecret);
        URI tokenType = new URI(WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE);
        if (rstTemplate.getTokenType() != null){
            tokenType = new URI(rstTemplate.getTokenType().trim());
        }
        final URI context = null;
        final Claims claims = null;
        final Lifetime lifetime = null;
        final RequestSecurityToken rst= fact.createRSTForIssue(tokenType,requestType,context,applTo,claims,entropy,lifetime);
        
        if (keySize > 0){
            rst.setKeySize(keySize);
        }
        
        final String keyType = rstTemplate.getKeyType();
        if (keyType != null){
            rst.setKeyType(new URI(keyType.trim()));
        }
        rst.setComputedKeyAlgorithm(URI.create(WSTrustConstants.CK_PSHA1));
        
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WST_1006_CREATED_RST_ISSUE(elemToString(rst)));
        }
        
        return rst;
    }
    
    private RequestSecurityTokenResponse invokeRST(final RequestSecurityToken request, final URI wsdlLocation, QName serviceName, QName portName, String stsURI) throws RemoteException, WSTrustException {
        
        if(serviceName == null || portName==null){
            //we have to get the serviceName and portName through MEX
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                        LogStringsMessages.WST_1012_SERVICE_PORTNAME_MEX(serviceName, portName));
            }
            if(stsURI == null){
                //could not get the STS location from the IssuedToken
                //try to get it from client configuration
                
                stsURI = wsdlLocation.toString();
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE,
                            LogStringsMessages.WST_1013_STS_URI_CLIENT(stsURI));
                }
            }
            //do the actual mex request
            QName[] names = doMexRequest(wsdlLocation.toString(), stsURI);
            if(names!=null){
                serviceName = names[0];
                portName = names[1];
            }else{
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0017_SERVICE_PORTNAME_ERROR(serviceName, portName));
                throw new WSTrustException(
                        LogStringsMessages.WST_0017_SERVICE_PORTNAME_ERROR(serviceName, portName));
            }
        }
        
        Service service = null;
        try{
            service = Service.create(wsdlLocation.toURL(), serviceName);
        }catch (MalformedURLException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0016_PROBLEM_IT_CTX(), ex);
            throw new RuntimeException(LogStringsMessages.WST_0016_PROBLEM_IT_CTX());
        }
        final Dispatch<Object> dispatch = service.createDispatch(portName, fact.getContext(), Service.Mode.PAYLOAD, new WebServiceFeature[]{new RespectBindingFeature(), new AddressingFeature(false)});
        //Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE, new WebServiceFeature[]{new AddressingFeature(false)});
        //WSBinding wsbinding = (WSBinding) dispatch.getBinding();
        //AddressingVersion addVer = wsbinding.getAddressingVersion();
        //SOAPVersion sv = wsbinding.getSOAPVersion();
        
        //dispatch = addAddressingHeaders(dispatch);
        if (stsURI != null){
            dispatch.getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, stsURI);
        }
        dispatch.getRequestContext().put(WSTrustConstants.IS_TRUST_MESSAGE, "true");
        dispatch.getRequestContext().put(WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION, WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION);
        
        //RequestSecurityTokenResponse rstr = null;
        // try{
        //  MessageFactory factory = sv.saajMessageFactory;
        //  SOAPMessage message = factory.createMessage();
        //   message.getSOAPBody().addDocument(fact.toElement(request).getOwnerDocument());
        //   SOAPHeader header = message.getSOAPHeader();
        // SOAPHeaderElement action = header.addHeaderElement(addVer.actionTag);
        // action.addTextNode(WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION);
        // SOAPHeaderElement to = header.addHeaderElement(addVer.toTag);
        // to.addTextNode(stsURI.toString());
        // SOAPHeaderElement msgID = header.addHeaderElement(addVer.messageIDTag);
        // msgID.addTextNode("uuid:" + UUID.randomUUID().toString());
        // SOAPHeaderElement replyTo = header.addHeaderElement(addVer.replyToTag);
        // SOAPElement add = replyTo.addChildElement(new QName(addVer.nsUri, "Address"));
        // add.addTextNode(AddressingVersion.W3C.getAnonymousUri());
        //SOAPMessage response = (SOAPMessage)dispatch.invoke(message);
        // SOAPBody rsp = response.getSOAPBody();
        // Element rspEle = rsp.extractContentAsDocument().getDocumentElement();
        // rstr = fact.createRSTRFrom(rspEle);
        // } catch(Exception ex){
        // ex.printStackTrace();
        // }
        
        final RequestSecurityTokenResponse rstr =  fact.createRSTRFrom((JAXBElement)dispatch.invoke(fact.toJAXBElement(request)));
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WST_1014_RESPONSE_INVOKING_RST(elemToString(rstr)));
        }
        return rstr;
    }
    
    /**
     * This method uses mex client api to issue a mex request and return the
     * matching service name and port name
     * @param stsURI URI to the STS. Mex request will be issued to this address
     * @return List of 2 QName objects. The first one will be serviceName
     * and the second one will be portName.
     */
    protected static QName[]  doMexRequest(final String wsdlLocation, final String stsURI) {
        final MetadataClient mexClient = new MetadataClient();
        
        final Metadata metadata = mexClient.retrieveMetadata(wsdlLocation);
        
        //this method gives the names of services and the corresponding port details
        final List<PortInfo> ports = mexClient.getServiceInformation(metadata);
        
        //we have to iterate through this to get the appropriate serviceName and portname
        QName[] serviceInfo = new QName[2];
        for(PortInfo port : ports){
            final String uri = port.getAddress();
            
            //if the stsAddress what we have matches the address of this port, return
            //this port information
            if(uri.equals(stsURI)){
                serviceInfo[0]= port.getServiceName();
                serviceInfo[1]= port.getPortName();
                return serviceInfo;
            }
        }
        return null;
    }
    
    /**
     * method to examine the IssuedToken assertion and return the URI of the Issuer
     * endpoint reference.
     * @param issuedToken The issuedToken assertion
     * @return The URI of the Issuer in IssuedToken, which is nothing but the URI of STS.
     */
    private URI getSTSURI(final IssuedToken issuedToken) {
        final Issuer issuer = issuedToken.getIssuer();
        if(issuer != null){
            final Address address = issuer.getAddress();
            if (address != null){
                return address.getURI();
            }
        }
        return null;
    }
    
    private URI getAddressFromMetadata(final IssuedToken issuedToken) throws MalformedURLException {
        final PolicyAssertion issuer = (PolicyAssertion)issuedToken.getIssuer();
        PolicyAssertion addressingMetadata = null;
        PolicyAssertion metadata = null;
        PolicyAssertion metadataSection = null;
        PolicyAssertion metadataReference = null;
        Address address = null;
        if(issuer != null){
            address = ((Issuer)issuer).getAddress();
            
            if ( issuer.hasNestedAssertions() ) {
                final Iterator <PolicyAssertion> it = issuer.getNestedAssertionsIterator();
                while ( it.hasNext() ) {
                    final PolicyAssertion assertion = it.next();
                    if ( WSTrustUtil.isAddressingMetadata(assertion)) {
                        addressingMetadata = assertion;
                        break;
                    }
                }
            }
        }
        
        if(addressingMetadata != null){
            if ( addressingMetadata.hasNestedAssertions() ) {
                final Iterator <PolicyAssertion> it = addressingMetadata.getNestedAssertionsIterator();
                while ( it.hasNext() ) {
                    final PolicyAssertion assertion = it.next();
                    if ( WSTrustUtil.isMetadata(assertion)) {
                        metadata = assertion;
                        break;
                    }
                }
            }
        }
        
        if(metadata != null){
            if ( metadata.hasNestedAssertions() ) {
                final Iterator <PolicyAssertion> it = metadata.getNestedAssertionsIterator();
                while ( it.hasNext() ) {
                    final PolicyAssertion assertion = it.next();
                    if ( WSTrustUtil.isMetadataSection(assertion)) {
                        metadataSection = assertion;
                        break;
                    }
                }
            }
            
        }
        
        if(metadataSection != null){
            if ( metadataSection.hasNestedAssertions() ) {
                final Iterator <PolicyAssertion> it = metadataSection.getNestedAssertionsIterator();
                while ( it.hasNext() ) {
                    final PolicyAssertion assertion = it.next();
                    if ( WSTrustUtil.isMetadataReference(assertion)) {
                        metadataReference = assertion;
                        break;
                    }
                }
            }
            
        }
        if(metadataReference != null){
            if ( metadataReference.hasNestedAssertions() ) {
                final Iterator <PolicyAssertion> it = metadataReference.getNestedAssertionsIterator();
                while ( it.hasNext() ) {
                    final PolicyAssertion assertion = it.next();
                    if ( PolicyUtil.isAddress(assertion)) {
                        address = (Address)assertion;
                        // return address.getURI();
                    }
                }
            }
            
        }
        
        if (address != null){
            return address.getURI();
        }
        
        return null;
    }
    
 /*   private Dispatch<Object> addAddressingHeaders(Dispatch<Object> provider) {
        AddressingBuilder builder = AddressingBuilder.newInstance();
        AddressingProperties ap = builder.newAddressingProperties();
  
        // Action
        ap.setAction(builder.newURI(WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION));
        provider.getRequestContext().put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, ap);
  
        return provider;
    }*/
    
    /**
     * Prints out the RST created as string.
     * This method is primarily used for logging purposes.
     */
    private String elemToString(final RequestSecurityToken rst) {
        try {
            final javax.xml.bind.Marshaller marshaller = fact.getContext().createMarshaller();
            final JAXBElement<RequestSecurityTokenType> rstElement =  (new ObjectFactory()).createRequestSecurityToken((RequestSecurityTokenType)rst);
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            final java.io.StringWriter writer = new java.io.StringWriter();
            marshaller.marshal(rstElement, writer);
            return writer.toString();
        } catch (Exception e) {
            if(log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                        LogStringsMessages.WST_1004_ERROR_MARSHAL_TO_STRING(), e);
            }
            throw new RuntimeException("Error in Marshalling RST to string for logging ", e);
        }
    }
    
    private String elemToString(final RequestSecurityTokenResponse rstr){
        try {
            final javax.xml.bind.Marshaller marshaller = fact.getContext().createMarshaller();
            final JAXBElement<RequestSecurityTokenResponseType> rstrElement =  (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            final java.io.StringWriter writer = new java.io.StringWriter();
            marshaller.marshal(rstrElement, writer);
            return writer.toString();
        } catch (Exception e) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE,
                        LogStringsMessages.WST_1004_ERROR_MARSHAL_TO_STRING(), e);
            }
            throw new RuntimeException(LogStringsMessages.WST_1004_ERROR_MARSHAL_TO_STRING(), e);
        }
    }
    
}
