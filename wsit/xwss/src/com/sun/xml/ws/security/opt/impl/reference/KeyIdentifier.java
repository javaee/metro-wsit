/*
 * KeyIdentifier.java
 *
 * Created on August 7, 2006, 1:48 PM
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

package com.sun.xml.ws.security.opt.impl.reference;

import com.sun.istack.NotNull;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.wss.XWSSecurityException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.ws.security.secext10.KeyIdentifierType;
import com.sun.xml.ws.security.secext10.ObjectFactory;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.stream.buffer.XMLStreamBufferResult;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.wss.impl.misc.Base64;

import java.util.Map;
import java.io.OutputStream;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class KeyIdentifier extends KeyIdentifierType
        implements com.sun.xml.ws.security.opt.api.reference.KeyIdentifier,
        SecurityHeaderElement, SecurityElementWriter {

    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    
    /** Creates a new instance of KeyIdentifier */
    public KeyIdentifier(SOAPVersion sv) {
        this.soapVersion = sv;
    }
    
    /**
     * 
     * @return the valueType attribute for KeyIdentifier
     */
    public String getValueType() {
        return super.getValueType();
    }
    
    /**
     * 
     * @param valueType the valueType attribute for KeyIdentifier
     */
    public void setValueType(final String valueType) {
        super.setValueType(valueType);
    }
    
    /**
     * 
     * @return the encodingType attribute
     */
    public String getEncodingType() {
        return super.getEncodingType();
    }
    
    /**
     * 
     * @param value the encodingType attribute
     */
    public void setEncodingType(final String value) {
        super.setEncodingType(value);
    }
    
    /**
     * 
     * @return the referenced value by this key identifier
     */
    public String getReferenceValue() {
        return super.getValue();
    }
    
    /**
     * 
     * @param referenceValue the referenced value by this keyIdentifier
     */
    public void setReferenceValue(final String referenceValue) {
        super.setValue(referenceValue);
    }
    
    /**
     * 
     * @return the reference type used
     */
    public String getType() {
        return MessageConstants.KEY_INDETIFIER_TYPE;
    }
    
    /**
     * 
     * @return id attribute
     */
    public String getId() {
        QName qname = new QName(MessageConstants.WSU_NS, "Id", MessageConstants.WSU_PREFIX);
        Map<QName, String> otherAttributes = this.getOtherAttributes();
        return otherAttributes.get(qname);
    }
    
    /**
     * 
     * @param id 
     */
    public void setId(String id) {
        QName qname = new QName(MessageConstants.WSU_NS, "Id", MessageConstants.WSU_PREFIX);
        Map<QName, String> otherAttributes = this.getOtherAttributes();
        otherAttributes.put(qname, id);
    }
    
    /**
     * 
     * @return namespace uri of Keyidentifier.
     */
    public String getNamespaceURI() {
        return MessageConstants.WSSE_NS;
    }
    
    /**
     * Gets the local name of this header element.
     *
     * @return
     *      this string must be interned.
     */
    public String getLocalPart() {
        return "KeyIdentifier".intern();
    }
    
    
    public String getAttribute(@NotNull String nsUri, @NotNull String localName) {
        QName qname = new QName(nsUri, localName);
        Map<QName, String> otherAttributes = this.getOtherAttributes();
        return otherAttributes.get(qname);
    }
    
    
    public String getAttribute(@NotNull QName name) {
        Map<QName, String> otherAttributes = this.getOtherAttributes();
        return otherAttributes.get(name);
    }
    
    public XMLStreamReader readHeader() throws XMLStreamException {
        XMLStreamBufferResult xbr = new XMLStreamBufferResult();
        JAXBElement<KeyIdentifierType> keyIdentifierElem = new ObjectFactory().createKeyIdentifier(this);
        try{
            getMarshaller().marshal(keyIdentifierElem, xbr);
            
        } catch(JAXBException je){
            throw new XMLStreamException(je);
        }
        return xbr.getXMLStreamBuffer().readAsXMLStreamReader();
    }
    
    /**
     * Writes out the header.
     *
     * @throws XMLStreamException
     *      if the operation fails for some reason. This leaves the
     *      writer to an undefined state.
     */
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
        JAXBElement<KeyIdentifierType> keyIdentifierElem = new ObjectFactory().createKeyIdentifier(this);
        try {
            // If writing to Zephyr, get output stream and use JAXB UTF-8 writer
            if (streamWriter instanceof Map) {
                OutputStream os = (OutputStream) ((Map) streamWriter).get("sjsxp-outputstream");
                if (os != null) {
                    streamWriter.writeCharacters("");        // Force completion of open elems
                    getMarshaller().marshal(keyIdentifierElem, os);
                    return;
                }
            }
            
            getMarshaller().marshal(keyIdentifierElem,streamWriter);
        } catch (JAXBException e) {
            throw new XMLStreamException(e);
        }
    }
    
    /**
     * 
     * @param streamWriter 
     * @param props 
     * @throws javax.xml.stream.XMLStreamException 
     */
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        try{
            Marshaller marshaller = getMarshaller();
            Iterator<Map.Entry<Object, Object>> itr = props.entrySet().iterator();
            while(itr.hasNext()){
                Map.Entry<Object, Object> entry = itr.next();
                marshaller.setProperty((String)entry.getKey(), entry.getValue());
            }
            writeTo(streamWriter);
        }catch(JAXBException jbe){
            throw new XMLStreamException(jbe);
        }
    }
    
    private Marshaller getMarshaller() throws JAXBException{
        return JAXBUtil.createMarshaller(soapVersion);
    }
    
    /**
     * 
     * @param os 
     */
    public void writeTo(OutputStream os) {
    }
    
    public void updateReferenceValue(byte[] kerberosToken) throws XWSSecurityException{
        if(getValueType() == MessageConstants.KERBEROS_v5_APREQ_IDENTIFIER){
            try {
                setReferenceValue(Base64.encode(MessageDigest.getInstance("SHA-1").digest(kerberosToken)));
            } catch (NoSuchAlgorithmException ex) {
                throw new XWSSecurityException("Digest algorithm SHA-1 not found");
            }
        } else{
            throw new XWSSecurityException(getValueType() + " ValueType not supported for kerberos tokens");
        }
    }    
    
    public void updateReferenceValue(X509Certificate cert) throws XWSSecurityException{
        if(getValueType() == MessageConstants.ThumbPrintIdentifier_NS){
            try {
                setReferenceValue(Base64.encode(MessageDigest.getInstance("SHA-1").digest(cert.getEncoded())));
            } catch ( NoSuchAlgorithmException ex ) {
                throw new XWSSecurityException("Digest algorithm SHA-1 not found");
            } catch ( CertificateEncodingException ex) {
                throw new XWSSecurityException("Error while getting certificate's raw content");
            }
        }else if(getValueType() ==MessageConstants.X509SubjectKeyIdentifier_NS) {
            byte[] keyId = X509SubjectKeyIdentifier.getSubjectKeyIdentifier(cert);
            if (keyId == null) {
                return;
            }
            setReferenceValue(Base64.encode(keyId));
        }
    }
    
    /**
     * 
     * @param id 
     * @return 
     */
    public boolean refersToSecHdrWithId(String id) {
        String valueType =this.getValueType();
        if(MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE.equals(valueType) ||
                MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE.equals(valueType)){
            if(this.getValue().equals(id)){
                return true;
            }
        }
        return false;
    }
}
