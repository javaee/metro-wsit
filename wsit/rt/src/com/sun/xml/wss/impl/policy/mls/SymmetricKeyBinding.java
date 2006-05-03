/*
 * $Id: SymmetricKeyBinding.java,v 1.1 2006-05-03 22:57:56 arungupta Exp $
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

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.MessageConstants;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.sun.xml.wss.impl.PolicyTypeUtil;

/**
 * A policy representing a SymmetricKey that can be used as the
 * KeyBinding for a SignaturePolicy or an EncryptionPolicy.
 */
public class SymmetricKeyBinding extends KeyBindingBase {
    
    /*
     * Feature Binding
     * Key Binding
     */
    
    String _keyAlgorithm  = MessageConstants._EMPTY;
    
    String _keyIdentifier = MessageConstants._EMPTY;
    
    String _certAlias = MessageConstants._EMPTY;
    
    boolean _useReceivedSecret = false;
    
    SecretKey _secretKey  = null;
    
    /**
     * Default constructor
     */
    public SymmetricKeyBinding() {
        setPolicyIdentifier(PolicyTypeUtil.SYMMETRIC_KEY_TYPE);
    }
    
    /**
     * @param keyIdentifier identifier for Key
     * @param keyAlgorithm Key Algorithm
     */
    public SymmetricKeyBinding(String keyIdentifier, String keyAlgorithm) {
        this();
        
        this._keyIdentifier = keyIdentifier;
        this._keyAlgorithm = keyAlgorithm;
    }
    
    /**
     * set the key identifier for the symmetric key
     * @param keyIdentifier
     */
    public void setKeyIdentifier(String keyIdentifier) {
        this._keyIdentifier = keyIdentifier;
    }
    
    /**
     * @return key identifier for the symmetric key
     */
    public String getKeyIdentifier() {
        return this._keyIdentifier;
    }
    
    public void setCertAlias(String certAlias) {
        this._certAlias = certAlias;
    }
    
    public String getCertAlias() {
        return this._certAlias;
    }
    
    public void setUseReceivedSecret(boolean useReceivedSecret) {
        this._useReceivedSecret = useReceivedSecret;
    }
    
    public boolean getUseReceivedSecret() {
        return this._useReceivedSecret;
    }
    
    /**
     * set the Key Algorithm of the Symmetric Key
     * @param keyAlgorithm
     */
    public void setKeyAlgorithm(String keyAlgorithm) {
        this._keyAlgorithm = keyAlgorithm;
    }
    
    /**
     * @return keyAlgorithm for the Symmetric Key
     */
    public String getKeyAlgorithm() {
        return this._keyAlgorithm;
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
     * Create and set the KeyBinding for this WSSPolicy to an X509CertificateBinding
     * @return a new X509CertificateBinding as a KeyBinding for this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy newX509CertificateKeyBinding () {
        if ( isReadOnly () ) {
            throw new RuntimeException ("Can not create X509CertificateKeyBinding : Policy is Readonly");
        }
        this._keyBinding = new AuthenticationTokenPolicy.X509CertificateBinding ();
        return _keyBinding;
    }

    
    /**
     * @param policy the policy to be compared for equality
     * @return true if the argument policy is equal to this
     */
    public boolean equals(WSSPolicy policy) {
        
        boolean assrt = false;
        
        try {
            SymmetricKeyBinding skBinding = (SymmetricKeyBinding) policy;
            
            boolean b1 = _keyIdentifier.equals("") ? true : _keyIdentifier.equals(skBinding.getKeyIdentifier());
            
            boolean b2 = _keyAlgorithm.equals("") ? true : _keyAlgorithm.equals(skBinding.getKeyAlgorithm());
            
            boolean b3 = _certAlias.equals("") ? true : _certAlias.equals(skBinding.getCertAlias());
            
            boolean b4 = (_useReceivedSecret == false) ? true : (_useReceivedSecret == skBinding.getUseReceivedSecret());
            boolean b5 = (this._keyBinding.equals(policy._keyBinding));
            
            assrt = b1 && b2 && b3 && b4 && b5;
        } catch (Exception e) {}
        
        return assrt;
    }
    
    /*
     * Equality comparision ignoring the Targets
     * @param policy the policy to be compared for equality
     * @return true if the argument policy is equal to this
     */
    public boolean equalsIgnoreTargets(WSSPolicy binding) {
        return equals(binding);
    }
    
    /**
     * Clone operator
     * @return clone of this policy
     */
    public Object clone(){
        SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
        
        try {
            skBinding.setKeyIdentifier(_keyIdentifier);
            skBinding.setKeyAlgorithm(_keyAlgorithm);
            skBinding.setCertAlias(_certAlias);
            skBinding.setUseReceivedSecret(_useReceivedSecret);
            
            SecretKeySpec ky0 = (SecretKeySpec) _secretKey;
            if (ky0 != null) {
                SecretKeySpec key = new SecretKeySpec(ky0.getEncoded(), ky0.getAlgorithm());
                skBinding.setSecretKey(key);
            }

            if (this._keyBinding != null) {
                if (this._keyBinding instanceof AuthenticationTokenPolicy.X509CertificateBinding) {
                    skBinding.setKeyBinding((AuthenticationTokenPolicy.X509CertificateBinding)
                        ((AuthenticationTokenPolicy.X509CertificateBinding)this._keyBinding).clone());
                }
            }

        } catch (Exception e) {
            // log
        }
        
        return skBinding;
    }
    
    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.SYMMETRIC_KEY_TYPE;
    }
    
    public String toString(){
        return PolicyTypeUtil.SYMMETRIC_KEY_TYPE+"::"+getKeyAlgorithm()+"::"+_keyIdentifier;
    }    
}

