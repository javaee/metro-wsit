/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.commons.xmlutil;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.message.Packet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Utility class that provides conversion of different XML representations 
 * from/to various other formats
 * 
 * @author Marek Potociar
 */
public final class Converter {
    private Converter() {
        // prevents instantiation
    }

    private static final Logger DUMPER_LOGGER = Logger.getLogger(Converter.class);
    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
    private static final AtomicBoolean logMissingStaxUtilsWarning = new AtomicBoolean(false);

    public static String convertToString(Throwable throwable) {
        if (throwable == null) {
            return "[ No exception ]";
        }

        StringWriter stringOut = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringOut));
        
        return stringOut.toString();
    }

    public static String convertToString(Packet packet) {
        if (packet == null) {
            return "[ Null packet ]";
        }
        StringWriter stringOut = null;
        try {
            stringOut = new StringWriter();
            if (packet.getMessage() == null) {
                stringOut.write("[ Empty packet ]");
            } else {
                XMLStreamWriter writer = null;
                try {
                    writer = xmlOutputFactory.createXMLStreamWriter(stringOut);
                    writer = createIndenter(writer);
                    packet.getMessage().copy().writeTo(writer);
                } catch (XMLStreamException e) {
                    DUMPER_LOGGER.log(Level.WARNING, "Unexpected exception occured while dumping message", e);
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (XMLStreamException ignored) {
                            DUMPER_LOGGER.fine("Unexpected exception occured while closing XMLStreamWriter", ignored);
                        }
                    }
                }
            }
            return stringOut.toString();
        } finally {
            if (stringOut != null) {
                try {
                    stringOut.close();
                } catch (IOException ex) {
                    DUMPER_LOGGER.finest("An exception occured when trying to close StringWriter", ex);
                }
            }
        }
    }

    /**
     * Wraps {@link XMLStreamWriter} by an indentation engine if possible.
     *
     * <p>
     * We can do this only when we have <tt>stax-utils.jar</tt> in the class path.
     */
    public static XMLStreamWriter createIndenter(XMLStreamWriter writer) {
        try {
            Class<?> clazz = Converter.class.getClassLoader().loadClass("javanet.staxutils.IndentingXMLStreamWriter");
            Constructor<?> c = clazz.getConstructor(XMLStreamWriter.class);
            writer = XMLStreamWriter.class.cast(c.newInstance(writer));
        } catch (Exception ex) {
            // if stax-utils.jar is not in the classpath, this will fail
            // so, we'll just have to do without indentation
            if (logMissingStaxUtilsWarning.compareAndSet(false, true)) {
                DUMPER_LOGGER.log(Level.WARNING, "Put stax-utils.jar to the classpath to indent the dump output", ex);
            }
        }
        return writer;
    }
    
}
