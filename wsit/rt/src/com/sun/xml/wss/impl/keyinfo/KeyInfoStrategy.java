/*
 * $Id: KeyInfoStrategy.java,v 1.1 2006-05-03 22:57:49 arungupta Exp $
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

package com.sun.xml.wss.impl.keyinfo;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;

import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.KeyInfoHeaderBlock;

import java.security.cert.X509Certificate;

/**
 * The interface for different KeyInfo Schemes
 * @author XWS Security team
 * @author K.Venugopal@sun.com
 */
public abstract class KeyInfoStrategy {
    
    public static KeyInfoStrategy getInstance(String strategy) {
        //TODO: For now.
        if(MessageConstants.KEY_INDETIFIER_TYPE == strategy || MessageConstants.KEY_INDETIFIER_TYPE.equals(strategy)){
            return new KeyIdentifierStrategy();
        }else if(MessageConstants.THUMB_PRINT_TYPE == strategy || MessageConstants.THUMB_PRINT_TYPE.equals(strategy)){
            return new KeyIdentifierStrategy(KeyIdentifierStrategy.THUMBPRINT);
        }else if(MessageConstants.EK_SHA1_TYPE == strategy || MessageConstants.EK_SHA1_TYPE.equals(strategy)){
            return new KeyIdentifierStrategy(KeyIdentifierStrategy.ENCRYPTEDKEYSHA1);
        }else if(MessageConstants.KEY_NAME_TYPE == strategy || MessageConstants.KEY_NAME_TYPE.equals(strategy)){
            return new KeyNameStrategy();
        }else if(MessageConstants.DIRECT_REFERENCE_TYPE == strategy || MessageConstants.DIRECT_REFERENCE_TYPE.equals(strategy)){
            return new DirectReferenceStrategy();
        }else if(MessageConstants.X509_ISSUER_TYPE == strategy || MessageConstants.X509_ISSUER_TYPE.equals(strategy)){
            return new X509IssuerSerialStrategy();
        }else if (MessageConstants.BINARY_SECRET == strategy || MessageConstants.BINARY_SECRET.equals(strategy)) {
            return new BinarySecretStrategy();
        }
        return null;
    }
    
    /**
     * insert the Key Information into a ds:KeyInfo using the
     * appropriate scheme
     *
     * @param keyInfo
     *    the KeyInfo block into which the Key Information has to be inserted.
     * @param secureMsg the SecurableSoapMessage
     * @param x509TokenId value of the <xwss:X509Token>/@id in config file
     * @throws XWSSecurityException
     *     if there was a problem in inserting the key information
     */
    public abstract void insertKey(KeyInfoHeaderBlock keyInfo,SecurableSoapMessage secureMsg,
    String x509TokenId)throws XWSSecurityException;
    
    /**
     * insert the Key Information into a SecurityTokenReference using the
     * appropriate scheme
     *
     * @param tokenRef
     *    the SecurityTokenReference into which the Key Information
     *    has to be inserted.
     * @param secureMsg the SecurableSoapMessage
     * @throws XWSSecurityException
     *     if there was a problem in inserting the key information
     */
    public abstract void insertKey(SecurityTokenReference tokenRef, SecurableSoapMessage secureMsg)
    throws XWSSecurityException;
    
    /**
     * Sets the certificate corresponding to the security operation
     */
    public abstract void setCertificate(X509Certificate cert);
    
    public abstract String getAlias();
}
