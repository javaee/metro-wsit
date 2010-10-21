/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.security.opt.impl.incoming.processor;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamReader;
import org.jvnet.staxex.XMLStreamReaderEx;
import org.jvnet.staxex.Base64Data;
import com.sun.xml.wss.impl.misc.Base64;
import javax.xml.stream.XMLStreamException;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import java.util.logging.Level;
import com.sun.xml.wss.logging.impl.opt.LogStringsMessages;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class BSTProcessor implements StreamFilter {
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_DOMAIN,
            LogDomainConstants.IMPL_OPT_DOMAIN_BUNDLE);
    
    private byte [] bstValue = null;
    private X509Certificate cert = null;
    /** Creates a new instance of BSTProcessor */
    public BSTProcessor() {
    }
    
    public byte [] getValue(){
        return bstValue;
    }
    
    public X509Certificate getCertificate(){
        return cert;
    }
    /**
     * parse an incomming X509 token
     * @param reader
     * @return
     */
    public boolean accept(XMLStreamReader reader){
        if(reader.getEventType() == XMLStreamReader.CHARACTERS){
            if(reader instanceof XMLStreamReaderEx){
                try{
                    CharSequence data = ((XMLStreamReaderEx)reader).getPCDATA();
                    if(data instanceof Base64Data){
                        Base64Data binaryData = (Base64Data)data;
                        //bstValue = binaryData.getExact();
                        buildCertificate(binaryData.getInputStream());
                        return true;
                    }
                }catch(XMLStreamException ex){
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1603_ERROR_READING_STREAM(ex),ex);
                    throw new XWSSecurityRuntimeException(LogStringsMessages.WSS_1603_ERROR_READING_STREAM(ex));
                }catch(IOException ex){
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1603_ERROR_READING_STREAM(ex),ex);
                    throw new XWSSecurityRuntimeException(LogStringsMessages.WSS_1603_ERROR_READING_STREAM(ex));
                }
            }
            
            try {
                bstValue = Base64.decode(reader.getText());
                buildCertificate(new ByteArrayInputStream(bstValue));
                
            } catch (Base64DecodingException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1604_ERROR_DECODING_BASE_64_DATA(ex),ex);
                throw new XWSSecurityRuntimeException(LogStringsMessages.WSS_1604_ERROR_DECODING_BASE_64_DATA(ex));
            }
        }
        return true;
    }
    
    /**
     * builds the certificate  from the given cert value
     * @param certValue InputStream
     */
    private void buildCertificate(InputStream certValue){
        try {
            CertificateFactory certFact;
            certFact = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) certFact.generateCertificate(certValue);
        } catch (CertificateException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1605_ERROR_GENERATING_CERTIFICATE(ex),ex);
            throw new XWSSecurityRuntimeException(LogStringsMessages.WSS_1605_ERROR_GENERATING_CERTIFICATE(ex));
        }
    }
    
}
