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

import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class EncryptedHeader implements SecurityHeaderElement, SecurityElementWriter  {
    
    private JAXBFilterProcessingContext pc = null;
    private String id = "";
    private String namespaceURI = "";
    private String localName = "";
    private EncryptedData ed = null;
    private HashMap<String, String> parentNS = null;

    /** Creates a new instance of EncryptedHeader */
    public EncryptedHeader(XMLStreamReader reader,JAXBFilterProcessingContext pc, HashMap<String, String> parentNS) throws XMLStreamException, XWSSecurityException {
        this.pc = pc;
        this.parentNS = parentNS;
        process(reader);
    }
    
    public EncryptedData getEncryptedData(){
        return ed;
    }
    
    public String getEncryptionAlgorithm(){
        return ed.getEncryptionAlgorithm();
    }
    
    public Key getKey(){
        return ed.getKey();
    }
    
    public InputStream getCipherInputStream() throws XWSSecurityException{
        return ed.getCipherInputStream();
    }
    
    public InputStream getCipherInputStream(Key key) throws XWSSecurityException{
        return ed.getCipherInputStream(key);
    }
    
    public XMLStreamReader getDecryptedData() throws XMLStreamException, XWSSecurityException{
        return ed.getDecryptedData();
    }
    
    public XMLStreamReader getDecryptedData(Key key) throws XMLStreamException, XWSSecurityException{
        return ed.getDecryptedData(key);
    }
    
    public boolean refersToSecHdrWithId(String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getNamespaceURI() {
        return namespaceURI;
    }
    
    public String getLocalPart() {
        return localName;
    }
    
    public XMLStreamReader readHeader() throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(XMLStreamWriter streamWriter, HashMap props) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(OutputStream os) {
        throw new UnsupportedOperationException();
    }
    
    private void process(XMLStreamReader reader) throws XMLStreamException, XWSSecurityException{
        id = reader.getAttributeValue(MessageConstants.WSU_NS,"Id");
        namespaceURI = reader.getNamespaceURI();
        localName = reader.getLocalName();
        
        while(reader.hasNext()){
            reader.next();
            if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
                if(MessageConstants.ENCRYPTED_DATA_LNAME.equals(reader.getLocalName()) && MessageConstants.XENC_NS.equals(reader.getNamespaceURI())){
                    ed = new EncryptedData(reader, pc, parentNS);
                }
            }
            
            if(reader.getEventType() == XMLStreamReader.END_ELEMENT){
                if(MessageConstants.ENCRYPTED_HEADER_LNAME.equals(reader.getLocalName()) && MessageConstants.WSSE11_NS.equals(reader.getNamespaceURI())){
                    break;
                }
            }
        }
    }
    
    public WSSPolicy getInferredKB(){
        return ed.getInferredKB();
    }
    
}
