/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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
 * EncryptionProcessor.java
 *
 * Created on August 1, 2006, 3:30 PM
 */

package com.sun.xml.ws.security.opt.impl.enc;

import com.sun.xml.security.core.xenc.EncryptedKeyType;
import com.sun.xml.security.core.xenc.ReferenceList;
import com.sun.xml.security.core.xenc.ReferenceType;
import com.sun.xml.ws.security.opt.api.EncryptedKey;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.keyinfo.BuilderResult;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.ws.security.opt.impl.util.WSSElementFactory;
import com.sun.xml.ws.security.opt.impl.message.ETHandler;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.keyinfo.KeyInfoStrategy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy.FeatureBinding;
import com.sun.xml.wss.logging.impl.opt.crypto.LogStringsMessages;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;

 /*
  * @author K.Venugopal@sun.com
  */
public class EncryptionProcessor {
    private static byte[] crlf = null;
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN_BUNDLE);
    static{
        try{
            crlf =  "\r\n".getBytes("US-ASCII");
        }catch( java.io.UnsupportedEncodingException ue){
            //log;
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1917_CRLF_INIT_FAILED(),ue);
        }
    }
    /** Creates a new instance of EncryptionProcessor */
    public EncryptionProcessor() {
    }
    /**
     * performs encryption
     * @param context JAXBFilterProcessingContext
     * @throws XWSSecurityException
     */
    public void process(JAXBFilterProcessingContext context) throws XWSSecurityException{
        boolean ekRefList = false;
        String referenceType = null;
        String x509TokenId = null;
        WSSElementFactory elementFactory = new WSSElementFactory(context.getSOAPVersion());
        X509Certificate _x509Cert = null;
        KeyInfoStrategy keyInfoStrategy =  null;
        String symmetricKeyName = null;
        AuthenticationTokenPolicy.X509CertificateBinding certificateBinding = null;
        ((NamespaceContextEx)context.getNamespaceContext()).addEncryptionNS();
        ((NamespaceContextEx)context.getNamespaceContext()).addSignatureNS();
        ReferenceList dataRefList = null;
        EncryptedKeyType ekt = null;
        WSSPolicy wssPolicy = (WSSPolicy)context.getSecurityPolicy();
        EncryptionPolicy.FeatureBinding featureBinding =(EncryptionPolicy.FeatureBinding)  wssPolicy.getFeatureBinding();
        WSSPolicy keyBinding = (WSSPolicy)wssPolicy.getKeyBinding();
        EncryptedKey ek = null;
        KeyInfo edKeyInfo = null;
        
        
        if(logger.isLoggable(Level.FINEST)){
            logger.log(Level.FINEST, LogStringsMessages.WSS_1952_ENCRYPTION_KEYBINDING_VALUE(keyBinding));
        }
        
        if(PolicyTypeUtil.derivedTokenKeyBinding(keyBinding)){
            DerivedTokenKeyBinding dtk = (DerivedTokenKeyBinding)keyBinding.clone();
            WSSPolicy originalKeyBinding = dtk.getOriginalKeyBinding();
            
            if (PolicyTypeUtil.x509CertificateBinding(originalKeyBinding)){
                AuthenticationTokenPolicy.X509CertificateBinding ckBindingClone =
                        (AuthenticationTokenPolicy.X509CertificateBinding)originalKeyBinding.clone();
                //create a symmetric key binding and set it as original key binding of dkt
                SymmetricKeyBinding skb = new SymmetricKeyBinding();
                skb.setKeyBinding(ckBindingClone);
                // set the x509 binding as key binding of symmetric binding
                dtk.setOriginalKeyBinding(skb);
                //keyBinding = dtk;
                EncryptionPolicy ep = (EncryptionPolicy)wssPolicy.clone();
                ep.setKeyBinding(dtk);
                context.setSecurityPolicy(ep);
                wssPolicy = ep;
            }
        }
        
        TokenProcessor tp = new TokenProcessor((EncryptionPolicy) wssPolicy, context);
        BuilderResult tokenInfo = tp.process();
        Key dataEncKey = null;
        Key dkEncKey = null;
        dataEncKey = tokenInfo.getDataProtectionKey();
        ek = tokenInfo.getEncryptedKey();
        ArrayList targets =  featureBinding.getTargetBindings();
        Iterator targetItr = targets.iterator();
        
        ETHandler edBuilder =  new ETHandler(context.getSOAPVersion());
        EncryptionPolicy.FeatureBinding  binding = (FeatureBinding) wssPolicy.getFeatureBinding();
        dataRefList = new ReferenceList();
        
        if(ek == null || binding.getUseStandAloneRefList()){
            edKeyInfo = tokenInfo.getKeyInfo();
        }
        
        boolean refAdded = false;
        while (targetItr.hasNext()) {
            EncryptionTarget target = (EncryptionTarget)targetItr.next();
            boolean contentOnly = target.getContentOnly();
            //target.getDataEncryptionAlgorithm();
            //target.getCipherReferenceTransforms();//TODO support this
            
            List edList = edBuilder.buildEDList( (EncryptionPolicy)wssPolicy,target ,context, dataEncKey,edKeyInfo);
            for(int i =0;i< edList.size();i++){
                JAXBElement<ReferenceType> rt = elementFactory.createDataReference((SecurityElement)edList.get(i));
                dataRefList.getDataReferenceOrKeyReference().add(rt);
                
                refAdded = true;
            }
        }
        if(refAdded){
            if(ek == null || (binding.getUseStandAloneRefList())){
                context.getSecurityHeader().add(elementFactory.createGSHeaderElement(dataRefList));
            }else{
                ek.setReferenceList(dataRefList);
            }
        }
    }
    /**
     * 
     * @param elemName
     * @param uri
     * @param contentOnly
     * @throws XWSSecurityException
     */
    private void checkBSP5607(String elemName, String uri, boolean contentOnly) throws XWSSecurityException {
        // BSP: 5607
        if (!contentOnly && (MessageConstants.SOAP_1_1_NS.equalsIgnoreCase(uri) || MessageConstants.SOAP_1_2_NS.equalsIgnoreCase(uri))
                && ("Header".equalsIgnoreCase(elemName) || "Envelope".equalsIgnoreCase(elemName) || "Body".equalsIgnoreCase(elemName))) {
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1918_ILLEGAL_ENCRYPTION_TARGET(uri, elemName));
            throw new XWSSecurityException("Encryption of SOAP " + elemName + " is not allowed"); // BSP 5607
        }
    }
}
