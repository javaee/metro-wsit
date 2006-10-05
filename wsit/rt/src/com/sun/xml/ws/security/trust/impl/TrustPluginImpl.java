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
import com.sun.xml.ws.security.trust.elements.Claims;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.bind.JAXBElement;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

/**
 *
 * @author hr124446
 */
public class TrustPluginImpl implements TrustPlugin {
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    private Configuration config;
    private static WSTrustElementFactory fact = WSTrustElementFactory.newInstance();
    
    /** Creates a new instance of TrustPluginImpl */
    public TrustPluginImpl(Configuration config) {
        this.config = config;
    }
    
    /**
     * Obtain the Token by using WS-Trust or WS-SecureConversation.
     * @param issuedToken, an instance of <sp:IssuedToken> or <sp:SecureConversation> assertion
     * @return issuedTokenContext, a context containing the issued Token and related information
     */
    public IssuedTokenContext process(PolicyAssertion token, URI stsEP, URI wsdlLocation, QName serviceName, QName portName, String appliesTo){
        IssuedToken issuedToken = (IssuedToken)token;
        RequestSecurityTokenTemplate rstTemplate = issuedToken.getRequestSecurityTokenTemplate();
        String stsURI = null;
        if (stsEP == null){
            stsURI = getSTSURI(issuedToken);
        }else{
            stsURI = stsEP.toString();
        }
        URI metadataAddress = null;
        try {
            metadataAddress = getAddressFromMetadata(issuedToken);
        } catch (MalformedURLException ex) {
            log.log(Level.WARNING, "WST1011.problem.metadata", ex);
        }
        if(metadataAddress != null){
            wsdlLocation = metadataAddress;
        }
        
        RequestSecurityTokenResponse result = null;
        try {
            RequestSecurityToken request = createRequest(rstTemplate, appliesTo);
            result = invokeRST(request, wsdlLocation.toURL(), serviceName, portName, stsURI);
            IssuedTokenContext itc = new IssuedTokenContextImpl();
            WSTrustClientContract contract = WSTrustFactory.createWSTrustClientContract(config);
            contract.handleRSTR(request, result, itc);
            return itc;
        } catch (RemoteException ex) {
            log.log(Level.SEVERE, "WST0016.problem.itCtx", ex);
            throw new RuntimeException(ex.toString());
        } catch (URISyntaxException ex){
            log.log(Level.SEVERE, "WST0016.problem.itCtx", ex);
            throw new RuntimeException(ex.toString());
        } catch (MalformedURLException ex){
            log.log(Level.SEVERE, "WST0016.problem.itCtx", ex);
            throw new RuntimeException(ex.toString());
        } catch (WSTrustException ex){
            log.log(Level.SEVERE, "WST0016.problem.itCtx", ex);
            throw new RuntimeException(ex.toString());
        }
    }
    
    private IssuedTokenContext createIssuedTokenContext(final RequestSecurityTokenResponse rstr) {
        URI tokType = rstr.getTokenType();
        long keySize = rstr.getKeySize();
        URI keyType = rstr.getKeyType();
        RequestedSecurityToken securityToken = rstr.getRequestedSecurityToken();
        RequestedAttachedReference attachedRef = rstr.getRequestedAttachedReference();
        RequestedUnattachedReference unattachedRef = rstr.getRequestedUnattachedReference();
        RequestedProofToken proofToken = rstr.getRequestedProofToken();
        IssuedTokenContext itc = new IssuedTokenContextImpl();
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
    
    private RequestSecurityToken createRequest(final RequestSecurityTokenTemplate rstTemplate, String appliesTo) throws URISyntaxException, WSTrustException, NumberFormatException {
        URI requestType = URI.create(WSTrustConstants.ISSUE_REQUEST);
        AppliesTo at = null;
        if (appliesTo != null){
            at = WSTrustUtil.createAppliesTo(appliesTo);
        }
        
        int len = 32;
        long keySize = rstTemplate.getKeySize();
        if (keySize > 0){
            len = (int)keySize/8;
        }
        
        SecureRandom sr = new SecureRandom();
        byte[] nonce = new byte[len];
        sr.nextBytes(nonce);
        BinarySecret binarySecret = fact.createBinarySecret(nonce, BinarySecret.NONCE_KEY_TYPE);
        Entropy entropy = fact.createEntropy(binarySecret);
        URI tokenType = new URI(WSTrustConstants.SAML11_ASEERTION_TOKEN_TYPE);
        if (rstTemplate.getTokenType() != null){
            tokenType = new URI(rstTemplate.getTokenType().trim());
        }
        URI context = null;
        Claims claims = null;
        Lifetime lifetime = null;
        RequestSecurityToken requestSecurityToken= fact.createRSTForIssue(tokenType,requestType,context,at,claims,entropy,lifetime);
        
        if (keySize > 0){
            requestSecurityToken.setKeySize(keySize);
        }
        
        String keyType = rstTemplate.getKeyType();
        if (keyType != null){
            requestSecurityToken.setKeyType(new URI(keyType.trim()));
        }
        requestSecurityToken.setComputedKeyAlgorithm(URI.create(WSTrustConstants.CK_PSHA1));
        
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,"WST1006.created.rst.issue", new Object[]{elemToString(requestSecurityToken)});
        }
        
        return requestSecurityToken;
    }
    
    private RequestSecurityTokenResponse invokeRST(RequestSecurityToken request, URL wsdlLocation, QName serviceName, QName portName, String stsURI) throws RemoteException, WSTrustException {
        if(serviceName == null || portName==null){
            //we have to get the serviceName and portName through MEX
            log.log(Level.FINE, "WST1012.service.portname.mex",
                    new Object[] {serviceName, portName});
            if(stsURI == null){
                //could not get the STS location from the IssuedToken
                //try to get it from client configuration
                if(wsdlLocation != null){
                    stsURI = wsdlLocation.toString();
                    log.log(Level.FINE, "WST1013.sts.uri.client", new Object[] {stsURI});
                }else{
                    //sts location could not be obtained from either IssuedToken or
                    //from client configuration
                    log.log(Level.SEVERE,
                            "WST0029.could.not.get.sts.location",
                            new IllegalArgumentException("STS information not passed"));
                }
            }
            //do the actual mex request to the stsURI
            QName[] names = doMexRequest(stsURI);
            if(names!=null){
                serviceName = names[0];
                portName = names[1];
            }else{
                log.log(Level.SEVERE, "WST0017.service.portname.error",
                        new Object[] {serviceName, portName});
                throw new WSTrustException("Could not obtain STS service and port names through MEX");
            }
        }
        Service service = Service.create(wsdlLocation, serviceName);
        Dispatch<Object> dispatch = service.createDispatch(portName, fact.getContext(), Service.Mode.PAYLOAD);
        //dispatch = addAddressingHeaders(dispatch);
        if (stsURI != null){
            dispatch.getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, stsURI);
        }
        dispatch.getRequestContext().put(WSTrustConstants.IS_TRUST_MESSAGE, true);
        RequestSecurityTokenResponse rstr =  fact.createRSTRFrom((JAXBElement)dispatch.invoke(fact.toJAXBElement(request)));
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,"WST1014.response.invoking.rst", new Object[]{elemToString(rstr)});
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
    protected static QName[]  doMexRequest(final String stsURI) {
        MetadataClient mexClient = new MetadataClient();
        String stsAddress = stsURI;
        Metadata metadata = mexClient.retrieveMetadata(stsAddress);
        
        //this method gives the names of services and the corresponding port details
        List<PortInfo> ports = mexClient.getServiceInformation(metadata);
        
        //we have to iterate through this to get the appropriate serviceName and portname
        for(PortInfo port : ports){
            String uri = port.getAddress();
            
            //if the stsAddress what we have matches the address of this port, return
            //this port information
            if(uri.equals(stsAddress)){
                QName[] serviceInfo = new QName[2];
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
    private String getSTSURI(final IssuedToken issuedToken) {
        Issuer issuer = issuedToken.getIssuer();
        if(issuer != null){
            Address address = issuer.getAddress();
            if (address != null){
                URI uri = address.getURI();
                return uri.toString();
            }
        }
        return null;
    }
    
    private URI getAddressFromMetadata(final IssuedToken issuedToken) throws MalformedURLException {
        PolicyAssertion issuer = (PolicyAssertion)issuedToken.getIssuer();
        PolicyAssertion metadata = null;
        PolicyAssertion metadataSection = null;
        PolicyAssertion metadataReference = null;
        Address address = null;
        if(issuer != null){
            if ( issuer.hasNestedAssertions() ) {
                Iterator <PolicyAssertion> it = issuer.getNestedAssertionsIterator();
                while ( it.hasNext() ) {
                    PolicyAssertion assertion = it.next();
                    if ( WSTrustUtil.isMetadata(assertion)) {
                        metadata = assertion;
                        break;
                    }
                }
            }
        }
        if(metadata != null){
            if ( metadata.hasNestedAssertions() ) {
                Iterator <PolicyAssertion> it = metadata.getNestedAssertionsIterator();
                while ( it.hasNext() ) {
                    PolicyAssertion assertion = it.next();
                    if ( WSTrustUtil.isMetadataSection(assertion)) {
                        metadataSection = assertion;
                        break;
                    }
                }
            }
            
        }
        if(metadataSection != null){
            if ( metadataSection.hasNestedAssertions() ) {
                Iterator <PolicyAssertion> it = metadataSection.getNestedAssertionsIterator();
                while ( it.hasNext() ) {
                    PolicyAssertion assertion = it.next();
                    if ( WSTrustUtil.isMetadataReference(assertion)) {
                        metadataReference = assertion;
                        break;
                    }
                }
            }
            
        }
        if(metadataReference != null){
            if ( metadataReference.hasNestedAssertions() ) {
                Iterator <PolicyAssertion> it = metadataReference.getNestedAssertionsIterator();
                while ( it.hasNext() ) {
                    PolicyAssertion assertion = it.next();
                    if ( PolicyUtil.isAddress(assertion)) {
                        address = (Address)assertion;
                        return address.getURI();
                    }
                }
            }
            
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
    private String elemToString(RequestSecurityToken rst) {
        try {
            javax.xml.bind.Marshaller marshaller = fact.getContext().createMarshaller();
            JAXBElement<RequestSecurityTokenType> rstElement =  (new ObjectFactory()).createRequestSecurityToken((RequestSecurityTokenType)rst);
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            java.io.StringWriter sw = new java.io.StringWriter();
            marshaller.marshal(rstElement, sw);
            return sw.toString();
        } catch (Exception e) {
            log.log(Level.FINE, "WST1004.error.marshal.toString", e);
            throw new RuntimeException("Error in Marshalling RST to string for logging ", e);
        }
    }
    
    private String elemToString(RequestSecurityTokenResponse rstr){
        try {
            javax.xml.bind.Marshaller marshaller = fact.getContext().createMarshaller();
            JAXBElement<RequestSecurityTokenResponseType> rstrElement =  (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            java.io.StringWriter sw = new java.io.StringWriter();
            marshaller.marshal(rstrElement, sw);
            return sw.toString();
        } catch (Exception e) {
            log.log(Level.FINE, "WST1004.error.marshal.toString", e);
            throw new RuntimeException("Error in Marshalling RSTR to string for logging ", e);
        }
    }
    
}
