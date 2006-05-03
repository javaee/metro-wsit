/*
 * $Id: MLSPolicy.java,v 1.1 2006-05-03 22:57:53 arungupta Exp $
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

package com.sun.xml.wss.impl.policy;

/**
 * Represents a base class for Message Level Security (MLS) Policies.
 * Any MLSPolicy can be epxressed as being composed of one or both of
 * two SecurityPolicy components called FeatureBinding and KeyBinding.
 * This generic structure for an MLSPolicy allows for representing complex,
 * concrete Message Level Security Policies.
 */
public abstract class MLSPolicy implements SecurityPolicy {

    protected boolean readonly = false;
    
    
    /**
     * Get FeatureBinding component
     * @return FeatureBinding component of this MLSPolicy
     * @exception PolicyGenerationException if a FeatureBinding component is invalid for this MLSPolicy
     */
    public abstract MLSPolicy getFeatureBinding () throws PolicyGenerationException;

    /**
     * Get KeyBinding component
     * @return KeyBinding component of this MLSPolicy
     * @exception PolicyGenerationException if a KeyBinding component is invalid for this MLSPolicy
     */
    public abstract MLSPolicy getKeyBinding () throws PolicyGenerationException;
    
    
    /**
     * @param readonly set the readonly status of the policy.
     *
     */
    public void isReadOnly(boolean readonly) {
        this.readonly = readonly;
        try {
            MLSPolicy featureBinding = getFeatureBinding();
            if ( featureBinding != null ) {
                featureBinding.isReadOnly(readonly);
            }
            
            MLSPolicy keybinding = getKeyBinding();
            if ( keybinding != null ) {
                keybinding.isReadOnly(readonly);
            }
        } catch ( PolicyGenerationException e) {
        }
    }

    /**
     * @return true if policy is readonly.
     *
     */
    public boolean isReadOnly() {
        return readonly;
    }

}
