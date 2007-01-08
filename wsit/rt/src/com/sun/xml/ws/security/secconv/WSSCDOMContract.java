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

/*
 * WSSCDOMContract.java
 *
 * Created on February 3, 2006, 1:56 PM
 *
 */

package com.sun.xml.ws.security.secconv;

import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustDOMContract;

import org.w3c.dom.Element;

/**
 *
 * @author ws-trust-implementation-team
 */
public class WSSCDOMContract implements WSTrustDOMContract {
    
    private WSSCContract contract;
    
    private static WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance();
    
    /** Creates a new instance of WSSCDOMContract */
    public void init(final Configuration config) {
        contract = new WSSCContract();
        contract.init(config);
    }
    
    /** Issue a Token */
    public Element issue(final Element request, final IssuedTokenContext context,final SecureConversationToken policy)throws WSSecureConversationException {
        return eleFac.toElement(contract.issue(eleFac.createRSTFrom(request), context, policy));
    }

    /** Issue a Collection of Token(s) possibly for different scopes */
    public Element issueMultiple(final Element request, final IssuedTokenContext context) throws WSSecureConversationException {
        return null;
    }

    /** Renew a Token */
    public Element renew(final Element request, final IssuedTokenContext context) throws WSSecureConversationException {
        return null;
    }

    /** Cancel a Token */
    public Element cancel(final Element request, final IssuedTokenContext context) throws WSSecureConversationException {
        return null;
    }

    /** Validate a Token */
    public Element validate(final Element request, final IssuedTokenContext context) throws WSSecureConversationException {
        return null;
    }
    
    /** 
     * handle an unsolicited RSTR like in the case of 
     * Client Initiated Secure Conversation.
     */
   public void handleUnsolicited(final Element rstr, final IssuedTokenContext context) throws WSSecureConversationException {
       
   }
   
   /**
    * Contains Challenge
    * @return true if the RSTR contains a SignChallenge/BinaryExchange or
    *  some other custom challenge recognized by this implementation OR the RST 
    *  contains Initial Negotiation/Challenge information
    * for a Multi-Message exchange.
    */
   public boolean containsChallenge(final Element rstORrstr){
       return false;
   }
   
    
}
