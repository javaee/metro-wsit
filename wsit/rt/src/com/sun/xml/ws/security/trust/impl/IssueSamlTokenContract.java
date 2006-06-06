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

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.security.auth.Subject;
import javax.xml.transform.Source;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.trust.elements.str.DirectReference;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustContract;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;

import com.sun.xml.ws.security.wsu.AttributedDateTime;

import com.sun.xml.wss.SubjectAccessor;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class IssueSamlTokenContract implements WSTrustContract {

    protected TrustSPMetadata config;

    protected static final WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance();
    protected static final SimpleDateFormat calendarFormatter
        = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'");

    private static final int DEFAULT_KEY_SIZE = 256;
    private static final String SAML_VALUE_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.0#SAMLAssertionID";

    public void init(Configuration config) 
    {
        this.config = (TrustSPMetadata)config;
    }

   /** Issue a Token */
    public RequestSecurityTokenResponse issue(RequestSecurityToken rst, IssuedTokenContext context, SecureConversationToken policy)throws WSTrustException
    {   

        //============================
        // Create required secret key
        //============================
        URI keyTypeURI = rst.getKeyType();
        if (keyTypeURI != null && WSTrustConstants.SYMMETRIC_KEY.equals(keyTypeURI.toString()))
            throw new WSTrustException("Unsupported proof key type: " + keyTypeURI.toString());

        Entropy serverEntropy = null;
        RequestedProofToken proofToken = eleFac.createRequestedProofToken();

        // Get client entropy
        byte[] clientEntropyValue = null;
        Entropy clientEntropy = rst.getEntropy();
        if (clientEntropy != null){
            BinarySecret clientBS = clientEntropy.getBinarySecret();
            if (clientBS == null){
                //ToDo
            }else {
                clientEntropyValue = clientBS.getRawValue(); 
            }
        }

        int keySize = (int)rst.getKeySize();
        if (keySize < 1){
            keySize = DEFAULT_KEY_SIZE;
        }


        byte[] key = WSTrustUtil.generateRandomSecret(keySize/8);
        BinarySecret serverBS = eleFac.createBinarySecret(key, BinarySecret.NONCE_KEY_TYPE);
        serverEntropy = eleFac.createEntropy(serverBS);
        proofToken.setProofTokenType(RequestedProofToken.COMPUTED_KEY_TYPE);

        // compute the secret key
        try {
             proofToken.setComputedKey(URI.create(WSTrustConstants.CK_PSHA1));
             key = SecurityUtil.P_SHA1(clientEntropyValue, key, keySize/8);
        } catch (Exception ex){
             throw new WSTrustException(ex.getMessage(), ex);
        }

        //==================
        // Create the RSTR 
        //==================

        // get Context
        URI ctx = null;
        try {
            String rstCtx = rst.getContext();
            if (rstCtx != null)
                ctx = new URI(rst.getContext());
        } catch (URISyntaxException ex) {
            throw new WSTrustException(ex.getMessage(), ex);
        }

        // Create RequestedSecurityToken with SAML assertion
        String assertionId = "uuid-" + UUID.randomUUID().toString();
         AppliesTo ap = rst.getAppliesTo();
         String appliesTo = null;
         if(ap != null){
                appliesTo = WSTrustUtil.getAppliesToURI(ap);
         }
        //Token samlToken = createSAMLAssertion(key, assertionId, appliesTo);
       // RequestedSecurityToken st = eleFac.createRequestedSecurityToken(samlToken);
         RequestedSecurityToken st = eleFac.createRequestedSecurityToken();

        // Create RequestedAttachedReference and RequestedUnattachedReference
        SecurityTokenReference samlReference = createSecurityTokenReference(assertionId);
        RequestedAttachedReference raRef =  eleFac.createRequestedAttachedReference(samlReference);
        RequestedUnattachedReference ruRef =  eleFac.createRequestedUnattachedReference(samlReference);

        // Create RequestedProofToken
     /*   BinarySecret keyBs = eleFac.createBinarySecret(key, BinarySecret.SYMMETRIC_KEY_TYPE);
        RequestedProofToken rpt = eleFac.createRequestedProofToken();
        rpt.setProofTokenType(RequestedProofToken.BINARY_SECRET_TYPE);
        rpt.setBinarySecret(keyBs);*/

        // Create Lifetime
        Lifetime lifetime = createLifetime();

        RequestSecurityTokenResponse rstr = 
                eleFac.createRSTRForIssue(rst.getTokenType(), ctx, st, ap, raRef, ruRef, proofToken, serverEntropy, lifetime);

        rstr.setKeySize(keySize);
        
        Token samlToken = createSAMLAssertion(key, assertionId, appliesTo);
        rstr.getRequestedSecurityToken().setToken(samlToken);
         
        // Populate IssuedTokenContext
        context.setSecurityToken(samlToken);
        context.setAttachedSecurityTokenReference(samlReference);
        context.setUnAttachedSecurityTokenReference(samlReference);
        context.setProofKey(key);
        context.setCreationTime(new Date(currentTime));
        context.setExpirationTime(new Date(currentTime + config.getIssuedTokenTimeout()));

        return rstr;   
    }

    /** Issue a Collection of Token(s) possibly for different scopes */
    public RequestSecurityTokenResponseCollection issueMultiple(
        RequestSecurityToken request, IssuedTokenContext context)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: issueMultiple");
   }

    /** Renew a Token */
    public RequestSecurityTokenResponse renew(
        RequestSecurityToken request, IssuedTokenContext context)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: renew");
   }

    /** Cancel a Token */
    public RequestSecurityTokenResponse cancel(
        RequestSecurityToken request, IssuedTokenContext context, Map issuedTokenContextMap)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: cancel");
    }

    /** Validate a Token */
    public RequestSecurityTokenResponse validate(
        RequestSecurityToken request, IssuedTokenContext context)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: validate");
    }

    /** 
     * handle an unsolicited RSTR like in the case of 
     * Client Initiated Secure Conversation.
     */
   public void handleUnsolicited(
       RequestSecurityTokenResponse rstr, IssuedTokenContext context)
           throws WSTrustException{
       throw new UnsupportedOperationException("Unsupported operation: handleUnsolicited");
   }

   /**
    * Contains Challenge
    * @return true if the RSTR contains a SignChallenge/BinaryExchange or
    *  some other custom challenge recognized by this implementation.
    */
   public boolean containsChallenge(RequestSecurityTokenResponse rstr){
       throw new UnsupportedOperationException("Unsupported operation: containsChallenge");
   }

   /**
    * Contains Challenge
    * @return true if the RST contains Initial Negotiation/Challenge information
    * for a Multi-Message exchange.
    */
   public boolean containsChallenge(RequestSecurityToken rst){
        throw new UnsupportedOperationException("Unsupported operation: containsChallenge");
   }

   protected abstract Token createSAMLAssertion(byte[] key, String assertionId, String appliesTo) throws WSTrustException;

   protected abstract boolean isAuthorized(Subject subject, String appliesTo, String tokenType);

   protected abstract Map getClaimedAttributes(Subject subject, String appliesTo, String tokenType);

 /*  protected byte[] createSecretKey(RequestSecurityToken rst)throws WSTrustException
   {
       // get key information
        int keySize = (int)rst.getKeySize();
        if (keySize < 1){
            keySize = DEFAULT_KEY_SIZE;
        }
        URI keyType = rst.getKeyType();
        URI alg = rst.getComputedKeyAlgorithm();

        Entropy entropy = rst.getEntropy();
        BinarySecret bs = entropy.getBinarySecret();
        byte[] nonce = bs.getRawValue();

        byte[] key = null;

        if (alg == null){
            key = nonce;
        } else if(alg.toString().equals(WSTrustConstants.CK_PSHA1)){
            try {
                  key = SecurityUtil.P_SHA1(nonce,null, keySize/8 );
                } catch (Exception ex) {
                      throw new WSTrustException(ex.getMessage(), ex);
                }
        } else {
            throw new WSTrustException("Unsupported key computation algorithm: " + alg.toString());
        } 

        return key;
   }*/

   private long currentTime;
   private Lifetime createLifetime()
   {
       Calendar c = new GregorianCalendar();
       int offset = c.get(Calendar.ZONE_OFFSET);
       if (c.getTimeZone().inDaylightTime(c.getTime())) 
       {
           offset += c.getTimeZone().getDSTSavings();
       }
       synchronized (calendarFormatter) {
           calendarFormatter.setTimeZone(c.getTimeZone());

           // always send UTC/GMT time
           long beforeTime = c.getTimeInMillis();
           currentTime = beforeTime - offset;
           c.setTimeInMillis(currentTime);

           AttributedDateTime created = new AttributedDateTime();
           created.setValue(calendarFormatter.format(c.getTime()));

           AttributedDateTime expires = new AttributedDateTime();
           c.setTimeInMillis(currentTime + config.getIssuedTokenTimeout());
           expires.setValue(calendarFormatter.format(c.getTime()));

           Lifetime lifetime = eleFac.createLifetime(created, expires);

           return lifetime;
        }
    }

    private SecurityTokenReference createSecurityTokenReference(String id){
       KeyIdentifier ref = eleFac.createKeyIdentifier(SAML_VALUE_TYPE, null);
       ref.setValue(id);
       return eleFac.createSecurityTokenReference(ref);
   }
}
