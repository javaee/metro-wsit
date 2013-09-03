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

import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.keyinfo.BinarySecurityToken;
import com.sun.xml.ws.security.opt.api.keyinfo.Token;
import com.sun.xml.ws.security.opt.api.reference.Reference;
import com.sun.xml.ws.security.opt.impl.crypto.SSEData;
import com.sun.xml.ws.security.opt.impl.outgoing.SecurityHeader;
import com.sun.xml.ws.security.opt.impl.reference.DirectReference;
import com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier;
import com.sun.xml.ws.security.opt.impl.util.WSSElementFactory;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyName;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyValue;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.RSAKeyValue;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.impl.opt.token.LogStringsMessages;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.crypto.Data;

/**
 *
 * @author K.Venugopal@sun.com
 */
public abstract class TokenBuilder implements com.sun.xml.ws.security.opt.api.keyinfo.TokenBuilder{
    
    protected static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_TOKEN_DOMAIN,
            LogDomainConstants.IMPL_OPT_TOKEN_DOMAIN_BUNDLE);
    
    protected JAXBFilterProcessingContext context = null;
    protected SecurityHeader securityHeader = null;
    protected WSSElementFactory elementFactory = null;
    
    protected KeyInfo keyInfo = null;
    /** Creates a new instance of TokenBuilder */
    public TokenBuilder(JAXBFilterProcessingContext context) {
        this.context = context;
        this.securityHeader = context.getSecurityHeader();
        this.elementFactory = new WSSElementFactory(context.getSOAPVersion());
    }
    
    protected BinarySecurityToken createBinarySecurityToken(AuthenticationTokenPolicy.X509CertificateBinding binding,X509Certificate x509Cert) throws XWSSecurityException{
        if(binding.INCLUDE_NEVER.equals(binding.getIncludeToken()))
            return null;
        String id = getID(binding);
        
        if(logger.isLoggable(Level.FINEST)){
            logger.log(Level.FINEST, "X509 Token id: "+id);
        }
        
        Token token = (Token)securityHeader.getChildElement(id);
        if(token != null){
            if(token instanceof BinarySecurityToken){
                return (BinarySecurityToken)token;
            }
            throw new XWSSecurityException("Found two tokens with same Id attribute");
        }
        BinarySecurityToken bst;
        try {
            bst = elementFactory.createBinarySecurityToken(id, x509Cert.getEncoded());
        } catch (CertificateEncodingException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1801_BST_CREATION_FAILED());
            throw new XWSSecurityException("Error occured while constructing BinarySecurityToken",ex);
        }
        context.getSecurityHeader().add((SecurityHeaderElement)bst);
        return bst;
    }
    
    protected BinarySecurityToken createKerberosBST(AuthenticationTokenPolicy.KerberosTokenBinding binding, 
            byte[] kerbToken) throws XWSSecurityException{
        if(binding.INCLUDE_NEVER.equals(binding.getIncludeToken()))
            return null;
        
        String id = getID(binding);
        
        if(logger.isLoggable(Level.FINEST)){
            logger.log(Level.FINEST, "Kerberos Token id: "+id);
        }
        
        Token token = (Token)securityHeader.getChildElement(id);
        if(token != null){
            if(token instanceof BinarySecurityToken){
                return (BinarySecurityToken)token;
            }
            throw new XWSSecurityException("Found two tokens with same Id attribute");
        }
        BinarySecurityToken bst;
        bst = elementFactory.createKerberosBinarySecurityToken(id, kerbToken);
        context.getSecurityHeader().add((SecurityHeaderElement)bst);
        return bst;
    }
    
    protected SecurityTokenReference buildSTR(String strId, Reference ref){
        SecurityTokenReference str = elementFactory.createSecurityTokenReference(ref);
        if(context.getSecurityPolicy() instanceof SignaturePolicy)
            ((SecurityElement)str).setId(strId);
        Data data = new SSEData((SecurityElement)str,false,context.getNamespaceContext());
        context.getElementCache().put(strId,data);
        return str;
    }
    
    protected SecurityTokenReference buildSTR(Reference ref){
        SecurityTokenReference str = elementFactory.createSecurityTokenReference(ref);
        return str;
    }
    
    protected KeyInfo buildKeyInfo(Reference ref,String strId){
        keyInfo = elementFactory.createKeyInfo(buildSTR(strId,ref));
        return keyInfo;
    }
    
    protected KeyInfo buildKeyInfo(com.sun.xml.ws.security.opt.impl.keyinfo.SecurityTokenReference str){
        keyInfo = elementFactory.createKeyInfo(str);
        return keyInfo;
    }
    
    protected KeyInfo buildKeyInfo(PublicKey pubKey){
        keyInfo = elementFactory.createKeyInfo(buildKeyValue(pubKey));
        return keyInfo;
    }
    
    protected KeyValue buildKeyValue(PublicKey pubKey){
        KeyValue kv = new KeyValue();
        RSAKeyValue rsaKV = new RSAKeyValue(pubKey);
        JAXBElement je = new com.sun.xml.security.core.dsig.ObjectFactory().createRSAKeyValue(rsaKV);
        List strList = Collections.singletonList(je);
        kv.setContent(strList);        
        return kv;
    }
    
    protected KeyInfo buildKIWithKeyName(String name){
        KeyName kn = new KeyName();
        kn.setKeyName(name);
        keyInfo = elementFactory.createKeyInfo(kn);
        return keyInfo;
    }
    
    protected DirectReference buildDirectReference(String id, String valueType){
        DirectReference dr = elementFactory.createDirectReference();
        dr.setURI("#"+id);
        if(valueType != null){
            dr.setValueType(valueType);
        }
        return dr;
    }
    
    protected KeyIdentifier buildKeyInfoWithKI(AuthenticationTokenPolicy.X509CertificateBinding binding, String refType) throws XWSSecurityException{
        KeyIdentifier keyIdentifier = elementFactory.createKeyIdentifier();
        //keyIdentifier.setValue(binding.getCertificateIdentifier());
        keyIdentifier.setValueType(refType);
        keyIdentifier.updateReferenceValue(binding.getX509Certificate());
        keyIdentifier.setEncodingType(MessageConstants.BASE64_ENCODING_NS);
        if(keyIdentifier.getValue() == null || keyIdentifier.getValue().length() ==0){
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1852_KEY_IDENTIFIER_EMPTY());
            throw new XWSSecurityException(LogStringsMessages.WSS_1852_KEY_IDENTIFIER_EMPTY());
        }
        buildKeyInfo(keyIdentifier,binding.getSTRID());
        return keyIdentifier;
    }
    
    protected KeyIdentifier buildKeyInfoWithKIKerberos(AuthenticationTokenPolicy.KerberosTokenBinding binding, String refType) throws XWSSecurityException{
        KeyIdentifier keyIdentifier = elementFactory.createKeyIdentifier();
        keyIdentifier.setValueType(refType);
        keyIdentifier.updateReferenceValue(binding.getTokenValue());
        keyIdentifier.setEncodingType(MessageConstants.BASE64_ENCODING_NS);
        if(keyIdentifier.getValue() == null || keyIdentifier.getValue().length() ==0){
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1852_KEY_IDENTIFIER_EMPTY());
            throw new XWSSecurityException(LogStringsMessages.WSS_1852_KEY_IDENTIFIER_EMPTY());
        }
        buildKeyInfo(keyIdentifier,binding.getSTRID());
        return keyIdentifier;
    }
    
    
    protected KeyIdentifier buildKeyInfoWithEKSHA1(String ekSHA1Ref){
        KeyIdentifier keyIdentifier = elementFactory.createKeyIdentifier();
        keyIdentifier.setValueType(MessageConstants.EncryptedKeyIdentifier_NS);
        keyIdentifier.setEncodingType(MessageConstants.BASE64_ENCODING_NS);
        keyIdentifier.setReferenceValue(ekSHA1Ref);
        buildKeyInfo(keyIdentifier,null);
        return keyIdentifier;
    }
    
    protected String getID(WSSPolicy policy){
        String id = policy.getUUID();
        if(id == null || id.length() == 0){
            return context.generateID();
        }
        return id;
    }
    
    public javax.xml.crypto.dsig.keyinfo.KeyInfo getKeyInfo() {
        return keyInfo;
    }
    
}
