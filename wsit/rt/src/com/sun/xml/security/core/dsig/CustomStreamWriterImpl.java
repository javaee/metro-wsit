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
