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

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.wss.impl.PolicyTypeUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents mandatory header elements that need to present in the message.
 * @author K.Venugopal@sun.com
 */
public class MandatoryTargetPolicy extends WSSPolicy {
    
    
    /** Creates a new instance of MandatoryTargetPolicy */
    public MandatoryTargetPolicy() {
    }
    
    /**
     * 
     * @return clone
     */
    public Object clone() {
        MandatoryTargetPolicy mp = new MandatoryTargetPolicy();
        WSSPolicy wp = (WSSPolicy) getFeatureBinding();
        if(wp != null){
            WSSPolicy nwp = (WSSPolicy)wp.clone();
            mp.setFeatureBinding(nwp);
        }
        return mp;
    }
    
    /**
     * 
     * @param policy 
     * @return true of policy is equal to this policy
     */
    public boolean equals(WSSPolicy policy) {
        if(policy.getType() == PolicyTypeUtil.MANDATORY_TARGET_POLICY_TYPE){
            WSSPolicy p1 = (WSSPolicy) policy.getFeatureBinding();
            if(p1 == null || getFeatureBinding() == null){
                return false;
            }
            return p1.equals(getFeatureBinding());
        }
        return false;
    }
    
    /**
     * 
     * @param policy 
     * @return true if argument policy is equal to this policy ignoring targets
     */
    public boolean equalsIgnoreTargets(WSSPolicy policy) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * 
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.MANDATORY_TARGET_POLICY_TYPE;
    }
    
    
    public static class FeatureBinding extends WSSPolicy {
        private List<Target> targets = new ArrayList<Target>();
        
        
        /**
         * adds the Target representing the Header element that must be present in the message.
         * Will by default set enforce flag on Target element to true.
         * @param target 
         */
        public void addTargetBinding(Target target){
            targets.add(target);
            target.setEnforce(true);
        }
        
        /**
         * 
         * @return list of Target elements
         */
        public List<Target> getTargetBindings(){
            return targets;
        }
        
        /**
         * 
         * @return clone
         */
        public Object clone() {
            FeatureBinding binding = new FeatureBinding();
            for(Target t: targets){
                binding.addTargetBinding(t);
            }
            return binding;
        }
        
        /**
         * 
         * @param policy 
         * @return true if this policy is equal to the argument policy
         */
        public boolean equals(WSSPolicy policy) {
            boolean retVal = false;
            if(policy.getType() == PolicyTypeUtil.MANDATORY_TARGET_FEATUREBINDING_TYPE){
                List<Target> tList = ((MandatoryTargetPolicy.FeatureBinding)policy).getTargetBindings();
                for(Target t: tList){ 
                    if(!targets.contains(t)){
                        break;
                    }
                }
                retVal = true;
            }
            return retVal;
        }
        
        /**
         * 
         * @param policy 
         * @return true if this policy is equal to the argument policy ignoring targets
         */
        public boolean equalsIgnoreTargets(WSSPolicy policy) {
            throw new UnsupportedOperationException();
        }
        
        /**
         * 
         * @return type of the policy
         */
        public String getType() {
            return PolicyTypeUtil.MANDATORY_TARGET_FEATUREBINDING_TYPE;
        }
        
    }
    
}
