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

package com.sun.xml.ws.transport.tcp.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Alexey Stashok
 */
public final class TCPStandaloneContext implements TCPContext {
    
    private final ClassLoader classloader;
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    
    public TCPStandaloneContext(final ClassLoader classloader) {
        this.classloader = classloader;
    }
    
    public InputStream getResourceAsStream(final String resource) throws IOException {
        return classloader.getResourceAsStream(resource);
    }
    
    public Set<String> getResourcePaths(final String path) {
        try {
            return populateResourcePaths(path);
        } catch (Exception ex) {
        }
        
        return Collections.emptySet();
    }
    
    
    public URL getResource(String resource) {
        if (resource.charAt(0) == '/') {
            resource = resource.substring(1, resource.length());
        }
        
        return classloader.getResource(resource);
    }
    
    private Enumeration<URL> getResources(String resource) throws IOException {
        if (resource.charAt(0) == '/') {
            resource = resource.substring(1, resource.length());
        }
        
        return classloader.getResources(resource);
    }
    
    private Set<String> populateResourcePaths(final String path) throws Exception {
        final Set<String> resources = new HashSet<String>();
        
        for(final Enumeration<URL> initResources = getResources(path); initResources.hasMoreElements(); ) {
            final URI resourceURI = initResources.nextElement().toURI();
            if (resourceURI.getScheme().equals("file")) {
                gatherResourcesWithFileMode(path, resourceURI, resources);
            } else if (resourceURI.getScheme().equals("jar")) {
                gatherResourcesWithJarMode(path, resourceURI, resources);
            }
        }
        
        return resources;
    }
    
    private void gatherResourcesWithFileMode(final String path, final URI resourceURI, final Set<String> resources) {
        final File file = new File(resourceURI);
        final String[] list = file.list(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.charAt(0) != '.';
            }
        });
        
        for(String filename : list) {
            resources.add(path + filename);
        }
    }
    
    private void gatherResourcesWithJarMode(final String path, final URI resourceURI, final Set<String> resources) {
        final String resourceURIAsString = resourceURI.toASCIIString();
        final int pathDelim = resourceURIAsString.indexOf('!');
        final String zipFile = resourceURIAsString.substring("jar:file:/".length(), (pathDelim != -1) ? pathDelim : resourceURIAsString.length());
        ZipFile file = null;
        
        try {
            file = new ZipFile(zipFile);
            
            String pathToCompare = path;
            if (pathToCompare.charAt(0) == '/') {
                pathToCompare = pathToCompare.substring(1, pathToCompare.length());
            }
            if (!pathToCompare.endsWith("/")) {
                pathToCompare = pathToCompare + "/";
            }
            
            for(final Enumeration<? extends ZipEntry> e = file.entries(); e.hasMoreElements(); ) {
                final ZipEntry entry = e.nextElement();
                if (entry.getName().startsWith(pathToCompare) && !entry.getName().equals(pathToCompare)) {
                    resources.add("/" + entry.getName());
                }
            }
        } catch(IOException e) {
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }
    
    public void setAttribute(final String name, final Object value) {
        attributes.put(name, value);
    }
}
