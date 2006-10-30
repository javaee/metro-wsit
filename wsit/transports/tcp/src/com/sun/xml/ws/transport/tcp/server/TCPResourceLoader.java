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

import com.sun.xml.ws.transport.http.ResourceLoader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * @author Alexey Stashok
 */
public class TCPResourceLoader implements ResourceLoader {
    private TCPContext context;
    
    public TCPResourceLoader(TCPContext context) {
        this.context = context;
    }
    
    public URL getResource(String path) throws MalformedURLException {
        return context.getResource(path);
    }

    public URL getCatalogFile() throws MalformedURLException {
        return getResource("/WEB-INF/jax-ws-catalog.xml");
    }

    public Set<String> getResourcePaths(String path) {
        return context.getResourcePaths(path);
    }
}
