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


package com.sun.xml.ws.transport.tcp.encoding;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.encoding.ContentTypeImpl;
import com.sun.xml.ws.message.stream.StreamHeader;
import com.sun.xml.ws.message.stream.StreamHeader11;
import com.sun.xml.ws.transport.tcp.encoding.WSTCPFastInfosetStreamReaderRecyclable.RecycleAwareListener;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.encoding.fastinfoset.FastInfosetMIMETypes;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Alexey Stashok
 */
public class WSTCPFastInfosetStreamSOAP11Codec extends WSTCPFastInfosetStreamCodec{
    /*package*/ WSTCPFastInfosetStreamSOAP11Codec(StreamSOAPCodec soapCodec, 
            RecycleAwareListener readerRecycleListener, boolean retainState) {
        super(soapCodec, SOAPVersion.SOAP_11, readerRecycleListener, retainState, 
                (retainState) ? FastInfosetMIMETypes.STATEFUL_SOAP_11 : FastInfosetMIMETypes.SOAP_11);
    }

    private WSTCPFastInfosetStreamSOAP11Codec(WSTCPFastInfosetStreamSOAP11Codec that) {
        super(that);
    }

    public Codec copy() {
        return new WSTCPFastInfosetStreamSOAP11Codec(this);
    }

    protected final StreamHeader createHeader(XMLStreamReader reader, XMLStreamBuffer mark) {
        return new StreamHeader11(reader, mark);
    }
    
    protected ContentType getContentType(String soapAction) {
        if (soapAction == null || soapAction.length() == 0) {
            return _defaultContentType;
        } else {
            return new ContentTypeImpl(_defaultContentType.getContentType(), soapAction);
        }
    }
}
