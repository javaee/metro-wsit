/*
 * $Id: X509IssuerSerialStrategy.java,v 1.1 2006-05-03 22:57:50 arungupta Exp $
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

import java.security.cert.X509Certificate;

import org.w3c.dom.Document;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.XWSSecurityException;

import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.reference.X509IssuerSerial;

/**
 * @author Vishal Mahajan
 */
public class X509IssuerSerialStrategy extends KeyInfoStrategy {

    protected static Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    X509Certificate cert = null;

    String alias = null;
    boolean forSigning;

    public X509IssuerSerialStrategy(){
        
    }
    
    public X509IssuerSerialStrategy(String alias, boolean forSigning) {
        this.alias = alias;
        this.forSigning = forSigning;
        this.cert = null;
    }

    public void insertKey(
        SecurityTokenReference tokenRef, SecurableSoapMessage secureMsg) 
        throws XWSSecurityException {
        X509IssuerSerial x509IssuerSerial =
            new  X509IssuerSerial(secureMsg.getSOAPPart(), cert);
        tokenRef.setReference(x509IssuerSerial);
    }

    public void insertKey(
        KeyInfoHeaderBlock keyInfo,
        SecurableSoapMessage secureMsg,
        String x509TokenId) // x509TokenId can be ignored
        throws XWSSecurityException {

        Document ownerDoc = keyInfo.getOwnerDocument();
        SecurityTokenReference tokenRef = 
            new SecurityTokenReference(ownerDoc);
        X509IssuerSerial x509IssuerSerial =
            new  X509IssuerSerial(ownerDoc, cert);
        tokenRef.setReference(x509IssuerSerial);
        keyInfo.addSecurityTokenReference(tokenRef);
    }

    public void setCertificate(X509Certificate cert) {
        this.cert = cert;
    }

    public String getAlias() {
        return alias;
    }
}
