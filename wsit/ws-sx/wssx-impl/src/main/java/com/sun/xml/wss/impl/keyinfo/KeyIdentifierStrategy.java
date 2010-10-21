/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
 * $Id: KeyIdentifierStrategy.java,v 1.2 2010-10-21 15:37:29 snajper Exp $
 */

package com.sun.xml.wss.impl.keyinfo;

import com.sun.xml.wss.core.reference.X509ThumbPrintIdentifier;
import java.security.cert.X509Certificate;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import com.sun.xml.wss.impl.misc.Base64;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;

import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.reference.KeyIdentifier;
import com.sun.xml.wss.core.reference.SamlKeyIdentifier;
import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import com.sun.xml.wss.core.reference.EncryptedKeySHA1Identifier;
import com.sun.xml.wss.logging.LogStringsMessages;

public class KeyIdentifierStrategy extends KeyInfoStrategy {
    
    public static final int THUMBPRINT = 0;
    public static final int ENCRYPTEDKEYSHA1 = 1;
    
    protected static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    X509Certificate cert = null;
    String alias = null;
    boolean forSigning;
    boolean thumbprint;
    boolean encryptedKey = false;
    
    String samlAssertionId = null;
    
    public KeyIdentifierStrategy(){
        
    }
    
    public KeyIdentifierStrategy(int value){
        if(value == THUMBPRINT)
            this.thumbprint = true;
        else if(value == ENCRYPTEDKEYSHA1)
            this.encryptedKey = true;
    }
    
    public KeyIdentifierStrategy(String samlAssertionId) {
        this.samlAssertionId = samlAssertionId;
        forSigning = false;
    }
    
    public KeyIdentifierStrategy(String alias, boolean forSigning) {
        this.alias = alias;
        this.forSigning = forSigning;
    }

    public KeyIdentifierStrategy(String alias, boolean forSigning, boolean thumbprint) {
        this.alias = alias;
        this.forSigning = forSigning;
        this.thumbprint = thumbprint;
    }
    
    public void insertKey(
            SecurityTokenReference tokenRef, SecurableSoapMessage secureMsg)
            throws XWSSecurityException {
        KeyIdentifier keyIdentifier = getKeyIdentifier(secureMsg);
        if (keyIdentifier == null) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSS_0701_CANNOT_LOCATE_CERTIFICATE(alias),
                    alias);
            throw new XWSSecurityException(
                    "Unable to locate certificate for the alias '" + alias + "'");
        }
        tokenRef.setReference(keyIdentifier);
    }
    
    public void insertKey(
            KeyInfoHeaderBlock keyInfo,
            SecurableSoapMessage secureMsg,
            String x509TokenId) // x509TokenId can be ignored
            throws XWSSecurityException {
        
        KeyIdentifier keyIdentifier = getKeyIdentifier(secureMsg);
        
        if (keyIdentifier == null) {
            log.log(Level.SEVERE,
                     LogStringsMessages.WSS_0701_CANNOT_LOCATE_CERTIFICATE(alias),
                    alias);
            throw new XWSSecurityException(
                    "Unable to locate certificate for the alias '" + alias + "'");
        }
        Document ownerDoc = keyInfo.getOwnerDocument();
        SecurityTokenReference tokenRef =
                new SecurityTokenReference(ownerDoc);
        tokenRef.setReference(keyIdentifier);
        keyInfo.addSecurityTokenReference(tokenRef);
    }
    
    private KeyIdentifier getKeyIdentifier(SecurableSoapMessage secureMsg)
    throws XWSSecurityException {
        
        KeyIdentifier keyIdentifier = null;        
        if (samlAssertionId != null) {
            keyIdentifier =
                    new SamlKeyIdentifier(secureMsg.getSOAPPart());
            keyIdentifier.setReferenceValue(samlAssertionId);
            return keyIdentifier;
        }
        
        if (cert != null) {
            if ( !thumbprint) {
                byte[] subjectKeyIdentifier =
                        X509SubjectKeyIdentifier.getSubjectKeyIdentifier(cert);
                if (subjectKeyIdentifier == null) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSS_0702_NO_SUBJECT_KEYIDENTIFIER(alias),
                            alias);
                    throw new XWSSecurityException(
                            "The found certificate does not contain subject key identifier X509 extension");
                }
                String keyId = Base64.encode(subjectKeyIdentifier);
                keyIdentifier =
                        new X509SubjectKeyIdentifier(secureMsg.getSOAPPart());
                keyIdentifier.setReferenceValue(keyId);
            } else {
                byte[] thumbPrintIdentifier =
                        X509ThumbPrintIdentifier.getThumbPrintIdentifier(cert);
                if (thumbPrintIdentifier == null) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSS_0702_NO_SUBJECT_KEYIDENTIFIER(alias),
                            alias);
                    throw new XWSSecurityException(
                            "Error while calculating thumb print identifier");
                }
                String keyId = Base64.encode(thumbPrintIdentifier);
                keyIdentifier =
                        new X509ThumbPrintIdentifier(secureMsg.getSOAPPart());
                keyIdentifier.setReferenceValue(keyId);
            }
        }else if(encryptedKey){
            keyIdentifier = new EncryptedKeySHA1Identifier(secureMsg.getSOAPPart());
        }
        return keyIdentifier;
    }
    
    public void setCertificate(X509Certificate cert) {
        this.cert = cert;
    }
    
    public String getAlias() {
        return alias;
    }
    

}
