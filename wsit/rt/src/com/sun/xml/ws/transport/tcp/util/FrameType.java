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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexey Stashok
 */

public class FrameType {
    public static final int MESSAGE = 0;
    public static final int MESSAGE_START_CHUNK = 1;
    public static final int MESSAGE_CHUNK = 2;
    public static final int MESSAGE_END_CHUNK = 3;
    public static final int ERROR = 4;
    public static final int NULL = 5;
    
    private static final Set<Integer> typesContainParameters;
    
    static {
        typesContainParameters = new HashSet<Integer>();
        typesContainParameters.add(MESSAGE);
        typesContainParameters.add(MESSAGE_START_CHUNK);
        typesContainParameters.add(ERROR);
    }
    
    public static boolean isFrameContainsParams(int msgId) {
        return typesContainParameters.contains(msgId);
    }
    
    public static boolean isLastFrame(int msgId) {
        return msgId == MESSAGE || msgId == MESSAGE_END_CHUNK || 
                msgId == ERROR || msgId == NULL;
    }
}
