/*
 * $Id: WSSPolicyGenerator.java,v 1.1 2006-05-03 22:57:57 arungupta Exp $
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
