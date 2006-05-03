/*
 * $Id: SignatureTarget.java,v 1.1 2006-05-03 22:57:56 arungupta Exp $
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

import java.security.spec.AlgorithmParameterSpec;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Properties;

import com.sun.xml.wss.impl.policy.mls.Target;
import com.sun.xml.wss.impl.MessageConstants;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

/**
 * Objects of this class represent a Signature Target that can be part of
 * the FeatureBinding for a SignaturePolicy (refer SignaturePolicy.FeatureBinding).
 */
public class SignatureTarget extends Target implements Cloneable {
    
    String _digestAlgorithm = "";
    
    ArrayList _transforms = new ArrayList();
   
    private boolean isOptimized = false;
    
    private String xpathVersion = "";
            
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
    public void addTransform(Transform transform) {
        String transformStr = transform.getTransform();
        if (isBSP() && 
                ((transformStr != MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS) 
                && (transformStr != MessageConstants.ATTACHMENT_COMPLETE_TRANSFORM_URI)
                && (transformStr != MessageConstants.ATTACHMENT_CONTENT_ONLY_TRANSFORM_URI)
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
    public static class Transform {
        
        String _transform = "";
        
        // List _canonicalizationParameters = new ArrayList();
        
        AlgorithmParameterSpec _algorithmParameters = null;
        
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
    
    public void setXPathVersion(String version){
        this.xpathVersion = version;
    }
    
    public String getXPathVersion(){
        return xpathVersion;
    }
}

