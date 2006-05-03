/*
 * $Id: WSTrustClientContract.java,v 1.1 2006-05-03 22:57:14 arungupta Exp $
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

package com.sun.xml.ws.security.trust;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;

import java.net.URI;

/**
 * The Contract to be used by the Trust-Plugin on the Client Side.
 * TODO: Need to refine this....
 * @author root
 */
public interface WSTrustClientContract {
   
   /**
    * Handle an RSTR returned by the Issuer and update Token information into the
    * IssuedTokenContext.
    */
   public void handleRSTR(
           RequestSecurityToken rst, RequestSecurityTokenResponse rstr, IssuedTokenContext context) throws WSTrustException;
   
   /**
    * Handle an RSTR returned by the Issuer and Respond to the Challenge
    * 
    */
   public RequestSecurityTokenResponse handleRSTRForNegotiatedExchange(
           RequestSecurityToken rst, RequestSecurityTokenResponse rstr, IssuedTokenContext context) throws WSTrustException;
   
   /**
    * Create an RSTR for a client initiated IssuedTokenContext establishment, 
    * for example a Client Initiated WS-SecureConversation context.
    * 
    */
   public RequestSecurityTokenResponse createRSTRForClientInitiatedIssuedTokenContext(AppliesTo scopes,IssuedTokenContext context) throws WSTrustException;
    
   /**
    * Contains Challenge
    * @return true if the RSTR contains a SignChallenge/BinaryExchange or
    *  some other custom challenge recognized by this implementation.
    */
   boolean containsChallenge(RequestSecurityTokenResponse rstr);
   
   /**
    * Return the &lt;wst:ComputedKey&gt; URI if any inside the RSTR, null otherwise
    */
   URI getComputedKeyAlgorithmFromProofToken(RequestSecurityTokenResponse rstr);
}
