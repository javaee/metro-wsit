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

/*
 * IssuedTokenKeyBinding.java
 *
 * Created on December 20, 2005, 5:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.wss.impl.PolicyTypeUtil;

/**
 *
 */
public class IssuedTokenKeyBinding extends KeyBindingBase implements LazyKeyBinding {
    
    String strId = null;
    private String realId;
    private String tokenType;
    
    
    /** Creates a new instance of IssuedTokenKeyBinding */
    public IssuedTokenKeyBinding() {
        setPolicyIdentifier(PolicyTypeUtil.ISSUED_TOKEN_KEY_BINDING);
    }
    
    public Object clone() {
        IssuedTokenKeyBinding itb = new IssuedTokenKeyBinding();
        //itb.setPolicyToken(this.getPolicyToken());
        itb.setUUID(this.getUUID());
        itb.setIncludeToken(this.getIncludeToken());
        itb.setPolicyTokenFlag(this.policyTokenWasSet());
        itb.setSTRID(this.strId);
        return itb;
    }
    
    public boolean equals(WSSPolicy policy) {
        if ( !PolicyTypeUtil.issuedTokenKeyBinding(policy)) {
            return false;
        }
        
        //TODO: Check the contents of IssuedTokenContext
        return true;
    }
    
    public boolean equalsIgnoreTargets(WSSPolicy policy) {
        return equals(policy);
    }
    
    public String getType() {
        return PolicyTypeUtil.ISSUED_TOKEN_KEY_BINDING;
    }
    
        /*
         * @param id the wsu:id of the wsse:SecurityTokenReference to
         * be generated for this Issued Token. Applicable while
         * sending a message (sender side policy)
         */
        public void setSTRID(String id) {
            if (isReadOnly()) {
                throw new RuntimeException("Can not set Issued Token STRID : Policy is ReadOnly");
            }
            
            this.strId = id;
        }
        
        /*
         * @return the wsu:id of the wsse:SecurityTokenReference to
         * be generated for this Issued Token, if specified,
         * null otherwise.
         */
        public String getSTRID() {
            return this.strId;
        }

    public String getRealId() {
        return realId;
    }

    public void setRealId(String realId) {
       this.realId = realId;
    }
    public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public String getTokenType() {
            return tokenType;
        }

}
