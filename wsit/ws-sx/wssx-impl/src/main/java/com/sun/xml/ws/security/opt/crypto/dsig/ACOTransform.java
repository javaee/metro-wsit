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

package com.sun.xml.ws.security.opt.crypto.dsig;

import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.security.opt.impl.crypto.AttachmentData;
import com.sun.xml.wss.impl.c14n.Canonicalizer;
import com.sun.xml.wss.impl.c14n.CanonicalizerFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

/**
 *
 * @author ashutosh.shahi@sun.com
 */
public class ACOTransform extends TransformService {
    
    /** Creates a new instance of ACOTransform */
    public ACOTransform() {
    }

    @Override
    public void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException {

    }

    @Override
    public void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException {

    }

    @Override
    public void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException {

    }

    public AlgorithmParameterSpec getParameterSpec() {
        return null;
    }

    public Data transform(Data data, XMLCryptoContext context) throws TransformException {
        if (data instanceof AttachmentData) {
            ByteArrayOutputStream os = null;
            return canonicalize((AttachmentData) data, os);
        }
        return null;
    }

    public Data transform(Data data, XMLCryptoContext context, OutputStream os) throws TransformException {
        if (data instanceof AttachmentData) {
            return canonicalize((AttachmentData) data, os);
        }
        return null;
    }

    public boolean isFeatureSupported(String feature) {
        return false;
    }

    private Data canonicalize(AttachmentData attachmentData, OutputStream os) throws TransformException {
        try{
        Attachment attachment = attachmentData.getAttachment();
        InputStream is = attachment.asInputStream();
        OutputStream byteStream = null;
        if (os == null) {
            byteStream = new ByteArrayOutputStream();
        } else {
            byteStream = os;
        }
        Canonicalizer canonicalizer =  CanonicalizerFactory.getCanonicalizer(attachment.getContentType());
        InputStream resultIs = canonicalizer.canonicalize(is,byteStream);
        if(resultIs!= null) return new OctetStreamData(resultIs);
        return null;
        }catch(Exception ex){
            throw new TransformException(ex.getMessage());
        }
    }
}
