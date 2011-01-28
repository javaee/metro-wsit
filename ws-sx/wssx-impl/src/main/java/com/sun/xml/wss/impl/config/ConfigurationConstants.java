/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

/*
 * $Id: ConfigurationConstants.java,v 1.2 2010-10-21 15:37:25 snajper Exp $
 */

package com.sun.xml.wss.impl.config;

import com.sun.xml.wss.impl.MessageConstants;
import javax.xml.namespace.QName;
import com.sun.xml.wss.impl.configuration.*;

/**
 * @author XWS-Security Development Team
 */
public interface ConfigurationConstants {

    public static final String CONFIGURATION_URL = "http://java.sun.com/xml/ns/xwss/config";
    public static final String DEFAULT_CONFIGURATION_PREFIX = "xwss";


    // --- JAXRPC Security Configuration -- //
    public static final String JAXRPC_SECURITY_ELEMENT_NAME = "JAXRPCSecurity";
    public static final String SECURITY_ENVIRONMENT_HANDLER_ELEMENT_NAME = "SecurityEnvironmentHandler";  
    public static final String SERVICE_ELEMENT_NAME = "Service";
    public static final String PORT_ELEMENT_NAME = "Port";
    public static final String OPERATION_ELEMENT_NAME = "Operation";
    public static final String NAME_ATTRIBUTE_NAME = "name";

    // 2.0 additions
    public static final String OPTIMIZE_ATTRIBUTE_NAME = "optimize";
    public static final String ID_ATTRIBUTE_NAME = "id"; // this one is to be used everywhere including UUID
    public static final String CONFORMANCE_ATTRIBUTE_NAME = "conformance";
    public static final String USECACHE_ATTRIBUTE_NAME = "useCache";

    public static final String BSP_CONFORMANCE = "bsp";

    public static final String RETAIN_SEC_HEADER = "retainSecurityHeader";
    public static final String RESET_MUST_UNDERSTAND = "resetMustUnderstand";
    
    // --- Declarative Configuration --
    public static final String DECLARATIVE_CONFIGURATION_ELEMENT_NAME = "SecurityConfiguration";

    public static final String DUMP_MESSAGES_ATTRIBUTE_NAME = "dumpMessages";

    //2.0 addition
    public static final String ENABLE_DYNAMIC_POLICY_ATTRIBUTE_NAME = "enableDynamicPolicy";
    
    // WSS 1.1 Policy
    public static final String ENABLE_WSS11_POLICY_ATTRIBUTE_NAME = "enableWSS11Policy";

    //TODO: something used by config tool check and remove
    public static final String SIGNED_TOKEN_REQUIRED_ATTRIBUTE_NAME = "signedTokenRequired";

    // OptionalTargets
    public static final String OPTIONAL_TARGETS_ELEMENT_NAME = "OptionalTargets";


    // requireSignature 
    public static final String SIGNATURE_REQUIREMENT_ELEMENT_NAME = "RequireSignature";
    public static final String TIMESTAMP_REQUIRED_ATTRIBUTE_NAME = "requireTimestamp";

    // requireEncryption
    public static final String ENCRYPTION_REQUIREMENT_ELEMENT_NAME = "RequireEncryption";

    // requireUsernameToken
    public static final String USERNAMETOKEN_REQUIREMENT_ELEMENT_NAME = "RequireUsernameToken";
    public static final String NONCE_REQUIRED_ATTRIBUTE_NAME = "nonceRequired";
    public static final String PASSWORD_DIGEST_REQUIRED_ATTRIBUTE_NAME = "passwordDigestRequired";

    // requireTimestamp
    public static final String TIMESTAMP_REQUIREMENT_ELEMENT_NAME = "RequireTimestamp";

    // Timestamp
    public static final String TIMESTAMP_ELEMENT_NAME = "Timestamp";
    public static final String TIMEOUT_ATTRIBUTE_NAME = "timeout";

    // Sign     
    public static final String SIGN_OPERATION_ELEMENT_NAME = "Sign";
    public static final String INCLUDE_TIMESTAMP_ATTRIBUTE_NAME = "includeTimestamp";

    // Encrypt
    public static final String ENCRYPT_OPERATION_ELEMENT_NAME = "Encrypt";

    //2.0 addition
    //SAML Assertion
    public static final String SAML_ASSERTION_ELEMENT_NAME = "SAMLAssertion";
    public static final String SAML_ASSERTION_TYPE_ATTRIBUTE_NAME = "type";
    public static final String SAML_AUTHORITY_ID_ATTRIBUTE_NAME = "authorityId";
    public static final String SAML_KEYIDENTIFIER_ATTRIBUTE_NAME = "keyIdentifier";

    public static final String SV_SAML_TYPE = "SV";
    public static final String HOK_SAML_TYPE = "HOK";
   

    public static final String REQUIRE_SAML_ASSERTION_ELEMENT_NAME = "RequireSAMLAssertion";


    // X509Token
    public static final String X509TOKEN_ELEMENT_NAME = "X509Token";
    public static final String KEY_REFERENCE_TYPE_ATTRIBUTE_NAME = "keyReferenceType";
    public static final String CERTIFICATE_ALIAS_ATTRIBUTE_NAME = "certificateAlias";
    //2.0 addition
    public static final String ENCODING_TYPE_ATTRIBUTE_NAME = "EncodingType";
    public static final String VALUE_TYPE_ATTRIBUTE_NAME = "ValueType";

    // SymmetricKey
    public static final String SYMMETRIC_KEY_ELEMENT_NAME = "SymmetricKey";
    public static final String SYMMETRIC_KEY_ALIAS_ATTRIBUTE_NAME = "keyAlias";

    // Target
    public static final String TARGET_ELEMENT_NAME = "Target";
    public static final String TARGET_TYPE_ATTRIBUTE_NAME = "type";
    public static final String CONTENT_ONLY_ATTRIBUTE_NAME = "contentOnly";
    public static final String ENFORCE_ATTRIBUTE_NAME = "enforce";
    public static final String TARGET_VALUE_SOAP_BODY = "SOAP-BODY";

    //2.0 addition
    public static final String URI_TARGET = "uri";
    public static final String QNAME_TARGET = "qname";
    public static final String XPATH_TARGET = "xpath";
    
    public static final String ENCRYPTION_TARGET_ELEMENT_NAME = "EncryptionTarget";
    public static final String SIGNATURE_TARGET_ELEMENT_NAME = "SignatureTarget";

    //2.0 addition
    public static final String DIGEST_METHOD_ELEMENT_NAME = "DigestMethod";
    public static final String CANONICALIZATION_METHOD_ELEMENT_NAME = "CanonicalizationMethod";
    public static final String SIGNATURE_METHOD_ELEMENT_NAME = "SignatureMethod";
    public static final String KEY_ENCRYPTION_METHOD_ELEMENT_NAME = "KeyEncryptionMethod";
    public static final String DATA_ENCRYPTION_METHOD_ELEMENT_NAME = "DataEncryptionMethod";

    //2.0 addition
    public static final String TRANSFORM_ELEMENT_NAME = "Transform";
    public static final String ALGORITHM_PARAMETER_ELEMENT_NAME = "AlgorithmParameter";

    //2.0 addition
    public static final String ALGORITHM_ATTRIBUTE_NAME = "algorithm";
    public static final String VALUE_ATTRIBUTE_NAME = "value";
    public static final String DISABLE_INCLUSIVE_PREFIX = "disableInclusivePrefix";

    
    // keyReferenceType
    public static final String DIRECT_KEY_REFERENCE_TYPE = MessageConstants.DIRECT_REFERENCE_TYPE;
    public static final String IDENTIFIER_KEY_REFERENCE_TYPE = MessageConstants.KEY_INDETIFIER_TYPE;
    public static final String SERIAL_KEY_REFERENCE_TYPE = MessageConstants.X509_ISSUER_TYPE;
    //2.0 addition
    public static final String EMBEDDED_KEY_REFERENCE_TYPE = MessageConstants.EMBEDDED_REFERENCE_TYPE;

    // UsernamePassword    
    public static final String USERNAME_PASSWORD_AUTHENTICATION_ELEMENT_NAME = "UsernameToken";
    public static final String USERNAME_ATTRIBUTE_NAME = "name";
    public static final String PASSWORD_ATTRIBUTE_NAME = "password";
    public static final String USE_NONCE_ATTRIBUTE_NAME = "useNonce";
    public static final String DIGEST_PASSWORD_ATTRIBUTE_NAME = "digestPassword";


    public static final QName DECLARATIVE_CONFIGURATION_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            DECLARATIVE_CONFIGURATION_ELEMENT_NAME);
    public static final QName SIGN_OPERATION_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            SIGN_OPERATION_ELEMENT_NAME);
    public static final QName ENCRYPT_OPERATION_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            ENCRYPT_OPERATION_ELEMENT_NAME);
    public static final QName TARGET_QNAME = new QName(
            CONFIGURATION_URL,
            TARGET_ELEMENT_NAME);
    public static final QName TIMESTAMP_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            TIMESTAMP_ELEMENT_NAME);
    public static final QName X509TOKEN_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            X509TOKEN_ELEMENT_NAME);
    public static final QName SYMMETRIC_KEY_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            SYMMETRIC_KEY_ELEMENT_NAME);
    public static final QName USERNAME_PASSWORD_AUTHENTICATION_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            USERNAME_PASSWORD_AUTHENTICATION_ELEMENT_NAME);
    public static final QName TIMESTAMP_REQUIREMENT_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            TIMESTAMP_REQUIREMENT_ELEMENT_NAME);
    public static final QName SIGNATURE_REQUIREMENT_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            SIGNATURE_REQUIREMENT_ELEMENT_NAME);
    public static final QName ENCRYPTION_REQUIREMENT_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            ENCRYPTION_REQUIREMENT_ELEMENT_NAME);
    public static final QName USERNAMETOKEN_REQUIREMENT_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            USERNAMETOKEN_REQUIREMENT_ELEMENT_NAME);
    public static final QName OPTIONAL_TARGETS_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            OPTIONAL_TARGETS_ELEMENT_NAME);
    public static final QName JAXRPC_SECURITY_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            JAXRPC_SECURITY_ELEMENT_NAME);
    public static final QName SERVICE_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            SERVICE_ELEMENT_NAME);
    public static final QName SECURITY_ENVIRONMENT_HANDLER_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            SECURITY_ENVIRONMENT_HANDLER_ELEMENT_NAME);
    public static final QName PORT_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            PORT_ELEMENT_NAME);
    public static final QName OPERATION_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            OPERATION_ELEMENT_NAME);

    //2.0 additions
    public static final QName SAML_ELEMENT_QNAME = new QName(
             CONFIGURATION_URL,
             SAML_ASSERTION_ELEMENT_NAME);
    public static final QName SAML_REQUIREMENT_ELEMENT_QNAME = new QName(
             CONFIGURATION_URL,
             REQUIRE_SAML_ASSERTION_ELEMENT_NAME);

    //2.0 addition
    public static final QName ENCRYPTION_TARGET_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            ENCRYPTION_TARGET_ELEMENT_NAME);

    public static final QName SIGNATURE_TARGET_ELEMENT_QNAME = new QName(
             CONFIGURATION_URL,
             SIGNATURE_TARGET_ELEMENT_NAME);

    //2.0 addition
    public static final QName DIGEST_METHOD_ELEMENT_QNAME =  new QName(
            CONFIGURATION_URL,
            DIGEST_METHOD_ELEMENT_NAME);

    public static final QName CANONICALIZATION_METHOD_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL,
            CANONICALIZATION_METHOD_ELEMENT_NAME);

    public static final QName SIGNATURE_METHOD_ELEMENT_QNAME =  new QName(
            CONFIGURATION_URL,
            SIGNATURE_METHOD_ELEMENT_NAME);

    public static final QName KEY_ENCRYPTION_METHOD_ELEMENT_QNAME =  new QName(
            CONFIGURATION_URL,
            KEY_ENCRYPTION_METHOD_ELEMENT_NAME);

    public static final QName DATA_ENCRYPTION_METHOD_ELEMENT_QNAME =  new QName(
            CONFIGURATION_URL,
            DATA_ENCRYPTION_METHOD_ELEMENT_NAME); 

    //2.0 addition
    public static final QName TRANSFORM_ELEMENT_QNAME = new QName(
            CONFIGURATION_URL, 
            TRANSFORM_ELEMENT_NAME);

    public static final QName ALGORITHM_PARAMETER_ELEMENT_QNAME =  new QName(
            CONFIGURATION_URL,
            ALGORITHM_PARAMETER_ELEMENT_NAME);

    //2.0 addition
    public static final String DEFAULT_DATA_ENC_ALGO = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";
    public static final String DEFAULT_KEY_ENC_ALGO = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";

    // 2.0 addition
    public static final String MAX_NONCE_AGE = "maxNonceAge";
    public static final String MAX_CLOCK_SKEW = "maxClockSkew" ;
    public static final String TIMESTAMP_FRESHNESS_LIMIT = "timestampFreshnessLimit";
    public static final String STRID =  "strId";
}
