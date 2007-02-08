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
public final class DumpUtils {
    public static String dumpBytes(final ByteBuffer[] bb) {
        final StringBuffer stringBuffer = new StringBuffer();
        for(int i=0; i<bb.length; i++) {
            stringBuffer.append(dumpBytes(bb[i]));
        }
        
        return stringBuffer.toString();
    }

    public static String dumpBytes(final ByteBuffer buffer) {
        return dumpBytes(buffer, buffer.position(), buffer.limit() - buffer.position());
    }

    public static String dumpBytes(final ByteBuffer buffer, final int offset, final int length) {
        final byte[] array = new byte[length];
        final int position = buffer.position();
        buffer.position(offset);
        buffer.get(array);
        buffer.position(position);
        return dumpBytes(array);
    }

    public static String dumpOctets(final ByteBuffer[] bb) {
        final StringBuffer stringBuffer = new StringBuffer();
        for(int i=0; i<bb.length; i++) {
            stringBuffer.append(dumpOctets(bb[i]));
        }
        
        return stringBuffer.toString();
    }

    public static String dumpOctets(final ByteBuffer buffer) {
        return dumpOctets(buffer, buffer.position(), buffer.limit() - buffer.position());
    }

    public static String dumpOctets(final ByteBuffer buffer, final int offset, final int length) {
        final byte[] array = new byte[length];
        final int position = buffer.position();
        buffer.position(offset);
        buffer.get(array);
        buffer.position(position);
        return dumpBytes(array);
    }

    public static String dump(final ByteBuffer[] bb) {
        final StringBuffer stringBuffer = new StringBuffer();
        for(int i=0; i<bb.length; i++) {
            stringBuffer.append(dump(bb[i]));
        }
        
        return stringBuffer.toString();
    }

    public static String dump(final ByteBuffer buffer) {
        return dump(buffer, buffer.position(), buffer.limit() - buffer.position());
    }

    public static String dump(final ByteBuffer buffer, final int offset, final int length) {
        final byte[] array = new byte[length];
        final int position = buffer.position();
        buffer.position(offset);
        buffer.get(array);
        buffer.position(position);
        return dump(array);
    }
    
    public static String dump(final byte[] buffer) {
        return dump(buffer, 0, buffer.length);
    }
    
    public static String dump(final byte[] buffer, final int offset, final int length) {
        final StringBuffer stringBuffer = new StringBuffer();
        for(int i=0; i<length; i++) {
            final int value = buffer[offset + i] & 0xFF;
            final String strValue = Integer.toHexString(value).toUpperCase();
            final String str = "00".substring(strValue.length()) + strValue;
            stringBuffer.append(str);
            stringBuffer.append('(');
            stringBuffer.append((char) value);
            stringBuffer.append(')');
            stringBuffer.append(' ');
        }
        
        return stringBuffer.toString();
    }

    public static String dumpOctets(final byte[] buffer) {
        return dumpOctets(buffer, 0, buffer.length);
    }

    public static String dumpOctets(final byte[] buffer, final int offset, final int length) {
        final StringBuffer stringBuffer = new StringBuffer();
        for(int i=0; i<length; i++) {
            final int value = buffer[offset + i] & 0xFF;
            final String strValue = Integer.toHexString(value).toUpperCase();
            final String str = "00".substring(strValue.length()) + strValue;
            stringBuffer.append(str);
            stringBuffer.append(' ');
        }
        
        return stringBuffer.toString();
    }

    public static String dumpBytes(final byte[] buffer) {
        return dumpBytes(buffer, 0, buffer.length);
    }

    public static String dumpBytes(final byte[] buffer, final int offset, final int length) {
        final StringBuffer stringBuffer = new StringBuffer();
        for(int i=0; i<length; i++) {
            final int value = buffer[offset + i] & 0xFF;
            stringBuffer.append((char) value);
        }
        
        return stringBuffer.toString();
    }
}
