/*
 * DerivedKeyTokenImpl.java
 *
 * Created on December 23, 2005, 7:11 PM
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

package com.sun.xml.ws.security.impl;

import com.sun.xml.ws.api.security.DerivedKeyToken;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Ashutosh Shahi
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
    
    public DerivedKeyTokenImpl(long offset, long length, byte[] secret, byte[] nonce, String label){
        this.offset = offset;
        this.length = length;
        this.secret = secret;
        this.nonce = nonce;
        if(label != null){
            this.label = label;
        }
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
    
    
    public SecretKey generateSymmetricKey(String algorithm) 
        throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        
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
       
    }

}
