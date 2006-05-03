/*
 * $Id: DynamicSecurityPolicy.java,v 1.1 2006-05-03 22:57:52 arungupta Exp $
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

import com.sun.xml.wss.impl.PolicyTypeUtil;

/**
 * Represents a dynamically generable SecurityPolicy
 */
public abstract class DynamicSecurityPolicy implements SecurityPolicy {
    
    /*
     * Associate static application context
     */
    StaticPolicyContext ctx;
    
    /**
     * Default constructor
     */
    public DynamicSecurityPolicy () {}
    
    /**
     * Instantiate and associate DynamicSecurityPolicy with StaticPolicyContext
     *
     * @param ctx static security context used for implying dynamic policy generation
     */
    public DynamicSecurityPolicy (StaticPolicyContext ctx) {
        this.ctx = ctx;
    }
    
    /**
     * @return the StaticPolicyContext associated with this DynamicSecurityPolicy, null otherwise
     */
    public StaticPolicyContext getStaticPolicyContext () {
        return ctx;
    }
    
    /**
     * set the StaticPolicyContext for this DynamicSecurityPolicy
     * @param ctx the StaticPolicyContext for this DynamicSecurityPolicy.
     */
    public void setStaticPolicyContext (StaticPolicyContext ctx) {
        this.ctx = ctx;
    }
    
    /**
     * Associate a SecurityPolicy generator
     * @return SecurityPolicyGenerator that can be used to generate concrete SecurityPolicies
     * @see com.sun.xml.wss.impl.callback.DynamicPolicyCallback
     */
    public abstract SecurityPolicyGenerator policyGenerator ();

    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.DYN_SEC_POLICY_TYPE;
    }
    
}
