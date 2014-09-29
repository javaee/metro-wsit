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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.xml.wss.impl.filter;

import org.apache.xml.security.exceptions.Base64DecodingException;
import com.sun.xml.ws.security.impl.PasswordDerivedKey;
import com.sun.xml.ws.security.secext10.AttributedString;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;

import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy.UsernameTokenBinding;
import com.sun.xml.ws.security.opt.impl.tokens.UsernameToken;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.impl.filter.LogStringsMessages;
/**
 *
 * @author suresh
 */
public class UsernameTokenDataResolver {

    private static final Logger log = Logger.getLogger(
            LogDomainConstants.IMPL_FILTER_DOMAIN,
            LogDomainConstants.IMPL_FILTER_DOMAIN_BUNDLE);
     /**
      * sets the values of Salt, Iterations , username for UsernameToken,
      * generates 160 bit key for signature and sets it in UsernameToken Binding
      * @param context FilterProcessingContext
      * @param unToken UsernameToken
      * @param policy SignaturePolicy
      * @param untBinding UsernameTokenBinding
      * @param firstByte int
      * @return untBinding  UsernameTokenBinding
      * @throws com.sun.xml.wss.XWSSecurityException
      * @throws java.io.UnsupportedEncodingException
      */
     public static UsernameTokenBinding setSaltandIterationsforUsernameToken(
            FilterProcessingContext context, UsernameToken unToken,
            SignaturePolicy policy,UsernameTokenBinding untBinding, int firstByte) throws XWSSecurityException, UnsupportedEncodingException {
            //Sets Salt and Iterations in UsernameToken;
            int iterations ;
            if(context.getiterationsForPDK() != 0){
                iterations = context.getiterationsForPDK();
            }else {
                iterations = MessageConstants.DEFAULT_VALUEOF_ITERATIONS;
            }
            if(iterations < 1000){
                iterations = MessageConstants.DEFAULT_VALUEOF_ITERATIONS;
            }
            byte[] macSignature = null;
            PasswordDerivedKey pdk = new PasswordDerivedKey();
            //Setting username in unToken ;
            String userName = unToken.getUsernameValue();
            if (userName == null || "".equals(userName)) {
                userName = context.getSecurityEnvironment().getUsername(context.getExtraneousProperties());
            }            
            if (userName == null || "".equals(userName)) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_1409_INVALID_USERNAME_TOKEN());
                throw new XWSSecurityException("Username has not been set");
            }
            unToken.setUsernameValue(userName);
            String password = untBinding.getPassword();
            if (!untBinding.hasNoPassword() && (password == null || "".equals(password))) {
                password = context.getSecurityEnvironment().getPassword(context.getExtraneousProperties());
            }
            if (!untBinding.hasNoPassword()) {
                if (password == null) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_1424_INVALID_USERNAME_TOKEN());
                    throw new XWSSecurityException("Password for the username has not been set");
                }
            }
            //Setting iterations in UsernameToken;
            AttributedString as =  new AttributedString();
            String iterate = Integer.toString(iterations);
            as.setValue(iterate);
            unToken.setIteration(as);
           
            byte[] salt = null;
            if (unToken.getSalt() == null) {
                 //Setting Salt in UsernameToken ;
                salt = pdk.get16ByteSalt();                
                AttributedString aString = new AttributedString();
                aString.setValue(Base64.encode(salt));
                unToken.setSalt(aString);
            } else {
                //Retrieving the salt already there in unToken;
                String decodeString = unToken.getSalt().getValue();
                String  iter = unToken.getIteration().getValue();
                iterations = Integer.parseInt(iter);
                try {
                    salt = Base64.decode(decodeString);
                } catch (Base64DecodingException ex) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_1426_BASE_64_DECODING_ERROR(), ex);
                    throw new UnsupportedEncodingException("error while decoding the salt in username token");
                }
            }
            //salt[0] = MessageConstants.VALUE_FOR_SIGNATURE;
            salt[0] = (byte) firstByte;
            try {
                macSignature = pdk.generate160BitKey(password, iterations, salt);
            } catch (UnsupportedEncodingException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_1425_UNSUPPORTED_ENCODING(), ex);
                throw new UnsupportedEncodingException("error while creating 160 bit key");
            }
            untBinding.setSecretKey(macSignature);        
        return untBinding;
    }
    /**
     * sets the values of salt, iterations and username in UsernameToken,
     * generates 128 bit key for encryption and sets it in username token binding
     * @param context FilterProcessingContext
     * @param unToken UsernameToken
     * @param policy EncryptionPolicy
     * @param untBinding UsernameTokenBinding
     * @return untBinding  AuthenticationTokenPolicy.UsernameTokenBinding
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public static AuthenticationTokenPolicy.UsernameTokenBinding setSaltandIterationsforUsernameToken(
    FilterProcessingContext context, UsernameToken unToken,
    EncryptionPolicy policy,UsernameTokenBinding untBinding) throws XWSSecurityException, UnsupportedEncodingException {
        //Setting Iterations for UsernameToken ;
        int iterations;
        if (context.getiterationsForPDK() != 0) {
            iterations = context.getiterationsForPDK();
        } else {
            iterations = MessageConstants.DEFAULT_VALUEOF_ITERATIONS;
        }
        if(iterations < 1000){
                iterations = MessageConstants.DEFAULT_VALUEOF_ITERATIONS;
            }
        byte[] keyof128bits = new byte[16];
        byte[] encSignature = null;
        //Setting username for UsernameToken ;
        String userName = unToken.getUsernameValue();
        if (userName == null || "".equals(userName)) {
            userName = context.getSecurityEnvironment().getUsername(context.getExtraneousProperties());
        }
        if (userName == null || "".equals(userName)) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_1409_INVALID_USERNAME_TOKEN());
            throw new XWSSecurityException("Username has not been set");
        }
        unToken.setUsernameValue(userName);
        //Retrieving password ;
        String password = untBinding.getPassword();
        if (!untBinding.hasNoPassword() && (password == null || "".equals(password))) {
            password = context.getSecurityEnvironment().getPassword(context.getExtraneousProperties());
        }
        if (!untBinding.hasNoPassword()) {
            if (password == null) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_1424_INVALID_USERNAME_TOKEN());
                throw new XWSSecurityException("Password for the username has not been set");
            }
        }
        //Setting iterations in UsernameToken;
        AttributedString as =  new AttributedString();
        String iterate = Integer.toString(iterations);
        as.setValue(iterate);
        unToken.setIteration(as);
        PasswordDerivedKey pdk = new PasswordDerivedKey();
        byte[] salt = null;
        if (unToken.getSalt() == null) {
            // Setting the Salt in unToken first time;
            salt = pdk.get16ByteSalt();
            AttributedString atbs =  new AttributedString();
            atbs.setValue(Base64.encode(salt));
            unToken.setSalt(atbs);
        } else {
            //Retrieving the salt already there in unToken;
            String decodeString = unToken.getSalt().getValue();
            String  iter = unToken.getIteration().getValue();
            iterations = Integer.parseInt(iter);
            try {
                salt = Base64.decode(decodeString);
            } catch (Base64DecodingException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_1426_BASE_64_DECODING_ERROR(), ex);
                throw new UnsupportedEncodingException("error while decoding the salt in username token");
            }
        }
        salt[0] = MessageConstants.VALUE_FOR_ENCRYPTION;
        try {
            encSignature = pdk.generate160BitKey(password, iterations, salt);
        } catch (UnsupportedEncodingException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_1425_UNSUPPORTED_ENCODING(), ex);
            throw new UnsupportedEncodingException("error while creating 128 bit key");
        }
        for (int i = 0; i < 16; i++) {
            keyof128bits[i] = encSignature[i];
        }
        untBinding.setSecretKey(keyof128bits);
        return untBinding;
  }
  /**
   * sets username and password in usernametoken
   * @param context FilterProcessingContext
   * @param token com.sun.xml.wss.core.UsernameToken
   * @param unToken UsernameToken
   * @param policy AuthenticationTokenPolicy
   * @return UsernameTokenBinding
   * @throws com.sun.xml.wss.XWSSecurityException
   */
   // currently we are not using this method
   public static AuthenticationTokenPolicy.UsernameTokenBinding resolveUsernameToken(
            FilterProcessingContext context, com.sun.xml.wss.core.UsernameToken token, UsernameToken unToken,
            AuthenticationTokenPolicy policy) throws XWSSecurityException {

        UsernameTokenBinding userNamePolicy =
                (UsernameTokenBinding) policy.getFeatureBinding();

            String userName = userNamePolicy.getUsername();
            String password = userNamePolicy.getPassword();

            if (userName == null || "".equals(userName)) {
                userName = context.getSecurityEnvironment().getUsername(context.getExtraneousProperties());
            }
            if (userName == null || "".equals(userName)) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_1409_INVALID_USERNAME_TOKEN());
                throw new XWSSecurityException("Username has not been set");
            }
            if (token != null) {
            token.setUsername(userName);
            } else {
            unToken.setUsernameValue(userName);
            }     
            if (!userNamePolicy.hasNoPassword() && (password == null || "".equals(password))) {
                password = context.getSecurityEnvironment().getPassword(context.getExtraneousProperties());
            }
            if (!userNamePolicy.hasNoPassword()) {
                if (password == null) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_1424_INVALID_USERNAME_TOKEN());
                    throw new XWSSecurityException("Password for the username has not been set");
                }
                if (token != null) {
                token.setPassword(password);
                } else {
                unToken.setPasswordValue(password);
                }              
            }       
        return userNamePolicy;
    }
}


