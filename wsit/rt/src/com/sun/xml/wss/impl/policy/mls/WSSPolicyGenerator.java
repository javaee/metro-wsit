/*
 * $Id: WSSPolicyGenerator.java,v 1.3.2.2 2010-07-14 14:07:15 m_potociar Exp $
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

package com.sun.xml.wss.impl.policy.mls;

//import com.sun.xml.wss.impl.policy.SSLPolicy;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.SecurityPolicyGenerator;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;


/**
 * This class is a Factory for generating the various Security Policy primitives
 * that are understood and processed by XWS-Security.
 * A <code>DynamicSecurityPolicy</code> can obtain an instance of this class to
 * create instances of SecurityPolicies at runtime.
 */
public class WSSPolicyGenerator implements SecurityPolicyGenerator {

    MessagePolicy configuration = new MessagePolicy ();

    /**
     * Default constructor
     */
    public WSSPolicyGenerator () {}
    
    /**
     * return a new concrete MLSPolicy instance
     * @return MLSPolicy
     * @exception PolicyGenerationException
     */
    public MLSPolicy newMLSPolicy () throws PolicyGenerationException {
        throw new PolicyGenerationException ("Unsupported Operation");
    }      

    /**
     * return a new TimestampPolicy instance
     * @return TimestampPolicy
     * @exception PolicyGenerationException
     */
    public TimestampPolicy newTimestampPolicy () throws PolicyGenerationException {
        TimestampPolicy policy = new TimestampPolicy ();

        configuration.append (policy);

        return policy; 
    }   

    /**
     * return a new SignaturePolicy instance
     * @return SignaturePolicy
     * @exception PolicyGenerationException
     */
    public SignaturePolicy newSignaturePolicy () throws PolicyGenerationException {
        SignaturePolicy policy = new SignaturePolicy ();

        configuration.append (policy);

        return policy; 
    }   

    /**
     * return a new EncryptionPolicy instance
     * @return EncryptionPolicy
     * @exception PolicyGenerationException
     */
    public EncryptionPolicy newEncryptionPolicy () throws PolicyGenerationException {
        EncryptionPolicy policy = new EncryptionPolicy ();

        configuration.append (policy);

        return policy; 
    }   

    /**
     * return a new AuthenticationTokenPolicy instance
     * @return AuthenticationTokenPolicy
     * @exception PolicyGenerationException
     */
    public AuthenticationTokenPolicy newAuthenticationTokenPolicy () throws PolicyGenerationException {
        AuthenticationTokenPolicy policy = new AuthenticationTokenPolicy ();

        configuration.append (policy);

        return policy; 
    }  

    /**
     * return a SecurityPolicy that represents a configuration
     * @return SecurityPolicy
     * @exception PolicyGenerationException
     */
    public SecurityPolicy configuration () throws PolicyGenerationException {
        return configuration; 
    }
}
