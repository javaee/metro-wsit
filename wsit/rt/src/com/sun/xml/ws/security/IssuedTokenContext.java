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
 * IssuedTokenContext.java
 *
 * Created on October 24, 2005, 6:55 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import com.sun.xml.wss.XWSSecurityException;

import java.net.URI;
import java.util.Date;


/**
 * This interface is the SPI defined by WS-Security to enable WS-Trust/SecureConversation 
 * specific security interactions.
 *<p>
 * This interface represents a  Context containing information
 * populated and used by the Trust and the Security Enforcement Layers 
 * (for example the proof-token of an Issued token needs to be used 
 * by the SecurityEnforcement Layer to secure the message).
 *</p>
 * 
 */
public interface IssuedTokenContext {
    
    /**
     * Requestor Certificate(s)
     * @return the sender certificate, null otherwise
     */
    X509Certificate getRequestorCertificate();
    
    /**
     * Append the Requestor Certificate that was used in an 
     * incoming message.
     */
    void setRequestorCertificate(X509Certificate cert);

    /**
     * Requestor username if any
     * @return the requestor username if provided
     */
    String getRequestorUsername();

    /**
     * set requestor username
     */
    void setRequestorUsername(String username);

    
    /**
     * Depending on the <sp:IncludeToken> server policy, set the Token to be
     * used in Securing requests and/or responses
     */
    void setSecurityToken(Token tok);
   
    /**
     * Depending on the <sp:IncludeToken> policy get the Token to be
     * used in Securing requests and/or responses. The token returned 
     * is to be used only for inserting into the SecurityHeader, if the
     * getAssociatedProofToken is not null, and it should also be used for
     * securing the message if there is no Proof Token associated. 
     */
    Token getSecurityToken();
   
   /**
    * Set the Proof Token Associated with the SecurityToken
    * <p>
    * when the SecurityToken is a SecurityContext token (as defined in 
    * WS-SecureConversation) and Derived Keys are being used then 
    * the Proof Token is the <wsc:DerivedKeyToken>
    */
    void setAssociatedProofToken(Token token);
   
   /**
    * get the Proof Token (if any) associated with the SecurityToken, null otherwise
    */
    Token getAssociatedProofToken(); 
   
   /**
    * If the token returned doesnt allow use of wsu:id attribute then a STR is returned as
    * <wst:RequestedAttachedReference> which needs to be inserted into a <ds:KeyInfo> for example.
    * @return STR if set, null otherwise
    *
    */
    Token getAttachedSecurityTokenReference();
  
   /**
    * If the token returned doesnt allow use of wsu:id attribute then a STR is returned as
    * <wst:RequestedUnAttachedReference> which needs to be inserted into a <ds:KeyInfo> for example.
    * @return STR if set, null otherwise
    *
    */
    Token getUnAttachedSecurityTokenReference();
  
   /**
    * If the token returned doesnt allow use of wsu:id attribute then a STR is returned as
    * <wst:RequestedAttachedReference> which needs to be inserted into a <ds:KeyInfo> for example
    *
    */
    void setAttachedSecurityTokenReference(Token str);
  
    /**
    * If the token returned doesnt allow use of wsu:id attribute then a STR is returned as
    * <wst:RequestedUnAttachedReference> which needs to be inserted into a <ds:KeyInfo> for example
    *
    */
    void setUnAttachedSecurityTokenReference(Token str);
    
   /**
    * get the SecurityPolicy to be applied for the request or response 
    * to which this SecurityContext corresponds to
    *
    * This allows the Client and/or the Service (WSP/STS) to dynamically inject 
    * policy to be applied. For example in the case of SignChallenge when the 
    * Initiator (client) has to sign a specific challenge.
    * <p>
    * Note: Inserting an un-solicited RSTR into a SOAP Header can also be expressed as
    * a policy and the subsequent requirement to sign the RSTR will also be expressed as
    * a policy
    * </p>
    * TODO: There is no policy today to insert a specific element to a SOAP Header, we 
    * need to extend the policy definitions in XWS-Security.
    */
    ArrayList getSecurityPolicy();
   
   /**
    * Set the Entropy information provided by the other Part (if any)
    *<p>
    * WS-Trust allows requestor to provide input 
    * to key material in the request. 
    * The requestor might do this to satisfy itself as to the degree of 
    * entropy(cyrptographic randomness) of atleast some of the material used to 
    * generate the actual Key.
    * </p>
    * For composite Keys Entropy can be set by both parties, the concrete
    * entropy element can be a <wst:Entropy> instance but the argument here is
    * generic to avoid a dependence of the SPI on WS-Trust packages
    */
    void setOtherPartyEntropy(Object entropy);
   
   /**
    * Get the Entropy if any provided by the other party, null otherwise
    * If the Entropy was specified as an <xenc:EncryptedKey> then
    * this method would return the decrypted secret
    */
    Key getDecipheredOtherPartyEntropy(Key privKey) throws XWSSecurityException;
   
   /**
    * Get the Entropy if any provided by the Other Party, null otherwise
    */
    Object getOtherPartyEntropy();
   
   /**
    * Set self Entropy
    */
    void setSelfEntropy(Object entropy);
   
   /**
    * Get self Entropy if set, null otherwise
    */
    Object getSelfEntropy();
   
   
   /**
    * Return the <wst:ComputedKey> URI if any inside the RSTR, null otherwise.
    * The Security Enforcement Layer would compute the Key as P_SHA1(Ent(req), Ent(res))
    */
    URI getComputedKeyAlgorithmFromProofToken();
   
   /**
    * set the SecureConversation ProofToken as a byte[] array
    */
    void setProofKey(byte[] key);
   
   /**
    * get the SecureConversation ProofToken as a byte[] array
    */
    byte[] getProofKey();
   
    /**
     *@return the creation Time of the IssuedToken
     */
     Date getCreationTime();

    /**
     * get the Expiration Time for this Token if any
     */
     Date getExpirationTime();

    /**
     *set the creation Time of the IssuedToken
     */
     void setCreationTime(Date date);

    /**
     * set the endpointaddress
     */
     void  setEndpointAddress(String endPointAddress);
     
      /**
     *get the endpoint address
     */
     String getEndpointAddress();

    /**
     * set the Expiration Time for this Token if any
     */
     void  setExpirationTime(Date date);

    /**
     * Destroy the IssuedTokenContext
     */
    void destroy();
}
