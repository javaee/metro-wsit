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
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.ws.security.opt.api.NamespaceContextInfo;
import com.sun.xml.ws.security.opt.api.PolicyBuilder;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.TokenValidator;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.incoming.processor.KeyInfoProcessor;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;

import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.xml.crypto.KeySelector.Purpose;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import static com.sun.xml.wss.impl.MessageConstants.SIGNATURE_LNAME;
import static com.sun.xml.wss.impl.MessageConstants.DSIG_NS;
import javax.security.auth.Subject;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;


/**
 *
 * @author K.Venugopal@sun.com
 */
public class SAMLAssertion implements SecurityHeaderElement,PolicyBuilder,TokenValidator,NamespaceContextInfo,
        SecurityElementWriter{
    
    private String id ="";
    private String localName= "";
    private String namespaceURI="";
    private Key key = null;
    private JAXBFilterProcessingContext jpc = null;
    private HashMap<String,String> samlHeaderNSContext = null;
    private StreamReaderBufferCreator creator = null;
    private Signature sig = null;
    private MutableXMLStreamBuffer buffer = null;
    private boolean signatureNSinReader = false;
    
    private AuthenticationTokenPolicy.SAMLAssertionBinding samlPolicy = null;
    
    private static final String KEYINFO_ELEMENT = "KeyInfo";
    private static final String SUBJECT_CONFIRMATION_ELEMENT = "SubjectConfirmation";
    
    /** Creates a new instance of SAMLAssertion */
    public SAMLAssertion(XMLStreamReader reader, JAXBFilterProcessingContext jpc,StreamReaderBufferCreator creator ,HashMap nsDecl) throws XWSSecurityException{
        this.jpc = jpc;
        this.creator = creator;
        id = reader.getAttributeValue(null,"AssertionID");
        if(id == null){
            id = reader.getAttributeValue(null,"ID");
        }
        namespaceURI = reader.getNamespaceURI();
        localName = reader.getLocalName();
        samlHeaderNSContext = new HashMap<String,String>();
        samlHeaderNSContext.putAll(nsDecl);        
        if (reader.getNamespaceCount() > 0) {
            for (int i = 0; i < reader.getNamespaceCount(); i++) {                
                samlHeaderNSContext.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
                if("ds".equals(reader.getNamespacePrefix(i)) && "http://www.w3.org/2000/09/xmldsig#".equals(reader.getNamespaceURI(i))){
                    signatureNSinReader = true;
                }
            }
        }
        
        samlPolicy = new AuthenticationTokenPolicy.SAMLAssertionBinding();
        samlPolicy.setUUID(id);
        
        //to be picked up from pool of buffers.
        buffer = new MutableXMLStreamBuffer();
        //StreamWriterBufferCreator bCreator = new StreamWriterBufferCreator(buffer);
        try {            
            buffer.createFromXMLStreamReader(reader);
            process(buffer.readAsXMLStreamReader());                        
        } catch (XMLStreamException xe) {
            throw new XWSSecurityException("Error occurred while reading SAMLAssertion",xe);
        }
        
    }

    public XMLStreamReader getSamlReader() throws XMLStreamException, XWSSecurityException {
        XMLStreamReader samlReader = readHeader();
        try {
            /*if (isSignatureNSinReader()) {
                return samlReader;
            }*/
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            XMLInputFactory xif = XMLInputFactory.newInstance();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter writer = xof.createXMLStreamWriter(baos);
            boolean samlElementProcessed = false;
            while (!(XMLStreamReader.END_DOCUMENT == samlReader.getEventType())) {
                if ((XMLStreamReader.START_ELEMENT == samlReader.getEventType()) &&
                        samlReader.getLocalName().equals("Assertion") && !samlElementProcessed) {
                    writer.writeStartElement(samlReader.getPrefix(), samlReader.getLocalName(), samlReader.getNamespaceURI());

                    Set<String> samlNSKeySet = samlHeaderNSContext.keySet();
                    Iterator<String> it = samlNSKeySet.iterator();
                    while (it.hasNext()) {
                        String prefix = (String) it.next();
                        writer.writeNamespace(prefix, samlHeaderNSContext.get(prefix));
                    }

                    int atCount = samlReader.getAttributeCount();
                    for (int i = 0; i < atCount; i++) {
                        if (samlReader.getAttributePrefix(i) == "" || samlReader.getAttributePrefix(i) == null) {
                            writer.writeAttribute(samlReader.getAttributeLocalName(i), samlReader.getAttributeValue(i));
                        } else {
                            writer.writeAttribute(samlReader.getAttributePrefix(i), samlReader.getAttributeNamespace(i), samlReader.getAttributeLocalName(i), samlReader.getAttributeValue(i));
                        }
                    }
                    samlElementProcessed = true;
                } else {
                    com.sun.xml.ws.security.opt.impl.util.StreamUtil.writeCurrentEvent(samlReader, writer);
                }
                samlReader.next();
            }
            writer.close();
            try {
                baos.close();
            } catch (IOException ex) {
                throw new XWSSecurityException("Error occurred while processing SAMLAssertion of type XMLSreamReader", ex);
            }
            samlReader = xif.createXMLStreamReader(new ByteArrayInputStream(baos.toByteArray()));
        } catch (XMLStreamException ex) {
            throw new XWSSecurityException("Error occurred while processing SAMLAssertion of type XMLSreamReader", ex);
        }
        return samlReader;
    }
    
    private boolean isSignatureNSinReader(){
        return this.signatureNSinReader;
    }
    public SAMLAssertion(){
    }
   
    
    public boolean refersToSecHdrWithId(String id) {
        return false;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(final String id) {
        throw new UnsupportedOperationException("not implemented");
    }
    
    public String getNamespaceURI() {
        return namespaceURI;
    }
    
    public String getLocalPart() {
        return localName;
    }
    
    public XMLStreamReader readHeader() throws XMLStreamException {        
        return buffer.readAsXMLStreamReader();
    }    
    
    public WSSPolicy getPolicy() {
        return samlPolicy;
    }
    
    public void validate(ProcessingContext context) throws XWSSecurityException {
        try{
            XMLStreamReader samlReader = getSamlReader();
            context.getSecurityEnvironment().validateSAMLAssertion(context.getExtraneousProperties(), samlReader);
            context.getSecurityEnvironment().updateOtherPartySubject((Subject)context.getExtraneousProperties().get(MessageConstants.AUTH_SUBJECT),samlReader);
        }catch(XMLStreamException xe){
            throw new XWSSecurityException("Error occurred while trying to validate SAMLAssertion",xe);
        }
    }
    
    public HashMap<String, String> getInscopeNSContext() {
        return samlHeaderNSContext;
    }
    
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
        buffer.writeToXMLStreamWriter(streamWriter);
    }
    
    public void writeTo(XMLStreamWriter streamWriter, HashMap props) throws XMLStreamException {
        //is this ok?
        writeTo(streamWriter);
    }
    
    public void writeTo(OutputStream os) {
        throw new UnsupportedOperationException();
    }
    
    public boolean isHOK(){
        if(sig != null){
            return true;
        }
        return false;
    }
    
    public boolean validateSignature()throws XWSSecurityException{
        if(isHOK()){
            return sig.validate();
        }
        return false;
    }
    
    public void processNoValidation(XMLStreamReader reader,XMLStreamWriter buffer) throws XWSSecurityException{
       
        try{
            StreamUtil.writeCurrentEvent(reader,buffer);
            while(reader.hasNext()){               
                reader.next();
                if(_break(reader)){
                    StreamUtil.writeCurrentEvent(reader,buffer);
                    reader.next();
                    break;
                }else{
                    StreamUtil.writeCurrentEvent(reader,buffer);
                }
            }
        }catch(XMLStreamException xe){
            throw new XWSSecurityException("Error occurred while reading SAMLAssertion",xe);
        }
    }
    
    /*public void process(XMLStreamReader reader,XMLStreamWriter buffer) throws XWSSecurityException{       
        
        try{
            StreamUtil.writeCurrentEvent(reader,buffer);
            while(reader.hasNext()){               
                reader.next();
                switch(reader.getEventType()){
                    case XMLStreamReader.START_ELEMENT :{                        
                        if(reader.getLocalName() == SIGNATURE_LNAME && reader.getNamespaceURI() == DSIG_NS){
                            sig = new Signature(jpc,samlHeaderNSContext,creator,false);
                            jpc.isSamlSignatureKey(true);
                            sig.process(reader, false);  
                            jpc.isSamlSignatureKey(false);
                        }
                        break;
                    }                    
                }
                if(_break(reader)){
                    StreamUtil.writeCurrentEvent(reader,buffer);
                    reader.next();
                    break;
                }else{
                    if(reader.getEventType() == reader.START_ELEMENT && reader.getLocalName().equals("Advice")){                        
                        StreamUtil.writeCurrentEvent(reader,buffer);
                        skipAdviceValidation(reader, buffer);
                    }else{
                        StreamUtil.writeCurrentEvent(reader,buffer);
                    }
                }
            }
        }catch(XMLStreamException xe){
            throw new XWSSecurityException("Error occurred while reading SAMLAssertion",xe);
        }
    }*/
    
    public void process(XMLStreamReader reader) throws XWSSecurityException{       
        
        try{
            while(reader.hasNext()){               
                reader.next();
                switch(reader.getEventType()){
                    case XMLStreamReader.START_ELEMENT :{                        
                        if(reader.getLocalName() == SIGNATURE_LNAME && reader.getNamespaceURI() == DSIG_NS){
                            sig = new Signature(jpc,samlHeaderNSContext,creator,false);
                            jpc.isSamlSignatureKey(true);
                            sig.process(reader, false);  
                            jpc.isSamlSignatureKey(false);
                        }
                        break;
                    }                    
                }
                if(_break(reader)){
                    reader.next();
                    break;
                }else{
                    if(reader.getEventType() == reader.START_ELEMENT && reader.getLocalName().equals("Advice")){
                        skipAdviceValidation(reader);
                    }
                }
            }
        }catch(XMLStreamException xe){
            throw new XWSSecurityException("Error occurred while reading SAMLAssertion",xe);
        }        
    }
    
    /*public void skipAdviceValidation(XMLStreamReader reader,XMLStreamWriter buffer) throws XWSSecurityException{
        int adviceElementCount = 1;
        try{
            while(!(reader.getLocalName().equals("Advice") && 
                        reader.getEventType() == reader.END_ELEMENT && 
                            adviceElementCount == 0)){                
                reader.next();
                if(reader.getEventType() == reader.START_ELEMENT && reader.getLocalName().equals("Advice")){
                    adviceElementCount++;
                }
                if(reader.getEventType() == reader.END_ELEMENT && reader.getLocalName().equals("Advice")){
                    adviceElementCount--;
                }
                StreamUtil.writeCurrentEvent(reader,buffer);
            }
        }catch(XMLStreamException xe){
            throw new XWSSecurityException("Error occurred while reading SAMLAssertion",xe);
        }
    }*/
    
    public void skipAdviceValidation(XMLStreamReader reader) throws XWSSecurityException{
        int adviceElementCount = 1;
        try{
            while(!(reader.getLocalName().equals("Advice") && 
                        reader.getEventType() == reader.END_ELEMENT && 
                            adviceElementCount == 0)){                
                reader.next();
                if(reader.getEventType() == reader.START_ELEMENT && reader.getLocalName().equals("Advice")){
                    adviceElementCount++;
                }
                if(reader.getEventType() == reader.END_ELEMENT && reader.getLocalName().equals("Advice")){
                    adviceElementCount--;
                }
            }
        }catch(XMLStreamException xe){
            throw new XWSSecurityException("Error occurred while reading SAMLAssertion",xe);
        }
    }
    
    public Key getKey()throws XWSSecurityException{
        if(key == null){
            try{
                XMLStreamReader reader = readHeader();
                while(reader.getEventType() != reader.END_DOCUMENT){
                    switch(reader.getEventType()){
                        case XMLStreamReader.START_ELEMENT :{
                            if (reader.getLocalName() == SUBJECT_CONFIRMATION_ELEMENT){
                                reader.next();
                                while(!(reader.getLocalName()== SUBJECT_CONFIRMATION_ELEMENT  &&
                                        reader.getEventType() == reader.END_ELEMENT)){
                                    reader.next();
                                    if(reader.getEventType() == reader.START_ELEMENT && reader.getLocalName() == KEYINFO_ELEMENT && reader.getNamespaceURI() == DSIG_NS){
                                        jpc.isSAMLEK(true);
                                        KeyInfoProcessor kip = new KeyInfoProcessor(jpc,Purpose.VERIFY, true);
                                        key = kip.getKey(reader);
                                        jpc.isSAMLEK(false);
                                        return key;
                                    }
                                }
                            }
                            break;
                        }
                        default:{
                            break;
                        }
                    }
                    if(reader.hasNext()){
                        if(reader.getEventType() == reader.START_ELEMENT && reader.getLocalName().equals("Advice")){
                            int adviceElementCount = 1;
                            while(!(reader.getLocalName().equals("Advice") &&
                                    reader.getEventType() == reader.END_ELEMENT &&
                                    adviceElementCount == 0)){
                                reader.next();
                                if(reader.getEventType() == reader.START_ELEMENT && reader.getLocalName().equals("Advice")){
                                    adviceElementCount++;
                                }
                                if(reader.getEventType() == reader.END_ELEMENT && reader.getLocalName().equals("Advice")){
                                    adviceElementCount--;
                                }
                            }
                            reader.next();
                        }else{
                            reader.next();
                        }
                    }else{
                        break;//should not happen;
                    }
                }
            }catch(XMLStreamException xe){
                throw new XWSSecurityException("Error occurred while obtaining Key from SAMLAssertion",xe);
            }
        }
        return key;
    }
    
    private boolean _break(XMLStreamReader reader) {
        if(reader.getEventType() == reader.END_ELEMENT){
            if(reader.getLocalName() == MessageConstants.SAML_ASSERTION_LNAME ){
                String uri = reader.getNamespaceURI();
                if( uri == MessageConstants.SAML_v2_0_NS || uri ==MessageConstants.SAML_v1_0_NS || uri == MessageConstants.SAML_v1_1_NS ){
                    return true;
                }
            }
        }
        return false;
    }
    
}
