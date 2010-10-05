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

package com.sun.xml.ws.transport.tcp.encoding;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.transport.tcp.encoding.WSTCPFastInfosetStreamReaderRecyclable.RecycleAwareListener;
import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;
import com.sun.xml.fastinfoset.vocab.ParserVocabulary;
import com.sun.xml.fastinfoset.vocab.SerializerVocabulary;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.encoding.ContentTypeImpl;
import com.sun.xml.ws.encoding.fastinfoset.FastInfosetStreamSOAPCodec;
import com.sun.xml.ws.message.stream.StreamHeader;
import com.sun.xml.ws.transport.tcp.encoding.configurator.WSTCPCodecConfigurator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

/**
 * @author Alexey Stashok
 */
public abstract class WSTCPFastInfosetStreamCodec implements Codec {
    private StAXDocumentParser _statefulParser;
    private StAXDocumentSerializer _serializer;
    
    private final StreamSOAPCodec _soapCodec;
    private final boolean _retainState;
    
    protected final ContentType _defaultContentType;
    
    private final RecycleAwareListener _readerRecycleListener;
    
    /* package */ WSTCPFastInfosetStreamCodec(@Nullable StreamSOAPCodec soapCodec, @NotNull SOAPVersion soapVersion,
            @NotNull RecycleAwareListener readerRecycleListener, boolean retainState, String mimeType) {
        _soapCodec = soapCodec != null ? soapCodec : Codecs.createSOAPEnvelopeXmlCodec(soapVersion);
        _readerRecycleListener = readerRecycleListener;
        _retainState = retainState;
        _defaultContentType = new ContentTypeImpl(mimeType);
    }
    
    /* package */ WSTCPFastInfosetStreamCodec(WSTCPFastInfosetStreamCodec that) {
        this._soapCodec = (StreamSOAPCodec) that._soapCodec.copy();
        this._readerRecycleListener = that._readerRecycleListener;
        this._retainState = that._retainState;
        this._defaultContentType = that._defaultContentType;
    }
    
    public String getMimeType() {
        return _defaultContentType.getContentType();
    }
    
    public ContentType getStaticContentType(Packet packet) {
        return getContentType(packet.soapAction);
    }
    
    public ContentType encode(Packet packet, OutputStream out) {
        if (packet.getMessage() != null) {
            final XMLStreamWriter writer = getXMLStreamWriter(out);
            try {
                packet.getMessage().writeTo(writer);
                writer.flush();
            } catch (XMLStreamException e) {
                throw new WebServiceException(e);
            }
        }
        return getContentType(packet.soapAction);
    }
    
    public ContentType encode(Packet packet, WritableByteChannel buffer) {
        //TODO: not yet implemented
        throw new UnsupportedOperationException();
    }
    
    public void decode(InputStream in, String contentType, Packet response) throws IOException {
        response.setMessage(
                _soapCodec.decode(getXMLStreamReader(in)));
    }
    
    public void decode(ReadableByteChannel in, String contentType, Packet response) {
        throw new UnsupportedOperationException();
    }
    
    protected abstract StreamHeader createHeader(XMLStreamReader reader, XMLStreamBuffer mark);
    
    protected abstract ContentType getContentType(String soapAction);
    
    private XMLStreamWriter getXMLStreamWriter(OutputStream out) {
        if (_serializer != null) {
            _serializer.setOutputStream(out);
            return _serializer;
        } else {
            WSTCPCodecConfigurator configurator = WSTCPCodecConfigurator.INSTANCE;
            StAXDocumentSerializer serializer = configurator.getDocumentSerializerFactory().newInstance();
            serializer.setOutputStream(out);
            
            if (_retainState) {
                SerializerVocabulary vocabulary = configurator.getSerializerVocabularyFactory().newInstance();
                serializer.setVocabulary(vocabulary);
                serializer.setMinAttributeValueSize(
                        configurator.getMinAttributeValueSize());
                serializer.setMaxAttributeValueSize(
                        configurator.getMaxAttributeValueSize());
                serializer.setMinCharacterContentChunkSize(
                        configurator.getMinCharacterContentChunkSize());
                serializer.setMaxCharacterContentChunkSize(
                        configurator.getMaxCharacterContentChunkSize());
                serializer.setAttributeValueMapMemoryLimit(
                        configurator.getAttributeValueMapMemoryLimit());
                serializer.setCharacterContentChunkMapMemoryLimit(
                        configurator.getCharacterContentChunkMapMemoryLimit());
            }
            _serializer = serializer;
            return serializer;
        }
    }
    
    private XMLStreamReader getXMLStreamReader(InputStream in) {
        if (_statefulParser != null) {
            _statefulParser.setInputStream(in);
            return _statefulParser;
        } else {
            WSTCPCodecConfigurator configurator = WSTCPCodecConfigurator.INSTANCE;
            StAXDocumentParser parser = configurator.getDocumentParserFactory().newInstance();
            parser.setInputStream(in);
            if (parser instanceof WSTCPFastInfosetStreamReaderRecyclable) {
                ((WSTCPFastInfosetStreamReaderRecyclable) parser).
                        setListener(_readerRecycleListener);
            }
            
            parser.setStringInterning(true);
            if (_retainState) {
                ParserVocabulary vocabulary = configurator.
                        getParserVocabularyFactory().newInstance();
                parser.setVocabulary(vocabulary);
            }
            _statefulParser = parser;
            return _statefulParser;
        }
    }
    
    /**
     * Creates a new {@link FastInfosetStreamSOAPCodec} instance.
     *
     * @param version the SOAP version of the codec.
     * @return a new {@link WSTCPFastInfosetStreamCodec} instance.
     */
    public static WSTCPFastInfosetStreamCodec create(StreamSOAPCodec soapCodec, 
            SOAPVersion version, RecycleAwareListener readerRecycleListener, boolean retainState) {
        if(version==null)
            // this decoder is for SOAP, not for XML/HTTP
            throw new IllegalArgumentException();
        switch(version) {
            case SOAP_11:
                return new WSTCPFastInfosetStreamSOAP11Codec(soapCodec, readerRecycleListener, retainState);
            case SOAP_12:
                return new WSTCPFastInfosetStreamSOAP12Codec(soapCodec, readerRecycleListener, retainState);
            default:
                throw new AssertionError();
        }
    }
}
