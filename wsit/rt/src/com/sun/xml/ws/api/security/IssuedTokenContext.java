/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

/*
 * IssuedTokenContext.java
 *
 * Created on October 24, 2005, 6:55 AM
 *
 */

package com.sun.xml.ws.api.security;

import com.sun.xml.wss.XWSSecurityException;

import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

import java.net.URI;
import java.security.Key;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.security.auth.Subject;


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
@ManagedData
@Description("Information used by Trust and Security enforcement")
public interface IssuedTokenContext {
    
    public static String CLAIMED_ATTRUBUTES = "cliamedAttributes";
    public static String TARGET_SERVICE_CERTIFICATE = "tagetedServiceCertificate";
    public static String STS_CERTIFICATE = "stsCertificate";
    public static String STS_PRIVATE_KEY = "stsPrivateKey";
    public static String WS_TRUST_VERSION = "wstVersion";
    public static String CONFIRMATION_METHOD = "samlConfirmationMethod";
    public static String CONFIRMATION_KEY_INFO = "samlConfirmationKeyInfo";
    public static String AUTHN_CONTEXT = "authnContext";
    public static String KEY_WRAP_ALGORITHM = "keyWrapAlgorithm";
    public static String STATUS = "status";
    
    void setTokenIssuer(String issuer);
    
    @ManagedAttribute
    @Description("Token issuer")
    String getTokenIssuer();
    
    /**
     * Requestor Certificate(s)
     * @return the sender certificate, null otherwise
     */
    @ManagedAttribute
    @Description("Requestor certificate")
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
    @ManagedAttribute
    @Description("Requestor username")
    String getRequestorUsername();

    /**
     * set requestor username
     */
    void setRequestorUsername(String username);

    
    @ManagedAttribute
    @Description("Requestor subject")
    Subject getRequestorSubject();
    
    void setRequestorSubject(Subject subject);
    
    void setTokenType(String tokenType);
    
    @ManagedAttribute
    @Description("Token type")
    String getTokenType();
    
    void setKeyType(String keyType);
    
    @ManagedAttribute
    @Description("Key type")
    String getKeyType();
    
    void setAppliesTo(String appliesTo);
    
    @ManagedAttribute
    @Description("appliesTo value")
    String getAppliesTo();
    
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
    @ManagedAttribute
    @Description("Security token")
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
    @ManagedAttribute
    @Description("Proof token")
    Token getAssociatedProofToken(); 
   
   /**
    * If the token returned doesnt allow use of wsu:id attribute then a STR is returned as
    * <wst:RequestedAttachedReference> which needs to be inserted into a <ds:KeyInfo> for example.
    * @return STR if set, null otherwise
    *
    */
    @ManagedAttribute
    @Description("Attached security token reference")
    Token getAttachedSecurityTokenReference();
  
   /**
    * If the token returned doesnt allow use of wsu:id attribute then a STR is returned as
    * <wst:RequestedUnAttachedReference> which needs to be inserted into a <ds:KeyInfo> for example.
    * @return STR if set, null otherwise
    *
    */
    @ManagedAttribute
    @Description("Unattached security token reference")
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
    ArrayList<Object> getSecurityPolicy();
   
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
    @ManagedAttribute
    @Description("Other party entropy")
    Object getOtherPartyEntropy();
   
   /**
    * Set self Entropy
    */
    void setSelfEntropy(Object entropy);
   
   /**
    * Get self Entropy if set, null otherwise
    */
    @ManagedAttribute
    @Description("Self entropy")
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

    void setProofKeyPair(KeyPair keys);
   
    KeyPair getProofKeyPair();
   
    void setAuthnContextClass(String authType);
    
    String getAuthnContextClass();
    
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
     * Get the endpoint address
     */
     String getEndpointAddress();

    /**
     * set the Expiration Time for this Token if any.
     */     
     void  setExpirationTime(Date date);
     
     /**      
      * @return The signature algorithm to use to sign IssuedToken
      */
     String getSignatureAlgorithm();
         
     /**      
      * @param sigAlgo : signature algorithm to use to sign IssuedToken
      */
     void setSignatureAlgorithm(String sigAlgo);
         
     /**      
      * @return The encryption algorithm to use to encrypt IssuedToken
      */
     String getEncryptionAlgorithm();
         
     /**      
      * @param encAlgo : The encryption algorithm to use to encrypt IssuedToken
      */
     void setEncryptionAlgorithm(String encAlgo);
     
     /**      
      * @return The canonicalization algorithm to use when signing IssuedToken
      */
     String getCanonicalizationAlgorithm();
     
     /**      
      * @param canonicalizationAlgo : The canonicalization algorithm to use when signing IssuedToken
      */
     void setCanonicalizationAlgorithm(String canonicalizationAlgo);

     /**      
      * @return The signature algorithm the client intends to use when using ProofKey to sign the application message
      */
     String getSignWith();
     
     /**      
      * @param sigAlgo : The signature algorithm the client intends to use when using ProofKey to sign the application message
      */
     void setSignWith(String sigAlgo);
     
     /**       
      * @return The encryption algorithm the client intends to use when using ProofKey to encrypt the application message
      */
     String getEncryptWith();
     
     /**      
      * @param encAlgo The encryption algorithm the client intends to use when using ProofKey to encrypt the application message
      */
     void setEncryptWith(String encAlgo);
          
    /**
     * Get the SecurityContextTokenInfo for this Token if any.
     */
     SecurityContextTokenInfo getSecurityContextTokenInfo();
     
     void setTarget(Token target);
     
     Token getTarget();

    /**
     * set the SecurityContextTokenInfo for this Token if any.
     */
     void setSecurityContextTokenInfo(SecurityContextTokenInfo sctInfo);
     
    /**
     * Destroy the IssuedTokenContext.
     */
    void destroy();
    
    Map<String, Object> getOtherProperties();
}
