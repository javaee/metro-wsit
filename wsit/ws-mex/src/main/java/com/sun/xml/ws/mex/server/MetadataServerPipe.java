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

package com.sun.xml.ws.mex.server;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.PipeAdapter;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.mex.MessagesMessages;

import static com.sun.xml.ws.mex.MetadataConstants.GET_MDATA_REQUEST;
import static com.sun.xml.ws.mex.MetadataConstants.GET_REQUEST;
import static com.sun.xml.ws.mex.MetadataConstants.GET_RESPONSE;
import static com.sun.xml.ws.mex.MetadataConstants.MEX_NAMESPACE;
import static com.sun.xml.ws.mex.MetadataConstants.MEX_PREFIX;
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
public class MetadataServerPipe extends AbstractFilterTubeImpl {

    private final WSDLRetriever wsdlRetriever;
    private final SOAPVersion soapVersion;
    
    private static final Logger logger =
        Logger.getLogger(MetadataServerPipe.class.getName());
    
    public MetadataServerPipe(WSEndpoint endpoint, Pipe next) {
        super(PipeAdapter.adapt(next));
        wsdlRetriever = new WSDLRetriever(endpoint);
        soapVersion = endpoint.getBinding().getSOAPVersion();
    }

    protected MetadataServerPipe(MetadataServerPipe that, TubeCloner cloner) {
        super(that, cloner);
        soapVersion = that.soapVersion;
        wsdlRetriever = that.wsdlRetriever;
    }

    public MetadataServerPipe copy(TubeCloner cloner) {
        return new MetadataServerPipe(this, cloner);
    }

    /**
     * Method returns immediately if there are no headers
     * in the message to check. If there are, the pipe checks
     * W3C and then MEMBER addressing for an action header.
     * If there is an action header, and if it is a mex Get
     * request, then ask addressing again for the address and
     * process the request.
     */
    @Override
    public NextAction processRequest(final Packet request) {
        if (request.getMessage()==null || !request.getMessage().hasHeaders()) {
            return super.processRequest(request);
        }
        
        // try w3c version of ws-a first, then member submission version
        final HeaderList headers = request.getMessage().getHeaders();
        String action = headers.getAction(AddressingVersion.W3C, soapVersion);
        AddressingVersion adVersion = AddressingVersion.W3C;
        if (action == null) {
            action = headers.getAction(AddressingVersion.MEMBER, soapVersion);
            adVersion = AddressingVersion.MEMBER;
        }
        
        if (action != null) {
            if (action.equals(GET_REQUEST)) {
                final String toAddress = headers.getTo(adVersion, soapVersion);
                return doReturnWith(processGetRequest(request, toAddress, adVersion));
            } else if (action.equals(GET_MDATA_REQUEST)) {
                final Message faultMessage = Messages.create(GET_MDATA_REQUEST,
                    adVersion, soapVersion);
                return doReturnWith(request.createServerResponse(
                    faultMessage, adVersion, soapVersion,
                    adVersion.getDefaultFaultAction()));
            }
        }
        return super.processRequest(request);
    }

    /*
     * This method creates an xml stream buffer, writes the response to
     * it, and uses it to create a response message.
     */
    private Packet processGetRequest(final Packet request,
        final String address, final AddressingVersion adVersion) {
        
        try {
            final MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
            final XMLStreamWriter writer = buffer.createFromXMLStreamWriter();

            writeStartEnvelope(writer, adVersion);
            wsdlRetriever.addDocuments(writer, request, address);
            writer.writeEndDocument();
            writer.flush();

            final Message responseMessage = Messages.create(buffer);
            final Packet response = request.createServerResponse(
                responseMessage, adVersion, soapVersion, GET_RESPONSE);
            return response;
        } catch (XMLStreamException streamE) {
            final String exceptionMessage =
                MessagesMessages.MEX_0001_RESPONSE_WRITING_FAILURE(address);
            logger.log(Level.SEVERE, exceptionMessage, streamE);
            throw new WebServiceException(exceptionMessage, streamE);
        }
    }

    private void writeStartEnvelope(final XMLStreamWriter writer,
        final AddressingVersion adVersion) throws XMLStreamException {

        final String soapPrefix = "soapenv";

        writer.writeStartDocument();
        writer.writeStartElement(soapPrefix, "Envelope", soapVersion.nsUri);

        // todo: this line should go away after bug fix - 6418039
        writer.writeNamespace(soapPrefix, soapVersion.nsUri);

        writer.writeNamespace(WSA_PREFIX, adVersion.nsUri);
        writer.writeNamespace(MEX_PREFIX, MEX_NAMESPACE);

        writer.writeStartElement(soapPrefix, "Body", soapVersion.nsUri);
        writer.writeStartElement(MEX_PREFIX, "Metadata", MEX_NAMESPACE);
    }
    
}
