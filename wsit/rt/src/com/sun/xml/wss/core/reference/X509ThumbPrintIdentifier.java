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

package com.sun.xml.wss.core.reference;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;

import java.io.ByteArrayInputStream;

import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurityHeaderException;
import com.sun.xml.wss.XWSSecurityException;
import java.io.IOException;


/**
 * @author Abhijit Das
 */
public class X509ThumbPrintIdentifier extends KeyIdentifier {

    /** Defaults */
    private String encodingType = MessageConstants.BASE64_ENCODING_NS;

    private String valueType = MessageConstants.ThumbPrintIdentifier_NS;
    
    private X509Certificate cert = null;

    /**
     * Creates an "empty" KeyIdentifier element with default encoding type
     * and default value type.
     */
    public X509ThumbPrintIdentifier(Document doc) throws XWSSecurityException {
        super(doc);
        // Set default attributes
        setAttribute("EncodingType", encodingType);
        setAttribute("ValueType", valueType);
    }

    public X509ThumbPrintIdentifier(SOAPElement element) 
        throws XWSSecurityException {
        super(element);
    }


    public byte[] getDecodedBase64EncodedValue() throws XWSSecurityException {
        try {
            return Base64.decode(getReferenceValue());
        } catch (Base64DecodingException e) {
            log.log(Level.SEVERE, "WSS0144.unableto.decode.base64.data",
                new Object[] {e.getMessage()});
            throw new SecurityHeaderException(
                "Unable to decode Base64 encoded data",
                e);
        }
    }

    /**
     * @return the SubjectKeyIdentifier from cert or null if cert does not
     *         contain one
     */
    public static byte[] getThumbPrintIdentifier(X509Certificate cert) 
       throws XWSSecurityException {
        byte[] thumbPrintIdentifier = null;

        try {
            thumbPrintIdentifier = MessageDigest.getInstance("SHA-1").digest(cert.getEncoded());
        } catch ( NoSuchAlgorithmException ex ) {
            throw new XWSSecurityException("Digest algorithm SHA-1 not found");
        } catch ( CertificateEncodingException ex) {
            throw new XWSSecurityException("Error while getting certificate's raw content");
        }
        
        return thumbPrintIdentifier;
    }
    
    public void setCertificate(X509Certificate cert){
        this.cert = cert;
    }
 
    public X509Certificate getCertificate(){
        return cert;
    }
} 
