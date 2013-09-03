/*
 * WSSKeyInfoFactory.java
 *
 * Created on August 2, 2006, 3:54 PM
 */

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

package com.sun.xml.ws.security.opt.impl.util;

import com.sun.org.apache.xml.internal.security.algorithms.JCEMapper;

import com.sun.xml.security.core.dsig.KeyInfoType;
import com.sun.xml.security.core.dsig.TransformType;
import com.sun.xml.security.core.xenc.CipherDataType;
import com.sun.xml.security.core.xenc.CipherReferenceType;
import com.sun.xml.security.core.xenc.EncryptedDataType;
import com.sun.xml.security.core.xenc.EncryptedKeyType;
import com.sun.xml.security.core.xenc.EncryptionMethodType;

import com.sun.xml.security.core.xenc.TransformsType;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.security.opt.api.EncryptedData;
import com.sun.xml.ws.security.opt.api.EncryptedKey;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.impl.enc.EncryptedHeader;
import com.sun.xml.ws.security.opt.impl.keyinfo.*;
import com.sun.xml.ws.security.opt.impl.message.GSHeaderElement;
import com.sun.xml.ws.security.opt.impl.reference.DirectReference;
import com.sun.xml.ws.security.opt.api.reference.Reference;
import com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier;
import com.sun.xml.ws.security.opt.impl.reference.X509Data;
import com.sun.xml.ws.security.secconv.impl.bindings.DerivedKeyTokenType;
import com.sun.xml.ws.security.secext10.BinarySecurityTokenType;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyName;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyValue;
import com.sun.xml.ws.security.opt.impl.crypto.AttachmentData;
import com.sun.xml.ws.security.opt.impl.enc.JAXBEncryptedData;
import com.sun.xml.ws.security.opt.impl.enc.JAXBEncryptedKey;
import com.sun.xml.ws.security.secext11.EncryptedHeaderType;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;

import java.net.URI;
import java.security.Key;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import com.sun.xml.ws.security.opt.impl.outgoing.SecurityHeader;
import com.sun.xml.ws.security.secconv.impl.bindings.SecurityContextTokenType;
import javax.xml.bind.JAXBElement;
import javax.xml.crypto.Data;

import com.sun.xml.ws.security.opt.impl.reference.X509IssuerSerial;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class WSSElementFactory {
    
    static {
        com.sun.org.apache.xml.internal.security.Init.init();
        
    }
    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    
    public static final com.sun.xml.security.core.xenc.ObjectFactory eoFactory = new com.sun.xml.security.core.xenc.ObjectFactory();
    /** Creates a new instance of WSSKeyInfoFactory */
    public WSSElementFactory(SOAPVersion soapVersion) {
        this.soapVersion = soapVersion;
    }
    
    
    public SecurityHeader createSecurityHeader(){
        return new SecurityHeader();
    }
    
    public SecurityHeader createSecurityHeader(int headerLayout, String soapVersion, boolean mustUnderstandValue){
        return new SecurityHeader(headerLayout, soapVersion, mustUnderstandValue);
    }
    
    /**
     * Create a BinarySecurity Token Header element.
     *
     */
    public BinarySecurityToken createBinarySecurityToken(String id,String valueType,String encodingType,byte[] token){
        BinarySecurityTokenType bst = new BinarySecurityTokenType();
        bst.setValueType(valueType);
        bst.setId(id);
        bst.setEncodingType(encodingType);
        //bst.setValue(Base64.encode(cert));
        bst.setValue(token);
        return new BinarySecurityToken(bst,soapVersion);
    }
        
     /**
      * Create a BinarySecurity Token Header element.
      *
      */
    public BinarySecurityToken createBinarySecurityToken(String id,byte[] cer){
        return createBinarySecurityToken(id,MessageConstants.X509v3_NS,MessageConstants.BASE64_ENCODING_NS,cer);
    }       
    
    /**
     * Create a Kerberos Binary Security Token
     * 
     */
    public BinarySecurityToken createKerberosBinarySecurityToken(String id, byte[] token){
        return createBinarySecurityToken(id, MessageConstants.KERBEROS_V5_GSS_APREQ, MessageConstants.BASE64_ENCODING_NS, token);
    }
    
    /**
     *Create a SecurityTokenReference
     *
     */
    public SecurityTokenReference createSecurityTokenReference(Reference reference){
        SecurityTokenReference str = new SecurityTokenReference(soapVersion);
        str.setReference(reference);
        return str;
    }
    
    public SecurityTokenReference createSecurityTokenReference(){
        SecurityTokenReference str = new SecurityTokenReference(soapVersion);        
        return str;
    }
    
    /**
     * Creates a DirectReference element
     *
     */
    public DirectReference createDirectReference(){
        return new DirectReference(soapVersion);
    }
    
    public KeyIdentifier createKeyIdentifier(){
        return new KeyIdentifier(soapVersion);
    }
    
    public X509Data createX509DataWithIssuerSerial(X509IssuerSerial xis){
        X509Data x509Data = new X509Data(soapVersion);
        List<Object> list = new ArrayList<Object>();
        list.add(xis);
        x509Data.setX509IssuerSerialOrX509SKIOrX509SubjectName(list);
        return x509Data;
    }
    
    public GSHeaderElement createGSHeaderElement(JAXBElement el){
        return new GSHeaderElement(el, soapVersion);
    }
    
    public GSHeaderElement createGSHeaderElement(Object obj){
        return new GSHeaderElement(obj, soapVersion);
    }
    
    public SecurityContextToken createSecurityContextToken(URI identifier, String instance, String wsuId){
        return new SecurityContextToken(identifier, instance, wsuId, soapVersion);
    }
    
    public SecurityContextToken createSecurityContextToken(SecurityContextTokenType sTokenType, String wsuId){
        return new SecurityContextToken(sTokenType, soapVersion);
    }
    
    public X509IssuerSerial createX509IssuerSerial(String issuerName, BigInteger serialNumber){
        X509IssuerSerial xis = new X509IssuerSerial(soapVersion);
        xis.setX509IssuerName(issuerName);
        xis.setX509SerialNumber(serialNumber);
        
        return xis;
    }
    
    public KeyInfo createKeyInfo(SecurityTokenReference str){
        KeyInfo keyInfo = new KeyInfo();
        JAXBElement je = new com.sun.xml.ws.security.secext10.ObjectFactory().createSecurityTokenReference(str);
        List strList = Collections.singletonList(je);
        keyInfo.setContent(strList);
        return keyInfo;
    }
    
    public KeyInfo createKeyInfo(KeyValue keyValue){
        KeyInfo keyInfo = new KeyInfo();
        JAXBElement je = new com.sun.xml.security.core.dsig.ObjectFactory().createKeyValue(keyValue);
        List strList = Collections.singletonList(je);
        keyInfo.setContent(strList);
        return keyInfo;
    }
    
    public KeyInfo createKeyInfo(KeyName name){
        KeyInfo keyInfo = new KeyInfo();
        List strList = Collections.singletonList(name);
        keyInfo.setContent(strList);
        return keyInfo;
    }
    
    
    public EncryptedData createEncryptedData(String id,Data data,String dataAlgo,KeyInfoType keyInfo,Key key,boolean contentOnly){
        EncryptedDataType edt = new EncryptedDataType();
        if(contentOnly){
            edt.setType(MessageConstants.ENCRYPT_ELEMENT_CONTENT);
        }else{
            edt.setType(MessageConstants.ENCRYPT_ELEMENT);
        }
        EncryptionMethodType emt = new EncryptionMethodType();
        emt.setAlgorithm(dataAlgo);
        edt.setEncryptionMethod(emt);
        CipherDataType ct = new CipherDataType();
        ct.setCipherValue("ed".getBytes());
        edt.setCipherData(ct);
        edt.setId(id);
        if(keyInfo != null){
            edt.setKeyInfo(keyInfo);
        }
        return new JAXBEncryptedData(edt,data,key,soapVersion);
    }
    
    public EncryptedData createEncryptedData(String id, Attachment attachment, String dataAlgo, KeyInfoType keyInfo, Key key, EncryptionTarget target) {
        AttachmentData attachData = new AttachmentData(attachment);
        String cid = "cid:" + attachment.getContentId();
        boolean contentOnly = target.getContentOnly();
        EncryptedDataType edt = new EncryptedDataType();
        if(contentOnly){
            edt.setType(MessageConstants.SWA11_ATTACHMENT_CONTENT_ONLY);
        }else{
            edt.setType(MessageConstants.SWA11_ATTACHMENT_COMPLETE);
        }
        edt.setMimeType(attachment.getContentType());
        EncryptionMethodType emt = new EncryptionMethodType();
        emt.setAlgorithm(dataAlgo);
        edt.setEncryptionMethod(emt);
        CipherDataType ct = new CipherDataType();
        CipherReferenceType crt = new CipherReferenceType();
        crt.setURI(cid);
        TransformsType tst = new TransformsType();
        ArrayList<TransformType> ttList = new ArrayList<TransformType>();
        ArrayList list = target.getCipherReferenceTransforms();
        for(Object obj : list){
            EncryptionTarget.Transform tr = (EncryptionTarget.Transform)obj;
            TransformType tt = new TransformType();
            tt.setAlgorithm(tr.getTransform());
            ttList.add(tt);
        }
        if(!ttList.isEmpty()){
            tst.setTransform(ttList);
        }
        crt.setTransforms(tst);
        ct.setCipherReference(crt);
        edt.setCipherData(ct);
        edt.setId(id);
        if(keyInfo != null){
            edt.setKeyInfo(keyInfo);
        }
        return new JAXBEncryptedData(edt,attachData,key,soapVersion);
    }
    
    public EncryptedHeader createEncryptedHeader(String ehId, String edId,Data data,String dataAlgo,KeyInfoType keyInfo,Key key,boolean contentOnly){
        EncryptedHeaderType eht = new EncryptedHeaderType();
        EncryptedDataType edt = new EncryptedDataType();
        if(contentOnly){
            // TODO: not valid, need to throw exception here
            edt.setType(MessageConstants.ENCRYPT_ELEMENT_CONTENT);
        }else{
            edt.setType(MessageConstants.ENCRYPT_ELEMENT);
        }
        EncryptionMethodType emt = new EncryptionMethodType();
        emt.setAlgorithm(dataAlgo);
        edt.setEncryptionMethod(emt);
        CipherDataType ct = new CipherDataType();
        ct.setCipherValue("ed".getBytes());
        edt.setCipherData(ct);
        edt.setId(edId);
        if(keyInfo != null){
            edt.setKeyInfo(keyInfo);
        }
        eht.setEncryptedData(edt);
        eht.setId(ehId);
        if(soapVersion == SOAPVersion.SOAP_11) {
            eht.setMustUnderstand(true);
        }else if(soapVersion == SOAPVersion.SOAP_12){
            eht.setMustUnderstand12(true);
        }
        EncryptedHeader eh =  new EncryptedHeader(eht, data, key, soapVersion);
        return eh;
    }
    
    public EncryptedKey createEncryptedKey(String id , String keyEncAlgo,KeyInfo keyInfo,Key dkEK, Key dataEncKey) throws XWSSecurityException{
        EncryptedKeyType ekt = eoFactory.createEncryptedKeyType();
        EncryptionMethodType emt = eoFactory.createEncryptionMethodType();
        emt.setAlgorithm(keyEncAlgo);
        ekt.setEncryptionMethod(emt);
        ekt.setKeyInfo(keyInfo);
        CipherDataType ct = new CipherDataType();
        ct.setCipherValue("ek".getBytes());
        ekt.setCipherData(ct);
        ekt.setId(id);
        return new JAXBEncryptedKey(ekt,dkEK,dataEncKey,soapVersion);
    }
    
    public JAXBElement<com.sun.xml.security.core.xenc.ReferenceType> createDataReference(SecurityElement se ){
        com.sun.xml.security.core.xenc.ReferenceType rt = eoFactory.createReferenceType();
        rt.setURI("#"+se.getId());
        return eoFactory.createReferenceListDataReference(rt);
        
    }
    
    public KeyInfoType createKeyInfoType(String keyAlgo,String refType,String refId){
        return new KeyInfoType();
    }
    
    public DerivedKey createDerivedKey(String id,String algo,byte[] nonce,long offset,long length,String label,SecurityTokenReference str, String spVersion){
        DerivedKeyTokenType dkt = new DerivedKeyTokenType();
        com.sun.xml.ws.security.secconv.impl.wssx.bindings.DerivedKeyTokenType dkt13 = new com.sun.xml.ws.security.secconv.impl.wssx.bindings.DerivedKeyTokenType();
        if(spVersion.equals(MessageConstants.SECURITYPOLICY_12_NS)){
            dkt13.setId(id);
            dkt13.setOffset(BigInteger.valueOf(offset));
            dkt13.setNonce(nonce);
            dkt13.setLength(BigInteger.valueOf(length));
            dkt13.setSecurityTokenReference(str);
            //dkt.setLabel(label);
            //dkt.setAlgorithm(algo);
            return new DerivedKey(dkt13,soapVersion, spVersion);
        }else{
            dkt.setId(id);
            dkt.setOffset(BigInteger.valueOf(offset));
            dkt.setNonce(nonce);
            dkt.setLength(BigInteger.valueOf(length));
            dkt.setSecurityTokenReference(str);
            //dkt.setLabel(label);
            //dkt.setAlgorithm(algo);
            return new DerivedKey(dkt,soapVersion, spVersion);
        }
    }
    
    public DerivedKey createDerivedKey(String id,String algo,byte[] nonce,long offset,long length,String label,SecurityTokenReferenceType str, String spVersion){
        DerivedKeyTokenType dkt = new DerivedKeyTokenType();
        com.sun.xml.ws.security.secconv.impl.wssx.bindings.DerivedKeyTokenType dkt13 = new com.sun.xml.ws.security.secconv.impl.wssx.bindings.DerivedKeyTokenType();
        if(spVersion.equals(MessageConstants.SECURITYPOLICY_12_NS)){
            dkt13.setId(id);
            dkt13.setOffset(BigInteger.valueOf(offset));
            dkt13.setNonce(nonce);
            dkt13.setLength(BigInteger.valueOf(length));
            dkt13.setSecurityTokenReference(str);
            //dkt.setLabel(label);
            //dkt.setAlgorithm(algo);
            return new DerivedKey(dkt13,soapVersion, spVersion);
        }else{
            dkt.setId(id);
            dkt.setOffset(BigInteger.valueOf(offset));
            dkt.setNonce(nonce);
            dkt.setLength(BigInteger.valueOf(length));
            dkt.setSecurityTokenReference(str);
            //dkt.setLabel(label);
            //dkt.setAlgorithm(algo);
            return new DerivedKey(dkt,soapVersion, spVersion);
        }
    }
    
    public DerivedKey createDerivedKey(String id,String algo,byte[] nonce,long offset,long length,String label,SecurityTokenReferenceType str,String refId, String spVersion){
        DerivedKeyTokenType dkt = new DerivedKeyTokenType();
        com.sun.xml.ws.security.secconv.impl.wssx.bindings.DerivedKeyTokenType dkt13 = new com.sun.xml.ws.security.secconv.impl.wssx.bindings.DerivedKeyTokenType();
        if(spVersion.equals(MessageConstants.SECURITYPOLICY_12_NS)){
            dkt13.setId(id);
            dkt13.setOffset(BigInteger.valueOf(offset));
            dkt13.setNonce(nonce);
            dkt13.setLength(BigInteger.valueOf(length));
            dkt13.setSecurityTokenReference(str);
            //dkt.setLabel(label);
            //dkt.setAlgorithm(algo);
            return new DerivedKey(dkt13,soapVersion,refId,spVersion);
        }else{
            dkt.setId(id);
            dkt.setOffset(BigInteger.valueOf(offset));
            dkt.setNonce(nonce);
            dkt.setLength(BigInteger.valueOf(length));
            dkt.setSecurityTokenReference(str);
            //dkt.setLabel(label);
            //dkt.setAlgorithm(algo);
            return new DerivedKey(dkt,soapVersion,refId,spVersion);
        }
    }
    
    protected String convertAlgURIToTransformation(String algorithmURI) {
        return JCEMapper.translateURItoJCEID(algorithmURI);
    }
}
