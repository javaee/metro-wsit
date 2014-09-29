/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.security.impl;

import com.sun.xml.ws.security.SecurityContextTokenInfo;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.Token;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.encryption.XMLCipher;
import com.sun.xml.wss.XWSSecurityException;
import java.net.URI;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.security.KeyPair;

import java.util.Date;

import java.util.HashMap;
import java.util.Map;
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
    ArrayList<Object> securityPolicies = new ArrayList<Object>();
    Object otherPartyEntropy = null;
    Object selfEntropy = null;
    URI computedKeyAlgorithm;
    String sigAlgorithm;
    String encAlgorithm;
    String canonicalizationAlgorithm;
    String signWith;
    String encryptWith;
    byte[] proofKey = null; // used in SecureConversation
    SecurityContextTokenInfo sctInfo = null; // used in SecureConversation
    Date creationTime = null;
    Date expiryTime = null;
    String username = null;
    String endPointAddress = null;
    Subject subject;
    KeyPair proofKeyPair;
    String authType = null;
    String tokenType = null;
    String keyType = null;
    String tokenIssuer = null;
    Token target = null;
    
    Map<String, Object> otherProps = new HashMap<String, Object>();
    
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
    
    public ArrayList<Object> getSecurityPolicy() {
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
    
    public void setProofKeyPair(KeyPair keys){
        this.proofKeyPair = keys;
    }
   
    public KeyPair getProofKeyPair(){
        return this.proofKeyPair;
    }
    
    public void setAuthnContextClass(String authType){
        this.authType = authType;
    }
   
    public String getAuthnContextClass(){
        return this.authType;
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
    
    public SecurityContextTokenInfo getSecurityContextTokenInfo() {
        return sctInfo;
    }
    
    public void setSecurityContextTokenInfo(SecurityContextTokenInfo sctInfo) {
        this.sctInfo = sctInfo;
    }

    public Map<String, Object> getOtherProperties() {
        return this.otherProps;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setAppliesTo(String appliesTo) {
        this.endPointAddress = appliesTo;
    }

    public String getAppliesTo() {
        return endPointAddress;
    }

    public void setTokenIssuer(String issuer) {
        this.tokenIssuer = issuer;
    }

    public String getTokenIssuer() {
        return tokenIssuer;
    }
    
    public void setSignatureAlgorithm(String sigAlg){
        this.sigAlgorithm = sigAlg;
    }
    
    public String getSignatureAlgorithm(){
        return sigAlgorithm;
    }
    
    public void setEncryptionAlgorithm(String encAlg){
        this.encAlgorithm = encAlg;
    }
    
    public String getEncryptionAlgorithm(){
        return encAlgorithm;
    }
    
    public void setCanonicalizationAlgorithm(String canonAlg){
        this.canonicalizationAlgorithm = canonAlg;
    }
    
    public String getCanonicalizationAlgorithm(){
        return canonicalizationAlgorithm;
    }
    
    public void setSignWith(String signWithAlgo){
        this.signWith = signWithAlgo;
    }
    
    public String getSignWith(){
        return signWith;
    }    
    
    public void setEncryptWith(String encryptWithAlgo){
        this.encryptWith = encryptWithAlgo;
    }
    
    public String getEncryptWith(){
        return encryptWith;
    }

    public void setTarget(Token target) {
        this.target = target;
    }

    public Token getTarget() {
        return target;
    }
}
