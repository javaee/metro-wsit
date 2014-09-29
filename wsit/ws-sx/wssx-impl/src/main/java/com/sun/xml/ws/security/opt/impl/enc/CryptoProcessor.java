/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

import org.apache.xml.security.algorithms.JCEMapper;

import com.sun.xml.util.XMLCipherAdapter;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.c14n.StAXC14nCanonicalizerImpl;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.impl.opt.crypto.LogStringsMessages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.ExemptionMechanism;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import com.sun.xml.ws.security.opt.crypto.JAXBData;
import com.sun.xml.ws.security.opt.crypto.StreamWriterData;
import com.sun.xml.ws.security.opt.impl.util.OutputStreamWrapper;
import java.io.OutputStream;
import java.security.Key;
import javax.crypto.CipherOutputStream;
import javax.crypto.Cipher;
import javax.xml.crypto.Data;
import javax.crypto.CipherInputStream;
import javax.xml.stream.XMLStreamException;
import org.jvnet.staxex.NamespaceContextEx;
import org.jvnet.staxex.NamespaceContextEx.Binding;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
/**
 * @author K.Venugopal@sun.com
 * @author Abhijit.Das@Sun.COM
 */
//TODO : Venu refactor this code after FCS.
public class CryptoProcessor {
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN_BUNDLE);
    
    protected Cipher cipher = null;
    protected Key key = null;
    protected Data data = null;
    private int mode = Cipher.ENCRYPT_MODE;
    private String algorithm = "";
    private Key dk = null;
    private byte[] ed = null;
    private IvParameterSpec ivSpec = null;
    private byte[] encryptedDataCV = null;
    
    public CryptoProcessor(){}
    /** Creates a new instance of EncryptionProcessor */
    public CryptoProcessor(int mode,String algo,Data ed,Key key) throws XWSSecurityException{
        this.mode = mode;
        this.algorithm = algo;
        this.data = ed;
        this.key = key;
    }
    
    public CryptoProcessor(int mode,String algo,Key dk,Key key) throws XWSSecurityException{
        this.mode = mode;
        this.algorithm = algo;
        this.key = key;
        this.dk = dk;
    }
    
    public CryptoProcessor(int mode,String algo,Key key) throws XWSSecurityException{
        this.mode = mode;
        this.algorithm = algo;
        this.key = key;
    }

    /**
     * creates an instance of javax.crypto.Cipher class and inits it .
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     */
    protected void initCipher() throws NoSuchAlgorithmException,NoSuchPaddingException, InvalidKeyException{
        if ( cipher == null ) {
            cipher = XMLCipherAdapter.constructCipher(getAlgorithm());
            cipher.init(mode, getKey());
        }
    }
    
    protected String getAlgorithm(){
        return algorithm;
    }
    /**
     * Convert algorithm URI to actual transformation (DES/CBC/PKCS5Padding)
     *
     * @param algorithmURI
     * @return String representing transforms
     */
    protected String convertAlgURIToTransformation(String algorithmURI) {
        return JCEMapper.translateURItoJCEID(algorithmURI);
    }
    
    protected Key getKey(){
        return key;
    }
    
    /**
     * encrypts outputStream
     * @param outputStream
     * @throws IOException
     */
    public void encrypt(OutputStream outputStream) throws IOException{
        if(mode == Cipher.ENCRYPT_MODE){
            encryptData(outputStream);
        }else if(mode == cipher.WRAP_MODE){
            encryptKey(outputStream);
        }
    }
    /**
     * wraps the data encryption key .
     * @return ed  byte[]
     */
    public byte[] getCipherValueOfEK(){
        try{
            if(ed == null){
                if(cipher == null){
                    initCipher();
                }
                ed = cipher.wrap(dk);
            }
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1904_UNSUPPORTED_KEYENCRYPTION_ALGORITHM(getAlgorithm()), ex);
            throw new XWSSecurityRuntimeException(ex);
        } catch (javax.crypto.NoSuchPaddingException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1905_ERROR_INITIALIZING_CIPHER(), ex);
            throw new XWSSecurityRuntimeException(ex);
        } catch (InvalidKeyException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1906_INVALID_KEY_ERROR(), ex);
            throw new XWSSecurityRuntimeException(ex);
        } catch(javax.crypto.IllegalBlockSizeException ibe){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1907_INCORRECT_BLOCK_SIZE(), ibe);
            throw new XWSSecurityRuntimeException(ibe);
        }
        return ed;
    }
    /**
     * wraps the data encryption key to byte[] and writes it to output stream
     * @param outputStream OutputStream
     * @throws IOException
     */
    public void encryptKey(OutputStream outputStream)throws IOException{
        try{
            if(ed == null){
                if(cipher == null){
                    initCipher();
                }
                ed = cipher.wrap(dk);
            }
            outputStream.write(ed);
            outputStream.flush();
            
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1904_UNSUPPORTED_KEYENCRYPTION_ALGORITHM(getAlgorithm()), ex);
            throw new XWSSecurityRuntimeException("Unable to compute CipherValue as "+getAlgorithm()+" is not supported", ex);
        } catch (javax.crypto.NoSuchPaddingException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1905_ERROR_INITIALIZING_CIPHER(), ex);
            throw new XWSSecurityRuntimeException("Error occurred while initializing the Cipher", ex);
        } catch (InvalidKeyException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1906_INVALID_KEY_ERROR(), ex);
            throw new XWSSecurityRuntimeException("Unable to calculate cipher value as invalid key was provided",ex);
        }catch(javax.crypto.IllegalBlockSizeException ibe){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1907_INCORRECT_BLOCK_SIZE(), ibe);
            throw new XWSSecurityRuntimeException(ibe);
        }
    }
    
    public void setEncryptedDataCV(byte [] cv){
        encryptedDataCV = cv;
    }
    
    /**
     * initialises the Cipher and encrypts the data  which is a byte[] and returns the encrypted data
     * @param cipherInput byte[]
     * @return encryptedBytes byte[] 
     */
    public byte[] encryptData(byte[] cipherInput){
        try {
            if(cipher == null){
                initCipher();    
            }
            
            byte[] cipherOutput = cipher.doFinal(cipherInput);
            byte[] iv = cipher.getIV();
            byte[] encryptedBytes = new byte[iv.length + cipherOutput.length];
            System.arraycopy(iv, 0, encryptedBytes, 0, iv.length);
            System.arraycopy(cipherOutput, 0, encryptedBytes, iv.length, cipherOutput.length);
            return encryptedBytes;
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1909_UNSUPPORTED_DATAENCRYPTION_ALGORITHM(getAlgorithm()), ex);
            throw new XWSSecurityRuntimeException("Unable to compute CipherValue as "+getAlgorithm()+" is not supported", ex);
        } catch (NoSuchPaddingException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1905_ERROR_INITIALIZING_CIPHER(), ex);
            throw new XWSSecurityRuntimeException("Error occurred while initializing the Cipher", ex);
        } catch (InvalidKeyException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1906_INVALID_KEY_ERROR(), ex);
            throw new XWSSecurityRuntimeException("Unable to calculate cipher value as invalid key was provided", ex);
        } catch (IllegalBlockSizeException ibse) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1915_INVALID_ALGORITHM_PARAMETERS(getAlgorithm()), ibse);
            throw new XWSSecurityRuntimeException(ibse);
        } catch (BadPaddingException bpe) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1915_INVALID_ALGORITHM_PARAMETERS(getAlgorithm()), bpe);
            throw new XWSSecurityRuntimeException(bpe);
        }
    }
    /**
     * initialises the Cipher and encrypts the data  which is a OutputStream and writes the encrypted data into the data member
     * @param eos OutputStream
     * @throws IOException
     */
    public void encryptData(OutputStream eos) throws IOException{
        try{
            OutputStreamWrapper outputStream = new OutputStreamWrapper(eos);
            if(encryptedDataCV != null){
                outputStream.write(encryptedDataCV);
                return;
            }
            //  Thread.dumpStack();
            if(cipher == null){
                initCipher();
            }
            //Base64OutputStream bos = new Base64OutputStream(outputStream);
            //TODO :: Wrap outputstream with base64 encoder
            CipherOutputStream cos = new CipherOutputStream(outputStream,cipher);
            //BufferedStreamWriter bsw = new BufferedStreamWriter(cos);
            byte [] iv = cipher.getIV();
            outputStream.write(iv);
            outputStream.flush();
            if(data instanceof JAXBData){
                ((JAXBData)data).writeTo(cos);// write in chucks
            }else if(data instanceof StreamWriterData){
                StAXC14nCanonicalizerImpl exc14n = new StAXEXC14nCanonicalizerImpl();
                //((StAXEXC14nCanonicalizerImpl)exc14n).setInclusivePrefixList(new ArrayList());
                NamespaceContextEx nsEx = ((StreamWriterData)data).getNamespaceContext();
                Iterator<Binding> iter = nsEx.iterator();
                while(iter.hasNext()){
                    Binding binding = iter.next();
                    exc14n.writeNamespace(binding.getPrefix(),binding.getNamespaceURI());
                }
                if(logger.isLoggable(Level.FINEST)){
                    exc14n.setStream(new ByteArrayOutputStream());
                }else{
                    exc14n.setStream(cos);
                }
                try {
                    ((StreamWriterData)data).write(exc14n);
                    if(logger.isLoggable(Level.FINEST)){
                        byte [] cd=((ByteArrayOutputStream)exc14n.getOutputStream()).toByteArray();
                        logger.log(Level.FINEST, LogStringsMessages.WSS_1951_ENCRYPTED_DATA_VALUE(new String(cd)));
                        cos.write(cd);
                    }
                } catch (javax.xml.stream.XMLStreamException ex) {
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1908_ERROR_WRITING_ENCRYPTEDDATA(),ex);
                }
            }
            
            cos.flush();
            cos.close();
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1909_UNSUPPORTED_DATAENCRYPTION_ALGORITHM(getAlgorithm()), ex);
            throw new XWSSecurityRuntimeException("Unable to compute CipherValue as "+getAlgorithm()+" is not supported", ex);
        } catch (javax.crypto.NoSuchPaddingException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1905_ERROR_INITIALIZING_CIPHER(), ex);
            throw new XWSSecurityRuntimeException("Error occurred while initializing the Cipher", ex);
        } catch (InvalidKeyException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1906_INVALID_KEY_ERROR(), ex);
            throw new XWSSecurityRuntimeException("Unable to calculate cipher value as invalid key was provided", ex);
        } catch(XMLStreamException xse){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1910_ERROR_WRITING_NAMESPACES_CANONICALIZER(xse.getMessage()), xse);
            throw new XWSSecurityRuntimeException("Unable to write namespaces to exclusive canonicalizer", xse);
        } catch (com.sun.xml.wss.XWSSecurityException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1911_ERROR_WRITING_CIPHERVALUE(ex.getMessage()), ex);
            throw new XWSSecurityRuntimeException("Unable to calculate cipher value ", ex);
        }
    }
    /**
     * decrypts the encrypted key which is a byte[]  with  encAlgo algorithm
     * @param encryptedKey byte[]
     * @param encAlgo String
     * @return Key
     * @throws IOException
     */
    public Key decryptKey(byte[] encryptedKey, String encAlgo) throws IOException{
        
        try {
            if(mode == Cipher.UNWRAP_MODE){
                if (algorithm == null || algorithm.length() == 0) {
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1912_DECRYPTION_ALGORITHM_NULL());
                    throw new IOException("Cannot decrypt a key without knowing the algorithm");
                }
                
                if(key == null){
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1913_DECRYPTION_KEY_NULL());
                    throw new IOException("Key used to decrypt EncryptedKey cannot be null");
                }
                if(cipher == null){
                    initCipher();
                }
                return cipher.unwrap(encryptedKey,JCEMapper.getJCEKeyAlgorithmFromURI(encAlgo), Cipher.SECRET_KEY);
                
            }
        } catch (InvalidKeyException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1906_INVALID_KEY_ERROR(), ex);
            throw new XWSSecurityRuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1904_UNSUPPORTED_KEYENCRYPTION_ALGORITHM(algorithm), ex);
            throw new XWSSecurityRuntimeException(ex);
        } catch (javax.crypto.NoSuchPaddingException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1905_ERROR_INITIALIZING_CIPHER(), ex);
            throw new XWSSecurityRuntimeException(ex);
        }
        logger.log(Level.SEVERE, LogStringsMessages.WSS_1914_INVALID_CIPHER_MODE(mode));
        throw new IOException("Invalid Cipher mode:"+mode);
    }
    /**
     * decrypts the given data which is of the form InputStream
     * @param is InputStream
     * @return InputStream
     * @throws IOException
     */
    public InputStream decryptData(InputStream is) throws IOException{
        try {
            if(mode == Cipher.DECRYPT_MODE){
                if ( cipher == null ) {
                    cipher = XMLCipherAdapter.constructCipher(getAlgorithm());
                    int len = cipher.getBlockSize();
                    byte [] iv  = new byte[len];
                    is.read(iv,0,len);
                    ivSpec = new IvParameterSpec(iv);
                    cipher.init(mode,key,ivSpec);
                }
                return new CipherInputStream(is,cipher);
            } else {
               logger.log(Level.SEVERE, LogStringsMessages.WSS_1914_INVALID_CIPHER_MODE(mode),"Invalid Cipher mode:"+mode);
               throw new IOException("Invalid Cipher mode:"+mode); 
            }
            
        } catch (InvalidKeyException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1906_INVALID_KEY_ERROR(),ex);
            throw new XWSSecurityRuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1909_UNSUPPORTED_DATAENCRYPTION_ALGORITHM(getAlgorithm()), ex);
            throw new XWSSecurityRuntimeException(ex);
        } catch (javax.crypto.NoSuchPaddingException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1905_ERROR_INITIALIZING_CIPHER(), ex);
            throw new XWSSecurityRuntimeException(ex);
        }catch (InvalidAlgorithmParameterException invalidAPE){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1915_INVALID_ALGORITHM_PARAMETERS(getAlgorithm()), invalidAPE);
            throw new XWSSecurityRuntimeException(invalidAPE);
        }
        
    }
    /**
     * decrypts the encryptedContent which a byte[]
     * @param encryptedContent byte[]
     * @return byte[]
     * @throws IOException
     */
    public byte[] decryptData(byte[] encryptedContent) throws IOException{
        try {
            if(mode == Cipher.DECRYPT_MODE){
                cipher = XMLCipherAdapter.constructCipher(getAlgorithm());
                int len = cipher.getBlockSize();
                byte [] iv  = new byte[len];
                System.arraycopy(encryptedContent, 0, iv, 0, len);
                ivSpec = new IvParameterSpec(iv);
                cipher.init(mode,key,ivSpec);
                return cipher.doFinal(encryptedContent, len, encryptedContent.length - len);
            } else {
               logger.log(Level.SEVERE, LogStringsMessages.WSS_1914_INVALID_CIPHER_MODE(mode));
               throw new IOException("Invalid Cipher mode:"+mode); 
            }
        } catch (InvalidKeyException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1906_INVALID_KEY_ERROR(),ex);
            throw new XWSSecurityRuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1909_UNSUPPORTED_DATAENCRYPTION_ALGORITHM(getAlgorithm()), ex);
            throw new XWSSecurityRuntimeException(ex);
        } catch (javax.crypto.NoSuchPaddingException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1905_ERROR_INITIALIZING_CIPHER(), ex);
            throw new XWSSecurityRuntimeException(ex);
        }catch (InvalidAlgorithmParameterException invalidAPE){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1915_INVALID_ALGORITHM_PARAMETERS(getAlgorithm()), invalidAPE);
            throw new XWSSecurityRuntimeException(invalidAPE);
        } catch (IllegalBlockSizeException ibse){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1915_INVALID_ALGORITHM_PARAMETERS(getAlgorithm()), ibse);
            throw new XWSSecurityRuntimeException(ibse);
        } catch (BadPaddingException bpe){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1915_INVALID_ALGORITHM_PARAMETERS(getAlgorithm()), bpe);
            throw new XWSSecurityRuntimeException(bpe);
        }
    }
}
