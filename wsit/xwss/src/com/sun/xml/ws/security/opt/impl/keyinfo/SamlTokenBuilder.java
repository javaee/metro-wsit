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

import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.keyinfo.BuilderResult;
import com.sun.xml.ws.security.opt.impl.enc.JAXBEncryptedKey;
import com.sun.xml.ws.security.opt.impl.incoming.SAMLAssertion;
import com.sun.xml.ws.security.opt.impl.reference.DirectReference;
import com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier;
import com.sun.xml.ws.security.opt.impl.message.GSHeaderElement;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.wss.impl.policy.mls.PrivateKeyBinding;
import com.sun.xml.wss.logging.impl.opt.token.LogStringsMessages;

import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.logging.Level;
import org.w3c.dom.Element;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SamlTokenBuilder extends TokenBuilder{
    
    private AuthenticationTokenPolicy.SAMLAssertionBinding keyBinding = null;
    private boolean forSign = false;
    /** Creates a new instance of SamlTokenProcessor */
    public SamlTokenBuilder(JAXBFilterProcessingContext context,AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding,boolean forSign) {
        super(context);
        this.forSign = forSign;
        this.keyBinding = samlBinding;
    }
    /**
     * 
     * @return BuilderResult
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public BuilderResult process() throws XWSSecurityException {
        BuilderResult result = new BuilderResult();
        String assertionId = null;
        
        SecurityHeaderElement she = null;
        
        Element samlAssertion = keyBinding.getAssertion();
        if(samlAssertion != null)
            she = new GSHeaderElement(samlAssertion);
        JAXBEncryptedKey ek  = null;
        String asID = "";
        String id = "";
        String keyEncAlgo = XMLCipher.RSA_v1dot5;
        X509Certificate x509Cert = null;
        Key samlkey = null;
        if(samlAssertion != null){
            asID = samlAssertion.getAttributeNS(null,"AssertionID");
            if(she == null){
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1811_NULL_SAML_ASSERTION());
                throw new XWSSecurityException("SAML Assertion is NULL");
            }
            if(asID == null || asID.length() ==0){
                id = samlAssertion.getAttributeNS(null,"ID");
                she.setId(id);
            }else{
                she.setId(asID);
            }
        }else{
            she = (SecurityHeaderElement) context.getExtraneousProperty(MessageConstants.INCOMING_SAML_ASSERTION);
            if(she == null){
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1811_NULL_SAML_ASSERTION());
                throw new XWSSecurityException("SAML Assertion is NULL");
            }
            asID = she.getId();
        }
        if(logger.isLoggable(Level.FINEST)){
            logger.log(Level.FINEST, "SAML Assertion id:"+asID);
        }
        
        Key dataProtectionKey = null;
        if(forSign){
            PrivateKeyBinding privKBinding  = (PrivateKeyBinding)keyBinding.getKeyBinding();
            dataProtectionKey = privKBinding.getPrivateKey();
            if (dataProtectionKey == null) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1810_NULL_PRIVATEKEY_SAML());
                throw new XWSSecurityException("PrivateKey null inside PrivateKeyBinding set for SAML Policy ");
            }
            
            if(context.getSecurityHeader().getChildElement(she.getId()) == null){
                context.getSecurityHeader().add(she);
            }
            
        }else{
            //Key key = null;
            //key = KeyResolver.resolveSamlAssertion(context.getSecurableSoapMessage(), samlBinding.getAssertion(), true, context, assertionID);
            
            SecurityHeaderElement assertion = (SecurityHeaderElement) context.getExtraneousProperty(MessageConstants.INCOMING_SAML_ASSERTION);
            samlkey = ((SAMLAssertion)assertion).getKey();
            /*
            x509Cert = context.getSecurityEnvironment().getCertificate(
                    context.getExtraneousProperties() ,(PublicKey)key, false);
            if (x509Cert == null) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1812_MISSING_CERT_SAMLASSERTION());
                throw new XWSSecurityException("Could not locate Certificate corresponding to Key in SubjectConfirmation of SAML Assertion");
            }
            */
            if (!"".equals(keyBinding.getKeyAlgorithm())) {
                keyEncAlgo = keyBinding.getKeyAlgorithm();
            }
            String dataEncAlgo = SecurityUtil.getDataEncryptionAlgo(context);
            dataProtectionKey = SecurityUtil.generateSymmetricKey(dataEncAlgo);
            
        }
        Element authorityBinding = keyBinding.getAuthorityBinding();
        //assertionId = keyBinding.getAssertionId();
        
        
        
        String referenceType = keyBinding.getReferenceType();
        if (referenceType.equals(MessageConstants.EMBEDDED_REFERENCE_TYPE)) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1813_UNSUPPORTED_EMBEDDEDREFERENCETYPE_SAML());
            throw new XWSSecurityException("Embedded Reference Type for SAML Assertions not supported yet");
        }
        
        assertionId = she.getId();
        
        //todo reference different keyreference types.
        SecurityTokenReference samlSTR = null;
        if(authorityBinding == null){
            KeyIdentifier keyIdentifier = new KeyIdentifier(context.getSOAPVersion());
            keyIdentifier.setValue(assertionId);
            if(MessageConstants.SAML_v2_0_NS.equals(she.getNamespaceURI())){
                keyIdentifier.setValueType(MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE);
            } else{
                keyIdentifier.setValueType(MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE);
            }
            samlSTR = elementFactory.createSecurityTokenReference(keyIdentifier);
            if(id != null){
                samlSTR.setTokenType(MessageConstants.WSSE_SAML_v2_0_TOKEN_TYPE);
            }else{
                samlSTR.setTokenType(MessageConstants.WSSE_SAML_v1_1_TOKEN_TYPE);
            }
            //((SecurityTokenReferenceType)samlSTR).getAny().add(authorityBinding);
            ((NamespaceContextEx)context.getNamespaceContext()).addWSS11NS();
            buildKeyInfo((SecurityTokenReference) samlSTR);
        } else{
            //TODO: handle authorityBinding != null
        }
        
        
        if(!forSign){
            HashMap ekCache = context.getEncryptedKeyCache();
            ek = (JAXBEncryptedKey)elementFactory.createEncryptedKey(context.generateID(),keyEncAlgo,super.keyInfo,samlkey,dataProtectionKey);
            context.getSecurityHeader().add(ek);
            String ekId = ek.getId();
            DirectReference dr = buildDirectReference(ekId,MessageConstants.EncryptedKey_NS);
            result.setKeyInfo(buildKeyInfo(dr,""));
        }else{
            result.setKeyInfo(super.keyInfo);
        }
        
        HashMap sentSamlKeys = (HashMap) context.getExtraneousProperty(MessageConstants.STORED_SAML_KEYS);
        if(sentSamlKeys == null)
            sentSamlKeys = new HashMap();
        sentSamlKeys.put(assertionId, dataProtectionKey);
        context.setExtraneousProperty(MessageConstants.STORED_SAML_KEYS, sentSamlKeys);
        
        result.setDataProtectionKey(dataProtectionKey);
        
        return result;
    }
    
}
