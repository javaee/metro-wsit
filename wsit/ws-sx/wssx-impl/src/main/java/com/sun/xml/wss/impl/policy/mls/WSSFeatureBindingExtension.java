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
 * WSSFeatureBindingExtension.java
 *
 * Created on August 31, 2005, 7:11 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;

/**
 *
 * @author abhijit.das@Sun.COM
 */
public abstract class WSSFeatureBindingExtension extends WSSPolicy {
    
    /** Creates a new instance of WSSFeatureBindingExtension */
    public WSSFeatureBindingExtension() {
    }
    
    /**
     * Create and set the FeatureBinding for this WSSPolicy to a UsernameTokenBinding
     * @return a new UsernameTokenBinding as a FeatureBinding for this WSSPolicy
     * @exception PolicyGenerationException if UsernameTokenBinding is not a valid FeatureBinding for this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy newUsernameTokenFeatureBinding () throws PolicyGenerationException {
        if ( isReadOnly () ) {
            throw new RuntimeException (
                    "Can not create a feature binding of UsernameToken type for ReadOnly " + _policyIdentifier);
        }
        
        if ((PolicyTypeUtil.SIGNATURE_POLICY_TYPE == _policyIdentifier) ||
                (PolicyTypeUtil.ENCRYPTION_POLICY_TYPE == _policyIdentifier)) {
            throw new PolicyGenerationException (
                    "Can not create a feature binding of UsernameToken type for " + _policyIdentifier);
        }
        
        this._featureBinding = new AuthenticationTokenPolicy.UsernameTokenBinding ();
        return _featureBinding;
    }
    
    
    /**
     * Create and set the FeatureBinding for this WSSPolicy to an X509CertificateBinding
     * @return a new X509CertificateBinding as a FeatureBinding for this WSSPolicy
     * @exception PolicyGenerationException if X509CertificateBinding is not a valid FeatureBinding for this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy newX509CertificateFeatureBinding () throws PolicyGenerationException {
        if ( isReadOnly () ) {
            throw new RuntimeException (
                    "Can not create a feature binding of X509Certificate type for ReadOnly " + _policyIdentifier);
        }
        
        if ((PolicyTypeUtil.SIGNATURE_POLICY_TYPE == _policyIdentifier) ||
                (PolicyTypeUtil.ENCRYPTION_POLICY_TYPE == _policyIdentifier)) {
            throw new PolicyGenerationException (
                    "Can not create a feature binding of X509Certificate type for " + _policyIdentifier);
        }
        
        this._featureBinding = new AuthenticationTokenPolicy.X509CertificateBinding ();
        return _featureBinding;
    }
    
    /**
     * Create and set the FeatureBinding for this WSSPolicy to a SAMLAssertionBinding
     * @return a new SAMLAssertionBinding as a FeatureBinding for this WSSPolicy
     * @exception PolicyGenerationException if SAMLAssertionBinding is not a valid FeatureBinding for this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    public MLSPolicy newSAMLAssertionFeatureBinding () throws PolicyGenerationException {
        if ( isReadOnly () ) {
            throw new RuntimeException (
                    "Can not create a feature binding of SAMLAssertion type for ReadOnly " + _policyIdentifier);
        }
        
        if ((PolicyTypeUtil.SIGNATURE_POLICY_TYPE == _policyIdentifier) ||
                (PolicyTypeUtil.ENCRYPTION_POLICY_TYPE == _policyIdentifier)) {
            throw new PolicyGenerationException (
                    "Can not create a feature binding of SAMLAssertion type for " + _policyIdentifier);
        }
        
        this._featureBinding = new AuthenticationTokenPolicy.SAMLAssertionBinding ();
        return _featureBinding;
    }
    
    
    /**
     * Create and set the FeatureBinding for this WSSPolicy to a TimestampPolicy
     * @return a new TimestampPolicy as a FeatureBinding for this WSSPolicy
     * @exception PolicyGenerationException, if TimestampPolicy is not a valid FeatureBinding for this WSSPolicy
     * @see SignaturePolicy
     * @see EncryptionPolicy
     * @see AuthenticationTokenPolicy
     */
    /*public MLSPolicy newTimestampFeatureBinding () throws PolicyGenerationException {
        if ( isReadOnly () ) {
            throw new RuntimeException (
                    "Can not create a feature binding of Timestamp type for ReadOnly " + _policyIdentifier);
        }
        
        if (!(_policyIdentifier == PolicyTypeUtil.USERNAMETOKEN_TYPE) &&
                !(_policyIdentifier == PolicyTypeUtil.SIGNATURE_POLICY_FEATUREBINDING_TYPE))
            throw new PolicyGenerationException (
                    "Can not create a feature binding of Timestamp type for " + _policyIdentifier);
        
        this._featureBinding = new TimestampPolicy ();
        return _featureBinding;
    }*/
    
}
