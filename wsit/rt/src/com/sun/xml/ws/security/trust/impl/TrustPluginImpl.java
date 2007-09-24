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
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.addressing.policy.Address;
import com.sun.xml.ws.mex.client.MetadataClient;
import com.sun.xml.ws.mex.client.PortInfo;
import com.sun.xml.ws.mex.client.schema.Metadata;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelGenerator;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.sourcemodel.XmlPolicyModelMarshaller;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.policy.IssuedToken;
import com.sun.xml.ws.security.policy.Issuer;
import com.sun.xml.ws.security.policy.RequestSecurityTokenTemplate;
import com.sun.xml.ws.security.trust.*;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.OnBehalfOf;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.impl.bindings.ClaimsType;
import com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenResponseType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenType;
import com.sun.xml.ws.security.trust.impl.elements.ClaimsImpl;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.util.JAXBResult;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.Dispatch;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.bind.JAXBElement;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

import javax.xml.ws.soap.AddressingFeature;

import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
import org.w3c.dom.Document;

import org.w3c.dom.Element;

import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;

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
    private static final String ENDPOINT = "endPoint";
    private static final String METADATA = "metadata";
    private static final String WSDL_LOCATION = "wsdlLocation";
    private static final String SERVICE_NAME = "serviceName";
    private static final String PORT_NAME = "portName";
    private static final String REQUEST_SECURITY_TOKEN_TEMPLATE = "RequestSecurityTokenTemplate";
    private static final String CLAIMS = "Claims";
    private static final String DIALECT = "Dialect";
    

    
    /** Creates a new instance of TrustPluginImpl */
    public TrustPluginImpl(Configuration config) {
        this.config = config;
    }
    
    public void process(IssuedTokenContext itc) throws WSTrustException{
        String appliesTo = itc.getEndpointAddress();
        STSIssuedTokenConfiguration stsConfig = (STSIssuedTokenConfiguration)itc.getSecurityPolicy().get(0);
        String stsURI = stsConfig.getSTSEndpoint();
        if(stsURI == null){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0029_COULD_NOT_GET_STS_LOCATION(appliesTo));
            throw new WebServiceException(LogStringsMessages.WST_0029_COULD_NOT_GET_STS_LOCATION(appliesTo));
        }
        
        URI wsdlLocation = null;
        QName serviceName = null;
        QName portName = null;
        
        final String metadataStr = stsConfig.getSTSMEXAddress();
        if (metadataStr != null){
            wsdlLocation = URI.create(metadataStr);
        }else{
            final String namespace = stsConfig.getSTSNamespace();
            String wsdlLocationStr = stsConfig.getSTSWSDLLocation();
            if (wsdlLocationStr == null){
                wsdlLocationStr = stsURI;
            }else{
                final String serviceNameStr = stsConfig.getSTSServiceName();
                if (serviceNameStr != null && namespace != null){
                      serviceName = new QName(namespace,serviceNameStr);
                }

                final String portNameStr = stsConfig.getSTSPortName();
                if (portNameStr != null && namespace != null){
                      portName = new QName(namespace, portNameStr);
                }
            }
            wsdlLocation = URI.create(wsdlLocationStr);
        }
        
        Token oboToken = stsConfig.getOBOToken();
       
        RequestSecurityTokenResponse result = null;
        try {
            final RequestSecurityToken request = createRequest(null, stsConfig, appliesTo, oboToken);
             
            result = invokeRST(request, wsdlLocation, serviceName, portName, stsURI);
            final WSTrustClientContract contract = WSTrustFactory.createWSTrustClientContract(config);
            contract.handleRSTR(request, result, itc);
        } catch (RemoteException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0016_PROBLEM_IT_CTX(stsURI, appliesTo), ex);
            throw new WSTrustException(LogStringsMessages.WST_0016_PROBLEM_IT_CTX(stsURI, appliesTo), ex);
        } catch (URISyntaxException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0016_PROBLEM_IT_CTX(stsURI, appliesTo), ex);
            throw new WSTrustException(LogStringsMessages.WST_0016_PROBLEM_IT_CTX(stsURI, appliesTo));
        }
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
            URI metadataAddress = getAddressFromMetadata(issuedToken);
            
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
                String stsEPStr = attrs.get(new QName(CONFIG_NAMESPACE,ENDPOINT));
                if (stsEPStr == null){
                    stsEPStr = attrs.get(new QName(CONFIG_NAMESPACE,ENDPOINT.toLowerCase()));
                }
                if (stsEPStr != null){
                    stsURI = URI.create(stsEPStr);
                }
                
                final String metadataStr = attrs.get(new QName(CONFIG_NAMESPACE, METADATA));
                if (metadataStr != null){
                    wsdlLocation = URI.create(metadataStr);
                }
                
                final String wsdlLocationStr = attrs.get(new QName(CONFIG_NAMESPACE,WSDL_LOCATION));
                if (wsdlLocationStr != null){
                    wsdlLocation = URI.create(wsdlLocationStr);
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
                    LogStringsMessages.WST_0029_COULD_NOT_GET_STS_LOCATION(appliesTo));
            throw new WebServiceException(LogStringsMessages.WST_0029_COULD_NOT_GET_STS_LOCATION(appliesTo));
        }
        
        RequestSecurityTokenResponse result = null;
        try {
            final RequestSecurityToken request = createRequest(rstTemplate, null, appliesTo, null);
            
            // handle Claims.
            // Not: should be in the RequestSecurityTokenTemplate api. Workaround for now.
            Claims claims = getClaims(token, appliesTo);
            if (claims != null){
                request.setClaims(claims);
            }

            
            result = invokeRST(request, wsdlLocation, serviceName, portName, stsURI.toString());
            final IssuedTokenContext itc = new IssuedTokenContextImpl();
            final WSTrustClientContract contract = WSTrustFactory.createWSTrustClientContract(config);
            contract.handleRSTR(request, result, itc);
            return itc;
        } catch (RemoteException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0016_PROBLEM_IT_CTX(stsURI, appliesTo), ex);
            throw new WebServiceException(LogStringsMessages.WST_0016_PROBLEM_IT_CTX(stsURI, appliesTo), ex);
        } catch (URISyntaxException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0016_PROBLEM_IT_CTX(stsURI, appliesTo), ex);
            throw new WebServiceException(LogStringsMessages.WST_0016_PROBLEM_IT_CTX(stsURI, appliesTo));
        } catch (WSTrustException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0016_PROBLEM_IT_CTX(stsURI, appliesTo), ex);
            throw new WebServiceException(LogStringsMessages.WST_0016_PROBLEM_IT_CTX(stsURI, appliesTo));
        }
    }
    
    private RequestSecurityToken createRequest(final RequestSecurityTokenTemplate rstTemplate, final STSIssuedTokenConfiguration stsConfig, final String appliesTo, final Token oboToken) throws URISyntaxException, WSTrustException, NumberFormatException {
        final URI requestType = URI.create(WSTrustConstants.ISSUE_REQUEST);
        AppliesTo applTo = null;
        if (appliesTo != null){
            applTo = WSTrustUtil.createAppliesTo(appliesTo);
        }
        
        long keySize = -1;
        String keyType = null;
        String tokenTypeStr = null;
        if (rstTemplate != null){
            keySize = rstTemplate.getKeySize();
            keyType = rstTemplate.getKeyType();
            tokenTypeStr = rstTemplate.getTokenType();
        }else if (stsConfig != null){
            keySize = stsConfig.getKeySize();
            keyType = stsConfig.getKeyType();
            tokenTypeStr = stsConfig.getTokenType();
        }
        
        int len = 32;
        if (keySize > 0){
            len = (int)keySize/8;
        }
        
        final SecureRandom secRandom = new SecureRandom();
        final byte[] nonce = new byte[len];
        secRandom.nextBytes(nonce);
        final BinarySecret binarySecret = fact.createBinarySecret(nonce, BinarySecret.NONCE_KEY_TYPE);
        final Entropy entropy = fact.createEntropy(binarySecret);
        URI tokenType = URI.create(WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE);
        if (tokenTypeStr != null){
            tokenType = new URI(tokenTypeStr.trim());
        }
        final URI context = null;
        final Claims claims = null;
        final Lifetime lifetime = null;
        final RequestSecurityToken rst= fact.createRSTForIssue(tokenType,requestType,context,applTo,claims,entropy,lifetime);
        
        if (keySize > 0){
            rst.setKeySize(keySize);
        }
        
        if (keyType != null){
            rst.setKeyType(new URI(keyType.trim()));
        }
        rst.setComputedKeyAlgorithm(URI.create(WSTrustConstants.CK_PSHA1));
        
        if (oboToken != null){
            OnBehalfOf obo = fact.createOnBehalfOf(oboToken);
            rst.setOnBehalfOf(obo);
        }
       
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
            
            final QName[] names = doMexRequest(wsdlLocation.toString(), stsURI);
            serviceName = names[0];
            portName = names[1];
        }
        
        Service service = null;
        try{
            // Work around for issue 338
            String url = wsdlLocation.toString();
            // if (url.endsWith("/mex")){
            //   int index = url.lastIndexOf("/mex");
            //  url = url.substring(0, index);
            //}
            service = Service.create(new URL(url), serviceName);
        }catch (MalformedURLException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0041_SERVICE_NOT_CREATED(wsdlLocation.toString()), ex);
            throw new WebServiceException(LogStringsMessages.WST_0041_SERVICE_NOT_CREATED(wsdlLocation.toString()), ex);
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
    protected static QName[]  doMexRequest(final String wsdlLocation, final String stsURI) throws WSTrustException {
        
        final QName[] serviceInfo = new QName[2];
        final MetadataClient mexClient = new MetadataClient();
        
        final Metadata metadata = mexClient.retrieveMetadata(wsdlLocation);
        
        //this method gives the names of services and the corresponding port details
        if(metadata != null){
            final List<PortInfo> ports = mexClient.getServiceInformation(metadata);
            
            //we have to iterate through this to get the appropriate serviceName and portname
            for(PortInfo port : ports){
                final String uri = port.getAddress();
                
                //if the stsAddress what we have matches the address of this port, return
                //this port information
                if(uri.equals(stsURI)){
                    serviceInfo[0]= port.getServiceName();
                    serviceInfo[1]= port.getPortName();
                    break;
                }
                
            }
            
            if(serviceInfo[0]==null || serviceInfo[1]==null){
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0042_NO_MATCHING_SERVICE_MEX(stsURI));
                throw new WSTrustException(
                        LogStringsMessages.WST_0042_NO_MATCHING_SERVICE_MEX(stsURI));
            }
        }else{
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0017_SERVICE_PORTNAME_ERROR(wsdlLocation.toString()));
            throw new WSTrustException(
                    LogStringsMessages.WST_0017_SERVICE_PORTNAME_ERROR(wsdlLocation.toString()));
        }
        
        return serviceInfo;
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
    
    private URI getAddressFromMetadata(final IssuedToken issuedToken)  {
        final PolicyAssertion issuer = (PolicyAssertion)issuedToken.getIssuer();
        PolicyAssertion addressingMetadata = null;
        PolicyAssertion metadata = null;
        PolicyAssertion metadataSection = null;
        PolicyAssertion metadataReference = null;
        Address address = null;
        if(issuer != null){
            address = ((Issuer)issuer).getAddress();
            
            if ( issuer.hasNestedAssertions() ) {
                final Iterator <PolicyAssertion> iterator = issuer.getNestedAssertionsIterator();
                while ( iterator.hasNext() ) {
                    final PolicyAssertion assertion = iterator.next();
                    if ( WSTrustUtil.isAddressingMetadata(assertion)) {
                        addressingMetadata = assertion;
                        break;
                    }
                }
            }
        }
        
        if(addressingMetadata != null){
            if ( addressingMetadata.hasNestedAssertions() ) {
                final Iterator <PolicyAssertion> iterator = addressingMetadata.getNestedAssertionsIterator();
                while ( iterator.hasNext() ) {
                    final PolicyAssertion assertion = iterator.next();
                    if ( WSTrustUtil.isMetadata(assertion)) {
                        metadata = assertion;
                        break;
                    }
                }
            }
        }
        
        if(metadata != null){
            if ( metadata.hasNestedAssertions() ) {
                final Iterator <PolicyAssertion> iterator = metadata.getNestedAssertionsIterator();
                while ( iterator.hasNext() ) {
                    final PolicyAssertion assertion = iterator.next();
                    if ( WSTrustUtil.isMetadataSection(assertion)) {
                        metadataSection = assertion;
                        break;
                    }
                }
            }
            
        }
        
        if(metadataSection != null){
            if ( metadataSection.hasNestedAssertions() ) {
                final Iterator <PolicyAssertion> iterator = metadataSection.getNestedAssertionsIterator();
                while ( iterator.hasNext() ) {
                    final PolicyAssertion assertion = iterator.next();
                    if ( WSTrustUtil.isMetadataReference(assertion)) {
                        metadataReference = assertion;
                        break;
                    }
                }
            }
            
        }
        if(metadataReference != null){
            if ( metadataReference.hasNestedAssertions() ) {
                final Iterator <PolicyAssertion> iterator = metadataReference.getNestedAssertionsIterator();
                while ( iterator.hasNext() ) {
                    final PolicyAssertion assertion = iterator.next();
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
    
     private Claims getClaims(final PolicyAssertion token, String appliesTo)throws WSTrustException{
        Claims claims = null;
        final Iterator<PolicyAssertion> tokens =
                    token.getNestedAssertionsIterator();
        while(tokens.hasNext()){
            final PolicyAssertion cToken = tokens.next();
            if(REQUEST_SECURITY_TOKEN_TEMPLATE.equals(cToken.getName().getLocalPart())){
                final Iterator<PolicyAssertion> cTokens =
                            cToken.getNestedAssertionsIterator();
                while (cTokens.hasNext()){
                    final PolicyAssertion gToken = cTokens.next();
                    if (CLAIMS.equals(gToken.getName().getLocalPart())){
                        try{
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            XMLOutputFactory xof = XMLOutputFactory.newInstance();
                            XMLStreamWriter writer = xof.createXMLStreamWriter(baos);
                           
                            AssertionSet set = AssertionSet.createAssertionSet(Arrays.asList(new PolicyAssertion[] {gToken}));
                            Policy policy = Policy.createPolicy(Arrays.asList(new AssertionSet[] { set }));
                            PolicySourceModel sourceModel = PolicyModelGenerator.getGenerator().translate(policy);
                            XmlPolicyModelMarshaller pm = (XmlPolicyModelMarshaller) XmlPolicyModelMarshaller.getXmlMarshaller(true);
                            pm.marshal(sourceModel, writer);
                            
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                            dbf.setNamespaceAware(true);
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document doc = db.parse(new ByteArrayInputStream(baos.toByteArray()));
                            Element claimsEle = (Element)doc.getElementsByTagNameNS("*", "Claims").item(0);
                            
                            claims = new ClaimsImpl(ClaimsImpl.fromElement(claimsEle));
                            writer.close();
                        }catch (Exception e){
                            log.log(Level.SEVERE,
                            LogStringsMessages.WST_0045_ERROR_UNMARSHALL_CLAIMS(appliesTo), e);
                            throw new WebServiceException(LogStringsMessages.WST_0045_ERROR_UNMARSHALL_CLAIMS(appliesTo), e);
                        }
                    }
                }          
            }
        }
        return claims;
    }

    
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
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_1004_ERROR_MARSHAL_TO_STRING("RST"), e);
            
            throw new WebServiceException(LogStringsMessages.WST_1004_ERROR_MARSHAL_TO_STRING("RST"), e);
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
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_1004_ERROR_MARSHAL_TO_STRING("RSTR"), e);
            throw new WebServiceException(LogStringsMessages.WST_1004_ERROR_MARSHAL_TO_STRING("RSTR"), e);
        }
    }
    
}
