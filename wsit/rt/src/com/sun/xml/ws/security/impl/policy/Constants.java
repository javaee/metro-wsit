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
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
public class Constants {
    
    
    //Namespace constants
    public final static String SECURITY_POLICY_NS = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
    public final static String ADDRESSING_NS = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    public final static String XPATH_NS = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    public final static String TRUST_NS = "http://schemas.xmlsoap.org/ws/2005/02/trust";
    public final static String UTILITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis- 200401-wss-wssecurity-utility-1.0.xsd";
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

    public static void log_invalid_assertion(PolicyAssertion assertion , boolean isServer,String parentAssertion){
        
        Level level = Level.SEVERE;
        if(!isServer){
            level = Level.WARNING;
        }
        if(logger.isLoggable(level)){
            logger.log(level,LogStringsMessages.SP_0100_INVALID_SECURITY_ASSERTION(assertion,parentAssertion));
        }
        
    }
}
