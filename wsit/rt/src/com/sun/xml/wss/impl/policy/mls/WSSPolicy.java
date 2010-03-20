/*
 * $Id: WSSPolicy.java,v 1.3 2010-03-20 12:32:25 kumarjayanti Exp $
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

import com.sun.xml.wss.impl.policy.MLSPolicy;


/**
 * Represents a base class for SOAP Message Security Policies.
 * Any WSSPolicy can be epxressed as being composed of one or both of
 * two SecurityPolicy components called FeatureBinding and KeyBinding.
 * This generic structure for a WSSPolicy allows for representing complex,
 * concrete WSS Policy Instances.
 *
 * For example, A SignaturePolicy can have a SAMLAssertion as its KeyBinding.
 * The SAMLAssertionBinding can in turn have a KeyBinding which is a PrivateKeyBinding.
 * The PrivateKeyBinding would contain a PrivateKey corresponding to the PublicKey
 * contained in the SAML Assertion of the SAMLAssertionBinding. Such a SignaturePolicy
 * instance can then be used by the XWS-Runtime to sign Message parts of an outgoing
 * SOAP Message. The MessageParts to be signed are inturn identified by the FeatureBinding
 * component of the SignaturePolicy.
 *
 */
public abstract class WSSPolicy extends MLSPolicy implements Cloneable {
    protected String UUID;
    protected String _policyIdentifier;
    
    protected MLSPolicy _keyBinding= null;
    protected MLSPolicy _featureBinding= null;
    
    protected boolean _isOptional = false;
    
    protected boolean bsp = false;
    
    
    /**
     *Default constructor
     */
    public WSSPolicy () {}
    
    
    
    /**
     * @return MLSPolicy the FeatureBinding associated with this WSSPolicy, null otherwise
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy getFeatureBinding () {
        return _featureBinding;
    }
    
    /**
     * @return MLSPolicy the KeyBinding associated with this WSSPolicy, null otherwise
     *
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy getKeyBinding () {
        return _keyBinding;
    }
    
    /**
     * set the FeatureBinding for this WSSPolicy
     * @param policy the FeatureBinding to be set for this WSSPolicy
     */
    public void setFeatureBinding (MLSPolicy policy) {
        if ( isReadOnly () ) {
            throw new RuntimeException ("Can not set FeatureBinding : Policy is Readonly");
        }
        
        this._featureBinding = policy;
    }
    
    /**
     * set the KeyBinding for this WSSPolicy
     * @param policy the KeyBinding to be set for this WSSPolicy
     */
    public void setKeyBinding (MLSPolicy policy) {
        if ( isReadOnly () ) {
            throw new RuntimeException ("Can not set KeyBinding : Policy is Readonly");
        }
        
        this._keyBinding = policy;
    }
    
    /*
     *@param pi the policy identifier
     */
    public void setPolicyIdentifier (String pi) {
        if ( isReadOnly () ) {
            throw new RuntimeException ("Can not set PolicyIdentifier : Policy is Readonly");
        }
        
        this._policyIdentifier = pi;
    }
    
    /*
     *@return policy identifier
     */
    public String getPolicyIdentifier () {
        return _policyIdentifier;
    }
    
    /**
     *@return unique policy identifier associated with this policy
     */
    public String getUUID () {
        return UUID;
    }
    
    /**
     * set a unique policy identifier for this WSSPolicy
     * @param uuid
     */
    public void setUUID (String uuid) {
        if ( isReadOnly () ) {
            throw new RuntimeException ("Can not set UUID : Policy is Readonly");
        }
        
        this.UUID = uuid;
    }
    
    /*
     * @return true if-requirement-is-optional
     */
    public boolean isOptional () {
        return this._isOptional;
    }
    
    /*
     * @param isOptional parameter to indicate if this requirement is optional
     */
    public void isOptional (boolean isOptional) {
        if ( isReadOnly () ) {
            throw new RuntimeException ("Can not set Optional Requirement flag : Policy is Readonly");
        }
        
        this._isOptional = isOptional;
    }
    
    
    //TODO: we are not making any validity checks before creating KeyBindings.
    
    /**
     * clone operatror
     * @return a clone of this WSSPolicy
     *
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public abstract Object clone ();
    
    /**
     * equals operator
     *
     * @return true if the argument policy is the same as this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     * @see PrivateKeyBinding
     * @see SymmetricKeyBinding
     */
    public abstract boolean equals (WSSPolicy policy);
    
    /*
     * @return true if the argument policy is the same as this WSSPolicy ignoring Target bindings.
     *
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     * @see PrivateKeyBinding
     * @see SymmetricKeyBinding
     */
    public abstract boolean equalsIgnoreTargets (WSSPolicy policy);
    
    /*
     * Sets whether Basic Security Profile restrictions should be enforced as part
     * of this policy.
     */
    public void isBSP (boolean flag) {
        bsp = flag;
    }
    
    /*
     * @return true if BSP restrictions will be enforced.
     */
    public boolean isBSP () {
        return bsp;
    }
    
}
