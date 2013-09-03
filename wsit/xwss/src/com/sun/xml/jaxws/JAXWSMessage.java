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

package com.sun.xml.jaxws;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.xml.messaging.saaj.soap.ExpressMessage;
import com.sun.xml.messaging.saaj.soap.MessageImpl;
import com.sun.xml.ws.spi.runtime.InternalSoapEncoder;

import com.sun.xml.ws.spi.runtime.MtomCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.handler.MessageContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class JAXWSMessage {
    private static final String TAG_ENVELOPE = "Envelope";
    private static final String TAG_HEADER = "Header";
    private static final String TAG_BODY = "Body";
    private static final String TAG_FAULT = "Fault";
    public static final String FAST_INFOSET_TYPE_SOAP11 =
            "application/fastinfoset";
    
    // FI + SOAP 1.2
    public static final String FAST_INFOSET_TYPE_SOAP12 =
            "application/soap+fastinfoset";
    
    // XML + XOP + SOAP 1.1
    public static final String XOP_SOAP11_XML_TYPE_VALUE =
            "application/xop+xml;type=\"text/xml\"";
    
    // XML + XOP + SOAP 1.2
    public static final String XOP_SOAP12_XML_TYPE_VALUE =
            "application/xop+xml;type=\"application/soap+xml\"";
    
    public static final String XML_CONTENT_TYPE_VALUE = "text/xml";
    private XMLSerializer _xmlSerializer;
    private static XMLOutputFactory staxOF = null;
    private Object messageInfo = null;
    private InternalSoapEncoder encoder = null;
    private boolean headerUsed = false;
    private boolean isBodyUsed = false;
    private List internalHeaders = null;
    private SOAPMessage soapMessage = null;
    private Object body = null;
    private byte[] cBody = null;
    private boolean isFI = false;
    Map mAttachments = new HashMap();
    MTOMCallbackImpl mtomCB = null;
    private int length;
    private ByteArrayOutputStream encryptedBody = null;
    
    
    static {
        try{
            staxOF = XMLOutputFactory.newInstance();
        }catch(Exception ex){
            //log error
        }
    }
    /** Creates a new instance of JAXWSMessage */
    public JAXWSMessage(){
        try{
            OutputFormat format = new OutputFormat();
            format.setOmitXMLDeclaration(true);
            _xmlSerializer = new XMLSerializer(format);            
        }catch(Exception ex){
            //this is bad , should not happen.
            ex.printStackTrace();
        }
    }
    
    
    /**
     * get the encoder used to write the JAXWS Message.
     * @return com.sun.xml.ws.spi.runtime.InternalSoapEncoder
     */
    public InternalSoapEncoder getEncoder() {
        return encoder;
    }
    
    /**
     * set the encoder to be used to write the JAXWS Message.
     * @param com.sun.xml.ws.spi.runtime.InternalSoapEncoder
     */
    public void setEncoder(InternalSoapEncoder encoder) {
        this.encoder = encoder;
    }
    
    /**
     *
     * @param msgInfo
     */
    public void setMessageInfo(Object msgInfo){
        messageInfo = msgInfo;
    }
    
    /**
     *
     * @return
     */
    public Object getMessageInfo(){
        return messageInfo;
    }
    
    /**
     *
     * @return  true if Headers supplied by JAXWS has been converted to SAAJ Objects
     */
    public boolean isHeaderUsed() {
        return headerUsed;
    }
    
    /**
     *
     * @param isHeaderUsed
     */
    public void setHeaderUsed(boolean isHeaderUsed) {
        this.headerUsed = isHeaderUsed;
    }
    
    /**
     *
     * @return true if Body supplied by JAXWS has been converted to SAAJ Objects
     */
    public boolean isBodyUsed() {
        return isBodyUsed;
    }
    
    /**
     *
     * @param isBodyUsed
     */
    public void setBodyUsed(boolean isBodyUsed) {
        this.isBodyUsed = isBodyUsed;
    }
    
    /**
     *
     * @param headers
     */
    public void setHeaders(List headers){
        internalHeaders = headers;
    }
    
    /**
     *
     * @return
     */
    public List getHeaders(){
        return internalHeaders;
    }
    
    /**
     *
     * @return instance of ExpressSOAPMessage.
     */
    public SOAPMessage getSoapMessage() {
        return soapMessage;
    }
    
    /**
     *
     * @param soapMessage
     */
    public void setSoapMessage(SOAPMessage soapMessage) throws SOAPException{
        this.soapMessage = soapMessage;
        initMTOMHandler();
    }
    
    /**
     *
     * @return
     */
    public Object getBody() {
        return body;
    }
    
    /**
     *
     * @param body
     */
    public void setBody(Object body) {
        this.body = body;
    }
    
    /**
     *
     * @return canonicalized form of the body if present, else returns null.
     * Canonicalized representation is what will be written to the stream if present.
     */
    public byte[] getCanonicalizedBody(){
        return cBody;
    }
    
    public int getCBLength(){
        return length;
    }
    
    /**
     *
     * @param data canonicalized representation of the body.
     */
    public void setCanonicalizedBody(byte[] data,int len){
        cBody = data;
        this.length = len;
    }
    
    
    /**
     * Writes the complete SOAPMessage to the Stream.
     * @param baos OuputStream to which SOAPMessage has to be written.
     */
    public void writeSOAPMessage( OutputStream baos ) throws XMLStreamException, SOAPException, IOException{
        XMLStreamWriter writer = null;
        boolean _FI= false;
        
        writer = staxOF.createXMLStreamWriter(baos);
        writer.writeStartDocument();
        _xmlSerializer.setOutputByteStream(baos);
        
        SOAPEnvelope env  = getSoapMessage().getSOAPPart().getEnvelope();
        writer.writeStartElement(env.getPrefix(),TAG_ENVELOPE, env.getNamespaceURI());
        //writer.writeNamespace(env.getPrefix(),env.getNamespaceURI());
        writeAttributes(env.getAttributes(),writer);
        writer.writeCharacters("");
        writer.writeStartElement(env.getPrefix(),TAG_HEADER,env.getNamespaceURI());
        SOAPHeader header = ((ExpressMessage)getSoapMessage()).getEMHeader();
        if(header!= null){
            writeAttributes(header.getAttributes(),writer);
            writer.writeCharacters("");
            writer.flush();
            Iterator hList = header.getChildElements();
            while(hList.hasNext()){
                Node node = (Node)hList.next();
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    _xmlSerializer.serialize((Element)node);
                }
            }
        }
        writer.flush();
        if(!isHeaderUsed()){
            writeJAXWSHeaders(writer);
            writer.flush();
        }
        writer.writeEndElement();
        writer.flush();
        
        if(!isBodyUsed()){
            if(encryptedBody != null){
                writer.writeStartElement(env.getPrefix(), TAG_BODY, env.getNamespaceURI());
                writeAttributes(((ExpressMessage)getSoapMessage()).getEMBody().getAttributes(),writer);
                writer.writeCharacters("");
                writer.flush();
                encryptedBody.writeTo(baos);
            }else if(getCanonicalizedBody() == null){
                writer.writeStartElement(env.getPrefix(), TAG_BODY, env.getNamespaceURI());
                writeAttributes(((ExpressMessage)getSoapMessage()).getEMBody().getAttributes(),writer);
                writer.writeCharacters("");
                writer.flush();
                writeJAXWSBody(baos);
            }else{
                byte [] cb = getCanonicalizedBody();
                baos.write(cb,0,length);
            }
        }else {
            SOAPBody body = env.getBody();
            if(body.getNodeType() == Node.ELEMENT_NODE){
                _xmlSerializer.serialize((Element)body);
            }
        }
        writer.flush();
        writer.writeEndDocument();
        writer.flush();
        
    }
    
    /**
     * Write JAXWS representation of SOAP Header objects
     * @param writer Stream Writer
     */
    public void writeJAXWSHeaders(XMLStreamWriter writer) throws SOAPException{
        InternalSoapEncoder encoder = getEncoder();
        Object messageInfo = getMessageInfo();
        List headers = getHeaders();
        if(headers == null){
            return;
        }
        
        for(int i=0;i<headers.size();i++){
            Object internalHeader = headers.get(i);
            encoder.write(internalHeader,messageInfo,writer,mtomCB);
        }
    }
    
    /**
     * write JAXWS representation of SOAP Body
     * @param writer
     * @param stream
     * @throws java.io.IOException
     */
    public void writeJAXWSBody(OutputStream stream) throws IOException,SOAPException{
        InternalSoapEncoder encoder = getEncoder();
        Object messageInfo = getMessageInfo();
        
        Object body = getBody();
        if(stream != null ){
            if(body != null){
                encoder.write(body,messageInfo,stream,mtomCB);
            }
        }else{
            throw new IOException("Stream cannot be null");
        }
        
    }
    
    private void initMTOMHandler() throws SOAPException {
        if(mtomCB == null ){
            if(soapMessage == null){
                throw new SOAPException("SOAPMessage cannot be null");
            }
            mtomCB = new MTOMCallbackImpl(mAttachments , soapMessage);
        }
    }
    public void writeJAXWSBody(XMLStreamWriter writer){
        
        InternalSoapEncoder encoder = getEncoder();
        Object messageInfo = getMessageInfo();
        Object body = getBody();
        if(body == null){
            return;
        }
        encoder.write(body,messageInfo,writer, mtomCB);
    }
    
    
    /**
     *
     * @param body
     * @throws javax.xml.stream.XMLStreamException
     * @throws java.io.IOException
     */
    public void constructSOAPBody(Node body) throws XMLStreamException, IOException{
        XMLStreamWriter writer = staxOF.createXMLStreamWriter(new DOMResult(body));
        writeJAXWSBody(writer);
    }
    
    /**
     *
     * @param header
     * @throws javax.xml.stream.XMLStreamException
     */
    public void constructSOAPHeaders( Node header) throws XMLStreamException,SOAPException{
        XMLStreamWriter writer = staxOF.createXMLStreamWriter(new DOMResult(header));
        writeJAXWSHeaders(writer);
    }
    
    
    private void writeAttributes(NamedNodeMap attrs , XMLStreamWriter writer)throws XMLStreamException{
        if(attrs== null){
            return;
        }
        for(int i=0;i< attrs.getLength();i++){
            Attr attr = (Attr)attrs.item(i);
            if(attr.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")){
                writer.writeNamespace(attr.getLocalName(),attr.getValue());
            } else{
                writer.writeAttribute(attr.getPrefix(),attr.getNamespaceURI(),attr.getLocalName(),attr.getValue());
            }
        }
    }
    
    //if FI is on will return FI StAX factory
    private XMLOutputFactory getStAXWriterFactory(){
        return staxOF;
    }
    
    
    public ByteArrayOutputStream getEncryptedBody() {
        return encryptedBody;
    }
    
    public void setEncryptedBody(ByteArrayOutputStream encryptedBody) {
        this.encryptedBody = encryptedBody;
    }
    
    public static SOAPMessage constructSOAPMessage(SOAPMessage sm , com.sun.xml.ws.spi.runtime.SOAPMessageContext msgContext) throws SOAPException{
        
        //Note : Once  JAXWS uses DOMWriter to build SOAPMessage in case of
        //context.getSOAPMessage this code can be removed.
        //until then ...
        
        //TODO:Create with appropriate MimeHeaders
        try{
            Object mi = msgContext.getMessageInfo();
            //SOAPMessage sm = soapMF.createMessage();
            setMimeHeader(msgContext,sm);
            
            List headers = msgContext.getHeaders();
            Object body = msgContext.getBody();
            InternalSoapEncoder encoder = msgContext.getEncoder();
            Object messageInfo  = msgContext.getMessageInfo();
            SOAPHeader header = sm.getSOAPHeader();
            XMLStreamWriter writer = null;
            Map  mtomAtt = new HashMap();
            MTOMCallbackImpl mci = new MTOMCallbackImpl(mtomAtt,sm);
            if(headers != null){
                writer = staxOF.createXMLStreamWriter(new DOMResult(header));
                for(int i=0;i<headers.size();i++){
                    Object internalHeader = headers.get(i);
                    encoder.write(internalHeader,messageInfo,writer, mci);
                }
            }
            if(body != null){
                writer = staxOF.createXMLStreamWriter(new DOMResult(sm.getSOAPBody()));
                encoder.write(body,messageInfo,writer,mci);
            }
            
            Map attachments = (Map)msgContext.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
            if(attachments != null){
                Set keys = attachments.keySet();
                Iterator itr = keys.iterator();
                while(itr.hasNext()){
                    String cid = (String) itr.next();
                    if(mtomAtt.keySet().contains(cid))
                        continue;
                    DataHandler dh = (DataHandler)attachments.get(cid);
                    AttachmentPart attachmentPart = sm.createAttachmentPart();
                    attachmentPart.setDataHandler(dh);
                    attachmentPart.setContentId(cid);
                    attachmentPart.setMimeHeader("Content-transfer-encoding", "binary");
                    sm.addAttachmentPart(attachmentPart);
                }
            }
            // add attachments here.
            msgContext.setMessage(sm);
            return sm;
        }catch(Exception ex){
            throw new SOAPException(ex);
        }
    }
    
    
    static class MTOMCallbackImpl implements MtomCallback {
        Map attachmentCont = null;
        SOAPMessage msg = null;
        MTOMCallbackImpl(Map map,SOAPMessage msg){
            attachmentCont = map;
            this.msg = msg;
        }
        
        public void addedMtomAttachment(String contentId, javax.activation.DataHandler attachment, String elementTargetNamespace, String elementLocalName) {
            AttachmentPart attachmentPart = msg.createAttachmentPart();
            attachmentPart.setDataHandler(attachment);
            attachmentPart.setContentId(contentId);
            attachmentPart.setMimeHeader("Content-transfer-encoding", "binary");
            MTOMAttachment ma  = new MTOMAttachment();
            ma.setAttachment(attachmentPart);
            ma.setNamespace(elementTargetNamespace);
            ma.setElementLocalName(elementLocalName);
            attachmentCont.put(contentId,ma);
            msg.addAttachmentPart(attachmentPart);
        }
    }
    
    static class MTOMAttachment {
        private String elementLocalName = null;
        private String namespace = null;
        private AttachmentPart attachment = null;
        
        public String getElementLocalName() {
            return elementLocalName;
        }
        
        public void setElementLocalName(String elementLocalName) {
            this.elementLocalName = elementLocalName;
        }
        
        public String getNamespace() {
            return namespace;
        }
        
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
        
        public AttachmentPart getAttachment() {
            return attachment;
        }
        
        public void setAttachment(AttachmentPart attachment) {
            this.attachment = attachment;
        }
        
    }
    
    public XMLSerializer getXmlSerializer() {
        return _xmlSerializer;
    }
    
    private static void setMimeHeader(com.sun.xml.ws.spi.runtime.SOAPMessageContext msgContext,SOAPMessage sm){
        MimeHeaders mh = sm.getMimeHeaders();
        String fiValue = (String) msgContext.get("com.sun.xml.ws.client.ContentNegotiation");
        
        String soapVersion = msgContext.getBindingId();
        //TODO: Remove after plugfest
        //Start
        boolean MTOM_ON = false;
        /*String _mtom = System.getProperty("SECURE_MTOM");
         
        if(_mtom != null){
            MTOM_ON = Boolean.valueOf(_mtom).booleanValue();
        }*/
        //end
        if(soapVersion == javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING){
            if("optimistic" == fiValue){
                mh.addHeader("Content-Type", FAST_INFOSET_TYPE_SOAP11);
                ((MessageImpl)sm).setIsFastInfoset(true);
                return ;
            }
            if(msgContext.isMtomEnabled() || MTOM_ON){
                mh.addHeader("Content-Type", XOP_SOAP11_XML_TYPE_VALUE);
                return ;
            }
            mh.addHeader("Content-Type", SOAPConstants.SOAP_1_1_CONTENT_TYPE);
            return ;
        }else{
            if("optimistic" == fiValue){
                mh.addHeader("Content-Type", FAST_INFOSET_TYPE_SOAP12);
                return;
            }
            if(msgContext.isMtomEnabled() || MTOM_ON){
                mh.addHeader("Content-Type", XOP_SOAP12_XML_TYPE_VALUE);
                return ;
            }
            mh.addHeader("Content-Type", SOAPConstants.SOAP_1_2_CONTENT_TYPE);
            return ;
        }
    }
}
