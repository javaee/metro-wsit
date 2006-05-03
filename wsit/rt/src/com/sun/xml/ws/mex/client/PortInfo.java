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
package com.sun.xml.ws.mex.client;

import javax.xml.namespace.QName;

/**
 * Class to hold information about a port, such as
 * the port name, address, and name of the containing service.
 *
 * @see com.sun.xml.ws.mex.client.MetadataClient
 */
public class PortInfo {

    private QName serviceName;
    private QName portName;
    private String address;
    
    PortInfo(QName serviceName, QName portName, String address) {
        this.serviceName = serviceName;
        this.portName = portName;
        this.address = address;
    }
    
    /**
     * Retrieve the qname for the service that
     * contains this port.
     */
    public QName getServiceName() {
        return serviceName;
    }
    
    /**
     * Retrieve the qname for this port.
     */
    public QName getPortName() {
        return portName;
    }
    
    /**
     * Retrieve the address for this port.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Utility method for obtaining port local name. This
     * method is equivalent to getPortName().getLocalPart().
     */
    public String getPortLocalPart() {
        return portName.getLocalPart();
    }

    /**
     * Utility method for obtaining port namespace. This
     * method is equivalent to getPortName().getNamespaceURI().
     */
    public String getPortNamespaceURI() {
        return portName.getNamespaceURI();
    }
    
    /**
     * Utility method for obtaining service local name. This
     * method is equivalent to getServiceName().getLocalPart().
     */
    public String getServiceLocalPart() {
        return serviceName.getLocalPart();
    }

}
