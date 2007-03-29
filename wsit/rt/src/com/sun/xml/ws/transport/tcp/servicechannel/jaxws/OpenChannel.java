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

package com.sun.xml.ws.transport.tcp.servicechannel.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "openChannel", namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "openChannel", namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/")
public class OpenChannel {

    @XmlElement(name = "channelSettings", namespace = "", required = true)
    private com.sun.xml.ws.transport.tcp.util.ChannelSettings channelSettings;

    /**
     * 
     * @return
     *     returns ChannelSettings
     */
    public com.sun.xml.ws.transport.tcp.util.ChannelSettings getChannelSettings() {
        return this.channelSettings;
    }

    /**
     * 
     * @param channelSettings
     *     the value for the channelSettings property
     */
    public void setChannelSettings(com.sun.xml.ws.transport.tcp.util.ChannelSettings channelSettings) {
        this.channelSettings = channelSettings;
    }

}
