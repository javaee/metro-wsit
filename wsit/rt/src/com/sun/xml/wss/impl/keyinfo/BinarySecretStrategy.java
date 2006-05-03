/*
 * BinarySecretStrategy.java
 *
 * Created on January 4, 2006, 3:22 PM
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

import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Abhijit Das
 */
public class BinarySecretStrategy extends KeyInfoStrategy {
    
    private byte[] secret = null;
    
    protected static Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /**
     * Creates a new instance of BinarySecretStrategy
     */
    public BinarySecretStrategy() {
    }
    
    public BinarySecretStrategy(byte[] secret) {
        this.secret = secret;
    }

    public void insertKey(KeyInfoHeaderBlock keyInfo, SecurableSoapMessage secureMsg, String x509TokenId) throws XWSSecurityException {
       //TODO: need to rework this
       // keyInfo.addBinarySecret(secret);
    }

    public void insertKey(SecurityTokenReference tokenRef, SecurableSoapMessage secureMsg) throws XWSSecurityException {
        log.log(Level.SEVERE,
                "WSS0703.unsupported.operation"); 
        throw new UnsupportedOperationException(
            "A ds:BinarySecret can't be put under a wsse:SecurityTokenReference");
    }

    public void setCertificate(X509Certificate cert) {
        log.log(Level.SEVERE,
                "WSS0705.unsupported.operation");
        throw new UnsupportedOperationException(
            "Setting a certificate is not a supported operation for ds:BinarySecret strategy");
    }

    public String getAlias() {
        return Base64.encode(secret);
    }
    
    public void setSecret(byte[] secret) {
        this.secret = secret;
    }
    
}
