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
package com.sun.xml.ws.messagedump;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.util.pipe.DumpTube;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class MessageDumpingTube extends AbstractFilterTubeImpl {

    private static enum MessageType {
        Request("Request message"),
        Response("Response message"),
        Exception("Response exception");

        private final String name;

        private MessageType(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    static final String DEFAULT_MSGDUMP_LOGGING_ROOT = com.sun.xml.ws.util.Constants.LoggingDomain + ".messagedump";
    private static final com.sun.xml.ws.commons.Logger TUBE_LOGGER = com.sun.xml.ws.commons.Logger.getLogger(DEFAULT_MSGDUMP_LOGGING_ROOT, MessageDumpingTube.class);
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    //
    private final XMLOutputFactory xmlOutputFactory;
    private final java.util.logging.Logger messageLogger;
    private final MessageDumpingFeature messageDumpingFeature;
    private final int tubeId;
    //
    private AtomicBoolean logMissingStaxUtilsWarning;

    /**
     * @param name
     *      Specify the name that identifies this {@link MessageDumpingTube}
     *      instance. This string will be printed when this pipe
     *      dumps messages, and allows people to distinguish which
     *      pipe instance is dumping a message when multiple
     *      {@link DumpTube}s print messages out.
     * @param out
     *      The output to send dumps to.
     * @param next
     *      The next {@link Tube} in the pipeline.
     */
    MessageDumpingTube(Tube next, MessageDumpingFeature feature) {
        super(next);

        this.xmlOutputFactory = XMLOutputFactory.newInstance();
        this.logMissingStaxUtilsWarning = new AtomicBoolean(false);

        this.messageLogger = java.util.logging.Logger.getLogger(feature.getMessageLoggingRoot());
        this.messageDumpingFeature = feature;
        this.tubeId = ID_GENERATOR.incrementAndGet();
    //staxOut.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES,true);
    }

    /**
     * Copy constructor.
     */
    MessageDumpingTube(MessageDumpingTube that, TubeCloner cloner) {
        super(that, cloner);

        this.xmlOutputFactory = that.xmlOutputFactory;
        this.logMissingStaxUtilsWarning = that.logMissingStaxUtilsWarning;

        this.messageLogger = that.messageLogger;
        this.messageDumpingFeature = that.messageDumpingFeature;
        this.tubeId = ID_GENERATOR.incrementAndGet();
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new MessageDumpingTube(this, cloner);
    }

    @Override
    public NextAction processRequest(Packet request) {
        dump(MessageType.Request, packetToString(request), Fiber.current().owner.id);
        return super.processRequest(request);
    }

    @Override
    public NextAction processResponse(Packet response) {
        dump(MessageType.Response, packetToString(response), Fiber.current().owner.id);
        return super.processResponse(response);
    }

    @Override
    public NextAction processException(Throwable t) {
        StringWriter stringOut = new StringWriter();
        t.printStackTrace(new PrintWriter(stringOut));

        dump(MessageType.Exception, stringOut.toString(), Fiber.current().owner.id);

        return super.processException(t);
    }

    private void dump(MessageType messageType, String message, String engineId) {
        String logMessage = String.format("%s received on DumpingTube [ %d ] Engine [ %s ] Thread [ %s ]:%n%s", messageType, tubeId, engineId, Thread.currentThread().getName(), message);
        if (messageDumpingFeature.getMessageLoggingStatus()) {
            messageLogger.log(messageDumpingFeature.getMessageLoggingLevel(), logMessage);
        }
        messageDumpingFeature.offerMessage(logMessage);
    }

    private String packetToString(Packet packet) {
        StringWriter stringOut = null;
        try {
            stringOut = new StringWriter();
            if (packet.getMessage() == null) {
                stringOut.write("[Empty response]");
            } else {
                XMLStreamWriter writer = null;
                try {
                    writer = xmlOutputFactory.createXMLStreamWriter(stringOut);
                    writer = createIndenter(writer);
                    packet.getMessage().copy().writeTo(writer);
                } catch (XMLStreamException e) {
                    TUBE_LOGGER.log(Level.WARNING, "Unexpected exception occured while dumping message", e);
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (XMLStreamException ignored) {
                            TUBE_LOGGER.fine("Unexpected exception occured while closing XMLStreamWriter", ignored);
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
                    TUBE_LOGGER.finest("An exception occured when trying to close StringWriter", ex);
                }
            }
        }
    }

    /**
     * Wraps {@link XMLStreamWriter} by an indentation engine if possible.
     *
     * <p>
     * We can do this only when we have <tt>stax-utils.jar</tt> in the classpath.
     */
    private XMLStreamWriter createIndenter(XMLStreamWriter writer) {
        try {
            Class<?> clazz = this.getClass().getClassLoader().loadClass("javanet.staxutils.IndentingXMLStreamWriter");
            Constructor<?> c = clazz.getConstructor(XMLStreamWriter.class);
            writer = XMLStreamWriter.class.cast(c.newInstance(writer));
        } catch (Exception ex) {
            // if stax-utils.jar is not in the classpath, this will fail
            // so, we'll just have to do without indentation
            if (logMissingStaxUtilsWarning.compareAndSet(false, true)) {
                TUBE_LOGGER.log(Level.WARNING, "Put stax-utils.jar to the classpath to indent the dump output", ex);
            }
        }
        return writer;
    }
}

