/*
 * $Id: KeyIdentifierStrategy.java,v 1.1 2006-05-03 22:57:49 arungupta Exp $
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

import com.sun.xml.wss.core.reference.X509ThumbPrintIdentifier;
import java.security.cert.X509Certificate;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import com.sun.xml.wss.impl.misc.Base64;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.XWSSecurityException;

import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.reference.KeyIdentifier;
import com.sun.xml.wss.core.reference.SamlKeyIdentifier;
import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import com.sun.xml.wss.core.reference.EncryptedKeySHA1Identifier;

public class KeyIdentifierStrategy extends KeyInfoStrategy {
    
    public static final int THUMBPRINT = 0;
    public static final int ENCRYPTEDKEYSHA1 = 1;
    
    protected static Logger log =
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
                    "WSS0701.cannot.locate.certificate",
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
                    "WSS0701.cannot.locate.certificate",
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
                            "WSS0702.no.subject.keyidentifier",
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
                            "WSS0702.no.subject.keyidentifier",
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
