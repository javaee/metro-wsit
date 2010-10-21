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
 * $Id: X509SecurityToken.java,v 1.2 2010-10-21 15:37:12 snajper Exp $
 */

package com.sun.xml.wss.core;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurityTokenException;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;

import com.sun.xml.ws.security.Token;

/**
 * An  X509 v3 certificate BinarySecurityToken.
 * 
 * @author Manveen Kaur
 * @author Edwin Goei
 */
public class X509SecurityToken extends BinarySecurityToken implements Token {

    private static Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    private X509Certificate cert;

    public X509SecurityToken(
        Document document,
        X509Certificate cert,
        String wsuId, String valueType) 
        throws SecurityTokenException {
                         
        super(document, wsuId, valueType);
        this.cert = cert;
        //checkCertVersion();
    }

    public X509SecurityToken(Document document, X509Certificate cert) 
        throws SecurityTokenException {
        super(document, null, MessageConstants.X509v3_NS);
        this.cert = cert;
        //checkCertVersion();
    }
    
     public X509SecurityToken(Document document, X509Certificate cert, String valueType) throws SecurityTokenException {
            super(document, null, valueType);

        this.cert = cert;
        //checkCertVersion();
    }

    public X509SecurityToken(SOAPElement tokenElement, boolean isBSP) 
        throws XWSSecurityException {
        super(tokenElement, isBSP);
        if (!(tokenElement.getLocalName().equals(
                  MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME) &&
              XMLUtil.inWsseNS(tokenElement))) {
            log.log(Level.SEVERE, "WSS0391.error.creating.X509SecurityToken", tokenElement.getTagName());
            throw new XWSSecurityException(
                "BinarySecurityToken expected, found " +
                tokenElement.getTagName());
        }
    }

   public X509SecurityToken(SOAPElement tokenElement) throws XWSSecurityException {
        this(tokenElement, false);
    } 

    public X509Certificate getCertificate() throws XWSSecurityException {

        if (cert == null) {

            byte[] data;
            String encodedData = XMLUtil.getFullTextFromChildren(this);
            try {
                data = Base64.decode(encodedData);
            } catch (Base64DecodingException bde) {
                log.log(Level.SEVERE, "WSS0301.unableto.decode.data"); 
                throw new SecurityTokenException("Unable to decode data", bde);
            }
            try {
                CertificateFactory certFact = CertificateFactory.getInstance("X.509");
                cert = (X509Certificate) certFact.generateCertificate(new ByteArrayInputStream(data));
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0302.unableto.create.x509cert");
                throw new XWSSecurityException(
                    "Unable to create X509Certificate from data");
            }
        }
        //checkCertVersion();
        return cert;
    }

    public static SecurityHeaderBlock fromSoapElement(SOAPElement element)
        throws XWSSecurityException {
        return SecurityHeaderBlockImpl.fromSoapElement(
            element, X509SecurityToken.class);
    }

    public String getTextValue() throws XWSSecurityException {

        if (encodedText == null) {
            byte[] rawBytes;
            try {
                rawBytes = cert.getEncoded();
                setRawValue(rawBytes);
            } catch (CertificateEncodingException e) {
                log.log(
                    Level.SEVERE,"WSS0303.unableto.get.encoded.x509cert");
                throw new XWSSecurityException (
                    "Unable to get encoded representation of X509Certificate",
                    e);
            }
        }
        return encodedText;
    }

    private void checkCertVersion() throws SecurityTokenException {
        if (cert.getVersion() != 3||cert.getVersion() !=1) {
            log.log(Level.SEVERE, 
                    "WSS0392.invalid.X509cert.version", 
                    Integer.toString(cert.getVersion())); 
            throw new SecurityTokenException(
                "Expected Version 1 or 3 Certificate, found Version " +
                cert.getVersion());
        }
    }

    // Token interface methods
    public String getType() {
        return MessageConstants.X509_TOKEN_NS;
    }

    public Object getTokenValue() {
        try {
            return getCertificate();
        } catch (XWSSecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
}
