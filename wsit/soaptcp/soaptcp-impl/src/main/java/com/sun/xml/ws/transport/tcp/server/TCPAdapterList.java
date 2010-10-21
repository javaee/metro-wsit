/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
