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
package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.spi.PolicySelector;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import java.util.ArrayList;
import javax.xml.namespace.QName;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class SecurityPolicySelector extends PolicySelector{
    private static ArrayList<QName> supportedAssertions = new ArrayList<QName>();
    static{
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,CanonicalizationAlgorithm));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic256));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic192));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic128));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,TripleDes));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic256Rsa15));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic192Rsa15));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic192Rsa15));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,TripleDesRsa15));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic256Sha256));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic256Rsa15));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic192Sha256));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic128Sha256));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic192Sha256));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,TripleDesSha256));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic256Sha256Rsa15));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic192Sha256Rsa15));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Basic128Sha256Rsa15));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,TripleDesSha256Rsa15));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,InclusiveC14N));
        //     supportedAssertions.add(new QName(SECURITY_POLICY_NS,SoapNormalization10));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,STRTransform10));
        //      supportedAssertions.add(new QName(SECURITY_POLICY_NS,XPath10));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,XPathFilter20));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Strict));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Lax));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,LaxTsFirst));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,LaxTsLast));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,IncludeTimestamp));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,EncryptBeforeSigning));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,EncryptSignature));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,ProtectTokens));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,OnlySignEntireHeadersAndBody));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Body));
        //     supportedAssertions.add(new QName(SECURITY_POLICY_NS,Header));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,XPath));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssUsernameToken10));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssUsernameToken11));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,Issuer));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,RequestSecurityTokenTemplate));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,RequireDerivedKeys));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,RequireExternalReference));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,RequireInternalReference));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,RequireKeyIdentifierReference));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,RequireIssuerSerialReference));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,RequireEmbeddedTokenReference));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,RequireThumbprintReference));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssX509V1Token10));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssX509V3Token10));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssX509Pkcs7Token10));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssX509PkiPathV1Token10));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssX509V1Token11));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssX509V3Token11));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssX509Pkcs7Token11));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssX509PkiPathV1Token11));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssKerberosV5ApReqToken11));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssGssKerberosV5ApReqToken11));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,SC10SecurityContextToken));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssSamlV10Token10));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssSamlV11Token10));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssSamlV10Token11));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssSamlV11Token11));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssSamlV20Token11));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssRelV10Token10));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssRelV20Token10));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssRelV10Token11));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,WssRelV20Token11));
        //     supportedAssertions.add(new QName(SECURITY_POLICY_NS,X509V3Token));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportRefKeyIdentifier));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportRefIssuerSerial));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportRefExternalURI));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportRefEmbeddedToken));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportRefKeyIdentifier));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportRefIssuerSerial));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportRefExternalURI));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportRefEmbeddedToken));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportRefThumbprint));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportRefEncryptedKey));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportClientChallenge));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportServerChallenge));
        
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,RequireClientEntropy));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,RequireServerEntropy));
        supportedAssertions.add(new QName(SECURITY_POLICY_NS,MustSupportIssuedTokens));
        
        supportedAssertions.add(new QName(TRUST_NS,RequestSecurityToken));
        supportedAssertions.add(new QName(TRUST_NS,RequestType));
        supportedAssertions.add(new QName(TRUST_NS,TokenType));
        supportedAssertions.add(new QName(TRUST_NS,AuthenticationType));
        
        supportedAssertions.add(new QName(TRUST_NS,OnBehalfOf));
        supportedAssertions.add(new QName(TRUST_NS,KeyType));
        supportedAssertions.add(new QName(TRUST_NS,KeySize));
        supportedAssertions.add(new QName(TRUST_NS,SignatureAlgorithm));
        
        supportedAssertions.add(new QName(TRUST_NS,EncryptionAlgorithm));
        supportedAssertions.add(new QName(TRUST_NS,CanonicalizationAlgorithm));
        supportedAssertions.add(new QName(TRUST_NS,ComputedKeyAlgorithm));
        
        supportedAssertions.add(new QName(TRUST_NS,Encryption));
        supportedAssertions.add(new QName(TRUST_NS,ProofEncryption));
        supportedAssertions.add(new QName(TRUST_NS,UseKey));
        supportedAssertions.add(new QName(TRUST_NS,SignWith));
        
        supportedAssertions.add(new QName(TRUST_NS,EncryptWith));
        
    }
    /** Creates a new instance of SecurityPolicySelector */
    public SecurityPolicySelector() {
        super(supportedAssertions);
    }
    
    public boolean test(PolicyAssertion policyAssertion) {
        if(policyAssertion instanceof SecurityAssertionValidator){
            return ((SecurityAssertionValidator)policyAssertion).validate();
        }else if(supportedAssertions.contains(policyAssertion) || policyAssertion.isOptional()){
            return true;
        }
        return super.test(policyAssertion);
    }
    
    
    public boolean isSupported(String uri) {
        if(SECURITY_POLICY_NS.equals(uri)|| TRUST_NS.equals(uri) || UTILITY_NS.equals(uri)
        || XPATH_NS.equals(uri) || ADDRESSING_NS.equals(uri)){
            return true;
        }
        return super.isSupported(uri);
        
    }
}
