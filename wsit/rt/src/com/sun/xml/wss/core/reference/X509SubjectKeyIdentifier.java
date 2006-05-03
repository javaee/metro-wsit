/*
 * $Id: X509SubjectKeyIdentifier.java,v 1.1 2006-05-03 22:57:35 arungupta Exp $
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

package com.sun.xml.wss.core.reference;

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
 * @author Vishal Mahajan
 * @author Manveen Kaur
 */
public class X509SubjectKeyIdentifier extends KeyIdentifier {

    private static final String SUBJECT_KEY_IDENTIFIER_OID = "2.5.29.14";

    /** Defaults */
    private String encodingType = MessageConstants.BASE64_ENCODING_NS;

    private String valueType = MessageConstants.X509SubjectKeyIdentifier_NS;
    
    private X509Certificate cert = null;

    /**
     * Creates an "empty" KeyIdentifier element with default encoding type
     * and default value type.
     */
    public X509SubjectKeyIdentifier(Document doc) throws XWSSecurityException {
        super(doc);
        // Set default attributes
        setAttribute("EncodingType", encodingType);
        setAttribute("ValueType", valueType);
    }

    public X509SubjectKeyIdentifier(SOAPElement element) 
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
    public static byte[] getSubjectKeyIdentifier(X509Certificate cert) 
       throws XWSSecurityException {
        byte[] subjectKeyIdentifier =
            cert.getExtensionValue(SUBJECT_KEY_IDENTIFIER_OID);
        if (subjectKeyIdentifier == null)
            return null;
         
        try {
          sun.security.x509.KeyIdentifier keyId = null; 
         
          sun.security.util.DerValue derVal = new sun.security.util.DerValue(
               new sun.security.util.DerInputStream(subjectKeyIdentifier).getOctetString());
         
          keyId = new sun.security.x509.KeyIdentifier(derVal.getOctetString());
          return keyId.getIdentifier();
          } catch (NoClassDefFoundError ncde) {
             // TODO X509 Token profile states that only the contents of the
             // OCTET STRING should be returned, excluding the "prefix"
            byte[] dest = new byte[subjectKeyIdentifier.length-4];
            System.arraycopy(
               subjectKeyIdentifier, 4, dest, 0, subjectKeyIdentifier.length-4);
            return dest;
 
          } catch (IOException e) {
            //log exception
            throw new XWSSecurityException("Error in extracting keyIdentifier" + e.getMessage());
          }
    }
    
    public void setCertificate(X509Certificate cert){
        this.cert = cert;
    }
 
    public X509Certificate getCertificate(){
        return cert;
    }
} 
