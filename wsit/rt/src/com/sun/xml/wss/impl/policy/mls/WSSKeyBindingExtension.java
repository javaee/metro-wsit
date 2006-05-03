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

/*
 * WSSKeyBindingExtension.java
 *
 * Created on August 31, 2005, 7:35 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.wss.impl.policy.MLSPolicy;

/**
 *
 * @author abhijit.das@Sun.COM
 */
public abstract class WSSKeyBindingExtension extends WSSPolicy {
    
    /** Creates a new instance of WSSKeyBindingExtension */
    public WSSKeyBindingExtension() {
    }
    
    
    /**
     * Create and set the KeyBinding for this WSSPolicy to an X509CertificateBinding
     * @return a new X509CertificateBinding as a KeyBinding for this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy newX509CertificateKeyBinding() {
        if ( isReadOnly() ) {
            throw new RuntimeException("Can not create X509CertificateKeyBinding : Policy is Readonly");
        }
        this._keyBinding = new AuthenticationTokenPolicy.X509CertificateBinding();
        return _keyBinding;
    }
    
    /**
     * Create and set the KeyBinding for this WSSPolicy to a SAMLAssertionBinding
     * @return a new SAMLAssertionBinding as a KeyBinding for this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy newSAMLAssertionKeyBinding() {
        if ( isReadOnly() ) {
            throw new RuntimeException("Can not create SAMLAssertionKeyBinding : Policy is Readonly");
        }
        
        this._keyBinding = new AuthenticationTokenPolicy.SAMLAssertionBinding();
        return _keyBinding;
    }
    
    /**
     * Create and set the KeyBinding for this WSSPolicy to a SymmetricKeyBinding
     * @return a new SymmetricKeyBinding as a KeyBinding for this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy newSymmetricKeyBinding() {
        if ( isReadOnly() ) {
            throw new RuntimeException("Can not create SymmetricKeyBinding : Policy is Readonly");
        }
        
        this._keyBinding = new SymmetricKeyBinding();
        return _keyBinding;
    }
    
    
    /**
     * Create and set the KeyBinding for this WSSPolicy to a DerivedTokenKeyBinding
     * @return a new DerivedTokenKeyBinding as a KeyBinding for this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy newDerivedTokenKeyBinding() {
        if ( isReadOnly() ) {
            throw new RuntimeException("Can not create DerivedTokenKeyBinding : Policy is Readonly");
        }
        
        this._keyBinding = new DerivedTokenKeyBinding();
        return _keyBinding;
    }
    
    
    /**
     * Create and set the KeyBinding for this WSSPolicy to a IssuedTokenKeyBinding
     * @return a new IssuedTokenKeyBinding as a KeyBinding for this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy newIssuedTokenKeyBinding() {
        if ( isReadOnly() ) {
            throw new RuntimeException("Can not create IssuedTokenKeyBinding : Policy is Readonly");
        }
        
        this._keyBinding = new IssuedTokenKeyBinding();
        return _keyBinding;
    }
    
    /**
     * Create and set the KeyBinding for this WSSPolicy to a IssuedTokenKeyBinding
     * @return a new IssuedTokenKeyBinding as a KeyBinding for this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy newSecureConversationTokenKeyBinding() {
        if ( isReadOnly() ) {
            throw new RuntimeException("Can not create SecureConversationKeyBinding : Policy is Readonly");
        }
        
        this._keyBinding = new SecureConversationTokenKeyBinding();
        return _keyBinding;
    }
    
    public MLSPolicy newUsernameTokenBindingKeyBinding(){
        if ( isReadOnly() ) {
            throw new RuntimeException("Can not create SAMLAssertionKeyBinding : Policy is Readonly");
        }
        this._keyBinding = new AuthenticationTokenPolicy.UsernameTokenBinding();
        return _keyBinding;
    }
}
