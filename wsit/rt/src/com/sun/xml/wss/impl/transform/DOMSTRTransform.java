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


/*
 * DOMSTRTransform.java
 *
 * Created on February 22, 2005, 2:18 PM
 */

package com.sun.xml.wss.impl.transform;

import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.transform.STRTransformImpl;
import com.sun.xml.wss.logging.LogDomainConstants;

import java.io.OutputStream;

import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.TransformException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author  K.Venugopal@sun.com
 * @author  Sean Mullan
 */
public class DOMSTRTransform extends TransformService {
    private STRTransformParameterSpec params;
    private static Logger logger = Logger.getLogger(LogDomainConstants.IMPL_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_SIGNATURE_DOMAIN_BUNDLE);
    public static final String WSSE =
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    
    public static final String WSU =
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    
    public void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params == null) {
            throw new InvalidAlgorithmParameterException("params are required");
        }
        this.params = (STRTransformParameterSpec) params;
    }
    
    public void init(javax.xml.crypto.XMLStructure params, javax.xml.crypto.XMLCryptoContext xMLCryptoContext)
    throws java.security.InvalidAlgorithmParameterException {
        DOMStructure domParams = (DOMStructure) params;
        try {
            unmarshalParams(domParams.getNode(), xMLCryptoContext);
        } catch (MarshalException me) {
            throw new InvalidAlgorithmParameterException(me.getMessage());
        }
    }
    
    public java.security.spec.AlgorithmParameterSpec getParameterSpec() {
        return params;
    }
    
    public void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
        
        Node pn = ((DOMStructure) parent).getNode();
        Document ownerDoc = XMLUtil.getOwnerDocument(pn);
        
        String prefix = null;
        String dsPrefix = null;
        if (context != null) {
            prefix = context.getNamespacePrefix
                    (WSSE, "wsse");
            dsPrefix = context.getNamespacePrefix
                    (XMLSignature.XMLNS, context.getDefaultNamespacePrefix());
        }
        
        Element transformParamElem =XMLUtil.createElement
                (ownerDoc, "TransformationParameters", WSSE, prefix);
        
        CanonicalizationMethod cm = params.getCanonicalizationMethod();
        Element c14nElem = XMLUtil.createElement
                (ownerDoc, "CanonicalizationMethod", XMLSignature.XMLNS, dsPrefix);
        c14nElem.setAttributeNS(null, "Algorithm", cm.getAlgorithm());
        
        C14NMethodParameterSpec cs =
                (C14NMethodParameterSpec) cm.getParameterSpec();
        if (cs != null) {
            TransformService cmSpi = null;
            try {
                cmSpi = TransformService.getInstance( cm.getAlgorithm(),"DOM");
                cmSpi.init(cs);
                cmSpi.marshalParams(new DOMStructure(c14nElem), context);
            } catch (Exception e) {
                logger.log(Level.SEVERE,"WSS1321.str_marshal.transform.error",e);
                throw new MarshalException(e);
            }
        }
        
        transformParamElem.appendChild(c14nElem);
        pn.appendChild(transformParamElem);
    }
    
    
    public javax.xml.crypto.Data transform(javax.xml.crypto.Data data, javax.xml.crypto.XMLCryptoContext xc) throws javax.xml.crypto.dsig.TransformException {
        java.io.OutputStream outputStream = null;
        return new STRTransformImpl().transform(data,xc,outputStream);
    }
    
    
    public javax.xml.crypto.Data transform(javax.xml.crypto.Data data, javax.xml.crypto.XMLCryptoContext xc, java.io.OutputStream outputStream) throws javax.xml.crypto.dsig.TransformException {
        //throw new UnsupportedOperationException();
        return new STRTransformImpl().transform(data,xc,outputStream);
    }
    
    public void unmarshalParams(XMLStructure parent, XMLCryptoContext context)
    throws MarshalException ,java.security.InvalidAlgorithmParameterException{
        
        Element transformElem = (Element) ((DOMStructure) parent).getNode();
        Element tpElem = XMLUtil.getFirstChildElement(transformElem);
        unmarshalParams(tpElem, context);
    }
    
    private void unmarshalParams(Node tpElem, XMLCryptoContext context)
    throws MarshalException,java.security.InvalidAlgorithmParameterException {
        
        Element c14nElem = null;
        if(tpElem.getNodeType() == Node.DOCUMENT_NODE){
            c14nElem =(Element) ((Document)tpElem).getFirstChild();
        }else{
            c14nElem = XMLUtil.getFirstChildElement(tpElem);
        }
        
        if(!"CanonicalizationMethod".equals(c14nElem.getLocalName())){
            NodeList nl = c14nElem.getElementsByTagNameNS(MessageConstants.DSIG_NS, "CanonicalizationMethod");
            if(nl.getLength() >0)c14nElem = (Element)nl.item(0);
        }
        if(c14nElem == null){
            new java.security.InvalidAlgorithmParameterException("Cannont find CanonicalizationMethod in TransformationParameters element");
        }
        String c14nAlg = c14nElem.getAttributeNodeNS(null, "Algorithm").getValue();
        if (MessageConstants.debug) {
            System.out.println("C14 Algo"+c14nAlg);
        }
        C14NMethodParameterSpec cs = null;
        Element paramsElem = XMLUtil.getFirstChildElement(c14nElem);
        javax.xml.crypto.dsig.TransformService cmSpi = null;
        try {
            cmSpi = javax.xml.crypto.dsig.TransformService.getInstance(c14nAlg, "DOM");
            if (paramsElem != null) {
                cmSpi.init(new DOMStructure(paramsElem), context);
                cs = (C14NMethodParameterSpec) cmSpi.getParameterSpec();
            }
            CanonicalizationMethod cm = new STRC14NMethod(cmSpi);
            this.params = new STRTransformParameterSpec( cm);
        } catch (Throwable e) {
            logger.log(Level.SEVERE,"WSS1320.str_un.transform.error",e);
            throw new MarshalException(e);
        }
        
    }
    
    public boolean isFeatureSupported(String str) {
        return false;
    }
    
    
    private static class STRC14NMethod implements CanonicalizationMethod {
        private javax.xml.crypto.dsig.TransformService cmSpi;
        STRC14NMethod(javax.xml.crypto.dsig.TransformService cmSpi) {
            this.cmSpi = cmSpi;
        }
        public String getAlgorithm() { return cmSpi.getAlgorithm(); }
        public AlgorithmParameterSpec getParameterSpec() {
            return cmSpi.getParameterSpec();
        }
        public boolean isFeatureSupported(String feature) { return false; }
        public Data transform(Data data, XMLCryptoContext context) throws TransformException {
            return cmSpi.transform(data, context);
        }
        public Data transform(Data data, XMLCryptoContext context, OutputStream os) throws TransformException {
            return cmSpi.transform(data, context, os);
        }
    }
    
    public static class STRTransformParameterSpec implements TransformParameterSpec {
        private CanonicalizationMethod c14nMethod;
        public STRTransformParameterSpec(CanonicalizationMethod c14nMethod) {
            this.c14nMethod = c14nMethod;
        }
        public CanonicalizationMethod getCanonicalizationMethod() {
            return c14nMethod;
        }
    }
}
