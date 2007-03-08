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

/**
 * @author Alexey Stashok
 */
public final class VersionController {
    
    private static final VersionController instance = new VersionController(
            new Version(1, 0), new Version(1, 0));
    
    private final Version framingVersion;
    private final Version connectionManagementVersion;
    
    private VersionController(final Version framingVersion,
            final Version connectionManagementVersion) {
        this.framingVersion = framingVersion;
        this.connectionManagementVersion = connectionManagementVersion;
    }
    
    public static VersionController getInstance() {
        return instance;
    }
    
    public Version getFramingVersion() {
        return framingVersion;
    }
    
    public Version getConnectionManagementVersion() {
        return connectionManagementVersion;
    }
    
    /**
    *  Method checks compatibility of server and client versions
    */
    public boolean isVersionSupported(final Version framingVersion,
            final Version connectionManagementVersion) {
        
        return this.framingVersion.equals(framingVersion) &&
                this.connectionManagementVersion.equals(connectionManagementVersion);
    }

    /**
    *  Method returns closest to given framing version, which current implementation supports
    */
    public Version getClosestSupportedFramingVersion(Version framingVersion) {
        return this.framingVersion;
    }

    /**
    *  Method returns closest to given connection management version, which current implementation supports
    */
    public Version getClosestSupportedConnectionManagementVersion(Version connectionManagementVersion) {
        return this.connectionManagementVersion;
    }
}
