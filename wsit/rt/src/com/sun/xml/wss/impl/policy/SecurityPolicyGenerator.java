/*
 * $Id: SecurityPolicyGenerator.java,v 1.1 2006-05-03 22:57:53 arungupta Exp $
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
 * A Factory interface for Generating Concrete Security Policies 
 * @see com.sun.xml.wss.impl.policy.mls.WSSPolicyGenerator
 */
public interface SecurityPolicyGenerator {
    
    /**
     * Create and return a new Concrete MLS policy
     * @return a new Concrete MLS policy
     * @exception PolicyGenerationException if an MLS Policy cannot be generated
     */
    public MLSPolicy newMLSPolicy () throws PolicyGenerationException;
    
   
    /**
     * Create and return a new Security Policy Configuration
     * @return a new Security Policy Configuration
     * @exception PolicyGenerationException if a Configuration cannot be generated
     */
    public SecurityPolicy configuration () throws PolicyGenerationException;
}
