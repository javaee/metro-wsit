/*
 * $Id: SecurityPolicy.java,v 1.1 2006-05-03 22:57:53 arungupta Exp $
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
 * Tagging interface for the following Security Policy types (or any custom defined security policy type)
 *
 *  <UL>
 *   <LI>MLSPolicy
 *   <LI>SecurityPolicyContainer
 *   <LI>DynamicSecurityPolicy
 * </UL>
 */
public interface SecurityPolicy {

    /**
     * Get the type of the policy.
     *<P>
     * Implementation Note: Useful to avoid instanceof checks and String.equals checks
     * @return the type of the policy
     */
    public String getType();
}
