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
package com.sun.xml.ws.mex.server;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.WebServiceException;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;

import com.sun.xml.ws.addressing.spi.WsaRuntimeFactory;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterPipeImpl;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.SOAPVersion;

import static com.sun.xml.ws.mex.MetadataConstants.GET_WXF_REQUEST;
import static com.sun.xml.ws.mex.MetadataConstants.GET_WXF_RESPONSE;
import static com.sun.xml.ws.mex.MetadataConstants.MEX_NAMESPACE;
import static com.sun.xml.ws.mex.MetadataConstants.MEX_PREFIX;
import static com.sun.xml.ws.mex.MetadataConstants.SOAP_1_2;
import static com.sun.xml.ws.mex.MetadataConstants.SOAP_1_1;
import static com.sun.xml.ws.mex.MetadataConstants.WXF_NAMESPACE;
import static com.sun.xml.ws.mex.MetadataConstants.WXF_PREFIX;

/**
 * todo: error handling, i18n
 *
 * @author WS Development Team
 */
public class MetadataServerPipe extends AbstractFilterPipeImpl {

    private final WSDLRetriever wsdlRetriever;
    private final String soapNamespace;
    private final WsaRuntimeFactory wsaRtFac;

    public MetadataServerPipe(WSEndpoint endpoint, Pipe next) {
        super(next);
        wsaRtFac = WsaRuntimeFactory.newInstance(
            endpoint.getPort(), endpoint.getBinding());

        wsdlRetriever = new WSDLRetriever(endpoint);

        // todo: get soap envelope namespace directly?
        // soap version has binding id, not namespace
        SOAPVersion version = endpoint.getBinding().getSOAPVersion();
        if (version == SOAPVersion.SOAP_11) {
            soapNamespace = SOAP_1_1;
        } else {
            soapNamespace = SOAP_1_2;
        }
    }

    protected MetadataServerPipe(MetadataServerPipe that, PipeCloner cloner) {
        super(that, cloner);
        soapNamespace = that.soapNamespace;
        wsaRtFac = that.wsaRtFac;
        wsdlRetriever = that.wsdlRetriever;
    }

    public Pipe copy(PipeCloner cloner) {
        return new MetadataServerPipe(this, cloner);
    }

    public Packet process(Packet request) {
        AddressingProperties ap = wsaRtFac.readHeaders(request);
        if (ap != null &&
            ap.getAction().toString().equals(GET_WXF_REQUEST)) {
            return processRequest(request, ap);
        }
        return next.process(request);
    }

    public void preDestroy() {}

    private Packet processRequest(Packet request, AddressingProperties ap) {
        try {
            String address = ap.getTo().toString();
            MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
            XMLStreamWriter writer = buffer.createFromXMLStreamWriter();

            writeStartEnvelope(writer);
            wsdlRetriever.addDocuments(writer, request, address);
            writer.writeEndDocument();
            writer.flush();

            Message responseMessage = Messages.create(buffer);
            Packet response = request.createResponse(responseMessage);
            createResponseHeaders(request, response, ap);
            return response;
        } catch (XMLStreamBufferException bufferE) {
            throw new WebServiceException(bufferE);
        } catch (XMLStreamException streamE) {
            throw new WebServiceException(streamE);
        } catch (IOException ioe) {
            throw new WebServiceException(ioe);
        }
    }

    private void writeStartEnvelope(XMLStreamWriter writer) 
        throws XMLStreamException {

        String soapPrefix = "soapenv";

        writer.writeStartDocument();
        writer.writeStartElement(soapPrefix, "Envelope", soapNamespace);

        // this line should go away after bug fix - 6418039
        writer.writeNamespace(soapPrefix, soapNamespace);

        // todo: consistent namespace
        writer.writeNamespace("wsa",
            "http://schemas.xmlsoap.org/ws/2004/08/addressing");
        writer.writeNamespace(MEX_PREFIX, MEX_NAMESPACE);
        writer.writeNamespace(WXF_PREFIX, WXF_NAMESPACE);

        writer.writeStartElement(soapPrefix, "Body", soapNamespace);
        writer.writeStartElement(MEX_PREFIX, "Metadata", MEX_NAMESPACE);
    }

    private void createResponseHeaders(Packet request, Packet response,
        AddressingProperties requestProps) {

        // running into element prefix not bound problem again
//        AddressingProperties responseProps =
//            wsaRtFac.toOutbound(requestProps, request);
//        responseProps.setAction(GET_WXF_RESPONSE);
//        wsaRtFac.writeHeaders(response, responseProps);
    }

}
