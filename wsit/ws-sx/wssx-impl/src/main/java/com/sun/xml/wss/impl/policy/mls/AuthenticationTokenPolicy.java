/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: AuthenticationTokenPolicy.java,v 1.2 2010-10-21 15:37:33 snajper Exp $
 */

package com.sun.xml.wss.impl.policy.mls;

//import com.sun.xml.wss.saml.internal.impl.AssertionImpl;
import com.sun.xml.ws.security.opt.impl.tokens.UsernameToken;
import java.security.cert.X509Certificate;
//import com.sun.xml.wss.saml.AuthorityBinding;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.MessageConstants;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Element;

/**
 *  Objects of this class  represent a concrete WSS  Authentication
 *  token as FeatureBinding.  The following WSS Authentication Tokens
 *  are supported :
 *   <UL>
 *   <LI>A <code> UsernameToken</code>
 *   <LI>A <code>X509Certificate</code>
 *   <LI>A <code>SAMLAssertion</code>
 *  </UL>
 *
 */
public class AuthenticationTokenPolicy extends WSSFeatureBindingExtension {
    
    /**
     * Feature Bindings
     *
     * (1) UsernameTokenBinding
     * (2) X509CertificateBinding
     * (3) SAMLAssertionBinding
     *
     * Key Bindings
     */
    /**
     * Default Constructor
     */
    public AuthenticationTokenPolicy() {
        setPolicyIdentifier(PolicyTypeUtil.AUTH_POLICY_TYPE);
    }
    
    /**
     * Equals operator
     * @param policy <code>WSSPolicy</code> to be compared for equality
     * @return true if the policy is equal to this policy
     */
    public boolean equals(WSSPolicy policy) {
        boolean _assert = false;
        
        try {
            if (!PolicyTypeUtil.authenticationTokenPolicy(policy)) {
                return false;
            }
            AuthenticationTokenPolicy aPolicy = (AuthenticationTokenPolicy) policy;
            _assert = ((WSSPolicy) getFeatureBinding()).equals((WSSPolicy) aPolicy.getFeatureBinding());
        } catch (Exception cce) {
        }
        
        return _assert;
    }
    
    /*
     * Equality comparision ignoring the Targets
     * @param policy the policy to be compared for equality
     * @return true if the argument policy is equal to this
     */
    public boolean equalsIgnoreTargets(WSSPolicy policy) {
        return equals(policy);
    }
    
    /**
     * Clone operator
     * @return a clone of this AuthenticationTokenPolicy
     */
    public Object clone() {
        AuthenticationTokenPolicy atPolicy = new AuthenticationTokenPolicy();
        
        try {
            WSSPolicy fBinding = (WSSPolicy) getFeatureBinding();
            WSSPolicy kBinding = (WSSPolicy) getKeyBinding();
            
            if (fBinding != null) {
                atPolicy.setFeatureBinding((MLSPolicy) fBinding.clone());
            }
            if (kBinding != null) {
                atPolicy.setKeyBinding((MLSPolicy) kBinding.clone());
            }
        } catch (Exception e) {
        }
        
        return atPolicy;
    }
    
    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.AUTH_POLICY_TYPE;
    }
    
    /**
     * A policy representing a WSS UsernameToken. An instance of
     * this class can be used as concrete feature binding for an
     * AuthenticationTokenPolicy.
     * Different parameters in this policy are applicable depending
     * upon whether this policy is used to construct a wss:UsernameToken
     * (sender side policy) or it is used to verify an incoming UsernameToken
     * (receiver side policy).  Information on applicability will be indicated
     * where appropriate.
     */
    public static class UsernameTokenBinding extends KeyBindingBase {
        
        /**
         * Feature Bindings
         *
         * (1) TimestampPolicy
         *
         * Key Bindings
         */
        String nonce = MessageConstants._EMPTY;
        String username = MessageConstants._EMPTY;
        String password = MessageConstants._EMPTY;
        String _referenceType = MessageConstants._EMPTY;
        UsernameToken usernametoken=null;
        String _valueType = MessageConstants._EMPTY;
        // setting this to false for PlugFest/Policy
        boolean useNonce = false;
        // setting this to false for PlugFest/Policy
        boolean doDigest = false;
        boolean noPasswd = false;
        long maxNonceAge = 0;
        private String strId=null;
        String _keyAlgorithm = MessageConstants._EMPTY;
        private byte[] sKey = null;
        private SecretKey secretKey = null;
        private SecretKey Key = null;
        private boolean endorsing;
        boolean useCreated = false;
        
        /**
         * Default Constructor
         */
        public UsernameTokenBinding() {
            setPolicyIdentifier(PolicyTypeUtil.USERNAMETOKEN_TYPE);
        }
        
        /**
         * Constructor
         *
         * @param username username to be sent
         * @param password password to be sent
         * @param nonce nonce
         * @param doDigest if password should be digested
         * @param creationTime timestamp
         */
        public UsernameTokenBinding(String username, String password, String nonce, boolean doDigest, String creationTime) {
            this();
            
            this.username = username;
            this.password = password;
            this.nonce = nonce;
            this.doDigest = doDigest;
        }

        public String getReferenceType() {
            return this._referenceType;
            //throw new UnsupportedOperationException("Not yet implemented");
        }
        
        public UsernameToken getUsernameToken() {
            return this.usernametoken;
        }

        public void isEndorsing(boolean flag) {
            this.endorsing = flag;
        }
        public boolean isEndorsing() {
            return this.endorsing;
        }
        
        public void setUsernameToken(UsernameToken token) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set UsernameToken : Policy is ReadOnly");
            }            
            this.usernametoken = token;
        }
        
        public void setReferenceType(String referenceType) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set ReferenceType of UsernameToken : Policy is ReadOnly");
            }            
            this._referenceType = referenceType;
        }
        /**
         * Create and set the FeatureBinding for this WSSPolicy to a TimestampPolicy     * @return a new TimestampPolicy as a FeatureBinding for this WSSPolicy
         * @exception PolicyGenerationException, if TimestampPolicy is not a valid FeatureBinding for this WSSPolicy
         * @see SignaturePolicy
         * @see EncryptionPolicy
         * @see AuthenticationTokenPolicy
         */
        public MLSPolicy newTimestampFeatureBinding() throws PolicyGenerationException {
            if (isReadOnly()) {
                throw new RuntimeException("Can not create a feature binding of Timestamp type for ReadOnly " + _policyIdentifier);
            }
            
            if (!(_policyIdentifier == PolicyTypeUtil.USERNAMETOKEN_TYPE) && !(_policyIdentifier == PolicyTypeUtil.SIGNATURE_POLICY_FEATUREBINDING_TYPE)) {
                throw new PolicyGenerationException("Can not create a feature binding of Timestamp type for " + _policyIdentifier);
            }
            this._featureBinding = new TimestampPolicy();
            return _featureBinding;
        }
        
        /**
         * set the username
         * @param username
         */
        public void setUsername(String username) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set Username : Policy is ReadOnly");
            }
            this.username = username;
        }
        
        /**
         * set the password
         * @param password
         */
        public void setPassword(String password) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set Password : Policy is ReadOnly");
            }
            this.password = password;
        }
        
        /**
         * set the nonce
         * @param nonce
         */
        public void setNonce(String nonce) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set Nonce : Policy is ReadOnly");
            }
            
            this.nonce = nonce;
        }
        
        /**
         * setter for a boolean flag indicating whether a nonce should be
         * while constructing a wss:UsernameToken  from this Policy
         * @param useNonce
         */
        public void setUseNonce(boolean useNonce) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set useNonce flag : Policy is ReadOnly");
            }
            
            this.useNonce = useNonce;
        }
        
        public void setUseCreated(boolean useCreated) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set useCreated flag : Policy is ReadOnly");
            }
            
            this.useCreated = useCreated;
        }
        
        /**
         * setter for a boolean flag indicating whether the password should be
         * digested while constructing a wss:UsernameToken  from this Policy
         * @param doDigest
         */
        public void setDigestOn(boolean doDigest) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set digest flag : Policy is ReadOnly");
            }
            
            this.doDigest = doDigest;
        }
        
        /**
         * set the maximum age in Milliseconds for which a receiving entity should
         * cache the nonce associated with this policy. A receiver may
         * cache received nonces for this period (or more) to minimize nonce-replay attacks
         * This parameter is applicable when this UsernameToken is used as a Receiver requirement.
         * @param nonceAge
         */
        public void setMaxNonceAge(long nonceAge) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set maxNonceAge flag : Policy is ReadOnly");
            }
            
            this.maxNonceAge = nonceAge;
        }
        
        /**
         * get the username
         * @return username
         */
        public String getUsername() {
            return this.username;
        }
        
        /**
         * get the password
         * @return password
         */
        public String getPassword() {
            return this.password;
        }
        
        /**
         * get the nonce
         * @return nonce
         */
        public String getNonce() {
            return this.nonce;
        }
        
        /**
         * get the useNonce flag
         * @return true if the useNonce flag is set to true
         */
        public boolean getUseNonce() {
            return this.useNonce;
        }
        
        public boolean getUseCreated() {
            return this.useCreated;
        }
        
        /**
         * @return if password is digested
         */
        public boolean getDigestOn() {
            return this.doDigest;
        }
        
        /**
         * @return the maxNonceAge
         */
        public long getMaxNonceAge() {
            return this.maxNonceAge;
        }
        
        public boolean hasNoPassword() {
            return noPasswd;
        }
        
        public void setNoPassword(boolean value) {
            this.noPasswd = value;
        }
        
        public void setSTRID(String id) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set STRID attribute : Policy is ReadOnly");
            }

            this.strId = id;
        }

        public String getSTRID() {
            return this.strId;
        }

        public void setValueType(String valueType) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set valueType of usernameToken : " + "Policy is ReadOnly");
            }
            this._valueType = valueType;
        }

        public void setKeyAlgorithm(String keyAlgorithm) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set KeyAlgorithm : Policy is ReadOnly");
            }
            this._keyAlgorithm = keyAlgorithm;
        }
        /**
         * @return the keyAlgorithm
         */
        public String getKeyAlgorithm() {
            return _keyAlgorithm;
        }

        public void setSecretKey(SecretKey secretKey) {
            this.secretKey = secretKey;
        }

        public void setSecretKey(byte[] secretKey) {
            sKey = secretKey;
        }

        public SecretKey getSecretKey(String algorithm) {
            if (Key == null) {
                Key = new SecretKeySpec(sKey, algorithm);
            }
            return Key;
        }

        public SecretKey getSecretKey() {
            return secretKey;
        }
        /**
         * Equals operator
         * @return true if the binding is equal to this UsernameToken Policy
         */
        public boolean equals(WSSPolicy policy) {
            boolean assrt = false;
            
            try {
                if (!PolicyTypeUtil.usernameTokenPolicy(policy)) {
                    return false;
                }
                UsernameTokenBinding utBinding = (UsernameTokenBinding) policy;
                assrt = (useNonce == utBinding.getUseNonce() && doDigest == utBinding.getDigestOn() &&
                        useCreated == utBinding.getUseCreated());
            } catch (Exception e) {
            }
            
            return assrt;
        }
        
        /*
         * Equality comparision ignoring the Targets
         * @param policy the policy to be compared for equality
         * @return true if the argument policy is equal to this
         */
        public boolean equalsIgnoreTargets(WSSPolicy policy) {
            return equals(policy);
        }
        
        /**
         *@return a clone of this policy
         */
        public Object clone() {
            UsernameTokenBinding utBinding = new UsernameTokenBinding();
           try {
            utBinding.setUsername(username);
            utBinding.setPassword(password);
            utBinding.setNonce(nonce);
            utBinding.setUseNonce(useNonce);
            utBinding.setUseCreated(useCreated);
            utBinding.setReferenceType(_referenceType);
            utBinding.setDigestOn(doDigest);
            utBinding.setUsernameToken(usernametoken);
            utBinding.setUUID(UUID);
            WSSPolicy kBinding = (WSSPolicy) this.getKeyBinding();

            if (kBinding != null) {
                utBinding.setKeyBinding((MLSPolicy) kBinding.clone());
            }
            utBinding.isEndorsing(this.endorsing);
            //utBinding.setPolicyToken(this.getPolicyToken());
            utBinding.setIncludeToken(this.getIncludeToken());
            utBinding.setPolicyTokenFlag(this.policyTokenWasSet());
            utBinding.isOptional(_isOptional);
           } catch (Exception e){
               e.printStackTrace();
           }
            return utBinding;
        }
        
        /**
         * @return the type of the policy
         */
        public String getType() {
            return PolicyTypeUtil.USERNAMETOKEN_TYPE;
        }
        
        public String toString() {
            return PolicyTypeUtil.USERNAMETOKEN_TYPE + "::" + getUsername();
        }
    }
    
    /**
     * A policy representing a WSS X509Certificate. An instance of
     * this class can be used as concrete feature binding for an
     * AuthenticationTokenPolicy.
     */
    public static class X509CertificateBinding extends KeyBindingBase {
        
        /**
         * Feature Bindings
         *
         * Key Bindings
         *
         * (1) PrivateKeyBinding
         */
        String _valueType = MessageConstants._EMPTY;
        String _encodingType = MessageConstants._EMPTY;
        String _referenceType = MessageConstants._EMPTY;
        //X509CRL _crl                   = null;
        //CertPath _certPath             = null;
        X509Certificate _certificate = null;
        String _keyAlgorithm = MessageConstants._EMPTY;
        String _certificateIdentifier = "";
        String strId = null;
        
        /**
         * Default Constructor
         */
        public X509CertificateBinding() {
            setPolicyIdentifier(PolicyTypeUtil.X509CERTIFICATE_TYPE);
        }
        
        /**
         * @param certificateIdentifier X509Certificate identifiers like alias
         * @param keyAlgorithm Key algorithm to be used
         */
        public X509CertificateBinding(String certificateIdentifier, String keyAlgorithm) {
            this();
            this._certificateIdentifier = certificateIdentifier;
            this._keyAlgorithm = keyAlgorithm;
        }
        
        /**
         * Create and set the KeyBinding for this WSSPolicy to a PrivateKeyBinding
         * @return a new PrivateKeyBinding as a KeyBinding for this WSSPolicy
         */
        public MLSPolicy newPrivateKeyBinding() {
            if (isReadOnly()) {
                throw new RuntimeException("Can not create PrivateKeyBinding : Policy is Readonly");
            }
            
            this._keyBinding = new PrivateKeyBinding();
            return _keyBinding;
        }
        
        /**
         * set the ValueType
         * @param valueType Token type like X509v3, X509PKIPathv1, PKCS7
         */
        public void setValueType(String valueType) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set ValueType of X509Certificate : Policy is ReadOnly");
            }
            
            this._valueType = valueType;
        }

        /**
         * set the EncodingType
         * @param encodingType encoding type like base64
         */
        public void setEncodingType(String encodingType) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set EncodingType of X509Certificate : Policy is ReadOnly");
            }
            
            this._encodingType = encodingType;
        }
        
        /**
         * set the ReferenceType
         * @param referenceType KeyIdentifier, Direct etc.,.
         */
        public void setReferenceType(String referenceType) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set ReferenceType of X509Certificate : Policy is ReadOnly");
            }
            
            this._referenceType = referenceType;
        }
        
        /**
         * set the Certificate Identifier
         * @param certificateIdentifier alias, key identifier etc.,.
         */
        public void setCertificateIdentifier(String certificateIdentifier) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set X509Certificate Identifier : Policy is ReadOnly");
            }
            
            this._certificateIdentifier = certificateIdentifier;
        }
        
        /**
         * set the Certificate
         * @param certificate X509Certificate
         */
        public void setX509Certificate(X509Certificate certificate) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set X509Certificate : Policy is ReadOnly");
            }
            
            this._certificate = certificate;
        }
        
        /**
         * @return valueType
         */
        public String getValueType() {
            return this._valueType;
        }
        
        /**
         * @return encodingType
         */
        public String getEncodingType() {
            return this._encodingType;
        }
        
        /**
         * @return referenceType
         */
        public String getReferenceType() {
            return this._referenceType;
        }
        
        /**
         * @return certificateIdentifier
         */
        public String getCertificateIdentifier() {
            return this._certificateIdentifier;
        }
        
        /**
         * @return X509Certificate
         */
        public X509Certificate getX509Certificate() {
            return this._certificate;
        }
        
        /**
         * @param keyAlgorithm the keyAlgorithm
         */
        public void setKeyAlgorithm(String keyAlgorithm) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set KeyAlgorithm : Policy is ReadOnly");
            }
            
            this._keyAlgorithm = keyAlgorithm;
        }
        
        /**
         * @return the keyAlgorithm
         */
        public String getKeyAlgorithm() {
            return _keyAlgorithm;
        }
        
        /*
         * @param id the wsu:id of the wsse:SecurityTokenReference to
         * be generated for this X509Certificate Token. Applicable while
         * sending a message (sender side policy)
         */
        public void setSTRID(String id) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set STRID attribute : Policy is ReadOnly");
            }
            
            this.strId = id;
        }
        
        /*
         * @return the wsu:id of the wsse:SecurityTokenReference to
         * be generated for this X509Certificate Token, if specified,
         * null otherwise.
         */
        public String getSTRID() {
            return this.strId;
        }
        
        /**
         * @param policy the policy to be compared for equality
         * @return true if the argument policy is equal to this
         */
        public boolean equals(WSSPolicy policy) {
            
            boolean assrt = false;
            
            try {
                if (!PolicyTypeUtil.x509CertificateBinding(policy)) {
                    return false;
                }
                X509CertificateBinding ctBinding = (X509CertificateBinding) policy;
                
                boolean b1 = _valueType.equals("") ? true : _valueType.equals(ctBinding.getValueType());
                if (!b1) {
                    return false;
                }
                boolean b2 = _encodingType.equals("") ? true : _encodingType.equals(ctBinding.getEncodingType());
                if (!b2) {
                    return false;
                }
                boolean b3 = _referenceType.equals("") ? true : _referenceType.equals(ctBinding.getReferenceType());
                if (!b3) {
                    return false;
                }
                boolean b4 = _keyAlgorithm.equals("") ? true : _keyAlgorithm.equals(ctBinding.getKeyAlgorithm());
                if (!b4) {
                    return false;
                }
                if (strId == null && ctBinding.getSTRID() == null) {
                    return true;
                }
                
                if (strId != null && strId.equals(ctBinding.getSTRID())) {
                    return true;
                }
            } catch (Exception e) {
            }
            return false;
        }
        
        /*
         * Equality comparision ignoring the Targets
         * @param policy the policy to be compared for equality
         * @return true if the argument policy is equal to this
         */
        public boolean equalsIgnoreTargets(WSSPolicy policy) {
            return equals(policy);
        }
        
        /**
         * Clone operator
         * @return clone of this policy
         */
        public Object clone() {
            X509CertificateBinding x509Binding = new X509CertificateBinding();
            
            try {
                x509Binding.setValueType(_valueType);
                x509Binding.setEncodingType(_encodingType);
                x509Binding.setReferenceType(_referenceType);
                x509Binding.setKeyAlgorithm(_keyAlgorithm);
                x509Binding.setCertificateIdentifier(_certificateIdentifier);
                x509Binding.setX509Certificate(_certificate);
                x509Binding.setUUID(UUID);
                x509Binding.setSTRID(this.strId);
                
                WSSPolicy kBinding = (WSSPolicy) this.getKeyBinding();
                
                if (kBinding != null) {
                    x509Binding.setKeyBinding((MLSPolicy) kBinding.clone());
                }
                //x509Binding.setPolicyToken(this.getPolicyToken());
                x509Binding.setIncludeToken(this.getIncludeToken());
                x509Binding.setPolicyTokenFlag(this.policyTokenWasSet());
                x509Binding.isOptional(_isOptional);
            } catch (Exception e) {
            }
            
            return x509Binding;
        }
        
        /**
         * @return the type of the policy
         */
        public String getType() {
            return PolicyTypeUtil.X509CERTIFICATE_TYPE;
        }
        
        public String toString() {
            return PolicyTypeUtil.X509CERTIFICATE_TYPE + "::" + getCertificateIdentifier() + "::" + strId + "::" + _referenceType;
        }
    }
    
    /**
     * A policy representing Kerberos Token. An instance of
     * this class can be used as concrete feature binding for an
     * AuthenticationTokenPolicy
     */
    public static class KerberosTokenBinding extends KeyBindingBase {
        
        String _valueType = MessageConstants._EMPTY;
        String _encodingType = MessageConstants._EMPTY;
        String _referenceType = MessageConstants._EMPTY;
        byte[] _token = null;
        private String strId;
        String _keyAlgorithm = MessageConstants._EMPTY;
        SecretKey _secretKey = null;
        
        /**
         * Default constructor
         */
        public KerberosTokenBinding() {
            setPolicyIdentifier(PolicyTypeUtil.KERBEROS_BST_TYPE);
        }
        
        /**
         * set the ValueType
         * @param valueType attribute like Kerberosv5_AP_REQ
         */
        public void setValueType(String valueType) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set valueType of KerberosToken : " + "Policy is ReadOnly");
            }
            this._valueType = valueType;
        }
        
        /**
         * set the ReferenceType
         * @param referenceType allowed values are Direct and KeyIdentifier
         */
        public void setReferenceType(String referenceType){
            if (isReadOnly()) {
                throw new RuntimeException("Can not set referenceType of KerberosToken : " + "Policy is ReadOnly");
            }
            this._referenceType = referenceType;
        }
        
        public void setEncodingType(String encodingType) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set EncodingType of KerberosToken : " + "Policy is ReadOnly");
            }
            this._encodingType = encodingType;
        }
        
        public void setTokenValue(byte[] token) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set TokenValue of KerberosToken : " + "Policy is ReadOnly");
            }
            this._token = token;
        }
        
        /*
         * @param id the wsu:id of the wsse:SecurityTokenReference to
         * be generated for this Kerberos Token. Applicable while
         * sending a message (sender side policy)
         */
        public void setSTRID(String id) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set STRID attribute : Policy is ReadOnly");
            }
            
            this.strId = id;
        }
        
        /*
         * @return the wsu:id of the wsse:SecurityTokenReference to
         * be generated for this Kerberos Token, if specified,
         * null otherwise.
         */
        public String getSTRID() {
            return this.strId;
        }
        
        /**
         * @return valueType
         */
        public String getValueType() {
            return this._valueType;
        }
        
        /**
         * @return referenceType
         */
        public String getReferenceType(){
            return this._referenceType;
        }
        
        /**
         * @return encodingType
         */
        public String getEncodingType() {
            return this._encodingType;
        }
        
        /**
         * @return Token Value
         */
        public byte[] getTokenValue() {
            return this._token;
        }
        
        /**
         * @param keyAlgorithm the keyAlgorithm
         */
        public void setKeyAlgorithm(String keyAlgorithm) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set KeyAlgorithm : Policy is ReadOnly");
            }
            
            this._keyAlgorithm = keyAlgorithm;
        }
        
        /**
         * @return the keyAlgorithm
         */
        public String getKeyAlgorithm() {
            return _keyAlgorithm;
        }
        
        /**
         * Set the symmetric key
         * @param secretKey the SecretKey
         */
        public void setSecretKey(SecretKey secretKey) {
            this._secretKey = secretKey;
        }
        
        /**
         * @return SecretKey the symmetric key
         */
        public SecretKey getSecretKey() {
            return this._secretKey;
        }
        
        /**
         * Clone operator
         * @return clone of this policy
         */
        public Object clone() {
            KerberosTokenBinding ktBinding = new KerberosTokenBinding();
            
            try {
                ktBinding.setValueType(_valueType);
                ktBinding.setEncodingType(_encodingType);
                ktBinding.setTokenValue(_token);
                ktBinding.setKeyAlgorithm(_keyAlgorithm);
                ktBinding.setUUID(UUID);
                
                SecretKeySpec ky0 = (SecretKeySpec) _secretKey;
                if (ky0 != null) {
                    SecretKeySpec key = new SecretKeySpec(ky0.getEncoded(), ky0.getAlgorithm());
                    ktBinding.setSecretKey(key);
                }
                
                WSSPolicy kBinding = (WSSPolicy) this.getKeyBinding();
                if (kBinding != null) {
                    ktBinding.setKeyBinding((MLSPolicy) kBinding.clone());
                }
                ktBinding.setIncludeToken(this.getIncludeToken());
                ktBinding.setPolicyTokenFlag(this.policyTokenWasSet());
            } catch (Exception e) {
            }
            
            return ktBinding;
        }
        
        /**
         * @param policy to be compared for equality
         * @return true if the argument policy is equal to this
         */
        public boolean equals(WSSPolicy policy) {
            boolean assrt = false;
            
            try {
                if (!PolicyTypeUtil.kerberosTokenBinding(policy)) {
                    return false;
                }
                KerberosTokenBinding ktBinding = (KerberosTokenBinding) policy;
                
                boolean b1 = _valueType.equals("") ? true : _valueType.equals(ktBinding.getValueType());
                if (!b1) {
                    return false;
                }
                boolean b2 = _encodingType.equals("") ? true : _encodingType.equals(ktBinding.getEncodingType());
                if (!b2) {
                    return false;
                }
                boolean b3 = _keyAlgorithm.equals("") ? true : _keyAlgorithm.equals(ktBinding.getKeyAlgorithm());
                if (!b3) {
                    return false;
                }
                
                return true;
            } catch (Exception e) {
            }
            return false;
        }
        
        /*
         * Equality comparision ignoring the Targets
         * @param policy the policy to be compared for equality
         * @return true if the argument policy is equal to this
         */
        public boolean equalsIgnoreTargets(WSSPolicy policy) {
            return equals(policy);
        }
        
        /**
         * @return the type of the policy
         */
        public String getType() {
            return PolicyTypeUtil.KERBEROS_BST_TYPE;
        }
        
        /**
         * override the method from KeyBindingBase as we will support IncludeToken=Once
         * in Kerberos token profile
         * @param include the value of IncludeToken parameter
         */
        public void setIncludeToken(String include) {
            
            if(INCLUDE_ALWAYS.equals(include) || INCLUDE_ALWAYS_TO_RECIPIENT.equals(include)){
                throw new UnsupportedOperationException("IncludeToken policy " + include +
                        " is not supported for Kerberos Tokens, Consider using Once");
            }
            
            this.includeToken = include;
            policyToken = true;
        }
    }
    
    /**
     * A policy representing a SAML Assertion. An instance of
     * this class can be used as concrete feature binding for an
     * AuthenticationTokenPolicy.
     */
    public static class SAMLAssertionBinding extends KeyBindingBase implements LazyKeyBinding {
        
        /**
         * Feature Bindings
         * Key Bindings
         */
        String _type = "";
        String _keyAlgorithm = "";
        String _keyIdentifier = "";
        String _referenceType = "";
        String _authorityIdentifier = "";
        String strId = null;
        String assertionId = null;
        String samlVersion = null;
        public static final String V10_ASSERTION = "SAML10Assertion";
        public static final String V11_ASSERTION = "SAML11Assertion";
        public static final String V20_ASSERTION = "SAML20Assertion";
        /**
         * Sender-Vouches Subject ConfirmationMethod
         */
        public static final String SV_ASSERTION = "SV";
        /**
         * Holder-Of-Key Subject ConfirmationMethod
         */
        public static final String HOK_ASSERTION = "HOK";
        Element _assertion = null;
        Element _authorityBinding = null;
        XMLStreamReader samlAssertion = null;
        
        /**
         * Default constructor
         */
        public SAMLAssertionBinding() {
            setPolicyIdentifier(PolicyTypeUtil.SAMLASSERTION_TYPE);
        }
        
        /**
         * Constructor
         * @param type the SubjectConfirmation type of the SAML assertion, one of SV, HOK
         * @param keyIdentifier an abstract identifier for the Confirmation Key
         * @param authorityIdentifier an abstract identifier for the issuing authority
         * @param referenceType the reference type for references to the SAML Assertion,
         *     should be one of  KeyIdentifier, Embedded reference type as defined by
         *     WSS SAML Token profile 1.0.
         */
        public SAMLAssertionBinding(String type, String keyIdentifier, String authorityIdentifier, String referenceType) {
            this();
            this._type = type;
            this._keyIdentifier = keyIdentifier;
            this._authorityIdentifier = authorityIdentifier;
            this._referenceType = referenceType;
        }
        
        /**
         * set the SubjectConfirmation type of the SAML assertion
         * @param type the SubjectConfirmation type of the SAML assertion, one of SV, HOK
         */
        public void setAssertionType(String type) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set SAMLAssertionType : Policy is ReadOnly");
            }
            
            if (SV_ASSERTION.equals(type)) {
                this._type = SV_ASSERTION;
            } else if (HOK_ASSERTION.equals(type)) {
                this._type = HOK_ASSERTION;
            } else {
                //throw error
            }
        }
        
        public void setSAMLVersion(String ver) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set SAMLAssertionType : Policy is ReadOnly");
            }
            
            this.samlVersion = ver;
        }
        
        public String getSAMLVersion() {
            return samlVersion;
        }
        
        /**
         * Create and set the KeyBinding for this WSSPolicy to a PrivateKeyBinding
         * @return a new PrivateKeyBinding as a KeyBinding for this WSSPolicy
         */
        public MLSPolicy newPrivateKeyBinding() {
            if (isReadOnly()) {
                throw new RuntimeException("Can not create PrivateKeyBinding : Policy is Readonly");
            }
            
            this._keyBinding = new PrivateKeyBinding();
            return _keyBinding;
        }
        
        /**
         * set the abstract identifier for the Confirmation Key
         * @param ki the abstract identifier for the Confirmation Key
         */
        public void setKeyIdentifier(String ki) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set SAML KeyIdentifier : Policy is ReadOnly");
            }
            
            this._keyIdentifier = ki;
        }
        
        /**
         * set the abstract identifier for the issuing authority
         * @param uri the URI of the Assertion Issuer
         */
        public void setAuthorityIdentifier(String uri) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set SAML AuthorityIdentifier : Policy is ReadOnly");
            }
            
            this._authorityIdentifier = uri;
        }
        
        /**
         * set the ReferenceType to be used for references to the SAML Assertion
         * @param rtype reference type (one of KeyIdentifier, Embedded)
         */
        public void setReferenceType(String rtype) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set SAML ReferenceType : Policy is ReadOnly");
            }
            
            this._referenceType = rtype;
        }
        
        /**
         * set the SAML AuthorityBinding element, identifying a remote assertion
         * @param authorityBinding
         */
        public void setAuthorityBinding(Element authorityBinding) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set SAML AuthorityBinding : Policy is ReadOnly");
            }
            
            this._authorityBinding = authorityBinding;
        }
        
        /**
         * set the SAML Assertion
         * @param assertion the SAML Assertion
         */
        public void setAssertion(Element assertion) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set SAML Assertion : Policy is ReadOnly");
            }
            
            this._assertion = assertion;
        }
        
        public void setAssertion(XMLStreamReader reader) {
            this.samlAssertion = reader;
        }
        
        /**
         * set the keyAlgorithm to be used
         * @param algorithm the keyAlgorithm to be used
         */
        public void setKeyAlgorithm(String algorithm) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set KeyAlgorithm : Policy is ReadOnly");
            }
            
            this._keyAlgorithm = algorithm;
        }
        
        /**
         * @return key algorithm
         */
        public String getKeyAlgorithm() {
            return this._keyAlgorithm;
        }
        
        /**
         * @return reference type
         */
        public String getReferenceType() {
            return this._referenceType;
        }
        
        /**
         * @return type of SAMLAssertion (SV/HOK)
         */
        public String getAssertionType() {
            return this._type;
        }
        
        /**
         * @return identifier to key bound to the Assertion
         */
        public String getKeyIdentifier() {
            return this._keyIdentifier;
        }
        
        /**
         * @return identifier to Authority issueing the Assertion
         */
        public String getAuthorityIdentifier() {
            return this._authorityIdentifier;
        }
        
        /**
         * @return authority binding component of the assertion
         */
        public Element getAuthorityBinding() {
            return this._authorityBinding;
        }
        
        /**
         * @return SAML assertion
         */
        public Element getAssertion() {
            return this._assertion;
        }
        
        public XMLStreamReader getAssertionReader() {
            return this.samlAssertion;
        }
        
        /**
         * equals operator
         * @param policy the policy to be compared for equality
         * @return true if the argument policy is equal to this
         */
        public boolean equals(WSSPolicy policy) {
            
            try {
                if (!PolicyTypeUtil.samlTokenPolicy(policy)) {
                    return false;
                }
                
                SAMLAssertionBinding sBinding = (SAMLAssertionBinding) policy;
                
                // this kind of equals is still incorrect
                boolean b1 = _type.equals("") ? true : _type.equals(sBinding.getAssertionType());
                if (!b1) {
                    return false;
                }
                boolean b2 = _authorityIdentifier.equals("") ? true : _authorityIdentifier.equals(sBinding.getAuthorityIdentifier());
                if (!b2) {
                    return false;
                }
                boolean b3 = _referenceType.equals("") ? true : _referenceType.equals(sBinding.getReferenceType());
                if (!b3) {
                    return false;
                }
                boolean b6 = _keyAlgorithm.equals("") ? true : _keyAlgorithm.equals(sBinding.getKeyAlgorithm());
                if (!b6) {
                    return false;
                }
                boolean b7 = (strId == null) ? true : strId.equals(sBinding.getSTRID());
                if (!b7) {
                    return false;
                }
                boolean b8 = (assertionId == null) ? true : assertionId.equals(sBinding.getAssertionId());
                if (!b8) {
                    return false;
                }
            } catch (Exception e) {
            }
            
            return true;
        }
        
        /*
         * Equality comparision ignoring the Targets
         * @param binding the policy to be compared for equality
         * @return true if the argument binding is equal to this
         */
        public boolean equalsIgnoreTargets(WSSPolicy binding) {
            return equals(binding);
        }
        
        /**
         *@return clone of this SAML Policy
         */
        public Object clone() {
            SAMLAssertionBinding samlBinding = new SAMLAssertionBinding();
            
            try {
                samlBinding.setAssertionType(_type);
                samlBinding.setKeyAlgorithm(_keyAlgorithm);
                samlBinding.setKeyIdentifier(_keyIdentifier);
                samlBinding.setReferenceType(_referenceType);
                samlBinding.setAuthorityIdentifier(_authorityIdentifier);
                samlBinding.setAssertion(_assertion);
                samlBinding.setAssertion(this.samlAssertion);
                samlBinding.setAuthorityBinding(_authorityBinding);
                samlBinding.setSTRID(this.strId);
                samlBinding.setAssertionId(this.assertionId);
                //samlBinding.setPolicyToken(this.getPolicyToken());
                samlBinding.setIncludeToken(this.getIncludeToken());
                samlBinding.setPolicyTokenFlag(this.policyTokenWasSet());
                samlBinding.isOptional(_isOptional);
            } catch (Exception e) {
            }
            
            return samlBinding;
        }
        
        /**
         * @return the type of the policy
         */
        public String getType() {
            return PolicyTypeUtil.SAMLASSERTION_TYPE;
        }
        
        /*
         * @param id the wsu:id of the wsse:SecurityTokenReference to
         * be generated for this X509Certificate Token. Applicable while
         * sending a message (sender side policy)
         */
        public void setSTRID(String id) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set SAML STRID : Policy is ReadOnly");
            }
            
            this.strId = id;
        }
        
        /*
         * @return the wsu:id of the wsse:SecurityTokenReference to
         * be generated for this X509Certificate Token, if specified,
         * null otherwise.
         */
        public String getSTRID() {
            return this.strId;
        }
        
        /**
         * set the AssertionId for the possibly remote assertion
         * A CallbackHandler can choose to just set the
         * AuthorityBinding and the AssertionId, and not set
         * the actual assertion
         * @param id the Assertion Id of the possibly remote SAML Assertion
         */
        public void setAssertionId(String id) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set SAML AssertionID : Policy is ReadOnly");
            }
            
            this.assertionId = id;
        }
        
        /**
         * A CallbackHandler can choose to just set the
         * AuthorityBinding and the AssertionId, and not set
         * the actual assertion
         * @return the Assertion ID of the SAML Assertion represented by this Policy
         */
        public String getAssertionId() {
            return this.assertionId;
        }
        
        public String toString() {
            return PolicyTypeUtil.SAMLASSERTION_TYPE + "::" + getReferenceType() + "::" + this._type;
        }
        
        public Element get_assertion() {
            return _assertion;
        }

        public String getRealId() {
            return assertionId;
        }

        public void setRealId(String realId) {
           // do nothing, real id is assertion id
        }
    }
    
    /**
     * A policy representing a RSAKeyPair. An instance of
     * this class can be used as concrete feature binding for an
     * AuthenticationTokenPolicy.
     */
    public static class KeyValueTokenBinding extends KeyBindingBase {
        
        /**
         * Feature Bindings
         *
         * Key Bindings
         *
         * (1) PrivateKeyBinding
         */
        String _valueType = MessageConstants._EMPTY;
        String _encodingType = MessageConstants._EMPTY;
        String _referenceType = MessageConstants._EMPTY;

        /**
         * Default Constructor
         */
        public KeyValueTokenBinding() {
            setPolicyIdentifier(PolicyTypeUtil.RSATOKEN_TYPE);
        }                
        
        /**
         * Create and set the KeyBinding for this WSSPolicy to a PrivateKeyBinding
         * @return a new PrivateKeyBinding as a KeyBinding for this WSSPolicy
         */
        public MLSPolicy newPrivateKeyBinding() {
            if (isReadOnly()) {
                throw new RuntimeException("Can not create PrivateKeyBinding : Policy is Readonly");
            }
            
            this._keyBinding = new PrivateKeyBinding();
            return _keyBinding;
        }
        
        /**
         * set the ValueType
         * @param valueType Token type like X509v3, X509PKIPathv1, PKCS7
         */
        public void setValueType(String valueType) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set ValueType of X509Certificate : Policy is ReadOnly");
            }
            
            this._valueType = valueType;
        }
        
        /**
         * set the EncodingType
         * @param encodingType encoding type like base64
         */
        public void setEncodingType(String encodingType) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set EncodingType of X509Certificate : Policy is ReadOnly");
            }
            
            this._encodingType = encodingType;
        }
        
        /**
         * set the ReferenceType
         * @param referenceType KeyIdentifier, Direct etc.,.
         */
        public void setReferenceType(String referenceType) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set ReferenceType of X509Certificate : Policy is ReadOnly");
            }
            
            this._referenceType = referenceType;
        }
                
        /**
         * @return valueType
         */
        public String getValueType() {
            return this._valueType;
        }
        
        /**
         * @return encodingType
         */
        public String getEncodingType() {
            return this._encodingType;
        }
        
        /**
         * @return referenceType
         */
        public String getReferenceType() {
            return this._referenceType;
        }

        /**
         * @param policy the policy to be compared for equality
         * @return true if the argument policy is equal to this
         */
        public boolean equals(WSSPolicy policy) {
            
            boolean assrt = false;
            
            try {
                if (!PolicyTypeUtil.keyValueTokenBinding(policy)) {
                    return false;
                }
                KeyValueTokenBinding rsaTokenBinding = (KeyValueTokenBinding) policy;
                
                boolean b1 = _valueType.equals("") ? true : _valueType.equals(rsaTokenBinding.getValueType());
                if (!b1) {
                    return false;
                }
                boolean b2 = _encodingType.equals("") ? true : _encodingType.equals(rsaTokenBinding.getEncodingType());
                if (!b2) {
                    return false;
                }
                boolean b3 = _referenceType.equals("") ? true : _referenceType.equals(rsaTokenBinding.getReferenceType());
                if (!b3) {
                    return false;
                }
            } catch (Exception e) {
            }
            return false;
        }
        
        /*
         * Equality comparision ignoring the Targets
         * @param policy the policy to be compared for equality
         * @return true if the argument policy is equal to this
         */
        public boolean equalsIgnoreTargets(WSSPolicy policy) {
            return equals(policy);
        }
        
        /**
         * Clone operator
         * @return clone of this policy
         */
        public Object clone() {
            KeyValueTokenBinding rsaTokenBinding = new KeyValueTokenBinding();
            
            try {
                rsaTokenBinding.setValueType(_valueType);
                rsaTokenBinding.setEncodingType(_encodingType);
                rsaTokenBinding.setReferenceType(_referenceType);
                rsaTokenBinding.setUUID(UUID);
                
                WSSPolicy kBinding = (WSSPolicy) this.getKeyBinding();
                
                if (kBinding != null) {
                    rsaTokenBinding.setKeyBinding((MLSPolicy) kBinding.clone());
                }
                //x509Binding.setPolicyToken(this.getPolicyToken());
                rsaTokenBinding.setIncludeToken(this.getIncludeToken());
                rsaTokenBinding.setPolicyTokenFlag(this.policyTokenWasSet());
            } catch (Exception e) {
            }
            
            return rsaTokenBinding;
        }
        
        /**
         * @return the type of the policy
         */
        public String getType() {
            //return PolicyTypeUtil.RSATOKEN_TYPE;
            return getPolicyIdentifier();
        }
        
        public String toString() {
            //return PolicyTypeUtil.RSATOKEN_TYPE + "::" + _referenceType;
            return getPolicyIdentifier() + "::" + _referenceType;
        }
    }
    
}
