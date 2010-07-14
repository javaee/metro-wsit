/*
 * $Id: PrivateKeyBinding.java,v 1.3.2.2 2010-07-14 14:07:09 m_potociar Exp $
 */

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

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.wss.impl.MessageConstants;
import java.security.PrivateKey;
import java.security.KeyFactory;

import com.sun.xml.wss.impl.PolicyTypeUtil;

/**
 * Objects of this class act as KeyBindings for AuthenticationTokens such
 * as AuthenticationTokenPolicy.X509CertificateBinding and  
 * AuthenticationTokenPolicy.SAMLAssertionBinding. When associated with an
 * AuthenticationToken they represent the PrivateKey associated with the
 * AuthenticationToken.
 */
public class PrivateKeyBinding extends WSSPolicy {
    
    /*
     * Feature Bindings
     * Key Bindings
     */
    
    /* this keyalgorithm is not used by our impl */
    String _keyAlgorithm   = MessageConstants._EMPTY;
    String _keyIdentifier  = MessageConstants._EMPTY;
    
    PrivateKey _privateKey = null;
    
    /**
     * Default constructor
     */
    public PrivateKeyBinding() {
        setPolicyIdentifier(PolicyTypeUtil.PRIVATEKEY_BINDING_TYPE);
    }
    
    /**
     * Constructor
     * @param keyIdentifier identifier for the Private Key
     * @param keyAlgorithm  identified for the Key Algorithm
     */
    public PrivateKeyBinding(String keyIdentifier, String keyAlgorithm) {
        this();
        
        this._keyIdentifier = keyIdentifier;
        this._keyAlgorithm = keyAlgorithm;
    }
    
    /**
     * set the keyIdentifier for the Private Key
     * @param keyIdentifier Key Identifier for the Private Key
     */
    public void setKeyIdentifier(String keyIdentifier) {
        this._keyIdentifier = keyIdentifier;
    }
    
    /**
     * @return key identifier for the Private Key
     */
    public String getKeyIdentifier() {
        return this._keyIdentifier;
    }
    
    /**
     * set the KeyAlgorithm of this Private Key.
     *
     * Implementation Note: This KeyAlgorithm is not used by XWS-Runtime,
     * refer setKeyAlgorithm on X509CertificateBinding, SAMLAssertionBinding,
     * and SymmetricKeyBinding instead.
     * @param keyAlgorithm  KeyAlgorithm of this Private Key
     */
    public void setKeyAlgorithm(String keyAlgorithm) {
        this._keyAlgorithm = keyAlgorithm;
    }
    
    /**
     * @return KeyAlgorithm of this Private Key
     */
    public String getKeyAlgorithm() {
        return this._keyAlgorithm;
    }
    
    /**
     * set the private key instance
     * @param privateKey PrivateKey for this PrivateKeyBinding
     */
    public void setPrivateKey(PrivateKey privateKey) {
        this._privateKey = privateKey;
    }
    
    /**
     * @return PrivateKey associated with this PrivateKeyBinding
     */
    public PrivateKey getPrivateKey() {
        return this._privateKey;
    }
    
    /**
     * equality operator
     * @param binding the Policy to be checked for equality
     * @return true if the argument binding is equal to this PrivateKeyBinding.
     */
    public boolean equals(WSSPolicy binding) {
        
        try {
            if (!PolicyTypeUtil.privateKeyBinding(binding))
                return false;

            PrivateKeyBinding policy = (PrivateKeyBinding) binding;
            
            boolean b1 = _keyIdentifier.equals("") ? true : _keyIdentifier.equals(policy.getKeyIdentifier());
            if (!b1) return false;            
            boolean b2 = _keyAlgorithm.equals("") ? true : _keyAlgorithm.equals(policy.getKeyAlgorithm());
            if (!b2) return false;            
        } catch (Exception e) {}
        
        return true;
    }
    
    /*
     * equality operator ignoring Target bindings
     */
    public boolean equalsIgnoreTargets(WSSPolicy binding) {
        return equals(binding);
    }
    
    /**
     * clone operator
     * @return a clone of this PrivateKeyBinding
     */
    public Object clone(){
        PrivateKeyBinding pkBinding = new PrivateKeyBinding();
        
        try {
            pkBinding.setKeyAlgorithm(_keyAlgorithm);
            pkBinding.setKeyIdentifier(_keyIdentifier);
            
            KeyFactory factory = KeyFactory.getInstance(_privateKey.getAlgorithm());
            pkBinding.setPrivateKey((PrivateKey)factory.translateKey(_privateKey));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return pkBinding;
    }
    
    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.PRIVATEKEY_BINDING_TYPE;
    }
    
    public String toString(){
        return PolicyTypeUtil.PRIVATEKEY_BINDING_TYPE+"::"+getKeyAlgorithm()+"::"+_keyIdentifier;
    }
}

