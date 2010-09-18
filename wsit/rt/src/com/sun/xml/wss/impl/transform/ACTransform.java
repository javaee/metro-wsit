/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

/*
 * ACTranform.java
 *
 * Created on March 16, 2005, 2:14 PM
 */

package com.sun.xml.wss.impl.transform;

import com.sun.xml.wss.impl.misc.UnsyncByteArrayOutputStream;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.crypto.dsig.TransformService;

import com.sun.xml.wss.impl.c14n.Canonicalizer;
import com.sun.xml.wss.impl.c14n.CanonicalizerFactory;
import com.sun.xml.wss.impl.c14n.MimeHeaderCanonicalizer;
import com.sun.xml.wss.impl.dsig.AttachmentData;
import com.sun.xml.wss.logging.impl.dsig.LogStringsMessages;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.soap.AttachmentPart;

/**
 *
 * @author  K.Venugopal@sun.com
 */
public class ACTransform extends TransformService {
    private static Logger logger = Logger.getLogger(LogDomainConstants.IMPL_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_SIGNATURE_DOMAIN_BUNDLE);
    /** Creates a new instance of ACTranform */
    public ACTransform() {
    }
    
    public void init(javax.xml.crypto.dsig.spec.TransformParameterSpec transformParameterSpec) throws java.security.InvalidAlgorithmParameterException {
    }
    
    public void init(javax.xml.crypto.XMLStructure xMLStructure, javax.xml.crypto.XMLCryptoContext xMLCryptoContext) throws java.security.InvalidAlgorithmParameterException {
    }
    
    public java.security.spec.AlgorithmParameterSpec getParameterSpec() {
        return null;
    }
    
    
    public void marshalParams(javax.xml.crypto.XMLStructure xMLStructure, javax.xml.crypto.XMLCryptoContext xMLCryptoContext) throws javax.xml.crypto.MarshalException {
    }
    
    /*
    private Data canonicalize(OctetStreamData octetData,javax.xml.crypto.XMLCryptoContext xMLCryptoContext) throws Exception {
       Vector mimeHeaders = (Vector)xMLCryptoContext.getProperty(MessageConstants.ATTACHMENT_MIME_HEADERS);
        InputStream os = octetData.getOctetStream();
        //Revisit ::
        // rf. RFC822
        MimeHeaderCanonicalizer mHCanonicalizer = CanonicalizerFactory.getMimeHeaderCanonicalizer("US-ASCII");
        byte[] outputHeaderBytes = mHCanonicalizer._canonicalize(mimeHeaders);
        Canonicalizer canonicalizer =  CanonicalizerFactory.getCanonicalizer(octetData.getMimeType());
        InputStream is = canonicalizer.canonicalize(os);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byteStream.write(outputHeaderBytes);
        int len=0;
        byte [] data= null;
        try{
            len = is.read(data);
        } catch (IOException e) {
            // log me
            throw new XWSSecurityException(e);
        }
     
        while(len > 0){
            try {
                byteStream.write(data);
                len = is.read(data);
            } catch (IOException e) {
                // log me
                throw new XWSSecurityException(e);
            }
        }
        return new OctetStreamData(new ByteArrayInputStream(byteStream.toByteArray()));
    }
     */
    
    private Data canonicalize(AttachmentData attachmentData,OutputStream outputStream) throws javax.xml.crypto.dsig.TransformException  {
        try{
            AttachmentPart attachment = attachmentData.getAttachmentPart();
            Iterator mimeHeaders = attachment.getAllMimeHeaders();
            //Revisit ::
            // rf. RFC822
            MimeHeaderCanonicalizer mHCanonicalizer = CanonicalizerFactory.getMimeHeaderCanonicalizer("US-ASCII");
            byte[] outputHeaderBytes = mHCanonicalizer._canonicalize(mimeHeaders);
            OutputStream byteStream = new UnsyncByteArrayOutputStream();
            attachment.getDataHandler().writeTo(byteStream);
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(((ByteArrayOutputStream)byteStream).toByteArray());
            byteStream.close();
            if(outputStream == null){
                byteStream = new ByteArrayOutputStream();
            }else{
                byteStream = outputStream;
            }
            byteStream.write(outputHeaderBytes);
            Canonicalizer canonicalizer =  CanonicalizerFactory.getCanonicalizer(attachment.getContentType());
            InputStream is = canonicalizer.canonicalize(byteInputStream,byteStream);
            if(is != null)  return new OctetStreamData(is);
            return null;
        }catch(javax.xml.crypto.dsig.TransformException te){
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1319_ACO_TRANSFORM_ERROR(),te);
            throw te;
        }catch(Exception ex){
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1319_ACO_TRANSFORM_ERROR(),ex);
            throw new javax.xml.crypto.dsig.TransformException(ex.getMessage());
        }
    }
    
    public boolean isFeatureSupported(String str) {
        return false;
    }
    
    public javax.xml.crypto.Data transform(javax.xml.crypto.Data data, javax.xml.crypto.XMLCryptoContext xMLCryptoContext) throws javax.xml.crypto.dsig.TransformException {
        if(data instanceof AttachmentData){
            try{
                return  canonicalize((AttachmentData)data, null);
            }catch(javax.xml.crypto.dsig.TransformException tex) {
                logger.log(Level.SEVERE,LogStringsMessages.WSS_1319_ACO_TRANSFORM_ERROR(),tex);
                throw tex;
            }catch(Exception ex){
                logger.log(Level.SEVERE,LogStringsMessages.WSS_1319_ACO_TRANSFORM_ERROR(),ex);
                throw new RuntimeException(ex);
            }
        }else{
            //TODO::
            throw new UnsupportedOperationException();
        }
    }
    
    public javax.xml.crypto.Data transform(javax.xml.crypto.Data data, javax.xml.crypto.XMLCryptoContext xMLCryptoContext, java.io.OutputStream outputStream) throws javax.xml.crypto.dsig.TransformException {
        if(data instanceof AttachmentData){
            return  canonicalize((AttachmentData)data, outputStream);
        }else{
            //TODO::
            throw new UnsupportedOperationException();
        }
    }
    
    
    
}
