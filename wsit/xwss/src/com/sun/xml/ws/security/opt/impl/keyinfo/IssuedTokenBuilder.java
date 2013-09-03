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

import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.keyinfo.BuilderResult;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.ws.security.opt.impl.message.GSHeaderElement;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import java.security.Key;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBElement;
import org.w3c.dom.Element;
import com.sun.xml.wss.logging.impl.opt.token.LogStringsMessages;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class IssuedTokenBuilder extends TokenBuilder {
    private IssuedTokenKeyBinding ikb = null;
    /** Creates a new instance of IssuedTokenBuilder */
    public IssuedTokenBuilder(JAXBFilterProcessingContext context,IssuedTokenKeyBinding kb) {
        super(context);
        this.ikb = kb;
    }
    
    public BuilderResult process() throws XWSSecurityException {
        BuilderResult itkbResult = new BuilderResult();
        byte[] proofKey = context.getTrustContext().getProofKey();
        Key dataProtectionKey = null;
        SecurityTokenReferenceType str = null;
        Key cacheKey = null;        
        //For Encryption proofKey will be null.
        if (proofKey == null) {
             KeyPair keyPair = context.getTrustContext().getProofKeyPair();
             if (keyPair == null){
                X509Certificate cert =
                        context.getTrustContext().getRequestorCertificate();
                if (cert == null){
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1823_KEY_PAIR_PROOF_KEY_NULL_ISSUEDTOKEN());
                    throw new XWSSecurityException(
                        "Proof Key and RSA KeyPair for Supporting token (KeyValueToken or RsaToken) are both null for Issued Token");
                }else{
                    dataProtectionKey = context.getSecurityEnvironment().getPrivateKey(context.getExtraneousProperties(), cert);
                    cacheKey = cert.getPublicKey();
                }
            }else{
                dataProtectionKey = keyPair.getPrivate();
                cacheKey = keyPair.getPublic();
            }
        }else{
            String secretKeyAlg = "AES";
            if (context.getAlgorithmSuite() != null) {
                secretKeyAlg = SecurityUtil.getSecretKeyAlgorithm(context.getAlgorithmSuite().getEncryptionAlgorithm());
            }
            //TODO: assuming proofkey is a byte array in case of Trust as well
            dataProtectionKey = new SecretKeySpec(proofKey, secretKeyAlg);
            cacheKey = dataProtectionKey;
            //SecurityUtil.updateSamlVsKeyCache(str, context, dataProtectionKey);
        }
        
        SecurityHeaderElement issuedTokenElement = null;
        GenericToken issuedToken = (GenericToken)context.getTrustContext().getSecurityToken();
        if(issuedToken != null){
            issuedTokenElement = issuedToken.getElement();
            if(issuedTokenElement == null){
                Element element = (Element)issuedToken.getTokenValue();
                issuedTokenElement = new GSHeaderElement(element);
                issuedTokenElement.setId(issuedToken.getId());
                itkbResult.setDPTokenId(issuedToken.getId());
            }
            String tokId = issuedTokenElement.getId();
            if ("".equals(tokId) &&  MessageConstants.ENCRYPTED_DATA_LNAME.equals(issuedTokenElement.getLocalPart())) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1808_ID_NOTSET_ENCRYPTED_ISSUEDTOKEN());
                throw new XWSSecurityException("ID attribute not set");
            }
            context.getTokenCache().put(ikb.getUUID(), issuedTokenElement);
            
            HashMap sentSamlKeys = (HashMap) context.getExtraneousProperty(MessageConstants.STORED_SAML_KEYS);
            if(sentSamlKeys == null){
                sentSamlKeys = new HashMap();
            }
            sentSamlKeys.put(tokId, dataProtectionKey);
            context.setExtraneousProperty(MessageConstants.STORED_SAML_KEYS, sentSamlKeys);
        }
        boolean includeToken = (ikb.INCLUDE_ALWAYS.equals(ikb.getIncludeToken()) ||
                (ikb.INCLUDE_ALWAYS_TO_RECIPIENT.equals(ikb.getIncludeToken())));
        
        if (includeToken) {
            str = (SecurityTokenReferenceType)context.getTrustContext().
                    getAttachedSecurityTokenReference();
        }else{
            str = (SecurityTokenReferenceType)context.getTrustContext().
                    getUnAttachedSecurityTokenReference();
        }
        
        if (issuedToken != null && includeToken) {
            if( context.getSecurityHeader().getChildElement(issuedTokenElement.getId()) == null){
                context.getSecurityHeader().add(issuedTokenElement);
            }
        }
        
        keyInfo = new KeyInfo();
        JAXBElement je = new com.sun.xml.ws.security.secext10.ObjectFactory().createSecurityTokenReference(str);
        List strList = Collections.singletonList(je);
        keyInfo.setContent(strList);
        if(str != null)
            SecurityUtil.updateSamlVsKeyCache(str, context, cacheKey);
        itkbResult.setDataProtectionKey(dataProtectionKey);
        itkbResult.setKeyInfo(keyInfo);
        return itkbResult;
    }
}
