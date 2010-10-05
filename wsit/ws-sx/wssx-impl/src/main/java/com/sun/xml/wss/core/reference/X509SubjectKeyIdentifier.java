/*
 * $Id: X509SubjectKeyIdentifier.java,v 1.1 2010-10-05 12:01:17 m_potociar Exp $
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

package com.sun.xml.wss.core.reference;

import java.security.cert.X509Certificate;
import java.util.logging.Level;

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
        KeyIdentifierSPI spi = KeyIdentifierSPI.getInstance();
        if (spi != null) {
            try {
                return spi.getSubjectKeyIdentifier(cert);
            } catch (KeyIdentifierSPI.KeyIdentifierSPIException ex) {
                throw new XWSSecurityException(ex);
            }
        }
        //todo : log here
        throw new XWSSecurityException("Could not locate SPI class for KeyIdentifierSPI");
    }
    
    public void setCertificate(X509Certificate cert){
        this.cert = cert;
    }
 
    public X509Certificate getCertificate(){
        return cert;
    }
} 
