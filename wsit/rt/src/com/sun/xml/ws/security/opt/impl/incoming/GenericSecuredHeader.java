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

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import com.sun.xml.stream.buffer.XMLStreamBufferMark;
import com.sun.xml.stream.buffer.XMLStreamBufferSource;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.message.AbstractHeaderImpl;
import com.sun.xml.ws.security.opt.api.NamespaceContextInfo;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.ws.security.opt.impl.util.XMLStreamReaderFactory;
import com.sun.istack.FinalArrayList;
import com.sun.xml.ws.message.Util;
import com.sun.xml.ws.security.opt.api.SecuredHeader;
import com.sun.xml.wss.impl.MessageConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.xml.bind.Unmarshaller;

import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.bind.JAXBException;

/**
 *
 * @author K.Venugopal@sun.com
 */

public class GenericSecuredHeader extends AbstractHeaderImpl implements SecuredHeader,NamespaceContextInfo {
    
    private static final String SOAP_1_1_MUST_UNDERSTAND = "mustUnderstand";
    private static final String SOAP_1_2_MUST_UNDERSTAND = SOAP_1_1_MUST_UNDERSTAND;
    private static final String SOAP_1_1_ROLE = "actor";
    private static final String SOAP_1_2_ROLE = "role";
    private static final String SOAP_1_2_RELAY = "relay";
    private XMLStreamBuffer completeHeader;
    //private XMLStreamBuffer headerContent;
    private boolean isMustUnderstand;
    private SOAPVersion soapVersion = null;
    //private boolean hasId = true;
    private Vector idValues = new Vector(2);
    private HashMap<String,String> shNSDecls = new HashMap<String,String>();
    private HashMap<String,String> nsDecls  = null;
    //private QName headerName = null;
    // never null. role or actor value
    private String role;
    private boolean isRelay;
    private String localName;
    private String namespaceURI="";
    private String id = "";
    private final FinalArrayList<Attribute> attributes;
    
    private boolean hasED = false;
    @SuppressWarnings("unchecked")
    public GenericSecuredHeader(XMLStreamReader reader,SOAPVersion soapVersion,StreamReaderBufferCreator creator,HashMap nsDecl, XMLInputFactory  staxIF, boolean encHeaderContent)throws XMLStreamBufferException, XMLStreamException  {
        
        this.shNSDecls = nsDecl;
        this.soapVersion = soapVersion;
        if(reader.getNamespaceURI() != null){
            namespaceURI = reader.getNamespaceURI();
        }
        localName = reader.getLocalName();
        attributes = processHeaderAttributes(reader);
        completeHeader = new XMLStreamBufferMark(this.nsDecls,creator);
        creator.createElementFragment(XMLStreamReaderFactory.createFilteredXMLStreamReader(reader,new IDProcessor()),true);
        nsDecls.putAll(shNSDecls);
        if(this.id.length() > 0){
            idValues.add(id);
        }

        if(encHeaderContent){
            checkEncryptedData();
        }
    }
    
    public boolean hasEncData(){
        return hasED;
    }
    @SuppressWarnings("unchecked")
    private void checkEncryptedData() throws XMLStreamException{
        XMLStreamReader reader = readHeader();
        while(StreamUtil.moveToNextElement(reader)){
            if(reader.getLocalName() == MessageConstants.ENCRYPTED_DATA_LNAME && 
                    reader.getNamespaceURI() == MessageConstants.XENC_NS){
                hasED = true;
                String encId = reader.getAttributeValue(null,"Id");
                if(encId != null && encId.length() > 0){
                    idValues.add(encId);
                }
                break;
            }
        }
    }
    
    private com.sun.istack.FinalArrayList<Attribute> processHeaderAttributes(XMLStreamReader reader) {
        if(soapVersion == soapVersion.SOAP_11){
            return process11Header(reader);
        }else{
            return process12Header(reader);
        }
    }
    
    private com.sun.istack.FinalArrayList<Attribute> process12Header(XMLStreamReader reader) {
        FinalArrayList<Attribute> atts = null;
        role = soapVersion.implicitRole;
        nsDecls = new HashMap<String,String>();
        //headerName = reader.getName();
        nsDecls = new HashMap<String,String>();
        if (reader.getNamespaceCount() > 0) {
            for (int i = 0; i < reader.getNamespaceCount(); i++) {
                nsDecls.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
            }
        }
        
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final String localName = reader.getAttributeLocalName(i);
            final String namespaceURI = reader.getAttributeNamespace(i);
            final String value = reader.getAttributeValue(i);
            if(namespaceURI == MessageConstants.WSU_NS && localName == "Id".intern()){
                //hasId = true;
                id = value;
                
            } else if(namespaceURI == null && localName == "Id".intern()){
                //hasId = true;
                id = value;
            }
            //handle other Id's eg SAML
            if (namespaceURI == SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE) {
                if (localName == SOAP_1_1_MUST_UNDERSTAND) {
                    isMustUnderstand = Util.parseBool(value);
                } else if (localName == SOAP_1_1_ROLE) {
                    if (value != null && value.length() > 0) {
                        role = value;
                    }
                }
            }
            
            if(atts==null) {
                atts = new FinalArrayList<Attribute>();
            }
            atts.add(new Attribute(namespaceURI,localName,value));
        }
        return atts;
    }
    
    private final FinalArrayList<Attribute> process11Header(XMLStreamReader reader) {
        FinalArrayList<Attribute> atts = null;
        
        role = soapVersion.implicitRole;
        //headerName = reader.getName();
        nsDecls = new HashMap<String,String>();
        if (reader.getNamespaceCount() > 0) {
            
            for (int j = 0; j < reader.getNamespaceCount(); j++) {
                nsDecls.put(reader.getNamespacePrefix(j), reader.getNamespaceURI(j));
            }
        }
        
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final String localName = reader.getAttributeLocalName(i);
            final String namespaceURI = reader.getAttributeNamespace(i);
            final String value = reader.getAttributeValue(i);
            
            if(namespaceURI == MessageConstants.WSU_NS && localName == "Id".intern()){
                //hasId = true;
                id = value;
            } else if(namespaceURI == null && localName == "Id".intern()){
                //hasId = true;
                id = value;
            }
            
            if (namespaceURI == SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE) {
                if (localName == SOAP_1_2_MUST_UNDERSTAND) {
                    isMustUnderstand = Util.parseBool(value);
                } else if (localName == SOAP_1_2_ROLE) {
                    if (value != null && value.length() > 0) {
                        role = value;
                    }
                } else if (localName == SOAP_1_2_RELAY) {
                    isRelay = Util.parseBool(value);
                }
            }
            
            if(atts==null) {
                atts = new FinalArrayList<Attribute>();
            }
            atts.add(new Attribute(namespaceURI,localName,value));
        }
        
        return atts;
    }
    
    
    
    public boolean hasID(String id) {
        return idValues.contains(id);
    }
    
    
    public final boolean isIgnorable( SOAPVersion soapVersion,  Set<String> roles) {
        // check mustUnderstand
        if(!isMustUnderstand) return true;
        
        // now role
        return !roles.contains(role);
    }
    
    public String getRole(SOAPVersion soapVersion) {
        assert role!=null;
        return role;
    }
    
    public boolean isRelay() {
        return isRelay;
    }
    
    public String getNamespaceURI() {
        return namespaceURI;
    }
    
    public String getLocalPart() {
        return localName;
    }
    
    public String getAttribute(String nsUri, String localName) {
        if(attributes!=null) {
            for(int i=attributes.size()-1; i>=0; i-- ) {
                Attribute a = attributes.get(i);
                if(a.localName.equals(localName) && a.nsUri.equals(nsUri))
                    return a.value;
            }
        }
        return null;
    }
    
    /**
     * Reads the header as a {@link XMLStreamReader}
     */
    public XMLStreamReader readHeader() throws XMLStreamException {
        return completeHeader.readAsXMLStreamReader();
    }
    
    public void writeTo(XMLStreamWriter w) throws XMLStreamException {
        try {
            // TODO what about in-scope namespaces
            completeHeader.writeToXMLStreamWriter(w);
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }
    }
    
    public void writeTo(SOAPMessage saaj) throws SOAPException {
        try {
            // TODO what about in-scope namespaces
            // Not very efficient consider implementing a stream buffer
            // processor that produces a DOM node from the buffer.
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            XMLStreamBufferSource source = new XMLStreamBufferSource(completeHeader);
            DOMResult result = new DOMResult();
            t.transform(source, result);
            Node d = result.getNode();
            if(d.getNodeType() == Node.DOCUMENT_NODE)
                d = d.getFirstChild();
            SOAPHeader header = saaj.getSOAPHeader();
            Node node = header.getOwnerDocument().importNode(d, true);
            header.appendChild(node);
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }
    
    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
        completeHeader.writeTo(contentHandler);
    }
    
    public String getStringContent(){
        try {
            XMLStreamReader xsr = readHeader();
            xsr.nextTag();
            return xsr.getElementText();
        } catch (XMLStreamException e) {
            return null;
        }
    }
    
    private static String fixNull(String s) {
        if(s==null) return "";
        else        return s;
    }
    @SuppressWarnings("unchecked")
    public <T> T readAsJAXB(Unmarshaller um) throws javax.xml.bind.JAXBException {
        try {
            return (T)um.unmarshal(completeHeader.readAsXMLStreamReader());
        } catch (XMLStreamException e) {
            throw new JAXBException(e);
        } catch (Exception e) {
            throw new JAXBException(e);
        }
    }
    
    public <T> T readAsJAXB(com.sun.xml.bind.api.Bridge<T>  bridge) throws javax.xml.bind.JAXBException {
        try {
            return bridge.unmarshal(completeHeader.readAsXMLStreamReader());
        } catch (XMLStreamException e) {
            throw new JAXBException(e);
        } catch (Exception e) {
            throw new JAXBException(e);
        }
    }
    
    public HashMap<String, String> getInscopeNSContext() {
        return nsDecls;
    }
    
    protected static final class Attribute {
        /**
         * Can be empty but never null.
         */
        final String nsUri;
        final String localName;
        final String value;
        
        public Attribute(String nsUri, String localName, String value) {
            this.nsUri = fixNull(nsUri);
            this.localName = localName;
            this.value = value;
        }
    }
    
    class IDProcessor implements StreamFilter{
        boolean elementRead = false;
        @SuppressWarnings("unchecked")
        public boolean accept(XMLStreamReader reader){
            if(reader.getEventType() == XMLStreamReader.END_ELEMENT ){
                if(reader.getLocalName() == localName && reader.getNamespaceURI() == namespaceURI){
                    elementRead = true;
                }
            }
            if(!elementRead && reader.getEventType() == XMLStreamReader.START_ELEMENT){
                String id = reader.getAttributeValue(MessageConstants.WSU_NS,"Id");
                if(id != null && id.length() > 0){
                    idValues.add(id);
                }
            }
            
            return true;
        }
    }
}
