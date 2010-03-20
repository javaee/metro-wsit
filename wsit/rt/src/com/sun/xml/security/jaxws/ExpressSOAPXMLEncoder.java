/*
 * ExpressSOAPXMLEncoder.java
 *
 * Created on August 5, 2005, 12:06 PM
 */

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

package com.sun.xml.security.jaxws;



import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.xml.messaging.saaj.soap.ExpressMessage;
import com.sun.xml.ws.spi.runtime.InternalSoapEncoder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;




/**
 * This encoder is used by the XWSS SOAP Plugin.This class is used to write different representations of SOAPMessage to the stream.
 * @author K.Venugopal@sun.com
 */
public class ExpressSOAPXMLEncoder {
    
    private static final String TAG_ENVELOPE = "Envelope";
    private static final String TAG_HEADER = "Header";
    private static final String TAG_BODY = "Body";
    private static final String TAG_FAULT = "Fault";
    private XMLSerializer _xmlSerializer;
    private XMLOutputFactory staxOF = null;
    /**
     * Creates a new instance of ExpressSOAPXMLEncoder
     */
    public ExpressSOAPXMLEncoder() {
        OutputFormat format = new OutputFormat();
        format.setOmitXMLDeclaration(true);
        _xmlSerializer = new XMLSerializer(format);
        staxOF = XMLOutputFactory.newInstance();
    }
    
    public ExpressSOAPXMLEncoder(XMLOutputFactory of) {
        OutputFormat format = new OutputFormat();
        format.setOmitXMLDeclaration(true);
        _xmlSerializer = new XMLSerializer(format);
        staxOF = of;
    }
    
    
    /**
     * Writes the complete SOAPMessage to the Stream.
     * @param baos OuputStream to which SOAPMessage has to be written.
     * @param message Message which is represented in different Object Models.
     * @param info JAXWS MessageInfo
     */
    public void writeSOAPMessage( OutputStream baos , JAXWSMessage message ) throws XMLStreamException, SOAPException, IOException{
        XMLStreamWriter writer = null;
        boolean _FI= false;
        
        writer = staxOF.createXMLStreamWriter(baos);
        writer.writeStartDocument();
        
        SOAPEnvelope env  = message.getSoapMessage().getSOAPPart().getEnvelope();
        writer.writeStartElement(env.getPrefix(),TAG_ENVELOPE, env.getNamespaceURI());
        writer.writeNamespace(env.getPrefix(),env.getNamespaceURI());
        writer.writeCharacters("");
        writer.writeStartElement(env.getPrefix(),TAG_HEADER,env.getPrefix());
        writer.writeCharacters("");
        writer.flush();
        
        Iterator hList = ((ExpressMessage)message.getSoapMessage()).getEMHeader().getChildElements();
        while(hList.hasNext()){
            Node node = (Node)hList.next();
            if(node.getNodeType() == Node.ELEMENT_NODE){
                _xmlSerializer.serialize((Element)node);
            }
        }
        writer.flush();
        if(!message.isHeaderUsed()){
            writeJAXWSHeaders(message,writer);
            writer.flush();
        }
        writer.writeEndElement();
        writer.flush();
        if(!message.isBodyUsed()){
            writer.writeStartElement(env.getPrefix(), TAG_BODY, env.getNamespaceURI());
            writer.writeCharacters("");
            writeJAXWSBody( message,writer,baos);
        }else{
            SOAPBody body = env.getBody();
            if(body.getNodeType() == Node.ELEMENT_NODE){
                _xmlSerializer.serialize((Element)body);
            }
        }
        writer.flush();
        writer.writeEndDocument();
        writer.flush();
        
        
    }
    
    public void writeJAXWSHeaders(JAXWSMessage message,XMLStreamWriter writer){
        InternalSoapEncoder encoder = message.getEncoder();
        Object messageInfo = message.getMessageInfo();
        List headers = message.getHeaders();
        for(int i=0;i<headers.size();i++){
            Object internalHeader = headers.get(i);
            //encoder.write(internalHeader,messageInfo,writer);
        }
    }
    
    public void writeJAXWSBody(JAXWSMessage message,XMLStreamWriter writer,OutputStream stream) throws IOException{
        InternalSoapEncoder encoder = message.getEncoder();
        Object messageInfo = message.getMessageInfo();
        byte [] cb = message.getCanonicalizedBody();
        if(cb!=null){
            stream.write(cb);
        }else{
            Object body = message.getBody();
            //encoder.write(body,messageInfo,writer);
        }
    }
    
    
    public void constructSOAPBody(JAXWSMessage message,Node body) throws XMLStreamException, IOException{
        XMLStreamWriter writer = staxOF.createXMLStreamWriter(new DOMResult(body));
        writeJAXWSBody(message,writer,null);
    }
    
    public void constructSOAPHeaders(JAXWSMessage message,Node header) throws XMLStreamException{
        XMLStreamWriter writer = staxOF.createXMLStreamWriter(new DOMResult(header));
        writeJAXWSHeaders( message,writer);
    }
    
    
    /*
    if((!message.isBodyUsed () && message.isHeaderUsed ()) || (message.isBodyUsed () && !message.isHeaderUsed ()) ){
         //commented out to be used when STAXWriter and STAX Readers are improved.
        DOMSource source = new DOMSource (message.getSoapMessage ().getSOAPPart ().getEnvelope ().getOwnerDocument ());
        XMLStreamReader reader = SourceReaderFactory.createSourceReader (source, true);
        XMLStreamReader  filteredReader = XMLInputFactory.newInstance ().createFilteredReader (reader,new DataFilter (message,writer));
        serializeReader (filteredReader,writer);*
     */
    
    
    
    /*
     * StAX Filters used to write SOAPHeader and Body elements . This is
     * used when SOAPMessage is DOM eg:Security Header is DOM Based
     * but the InternalMessage was not accessed by handlers or Security code.
     *
     * Note: Due to performance degradation using DOMReader and STAXWriter, this
     * class is not used and is retained to reuse when performance of the above two
     * implementations are improved.
     */
    
    class DataFilter implements StreamFilter {
        JAXWSMessage jxMessage = null;
        XMLStreamWriter streamWriter = null;
     
        DataFilter(JAXWSMessage message,XMLStreamWriter writer){
            jxMessage = message;
            streamWriter = writer;
        }
     
        public boolean accept(XMLStreamReader reader){
     
            if(reader.isEndElement()){
                String localName = reader.getLocalName();
                String uri = reader.getNamespaceURI();
     
                if("Header".equals(localName) && uri.equals(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE) || uri.equals("http://www.w3.org/2003/05/soap-envelope")){
                    if(!jxMessage.isHeaderUsed()){
                        writeJAXWSHeaders(jxMessage,streamWriter);
                    }
                }else if("Body".equals(localName) && uri.equals(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE) || uri.equals("http://www.w3.org/2003/05/soap-envelope")){
     
                    if(!jxMessage.isBodyUsed()){     
                        try{
                            writeJAXWSBody( jxMessage,streamWriter,null);
                            streamWriter.flush();
                        }catch(Exception ex){
                            throw new RuntimeException(ex);
                        }
                    }     
                }
            }
            return true;
        }
    }
     
     
     
}
