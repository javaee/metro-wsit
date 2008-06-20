/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.xml.ws.transport.tcp.client.WSConnectionManager;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Alexey Stashok
 */
public final class WSTCPURI implements com.sun.xml.ws.transport.tcp.connectioncache.spi.transport.ContactInfo<ConnectionSession> {
    public String host;
    public int port;
    public String path;
    
    // The TCP port, where connection will be established.
    // If -1 then port value will be used.
    public int customPort = -1;

    private String uri2string;
    private Map<String, String> params;
    
    /**
     * This constructor should be used just by JAXB runtime
     */
    public WSTCPURI() {}
    
    private WSTCPURI(String host, int port, String path, Map<String, String> params, String uri2string) {
        this.host = host;
        this.port = port;
        this.path = path;
        this.params = params;
        this.uri2string = uri2string;
    }
    
    public String getParameter(final String name) {
        if (params != null) {
            return params.get(name);
        }
        
        return null;
    }
    
    public static WSTCPURI parse(final String uri) {
        try {
            return parse(new URI(uri));
        } catch (URISyntaxException ex) {
            return null;
        }
    }
    
    public static WSTCPURI parse(final URI uri) {
        final String path = uri.getPath();
        final String query = uri.getQuery();
        Map<String, String> params = null;
        
        if (query != null && query.length() > 0) {
            final String[] paramsStr = query.split(";");
            params = new HashMap<String, String>(paramsStr.length);
            for(String paramStr : paramsStr) {
                if (paramStr.length() > 0) {
                    final String[] paramAsgn = paramStr.split("=");
                    if (paramAsgn != null && paramAsgn.length == 2 && paramAsgn[0].length() > 0 && paramAsgn[1].length() > 0) {
                        params.put(paramAsgn[0], paramAsgn[1]);
                    }
                }
            }
        }
        
        return new WSTCPURI(uri.getHost(), uri.getPort(), path, params, uri.toASCIIString());
    }

    /**
     * Get custom TCP port, where connection should be established
     * @return custom TCP port
     */
    public int getCustomPort() {
        return customPort;
    }

    /**
     * Set custom TCP port, where connection should be established
     * @param customPort custom TCP port
     */
    public void setCustomPort(int customPort) {
        this.customPort = customPort;
    }
    
    public int getEffectivePort() {
        if (customPort == -1) {
            return port;
        }
        
        return customPort;
    }
    
    @Override
    public String toString() {
        return uri2string;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WSTCPURI) {
            WSTCPURI toCmp = (WSTCPURI) o;
            boolean basicResult = (port == toCmp.port && host.equals(toCmp.host));
            if (customPort == -1 && toCmp.customPort == -1) {
                return basicResult;
            } else {
                return basicResult && (customPort == toCmp.customPort);
            }
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        return host.hashCode() + (port << 2) + customPort;
    }

    public ConnectionSession createConnection() throws IOException {
        try {
            return WSConnectionManager.getInstance().createConnectionSession(this);
        } catch (VersionMismatchException e) {
            throw new IOException(e.getMessage());
        } catch (ServiceChannelException e) {
            throw new IOException(MessagesMessages.WSTCP_0024_SERVICE_CHANNEL_EXCEPTION(e.getFaultInfo().getErrorCode(), e.getMessage()));
        }
    }
    
    /**
     * Class is used to translate WSTCPURI to String and vice versa
     * This is used in JAXB serialization/deserialization
     */
    public static final class WSTCPURI2StringJAXBAdapter extends XmlAdapter<String, WSTCPURI> {
        public String marshal(final WSTCPURI tcpURI) throws Exception {
            return tcpURI.toString();
        }

        public WSTCPURI unmarshal(final String uri) throws Exception {
            return WSTCPURI.parse(uri);
        }
        
    }
}
