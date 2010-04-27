/*
 * $Id: WSTrustClientContract.java,v 1.7 2010-04-27 14:20:28 m_potociar Exp $
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

package com.sun.xml.ws.security.trust;

import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
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
           BaseSTSRequest request, BaseSTSResponse response, IssuedTokenContext context) throws WSTrustException;
   
   /**
    * Handle an RSTR returned by the Issuer and Respond to the Challenge
    * 
    */
   public BaseSTSResponse handleRSTRForNegotiatedExchange(
           BaseSTSRequest rst, BaseSTSResponse rstr, IssuedTokenContext context) throws WSTrustException;
   
   /**
    * Create an RSTR for a client initiated IssuedTokenContext establishment, 
    * for example a Client Initiated WS-SecureConversation context.
    * 
    */
   public BaseSTSResponse createRSTRForClientInitiatedIssuedTokenContext(AppliesTo scopes,IssuedTokenContext context) throws WSTrustException;
    
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
