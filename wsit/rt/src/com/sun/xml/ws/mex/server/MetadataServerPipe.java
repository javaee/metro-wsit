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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;

import com.sun.xml.ws.addressing.model.ActionNotSupportedException;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterPipeImpl;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.SOAPVersion;

import static com.sun.xml.ws.mex.MetadataConstants.GET_METADATA_REQUEST;
import static com.sun.xml.ws.mex.MetadataConstants.GET_REQUEST;
import static com.sun.xml.ws.mex.MetadataConstants.MEX_NAMESPACE;
import static com.sun.xml.ws.mex.MetadataConstants.MEX_PREFIX;
import static com.sun.xml.ws.mex.MetadataConstants.WSA_MS_NAMESPACE;
import static com.sun.xml.ws.mex.MetadataConstants.WSA_W3C_NAMESPACE;
import static com.sun.xml.ws.mex.MetadataConstants.WSA_PREFIX;

/**
 * This pipe handles any mex requests that come through. If a
 * message comes through that has no headers or does not have
 * a mex action in the header, then the pipe ignores the message
 * and passes it on to the next pipe. Otherwise, it responds
 * to a mex Get request and returns a fault for a GetMetadata
 * request (these optional requests are not supported).
 *
 * TODO: Remove the createANSFault() method after the next
 * jax-ws integration. See the method for more details.
 *
 * @author WS Development Team
 */
public class MetadataServerPipe extends AbstractFilterPipeImpl {

    private final WSDLRetriever wsdlRetriever;
    private final SOAPVersion soapVersion;
    
    public MetadataServerPipe(WSEndpoint endpoint, Pipe next) {
        super(next);
        wsdlRetriever = new WSDLRetriever(endpoint);
        soapVersion = endpoint.getBinding().getSOAPVersion();
    }

    protected MetadataServerPipe(MetadataServerPipe that, PipeCloner cloner) {
        super(that, cloner);
        soapVersion = that.soapVersion;
        wsdlRetriever = that.wsdlRetriever;
    }

    public Pipe copy(PipeCloner cloner) {
        return new MetadataServerPipe(this, cloner);
    }

    /**
     * Method returns immediately if there are no headers
     * in the message to check.
     */
    public Packet process(Packet request) {
        if (request.getMessage()==null || !request.getMessage().hasHeaders()) {
            return next.process(request);
        }
        
        // try w3c version of ws-a first, then member submission version
        HeaderList headers = request.getMessage().getHeaders();
        String action = headers.getAction(AddressingVersion.W3C, soapVersion);
        String toAddress = headers.getTo(AddressingVersion.W3C, soapVersion);
        boolean useW3C = true;
        if (action == null) {
            action = headers.getAction(AddressingVersion.MEMBER, soapVersion);
            toAddress = headers.getTo(AddressingVersion.MEMBER, soapVersion);
            useW3C = false;
        }
        
        if (action != null) {
            if (action.equals(GET_REQUEST)) {
                return processGetRequest(request, toAddress, useW3C);
            } else if (action.equals(GET_METADATA_REQUEST)) {
                throw new ActionNotSupportedException(GET_METADATA_REQUEST);
            }
        }
        return next.process(request);
    }

    /*
     * This method creates an xml stream buffer, writes the response to
     * it, and uses it to create a response message.
     */
    private Packet processGetRequest(Packet req, String add, boolean useW3C) {
        try {
            MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
            XMLStreamWriter writer = buffer.createFromXMLStreamWriter();

            writeStartEnvelope(writer, useW3C);
            wsdlRetriever.addDocuments(writer, req, add);
            writer.writeEndDocument();
            writer.flush();

            Message responseMessage = Messages.create(buffer);
            Packet response = req.createResponse(responseMessage);
            createResponseHeaders(req, response, useW3C);
            return response;
        } catch (XMLStreamException streamE) {
            throw new WebServiceException(streamE);
        }
    }

    private void writeStartEnvelope(XMLStreamWriter writer, boolean useW3C) 
        throws XMLStreamException {

        String soapPrefix = "soapenv";

        writer.writeStartDocument();
        writer.writeStartElement(soapPrefix, "Envelope", soapVersion.nsUri);

        // todo: this line should go away after bug fix - 6418039
        writer.writeNamespace(soapPrefix, soapVersion.nsUri);

        if (useW3C) {
            writer.writeNamespace(WSA_PREFIX, WSA_W3C_NAMESPACE);
        } else {
            writer.writeNamespace(WSA_PREFIX, WSA_MS_NAMESPACE);
        }
        writer.writeNamespace(MEX_PREFIX, MEX_NAMESPACE);

        writer.writeStartElement(soapPrefix, "Body", soapVersion.nsUri);
        writer.writeStartElement(MEX_PREFIX, "Metadata", MEX_NAMESPACE);
    }

    /*
     * This method delegates WS-A header duties to the WS-A code
     * for creating response headers based on the request headers.
     * It also adds the mex-specific response action header.
     */
    private void createResponseHeaders(Packet request, Packet response,
        boolean useW3C) {

//        WsaRuntimeFactory wrf = wsaW3Factory;
//        if (!useW3C) {
//            wrf = wsaMSFactory;
//        }
//        AddressingProperties responseProps =
//            wrf.toOutbound(requestProps, request);
//        responseProps.setAction(GET_RESPONSE);
//        wrf.writeHeaders(response, responseProps);
    }

    /*
     * Note: this method will be removed later and this pipe
     * will instead be able to throw a ws-a exception. The exception
     * will be caught by the ws-a pipe to create the necessary
     * fault.
     */
//    private Packet createANSFault(Packet request, boolean useW3C) {
//        try {
//            SOAPFactory factory;
//            SOAPFault fault;
//            String wsaNamespace = WSA_W3C_NAMESPACE;
//            if (!useW3C) {
//                wsaNamespace = WSA_MS_NAMESPACE;
//            }
//            QName wsaFaultCode = new QName(wsaNamespace, "ActionNotSupported");
//            if (soapNamespace.equals(SOAP_1_1)) {
//                factory = SOAPVersion.SOAP_11.saajSoapFactory;
//                fault = factory.createFault();
//                fault.setFaultCode(wsaFaultCode);
//            } else {
//                factory = SOAPVersion.SOAP_12.saajSoapFactory;
//                fault = factory.createFault();
//                fault.setFaultCode(JAXWSAConstants.SOAP12_SENDER_QNAME);
//                fault.appendFaultSubcode(wsaFaultCode);
//                fault.setFaultString("The " +
//                    GET_METADATA_REQUEST +
//                    " cannot be processed at the receiver");
//            }
//            Message faultMessage = Messages.create(fault);
//            return request.createResponse(faultMessage);
//        } catch (SOAPException se) {
//            throw new WebServiceException(
//                "Exception while trying to create fault message", se);
//        }
//    }
    
}
