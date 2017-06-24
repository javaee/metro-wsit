/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.xml.ws.security.addressing.policy.Address;
import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.api.security.trust.client.SecondaryIssuedTokenParameters;
import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.policy.Issuer;
import com.sun.xml.ws.security.policy.IssuedToken;
import com.sun.xml.ws.security.policy.RequestSecurityTokenTemplate;
import com.sun.xml.ws.security.secext10.*;
import com.sun.xml.ws.security.trust.*;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
import com.sun.xml.wss.impl.MessageConstants;

import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import org.w3c.dom.Element;

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
    private static final String IDENTITY = "Identity";
    private static final String WST_VERSION ="wstVersion";

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

    public DefaultSTSIssuedTokenConfiguration(){
        
    }
    
    public DefaultSTSIssuedTokenConfiguration(String protocol, IssuedToken issuedToken, PolicyAssertion localToken){
        if (protocol != null){
            this.protocol = protocol;
        }
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

    public void setProtocol(String protocol){
        this.protocol = protocol;
    }

    public void setSTSInfo(String stsEndpoint, String stsMEXAddress){
        this.stsEndpoint = stsEndpoint;
        this.stsMEXAddress = stsMEXAddress;
    }

    public void setSTSInfo (String protocol, String stsEndpoint,
                            String stsWSDLLocation, String stsServiceName, String stsPortName, String stsNamespace){
        this.protocol = protocol;
        this.stsEndpoint = stsEndpoint;
        this.stsWSDLLocation = stsWSDLLocation;
        this.stsServiceName = stsServiceName;
        this.stsPortName = stsPortName;
        this.stsNamespace = stsNamespace;
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

    public void setOBOToken(String username, String password){
        this.oboToken = createUsernameToken(username, password);
    }

    public void setOBOToken(X509Certificate cert){
        this.oboToken = this.createBinaryTokenForCertificate(cert);
    }

    public void setActAsToken(String username, String password){
        this.getOtherOptions().put(STSIssuedTokenConfiguration.ACT_AS, this.createUsernameToken(username, password));
    }

    public void setActAsToken(X509Certificate cert){
        this.getOtherOptions().put(STSIssuedTokenConfiguration.ACT_AS, this.createBinaryTokenForCertificate(cert));
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
        Issuer issuer = issuedToken.getIssuer();
        URI stsURI = null;
        if (issuer != null){
            stsURI = issuedToken.getIssuer().getAddress().getURI();
            if(issuer.getIdentity() != null){
                this.getOtherOptions().put(IDENTITY, issuer.getIdentity());
            }
        }
        
        // Get STS information from IssuedToken
        if (stsURI != null){
            this.stsEndpoint = stsURI.toString();            
            Address metadataIssuerAddress = issuer.getMetadataAddress();
            if(metadataIssuerAddress != null){
                URI metadataIssuerAddressURI = metadataIssuerAddress.getURI();
                if(metadataIssuerAddressURI != null){
                    stsMEXAddress = metadataIssuerAddressURI.toString();
                }
            }                        
            
            if(stsMEXAddress == null){
                stsMEXAddress = stsEndpoint + "/mex";
            }
        }
          
        String stsProtocol = null;
        if (localToken != null){
            // Get STS information from local configuration
            if (PRE_CONFIGURED_STS.equals(localToken.getName().getLocalPart())) {
                final Map<QName,String> attrs = localToken.getAttributes();
                stsProtocol = trim(attrs.get(new QName(CONFIG_NAMESPACE, WST_VERSION)));
                if (stsURI == null){
                    this.stsNamespace = trim(attrs.get(new QName(CONFIG_NAMESPACE,NAMESPACE)));
                    this.stsEndpoint = trim(attrs.get(new QName(CONFIG_NAMESPACE,ENDPOINT)));
                    if (stsEndpoint == null){
                        stsEndpoint = trim(attrs.get(new QName(CONFIG_NAMESPACE,ENDPOINT.toLowerCase())));
                    }
                    this.stsMEXAddress = trim(attrs.get(new QName(CONFIG_NAMESPACE, METADATA)));
                
                    if (stsMEXAddress == null){
                        this.stsWSDLLocation = trim(attrs.get(new QName(CONFIG_NAMESPACE,WSDL_LOCATION)));
                        this.stsServiceName = trim(attrs.get(new QName(CONFIG_NAMESPACE,SERVICE_NAME)));
                        this.stsPortName = trim(attrs.get(new QName(CONFIG_NAMESPACE,PORT_NAME)));
                    }
                }

                // check if shareToken is set
                String shareToken = attrs.get(new QName(CONFIG_NAMESPACE, SHARE_TOKEN));
                if ("true".equals(shareToken)){
                    this.getOtherOptions().put(SHARE_TOKEN, shareToken);
                }

                // check if renewExpiredToken is set
                String renewExpiredToken = attrs.get(new QName(CONFIG_NAMESPACE, RENEW_EXPIRED_TOKEN));
                if ("true".equals(renewExpiredToken)){
                    this.getOtherOptions().put(RENEW_EXPIRED_TOKEN, renewExpiredToken);
                }

                // maxClockSkew
                String maxClockSkew = attrs.get(new QName(CONFIG_NAMESPACE, MAX_CLOCK_SKEW));
                if (maxClockSkew != null){
                    this.getOtherOptions().put(MAX_CLOCK_SKEW, maxClockSkew);
                }
                
                // handle LifeTime
                if (localToken.hasParameters()){
                    Iterator<PolicyAssertion> pas = localToken.getParametersIterator();
                    while (pas.hasNext()){
                        PolicyAssertion pa = pas.next();
                        if (LIFE_TIME.equals(pa.getName().getLocalPart())){
                             this.getOtherOptions().put(LIFE_TIME, Integer.parseInt(pa.getValue()));
                             break;
                        }
                    }
                }
            }
        }
        if (stsProtocol == null){
            stsProtocol = protocol;
        }
        RequestSecurityTokenTemplate rstt = issuedToken.getRequestSecurityTokenTemplate();
        if (rstt != null){
            Claims claims = null;            
            if (protocol.equals(WSTrustVersion.WS_TRUST_13.getNamespaceURI())){
                if(issuedToken.getClaims() != null){
                   claims = getClaims(issuedToken, stsProtocol);
                }
            }else{
                if(rstt.getClaims() != null){
                    claims = getClaims(issuedToken, stsProtocol);
                }                
            }

            if (!protocol.equals(stsProtocol)){
                // Mixed versions of trust
                copy(rstt, stsProtocol, protocol);
                setClaims(claims);
                protocol = stsProtocol;
            }else if (protocol.equals(WSTrustVersion.WS_TRUST_13.getNamespaceURI())){
                SecondaryIssuedTokenParametersImpl sitp = new SecondaryIssuedTokenParametersImpl();
                copy(rstt, sitp);
                sitp.setClaims(claims);
                this.sisPara = sitp;
            }else{
                copy(rstt);
                setClaims(claims);
            }
        }
    }
    
     private Claims getClaims(final IssuedToken issuedToken, String stsWstProtocol){
        Claims cs = null;        
         try {
             //Element claimsEle = null;
             if (protocol.equals(WSTrustVersion.WS_TRUST_13.getNamespaceURI())){
                Element claimsEle = issuedToken.getClaims().getClaimsAsElement();
                cs = WSTrustElementFactory.newInstance(WSTrustVersion.WS_TRUST_13.getNamespaceURI()).createClaims(claimsEle);
             }else{
                 RequestSecurityTokenTemplate rstt = issuedToken.getRequestSecurityTokenTemplate();
                 Element claimsEle = rstt.getClaims().getClaimsAsElement();
                 cs = WSTrustElementFactory.newInstance(WSTrustVersion.WS_TRUST_10.getNamespaceURI()).createClaims(claimsEle);
             }
             cs = WSTrustElementFactory.newInstance(WSTrustVersion.getInstance(stsWstProtocol)).createClaims(cs);
         } catch (Exception e) {
             throw new WebServiceException(e);
         }        
        return cs;
    }
      
    private void copy(RequestSecurityTokenTemplate rstt){
        this.setTokenType(trim(rstt.getTokenType()));
        this.setKeyType(trim(rstt.getKeyType()));
        this.setKeySize(rstt.getKeySize());
        this.setSignWith(trim(rstt.getSignWith()));
        this.setEncryptWith(trim(rstt.getEncryptWith()));
        this.setSignatureAlgorithm(trim(rstt.getSignatureAlgorithm()));
        this.setEncryptionAlgorithm(trim(rstt.getEncryptionAlgorithm()));
        this.setCanonicalizationAlgorithm(trim(rstt.getCanonicalizationAlgorithm()));
    }

    private void copy(RequestSecurityTokenTemplate rstt, SecondaryIssuedTokenParametersImpl sitp){
        sitp.setTokenType(trim(rstt.getTokenType()));
        sitp.setKeyType(trim(rstt.getKeyType()));
        sitp.setKeySize(rstt.getKeySize());
        sitp.setSignWith(trim(rstt.getSignWith()));
        sitp.setEncryptWith(trim(rstt.getEncryptWith()));
        sitp.setSignatureAlgorithm(trim(rstt.getSignatureAlgorithm()));
        sitp.setEncryptionAlgorithm(trim(rstt.getEncryptionAlgorithm()));
        sitp.setCanonicalizationAlgorithm(trim(rstt.getCanonicalizationAlgorithm()));
        sitp.setKeyWrapAlgorithm(trim(rstt.getKeyWrapAlgorithm()));
    }
    
    private void copy(RequestSecurityTokenTemplate rstt, String stsWstProtocol, String serviceWstProtocol){
        // Convert KeyType
        WSTrustVersion stsWstVer = WSTrustVersion.getInstance(stsWstProtocol);
        WSTrustVersion serviceWstVer = WSTrustVersion.getInstance(serviceWstProtocol);
        String rsttKeyType = trim(rstt.getKeyType());
        if (serviceWstVer.getPublicKeyTypeURI().equals(rsttKeyType)){
            setKeyType(stsWstVer.getPublicKeyTypeURI());
        }else if (serviceWstVer.getSymmetricKeyTypeURI().equals(rsttKeyType)){
            setKeyType(stsWstVer.getSymmetricKeyTypeURI());
        }else if (serviceWstVer.getBearerKeyTypeURI().equals(rsttKeyType)){
            setKeyType(stsWstVer.getBearerKeyTypeURI());
        }
        this.setTokenType(trim(rstt.getTokenType()));
        this.setKeySize(rstt.getKeySize());
        this.setSignWith(trim(rstt.getSignWith()));
        this.setEncryptWith(trim(rstt.getEncryptWith()));
        this.setSignatureAlgorithm(trim(rstt.getSignatureAlgorithm()));
        this.setEncryptionAlgorithm(trim(rstt.getEncryptionAlgorithm()));
        this.setCanonicalizationAlgorithm(trim(rstt.getCanonicalizationAlgorithm()));
    }

    public void copy(STSIssuedTokenConfiguration config){
        if (config.getProtocol() != null){
            this.protocol = config.getProtocol();
        }

        if (stsEndpoint == null && config.getSTSEndpoint() != null){
            this.stsEndpoint = config.getSTSEndpoint();
            if (config.getSTSMEXAddress()!= null){
                this.stsMEXAddress = config.getSTSMEXAddress();
            }else if (config.getSTSWSDLLocation()!= null){
                this.stsWSDLLocation = config.getSTSWSDLLocation();
                this.stsServiceName = config.getSTSServiceName();
                this.stsPortName = config.getSTSPortName();
                this.stsNamespace = config.getSTSNamespace();
            }
        }

        if (tokenType == null && config.getTokenType() != null){
            this.tokenType = config.getTokenType();
        }

        if (keyType == null && config.getKeyType() != null){
            this.keyType = config.getKeyType();
        }

        if (keySize < 1 && config.getKeySize() > 0){
            this.keySize = config.getKeySize();
        }

        if (signatureAlg == null && config.getSignatureAlgorithm() != null){
            this.signatureAlg = config.getSignatureAlgorithm();
        }

        if (encAlg == null && config.getEncryptionAlgorithm() != null){
            this.encAlg = config.getEncryptionAlgorithm();
        }

        if (config.getCanonicalizationAlgorithm() != null){
            this.canAlg = config.getCanonicalizationAlgorithm();
        }

        if (keyWrapAlg == null && config.getKeyWrapAlgorithm() != null){
            this.keyWrapAlg = config.getKeyWrapAlgorithm();
        }

        if (signWith == null && config.getSignWith() != null){
            this.signWith = config.getSignWith();
        }
        if (encryptWith == null && config.getEncryptWith() != null){
            this.encryptWith = config.getEncryptWith();
        }

        if (config.getOBOToken() != null){
            this.oboToken = config.getOBOToken();
        }

        if (claims == null && config.getClaims() != null){
            this.claims = config.getClaims();
        }

        this.getOtherOptions().putAll(config.getOtherOptions());
        if (config.getOtherOptions().containsKey(ISSUED_TOKEN)){
            this.getOtherOptions().remove(ISSUED_TOKEN);
        }
    }

    private Token createUsernameToken(String username, String password){
        ObjectFactory fact = new ObjectFactory();
        UsernameTokenType ut = fact.createUsernameTokenType();
        AttributedString un = fact.createAttributedString();
        un.setValue(username);
        AttributedString pwd = fact.createAttributedString();
        pwd.setValue(password);
        ut.setUsername(un);
        ut.setPassword(pwd);

        return new GenericToken(fact.createUsernameToken(ut));
    }

    private Token createBinaryTokenForCertificate(X509Certificate cert){
        ObjectFactory fact = new ObjectFactory();
        BinarySecurityTokenType bst = fact.createBinarySecurityTokenType();
        bst.setValueType(MessageConstants.X509v3_NS);
        bst.setEncodingType(MessageConstants.BASE64_ENCODING_NS);
        try{
            bst.setValue(cert.getEncoded());
        }catch(CertificateEncodingException ex){
            throw new RuntimeException(ex);
        }

        return new GenericToken(fact.createBinarySecurityToken(bst));
    }

    private String trim(String uriStr){
        if (uriStr != null){
            return uriStr.trim();
        }

        return uriStr;
    }
}

