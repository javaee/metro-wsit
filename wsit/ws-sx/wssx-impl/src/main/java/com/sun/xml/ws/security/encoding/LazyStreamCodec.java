/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.security.encoding;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

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
    
    @Override
    public Message decode(XMLStreamReader reader) {
        return new com.sun.xml.ws.security.message.stream.LazyStreamBasedMessage(reader,codec);
    }
    
    public  @NotNull@Override
 Message decode(@NotNull XMLStreamReader reader, AttachmentSet att){
        return new com.sun.xml.ws.security.message.stream.LazyStreamBasedMessage(reader,codec, att);
    }
    
    @Override
    public String getMimeType() {
        return codec.getMimeType();
    }
    
    @Override
    public ContentType getStaticContentType(Packet packet) {
        return codec.getStaticContentType(packet);
    }
    
    @Override
    public ContentType encode(Packet packet, OutputStream outputStream) throws IOException {
        return codec.encode(packet,outputStream);
    }
    
    @Override
    public ContentType encode(Packet packet, WritableByteChannel writableByteChannel) {
        return codec.encode(packet,writableByteChannel);
    }
    
    @Override
    public Codec copy() {
        return this;
    }
    
    @Override
    public void decode(InputStream inputStream, String string, Packet packet) throws IOException {
        XMLStreamReader reader = XMLStreamReaderFactory.create(null, inputStream,true);
        packet.setMessage(decode(reader));
    }
    
    @Override
    public void decode(ReadableByteChannel readableByteChannel, String string, Packet packet) {
        throw new UnsupportedOperationException();
    }
    
}
