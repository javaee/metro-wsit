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

import java.nio.ByteBuffer;

/**
 * @author Alexey Stashok
 */
public class DumpUtils {
    public static String dump(ByteBuffer buffer) {
        return dump(buffer, buffer.position(), buffer.limit() - buffer.position());
    }

    public static String dump(ByteBuffer buffer, int offset, int length) {
        byte[] array = new byte[length];
        int position = buffer.position();
        buffer.position(offset);
        buffer.get(array);
        buffer.position(position);
        return dump(array);
    }
    
    public static String dump(byte[] buffer) {
        return dump(buffer, 0, buffer.length);
    }
    
    public static String dump(byte[] buffer, int offset, int length) {
        StringBuffer stringBuffer = new StringBuffer();
        for(int i=0; i<length; i++) {
            int value = buffer[offset + i] & 0xFF;
            String strValue = Integer.toHexString(value).toUpperCase();
            String str = "00".substring(strValue.length()) + strValue;
            stringBuffer.append(str);
            stringBuffer.append(' ');
        }
        
        return stringBuffer.toString();
    }
}
