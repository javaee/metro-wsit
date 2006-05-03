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

/*
 * DerivedTokenKeyBinding.java
 *
 * Created on December 20, 2005, 5:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.ws.security.Token;
import com.sun.xml.wss.impl.PolicyTypeUtil;

/**
 *
 * @author Abhijit Das
 */
public class DerivedTokenKeyBinding extends KeyBindingBase {
    
    private WSSPolicy originalKeyBinding = null;
    
    
    /** Creates a new instance of DerivedTokenKeyBinding */
    public DerivedTokenKeyBinding() {
        setPolicyIdentifier(PolicyTypeUtil.DERIVED_TOKEN_KEY_BINDING);
    }
    
    public Object clone() {
        DerivedTokenKeyBinding dkt = new DerivedTokenKeyBinding();
        dkt.setOriginalKeyBinding((WSSPolicy)getOriginalKeyBinding().clone());
        return dkt;
    }
    
    public boolean equals(WSSPolicy policy) {
        if ( !PolicyTypeUtil.derivedTokenKeyBinding(policy)) {
            return false;
        }
        
        WSSPolicy dkt = ((DerivedTokenKeyBinding)policy).getOriginalKeyBinding();
        if ( dkt.getType().intern() != getOriginalKeyBinding().getType().intern() )
            return false;
        //TODO: check the contents (dkt.getValue() and derivedTokenKeyBinding.getValue()
        return true;
    }
    
    public boolean equalsIgnoreTargets(WSSPolicy policy) {
        return equals(policy);
    }
    
    public String getType() {
        return PolicyTypeUtil.DERIVED_TOKEN_KEY_BINDING;
    }
    
    public WSSPolicy getOriginalKeyBinding() {
        return originalKeyBinding;
    }
    
    public void setOriginalKeyBinding(WSSPolicy originalKeyBinding) {
        this.originalKeyBinding = originalKeyBinding;
    }

}
