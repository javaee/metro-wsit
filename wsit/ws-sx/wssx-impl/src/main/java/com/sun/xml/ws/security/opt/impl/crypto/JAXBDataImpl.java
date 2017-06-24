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

package com.sun.xml.ws.security.opt.impl.crypto;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.message.jaxb.JAXBHeader;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.ws.security.opt.crypto.JAXBData;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.NamespaceContextEx;
import com.sun.xml.wss.logging.impl.opt.LogStringsMessages;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class JAXBDataImpl implements JAXBData {

    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_DOMAIN,
            LogDomainConstants.IMPL_OPT_DOMAIN_BUNDLE);

    private JAXBElement jb;
    private JAXBContext jc= null;
    private Header header = null;
    private SecurityElement securityElement = null;
    private boolean contentOnly = false;
    private NamespaceContextEx nsContext = null;

    /** Creates a new instance of JAXBDataImpl */

    public JAXBDataImpl(JAXBElement jb,JAXBContext jc,boolean contentOnly, NamespaceContextEx nsContext) {
        this.jb = jb;
        this.jc = jc;
        this.contentOnly = contentOnly;
        this.nsContext = nsContext;
    }

    public JAXBDataImpl(Header header,boolean contentOnly, NamespaceContextEx nsContext,JAXBContext jcc) {
        this.header = header;
        this.contentOnly = contentOnly;
        this.nsContext = nsContext;
        this.jc = jcc;
    }

    public JAXBDataImpl(SecurityElement se, NamespaceContextEx nsContext,boolean contentOnly) {
        this.securityElement = se;
        this.contentOnly = contentOnly;
        this.nsContext = nsContext;
    }

    /** Creates a new instance of JAXBDataImpl */
    public JAXBDataImpl(JAXBElement jb,JAXBContext jc, NamespaceContextEx nsContext) {
        this.jb = jb;
        this.jc = jc;
        this.nsContext = nsContext;
    }


    public JAXBDataImpl(Header header) {
        this.header = header;
    }

    public JAXBDataImpl(SecurityElement se) {
        this.securityElement = se;
    }

    public JAXBElement getJAXBElement(){
        return jb;
    }

    public void writeTo(XMLStreamWriter writer)throws XWSSecurityException{
        if(securityElement != null){
            try {
                ((SecurityElementWriter)securityElement).writeTo(writer);
            } catch (XMLStreamException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1609_ERROR_SERIALIZING_ELEMENT(securityElement.getLocalPart()));
                throw new XWSSecurityException(LogStringsMessages.WSS_1609_ERROR_SERIALIZING_ELEMENT(securityElement.getLocalPart()),ex);
            }
            return;
        }

        if(header != null){
            try {
                header.writeTo(writer);
            } catch (XMLStreamException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1609_ERROR_SERIALIZING_ELEMENT(header.getLocalPart()));
                throw new XWSSecurityException(LogStringsMessages.WSS_1609_ERROR_SERIALIZING_ELEMENT(header.getLocalPart()),ex);
            }
            return;
        }

        Marshaller mh;
        try {
            mh = jc.createMarshaller();
            mh.marshal(jb,writer);
        }catch (JAXBException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1610_ERROR_MARSHALLING_JBOBJECT(jb.getName()));
            throw new XWSSecurityException(
                    LogStringsMessages.WSS_1610_ERROR_MARSHALLING_JBOBJECT(jb.getName()),ex);
        }
    }

    public void writeTo(OutputStream os) throws XWSSecurityException {
        Marshaller mh;
        try {
            if (header != null && header instanceof JAXBHeader) {
                final JAXBHeader hdr = ((JAXBHeader) header);             
                Object obj = header.readAsJAXB(jc.createUnmarshaller());
                mh = jc.createMarshaller();
                //mh.setProperty("com.sun.xml.bind.c14n", true);
                mh.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                mh.marshal(obj, os);
            } else {
                mh = jc.createMarshaller();
                mh.setProperty("com.sun.xml.bind.c14n", true);
                mh.marshal(jb, os);
            }

        } catch (javax.xml.bind.JAXBException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1610_ERROR_MARSHALLING_JBOBJECT(jb.getName()));
            throw new XWSSecurityException(LogStringsMessages.WSS_1610_ERROR_MARSHALLING_JBOBJECT(jb.getName()), ex);
        }
    }

    public NamespaceContextEx getNamespaceContext() {
        return nsContext;
    }

    public SecurityElement getSecurityElement() {
        return securityElement;
    }
}
