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
 * IssuedTokenContextImpl.java
 *
 * Created on December 14, 2005, 3:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.impl;

import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLEncryptionException;
import com.sun.xml.ws.security.*;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.wss.XWSSecurityException;
import java.net.URI;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;

import java.util.Date;

import javax.security.auth.Subject;

/**
 *
 * @author Abhijit Das
 */
public class IssuedTokenContextImpl implements IssuedTokenContext {
    
    
    X509Certificate x509Certificate = null;
    Token securityToken = null;
    Token associatedProofToken = null;
    Token secTokenReference = null;
    Token unAttachedSecTokenReference = null;
    ArrayList securityPolicies = null;
    Object otherPartyEntropy = null;
    Object selfEntropy = null;
    URI computedKeyAlgorithm;
    byte[] proofKey = null; // used in secureConversation
    Date creationTime = null;
    Date expiryTime = null;    
    String username = null;
    String endPointAddress = null;
    Subject subject;
    
    public X509Certificate getRequestorCertificate() {
        return x509Certificate;
    }
    
    
    
    public void setRequestorCertificate(X509Certificate cert) {
        this.x509Certificate = cert;
    }
    
     public Subject getRequestorSubject(){
        return subject;
    }

    public void setRequestorSubject(Subject subject){
        this.subject = subject;
    }

    public String getRequestorUsername() {
        return username;
    }
                                                                                                                                  
    public void setRequestorUsername(String username) {
        this.username = username;
    }

    
    public void setSecurityToken(Token securityToken) {
        this.securityToken = securityToken;
    }
    
    public Token getSecurityToken() {
        return securityToken;
    }
    
    public void setAssociatedProofToken(Token associatedProofToken) {
        this.associatedProofToken = associatedProofToken;
    }
    
    public Token getAssociatedProofToken() {
        return associatedProofToken;
    }
    
    public Token getAttachedSecurityTokenReference() {
        return secTokenReference;
    }
    
    public void setAttachedSecurityTokenReference(Token secTokenReference) {
        this.secTokenReference = secTokenReference;
    }
    
    public Token getUnAttachedSecurityTokenReference() {
        return unAttachedSecTokenReference;
    }
    
    public void setUnAttachedSecurityTokenReference(Token secTokenReference) {
        this.unAttachedSecTokenReference = secTokenReference;
    }
    
    public ArrayList getSecurityPolicy() {
        return securityPolicies;
    }
    
    public void setOtherPartyEntropy(Object otherPartyEntropy) {
        this.otherPartyEntropy = otherPartyEntropy;
    }
    
    public Object getOtherPartyEntropy() {
        return otherPartyEntropy;
    }
    
    public Key getDecipheredOtherPartyEntropy(Key privKey) throws XWSSecurityException {
        try {
            return getDecipheredOtherPartyEntropy(getOtherPartyEntropy(), privKey);
        } catch ( XMLEncryptionException xee) {
            throw new XWSSecurityException(xee);
        }
    }
    
    
    
    private Key getDecipheredOtherPartyEntropy(Object encryptedKey, Key privKey) throws XMLEncryptionException {
        if ( encryptedKey instanceof EncryptedKey ) {
            EncryptedKey encKey = (EncryptedKey)encryptedKey;
            XMLCipher cipher = XMLCipher.getInstance();
            cipher.setKEK(privKey);
            cipher.decryptKey(encKey);
            return null;
        } else {
            return null;
        }
    }
    
    public void setSelfEntropy(Object selfEntropy) {
        this.selfEntropy = selfEntropy;
    }
    
    public Object getSelfEntropy() {
        return selfEntropy;
    }
    
    
    public URI getComputedKeyAlgorithmFromProofToken() {
        return computedKeyAlgorithm;
    }
    
    public void setComputedKeyAlgorithmFromProofToken(URI computedKeyAlgorithm) {
        this.computedKeyAlgorithm = computedKeyAlgorithm;
    }
    
    public void setProofKey(byte[] key){
        this.proofKey = key;
    }
    
    public byte[] getProofKey() {
        return proofKey;
    }

    public Date getCreationTime() {
        return creationTime;
    }
                                                                                                                                     
    public Date getExpirationTime() {
        return expiryTime;
    }
                                                                                                                                     
    public void setCreationTime(Date date) {
        creationTime = date;
    }
                                                                                                                                     
    public void  setExpirationTime(Date date) {
        expiryTime = date;
    }
    
     /**
     * set the endpointaddress
     */
     public void  setEndpointAddress(String endPointAddress){
         this.endPointAddress = endPointAddress;
     }
     
      /**
     *get the endpoint address
     */
     public String getEndpointAddress(){
         return this.endPointAddress;
     }
                                                                                                                                     
    public void destroy() {

    }
 
}
