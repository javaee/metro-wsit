/*
 * $Id: SignaturePolicy.java,v 1.3 2010-03-20 12:32:28 kumarjayanti Exp $
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
import java.util.Iterator;
import java.util.ArrayList;

import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.PolicyTypeUtil;

import javax.xml.crypto.dsig.DigestMethod;

/**
 *  Objects of this class  represent a policy for Signing parts of a SOAP Message. The
 *  Message Parts to be signed and the Canonicalization Algorithm to be used for the ds:SignedInfo are
 *  captured as FeatureBindings of this Policy. The exact Key to be used is to be represented
 *  as a distinct KeyBinding for this policy instance.
 *  The SignatureMethod for the signature is obtained as the keyAlgorithm
 *  on the corresponding KeyBinding associated with this SignaturePolicy
 *
 *  Allowed KeyBindings for a SignaturePolicy include the following :
 * <UL>
 *   <LI>AuthenticationTokenPolicy.X509CertificateBinding
 *   <LI>AuthenticationTokenPolicy.SAMLAssertionBinding
 *   <LI>SymmetricKeyBinding
 * </UL>
 */
public class SignaturePolicy extends WSSKeyBindingExtension {
    
    /*
     * Feature Bindings
     *
     * (1) SignaturePolicy.FeatureBinding
     *
     * Key Bindings
     *
     * (1) X509KeyBinding
     * (2) SymmetricKeyBinding
     * (3) SAMLAssertionBinding
     */
    
    /**
     *Default constructor
     */
    public SignaturePolicy() {
        setPolicyIdentifier(PolicyTypeUtil.SIGNATURE_POLICY_TYPE);
        this._featureBinding = new FeatureBinding();
    }
    
    /**
     * clone operator
     * @return a clone of this SignaturePolicy
     */
    public Object clone() {
        SignaturePolicy policy = new SignaturePolicy();
        
        try {
            WSSPolicy fBinding = (WSSPolicy) getFeatureBinding();
            WSSPolicy kBinding = (WSSPolicy) getKeyBinding();
            
            if (fBinding != null)
                policy.setFeatureBinding((MLSPolicy)fBinding.clone());
            
            if (kBinding != null)
                policy.setKeyBinding((MLSPolicy)kBinding.clone());
        } catch (Exception e) {}
        
        return policy;
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
            //TODO :: Uncomment;
            /*SignaturePolicy sPolicy = (SignaturePolicy) policy;
             
            _assert = ((WSSPolicy) getFeatureBinding()).equals(
            (WSSPolicy) sPolicy.getFeatureBinding()) &&
            getKeyBinding().equals((WSSPolicy) sPolicy.getKeyBinding());*/
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
            if(!(PolicyTypeUtil.signaturePolicy(policy))) return false;
            SignaturePolicy sPolicy = (SignaturePolicy) policy;
            _assert = ((WSSPolicy) getFeatureBinding()).equalsIgnoreTargets(
                    (WSSPolicy) sPolicy.getFeatureBinding()) ;
            //TODO : Un comment later;
            //&&   getKeyBinding().equals((WSSPolicy) sPolicy.getKeyBinding());
        } catch (Exception cce) {}
        
        return _assert;
    }
    
    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.SIGNATURE_POLICY_TYPE;
    }
    
    /**
     * A class representing FeatureBindings for a SignaturePolicy
     * The FeatureBinding would contain information about the MessageParts
     * to be Signed, and the CanonicalizationMethod.
     * The SignatureMethod for the signature is obtained as the keyAlgorithm
     * on the corresponding KeyBinding associated with this SignaturePolicy
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
         * (1) X509KeyBinding
         * (2) SymmetricKeyBinding
         * (3) SAMLAssertionBinding
         */
        
        String _canonicalizationAlgorithm = "";
        
        private SignatureTarget timestamp = null;
        
        final ArrayList _targets = new ArrayList();
        
        private boolean isEndorsingSignature = false;
        
        private boolean isPrimarySignature = false;

        private boolean disableInclusivePrefix = false;
        
        /**
         * Default constructor
         */
        public FeatureBinding() {
            setPolicyIdentifier(PolicyTypeUtil.SIGNATURE_POLICY_FEATUREBINDING_TYPE);
        }
        
        /**
         * Constructor
         * @param canonicalization algorithm
         */
        public FeatureBinding(String canonicalization) {
            this();
            
            this._canonicalizationAlgorithm = canonicalization;
        }
        
        /**
         * @return Canonicalization Algorithm for the ds:SignedInfo
         */
        public String getCanonicalizationAlgorithm() {
            return _canonicalizationAlgorithm;
        }
        
        /**
         * set the Canonicalization Algorithm for the ds:SignedInfo
         * @param canonicalization Canonicalization Algorithm
         */
        public void setCanonicalizationAlgorithm(String canonicalization) {
            if ( isReadOnly()) {
                throw new RuntimeException("Can not set CanonicalizationAlgorithm : Policy is ReadOnly");
            }
            
            if (isBSP() && 
                canonicalization != MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS) {
                throw new RuntimeException("Does not meet BSP requirement: 5404. C14n algorithm must be exc-c14n");
            }
            this._canonicalizationAlgorithm = canonicalization;
        }
        
        /*
         * @return true if a Timestamp Reference is to be included in the Signature
         */
        public boolean includeTimestamp() {
        return timestamp != null ? true: false;
        }
        
        /*
         * indicate whether to include a Timestamp Reference in the Signature
         * @param includeTimestamp
         */
        @SuppressWarnings("unchecked")
        public void includeTimestamp(boolean include) {
            if ( isReadOnly()) {
                throw new RuntimeException("Can not set includeTimestamp Flag : Policy is ReadOnly");
            }
            
            if (include) {
                if(timestamp == null) {
                    timestamp = new SignatureTarget();
                    timestamp.setType("qname");
                    timestamp.setValue(MessageConstants.TIMESTAMP_QNAME);
                    timestamp.setDigestAlgorithm(DigestMethod.SHA1);
                    _targets.add(0, timestamp);
                }
            } else {
                if (timestamp != null) {
                    int idx = _targets.indexOf(timestamp);
                    _targets.remove(idx);
                    timestamp = null;
                }
            }
        }
        
        public void isEndorsingSignature(boolean isEndorsingSignature){
            this.isEndorsingSignature = isEndorsingSignature;
        }
        
        public boolean isEndorsingSignature(){
            return this.isEndorsingSignature;
        }
        
        public void isPrimarySignature(boolean isPrimarySignature){
            this.isPrimarySignature = isPrimarySignature;
        }
        
        public boolean isPrimarySignature(){
            return this.isPrimarySignature;
        }

        public boolean getDisableInclusivePrefix(){
            return this.disableInclusivePrefix;
        }

        public void setDisbaleInclusivePrefix(boolean disableInclusivePrefix){
            this.disableInclusivePrefix = disableInclusivePrefix;
        }

        /**
         * @return collection of target bindings
         */
        public ArrayList getTargetBindings() {
            return _targets;
        }
        
        /**
         * Add target to the list of targets for this FeatureBinding
         * @param target SignatureTarget
         */
        @SuppressWarnings("unchecked")
        public void addTargetBinding(SignatureTarget target) {
            
            if ( isReadOnly()) {
                throw new RuntimeException("Can not add Target : Policy is ReadOnly");
            }            
            _targets.add(target);
        }
        
        /*
         * Add target to the list of targets for this FeatureBinding
         * @param target Target to be added
         */
        public void addTargetBinding(Target target) {            
            addTargetBinding(new SignatureTarget(target));
            //if ( isReadOnly()) {
            //    throw new RuntimeException("Can not add Target : Policy is ReadOnly");
            //}
            
            //_targets.add(new SignatureTarget(target));
        }
        
        /**
         * @param targets ArrayList of targets to be removed
         */
        @SuppressWarnings("unchecked")
        public void removeTargetBindings(ArrayList targets) {
            if ( isReadOnly()) {
                throw new RuntimeException("Can not remove Target : Policy is ReadOnly");
            }
            
            _targets.removeAll(targets);
        }
        
        /**
         * Equals operator
         * @param binding <code>WSSPolicy</code> to be compared for equality
         * @return true if the binding is equal to this policy
         */
        public boolean equals(WSSPolicy binding) {
            
            try {
                if (!PolicyTypeUtil.signaturePolicyFeatureBinding(binding))
                    return false;
                FeatureBinding policy = (FeatureBinding) binding;
                
                boolean b1 = _canonicalizationAlgorithm.equals("") ? true :
                    _canonicalizationAlgorithm.equals(policy.getCanonicalizationAlgorithm());
                if (!b1) return false;
                
                boolean b2 = _targets.equals(policy.getTargetBindings());
                if (!b2) return false;
                
            } catch (Exception e) {}
            
            return true;
        }
        
        /*
         * Equality comparision ignoring the Targets
         * @param binding the binding to be compared for equality
         * @return true if the argument binding is equal to this
         */
        public boolean equalsIgnoreTargets(WSSPolicy binding) {
            
            boolean assrt = false;
           
            if (!PolicyTypeUtil.signaturePolicyFeatureBinding(binding)){
                return false;
            }
           
            try {
                FeatureBinding policy = (FeatureBinding) binding;
                assrt = _canonicalizationAlgorithm.equals(policy.getCanonicalizationAlgorithm());
            } catch (Exception e) {}
            
            return assrt;
        }
        
        /**
         * @return a clone of this SignaturePolicy.FeatureBinding
         */
        public Object clone() {
            FeatureBinding binding = new FeatureBinding();
            
            try {
                WSSPolicy kBinding = (WSSPolicy) getKeyBinding();
                WSSPolicy fBinding = (WSSPolicy) getFeatureBinding();
                
                if (fBinding != null)
                    binding.setFeatureBinding((MLSPolicy) fBinding.clone());
                
                if (kBinding != null)
                    binding.setKeyBinding((MLSPolicy) kBinding.clone());
                
                binding.setCanonicalizationAlgorithm(getCanonicalizationAlgorithm());
                
                Iterator i = getTargetBindings().iterator();
                while (i.hasNext()) {
                    SignatureTarget target = (SignatureTarget) i.next();
                    binding.addTargetBinding((SignatureTarget)target.clone());
                }
            } catch (Exception e) {}
            
            return binding;
        }
        
        /**
         * @return the type of the policy
         */
        public String getType() {
            return PolicyTypeUtil.SIGNATURE_POLICY_FEATUREBINDING_TYPE;
        }
        
    }
}

