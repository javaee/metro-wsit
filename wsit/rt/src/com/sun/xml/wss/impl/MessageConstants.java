/*
 * $Id: MessageConstants.java,v 1.7.2.2 2010-07-14 14:05:47 m_potociar Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.wss.impl;

import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import javax.xml.namespace.QName;
import com.sun.xml.wss.*;

/**
 * WSS xmlns and prefix constants used in wss code throughout.
 *
 * @author Manveen Kaur
 */
public class MessageConstants {
    
    public static final long MAX_NONCE_AGE=900000;;
    public static final int NOT_OPTIMIZED = 0 ;
    public static final int SIGN_BODY = 1;
    public static final int SIGN_ENCRYPT_BODY = 2;
    public static final int ENCRYPT_SIGN_BODY =3;
    public static final int SECURITY_HEADERS = 4;
    public static final int ENCRYPT_BODY = 5;
    public static final int SECURE_ATTACHMENTS = 6;
    public static final int SECURITY_HEADERS_AND_ATTACHMENTS = 7;
    public static final boolean debug = false;
    public static final int OCTECT_STREAM_DATA = 1;
    public static final int NODE_SET_DATA = 2;
    public static final int ATTACHMENT_DATA = 3;
    public static final int DEFAULT_VALUEOF_ITERATIONS = 1000;
    public static final int VALUE_FOR_SIGNATURE = 01;
    public static final int VALUE_FOR_ENCRYPTION = 02;
    public static final String EMPTY_STRING ="";
    public static final String _EMPTY = "".intern();
    public static final String TIMESTAMP_XPATH = "//wsu:Timestamp";
    public static final String ATTACHMENT_MIME_HEADERS = "MIME-HEADERS";
    public static final String WSS_PROCESSING_CONTEXT = "http://wss.sun.com#processingContext";
    public static final String AUTH_SUBJECT = "javax.security.auth.Subject";
    public static final String DIRECT_REFERENCE_TYPE = "Direct";
    public static final String KEY_INDETIFIER_TYPE = "Identifier";
    public static final String THUMB_PRINT_TYPE = "Thumbprint";
    public static final String EK_SHA1_TYPE = "EncryptedKeySHA1";
    public static final String KEY_NAME_TYPE = "KeyName";
    public static final String X509_ISSUER_TYPE = "IssuerSerialNumber";
    public static final String BINARY_SECRET="BinarySecret";
    public static final String EMBEDDED_REFERENCE_TYPE = "Embedded";
    public static final String NONCE_CACHE = "NonceCache";
    public static final String SOAP_1_1_NS = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP_1_2_NS = "http://www.w3.org/2003/05/soap-envelope";
    public static final String XMLNS_TAG = "xmlns";
    public final static String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    public final static String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String NAMESPACES_NS = "http://www.w3.org/2000/xmlns/";
    
    public static final String XML_PREFIX = "xml";
    public static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
    
    public static final String DSIG_NS = "http://www.w3.org/2000/09/xmldsig#";
    public static final String DSIG_PREFIX = "ds";
    
    public static final String DS_SIGNATURE_LNAME = "Signature";
    public static final String DS_SIGNATURE_QNAME =
            DSIG_PREFIX + ":" + DS_SIGNATURE_LNAME;
    
    public static final short SAML_v1_0_NUMBER = 0x000;
    public static final short SAML_v1_1_NUMBER = 0x001;
    public static final short SAML_v2_0_NUMBER = 0x002;
    public static final String SAML_v1_0_STRING = "1.0";
    public static final String SAML_v1_1_STRING = "1.1";
    public static final String SAML_v2_0_STRING = "2.0";
    public static final String SAML_v1_0_NS =
            "urn:oasis:names:tc:SAML:1.0:assertion";
    public static final String SAML_v1_1_NS =
            "urn:oasis:names:tc:SAML:1.0:assertion";
    public static final String SAML_v2_0_NS =
            "urn:oasis:names:tc:SAML:2.0:assertion";
    public static final String SAMLP_NS =
            "urn:oasis:names:tc:SAML:1.0:protocol";
    public static final String SAML_PREFIX = "saml";
    public static final String SAML2_PREFIX = "saml2";
    public static final String SAMLP_PREFIX = "samlp";
    public static final String SAML_ASSERTION_LNAME = "Assertion";
    public static final String SAML_ASSERTIONID_LNAME = "AssertionID";
    public static final String SAML_ID_LNAME = "ID";
    public static final String SAML_QNAME =
            SAML_PREFIX + ":" + SAML_ASSERTION_LNAME;
    public static final String SAML2_QNAME =
            SAML2_PREFIX + ":" + SAML_ASSERTION_LNAME;
    public static final String WSSE_SAML_v1_0_VALUE_TYPE =
            "http://docs.oasis-open.org/wss/2004/XX/"
            + "oasis-2004XX-wss-saml-token-profile-1.0#SAMLAssertion-1.0";
    public static final String WSSE_SAML_v1_1_VALUE_TYPE =
            "http://docs.oasis-open.org/wss/2004/XX/"
            + "oasis-2004XX-wss-saml-token-profile-1.0#SAMLAssertion-1.1";
    public static final String WSSE_SAML_v2_0_VALUE_TYPE =
            "http://docs.oasis-open.org/wss/2004/XX/"
            + "oasis-2004XX-wss-saml-token-profile-1.1#SAMLAssertion-2.0";
    public static final String WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE =
            "http://docs.oasis-open.org/wss/"
            + "oasis-wss-saml-token-profile-1.0#SAMLAssertionID";
    public static final String WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE =
            "http://docs.oasis-open.org/wss/"
            + "oasis-wss-saml-token-profile-1.1#SAMLID";
    public static final String WSSE_SAML_v1_1_TOKEN_TYPE =
            "http://docs.oasis-open.org/wss/"
            + "oasis-wss-saml-token-profile-1.1#SAMLV1.1";
    public static final String WSSE_SAML_v2_0_TOKEN_TYPE =
            "http://docs.oasis-open.org/wss/"
            + "oasis-wss-saml-token-profile-1.1#SAMLV2.0";
    
    public static final String INCOMING_SAML_ASSERTION = "incoming_saml_assertion";
    public static final String STORED_SAML_KEYS = "stored_saml_keys";
    public static final String SAML_ASSERTION_CLIENT_CACHE ="Saml_Assertion_Client_Cache";
    public static final String SAML_SIG_RESOLVED ="Saml_Signature_resolved";
    
    public static final String SAML_XMLNS_TAG = XMLNS_TAG + ":" + SAML_PREFIX;
    public static final String SAML2_XMLNS_TAG = XMLNS_TAG + ":" + SAML2_PREFIX;
    public static final String SAMLP_XMLNS_TAG = XMLNS_TAG + ":" + SAMLP_PREFIX;
    
    public static final String SAML_SENDER_VOUCHES =
            "urn:oasis:names:tc:SAML:1.0:cm:sender-vouches";
    public static final String SAML2_SENDER_VOUCHES =
            "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";
    
    public static final String SAML_HOLDER_OF_KEY =
            "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key";
    public static final String SAML2_HOLDER_OF_KEY =
            "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";
    
    public static final String XENC_NS = "http://www.w3.org/2001/04/xmlenc#";
    public static final String XENC_PREFIX = "xenc";
    public static final String WSE_VALUE_TYPE="ValueType";
    
    public static final String XENC_REFERENCE_LIST_LNAME = "ReferenceList";
    public static final String XENC_REFERENCE_LIST_QNAME =
            XENC_PREFIX + ":" + XENC_REFERENCE_LIST_LNAME;
    
    public static final String XENC_ENCRYPTED_KEY_LNAME = "EncryptedKey";
    public static final String XENC_ENCRYPTED_KEY_QNAME =
            XENC_PREFIX + ":" + XENC_ENCRYPTED_KEY_LNAME;
    
    public static final String XENC_CIPHER_DATA_LNAME = "CipherData";
    public static final String XENC_CIPHER_DATA_QNAME =
            XENC_PREFIX + ":" + XENC_CIPHER_DATA_LNAME;
    
    public static final String ENCRYPTED_DATA_LNAME = "EncryptedData";
    public static final String ENCRYPTED_DATA_QNAME =
            XENC_PREFIX + ":" + ENCRYPTED_DATA_LNAME;
    
    public static final String WSU_NS =
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    public static final String WSU_PREFIX = "wsu";
    
    public static final String SWA_NS =
            "http://docs.oasis-open.org/wss/2004/XX/oasis-2004XX-wss-swa-profile-1.0";
    
    public static final String WSU_ID_QNAME = WSU_PREFIX + ":Id";
    
    public static final String SOAP_HEADER_LNAME = "Header";
    public static final String SOAP_BODY_LNAME = "Body";
    
    public static final String WSSE_NS =
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String WSSE_PREFIX = "wsse";
    public static final String WSSE11_PREFIX = "wsse11";
    
    public static final String WSSE11_NS =
            "http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd";
    
    public static final String ENCRYPTED_HEADER_LNAME = "EncryptedHeader";
    public static final String ENCRYPTED_HEADER_QNAME =
            WSSE11_PREFIX + ":" + ENCRYPTED_HEADER_LNAME;
    
    public static final String WSSE11_TOKEN_TYPE = WSSE11_PREFIX + ":TokenType";
    public static final String TOKEN_TYPE_LNAME = "TokenType";
    
    public static final String WSSE_SECURITY_LNAME = "Security";
    public static final String WSSE_SECURITY_QNAME =
            WSSE_PREFIX + ":" + WSSE_SECURITY_LNAME;
    
    public static final String WSSE_BINARY_SECURITY_TOKEN_LNAME =
            "BinarySecurityToken";
    public static final String WSSE_BINARY_SECURITY_TOKEN_QNAME =
            WSSE_PREFIX + ":" + WSSE_BINARY_SECURITY_TOKEN_LNAME;
    
    public static final String WSSE_SECURITY_TOKEN_REFERENCE_LNAME =
            "SecurityTokenReference";
    
    public static final String WSSE_SECURITY_TOKEN_REFERENCE_QNAME =
            WSSE_PREFIX + ":" + WSSE_SECURITY_TOKEN_REFERENCE_LNAME;
    
    public static final String WSSE_REFERENCE_LNAME = "Reference";
    
    public static final String USERNAME_TOKEN_LNAME = "UsernameToken";
    
    public static final String TIMESTAMP_LNAME = "Timestamp";
    
    public static final String SIGNATURE_CONFIRMATION_LNAME = "SignatureConfirmation";
    
    public static final String DERIVEDKEY_TOKEN_LNAME = "DerivedKeyToken";
    
    public static final String WSSE_REFERENCE_QNAME =
            WSSE_PREFIX + ":" + WSSE_REFERENCE_LNAME;
    
    public static final String WSSE_REFERENCE_ATTR_URI = "URI";
    
    public static final String WSSE_IDENTIFIER_ATTR_VALUETYPE = "ValueType";
    
    public static final String USERNAME_TOKEN_NS =
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0";
    
    public static final String PASSWORD_TEXT_NS = USERNAME_TOKEN_NS + "#PasswordText";
    
    public static final String PASSWORD_DIGEST_NS = USERNAME_TOKEN_NS + "#PasswordDigest";

    public static final String USERNAME_STR_REFERENCE_NS = USERNAME_TOKEN_NS+"#"+USERNAME_TOKEN_LNAME;
    public static final String WSS_SPEC_NS =
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0";
    
    public static final String WSS11_SPEC_NS =
            "http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1";
    
    public static final String BASE64_ENCODING_NS =
            WSS_SPEC_NS + "#Base64Binary";
    
    public static final String X509_TOKEN_NS =
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0";
    
    public static final String X509_NS = X509_TOKEN_NS + "#X509";
    
    public static final String X509v1_NS = X509_TOKEN_NS + "#X509v1";
    
    public static final String X509v3_NS = X509_TOKEN_NS + "#X509v3";
    
    public static final String X509SubjectKeyIdentifier_NS =
            X509_TOKEN_NS + "#X509SubjectKeyIdentifier";
    
    public static final String KERBEROS_TOKEN_NS = "http://docs.oasis-open.org/wss/oasis-wss-kerberos-token-profile-1.1";
    
    public static final String KERBEROS_V5_APREQ = KERBEROS_TOKEN_NS + "#Kerberosv5_AP_REQ";
    
    public static final String KERBEROS_V5_GSS_APREQ = KERBEROS_TOKEN_NS + "#GSS_Kerberosv5_AP_REQ";
    
    public static final String KERBEROS_V5_APREQ_1510 = KERBEROS_TOKEN_NS + "#Kerberosv5_AP_REQ1510";
    
    public static final String KERBEROS_V5_GSS_APREQ_1510 = KERBEROS_TOKEN_NS + "#GSS_Kerberosv5_AP_REQ1510";
    
    public static final String KERBEROS_V5_APREQ_4120 = KERBEROS_TOKEN_NS + "#Kerberosv5_AP_REQ4120";
    
    public static final String KERBEROS_V5_GSS_APREQ_4120  =  KERBEROS_TOKEN_NS + "#GSS_Kerberosv5_AP_REQ4120";
    
    public static final String KERBEROS_v5_APREQ_IDENTIFIER = KERBEROS_TOKEN_NS + "#Kerberosv5APREQSHA1";
    
    public static final String ThumbPrintIdentifier_NS =
            WSS11_SPEC_NS + "#ThumbprintSHA1";
    
    public static final String EncryptedKeyIdentifier_NS =
            WSS11_SPEC_NS + "#EncryptedKeySHA1";
    
    public static final String EncryptedKey_NS =
            WSS11_SPEC_NS + "#EncryptedKey";
    // added in X509 Token Profile Errata
    public static final String X509v3SubjectKeyIdentifier_NS =
            X509_TOKEN_NS + "#X509v3SubjectKeyIdentifier";
    
    public static final String STR_TRANSFORM_URI =
            WSS_SPEC_NS + "#STR-Transform";
    
    public static final String ATTACHMENT_CONTENT_ONLY_TRANSFORM_URI =
            SWA_NS + "#Attachment-Content-Only-Transform";
    
    public static final String ATTACHMENT_COMPLETE_TRANSFORM_URI =
            SWA_NS + "#Attachment-Complete-Transform";
    
    public static final String ATTACHMENT_CONTENT_ONLY_URI =
            SWA_NS + "#Attachment-Content-Only";
    
    public static final String ATTACHMENT_COMPLETE_URI =
            SWA_NS + "#Attachment-Complete";
    
    public static final String TRANSFORM_C14N_EXCL_OMIT_COMMENTS =
            "http://www.w3.org/2001/10/xml-exc-c14n#";
    
    public static final String TRANSFORM_FILTER2 =
            "http://www.w3.org/2002/06/xmldsig-filter2";
    
    //Security Policy Namespace
    public static final String SECURITYPOLICY_200507_NS = 
            "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
    
    //Security Policy Namespace for 1.2 version 
    public static final String SECURITYPOLICY_12_NS = 
            "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702";
    
    // Secure Conversation Namespace
    public static final String WSSC_NS =
            "http://schemas.xmlsoap.org/ws/2005/02/sc";
    
    // Secure Conversation Namespace for 1.3 version
    public static final String WSSC_13NS =
            "http://docs.oasis-open.org/ws-sx/ws-secureconversation/200512";
    
    public static final String WSSC_PREFIX = "wsc";
    
    public static final String SCT_VALUETYPE = WSSC_NS + "/sct";
    public static final String DKT_VALUETYPE = WSSC_NS + "/dk";
    
    public static final String SCT_13_VALUETYPE = WSSC_13NS + "/sct";
    public static final String DKT_13_VALUETYPE = WSSC_13NS + "/dk";

    /** SOAPFault related constants **/
    
    //---------- Errors ---------------
    public static final QName WSSE_UNSUPPORTED_SECURITY_TOKEN =
            new QName(WSSE_NS, "UnsupportedSecurityToken", WSSE_PREFIX);
    
    public static final QName WSSE_UNSUPPORTED_ALGORITHM =
            new QName(WSSE_NS, "UnsupportedAlgorithm", WSSE_PREFIX);
    
    public static final QName WSSE_INTERNAL_SERVER_ERROR =
            new QName(WSSE_NS, "InternalServerError", WSSE_PREFIX);
    
    //--------- Failures --------------
    public static final QName WSSE_INVALID_SECURITY =
            new QName(WSSE_NS, "InvalidSecurity", WSSE_PREFIX);
    
    public static final QName WSSE_INVALID_SECURITY_TOKEN =
            new QName(WSSE_NS, "InvalidSecurityToken", WSSE_PREFIX);
    
    public static final QName WSSE_FAILED_AUTHENTICATION =
            new QName(WSSE_NS, "FailedAuthentication", WSSE_PREFIX);
    
    public static final QName WSSE_RECEIVER_POLICY_VIOLATION =
            new QName(WSSE_NS, "PolicyViolation", WSSE_PREFIX);
    
    public static final QName WSSE_FAILED_CHECK =
            new QName(WSSE_NS, "FailedCheck", WSSE_PREFIX);
    
    public static final QName WSSE_SECURITY_TOKEN_UNAVAILABLE =
            new QName(WSSE_NS, "SecurityTokenUnavailable", WSSE_PREFIX);
    
    public static final QName WSU_MESSAGE_EXPIRED =
            new QName(WSU_NS, "MessageExpired", WSU_PREFIX);
    
    /** WSS security header QName */
    private static final QName securityHeaderName =
            new QName(WSSE_NS, WSSE_SECURITY_LNAME, WSSE_PREFIX);
    
    public static final String TRANSFORMATION_PARAMETERS = "TransformationParameters";

    public static final String RSTR_CANCEL_ACTION = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RSTR/SCT/Cancel";
    
    /**
     * TODO Decide if there is a better interface for this
     *
     * @return QName representing the Security header block from WSS spec
     */
    public static QName getSecurityHeaderName() {
        return securityHeaderName;
    }
    public static final String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
    public static final String SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";
    public static final String RSA_SHA1 = "SHA1withRSA";
    public static final String RSA_SHA256 = "SHA256withRSA";
    public static final String RSA_SHA384 = "SHA384withRSA";
    public static final String RSA_SHA512 = "SHA512withRSA";  
    public static final String SHA_1 = "SHA-1";
    public static final String SHA_256 = "SHA-256";
    public static final String SHA_512 = "SHA-512";
    //Algorithm Constants
    public static final String RSA_OAEP_KEY_TRANSPORT = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";
    public static final String RSA_15_KEY_TRANSPORT = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";
    
    public static final String TRIPLE_DES_KEY_WRAP = "http://www.w3.org/2001/04/xmlenc#kw-tripledes";
    
    public static final String AES_KEY_WRAP_128 = "http://www.w3.org/2001/04/xmlenc#kw-aes128";
    public static final String AES_KEY_WRAP_192 = "http://www.w3.org/2001/04/xmlenc#kw-aes192";
    public static final String AES_KEY_WRAP_256 = "http://www.w3.org/2001/04/xmlenc#kw-aes256";
    
    public static final String TRIPLE_DES_BLOCK_ENCRYPTION = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";
    
    public static final String AES_BLOCK_ENCRYPTION_128 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";
    public static final String AES_BLOCK_ENCRYPTION_192 = "http://www.w3.org/2001/04/xmlenc#aes192-cbc";
    public static final String AES_BLOCK_ENCRYPTION_256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";
    
    public static final String SHA1_DIGEST = "http://www.w3.org/2000/09/xmldsig#sha1";
    public static final String PSHA1_DIGEST = "http://schemas.xmlsoap.org/ws/2005/02/trust/CK/PSHA1";
    public static final String DSIG_BASE64_ENCODING = "http://www.w3.org/2000/09/xmldsig#base64";
    
    public static final String RSA_SHA1_SIGMETHOD = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    public static final String RSA_SHA256_SIGMETHOD = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    public static final String RSA_SHA384_SIGMETHOD = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384"; 
    public static final String RSA_SHA512_SIGMETHOD = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";
    public static final String DSA_SHA1_SIGMETHOD = "http://www.w3.org/2000/09/xmldsig#dsa-sha1";
    public static final String HMAC_SHA1_SIGMETHOD = "http://www.w3.org/2000/09/xmldsig#hmac-sha1";
    public static final String DEFAULT_DATA_ENC_ALGO = XMLCipher.TRIPLEDES;
    
    public static final String ATTACHMENTREF = "attachmentRef:";
    
    public static final String TIMESTAMP_QNAME = "{"+ WSU_NS + "}" + TIMESTAMP_LNAME;
    public static final String PROCESS_ALL_ATTACHMENTS = "cid:*";
    
    // added for JSR 196 Provider new Features
    public static final String SELF_SUBJECT = "javax.security.auth.Subject.self";
    public static final String REQUESTER_KEYID = "requester.keyid";
    public static final String REQUESTER_ISSUERNAME = "requester.issuername";
    public static final String REQUESTER_SERIAL = "requester.serial";
    
    // FI + SOAP 1.1
    public static final String FAST_INFOSET_TYPE_SOAP11 =
            "application/fastinfoset";
    
    // FI + SOAP 1.2
    public static final String FAST_INFOSET_TYPE_SOAP12 =
            "application/soap+fastinfoset";
    
    // XML + XOP + SOAP 1.1
    public static final String XOP_SOAP11_XML_TYPE_VALUE =
            "application/xop+xml;type=\"text/xml\"";
    
    // XML + XOP + SOAP 1.2
    public static final String XOP_SOAP12_XML_TYPE_VALUE =
            "application/xop+xml;type=\"application/soap+xml\"";
    
    public static final String XML_CONTENT_TYPE_VALUE = "text/xml";
    
    public static final String ENCRYPTEDKEY_LNAME="EncryptedKey";
    
    public static final String SECURITY_CONTEXT_TOKEN_LNAME="SecurityContextToken";
    
    public static final String SECURITY_CONTEXT_TOKEN_NS =
            "http://schemas.xmlsoap.org/ws/2005/02/sc/sct";
    
    public static final String DERIVEDKEY_TOKEN_NS =
            "http://schemas.xmlsoap.org/ws/2005/02/sc/dk";
    
    public static final String INCOMING_SCT = "Incoming_SCT";
    
    public static final String BINARY_SECRET_LNAME = "BinarySecret";
    
    public static final String ADDRESSING_MEMBER_SUBMISSION_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    
    public static final String ADDRESSING_W3C_NAMESPACE = "http://www.w3.org/2005/08/addressing";
    
    public static final String ENCRYPT_ELEMENT_CONTENT = "http://www.w3.org/2001/04/xmlenc#Content";
    
    public static final String ENCRYPT_ELEMENT = "http://www.w3.org/2001/04/xmlenc#Element";
    
    public static final String MUST_UNDERSTAND = "mustUnderstand";
    
    public static final String SIGNATURE_LNAME = "Signature";
    
    public static final String EK_SHA1_VALUE = "EKSHA1Value";
    
    public static final String KERBEROS_SHA1_VALUE = "KerbSHA1Value";
    
    public static final String KERBEROS_CONTEXT = "KerberosContext";
    
    public static final String SECRET_KEY_VALUE = "SecretKeyValue";
    
    public static final String SUBJECT_KEY_IDENTIFIER_OID = "2.5.29.14";
    public static final String MEX_GET="http://schemas.xmlsoap.org/ws/2004/09/transfer/Get";
    public static final String CANONICALIZATION_METHOD = "CanonicalizationMethod";
    public static final String KEYIDENTIFIER = "KeyIdentifier";
    
    public static final String CANCEL_SECURITY_CONTEXT_TOKEN_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/SCT/Cancel";
    public static final String CANCEL_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION = "http://schemas.xmlsoap.org/ws/2005/02/trust/RSTR/SCT/Cancel";
    public static final QName SCT_NAME = new QName(SECURITY_CONTEXT_TOKEN_NS,SECURITY_CONTEXT_TOKEN_LNAME);
    public static final long MAX_CLOCK_SKEW = 300000; // milliseconds
    public static final long TIMESTAMP_FRESHNESS_LIMIT = 300000; // milliseconds
    
    public static final String SCBOOTSTRAP_CRED_IN_SUBJ = "SCBOOTSTRAP_CRED_IN_SUBJ";
    
    public static final String SWA11_NS = "http://docs.oasis-open.org/wss/oasis-wss-SwAProfile-1.1";
    
    public static final String SWA11_ATTACHMENT_CONTENT_SIGNATURE_TRANSFORM = 
            SWA11_NS + "#Attachment-Content-Signature-Transform";
    
    public static final String SWA11_ATTACHMENT_COMPLETE_SIGNATURE_TRANSFORM =
            SWA11_NS + "#Attachment-Content-Signature-Transform";
    
    public static final String SWA11_ATTACHMENT_CIPHERTEXT_TRANSFORM =
            SWA11_NS + "#Attachment-Ciphertext-Transform";
    
    public static final String SWA11_ATTACHMENT_CONTENT_ONLY = 
            SWA11_NS + "#Attachment-Content-Only";
    
    public static final String SWA11_ATTACHMENT_COMPLETE = 
            SWA11_NS + "#Attachment-Complete";
    
    public static final String KERBEROS_AUTH_TYPE = "urn:oasis:names:tc:SAML:2.0:ac:classes:Kerberos";
    public static final String PASSWORD_AUTH_TYPE = "urn:oasis:names:tc:SAML:2.0:ac:classes:Password";
    public static final String PASSWORD_PROTECTED_TRANSPORT_AUTHTYPE = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
    public static final String PREVIOUS_SESSION_AUTH_TYPE = "urn:oasis:names:tc:SAML:2.0:ac:classes:PreviousSession";
    public static final String X509_AUTH_TYPE = "urn:oasis:names:tc:SAML:2.0:ac:classes:X509";
    
    public static final String WSENDPOINT="WSEndpoint";
}
