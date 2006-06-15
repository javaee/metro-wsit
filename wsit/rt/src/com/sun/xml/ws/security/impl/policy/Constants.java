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

import java.util.logging.Logger;
import javax.xml.namespace.QName;

public class Constants {
    
    
    //Namespace constants
    public final static String SECURITY_POLICY_NS = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
    public final static String ADDRESSING_NS = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    public final static String XPATH_NS = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    public final static String TRUST_NS = "http://schemas.xmlsoap.org/ws/2005/02/trust";
    public final static String UTILITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis- 200401-wss-wssecurity-utility-1.0.xsd";
    
    
    
    //Local names
//    public final static QName _InclusiveC14N_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "InclusiveC14N");
//    public final static QName _MustSupportServerChallenge_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "MustSupportServerChallenge");
//    public final static QName _Basic192Sha256Rsa15_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic192Sha256Rsa15");
//    public final static QName _STRTransform10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "STRTransform10");
//    public final static QName _WssX509PkiPathV1Token11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssX509PkiPathV1Token11");
//    public final static QName _WssUsernameToken11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssUsernameToken11");
//    public final static QName _Basic128_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic128");
//    public final static QName _IssuedToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "IssuedToken");
//    public final static QName _ProtectTokens_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "ProtectTokens");
//    public final static QName _Basic256Sha256Rsa15_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic256Sha256Rsa15");
//    public final static QName _WssGssKerberosV5ApReqToken11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssGssKerberosV5ApReqToken11");
//    public final static QName _EncryptBeforeSigning_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "EncryptBeforeSigning");
//    public final static QName _WssX509V3Token10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssX509V3Token10");
//    public final static QName _SpnegoContextToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SpnegoContextToken");
//    public final static QName _EncryptSignature_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "EncryptSignature");
//    public final static QName _SignedParts_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SignedParts");
//    public final static QName _EndorsingSupportingTokens_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "EndorsingSupportingTokens");
//    public final static QName _MustSupportIssuedTokens_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "MustSupportIssuedTokens");
//    public final static QName _WssX509PkiPathV1Token10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssX509PkiPathV1Token10");
//    public final static QName _MustSupportRefEncryptedKey_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "MustSupportRefEncryptedKey");
//    public final static QName _RequiredElements_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequiredElements");
//    public final static QName _SOAPNormalization10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SOAPNormalization10");
//    public final static QName _WssSamlV11Token11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssSamlV11Token11");
//    public final static QName _Basic128Sha256Rsa15_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic128Sha256Rsa15");
//    public final static QName _MustSupportRefKeyIdentifier_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "MustSupportRefKeyIdentifier");
//    public final static QName _RequireExternalUriReference_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequireExternalUriReference");
//    public final static QName _SamlToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SamlToken");
//    public final static QName _RelToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RelToken");
//    public final static QName _RequireInternalReference_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequireInternalReference");
//    public final static QName _Basic256Rsa15_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic256Rsa15");
//    public final static QName _SignatureToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SignatureToken");
//    public final static QName _MustSupportClientChallenge_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "MustSupportClientChallenge");
//    public final static QName _SignedEndorsingSupportingTokens_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SignedEndorsingSupportingTokens");
//    public final static QName _WssKerberosV5ApReqToken11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssKerberosV5ApReqToken11");
//    public final static QName _Basic192Rsa15_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic192Rsa15");
//    public final static QName _TripleDesRsa15_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "TripleDesRsa15");
//    public final static QName _Trust10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Trust10");
//    public final static QName _RequireClientEntropy_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequireClientEntropy");
//    public final static QName _RequireDerivedKeys_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequireDerivedKeys");
//    public final static QName _Strict_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Strict");
//    public final static QName _RequireKeyIdentifierReference_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequireKeyIdentifierReference");
//    public final static QName _LaxTsFirst_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "LaxTsFirst");
//    public final static QName _SecureConversationToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SecureConversationToken");
//    public final static QName _RequireThumbprintReference_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequireThumbprintReference");
//    public final static QName _XPathFilter20_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "XPathFilter20");
//    public final static QName _HttpsToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "HttpsToken");
//    public final static QName _SignedElements_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SignedElements");
//    public final static QName _WssX509Pkcs7Token10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssX509Pkcs7Token10");
//    public final static QName _Wss10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Wss10");
//    public final static QName _MustSupportRefExternalURI_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "MustSupportRefExternalURI");
//    public final static QName _TransportToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "TransportToken");
//    public final static QName _MustSupportRefEmbeddedToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "MustSupportRefEmbeddedToken");
//    public final static QName _Wss11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Wss11");
//    public final static QName _EncryptedElements_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "EncryptedElements");
//    public final static QName _WssSamlV11Token10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssSamlV11Token10");
//    public final static QName _TripleDesSha256_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "TripleDesSha256");
//    public final static QName _WssRelV10Token11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssRelV10Token11");
//    public final static QName _SignedSupportingTokens_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SignedSupportingTokens");
//    public final static QName _SecurityContextToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SecurityContextToken");
//    public final static QName _Basic256Sha256_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic256Sha256");
//    public final static QName _UsernameToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "UsernameToken");
//    public final static QName _OnlySignEntireHeadersAndBody_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "OnlySignEntireHeadersAndBody");
//    public final static QName _InitiatorToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "InitiatorToken");
//    public final static QName _WssSamlV20Token11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssSamlV20Token11");
//    public final static QName _WssSamlV10Token11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssSamlV10Token11");
//    public final static QName _Basic256_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic256");
//    public final static QName _WssRelV10Token10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssRelV10Token10");
//    public final static QName _ProtectionToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "ProtectionToken");
//    public final static QName _BootstrapPolicy_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "BootstrapPolicy");
//    public final static QName _SC10SecurityContextToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SC10SecurityContextToken");
//    public final static QName _KerberosToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "KerberosToken");
//    public final static QName _WssRelV20Token10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssRelV20Token10");
//    public final static QName _LaxTsLast_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "LaxTsLast");
//    public final static QName _RequireServerEntropy_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequireServerEntropy");
//    public final static QName _RequireExternalReference_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequireExternalReference");
//    public final static QName _RequireSignatureConfirmation_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequireSignatureConfirmation");
//    public final static QName _Basic128Rsa15_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic128Rsa15");
//    public final static QName _AsymmetricBinding_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "AsymmetricBinding");
//    public final static QName _IncludeTimestamp_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "IncludeTimestamp");
//    public final static QName _RequireEmbeddedTokenReference_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequireEmbeddedTokenReference");
//    public final static QName _MustSupportRefThumbprint_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "MustSupportRefThumbprint");
//    public final static QName _Basic192_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic192");
//    public final static QName _WssX509Pkcs7Token11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssX509Pkcs7Token11");
//    public final static QName _WssSamlV10Token10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssSamlV10Token10");
//    public final static QName _Basic128Sha256_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic128Sha256");
//    public final static QName _TripleDesSha256Rsa15_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "TripleDesSha256Rsa15");
//    public final static QName _WssUsernameToken10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssUsernameToken10");
//    public final static QName _SymmetricBinding_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SymmetricBinding");
//    public final static QName _TripleDes_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "TripleDes");
//    public final static QName _MustSupportRefIssuerSerial_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "MustSupportRefIssuerSerial");
//    public final static QName _EncryptedParts_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "EncryptedParts");
//    public final static QName _Basic192Sha256_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Basic192Sha256");
//    public final static QName _AlgorithmSuite_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "AlgorithmSuite");
//    public final static QName _WssRelV20Token11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssRelV20Token11");
//    public final static QName _TransportBinding_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "TransportBinding");
//    public final static QName _SupportingTokens_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SupportingTokens");
//    public final static QName _X509Token_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "X509Token");
//    public final static QName _WssX509V1Token10_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssX509V1Token10");
//    public final static QName _WssX509V1Token11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssX509V1Token11");
//    public final static QName _WssX509V3Token11_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "WssX509V3Token11");
//    public final static QName _RecipientToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RecipientToken");
//    public final static QName _EncryptionToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "EncryptionToken");
//    public final static QName _Lax_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Lax");
//    public final static QName _Layout_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Layout");
//    public final static QName _RequireIssuerSerialReference_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequireIssuerSerialReference");
//
//    public final static QName _Body_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Body");
//    public final static Body BODY_ASSERTION = new Body();
//    public final static QName HEADER_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Header");
//    public final static QName _RequestSecurityTokenTemplate_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "RequestSecurityTokenTemplate");
//    public final static QName _EndpointReference_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "EndpointReference");
//    public final static QName _IncludeToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "IncludeToken");
//    public final static QName _XPath_QNAME = new QName("http://www.w3.org/TR/1999/REC-xpath-19991116", "XPath");
//    public final static QName _RequireClientCertificate = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy","RequireClientCertificate");
//    public final static QName _RequireDerivedKeys = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy","RequireDerivedKeys");
//
//
//    //Trust constants
//    public final static QName _KeyType_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "KeyType");
//    public final static QName _KeySize_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "KeySize");
//    public final static QName _UseKey_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "UseKey");
//    public final static QName _Encryption_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Encryption");
//    public final static QName _ProofEncryption_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "ProofEncryption");
//    public final static QName _Lifetime_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Lifetime");
//    public final static QName _Issuer = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust","Issuer");
//
//    //Addressing constants
//    public final static QName _Address_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "Address");
//    //public final static QName _Identity_QNAME = new QName("http://schemas.microsoft.com/ws/2004/04/addressingidentityextension" , "Identity");
//
//    public final static String _XPATHVERSION = "XPathVersion";
//
//
//    //utility constants
//    public final static QName _Created_QNAME = new QName("http://docs.oasis-open.org/wss/2004/01/oasis- 200401-wss-wssecurity-utility-1.0.xsd", "Created");
//    public final static QName _Expires_QNAME = new QName("http://docs.oasis-open.org/wss/2004/01/oasis- 200401-wss-wssecurity-utility-1.0.xsd", "Expires");
//
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public final static String _XPATHVERSION = "XPathVersion";
    
    public final static String InclusiveC14N = "InclusiveC14N";
    public final static String MustSupportServerChallenge = "MustSupportServerChallenge";
    public final static String Basic192Sha256Rsa15 = "Basic192Sha256Rsa15";
    public final static String STRTransform10 = "STRTransform10";
    public final static String WssX509PkiPathV1Token11 = "WssX509PkiPathV1Token11";
    public final static String WssUsernameToken11 = "WssUsernameToken11";
    public final static String Basic128 = "Basic128";
    public final static String IssuedToken = "IssuedToken";
    public final static String ProtectTokens = "ProtectTokens";
    public final static String Basic256Sha256Rsa15 = "Basic256Sha256Rsa15";
    public final static String WssGssKerberosV5ApReqToken11 = "WssGssKerberosV5ApReqToken11";
    public final static String EncryptBeforeSigning = "EncryptBeforeSigning";
    public final static String WssX509V3Token10 = "WssX509V3Token10";
    public final static String SpnegoContextToken = "SpnegoContextToken";
    public final static String EncryptSignature = "EncryptSignature";
    public final static String SignedParts = "SignedParts";
    public final static String EndorsingSupportingTokens = "EndorsingSupportingTokens";
    public final static String MustSupportIssuedTokens = "MustSupportIssuedTokens";
    public final static String WssX509PkiPathV1Token10 = "WssX509PkiPathV1Token10";
    public final static String MustSupportRefEncryptedKey = "MustSupportRefEncryptedKey";
    public final static String RequiredElements = "RequiredElements";
    public final static String SOAPNormalization10 = "SOAPNormalization10";
    public final static String WssSamlV11Token11 = "WssSamlV11Token11";
    public final static String Basic128Sha256Rsa15 = "Basic128Sha256Rsa15";
    public final static String MustSupportRefKeyIdentifier = "MustSupportRefKeyIdentifier";
    public final static String RequireExternalUriReference = "RequireExternalUriReference";
    public final static String SamlToken = "SamlToken";
    public final static String RelToken = "RelToken";
    public final static String RequireInternalReference = "RequireInternalReference";
    public final static String Basic256Rsa15 = "Basic256Rsa15";
    public final static String SignatureToken = "SignatureToken";
    public final static String MustSupportClientChallenge = "MustSupportClientChallenge";
    public final static String SignedEndorsingSupportingTokens = "SignedEndorsingSupportingTokens";
    public final static String WssKerberosV5ApReqToken11 = "WssKerberosV5ApReqToken11";
    public final static String Basic192Rsa15 = "Basic192Rsa15";
    public final static String TripleDesRsa15 = "TripleDesRsa15";
    public final static String Trust10 = "Trust10";
    public final static String RequireClientEntropy = "RequireClientEntropy";
    public final static String RequireDerivedKeys = "RequireDerivedKeys";
    public final static String Strict = "Strict";
    public final static String RequireKeyIdentifierReference = "RequireKeyIdentifierReference";
    public final static String LaxTsFirst = "LaxTsFirst";
    public final static String SecureConversationToken = "SecureConversationToken";
    public final static String RequireThumbprintReference = "RequireThumbprintReference";
    public final static String XPathFilter20 = "XPathFilter20";
    public final static String HttpsToken = "HttpsToken";
    public final static String SignedElements = "SignedElements";
    public final static String WssX509Pkcs7Token10 = "WssX509Pkcs7Token10";
    public final static String Wss10 = "Wss10";
    public final static String MustSupportRefExternalURI = "MustSupportRefExternalURI";
    public final static String TransportToken = "TransportToken";
    public final static String MustSupportRefEmbeddedToken = "MustSupportRefEmbeddedToken";
    public final static String Wss11 = "Wss11";
    public final static String EncryptedElements = "EncryptedElements";
    public final static String WssSamlV11Token10 = "WssSamlV11Token10";
    public final static String TripleDesSha256 = "TripleDesSha256";
    public final static String WssRelV10Token11 = "WssRelV10Token11";
    public final static String SignedSupportingTokens = "SignedSupportingTokens";
    public final static String SecurityContextToken = "SecurityContextToken";
    public final static String Basic256Sha256 = "Basic256Sha256";
    public final static String UsernameToken = "UsernameToken";
    public final static String OnlySignEntireHeadersAndBody =  "OnlySignEntireHeadersAndBody";
    public final static String InitiatorToken = "InitiatorToken";
    public final static String WssSamlV20Token11 = "WssSamlV20Token11";
    public final static String WssSamlV10Token11 = "WssSamlV10Token11";
    public final static String Basic256 = "Basic256";
    public final static String WssRelV10Token10 = "WssRelV10Token10";
    public final static String ProtectionToken = "ProtectionToken";
    public final static String BootstrapPolicy = "BootstrapPolicy";
    public final static String SC10SecurityContextToken = "SC10SecurityContextToken";
    public final static String KerberosToken = "KerberosToken";
    public final static String WssRelV20Token10 = "WssRelV20Token10";
    public final static String LaxTsLast = "LaxTsLast";
    public final static String RequireServerEntropy = "RequireServerEntropy";
    public final static String RequireExternalReference = "RequireExternalReference";
    public final static String RequireSignatureConfirmation = "RequireSignatureConfirmation";
    public final static String Basic128Rsa15 = "Basic128Rsa15";
    public final static String AsymmetricBinding = "AsymmetricBinding";
    public final static String IncludeTimestamp = "IncludeTimestamp";
    public final static String RequireEmbeddedTokenReference = "RequireEmbeddedTokenReference";
    public final static String MustSupportRefThumbprint = "MustSupportRefThumbprint";
    public final static String Basic192 = "Basic192";
    public final static String WssX509Pkcs7Token11 = "WssX509Pkcs7Token11";
    public final static String WssSamlV10Token10 = "WssSamlV10Token10";
    public final static String Basic128Sha256 = "Basic128Sha256";
    public final static String TripleDesSha256Rsa15 = "TripleDesSha256Rsa15";
    public final static String WssUsernameToken10 = "WssUsernameToken10";
    public final static String SymmetricBinding = "SymmetricBinding";
    public final static String TripleDes = "TripleDes";
    public final static String MustSupportRefIssuerSerial = "MustSupportRefIssuerSerial";
    public final static String EncryptedParts = "EncryptedParts";
    public final static String Basic192Sha256 = "Basic192Sha256";
    public final static String AlgorithmSuite = "AlgorithmSuite";
    public final static String WssRelV20Token11 = "WssRelV20Token11";
    public final static String TransportBinding = "TransportBinding";
    public final static String SupportingTokens = "SupportingTokens";
    public final static String X509Token = "X509Token";
    public final static String WssX509V1Token10 = "WssX509V1Token10";
    public final static String WssX509V1Token11 = "WssX509V1Token11";
    public final static String WssX509V3Token11 = "WssX509V3Token11";
    public final static String RecipientToken = "RecipientToken";
    public final static String EncryptionToken = "EncryptionToken";
    public final static String Lax = "Lax";
    public final static String Layout = "Layout";
    public final static String RequireIssuerSerialReference = "RequireIssuerSerialReference";
    
    public final static String Body = "Body";
    
    public final static String HEADER = "Header";
    public final static String RequestSecurityTokenTemplate = "RequestSecurityTokenTemplate";
    public final static String EndpointReference = "EndpointReference";
    public final static String IncludeToken = "IncludeToken";
    public final static String XPath = "XPath";
    public final static String RequireClientCertificate = "RequireClientCertificate";
    
    
    //Trust constants
    public final static String KeyType = "KeyType";
    public final static String KeySize = "KeySize";
    public final static String UseKey = "UseKey";
    public final static String Encryption = "Encryption";
    public final static String ProofEncryption = "ProofEncryption";
    public final static String Lifetime = "Lifetime";
    public final static String Issuer = "Issuer";
    
    //Addressing constants
    public final static String Address = "Address";
    
    //utility constants
    public final static String Created = "Created";
    public final static String Expires = "Expires";
    public final static String SignWith ="SignWith";
    public final static String EncryptWith = "EncryptWith";
    public final static String TokenType ="TokenType";
    public final static String RequestType = "RequestType";
    public final static String RequestSecurityToken = "RequestSecurityToken";
    public final static String OnBehalfOf ="OnBehalfOf"; 
    public final static String AuthenticationType = "AuthenticationType";
    public final static String CanonicalizationAlgorithm ="CanonicalizationAlgorithm";
    public final static String SignatureAlgorithm ="SignatureAlgorithm";
    public final static String EncryptionAlgorithm ="EncryptionAlgorithm";
    public final static String ComputedKeyAlgorithm ="ComputedKeyAlgorithm";
    public static final String WS_SECURITY_POLICY_DOMAIN = "javax.enterprise.resource.xml.webservices.security.policy";
    public static final String WS_SECURITY_POLICY_PACKAGE_ROOT = "com.sun.xml.ws.security.impl.policy";
    public static final String WS_SECURITY_POLICY_DOMAIN_BUNDLE = WS_SECURITY_POLICY_PACKAGE_ROOT + ".LogStrings";
    public static Logger logger = Logger.getLogger(Constants.WS_SECURITY_POLICY_DOMAIN,Constants.WS_SECURITY_POLICY_DOMAIN_BUNDLE);
    
    public static final String SUN_WSS_SECURITY_CLIENT_POLICY_NS="http://schemas.sun.com/2006/03/wss/client";
    public static final String SUN_WSS_SECURITY_SERVER_POLICY_NS="http://schemas.sun.com/2006/03/wss/server";
    
    public static final String SUN_TRUST_CLIENT_SECURITY_POLICY_NS="http://schemas.sun.com/ws/2006/05/trust/client";
    public static final String SUN_TRUST_SERVER_SECURITY_POLICY_NS="http://schemas.sun.com/ws/2006/05/trust/server";
    public static final String SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS="http://schemas.sun.com/ws/2006/05/sc/client";
    public static final String SUN_SECURE_SERVER_CONVERSATION_POLICY_NS="http://schemas.sun.com/ws/2006/05/sc/server";
    
    public static final String KeyStore = "KeyStore";
    public static final String TrustStore = "TrustStore";
    public static final String CallbackHandler = "CallbackHandler";
    public static final String CallbackHandlerConfiguration = "CallbackHandlerConfiguration";
    public static final String Validator = "Validator";
    public static final String ValidatorConfiguration = "ValidatorConfiguration";
    public static final String ReferenceParameters = "ReferenceParameters";
    public static final String ReferenceProperties = "ReferenceProperties";
    public final static String PortType ="PortType";
    public final static String ServiceName ="ServiceName";
}
