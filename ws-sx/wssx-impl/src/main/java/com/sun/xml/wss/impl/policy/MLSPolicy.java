/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
 * $Id: MLSPolicy.java,v 1.2 2010-10-21 15:37:33 snajper Exp $
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
