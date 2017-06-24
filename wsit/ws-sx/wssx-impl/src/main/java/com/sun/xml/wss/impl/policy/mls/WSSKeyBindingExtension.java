/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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
