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

package com.sun.xml.ws.security.opt.impl.dsig;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.SignedData;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.ws.security.opt.impl.util.XMLStreamFilterWithId;
import java.io.OutputStream;
import java.util.HashMap;
import com.sun.istack.NotNull;

import com.sun.xml.ws.api.message.Header;
import javax.xml.stream.XMLStreamWriter;

/**
 * 
 * A wrapper over a <CODE>Header</CODE> or a <CODE>SecurityHeaderElement</CODE>
 * @author K.Venugopal@sun.com
 */

public class SignedMessageHeader extends SignedMessagePart
        implements SecurityHeaderElement, SignedData, SecurityElementWriter{
    
    private Header header = null;
    private SecurityHeaderElement she = null;
    
    private byte[] digestValue;
    
    private String id;
    
    JAXBFilterProcessingContext context = null;
    private MutableXMLStreamBuffer buffer = null;
    
    /**
     * Creates a new instance of SignedMessageHeader
     * @param header The SOAP Header which is to be signed
     * @param id The id assigned to the SOAP header
     * @param context JAXBFilterProcessingContext
     */
    public SignedMessageHeader(Header header, String id, JAXBFilterProcessingContext context ) {
        this.header = header;
        this.id = id;
        this.context = context;
    }
    
    /**
     * 
     * Sign a <CODE>SecurityHeaderElement</CODE>
     * @param she The SecurityHeaderElement to be signed
     */
    public SignedMessageHeader(SecurityHeaderElement she){
        this.she = she;
    }
    
    /**
     * 
     * @return the id of the SignedMessageHeader
     */
    public String getId() {
        if(header != null){
            return id;
        } else{
            return she.getId();
        }
    }
    
    /**
     * Assign an id to the SignedMessageHeader
     */
    public void setId(final String id) {
        if(header != null){
            this.id = id;
        } else {
            she.setId(id);
        }
    }
    
    /**
     * 
     * @return the namespace of the underlying SOAP header or SecurityHeaderElement
     */
    @NotNull
    public String getNamespaceURI() {
        if(header != null){
            return header.getNamespaceURI();
        } else {
            return she.getNamespaceURI();
        }
    }
    
    /**
     * 
     * @return The localname of the underlying SOAP Header or SecurityHeaderElement
     */
    @NotNull
    public String getLocalPart() {
        if(header != null){
            return header.getLocalPart();
        } else {
            return she.getLocalPart();
        }
    }
    
    /**
     * 
     * @return The header as as XMLStreamReader
     */
    public javax.xml.stream.XMLStreamReader readHeader() throws javax.xml.stream.XMLStreamException {
        if(buffer == null){
          buffer = new MutableXMLStreamBuffer();
          XMLStreamWriter writer = buffer.createFromXMLStreamWriter();
          this.writeTo(writer);
        }
        return buffer.readAsXMLStreamReader();
    }
    
    /**
     * Write the header to the passed outputStream
     */
    public void writeTo(OutputStream os) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    /**
     * Write the header to an XMLStreamWriter
     */
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter) throws javax.xml.stream.XMLStreamException {                
        if(header != null){
            XMLStreamFilterWithId xmlStreamFilterWithId = new XMLStreamFilterWithId(streamWriter, (NamespaceContextEx) context.getNamespaceContext(), id);
            header.writeTo(xmlStreamFilterWithId);
        } else{
            ((SecurityElementWriter)she).writeTo(streamWriter);
        }
        
    }
    
    /**
     * Write the header to an XMLStreamWriter
     */
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, final HashMap props) throws javax.xml.stream.XMLStreamException {
        /*Marshaller marshaller = getMarshaller();
        Iterator<String> itr = props.keySet().iterator();
        while(itr.hasNext()){
            String key = itr.next();
            Object value = props.get(key);
            marshaller.setProperty(key,value);
        }*/
        if(header != null){
            XMLStreamFilterWithId xmlStreamFilterWithId = new XMLStreamFilterWithId(streamWriter, (NamespaceContextEx) context.getNamespaceContext(), id);
            header.writeTo(xmlStreamFilterWithId);
        } else{
            ((SecurityElementWriter)she).writeTo(streamWriter,props);
        }
    }
    
    public void setDigestValue(final byte[] digestValue){
        this.digestValue = digestValue;
    }
    
    /**
     * 
     * @return The DigestValue of this Header
     */
    public byte[] getDigestValue() {
        return digestValue;
    }
    
    /**
     * 
     * @param id The id of the SecurityHeaderElement against which to compare
     * @return true if the current SecurityHeaderElement has reference to the
     * SecurityHeaderElement with passed id
     */
    public boolean refersToSecHdrWithId(String id) {
        return she.refersToSecHdrWithId(id);
    }
    
    public Header getSignedHeader(){
        return header;
    }
}