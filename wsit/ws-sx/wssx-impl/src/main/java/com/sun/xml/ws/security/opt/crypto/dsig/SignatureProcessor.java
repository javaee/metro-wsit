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

import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.util.NamespaceAndPrefixMapper;
import com.sun.xml.ws.security.opt.impl.util.WSSNamespacePrefixMapper;
import com.sun.xml.ws.security.opt.crypto.dsig.internal.HmacSHA1;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.c14n.AttributeNS;
import com.sun.xml.wss.impl.c14n.EXC14nStAXReaderBasedCanonicalizer;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import com.sun.xml.ws.security.opt.crypto.dsig.internal.SignerOutputStream;
import com.sun.xml.ws.security.opt.crypto.dsig.internal.MacOutputStream;
import org.jvnet.staxex.NamespaceContextEx;
import org.jvnet.staxex.XMLStreamReaderEx;
import java.util.Iterator;
import javax.xml.crypto.XMLCryptoContext;
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SignatureProcessor {
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    
    private JAXBContext _jaxbContext;
    StAXEXC14nCanonicalizerImpl _exc14nCanonicalizer = new StAXEXC14nCanonicalizerImpl();
    EXC14nStAXReaderBasedCanonicalizer _exc14nSBCanonicalizer;
    XMLCryptoContext context = null;
    private Signature _rsaSignature;
    private Signature _dsaSignature;
    //private Signature _hmacSignature;
    
    /**
     * Creates a new instance of SignatureProcessor
     */
    public SignatureProcessor() {
        
    }
    
    public void setJAXBContext(JAXBContext _jaxbContext) {
        this._jaxbContext = _jaxbContext;
    }
    
    public JAXBContext getJAXBContext() {
        return _jaxbContext;
    }
    
    public void setCryptoContext(XMLCryptoContext context){
        this.context = context;
    }
    
    public byte []  performRSASign(Key privateKey,SignedInfo signedInfo, String signatureAlgo) throws InvalidKeyException{
        if (privateKey == null || signedInfo == null) {
            throw new NullPointerException();
        }
        
        if (!(privateKey instanceof PrivateKey)) {
            throw new InvalidKeyException("key must be PrivateKey");
        }
        
        if(_rsaSignature == null ){
            try {
                _rsaSignature = Signature.getInstance(signatureAlgo);
            } catch (NoSuchAlgorithmException ex) {
                // shud never come here
                throw new XWSSecurityRuntimeException(ex);
            }
        }
        
        _rsaSignature.initSign((PrivateKey) privateKey);
        
        SignerOutputStream signerOutputStream = new SignerOutputStream(_rsaSignature);
        Marshaller marshaller;
        try {
            marshaller = getMarshaller();
            _exc14nCanonicalizer.reset();
            
            setNamespaceAndPrefixList();
            
            _exc14nCanonicalizer.setStream(signerOutputStream);
            marshaller.marshal(signedInfo,_exc14nCanonicalizer);
            if(logger.isLoggable(Level.FINEST)){
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                _exc14nCanonicalizer.reset();
                _exc14nCanonicalizer.setStream(baos);
                marshaller.marshal(signedInfo,_exc14nCanonicalizer);
                logger.log(Level.FINEST, LogStringsMessages.WSS_1756_CANONICALIZED_SIGNEDINFO_VALUE(baos.toString()));
            }
        } catch (JAXBException ex) {
            throw new XWSSecurityRuntimeException(ex);
        }
        
        
        try {
            return _rsaSignature.sign();
            
        } catch (SignatureException se) {
            // should never occur!
            throw new RuntimeException(se.getMessage());
        }
    }
       
    public byte []  performHMACSign(Key key,SignedInfo signedInfo, int outputLength) throws InvalidKeyException{
        
        if (key == null || signedInfo == null) {
            throw new NullPointerException();
        }
        HmacSHA1 hmac = new HmacSHA1();
        hmac.init(key, outputLength);
        
        MacOutputStream macOutputStream = new MacOutputStream(hmac);
        Marshaller marshaller;
        try {
            marshaller = getMarshaller();
            _exc14nCanonicalizer.reset();
            setNamespaceAndPrefixList();
            _exc14nCanonicalizer.setStream(macOutputStream);
            marshaller.marshal(signedInfo,_exc14nCanonicalizer);
            if(logger.isLoggable(Level.FINEST)){
                marshaller = getMarshaller();
                _exc14nCanonicalizer.reset();
                setNamespaceAndPrefixList();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                _exc14nCanonicalizer.setStream(bos);
                marshaller.marshal(signedInfo,_exc14nCanonicalizer);
                logger.log(Level.FINEST, LogStringsMessages.WSS_1756_CANONICALIZED_SIGNEDINFO_VALUE(bos.toString()));
            }
        } catch (JAXBException ex) {
            throw new XWSSecurityRuntimeException(ex);
        }
        try {
            return hmac.sign();
            
        } catch (SignatureException se) {
            // should never occur!
            throw new RuntimeException(se.getMessage());
        }
    }
    
    public byte []  performDSASign(Key privateKey,SignedInfo signedInfo) throws InvalidKeyException{
        if (privateKey == null || signedInfo == null) {
            throw new NullPointerException();
        }
        
        if (!(privateKey instanceof PrivateKey)) {
            throw new InvalidKeyException("key must be PrivateKey");
        }
        
        if(_dsaSignature == null ){
            try {
                _dsaSignature = Signature.getInstance("SHA1withDSA");
            } catch (NoSuchAlgorithmException ex) {
                // shud never come here
                throw new XWSSecurityRuntimeException(ex);
            }
        }
        
        _dsaSignature.initSign((PrivateKey) privateKey);
        
        SignerOutputStream signerOutputStream = new SignerOutputStream(_dsaSignature);
        Marshaller marshaller;
        try {
            marshaller = getMarshaller();
            _exc14nCanonicalizer.reset();
            setNamespaceAndPrefixList();
            _exc14nCanonicalizer.setStream(signerOutputStream);
            marshaller.marshal(signedInfo,_exc14nCanonicalizer);
        } catch (JAXBException ex) {
            throw new XWSSecurityRuntimeException(ex);
        }
        
        try {
            return convertASN1toXMLDSIG(_dsaSignature.sign());
            
        } catch (SignatureException se) {
            // should never occur!
            throw new RuntimeException(se.getMessage());
        } catch (IOException ioex ) {
            throw new RuntimeException(ioex.getMessage());
        }
    }
    
    
    private static byte[] convertASN1toXMLDSIG(byte asn1Bytes[])
    throws IOException {
        
        // THIS CODE IS COPIED FROM APACHE (see copyright at top of file)
        byte rLength = asn1Bytes[3];
        int i;
        
        for (i = rLength; (i > 0) && (asn1Bytes[(4 + rLength) - i] == 0); i--);
        
        byte sLength = asn1Bytes[5 + rLength];
        int j;
        
        for (j = sLength;
        (j > 0) && (asn1Bytes[(6 + rLength + sLength) - j] == 0); j--);
        
        if ((asn1Bytes[0] != 48) || (asn1Bytes[1] != asn1Bytes.length - 2)
        || (asn1Bytes[2] != 2) || (i > 20)
        || (asn1Bytes[4 + rLength] != 2) || (j > 20)) {
            throw new IOException("Invalid ASN.1 format of DSA signature");
        } else {
            byte xmldsigBytes[] = new byte[40];
            
            System.arraycopy(asn1Bytes, (4+rLength)-i, xmldsigBytes, 20-i, i);
            System.arraycopy(asn1Bytes, (6+rLength+sLength)-j, xmldsigBytes,
                    40 - j, j);
            
            return xmldsigBytes;
        }
    }

     @SuppressWarnings("unchecked")
    public boolean verifyDSASignature(Key publicKey,SignedInfo si, byte [] signatureValue) throws InvalidKeyException, SignatureException {
        if (!(publicKey instanceof PublicKey)) {
            throw new InvalidKeyException("key must be PublicKey");
        }
        if (_dsaSignature == null) {
            try {
                _dsaSignature = Signature.getInstance("SHA1withDSA");
            } catch (NoSuchAlgorithmException nsae) {
                throw new SignatureException("SHA1withDSA Signature not found");
            }
        }
        _dsaSignature.initVerify((PublicKey) publicKey);
        SignerOutputStream sos = new SignerOutputStream(_dsaSignature);
        if(si.getSignedInfo() != null){
            XMLStreamReaderEx signedInfo = (XMLStreamReaderEx) si.getSignedInfo();
            if(_exc14nSBCanonicalizer == null){
                _exc14nSBCanonicalizer = new EXC14nStAXReaderBasedCanonicalizer();
            }
            
            NamespaceContextEx nsContext = signedInfo.getNamespaceContext();
            Iterator<NamespaceContextEx.Binding> itr = nsContext.iterator();
            ArrayList list = new ArrayList();
            while(itr.hasNext()){
                NamespaceContextEx.Binding binding = itr.next();
                AttributeNS ans = new AttributeNS();
                ans.setPrefix( binding.getPrefix());
                ans.setUri(binding.getNamespaceURI());
                list.add(ans);
            }
            
            _exc14nSBCanonicalizer.addParentNamespaces(list);
            try {
                _exc14nSBCanonicalizer.canonicalize(signedInfo,sos,null);
            } catch (XMLStreamException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1withDSA"));
                throw new SignatureException(LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1withDSA"),ex);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1withDSA"));
                throw new SignatureException(LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1withDSA"),ex);
            }
        }else{
            sos.write(si.getCanonicalizedSI());
        }
        try {
            return  _dsaSignature.verify(convertXMLDSIGtoASN1(signatureValue));
        } catch (SignatureException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1withDSA"));
            throw new SignatureException(LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1withDSA"),ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1withDSA"));
            throw new SignatureException(LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1withDSA"),ex);
        }
        
    }
     @SuppressWarnings("unchecked")
    public boolean verifyHMACSignature(Key key,SignedInfo si,byte [] signatureValue,
            int outputLength) throws InvalidKeyException, SignatureException{
        if (key == null || si == null || signatureValue == null) {
            throw new NullPointerException("key, signedinfo or signature data can't be null");
        }
        HmacSHA1 hmac = new HmacSHA1();
        hmac.init(key, outputLength);
        
        MacOutputStream mos = new MacOutputStream(hmac);
        if(si.getSignedInfo() != null){
            XMLStreamReaderEx signedInfo = (XMLStreamReaderEx) si.getSignedInfo();
            if(_exc14nSBCanonicalizer == null){
                _exc14nSBCanonicalizer = new EXC14nStAXReaderBasedCanonicalizer();
            }
            NamespaceContextEx nsContext = signedInfo.getNamespaceContext();
            Iterator<NamespaceContextEx.Binding>  itr = nsContext.iterator();
            ArrayList list = new ArrayList();
            while(itr.hasNext()){
                NamespaceContextEx.Binding binding = itr.next();
                AttributeNS ans = new AttributeNS();
                ans.setPrefix( binding.getPrefix());
                ans.setUri(binding.getNamespaceURI());
                list.add(ans);
            }
            
            _exc14nSBCanonicalizer.addParentNamespaces(list);
            _exc14nSBCanonicalizer.addParentNamespaces(list);
            
            try {
                _exc14nSBCanonicalizer.canonicalize(signedInfo,mos,null);
            } catch (XMLStreamException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("HMAC_SHA1"));
                throw new SignatureException(LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("HMAC_SHA1"),ex);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("HMAC_SHA1"));
                throw new SignatureException(LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("HMAC_SHA1"),ex);
            }
        }else{
            mos.write(si.getCanonicalizedSI());
        }
        return  hmac.verify(signatureValue);
    }
     @SuppressWarnings("unchecked")
    public boolean verifyRSASignature(Key publicKey,SignedInfo si,byte [] signatureValue,String signatureAlgo) throws InvalidKeyException, SignatureException{
        
        if (!(publicKey instanceof PublicKey)) {
            throw new InvalidKeyException("key must be PublicKey");
        }
        if (_rsaSignature == null) {
            try {
                _rsaSignature = Signature.getInstance(signatureAlgo);
            } catch (NoSuchAlgorithmException nsae) {
                throw new SignatureException("SHA1withRSA Signature not found");
            }
        }
        _rsaSignature.initVerify((PublicKey) publicKey);
        SignerOutputStream sos = new SignerOutputStream(_rsaSignature);
        if(si.getSignedInfo() != null){
            XMLStreamReaderEx signedInfo = (XMLStreamReaderEx) si.getSignedInfo();
            if(_exc14nSBCanonicalizer == null){
                _exc14nSBCanonicalizer = new EXC14nStAXReaderBasedCanonicalizer();
            }
            NamespaceContextEx nsContext = signedInfo.getNamespaceContext();
            Iterator<NamespaceContextEx.Binding>  itr = nsContext.iterator();
            ArrayList list = new ArrayList();
            while(itr.hasNext()){
                NamespaceContextEx.Binding binding = itr.next();
                AttributeNS ans = new AttributeNS();
                ans.setPrefix( binding.getPrefix());
                ans.setUri(binding.getNamespaceURI());
                list.add(ans);
            }
            
            //    _exc14nSBCanonicalizer.addParentNamespaces(list);
            _exc14nSBCanonicalizer.addParentNamespaces(list);
            try {
                _exc14nSBCanonicalizer.canonicalize(signedInfo,sos,null);
            } catch (XMLStreamException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1WithRSA"));
                throw new SignatureException(LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1WithRSA"),ex);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1WithRSA"));
                throw new SignatureException(LogStringsMessages.WSS_1724_SIGTYPE_VERIFICATION_FAILED("SHA1WithRSA"),ex);
            }
        }else{
            sos.write(si.getCanonicalizedSI());
        }
        return  _rsaSignature.verify(signatureValue);
    }

    private static byte[] convertXMLDSIGtoASN1(byte xmldsigBytes[])
    throws IOException {
        
        // THIS CODE IS COPIED FROM APACHE (see copyright at top of file)
        if (xmldsigBytes.length != 40) {
            throw new IOException("Invalid XMLDSIG format of DSA signature");
        }
        
        int i;
        
        for (i = 20; (i > 0) && (xmldsigBytes[20 - i] == 0); i--);
        
        int j = i;
        
        if (xmldsigBytes[20 - i] < 0) {
            j += 1;
        }
        
        int k;
        
        for (k = 20; (k > 0) && (xmldsigBytes[40 - k] == 0); k--);
        
        int l = k;
        
        if (xmldsigBytes[40 - k] < 0) {
            l += 1;
        }
        
        byte asn1Bytes[] = new byte[6 + j + l];
        
        asn1Bytes[0] = 48;
        asn1Bytes[1] = (byte) (4 + j + l);
        asn1Bytes[2] = 2;
        asn1Bytes[3] = (byte) j;
        
        System.arraycopy(xmldsigBytes, 20 - i, asn1Bytes, (4 + j) - i, i);
        
        asn1Bytes[4 + j] = 2;
        asn1Bytes[5 + j] = (byte) l;
        
        System.arraycopy(xmldsigBytes, 40 - k, asn1Bytes, (6 + j + l) - k, k);
        
        return asn1Bytes;
    }

    private Marshaller getMarshaller() throws JAXBException{
        JAXBFilterProcessingContext wssContext = (JAXBFilterProcessingContext)context.get(MessageConstants.WSS_PROCESSING_CONTEXT);
        Marshaller marshaller =  _jaxbContext.createMarshaller();
        marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", false);
        if(wssContext != null)
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new WSSNamespacePrefixMapper(wssContext.isSOAP12()));
        else
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new WSSNamespacePrefixMapper());
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT,true);
        return marshaller;
    }
    
    private void setNamespaceAndPrefixList() {
        
        NamespaceAndPrefixMapper nsMapper = (NamespaceAndPrefixMapper)context.get(NamespaceAndPrefixMapper.NS_PREFIX_MAPPER);
        if(nsMapper != null){
            NamespaceContextEx nc = nsMapper.getNamespaceContext();
            Iterator<NamespaceContextEx.Binding> itr = nc.iterator();
            while(itr.hasNext()){
                final NamespaceContextEx.Binding nd = itr.next();
                try {
                    _exc14nCanonicalizer.writeNamespace(nd.getPrefix(),nd.getNamespaceURI());
                } catch (XMLStreamException ex) {
                    throw new XWSSecurityRuntimeException(ex);
                }
            }
            List incList = nsMapper.getInlusivePrefixList();
            _exc14nCanonicalizer.setInclusivePrefixList(incList);
        }
    }
    
}
