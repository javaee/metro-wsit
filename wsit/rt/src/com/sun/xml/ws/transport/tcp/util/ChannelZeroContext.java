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
import java.util.Arrays;

/**
 * @author Alexey Stashok
 */
public class ChannelZeroContext extends ChannelContext {

    static final ChannelSettings channelZeroSettings = new ChannelSettings(
            Arrays.asList(MimeTypeConstants.SOAP11, MimeTypeConstants.FAST_INFOSET_SOAP11),
            Arrays.asList(TCPConstants.CHARSET_PROPERTY, TCPConstants.TRANSPORT_SOAP_ACTION_PROPERTY),
            0, TCPConstants.SERVICE_CHANNEL_WS_NAME, 
            WSTCPURI.parse(TCPConstants.PROTOCOL_SCHEMA + "://somehost:8080/service"));
    
    
    public ChannelZeroContext(@NotNull final ConnectionSession connectionSession) {
        super(connectionSession, channelZeroSettings);
    }
    
}
