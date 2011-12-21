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

package com.sun.xml.ws.security.message.stream;

import com.sun.xml.ws.message.stream.*;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import java.io.IOException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.wss.jaxws.impl.logging.LogDomainConstants;
import java.util.logging.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class LazyStreamBasedMessage extends Message{
    protected static final Logger logger =
            Logger.getLogger(
            LogDomainConstants.WSS_JAXWS_IMPL_DOMAIN,
            LogDomainConstants.WSS_JAXWS_IMPL_DOMAIN_BUNDLE);
    private StreamSOAPCodec codec = null;
    private boolean readMessage = false;
    private XMLStreamReader reader = null;
    private Message message = null;
    AttachmentSet as = null;
    private MutableXMLStreamBuffer buffer = null;
    /** Creates a new instance of StreamMessage */
    public LazyStreamBasedMessage(XMLStreamReader message,StreamSOAPCodec codec) {
        this.reader = message;
        this.codec = codec;
    }
    
    public LazyStreamBasedMessage(XMLStreamReader message,StreamSOAPCodec codec, AttachmentSet as) {
        this.reader = message;
        this.codec = codec;
        this.as = as;
    }
    
    public StreamSOAPCodec getCodec(){
        return codec;
    }
    
    private synchronized void cacheMessage(){
        if(!readMessage){
            if(as == null){
                message = codec.decode(reader);
            } else {
                message = codec.decode(reader, as);
            }
            readMessage = true;
        }
    }
    
    
    
    /**
     * Returns true if headers are present in the message.
     *
     * @return
     *      true if headers are present.
     */
    public boolean hasHeaders(){
        if(!readMessage){
            cacheMessage();
        }
        return message.hasHeaders();
    }
    
    /**
     * Gets all the headers of this message.
     *
     * <h3>Implementation Note</h3>
     * <p>
     * {@link Message} implementation is allowed to defer
     * the construction of {@link HeaderList} object. So
     * if you only want to check for the existence of any header
     * element, use {@link #hasHeaders()}.
     *
     * @return
     *      always return the same non-null object.
     */
    public HeaderList getHeaders(){
        if(!readMessage){
            cacheMessage();
        }
        return message.getHeaders();
    }
    
    /**
     * Gets the attachments of this message
     * (attachments live outside a message.)
     */
    public @NotNull AttachmentSet getAttachments() {
        if(!readMessage){
            cacheMessage();
        }
        return message.getAttachments();
    }
    
    
    
    
    
    
    /**
     * Returns true if this message is a request message for a
     * one way operation according to the given WSDL. False otherwise.
     *
     * <p>
     * This method is functionally equivalent as doing
     * {@code getOperation(port).getOperation().isOneWay()}
     * (with proper null check and all.) But this method
     * can sometimes work faster than that (for example,
     * on the client side when used with SEI.)
     *
     * @param port
     *      {@link Message}s are always created under the context of
     *      one {@link WSDLPort} and they never go outside that context.
     *      Pass in that "governing" {@link WSDLPort} object here.
     *      We chose to receive this as a parameter instead of
     *      keeping {@link WSDLPort} in a message, just to save the storage.
     *
     *      <p>
     *      The implementation of this method involves caching the return
     *      value, so the behavior is undefined if multiple callers provide
     *      different {@link WSDLPort} objects, which is a bug of the caller.
     */
    public boolean isOneWay(@NotNull WSDLPort port) {
        if(!readMessage){
            cacheMessage();
        }
        return message.isOneWay(port);
    }
    
    /**
     * Gets the local name of the payload element.
     *
     * @return
     *      null if a {@link Message} doesn't have any payload.
     */
    public  @Nullable String getPayloadLocalPart(){
        if(!readMessage){
            cacheMessage();
        }
        return message.getPayloadLocalPart();
    }
    
    /**
     * Gets the namespace URI of the payload element.
     *
     * @return
     *      null if a {@link Message} doesn't have any payload.
     */
    public String getPayloadNamespaceURI(){
        if(!readMessage){
            cacheMessage();
        }
        return message.getPayloadNamespaceURI();
    }
    // I'm not putting @Nullable on it because doing null check on getPayloadLocalPart() should be suffice
    
    /**
     * Returns true if a {@link Message} has a payload.
     *
     * <p>
     * A message without a payload is a SOAP message that looks like:
     * <pre><xmp>
     * <S:Envelope>
     *   <S:Header>
     *     ...
     *   </S:Header>
     *   <S:Body />
     * </S:Envelope>
     * </xmp></pre>
     */
    public boolean hasPayload(){
        if(!readMessage){
            cacheMessage();
        }
        return message.hasPayload();
    }
    

    /**
     * Consumes this message including the envelope.
     * returns it as a {@link Source} object.
     */
    public Source readEnvelopeAsSource(){
        if(!readMessage){
            cacheMessage();
        }
        return message.readEnvelopeAsSource();
    }
    
    
    /**
     * Returns the payload as a {@link Source} object.
     *
     * This consumes the message.
     *
     * @return
     *      if there's no payload, this method returns null.
     */
    public Source readPayloadAsSource(){
        if(!readMessage){
            cacheMessage();
        }
        return message.readPayloadAsSource();
    }
    
    /**
     * Creates the equivalent {@link SOAPMessage} from this message.
     *
     * This consumes the message.
     *
     * @throws SOAPException
     *      if there's any error while creating a {@link SOAPMessage}.
     */
    public SOAPMessage readAsSOAPMessage() throws SOAPException{
        if(!readMessage){
            cacheMessage();
        }
        return message.readAsSOAPMessage();
    }
    
    /**
     * Reads the payload as a JAXB object by using the given unmarshaller.
     *
     * This consumes the message.
     *
     * @throws JAXBException
     *      If JAXB reports an error during the processing.
     */
    public <T> T readPayloadAsJAXB(Unmarshaller unmarshaller) throws JAXBException{
        if(!readMessage){
            cacheMessage();
        }
        throw new UnsupportedOperationException();
    }
    
    /**
     * Reads the payload as a JAXB object according to the given {@link Bridge}.
     *
     * This consumes the message.
     *
     * @throws JAXBException
     *      If JAXB reports an error during the processing.
     */
    public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException{
        if(!readMessage){
            cacheMessage();
        }
        return message.readPayloadAsJAXB(bridge);
    }
    
    
    
    
    /**
     * Reads the payload as a {@link XMLStreamReader}
     *
     * This consumes the message.
     *
     * @return
     *      If there's no payload, this method returns null.
     *      Otherwise always non-null valid {@link XMLStreamReader} that points to
     *      the payload tag name.
     */
    public  XMLStreamReader readPayload() throws XMLStreamException{
        if(!readMessage){
            cacheMessage();
        }
        return message.readPayload();
    }
    
    /**
     * Writes the payload to StAX.
     *
     * This method writes just the payload of the message to the writer.
     * This consumes the message.
     * The implementation will not write
     * {@link XMLStreamWriter#writeStartDocument()}
     * nor
     * {@link XMLStreamWriter#writeEndDocument()}
     *
     * <p>
     * If there's no payload, this method is no-op.
     *
     * @throws XMLStreamException
     *      If the {@link XMLStreamWriter} reports an error,
     *      or some other errors happen during the processing.
     */
    public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException{
        if(!readMessage){
            cacheMessage();
        }
        message.writePayloadTo(sw);
    }
    
    /**
     * Writes the whole SOAP message (but not attachments)
     * to the given writer.
     *
     * This consumes the message.
     *
     * @throws XMLStreamException
     *      If the {@link XMLStreamWriter} reports an error,
     *      or some other errors happen during the processing.
     */
    public void writeTo(XMLStreamWriter sw) throws XMLStreamException{
        if(!readMessage){
            cacheMessage();
        }
        message.writeTo(sw);
    }
    
    /**
     * Writes the whole SOAP envelope as SAX events.
     *
     * <p>
     * This consumes the message.
     *
     * @param contentHandler
     *      must not be nulll.
     * @param errorHandler
     *      must not be null.
     *      any error encountered during the SAX event production must be
     *      first reported to this error handler. Fatal errors can be then
     *      thrown as {@link SAXParseException}. {@link SAXException}s thrown
     *      from {@link ErrorHandler} should propagate directly through this method.
     */
    public void writeTo( ContentHandler contentHandler, ErrorHandler errorHandler ) throws SAXException{
        if(!readMessage){
            cacheMessage();
        }
        message.writeTo(contentHandler,errorHandler);
    }
    
    // TODO: do we need a method that reads payload as a fault?
    // do we want a separte streaming representation of fault?
    // or would SOAPFault in SAAJ do?
    
    
    
    /**
     * Creates a copy of a {@link Message}.
     *
     * <p>
     * This method creates a new {@link Message} whose header/payload/attachments/properties
     * are identical to this {@link Message}. Once created, the created {@link Message}
     * and the original {@link Message} behaves independently --- adding header/
     * attachment to one {@link Message} doesn't affect another {@link Message}
     * at all.
     *
     * <p>
     * This method does <b>NOT</b> consume a message.
     *
     * <p>
     * To enable efficient copy operations, there's a few restrictions on
     * how copied message can be used.
     *
     * <ol>
     *  <li>The original and the copy may not be
     *      used concurrently by two threads (this allows two {@link Message}s
     *      to share some internal resources, such as JAXB marshallers.)
     *      Note that it's OK for the original and the copy to be processed
     *      by two threads, as long as they are not concurrent.
     *
     *  <li>The copy has the same 'life scope'
     *      as the original (this allows shallower copy, such as
     *      JAXB beans wrapped in {@link JAXBMessage}.)
     * </ol>
     *
     * <p>
     * A 'life scope' of a message created during a message processing
     * in a pipeline is until a pipeline processes the next message.
     * A message cannot be kept beyond its life scope.
     *
     * (This experimental design is to allow message objects to be reused
     * --- feedback appreciated.)
     *
     *
     *
     * <h3>Design Rationale</h3>
     * <p>
     * Since a {@link Message} body is read-once, sometimes
     * (such as when you do fail-over, or WS-RM) you need to
     * create an idential copy of a {@link Message}.
     *
     * <p>
     * The actual copy operation depends on the layout
     * of the data in memory, hence it's best to be done by
     * the {@link Message} implementation itself.
     *
     * <p>
     * The restrictions placed on the use of copied {@link Message} can be
     * relaxed if necessary, but it will make the copy method more expensive.
     */
    // TODO: update the class javadoc with 'lifescope'
    // and move the discussion about life scope there.
    public  Message copy(){
        if(!readMessage){
            cacheMessage();
        }
        return message.copy();
    }
    
    public XMLStreamReader readMessage(){
        
        if (!readMessage) {
            return reader;
        }
        
        if (buffer == null) {
            try {
                buffer = new com.sun.xml.stream.buffer.MutableXMLStreamBuffer();
                javax.xml.stream.XMLStreamWriter writer = buffer.createFromXMLStreamWriter();
                
                message.writeTo(writer);
            } catch (javax.xml.stream.XMLStreamException ex) {
                logger.log(java.util.logging.Level.SEVERE,LogStringsMessages.WSSMSG_0001_PROBLEM_CACHING(),ex);
            }
        }
        try     {
            reader = buffer.readAsXMLStreamReader();
            return reader;
        } catch (XMLStreamException ex) {
            logger.log(java.util.logging.Level.SEVERE,LogStringsMessages.WSSMSG_0002_ERROR_READING_BUFFER(),ex);                   
        }
        return null;
    }
    
    
    public void print() throws XMLStreamException{
        if(readMessage){
            try         {
                message.readAsSOAPMessage().writeTo(java.lang.System.out);
                return;
            } catch (SOAPException ex) {
                logger.log(java.util.logging.Level.SEVERE,
                       LogStringsMessages.WSSMSG_0003_ERROR_PRINT(),ex);
            } catch (IOException ex) {
                logger.log(java.util.logging.Level.SEVERE,
                       LogStringsMessages.WSSMSG_0003_ERROR_PRINT(),ex);
            }
        }
        if(buffer == null){
            buffer = new MutableXMLStreamBuffer();
            buffer.createFromXMLStreamReader(reader);
            reader =  buffer.readAsXMLStreamReader();
        }
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        buffer.writeToXMLStreamWriter(xof.createXMLStreamWriter(System.out));
    }

	public <T> T readPayloadAsJAXB(XMLBridge<T> bridge) throws JAXBException {
        if(!readMessage){
            cacheMessage();
        }
        return message.readPayloadAsJAXB(bridge);
	}

    /**
     * Since the StreamMessage is leaving out the white spaces around message payload,
     * it must be handled specially to allow message signature verification
     * @return white space prolog of the SOAP message body
     */
    public String getBodyEpilogue() {
        return message instanceof StreamMessage ?
                    ((StreamMessage) message).getBodyEpilogue(): null;
    }

    /**
     * Since the StreamMessage is leaving out the white spaces around message payload,
     * it must be handled specially to allow message signature verification
     * @return white space epilog of the SOAP message body
     */
    public String getBodyPrologue() {
        return message instanceof StreamMessage ?
                ((StreamMessage) message).getBodyPrologue() : null;
    }
}

