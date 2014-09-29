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
 * Transform.java
 *
 * Created on January 24, 2006, 3:42 PM
 */

package com.sun.xml.ws.security.opt.crypto.dsig;

import com.sun.xml.security.core.dsig.InclusiveNamespacesType;
import com.sun.xml.ws.security.opt.impl.dsig.ExcC14NParameterSpec;
import com.sun.xml.ws.security.opt.impl.dsig.StAXEnvelopedTransformWriter;
import com.sun.xml.ws.security.opt.impl.dsig.StAXSTRTransformWriter;
import com.sun.xml.ws.security.secext10.TransformationParametersType;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import com.sun.xml.wss.logging.LogDomainConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.crypto.Data;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;

import java.util.ArrayList;

/**
 *
 * @author Abhijit Das
 * @author K.Venugopal@sun.com
 */
@XmlRootElement(name="Transform",namespace = "http://www.w3.org/2000/09/xmldsig#")
public class Transform extends com.sun.xml.security.core.dsig.TransformType implements javax.xml.crypto.dsig.Transform {
    @XmlTransient private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    
    @XmlTransient private AlgorithmParameterSpec algSpec = null;
    @XmlTransient private Exc14nCanonicalizer _exc14nTransform;
    @XmlTransient private String refId = "";
    
    /** Creates a new instance of Transform */
    public Transform() {
    }
    
    
    public AlgorithmParameterSpec getParameterSpec() {
        return algSpec;
    }
    
    public void setParameterSpec(AlgorithmParameterSpec algSpec) {
        this.algSpec = algSpec;
    }

    @SuppressWarnings("unchecked")
    public void setContent(List content) {
        this.content = content;
    }
    
    public Data transform(Data data, XMLCryptoContext xMLCryptoContext) throws TransformException {
        if(javax.xml.crypto.dsig.CanonicalizationMethod.EXCLUSIVE.equals(getAlgorithm())){
            if(_exc14nTransform == null){
                _exc14nTransform = new Exc14nCanonicalizer();
            }
            return _exc14nTransform.transform(data,xMLCryptoContext);
        }else if(getAlgorithm().equals(Transform.ENVELOPED)){
            return new StAXEnvelopedTransformWriter(data);
        }else if(getAlgorithm().equals(MessageConstants.STR_TRANSFORM_URI)){
            return new StAXSTRTransformWriter(data,xMLCryptoContext,refId);
        }
        throw new UnsupportedOperationException("Algorithm Transform "+ getAlgorithm() +" not supported yet");
    }
    
    public Data transform(Data data, XMLCryptoContext xMLCryptoContext, OutputStream outputStream) throws TransformException {
        
        if(getAlgorithm().equals(MessageConstants.STR_TRANSFORM_URI)){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStream fis = outputStream;
            if(logger.isLoggable(Level.FINEST)){
                fis = bos;
            }
            StAXEXC14nCanonicalizerImpl _canonicalizer  = null;
            if(algSpec != null || content.size() >0){
                Object ob = content.get(0);
                if(ob instanceof JAXBElement){
                    JAXBElement el = (JAXBElement)ob;
                    TransformationParametersType tp = (TransformationParametersType) el.getValue();
                    CanonicalizationMethod cm = (CanonicalizationMethod) tp.getAny().get(0);
                    String algo = cm.getAlgorithm();
                    if(javax.xml.crypto.dsig.CanonicalizationMethod.EXCLUSIVE.equals(algo)){
                        _canonicalizer = new StAXEXC14nCanonicalizerImpl();
                        if (!logger.isLoggable(Level.FINEST)){
                            _canonicalizer.setStream(outputStream);
                        }else{
                            _canonicalizer.setStream(fis);
                        }
                    }
                }
            }

            StAXSTRTransformWriter strWriter = new StAXSTRTransformWriter(data,xMLCryptoContext,refId);
            try{
                strWriter.write(_canonicalizer);
            }catch(XMLStreamException ex){
                throw new TransformException(ex);
            }
            
            if (logger.isLoggable(Level.FINEST)){                
                logger.log(Level.FINEST, LogStringsMessages.WSS_1757_CANONICALIZED_TARGET_VALUE(bos.toString()));
                try {
                    outputStream.write(bos.toByteArray());
                    return null;
                } catch (IOException ex) {
                    throw new TransformException(ex);
                }
            }            
            return null;
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream fis = outputStream;
        if(logger.isLoggable(Level.FINEST)){
            fis = bos;
        }
        if(getAlgorithm().intern() == javax.xml.crypto.dsig.CanonicalizationMethod.EXCLUSIVE.intern()){
            if(_exc14nTransform == null){
                _exc14nTransform = new Exc14nCanonicalizer();
                try{
                    _exc14nTransform.init((javax.xml.crypto.dsig.spec.TransformParameterSpec)algSpec);
                } catch(InvalidAlgorithmParameterException e){
                    throw new TransformException(e);
                }
            }
            if(!logger.isLoggable(Level.FINEST)){
                //return _exc14nTransform.transform(data,xMLCryptoContext,fis);
                Data canData =  _exc14nTransform.transform(data,xMLCryptoContext,fis);
                setContentList();
                return canData;
            }else{
                _exc14nTransform.transform(data,xMLCryptoContext,fis);
                setContentList();
                logger.log(Level.FINEST, LogStringsMessages.WSS_1757_CANONICALIZED_TARGET_VALUE(bos.toString()));
                try {
                    outputStream.write(bos.toByteArray());
                    return null;
                } catch (IOException ex) {
                    throw new TransformException(ex);
                }
            }
        } else if(getAlgorithm().intern() == MessageConstants.SWA11_ATTACHMENT_CONTENT_SIGNATURE_TRANSFORM){
            ACOTransform acoTransform = new ACOTransform();
            return acoTransform.transform(data, xMLCryptoContext, fis);
        } else if(getAlgorithm().intern() == MessageConstants.SWA11_ATTACHMENT_COMPLETE_SIGNATURE_TRANSFORM){
            // TODO:
        }else if(getAlgorithm().intern() == MessageConstants.SWA11_ATTACHMENT_CIPHERTEXT_TRANSFORM){
            //ACOTransform acoTransform = new ACOTransform();
            //return acoTransform.transform(data, xMLCryptoContext, fis);
        }
        throw new UnsupportedOperationException("Algorithm Transform "+ getAlgorithm() +" not supported yet");
    }
    
    public boolean isFeatureSupported(String string) {
        return false;
    }
    
    public void setReferenceId(String id){
        this.refId = id;
    }

    @SuppressWarnings("unchecked")
    private void setContentList(){
        if(algSpec != null){
            content = setInclusiveNamespaces((ExcC14NParameterSpec)algSpec);
        }
    }
    @SuppressWarnings("unchecked")
    private List setInclusiveNamespaces(ExcC14NParameterSpec spec){
        com.sun.xml.security.core.dsig.ObjectFactory objFac = new com.sun.xml.security.core.dsig.ObjectFactory();
        InclusiveNamespacesType incList = objFac.createInclusiveNamespaces();
        List prefixList = spec.getPrefixList();
        for(int j = 0; j < prefixList.size(); j++){
            String prefix = (String)prefixList.get(j);
            incList.addToPrefixList(prefix);
        }
        JAXBElement<InclusiveNamespacesType> je = objFac.createInclusiveNamespaces(incList);
        List contentList = new ArrayList();
        contentList.add(je);
        return contentList;
    }
 
    
}
