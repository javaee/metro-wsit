/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.security.impl.kerberos;

import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

/**
 * A utility class for reading passwords
 *
 */
public class Password {

    /** Reads user password from given input stream. */
    public static char[] readPassword(InputStream in) throws IOException {

        char[] consoleEntered = null;
        byte[] consoleBytes = null;

        try {
            // Use the new java.io.Console class
            Console con = null;
            if (in == System.in && ((con = System.console()) != null)) {
                consoleEntered = con.readPassword();
                // readPassword returns "" if you just print ENTER,
                // to be compatible with old Password class, change to null
                if (consoleEntered != null && consoleEntered.length == 0) {
                    return null;
                }
                consoleBytes = convertToBytes(consoleEntered);
                in = new ByteArrayInputStream(consoleBytes);
            }

            // Rest of the lines still necessary for KeyStoreLoginModule
            // and when there is no console.

            char[] lineBuffer;
            char[] buf;
            int i;

            buf = lineBuffer = new char[128];

            int room = buf.length;
            int offset = 0;
            int c;

            boolean done = false;
            while (!done) {
                switch (c = in.read()) {
                    case -1:
                    case '\n':
                        done = true;
                        break;

                    case '\r':
                        int c2 = in.read();
                        if ((c2 != '\n') && (c2 != -1)) {
                            if (!(in instanceof PushbackInputStream)) {
                                in = new PushbackInputStream(in);
                            }
                            ((PushbackInputStream) in).unread(c2);
                        } else {
                            done = true;
                            break;
                        }

                    default:
                        if (--room < 0) {
                            buf = new char[offset + 128];
                            room = buf.length - offset - 1;
                            System.arraycopy(lineBuffer, 0, buf, 0, offset);
                            Arrays.fill(lineBuffer, ' ');
                            lineBuffer = buf;
                        }
                        buf[offset++] = (char) c;
                        break;
                }
            }

            if (offset == 0) {
                return null;
            }

            char[] ret = new char[offset];
            System.arraycopy(buf, 0, ret, 0, offset);
            Arrays.fill(buf, ' ');

            return ret;
        } finally {
            if (consoleEntered != null) {
                Arrays.fill(consoleEntered, ' ');
            }
            if (consoleBytes != null) {
                Arrays.fill(consoleBytes, (byte) 0);
            }
        }
    }

    /**
     * Change a password read from Console.readPassword() into
     * its original bytes.
     *
     * @param pass a char[]
     * @return its byte[] format, similar to new String(pass).getBytes()
     */
    private static byte[] convertToBytes(char[] pass) {
        if (enc == null) {
            synchronized (Password.class) {
                enc = sun.misc.SharedSecrets.getJavaIOAccess().charset().newEncoder().
                        onMalformedInput(CodingErrorAction.REPLACE).
                        onUnmappableCharacter(CodingErrorAction.REPLACE);
            }
        }
        byte[] ba = new byte[(int) (enc.maxBytesPerChar() * pass.length)];
        ByteBuffer bb = ByteBuffer.wrap(ba);
        synchronized (enc) {
            enc.reset().encode(CharBuffer.wrap(pass), bb, true);
        }
        if (bb.position() < ba.length) {
            ba[bb.position()] = '\n';
        }
        return ba;
    }
    private static volatile CharsetEncoder enc;
}
