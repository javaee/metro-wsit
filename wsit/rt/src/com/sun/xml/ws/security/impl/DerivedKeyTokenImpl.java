/*
 * DerivedKeyTokenImpl.java
 *
 * Created on December 23, 2005, 7:11 PM
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

package com.sun.xml.ws.security.impl;

import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.xml.ws.security.SecurityTokenReference;
import com.sun.xml.ws.security.DerivedKeyToken;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Abhijit Das
 */
public class DerivedKeyTokenImpl implements DerivedKeyToken {
    
    private long length = 32; // Default length 
    private long offset = 0; // Default offset
    private long generation = 0;
    private String label = this.DEFAULT_DERIVEDKEYTOKEN_LABEL;
    private byte[] secret, nonce;
    
    /** Creates a new instance of DerivedKeyTokenImpl */
    public DerivedKeyTokenImpl(long offset, long length, byte[] secret){
        this.offset = offset;
        this.length = length;
        this.secret = secret;
        try {
            nonce = new byte[18];
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.nextBytes(nonce);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "No such algorithm found" + e.getMessage());
        }
    }

    public DerivedKeyTokenImpl(long offset, long length, byte[] secret, byte[] nonce){
        this.offset = offset;
        this.length = length;
        this.secret = secret;
        this.nonce = nonce;
    }
    
    public DerivedKeyTokenImpl(long generation, byte[] secret){
        this.generation = generation;
        this.secret = secret;
        try {
            nonce = new byte[18];
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.nextBytes(nonce);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "No such algorithm found" + e.getMessage());
        }
    }
    
    public URI getAlgorithm() {
        try {
            return new URI(this.DEFAULT_DERIVED_KEY_TOKEN_ALGORITHM);
        } catch (URISyntaxException ex) {
            //ignore
        }
        return null;
    }
    
    public long getLength() {
        return length;
    }
    
    public long getOffset() {
        return offset;
    }
    
    public String getType() {
        return this.DERIVED_KEY_TOKEN_TYPE;
    }
    
    public Object getTokenValue() {
        //TODO: implement this method
        return null;
    }
    
    public long getGeneration() {
        return generation;
    }
    
    public String getLabel(){
        return label;
    }
    
    public byte[] getNonce() {
        return nonce;
    }
    
    
    public SecretKey generateSymmetricKey(String algorithm) throws Exception{
        try {
            byte[] temp = label.getBytes("UTF-8");
            byte[] seed = new byte[temp.length + nonce.length];
            System.arraycopy(temp, 0, seed, 0, temp.length);
            System.arraycopy(nonce, 0, seed, temp.length, nonce.length);
            
            byte[] tempBytes = SecurityUtil.P_SHA1(secret, seed, (int)(offset + length));
            byte[] key = new byte[(int)length];
            
            for(int i = 0; i < key.length; i++)
            	key[i] = tempBytes[i+(int)offset];
            
            SecretKeySpec keySpec = new SecretKeySpec(key, algorithm); 
            return (SecretKey)keySpec;
        } catch (Exception ex) {
            throw new Exception(ex);
        }
    }

}
