/*
 * $Id: SecurityPolicyContainer.java,v 1.5 2008/07/03 05:29:02 ofung Exp $
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

package com.sun.xml.wss.impl.policy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import com.sun.xml.wss.impl.PolicyTypeUtil;

/**
 * Represents a container for a static collection of SecurityPolicies.
 * It Associates a StaticPolicyContext with a SecurityPolicy.
 */
public class SecurityPolicyContainer implements SecurityPolicy {
    
    protected HashMap _ctx2PolicyMap = new HashMap();
    
    public SecurityPolicyContainer() {}
    
    /**
     * Associate more than one SecurityPolicy with a StaticPolicyContext
     * @param ctx StaticPolicyContext
     * @param policy SecurityPolicy
     */
    public void setSecurityPolicy(StaticPolicyContext ctx, SecurityPolicy policy) {
        ArrayList al = (ArrayList)_ctx2PolicyMap.get(ctx);
        
        if (al != null)
            al.add(policy);
        else {
            al = new ArrayList();
            al.add(policy);
            _ctx2PolicyMap.put(ctx, al);
        }
    }
    
    /**
     * Return an immutable collection of SecurityPolicies,
     *  association between policies are free to inference
     *
     * @param ctx StaticPolicyContext
     * @return Iterator of security policies associated with the StaticPolicyContext <code>ctx</code>
     */
    public Iterator getSecurityPolicies(StaticPolicyContext ctx) {
        ArrayList list = (ArrayList)_ctx2PolicyMap.get(ctx);
        
        if (list != null)
            return list.iterator();
        return null;
    }
    
    /**
     * Returns all keys (StaticPolicyContext)
     * @return Iterator on Key Set
     */
    public Iterator getAllContexts() {
        return _ctx2PolicyMap.keySet().iterator();
    }
    
    /*
     * Composite SecurityPolicy instances are evaluated at runtime,
     * Throws PolicyGenerationException if evaluation is unsuccessful
     *
     * @param sCtx StaticPolicyContext
     *        dCtx DynamicPolicyContext
     * @return Iterator of SecurityPolicies
     * @exception PolicyGenerationException
     */
    public Iterator getSecurityPolicies(StaticPolicyContext sCtx, DynamicPolicyContext dCtx)
    throws PolicyGenerationException {
        ArrayList hs0 = (ArrayList)_ctx2PolicyMap.get(sCtx);
        
        ArrayList hs1 = new ArrayList();
        
        Iterator i = hs0.iterator();
        while (i.hasNext()) {
            Object obj = i.next();
            
            /*if (obj instanceof PolicyComposer) {
                PolicyComposer pc = (PolicyComposer)obj;
                try {
                    SecurityPolicy sp = pc.evaluateSecurityPolicy(dCtx);
                    hs1.add(sp);
                } catch (UnsupportedOperationException uoe) {
                    try {
                        Collection s = pc.evaluate(dCtx);
                        hs1.addAll(s);
                    } catch (UnsupportedOperationException eou) {
                        throw new PolicyGenerationException(eou);
                    }
                }
            } else*/
            hs1.add(obj);
        }
        
        return hs1.iterator();
    }
    
    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.SEC_POLICY_CONTAINER_TYPE;
    }
    
}
