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

import org.jvnet.staxex.NamespaceContextEx;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.ws.security.opt.crypto.JAXBData;
import com.sun.xml.ws.security.opt.crypto.StreamWriterData;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import com.sun.xml.wss.impl.misc.UnsyncByteArrayOutputStream;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.TransformService;
import com.sun.xml.ws.security.opt.impl.dsig.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.stream.XMLStreamException;
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;
import java.util.logging.Level;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class Exc14nCanonicalizer extends TransformService {
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    
    
    StAXEXC14nCanonicalizerImpl _canonicalizer = new StAXEXC14nCanonicalizerImpl();
    UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
    TransformParameterSpec _transformParameterSpec;
    /** Creates a new instance of Exc14nCanonicalizer */
    public Exc14nCanonicalizer() {
    }
    
    public void init(TransformParameterSpec transformParameterSpec) throws InvalidAlgorithmParameterException {
        _transformParameterSpec = transformParameterSpec;
    }
    
    public void marshalParams(XMLStructure xMLStructure, XMLCryptoContext xMLCryptoContext) throws MarshalException {
    }
    
    public void init(XMLStructure xMLStructure, XMLCryptoContext xMLCryptoContext) throws InvalidAlgorithmParameterException {
    }
    
    public AlgorithmParameterSpec getParameterSpec() {
        return _transformParameterSpec;
    }
    
    public Data transform(Data data, XMLCryptoContext xMLCryptoContext) throws TransformException {
        _canonicalizer.setStream(baos);
        _canonicalizer.reset();
        
        if(data instanceof StreamWriterData ){
            StreamWriterData swd = (StreamWriterData)data;
            NamespaceContextEx nc  = swd.getNamespaceContext();
            Iterator<NamespaceContextEx.Binding> itr = nc.iterator();
            
            while(itr.hasNext()){
                final NamespaceContextEx.Binding nd = itr.next();
                try {
                    _canonicalizer.writeNamespace(nd.getPrefix(),nd.getNamespaceURI());
                } catch (XMLStreamException ex) {
                    throw new TransformException(ex);
                }
            }
            try {
                ExcC14NParameterSpec spec = (ExcC14NParameterSpec)_transformParameterSpec;
                if(spec != null){
                    _canonicalizer.setInclusivePrefixList(spec.getPrefixList());
                }
                swd.write(_canonicalizer);
                _canonicalizer.flush();
            } catch (XMLStreamException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1759_TRANSFORM_ERROR(ex.getMessage()),ex);
                throw new TransformException(ex);
            }
            
            
            return new OctetStreamData(new ByteArrayInputStream(baos.getBytes(),0,baos.getLength()));
        }
        throw new UnsupportedOperationException("Data type"+data+" not yet supported");
    }
    
    public Data transform(Data data, XMLCryptoContext xMLCryptoContext, OutputStream outputStream) throws TransformException {
        _canonicalizer.setStream(outputStream);
        _canonicalizer.reset();
        
        if(data instanceof StreamWriterData){
            StreamWriterData swd = (StreamWriterData)data;
            NamespaceContextEx nc  = swd.getNamespaceContext();
            Iterator<NamespaceContextEx.Binding> itr = nc.iterator();
            
            while(itr.hasNext()){
                final NamespaceContextEx.Binding nd = itr.next();
                try {
                    _canonicalizer.writeNamespace(nd.getPrefix(),nd.getNamespaceURI());
                } catch (XMLStreamException ex) {
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1759_TRANSFORM_ERROR(ex.getMessage()),ex);
                    throw new TransformException(ex);
                }
            }
            try {
                ExcC14NParameterSpec spec = (ExcC14NParameterSpec)_transformParameterSpec;
                if(spec != null){
                    _canonicalizer.setInclusivePrefixList(spec.getPrefixList());
                }
                swd.write(_canonicalizer);
                _canonicalizer.flush();
            } catch (XMLStreamException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1759_TRANSFORM_ERROR(ex.getMessage()),ex);
                throw new TransformException(ex);
            }
            
            return null;
        }else if(data instanceof JAXBData){
            JAXBData jd =(JAXBData)data;
            NamespaceContextEx nc  = jd.getNamespaceContext();
            Iterator<NamespaceContextEx.Binding> itr = nc.iterator();
            
            while(itr.hasNext()){
                final NamespaceContextEx.Binding nd = itr.next();
                try {
                    _canonicalizer.writeNamespace(nd.getPrefix(),nd.getNamespaceURI());
                } catch (XMLStreamException ex) {
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1759_TRANSFORM_ERROR(ex.getMessage()),ex);
                    throw new TransformException(ex);
                }
            }
            
            try {
                ExcC14NParameterSpec spec = (ExcC14NParameterSpec)_transformParameterSpec;
                if(spec != null){
                    _canonicalizer.setInclusivePrefixList(spec.getPrefixList());
                }
                jd.writeTo(_canonicalizer);
                _canonicalizer.flush();
            } catch ( XMLStreamException ex ) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1759_TRANSFORM_ERROR(ex.getMessage()),ex);
                throw new TransformException(ex);
            }catch (XWSSecurityException ex) {
                throw new TransformException(ex);
            }
            
            return null;
        }
        throw new UnsupportedOperationException("Data type "+data+" not yet supported");
    }
    
    public boolean isFeatureSupported(String string) {
        return true;
    }
    
    
}
