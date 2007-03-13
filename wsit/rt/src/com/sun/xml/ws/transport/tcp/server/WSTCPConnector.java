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

import java.io.IOException;

/**
 * @author Alexey Stashok
 */
public interface WSTCPConnector {
    public void listen() throws IOException;
    public String getHost();
    public void setHost(String host);
    public int getPort();
    public void setPort(int port);
    public TCPMessageListener getListener();
    public void setListener(TCPMessageListener listener);
    public void setFrameSize(int frameSize);
    public int getFrameSize();
    public void close();
}
