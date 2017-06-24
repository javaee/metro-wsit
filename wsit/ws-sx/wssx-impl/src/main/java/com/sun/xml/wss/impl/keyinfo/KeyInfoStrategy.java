/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * $Id: KeyInfoStrategy.java,v 1.2 2010-10-21 15:37:29 snajper Exp $
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
     * @param x509TokenId value of the &lt;xwss:X509Token&gt;/@id in config file
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
