/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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
 * ACOTransform.java
 *
 * Created on March 15, 2005, 8:25 PM
 */

package com.sun.xml.wss.impl.transform;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.crypto.dsig.TransformService;
import com.sun.xml.wss.impl.c14n.Canonicalizer;
import com.sun.xml.wss.impl.c14n.CanonicalizerFactory;
import com.sun.xml.wss.impl.dsig.AttachmentData;
import com.sun.xml.wss.logging.impl.dsig.LogStringsMessages;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.crypto.OctetStreamData;
import javax.xml.soap.AttachmentPart;

/**
 *
 * @author  K.venugopal@sun.com
 */
public class ACOTransform extends TransformService {
    private static Logger logger = Logger.getLogger(LogDomainConstants.IMPL_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_SIGNATURE_DOMAIN_BUNDLE);
    
    private static final String implementedTransformURI = MessageConstants.ATTACHMENT_CONTENT_ONLY_TRANSFORM_URI;
    
    /** Creates a new instance of ACOTransform */
    public ACOTransform() {
    }
    
    public java.security.spec.AlgorithmParameterSpec getParameterSpec() {
        return null;
        
        //Revisit.
    }
    
    public void init(javax.xml.crypto.dsig.spec.TransformParameterSpec transformParameterSpec) throws java.security.InvalidAlgorithmParameterException {
    }
    
    public void init(javax.xml.crypto.XMLStructure xMLStructure, javax.xml.crypto.XMLCryptoContext xMLCryptoContext) throws java.security.InvalidAlgorithmParameterException {
    }
    
    public void marshalParams(javax.xml.crypto.XMLStructure xMLStructure, javax.xml.crypto.XMLCryptoContext xMLCryptoContext) throws javax.xml.crypto.MarshalException {
        //no-op
    }
    
    
    private  javax.xml.crypto.Data canonicalize(OctetStreamData data) {
        throw new UnsupportedOperationException();
        //Revisit ::
        /*try{
            //Raised issue that passing of input to attachment complete
            //transform  to be standardised.
            String contentType = data.getMimeType();
            InputStream ioStream = data.getOctetStream();
            Canonicalizer canonicalizer = CanonicalizerFactory.getCanonicalizer(contentType);
            InputStream canonicalizedStream =  canonicalizer.canonicalize(ioStream);
            return new OctetStreamData(canonicalizedStream);
        }catch(Exception ex){
            ex.printStackTrace();
        }*/
        
    }
    
    private javax.xml.crypto.Data canonicalize(AttachmentData data,OutputStream outputStream) throws javax.xml.crypto.dsig.TransformException{
        try{
            AttachmentPart attachment = data.getAttachmentPart();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            attachment.getDataHandler().writeTo(os);
            OutputStream byteStream = null;
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(os.toByteArray());
            if(outputStream == null){
                byteStream = new ByteArrayOutputStream();
            }else{
                byteStream = outputStream;
            }
            Canonicalizer canonicalizer =  CanonicalizerFactory.getCanonicalizer(attachment.getContentType());
            InputStream is = canonicalizer.canonicalize(byteInputStream,byteStream);
            if(is!= null) return new OctetStreamData(is);
            
            return null;
        }catch(javax.xml.crypto.dsig.TransformException te){
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1318_AC_TRANSFORM_ERROR(),te);
            throw te;
        }catch(Exception ex){
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1318_AC_TRANSFORM_ERROR(),ex);
            throw new javax.xml.crypto.dsig.TransformException(ex.getMessage());
        }
    }
    
    public boolean isFeatureSupported(String str) {
        return false;
    }
    
    public javax.xml.crypto.Data transform(javax.xml.crypto.Data data, javax.xml.crypto.XMLCryptoContext xMLCryptoContext) throws javax.xml.crypto.dsig.TransformException {
        if(data instanceof OctetStreamData){
            return canonicalize((OctetStreamData)data);
        }else if(data instanceof AttachmentData){
            ByteArrayOutputStream os = null;
            return canonicalize((AttachmentData)data,os);
        }
        return null;
    }
    
    public javax.xml.crypto.Data transform(javax.xml.crypto.Data data, javax.xml.crypto.XMLCryptoContext xMLCryptoContext, java.io.OutputStream outputStream) throws javax.xml.crypto.dsig.TransformException {
        if(data instanceof AttachmentData){
            return canonicalize((AttachmentData)data,outputStream);
        }
        return null;
    }
    
    
}
