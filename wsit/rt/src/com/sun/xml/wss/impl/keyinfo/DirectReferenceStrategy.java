/*
 * $Id: DirectReferenceStrategy.java,v 1.1 2006-05-03 22:57:49 arungupta Exp $
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

import java.util.Set;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;

import java.security.cert.X509Certificate;


import com.sun.xml.wss.impl.keyinfo.KeyInfoStrategy;
//import com.sun.xml.wss.impl.filter.FilterParameterConstants;

import com.sun.xml.wss.core.reference.DirectReference;
import com.sun.xml.wss.core.X509SecurityToken;
import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.SecurityTokenReference;

public class DirectReferenceStrategy extends KeyInfoStrategy {

    X509Certificate cert = null;

    String alias = null;
    boolean forSigning;

    String samlAssertionId = null;

    protected static Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    public DirectReferenceStrategy(){
        
    }
    public DirectReferenceStrategy(String samlAssertionId) {
        this.samlAssertionId = samlAssertionId;
        this.cert = null;
        this.alias = null;
        forSigning = false;
    }

    public DirectReferenceStrategy(String alias, boolean forSigning) {
        this.alias = alias; 
        this.forSigning = forSigning;
        this.samlAssertionId = null;
        this.cert = null;
    }

    public void insertKey(
        SecurityTokenReference tokenRef, SecurableSoapMessage secureMsg)
        throws XWSSecurityException {
        DirectReference ref = getDirectReference(secureMsg, null);
        tokenRef.setReference(ref);
    }


    public void insertKey(
        KeyInfoHeaderBlock keyInfo,
        SecurableSoapMessage secureMsg,
        String x509TokenId)
        throws XWSSecurityException {

        Document ownerDoc = keyInfo.getOwnerDocument();
        SecurityTokenReference tokenRef = new SecurityTokenReference(ownerDoc);
        DirectReference ref = getDirectReference(secureMsg, x509TokenId);
        tokenRef.setReference(ref);
        keyInfo.addSecurityTokenReference(tokenRef);
    }

    public void setCertificate(X509Certificate cert) {
        this.cert = cert;
    }

    public String getAlias() {
        return alias;
    }

    private DirectReference getDirectReference(
        SecurableSoapMessage secureMsg,
        String x509TokenId)
        throws XWSSecurityException {

        DirectReference ref = new DirectReference();

        if (samlAssertionId != null) {
            String uri = "#" + samlAssertionId;
            ref.setURI(uri);
            ref.setValueType(MessageConstants.WSSE_SAML_v1_1_VALUE_TYPE);
         
        } else  {
            // create a certificate token
            if (cert == null) {
                log.log(
                        Level.SEVERE,
                        "WSS0185.filterparameter.not.set",
                        new Object[] { "subjectkeyidentifier"});
                throw new XWSSecurityException(
                        "No certificate specified and no default found.");
            }
            if(x509TokenId == null){
                throw new XWSSecurityException("WSU ID is null");
            }            
            String uri = "#" + x509TokenId;
            ref.setURI(uri);
            ref.setValueType(MessageConstants.X509v3_NS);
        }
        return ref;
    }
}
