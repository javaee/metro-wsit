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
package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import com.sun.xml.ws.api.security.policy.SecurityPolicyVersion;
import java.util.ArrayList;
import javax.xml.namespace.QName;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class SecurityPolicyValidator implements PolicyAssertionValidator{
    private static final ArrayList<QName> supportedAssertions = new ArrayList<QName>();
    static{
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,CanonicalizationAlgorithm));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic192));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic128));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,TripleDes));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic256Rsa15));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic192Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic192Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,TripleDesRsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic256Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic256Rsa15));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic192Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic128Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic192Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,TripleDesSha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic256Sha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic192Sha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Basic128Sha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,TripleDesSha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,InclusiveC14N));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,InclusiveC14NWithComments));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,ExclusiveC14NWithComments));
        //     supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,SoapNormalization10));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,STRTransform10));
        //supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,XPath10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,XPathFilter20));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Strict));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Lax));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,LaxTsFirst));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,LaxTsLast));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,IncludeTimestamp));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,EncryptBeforeSigning));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,EncryptSignature));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,ProtectTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,OnlySignEntireHeadersAndBody));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Body));
        //supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Header));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,XPath));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssUsernameToken10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssUsernameToken11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,Issuer));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,RequestSecurityTokenTemplate));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,RequireDerivedKeys));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,RequireExternalReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,RequireInternalReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,RequireKeyIdentifierReference));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,RequireIssuerSerialReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,RequireEmbeddedTokenReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,RequireThumbprintReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssX509V1Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssX509V3Token10));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssX509Pkcs7Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssX509PkiPathV1Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssX509V1Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssX509V3Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssX509Pkcs7Token11));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssX509PkiPathV1Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssKerberosV5ApReqToken11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssGssKerberosV5ApReqToken11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,SC10SecurityContextToken));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssSamlV10Token10));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssSamlV11Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssSamlV10Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssSamlV11Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssSamlV20Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssRelV10Token10));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssRelV20Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssRelV10Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,WssRelV20Token11));
        //supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,X509V3Token));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,SupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,SignedSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,EndorsingSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,SignedEndorsingSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportRefKeyIdentifier));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportRefIssuerSerial));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportRefExternalURI));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportRefEmbeddedToken));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportRefKeyIdentifier));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportRefIssuerSerial));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportRefExternalURI));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportRefEmbeddedToken));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportRefThumbprint));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportRefEncryptedKey));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportClientChallenge));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportServerChallenge));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,RequireClientEntropy));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,RequireServerEntropy));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,MustSupportIssuedTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,NoPassword));
        
        // SecurityPolicy 1.2 assertions
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,CanonicalizationAlgorithm));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic192));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic128));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,TripleDes));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic256Rsa15));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic192Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic192Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,TripleDesRsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic256Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic256Rsa15));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic192Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic128Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic192Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,TripleDesSha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic256Sha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic192Sha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Basic128Sha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,TripleDesSha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,InclusiveC14N));
        //     supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,SoapNormalization10));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,STRTransform10));
        //supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,XPath10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,XPathFilter20));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Strict));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Lax));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,LaxTsFirst));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,LaxTsLast));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,IncludeTimestamp));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,EncryptBeforeSigning));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,EncryptSignature));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,ProtectTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,OnlySignEntireHeadersAndBody));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Body));
        //supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Header));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,XPath));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssUsernameToken10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssUsernameToken11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,Issuer));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequestSecurityTokenTemplate));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireDerivedKeys));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireExternalReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireInternalReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireKeyIdentifierReference));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireIssuerSerialReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireEmbeddedTokenReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireThumbprintReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssX509V1Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssX509V3Token10));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssX509Pkcs7Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssX509PkiPathV1Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssX509V1Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssX509V3Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssX509Pkcs7Token11));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssX509PkiPathV1Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssKerberosV5ApReqToken11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssGssKerberosV5ApReqToken11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,SC10SecurityContextToken));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssSamlV10Token10));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssSamlV11Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssSamlV10Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssSamlV11Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssSamlV20Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssRelV10Token10));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssRelV20Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssRelV10Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,WssRelV20Token11));
        //supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,X509V3Token));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,SupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,SignedSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,EndorsingSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,SignedEndorsingSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,EncryptedSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,SignedEncryptedSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,EndorsingEncryptedSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,SignedEndorsingEncryptedSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportRefKeyIdentifier));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportRefIssuerSerial));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportRefExternalURI));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportRefEmbeddedToken));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportRefKeyIdentifier));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportRefIssuerSerial));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportRefExternalURI));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportRefEmbeddedToken));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportRefThumbprint));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportRefEncryptedKey));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportClientChallenge));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportServerChallenge));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireClientEntropy));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireServerEntropy));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,MustSupportIssuedTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,NoPassword));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireClientCertificate));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,HttpBasicAuthentication));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,HttpDigestAuthentication));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireRequestSecurityTokenCollection));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RequireAppliesTo));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,RsaKeyValue));
        
        //Security Policy 200512 : ADDED for Nov 07 Plugfest
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,CanonicalizationAlgorithm));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic192));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic128));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,TripleDes));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic256Rsa15));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic192Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic192Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,TripleDesRsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic256Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic256Rsa15));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic192Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic128Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic192Sha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,TripleDesSha256));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic256Sha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic192Sha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Basic128Sha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,TripleDesSha256Rsa15));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,InclusiveC14N));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,STRTransform10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,XPathFilter20));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Strict));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Lax));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,LaxTsFirst));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,LaxTsLast));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,IncludeTimestamp));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,EncryptBeforeSigning));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,EncryptSignature));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,ProtectTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,OnlySignEntireHeadersAndBody));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Body));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,XPath));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssUsernameToken10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssUsernameToken11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,Issuer));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,RequestSecurityTokenTemplate));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,RequireDerivedKeys));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,RequireExternalReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,RequireInternalReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,RequireKeyIdentifierReference));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,RequireIssuerSerialReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,RequireEmbeddedTokenReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,RequireThumbprintReference));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssX509V1Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssX509V3Token10));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssX509Pkcs7Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssX509PkiPathV1Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssX509V1Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssX509V3Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssX509Pkcs7Token11));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssX509PkiPathV1Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssKerberosV5ApReqToken11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssGssKerberosV5ApReqToken11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,SC10SecurityContextToken));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssSamlV10Token10));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssSamlV11Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssSamlV10Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssSamlV11Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssSamlV20Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssRelV10Token10));
        
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssRelV20Token10));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssRelV10Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,WssRelV20Token11));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,SupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,SignedSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,EndorsingSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,SignedEndorsingSupportingTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportRefKeyIdentifier));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportRefIssuerSerial));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportRefExternalURI));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportRefEmbeddedToken));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportRefKeyIdentifier));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportRefIssuerSerial));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportRefExternalURI));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportRefEmbeddedToken));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportRefThumbprint));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportRefEncryptedKey));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportClientChallenge));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportServerChallenge));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,RequireClientEntropy));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,RequireServerEntropy));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,MustSupportIssuedTokens));
        supportedAssertions.add(new QName(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,NoPassword));
        //-----------------------------------------------------------
        
        // Trust10 Assertions
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
        
        //Trust13 Assertions
        supportedAssertions.add(new QName(TRUST13_NS,RequestSecurityToken));
        supportedAssertions.add(new QName(TRUST13_NS,RequestType));
        supportedAssertions.add(new QName(TRUST13_NS,TokenType));
        supportedAssertions.add(new QName(TRUST13_NS,AuthenticationType));
        supportedAssertions.add(new QName(TRUST13_NS,OnBehalfOf));
        supportedAssertions.add(new QName(TRUST13_NS,KeyType));
        supportedAssertions.add(new QName(TRUST13_NS,KeySize));
        supportedAssertions.add(new QName(TRUST13_NS,SignatureAlgorithm));
        supportedAssertions.add(new QName(TRUST13_NS,EncryptionAlgorithm));
        supportedAssertions.add(new QName(TRUST13_NS,CanonicalizationAlgorithm));
        supportedAssertions.add(new QName(TRUST13_NS,ComputedKeyAlgorithm));
        supportedAssertions.add(new QName(TRUST13_NS,Encryption));
        supportedAssertions.add(new QName(TRUST13_NS,ProofEncryption));
        supportedAssertions.add(new QName(TRUST13_NS,UseKey));
        supportedAssertions.add(new QName(TRUST13_NS,SignWith));
        supportedAssertions.add(new QName(TRUST13_NS,EncryptWith));

        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"EnableEPRIdentity"));
        supportedAssertions.add(new QName("http://schemas.sun.com/2006/03/wss/server","EncSCCancel"));
        supportedAssertions.add(new QName("http://schemas.sun.com/2006/03/wss/client","EncSCCancel"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"DisableStreamingSecurity"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,"DisableStreamingSecurity"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"DisableTimestampSigning"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,"DisableTimestampSigning"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"EncryptHeaderContent"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,"EncryptHeaderContent"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"EncryptRMLifecycleMessage"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,"EncryptRMLifecycleMessage"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"DisableInclusivePrefixList"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,"DisableInclusivePrefixList"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"DisablePayloadBuffering"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,"DisablePayloadBuffering"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"AllowMissingTimestamp"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,"AllowMissingTimestamp"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"UnsetSecurityMUValue"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,"UnsetSecurityMUValue"));
        // newly added by M.P.
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"KeyStore"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"TrustStore"));
        
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,"KeyStore"));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,"TrustStore"));
        
        // Kerberos information from custom assertions
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,"KerberosConfig"));       
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,"KerberosConfig"));
        
        supportedAssertions.add(new QName(SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS,"SCClientConfiguration"));
        supportedAssertions.add(new QName(SUN_SECURE_SERVER_CONVERSATION_POLICY_NS,"SCConfiguration"));
        
        supportedAssertions.add(new QName(SUN_TRUST_CLIENT_SECURITY_POLICY_NS,"PreconfiguredSTS"));
        supportedAssertions.add(new QName(SUN_TRUST_SERVER_SECURITY_POLICY_NS,"STSConfiguration"));
        
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,Constants.CertStore));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,Constants.CertStore));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_CLIENT_POLICY_NS,Constants.BSP10));
        supportedAssertions.add(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS,Constants.BSP10));

        // Identity Selector Interoproperability Profile
        supportedAssertions.add(new QName("http://schemas.xmlsoap.org/ws/2005/05/identity", "RequireFederatedIdentityProvisioning"));
    }
    
    /** Creates a new instance of SecurityPolicyValidator. To be used by appropriate service finder */
    public SecurityPolicyValidator() {
    }
    
    public Fitness validateClientSide(PolicyAssertion policyAssertion) {
        String uri = policyAssertion.getName().getNamespaceURI();
        
        if(uri.equals(SUN_WSS_SECURITY_SERVER_POLICY_NS) || uri.equals(SUN_TRUST_SERVER_SECURITY_POLICY_NS)){
            return Fitness.UNSUPPORTED;
        }
        
        if (policyAssertion instanceof SecurityAssertionValidator) {
            SecurityAssertionValidator.AssertionFitness fitness =((SecurityAssertionValidator)policyAssertion).validate(false);
            if(fitness == fitness.IS_VALID){
                return Fitness.SUPPORTED;
            }else {
                return Fitness.UNSUPPORTED;
            }
            
            //return ((SecurityAssertionValidator)policyAssertion).validate() ? Fitness.SUPPORTED : Fitness.UNSUPPORTED;
        } else if (supportedAssertions.contains(policyAssertion.getName())) {
            return Fitness.SUPPORTED;
        } else {
            return Fitness.UNKNOWN;
        }
    }
    
    public Fitness validateServerSide(PolicyAssertion policyAssertion) {
        String uri = policyAssertion.getName().getNamespaceURI();
        
        if(uri.equals(SUN_WSS_SECURITY_CLIENT_POLICY_NS) || uri.equals(SUN_WSS_SECURITY_CLIENT_POLICY_NS)
                || uri.equals(SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS) || uri.equals(SUN_TRUST_CLIENT_SECURITY_POLICY_NS)){
            return Fitness.UNSUPPORTED;
        }
        
        if (policyAssertion instanceof SecurityAssertionValidator) {
            return (((SecurityAssertionValidator)policyAssertion).validate(true) == SecurityAssertionValidator.AssertionFitness.IS_VALID )? Fitness.SUPPORTED : Fitness.UNSUPPORTED;
        } else if (supportedAssertions.contains(policyAssertion.getName())) {
            return Fitness.SUPPORTED;
        } else {
            return Fitness.UNKNOWN;
        }
    }
    
    public String[] declareSupportedDomains() {
        return new String[] {
            SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,
            SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,
            SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri,
            TRUST_NS,
            SUN_WSS_SECURITY_CLIENT_POLICY_NS,
            SUN_WSS_SECURITY_SERVER_POLICY_NS,
            SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS,
            
        };
    }
}
