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


import com.sun.xml.ws.security.opt.crypto.dsig.Reference;
import com.sun.xml.ws.security.opt.crypto.dsig.internal.DigesterOutputStream;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class EnvelopedTransformWriter implements XMLStreamWriter{
    private StAXEXC14nCanonicalizerImpl stAXC14n = null;
    private JAXBSignatureHeaderElement signature = null;
    private Reference ref = null;
    private DigesterOutputStream dos = null;
    private int index = 0;
    private XMLStreamWriter writer = null;
    /** Creates a new instance of EnvelopedTransform */
    public EnvelopedTransformWriter(XMLStreamWriter writer,StAXEXC14nCanonicalizerImpl stAXC14n,Reference ref ,JAXBSignatureHeaderElement signature,DigesterOutputStream dos) {
        this.stAXC14n = stAXC14n;
        this.writer = writer;
        this.ref = ref;
        this.signature = signature;
        this.dos = dos;
    }
    
    public NamespaceContext getNamespaceContext() {
        return writer.getNamespaceContext();
    }
    
    public void close() throws XMLStreamException {
        //writer.close();
    }
    
    public void flush() throws XMLStreamException {
        writer.flush();
    }
    
    public void writeEndDocument() throws XMLStreamException {
        for(int i=0;i< index;i++){
            stAXC14n.writeEndElement();
            writer.writeEndElement();
        }
    }
    
    public void writeEndElement() throws XMLStreamException {
        if(index ==0){
            return;
        }
        --index;
        stAXC14n.writeEndElement();
        ref.setDigestValue(dos.getDigestValue());
        
        signature.sign();
        
        signature.writeTo(writer);
        writer.writeEndElement();
    }
    
    public void writeStartDocument() throws XMLStreamException {
        stAXC14n.writeStartDocument();
        writer.writeStartDocument();
    }
    
    public void writeCharacters(char[] c, int index, int len) throws XMLStreamException {
        stAXC14n.writeCharacters(c,index,len);
        writer.writeCharacters(c,index,len);
    }
    
    public void setDefaultNamespace(String string) throws XMLStreamException {
        writer.setDefaultNamespace(string);
        stAXC14n.setDefaultNamespace(string);
    }
    
    public void writeCData(String string) throws XMLStreamException {
        stAXC14n.writeCData(string);
        writer.writeCData(string);
    }
    
    public void writeCharacters(String string) throws XMLStreamException {
        stAXC14n.writeCharacters(string);
        writer.writeCharacters(string);
    }
    
    public void writeComment(String string) throws XMLStreamException {
        stAXC14n.writeComment(string);
        writer.writeComment(string);
    }
    
    public void writeDTD(String string) throws XMLStreamException {
        stAXC14n.writeDTD(string);
        writer.writeDTD(string);
    }
    
    public void writeDefaultNamespace(String string) throws XMLStreamException {
        stAXC14n.writeDefaultNamespace(string);
        writer.writeDefaultNamespace(string);
    }
    
    public void writeEmptyElement(String string) throws XMLStreamException {
        stAXC14n.writeEmptyElement(string);
        writer.writeEmptyElement(string);
    }
    
    public void writeEntityRef(String string) throws XMLStreamException {
        stAXC14n.writeEntityRef(string);
        writer.writeEntityRef(string);
    }
    
    public void writeProcessingInstruction(String string) throws XMLStreamException {
        stAXC14n.writeProcessingInstruction(string);
        writer.writeProcessingInstruction(string);
    }
    
    public void writeStartDocument(String string) throws XMLStreamException {
        stAXC14n.writeStartDocument(string);
        writer.writeStartDocument(string);
    }
    
    public void writeStartElement(String string) throws XMLStreamException {
        index++;
        stAXC14n.writeStartElement(string);
        writer.writeStartElement(string);
    }
    
    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        writer.setNamespaceContext(namespaceContext);
    }
    
    public Object getProperty(String string) throws IllegalArgumentException {
        return writer.getProperty(string);
    }
    
    public String getPrefix(String string) throws XMLStreamException {
        return writer.getPrefix(string);
    }
    
    public void setPrefix(String string, String string0) throws XMLStreamException {
        stAXC14n.setPrefix(string,string0);
        writer.setPrefix(string,string0);
    }
    
    public void writeAttribute(String string, String string0) throws XMLStreamException {
        stAXC14n.writeAttribute(string,string0);
        writer.writeAttribute(string,string0);
    }
    
    public void writeEmptyElement(String string, String string0) throws XMLStreamException {
        stAXC14n.writeEmptyElement(string,string0);
        writer.writeEmptyElement(string,string0);
    }
    
    public void writeNamespace(String string, String string0) throws XMLStreamException {
        stAXC14n.writeNamespace(string,string0);
        writer.writeNamespace(string,string0);
    }
    
    public void writeProcessingInstruction(String string, String string0) throws XMLStreamException {
        stAXC14n.writeProcessingInstruction(string,string0);
        writer.writeProcessingInstruction(string,string0);
    }
    
    public void writeStartDocument(String string, String string0) throws XMLStreamException {
        stAXC14n.writeStartDocument(string,string0);
        writer.writeStartDocument(string,string0);
    }
    
    public void writeStartElement(String string, String string0) throws XMLStreamException {
        index++;
        stAXC14n.writeStartElement(string,string0);
        writer.writeStartElement(string,string0);
    }
    
    public void writeAttribute(String string, String string0, String string1) throws XMLStreamException {
        stAXC14n.writeAttribute(string,string0,string1);
        writer.writeAttribute(string,string0,string1);
    }
    
    public void writeEmptyElement(String string, String string0, String string1) throws XMLStreamException {
        stAXC14n.writeEmptyElement(string,string0,string1);
        writer.writeEmptyElement(string,string0,string1);
    }
    
    public void writeStartElement(String string, String string0, String string1) throws XMLStreamException {
        index++;
        stAXC14n.writeStartElement(string,string0,string1);
        writer.writeStartElement(string,string0,string1);
    }
    
    public void writeAttribute(String string, String string0, String string1, String string2) throws XMLStreamException {
        stAXC14n.writeAttribute(string,string0,string1,string2);
        writer.writeAttribute(string,string0,string1,string2);
    }
    
}
