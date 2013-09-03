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

package com.sun.xml.ws.security.opt.impl.keyinfo;

import com.sun.xml.ws.security.opt.api.EncryptedKey;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.keyinfo.BuilderResult;
import com.sun.xml.ws.security.opt.impl.enc.JAXBEncryptedKey;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.security.Key;
import java.security.MessageDigest;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.logging.Level;
import com.sun.xml.wss.logging.impl.opt.token.LogStringsMessages;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SymmetricTokenBuilder extends TokenBuilder {
    
    private Key dataProtectionKey = null;
    private Key keyProtectionKey = null;
    private SymmetricKeyBinding binding = null;
    private String dataProtectionAlg;
    private String keyProtectionAlg;
    
    /** Creates a new instance of SymmetricTokenBuilder */
    public SymmetricTokenBuilder( SymmetricKeyBinding binding, JAXBFilterProcessingContext context,String dpAlgo,String kpAlgo) {
        super(context);
        this.binding = binding;
        this.dataProtectionAlg = dpAlgo;
        this.keyProtectionAlg = kpAlgo;
    }
    
    public BuilderResult process() throws XWSSecurityException {
        
        //TODO : Fix me
        boolean wss11Receiver = "true".equals(context.getExtraneousProperty("EnableWSS11PolicyReceiver"));
        boolean wss11Sender = "true".equals(context.getExtraneousProperty("EnableWSS11PolicySender"));
        boolean sendEKSHA1 =  wss11Receiver && wss11Sender;
        boolean wss10 = !wss11Sender;
        ((NamespaceContextEx)context.getNamespaceContext()).addEncryptionNS();
        if(sendEKSHA1){
            if(context.getExtraneousProperty(MessageConstants.SECRET_KEY_VALUE) == null){
                sendEKSHA1 = false;
            }
        }
        BuilderResult stbResult = new BuilderResult();
        
        WSSPolicy ckBinding = (WSSPolicy)binding.getKeyBinding();
        
        if(!PolicyTypeUtil.kerberosTokenBinding(ckBinding)){
            if(!binding.getKeyIdentifier().equals(MessageConstants._EMPTY)){
                
                if(keyProtectionAlg != null && !"".equals(keyProtectionAlg)){
                    dataProtectionKey = SecurityUtil.generateSymmetricKey(dataProtectionAlg);
                }
                
                keyProtectionKey = binding.getSecretKey();
                if(dataProtectionKey == null){
                    dataProtectionKey = keyProtectionKey;
                    keyProtectionKey = null;
                    buildKIWithKeyName(binding.getKeyIdentifier());
                }
                stbResult.setKeyInfo(super.keyInfo);
                stbResult.setDataProtectionKey(dataProtectionKey);
            } else if (sendEKSHA1) {
                //get the signing key and EKSHA1 reference from the Subject, it was stored from the incoming message
                String ekSha1Ref = (String)context.getExtraneousProperty(MessageConstants.EK_SHA1_VALUE);
                buildKeyInfoWithEKSHA1(ekSha1Ref);
                dataProtectionKey = binding.getSecretKey();
                stbResult.setKeyInfo(super.keyInfo);
                stbResult.setDataProtectionKey(dataProtectionKey);
            } else if (wss11Sender || wss10) {
                dataProtectionKey = binding.getSecretKey();
                //TODO :: REMOVE ONCE THE CHANGE IS MADE IN FITERS
                AuthenticationTokenPolicy.X509CertificateBinding certificateBinding = null;
                if(!binding.getCertAlias().equals(MessageConstants._EMPTY)){
                    certificateBinding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    //x509Binding.newPrivateKeyBinding();
                    certificateBinding.setCertificateIdentifier(binding.getCertAlias());
                    X509Certificate x509Cert = context.getSecurityEnvironment().getCertificate(context.getExtraneousProperties(), certificateBinding.getCertificateIdentifier(), false);
                    certificateBinding.setX509Certificate(x509Cert);
                    certificateBinding.setReferenceType("Direct");
                }else if ( context.getX509CertificateBinding() != null ) {
                    certificateBinding = context.getX509CertificateBinding();
                    context.setX509CertificateBinding(null);
                }else{
                    throw new XWSSecurityException("Internal Error: X509CertificateBinding not set on context");
                }
                
                X509Certificate x509Cert = certificateBinding.getX509Certificate();
                String x509TokenId = certificateBinding.getUUID();
                if(x509TokenId == null || x509TokenId.equals("")){
                    x509TokenId = context.generateID();
                }
                
                SecurityUtil.checkIncludeTokenPolicyOpt(context, certificateBinding, x509TokenId);
                
                if(logger.isLoggable(Level.FINEST)){
                    logger.log(Level.FINEST, "Certificate for SymmetricBinding is: "+x509Cert);
                    logger.log(Level.FINEST, "BinaryToken ID for SymmetricBinding is: "+x509TokenId);
                }
                BinarySecurityToken bst = null;
                SecurityHeaderElement ek = null;
                
                HashMap ekCache = context.getEncryptedKeyCache();
                String ekId = (String) ekCache.get(x509TokenId);
                
                keyProtectionKey = x509Cert.getPublicKey();
                if(ekId == null){
                    
                    TokenBuilder builder = new X509TokenBuilder(context,certificateBinding);
                    BuilderResult result = builder.process();
                    KeyInfo ekKI  = (com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo) result.getKeyInfo();
                    context.setExtraneousProperty("SecretKey", dataProtectionKey);
                    ek = (SecurityHeaderElement)elementFactory.createEncryptedKey(context.generateID(),keyProtectionAlg,ekKI,keyProtectionKey,dataProtectionKey);
                    context.getSecurityHeader().add(ek);
                    ekId = ek.getId();
                    ekCache.put(x509TokenId, ekId);
                    context.addToCurrentSecretMap(ekId, dataProtectionKey);
                    stbResult.setEncryptedKey((EncryptedKey)ek);
                    //store EKSHA1 of KeyValue contents in context
                    try{
                        byte[] cipherVal = ((JAXBEncryptedKey)ek).getCipherValue();
                        byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(cipherVal);
                        String encEkSha1 = Base64.encode(ekSha1);
                        context.setExtraneousProperty("EncryptedKeySHA1", encEkSha1);
                    } catch(java.security.NoSuchAlgorithmException nsa){
                        throw new XWSSecurityException(nsa);
                    }
                } else{
                    //skbX509TokenInserted = true;
                    //ekId = (String)ekCache.get(x509TokenId);
                    if(ekId == null || ekId.length() ==0){
                        logger.log(Level.SEVERE, LogStringsMessages.WSS_1804_WRONG_ENCRYPTED_KEY());
                        throw new XWSSecurityException("Invalid EncryptedKey Id ");
                    }
                    dataProtectionKey = context.getCurrentSecretFromMap(ekId);
                }
                String valType = null;
                if(wss11Sender){
                    valType = MessageConstants.EncryptedKey_NS;
                }
                com.sun.xml.ws.security.opt.api.keyinfo.SecurityTokenReference str = buildSTR(certificateBinding.getUUID(),buildDirectReference(ekId, valType));
                //str.setTokenType(MessageConstants.EncryptedKey_NS);
                buildKeyInfo((SecurityTokenReference) str);
                stbResult.setDataProtectionKey(dataProtectionKey);
                stbResult.setKeyInfo(super.keyInfo);
                stbResult.setEncryptedKey((EncryptedKey)ek);
            }
        } else{
            AuthenticationTokenPolicy.KerberosTokenBinding krbBinding = null;
            if(context.getKerberosTokenBinding() != null){
                krbBinding = context.getKerberosTokenBinding();
                context.setKerberosTokenBinding(null);
                
                dataProtectionKey = krbBinding.getSecretKey();
                TokenBuilder builder = new KerberosTokenBuilder(context, krbBinding);
                stbResult = builder.process();
                stbResult.setDataProtectionKey(dataProtectionKey);
            } else{
                throw new XWSSecurityException("Internal error: Kerberos Binding not set on context");
            }
        }
        return stbResult;
    }
    
}