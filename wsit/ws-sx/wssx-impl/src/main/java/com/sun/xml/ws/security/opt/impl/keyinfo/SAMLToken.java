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

package com.sun.xml.ws.security.opt.impl.keyinfo;

import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.wss.saml.Assertion;
import java.io.OutputStream;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.ws.api.SOAPVersion;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class SAMLToken implements SecurityHeaderElement, SecurityElementWriter {
    private Assertion samlToken =null;
    private JAXBContext jxbContext = null;
    private SOAPVersion soapVersion = null;
    /** Creates a new instance of SAMLToken */
    public SAMLToken(Assertion assertion,JAXBContext jxbContext,SOAPVersion soapVersion) {
        this.samlToken = assertion;
        this.jxbContext = jxbContext;
        this.soapVersion = soapVersion;
        
    }
    
    public boolean refersToSecHdrWithId(String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getId() {
        return samlToken.getAssertionID();
    }
    
    public void setId(String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getNamespaceURI() {
        throw new UnsupportedOperationException();
    }
    
    public String getLocalPart() {
        throw new UnsupportedOperationException();
    }
    
    public XMLStreamReader readHeader() throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    /**
     * writes the SAML assertion to the XMLStreamWriter
     * @param streamWriter XMLStreamWriter
     * @throws javax.xml.stream.XMLStreamException
     */
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
        try{
            Marshaller marshaller = jxbContext.createMarshaller();
            if(SOAPVersion.SOAP_11 == soapVersion){
                marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", JAXBUtil.prefixMapper11);
            }else{
                marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", JAXBUtil.prefixMapper12);
            }
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT,true);
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", false);
            marshaller.marshal(samlToken,streamWriter);
            
        }catch(javax.xml.bind.PropertyException pe){
            //log here
            throw new XMLStreamException("Error occurred while setting security marshaller properties",pe);
        }catch(JAXBException je){
            //log here
            throw new XMLStreamException("Error occurred while marshalling SAMLAssertion",je);
        }
    }
    
    public void writeTo(XMLStreamWriter streamWriter, HashMap props) throws XMLStreamException {
    }
    
    public void writeTo(OutputStream os) {
    }
    
}
