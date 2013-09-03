/*
 * BinarySecretStrategy.java
 *
 * Created on January 4, 2006, 3:22 PM
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
    
    protected static final Logger log =
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
