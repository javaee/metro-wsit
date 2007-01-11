/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.encoding;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.message.stream.StreamHeader;
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import javax.xml.stream.XMLStreamReader;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class LazyStreamCodec implements StreamSOAPCodec{
    
    private StreamSOAPCodec  codec = null;
    /** Creates a new instance of SecurityStream11Codec */
    public LazyStreamCodec(StreamSOAPCodec codec) {
        this.codec = codec;
    }
    
    public Message decode(XMLStreamReader reader) {
        return new com.sun.xml.ws.message.stream.LazyStreamBasedMessage(reader,codec);
    }
    
    public String getMimeType() {
        return codec.getMimeType();
    }
    
    public ContentType getStaticContentType(Packet packet) {
        return codec.getStaticContentType(packet);
    }
    
    public ContentType encode(Packet packet, OutputStream outputStream) throws IOException {
        return codec.encode(packet,outputStream);
    }
    
    public ContentType encode(Packet packet, WritableByteChannel writableByteChannel) {
        return codec.encode(packet,writableByteChannel);
    }
    
    public Codec copy() {
        return this;
    }
    
    public void decode(InputStream inputStream, String string, Packet packet) throws IOException {
        XMLStreamReader reader = XMLStreamReaderFactory.create(null, inputStream,true);
        packet.setMessage(decode(reader));
    }
    
    public void decode(ReadableByteChannel readableByteChannel, String string, Packet packet) {
        throw new UnsupportedOperationException();
    }    
}
