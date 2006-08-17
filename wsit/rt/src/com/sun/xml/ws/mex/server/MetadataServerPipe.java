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

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.addressing.ActionNotSupportedException;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;
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

import static com.sun.xml.ws.mex.MetadataConstants.GET_METADATA_REQUEST;
import static com.sun.xml.ws.mex.MetadataConstants.GET_METADATA_RESPONSE;
import static com.sun.xml.ws.mex.MetadataConstants.GET_REQUEST;
import static com.sun.xml.ws.mex.MetadataConstants.GET_RESPONSE;
import static com.sun.xml.ws.mex.MetadataConstants.MEX_NAMESPACE;
import static com.sun.xml.ws.mex.MetadataConstants.MEX_PREFIX;
import static com.sun.xml.ws.mex.MetadataConstants.SOAP_1_2;
import static com.sun.xml.ws.mex.MetadataConstants.SOAP_1_1;
import static com.sun.xml.ws.mex.MetadataConstants.WSA_MS_NAMESPACE;
import static com.sun.xml.ws.mex.MetadataConstants.WSA_W3C_NAMESPACE;
import static com.sun.xml.ws.mex.MetadataConstants.WSA_PREFIX;

/**
 * todo: error handling, i18n
 *
 * @author WS Development Team
 */
public class MetadataServerPipe extends AbstractFilterPipeImpl {

    private final WSDLRetriever wsdlRetriever;
    private final String soapNamespace;
    private final WsaRuntimeFactory wsaMSFactory;
    private final WsaRuntimeFactory wsaW3Factory;

    public MetadataServerPipe(WSEndpoint endpoint, Pipe next) {
        super(next);
        wsaMSFactory = WsaRuntimeFactory.newInstance(WSA_MS_NAMESPACE,
            endpoint.getPort(), endpoint.getBinding());
        wsaW3Factory = WsaRuntimeFactory.newInstance(WSA_W3C_NAMESPACE,
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
        wsaMSFactory = that.wsaMSFactory;
        wsaW3Factory = that.wsaW3Factory;
        wsdlRetriever = that.wsdlRetriever;
    }

    public Pipe copy(PipeCloner cloner) {
        return new MetadataServerPipe(this, cloner);
    }

    public Packet process(Packet request) {
        
        // return quickly if there are no headers to check
        if (request.getMessage() == null ||
            !request.getMessage().hasHeaders()) {
            
            return next.process(request);
        }
        
        AddressingProperties ap = wsaW3Factory.readHeaders(request);
        boolean useW3C = true;
        if (ap == null) {
            ap = wsaMSFactory.readHeaders(request);
            useW3C = false;
        }
        if (ap != null && ap.getAction() != null) {
            if (ap.getAction().toString().equals(GET_REQUEST)) {
                return processGetRequest(request, ap, GET_RESPONSE, useW3C);
            } else if (ap.getAction().toString().equals(GET_METADATA_REQUEST)) {
                return createANSFault(request, useW3C);
            }
        }
        return next.process(request);
    }

    private Packet processGetRequest(Packet request, AddressingProperties ap,
        String responseAction, boolean useW3C) {
        
        try {
            String address = ap.getTo().toString();
            MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
            XMLStreamWriter writer = buffer.createFromXMLStreamWriter();

            writeStartEnvelope(writer, useW3C);
            wsdlRetriever.addDocuments(writer, request, address);
            writer.writeEndDocument();
            writer.flush();

            Message responseMessage = Messages.create(buffer);
            Packet response = request.createResponse(responseMessage);
            createResponseHeaders(request, response,
                ap, responseAction, useW3C);
            return response;
        } catch (XMLStreamBufferException bufferE) {
            throw new WebServiceException(bufferE);
        } catch (XMLStreamException streamE) {
            throw new WebServiceException(streamE);
        }
    }

    private void writeStartEnvelope(XMLStreamWriter writer, boolean useW3C) 
        throws XMLStreamException {

        String soapPrefix = "soapenv";

        writer.writeStartDocument();
        writer.writeStartElement(soapPrefix, "Envelope", soapNamespace);

        // this line should go away after bug fix - 6418039
        writer.writeNamespace(soapPrefix, soapNamespace);

        if (useW3C) {
            writer.writeNamespace(WSA_PREFIX, WSA_W3C_NAMESPACE);
        } else {
            writer.writeNamespace(WSA_PREFIX, WSA_MS_NAMESPACE);
        }
        writer.writeNamespace(MEX_PREFIX, MEX_NAMESPACE);

        writer.writeStartElement(soapPrefix, "Body", soapNamespace);
        writer.writeStartElement(MEX_PREFIX, "Metadata", MEX_NAMESPACE);
    }

    private void createResponseHeaders(Packet request, Packet response,
        AddressingProperties requestProps, String action, boolean useW3C) {

        WsaRuntimeFactory wrf = wsaW3Factory;
        if (!useW3C) {
            wrf = wsaMSFactory;
        }
        AddressingProperties responseProps =
            wrf.toOutbound(requestProps, request);
        responseProps.setAction(action);
        wrf.writeHeaders(response, responseProps);
    }

    /*
     * Note: this method will be removed later and this pipe
     * will instead be able to throw a ws-a exception.
     */
    private Packet createANSFault(Packet request, boolean useW3C) {
        try {
            SOAPFactory factory;
            SOAPFault fault;
            String wsaNamespace = WSA_W3C_NAMESPACE;
            if (!useW3C) {
                wsaNamespace = WSA_MS_NAMESPACE;
            }
            QName wsaFaultCode = new QName(wsaNamespace, "ActionNotSupported");
            if (soapNamespace.equals(SOAP_1_1)) {
                factory = SOAPVersion.SOAP_11.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(wsaFaultCode);
            } else {
                factory = SOAPVersion.SOAP_12.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(JAXWSAConstants.SOAP12_SENDER_QNAME);
                fault.appendFaultSubcode(wsaFaultCode);
                fault.setFaultString("The " +
                    GET_METADATA_REQUEST +
                    " cannot be processed at the receiver");
            }
            Message faultMessage = Messages.create(fault);
            return request.createResponse(faultMessage);
        } catch (SOAPException se) {
            throw new WebServiceException(
                "Exception while trying to create fault message", se);
        }
    }
    
}
