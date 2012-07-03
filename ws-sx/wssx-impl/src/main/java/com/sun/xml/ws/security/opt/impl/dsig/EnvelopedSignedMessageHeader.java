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

package com.sun.xml.ws.security.opt.impl.dsig;

import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.crypto.dsig.Reference;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.wss.impl.c14n.AttributeNS;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import com.sun.org.apache.xml.internal.security.utils.UnsyncBufferedOutputStream;
import com.sun.xml.ws.security.opt.crypto.dsig.internal.DigesterOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;



/**
 *
 * @author K.Venugopal@sun.com
 */
public class EnvelopedSignedMessageHeader implements SecurityHeaderElement,SecurityElementWriter{
    private Reference ref = null;
    private SecurityHeaderElement she = null;
    private StAXEXC14nCanonicalizerImpl stAXC14n = null;    
    private String id = "";
    private NamespaceContextEx nsContext = null;
    /** Creates a new instance of EnvelopedSignedMessageHeader */
    public EnvelopedSignedMessageHeader(SecurityHeaderElement she,Reference ref,JAXBSignatureHeaderElement jse,NamespaceContextEx nsContext) {
        this.she = she;
        this.ref = ref;
        //this.jse = jse;
        this.nsContext = nsContext;
        stAXC14n = new StAXEXC14nCanonicalizerImpl();
    }
    
    public boolean refersToSecHdrWithId(final String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(final String id) {
    }
    
    public String getNamespaceURI() {
        return she.getNamespaceURI();
    }
    
    public String getLocalPart() {
        return she.getLocalPart();
    }
    
    public XMLStreamReader readHeader() throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public byte[] canonicalize(final String algorithm, final List<AttributeNS> namespaceDecls) {
        throw new UnsupportedOperationException();
        
    }
    
    public boolean isCanonicalized() {
        throw new UnsupportedOperationException();
    }
    /**
     * writes the enveloped signed message header to an XMLStreamWriter
     * @param streamWriter XMLStreamWriter
     * @throws XMLStreamException
     */
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException{
        if(nsContext == null){
            throw new XMLStreamException("NamespaceContext is null in writeTo method");
        }
        
        Iterator<NamespaceContextEx.Binding> itr = nsContext.iterator();
        stAXC14n.reset();
        while(itr.hasNext()){
            final NamespaceContextEx.Binding nd = itr.next();
            stAXC14n.writeNamespace(nd.getPrefix(),nd.getNamespaceURI());
        }
        DigesterOutputStream dos= null;
        try{
            dos = ref.getDigestOutputStream();
        }catch(XMLSignatureException xse){
            throw new XMLStreamException(xse);
        }
        OutputStream os = new UnsyncBufferedOutputStream(dos);
        stAXC14n.setStream(os);
        //EnvelopedTransformWriter etw = new EnvelopedTransformWriter(streamWriter,stAXC14n,ref,jse,dos);
        ((SecurityElementWriter)she).writeTo(streamWriter);
    }
    /**
     *
     * @param streamWriter XMLStreamWriter
     * @param props HashMap
     * @throws XMLStreamException
     */
    public void writeTo(XMLStreamWriter streamWriter, HashMap props) throws XMLStreamException{
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    /**
     * 
     * @param os OutputStream
     */
    public void writeTo(OutputStream os){
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
