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
public final class TCPSettings {
    private static final TCPSettings instance = new TCPSettings();
    private static final String ENCODING_MODE_PROPERTY = "com.sun.xml.ws.transport.tcp.encodingMode";
    
    private EncodingMode encodingMode;
    
    public enum EncodingMode {
        XML,
        FI_STATELESS,
        FI_STATEFUL
    }
    
    private TCPSettings() {
        gatherSettings();
    }
    
    public static TCPSettings getInstance() {
        return instance;
    }
    
    public EncodingMode getEncodingMode() {
        return encodingMode;
    }
    
    private void gatherSettings() {
        if (System.getProperty(ENCODING_MODE_PROPERTY) != null){
            final String encodingModeS = System.getProperty(ENCODING_MODE_PROPERTY);
            if ("xml".equalsIgnoreCase(encodingModeS)) {
                encodingMode = EncodingMode.XML;
            } else if ("FIStateless".equalsIgnoreCase(encodingModeS)) {
                encodingMode = EncodingMode.FI_STATELESS;
            } else {
                encodingMode = EncodingMode.FI_STATEFUL;
            }
        } else {
            encodingMode = EncodingMode.FI_STATEFUL;
        }
    }
}
