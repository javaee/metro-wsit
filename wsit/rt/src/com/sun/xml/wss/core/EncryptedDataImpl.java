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
 * EncryptedData.java
 *
 * Created on September 13, 2005, 2:11 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.core;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.misc.ByteArray;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Simple EncryptedData for Sign and Encrypt Usecase.
 * @author K.Venugopal@sun.com
 */
public class EncryptedDataImpl extends ByteArrayOutputStream {
    private byte [] iv = null;
    private byte [] encryptedData = null;
    private String id = null;
    private String mimeType = null;
    private String encoding = null;
    private String type = null;
    private KeyInfoHeaderBlock keyInfo = null;
    private String encAlgo = null;
    /** Creates a new instance of EncryptedData */
    private static final byte [] ENCRYPTED_DATA = MessageConstants.ENCRYPTED_DATA_LNAME.getBytes();
    private static final byte [] ENC_PREFIX = MessageConstants.XENC_PREFIX.getBytes();
    private static final byte [] ENC_NS = MessageConstants.XENC_NS.getBytes();
    private static byte [] OPENTAG =  "<".getBytes();
    private static byte [] CLOSETAG =  ">".getBytes();
    private static byte [] ENDTAG =  "</".getBytes();
    private static byte [] CLOSEELEMENT =  "/>".getBytes();
    private static byte [] ENCRYPTION_METHOD = "EncryptionMethod ".getBytes();
    private static byte [] ALGORITHM = "Algorithm ".getBytes();
    private static byte [] XMLNS =  "xmlns".getBytes();
    private static byte [] ID = "Id".getBytes();
    
    private static byte [] CIPHER_DATA = "CipherData".getBytes();
    private static byte [] CIPHER_VALUE = "CipherValue".getBytes();
    private static byte [] TYPE = "Type".getBytes();
    private static byte [] CONTENT_ONLY = "http://www.w3.org/2001/04/xmlenc#Content".getBytes();
    private XMLSerializer xmlSerializer = null;
    public EncryptedDataImpl() {
    }
    
    public byte[] getIv() {
        return iv;
    }
    
    public void setIv(byte[] iv) {
        this.iv = iv;
    }
    
    public byte[] getEncryptedData() {
        return encryptedData;
    }
    
    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }
    
    public KeyInfoHeaderBlock getKeyInfo() {
        return keyInfo;
    }
    
    public void setKeyInfo(KeyInfoHeaderBlock keyInfo) {
        this.keyInfo = keyInfo;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setXMLSerializer(XMLSerializer xmlWriter){
        xmlSerializer = xmlWriter;
    }
    
    
    public void writeTo(OutputStream stream) throws IOException{
        
        stream.write(OPENTAG);
        stream.write(ENC_PREFIX);
        stream.write(':');
        stream.write(ENCRYPTED_DATA);
        stream.write(' ');
        stream.write(XMLNS);
        stream.write(':');
        stream.write(ENC_PREFIX);
        
        stream.write('=');
        stream.write('"');
        stream.write(ENC_NS);
        stream.write('"');
        stream.write(' ');
        if(getId() != null){
            stream.write(ID);
            stream.write('=');
            stream.write('"');
            stream.write(getId().getBytes());
            stream.write('"');
        }
        stream.write(' ');
        stream.write(TYPE);
        stream.write('=');
        stream.write('"');
        stream.write(CONTENT_ONLY);
        stream.write('"');
        stream.write(CLOSETAG);
        stream.write(OPENTAG);
        stream.write(ENC_PREFIX);
        stream.write(':');
        stream.write(ENCRYPTION_METHOD);
        stream.write(' ');
        stream.write(ALGORITHM);
        
        stream.write('=');
        stream.write('"');
        stream.write(getEncAlgo().getBytes());
        stream.write('"');
        stream.write(CLOSEELEMENT);
        stream.write(OPENTAG);
        stream.write(ENC_PREFIX);
        stream.write(':');
        
        stream.write(CIPHER_DATA);
        stream.write(CLOSETAG);
        try{
            if(keyInfo != null){
                xmlSerializer.setOutputByteStream(stream);
                xmlSerializer.serialize(keyInfo.getAsSoapElement());
                // xmlSerializer.reset();
            }
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
        stream.write(OPENTAG);
        stream.write(ENC_PREFIX);
        stream.write(':');
        stream.write(CIPHER_VALUE);
        stream.write(CLOSETAG);
        //Base64.encodeToStream(getEncryptedData(),Base64.BASE64DEFAULTLENGTH,stream);
        Base64.encodeToStream(new ByteArray(iv,encryptedData),Base64.BASE64DEFAULTLENGTH,stream);
        stream.write(ENDTAG);
        stream.write(ENC_PREFIX);
        stream.write(':');
        stream.write(CIPHER_VALUE);
        stream.write(CLOSETAG);
        stream.write(ENDTAG);
        stream.write(ENC_PREFIX);
        stream.write(':');
        stream.write(CIPHER_DATA);
        stream.write(CLOSETAG);
        stream.write(ENDTAG);
        stream.write(ENC_PREFIX);
        stream.write(':');
        stream.write(ENCRYPTED_DATA);
        stream.write(CLOSETAG);
        
    }
    
    public String getEncAlgo() {
        return encAlgo;
    }
    
    public void setEncAlgo(String encAlgo) {
        this.encAlgo = encAlgo;
    }
    
    
    
}
