/*
 * $Id: WSTrustSourceContract.java,v 1.2 2007-01-11 13:15:10 raharsha Exp $
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

import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.IssuedTokenContext;
import javax.xml.transform.Source;


/**
 * The Contract (SPI) to be used by an STS OR a WSP (engaging in
 * a Multi-Message exchange) to handle an Incoming WS-Trust request 
 * and send the corresponding response
 */
public interface WSTrustSourceContract   {

    /** Issue a Token */
    public Source issue(Source request, IssuedTokenContext context,SecureConversationToken policy)throws WSTrustException;

    /** Issue a Collection of Token(s) possibly for different scopes */
    public Source issueMultiple(Source request, IssuedTokenContext context) throws WSTrustException;

    /** Renew a Token */
    public Source renew(Source request, IssuedTokenContext context) throws WSTrustException;

    /** Cancel a Token */
    public Source cancel(Source request, IssuedTokenContext context) throws WSTrustException;

    /** Validate a Token */
    public Source validate(Source request, IssuedTokenContext context) throws WSTrustException;

    /** 
     * handle an unsolicited RSTR like in the case of 
     * Client Initiated Secure Conversation.
     */
   public void handleUnsolicited(Source rstr, IssuedTokenContext context) throws WSTrustException;
   
   /**
    * Contains Challenge
    * @return true if the RSTR contains a SignChallenge/BinaryExchange or
    *  some other custom challenge recognized by this implementation Or the 
    *  RST contains Initial Negotiation/Challenge information
    *   for a Multi-Message exchange.
    */
   boolean containsChallenge(Source rstORrstr);
   
  
}
