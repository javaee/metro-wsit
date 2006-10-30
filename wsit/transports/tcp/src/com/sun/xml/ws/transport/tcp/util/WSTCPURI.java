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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Alexey Stashok
 */
public class WSTCPURI {
    public String host;
    public int port;
    public String path;

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
    
    public String getParameter(String name) {
        if (params != null) {
            return params.get(name);
        }
        
        return null;
    }
    
    public static WSTCPURI parse(String uri) {
        try {
            return parse(new URI(uri));
        } catch (URISyntaxException ex) {
            return null;
        }
    }
    
    public static WSTCPURI parse(URI uri) {
        String path = uri.getPath();
        String query = uri.getQuery();
        Map<String, String> params = null;
        
        if (query != null && query.length() > 0) {
            String[] paramsStr = query.split(";");
            params = new HashMap<String, String>(paramsStr.length);
            for(String paramStr : paramsStr) {
                if (paramStr.length() > 0) {
                    String[] paramAsgn = paramStr.split("=");
                    if (paramAsgn != null && paramAsgn.length == 2 && paramAsgn[0].length() > 0 && paramAsgn[1].length() > 0) {
                        params.put(paramAsgn[0], paramAsgn[1]);
                    }
                }
            }
        }
        
        return new WSTCPURI(uri.getHost(), uri.getPort(), path, params, uri.toASCIIString());
    }
    
    public String toString() {
        return uri2string;
    }

    public int hashCode() {
        return host.hashCode() + port;
    }
    
    /**
     * Class is used to translate WSTCPURI to String and vice versa
     * This is used in JAXB serialization/deserialization
     */
    public static class WSTCPURI2StringJAXBAdapter extends XmlAdapter<String, WSTCPURI> {
        public String marshal(WSTCPURI tcpURI) throws Exception {
            return tcpURI.toString();
        }

        public WSTCPURI unmarshal(String uri) throws Exception {
            return WSTCPURI.parse(uri);
        }
        
    }
}
