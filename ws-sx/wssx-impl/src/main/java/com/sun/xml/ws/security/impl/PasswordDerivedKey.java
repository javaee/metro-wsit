/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 *
 * @author suresh Created:22-Dec-2008.
 */
package com.sun.xml.ws.security.impl;


import java.io.UnsupportedEncodingException;
import java.security.*;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;

import java.util.Random;

import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.ws.security.opt.crypto.dsig.internal.HmacSHA1;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;


public class PasswordDerivedKey {

    private byte[] salt=null;       
    private final int keylength = 160;    
    private byte[] sign = null;

    private byte[] generateRandomSaltof15Bytes() {
      
        Random random = new Random();
        byte[] randomSalt = new byte[15];
        random.nextBytes(randomSalt);
        return randomSalt;
    }

    private void generate16ByteSalt() {
        salt = new byte[16];
        salt[0] = 0;
        byte[] temp = generateRandomSaltof15Bytes();
        for (int i = 1; i < 16; i++) {
            salt[i] = temp[i-1];
        }
    }

    public  byte[] generate160BitKey(String password, int iteration, byte[] reqsalt)
            throws UnsupportedEncodingException {

        String saltencode = Base64.encode(reqsalt);

        byte[] keyof160bits = new byte[20];
        byte[] temp = password.getBytes();
        byte[] temp1 = saltencode.getBytes();
        byte[] input = new byte[temp1.length + temp.length];

        System.arraycopy(temp, 0, input, 0, temp.length);
        System.arraycopy(temp1, 0, input, temp.length, temp1.length);
        
        MessageDigest md = null;
                
        try {
            md = java.security.MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        md.reset();
        md.update(input);
        keyof160bits = md.digest();

        for (int i = 2; i <= iteration; i++) {
            md.reset();
            md.update(keyof160bits);
            keyof160bits = md.digest();

        }        
        return keyof160bits;

    }
    public SecretKey generate16ByteKeyforEncryption(byte[] keyof20Bytes){
        byte[] keyof16Bytes = new byte[16];
        for(int i=0;i<16;i++)
            keyof16Bytes[i] = keyof20Bytes[i];
        AuthenticationTokenPolicy.UsernameTokenBinding untBinding = new AuthenticationTokenPolicy.UsernameTokenBinding();
        untBinding.setSecretKey(keyof16Bytes);
        SecretKey sKey = untBinding.getSecretKey(SecurityUtil.getSecretKeyAlgorithm(MessageConstants.AES_BLOCK_ENCRYPTION_128));
        //untBinding.setSecretKey(sKey);
        return sKey;
    }

    public SecretKey generateDerivedKeyforEncryption(String password, String algorithm, int iteration)
            throws UnsupportedEncodingException {

        SecretKey keySpec = null;
        byte[] reqsalt = new byte[16];
        byte[] keyof128length = new byte[16];
        byte[] keyof160bits = new byte[20];
        if (salt == null) {
            salt=new byte[16];
            generate16ByteSalt();

        }
        reqsalt[0] = 02;
        for (int i = 1; i < 16; i++) {
            reqsalt[i] = salt[i];
        }

        keyof160bits = generate160BitKey(password, iteration, reqsalt);
        for (int i = 0; i < 16; i++) {
            keyof128length[i] = keyof160bits[i];
        }

        if (testAlgorithm(algorithm)) {
            keySpec = new SecretKeySpec(keyof128length, algorithm);
        } else {
            throw new RuntimeException("This Derived Key procedure doesnot support " +algorithm);
        }
        return (SecretKey) keySpec;


    }

    public byte[] generateMAC(byte[] data, String password, int iteration)
            throws InvalidKeyException, SignatureException, UnsupportedEncodingException {

        SecretKey keySpec = null;
        byte[] reqsalt = new byte[16];
        byte[] keyof160bits = new byte[20];
        if (salt == null) {
            salt=new byte[16];
            generate16ByteSalt();
        }
        reqsalt[0] = 01;
        for (int i = 1; i < 16; i++) {
            reqsalt[i] = salt[i];
        }
        keyof160bits = generate160BitKey(password, iteration, reqsalt);
        keySpec = new SecretKeySpec(keyof160bits, "AES");

        HmacSHA1 mac = new HmacSHA1();
        mac.init((Key) keySpec, keylength);
        mac.update(data);

        byte[] signature = mac.sign();

        return signature;

    }

    public byte[] get16ByteSalt() {
        generate16ByteSalt();
        return salt;
    }

    public SecretKey verifyEncryptionKey(String password, int iterate, byte[] receivedSalt) throws UnsupportedEncodingException {

        byte[] keyof160bits = new byte[20];
        receivedSalt[0]=02;
        keyof160bits = generate160BitKey(password, iterate, receivedSalt);
        byte[] keyof128length = new byte[16];
        for (int i = 0; i < 16; i++) {
            keyof128length[i] = keyof160bits[i];
        }
        SecretKey keySpec = new SecretKeySpec(keyof128length, "AES");
        return keySpec;

    }

    public boolean verifyMACSignature(byte[] receivedSignature,byte[] data,String password, int iterate, byte[] receivedsalt) throws UnsupportedEncodingException, InvalidKeyException, SignatureException {
        
        receivedsalt[0]=01;
        byte[] keyof160bits = generate160BitKey(password, iterate, receivedsalt);
        SecretKey keySpec = new SecretKeySpec(keyof160bits, "AES");

        HmacSHA1 mac = new HmacSHA1();
        mac.init(keySpec, keylength);
        mac.update(data);

        byte[] signature = mac.sign();
        return MessageDigest.isEqual(receivedSignature,signature);

    }
    

    public boolean testAlgorithm(String algo) {

        if (algo.equalsIgnoreCase("AES") || algo.equalsIgnoreCase("Aes128")||algo.startsWith("A")||algo.startsWith("a")) {
            return true;
        } else {

            return false;

        }
    }
}
