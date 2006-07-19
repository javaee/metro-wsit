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

package com.sun.xml.wss.impl.misc;

import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import javax.xml.soap.SOAPElement;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;

import java.util.Random;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;

import java.net.URI;

import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.core.X509SecurityToken;
import com.sun.xml.ws.security.policy.WSSAssertion;

import com.sun.org.apache.xml.internal.security.algorithms.JCEMapper;

import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.IssuedTokenContext;

import com.sun.xml.wss.core.SecurityContextTokenImpl;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;

import com.sun.xml.ws.security.SecurityTokenReference;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.sun.xml.ws.security.trust.WSTrustConstants;

import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.wss.core.SecurityContextTokenImpl;

/**
 * Utility class for the Encryption and Signature related methods
 * @author Ashutosh Shahi
 */
public class SecurityUtil {
    
    protected static Logger log =  Logger.getLogger( LogDomainConstants.IMPL_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_CRYPTO_DOMAIN_BUNDLE);
    
    /** Creates a new instance of SecurityUtil */
    public SecurityUtil() {
    }
    
    public static SecretKey generateSymmetricKey(String algorithm) throws XWSSecurityException{
        try {
            
            //keyGen.init(168);//TODO-Venu
            String jceAlgo = JCEMapper.getJCEKeyAlgorithmFromURI(algorithm);
            //JCEMapper.translateURItoJCEID(algorithm);
            //
            if (MessageConstants.debug) {
                log.log(Level.FINEST, "JCE ALGORITHM "+ jceAlgo);
            }
            KeyGenerator keyGen = KeyGenerator.getInstance(jceAlgo);
            int length = 0;
            if(jceAlgo.startsWith("DES")){
                length = 168;
            }else{
                length = JCEMapper.getKeyLengthFromURI(algorithm);
            }
            keyGen.init(length);
            if (MessageConstants.debug) {
                log.log(Level.FINEST, "Algorithm key length "+length);
            }
            return keyGen.generateKey();
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    "WSS1208.failedto.generate.random.symmetrickey",
                    new Object[] {e.getMessage()});
                    throw new XWSSecurityException(
                            "Unable to Generate Symmetric Key", e);
        }
    }
    
    /**
     * Lookup method to get the Key Length based on algorithm
     * TODO: Not complete yet, need to add more algorithms
     * NOTE: This method should only be used for DerivedKeyTokenLengths
     **/
    public static int getLengthFromAlgorithm(String algorithm) throws XWSSecurityException{
        if(algorithm.equals(MessageConstants.AES_BLOCK_ENCRYPTION_192)){
            return 24;
        } else if(algorithm.equals(MessageConstants.AES_BLOCK_ENCRYPTION_256)){
            return 32;
        } else if(algorithm.equals(MessageConstants.AES_BLOCK_ENCRYPTION_128)){
            return 16;
        } else if (algorithm.equals(MessageConstants.TRIPLE_DES_BLOCK_ENCRYPTION)) {
            return 24;
        } else {
            throw new UnsupportedOperationException("TODO: not yet implemented keyLength for" + algorithm);
        }
    }
    
    public static String generateUUID() {
        Random rnd = new Random();
        int intRandom = rnd.nextInt();
        String id = "XWSSGID-"+String.valueOf(System.currentTimeMillis())+String.valueOf(intRandom);
        return id;
    }
    
    public static byte[] P_SHA1(byte[] secret, byte[] seed
            ) throws Exception {

        byte[] aBytes, result;
        aBytes = seed;
        
        Mac hMac = Mac.getInstance("HMACSHA1");        
        SecretKeySpec sKey = new SecretKeySpec(secret, "HMACSHA1");
        hMac.init(sKey);
        hMac.update(aBytes);
        aBytes = hMac.doFinal();
        hMac.reset();
        hMac.init(sKey);
        hMac.update(aBytes);
        hMac.update(seed);
        result = hMac.doFinal();
        
        return result;
    }
    
    public static byte[] P_SHA1(byte[] secret, byte[] seed,
                                 int requiredSize) throws Exception {
        Mac hMac = Mac.getInstance("HMACSHA1"); 
        SecretKeySpec sKey = new SecretKeySpec(secret, "HMACSHA1");
        
        byte[] result = new byte[requiredSize];
        int copied=0;
              
        byte[] aBytes = seed;
        hMac.init(sKey);
        hMac.update(aBytes);
        aBytes  = hMac.doFinal();
        
        int rounds = requiredSize/aBytes.length ;
        if(requiredSize % aBytes.length != 0)
            rounds++;
        
        for(int i = 0; i < rounds; i ++){

            hMac.reset();
            hMac.init(sKey);
            hMac.update(aBytes);
            hMac.update(seed);
            byte[] generated = hMac.doFinal();
            int takeBytes;
            if(i != rounds-1)
                takeBytes = generated.length;
            else
                takeBytes = requiredSize - (generated.length * i);
            System.arraycopy(generated, 0, result, copied, takeBytes);
            copied += takeBytes;
            hMac.init(sKey);
            hMac.update(aBytes);
            aBytes  = hMac.doFinal();
        }
        return result;
     }

     public static String getSecretKeyAlgorithm(String encryptionAlgo) {
         String encAlgo = JCEMapper.translateURItoJCEID(encryptionAlgo);
         if (encAlgo.startsWith("AES")) {
             return "AES";
         } else if (encAlgo.startsWith("DESede")) {
             return "DESede";
         } else if (encAlgo.startsWith("DES")) {
             return "DES";
         }
         return encAlgo;
     }
     
    public static void checkIncludeTokenPolicy(FilterProcessingContext context, 
            AuthenticationTokenPolicy.X509CertificateBinding certInfo, 
            String x509id) throws XWSSecurityException{
        
        HashMap insertedX509Cache = context.getInsertedX509Cache();
        X509SecurityToken x509Token = (X509SecurityToken)insertedX509Cache.get(x509id);
        SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();

        try{
            if(x509Token == null){                    
                Token policyToken = certInfo.getPolicyToken();
                if (policyToken == null) {
                    return;
                }

                if(Token.INCLUDE_ALWAYS_TO_RECIPIENT.equals(certInfo.getIncludeToken()) || 
                        Token.INCLUDE_ALWAYS.equals(certInfo.getIncludeToken())){
                    x509Token = new X509SecurityToken(secureMessage.getSOAPPart(),certInfo.getX509Certificate(), x509id);
                    secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(x509Token);
                    insertedX509Cache.put(x509id, x509Token);
                    certInfo.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                } else if(Token.INCLUDE_NEVER.equals(certInfo.getIncludeToken())){
                    WSSAssertion wssAssertion = context.getWSSAssertion();
                    if(MessageConstants.DIRECT_REFERENCE_TYPE.equals(certInfo.getReferenceType())){
                        if(wssAssertion != null){
                            if(wssAssertion.getRequiredProperties().contains(WSSAssertion.MUST_SUPPORT_REF_KEYIDENTIFIER))
                                certInfo.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                            else if(wssAssertion.getRequiredProperties().contains(WSSAssertion.MUSTSUPPORT_REF_THUMBPRINT))
                                 certInfo.setReferenceType(MessageConstants.THUMB_PRINT_TYPE);
                        } else {
                            // when wssAssertion is not set use KeyIdentifier
                            certInfo.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                        }
                    }
                } else if(Token.INCLUDE_ONCE.equals(certInfo.getIncludeToken())){
                    throw new UnsupportedOperationException(Token.INCLUDE_ONCE + " not supported yet as IncludeToken policy");
                }
            }
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
    }
    
     public static String  getWsuIdOrId(Element elem) throws XWSSecurityException {
        NamedNodeMap nmap = elem.getAttributes();
        Node attr = nmap.getNamedItem("Id");
        if (attr == null) {
            attr = nmap.getNamedItem("AssertionID");
            if (attr == null) {
                throw new XWSSecurityException("Issued Token Element does not have a Id or AssertionId attribute");
            }
        }
        return attr.getNodeValue();
    }

     public static void resolveSCT(FilterProcessingContext context, SecureConversationTokenKeyBinding sctBinding)
        throws XWSSecurityException {
        // resolve the ProofKey here and set it into ProcessingContext
        String sctPolicyId = sctBinding.getPolicyToken().getTokenId();
        // this will work on the client side only
        IssuedTokenContext ictx = context.getIssuedTokenContext(sctPolicyId);
        if (ictx == null) {
            // this will work on the server side
            SecurityContextTokenImpl sct = (SecurityContextTokenImpl)
            context.getExtraneousProperty(MessageConstants.INCOMING_SCT);
            if (sct == null) {
               throw new XWSSecurityException("SecureConversation Session Context not Found");
            }
            String sctId = sct.getSCId();
            ictx = context.getIssuedTokenContext(sctId);
        }
                                                                                                                                                           
        if (ictx == null) {
            throw new XWSSecurityException("SecureConversation Session Context not Found");
        } else {
            //System.out.println("SC Session located...");
        }
        //TODO: assuming only a single secure-conversation context
        context.setSecureConversationContext(ictx);
    }
                                                                                                                                                           
    public static void resolveIssuedToken(FilterProcessingContext context, IssuedTokenKeyBinding itkb) throws XWSSecurityException {
        //resolve the ProofKey here and set it into ProcessingContext
        String itPolicyId = itkb.getPolicyToken().getTokenId();
        // this will work on the client side only
        IssuedTokenContext ictx = context.getIssuedTokenContext(itPolicyId);
        if (ictx == null) {
            // on the server we have the TrustCredentialHolder
            ictx = context.getTrustCredentialHolder();
        }
                                                                                                                                                           
        if (ictx == null) {
            throw new XWSSecurityException("Trust IssuedToken not Found");
        } else {
            //System.out.println("Trust IssuedToken located...");
        }
        context.setTrustContext(ictx);
    }

    public static void initInferredIssuedTokenContext(FilterProcessingContext wssContext, SecurityTokenReference str, Key returnKey) throws XWSSecurityException {
         // make sure this is called only when its is a secret key
         /*
         IssuedTokenContextImpl ictx = (IssuedTokenContextImpl)wssContext.getTrustCredentialHolder();
         if (ictx == null) {
            if (!(returnKey instanceof javax.crypto.SecretKey)) {
                throw new XWSSecurityException("Internal Error while trying to initialize IssuedToken Context for Issued Token");
            }
            ictx = new IssuedTokenContextImpl();
            ictx.setProofKey(returnKey.getEncoded());
            ictx.setUnAttachedSecurityTokenReference(str);
            wssContext.setTrustCredentialHolder(ictx);
        } else {
            //make sure it is the same SAML assertion
            if (ictx.getProofKey() == null) {
                throw new RuntimeException("Internal Error when trying to construct IssuedToken context");            }
        }
        */
        // new code which fixes issues with Brokered Trust. Fix by Jiandong
        // need to understand why the i coded the else part above.
        IssuedTokenContextImpl ictx = (IssuedTokenContextImpl)wssContext.getTrustCredentialHolder();
        if (ictx == null) {
            ictx = new IssuedTokenContextImpl();
        }
        if (!(returnKey instanceof javax.crypto.SecretKey)) {
               throw new XWSSecurityException("Internal Error while trying to initialize IssuedToken Context for Issued Token");
        }
        ictx.setProofKey(returnKey.getEncoded());
        ictx.setUnAttachedSecurityTokenReference(str);
        wssContext.setTrustCredentialHolder(ictx);
    }

    public static boolean isEncryptedKey(SOAPElement elem) {

        if (MessageConstants.XENC_ENCRYPTED_KEY_LNAME.equals(elem.getLocalName()) &&
            MessageConstants.XENC_NS.equals(elem.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    public static boolean isBinarySecret(SOAPElement elem) {
        if (MessageConstants.BINARY_SECRET_LNAME.equals(elem.getLocalName()) &&
            WSTrustConstants.WST_NAMESPACE.equals(elem.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    public static SecurityContextTokenImpl locateBySCTId(FilterProcessingContext context, String sctId) throws XWSSecurityException {

        Hashtable contextMap = context.getIssuedTokenContextMap();

        if (contextMap == null) {
            // print a warning here
            //System.out.println("context.getIssuedTokenContextMap was null.........");
            return null;
        }

        Iterator it = contextMap.keySet().iterator();

        while (it.hasNext()) {
            String tokenId = (String)it.next();
            Object token = contextMap.get(tokenId);
            if (token instanceof IssuedTokenContext) {
                Object securityToken = ((IssuedTokenContext)token).getSecurityToken();
                if (securityToken instanceof SecurityContextToken) {
                    SecurityContextToken ret = (SecurityContextToken)securityToken;
                    if (sctId.equals(ret.getIdentifier().toString())) {
                        return new SecurityContextTokenImpl(
                            context.getSOAPMessage().getSOAPPart(), ret.getIdentifier().toString(), ret.getInstance(), ret.getWsuId(), ret.getExtElements());
                    }
                }
            }
        }
        return null;
    }

    public static void updateSamlVsKeyCache(SecurityTokenReference str, FilterProcessingContext ctx, Key symKey) {
         com.sun.xml.wss.core.ReferenceElement ref = ((com.sun.xml.wss.core.SecurityTokenReference)str).getReference();
         if (ref instanceof com.sun.xml.wss.core.reference.KeyIdentifier) {
            String assertionId = ((com.sun.xml.wss.core.reference.KeyIdentifier)ref).getReferenceValue();
            if (ctx.getSamlIdVSKeyCache().get(assertionId) == null) {
                ctx.getSamlIdVSKeyCache().put(assertionId, symKey);
            }
         }
    }

}
