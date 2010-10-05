/*
 * $Id: SignatureTarget.java,v 1.1 2010-10-05 11:41:25 m_potociar Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.ArrayList;

import com.sun.xml.wss.impl.MessageConstants;

/**
 * Objects of this class represent a Signature Target that can be part of
 * the FeatureBinding for a SignaturePolicy (refer SignaturePolicy.FeatureBinding).
 */
public class SignatureTarget extends Target implements Cloneable {
    
    String _digestAlgorithm = "";
    
    ArrayList _transforms = new ArrayList();
   
    private boolean isOptimized = false;
    
    private String xpathVersion = "";
    private boolean isITNever = false;
            
    /**
     * Default constructor
     */
    public SignatureTarget() {}
    
    /**
     * Constructor that takes a Target
     */
    public SignatureTarget(Target target) {
        this.setEnforce(target.getEnforce());
        this.setType(target.getType());
        this.setValue(target.getValue());
        this.setContentOnly(target.getContentOnly());
        this._digestAlgorithm = MessageConstants.SHA1_DIGEST;
    }
    
    /**
     * @param digest Digest Algorithm to be used for this Target
     * @param transform Transform Algorithm to applied on this Target
     */
    @SuppressWarnings("unchecked")
    public SignatureTarget(String digest, String transform) {
        this._digestAlgorithm = digest;
        this._transforms.add(new Transform(transform));
    }
    
    /**
     * @return Digest Algorithm for this Target
     */
    public String getDigestAlgorithm() {
        return _digestAlgorithm;
    }
    
    /**
     * @return Collection of Transform Algorithms
     */
    public ArrayList getTransforms() {
        return _transforms;
    }
    /**
     * is the include token type Never?
     */
    public void isITNever(boolean iToken){
        this.isITNever = iToken;
    }
    /**
     * is the include token type Never?
     */
    public boolean isITNever(){
        return this.isITNever;
    }

    /**
     * set the Digest Algorithm to be used for this Target
     * @param digest Digest Algorithm
     */
    public void setDigestAlgorithm(String digest) {
        if (isBSP() && (digest.intern() != MessageConstants.SHA1_DIGEST)) {
            throw new RuntimeException("Does not meet BSP requirement 5420 for Digest Algorithm");
        }
        this._digestAlgorithm = digest;
    }
    
    /**
     * Add a Transform for this Target
     * @param transform Transform
     */
    @SuppressWarnings("unchecked")
    public void addTransform(Transform transform) {
        String transformStr = transform.getTransform();
        if (isBSP() && 
                ((transformStr != MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS) 
                && (transformStr != MessageConstants.ATTACHMENT_COMPLETE_TRANSFORM_URI)
                && (transformStr != MessageConstants.ATTACHMENT_CONTENT_ONLY_TRANSFORM_URI)
                && (transformStr != MessageConstants.SWA11_ATTACHMENT_CONTENT_SIGNATURE_TRANSFORM)
                && (transformStr != MessageConstants.SWA11_ATTACHMENT_COMPLETE_SIGNATURE_TRANSFORM)
                && (transformStr != MessageConstants.STR_TRANSFORM_URI)
                && (transformStr != MessageConstants.TRANSFORM_FILTER2))) {
            throw new RuntimeException("Does not meet BSP requirement 5423 for signature transforms");
        }
        this._transforms.add(transform);
    }
    
    /**
     * @return a new instance of Signatuer Transform
     */
    public Transform newSignatureTransform() {
        return new Transform();
    }
    
    /**
     * Equals operator
     * @param target SignatureTarget
     * @return true if the target argument is equal to this Target
     */
    public boolean equals(SignatureTarget target) {
        
        boolean b1 = _digestAlgorithm.equals("") ? true : _digestAlgorithm.equals(target.getDigestAlgorithm());
        
        boolean b2 = _transforms.equals(target.getTransforms());
        
        return b1 && b2;
    }
    
    /**
     * clone operator
     * @return a clone of this SignatureTarget
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        SignatureTarget target = new SignatureTarget();
        
        try {
            ArrayList list = target.getTransforms();
            
            target.setDigestAlgorithm(_digestAlgorithm);
            target.setValue(this.getValue());
            target.setType(this.getType());
            target.setContentOnly(this.getContentOnly());
            target.setEnforce(this.getEnforce());
            
            Iterator i = _transforms.iterator();
            while (i.hasNext()) {
                Transform transform = (Transform) i.next();
                list.add(transform.clone());
            }
        } catch (Exception e) {}
        
        return target;
    }
    
    /**
     * This class represents a Transform that can appear on a SignatureTarget.
     */
    public static class Transform implements Cloneable{
        
        String _transform = "";
        
        // List _canonicalizationParameters = new ArrayList();
        
        AlgorithmParameterSpec _algorithmParameters = null;

        private boolean disableInclusivePrefix = false;
        
        /**
         * Default constructor
         */
        public Transform() {}
        
        /**
         * Constructor
         * @param algorithm the Transform Algorithm
         */
        public Transform(String algorithm) {
            this._transform = algorithm;
        }
        
        
        /**
         * @return Algorithm Parameters for the Transform Algorithm
         */
        public AlgorithmParameterSpec getAlgorithmParameters() {
            return this._algorithmParameters;
        }
        
        /**
         * set Algorithm Parameters
         * @param param the list of parameters for the Transform Algorithm
         */
        public void setAlgorithmParameters(AlgorithmParameterSpec param){
            this._algorithmParameters= param;
        }
        
        /**
         * set the transform Algorithm
         * @param algorithm
         */
        public void setTransform(String algorithm) {
            this._transform = algorithm;
        }
        
        /**
         * @return the transform Algorithm
         */
        public String getTransform() {
            return this._transform;
        }

        public boolean getDisableInclusivePrefix(){
            return this.disableInclusivePrefix;
        }

        public void setDisbaleInclusivePrefix(boolean disableInclusivePrefix){
            this.disableInclusivePrefix = disableInclusivePrefix;
        }
        
        /**
         * equals operator
         * @param transform the transform to be compared for equality
         * @return true if the argument transform is equal to this transform
         */
        public boolean equals(Transform transform) {
            
            boolean b1 = _transform.equals("") ? true : _transform.equals(transform.getTransform());
            if (!b1) return false;
            
            boolean b2 = _algorithmParameters.equals(transform.getAlgorithmParameters());
            if (!b2) return false;
            
            return true;
        }
        
        /**
         * clone operator
         * @return a clone of this Transform
         */
        @Override
        public Object clone() {
            Transform transform = new Transform(_transform);
            
            /*
            List params = new ArrayList();
            Iterator i  = getCanonicalizationParameters().iterator();
            while (i.hasNext()) params.add(i.next());
             
            transform.getCanonicalizationParameters().addAll(params);
             */
            //NOTE: shallow copy here
            //TODO: should change this since we support DynamicPolicy
            //TODO: Need to handle clone;
            transform.setAlgorithmParameters(_algorithmParameters);
            return transform;
        }
    }

    public boolean isIsOptimized () {
        return isOptimized;
    }

    public void setIsOptimized (boolean isOptimized) {
        this.isOptimized = isOptimized;
    }
    
    @Override
    public void setXPathVersion(String version){
        this.xpathVersion = version;
    }
    
    @Override
    public String getXPathVersion(){
        return xpathVersion;
    }
}

