/*
 * $Id: EncryptionTarget.java,v 1.3.2.2 2010-07-14 14:07:05 m_potociar Exp $
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

import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.ArrayList;

import org.w3c.dom.Element;

/**
 * Objects of this class represent an Encryption Target that can be part of
 * the FeatureBinding for an EncryptionPolicy (refer EncryptionPolicy.FeatureBinding).
 */
public class EncryptionTarget extends Target implements Cloneable {
    
    String _dataEncryptionAlgorithm = ""; // Rf. MessageConstants
    
    ArrayList _cipherReferenceTransforms = new ArrayList();
    
    Element drefData = null;
    private boolean isOptimized = false;
    
    /**
     * Default constructor
     */
    public EncryptionTarget() {}
    
    /**
     * Constructor that takes a Target
     */
    public EncryptionTarget(Target target) {
        this.setEnforce(target.getEnforce());
        this.setType(target.getType());
        this.setValue(target.getValue());
        this.setContentOnly(target.getContentOnly());
    }
    
    
    /**
     * Constructor
     * @param algorithm Data Encryption Algorithm
     */
    public EncryptionTarget(String algorithm) {
        this._dataEncryptionAlgorithm = algorithm;
    }
    
    /**
     * Constructor
     * @param algorithm Data Encryption Algorithm
     * @param transform Cipher Reference Transform
     */
    @SuppressWarnings("unchecked")
    public EncryptionTarget(String algorithm, String transform) {
        this._dataEncryptionAlgorithm = algorithm;
        this._cipherReferenceTransforms.add(new Transform(transform));
    }
    
    /**
     * set the DataEncryptionAlgorithm
     * @param algorithm Data Encryption Algorithm
     */
    public void setDataEncryptionAlgorithm(String algorithm) {
        this._dataEncryptionAlgorithm = algorithm;
    }
    
    /**
     * @return Data Encryption Algorithm
     */
    public String getDataEncryptionAlgorithm() {
        return this._dataEncryptionAlgorithm;
    }
    
    /**
     * add a CipherReference Transform
     * @param transform Cipher Reference Transform
     */
    @SuppressWarnings("unchecked")
    public void addCipherReferenceTransform(String transform) {
        this._cipherReferenceTransforms.add(new Transform(transform));
    }
    
    /**
     * add a CipherReference Transform
     * @param transform CipherReference Transform
     */
    @SuppressWarnings("unchecked")
    public void addCipherReferenceTransform(Transform transform) {
        this._cipherReferenceTransforms.add(transform);
    }
    
    /**
     * @return Collection of CipherReference Transforms
     */
    public ArrayList getCipherReferenceTransforms() {
        return _cipherReferenceTransforms;
    }
    
    /**
     * @return  a new instance of Encryption Transform
     */
    public Transform newEncryptionTransform() {
        return new Transform();
    }
    
    /**
     * Equals operator
     * @param target EncryptionTarget
     * @return true if the target argument is equal to this Target
     */
    public boolean equals(EncryptionTarget target) {
        boolean b1 = _dataEncryptionAlgorithm.equals("") ? true :
            _dataEncryptionAlgorithm.equals(target.getDataEncryptionAlgorithm());
        
        boolean b2 = _cipherReferenceTransforms.equals(target.getCipherReferenceTransforms());
        
        return b1 && b2;
    }
    
    /**
     * clone operator
     * @return a clone of this EncryptionTarget
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        EncryptionTarget target = new EncryptionTarget();
        
        try {
            target.setDataEncryptionAlgorithm(_dataEncryptionAlgorithm);
            target.setValue(this.getValue());
            target.setType(this.getType());
            target.setContentOnly(this.getContentOnly());
            target.setEnforce(this.getEnforce());
            
            Iterator i = getCipherReferenceTransforms().iterator();
            while (i.hasNext()) {
                Transform transform = (Transform) i.next();
                target.getCipherReferenceTransforms().add(transform.clone());
            }
        } catch (Exception e) {}
        
        return target;
    }
    
    public void setElementData(Element data){
        this.drefData = data;
    }
    
    public Element getElementData(){
        return this.drefData ;
    }
    
    /**
     * This class represents a Transform that can appear on an EcncryptionTarget,
     * Instances of this class are added as CipherReference Transforms on an EcncryptionTarget
     */
    public static class Transform implements Cloneable {
        
        String _transform = "";
        
        AlgorithmParameterSpec _algorithmParameters = null;
        
        /**
         *Default constructor
         */
        public Transform() {}
        
        /**
         * Constructor
         * @param algorithm the URI for the transform alogrithm
         */
        public Transform(String algorithm) {
            this._transform = algorithm;
        }
        
        
        /**
         * @return the algorithm parameters
         */
        public AlgorithmParameterSpec getAlgorithmParameters() {
            return this._algorithmParameters;
        }
        
        /**
         * set any parameters for the Transform Algorithm
         * @param params a HashMap of AlgorithmParameters
         */
        public void setAlgorithmParameters(AlgorithmParameterSpec params) {
            this._algorithmParameters= params;
        }
        
        /**
         * set the Transform Algorithm
         * @param algorithm the Algorithm for the Transform
         */
        public void setTransform(String algorithm) {
            this._transform = algorithm;
        }
        
        /**
         * @return algorithm the transform algorithm
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
            transform.setAlgorithmParameters(_algorithmParameters);
            return transform;
        }
    }

    public boolean isIsOptimized () {
        return isOptimized;
    }  
}

