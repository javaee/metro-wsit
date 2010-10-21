/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * BinarySecurityToken.java
 *
 * Created on August 2, 2006, 10:36 AM
 */

package com.sun.xml.ws.security.opt.impl.keyinfo;

import com.sun.istack.NotNull;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.stream.buffer.XMLStreamBufferResult;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.ws.security.secext10.BinarySecurityTokenType;
import com.sun.xml.wss.impl.MessageConstants;

import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import com.sun.xml.ws.security.secext10.ObjectFactory;
import com.sun.xml.wss.logging.impl.crypto.LogStringsMessages;
import static com.sun.xml.wss.impl.MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME;
import static com.sun.xml.wss.impl.MessageConstants.WSSE_NS;
import static com.sun.xml.wss.logging.LogDomainConstants.CRYPTO_IMPL_LOGGER;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class BinarySecurityToken implements com.sun.xml.ws.security.opt.api.keyinfo.BinarySecurityToken,SecurityHeaderElement, SecurityElementWriter {
    
    private BinarySecurityTokenType bst = null;
    
    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    /** Creates a new instance of BinarySecurityToken */
    public BinarySecurityToken(BinarySecurityTokenType token,SOAPVersion sv) {
        this.bst = token;
        this.soapVersion = sv;
    }
    
    public String getValueType() {
        return bst.getValueType();
    }
    
    public String getEncodingType() {
        return bst.getEncodingType();
    }
    
    public String getId() {
        return bst.getId();
    }
    
    public void setId(String id) {
        bst.setId(id);
    }
    
    @NotNull
    public String getNamespaceURI() {
        return WSSE_NS;
    }
    
    @NotNull
    public String getLocalPart() {
        return WSSE_BINARY_SECURITY_TOKEN_LNAME;
    }
    /**
     * marshalls the BST element into the XMLStreamBuffer
     * @return XMLStreamReader
     * @throws javax.xml.stream.XMLStreamException
     */
    public XMLStreamReader readHeader() throws XMLStreamException {
        XMLStreamBufferResult xbr = new XMLStreamBufferResult();
        JAXBElement<BinarySecurityTokenType> bstElem =
                new ObjectFactory().createBinarySecurityToken(bst);
        try{
            getMarshaller().marshal(bstElem, xbr);
        }catch(JAXBException je){
            //log here
            throw new XMLStreamException(je);
        }
        return xbr.getXMLStreamBuffer().readAsXMLStreamReader();
    }
    
    public <T> T readAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
        throw new UnsupportedOperationException();
    }
    
    public <T> T readAsJAXB(Bridge<T> bridge, BridgeContext context) throws JAXBException {
        throw new UnsupportedOperationException();
    }
    
    public <T> T readAsJAXB(Bridge<T> bridge) throws JAXBException {
        throw new UnsupportedOperationException();
    }
    /**
     *  writes the binary security token to the XMLStreamWriter
     * @param streamWriter XMLStreamWriter
     * @throws javax.xml.stream.XMLStreamException
     */
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
        JAXBElement<BinarySecurityTokenType> bstElem =
                new ObjectFactory().createBinarySecurityToken(bst);
        try {
            // If writing to Zephyr, get output stream and use JAXB UTF-8 writer
            if (streamWriter instanceof Map) {
                OutputStream os = (OutputStream) ((Map) streamWriter).get("sjsxp-outputstream");
                if (os != null) {
                    streamWriter.writeCharacters("");        // Force completion of open elems
                    Marshaller writer = getMarshaller();
                    
                    writer.marshal(bstElem, os);
                    return;
                }
            }
            getMarshaller().marshal(bstElem, streamWriter);
        } catch (JAXBException e) {
            //log here also
            throw new XMLStreamException(e);
        }
    }
    
    public void writeTo(SOAPMessage saaj) throws SOAPException {
        NodeList nl = saaj.getSOAPHeader().getElementsByTagNameNS(MessageConstants.WSSE_NS,MessageConstants.WSSE_SECURITY_LNAME);
        try {
            Marshaller writer = getMarshaller();
         
            writer.marshal(bst,nl.item(0));
        } catch (JAXBException ex) {
            throw new SOAPException(ex);
        }
    }
    
    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
        throw new UnsupportedOperationException();
    }
   /**
    * returns base64 decoded value of the binary securt token value
    * @return byte[]
    */
    public byte[] getTokenValue() {
        try {
            return Base64.decode(bst.getValue());
        } catch (Base64DecodingException ex) {
            CRYPTO_IMPL_LOGGER.log(Level.SEVERE,LogStringsMessages.WSS_1243_BST_DECODING_ERROR(),ex);
            return null;
        }
    }
    
    private Marshaller getMarshaller() throws JAXBException{
        return JAXBUtil.createMarshaller(soapVersion);
    }
    
    public void writeTo(OutputStream os) {
    }
    
    public boolean refersToSecHdrWithId(String id) {
        return false;
    }

    public X509Certificate getCertificate() {
        return null;
    }
    /**
     * writes the binary security token to the XMLStreamWriter
     * @param streamWriter javax.xml.stream.XMLStreamWriter
     * @param props HashMap
     * @throws javax.xml.stream.XMLStreamException
     */
    @SuppressWarnings("unchecked")
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
            //log here
            throw new XMLStreamException(jbe);
        }
    }
    
}
