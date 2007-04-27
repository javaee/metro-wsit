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
import java.util.List;
import javax.xml.namespace.QName;

/**
 * @author Alexey Stashok
 */
public final class ChannelSettings {
    
    private List<String> negotiatedMimeTypes;
    
    private List<String> negotiatedParams;
    
    private int channelId;
    
    private QName wsServiceName;
    
    private WSTCPURI targetWSURI;
    
    public ChannelSettings() {
    }

    public ChannelSettings(@NotNull final List<String> negotiatedMimeTypes, 
            @NotNull final List<String> negotiatedParams, 
            final int channelId, 
            final QName wsServiceName, 
            final WSTCPURI targetWSURI) {
        this.negotiatedMimeTypes = negotiatedMimeTypes;
        this.negotiatedParams = negotiatedParams;
        this.channelId = channelId;
        this.wsServiceName = wsServiceName;
        this.targetWSURI = targetWSURI;
    }

    public @NotNull List<String> getNegotiatedMimeTypes() {
        return negotiatedMimeTypes;
    }

    public void setNegotiatedMimeTypes(@NotNull final List<String> negotiatedMimeTypes) {
        this.negotiatedMimeTypes = negotiatedMimeTypes;
    }

    public @NotNull List<String> getNegotiatedParams() {
        return negotiatedParams;
    }

    public void setNegotiatedParams(@NotNull final List<String> negotiatedParams) {
        this.negotiatedParams = negotiatedParams;
    }

    public @NotNull WSTCPURI getTargetWSURI() {
        return targetWSURI;
    }

    public void setTargetWSURI(@NotNull final WSTCPURI targetWSURI) {
        this.targetWSURI = targetWSURI;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(final int channelId) {
        this.channelId = channelId;
    }

    public @NotNull QName getWSServiceName() {
        return wsServiceName;
    }

    public void setWSServiceName(@NotNull final QName wsServiceName) {
        this.wsServiceName = wsServiceName;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(200);
        sb.append("TargetURI: ");
        sb.append(targetWSURI);
        sb.append(" wsServiceName: ");
        sb.append(wsServiceName);
        sb.append(" channelId: ");
        sb.append(channelId);
        sb.append(" negotiatedParams: ");
        sb.append(negotiatedParams);
        sb.append(" negotiatedMimeTypes: ");
        sb.append(negotiatedMimeTypes);
                
        return sb.toString();
    }
}
