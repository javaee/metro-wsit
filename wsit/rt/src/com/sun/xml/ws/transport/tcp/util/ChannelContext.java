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

package com.sun.xml.ws.transport.tcp.util;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.transport.tcp.encoding.WSTCPFastInfosetStreamCodec;
import com.sun.xml.ws.transport.tcp.encoding.WSTCPFastInfosetStreamReaderRecyclable;
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;


public final class ChannelContext implements WSTCPFastInfosetStreamReaderRecyclable.RecycleAwareListener {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain);
    
    private static final Map<String, Integer> staticParamsEncodingMap = new HashMap<String, Integer>(4);
    private static final Map<Integer, String> staticParamsDecodingMap = new HashMap<Integer, String> (4);
    
    private static final Map<MimeType, Integer> staticMimeTypeEncodingMap = new HashMap<MimeType, Integer>(4);
    private static final Map<Integer, MimeType> staticMimeTypeDecodingMap = new HashMap<Integer, MimeType>(4);
    static {
        staticParamsEncodingMap.put(TCPConstants.CHARSET_PROPERTY, 0);
        staticParamsEncodingMap.put(TCPConstants.SOAP_ACTION_PROPERTY, 1);
        staticParamsEncodingMap.put(TCPConstants.ERROR_CODE_PROPERTY, 2);
        staticParamsEncodingMap.put(TCPConstants.ERROR_DESCRIPTION_PROPERTY, 3);
        
        staticMimeTypeEncodingMap.put(MimeType.SOAP11, 0);   //default mime type for soap1.1
        staticMimeTypeEncodingMap.put(MimeType.SOAP12, 1);   //default mime type for soap1.2
        staticMimeTypeEncodingMap.put(MimeType.FAST_INFOSET_SOAP11, 3);   //default mime type for stateless FI 1.1
        staticMimeTypeEncodingMap.put(MimeType.FAST_INFOSET_SOAP12, 4);   //default mime type for stateless FI 1.2
        staticMimeTypeEncodingMap.put(MimeType.ERROR, 5);    //mime type for error messages
        
        for(Map.Entry<MimeType, Integer> entry : staticMimeTypeEncodingMap.entrySet()) staticMimeTypeDecodingMap.put(entry.getValue(), entry.getKey());
        for(Map.Entry<String, Integer> entry : staticParamsEncodingMap.entrySet()) staticParamsDecodingMap.put(entry.getValue(), entry.getKey());
    }
    
    // tcp connection session this channel belongs to
    private final ConnectionSession connectionSession;
    
    /**
     * Channel settings aggreed during client-service handshaking
     */
    private final ChannelSettings channelSettings;
    
    /**
     * Codec used to encode/decode messages on this channel
     */
    private Codec codec;
    
    // Temp storage for decode content type from String representation
    private final ContentType contentType = new ContentType();
    
    public ChannelContext(@NotNull final ConnectionSession connectionSession,
            @NotNull final ChannelSettings channelSettings) {
        this.connectionSession = connectionSession;
        this.channelSettings = channelSettings;
    }
    
    /**
     * Return TCP session object where which this virual channel is open on
     */
    public @NotNull ConnectionSession getConnectionSession() {
        return connectionSession;
    }
    
    /**
     * Return channel settings, which were aggreed during handshake phase
     */
    public @NotNull ChannelSettings getChannelSettings() {
        return channelSettings;
    }
    
    /**
     * Return message Codec, which is used for encoding/decoding messages
     * on this virtual channel
     */
    public @Nullable Codec getCodec() {
        return codec;
    }
    
    private void setCodec(@NotNull final Codec codec) {
        this.codec = codec;
    }
    
    /**
     * Return TCP connection object, where this virtual channel is acting on
     */
    public @NotNull Connection getConnection() {
        return connectionSession.getConnection();
    }
    
    /**
     * Return channel id
     */
    public int getChannelId() {
        return channelSettings.getChannelId();
    }
    
    /**
     * Return virtual channel's correspondent service name
     */
    public @NotNull QName getWSServiceName() {
        return channelSettings.getWSServiceName();
    }
    
    public void setWSServiceName(@NotNull final QName wsServiceName) {
        channelSettings.setWSServiceName(wsServiceName);
    }
    
    /**
     * Return correspondent WS's URI
     */
    public @Nullable WSTCPURI getTargetWSURI() {
        return channelSettings.getTargetWSURI();
    }
    
    /**
     * Sets message's content type to TCP protocol specific representation
     */
    public void setContentType(@NotNull final String contentTypeS) {
        Connection connection = connectionSession.getConnection();
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, MessagesMessages.WSTCP_1120_CHANNEL_CONTEXT_ENCODE_CT(contentTypeS));
        }
        contentType.parse(contentTypeS);
        
        Integer mt = staticMimeTypeEncodingMap.get(contentType.getMimeType());
        if (mt == null) {
            mt = channelSettings.getNegotiatedMimeTypes().indexOf(contentType.getMimeType());
            if (mt != -1)
                mt += staticMimeTypeEncodingMap.size();
        }
        
        if (mt == null || mt == -1)  throw new IllegalStateException(MessagesMessages.WSTCP_0011_UNKNOWN_CONTENT_TYPE(contentTypeS));
        
        connection.setContentId(mt);
        final Map<String, String> parameters = contentType.getParameters();
        for(Map.Entry<String, String> parameter : parameters.entrySet()) {
            final int paramId = encodeParam(parameter.getKey());
            connection.setContentProperty(paramId, parameter.getValue());
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, MessagesMessages.WSTCP_1121_CHANNEL_CONTEXT_ENCODED_CT(mt, parameters));
        }
    }
    
    /**
     * Gets message's content type from TCP protocol specific representation
     */
    public @NotNull String getContentType() {
        Connection connection = connectionSession.getConnection();
        final int mimeId = connection.getContentId();
        Map<Integer, String> params = connection.getContentProperties();
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, MessagesMessages.WSTCP_1122_CHANNEL_CONTEXT_DECODE_CT(mimeId, params));
        }        
        MimeType mimeType = staticMimeTypeDecodingMap.get(mimeId);
        if (mimeType == null) {
            mimeType = channelSettings.getNegotiatedMimeTypes().get(mimeId - staticMimeTypeDecodingMap.size());
        }
        
        if (mimeType == null) throw new IllegalStateException(MessagesMessages.WSTCP_0011_UNKNOWN_CONTENT_TYPE(mimeId));
        
        String contentTypeStr = mimeType.toString();
        if (params.size() > 0) {
            final StringBuffer ctBuf = new StringBuffer(contentTypeStr);
            for(Map.Entry<Integer, String> parameter : params.entrySet()) {
                ctBuf.append(';');
                final String paramKey = decodeParam(parameter.getKey());
                final String paramValue = parameter.getValue();
                ctBuf.append(paramKey);
                ctBuf.append('=');
                ctBuf.append(paramValue);
            }
            contentTypeStr = ctBuf.toString();
        }
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, MessagesMessages.WSTCP_1123_CHANNEL_CONTEXT_DECODED_CT(contentTypeStr));
        }
        return contentTypeStr;
    }
    
    private int encodeParam(@NotNull final String paramStr) {
        Integer paramId = staticParamsEncodingMap.get(paramStr);
        if (paramId == null) {
            paramId = channelSettings.getNegotiatedParams().indexOf(paramStr);
            if (paramId != -1)
                return paramId + staticParamsEncodingMap.size();
        } else {
            return paramId;
        }
        
        throw new IllegalStateException(MessagesMessages.WSTCP_0010_UNKNOWN_PARAMETER(paramStr));
    }
    
    private @NotNull String decodeParam(final int paramId) {
        String paramStr = staticParamsDecodingMap.get(paramId);
        if (paramStr == null) {
            paramStr = channelSettings.getNegotiatedParams().get(paramId - staticParamsDecodingMap.size());
            if (paramStr != null) {
                return paramStr;
            }
        } else {
            return paramStr;
        }
        
        throw new IllegalStateException(MessagesMessages.WSTCP_0010_UNKNOWN_PARAMETER(paramId));
    }
    
    /**
     * Configure Codec according to channel settings
     */
    public static void configureCodec(@NotNull final ChannelContext channelContext,
            @NotNull final SOAPVersion soapVersion,
    @NotNull final Codec defaultCodec) {
        final List<MimeType> supportedMimeTypes = channelContext.getChannelSettings().getNegotiatedMimeTypes();
        if (supportedMimeTypes != null) {
            if (supportedMimeTypes.contains(MimeType.FAST_INFOSET_STATEFUL_SOAP11) ||
                    supportedMimeTypes.contains(MimeType.FAST_INFOSET_STATEFUL_SOAP12)) {
                logger.log(Level.FINEST, "ChannelContext.configureCodec: FI Stateful");
                channelContext.setCodec(WSTCPFastInfosetStreamCodec.create(soapVersion, channelContext, true));
                return;
            } else if (supportedMimeTypes.contains(MimeType.FAST_INFOSET_SOAP11) ||
                    supportedMimeTypes.contains(MimeType.FAST_INFOSET_SOAP12)) {
                logger.log(Level.FINEST, "ChannelContext.configureCodec: FI Stateless");
                channelContext.setCodec(WSTCPFastInfosetStreamCodec.create(soapVersion, channelContext, false));
                return;
            }
        }
        
        logger.log(Level.FINEST, "ChannelContext.configureCodec: default");
        channelContext.setCodec(defaultCodec);
    }
    
    @Override
    public String toString() {
        return String.format("ID: %d\nURI: %s\nCodec:%s", new Object[] {getChannelId(), getTargetWSURI(), getCodec()});
    }
    
    public void onRecycled() {
        connectionSession.onReadCompleted();
    }
}