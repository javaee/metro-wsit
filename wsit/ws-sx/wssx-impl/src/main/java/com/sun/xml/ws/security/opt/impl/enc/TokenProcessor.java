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

package com.sun.xml.ws.security.opt.impl.enc;

import com.sun.xml.ws.security.opt.api.EncryptedKey;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.keyinfo.BuilderResult;
import com.sun.xml.ws.security.opt.api.keyinfo.TokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.DerivedKeyTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.IssuedTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.KerberosTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.SCTBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.SamlTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.SecurityTokenReference;
import com.sun.xml.ws.security.opt.impl.keyinfo.SymmetricTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.X509TokenBuilder;
import com.sun.xml.ws.security.opt.impl.reference.DirectReference;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.ws.security.opt.impl.util.WSSElementFactory;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.keyinfo.UsernameTokenBuilder;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.impl.opt.crypto.LogStringsMessages;
import java.security.Key;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author K.Venugopal@sun.com
 */

public class TokenProcessor {
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN_BUNDLE);
    
    private Key  dataEncKey = null;
    private Key dkEncKey = null;
    private WSSElementFactory elementFactory =null;
    private WSSPolicy keyBinding = null;
    private JAXBFilterProcessingContext context = null;
    private EncryptionPolicy ep = null;
    private TokenBuilder builder = null;
    
    private EncryptedKey ek = null;
    private KeyInfo keyInfo = null;
    /** Creates a new instance of TokenProcessor */
    public TokenProcessor(EncryptionPolicy ep,JAXBFilterProcessingContext context) {
        this.context = context;
        this.ep = ep;
        this.keyBinding = (WSSPolicy) ep.getKeyBinding();
        this.elementFactory =  new WSSElementFactory(context.getSOAPVersion());
    }
    
    
    public Key getDataEncKey(){
        return dataEncKey;
    }
    
    public Key getKeyEncKey(){
        return dkEncKey;
    }
    
    public EncryptedKey  getEncryptedKey(){
        return ek;
    }
    
    public KeyInfo getKeyInfo(){
        return keyInfo;
    }

    /**
     * Identifies suitable key binding and creates corresponding BuilderResult element for setting various credentials
     * like data protecton key ..etc
     * process the keyBinding and populate BuilderResult with appropriate key and KeyInfo
     * @return BuilderResult
     * @throws XWSSecurityException
     */
    public BuilderResult process() throws XWSSecurityException{
        
        String keyEncAlgo ="http://www.w3.org/2001/04/xmlenc#kw-aes256";
        //XMLCipher.RSA_v1dot5;
        String dataEncAlgo = MessageConstants.TRIPLE_DES_BLOCK_ENCRYPTION;
        EncryptionPolicy.FeatureBinding featureBinding =(EncryptionPolicy.FeatureBinding)  ep.getFeatureBinding();
        String tmp = featureBinding.getDataEncryptionAlgorithm();
        if (tmp == null || "".equals(tmp)) {
            if (context.getAlgorithmSuite() != null) {
                tmp = context.getAlgorithmSuite().getEncryptionAlgorithm();
            } else {
                //warn that data encryption algorithm not set
                if(logger.isLoggable(Level.FINEST)){
                    logger.log(Level.FINEST,LogStringsMessages.WSS_1950_DATAENCRYPTION_ALGORITHM_NOTSET());
                }
            }
        }
        //TODO :: Change to getDataEncryptionAlgorith,
        if(tmp != null && !"".equals(tmp)){
            dataEncAlgo = tmp;
        }
        
        if (context.getAlgorithmSuite() != null) {
            keyEncAlgo = context.getAlgorithmSuite().getAsymmetricKeyAlgorithm();
        }
        if (PolicyTypeUtil.usernameTokenBinding(keyBinding)) {
            AuthenticationTokenPolicy.UsernameTokenBinding untBinding = null;
            if(context.getusernameTokenBinding()!=null){
                untBinding = context.getusernameTokenBinding();
                context.setUsernameTokenBinding(null);
            }else {
                untBinding = (AuthenticationTokenPolicy.UsernameTokenBinding)keyBinding;
            }    
            this.dataEncKey = untBinding.getSecretKey();
            builder = new UsernameTokenBuilder(context, untBinding);
            BuilderResult untResult = builder.process();
            untResult.setDataProtectionKey(dataEncKey);
            return untResult;            
            
        } else if(PolicyTypeUtil.x509CertificateBinding(keyBinding)) {
            AuthenticationTokenPolicy.X509CertificateBinding certificateBinding = null;
            if ( context.getX509CertificateBinding() != null) {
                certificateBinding  = context.getX509CertificateBinding();
                context.setX509CertificateBinding(null);
            } else {
                certificateBinding  =(AuthenticationTokenPolicy.X509CertificateBinding)keyBinding;
            }
            
            String x509TokenId = certificateBinding.getUUID();
            if(x509TokenId == null || x509TokenId.equals("")){
                x509TokenId = context.generateID();
            }
            
            builder = new X509TokenBuilder(context,certificateBinding);
            BuilderResult xtbResult = builder.process();
            KeyInfo ekKI  = (com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo) xtbResult.getKeyInfo();
            
            tmp = null;
            tmp = certificateBinding.getKeyAlgorithm();
            if(tmp != null && !tmp.equals("")){
                keyEncAlgo = tmp;
            }
            
            dataEncKey = SecurityUtil.generateSymmetricKey(dataEncAlgo);
            //ekRefList = true;
            dkEncKey = certificateBinding.getX509Certificate().getPublicKey();
            String ekId = context.generateID();
            ek = elementFactory.createEncryptedKey(ekId,keyEncAlgo,ekKI,dkEncKey,dataEncKey);
            context.getSecurityHeader().add((SecurityHeaderElement)ek);
            xtbResult.setKeyInfo(null);
            
            DirectReference dr = elementFactory.createDirectReference();
            dr.setURI("#"+ekId);
            boolean wss11Sender = "true".equals(context.getExtraneousProperty("EnableWSS11PolicySender"));
            if(wss11Sender){
                dr.setValueType(MessageConstants.EncryptedKey_NS);
            }
            SecurityTokenReference str = elementFactory.createSecurityTokenReference(dr);
            keyInfo = elementFactory.createKeyInfo(str);
            
            xtbResult.setKeyInfo(keyInfo);
            xtbResult.setEncryptedKey(ek);
            xtbResult.setDataProtectionKey(dataEncKey);
            xtbResult.setKeyProtectionKey(dkEncKey);
            return xtbResult;
            
        } else if(PolicyTypeUtil.kerberosTokenBinding(keyBinding)){
            AuthenticationTokenPolicy.KerberosTokenBinding krbBinding = null;
            if(context.getKerberosTokenBinding() != null){
                krbBinding = context.getKerberosTokenBinding();
                context.setKerberosTokenBinding(null);
            } else{
                krbBinding = (AuthenticationTokenPolicy.KerberosTokenBinding)keyBinding;
            }
            this.dataEncKey = krbBinding.getSecretKey();
            builder = new KerberosTokenBuilder(context, krbBinding);
            BuilderResult ktbResult = builder.process();
            ktbResult.setDataProtectionKey(dataEncKey);
            return ktbResult;
            
        } else if (PolicyTypeUtil.symmetricKeyBinding(keyBinding)) {
            SymmetricKeyBinding skb = null;
            if (context.getSymmetricKeyBinding() != null) {
                skb = context.getSymmetricKeyBinding();
                context.setSymmetricKeyBinding(null);
            } else {
                skb = (SymmetricKeyBinding)keyBinding;
            }
            builder = new SymmetricTokenBuilder(skb,context,dataEncAlgo,keyEncAlgo);
            BuilderResult skbResult = builder.process();
            this.dataEncKey = skbResult.getDataProtectionKey();
            keyInfo = (com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo)skbResult.getKeyInfo();
            return skbResult;
            
        }else if(PolicyTypeUtil.derivedTokenKeyBinding(keyBinding)){
            DerivedTokenKeyBinding dtk = (DerivedTokenKeyBinding)keyBinding;
            ((NamespaceContextEx)context.getNamespaceContext()).addSCNS();
            builder = new DerivedKeyTokenBuilder(context, dtk);
            BuilderResult dtkResult = builder.process();
            //dtkResult.setEncryptedKey(null);
            return dtkResult;
        } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(keyBinding)){
            ((NamespaceContextEx)context.getNamespaceContext()).addSCNS();
            SCTBuilder sctBuilder = new SCTBuilder(context,(SecureConversationTokenKeyBinding)keyBinding);
            BuilderResult sctResult = sctBuilder.process();
            return sctResult;
        }else if ( PolicyTypeUtil.issuedTokenKeyBinding(keyBinding)) {            
            IssuedTokenBuilder itb = new IssuedTokenBuilder(context,(IssuedTokenKeyBinding)keyBinding);
            BuilderResult itbResult = itb.process();
            return itbResult;
        }else if (PolicyTypeUtil.samlTokenPolicy(keyBinding)) {            
            ((NamespaceContextEx)context.getNamespaceContext()).addSAMLNS();
            SamlTokenBuilder stb = new SamlTokenBuilder(context,(AuthenticationTokenPolicy.SAMLAssertionBinding)keyBinding,false);
            return stb.process();
        }else{
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1903_UNSUPPORTED_KEYBINDING_ENCRYPTIONPOLICY(keyBinding));
            throw new UnsupportedOperationException("Unsupported Key Binding"+keyBinding);
        }
    }
}
