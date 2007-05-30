package com.sun.xml.ws.mex.server;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSWebServiceContext;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.transport.http.servlet.ServletModule;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.Addressing;
import com.sun.xml.ws.mex.MetadataConstants;
import javax.servlet.http.HttpServletRequest;
import com.sun.xml.ws.mex.MessagesMessages;

import static com.sun.xml.ws.mex.MetadataConstants.GET_MDATA_REQUEST;
import static com.sun.xml.ws.mex.MetadataConstants.GET_REQUEST;
import static com.sun.xml.ws.mex.MetadataConstants.GET_RESPONSE;
import static com.sun.xml.ws.mex.MetadataConstants.MEX_NAMESPACE;
import static com.sun.xml.ws.mex.MetadataConstants.MEX_PREFIX;
import static com.sun.xml.ws.mex.MetadataConstants.WSA_PREFIX;


@ServiceMode(value=Service.Mode.MESSAGE)
@WebServiceProvider
@Addressing(enabled=true,required=true)
public class MEXEndpoint implements Provider<Message> {

    @Resource
    protected WebServiceContext wsContext;
    
    private static final Logger logger =
        Logger.getLogger(MEXEndpoint.class.getName());

    public Message invoke(Message requestMsg) {
        if (requestMsg == null || !requestMsg.hasHeaders()) {
            // TODO: Better error message
            throw new WebServiceException("Malformed MEX Request");
        }

        WSEndpoint wsEndpoint = (WSEndpoint) wsContext.getMessageContext().get(JAXWSProperties.WSENDPOINT);
        SOAPVersion soapVersion = wsEndpoint.getBinding().getSOAPVersion();

        // try w3c version of ws-a first, then member submission version
        final HeaderList headers = requestMsg.getHeaders();

        String action = headers.getAction(AddressingVersion.W3C, soapVersion);
        AddressingVersion wsaVersion = AddressingVersion.W3C;
        if (action == null) {
            action = headers.getAction(AddressingVersion.MEMBER, soapVersion);
            wsaVersion = AddressingVersion.MEMBER;
        }

        if (action == null) {
            // TODO: Better error message
            throw new WebServiceException("No wsa:Action specified");
        }
        else if (action.equals(GET_REQUEST)) {
            final String toAddress = headers.getTo(wsaVersion, soapVersion);
            return processGetRequest(requestMsg, toAddress, wsaVersion, soapVersion);
        }
        else if (action.equals(GET_MDATA_REQUEST)) {
            final Message faultMessage = Messages.create(GET_MDATA_REQUEST,
                wsaVersion, soapVersion);
            wsContext.getMessageContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, wsaVersion.getDefaultFaultAction());
            return faultMessage;
        }
        // If here, either action is unsupported 
        // TODO: Better error message
        throw new UnsupportedOperationException(action);
    }

    /*
     * This method creates an xml stream buffer, writes the response to
     * it, and uses it to create a response message.
     */
    private Message processGetRequest(final Message request,
        final String address, final AddressingVersion wsaVersion,
        final SOAPVersion soapVersion) {
        
        try {
            final MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
            final XMLStreamWriter writer = buffer.createFromXMLStreamWriter();

            WSEndpoint wsEndpoint = (WSEndpoint) wsContext.getMessageContext().get(JAXWSProperties.WSENDPOINT);
            HttpServletRequest servletRequest = (HttpServletRequest)wsContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
            if (servletRequest == null) {
                // TODO: better error message
                throw new WebServiceException("MEX: no ServletRequest can be found");
            }
            
            // Derive the address of the owner endpoint.
            // e.g. http://localhost/foo/mex --> http://localhost/foo
            WSEndpoint ownerEndpoint = null;
            ServletModule module = (ServletModule) wsEndpoint.getContainer().getSPI(ServletModule.class);
            String baseAddress = module.getContextPath(servletRequest);
            String ownerEndpointAddress = null;
            List<BoundEndpoint> boundEndpoints = module.getBoundEndpoints();
            for (BoundEndpoint endpoint : boundEndpoints) {
                if (wsEndpoint == endpoint.getEndpoint()) {
                    ownerEndpointAddress = endpoint.getAddress(baseAddress).toString();                
                    break;
                }
            }
            ownerEndpointAddress = ownerEndpointAddress.substring(0,ownerEndpointAddress.length() - "/mex".length());

            boundEndpoints = module.getBoundEndpoints();
            for (BoundEndpoint endpoint : boundEndpoints) {
                //compare ownerEndpointAddress with this endpoints address
                //   if matches, set ownerEndpoint to the corresponding WSEndpoint
                String endpointAddress = endpoint.getAddress(baseAddress).toString();
                if (endpointAddress.equals(ownerEndpointAddress)) {
                    ownerEndpoint = endpoint.getEndpoint();
                    break;
                }
            }
            

            // If the owner endpoint has been found, then
            // get its metadata and write it to the response message
            if (ownerEndpoint != null) {
                writeStartEnvelope(writer, wsaVersion, soapVersion);
                WSDLRetriever wsdlRetriever = new WSDLRetriever(ownerEndpoint);
                wsdlRetriever.addDocuments(writer, null, address);
                writer.writeEndDocument();
                writer.flush();
                final Message responseMessage = Messages.create(buffer);
                wsContext.getMessageContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, GET_RESPONSE);
                return responseMessage;
            }

            // If we get here there was no metadata for the owner endpoint
            // TODO: This should probably be something other than unsupported action for clarity
            final Message faultMessage = Messages.create(GET_REQUEST,
                wsaVersion, soapVersion);
            wsContext.getMessageContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, wsaVersion.getDefaultFaultAction());
            return faultMessage;
        } catch (XMLStreamException streamE) {
            final String exceptionMessage = 
               MessagesMessages.MEX_0001_RESPONSE_WRITING_FAILURE(address);
            logger.log(Level.SEVERE, exceptionMessage, streamE);
            throw new WebServiceException(exceptionMessage, streamE);
        }
    }

    private void writeStartEnvelope(final XMLStreamWriter writer,
        final AddressingVersion wsaVersion, final SOAPVersion soapVersion)
        throws XMLStreamException {

        final String soapPrefix = "soapenv";

        writer.writeStartDocument();
        writer.writeStartElement(soapPrefix, "Envelope", soapVersion.nsUri);

        // todo: this line should go away after bug fix - 6418039
        writer.writeNamespace(soapPrefix, soapVersion.nsUri);

        writer.writeNamespace(MetadataConstants.WSA_PREFIX, wsaVersion.nsUri);
        writer.writeNamespace(MetadataConstants.MEX_PREFIX, MetadataConstants.MEX_NAMESPACE);

        writer.writeStartElement(soapPrefix, "Body", soapVersion.nsUri);
        writer.writeStartElement(MetadataConstants.MEX_PREFIX, "Metadata", MetadataConstants.MEX_NAMESPACE);
    }
    
}

