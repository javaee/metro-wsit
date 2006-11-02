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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

/**
 * @author Alexey Stashok
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "channelSettings", propOrder = {
    "negotiatedMimeTypes",
    "negotiatedParams",
    "targetWSURI",
    "channelId",
    "wsServiceName"    
})
public class ChannelSettings {
    
    private List<MimeType> negotiatedMimeTypes;
    
    private List<String> negotiatedParams;
    
    private int channelId;
    
    private QName wsServiceName;
    
    @XmlJavaTypeAdapter(WSTCPURI.WSTCPURI2StringJAXBAdapter.class)
    private WSTCPURI targetWSURI;
    
    public ChannelSettings() {
    }

    public ChannelSettings(@NotNull List<MimeType> negotiatedMimeTypes, @NotNull List<String> negotiatedParams, 
            int channelId, QName wsServiceName, WSTCPURI targetWSURI) {
        this.negotiatedMimeTypes = negotiatedMimeTypes;
        this.negotiatedParams = negotiatedParams;
        this.channelId = channelId;
        this.wsServiceName = wsServiceName;
        this.targetWSURI = targetWSURI;
    }

    public @NotNull List<MimeType> getNegotiatedMimeTypes() {
        return negotiatedMimeTypes;
    }

    public void setNegotiatedMimeTypes(@NotNull List<MimeType> negotiatedMimeTypes) {
        this.negotiatedMimeTypes = negotiatedMimeTypes;
    }

    public @NotNull List<String> getNegotiatedParams() {
        return negotiatedParams;
    }

    public void setNegotiatedParams(@NotNull List<String> negotiatedParams) {
        this.negotiatedParams = negotiatedParams;
    }

    public @NotNull WSTCPURI getTargetWSURI() {
        return targetWSURI;
    }

    public void setTargetWSURI(@NotNull WSTCPURI targetWSURI) {
        this.targetWSURI = targetWSURI;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public @NotNull QName getWSServiceName() {
        return wsServiceName;
    }

    public void setWSServiceName(@NotNull QName wsServiceName) {
        this.wsServiceName = wsServiceName;
    }
    
    
}
