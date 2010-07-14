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

package com.sun.xml.ws.transport.tcp.server.tomcat;

import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;

/**
 * SOAP/TCP implementation of Tomcat ProtocolHandler, based on Grizzly 1.0
 * @author Alexey Stashok
 */
public abstract class WSTCPTomcatProtocolHandlerBase implements ProtocolHandler, Runnable {    
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");

    private Map<String, Object> atts = new HashMap<String, Object>();
    
    private Adapter adapter;
    
    protected int port;
    protected int redirectHttpPort = 8080;
    protected int readThreadsCount;
    protected int maxWorkerThreadsCount = -1;
    protected int minWorkerThreadsCount = -1;

    public void setAttribute(String string, Object object) {
        atts.put(string, object);
    }

    public Object getAttribute(String string) {
        return atts.get(string);
    }

    public Iterator getAttributeNames() {
        return atts.keySet().iterator();
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    public void init() throws Exception {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, MessagesMessages.WSTCP_1170_INIT_SOAPTCP(port));
        }
        
        WSTCPTomcatRegistry.setInstance(new WSTCPTomcatRegistry(port));
    }

    public void start() throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1171_START_SOAPTCP_LISTENER());
        }
        new Thread(this).start();
    }

    public void resume() throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1173_RESUME_SOAPTCP_LISTENER());
        }
        start();
    }

    public void pause() throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1172_PAUSE_SOAPTCP_LISTENER());
        }
        WSTCPTomcatRegistry.setInstance(new WSTCPTomcatRegistry(-1));
        destroy();
    }    
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setReadThreadsCount(int readThreadsCount) {
        this.readThreadsCount = readThreadsCount;
    }
    
    public int getReadThreadsCount() {
        return readThreadsCount;
    }
    
    public void setMaxWorkerThreadsCount(int maxWorkerThreadsCount) {
        this.maxWorkerThreadsCount = maxWorkerThreadsCount;
    }
    
    public int getMaxWorkerThreadsCount() {
        return maxWorkerThreadsCount;
    }
    
    public void setMinWorkerThreadsCount(int minWorkerThreadsCount) {
        this.minWorkerThreadsCount = minWorkerThreadsCount;
    }
    
    public int getMinWorkerThreadsCount() {
        return minWorkerThreadsCount;
    }

    public void setRedirectHttpPort(int redirectHttpPort) {
        this.redirectHttpPort = redirectHttpPort;
    }

    public int getRedirectHttpPort() {
        return redirectHttpPort;
    }

    @Override
    public String toString() {
        return MessagesMessages.WSTCP_1174_TOMCAT_SOAPTCP_LISTENER(port);
    }
}
