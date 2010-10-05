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
