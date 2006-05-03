/*
 * $Id: PasswordValidationCallback.java,v 1.1 2006-05-03 22:57:43 arungupta Exp $
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

package com.sun.xml.wss.impl.callback;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import javax.security.auth.callback.*;

import com.sun.xml.wss.impl.misc.Base64;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.SecurityTokenException;
import com.sun.xml.wss.logging.LogDomainConstants;

/**
 * This Callback is intended for Username-Password validation.
 * A validator that implements the PasswordValidator interface 
 * should be set on the callback by the callback handler.
 *
 * <p>Note: A validator for WSS Digested Username-Password is provided 
 * as part of this callback.
 *
 * @author XWS-Security Team
 */
public class PasswordValidationCallback extends XWSSCallback implements Callback {

    private Request  request;
    private boolean result = false;
    private PasswordValidator validator;

    public PasswordValidationCallback(Request request) {
        this.request = request;
    }

    public boolean getResult() {
        try {
            if (validator != null)
                result = validator.validate(request);
        } catch (Exception e) {
            return false;
        }
        return result;
    }

    public Request getRequest() {
        return request;
    }

    /**
     * This method must be invoked by the CallbackHandler while handling
     * this callback.
     */
    public void setValidator(PasswordValidator validator) {
        this.validator = validator;
    }


    public static interface Request {
    }

    /**
     * Represents a validation request when the password in the username token
     * is in plain text.
     */
    public static class PlainTextPasswordRequest implements Request {

        private String password;
        private String userName;

        /**
         * Constructor.
         *
         * @param userName <code>java.lang.String</code> representation of User name.
         * @param password <code>java.lang.String</code> representation of password.
         */
        public PlainTextPasswordRequest(String userName, String password) {
            this.password = password;
            this.userName = userName;
        }

        /**
         * Get the username stored in this Request.
         *
         * @return <code>java.lang.String</code> representation of username.
         */
        public String getUsername() {
            return userName;
        }

        /**
         * Get the password stored in the Request.
         *
         * @return <code>java.lang.String</code> representation of password.
         */
        public String getPassword() {
            return password;
        }

    }

    /**
     * Represents a validation request when the password in the username token
     * is in digested form.
     */
    public static class DigestPasswordRequest implements Request {

        private String password;
        private String userName;
        private String digest;
        private String nonce;
        private String created;

        /**
         * Constructor.
         *
         * @param userName <code>java.lang.String</code> representing Username.
         * @param digest <code>java.lang.String</code> Base64 encoded form of Digested Password.
         * @param nonce <code>java.lang.String</code> representation of unique Nonce 
         * used for calculating Digested password.
         * @param created <code>java.security.String</code> representation of created time
         * used for password digest calculation.
         *
         */
        public DigestPasswordRequest(
            String userName,
            String digest,
            String nonce,
            String created) {

            this.userName = userName;
            this.digest = digest;
            this.nonce = nonce;
            this.created = created;
        }

        /**
         * This method must be invoked by the CallbackHandler while handling
         * Callback initialized with DigestPasswordRequest.
         */
        public void setPassword(String password) {
            this.password = password;
        }

        public String getPassword() {
            return password;
        }

        public String getUsername() {
            return userName;
        }

        public String getDigest() {
            return digest;
        }

        public String getNonce() {
            return nonce;
        }

        public String getCreated() {
            return created;
        }

    }

    /**
     * Interface for validating password.
     */
    public static interface PasswordValidator {
        
        /**
         * @param request PasswordValidationRequest
         * @return true if password validation succeeds else false
         */
        public boolean validate(Request request) throws PasswordValidationException;
    }

    /**
     * Implements WSS digest Password Validation.
     * The method to compute password digest is described in http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0.pdf
     */
    public static class DigestPasswordValidator implements PasswordValidator {

         public boolean validate(Request request) throws PasswordValidationException {

             DigestPasswordRequest req = (DigestPasswordRequest)request;
             String passwd = req.getPassword();
             String nonce = req.getNonce();
             String created = req.getCreated();
             String passwordDigest = req.getDigest();
             String username = req.getUsername();

             if (null == passwd)
               return false;
              byte[] decodedNonce = null;
              if (null != nonce) {
                  try {
                      decodedNonce = Base64.decode(nonce);
                  } catch (Base64DecodingException bde) {
                      throw new PasswordValidationException(bde);
                  }
              }
              String utf8String = "";
              if (created != null) {
                  utf8String += created;
              }
              utf8String += passwd;
              byte[] utf8Bytes;
              try {
                  utf8Bytes = utf8String.getBytes("utf-8");
              } catch (UnsupportedEncodingException uee) {
                  throw new PasswordValidationException(uee);
              }

              byte[] bytesToHash;
              if (decodedNonce != null) {
                  bytesToHash = new byte[utf8Bytes.length + decodedNonce.length];
                  for (int i = 0; i < decodedNonce.length; i++)
                      bytesToHash[i] = decodedNonce[i];
                  for (int i = decodedNonce.length;
                       i < utf8Bytes.length + decodedNonce.length;
                       i++)
                      bytesToHash[i] = utf8Bytes[i - decodedNonce.length];
              } else {
                  bytesToHash = utf8Bytes;
              }
              byte[] hash;
              try {
                  MessageDigest sha = MessageDigest.getInstance("SHA-1");
                  hash = sha.digest(bytesToHash);
              } catch (Exception e) {
                  throw new PasswordValidationException(
                      "Password Digest could not be created" + e);
              }
              return (passwordDigest.equals(Base64.encode(hash)));
         }    
    }

    public static class PasswordValidationException extends Exception {

        public PasswordValidationException(String message) {
            super(message);
        }

        public PasswordValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    
        public PasswordValidationException(Throwable cause) {
            super(cause);
        }
    }
}
