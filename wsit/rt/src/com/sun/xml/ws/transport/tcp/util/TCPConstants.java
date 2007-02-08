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

import java.net.HttpURLConnection;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

/**
 * @author Alexey Stashok
 */
public final class TCPConstants {
    public static final String CHARSET_PROPERTY = "charset";
    public static final String CONTENT_TYPE_PROPERTY = "Content-Type";
    public static final String SOAP_ACTION_PROPERTY = "action";
    public static final String ERROR_CODE_PROPERTY = "ErrorCode";
    public static final String ERROR_DESCRIPTION_PROPERTY = "ErrorDescription";

    public static final int OK = 0;
    public static final int ONE_WAY = 202;
    public static final int ERROR = 400;
    
    public static final int RS_INTERNAL_SERVER_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    public static final int RS_INTERNAL_ERROR = HttpURLConnection.HTTP_INTERNAL_ERROR;
    public static final int RS_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;

    /** ByteBuffer settings for FramedBufferInputStream and FramedBufferOutputStream */
    public static final int DEFAULT_FRAME_SIZE = 4096;
    public static final boolean DEFAULT_USE_DIRECT_BUFFER = false;

    /** Name of property in MessageContext, which hold tcp connection context */
    public static final String CHANNEL_CONTEXT = "channelContext";
    public static final String TCP_SESSION = "tcpSession";

    /** Name of Service pipeline attribute in client's connection session */
    public static final String SERVICE_PIPELINE_ATTR_NAME = "ServicePipeline";
    
    /** Service Channel WS endpoint path */
    public static final String SERVICE_CHANNEL_URL_PATTERN = "/servicechannel";
    public static final String SERVICE_CHANNEL_CONTEXT_PATH = "/service";
    
    
    /** Number of tries client will use to send message */
    public static final int CLIENT_MAX_FAIL_TRIES = 5;
    
    /** Attribute names for ServiceWS invocation */
    public static final String ADAPTER_REGISTRY = "AdapterRegistry";
    
    /** SOAP/TCP protocol schema */
    public static final String PROTOCOL_SCHEMA = "vnd.sun.ws.tcp";
    
    /** SOAP/TCP logging domain root */
    public static final String LoggingDomain = "com.sun.xml.ws.transport.tcp";
    
    /** Lifecycle module paratemer names*/
    public static final String HIGH_WATER_MARK = "high-water-mark";
    public static final String NUMBER_TO_RECLAIM = "number-to-reclaim";

    /** Service Channel web service: Service and Port names*/
    public static final QName SERVICE_CHANNEL_WS_NAME = new QName("http://servicechannel.tcp.transport.ws.xml.sun.com/", "ServiceChannelWSImplService");
    public static final QName SERVICE_CHANNEL_WS_PORT_NAME = new QName("http://servicechannel.tcp.transport.ws.xml.sun.com/", "ServiceChannelWSImplPort");
}
