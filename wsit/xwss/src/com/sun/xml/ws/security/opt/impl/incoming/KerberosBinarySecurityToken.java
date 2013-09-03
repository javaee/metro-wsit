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

/*
 * KerberosBinarySecurityToken.java
 *
 * Created on 11 October, 2007, 4:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.opt.impl.incoming;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import com.sun.xml.stream.buffer.XMLStreamBufferMark;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.ws.security.opt.api.NamespaceContextInfo;
import com.sun.xml.ws.security.opt.api.PolicyBuilder;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.TokenValidator;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.XMLStreamReaderEx;
import com.sun.xml.wss.logging.impl.opt.LogStringsMessages;

/**
 *
 * @author ashutosh.shahi@sun.com
 */
public class KerberosBinarySecurityToken implements com.sun.xml.ws.security.opt.api.keyinfo.KerberosBinarySecurityToken,
        SecurityHeaderElement,PolicyBuilder,TokenValidator,NamespaceContextInfo,
        SecurityElementWriter{
    
    private String valueType = null;
    private String encodingType = null;
    private String id = "";
    private XMLStreamBuffer mark = null;
    private String namespaceURI = null;
    private String localPart = null;
    
    private AuthenticationTokenPolicy.KerberosTokenBinding ktPolicy = null;
    private HashMap<String,String> nsDecls;
    private byte [] bstValue = null;
    private SecretKey secretKey = null;
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_DOMAIN,
            LogDomainConstants.IMPL_OPT_DOMAIN_BUNDLE);
    
    /** Creates a new instance of KerberosBinarySecurityToken */
    public KerberosBinarySecurityToken(XMLStreamReader reader, StreamReaderBufferCreator creator,HashMap nsDecl,
            XMLInputFactory  staxIF) throws XMLStreamException,XMLStreamBufferException{
        localPart = reader.getLocalName();
        namespaceURI = reader.getNamespaceURI();
        id = reader.getAttributeValue(MessageConstants.WSU_NS,"Id");
        valueType = reader.getAttributeValue(null,MessageConstants.WSE_VALUE_TYPE);
        encodingType = reader.getAttributeValue(null,"EncodingType");
        mark = new XMLStreamBufferMark(nsDecl,creator);
        creator.createElementFragment(reader,true);
        ktPolicy = new AuthenticationTokenPolicy.KerberosTokenBinding();
        ktPolicy.setUUID(id);
        ktPolicy.setValueType(valueType);
        ktPolicy.setEncodingType(encodingType);
        this.nsDecls = nsDecl;
        XMLStreamReader bstReader = mark.readAsXMLStreamReader();
        bstReader.next();
        digestBST(bstReader);
    }
    
    public String getValueType() {
        return valueType;
    }
    
    public String getEncodingType() {
        return encodingType;
    }
    
    public byte[] getTokenValue() {
        return bstValue;
    }
    
    public String getId() {
        return id;
    }
    
    public boolean refersToSecHdrWithId(final String id) {
        throw new UnsupportedOperationException();
    }
    
    public void setId(final String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getNamespaceURI() {
        return namespaceURI;
    }
    
    public String getLocalPart() {
        return localPart;
    }
    
    public XMLStreamReader readHeader() throws XMLStreamException {
        return mark.readAsXMLStreamReader();
    }
    
    public WSSPolicy getPolicy() {
        return ktPolicy;
    }
    
    public void validate(ProcessingContext context) throws XWSSecurityException {
        //TODO
    }
    
    public HashMap<String, String> getInscopeNSContext() {
        return nsDecls;
    }
    
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
        mark.writeToXMLStreamWriter(streamWriter);
    }
    
    public void writeTo(XMLStreamWriter streamWriter, HashMap props) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(OutputStream os) {
        throw new UnsupportedOperationException();
    }
    
    private void digestBST(XMLStreamReader reader) throws XMLStreamException{
        if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
            reader.next();
        }
        if(reader.getEventType() == XMLStreamReader.CHARACTERS){
            if(reader instanceof XMLStreamReaderEx){
                CharSequence data = ((XMLStreamReaderEx)reader).getPCDATA();
                if(data instanceof Base64Data){
                    Base64Data binaryData = (Base64Data)data;
                    bstValue = binaryData.getExact();
                    return;
                }
            }
            try {
                bstValue = Base64.decode(StreamUtil.getCV(reader));
                
            } catch (Base64DecodingException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1604_ERROR_DECODING_BASE_64_DATA(ex));
                throw new XWSSecurityRuntimeException(LogStringsMessages.WSS_1604_ERROR_DECODING_BASE_64_DATA(ex));
            } catch (XMLStreamException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1604_ERROR_DECODING_BASE_64_DATA(ex));
                throw new XWSSecurityRuntimeException(LogStringsMessages.WSS_1604_ERROR_DECODING_BASE_64_DATA(ex));
            }
        }else{
             logger.log(Level.SEVERE, LogStringsMessages.WSS_1603_ERROR_READING_STREAM(null));
             throw new XWSSecurityRuntimeException(LogStringsMessages.WSS_1603_ERROR_READING_STREAM(null));
        }
        
        if(reader.getEventType() != reader.END_ELEMENT){
            reader.next();
        }        //else it is end of BST.
    }
    
}
