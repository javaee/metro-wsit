/*
 * $Id: DynamicSecurityPolicy.java,v 1.1 2006-05-03 22:57:54 arungupta Exp $
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

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.wss.impl.policy.SecurityPolicyGenerator;
import com.sun.xml.wss.impl.configuration.*;

/**
 * Represents  a dynamically generable SecurityPolicy.
 * It contains an associated Policy Generator that can be used to 
 * generate appropriate Security Policies understood by the
 * XWS-Security framework.
 */
public class DynamicSecurityPolicy extends com.sun.xml.wss.impl.policy.DynamicSecurityPolicy {
    
    /**
     * Return the associated SecurityPolicy generator
     * @return SecurityPolicyGenerator, the associated generator
     */
    public  SecurityPolicyGenerator policyGenerator () {
        return new WSSPolicyGenerator();
    }
}
