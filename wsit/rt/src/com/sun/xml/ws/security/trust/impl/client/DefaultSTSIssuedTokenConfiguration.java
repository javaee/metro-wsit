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

package com.sun.xml.ws.security.trust.impl.client;

import com.sun.xml.ws.addressing.policy.Address;
import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;
import com.sun.xml.ws.api.security.trust.client.SecondaryIssuedTokenParameters;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.policy.Issuer;
import com.sun.xml.ws.security.policy.IssuedToken;
import com.sun.xml.ws.security.policy.RequestSecurityTokenTemplate;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;

import java.util.logging.Level;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
import java.io.ByteArrayOutputStream;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.addressing.policy.Address;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelGenerator;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.sourcemodel.XmlPolicyModelMarshaller;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.policy.IssuedToken;
import com.sun.xml.ws.security.policy.Issuer;
import com.sun.xml.ws.security.policy.RequestSecurityTokenTemplate;
import com.sun.xml.ws.security.trust.*;
import com.sun.xml.ws.security.trust.impl.elements.ClaimsImpl;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

import java.util.logging.Level;


import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
import org.w3c.dom.Document;

import org.w3c.dom.Element;

import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;


/**
 *
 * @author Jiandong Guo
 */
public class DefaultSTSIssuedTokenConfiguration extends STSIssuedTokenConfiguration{
    
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

    private String tokenType = null;
    
    private String keyType = null;
    
    private long keySize = -1;
    
    private String signatureAlg = null;
    
    private String encAlg = null;
    
    private String canAlg = null;
    
    private String keyWrapAlg = null;
    
    private Token oboToken = null;
    
    private String signWith = null;
    
    private String encryptWith = null;
    
    private Claims claims = null;
    
    public DefaultSTSIssuedTokenConfiguration(String protocol, IssuedToken issuedToken, PolicyAssertion localToken){
        this.protocol = protocol;
        parseAssertions(issuedToken, localToken);
    }
    public DefaultSTSIssuedTokenConfiguration(String stsEndpoint, String stsMEXAddress){
        super(stsEndpoint, stsMEXAddress);
    }
    
    public DefaultSTSIssuedTokenConfiguration(String stsEndpoint, 
                          String stsWSDLLocation, String stsServiceName, String stsPortName, String stsNamespace){
        super(stsEndpoint, stsWSDLLocation, stsServiceName, stsPortName, stsNamespace);
    }
    
    public DefaultSTSIssuedTokenConfiguration(String protocol, String stsEndpoint, String stsMEXAddress){
        super(protocol, stsEndpoint, stsMEXAddress);
    }
    
    public DefaultSTSIssuedTokenConfiguration(String protocol, String stsEndpoint, 
                          String stsWSDLLocation, String stsServiceName, String stsPortName, String stsNamespace){
        super(protocol, stsEndpoint, stsWSDLLocation, stsServiceName, stsPortName, stsNamespace);
    }
    
    public void setTokenType(String tokenType){
        this.tokenType = tokenType;
    }
    
    public void setKeyType(String keyType){
        this.keyType = keyType;
    }
    
    public void setKeySize(long keySize){
        this.keySize = keySize;
    }
    
    public void setSignWith(String signWithAlg){
        this.signWith = signWithAlg;
    }
    
    public void setEncryptWith(String encWithAlg){
        this.encryptWith = encWithAlg;
    }
    
    public void setSignatureAlgorithm(String sigAlg){
        this.signatureAlg = sigAlg;
    }
    
    public void setEncryptionAlgorithm(String encAlg){
        this.encAlg = encAlg;
    }
    
    public void setCanonicalizationAlgorithm(String canAlg){
        this.canAlg = canAlg;
    }
    
    public void setKeyWrapAlgorithm(String keyWrapAlg){
        this.keyWrapAlg = keyWrapAlg;
    }
    
    public void setClaims(Claims claims){
        this.claims = claims;
    }
    
    public void setOBOToken(Token token){
        this.oboToken = token;
    }
    
    public String getTokenType(){
        return this.tokenType;
    }
    
    public String getKeyType(){
        return this.keyType;
    }
    
    public long getKeySize(){
        return this.keySize;
    }
    
    public String getSignatureAlgorithm(){
        return this.signatureAlg;
    }
    
    public String getEncryptionAlgorithm(){
        return this.encAlg;
    }
    
    public String getCanonicalizationAlgorithm(){
        return this.canAlg;
    }
    
    public String getKeyWrapAlgorithm(){
        return this.keyWrapAlg;
    }
    
    public String getSignWith(){
        return signWith;
    }
    
    public String getEncryptWith(){
        return encryptWith;
    }
    
    public Claims getClaims(){
        return this.claims;
    }
    
    public Token getOBOToken(){
        return this.oboToken;
    }

    public void setSecondaryIssuedTokenParameters(SecondaryIssuedTokenParameters sisPara){
        this.sisPara = sisPara;
    }

    private void parseAssertions(IssuedToken issuedToken, PolicyAssertion localToken){
        final RequestSecurityTokenTemplate rstTemplate = issuedToken.getRequestSecurityTokenTemplate();
        
        Issuer issuer = issuedToken.getIssuer();
        URI stsURI = null;
        if (issuer != null){
            stsURI = issuedToken.getIssuer().getAddress().getURI();
        }
        
        // Get STS information from IssuedToken
        if (stsURI != null){
            this.stsEndpoint = stsURI.toString();
            this.stsMEXAddress = getAddressFromMetadata(issuedToken);
            
            if(stsMEXAddress == null){
                stsMEXAddress = stsEndpoint;
            }
        }else if (localToken != null){
            // Get STS information from local configuration
            if (PRE_CONFIGURED_STS.equals(localToken.getName().getLocalPart())) {
                final Map<QName,String> attrs = localToken.getAttributes();
                this.stsNamespace = attrs.get(new QName(CONFIG_NAMESPACE,NAMESPACE));
                this.stsEndpoint = attrs.get(new QName(CONFIG_NAMESPACE,ENDPOINT));
                if (stsEndpoint == null){
                    stsEndpoint = attrs.get(new QName(CONFIG_NAMESPACE,ENDPOINT.toLowerCase()));
                }
                this.stsMEXAddress = attrs.get(new QName(CONFIG_NAMESPACE, METADATA));
                
                if (stsMEXAddress == null){
                    this.stsWSDLLocation = attrs.get(new QName(CONFIG_NAMESPACE,WSDL_LOCATION));
                    this.stsServiceName = attrs.get(new QName(CONFIG_NAMESPACE,SERVICE_NAME));
                    this.stsPortName = attrs.get(new QName(CONFIG_NAMESPACE,PORT_NAME));
                }
            }
        }
        RequestSecurityTokenTemplate rstt = issuedToken.getRequestSecurityTokenTemplate();
        if (rstt != null){
            if (protocol.equals(WSTrustVersion.WS_TRUST_13.getNamespaceURI())){
                SecondaryIssuedTokenParametersImpl sitp = new SecondaryIssuedTokenParametersImpl();
                copy(rstt, sitp);
                sitp.setClaims(getClaims((PolicyAssertion)issuedToken));
                this.sisPara = sitp;
            }else{
                copy(rstt);
                setClaims(getClaims((PolicyAssertion)issuedToken));
            }
        }
    }

    private String getAddressFromMetadata(final IssuedToken issuedToken)  {
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
            return address.getURI().toString();
        }
        
        return null;
    }
    
     private Claims getClaims(final PolicyAssertion token){
        Claims cs = null;
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
                            throw new WebServiceException(e);
                        }
                    }
                }          
            }
        }
        return claims;
    }

    private void copy(RequestSecurityTokenTemplate rstt){
        this.setTokenType(rstt.getTokenType());
        this.setKeyType(rstt.getKeyType());
        this.setKeySize(rstt.getKeySize());
        this.setSignWith(rstt.getSignWith());
        this.setEncryptWith(rstt.getEncryptWith());
        this.setSignatureAlgorithm(rstt.getSignatureAlgorithm());
        this.setEncryptionAlgorithm(rstt.getEncryptionAlgorithm());
        this.setCanonicalizationAlgorithm(rstt.getCanonicalizationAlgorithm());  
    }

    private void copy(RequestSecurityTokenTemplate rstt, SecondaryIssuedTokenParametersImpl sitp){
        sitp.setTokenType(rstt.getTokenType());
        sitp.setKeyType(rstt.getKeyType());
        sitp.setKeySize(rstt.getKeySize());
        sitp.setSignWith(rstt.getSignWith());
        sitp.setEncryptWith(rstt.getEncryptWith());
        sitp.setSignatureAlgorithm(rstt.getSignatureAlgorithm());
        sitp.setEncryptionAlgorithm(rstt.getEncryptionAlgorithm());
        sitp.setCanonicalizationAlgorithm(rstt.getCanonicalizationAlgorithm());
    }
}

