/*
 * $Id: EncryptionPolicy.java,v 1.1 2010-10-05 11:41:31 m_potociar Exp $
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

import java.util.Iterator;
import java.util.ArrayList;

import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.PolicyTypeUtil;


/**
 *  Objects of this class  represent a policy for Encrypting parts of a SOAP Message. The
 *  Message Parts to be encrypted and the Data Encryption Algorithm to be used are
 *  captured as FeatureBindings of this Policy. The exact Key to be used is to be represented
 *  as a distinct KeyBinding for this policy instance.
 *
 *  Allowed KeyBindings for an EncryptionPolicy include the following :
 * <UL>
 *   <LI>AuthenticationTokenPolicy.X509CertificateBinding
 *   <LI>AuthenticationTokenPolicy.SAMLAssertionBinding
 *   <LI>SymmetricKeyBinding
 * </UL>
 */
public class EncryptionPolicy extends WSSKeyBindingExtension {
    
    /*
     * Feature Bindings
     *
     * (1) EncryptionPolicy.FeatureBinding
     *
     * Key Bindings
     *
     * (1) X509CertificateBinding
     * (2) SymmetricKeyBinding
     * (3) SAMLAssertionBinding
     */
    
    /**
     * default constructor
     */
    public EncryptionPolicy() {
        setPolicyIdentifier(PolicyTypeUtil.ENCRYPTION_POLICY_TYPE);
        this._featureBinding = new FeatureBinding();
    }
    
    /**
     * Equals operator
     * @param policy <code>WSSPolicy</code> to be compared for equality
     * @return true if the policy is equal to this policy
     */
    public boolean equals(WSSPolicy policy) {
        boolean _assert = false;
        
        try {
            return equalsIgnoreTargets(policy);
            /*EncryptionPolicy sPolicy = (EncryptionPolicy) policy;
             
            _assert = ((WSSPolicy) getFeatureBinding()).equals (
                         (WSSPolicy) sPolicy.getFeatureBinding()) &&
            getKeyBinding().equals ((WSSPolicy) sPolicy.getKeyBinding());
             */
        } catch (Exception cce) {}
        
        return _assert;
    }
    
    /*
     * Equality comparision ignoring the Targets
     * @param policy the policy to be compared for equality
     * @return true if the argument policy is equal to this
     */
    public boolean equalsIgnoreTargets(WSSPolicy policy) {
        boolean _assert = false;
        
        try {
            if(PolicyTypeUtil.encryptionPolicy(policy))
                return true;
            
            //EncryptionPolicy sPolicy = (EncryptionPolicy) policy;
            //TODO : Uncomment it
            //_assert = getKeyBinding().equals((WSSPolicy) sPolicy.getKeyBinding());
        } catch (Exception cce) {}
        
        return _assert;
    }
    
    /**
     * clone operator
     * @return a clone of this EncryptionPolicy
     */
    public Object clone() {
        EncryptionPolicy ePolicy = new EncryptionPolicy();
        
        try {
            WSSPolicy fBinding = (WSSPolicy) getFeatureBinding();
            WSSPolicy kBinding = (WSSPolicy) getKeyBinding();
            
            if (fBinding != null)
                ePolicy.setFeatureBinding((MLSPolicy)fBinding.clone());
            
            if (kBinding != null)
                ePolicy.setKeyBinding((MLSPolicy)kBinding.clone());
        } catch (Exception e) {}
        
        return ePolicy;
    }
    
    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.ENCRYPTION_POLICY_TYPE;
    }
    
    /**
     * A class representing FeatureBindings for an EncryptionPolicy
     * The FeatureBinding would contain information about the MessageParts
     * to be Encrypted, The data encryption algorithm to be used.
     */
    public static class FeatureBinding extends WSSPolicy {
        
        /*
         * Feature Bindings
         *
         * (1) SignaturePolicy
         * (2) EncryptionPolicy
         * (3) AuthenticationTokenPolicy
         *
         * Key Bindings
         *
         * (1) X509CertificateBinding
         * (2) SymmetricKeyBinding
         * (3) SAMLAssertionBinding
         */
        
        String _dataEncryptionAlgorithm = "";
        ArrayList _targets = new ArrayList();
        boolean standAloneRefList = false;
        boolean targetIsIssuedToken = false;
        boolean targetIsSignature = false;
        
        /**
         *default constructor
         */
        public FeatureBinding() {
            setPolicyIdentifier(PolicyTypeUtil.ENCRYPTION_POLICY_FEATUREBINDING_TYPE);
        }
        
        /**
         * @return the DataEncryptionAlgorithm
         */
        public String getDataEncryptionAlgorithm() {
            return _dataEncryptionAlgorithm;
        }
        
        /**
         * set the DataEncryptionAlgorithm to be used
         * @param algorithm the DataEncryptionAlgorithm
         */
        public void setDataEncryptionAlgorithm(String algorithm) {
            if ( isReadOnly() ) {
                throw new RuntimeException("Can not set DateEncryptionAlgorithm : Policy is ReadOnly");
            }
            this._dataEncryptionAlgorithm = algorithm;
        }
        
        /**
         * @return Target collection
         */
        public ArrayList getTargetBindings() {
            return _targets;
        }
        
        /**
         * @param target EncryptionTarget
         */
        @SuppressWarnings("unchecked")
        public void addTargetBinding(EncryptionTarget target) {
            if ( isReadOnly() ) {
                throw new RuntimeException("Can not add Target : Policy is ReadOnly");
            }
            _targets.add(target);
        }
        
        /*
         * @param target Target
         */
        @SuppressWarnings("unchecked")
        public void addTargetBinding(Target target) {
            if ( isReadOnly() ) {
                throw new RuntimeException("Can not add Target : Policy is ReadOnly");
            }
            _targets.add(new EncryptionTarget(target));
        }
        
        /**
         * @param targets ArrayList of all targets to be removed
         */
        @SuppressWarnings("unchecked")
        public void removeTargetBindings(ArrayList targets) {
            if ( isReadOnly() ) {
                throw new RuntimeException("Can not remove Target : Policy is ReadOnly");
            }
            _targets.removeAll(targets);
        }
        
        /**
         * Equals operator
         * @return true if the binding is equal to this Encryption Policy
         */
        public boolean equals(WSSPolicy policy) {
            
            try {
                FeatureBinding fBinding = (FeatureBinding) policy;
                boolean b1 = _targets.equals(fBinding.getTargetBindings());
                if (!b1) return false;
            } catch (Exception e) {}
            
            return true;
        }
        
       /*
        * Equality comparision ignoring the Targets
        * @param policy the policy to be compared for equality
        * @return true if the argument policy is equal to this
        */
        public boolean equalsIgnoreTargets(WSSPolicy policy) {
            return true;
        }
        
        /**
         * clone operator
         * @return a clone of this EncryptionPolicy.FeatureBinding
         */
        @SuppressWarnings("unchecked")
        public Object clone(){
            FeatureBinding fBinding = new FeatureBinding();
            
            try {
                ArrayList list = new ArrayList();
                
                Iterator i = getTargetBindings().iterator();
                while (i.hasNext()) list.add(((EncryptionTarget)i.next()).clone());
                
                ((ArrayList) fBinding.getTargetBindings()).addAll(list);
                
                WSSPolicy kBinding = (WSSPolicy)getKeyBinding();
                fBinding.setDataEncryptionAlgorithm(this.getDataEncryptionAlgorithm());
                if (kBinding != null)
                    fBinding.setKeyBinding((MLSPolicy)kBinding.clone());
            } catch (Exception e) {}
            
            fBinding.encryptsIssuedToken(this.encryptsIssuedToken());
            fBinding.encryptsSignature(this.encryptsSignature());
            return fBinding;
        }
        
        /**
         * @return the type of the policy
         */
        public String getType() {
            return PolicyTypeUtil.ENCRYPTION_POLICY_FEATUREBINDING_TYPE;
        }
        
        public boolean encryptsIssuedToken() {
            return targetIsIssuedToken;
        }
        
        public void encryptsIssuedToken(boolean flag) {
            targetIsIssuedToken = flag;
        }
        
        public boolean encryptsSignature() {
            return targetIsSignature;
        }
        public void encryptsSignature(boolean flag) {
            targetIsSignature = flag;
        }
        public boolean getUseStandAloneRefList(){
            return standAloneRefList;
        }
        
        public void setUseStandAloneRefList(boolean value){
            this.standAloneRefList = value;
        }
    }
}

