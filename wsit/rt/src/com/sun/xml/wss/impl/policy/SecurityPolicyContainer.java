/*
 * $Id: SecurityPolicyContainer.java,v 1.1 2006-05-03 22:57:53 arungupta Exp $
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;

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
