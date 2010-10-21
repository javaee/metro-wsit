/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * EncryptedSignedMessageHeader.java
 *
 * Created on May 22, 2007, 12:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.opt.impl.message;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.dsig.SignedMessageHeader;
import com.sun.xml.wss.impl.c14n.AttributeNS;
import com.sun.xml.wss.impl.c14n.StAXAttr;
import java.util.HashMap;
import java.util.Vector;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class EncryptedSignedMessageHeader extends SignedMessageHeader{
    
    private SecurityHeaderElement encHeader = null;
    private boolean parsed = false;
    private String localName;
    private String uri;
    private String prefix;
    private Vector attrList = new Vector();
    private Vector attrNSList = new Vector();
    
    private MutableXMLStreamBuffer buffer = null;
    
    /** Creates a new instance of EncryptedSignedMessageHeader */
    public EncryptedSignedMessageHeader(SignedMessageHeader hdr, SecurityHeaderElement she) {
        super(hdr);
        encHeader = she;
    }
    
    /**
     *
     * @return The header as as XMLStreamReader
     */
    public javax.xml.stream.XMLStreamReader readHeader() throws javax.xml.stream.XMLStreamException {
        if(buffer == null){
            buffer = new MutableXMLStreamBuffer();
            XMLStreamWriter writer = buffer.createFromXMLStreamWriter();
            super.writeTo(writer);
        }
        return buffer.readAsXMLStreamReader();
    }
    
    /**
     * Write the header to an XMLStreamWriter
     */
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter) throws javax.xml.stream.XMLStreamException {
        if(!parsed){
            parse();
        }
        writeStartElement(streamWriter);
        ((SecurityElementWriter)encHeader).writeTo(streamWriter);
        writeEndElement(streamWriter);
    }
    
    /**
     * Write the header to an XMLStreamWriter
     */
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, final HashMap props) throws javax.xml.stream.XMLStreamException {
        if(!parsed){
            parse();
        }
        writeStartElement(streamWriter);
        ((SecurityElementWriter)encHeader).writeTo(streamWriter, props);
        writeEndElement(streamWriter);
        
    }
    @SuppressWarnings("unchecked")
    protected void parse()throws XMLStreamException{
        XMLStreamReader reader = readHeader();
        parsed = true;
        boolean stop = false;
        while(reader.hasNext()){
            int eventType = reader.next();
            if(stop){
                return;
            }
            switch(eventType){
                case XMLStreamConstants.START_ELEMENT :{
                    localName = reader.getLocalName();
                    uri = reader.getNamespaceURI();
                    prefix = reader.getPrefix();
                    if(prefix == null)
                        prefix = "";
                    int count = reader.getAttributeCount();
                    for(int i=0;i<count ;i++){
                        String localName = reader.getAttributeLocalName(i);
                        String uri = reader.getAttributeNamespace(i);
                        String prefix = reader.getAttributePrefix(i);
                        if(prefix == null)
                            prefix = "";
                        final String value = reader.getAttributeValue(i);
                        StAXAttr attr = new StAXAttr();
                        attr.setLocalName(localName);
                        attr.setValue(value);
                        attr.setPrefix(prefix);
                        attr.setUri(uri);
                        attrList.add(attr);
                    }
                    
                    count = 0;
                    count = reader.getNamespaceCount();
                    for(int i=0;i<count ;i++){
                        String prefix = reader.getNamespacePrefix(i);
                        if(prefix == null)
                            prefix = "";
                        String uri = reader.getNamespaceURI(i);
                        AttributeNS attrNS = new AttributeNS();
                        attrNS.setPrefix(prefix);
                        attrNS.setUri(uri);
                        attrNSList.add(attrNS);
                    }
                    stop = true;
                    break;
                }
                case XMLStreamConstants.END_ELEMENT :{
                    stop = true;
                    break;
                }
            }
            
        }
    }
    
    private void writeEndElement(XMLStreamWriter xsw) throws XMLStreamException{
        xsw.writeEndElement();
    }
    
    private void writeStartElement(XMLStreamWriter xsw) throws XMLStreamException{
        xsw.writeStartElement(prefix,localName,uri);
        for(int i=0;i<attrNSList.size();i++){
            AttributeNS attrNs = (AttributeNS)attrNSList.get(i);
            xsw.writeNamespace(attrNs.getPrefix(),attrNs.getUri());
        }
        for(int i=0;i<attrList.size();i++){
            StAXAttr attr = (StAXAttr) attrList.get(i);
            xsw.writeAttribute(attr.getPrefix(),attr.getUri(),attr.getLocalName(),attr.getValue());
        }
    }
}
