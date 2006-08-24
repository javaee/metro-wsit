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
     * @return 
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
     * @return 
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
     * @return 
     */
    public boolean equalsIgnoreTargets(WSSPolicy policy) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * 
     * @return 
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
         * @return 
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
         * @return 
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
         * @return 
         */
        public boolean equalsIgnoreTargets(WSSPolicy policy) {
            throw new UnsupportedOperationException();
        }
        
        /**
         * 
         * @return 
         */
        public String getType() {
            return PolicyTypeUtil.MANDATORY_TARGET_FEATUREBINDING_TYPE;
        }
        
    }
    
}
