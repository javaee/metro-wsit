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

package com.sun.xml.ws.transport.tcp.server.glassfish;

import javax.xml.namespace.QName;

import com.sun.enterprise.deployment.WebServiceEndpoint;

/**
 * @author Alexey Stashok
 */
public class WSEndpointDescriptor {
    
    private QName wsServiceName;
    private String uri;
    private String contextRoot;
    private String urlPattern;
    private boolean isEJB;
    private WebServiceEndpoint wsServiceEndpoint;
    
    // Full address to endpoint
    private String requestURL;
    
    public WSEndpointDescriptor(WebServiceEndpoint wsServiceDescriptor, String contextRoot, 
            String urlPattern, String requestURL) {
        this.wsServiceName = wsServiceDescriptor.getServiceName();
        this.uri = wsServiceDescriptor.getEndpointAddressUri();
        this.isEJB = wsServiceDescriptor.implementedByEjbComponent();
        this.wsServiceEndpoint = wsServiceDescriptor;
        this.contextRoot = contextRoot;
        this.urlPattern = urlPattern;
        this.requestURL = requestURL;
    }

    public QName getWSServiceName() {
        return wsServiceName;
    }

    public String getURI() {
        return uri;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public String getRequestURL() {
        return requestURL;
    }
    
    public String getUrlPattern() {
        return urlPattern;
    }

    public WebServiceEndpoint getWSServiceEndpoint() {
        return wsServiceEndpoint;
    }
    
    public boolean isEJB() {
        return isEJB;
    }
}
