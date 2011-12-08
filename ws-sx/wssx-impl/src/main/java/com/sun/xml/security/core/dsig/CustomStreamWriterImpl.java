/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.xml.security.core.dsig;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.xml.bind.v2.util.ByteArrayOutputStreamEx;
import com.sun.xml.ws.streaming.MtomStreamWriter;
import com.sun.xml.ws.util.xml.XMLStreamWriterFilter;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.stream.XMLStreamException;
import org.jvnet.staxex.NamespaceContextEx;
import org.jvnet.staxex.XMLStreamWriterEx;

/**
 *
 * @author suresh
 */
public class CustomStreamWriterImpl extends XMLStreamWriterFilter implements XMLStreamWriterEx,
        MtomStreamWriter {

    protected XMLStreamWriterEx sw = null;
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);

    public CustomStreamWriterImpl(javax.xml.stream.XMLStreamWriter sw) {
        super(sw);
        this.sw = (XMLStreamWriterEx) sw;
    }

    public void writeBinary(byte[] arg0, int arg1, int arg2, String arg3) throws XMLStreamException {
        sw.writeBinary(arg0, arg1, arg2, arg3);
    }

    public void writeBinary(DataHandler dh) throws XMLStreamException {
        int len =0;
        byte[] data = null;
        InputStream is = null;
        ByteArrayOutputStreamEx baos = null;
        try {
            baos = new ByteArrayOutputStreamEx();
            is = dh.getDataSource().getInputStream();
            baos.readFrom(is);
            data = baos.toByteArray();
            len = data.length;
            baos.close();
            is.close();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "could not get the inputstream from the data handler", ioe);
        }
        if (len > 1000) {
            sw.writeBinary(dh);
        } else {
            sw.writePCDATA(Base64.encode(data));
        }
    }

    public OutputStream writeBinary(String arg0) throws XMLStreamException {
        return sw.writeBinary(arg0);
    }

    public void writePCDATA(CharSequence data) throws XMLStreamException {
        sw.writePCDATA(data);
    }

    public NamespaceContextEx getNamespaceContext() {
        return (NamespaceContextEx) sw.getNamespaceContext();
    }

    public AttachmentMarshaller getAttachmentMarshaller() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
