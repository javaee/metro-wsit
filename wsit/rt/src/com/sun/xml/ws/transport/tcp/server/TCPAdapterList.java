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

package com.sun.xml.ws.transport.tcp.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.api.server.WSEndpoint;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * @author jax-ws team
 */
public final class TCPAdapterList extends AbstractList<TCPAdapter> implements AdapterFactory<TCPAdapter> {
    private final List<TCPAdapter> adapters = new ArrayList<TCPAdapter>();
    private final Map<String, String> addressMap = new HashMap<String, String>();
    
    // TODO: documented because it's used by AS
    public TCPAdapter createAdapter(final String name, final String urlPattern, final WSEndpoint<?> endpoint) {
        final TCPAdapter tcpAdapter = new TCPAdapter(name, urlPattern, endpoint);
        adapters.add(tcpAdapter);
        final WSDLPort port = endpoint.getPort();
        if (port != null) {
            addressMap.put(port.getName().getLocalPart(), getValidPath(urlPattern));
        }
        return tcpAdapter;
    }
    
    /**
     * @return urlPattern without "/*"
     */
    private String getValidPath(@NotNull final String urlPattern) {
        if (urlPattern.endsWith("/*")) {
            return urlPattern.substring(0, urlPattern.length() - 2);
        } else {
            return urlPattern;
        }
    }
    
    /**
     * Creates a PortAddressResolver that maps portname to its address
     */
    protected PortAddressResolver createPortAddressResolver(final String baseAddress) {
        return new PortAddressResolver() {
            public String getAddressFor(QName serviceName, @NotNull String portName) {
                final String urlPattern = addressMap.get(portName);
                return (urlPattern == null) ? null : baseAddress+urlPattern;
            }
        };
    }
    
    
    public TCPAdapter get(final int index) {
        return adapters.get(index);
    }
    
    public int size() {
        return adapters.size();
    }
}
