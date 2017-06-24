/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
