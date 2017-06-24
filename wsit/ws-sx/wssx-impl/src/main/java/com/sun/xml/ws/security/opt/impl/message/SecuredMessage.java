/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.security.opt.impl.message;

import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.impl.outgoing.SecurityHeader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.NamespaceContextEx;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.security.opt.impl.attachment.AttachmentSetImpl;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.Header;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SecuredMessage {

    ArrayList headers;    
    SOAPBody body = null;
    boolean isOneWay = false;
    SecurityElement securedBody = null;
    AttachmentSet attachments = null;
    private NamespaceContextEx context = null;
    SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    private SecurityHeader sh = null;

    /** Creates a new instance of SecuredMessage */
    public SecuredMessage(Message msg, SecurityHeader sh, SOAPVersion soapVersion) {
        this(msg, sh);
        this.body = new SOAPBody(msg, soapVersion);
        this.soapVersion = soapVersion;
        boolean isSOAP12 = (soapVersion == SOAPVersion.SOAP_12) ? true : false;
    }

    @SuppressWarnings("unchecked")
    public SecuredMessage(Message msg, SecurityHeader sh) {
        // FIXME: RJE - Remove cast and then just use MessageHeaders rather than ArrayList
        HeaderList hl = (HeaderList) msg.getHeaders();
        headers = new ArrayList(hl);
        //this.msg = msg;
        this.body = new SOAPBody(msg);
        attachments = msg.getAttachments();
        this.sh = sh;
    }

    public SOAPVersion getSOAPVersion() {
       return this.soapVersion;
    }
    
    public ArrayList getHeaders() {
        return headers;
    }

    public void setRootElements(NamespaceContextEx ne) {
        this.context = ne;
    }

    public boolean isOneWay() {
        return this.isOneWay;
    }

    public Iterator getHeaders(final String localName, final String uri) {
        return new Iterator() {

            int idx = 0;
            Object next;

            public boolean hasNext() {
                if (next == null) {
                    fetch();
                }
                return next != null;
            }

            public Object next() {
                if (next == null) {
                    fetch();
                    if (next == null) {
                        throw new NoSuchElementException();
                    }
                }

                Object r = next;
                next = null;
                return r;
            }

            private void fetch() {
                while (idx < headers.size()) {
                    Object obj = headers.get(idx++);
                    if (obj instanceof Header) {
                        Header hd = (Header) obj;
                        if ((uri == null && localName.equals(hd.getLocalPart())) ||
                                (localName.equals(hd.getLocalPart()) && uri.equals(hd.getNamespaceURI()))) {
                            next = hd;
                            break;
                        }
                    } else if (obj instanceof SecurityElement) {
                        SecurityElement she = (SecurityElement) obj;
                        if ((uri == null && localName.equals(she.getLocalPart())) ||
                                (localName.equals(she.getLocalPart()) && uri.equals(she.getNamespaceURI()))) {
                            next = she;
                            break;
                        }
                    }
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

    }

    public Iterator getHeaders(final String uri) {
        return new Iterator() {

            int idx = 0;
            Object next;

            public boolean hasNext() {
                if (next == null) {
                    fetch();
                }
                return next != null;
            }

            public Object next() {
                if (next == null) {
                    fetch();
                    if (next == null) {
                        throw new NoSuchElementException();
                    }
                }

                Object r = next;
                next = null;
                return r;
            }

            private void fetch() {
                while (idx < headers.size()) {
                    Object obj = headers.get(idx++);
                    if (obj instanceof Header) {
                        Header hd = (Header) obj;
                        if (uri.equals(hd.getNamespaceURI())) {
                            next = hd;
                            break;
                        }
                    } else if (obj instanceof SecurityElement) {
                        SecurityElement she = (SecurityElement) obj;
                        if (uri.equals(she.getNamespaceURI())) {
                            next = she;
                            break;
                        }
                    }
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    @SuppressWarnings("unchecked")
    public boolean replaceHeader(Object header1, Object header2) {
        boolean replaced = false;
        for (int i = 0; i < headers.size(); i++) {
            Object obj = headers.get(i);
            if (obj == header1 && obj.equals(header1)) {
                headers.set(i, header2);
                replaced = true;
                break;
            }
        }
        return replaced;
    }

    public Object getHeader(String id) {
        Object hdr = null;
        for (int i = 0; i < headers.size(); i++) {
            Object obj = headers.get(i);
            if (obj instanceof Header) {
                Header hd = (Header) obj;
                String wsuId = hd.getAttribute(MessageConstants.WSSE_NS, "Id");
                if (id.equals(wsuId)) {
                    hdr = hd;
                    break;
                }
            } else if (obj instanceof SecurityElement) {
                SecurityElement she = (SecurityElement) obj;
                if (id.equals(she.getId())) {
                    hdr = she;
                    break;
                }
            }
        }
        return hdr;
    }

    public String getPayloadNamespaceURI() {
        if (body != null) {
            return body.getPayloadNamespaceURI();
        }
        if (securedBody != null) {
            return securedBody.getNamespaceURI();
        }
        return null;
    }

    public String getPayloadLocalPart() {
        if (body != null) {
            return body.getPayloadLocalPart();
        }
        if (securedBody != null) {
            return securedBody.getLocalPart();
        }
        return null;
    }

    public XMLStreamReader readPayload() throws XMLStreamException {
        if (body != null) {
            return body.read();
        }

        if (securedBody != null) {
            return securedBody.readHeader();
        }
        throw new XMLStreamException("No Payload found");
    }

    public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
        if (body != null) {
            body.writeTo(sw);
        } else if (securedBody != null) {
            ((SecurityElementWriter) securedBody).writeTo(sw);
        } else {
            throw new XMLStreamException("No Payload found");
        }
        return;
    }

    public Object getBody() throws XWSSecurityException {
        if (body != null) {
            return body;
        } else if (securedBody != null) {
            return securedBody;
        } else {
            throw new XWSSecurityException("No body present in message");
        }
    }

    public void replaceBody(SecurityElement she) {
        //msg = null;
        securedBody = she;
        body = null;
    }

    public void replaceBody(SOAPBody sb) {
        //msg = null;
        body = sb;
        securedBody = null;
    }

    public AttachmentSet getAttachments() {
        if (attachments == null) {
            attachments = new AttachmentSetImpl();
        }
        return attachments;
    }
    
    public void setAttachments(AttachmentSet as){
        attachments = as;
    }

    public Attachment getAttachment(String uri) {
        Attachment attachment = null;

        if (attachments != null && uri.startsWith("cid:")) {
            uri = uri.substring("cid:".length());
            attachment = attachments.get(uri);
        }
        
        return attachment;
    }

    public void writeTo(XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartDocument();
        sw.writeStartElement("S", "Envelope", soapVersion.nsUri);
        Iterator<org.jvnet.staxex.NamespaceContextEx.Binding> itr = context.iterator();

        while (itr.hasNext()) {
            com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx.Binding binding = itr.next();
            sw.writeNamespace(binding.getPrefix(), binding.getNamespaceURI());
        }

        sw.writeStartElement("S", "Header", soapVersion.nsUri);
        for (int i = 0; i < headers.size(); i++) {
            Object hdr = headers.get(i);
            if (hdr instanceof Header) {
                ((Header) hdr).writeTo(sw);
            } else {
                ((SecurityElementWriter) hdr).writeTo(sw);
            }
        }

        sh.writeTo(sw);
        sw.writeEndElement();
        if (securedBody != null) {
            ((SecurityElementWriter) securedBody).writeTo(sw);
        } else if (body != null) {
            body.writeTo(sw);
        }

        sw.writeEndDocument();
    }
}
