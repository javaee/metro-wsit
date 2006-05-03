/*
 * $Id: PolicyTypeUtil.java,v 1.1 2006-05-03 22:57:37 arungupta Exp $
 */

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

package com.sun.xml.wss.impl;

import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.*;

/*
 * A type utility class for policies (useful for avoiding instanceof checks).
 */
public  class PolicyTypeUtil {
    

    public static final String SEC_POLICY_CONTAINER_TYPE = "SecurityPolicyContainer";
    public static final String DYN_SEC_POLICY_TYPE= "DynamicSecurityPolicy";
    public static final String BOOLEAN_COMPOSER_TYPE = "BooleanComposer";
    public static final String APP_SEC_CONFIG_TYPE = "ApplicationSecurityConfiguration";
    public static final String DECL_SEC_CONFIG_TYPE = "DeclarativeSecurityConfiguration";
    public static final String MESSAGEPOLICY_CONFIG_TYPE = "MessagePolicy";

    public static final String AUTH_POLICY_TYPE = "AuthenticationTokenPolicy";
    public static final String SIGNATURE_POLICY_TYPE = "SignaturePolicy";
    public static final String ENCRYPTION_POLICY_TYPE = "EncryptionPolicy";
    public static final String TIMESTAMP_POLICY_TYPE = "TimestampPolicy";
    public static final String SIGNATURE_CONFIRMATION_POLICY_TYPE = "SignatureConfirmationPolicy";

    public static final String USERNAMETOKEN_TYPE = "UsernameTokenBinding";
    public static final String X509CERTIFICATE_TYPE = "X509CertificateBinding";
    public static final String SAMLASSERTION_TYPE = "SAMLAssertionBinding";
    public static final String SYMMETRIC_KEY_TYPE = "SymmetricKeyBinding";

    public static final String PRIVATEKEY_BINDING_TYPE = "PrivateKeyBinding";
    public static final String ENCRYPTION_POLICY_FEATUREBINDING_TYPE = "EncryptionPolicy.FeatureBinding";
    public static final String SIGNATURE_POLICY_FEATUREBINDING_TYPE = "SignaturePolicy.FeatureBinding";

    public static final String DERIVED_TOKEN_KEY_BINDING = "DerivedTokenKeyBinding";
    public static final String ISSUED_TOKEN_KEY_BINDING = "IssuedTokenKeyBinding";
    public static final String SECURE_CONVERSATION_TOKEN_KEY_BINDING = "SecureConversationTokenKeyBinding";
    
    public static boolean isPrimaryPolicy(WSSPolicy policy) {
        if (policy == null) return false;

        if (signaturePolicy(policy) || encryptionPolicy(policy))
            return true;

        return false;
    }

    public static boolean isSecondaryPolicy(WSSPolicy policy) {
        if (policy == null) return false;
        if (authenticationTokenPolicy(policy) || timestampPolicy(policy))
            return true;

        return false;
    }

    public static boolean signaturePolicyFeatureBinding(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == SIGNATURE_POLICY_FEATUREBINDING_TYPE);
    }

    public static boolean encryptionPolicyFeatureBinding(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == ENCRYPTION_POLICY_FEATUREBINDING_TYPE);
    }

    public static boolean privateKeyBinding(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() ==  PRIVATEKEY_BINDING_TYPE);
    }

    public static boolean encryptionPolicy(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == ENCRYPTION_POLICY_TYPE);
    }

    public static boolean signaturePolicy(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == SIGNATURE_POLICY_TYPE);
    }

    public static boolean timestampPolicy(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == TIMESTAMP_POLICY_TYPE);
    }
    
    public static boolean signatureConfirmationPolicy(SecurityPolicy policy){
        if(policy == null) return false;
        return (policy.getType() == SIGNATURE_CONFIRMATION_POLICY_TYPE);
    }

    public static boolean authenticationTokenPolicy(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == AUTH_POLICY_TYPE);
    }

    public static boolean usernameTokenPolicy(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == USERNAMETOKEN_TYPE);
    }

    public static boolean x509CertificateBinding(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == X509CERTIFICATE_TYPE);
    }

    public static boolean samlTokenPolicy(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == SAMLASSERTION_TYPE);
    }

    public static boolean symmetricKeyBinding(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == SYMMETRIC_KEY_TYPE);
    }
   
    public static boolean booleanComposerPolicy(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == BOOLEAN_COMPOSER_TYPE);
    }

    public static boolean dynamicSecurityPolicy(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == DYN_SEC_POLICY_TYPE);
    }
   
    public static boolean messagePolicy(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == MESSAGEPOLICY_CONFIG_TYPE);
    }

    public static boolean applicationSecurityConfiguration(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == APP_SEC_CONFIG_TYPE);
    }

    public static boolean declarativeSecurityConfiguration(SecurityPolicy policy) {
        if (policy == null) return false;
        return (policy.getType() == DECL_SEC_CONFIG_TYPE);
    }
    
    public static boolean derivedTokenKeyBinding(SecurityPolicy policy) {
        if ( policy == null ) return false;
        return ( policy.getType() == DERIVED_TOKEN_KEY_BINDING);
    }

    public String getTIMESTAMP_POLICY_TYPE() {
        return TIMESTAMP_POLICY_TYPE;
    }
    
    public static boolean issuedTokenKeyBinding(SecurityPolicy policy) {
        if ( policy == null) return false;
        return ( policy.getType() == ISSUED_TOKEN_KEY_BINDING);
    }

    public static boolean secureConversationTokenKeyBinding(
        SecurityPolicy policy) {
        if ( policy == null) return false;
        return ( policy.getType() == SECURE_CONVERSATION_TOKEN_KEY_BINDING);
    }

}
