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

package com.sun.xml.ws.security.opt.impl.incoming;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.stax.StreamWriterBufferCreator;
import com.sun.xml.ws.security.opt.api.NamespaceContextInfo;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class SecurityContextToken implements SecurityHeaderElement, NamespaceContextInfo,
        SecurityElementWriter, com.sun.xml.ws.security.SecurityContextToken {
    
    private static final String IDENTIFIER = "Identifier".intern();
    private static final String INSTANCE = "Instance".intern();
    
    private static final int IDENTIFIER_ELEMENT = 1;
    private static final int INSTANCE_ELEMENT = 2;
    
    private String id = "";
    private String namespaceURI = "";
    private String localName = "";
    private String identifier = null;
    private String instance = null;
    private List extElements = null;
    private JAXBFilterProcessingContext pc;
    private MutableXMLStreamBuffer buffer = null;
    private HashMap<String,String> nsDecls;
    
    /** Creates a new instance of SecurityContextToken */
    public SecurityContextToken(XMLStreamReader reader,JAXBFilterProcessingContext pc,
            HashMap nsDecls) throws XMLStreamException, XWSSecurityException {
        this.pc = pc;
        this.nsDecls = nsDecls;
        id = reader.getAttributeValue(MessageConstants.WSU_NS,"Id");
        namespaceURI = reader.getNamespaceURI();
        localName = reader.getLocalName();
        buffer = new MutableXMLStreamBuffer();
        buffer.createFromXMLStreamReader(reader);
        XMLStreamReader sct =  buffer.readAsXMLStreamReader();
        sct.next();
        process(sct);
    }
    
    public String getSCId(){
        return identifier;
    }
    
    public URI getIdentifier() {
        try {
            return new URI(identifier);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getInstance() {
        return instance;
    }
    
    public List getExtElements() {
        return extElements;
    }
    
    public boolean refersToSecHdrWithId(final String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(final String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getNamespaceURI() {
        return namespaceURI;
    }
    
    public String getLocalPart() {
        return localName;
    }
    
    public javax.xml.stream.XMLStreamReader readHeader() throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(OutputStream os) {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter) throws javax.xml.stream.XMLStreamException {
        buffer.writeToXMLStreamWriter(streamWriter);
    }
    
    
    public HashMap<String, String> getInscopeNSContext() {
        return nsDecls;
    }
    
    private void process(XMLStreamReader reader) throws XMLStreamException, XWSSecurityException {
        
        
        if(StreamUtil.moveToNextElement(reader)){
            int refElement = getEventType(reader);
            while(reader.getEventType() != reader.END_DOCUMENT){
                switch(refElement){
                    case IDENTIFIER_ELEMENT : {
                        identifier = reader.getElementText();
                        break;
                    }
                    case INSTANCE_ELEMENT:{
                        instance = reader.getElementText();
                        break;
                    }
                    // extension elements?
                    default :{
                        throw new XWSSecurityException("Element name "+reader.getName()+" is not recognized under SecurityContextToken");
                    }
                }
                if(StreamUtil.moveToNextStartOREndElement(reader) &&
                        StreamUtil._break(reader, "SecurityContextToken", MessageConstants.WSSC_NS)){
                    
                    break;
                }else{
                    if(reader.getEventType() != XMLStreamReader.START_ELEMENT){
                        StreamUtil.moveToNextElement(reader);
                    }
                }
                refElement = getEventType(reader);
            }
            
        }
    }
    
    private int getEventType(javax.xml.stream.XMLStreamReader reader) {
        if(reader.getEventType()== XMLStreamReader.START_ELEMENT){
            if(reader.getLocalName() == IDENTIFIER){
                return IDENTIFIER_ELEMENT;
            }
            if(reader.getLocalName() == INSTANCE){
                return INSTANCE_ELEMENT;
            }
        }
        return -1;
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public String getWsuId() {
        return id;
    }
    
    public String getType() {
        return MessageConstants.SECURITY_CONTEXT_TOKEN_NS;
    }
    
    public Object getTokenValue() {
        return this;
    }
    
}
