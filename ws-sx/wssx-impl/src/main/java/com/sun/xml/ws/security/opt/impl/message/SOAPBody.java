/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.security.opt.impl.message;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.stax.StreamWriterBufferCreator;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.message.source.PayloadSourceMessage;
import com.sun.xml.ws.message.stream.StreamMessage;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.ws.security.opt.impl.util.XMLStreamFilterWithId;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SOAPBody{
    private static final String BODY = "Body";
    private static final String BODY_PREFIX = "S";
    
    private Message  message;
    private SOAPVersion soapVersion ;
    ///private byte [] byteStream;
    private SecurityElement bodyContent;
    private String wsuId;
    private String contentId;
    private MutableXMLStreamBuffer buffer = null;
    private List attributeValuePrefixes = null;
    
    public SOAPBody(Message message ) {
        this.message = message;
        this.soapVersion  = SOAPVersion.SOAP_11;
    }
    
    /**
     *
     * Creates a new instance of SOAPBody
     *
     */
    
    public SOAPBody(Message message,SOAPVersion soapVersion ) {
        this.message = message;
        this.soapVersion  = soapVersion;
    }
    
    public SOAPBody(byte[]  payLoad,SOAPVersion soapVersion ) {
        //byteStream = payLoad;
        this.soapVersion  = soapVersion;
    }
    
    public SOAPBody(SecurityElement se,SOAPVersion soapVersion ) {
        bodyContent = se;
        this.soapVersion  = soapVersion;
    }
    
    public SOAPVersion getSOAPVersion(){
        return soapVersion;
    }
    
    public String getId(){
        return wsuId;
    }
    
    public void setId(String id){
        wsuId = id;
    }
    
    public String getBodyContentId(){
        if(contentId != null)
            return contentId;
        else if(bodyContent != null)
            return bodyContent.getId();
        return null;
    }
    
    public void setBodyContentId(String id){
        this.contentId = id;
    }
    @SuppressWarnings("unchecked")
    public void writePayload(XMLStreamWriter writer)throws XMLStreamException{
        if(this.message != null){
            if(getBodyContentId() == null)
                this.message.writePayloadTo(writer);
            else{
                boolean isSOAP12 = (this.soapVersion == SOAPVersion.SOAP_12) ? true : false;
                XMLStreamFilterWithId xmlStreamFilterWithId = new XMLStreamFilterWithId(writer, new NamespaceContextEx(isSOAP12),getBodyContentId());
                this.message.writePayloadTo(xmlStreamFilterWithId);
            }
        }else if(bodyContent != null){
            ((SecurityElementWriter)bodyContent).writeTo(writer);
        }else if(buffer != null){
            if(writer instanceof StAXEXC14nCanonicalizerImpl){          
                if(attributeValuePrefixes != null && !attributeValuePrefixes.isEmpty()){
                    List prefixList = ((StAXEXC14nCanonicalizerImpl)writer).getInclusivePrefixList();
                    if(prefixList == null){
                        prefixList = new ArrayList();
                    }
                    prefixList.addAll(attributeValuePrefixes);
                    // remove duplicates by going through a HashSet
                    HashSet set = new HashSet(prefixList);
                    prefixList = new ArrayList(set);
                    ((StAXEXC14nCanonicalizerImpl)writer).setInclusivePrefixList(prefixList);
                }
            }
            buffer.writeToXMLStreamWriter(writer);
        }else{
            throw new UnsupportedOperationException();
            //TODO
        }
    }
    
    public void writeTo(XMLStreamWriter writer) throws XMLStreamException{
        writer.writeStartElement(BODY_PREFIX,BODY,this.soapVersion.nsUri);
        if(wsuId != null){
            writer.writeAttribute("wsu",MessageConstants.WSU_NS,"Id",wsuId);
        }
        writePayload(writer);
        writer.writeEndElement();
        //writer.flush();
    }
    
    public String getPayloadNamespaceURI(){
        if(message != null){
            return message.getPayloadNamespaceURI();
        }
        if(bodyContent != null){
            return bodyContent.getNamespaceURI();
        }
        return null;
    }
    
    public String getPayloadLocalPart(){
        if(message != null){
            return message.getPayloadLocalPart();
        }
        if(bodyContent != null){
            return bodyContent.getLocalPart();
        }
        return null;
    }
    
    public XMLStreamReader read() throws XMLStreamException{
        if(message != null){
            return message.readPayload();
        }else if(bodyContent != null){
            return bodyContent.readHeader();
        }
        throw new XMLStreamException("Invalid SOAPBody");
    }
    
    public void cachePayLoad() throws XMLStreamException {
        if(message != null){
            if(message instanceof StreamMessage ||  message instanceof PayloadSourceMessage ||
                    message instanceof com.sun.xml.ws.message.jaxb.JAXBMessage){
                if(buffer == null){
                    buffer = new MutableXMLStreamBuffer();
                    StreamWriterBufferCreator creator = new StreamWriterBufferCreator(buffer);
                    // check for attribute value prefixes
                    creator.setCheckAttributeValue(true);
                    this.writePayload(creator);
                    attributeValuePrefixes = creator.getAttributeValuePrefixes();
                    this.message = null;
                }
            }
        }
    }
    
    public List getAttributeValuePrefixes(){
        return attributeValuePrefixes;
    }
}

