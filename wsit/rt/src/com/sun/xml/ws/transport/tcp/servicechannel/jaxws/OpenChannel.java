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

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "openChannel", namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "openChannel", namespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/", propOrder = {
    "targetWSURI",
    "negotiatedMimeTypes",
    "negotiatedParams"
})
public class OpenChannel {

    @XmlElement(name = "targetWSURI", namespace = "", required=true)
    private String targetWSURI;
    @XmlElement(name = "negotiatedMimeTypes", namespace = "", required=true)
    private List<String> negotiatedMimeTypes;
    @XmlElement(name = "negotiatedParams", namespace = "")
    private List<String> negotiatedParams;

    /**
     * 
     * @return
     *     returns String
     */
    public String getTargetWSURI() {
        return this.targetWSURI;
    }

    /**
     * 
     * @param targetWSURI
     *     the value for the targetWSURI property
     */
    public void setTargetWSURI(String targetWSURI) {
        this.targetWSURI = targetWSURI;
    }

    /**
     * 
     * @return
     *     returns List<String>
     */
    public List<String> getNegotiatedMimeTypes() {
        return this.negotiatedMimeTypes;
    }

    /**
     * 
     * @param negotiatedMimeTypes
     *     the value for the negotiatedMimeTypes property
     */
    public void setNegotiatedMimeTypes(List<String> negotiatedMimeTypes) {
        this.negotiatedMimeTypes = negotiatedMimeTypes;
    }

    /**
     * 
     * @return
     *     returns List<String>
     */
    public List<String> getNegotiatedParams() {
        return this.negotiatedParams;
    }

    /**
     * 
     * @param negotiatedParams
     *     the value for the negotiatedParams property
     */
    public void setNegotiatedParams(List<String> negotiatedParams) {
        this.negotiatedParams = negotiatedParams;
    }

}
