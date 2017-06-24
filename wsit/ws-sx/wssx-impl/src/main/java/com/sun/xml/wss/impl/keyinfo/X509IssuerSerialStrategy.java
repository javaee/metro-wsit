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
 * $Id: X509IssuerSerialStrategy.java,v 1.2 2010-10-21 15:37:29 snajper Exp $
 */

package com.sun.xml.wss.impl.keyinfo;

import java.security.cert.X509Certificate;

import org.w3c.dom.Document;

import java.util.logging.Logger;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;

import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.reference.X509IssuerSerial;

/**
 * @author Vishal Mahajan
 */
public class X509IssuerSerialStrategy extends KeyInfoStrategy {

    protected static final Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    X509Certificate cert = null;

    String alias = null;    

    public X509IssuerSerialStrategy(){
        
    }
    
    public X509IssuerSerialStrategy(String alias, boolean forSigning) {
        this.alias = alias;
        //this.forSigning = forSigning;
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
